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
package samples.lucy.frontend.internalframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyLucyEnvFactory;
import com.luciad.lucy.workspace.ILcyWorkspaceManagerListener;
import com.luciad.lucy.workspace.TLcyWorkspaceManagerEvent;

import samples.lucy.frontend.AFrontend;

/**
 * This extension of <code>AFrontend</code> demonstrates how to create your own custom frontend
 * using internal frames.
 *
 * It automatically loads the 'internal_frames_sample_workspace.lws' workspace file to nicely
 * lay out the map, layer control and map overview.
 *
 * Warnings might appear when loading a workspace that was for example written by TLcyMain.
 * This happens because the default front end (TLcyMain) saves the divider locations of
 * the split panes in the workspace.  Because a custom frontend is used now, the codec
 * delegate registered by TLcyMain is no longer registered, and those split pane divider
 * locations can't be read anymore.  This causes the warning to appear.  It is no harm
 * however, the divider settings will simply be ignored.
 *
 */
public class InternalFramesFrontendMain extends AFrontend {
  private static final String UID = "samples.lucy.frontend.internalframes.InternalFramesFrontendMain";
  private static final String PREFIX = "InternalFramesFrontendMain.";

  private JDesktopPane fDesktopPane = new JDesktopPane();

  public InternalFramesFrontendMain() {
    super(CONFIG_FILE, "samples/frontend/internalframes/internal_frames_sample_workspace_addon.cfg", "samples/frontend/internalframes/InternalFramesFrontendReadMe.cfg");
  }

  public InternalFramesFrontendMain(String aAddOnsFile) {
    super(aAddOnsFile, CONFIG_FILE);
  }

  @Override
  protected ILcyLucyEnv createLucyEnv() {
    //Create the ILcyApplicationPaneFactory
    InternalFrameAppPaneFactory app_pane_fact = new InternalFrameAppPaneFactory(fDesktopPane);

    //Create the ILcyLucyEnv
    ILcyLucyEnv env = new TLcyLucyEnvFactory().createLucyEnv(app_pane_fact, getMenuBar(), true);

    //Pass the environment on the factory
    app_pane_fact.setLucyEnv(env);

    //Init workspace support
    setupWorkspaceSupport(env);

    return env;
  }

  private void setupWorkspaceSupport(ILcyLucyEnv aLucyEnv) {
    //Add codec delegate to (re)store all application panes
    aLucyEnv.getWorkspaceManager().addWorkspaceCodecDelegate(
        new InternalFramesWorkspaceCodecDelegate(UID, PREFIX, fDesktopPane));

    //Make the desktop pane invisible when restoring workspace to avoid flashing frames
    aLucyEnv.getWorkspaceManager().addWorkspaceManagerListener(new ILcyWorkspaceManagerListener() {
      @Override
      public void workspaceStatusChanged(TLcyWorkspaceManagerEvent aEvent) {
        fDesktopPane.setVisible(!getLucyEnv().getWorkspaceManager().isDecodingWorkspace());
      }
    });
  }

  @Override
  protected void initGUI() {
    Container content_pane = getMainFrame().getContentPane();

    //add the desktop pane in the center
    content_pane.setLayout(new BorderLayout());
    content_pane.add(fDesktopPane, BorderLayout.CENTER);

    //Status bar in the south
    JPanel status_bar_wrapper = new JPanel(new BorderLayout());
    status_bar_wrapper.add(getStatusBar().getComponent(), BorderLayout.CENTER);
    status_bar_wrapper.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    content_pane.add(status_bar_wrapper, BorderLayout.SOUTH);
  }

  public static void main(String[] aArgs) {
    final String addOnsFile = retrieveAddOnsFile(aArgs, null);
    final InternalFramesFrontendMain[] frontEnd = new InternalFramesFrontendMain[]{null};
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        if (addOnsFile == null) {
          frontEnd[0] = new InternalFramesFrontendMain();
        } else {
          frontEnd[0] = new InternalFramesFrontendMain(addOnsFile);
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
   *       InternalFramesFrontendMain.main(aArgs, "-addons", "myapp/my_addons.xml");
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
    InternalFramesFrontendMain.main(newArgs);
  }
}
