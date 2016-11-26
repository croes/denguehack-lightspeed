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
package samples.lucy.frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;

import com.luciad.beans.TLcdBeanGUIFactory;
import com.luciad.beans.swing.TLcdBeanGUIFactorySW;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdGUIManager;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdIconImageUtil;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.gui.swing.TLcdGUIFactorySW;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.MacUtil;
import samples.common.XWindowUtil;
import samples.lucy.gui.status.ShowProgressAction;
import samples.lucy.showreadme.ShowReadMeAddOn;
import samples.lucy.util.ExceptionHandler;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyXMLAddOnLoader;
import com.luciad.lucy.addons.ALcyAddOn;
import com.luciad.lucy.addons.decoders.TLcyDefaultDecodersAddOn;
import com.luciad.lucy.addons.genericmap.TLcyGenericMapAddOn;
import com.luciad.lucy.addons.layercontrol.TLcyLayerControlAddOn;
import com.luciad.lucy.addons.map.TLcyMapAddOn;
import com.luciad.lucy.addons.map.TLcyMapOverviewAddOn;
import com.luciad.lucy.addons.workspace.TLcyWorkspaceAddon;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.gui.TLcyMenuBar;
import com.luciad.lucy.gui.TLcySplashScreenMediator;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.gui.status.TLcyInterruptTaskAction;
import com.luciad.lucy.gui.status.TLcyProgressBar;
import com.luciad.lucy.gui.status.TLcyStatusMessageBar;
import com.luciad.lucy.gui.status.TLcyUserInteractionBlocker;
import com.luciad.lucy.util.TLcyProperties;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.util.TLcdCopyright;
import com.luciad.util.TLcdLicenseError;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Abstract implementation of a custom Lucy frontend. It offers unlimited flexibility
 * over the main class, the user interface, position of the frame, number of frames,
 * ... It also allows you to integrate Lucy within an existing application.
 *
 * It performs these tasks:
 * <ul>
 * <li>It creates a JFrame to put the application in.
 * <li>It creates a TLcyMenuBar with the correct ordering of the items inside it.
 * <li>It creates a status bar.
 * <li>It shows a splash screen telling which add-on is initializing.
 * <li>It shows a pop up dialog if an uncaught exception occurs.
 * <li>It (re)stores the main frame location and size to (from) the user preferences.
 * <li>Depending on the used constructor, it loads the add-ons, either hardcoded or from file.
 * </ul>
 *
 * These are the responsibilities of subclasses:
 * <ul>
 * <li>Creating the {@code ILcyLucyEnv} instance.  See {@link #createLucyEnv()}.
 * <li>Putting the user interface together, for example laying out the menu bar, containers for application panes, ...
 * See {@link #initGUI()}.
 * </ul>
 *
 * The warning that appears about some missing codec delegates when you start this sample
 * is happening because the default frontend (TLcyMain) saves the divider locations of
 * the split panes in the workspace.  Because a custom frontend is used now, the codec
 * delegate registered by TLcyMain is no longer registered, and those split pane divider
 * locations can't be read anymore.  This causes the warning to appear.  It is no harm
 * however, the divider settings will simply be ignored.
 *
 */
public abstract class AFrontend {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(AFrontend.class.getName());

  /**
   * The default application configuration file.
   * @see #getPreferences()
   */
  public static final String CONFIG_FILE = "lucy/lucy.cfg";
  private static final String SPLASH_DISPLAYED = "splashDisplayed";
  private static final String SPLASH_IMAGE_FILE_NAME_KEY = "splashImageFileName";
  private static final String SPLASH_TEXT_DISPLAYED_KEY = "splashTextDisplayed";
  private static final String SPLASH_RELATIVE_TEXT_LOCATION_KEY = "splashRelativeTextLocation";
  private static final String SPLASH_FONT_KEY = "splashFont";
  private static final String SPLASH_FONT_COLOR_KEY = "splashFontColor";
  private static final String SPLASH_PROGRESS_BAR_DISPLAYED_KEY = "splashProgressBarDisplayed";
  private static final String SPLASH_PROGRESS_BAR_HEIGHT_KEY = "splashProgressBarHeight";
  private static final String SPLASH_PROGRESS_BAR_COLOR_KEY = "splashProgressBarColor";

  /**
   * True to support heavy weight components. This is only needed when using
   * heavy weight components, such as 3D.  When 3D is not used, this flag can
   * be false, but leaving it true doesn't harm.
   */
  protected static final boolean SUPPORT_HEAVY_WEIGHT = true;

  private JFrame fMainFrame;
  private TLcyMenuBar fMenuBar;
  private ILcyLucyEnv fLucyEnv;

  private final String fWorkspaceConfigFile;
  private final String fReadMeAddOnConfigFile;
  private final String fMapConfigFile;
  private final String fAddOnsFile;
  private TLcyToolBar fStatusBar;
  private TLcyXMLAddOnLoader fXMLAddOnLoader;
  private final ALcyProperties fPreferences;

  /**
   * Utility method which retrieves the add-ons file from the command line args. The add-ons file should be
   * specified as {@code -addons path/to/addons/file}
   *
   * @param aCommandLineArgs The command line args
   * @param aDefault The default value, used if none is specified in the given arguments.
   *
   * @return The path to the add-ons file, or {@code null} when no path is specified
   */
  public static String retrieveAddOnsFile(String[] aCommandLineArgs, String aDefault) {
    for (int i = 0; i < aCommandLineArgs.length - 1; i++) {
      String commandLineArg = aCommandLineArgs[i];
      if ("-addons".equals(commandLineArg) && !aCommandLineArgs[i + 1].isEmpty()) {
        return aCommandLineArgs[i + 1];
      }
    }
    return aDefault;
  }

  /**
   * Utility method which retrieves the configuration file from the command line args. The config file should be
   * specified as {@code -properties path/to/config/file}
   *
   * @param aCommandLineArgs The command line args
   * @param aDefault The default value, used if none is specified in the given arguments.
   *
   * @return The path to the configuration file, or {@code null} when no path is specified
   */
  public static String retrieveConfigurationFile(String[] aCommandLineArgs, String aDefault) {
    for (int i = 0; i < aCommandLineArgs.length - 1; i++) {
      String commandLineArg = aCommandLineArgs[i];
      if ("-properties".equals(commandLineArg) && !aCommandLineArgs[i + 1].isEmpty()) {
        return aCommandLineArgs[i + 1];
      }
    }
    return aDefault;
  }

  /**
   * Constructor for front-ends whose add-ons are configured using an XML file.
   *
   * @param aAddOnsFile path to the add-ons file.
   * @param aConfigFile path to the application's configuration file
   */
  protected AFrontend(String aAddOnsFile, String aConfigFile) {
    this(null, null, null, aAddOnsFile, aConfigFile);
  }

  /**
   * Constructor for front-ends that don't use an add-ons file.
   * Instead, a series of hard-coded add-ons are loaded.
   *
   * @param aConfigFile path to the application's configuration file
   * @param aWorkspaceConfigFile path to the configuration file for the workspace add-on
   * @param aReadMeAddOnConfigFile path to configuration file for the readme add-on
   * @see #loadAddOnsHardCoded(ILcyLucyEnv)
   */
  protected AFrontend(String aConfigFile, String aWorkspaceConfigFile, String aReadMeAddOnConfigFile) {
    this(aWorkspaceConfigFile, aReadMeAddOnConfigFile, "lucy/map/map_addon.cfg", null, aConfigFile);
  }

  private AFrontend(String aWorkspaceConfigFile, String aReadMeAddOnConfigFile, String aMapConfigFile, String aAddonsFile, String aConfigFile) {
    fReadMeAddOnConfigFile = aReadMeAddOnConfigFile;
    fWorkspaceConfigFile = aWorkspaceConfigFile;
    fMapConfigFile = aMapConfigFile;
    fAddOnsFile = aAddonsFile;
    fPreferences = decodePreferences(aConfigFile);

    initSwing();
  }

  /**
   * Starts this front-end.  This method can be called from any thread.
   */
  public final void startup() {

    XWindowUtil.workAroundWindowManagerProblems();
    MacUtil.installWorkarounds();
    MacUtil.initMacSystemProperties();

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        //Show a popup dialog when an uncaught exception occurs.
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
      }
    });

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        TLcdCopyright.setCopyright("");
      }
    });


    //Follow Swing guidelines to create frame etc. on event dispatch thread.
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try {
          initFrameAndMenu();

          fLucyEnv = createLucyEnv();
          installSplashScreen();

          initFrameTitle();
          initStatusBar();

          //Indicate to possible lucy env listeners that initializing the lucy env has started.
          fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_INITIALIZING);

          //Register the top level component to ILcyLucyEnv. This allows add-ons (such
          //as the look&feel add-on) to retrieve all top level components.
          fLucyEnv.addTopLevelComponent(fMainFrame);

          //Wire the lucy help manager to the menu bar
          fMenuBar.setHelpManager(fLucyEnv.getHelpManager());

          loadAddOns();

          initGUI();
          initializeExitMenuItem();
          FrontEndUtil.restoreFrameState(getMainFrame(), AFrontend.class.getName(), fLucyEnv);
          getMainFrame().setVisible(true);

          //Indicate to possible lucy env listeners that the initialization has finished.
          fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_INITIALIZED);

        } catch (TLcdLicenseError e) {
          if (fMainFrame != null) {
            //In case of a license error, allow AWT to shutdown if no other apps are running.
            fMainFrame.dispose();
          }
          throw e;
        } catch (TLcyVetoException veto) {
          //somebody veto'ed our state changes
          throw new RuntimeException("Throwing TLcyVetoException's when Lucy switches state is only supported when switching from STATE_INITIALIZED to STATE_CLOSING. "
                                     + "Throwing an exception on other state transitions will leave Lucy in an undefined state.");
        }
      }
    });
  }

  /**
   * The application's configuration preferences. AFrontend will use it to read the menu and status bar ordering from.
   * @return the preferences
   */
  protected ALcyProperties getPreferences() {
    return fPreferences;
  }

  public JFrame getMainFrame() {
    return fMainFrame;
  }

  public TLcyMenuBar getMenuBar() {
    return fMenuBar;
  }

  public ILcyLucyEnv getLucyEnv() {
    return fLucyEnv;
  }

  public TLcyToolBar getStatusBar() {
    return fStatusBar;
  }

  private void loadAddOns() {
    //show readme
    //use the ShowReadMeAddOn
    if (fReadMeAddOnConfigFile != null) {
      ShowReadMeAddOn showReadMeAddOn = new ShowReadMeAddOn();
      //use the supplied configuration file
      loadAddOn(showReadMeAddOn, "Show ReadMe", fReadMeAddOnConfigFile, fLucyEnv);
    }

    if (fAddOnsFile != null) {
      loadAddOnsFromFile(fLucyEnv, fAddOnsFile);
    } else {
      loadAddOnsHardCoded(fLucyEnv);
    }
  }

  /**
   * Creates a <code>ILcyLucyEnv</code> based on the type of GUI we want to have.  A {@code ILcyLucyEnv}
   * is created using either {@link com.luciad.lucy.TLcyTabbedPaneLucyEnvFactory} or
   * {@link com.luciad.lucy.TLcyLucyEnvFactory}. This method
   * is normally implemented in close relation with {@code initGUI}, as the panels created by the
   * <code>ILcyApplicationPaneFactory</code> of the {@code ILcyLucy} must somehow be added to the
   * user interface.
   *
   * @return The newly create {@code ILcyLucyEnv}.
   */
  protected abstract ILcyLucyEnv createLucyEnv();

  /**
   * Initializes the user interface by laying out all components, excluding {@link #getMenuBar()}.  They
   * can be put in the main frame (see {@link #getMainFrame()}, or in additional frames.  This method
   * is normally implemented in close relation with <code>createLucyEnv</code>, as the {@code ILcyLucyEnv}
   * will have to know how to put the panels of the add-ons in the user interface
   * (see also {@link com.luciad.lucy.gui.ILcyApplicationPaneFactory}).
   */
  protected abstract void initGUI();

  /**
   * Loads the add-ons from the given xml file into lucy env.
   * @param aLucyEnv The lucy env to load the add-ons into.
   * @param aAddonsFile The file name (relative to classpath) of the .xml file containing which add-ons
   * to load.
   */
  private void loadAddOnsFromFile(ILcyLucyEnv aLucyEnv, String aAddonsFile) {
    fXMLAddOnLoader = new TLcyXMLAddOnLoader(aLucyEnv, aAddonsFile);
    fXMLAddOnLoader.loadAddOns();
  }

  /**
   * Load some hardcoded add-ons into lucy env.
   * @param aLucyEnv The lucy env to load the add-ons into.
   */
  private void loadAddOnsHardCoded(ILcyLucyEnv aLucyEnv) {
    //load the default decoders add-on: allows lucy to recognize basic data formats, such as shp, mif, ...
    loadAddOn(new TLcyDefaultDecodersAddOn(), "Default Formats", "lucy/decoders/default_decoders.cfg", aLucyEnv);

    //load the map add-on
    loadAddOn(new TLcyMapAddOn(), "Map", fMapConfigFile, aLucyEnv);

    //load the overview add-on
    loadAddOn(new TLcyMapOverviewAddOn(), "Map Overview", "lucy/map/mapoverview_addon.cfg", aLucyEnv);

    //load the layer control add-on
    loadAddOn(new TLcyLayerControlAddOn(), "Layer Control", "lucy/layercontrol/TLcyLayerControlAddOn.cfg", aLucyEnv);

    //load the overview add-on
    loadAddOn(new TLcyGenericMapAddOn(), "Generic map add-on", "lucy/genericmap/TLcyGenericMapAddOn.cfg", aLucyEnv);

    //load the workspace add-on
    loadAddOn(new TLcyWorkspaceAddon(), "Workspace",
              fWorkspaceConfigFile != null ? fWorkspaceConfigFile : "lucy/workspace/workspace_addon.cfg", aLucyEnv);
  }

  private void loadAddOn(Object aAddOn, String aUntranslatedName, String aConfigSourceName, ILcyLucyEnv aLucyEnv) {
    ALcyAddOn addOn;

// Uncommented this code if you still have add-ons implementing ILcyAddOnDecoder
//    if ( aAddOn instanceof ILcyAddOnDecoder ) {
//      ILcyAddOnDecoder addOnDecoder = ( ILcyAddOnDecoder ) aAddOn;
//      addOn = addOnDecoder.decodeAddOn( aConfigSourceName );
//    }
//    else

    if (aAddOn instanceof ALcyAddOn) {
      addOn = (ALcyAddOn) aAddOn;
      addOn.setConfigSourceName(aConfigSourceName);
    } else {
      throw new IllegalArgumentException("aAddOn must extend ALcyAddOn");
    }

    addOn.setDisplayName(TLcyLang.getString(aUntranslatedName));
    aLucyEnv.plugAddOn(addOn);
  }

  private ALcyProperties decodePreferences(String aPropertiesFile) {
    try {
      return new TLcyStringPropertiesCodec().decode(aPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Could not load preferences", e);
    }
    return new TLcyProperties();
  }

  /**
   * Sets up the splash screen.
   */
  private void installSplashScreen() {
    if (getPreferences().getBoolean(SPLASH_DISPLAYED, true)) {
      String splash_image_file_name = getPreferences().getString(SPLASH_IMAGE_FILE_NAME_KEY, "lucy/splash_screen.png");

      boolean showText = getPreferences().getBoolean(SPLASH_TEXT_DISPLAYED_KEY, true);
      Font font = getFont(getPreferences(), SPLASH_FONT_KEY, new Font("Dialog", Font.PLAIN, 11));
      Point text_location = getPoint(getPreferences(), SPLASH_RELATIVE_TEXT_LOCATION_KEY, new Point(2, -2));
      Color font_color = getPreferences().getColor(SPLASH_FONT_COLOR_KEY, Color.white);

      boolean showProgressBar = getPreferences().getBoolean(SPLASH_PROGRESS_BAR_DISPLAYED_KEY, false);
      int progressBarHeight = getPreferences().getInt(SPLASH_PROGRESS_BAR_HEIGHT_KEY, 2);
      Color progressBarColor = getPreferences().getColor(SPLASH_PROGRESS_BAR_COLOR_KEY, Color.white);

      TLcySplashScreenMediator.install(fLucyEnv, splash_image_file_name,
                                       showText, text_location, font, font_color,
                                       showProgressBar, progressBarHeight, progressBarColor);
    }
  }

  private static Font getFont(ALcyProperties aProperties, String aKey, Font aDefaultFont) {
    String name = aProperties.getString(aKey + ".name", null);
    int defaultValue1 = aDefaultFont == null ? Font.PLAIN : aDefaultFont.getStyle();
    int style = aProperties.getInt(aKey + ".style", defaultValue1);
    int defaultValue = aDefaultFont == null ? 10 : aDefaultFont.getSize();
    int size = aProperties.getInt(aKey + ".size", defaultValue);
    if (name != null) {
      return new Font(name, style, size);
    } else {
      return aDefaultFont;
    }
  }

  private static Point getPoint(ALcyProperties aProperties, String aPropName, Point aDefaultValue) {
    int[] coords = aProperties.getIntArray(aPropName, aDefaultValue == null ? null : new int[]{aDefaultValue.x, aDefaultValue.y});
    if (coords != null && coords.length == 2) {
      return new Point(coords[0], coords[1]);
    } else {
      return aDefaultValue;
    }
  }

  /**
   * Initialize the frame and the menu bar.
   */
  private void initFrameAndMenu() {
    //Create a frame
    fMainFrame = new JFrame();
    String[] iconFileNames = getPreferences().getStringArray(
        "iconImageFileName", new String[]{"images/luciad_icon32.png"});
    List<Image> images = convertToImages(iconFileNames);
    if (images != null && !images.isEmpty()) {
      fMainFrame.setIconImages(images);
    }

    //Properly shut down the lucy env if the frame is closed
    fMainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    fMainFrame.addWindowListener(new WindowAdapter() {
      private ILcdAction fExitAction;

      @Override
      public void windowClosing(WindowEvent aEvent) {
        if (fExitAction == null) {
          fExitAction = createExitAction();
        }
        if (fExitAction.isEnabled()) {
          fExitAction.actionPerformed(new ActionEvent(aEvent.getSource(), ActionEvent.ACTION_PERFORMED, "Close"));
        }
      }
    });

    //Create and add the menu bar.
    JMenuBar swing_menu = new JMenuBar();
    fMenuBar = createMainMenuBar(swing_menu);
    fMainFrame.setJMenuBar(swing_menu);

    MacUtil.initMacApplication(images,
                               fMainFrame);
  }

  public static List<Image> convertToImages(String[] aIconFileNames) {
    ArrayList<Image> images = new ArrayList<>();
    if (aIconFileNames.length > 0 && !"".equals(aIconFileNames[0])) {
      TLcdIconImageUtil util = new TLcdIconImageUtil();
      for (String iconFilename : aIconFileNames) {
        Image image = util.loadImage(iconFilename);
        if (image != null) {
          images.add(image);
        }
      }
    }
    return images;
  }

  /**
   * Links the title of the main frame to the loaded workspace.
   */
  private void initFrameTitle() {
    String title = getPreferences().getString("frameTitle", "Lucy - Powered by Luciad - [ {0} ]");
    final MessageFormat format = new MessageFormat(TLcyLang.getString(title));
    String workspace_name = fLucyEnv.getWorkspaceManager().getWorkspaceStorageName();
    fMainFrame.setTitle(format.format(new Object[]{workspace_name == null ? "" : workspace_name}));

    fLucyEnv.getWorkspaceManager().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        if ("workspaceStorageName".equals(evt.getPropertyName())) {
          //Make sure this happens on the EDT
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              fMainFrame.setTitle(format.format(new Object[]{evt.getNewValue() == null ? "" : (String) evt.getNewValue()}));
            }
          });
        }
      }
    });
  }

  /**
   * Initialize the status bar.
   */
  private void initStatusBar() {
    //Block the user interface when needed
    fLucyEnv.addStatusListener(new TLcyUserInteractionBlocker(fMainFrame));

    //Shows status messages
    TLcyStatusMessageBar message_bar = new TLcyStatusMessageBar(fLucyEnv);
    fLucyEnv.addStatusListener(message_bar);

    //Shows a progress bar for lengthy operations
    TLcyProgressBar progress_bar = new TLcyProgressBar();
    fLucyEnv.addStatusListener(progress_bar);

    //Shows a cancel button for lengthy operations
    TLcyInterruptTaskAction interruptTaskAction = new TLcyInterruptTaskAction(fLucyEnv);
    getLucyEnv().addStatusListener(interruptTaskAction);

    //Using a tool bar here to automatically have the separators between the groups.
    final Box.Filler filler = new Box.Filler(null, null, null);
    JToolBar tb = new JToolBar() {
      @Override
      public void updateUI() {
        super.updateUI();

        //Workaround for synth look and feel bugs: synth does not use BoxLayout,
        //nor does synth look & feel respect the set layout.  It has to be set
        //every time the look and feel changes
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        //Fixing nasty layout problem: interrupt button is set visible/invisible, but we don't want
        //its presence or absence to change the height of the tool bar
        int minHeight = new JButton(new TLcdSWIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.CANCEL_CHANGES_ICON), 12, 12))).getPreferredSize().height;
        Dimension dim = new Dimension(5, minHeight);
        filler.changeShape(dim, dim, dim);
      }
    };
    tb.setFloatable(false);
    fStatusBar = new TLcyToolBar(tb);

    //mediate the status bar with the action bar manager
    fStatusBar.setProperties(fPreferences.subset("TLcyMain.mainStatusBar."));
    TLcyActionBarMediatorBuilder.newInstance(fLucyEnv.getUserInterfaceManager().getActionBarManager())
                                .sourceActionBarIncludingActiveContexts("mainStatusBar")
                                .targetActionBar(fStatusBar)
                                .bidirectional()
                                .mediate();

    //Lay out the components
    TLcyGroupDescriptor status_group = new TLcyGroupDescriptor("statusGroup");
    fStatusBar.insertComponent(filler, status_group);
    fStatusBar.insertComponent(message_bar, status_group);
    fStatusBar.insertComponent(Box.createHorizontalGlue(), status_group);
    fStatusBar.insertAction(new ShowProgressAction(fLucyEnv), status_group);
  }

  private TLcyMenuBar createMainMenuBar(JMenuBar fSwingMenu) {
    TLcyMenuBar menu_bar = new TLcyMenuBar(fSwingMenu);

    //If the menu items of the main menu bar need to be ordered correctly, we
    //need to apply an ordering. Luckily there is such an ordering available
    //in lucy.cfg, so lets use that ordering.
    menu_bar.setProperties(fPreferences.subset("TLcyMain.menuBar."));

    return menu_bar;
  }

  private void initializeExitMenuItem() {
    ExitAction exitAction = createExitAction();
    exitAction.putValue(TLcyActionBarUtil.ID_KEY, "TLcyMain.exitAction");
    TLcyActionBarUtil.insertInConfiguredActionBars(exitAction,
                                                   null,
                                                   fLucyEnv.getUserInterfaceManager().getActionBarManager(),
                                                   getPreferences());
  }

  private ExitAction createExitAction() {
    return new ExitAction(fLucyEnv, fXMLAddOnLoader, fMainFrame, AFrontend.class.getName());
  }

  /**
   * Performs basic setup for Swing.  This method must be invoked before any Swing components
   * are created.
   */
  private void initSwing() {

    // set GUI factories to use Swing
    TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());
    TLcdGUIManager.setSharedGUIFactory(new TLcdGUIFactorySW());
    TLcdBeanGUIFactory.setSharedBeanGUIFactory(new TLcdBeanGUIFactorySW());

    // Tell Swing to use heavy weight popups if needed.
    if (SUPPORT_HEAVY_WEIGHT) {
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }

    //Increase tooltip time
    ToolTipManager.sharedInstance().setDismissDelay(15 * 1000);
  }
}
