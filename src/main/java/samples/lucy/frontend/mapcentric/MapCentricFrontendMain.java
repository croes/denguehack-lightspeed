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
package samples.lucy.frontend.mapcentric;

import static com.luciad.gui.swing.TLcdOverlayLayout.Location.NORTH_EAST;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.gui.blacklime.BlackLimeLookAndFeel;
import samples.lucy.frontend.AFrontend;
import samples.lucy.frontend.mapcentric.applicationpane.MapCentricAppPaneFactory;
import samples.lucy.frontend.mapcentric.applicationpane.MapCentricWorkspaceCodecDelegate;
import samples.lucy.frontend.mapcentric.gui.CollapsibleSplitPane;
import samples.lucy.frontend.mapcentric.gui.HideableTabbedPane;
import samples.lucy.frontend.mapcentric.gui.MapCentricUtil;
import samples.lucy.frontend.mapcentric.gui.RelativePreferredSizePanel;
import samples.lucy.frontend.mapcentric.gui.onmappanel.OnMapPanelContainer;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyLucyEnvFactory;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyAlwaysFitJToolBar;
import com.luciad.lucy.gui.TLcyFullScreenActiveSettable;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.TLcyMapManagerEvent;
import com.luciad.lucy.workspace.ILcyWorkspaceManagerListener;
import com.luciad.lucy.workspace.TLcyWorkspaceManagerEvent;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * This extension of <code>AFrontend</code> demonstrates how to create your own map-centric frontend.
 *
 * It offers a simplified user interface for a single map, with the following areas:
 * <ul>
 *   <li>a large area for the map, overlayed with on-map panes</li>
 *   <li>a toolbar combining menu items, toolbar buttons, and format bar tab selectors</li>
 *   <li>tabs at the bottom for the profile view and tabular data</li>
 * </ul>
 *
 * It automatically loads the 'map_centric_workspace.lws' workspace file. Albeit the menu items
 * to save and load workspaces aren't accessible by default because the menu bar is hidden,
 * workspace saving and loading does work.
 *
 * Warnings might appear when loading a workspace that was for example written by TLcyMain.
 * This happens because the default front end (TLcyMain) saves the divider locations of
 * the split panes in the workspace.  Because a custom frontend is used now, the codec
 * delegate registered by TLcyMain is no longer registered, and those split pane divider
 * locations can't be read anymore.  This causes the warning to appear.  It is no harm
 * however, the divider settings will simply be ignored.
 *
 */
public class MapCentricFrontendMain extends AFrontend {
  private static final String UID = "samples.lucy.frontend.mapcentric.MapCentricFrontendMain";
  private static final String PREFIX = "MapCentricFrontendMain.";

  public static final double TIME_CONTROLS_WIDTH = 0.35; // % of map width
  private static final double ON_MAP_PANE_WIDTH = 0.30; // % of map width

  private final JPanel fMapArea = new JPanel(new BorderLayout());
  private final OnMapPanelContainer fOnMapPanelContainer;
  private final RelativePreferredSizePanel fOnMapPanelComponent;

  private final HideableTabbedPane fBottomTabs = new HideableTabbedPane();

  public MapCentricFrontendMain(String[] aArgs) {
    this(aArgs,
         "samples/frontend/mapcentric/map_centric_addons.xml",
         "samples/frontend/mapcentric/map_centric_frontend.cfg");
  }

  public MapCentricFrontendMain(String[] aArgs, String aDefaultAddOnsFile, String aDefaultConfigFile) {
    super(retrieveAddOnsFile(aArgs, aDefaultAddOnsFile),
          retrieveConfigurationFile(aArgs, aDefaultConfigFile));

    double dpi = Toolkit.getDefaultToolkit().getScreenResolution() * BlackLimeLookAndFeel.getEnlargeFactor();
    TLcdIconFactory.setDefaultSize(dpi >= 192 ? TLcdIconFactory.Size.SIZE_32 :
                                   dpi >= 120 ? TLcdIconFactory.Size.SIZE_24 :
                                   TLcdIconFactory.Size.SIZE_16);
    TLcdIconFactory.setDefaultTheme(TLcdIconFactory.Theme.WHITE_THEME);

    boolean autoCollapse = getPreferences().getBoolean("mapCentric.autoCollapseOnMapPanels", true);
    fOnMapPanelContainer = new OnMapPanelContainer(autoCollapse);
    fOnMapPanelComponent = new RelativePreferredSizePanel(0, ON_MAP_PANE_WIDTH, fOnMapPanelContainer);
  }

