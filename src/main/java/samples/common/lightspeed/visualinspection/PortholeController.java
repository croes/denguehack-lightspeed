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
package samples.common.lightspeed.visualinspection;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import com.luciad.util.ILcdStringTranslator;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspPortholeController;

/**
 * Extension of the porthole controller that shows a layer selection dialog.
 */
public class PortholeController extends TLspPortholeController {

  private final PortholeOverlayControls fOverlayControls;

  /**
   * @param aView the view for the overlay controls
   * @param aFallbackController the controller to activate if the user quits the inspection
   */
  public PortholeController(ILspAWTView aView, ILspController aFallbackController, ILcdStringTranslator aStringTranslator) {
    super();
    fOverlayControls = new PortholeOverlayControls(aView, this, aFallbackController, aStringTranslator);
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    if (aAWTEvent instanceof MouseEvent && aAWTEvent.getID() == MouseEvent.MOUSE_MOVED) {
      fOverlayControls.hideTooltip();
    }
    return super.handleAWTEventImpl(aAWTEvent);
  }

  @Override
  public void startInteraction(ILspView aView) {
    if (fOverlayControls.startInteraction()) {
      super.startInteraction(aView);
    }
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    if (getView() != null) {
      fOverlayControls.terminateInteraction();
      super.terminateInteraction(aView);
    }
  }
}
