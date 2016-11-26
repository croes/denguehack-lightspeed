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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.ILcdLayered;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.gxy.common.touch.MouseToTouchEventAdapter;

/**
 * Translates mouse events to touch events (with a single touch point!).
 */
public class MouseToTouchControllerWrapper extends ALspController {

  private ILspController fController;
  private MouseToTouchEventAdapter fAdapter;

  public MouseToTouchControllerWrapper(ILspController aController) {
    fController = aController;
    fAdapter = new MouseToTouchEventAdapter(aController);
  }

  public ILspController getController() {
    return fController;
  }

  @Override
  public void startInteraction(ILspView aView) {
    fController.startInteraction(aView);
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    fController.terminateInteraction(aView);
  }

  @Override
  public TLspPaintProgress paint(ILcdGLDrawable aGLDrawable, ILspView aView, TLspPaintPhase aPaintPhase) {
    return fController.paint(aGLDrawable, aView, aPaintPhase);
  }

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {
    if (aEvent instanceof MouseWheelEvent) {
      fAdapter.mouseWheelMoved((MouseWheelEvent) aEvent);
    } else if (aEvent instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) aEvent;
      if (me.getID() == MouseEvent.MOUSE_CLICKED) {
        fAdapter.mouseClicked(me);
      } else if (me.getID() == MouseEvent.MOUSE_PRESSED) {
        fAdapter.mousePressed(me);
      } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
        fAdapter.mouseReleased(me);
      } else if (me.getID() == MouseEvent.MOUSE_ENTERED) {
        fAdapter.mouseEntered(me);
      } else if (me.getID() == MouseEvent.MOUSE_EXITED) {
        fAdapter.mouseEntered(me);
      } else if (me.getID() == MouseEvent.MOUSE_DRAGGED) {
        fAdapter.mouseDragged(me);
      } else if (me.getModifiers() == MouseEvent.MOUSE_MOVED) {
        fAdapter.mouseMoved(me);
      }
    } else {
      fController.handleAWTEvent(aEvent);
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    return aAWTEvent;
  }

  public String getName() {
    return fController.getName();
  }

  public String getShortDescription() {
    return fController.getShortDescription();
  }

  public ILcdIcon getIcon() {
    return fController.getIcon();
  }

  @Override
  public ILcdLayered getLayered() {
    return fController.getLayered();
  }

  public void mousePressed(MouseEvent e) {
    fAdapter.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e) {
    fAdapter.mouseReleased(e);
  }

  public void mouseDragged(MouseEvent e) {
    fAdapter.mouseDragged(e);
  }

  public void mouseEntered(MouseEvent e) {
    fAdapter.mouseEntered(e);
  }

  public void mouseExited(MouseEvent e) {
    fAdapter.mouseExited(e);
  }

  public void mouseMoved(MouseEvent e) {
    fAdapter.mouseMoved(e);
  }

  public void mouseClicked(MouseEvent e) {
    fAdapter.mouseClicked(e);
  }

}