  @Override
  protected ILcyLucyEnv createLucyEnv() {
    //Create the ILcyApplicationPaneFactory
    MapCentricAppPaneFactory appPaneFact = new MapCentricAppPaneFactory(fMapArea, fOnMapPanelContainer, fBottomTabs);

    //Create the ILcyLucyEnv
    ILcyLucyEnv env = new TLcyLucyEnvFactory().createLucyEnv(appPaneFact, getMenuBar(), true);

    //Pass the environment on the factory
    appPaneFact.setLucyEnv(env);

    //Init workspace support
    setupWorkspaceSupport(env);

    return env;
  }

  private void setupWorkspaceSupport(ILcyLucyEnv aLucyEnv) {
    //Add codec delegate to (re)store all application panes
    aLucyEnv.getWorkspaceManager().addWorkspaceCodecDelegate(
        new MapCentricWorkspaceCodecDelegate(UID, PREFIX, fMapArea, fOnMapPanelContainer, fBottomTabs));

    //Make the UI invisible when restoring workspace to avoid flashing frames
    aLucyEnv.getWorkspaceManager().addWorkspaceManagerListener(new ILcyWorkspaceManagerListener() {
      @Override
      public void workspaceStatusChanged(TLcyWorkspaceManagerEvent aEvent) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            // Find the parent window. Using fMapArea to start looking, but could as well use something else.
            // The parent window can be different from {@link #getMainFrame} in case full screen is active.
            Window parentWindow = TLcdAWTUtil.findParentWindow(fMapArea);
            if (parentWindow instanceof RootPaneContainer) {
              RootPaneContainer window = (RootPaneContainer) parentWindow;
              window.getContentPane().setVisible(!getLucyEnv().getWorkspaceManager().isDecodingWorkspace());
            }
          }
        });
      }
    });
  }

  @Override
  protected void initGUI() {

    // Hide the menu bar by default, it can be shown using a hot key
    menuBarToggleable(getMenuBar(), false);

    getLucyEnv().getCombinedMapManager().addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aEvent) {
        if (aEvent.getId() == TLcyMapManagerEvent.MAP_COMPONENT_ADDED) {

          ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> mapComponent = aEvent.getMapComponent();
          // Add the fOnMapPanelComponent to the most recent map. Note that Swing will automatically
          // remove it should it already be added to another map. This happens for example when
          // loading a workspace as that creates a new map.
          final Container overlay = mapComponent.getMapOverlayPanel();
          overlay.add(fOnMapPanelComponent, 0);
          ((TLcdOverlayLayout) overlay.getLayout()).putConstraint(
              fOnMapPanelComponent, NORTH_EAST, TLcdOverlayLayout.ResolveClash.VERTICAL, 0, 0);
        }
      }
    }, true);

    JComponent contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());

    JPanel sideBarPanel = new JPanel();
    sideBarPanel.setLayout(new FormLayout("pref", "top:0px:grow, pref"));
    CellConstraints cc = new CellConstraints();

    JPanel centerPanel = new JPanel(new BorderLayout());

    contentPane.add(centerPanel, BorderLayout.CENTER);
    contentPane.add(sideBarPanel, BorderLayout.WEST);


    // Side bar
    TLcyToolBar sideBar = MapCentricUtil.createToolBar(createVerticalToolbar(), MapCentricUtil.SIDE_BAR_TOOL_BAR, getPreferences(), (JComponent) getMainFrame().getContentPane(), getLucyEnv());
    sideBarPanel.add(sideBar.getComponent(), cc.xy(1,1));

    TLcyToolBar bottomSideBar = MapCentricUtil.createToolBar(createVerticalToolbar(), MapCentricUtil.BOTTOM_SIDE_BAR_TOOL_BAR, getPreferences(), (JComponent) getMainFrame().getContentPane(), getLucyEnv());
    sideBarPanel.add(bottomSideBar.getComponent(), cc.xy(1, 2));

    // Put the tabs to show/hide the table view and vertical view in the side bar
    fBottomTabs.setActionBar(bottomSideBar);

    // Create a split pane for the map area and the bottom tabs (table view etc.)
    CollapsibleSplitPane split = new CollapsibleSplitPane(fMapArea, fBottomTabs);
    centerPanel.add(split, BorderLayout.CENTER);

    Object contentPanelLayoutConstraints = BorderLayout.CENTER;
    addFullScreenActiveSettable(contentPane, contentPanelLayoutConstraints);

    getMainFrame().getContentPane().add(contentPane, contentPanelLayoutConstraints);

    // This color shows when a workspace is loaded. It doesn't harm in case it would be null.
    getMainFrame().setBackground(UIManager.getColor("nimbusBlueGrey"));
  }

  private JToolBar createVerticalToolbar() {
    JToolBar bar = new TLcyAlwaysFitJToolBar(SwingConstants.VERTICAL);
    bar.setFloatable(false);
    return bar;
  }

  private void menuBarToggleable(final ILcyActionBar aBar, boolean aDefaultVisible) {
    aBar.getComponent().setVisible(aDefaultVisible);
    AbstractAction toggle = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aBar.getComponent().setVisible(!aBar.getComponent().isVisible());
      }
    };

    // Use F2 to toggle menu bar visibility
    JComponent content = (JComponent) getMainFrame().getContentPane();
    String key = "toggleMenu";
    String acc = getPreferences().getString("mapCentric.toggleMenuBarAcceleratorKey", null);
    content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(acc), key);
    content.getActionMap().put(key, toggle);
  }

  private void addFullScreenActiveSettable(final Component aContentPane, final Object aLayoutConstraints) {
    //On mac, we have native full screen support for the whole frame
    //No need to insert our own active settable
    if (!TLcdSystemPropertiesUtil.isMacOS()) {
      final ILcyLucyEnv lucyEnv = getLucyEnv();
      ILcyActiveSettable as = new TLcyFullScreenActiveSettable(aContentPane, aLayoutConstraints, lucyEnv);
      as.putValue(TLcyActionBarUtil.ID_KEY, "mapcentric.fullScreenActiveSettable");
      as.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F11"));
      TLcyActionBarUtil.insertInConfiguredActionBars(as, null, lucyEnv.getUserInterfaceManager().getActionBarManager(), getPreferences());
    }
  }

  protected OnMapPanelContainer getOnMapPanelContainer() {
    return fOnMapPanelContainer;
  }

  public static void main(final String[] aArgs) {
    MapCentricFrontendMain frontEnd = TLcdAWTUtil.invokeAndWait(new Callable<MapCentricFrontendMain>() {
      @Override
      public MapCentricFrontendMain call() throws Exception {
        return new MapCentricFrontendMain(aArgs);
      }
    });
    frontEnd.startup();
  }

  /**
   * Convenience method to make it easy to programmatically append additional arguments to the main method.
   * A typical use case is to start with a custom add-ons file:
   *
   * <pre class="code">
   *   public class MyMain {
   *     public static void main(String[] aArgs) {
   *       MapCentricFrontendMain.main(aArgs, "-addons", "myapp/my_addons.xml");
   *     }
   *   }
   * </pre>
   *
   * @param aArgs The original arguments (the command line arguments)
   * @param aAdditionalArgs The additional arguments, to be appended to the {@code aArgs}.
   */
  public static void main(String[] aArgs, String... aAdditionalArgs) {
    String[] newArgs = new String[aArgs.length + aAdditionalArgs.length];
    System.arraycopy(aArgs, 0, newArgs, 0, aArgs.length);
    System.arraycopy(aAdditionalArgs, 0, newArgs, aArgs.length, aAdditionalArgs.length);
    MapCentricFrontendMain.main(newArgs);
  }
}
