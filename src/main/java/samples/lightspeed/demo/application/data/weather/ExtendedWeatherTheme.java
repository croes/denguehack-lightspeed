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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import com.luciad.format.object3d.TLcd3DPrimitiveType;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.shape.ILcdShape;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationMode;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.ALspStyle;

import samples.lightspeed.demo.application.data.weather.ExtendedWeatherPanelFactory.ExtendedWeatherPanel;
import samples.lightspeed.demo.application.data.weather.IcingModel.IcingSLDModel;
import samples.lightspeed.demo.application.data.weather.IcingModel.IcingSeverityModel;
import samples.lightspeed.demo.application.data.weather.IcingSLDExtrudedContourLayerFactory.ExtrudedIcingSLDContourStyler;
import samples.lightspeed.demo.application.data.weather.IcingSeverityExtrudedContourLayerFactory.ExtrudedIcingSeverityContourStyler;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.meshencoder.Mesh;

/**
 * Extends the weather theme with 3D contours.
 */
public class ExtendedWeatherTheme extends WeatherTheme {

  private static final String ICING_SEVERITY_LAYER_ID = "layer.id.weather.icing.severity";
  private static final String ICING_SEVERITY_EXTRUDED_CONTOUR_LAYER_ID = "layer.id.weather.icing.severity.extruded.contour";
  private static final String ICING_SLD_LAYER_ID = "layer.id.weather.icing.sld";
  private static final String ICING_SLD_LAYER_EXTRUDED_CONTOUR_ID = "layer.id.weather.icing.sld.extruded.contour";
  private static final String ICING_MODEL_ID = "model.id.weather.icing";

  private final IcingModel fIcingModel;

  private ExtendedWeatherPanel fExtendedWeatherPanel;
  private final List<ILspLayer> fIcingSeverityLayers = new ArrayList<>();
  private final List<ILspLayer> fIcingSeverityExtrudedContourLayers = new ArrayList<>();
  private final List<ILspLayer> fIcingSLDLayers = new ArrayList<>();
  private final List<ILspLayer> fIcingSLDExtrudedContourLayers = new ArrayList<>();
  private final Set<CachingExtrudedShapeDiscretizer> fCachingExtrudedShapeDiscretizers = new HashSet<>();

  public ExtendedWeatherTheme() {
    fIcingModel = (IcingModel) Framework.getInstance().getModelWithID(ICING_MODEL_ID);
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();

    List<ILspLayer> layers = new ArrayList<>();
    layers.addAll(super.createLayers(aViews));
    for (ILspView view : aViews) {
      ILspLayer icingSeverityLayer = new IcingSeverityContourLayerFactory().createLayer(fIcingModel);
      layers.add(icingSeverityLayer);
      fIcingSeverityLayers.add(icingSeverityLayer);
      view.addLayer(icingSeverityLayer);
      framework.registerLayers(ICING_SEVERITY_LAYER_ID, view, Collections.singletonList(icingSeverityLayer));

      ILspLayer icingSeverityExtrudedContourLayer = new IcingSeverityExtrudedContourLayerFactory().createLayer(fIcingModel);
      layers.add(icingSeverityExtrudedContourLayer);
      fIcingSeverityExtrudedContourLayers.add(icingSeverityExtrudedContourLayer);
      view.addLayer(icingSeverityExtrudedContourLayer);
      framework.registerLayers(ICING_SEVERITY_EXTRUDED_CONTOUR_LAYER_ID, view, Collections.singletonList(icingSeverityExtrudedContourLayer));

      ILspLayer icingSLDLayer = new IcingSLDContourLayerFactory().createLayer(fIcingModel);
      layers.add(icingSLDLayer);
      fIcingSLDLayers.add(icingSLDLayer);
      view.addLayer(icingSLDLayer);
      framework.registerLayers(ICING_SLD_LAYER_ID, view, Collections.singletonList(icingSLDLayer));

      ILspLayer icingSLDExtrudedContourLayer = new IcingSLDExtrudedContourLayerFactory().createLayer(fIcingModel);
      layers.add(icingSLDExtrudedContourLayer);
      fIcingSLDExtrudedContourLayers.add(icingSLDExtrudedContourLayer);
      view.addLayer(icingSLDExtrudedContourLayer);
      framework.registerLayers(ICING_SLD_LAYER_EXTRUDED_CONTOUR_ID, view, Collections.singletonList(icingSLDExtrudedContourLayer));
    }

    return layers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    ExtendedWeatherPanelFactory extendedWeatherPanelFactory = new ExtendedWeatherPanelFactory(fModel, fTemperatureLayers, fTemperatureContourLayers, fWindLayers,
                                                                                              fIcingModel, fIcingSeverityLayers, fIcingSeverityExtrudedContourLayers,
                                                                                              fIcingSLDLayers, fIcingSLDExtrudedContourLayers);
    fExtendedWeatherPanel = extendedWeatherPanelFactory.createPanel(this);
    List<JPanel> result = new ArrayList<>();
    if (fExtendedWeatherPanel != null) {
      result.add(fExtendedWeatherPanel.getPanel());
    }
    return result;
  }

