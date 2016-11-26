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
package samples.gxy.touch.editing;

import java.awt.Color;

import javax.swing.JPanel;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.controller.ILcdGXYChainableController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNavigateController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchSelectEditController;

import samples.common.SampleData;
import samples.common.action.ShowPopupAction;
import samples.common.model.GeodeticModelFactory;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.actions.CreateBufferAction;
import samples.gxy.editing.actions.RemoveCurveAction;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.NewCompositeCurveControllerModel;
import samples.gxy.editing.controllers.NewShapeControllerModel;
import samples.gxy.touch.GXYTouchSample;

/**
 * Version of the {@link samples.gxy.editing.MainPanel} sample that is more
 * suited for use with touch devices.
 * It uses a larger GUI (with overlay icons) and creates TLcdGXYTouchNewController instances
 * instead of TLcdGXYNewController2 instances.
 */
public class MainPanel extends GXYTouchSample {

  private final ControllerSettingsNotifier fControllerSettingsNotifier = new ControllerSettingsNotifier();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-85.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected TouchToolBar createTouchToolBar(boolean aTouchSupported) {
    TouchToolBar toolBar = super.createTouchToolBar(aTouchSupported);
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 16, Color.blue);

    // Set the edit controller as initial controller.
    TLcdGXYTouchSelectEditController editController = toolBar.getEditController();
    fControllerSettingsNotifier.setView(getView());
    addTouchAndHoldMenu(editController);
    getView().setGXYController(toolBar.getWrappedController(editController));

    // Add specific touch controllers to create new shapes.
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.TEXT, fControllerSettingsNotifier),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON), "Create a text");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POINT, fControllerSettingsNotifier), icon,
                        "Create a point");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.BOUNDS, fControllerSettingsNotifier),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON), "Create a bounds");
    toolBar.addSpace();
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYLINE, fControllerSettingsNotifier),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), "Create a polyline");
    addGXYNewController(toolBar,
                        new NewShapeControllerModel(NewShapeControllerModel.ShapeType.RHUMBPOLYLINE, fControllerSettingsNotifier),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_RHUMBLINE_POLYLINE_ICON), "Create a rhumb polyline");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYGON, fControllerSettingsNotifier),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), "Create a polygon");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.RHUMBPOLYGON, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_RHUMBLINE_POLYGON_ICON), "Create a rhumb polygon");
    toolBar.addSpace();
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.CIRCLE, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON), "Create a circle");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ELLIPSE, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON), "Create an ellipse");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.CIRCLE_BY_3_POINTS, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_3_POINTS_ICON), "Create a circle-by-3-points");
    toolBar.addSpace();
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ARC, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON), "Create an elliptical arc");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ARCBY3POINTS, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON), "Create an arc-by-3-points");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ARCBYBULGE, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON), "Create an arc-by-bulge");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ARCBYCENTERPOINT, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_CENTERPOINT_ICON), "Create an arc-by-center-point");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.ARCBAND, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON), "Create an arcband");
    toolBar.addSpace();
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.GEO_BUFFER_POLYLINE, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), "Create a polyline geobuffer");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.GEO_BUFFER_POLYGON, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_BUFFER_ICON), "Create a polygon geobuffer");
    addGXYNewController(toolBar, new NewShapeControllerModel(NewShapeControllerModel.ShapeType.VARIABLE_GEO_BUFFER, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.DRAW_VARIABLE_BUFFER_ICON), "Create a buffer with varying width and height");
    toolBar.addSpace();
    addGXYNewController(toolBar, new NewCompositeCurveControllerModel(NewShapeControllerModel.ShapeType.COMPOSITE_CURVE, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_NON_CLOSED_ICON), "Create a composite curve");
    addGXYNewController(toolBar, new NewCompositeCurveControllerModel(NewShapeControllerModel.ShapeType.COMPOSITE_RING, fControllerSettingsNotifier), TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_CLOSED_ICON), "Create a composite ring");
    return toolBar;
  }

  @Override
  protected JPanel createSettingsPanel() {
    return samples.gxy.editing.MainPanel.createCompositeCurveTypePanel(fControllerSettingsNotifier);
  }

  private void addTouchAndHoldMenu(TLcdGXYTouchSelectEditController aController) {
    ILcdAction[] rightClickActions = {
        new TLcdDeleteSelectionAction(getView()),
        new CreateBufferAction(getView()),
        new RemoveCurveAction(getView())
    };
    ShowPopupAction showPopupAction
        = new ShowPopupAction(rightClickActions, getView());
    aController.setPostTouchAndHoldAction(showPopupAction);
  }

  /**
   * Loads the background data and adds the layer in which
   * the newly created shapes will be added, on top of the
   * background data.
   */
  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();
    ILcdGXYLayer lonLatLayer = factory.createGXYLayer(new GeodeticModelFactory().createSimpleModel());

    // Configure a (larger) snap icon.
    TLcdGXYShapePainter painterEditorProvider = ShapeGXYLayerFactory.retrieveGXYPainterEditor(lonLatLayer);
    if (painterEditorProvider != null) {
      painterEditorProvider.setSnapIcon(new TLcdSymbol(TLcdSymbol.CROSS_RECT, 40, Color.magenta));
    }

    getTouchToolBar().getSnappables().getSnappableLayers().add(lonLatLayer);

    // Adds the layer to the map, just beneath the grid layer.
    GXYLayerUtil.addGXYLayer(getView(), lonLatLayer, true, false);
  }

  private ILcdGXYChainableController addGXYNewController(TouchToolBar aToolbarSW,
                                                         NewShapeControllerModel aModel,
                                                         ILcdIcon aShapeIcon,
                                                         String aDescription) {
    TLcdGXYTouchNewController new_controller = new TouchNewController(aModel, getOverlayPanel());
    new_controller.setIcon(aShapeIcon);                 // Set the icon
    new_controller.setShortDescription(aDescription);   // Set the short descriptions
    new_controller.setName("");                         // Set the name
    new_controller.setActionToTriggerAfterCommit(       // Switch to edit mode after creating the ILcdShape
      new TLcdGXYSetControllerAction(getView(), aToolbarSW.getWrappedController(aToolbarSW.getEditController()))
    );

    new_controller.setSnappables(aToolbarSW.getSnappables());

    //Add pan functionality.
    new_controller.appendGXYController(new TLcdGXYTouchNavigateController());

    // add the controller to the toolbar
    aToolbarSW.addGXYController(new_controller);
    return new_controller;
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Create and edit by touch");
  }
}
