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
package samples.gxy.editing;

import java.awt.event.ActionEvent;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.editing.controllers.NewShapeControllerModel;

/**
 * An action that creates and inserts a new shape on the given map.
 * After creation, it switches back to the previously set (non-creation) controller.
 */
public class GXYCreateAction extends TLcdGXYSetControllerAction {

  private final TLcdGXYSetControllerAction fRestoreControllerAction;

  public GXYCreateAction(
      final ALcdGXYNewControllerModel2 aControllerModel2,
      ILcdGXYView aView,
      ILcdUndoableListener aUndoableListener,
      ILcdGXYLayerSubsetList aSnappables
  ) {
    putValue(ILcdAction.SHORT_DESCRIPTION, "Adds a new shape");
    if (aControllerModel2 instanceof NewShapeControllerModel) {
      final NewShapeControllerModel controllerModel2 = (NewShapeControllerModel) aControllerModel2;
      controllerModel2.addChangeListener(new ILcdChangeListener() {
        @Override
        public void stateChanged(TLcdChangeEvent aChangeEvent) {
          setEnabled(controllerModel2.isSupported());
        }
      });
    }

    TLcdGXYNewController2 controller = new TLcdGXYNewController2(aControllerModel2);
    controller.addUndoableListener(aUndoableListener);
    fRestoreControllerAction = new TLcdGXYSetControllerAction();
    controller.setActionToTriggerAfterCommit(fRestoreControllerAction);
    controller.setSnappables(aSnappables);
    setGXYView(aView);
    setGXYController(ControllerUtil.wrapWithZoomAndPan(controller));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ILcdGXYView view = getGXYView();
    if (view != null) {
      fRestoreControllerAction.setGXYView(view);
      fRestoreControllerAction.setGXYController(retrieveFallbackController(view.getGXYController()));
    }
    super.actionPerformed(e);
  }

  private ILcdGXYController retrieveFallbackController(ILcdGXYController aFallbackCandidate) {
    // If we switch from one to another new controller, make sure to get the original fallback controller.
    ILcdGXYController unwrappedController = ControllerUtil.unwrapController(aFallbackCandidate);
    if (unwrappedController instanceof TLcdGXYNewController2 &&
        ((TLcdGXYNewController2) unwrappedController).getActionToTriggerAfterCommit() instanceof TLcdGXYSetControllerAction) {
      ILcdAction triggerAfterCommit = ((TLcdGXYNewController2) unwrappedController).getActionToTriggerAfterCommit();
      return retrieveFallbackController(((TLcdGXYSetControllerAction) triggerAfterCommit).getGXYController());
    }
    return aFallbackCandidate;
  }
}
