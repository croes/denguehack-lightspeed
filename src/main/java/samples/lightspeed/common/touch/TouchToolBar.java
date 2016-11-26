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

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

import com.luciad.input.touch.TLcdTouchDevice;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;

import samples.gxy.common.touch.TouchUtil;
import samples.lightspeed.common.ToolBar;

/**
 * ToolBar extension that uses touch controllers.
 * If the system does not support touch input, the touch controllers are wrapped to support mouse input.
 */
public class TouchToolBar extends ToolBar {

  private Hashtable<ILspController, ILspController> fWrappedControllerTable = null;

  //////////////////////////////

  private Hashtable<ILspController, ILspController> getWrappedControllerTable() {
    if (fWrappedControllerTable == null) {
      fWrappedControllerTable = new Hashtable<ILspController, ILspController>();
    }
    return fWrappedControllerTable;
  }

  public TouchToolBar(ILspView aView, Component aParent, boolean aAllow2D, boolean aAllow3D) {
    super(aView, aParent, aAllow2D, aAllow3D);
  }

  protected void createControllers() {
    ILspController defaultController = getDefaultController();
    addController(defaultController);

    // Set our navigation controller as the initial controller
    getView().setController(getWrappedTouchController(defaultController));
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        TouchUtil.checkTouchDevice(null);
      }
    });
  }

  @Override
  protected ILspController createDefaultController() {
    return TouchControllerFactory.createTouchGeneralController(getView(), getUndoManager());
  }

  @Override
  protected TLspRulerController createRulerController() {
    return TouchControllerFactory.createTouchRulerController(getView(), getUndoManager());
  }

//////////////////////////////

  /**
   * Returns the touch controller that is wrapped by the MouseToTouchControllerWrapper when
   * the system is not touch capable.
   *
   * @param aController the mouse-to-touch controller wrapper
   *
   * @return the touch controller that is wrapped
   */
  public ILspController getWrappedTouchController(ILspController aController) {
    return getWrappedControllerTable().get(aController);
  }

  @Override
  public AbstractButton addController(ILspController aController, int aIndex) {
    ILspController controllerWrapper = isTouchSupported() || !(aController != null) ?
                                       aController :
                                       new MouseToTouchControllerWrapper(aController);
    getWrappedControllerTable().put(aController, controllerWrapper);
    return super.addController(controllerWrapper, aIndex);
  }

  private boolean isTouchSupported() {
    return TLcdTouchDevice.getInstance().getTouchDeviceStatus() == TLcdTouchDevice.Status.READY;
  }

}

