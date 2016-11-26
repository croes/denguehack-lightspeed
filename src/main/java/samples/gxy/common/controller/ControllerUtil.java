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
package samples.gxy.common.controller;

import java.awt.event.MouseEvent;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYPanController;
import com.luciad.view.gxy.controller.TLcdGXYZoomWheelController;

import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.common.touch.MouseToTouchGXYControllerWrapper;
import samples.gxy.editing.GXYCreateAction;

public class ControllerUtil {

  public static TLcdGXYCompositeController wrapWithZoomAndPan(ILcdGXYController aController) {
    TLcdGXYCompositeController compositeController = new TLcdGXYCompositeController();

    // adds the given controller, but filters out the middle mouse button, so that it won't interfere with the panning
    compositeController.addGXYController(new MouseButtonFilteringController(aController, MouseEvent.BUTTON2));

    // adds a pan controller for the middle mouse button
    TLcdGXYPanController panController = new TLcdGXYPanController();
    panController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().middleMouseButton().build());
    panController.setCursor(aController.getCursor());
    panController.setDragViewOnPan(true);
    panController.setDrawLineOnPan(false);
    compositeController.addGXYController(panController);

    // adds a mouse-wheel specific zoom controller
    compositeController.addGXYController(new TLcdGXYZoomWheelController());

    return compositeController;
  }

  public static ILcdGXYController unwrapController(ILcdGXYController aController) {
    if (aController instanceof TLcdGXYCompositeController) {
      TLcdGXYCompositeController compositeController = (TLcdGXYCompositeController) aController;
      if (compositeController.getGXYController(0) instanceof MouseButtonFilteringController) {
        MouseButtonFilteringController filteringController = (MouseButtonFilteringController) compositeController.getGXYController(0);
        return filteringController.getControllerDelegate();
      }
    }
    if (aController instanceof MouseToTouchGXYControllerWrapper) {
      return ((MouseToTouchGXYControllerWrapper)aController).getController();
    }
    return aController;
  }

  public static void addNewShapeAction(
      ALcdGXYNewControllerModel2 aModel,
      ILcdIcon aShapeIcon,
      String aDescription,
      ToolBar aToolBarSFCT) {

    GXYCreateAction action = new GXYCreateAction(aModel, aToolBarSFCT.getGXYView(), aToolBarSFCT.getUndoManager(), aToolBarSFCT.getSnappables());
    action.setIcon(aShapeIcon);
    action.setShortDescription(aDescription);
    aToolBarSFCT.addAction(action);
  }
}
