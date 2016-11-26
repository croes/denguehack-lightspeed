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
package samples.lightspeed.imaging.multispectral.curves;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditHandleStyler;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.editor.TLsp2DPointListEditor;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.model.CartesianReference;

/**
 * Wrapper for the view on which the curves and histogram of an image are to be visualized.
 */
public class HistogramPanel implements ILcdDisposable {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger("samples.lightspeed.imaging");

  private ILspAWTView fView;
  private final TLspViewNavigationUtil fViewNavigationUtil;
  // model containing the curves
  private final TLcdVectorModel fCurvesModel = new TLcdVectorModel();
  // model containing the shapes representing the histogram
  private final TLcdVectorModel fHistogramModel = new TLcdVectorModel();

  private static final Color VISUAL_AID_LINE_COLOR = new Color(200, 200, 200);
  private static final Color VISUAL_AID_LINE_FOCUS_COLOR = new Color(255, 255, 255);
  private static final Color VIEW_BACKGROUND_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.9f);
  private final ILspLayer fCurvesLayer;
  private final ILspLayer fHistogramLayer;

  private List<ILspLayer> fLayers = new ArrayList<>();

  public HistogramPanel(int aViewSize) {
    fCurvesModel.setModelReference(CartesianReference.getInstance());
    fHistogramModel.setModelReference(CartesianReference.getInstance());

    //view in which the curves and the histogram will be visualizes.
    fView = TLspViewBuilder.newBuilder()
                           .size(aViewSize, aViewSize)
                           .addAtmosphere(false)
                           .viewType(ILspView.ViewType.VIEW_2D)
                           .worldReference(CartesianReference.getInstance())
                           .background(VIEW_BACKGROUND_COLOR)
                           .overlayComponents(false)
                           .buildSwingView();
    fViewNavigationUtil = new TLspViewNavigationUtil(fView);
    fViewNavigationUtil.setFitMargin(0.0);

    //edit controller to be able to modify the curves
    TLspEditController editController = new TLspEditController();
    ((TLspEditHandleStyler) editController.getHandleStyler()).setStyles(
        TLspHandleGeometryType.VISUAL_AID_LINE,
        TLspLineStyle.newBuilder().color(VISUAL_AID_LINE_COLOR).build()
    );
    ((TLspEditHandleStyler) editController.getFocusHandleStyler()).setStyles(
        TLspHandleGeometryType.VISUAL_AID_LINE,
        TLspLineStyle.newBuilder().color(VISUAL_AID_LINE_FOCUS_COLOR).build()
    );
    editController.setSnapperProvider(null);
    editController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().keyEvents().build());

    //default select controller.
    TLspSelectController selectController = new TLspSelectController();
    selectController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());
    editController.appendController(selectController);
    fView.setController(editController);

    // painter to visualize the curves
    TLspShapePainter curvesPainter = new TLspShapePainter();
    curvesPainter.setShapeDiscretizer(new CatmullRomDiscretizer());
    CurvesStyler styler = new CurvesStyler();
    curvesPainter.setStyler(TLspPaintState.REGULAR, styler);
    curvesPainter.setStyler(TLspPaintState.SELECTED, styler);
    curvesPainter.setStyler(TLspPaintState.EDITED, styler);
    // create a layer for the curves
    fCurvesLayer = TLspShapeLayerBuilder.newBuilder()
                                        .model(fCurvesModel)
                                        .bodyPainter(curvesPainter)
                                        .bodyEditable(true)
                                        .bodyEditor(new TLsp2DPointListEditor())
                                        .build();

    // create a layer for the histograms
    fHistogramLayer = TLspShapeLayerBuilder.newBuilder()
                                           .model(fHistogramModel)
                                           .selectable(false)
                                           .bodyEditable(false)
                                           .bodyStyler(TLspPaintState.REGULAR, TLspFillStyle.newBuilder()
                                                                                            .color(Color.WHITE)
                                                                                            .opacity(0.7f)
                                                                                            .build())
                                           .build();

    fView.addLayer(fHistogramLayer);
    fView.addLayer(fCurvesLayer);
    fit();
  }

  public void fitOnCurves() {
    try {
      fViewNavigationUtil.fit(fCurvesLayer);
    } catch (Exception e) {
      sLogger.error("Could not fit on curves layer", e);
    }
  }

  public void fit() {
    try {
      fViewNavigationUtil.fitOnModelBounds(new TLcdXYBounds(0, 0, 1, 1), fHistogramModel.getModelReference());
    } catch (Exception e) {
      sLogger.error("Could not fit", e);
    }
  }

  public Component getComponent() {
    return fView.getHostComponent();
  }

  public TLcdVectorModel getCurvesModel() {
    return fCurvesModel;
  }

  public TLcdVectorModel getHistogramModel() {
    return fHistogramModel;
  }

  public void setCurvesEditable(boolean aEditable) {
    fCurvesLayer.setEditable(aEditable);
  }

  public void setCurveOnCurveLayer(CatmullRomEditLine aCurve) {
    fCurvesModel.removeAllElements(ILcdModel.FIRE_LATER);
    fCurvesModel.addElement(aCurve, ILcdModel.FIRE_NOW);
  }

  /**
   * Disables or enables the view
   *
   * @param aEnabled {@code true} if the view should be enabled
   */
  public void setEnabled(boolean aEnabled) {
    if (!aEnabled) {
      for (int i = 0; i < fView.layerCount(); i++) {
        fLayers.add(fView.getLayer(i));
      }
      fView.removeAllLayers();
    } else {
      for (ILspLayer layer : fLayers) {
        fView.addLayer(layer);
      }
      fLayers.clear();
    }
  }

  @Override
  public void dispose() {
    if (fView != null) {
      fView.removeAllLayers();
      fView.destroy();
      fView = null;
    }
  }
}
