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

import javax.swing.JOptionPane;

import com.jidesoft.docking.DockableHolderPanel;
import com.jidesoft.utils.Lm;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyLucyEnvFactory;
import com.luciad.lucy.util.language.TLcyLang;

import samples.lucy.frontend.AFrontend;

/**
 * This extension of <code>AFrontend</code> demonstrates how to create your own custom frontend
 * using dockable frames. The docking functionality of this frontend is identical to the 'DOCKING'
 * mode of TLcyMain.
 * <p/>
 * This sample uses the JIDE docking framework, which requires a license to run. This license can
 * be purchased from JIDE's site: http://www.jidesoft.com/. This license should then be entered in
 * the dialog that is shown when starting this sample.
 * <p/>
 * Alternatively you can download the evaluation jars from JIDE's site and replace jide-docking.jar
 * and jide-common.jar in the lib directory of the LuciadLightspeed distribution.
 * <p/>
 * The sample automatically loads the 'dockable_frames_sample_workspace_addon.lws' workspace file to
 * nicely lay out the map, layer control and map overview.
 */
public class DockableFramesFrontendMain extends AFrontend {
  private static final String UID = "com.luciad.lucy.DockingFrontendMain";
  private static final String PREFIX = "DockingFrontendMain.";

  private DockableHolderPanel fDockableHolder;

  public DockableFramesFrontendMain() {
    super(CONFIG_FILE, "samples/frontend/dockableframes/dockable_frames_sample_workspace_addon.cfg", "samples/frontend/dockableframes/DockableFramesFrontendReadMe.cfg");
  }

  public DockableFramesFrontendMain(String aAddOnsFile) {
    super(aAddOnsFile, CONFIG_FILE);
  }

  public DockableHolderPanel getDockableHolder() {
    return fDockableHolder;
  }

  private void initJIDELicense() {
    JIDELicensePanel license_panel = new JIDELicensePanel();
    int choice = JOptionPane.showConfirmDialog(
        getMainFrame(),
        license_panel, TLcyLang.getString("JIDE license"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
                                              );
    if (choice != JOptionPane.OK_OPTION) {
      System.exit(1);
    }

    String company = license_panel.getCompany();
    String product = license_panel.getProduct();
    String license = license_panel.getLicense();

    if (isEmptyString(company) || isEmptyString(product) || isEmptyString(license)) {
      JOptionPane.showMessageDialog(
          getMainFrame(),
          TLcyLang.getString("All license information should be specified"),
          TLcyLang.getString("Missing license information"),
          JOptionPane.ERROR_MESSAGE
                                   );
      System.exit(1);
    }

    Lm.verifyLicense(company, product, license);
  }

  private static boolean isEmptyString(String aString) {
    return aString == null || "".equals(aString.trim());
  }

  @Override
  protected ILcyLucyEnv createLucyEnv() {
    //only ask for JIDE license information if the com.luciad.lucy.verifyJIDELicense is not set to false
    //some customers might already work with JIDE and registered their own license
    //This is the same property as documented in TLcyDockableApplicationPaneFactory
    if (!"false".equals(System.getProperty("com.luciad.lucy.verifyJIDELicense"))) {
      initJIDELicense();
    }
    fDockableHolder = DockableHolderFactory.createDockableHolder(getMainFrame(), SUPPORT_HEAVY_WEIGHT, false);

    //Create the ILcyApplicationPaneFactory
    DockableFrameAppPaneFactory app_pane_fact = new DockableFrameAppPaneFactory(fDockableHolder.getDockingManager(), true);

    //Create the ILcyLucyEnv
    ILcyLucyEnv env = new TLcyLucyEnvFactory().createLucyEnv(app_pane_fact, getMenuBar(), true);

    //Pass the Lucy environment
    app_pane_fact.setLucyEnv(env);

    //Init workspace support
    setupWorkspaceSupport(env);

    return env;
  }

  private void setupWorkspaceSupport(ILcyLucyEnv aLucyEnv) {
    // Add codec delegate to (re)store all application panes.
    aLucyEnv.getWorkspaceManager().addWorkspaceCodecDelegate(
        new DockableWorkspaceCodecDelegate(aLucyEnv, UID, PREFIX, fDockableHolder));
  }

  @Override
  protected void initGUI() {
    //Add the content
    Container content_pane = getMainFrame().getContentPane();
    content_pane.setLayout(new BorderLayout());
    content_pane.add(fDockableHolder, BorderLayout.CENTER);
    content_pane.add(getStatusBar().getComponent(), BorderLayout.SOUTH);
  }

  public static void main(String[] aArgs) {
    String addOnsFile = retrieveAddOnsFile(aArgs, null);
    if (addOnsFile == null) {
      new DockableFramesFrontendMain().startup();
    } else {
      new DockableFramesFrontendMain(addOnsFile).startup();
    }
  }

  /**
   * Convenience method to make it easy to programmatically append additional arguments to the main method.
   * A typical use case is to start with a custom add-ons file:
   *
   * <pre class="code">
   *   public class MyMain {
   *     public static void main(String[] aArgs) {
   *       DockableFramesFrontendMain.main(aArgs, "-addons", "myapp/my_addons.xml");
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
    DockableFramesFrontendMain.main(newArgs);
  }
}
