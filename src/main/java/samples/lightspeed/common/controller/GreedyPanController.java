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
package samples.lightspeed.common.controller;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.navigation.TLspPanController;

/**
 * This TLspPanController extension avoids using panning and e.g. zooming or rotating at the same time.
 * It swallows input events during panning and sends out fake mouse released events to the next controller in the chain
 * to cancel other ongoing interactions.
 */
public class GreedyPanController extends TLspPanController {

  private boolean fPanning;
  private HashMap<Integer, MouseEvent> fCancelEvents = new HashMap<Integer, MouseEvent>();

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {

    // behave like an ordinary pan controller if we're the last one in the controller chain
    ILspController nextController = getNextController();
    if (nextController == null) {
      super.handleAWTEvent(aEvent);
      return;
    }

    AWTEvent event = aEvent;
    ILcdFilter<AWTEvent> awtFilter = getAWTFilter();
    boolean ownEvent = awtFilter == null || awtFilter.accept(aEvent);
    if (ownEvent) {
      event = handleAWTEventImpl(aEvent);
    }
    if (aEvent instanceof MouseEvent) {
      MouseEvent mouseEvent = (MouseEvent) aEvent;
      if (fPanning) {
        // block or cancel
        if (!fCancelEvents.isEmpty()) {
          for (MouseEvent cancelEvent : fCancelEvents.values()) {
            nextController.handleAWTEvent(cancelEvent);
          }
          fCancelEvents.clear();
        } else {
          event = null;
        }
      } else {
        // store a canceling event
        if (!ownEvent && mouseEvent.getButton() != MouseEvent.NOBUTTON) {
          MouseEvent cancelEvent = new MouseEvent(
              (Component) aEvent.getSource(),
              MouseEvent.MOUSE_RELEASED,
              mouseEvent.getWhen() + 1,
              mouseEvent.getModifiersEx(),
              mouseEvent.getX(),
              mouseEvent.getY(),
              0,
              false,
              mouseEvent.getButton()
          );
          fCancelEvents.put(mouseEvent.getButton(),
                            cancelEvent);
        }
      }
    }
    if (event != null) {
      nextController.handleAWTEvent(event);
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aEvent) {
    if (aEvent instanceof MouseEvent) {
      final MouseEvent me = (MouseEvent) aEvent;
      switch (me.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        fPanning = true;
        break;
      case MouseEvent.MOUSE_DRAGGED:
        break;
      case MouseEvent.MOUSE_RELEASED:
        fPanning = false;
        break;
      }
    }
    return super.handleAWTEventImpl(aEvent);
  }
}
