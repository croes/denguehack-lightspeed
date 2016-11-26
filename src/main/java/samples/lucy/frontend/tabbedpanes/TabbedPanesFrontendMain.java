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
package samples.lucy.frontend.tabbedpanes;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyTabbedPaneLucyEnvFactory;

import samples.lucy.frontend.AFrontend;

/**
 * This sample demonstrates how to create your own custom frontend (with a
 * complete custom layout) for Lucy.
 *
 * It offers unlimited flexibility over the main class, the user interface, how
 * many tabbed panes to use, where to put them, whether to use tabbed panes at all,
 * addon loading, position of the frame, number of frames, ... It also allows you
 * to integrate Lucy within an existing application.
 *
 * The warning that appears about some missing codec delegates when you start this sample
 * is happening because the default frontend (TLcyMain) saves the divider locations of
 * the split panes in the workspace.  Because a custom frontend is used now, the codec
 * delegate registered by TLcyMain is no longer registered, and those split pane divider
 * locations can't be read anymore.  This causes the warning to appear.  It is no harm
 * however, the divider settings will simply be ignored.
 *
 */
public class TabbedPanesFrontendMain extends AFrontend {
  /**
   * In this mode, the tabbed panes always have tabs.
   */
  public static final int ALWAYS_TABS_MODE = 0;

  /**
   * In this mode, the tabbed panes only have tabs if necessary (more than 1 child).
   */
  public static final int AUTO_TABS_MODE = 1;

  /**
   * Single map mode: no tabs for the map part.
   */
  public static final int SINGLE_MAP_MODE = 2;

  private int fMode = SINGLE_MAP_MODE;

  private JTabbedPane fLeftTabbedPane = new JTabbedPane();
  private JTabbedPane fRightTabbedPane = new JTabbedPane();

  private JPanel fLeftPane = new JPanel(new BorderLayout());
  private JPanel fRightPane = new JPanel(new BorderLayout());

  public TabbedPanesFrontendMain() {
    super(CONFIG_FILE, "samples/frontend/tabbedpanes/tabbedpanes_sample_workspace_addon.cfg", "samples/frontend/tabbedpanes/TabbedPanesFrontendReadMe.cfg");
  }

  public TabbedPanesFrontendMain(String aAddOnsFile) {
    super(aAddOnsFile, CONFIG_FILE);
  }

  /**
   * Create a lucy env based on the type of gui we want to have.
   * @return The newly create lucy env.
   */
  @Override
  protected ILcyLucyEnv createLucyEnv() {
    TLcyTabbedPaneLucyEnvFactory lucy_env_factory = new TLcyTabbedPaneLucyEnvFactory();
    switch (fMode) {
    case ALWAYS_TABS_MODE:
      return lucy_env_factory.createLucyEnv(new JTabbedPane[]{fLeftTabbedPane, fRightTabbedPane}, getMenuBar(), true);
    case AUTO_TABS_MODE:
      return lucy_env_factory.createLucyEnv(new Container[]{fLeftPane, fRightPane}, getMenuBar(), true);
    case SINGLE_MAP_MODE:
      return lucy_env_factory.createLucyEnv(fLeftPane, new JTabbedPane[]{fRightTabbedPane}, getMenuBar(), true);
    }
    return null;
  }

  /**
   * Initialize the gui: put the components in place.  Note that all the tabbed panes and containers
   * are put into one panel which is displayed in one JFrame. It is however perfectly possible to
   * spread the tabbed panes and containers over multiple frames, or to integrate them into an
   * existing application.
   *
   * The user interface is initialized before the lucy environment, to allow (error) dialogs
   * to find a parent frame.  This prevents those dialogs to be hidden behind the main frame.
   */
  @Override
  protected void initGUI() {
    //Set content pane layout
    Container content_pane = getMainFrame().getContentPane();
    content_pane.setLayout(new BorderLayout());

    //Create and add the split pane
    JSplitPane split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    content_pane.add(split_pane, BorderLayout.CENTER);
    content_pane.add(getStatusBar().getComponent(), BorderLayout.SOUTH);
    split_pane.setResizeWeight(0.7);

    //Add the correct components to the split pane, depending on the mode.
    switch (fMode) {
    case ALWAYS_TABS_MODE:
      split_pane.setLeftComponent(fLeftTabbedPane);
      split_pane.setRightComponent(fRightTabbedPane);
      break;
    case AUTO_TABS_MODE:
      split_pane.setLeftComponent(fLeftPane);
      split_pane.setRightComponent(fRightPane);
      break;
    case SINGLE_MAP_MODE:
      split_pane.setLeftComponent(fLeftPane);
      split_pane.setRightComponent(fRightTabbedPane);
      break;
    }
  }

  public static void main(String[] aArgs) {
    final String addOnsFile = retrieveAddOnsFile(aArgs, null);
    final TabbedPanesFrontendMain[] frontEnd = new TabbedPanesFrontendMain[]{null};
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        if (addOnsFile == null) {
          frontEnd[0] = new TabbedPanesFrontendMain();
        } else {
          frontEnd[0] = new TabbedPanesFrontendMain(addOnsFile);
        }
      }
    });
    frontEnd[0].startup();
  }

  /**
   * Convenience method to make it easy to programmatically append additional arguments to the main method.
   * A typical use case is to start with a custom add-ons file:
   *
   * <pre class="code">
   *   public class MyMain {
   *     public static void main(String[] aArgs) {
   *       TabbedPanesFrontendMain.main(aArgs, "-addons", "myapp/my_addons.xml");
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
    TabbedPanesFrontendMain.main(newArgs);
  }
}
