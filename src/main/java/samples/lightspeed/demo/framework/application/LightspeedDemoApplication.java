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
package samples.lightspeed.demo.framework.application;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import com.jgoodies.forms.FormsSetup;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;

import samples.common.MacUtil;
import samples.common.SamplePanel;
import samples.common.SwingUtil;
import samples.lightspeed.demo.framework.gui.ApplicationPanel;
import samples.lightspeed.demo.framework.gui.LightspeedDemoFrame;
import samples.lightspeed.demo.framework.util.CaseInsensitiveProperties;
import samples.lightspeed.demo.framework.util.CommandLineUtil;

/**
 * Starting point of the demo application.
 */
public class LightspeedDemoApplication extends SamplePanel {

  static {
    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        FormsSetup.setOpaqueDefault(true);
      }
    });
  }

  /**
   * In windowed mode, we add a margin of 100 pixels so that the window does not occupy the
   * whole screen.
   */
  private final static int DEFAULT_MARGIN = 100;

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LightspeedDemoApplication.class);
  private ApplicationPanel fPanel3D;

  private Framework fFramework;

  /**
   * Creates a new LightspeedDemoApplication with the given command line arguments
   *
   * @param args An array containing command line arguments.
   */
  public LightspeedDemoApplication(String[] args) {
    // Parse command-line arguments
    CaseInsensitiveProperties cmdline = new CaseInsensitiveProperties();
    CommandLineUtil.parseCommandLineParametersSFCT(args, cmdline);

    // Create application
    final Framework framework = Framework.getInstance();

    if (Boolean.parseBoolean(framework.getProperty("ui.touch", "false"))) {
      TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    }

    // Add properties of specified config file
    String configFileName = cmdline.getProperty("config", "samples/lightspeed/demo/demo.properties");
    if (configFileName != null) {
      if (sLogger.isTraceEnabled()) {
        sLogger.trace("Config file found: " + configFileName);
      }

      try {
        framework.loadProperties(configFileName);
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not load config file: " + configFileName, e);
      }
    } else {
      throw new IllegalArgumentException("No configuration file defined! To load config file, " + "use command line option -config <path/to/config/file>");
    }

    // Add command-line properties
    for (Object key : cmdline.keySet()) {
      framework.setProperty((String) key, cmdline.getProperty((String) key));
    }

    // Load default dataset
    framework.loadDataSet(framework.getProperty("dataset", "LightspeedDemo"));

    fFramework = Framework.getInstance();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Start the animation manager with the preferred framerate
        double targetFPS = Double.valueOf(fFramework.getProperty("framerate", "30"));
        ALcdAnimationManager.getInstance().setTargetUpdateRate(targetFPS);
        createGUI();
        framework.setActiveTheme(framework.getThemes()[0]);

        // Now that all the views have been created, we can initialize the themes.
        List<ILspView> views = framework.getFrameworkContext().getViews();
        if (views != null) {
          framework.initializeThemes(views);

          // NOTE: we delay building the menus, because some themes might fail during
          //       initialization, in which case they should not be shown in the panels.
          buildMenus();
        } else {
          throw new IllegalStateException("Cannot initialize themes because no ILspView " +
                                          "instances have been registered with the framework.");
        }
      }
    });
    fPanel3D = null;
  }

  protected ApplicationPanel createDemoPanel(boolean a2DView, int aWidth, int aHeight) {
    return new ApplicationPanel(a2DView, aWidth, aHeight);
  }

  public static Dimension getPreferredDimension(GraphicsDevice aDevice) {
    int deviceWidth = aDevice.getDefaultConfiguration().getBounds().getSize().width;
    int deviceHeight = aDevice.getDefaultConfiguration().getBounds().getSize().height;

    int viewWidth = deviceWidth;
    int viewHeight = deviceHeight;

    viewWidth -= DEFAULT_MARGIN;
    viewHeight -= DEFAULT_MARGIN;

    return new Dimension(viewWidth, viewHeight);
  }

  @Override
  protected void createGUI() {
    JFrame frame3D;

    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = environment.getScreenDevices();

    boolean isMacOS = TLcdSystemPropertiesUtil.isMacOS();
    // Check which display mode was entered in the command line
    Dimension d = getPreferredDimension(devices[0]);
    fPanel3D = createDemoPanel(false, d.width, d.height);
    frame3D = new LightspeedDemoFrame(fPanel3D, "LuciadLightspeed Demo", d.width, d.height);
    if (isMacOS) {
      FullScreenUtilities.setWindowCanFullScreen(frame3D, true);
    }
    frame3D.setLocation(devices[0].getDefaultConfiguration().getBounds().getLocation());


    // Create application context
    FrameworkContext appContext = new FrameworkContext();
    if (fPanel3D != null) {
      appContext.addView(fPanel3D.getView());
    }
    fFramework.setFrameworkContext(appContext);

    frame3D.setVisible(true);
    MacUtil.initMacApplication(SwingUtil.sLuciadFrameImage,
                               frame3D);
  }

  private void buildMenus() {
    if (fPanel3D != null) {
      fPanel3D.buildThemeMenu();
      fPanel3D.buildMainMenu();
      fPanel3D.buildZSliderMenu();
    }
  }
}
