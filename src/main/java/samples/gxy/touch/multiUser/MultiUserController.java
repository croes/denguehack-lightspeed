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
package samples.gxy.touch.multiUser;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import com.luciad.input.ILcdAWTEventListener;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYController;

/**
 * Multi user controller. This controller checks the user id in received touch events and then
 * delegates to one of two new controllers. 
 */
public class MultiUserController extends ALcdGXYController implements ILcdAWTEventListener {

  private Map<String, ILcdGXYController> fUserControllerMap = new HashMap<String, ILcdGXYController>();

  /**
   * Sets a controller for a specific user. When an event with a specific user ID is intercepted,
   * it is forwarded to the given controller. For this to work correctly, the given controller should
   * also implement ILcdAWTEventListener.
   *
   * @param aUserID        a user ID.
   * @param aGXYController a GXY controller.
   */
  public void setUserController(String aUserID, ILcdGXYController aGXYController) {
    fUserControllerMap.put(aUserID, aGXYController);
  }

  public void handleAWTEvent(AWTEvent awtEvent) {
    if (awtEvent instanceof TLcdTouchEvent) {
      TLcdTouchEvent touch_event = (TLcdTouchEvent) awtEvent;
      ILcdGXYController user_controller = fUserControllerMap.get(touch_event.getUserID());
      if (user_controller != null && user_controller instanceof ILcdAWTEventListener) {
        ILcdAWTEventListener listener = (ILcdAWTEventListener) user_controller;
        listener.handleAWTEvent(awtEvent);
      }
    }
  }

  @Override
  public void startInteraction(ILcdGXYView aGXYView) {
    super.startInteraction(aGXYView);
    for (ILcdGXYController controller : fUserControllerMap.values()) {
      controller.startInteraction(aGXYView);
    }
  }

  @Override
  public void terminateInteraction(ILcdGXYView aGXYView) {
    super.terminateInteraction(aGXYView);
    for (ILcdGXYController controller : fUserControllerMap.values()) {
      controller.terminateInteraction(aGXYView);
    }
  }

  @Override
  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    for (ILcdGXYController controller : fUserControllerMap.values()) {
      controller.paint(aGraphics);
    }
  }
}