  @Override
  public void activate() {
    super.activate();
    for (ILspLayer layer : fIcingSeverityLayers) {
      layer.setVisible(false);
    }
    for (ILspLayer layer : fIcingSeverityExtrudedContourLayers) {
      layer.setVisible(false);
    }
    for (ILspLayer layer : fIcingSLDLayers) {
      layer.setVisible(false);
    }
    for (ILspLayer layer : fIcingSLDExtrudedContourLayers) {
      layer.setVisible(false);
    }
    startDiscretizingExtrudedContours();//Not strictly necessary.  Improves user experience because the discretization takes a little while.
  }

  private void startDiscretizingExtrudedContours() {
    Runnable runnable = createRunnableToStartDiscretizingExtrudedContours();
    new Thread(runnable).start();
  }

  private Runnable createRunnableToStartDiscretizingExtrudedContours() {
    return new Runnable() {

        public void run() {
          ILspView view = get3DView();
          Collection<ILspLayer> layers = getExtrudedContourLayers(view);
          if (view != null && !layers.isEmpty()) {
            for (ILspLayer layer : layers) {
              CachingExtrudedShapeDiscretizer shapeDiscretizer = getShapeDiscretizer(layer);
              if (shapeDiscretizer != null) {
                fCachingExtrudedShapeDiscretizers.add(shapeDiscretizer);
                TLspContext context = new TLspContext(layer, view);
                if (layer.getModel() instanceof IcingSeverityModel) {
                  Enumeration<IcingSeverityContour> icingContours = layer.getModel().elements();
                  while (icingContours.hasMoreElements()) {
                    IcingSeverityContour icingSeverityContour = icingContours.nextElement();
                    TLspShapeDiscretizationParameters params = getParams(view, layer, ExtrudedIcingSeverityContourStyler.createStyles(icingSeverityContour));
                    discretize(shapeDiscretizer, ExtrudedIcingSeverityContourStyler.createExtrudedShape(fIcingModel, icingSeverityContour), params, context);
                  }
                } else if (layer.getModel() instanceof IcingSLDModel) {
                  Enumeration<IntervalContour> sldContours = layer.getModel().elements();
                  while (sldContours.hasMoreElements()) {
                    IntervalContour icingSLDContour = sldContours.nextElement();
                    TLspShapeDiscretizationParameters params = getParams(view, layer, ExtrudedIcingSLDContourStyler.createStyles());
                    discretize(shapeDiscretizer, ExtrudedIcingSLDContourStyler.createExtrudedShape(fIcingModel, icingSLDContour), params, context);
                  }
                }
              }
            }
          }
        }

      private void discretize(CachingExtrudedShapeDiscretizer aShapeDiscretizer, ILcdShape aShape, TLspShapeDiscretizationParameters aParams, TLspContext aContext) {
        try {
          aShapeDiscretizer.discretizeSFCT(aShape, aParams, aContext, new Mesh());
        } catch (TLspDiscretizationException ignored) {
        }
      }

      private TLspShapeDiscretizationParameters getParams(ILspView aView, ILspLayer aLayer, List<ALspStyle> styles) {
          return new TLspShapeDiscretizationParameters.Builder()
              .allowPrimitiveType(TLcd3DPrimitiveType.TRIANGLES)
              .allowPrimitiveType(TLcd3DPrimitiveType.LINES)
              .maximalEdgeLength(500000)
              .maximalFillEdgeLength(0)
              .modes(TLspShapeDiscretizationMode.INTERIOR, TLspShapeDiscretizationMode.OUTLINE)
              .styles(styles)
              .modelXYZWorldTransformation(aLayer.getModelXYZWorldTransformation(aView))
              .build();
        }

      };
  }

  @Override
  public void deactivate() {
    if (fExtendedWeatherPanel != null) {
      fExtendedWeatherPanel.deactivate();
    }
    CachingShapeDiscretizer.clear();
    for (CachingExtrudedShapeDiscretizer discretizer : fCachingExtrudedShapeDiscretizers) {
      discretizer.clear();
    }

    super.deactivate();
  }

  private static ILspView get3DView() {
    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    for (ILspView view : views) {
      if (view.getXYZWorldReference() instanceof ILcdGeocentricReference) {
        return view;
      }
    }
    return null;
  }

  private Collection<ILspLayer> getExtrudedContourLayers(ILspView aView) {
    Collection<ILspLayer> result = new ArrayList<>();
    if (aView == null) {
      return result;
    }
    for (int i = 0; i < aView.layerCount(); i++) {
      ILspLayer layer = aView.getLayer(i);
      if (fIcingSeverityExtrudedContourLayers.contains(layer)
          || fIcingSLDExtrudedContourLayers.contains(layer)) {
        result.add(layer);
      }
    }
    return result;
  }

  private static CachingExtrudedShapeDiscretizer getShapeDiscretizer(ILspLayer aLayer) {
    if (aLayer instanceof ILspInteractivePaintableLayer) {
      ILspInteractivePaintableLayer paintableLayer = (ILspInteractivePaintableLayer) aLayer;
      ILspPainter painter = paintableLayer.getPainter(TLspPaintRepresentation.BODY);
      if (painter instanceof TLspShapePainter) {
        TLspShapePainter shapePainter = (TLspShapePainter) painter;
        if (shapePainter.getShapeDiscretizer() instanceof CachingExtrudedShapeDiscretizer) {
          return (CachingExtrudedShapeDiscretizer) shapePainter.getShapeDiscretizer();
        }
      }
    }

    return null;
  }

}
