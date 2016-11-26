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
package samples.geometry.constructive;

import java.awt.Color;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ALcdGXYPen;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.action.ShowPopupAction;
import samples.gxy.common.GXYSample;
import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.common.controller.SnappablesSubsetList;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.NewShapeControllerModel;

/**
 * This sample demonstrates convex hull and constructive geometry operations on shapes.
 */
public class MainPanel extends GXYSample {

  private SnappablesSubsetList fSnappables;
  private TLcdGXYEditController2 fEditController;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-85.00, 25.00, 20.00, 15.00);
  }

  protected void createGUI() {
    super.createGUI();
    ToolBar toolBar = getToolBars()[0];
    fSnappables = toolBar.getSnappables();
    fEditController = toolBar.getGXYControllerEdit();

    // Adds controllers to create new shapes.
    ControllerSettingsNotifier notifier = new ControllerSettingsNotifier(getView());
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POINT, notifier),
                                     new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 16, Color.blue), "Create a point",
                                     toolBar);
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYLINE, notifier),
                                     TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), "Create a polyline", toolBar);
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYGON, notifier),
                                     TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), "Create a polygon", toolBar);
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.GEO_BUFFER_POLYLINE, notifier),
                                     TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), "Create a polyline geobuffer", toolBar);
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.GEO_BUFFER_POLYGON, notifier),
                                     TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_BUFFER_ICON), "Create a polygon geobuffer", toolBar);
    ControllerUtil.addNewShapeAction(new NewShapeControllerModel(NewShapeControllerModel.ShapeType.VARIABLE_GEO_BUFFER, notifier),
                                     TLcdIconFactory.create(TLcdIconFactory.DRAW_VARIABLE_BUFFER_ICON), "Create a buffer with varying width and height", toolBar);
  }

  /**
   * Loads the background data and adds the layer in which
   * the newly created shapes will be added, on top of the
   * background data.
   */
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();
    final ILcdGXYLayer lonLatLayer = factory.createGXYLayer(new ShapeModelFactory().createModel());
    fSnappables.getSnappableLayers().add(lonLatLayer);

    //Increasing pen precision for an accurate visualization
    //of the constructive geometry operations
    ALcdGXYPen pen = (ALcdGXYPen) lonLatLayer.getGXYPen();
    pen.setMinRecursionDepth(0);
    pen.setMaxRecursionDepth(12);
    pen.setWorldDistanceThreshold(1000.0);
    pen.setViewDistanceThreshold(1);
    pen.setAngleThreshold(0.1);

    // Adds the layer to the map panel just beneath the grid layer
    GXYLayerUtil.addGXYLayer(getView(), lonLatLayer, true, false);

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      public void run() {

        // Adds geometry actions to the edit controller.
        TLcdMapJPanel view = getView();
        ILcdAction[] right_mouse_button_actions = {
            new ConstructiveGeometryAction(lonLatLayer, ConstructiveGeometryAction.Type.INTERSECTION, fEditController, view),
            new ConstructiveGeometryAction(lonLatLayer, ConstructiveGeometryAction.Type.UNION, fEditController, view),
            new ConstructiveGeometryAction(lonLatLayer, ConstructiveGeometryAction.Type.DIFFERENCE, fEditController, view),
            new ConstructiveGeometryAction(lonLatLayer, ConstructiveGeometryAction.Type.INVERTED_DIFFERENCE, fEditController, view),
            new ConstructiveGeometryAction(lonLatLayer, ConstructiveGeometryAction.Type.SYMMETRIC_DIFERENCE, fEditController, view),
            new ConvexHullAction(lonLatLayer, fEditController, view),
            new TLcdDeleteSelectionAction(view),
            };
        ShowPopupAction show_popup_action
            = new ShowPopupAction(right_mouse_button_actions, view);
        fEditController.setRightClickAction(show_popup_action);
      }
    });
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Constructive Geometry");
  }

}
