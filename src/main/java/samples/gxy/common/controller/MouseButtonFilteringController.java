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

import java.awt.event.MouseEvent;

import com.luciad.view.gxy.ILcdGXYController;

/**
 * Decorator for an <code>ILcdGXYController</code> that filters out mouse events
 * pertaining to one particular mouse button.
 */
public class MouseButtonFilteringController extends AMouseEventMorphingController {

  private final int fFilteredButton;
  private final int fFilteredButtonDownMask;

  /**
   * Constructs a controller filtering mouse buttons. The given controller should implement
   * <code>MouseListener</code> or <code>MouseMotionListener</code>, to be able to use the mouse button mapping
   * functionality.
   *
   * @param aControllerDelegate A delegate controller.
   * @param aFilteredButton the mouse button to filter
   */
  public MouseButtonFilteringController(ILcdGXYController aControllerDelegate, int aFilteredButton) {
    super(aControllerDelegate);
    fFilteredButton = aFilteredButton;
    fFilteredButtonDownMask = getButtonDownMask(fFilteredButton);
  }

  protected boolean acceptMouseEvent(MouseEvent aMouseEvent) {
    return fFilteredButton == MouseEvent.NOBUTTON ||
           (aMouseEvent.getButton() != fFilteredButton &&
            (aMouseEvent.getModifiersEx() & fFilteredButtonDownMask) == 0);
  }

  protected MouseEvent convertMouseEvent(MouseEvent aSourceEvent) {
    // nothing to do
    return aSourceEvent;
  }

}
