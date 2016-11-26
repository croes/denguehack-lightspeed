/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.lightspeed.demo.application.data.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import com.luciad.format.netcdf.TLcdNetCDFModelDecoder;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.weather.WeatherPanelFactory.WeatherPanel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Theme that displays weather data.
 */
public class WeatherTheme extends AbstractTheme {

  private static final String MODEL_ID = "model.id.weather";

  private static final String TEMPERATURE_LAYER_ID = "layer.id.weather.temperature";
  private static final String TEMPERATURE_CONTOUR_LAYER_ID = "layer.id.weather.temperature.contour";
  private static final String WIND_LAYER_ID = "layer.id.weather.wind";

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(WeatherTheme.class);

  protected final WeatherModel fModel;
  private WeatherPanel fWeatherPanel;
  protected final List<ILspLayer> fTemperatureLayers = new ArrayList<>();
  protected final List<ILspLayer> fTemperatureContourLayers = new ArrayList<>();
  protected final List<ILspLayer> fWindLayers = new ArrayList<>();

  public WeatherTheme() {
    setName("Weather");
    setCategory("Tracks");
    loadRequiredClassForQuickFail(TLcdNetCDFModelDecoder.class);
    fModel = (WeatherModel) Framework.getInstance().getModelWithID(MODEL_ID);
    if (fModel == null) {
      sLogger.error("Weather model could not be created.");
    }
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();

    List<ILspLayer> allLayers = new ArrayList<>();

    for (ILspView view : aViews) {
      ILspLayer temperatureLayer = new TemperatureLayerFactory(fModel.getContourModel().getColorMap()).createLayer(fModel.getTemperatureModel());
      ILspLayer contourLayer = new TemperatureContourLayerFactory().createLayer(fModel.getContourModel());
      ILspLayer windLayer = new WindLayerFactory().createLayer(fModel.getWindModel());

      allLayers.add(temperatureLayer);
      allLayers.add(contourLayer);
      allLayers.add(windLayer);

      fTemperatureLayers.add(temperatureLayer);
      fTemperatureContourLayers.add(contourLayer);
      fWindLayers.add(windLayer);

      view.addLayer(temperatureLayer);
      view.addLayer(contourLayer);
      view.addLayer(windLayer);

      // We register the layers with the framework so that they will
      // also show up in the layer control panel (if we don't, they
      // will only be implicitly available by activating the theme)
      framework.registerLayers(TEMPERATURE_LAYER_ID, view, Collections.singletonList(temperatureLayer));
      framework.registerLayers(TEMPERATURE_CONTOUR_LAYER_ID, view, Collections.singletonList(contourLayer));
      framework.registerLayers(WIND_LAYER_ID, view, Collections.singletonList(windLayer));
    }

    return allLayers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    WeatherPanelFactory weatherPanelFactory = new WeatherPanelFactory(fModel, fTemperatureLayers, fTemperatureContourLayers, fWindLayers);
    fWeatherPanel = weatherPanelFactory.createPanel(this);
    List<JPanel> result = new ArrayList<>();
    if (fWeatherPanel != null) {
      result.add(fWeatherPanel.getPanel());
    }
    return result;
  }

  @Override
  public void activate() {
    super.activate();
    for (ILspLayer layer : fWindLayers) {
     layer.setVisible(false);
    }
  }

  @Override
  public void deactivate() {
    if (fWeatherPanel != null) {
      fWeatherPanel.deactivate();
    }
    super.deactivate();
  }

}
