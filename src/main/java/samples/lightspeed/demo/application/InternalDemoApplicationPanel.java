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
package samples.lightspeed.demo.application;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchNavigateController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchSelectEditController;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.demo.application.controller.TouchControllerFilter;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.gui.menu.SlideMenuManager;

public class InternalDemoApplicationPanel extends DemoApplicationPanel {
  public InternalDemoApplicationPanel(boolean a2DView, int aWidth, int aHeight) {
    super(a2DView, aWidth, aHeight);
    Framework.getInstance().storeSharedValue(SlideMenuManager.class.getName(), getSlideMenuManager());
  }

  @Override
  // Overridden to add an experimental TouchControllerFilter
  protected ILspController createTouchGeneralController() {
    TLspTouchSelectEditController selectEditController = new TLspTouchSelectEditController() {
      @Override
      protected TLspPaintProgress paintImpl(ILcdGLDrawable aGLDrawable, ILspView aView, TLspPaintPhase aPaintPhase) {
        return TLspPaintProgress.COMPLETE;
      }
    };
    selectEditController.addUndoableListener(new TLcdUndoManager());
    final TLspTouchNavigateController touchNavigateController = new TLspTouchNavigateController(true, true, true);
    selectEditController.appendController(new TouchControllerFilter(touchNavigateController));
    selectEditController.setIcon(new TLcdImageIcon("images/gui/touchicons/selecttouch_64.png"));
    return selectEditController;
  }

}
