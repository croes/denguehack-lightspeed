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
package samples.lucy.cop.addons.missioncontroltheme;

import java.awt.event.ActionEvent;

import com.luciad.shape.ILcdShape;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.common.controller.LonLatCreateControllerModel;

final class GeoJsonRestModelCreateControllerFactory {
  private GeoJsonRestModelCreateControllerFactory() {
    //utility class, no constructor needed
  }

  /**
   * Create a new create controller instance
   *
   * @param aLayer The layer in which the newly create objects should be inserted.
   * @param aModel The model of {@code aLayer}
   * @param aType  The type of elements this controller should create
   *
   * @return The controller
   */
  public static ILspController newCreateController(final ILspInteractivePaintableLayer aLayer,
                                                   final AGeoJsonRestModelWithUpdates aModel,
                                                   LonLatCreateControllerModel.Type aType,
                                                   final ILspController aControllerAfterCreation,
                                                   final ILspView aView) {
    final LonLatCreateControllerModel cm = new LonLatCreateControllerModel(aType, aLayer) {
      @Override
      public Object create(ILspView aView, ILspLayer aLayer) {
        Object o = super.create(aView, aLayer);
        if (o instanceof ILcdShape) {
          return aModel.createDomainObjectForShape((ILcdShape) o);
        }
        return o;
      }

      @Override
      public void canceled(TLspEditContext aEditContext) {
        super.canceled(aEditContext);
        switchBackToOriginalController();
      }

      @Override
      public void finished(TLspEditContext aEditContext) {
        super.finished(aEditContext);
        Object addedElement = aEditContext.getObject();
        AGeoJsonRestModelWithUpdates model = (AGeoJsonRestModelWithUpdates) aLayer.getModel();
        model.creationFinished(((GeoJsonRestModelElement) addedElement));
        switchBackToOriginalController();
      }

      private void switchBackToOriginalController() {
        if (aControllerAfterCreation != null && aView != null) {
          new TLspSetControllerAction(aView, aControllerAfterCreation).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Switch back to original controller"));
        }
      }
    };
    TLspCreateController controller = new TLspCreateController(cm);

    controller.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().rightMouseButton().or().keyEvents().build());
    ALspController navigation = ControllerFactory.createNavigationController();
    controller.appendController(navigation);
    return controller;
  }
}
