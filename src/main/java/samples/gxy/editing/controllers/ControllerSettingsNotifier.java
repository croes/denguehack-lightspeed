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
package samples.gxy.editing.controllers;

import java.util.ArrayList;
import java.util.List;

import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;

import samples.gxy.common.controller.ControllerUtil;

/**
 * Allows to subscribe NewShapeControllerModels for changes in the UI controller settings.
 */
public class ControllerSettingsNotifier {

  private ILcdGXYView fView;
  private List<NewShapeControllerModel> fListeners = new ArrayList<>();

  private NewShapeControllerModel.ShapeType fShapeType;
  private boolean fGeodetic;

  public ControllerSettingsNotifier() {
  }

  public ControllerSettingsNotifier(ILcdGXYView aMapJPanel) {
    setView(aMapJPanel);
  }

  public void setView(ILcdGXYView aView) {
    fView = aView;
  }

  public void addListener(NewShapeControllerModel aModel) {
    fListeners.add(aModel);
  }

  /* ---------- Notification methods --------- */

  public void curveTypeChanged(NewShapeControllerModel.ShapeType aShapeType) {
    fShapeType = aShapeType;

    ILcdGXYController controller = getActiveController();
    Object object = null;
    ILcdGXYLayer layer = null;
    ALcdGXYNewControllerModel2 model = null;

    if ( controller instanceof TLcdGXYTouchNewController) {
      TLcdGXYTouchNewController newController = (TLcdGXYTouchNewController) controller;
      object = newController.getObject();
      layer = newController.getGXYLayer();
      model = newController.getNewControllerModel();
    }
    if ( controller instanceof TLcdGXYNewController2) {
      TLcdGXYNewController2 newController = (TLcdGXYNewController2) controller;
      object = newController.getObject();
      layer = newController.getGXYLayer();
      model = newController.getNewControllerModel();
    }
    for (NewShapeControllerModel listener : fListeners) {
      if (model == listener && object instanceof ILcdCompositeCurve) {
        listener.setSubCurveType(aShapeType, (ILcdCompositeCurve) object, layer);
      } else {
        listener.setSubCurveType(aShapeType, null, null);
      }
    }
  }

  public void geodeticShapeChanged(boolean aGeodetic) {
    fGeodetic = aGeodetic;
    for (NewShapeControllerModel listener : fListeners) {
      listener.setGeodetic(aGeodetic);
    }
  }

  /* ---------- Getters for settings  --------- */

  public NewShapeControllerModel.ShapeType getCurveType() {
    return fShapeType;
  }

  public boolean isGeodetic() {
    return fGeodetic;
  }

  /* ---------- Helper methods --------- */

  private ILcdGXYController getActiveController() {
    if (fView == null) {
      return null;
    }
    ILcdGXYController wrappedController = fView.getGXYController();
    ILcdGXYController unwrappedController = ControllerUtil.unwrapController(wrappedController);
    ILcdGXYController controller = unwrappedController != null ? unwrappedController : wrappedController;

    return controller;
  }

}
