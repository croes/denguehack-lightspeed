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
package samples.hana.lightspeed;

import static java.util.Arrays.asList;

import static com.luciad.view.lightspeed.layer.TLspPaintRepresentation.BODY;
import static com.luciad.view.lightspeed.layer.TLspPaintRepresentation.LABEL;
import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;
import static com.luciad.view.lightspeed.layer.TLspPaintState.SELECTED;

import static samples.hana.lightspeed.common.Configuration.getScaleRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.common.SampleData;
import samples.hana.lightspeed.common.Configuration;
import samples.hana.lightspeed.model.StormsFilter;
import samples.hana.lightspeed.model.StormsModel;
import samples.hana.lightspeed.statistics.StatisticsProvider;
import samples.hana.lightspeed.styling.SelectionStyler;
import samples.hana.lightspeed.styling.StatisticsBodyStyler;
import samples.hana.lightspeed.styling.StatisticsLabelStyler;
import samples.hana.lightspeed.styling.StormStyler;
import samples.hana.lightspeed.ui.CustomerStatisticsTooltip;
import samples.hana.lightspeed.ui.TooltipMouseListener;
import samples.lightspeed.common.LspDataUtil;

/**
 * Creates the various layers in this sample.
 */
public class LayersManager {

  private final List<ILspLayer> fLayers;
  private final ILspAWTView fView;
  private final StormsFilter fStormsFilter;

  public LayersManager(ILspAWTView aView, StormsFilter aStormsFilter) {
    fView = aView;
    fLayers = new ArrayList<ILspLayer>();
    fStormsFilter = aStormsFilter;
  }

  public void addBackgroundLayers() {
    try {
      if (Configuration.is("layer.world.visible")) {
        ILspLayer worldLayer = LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().getLayer();
        worldLayer.setLabel("World");
        fLayers.add(worldLayer);
        fView.addLayer(worldLayer);
      }

      if (Configuration.is("layer.cities.visible")) {
        ILcdModel cities = new TLcdSHPModelDecoder().decode("Data/Shp/Usa/city_125.shp");
        ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                               .model(cities)
                                               .label("cities")
                                               .bodyStyler(REGULAR, TLspIconStyle.newBuilder().icon(TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON)).build())
                                               .labelStyler(REGULAR, TLspLabelStyler.newBuilder().build())
                                               .selectable(false)
                                               .build();
        layer.setVisible(false);
        fView.addLayer(layer);
        fLayers.add(layer);
      }

      if (Configuration.is("layer.grid.visible")) {
        ILspLayer gridLayer = LspDataUtil.instance().grid().getLayer();
        gridLayer.setLabel("Grid");
        fLayers.add(gridLayer);
        fView.addLayer(gridLayer);
      }

    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public void addCustomersLayers(ILcdModel aCustomersModel, ILspStyler aCustomerStyler, StatisticsProvider aStatisticsProvider) {
    StatisticsBodyStyler stateStyler = new StatisticsBodyStyler(aStatisticsProvider);
    StatisticsBodyStyler countiesStyler = new StatisticsBodyStyler(aStatisticsProvider);
    StatisticsLabelStyler labelStyler = new StatisticsLabelStyler(aStatisticsProvider);

    aStatisticsProvider.addChangeListener(stateStyler);
    aStatisticsProvider.addChangeListener(countiesStyler);
    aStatisticsProvider.addChangeListener(labelStyler);

    if (Configuration.is("layer.states.visible")) {
      ILcdModel model = LspDataUtil.instance().model(SampleData.US_STATES).getModel();
      ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                             .model(model)
                                             .bodyStyler(REGULAR, stateStyler)
                                             .bodyStyler(SELECTED, new SelectionStyler(stateStyler))
                                             .labelStyler(REGULAR, labelStyler)
                                             .bodyScaleRange(getScaleRange("layer.states", BODY))
                                             .labelScaleRange(getScaleRange("layer.states", LABEL))
                                             .build();
      layer.setVisible(false);
      fView.addLayer(layer);
      fLayers.add(layer);
    }

    if (Configuration.is("layer.counties.visible")) {
      ILcdModel model = LspDataUtil.instance().model(SampleData.US_COUNTIES).getModel();
      ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                             .model(model)
                                             .bodyStyler(REGULAR, countiesStyler)
                                             .bodyStyler(SELECTED, new SelectionStyler(stateStyler))
                                             .labelStyler(REGULAR, labelStyler)
                                             .bodyScaleRange(getScaleRange("layer.counties", BODY))
                                             .labelScaleRange(getScaleRange("layer.counties", LABEL))
                                             .build();
      layer.setVisible(false);
      fView.addLayer(layer);
      fLayers.add(layer);
    }

    if (Configuration.is("layer.customer.visible")) {
      System.setProperty(TLspPlotLayerBuilder.class.getName() + ".maxObjectsPerNode", Configuration.get("layer.customer.maxObjectsPerNode"));
      System.setProperty(TLspPlotLayerBuilder.class.getName() + ".maxCachedObjects", Configuration.get("layer.customer.maxCachedObjects"));
      ILspLayer layer = TLspPlotLayerBuilder.newBuilder()
                                            .model(aCustomersModel)
                                            .bodyStyler(REGULAR, aCustomerStyler)
                                            .bodyScaleRange(getScaleRange("layer.customer", BODY))
                                            .labelScaleRange(getScaleRange("layer.customer", LABEL))
                                            .build();

      List<TooltipMouseListener.TooltipLogic> logicList = Collections.singletonList((TooltipMouseListener.TooltipLogic) new CustomerStatisticsTooltip());
      TooltipMouseListener listener = new TooltipMouseListener(fView, Collections.singletonList(layer), logicList);
      fView.getHostComponent().addMouseMotionListener(listener);
      fView.getHostComponent().addMouseListener(listener);

      layer.setVisible(false);
      fView.addLayer(layer);
      fView.moveLayerAt(fView.layerCount() - 1, layer);
      fLayers.add(layer);
    }

    moveStormLayerToTheTop();
  }

  private void moveStormLayerToTheTop() {
    for (ILspLayer layer : fLayers) {
      if (layer.getLabel().toLowerCase().contains("storm")) {
        fView.moveLayerAt(fView.layerCount() - 1, layer);
      }
    }
  }

  public void addStormsLayers(StormsModel aStormsModel) {
    if (Configuration.is("layer.storms.visible")) {
      final ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                   .model(aStormsModel)
                                                   .label("Storms")
                                                   .filter(fStormsFilter)
                                                   .bodyStyler(REGULAR, new StormStyler())
                                                   .bodyScaleRange(getScaleRange("layer.storms", BODY))
                                                   .labelScaleRange(getScaleRange("layer.storms", LABEL))
                                                   .selectable(false)
                                                   .build();

      layer.setVisible(false);
      fView.addLayer(layer);
      fLayers.add(layer);
    }
  }

  public void setVisible(String... aLayers) {
    Set<String> layers = new HashSet<String>(asList(aLayers));
    layers.add("world");
    layers.add("grid");
    layers.add("storm");
    for (ILspLayer layer : fLayers) {
      boolean visible = false;
      for (String match : layers) {
        if (layer.getLabel().toLowerCase().contains(match)) {
          visible = true;
        }
      }
      layer.setVisible(visible);
    }
  }
}
