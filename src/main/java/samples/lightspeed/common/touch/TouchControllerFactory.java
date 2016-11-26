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
package samples.lightspeed.common.touch;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdTranslatedIcon;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.util.ILcdFilter;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectControllerModel;
import com.luciad.view.lightspeed.controller.selection.TLspSelectPointInput;
import com.luciad.view.lightspeed.controller.touch.TLspTouchAndHoldActionController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchNavigateController;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.common.action.DialogSelectionHandler;

/**
 * Factory for some popular touch controllers, showing typical filtering and chaining functionality.
 */
public class TouchControllerFactory {
  /*
     * A default touch controller, offering most of the functionality generally used.
     * @param aUndoableListener an undoable listener for editing functionality.
     */
  public static ILspController createTouchGeneralController(ILspView aView, ILcdUndoableListener aUndoableListener) {
    TouchEditController selectEditController = new TouchEditController(aUndoableListener);
    TLcdCompositeIcon icon = new TLcdCompositeIcon();
    icon.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.ARROW_ICON)));
    icon.addIcon(new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.TOUCH_SELECT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_16), 19, 0)));
    selectEditController.setIcon(icon);

    if (aView instanceof ILspAWTView) {
      ILspController touchAndHoldController = createTouchSelectionDialogController((ILspAWTView) aView);
      selectEditController.appendController(touchAndHoldController);
    }

    TLspTouchNavigateController navigateController = new TLspTouchNavigateController();
    selectEditController.appendController(navigateController);

    return selectEditController;
  }

  public static TLspRulerController createTouchRulerController(ILspView aView, TLcdUndoManager aUndoManager) {
    if (aView instanceof ILspAWTView) {
      TouchRulerController ruler = new TouchRulerController(((ILspAWTView) aView).getOverlayComponent(), false);
      ruler.addUndoableListener(aUndoManager);
      ruler.setAWTFilter(new TouchFilter());
      ruler.appendController(new TLspTouchNavigateController());
      return ruler;
    }
    return null;
  }

  private static ILspController createTouchSelectionDialogController(final ILspAWTView aView) {
    // This controller displays a selection dialog when making a touch-and-hold gesture on a selectable object.
    TLspTouchAndHoldActionController touchAndHoldController = new TLspTouchAndHoldActionController();
    touchAndHoldController.setPostTouchAndHoldAction(new ALcdAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TLcdActionAtLocationEvent locationEvent = (TLcdActionAtLocationEvent) e;
        TLspSelectControllerModel selectControllerModel = new TLspSelectControllerModel();
        List<TLspDomainObjectContext> candidates = selectControllerModel.selectionCandidates(
            new TLspSelectPointInput(locationEvent.getLocation()), Collections.singleton(TLspPaintRepresentation.BODY), true, aView);
        DialogSelectionHandler.handleSelectionCandidates((aView).getHostComponent(), new ArrayList<TLcdDomainObjectContext>(candidates));
      }
    });
    return touchAndHoldController;
  }

  private static class TouchFilter implements ILcdFilter<AWTEvent> {
    @Override
    public boolean accept(AWTEvent aObject) {
      return aObject instanceof TLcdTouchEvent;
    }
  }
}
