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
package samples.decoder.netcdf.gxy;

import java.awt.Color;
import java.io.IOException;

import com.luciad.earth.view.gxy.util.ALcdEarth2DVectorIcon;
import com.luciad.earth.view.gxy.util.TLcdEarthWindIcon;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.format.raster.TLcdNumericParameterizedIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdConstant;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

import samples.common.HaloLabel;
import samples.decoder.netcdf.NetCDFModelFactory;
import samples.decoder.netcdf.gxy.custom.CustomNetCDFLayerFactory;
import samples.earth.util.ConvertParameterIcon;
import samples.earth.util.WeatherUtil;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates how to decode NetCDF data and visualize it in a GXY view.
 * It includes filtering based on time and vertical dimensions
 */
public class MainPanel extends samples.gxy.decoder.MainPanel {

  @Override
  protected void addData() throws IOException {
    super.addData();

    NetCDFModelFactory netCDFModelFactory = new NetCDFModelFactory("");

    // Use a custom layer factory for the default data. This custom layer factory adjusts the color map of one of
    // the layers and makes sure all layers are visible.
    CustomNetCDFLayerFactory customLayerFactory = new CustomNetCDFLayerFactory();

    // Add mediterranean data
    ILcdModel model = netCDFModelFactory.createMedModel();
    ILcdGXYLayer layer = customLayerFactory.createGXYLayer(model);
    GXYLayerUtil.addGXYLayer(getView(), layer);

    // Load the data and add it to the view
    model = netCDFModelFactory.createModel();
    layer = customLayerFactory.createGXYLayer(model);
    GXYLayerUtil.addGXYLayer(getView(), layer);

    // Add a wind layer that is displayed using a grid of wind barb icons
    addWindIconLayer(netCDFModelFactory);

    // Add a temperature layer that is displayed using a grid of numeric icons
    addTemperatureIconLayer(netCDFModelFactory);

    GXYLayerUtil.fitGXYLayer(getView(), layer);
  }

  private void addWindIconLayer(NetCDFModelFactory aNetCDFModelFactory) throws IOException {
    // Create a wind icon. TLcdEarthWindIcon accepts knots as parameters, so we need to convert meters to knots.
    // We also need to invert the wind barb direction since TLcdEarthWindIcon paints the icon in the direction that
    // the wind is blowing from.
    double toKnots = 3.600 / TLcdConstant.NM2KM; // m/s to knots
    ILcdParameterizedIcon windIcon = new TLcdEarthWindIcon(ALcdEarth2DVectorIcon.Parameterization.STRENGTH_2D);
    windIcon = new ConvertParameterIcon(windIcon, new double[]{-toKnots, -toKnots}, null);

    // Create a wind layer that is visualized using a wind icon
    ILcdModel windModel = aNetCDFModelFactory.createWindModel();
    CustomNetCDFLayerFactory windLayerFactory = new CustomNetCDFLayerFactory(windIcon, 75, new Color(180, 180, 180, 100), 1);
    ILcdGXYLayer windLayer = windLayerFactory.createGXYLayer(windModel);
    GXYLayerUtil.addGXYLayer(getView(), new TLcdGXYAsynchronousLayerWrapper(windLayer));
  }

  private void addTemperatureIconLayer(NetCDFModelFactory aNetCDFModelFactory) throws IOException {
    // Create a numeric icon that converts the values (which are expressed in Kelvin) to degrees Celsius.
    TLcdNumericParameterizedIcon numericIcon = new TLcdNumericParameterizedIcon();
    numericIcon.setFormat(WeatherUtil.createDecimalFormat("°C"));
    ILcdParameterizedIcon degreesNumericIcon = new ConvertParameterIcon(numericIcon, null, new double[]{-273.15});

    // Create the temperature layer
    CustomNetCDFLayerFactory temperatureLayerFactory = new CustomNetCDFLayerFactory(degreesNumericIcon, 225, new Color(255, 255, 255, 255), 1);
    ILcdModel temperatureModel = aNetCDFModelFactory.createTemperatureModel();
    ILcdGXYLayer temperatureLayer = temperatureLayerFactory.createGXYLayer(temperatureModel);
    GXYLayerUtil.addGXYLayer(getView(), temperatureLayer);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    HaloLabel label = new HaloLabel("Generated using MyOcean Products", 12, true);
    getOverlayPanel().add(label, TLcdOverlayLayout.Location.SOUTH_WEST);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding NetCDF");
  }
}
