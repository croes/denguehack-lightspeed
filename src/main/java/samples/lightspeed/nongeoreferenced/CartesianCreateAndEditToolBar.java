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
package samples.lightspeed.nongeoreferenced;

import java.awt.Component;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.ToolBar;

/**
 * Toolbar for creating cartesian shapes.
 */
public class CartesianCreateAndEditToolBar extends CreateAndEditToolBar {
  private final ToolBar fRegularToolBar;

  public CartesianCreateAndEditToolBar(ILspView aView, Component aParent, ToolBar aRegularToolBar, ILspInteractivePaintableLayer aShapesLayer) {
    super(aView, aParent, aRegularToolBar.getButtonGroup(), false, true, false, aShapesLayer);
    fRegularToolBar = aRegularToolBar;
    addCreateControllers();
  }

  @Override
  protected ILspController createDefaultController() {
    return fRegularToolBar.getDefaultController();
  }

  /**
   * Creates and adds Creation controllers for several shape types to the toolbar.
   */
  private void addCreateControllers() {
    // For each shape type, add a creation controller
    addCreateController(CartesianCreateControllerModel.Type.TEXT, TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON));
    addCreateController(CartesianCreateControllerModel.Type.POINT2D, TLcdIconFactory.create(TLcdIconFactory.DRAW_POINT_ICON));
    addCreateController(CartesianCreateControllerModel.Type.BOUNDS, TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON));
    addSpace();
    addCreateController(CartesianCreateControllerModel.Type.POLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON));
    addCreateController(CartesianCreateControllerModel.Type.POLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON));
    addCreateController(CartesianCreateControllerModel.Type.COMPLEXPOLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON));
    addSpace();
    addCreateController(CartesianCreateControllerModel.Type.CIRCLE, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON));
    addCreateController(CartesianCreateControllerModel.Type.CIRCLE_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_3_POINTS_ICON));
    addCreateController(CartesianCreateControllerModel.Type.ELLIPSE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON));
    addSpace();
    addCreateController(CartesianCreateControllerModel.Type.ARC, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON));
    addCreateController(CartesianCreateControllerModel.Type.ARC_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON));
    addCreateController(CartesianCreateControllerModel.Type.ARC_BY_BULGE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON));
    addCreateController(CartesianCreateControllerModel.Type.ARC_BY_CENTER, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_CENTERPOINT_ICON));
    addCreateController(CartesianCreateControllerModel.Type.ARCBAND, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON));
    addSpace();
    addCreateController(CartesianCreateControllerModel.Type.GEOBUFFER, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON));
    addSpace();
    addCreateController(CartesianCreateControllerModel.Type.POLYLINE_3D, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON));
  }

  private void addCreateController(CartesianCreateControllerModel.Type aType, ILcdIcon aIcon) {
    addCreateController(new CartesianCreateControllerModel(aType, getCreationLayer()), aIcon, aType.toString());
  }
}
