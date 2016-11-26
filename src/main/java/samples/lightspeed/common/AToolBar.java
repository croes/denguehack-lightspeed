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
package samples.lightspeed.common;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import samples.common.SwingUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ILspController;

/**
 * A JToolBar extension that can directly add items of type ILcdAction and ILcdGXYController.
 *
 * ILcdAction's are wrapped with a TLcdSWAction so they can work with any Swing button.
 */
public class AToolBar extends JToolBar {

  /**
   * Index constant to add components to the toolbars file group.
   */
  public static final int FILE_GROUP = -2;

  private static final TLcdUndoManager UNDO_MANAGER = new TLcdUndoManager();

  // Controller-to-button mapping
  private Hashtable<ILspController, AbstractButton> fControllerButtonTable = new Hashtable<>();

  // View associated to toolbar
  private ILspView fView;
  // Internal attributes
  private ButtonGroup fButtonGroup;
  private int fComponentIndex = 0;
  private int fIndexFileGroup = 0;

  public AToolBar(ILspView aView) {
    fButtonGroup = new ButtonGroup();
    init(aView);
  }

  AToolBar() {
    // Client should call init himself.
    this(new ButtonGroup());
  }

  AToolBar(ButtonGroup aButtonGroup) {
    fButtonGroup = aButtonGroup != null ? aButtonGroup : new ButtonGroup();
  }

  void init(final ILspView aView) {
    fView = aView;
    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());
    // Cosmetic enhancement
    SwingUtil.makeFlat(this);
  }

  public ButtonGroup getButtonGroup() {
    return fButtonGroup;
  }

  protected ILspView getView() {
    return fView;
  }

  //////////////////////////////

  /*
   * A undo manager used by the default controller.
   */
  public TLcdUndoManager getUndoManager() {
    return UNDO_MANAGER;
  }

  //////////////////////////////

  public AbstractButton addController(ILspController aController) {
    return addController(aController, fComponentIndex);
  }

  public AbstractButton addController(ILspController aController, int aIndex) {
    return insertController(aController, new TLspSetControllerAction(fView, aController), aIndex);
  }

  public AbstractButton addController(ILspController aController, ILcdAction aAction) {
    return addController(aController, aAction, fComponentIndex);
  }

  public AbstractButton addController(ILspController aController, ILcdAction aAction, int aIndex) {
    return insertController(aController, aAction, aIndex);
  }

  private AbstractButton insertController(ILspController aController, ILcdAction aAction, int aIndex) {
    // Initialize the swing button that will be associated to the controller
    AbstractButton button = SwingUtil.createButtonForAction(this, aAction, true);
    fButtonGroup.add(button);

    // Add button to toolbar and store the controller-button pair
    addComponent(button, aIndex);
    fControllerButtonTable.put(aController, button);
    return button;
  }

  public void removeController(ILspController aController) {
    AbstractButton button = fControllerButtonTable.get(aController);
    if (button != null) {
      int index = getComponentIndex(button);
      if (index < fComponentIndex) {
        fComponentIndex--;
      }
      remove(index);
    }
  }

  public void addAction(ILcdAction aAction) {
    addAction(aAction, fComponentIndex);
  }

  public void addAction(ILcdAction aAction, int aIndex) {
    insertAction(aAction, aIndex);
  }

  protected void insertAction(ILcdAction aAction, int aIndex) {
    boolean toggleButton = aAction.getValue(ILcdAction.SELECTED_KEY) != null;
    AbstractButton button = SwingUtil.createButtonForAction(this, aAction, toggleButton);
    addComponent(button, aIndex);
  }

  public void addSpace() {
    addSpace(10, 10);
  }

  public void addSpace(int aIndex) {
    addSpace(10, 10, aIndex);
  }

  public void addSpace(int aWidth, int aHeight) {
    addSpace(aWidth, aHeight, fComponentIndex);
  }

  private void addSpace(int aWidth, int aHeight, int aIndex) {
    addComponent(Box.createRigidArea(new Dimension(aWidth, aHeight)), aIndex);
  }

  public void addComponent(Component aComponent) {
    addComponent(aComponent, fComponentIndex);
  }

  public void addComponent(Component aComponent, int aIndex) {
    int index = aIndex;
    if (aIndex != -1) {
      if (aIndex == FILE_GROUP) {
        index = fIndexFileGroup++;
      } else if (aIndex < fIndexFileGroup) {
        fIndexFileGroup++;
      }
      fComponentIndex++;
    }
    add(aComponent, index);
  }

  /**
   * Returns the parent component of a view.
   *
   * @param aView the view
   *
   * @return the view's parent component or {@code null}
   */
  public static Component getParentComponent(ILspView aView) {
    return aView instanceof ILspAWTView ? ((ILspAWTView) aView).getHostComponent() : null;
  }
}
