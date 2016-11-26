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
package samples.lightspeed.editing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.common.controller.ControllerFactory;

/**
 * An action that creates and inserts a new shape on one or more Lightspeed views.
 */
public class LspCreateAction extends ALcdAction {

  private final List<ILspView> fViews;
  private final List<TLspCreateController> fControllers;
  private final ILcdUndoableListener fUndoableListener;
  private final ALspCreateControllerModel fCreateControllerModel;

  public LspCreateAction(ILspView aView, ALspCreateControllerModel aCreateControllerModel, ILcdUndoableListener aListener) {
    this(aCreateControllerModel, Collections.singletonList(aView), aListener);
  }

  /**
   * Makes a new action that when triggered, creates a new symbol on the given views.
   *
   * @param aCreateControllerModel the controller model determining what shape is created. The model is shared between the different views
   *                               so it should be thread-safe if multiple views are used.
   * @param aViews                 the views to insert the shape
   * @param aListener              a listener that will be notified of undoable events (e.g. an TLcdUndoManager)
   */
  public LspCreateAction(ALspCreateControllerModel aCreateControllerModel, List<ILspView> aViews, ILcdUndoableListener aListener) {
    fViews = new ArrayList<ILspView>(aViews);
    fControllers = new ArrayList<TLspCreateController>();
    fUndoableListener = aListener;
    fCreateControllerModel = aCreateControllerModel;
    putValue(ILcdAction.NAME, "Add");
    putValue(ILcdAction.SHORT_DESCRIPTION, "Add a new shape");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    fControllers.clear();
    for (ILspView view : fViews) {
      TLspCreateController controller = createAndSetCreateController(view);
      fControllers.add(controller);
    }
  }

  private TLspCreateController createAndSetCreateController(ILspView aView) {
    ILspInteractivePaintableLayer layer = fCreateControllerModel.getLayer(aView);
    if (layer == null) {
      return null;
    }

    // Set up a create controller and wire its undo behavior.
    TLspCreateController controller = new TLspCreateController(fCreateControllerModel);
    controller.addUndoableListener(fUndoableListener);
    ILspController previousController = aView.getController();

    // Cancel pending creations.
    if (previousController instanceof TLspCreateController) {
      ((TLspCreateController) previousController).cancel();
    }

    controller.setActionToTriggerAfterCommit(
        new ResetControllerAction(aView, aView.getController(), controller));
    controller.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        leftMouseButton().or().rightMouseButton().or().keyEvents().build());
    controller.appendController(ControllerFactory.createNavigationController());

    aView.setController(controller);
    return controller;
  }

  private class ResetControllerAction extends TLspSetControllerAction {
    private final TLspCreateController fController;

    private ResetControllerAction(ILspView aView, ILspController aOldController, TLspCreateController aNewController) {
      super(aView, aOldController);
      fController = aNewController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      if (!fControllers.isEmpty()) {
        cancelOthers();
      }
    }

    private void cancelOthers() {
      ArrayList<TLspCreateController> controllers = new ArrayList<TLspCreateController>(fControllers);
      controllers.remove(fController);
      // Clear the list before proceeding, as cancel() calls actionPerformed again
      fControllers.clear();
      for (TLspCreateController controller : controllers) {
        controller.cancel();
      }
    }
  }
}
