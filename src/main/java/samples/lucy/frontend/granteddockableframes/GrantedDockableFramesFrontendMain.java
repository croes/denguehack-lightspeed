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
package samples.lucy.frontend.granteddockableframes;

import java.awt.BorderLayout;
import java.awt.Container;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyLucyEnvFactory;
import com.luciad.lucy.gui.TLcyDockableApplicationPaneFactory;

import samples.lucy.frontend.AFrontend;

/**
 * This extension of <code>AFrontend</code> demonstrates how to create your own custom frontend
 * using dockable frames. The docking functionality of this frontend is identical to the 'DOCKING'
 * mode of TLcyMain.
 * <p/>
 * This sample does not require a JIDE license as it does not allow to program to the JIDE libraries.
 * It does allow to customize the overall layout to your needs.
 *
 */
public class GrantedDockableFramesFrontendMain extends AFrontend {
  private TLcyDockableApplicationPaneFactory fAppPaneFactory;

  public GrantedDockableFramesFrontendMain() {
    super(CONFIG_FILE,
          "samples/frontend/granteddockableframes/granted_dockable_frames_sample_workspace_addon.cfg",
          "samples/frontend/granteddockableframes/GrantedDockableFramesReadMe.cfg");
  }

  public GrantedDockableFramesFrontendMain(String aAddOnsFile) {
    super(aAddOnsFile, CONFIG_FILE);
  }

  @Override
  protected ILcyLucyEnv createLucyEnv() {
    //Create the ILcyApplicationPaneFactory
    fAppPaneFactory = new TLcyDockableApplicationPaneFactory(getMainFrame(), SUPPORT_HEAVY_WEIGHT, true, false);

    //Create the ILcyLucyEnv
    ILcyLucyEnv env = new TLcyLucyEnvFactory().createLucyEnv(fAppPaneFactory, getMenuBar(), true);

    //Pass the Lucy environment
    fAppPaneFactory.setLucyEnv(env);

    return env;
  }

  @Override
  protected void initGUI() {
    //Add the content
    Container content_pane = getMainFrame().getContentPane();
    content_pane.setLayout(new BorderLayout());
    content_pane.add(fAppPaneFactory.getComponent(), BorderLayout.CENTER);
    content_pane.add(getStatusBar().getComponent(), BorderLayout.SOUTH);
  }

  public static void main(String[] aArgs) {
    String addOnsFile = retrieveAddOnsFile(aArgs, null);
    if (addOnsFile == null) {
      new GrantedDockableFramesFrontendMain().startup();
    } else {
      new GrantedDockableFramesFrontendMain(addOnsFile).startup();
    }
  }

  /**
   * Convenience method to make it easy to programmatically append additional arguments to the main method.
   * A typical use case is to start with a custom add-ons file:
   *
   * <pre class="code">
   *   public class MyMain {
   *     public static void main(String[] aArgs) {
   *       GrantedDockableFramesFrontendMain.main(aArgs, "-addons", "myapp/my_addons.xml");
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
    GrantedDockableFramesFrontendMain.main(newArgs);
  }
}
