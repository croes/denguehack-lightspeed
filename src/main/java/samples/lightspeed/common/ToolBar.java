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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.reference.ILcdGridReference;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.TLspInteractiveLabelsController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SwingUtil;
import samples.common.action.ShowPropertiesAction;
import samples.common.action.ShowReadMeAction;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.debug.DebugOverlayToggleAction;

/**
 * A toolbar which allows the user to do the following things:
 * - Choose a controller to manipulate the view
 * - Switch the view's projection between a 2D and 3D projection and choose from a specific projection within this category
 * - Read out the current coordinates of the mouse location
 * - Show the README file associated with a sample
 *
 * The toolbar uses a default controller which offers editing, selection and navigation functionality
 * and is equipped with an undo manager to undo/redo any editing actions performed with this controller.
 */
public class ToolBar extends JToolBar {

  /**
   * Index constant to add components to the toolbars file group.
   */
  public static final int FILE_GROUP = -2;

  private static final TLcdUndoManager UNDO_MANAGER = new TLcdUndoManager();

  // Controller-to-button mapping
  private Hashtable<ILspController, AbstractButton> fControllerButtonTable = new Hashtable<ILspController, AbstractButton>();

  // View associated to toolbar
  private ILspView fView;
  // Internal attributes
  private ButtonGroup fButtonGroup;
  private int fComponentIndex = 0;
  private int fIndexFileGroup = 0;
  private boolean fFileGroup = false;
  private boolean fAllow3D = true;
  private boolean fAllow2D = true;
  private ILspController fDefaultController;
  private TLspRulerController fRulerController;

  public ToolBar(ILspView aView, Component aParent, boolean aAllow2D, boolean aAllow3D) {
    fButtonGroup = new ButtonGroup();
    fAllow2D = aAllow2D;
    fAllow3D = aAllow3D;
    init(aView, aParent, true, true);
  }

  public ToolBar(ILspView aView, Component aParent) {
    fButtonGroup = new ButtonGroup();
    fAllow2D = false;
    fAllow3D = false;
    init(aView, aParent, false, false);
  }

  ToolBar() {
    // Client should call init himself.
    this(new ButtonGroup());
  }

  ToolBar(ButtonGroup aButtonGroup) {
    fButtonGroup = aButtonGroup != null ? aButtonGroup : new ButtonGroup();
  }

  void init(final ILspView aView, Component aParent, boolean aIsMainToolBar, boolean aAddCreateController) {
    fView = aView;

    if (aAddCreateController) {
      createControllers();
    }

    if (aIsMainToolBar) {
      addSpace();

      // file actions come after this point
      fIndexFileGroup = fComponentIndex;

      if (fAllow2D || fAllow3D) {
        addComponent(ProjectionControls.createProjectionControls(aView));
        addSpace();
        final AbstractButton recenterButton = addController(createRecenterController());
        // Enable the button for grid references only.
        recenterButton.setEnabled(aView.getXYZWorldReference() instanceof ILcdGridReference);
        aView.addPropertyChangeListener(new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof ILspView && "xyzworldreference".equalsIgnoreCase(evt.getPropertyName())) {
              recenterButton.setEnabled(((ILspView) evt.getSource()).getXYZWorldReference() instanceof ILcdGridReference);
            }
          }
        });
      }
      addSpace();
    }

    // Add padding
    addComponent(Box.createHorizontalGlue(), -1);

    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());

    if (aIsMainToolBar) {
      addComponent(SwingUtil.createButtonForAction(this, new DebugOverlayToggleAction(aView), true), -1);

      // Add action to display information on how to use this sample
      addReadmeAction(aParent);
    }

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

  protected void createControllers() {
    // Create default controller offering editing, selection and navigation functionality ( latter
    // consists of pan-, zoom- and rotate controller)
    fDefaultController = getDefaultController();
    addController(fDefaultController);
    // Set our default controller as the initial controller
    getView().setController(fDefaultController);
    // Create a ruler controller
    fRulerController = createRulerController();
    if (fRulerController != null) {
      addController(fRulerController);
    }
  }

  protected ILspController createDefaultController() {
    ILcdAction[] defaultControllerActions = createDefaultControllerActions();
    return ControllerFactory.createGeneralController(
        UNDO_MANAGER,
        fView,
        defaultControllerActions,
        defaultControllerActions[0],
        createInteractiveLabelsController(),
        createStickyLabelsLayerFilter());
  }

  protected ILcdAction[] createDefaultControllerActions() {
    ShowPropertiesAction propertiesAction = new ShowPropertiesAction(fView, ToolBar.getParentComponent(fView));
    return new ILcdAction[]{propertiesAction};
  }

  protected ILspController createRecenterController() {
    return ControllerFactory.createRecenterProjectionController();
  }

  protected TLspRulerController createRulerController() {
    return ControllerFactory.createRulerController(UNDO_MANAGER);
  }

  protected TLspInteractiveLabelsController createInteractiveLabelsController() {
    return null;
  }

  public ILspController getDefaultController() {
    if (fDefaultController == null) {
      fDefaultController = createDefaultController();
    }
    return fDefaultController;
  }

  public TLspRulerController getRulerController() {
    return fRulerController;
  }

  private void addReadmeAction(Component aParent) {
    if (aParent != null) {
      // Add action to display information on how to use this sample
      ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
      if (showReadme != null) {
        addAction(showReadme, -1);
        // Display an info panel when running standalone.
        ShowReadMeAction.showAtStartup(showReadme);
      }
    }
  }

  protected ILcdFilter<ILspLayer> createStickyLabelsLayerFilter() {
    return new ILcdFilter<ILspLayer>() {
      @Override
      public boolean accept(ILspLayer aObject) {
        return false;
      }
    };
  }

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

  public void addAction(ILcdAction aAction) {
    addAction(aAction, fComponentIndex);
  }

  public void addAction(ILcdAction aAction, int aIndex) {
    insertAction(aAction, aIndex);
  }

  private void insertAction(ILcdAction aAction, int aIndex) {
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

    // add a space after the file group
    if (aIndex == FILE_GROUP && !fFileGroup) {
      fFileGroup = true;
      addSpace(fIndexFileGroup);
    }
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
