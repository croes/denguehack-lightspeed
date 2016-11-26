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
package samples.gxy.common.touch;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.luciad.gui.ILcdIcon;
import com.luciad.input.ILcdAWTEventListener;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYController;

/**
 * Translates mouse events to touch events.
 */
public class MouseToTouchGXYControllerWrapper extends ALcdGXYController implements MouseListener, MouseMotionListener {
  private ILcdGXYController fController;
  private MouseToTouchEventAdapter fAdapter;

  public MouseToTouchGXYControllerWrapper(ILcdGXYController aController) {
    fController = aController;
    fAdapter = new MouseToTouchEventAdapter((ILcdAWTEventListener) aController);
  }

  public ILcdGXYController getController() {
    return fController;
  }

  public void startInteraction(ILcdGXYView aGXYView) {
    super.startInteraction(aGXYView);
    fController.startInteraction(aGXYView);
  }

  public void terminateInteraction(ILcdGXYView aGXYView) {
    super.terminateInteraction(aGXYView);
    fController.terminateInteraction(aGXYView);
  }

  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    fController.paint(aGraphics);
  }

  public Cursor getCursor() {
    return fController.getCursor();
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
