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
package samples.gxy.common.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.*;

import com.luciad.gui.ILcdAction;
import samples.common.SwingUtil;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;

import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.common.controller.EditPanController;

/**
 * A JToolBar extension that can directly add items of type ILcdAction and ILcdGXYController.
 *
 * ILcdAction's are wrapped with a TLcdSWAction so they can work with any Swing button.
 * ILcdGXYController's are activated using a TLcdGXYSetControllerAction.
 */
public class AToolBar extends JToolBar {

  // controller to button mapping
  private Hashtable<ILcdGXYController, AbstractButton> fGXYControllerButtonTable = new Hashtable<ILcdGXYController, AbstractButton>();
  private Hashtable<ILcdGXYController, ILcdGXYController> fGXYWrappedControllerTable = new Hashtable<ILcdGXYController, ILcdGXYController>();

  private ILcdGXYView fGXYView;

  // toggle button group
  private ButtonGroup fToggleButtonGroup = new ButtonGroup();

  private int fComponentIndex = 0;

  public AToolBar(ILcdGXYView aGXYView) {
    fGXYView = aGXYView;
    SwingUtil.makeFlat(this); // cosmetic enhancement
  }

  public ILcdGXYView getGXYView() {
    return fGXYView;
  }

  /**
   * Adds a controller to the toolbar, adding mousewheel zoom support.
   * The controller might be wrapped; the wrapper can be obtained with
   * {@link #getGXYController(ILcdGXYController)}
   * @param aController the controller to add
   */
  public void addGXYController(ILcdGXYController aController) {
    addGXYController(aController, fComponentIndex, true);
  }

  public void addGXYController(ILcdGXYController aController, int aIndex) {
    addGXYController(aController, aIndex, true);
  }

  /**
   * Adds a controller to the toolbar, adding mousewheel zoom support if
   * <code>aWrapWithPanAndZoom</code> is set to <code>true</code>
   *
   * @param aController         the controller to add
   * @param aWrapWithPanAndZoom <code>true</code> when mousewheel support should be added to the
   *                            controller. In this case, the wrapper controller can be obtained
   *                            with {@link #getGXYController(ILcdGXYController)}
   */
  public void addGXYController(ILcdGXYController aController, boolean aWrapWithPanAndZoom) {
    addGXYController(aController, fComponentIndex, aWrapWithPanAndZoom);
  }

  private void addGXYController(ILcdGXYController aController, int aIndex, boolean aWrapWithZoomAndPan) {
    ILcdGXYController controller = aController;
    if (aWrapWithZoomAndPan) {
      controller = wrapController(aController);
      fGXYWrappedControllerTable.put(aController, controller);
    }

    AbstractButton button = insertButton(new TLcdGXYSetControllerAction(fGXYView, controller), aIndex, true);
    fGXYControllerButtonTable.put(controller, button);
    fToggleButtonGroup.add(button);
  }

  /**
   * Returns the controller that has been added to the toolbar, or its
   * wrapper.
   * @param aController the controller added with {@link #addGXYController(ILcdGXYController)}
   * @return aController or its wrapper
   */
  public ILcdGXYController getGXYController(ILcdGXYController aController) {
    return fGXYWrappedControllerTable.get(aController);
  }

  public void removeGXYController(ILcdGXYController aController) {
    // check if the controller was wrapped
    ILcdGXYController controller_wrapper = fGXYWrappedControllerTable.get(aController);
    AbstractButton button;
    if (controller_wrapper != null) {
      button = fGXYControllerButtonTable.get(controller_wrapper);
    } else {
      button = fGXYControllerButtonTable.get(aController);
    }
    if (button != null) {
      int index = getComponentIndex(button);
      if (index < fComponentIndex) {
        fComponentIndex--;
      }
      remove(index);
    }
  }

  /**
   * Allows to wrap the given controller.
   * This implementation wraps it with pan/zoom behavior for the middle mouse button.
   * Edit controllers are augmented with pan behavior.
   *
   * @param aController The controller to wrap.
   * @return The wrapped controller.
   */
  protected ILcdGXYController wrapController(ILcdGXYController aController) {
    if (aController instanceof TLcdGXYEditController2) {
      aController = new EditPanController((TLcdGXYEditController2) aController);
    }
    return ControllerUtil.wrapWithZoomAndPan(aController);
  }

  public void addAction(ILcdAction aAction) {
    addAction(aAction, fComponentIndex);
  }

  protected void addAction(ILcdAction aAction, int aIndex) {
    insertButton(aAction, aIndex, aAction.getValue(ILcdAction.SELECTED_KEY) != null);
  }

  protected AbstractButton insertButton(ILcdAction aAction, int aIndex, boolean aToggle) {
    AbstractButton button = SwingUtil.createButtonForAction(this, aAction, aToggle);
    addComponent(button, aIndex);
    return button;
  }

  public void addSpace() {
    addSpace(fComponentIndex);
  }

  public void addSpace(int aIndex) {
    addSpace(10, 10, aIndex);
  }

  public void addSpace(int aWidth, int aHeight) {
    addSpace(aWidth, aHeight, fComponentIndex);
  }

  public void addSpace(int aWidth, int aHeight, int aIndex) {
    addComponent(Box.createRigidArea(new Dimension(aWidth, aHeight)), aIndex);
  }

  public void addComponent(Component aComponent) {
    addComponent(aComponent, fComponentIndex);
  }

  protected void addComponent(Component aComponent, int aIndex) {
    if (aIndex != -1) {
      fComponentIndex++;
    }
    add(aComponent, aIndex);
  }
}

