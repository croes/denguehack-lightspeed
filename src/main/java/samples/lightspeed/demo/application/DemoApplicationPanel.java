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

import java.util.HashMap;
import java.util.Map;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.navigation.TLspPanController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchNavigateController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchSelectEditController;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.gui.ApplicationPanel;
import samples.lightspeed.demo.framework.gui.action.ShowPropertiesAction;
import samples.lightspeed.demo.framework.gui.menu.ActiveThemeMenuController;

/**
 * ApplicationPanel for the bundled Lightspeed Demo.
 */
public class DemoApplicationPanel extends ApplicationPanel {

  public DemoApplicationPanel(boolean a2DView, int aWidth, int aHeight) {
    super(a2DView, aWidth, aHeight);
  }

  @Override
  public void buildThemeMenu() {
    super.buildThemeMenu();
    ActiveThemeMenuController controller = new ActiveThemeMenuController(getSlideMenuManager(), this);
    Framework.getInstance().registerActiveThemeMenuController(controller);
  }

  protected ILspController createDefaultController() {
    Framework framework = Framework.getInstance();

    boolean isTouchEnabled = Boolean.parseBoolean(framework.getProperty("controllers.touch.enabled", "false"));
    ILspController defaultController = isTouchEnabled ? createTouchGeneralController() : createGeneralController();

    boolean isEnableInertia = Boolean.parseBoolean(framework.getProperty("controllers.inertia.enabled", "true"));
    customizeInertia(defaultController, isEnableInertia);

    // Store this default controller as a shared value
    Map<ILspView, ILspController> defaultControllers = (Map<ILspView, ILspController>) framework
        .getSharedValue("view.default.controllers");
    if (defaultControllers == null) {
      defaultControllers = new HashMap<ILspView, ILspController>();
      framework.storeSharedValue("view.default.controllers", defaultControllers);
    }
    defaultControllers.put(getView(), defaultController);

    return defaultController;
  }

  protected ALspController createGeneralController() {
    ShowPropertiesAction propertiesAction = new ShowPropertiesAction(getView());
    return ControllerFactory.createGeneralController(null, getView(), new ILcdAction[]{propertiesAction}, propertiesAction, null);
  }

  protected ILspController createTouchGeneralController() {
    TLspTouchSelectEditController selectEditController = new TLspTouchSelectEditController() {
      @Override
      protected TLspPaintProgress paintImpl(ILcdGLDrawable aGLDrawable, ILspView aView, TLspPaintPhase aPaintPhase) {
        return TLspPaintProgress.COMPLETE;
      }
    };
    selectEditController.addUndoableListener(new TLcdUndoManager());
    final TLspTouchNavigateController touchNavigateController = new TLspTouchNavigateController(true, true, true);
    selectEditController.appendController(touchNavigateController);
    selectEditController.setIcon(new TLcdImageIcon("images/gui/touchicons/selecttouch_64.png"));
    return selectEditController;
  }

  private void customizeInertia(ILspController aDefaultController, boolean aEnableInertia) {
    ILspController controller = aDefaultController;
    while (controller != null) {
      if (controller instanceof TLspPanController) {
        ((TLspPanController) controller).setEnableInertia(aEnableInertia);
      } else if (controller instanceof TLspTouchNavigateController) {
        ((TLspTouchNavigateController) controller).setEnableInertia(aEnableInertia);
      }
      controller = controller.getNextController();
    }
  }
}
