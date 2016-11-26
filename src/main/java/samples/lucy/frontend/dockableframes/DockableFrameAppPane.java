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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.MetaKeyUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>
 *   Implementation of <code>ILcyApplicationPane</code> that is based on a <code>DockableFrame</code>.
 * </p>
 *
 * <p>
 *   As of V2016.1, the {@code DockableFrameAppPane} will add itself to the {@code DockingManager} as soon as contents is
 *   added to the application pane.
 *   The reason we do this is because the JIDE framework cannot handle an empty {@code DockableFrame} properly.
 *   If you first add an empty dockable frame to the docking manager, and add contents to the frame immediately afterwards,
 *   the initial size of the frame does not respect the size of the contents.
 * </p>
 *
 * <p>
 *   Therefore, this class will:
 * </p>
 *
 * <ul>
 *   <li>
 *     Attach a {@link java.awt.event.ContainerListener} to the {@link #getAppContentPane()}.
 *     When contents is added, the dockable frame is added to the manager.
 *   </li>
 *   <li>
 *     When any of the methods like {@link #setAppVisible(boolean)}, {@link #bringAppToFront()}, ...
 *     are called (which assume the frame is already added to the manager), the frame will be added.
 *   </li>
 * </ul>
 *
 * <p>
 *   This mechanism assumes that a typical call sequence to create and populate application panes is:
 * </p>
 *
 * <pre class="code">
 *    ILcyApplicationPaneFactory factory = ...;
 *    ILcyApplicationPane pane = factory.createApplicationPane( location, owner );
 *
 *    pane.getAppContentPane().setLayout( new BorderLayout() );
 *    pane.getAppContentPane().add( contents, BorderLayout.CENTER );
 *
 *    //configure the application pane
 *    pane.setAppTitle( "My application pane" );
 *    ...
 *
 *    pane.setAppVisible( true );
 *    pane.bringAppToFront();
 * </pre>
 *
 * <p>
 *   In case you first call {@code setAppVisible} or {@code bringAppToFront} and only afterwards add contents,
 *   it might be that the initial size of the panel will be different from previous versions of Lucy.
 *   To restore the old behavior, alter the call sequence to match the sequence illustrated in the above snippet.
 * </p>
 *
 * <p>
 *   During workspace decoding, the dockable frame is immediately added to the docking manager.
 *   As the workspace mechanism will restore the whole layout (including the sizes of the panel), the problem of
 *   wrong initial sizes is avoided.
 *   If we would apply the same workaround during workspace decoding, we might interfere with the correct loading of the
 *   layout.
 * </p>
 */
public class DockableFrameAppPane extends DockableFrame implements ILcyApplicationPane {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DockableFrameAppPane.class.getName());
  private final HashMap<String, Object> fProperties = new HashMap<String, Object>();
  private final ILcyApplicationPaneOwner fOwner;
  private boolean fDisposable = true;
  private final AbstractAction fCloseAction;
  private final ILcyLucyEnv fLucyEnv;
  private boolean fDisposed = false;
  private final JPanel fAppContentPanel;

  private final DockingManager fDockingManager;
  private boolean fAdded = false;
  private final PropertyChangeListener fRedispatcher = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      String propertyName = evt.getPropertyName();
      switch (propertyName) {
      case "autohide":
        putValue(HINT_AUTO_HIDE, isAutohide());
        break;
      case "docked":
        putValue(HINT_AUTO_HIDE, !isDocked());
        putValue(HINT_FLOATING, !isDocked());
        if (isDocked() && getDockingManager() != null) {
          getDockingManager().activateFrame(getKey());
        }
        break;
      case "floated":
        putValue(HINT_FLOATING, isFloated());
        break;
      case "undockedBounds":
        Rectangle bounds = getUndockedBounds();
        Point newValue = bounds != null ? bounds.getLocation() : null;
        putValue(HINT_FLOATING_LOCATION, newValue);
        break;
      }
    }
  };

  public DockableFrameAppPane(DockingManager aDockingManager, ILcyApplicationPaneOwner aOwner, ILcyLucyEnv aLucyEnv) {
    this(aDockingManager, null, aOwner, aLucyEnv);
  }

  public DockableFrameAppPane(DockingManager aDockingManager, Icon aIcon, ILcyApplicationPaneOwner aOwner, ILcyLucyEnv aLucyEnv) {
    super(aIcon);

    fDockingManager = aDockingManager;

    fOwner = aOwner;
    fLucyEnv = aLucyEnv;
    setTabTitle("Title");
    getContext().setFloatable(true);
    getContext().setHidable(true);
    getContext().setDockable(true);
    fCloseAction = new MyCloseAction(this, getCloseAction());
    setCloseAction(fCloseAction);
    setMaximizeAction(new DockableFrameMaximizeAction(this, getMaximizeAction()));

    //insert an extra panel in between the content panel of the dockable frame and the
    //content added by the client of this application pane, to enable minimum size behaviour,
    //which is implemented in DockableFrameMinimumSizeListener
    fAppContentPanel = new JPanel();

    // Set a content pane with a minimum size which is explicitly set to a small value. This allows the user to resize the
    // split panes regardless of their content.
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setMinimumSize(new Dimension(30, 30));
    setContentPane(contentPane);

    getContentPane().add(fAppContentPanel, BorderLayout.CENTER);

    // Ensure that the dockable frame gets added to the manager when it gets populated
    if (!fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
      fAppContentPanel.addContainerListener(new ContainerAdapter() {
        @Override
        public void componentAdded(ContainerEvent e) {
          Container container = e.getContainer();
          //we only need to add it once, so the listener can be removed
          container.removeContainerListener(this);
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              addFrameToDockingManagerIfNeeded();
            }
          });
        }
      });
    } else {
      //Use an invokeLater, because at this point the panel is not yet fully initialized
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          addFrameToDockingManagerIfNeeded();
        }
      });
    }

    // Provide better initial bounds when undocking a tab (instead of JIDE's 200x200 pixels)
    new BetterUndockedSizeListener();

    addPropertyChangeListener(fRedispatcher);

    if (TLcdSystemPropertiesUtil.isMacOS()) {
      String key = "lcd_close_dockable_frame";
      getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, MetaKeyUtil.getCMDDownMask()), key);
      getActionMap().put(key, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (isDisposable()) {
            disposeApp();
          }
        }
      });
    }
  }

  @Override
  public void putValue(String aKey, Object aValue) {
    Object old_value = fProperties.put(aKey, aValue);
    firePropertyChange(aKey, old_value, aValue);

    //Use the icon and small description as frame icon and tooltip.
    if (SMALL_ICON.equals(aKey)) {
      setFrameIcon(aValue == null ? null : new TLcdSWIcon((ILcdIcon) aValue));
    } else if (SHORT_DESCRIPTION.equals(aKey)) {
      setToolTipText((String) aValue);
      ((JComponent) getContentPane()).setToolTipText((String) aValue);
    } else if (HINT_AUTO_HIDE.equals(aKey)) {
      applyHintAutoHide();
    } else if (HINT_FLOATING.equals(aKey)) {
      applyHintFloating();
    } else if (HINT_FLOATING_LOCATION.equals(aKey)) {
      applyHintFloatingLocation();
    }
  }

  private void applyHintAutoHide() {
    removePropertyChangeListener(fRedispatcher);
    try {
      if (fAdded && fProperties.containsKey(HINT_AUTO_HIDE)) {
        if ((boolean) fProperties.get(HINT_AUTO_HIDE)) {
          if (!isAutohide()) {
            getDockingManager().autohideFrame(getKey(), getContext().getCurrentDockSide(), getInitIndex());
          }
        } else {
          if (!isDocked()) {
            getDockingManager().dockFrame(getKey(), getContext().getCurrentDockSide(), getInitIndex());
          }
        }
      }
    } finally {
      addPropertyChangeListener(fRedispatcher);
    }
  }

  private void applyHintFloating() {
    removePropertyChangeListener(fRedispatcher);
    try {
      if (fAdded && fProperties.containsKey(HINT_FLOATING)) {
        if ((boolean) fProperties.get(HINT_FLOATING)) {
          if (!isFloated()) {
            setUndockedBounds(computeUndockedBounds());
            getDockingManager().floatFrame(getKey(), getUndockedBounds(), true);
          }
        } else {
          if (!isDocked()) {
            getDockingManager().dockFrame(getKey(), getContext().getCurrentDockSide(), getInitIndex());
          }
        }
      }
    } finally {
      addPropertyChangeListener(fRedispatcher);
    }
  }

  private void applyHintFloatingLocation() {
    removePropertyChangeListener(fRedispatcher);
    try {
      if (fAdded && fProperties.containsKey(HINT_FLOATING_LOCATION)) {
        if (fProperties.containsKey(HINT_FLOATING) && (boolean) fProperties.get(HINT_FLOATING)) {
          if (!isFloated()) {
            setUndockedBounds(computeUndockedBounds());
            getDockingManager().floatFrame(getKey(), getUndockedBounds(), true);
          }
        } else {
          // Adjust the current undocked bounds, with the new location.
          setUndockedBounds(new MyUndockedBounds((Point) fProperties.get(HINT_FLOATING_LOCATION), getSize()));
        }
      }
    } finally {
      addPropertyChangeListener(fRedispatcher);
    }
  }

  private void addFrameToDockingManagerIfNeeded() {
    if (!fAdded) {
      fAdded = true;
      fDockingManager.addFrame(this);
      fDockingManager.activateFrame(getKey());
      applyHintAutoHide();
      applyHintFloating();
      applyHintFloatingLocation();
    }
  }

  @Override
  public Object getValue(String aKey) {
    return fProperties.get(aKey);
  }

  @Override
  public void packApp() {
  }

  @Override
  public boolean canPackApp() {
    return false;
  }

  @Override
  public void dispose() {
    super.dispose();
    notifyInterestedParties();
  }

  @Override
  public void disposeApp() {
    if (!fDisposed) {
      fDisposed = true;

      notifyInterestedParties();

      DockingManager dockingManager = getDockingManager();
      if (dockingManager != null) {
        dockingManager.removeFrame(getKey());
      }

      // Next line is required to fix a memory leak. This fix removes the
      // internal JideFocusTracker listeners from the content panel.
      getContentPane().removeAll();

      super.dispose();
    } else {
      sLogger.warn("Disposing an application pane [" + getAppTitle() + "] that already was disposed!");
    }
  }

  public boolean isDisposed() {
    return fDisposed;
  }

  private void notifyInterestedParties() {
    if (fOwner != null) {
      fOwner.applicationPaneDisposing(new EventObject(this));
    }
    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneRemoved(this);
  }

  @Override
  public boolean isDisposable() {
    return fDisposable;
  }

  @Override
  public void setDisposable(boolean aDisposable) {
    boolean old_value = isDisposable();
    fDisposable = aDisposable;

    if (!aDisposable && (getAvailableButtons() & BUTTON_CLOSE) == BUTTON_CLOSE) {
      setAvailableButtons(getAvailableButtons() & ~BUTTON_CLOSE);
      fCloseAction.setEnabled(false);
    } else if (aDisposable && (getAvailableButtons() & BUTTON_CLOSE) != BUTTON_CLOSE) {
      setAvailableButtons(getAvailableButtons() | BUTTON_CLOSE);
      fCloseAction.setEnabled(true);
    }

    firePropertyChange("disposable", old_value, aDisposable);
  }

  @Override
  public void setAppEnabled(boolean aEnabled) {
    boolean old_value = isEnabled();
    setEnabled(aEnabled);
    firePropertyChange("appEnabled", old_value, aEnabled);
  }

  @Override
  public boolean isAppVisible() {
    return !isHidden();
  }

  @Override
  public void setAppVisible(boolean aVisible) {
    addFrameToDockingManagerIfNeeded();
    boolean old_value = isAppVisible();
    if (old_value != aVisible) {
      if (aVisible) {
        getDockingManager().showFrame(getKey());
      } else {
        getDockingManager().hideFrame(getKey());
      }

      firePropertyChange("appVisible", old_value, aVisible);
    }
  }

  @Override
  public void setAppSize(Dimension aDimension) {
    Dimension old_value = getAppSize();
    setSize(aDimension);
    firePropertyChange("appSize", old_value, aDimension);
  }

  @Override
  public Dimension getAppSize() {
    return getSize();
  }

  @Override
  public void setAppTitle(String aTitle) {
    String old_value = getAppTitle();

    setTabTitle(aTitle);
    firePropertyChange("appTitle", old_value, aTitle);
  }

  @Override
  public String getAppTitle() {
    return getTabTitle();
  }

  @Override
  public Container getAppContentPane() {
    return fAppContentPanel;
  }

  @Override
  public void bringAppToFront() {
    addFrameToDockingManagerIfNeeded();
    getDockingManager().activateFrame(getKey());
  }

  @Override
  public void setResizable(boolean aResizable) {
    if (!aResizable) {
      sLogger.warn("In docking mode, all application panes can always be resized, ignoring setResizable(false) call for pane[" + getAppTitle() + "]");
    }
  }

  private Rectangle computeUndockedBounds() {
    Frame frame = TLcdAWTUtil.findParentFrame(this);
    if (frame == null || !(fProperties.containsKey(HINT_FLOATING) && (boolean) fProperties.get(HINT_FLOATING))) {
      return super.getUndockedBounds();
    }

    Dimension size = fAppContentPanel.getPreferredSize();
    if (fProperties.get(HINT_FLOATING_LOCATION) == null) {
      JDialog dialog = new JDialog();
      dialog.setSize(size);
      dialog.setLocationRelativeTo(frame);
      return new Rectangle(dialog.getLocation(), size);
    } else {
      return new Rectangle((Point) fProperties.get(HINT_FLOATING_LOCATION), size);
    }
  }



  @Override
  public void updateUI() {
    //override this method to call to set the proper classloader on the UIManager.
    //In case of separated classloaders (e.g., OSGi), the classes to load for the JIDE ui
    //are in the JIDE classloader, so the UIManager should be using that classloader.
    Object previousClassLoader = UIManager.get("ClassLoader");
    try {
      UIManager.put("ClassLoader", DockableFrame.class.getClassLoader());
      super.updateUI();
    } finally {
      UIManager.put("ClassLoader", previousClassLoader);
    }
  }

  private static class MyCloseAction extends AbstractAction {

    private final Action fDefaultCloseAction;
    private final DockableFrameAppPane fDockableFrame;

    public MyCloseAction(DockableFrameAppPane aDockableFrame, Action aDefaultCloseAction) {
      super(TLcyLang.getString("Close"));
      fDockableFrame = aDockableFrame;
      fDefaultCloseAction = aDefaultCloseAction;
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
      //JIDE calls the close action directly without checking whether the action is actually enabled
      if (!(isEnabled() && fDefaultCloseAction.isEnabled())) {
        return;
      }
      fDefaultCloseAction.actionPerformed(aActionEvent);
      fDockableFrame.disposeApp();
    }
  }

  /**
   * This listener provides better initial bounds when undocking a tab (instead of JIDE's 200x200 pixels).
   */
  private class BetterUndockedSizeListener extends DockableFrameAdapter implements ComponentListener {
    public BetterUndockedSizeListener() {
      setUndockedBounds(new MyUndockedBounds((Point) fProperties.get(HINT_FLOATING_LOCATION)));
      addComponentListener(this);
      addDockableFrameListener(this);
    }

    @Override
    public void dockableFrameFloating(DockableFrameEvent aEvent) {
      //The frame is undocked, stop listening.
      removeComponentListener(this);
      removeDockableFrameListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e) {
      //As long as nobody else has set an undocked bounds, keep the default undocked size
      //in sync with the current docked size.
      if (getUndockedBounds() instanceof MyUndockedBounds) {
        setUndockedBounds(new MyUndockedBounds((Point) fProperties.get(HINT_FLOATING_LOCATION), getSize()));
      }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
  }

  private static class MyUndockedBounds extends Rectangle {
    private MyUndockedBounds(Point aPoint) {
      this(aPoint, new Dimension(200, 200));
    }

    private MyUndockedBounds(Point aPoint, Dimension aDimension) {
      super(aPoint == null ? new Point(-1, -1) : aPoint, aDimension);
    }
  }
}
