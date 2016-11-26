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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneFactory;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.TLcyGenericMapManager;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.TLcyMapManagerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * Implementation of <code>ILcyApplicationPaneFactory</code> that creates <code>DockingFrameAppPane</code>s,
 * based on JIDEs <code>DockableFrame</code>.
 */
public class DockableFrameAppPaneFactory implements ILcyApplicationPaneFactory {

  private static boolean sKeyListenerInstalled;
  private final DockingManager fDockingManager;
  private boolean fPreferRightSide;
  private ILcyLucyEnv fLucyEnv;
  private int fIndex = 0;

  public DockableFrameAppPaneFactory(DockingManager aDockingManager) {
    this(aDockingManager, true);
  }

  public DockableFrameAppPaneFactory(DockingManager aDockingManager, boolean aPreferRightSide) {
    fDockingManager = aDockingManager;
    fPreferRightSide = aPreferRightSide;
  }

  public void setLucyEnv(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    TLcyCombinedMapManager combinedMapManager = aLucyEnv.getCombinedMapManager();
    installKeyEventListener();

    ActiveMapMediator.install(fDockingManager, combinedMapManager);
  }

  // This listener catches unhandled key events. In case the ancestor of the source of the event is
  // floating, then the event is rerouted to the root pane container (i.e. the main lucy window). This
  // enables the usage of keyboard shortcuts from defined in the main menubar of lucy.
  private static void installKeyEventListener() {
    if (sKeyListenerInstalled) {
      return;
    }
    sKeyListenerInstalled = true;
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor() {

          @Override
          public boolean postProcessKeyEvent(KeyEvent aE) {
            DockableFrameAppPane ancestor = (DockableFrameAppPane) SwingUtilities.getAncestorOfClass(DockableFrameAppPane.class, aE.getComponent());
            if (ancestor == null || !ancestor.isFloated()) {
              return false;
            }
            Object oldSource = aE.getSource();
            aE.setSource(ancestor.getDockingManager().getRootPaneContainer());
            if (SwingUtilities.processKeyBindings(aE)) {
              return true;
            }
            aE.setSource(oldSource);
            return false;
          }

        });
      }
    });
  }

  @Override
  public ILcyApplicationPane createApplicationPane(ILcyApplicationPaneOwner aOwner) {
    return createApplicationPane(ILcyApplicationPaneFactory.HORIZONTAL_PANE, aOwner);
  }

  @Override
  public ILcyApplicationPane createApplicationPane(int aLocationIndex, ILcyApplicationPaneOwner aOwner) {
    DockableFrameAppPane dockable_frame =
        new DockableFrameAppPane(fDockingManager, aOwner, fLucyEnv);
    dockable_frame.putValue(ILcyApplicationPane.SMALL_ICON, new DummyIcon());

    int map_dock_side = fPreferRightSide ? DockContext.DOCK_SIDE_EAST : DockContext.DOCK_SIDE_WEST;
    int tool_dock_side = fPreferRightSide ? DockContext.DOCK_SIDE_WEST : DockContext.DOCK_SIDE_EAST;
    switch (aLocationIndex) {
    case ILcyApplicationPaneFactory.MAP_PANE:
      dockable_frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
      dockable_frame.getContext().setInitSide(map_dock_side);
      dockable_frame.getContext().setInitIndex(1);
      break;
    case ILcyApplicationPaneFactory.HORIZONTAL_PANE:
      dockable_frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
      dockable_frame.getContext().setInitSide(map_dock_side);
      dockable_frame.getContext().setInitIndex(2);
      break;
    case ILcyApplicationPaneFactory.VERTICAL_PANE:
      dockable_frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
      dockable_frame.getContext().setInitSide(tool_dock_side);
      dockable_frame.getContext().setInitIndex(1);
      break;
    case ILcyApplicationPaneFactory.VERTICAL_PANE2:
      dockable_frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
      dockable_frame.getContext().setInitSide(tool_dock_side);
      dockable_frame.getContext().setInitIndex(2);
      break;
    }

    // Initialize the key of the frame. This must be done before adding it to the docking manager.
    // Check first if the key we want to use is already in use (e.g., through a workspace that is loaded).
    while (fDockingManager.getFrame("DockableFrameAppPane " + fIndex) != null) {
      fIndex++;
    }

    // Initialize frame key.
    dockable_frame.setKey("DockableFrameAppPane " + fIndex);

    // Initialize frame name.
    dockable_frame.setName("DockableFrameAppPane " + fIndex++);

    // We do not yet add the dockable frame to the DockingManager
    // The DockableFrameAppPane will add itself as soon as content is added to it
    // The JIDE framework cannot handle adding empty dockable panes, and adding contents afterwards, very well
    // The calculation of the initial sizes gets messed up if you add empty panes.

    // Ensure that the frame is granted its minimum size when created, so the user does not miss it.
    if (!fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
      dockable_frame.getAppContentPane().addContainerListener(new DockableFrameMinimumSizeListener());
    }

    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneAdded(dockable_frame);

    return dockable_frame;
  }

  /**
   * When a dockable frame is selected that contains a map, this listener makes sure that map
   * becomes the active map.
   *
   * When a floating application pane, containing a map component, is used, multiple focus change events
   * are received.
   * E.g. having a floating 3D map and a regular 2D map, changing the focus from the 3D map to the docked
   * layer control could lead to 2 focus change events: one from the 3D map to the window containing the 2D
   * map component, and one from the window containing the 2D map component to the layer control. As a
   * result, the active map would become the 2D map iso the 3D map.
   *
   * Using a timer and switching the active map component based on the last component which gained focus
   * will avoid this behaviour, since the first focus changed event will be ignored.
   */
  private static class ActiveMapMediator implements PropertyChangeListener,
                                                    ILcyGenericMapManagerListener<ILcdView, ILcdLayer>,
                                                    ActionListener {
    private static final String FOCUS_OWNER_PROPERTY_NAME = "focusOwner";

    private WeakReference<DockingManager> fDockingManager;
    private WeakReference<TLcyCombinedMapManager> fMapManager;

    private Timer fTimer;
    private WeakReference<Component> fLastFocusedComponent;

    public ActiveMapMediator(DockingManager aDockingManager, TLcyCombinedMapManager aCombinedMapManager) {
      // We keep weak references to the manager to prevent memory leaks:
      // The keyboard focus manager is a static field that can possibly live longer than Lucy
      // For instance, Lucy can be started and stopped multiple times from another Swing application,
      // in the same virtual machine session. Hence we must prevent any strong links from the
      // focus manager to Lucy's frontend.
      this.fDockingManager = new WeakReference<DockingManager>(aDockingManager);
      this.fMapManager = new WeakReference<TLcyCombinedMapManager>(aCombinedMapManager);
      initTimer();
    }

    private void initTimer() {
      fTimer = new Timer(50, this);
      fTimer.setRepeats(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      Component focus_owner = (Component) evt.getNewValue();
      DockingManager docking_manager = getDockingManager();
      TLcyCombinedMapManager combinedMapManager = getCombinedMapManager();
      if (docking_manager == null || combinedMapManager == null) {
        // The managers were garbage collected meaning that Lucy was shut down
        // so the mediator is no longer necessary
        KeyboardFocusManager focus_manager = (KeyboardFocusManager) evt.getSource();
        focus_manager.removePropertyChangeListener(FOCUS_OWNER_PROPERTY_NAME, this);

        //stop the timer
        fTimer.stop();
      } else {
        //set the correct values on the action listener
        fLastFocusedComponent = new WeakReference<Component>(focus_owner);
        if (fTimer.isRunning()) {
          fTimer.restart();
        } else {
          fTimer.start();
        }
      }
    }

    /**
     * Keep the active map component in sync with the component that has the focus
     * @param aFocusOwner the component which received the focus
     * @param aDocking_manager the docking manager. Must not be <code>null</code>
     * @param aCombinedMapManager the map manager. Must not be <code>null</code>
     */
    private void switchActiveMapComponent(Component aFocusOwner,
                                          DockingManager aDocking_manager,
                                          TLcyCombinedMapManager aCombinedMapManager) {
      if (aFocusOwner == null) {
        return;
      }
      DockableFrame frame = findFrame(aFocusOwner, aDocking_manager);
      if (frame == null) {
        return;
      }
      ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> map = findMapComponent(aCombinedMapManager, frame);
      if (map == null || map == aCombinedMapManager.getActiveMapComponent()) {
        return;
      }
      aCombinedMapManager.setActiveMapComponent(map);
    }

    private static DockableFrame findFrame(Component aFocusOwner, DockingManager aDockingManager) {
      Collection frames = aDockingManager.getAllFrameNames();
      for (Object frameName : frames) {
        DockableFrame frame = aDockingManager.getFrame((String) frameName);
        if (SwingUtilities.isDescendingFrom(aFocusOwner, frame)) {
          return frame;
        }
      }
      return null;
    }

    private DockingManager getDockingManager() {
      return fDockingManager != null ? fDockingManager.get() : null;
    }

    private TLcyCombinedMapManager getCombinedMapManager() {
      return fMapManager != null ? fMapManager.get() : null;
    }

    private Component getLastFocusedComponent() {
      return fLastFocusedComponent != null ? fLastFocusedComponent.get() : null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      DockingManager dockingManager = getDockingManager();
      TLcyCombinedMapManager combinedMapManager = getCombinedMapManager();
      Component component = getLastFocusedComponent();
      if (dockingManager != null && combinedMapManager != null) {
        switchActiveMapComponent(component, dockingManager, combinedMapManager);
      }
    }

    private static ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> findMapComponent(TLcyCombinedMapManager aCombinedMapManager,
                                                                                                     DockableFrame aDockableFrame) {
      Container content_pane = aDockableFrame.getContentPane();
      List<TLcyGenericMapManager<? extends ILcdView, ? extends ILcdLayer>> mapManagerList = aCombinedMapManager.getMapManagers();
      for (TLcyGenericMapManager aMapManager : mapManagerList) {
        for (int i = 0, c = aMapManager.getMapComponentCount(); i < c; i++) {
          ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> generalMapComponent = aMapManager.getMapComponent(i);
          if (SwingUtilities.isDescendingFrom(generalMapComponent.getComponent(), content_pane)) {
            return generalMapComponent;
          }
        }
      }

      return null;
    }

    public static void install(DockingManager aDockingManager, TLcyCombinedMapManager aCombinedMapManager) {
      //add the necessary listeners to keep the active map up to date
      final ActiveMapMediator mediator = new ActiveMapMediator(aDockingManager, aCombinedMapManager);
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
              FOCUS_OWNER_PROPERTY_NAME,
              mediator);
        }
      });

      aCombinedMapManager.addMapManagerListener(mediator);
    }

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent aMapManagerEvent) {
      if (aMapManagerEvent.getId() == TLcyMapManagerEvent.MAP_COMPONENT_REMOVED) {
        getCombinedMapManager().findAndSetNewActiveMap();
      }
    }
  }

  private static class DummyIcon implements ILcdIcon, Cloneable {

    @Override
    public void paintIcon(Component component, Graphics graphics, int aX, int aY) {
    }

    @Override
    public int getIconWidth() {
      return 0;
    }

    @Override
    public int getIconHeight() {
      return 0;
    }

    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError("Could not clone " + this + "!");
      }
    }
  }
}
