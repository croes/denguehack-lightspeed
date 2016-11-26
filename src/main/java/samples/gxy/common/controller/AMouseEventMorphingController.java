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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Abstract decorator for an <code>ILcdGXYController</code> that converts mouse events.
 */
public abstract class AMouseEventMorphingController implements ILcdGXYController, MouseListener, MouseMotionListener {

  private ILcdGXYController fControllerDelegate;

  /**
   * Constructs a new <code>AMouseEventMorphingController</code>.
   * <p/>
   * The given controller should implement <code>MouseListener</code>
   * or <code>MouseMotionListener</code>, to be able to use the mouse button mapping
   * functionality.
   *
   * @param aControllerDelegate A delegate controller.
   */
  public AMouseEventMorphingController(ILcdGXYController aControllerDelegate) {
    fControllerDelegate = aControllerDelegate;
  }

  public ILcdGXYController getControllerDelegate() {
    return fControllerDelegate;
  }

  protected abstract boolean acceptMouseEvent(MouseEvent aEvent);

  protected abstract MouseEvent convertMouseEvent(MouseEvent aEvent);

  public void startInteraction(ILcdGXYView aGXYView) {
    fControllerDelegate.startInteraction(aGXYView);
  }

  public void terminateInteraction(ILcdGXYView aGXYView) {
    fControllerDelegate.terminateInteraction(aGXYView);
  }

  /**
   * @deprecated
   */
  public void viewRepaint(ILcdGXYView aGXYView) {
    fControllerDelegate.viewRepaint(aGXYView);
  }

  public void paint(Graphics graphics) {
    fControllerDelegate.paint(graphics);
  }

  public Cursor getCursor() {
    return fControllerDelegate.getCursor();
  }

  public String getName() {
    return fControllerDelegate.getName();
  }

  public String getShortDescription() {
    return fControllerDelegate.getShortDescription();
  }

  public ILcdIcon getIcon() {
    return fControllerDelegate.getIcon();
  }

  // Implementations for MouseListener.

  public void mouseClicked(MouseEvent e) {
    if (fControllerDelegate instanceof MouseListener &&
        acceptMouseEvent(e)) {
      ((MouseListener) fControllerDelegate).mouseClicked(convertMouseEvent(e));
    }
  }

  public void mousePressed(MouseEvent e) {
    if (fControllerDelegate instanceof MouseListener &&
        acceptMouseEvent(e)) {
      ((MouseListener) fControllerDelegate).mousePressed(convertMouseEvent(e));
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (fControllerDelegate instanceof MouseListener &&
        acceptMouseEvent(e)) {
      ((MouseListener) fControllerDelegate).mouseReleased(convertMouseEvent(e));
    }
  }

  public void mouseEntered(MouseEvent e) {
    if (fControllerDelegate instanceof MouseListener &&
        acceptMouseEvent(e)) {
      ((MouseListener) fControllerDelegate).mouseEntered(convertMouseEvent(e));
    }
  }

  public void mouseExited(MouseEvent e) {
    if (fControllerDelegate instanceof MouseListener &&
        acceptMouseEvent(e)) {
      ((MouseListener) fControllerDelegate).mouseExited(convertMouseEvent(e));
    }
  }

  // Implementations for MouseMotionListener.

  public void mouseDragged(MouseEvent e) {
    if (fControllerDelegate instanceof MouseMotionListener &&
        acceptMouseEvent(e)) {
      ((MouseMotionListener) fControllerDelegate).mouseDragged(convertMouseEvent(e));
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (fControllerDelegate instanceof MouseMotionListener &&
        acceptMouseEvent(e)) {
      ((MouseMotionListener) fControllerDelegate).mouseMoved(convertMouseEvent(e));
    }
  }

  protected static int getButtonDownMask(int aButton) {
    switch (aButton) {
    case MouseEvent.BUTTON1:
      return InputEvent.BUTTON1_DOWN_MASK;
    case MouseEvent.BUTTON2:
      return InputEvent.BUTTON2_DOWN_MASK;
    case MouseEvent.BUTTON3:
      return InputEvent.BUTTON3_DOWN_MASK;
    default:
      return 0;
    }
  }
}
