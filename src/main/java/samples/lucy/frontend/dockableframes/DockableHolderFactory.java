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
package samples.lucy.frontend.dockableframes;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.undo.UndoManager;

import com.jidesoft.docking.ContainerContainer;
import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableHolderPanel;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.PopupMenuCustomizer;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.utils.Lm;
import samples.common.gui.blacklime.BlackLimeLookAndFeel;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Factory to create and initialize a JIDE DockableHolder.
 */
public class DockableHolderFactory {
  public static DockableHolderPanel createDockableHolder(JFrame aFrame, boolean aSupportHeavyWeight, final boolean aAddUndoRedoSupport) {
    Lm.setParent(aFrame);

    initLookAndFeel();

    //Create dockable holder
    DockableHolderPanel dockable_holder = new MyDockableHolderPanel(aFrame);
    final DockingManager docking_manager = dockable_holder.getDockingManager();

    //avoid that the JIDE persistence layout mechanism stores the size and location of the main frame
    //since this is already handled by our front-end
    //now it will use the frame bounds and state of the main frame iso trying to restore it
    //when calling resetToDefault();
    docking_manager.setUseFrameBounds(false);
    docking_manager.setUseFrameState(false);

    //do not let JIDE itself store layout data, but let us do it ourselves,
    //by storing it in the workspace.
    docking_manager.setUsePref(false);

    //Enable support for heavy weight components, such as 3D view.
    docking_manager.setHeavyweightComponentEnabled(aSupportHeavyWeight);

    //Configure auto hiding
    docking_manager.setInitDelay(100);
    docking_manager.setSteps(1);
    docking_manager.setStepDelay(0);

    //Setup undo/redo
    docking_manager.setUndoLimit(10);
    docking_manager.setPopupMenuCustomizer(new PopupMenuCustomizer() {

      @Override
      public void customizePopupMenu(JPopupMenu aPopupMenu, final DockingManager aDockingManager,
                                     DockableFrame aDockableFrame, boolean aOnTab) {
        //Remove dockable toggle: it's not very useful as we set setPreserveStateOnDragging(true)
        Component[] items = aPopupMenu.getComponents();
        for (Component item : items) {
          if (item instanceof AbstractButton && "Dockable".equals(((AbstractButton) item).getText())) {
            aPopupMenu.remove(item);
          }
        }

        aPopupMenu.add(new DockingModeResetLayoutAction(docking_manager));
        if (aAddUndoRedoSupport) {
          UndoAction undoAction = new UndoAction(TLcyLang.getString("Undo layout change"), docking_manager.getUndoManager());
          undoAction.setEnabled(docking_manager.getUndoManager().canUndo());
          aPopupMenu.add(undoAction);
          RedoAction redoAction = new RedoAction(TLcyLang.getString("Redo layout change"), docking_manager.getUndoManager());
          redoAction.setEnabled(docking_manager.getUndoManager().canRedo());
          aPopupMenu.add(redoAction);
        }
        int index = getIndex(aDockableFrame.getCloseAction(), aPopupMenu);
        Action action = new CloseAllAction(aDockableFrame, false);
        if (action.isEnabled()) {
          aPopupMenu.add(new JMenuItem(action), ++index);
        }
        action = new CloseAllAction(aDockableFrame, true);
        if (action.isEnabled()) {
          aPopupMenu.add(new JMenuItem(action), ++index);
        }
        aPopupMenu.add(new JPopupMenu.Separator(), ++index);
      }

      int getIndex(Action aAction, JPopupMenu aPopupMenu) {
        for (int i = 0; i < aPopupMenu.getComponentCount(); i++) {
          if (aPopupMenu.getComponent(i) instanceof AbstractButton) {
            if (aAction == ((AbstractButton) aPopupMenu.getComponent(i)).getAction()) {
              return i;
            }
          }
        }
        return aPopupMenu.getComponentCount() - 1;
      }

    });

    //Configure tabbed panes
    docking_manager.setTabbedPaneCustomizer(new DefaultDockingManager.TabbedPaneCustomizer() {
      @Override
      public void customize(JideTabbedPane aTabbedPane) {
        //Many tabs -> show drop down button to select tab
        aTabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_NONE);
        aTabbedPane.setTabPlacement(SwingConstants.TOP);
      }
    });

    docking_manager.setInitSplitPriority(DefaultDockingManager.SPLIT_EAST_WEST_SOUTH_NORTH);

    // Disable the workspace area
    docking_manager.getWorkspace().setAcceptDockableFrame(false);
    docking_manager.setShowWorkspace(false);

    // Can only change from docking to floating by using the button or the right click menu
    // This simplifies dropping a panel.
    docking_manager.setPreserveStateOnDragging(true);
    docking_manager.setEasyTabDock(true);

    // Let double click maximize the tab
    docking_manager.setDoubleClickAction(DockingManager.DOUBLE_CLICK_TO_MAXIMIZE);

    // Should groups of tabs stick together?
    boolean support_grouping = false;
    docking_manager.setDragAllTabs(support_grouping);
    docking_manager.setAutohideAllTabs(support_grouping);
    docking_manager.setGroupAllowedOnSidePane(support_grouping);
    docking_manager.setFloatAllTabs(support_grouping);
    docking_manager.setHideAllTabs(support_grouping);
    docking_manager.setMaximizeAllTabs(support_grouping);

    // Clean up the JIDE resources when the frame is disposed
    aFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        dispose(docking_manager);
      }
    });

    // Tell Jide we're about to add frames.  This is ended by either calling resetLayoutData or
    // loadLayoutFrom
    docking_manager.beginLoadLayoutData();

    return dockable_holder;
  }

  private static void initLookAndFeel() {
    //JIDE requires to install its look and feel extensions whenever the look and feel changes
    updateJideLookAndFeel();
    UIManager.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("lookAndFeel".equals(evt.getPropertyName())) {
          updateJideLookAndFeel();
        }
      }
    });
  }

  private static void updateJideLookAndFeel() {
    int xerto = LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU;
    if (BlackLimeLookAndFeel.isInstalled()) {
      if (LookAndFeelFactory.getDefaultStyle() == xerto) {
        throw new IllegalStateException(
            "JIDE does not support to change its style without artifacts. You are likely installing "
            + "BlackLimeLookAndFeel after the JIDE style had been set. To avoid this, install the "
            + "BlackLimeLookAndFeel before JIDE, for example from a main method wrapper.");
      }
      LookAndFeelFactory.setDefaultStyle(LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);
      LookAndFeelFactory.installJideExtension();
      BlackLimeLookAndFeel.installJideDockingDefaults();
    } else {
      LookAndFeelFactory.setDefaultStyle(xerto);
      LookAndFeelFactory.installJideExtension();
    }
  }

  /**
   * Disposes the JIDE resources.
   * @param aDockingManager The docking manager.
   */
  private static void dispose(DockingManager aDockingManager) {
    Lm.setParent(null);

    // Remove all frames before disposing the docking manager as otherwise the frames might be disposed twice:
    // once by JIDE and once by Lucy tear down code.
    aDockingManager.removeAllFrames();

    aDockingManager.dispose();
  }

  /**
   * Restore all panels to their original location and keep the visibility settings of the panels.
   * @param aDockingManager the docking manager
   */
  public static void resetToDefault(DockingManager aDockingManager) {
    //store the list of hidden application panes, because DockingManager#resetToDefault
    //makes all DockableFrames visible again.
    //This is due to the initMode with which the panels are added. That mode is STATE_FRAMEDOCKED
    //or STATE_FLOATING, so when calling reset to default the panel will be set to that mode and
    //the visibility setting is lost.
    List<String> hidden_frames = new ArrayList<String>();
    List<String> visible_frames = new ArrayList<String>();
    for (String name : aDockingManager.getAllFrameNames()) {
      DockableFrame frame = aDockingManager.getFrame(name);
      if (frame.isHidden()) {
        hidden_frames.add(name);
      } else {
        visible_frames.add(name);
      }
    }

    aDockingManager.resetToDefault();

    //the reset cannot be undone (it is not registered in the undomanager of the dockingmanager)
    //so discard the undo history.
    aDockingManager.getUndoManager().discardAllEdits();

    //restore the hidden property of the DockableFrames
    for (String hidden_frame : hidden_frames) {
      aDockingManager.hideFrame(hidden_frame);
    }
    for (String visible_frame : visible_frames) {
      aDockingManager.showFrame(visible_frame);
    }
  }

  /**
   * Action to reset the JIDE layout to the original layout, like it was loaded from the workspace.
   */
  private static class DockingModeResetLayoutAction extends AbstractAction {

    private DockingManager fDockingManager;

    public DockingModeResetLayoutAction(DockingManager aDockingManager) {
      super(TLcyLang.getString("Reset layout"));
      fDockingManager = aDockingManager;
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
      resetToDefault(fDockingManager);
    }
  }

  private static class UndoAction extends AbstractAction {
    private final UndoManager fUndoManager;

    public UndoAction(String name, UndoManager aUndoManager) {
      super(name);
      fUndoManager = aUndoManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (fUndoManager.canUndo()) {
        fUndoManager.undo();
      }
    }
  }

  private static class RedoAction extends AbstractAction {
    private final UndoManager fUndoManager;

    public RedoAction(String name, UndoManager aUndoManager) {
      super(name);
      fUndoManager = aUndoManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (fUndoManager.canRedo()) {
        fUndoManager.redo();
      }
    }
  }

  private static class CloseAllAction extends AbstractAction {

    private DockableFrame fDockableFrame;
    private boolean fAlsoCloseFrameItself;

    public CloseAllAction(DockableFrame aFrame, boolean aAlsoCloseFrameItself) {
      super(aAlsoCloseFrameItself ? TLcyLang.getString("Close all") : TLcyLang.getString("Close others"));
      fDockableFrame = aFrame;
      setEnabled(false);
      List<DockableFrame> peers = getPeers();
      if (!peers.isEmpty()) {
        for (DockableFrame frame : peers) {
          if (frame.getCloseAction().isEnabled()) {
            setEnabled(true);
          }
        }
        if (aAlsoCloseFrameItself) {
          setEnabled(isEnabled() && fDockableFrame.getCloseAction().isEnabled());
        }
      }
      fAlsoCloseFrameItself = aAlsoCloseFrameItself;
    }

    List<DockableFrame> getPeers() {
      List<DockableFrame> result = new ArrayList<DockableFrame>();
      DockingManager dockingManager = fDockableFrame.getDockingManager();
      DockableFrame aFrame = dockingManager.getFrame(dockingManager.getNextFrame(fDockableFrame.getKey()));
      while (aFrame != fDockableFrame) {
        if (aFrame.getParent() == fDockableFrame.getParent()) {
          result.add(aFrame);
        }
        aFrame = dockingManager.getFrame(dockingManager.getNextFrame(aFrame.getKey()));
      }
      if (fAlsoCloseFrameItself) {
        result.add(fDockableFrame);
      }
      return result;
    }

    @Override
    public void actionPerformed(ActionEvent aE) {
      for (DockableFrame aFrame : getPeers()) {
        close(aFrame, aE);
      }
    }

    private void close(DockableFrame aFrame, ActionEvent aE) {
      Action action = aFrame.getCloseAction();
      if (action != null && action.isEnabled()) {
        action.actionPerformed(aE);
      }
    }

  }

  /**
   * Extension of DockableHolderPanel that creates a dockable holder with larger dividers
   * for the split panes.
   */
  private static class MyDockableHolderPanel extends DockableHolderPanel {
    public MyDockableHolderPanel(JFrame aFrame) {
      super(aFrame);
    }

    @Override
    protected DockingManager createDockingManager(RootPaneContainer aRootPaneContainer) {
      return new MyDefaultDockingManager(aRootPaneContainer, this);
    }

    private static class MyDefaultDockingManager extends DefaultDockingManager {
      public MyDefaultDockingManager(RootPaneContainer aRootPaneContainer, MyDockableHolderPanel aPanel) {
        super(aRootPaneContainer, aPanel);
      }

      @Override
      public ContainerContainer createContainerContainer() {
        ContainerContainer container_container = super.createContainerContainer();
        container_container.setDividerSize(5);
        container_container.setShowGripper(true);
        return container_container;
      }
    }
  }
}
