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
package samples.lightspeed.demo.framework.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdCopyright;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.aboveterrain.TLspAboveTerrainCameraConstraint3D;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.services.effects.ALspGraphicsEffect;
import com.luciad.view.lightspeed.services.effects.ALspLight;
import com.luciad.view.lightspeed.services.effects.TLspAmbientLight;
import com.luciad.view.lightspeed.services.effects.TLspFog;
import com.luciad.view.lightspeed.services.effects.TLspHeadLight;
import com.luciad.view.lightspeed.swing.TLspScaleIndicator;
import com.luciad.view.lightspeed.swing.TLspViewComponentPrintable;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.common.HaloLabel;
import samples.common.MetaKeyUtil;
import samples.common.gui.blacklime.BlackLimeLookAndFeel;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.ProgressUtil;
import samples.lightspeed.common.FullScreenAction;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.LuciadLogoIcon;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.demo.application.data.standard.DefaultTheme;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.data.themes.ThemeAnimation;
import samples.lightspeed.demo.framework.gui.action.ExitAction;
import samples.lightspeed.demo.framework.gui.menu.FocusIcon;
import samples.lightspeed.demo.framework.gui.menu.MainMenu;
import samples.lightspeed.demo.framework.gui.menu.ScalableButton;
import samples.lightspeed.demo.framework.gui.menu.SlideMenu;
import samples.lightspeed.demo.framework.gui.menu.SlideMenuManager;
import samples.lightspeed.demo.framework.gui.menu.SubMenuBuilder;
import samples.lightspeed.demo.framework.gui.menu.TiledThemeMenu;
import samples.lightspeed.demo.framework.util.CameraFileUtil;
import samples.lightspeed.demo.simulation.SimulationSupport;
import samples.lightspeed.printing.PrintPreviewAction;

/**
 * Base class for the GUI components that contain the actual views.
 */
public class ApplicationPanel extends JPanel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ApplicationPanel.class);

  static {
    try {
      BlackLimeLookAndFeel.install(false);
    } catch (Exception e) {
      sLogger.error("Could not set Nimbus Look and Feel!", e);
    }
  }

  // GUI attributes
  private TLspAWTView fAWTViewPanel;

  // Indicates whether this panel holds a 2d view (true) or a 3d view (false)
  private boolean fContains2DView;
  private SlideMenuManager fSlideMenuManager;
  private JPanel fSouthComponentDock;

  private LspOpenSupport fLspOpenSupport;

  /**
   * Creates a demo panel for the given application. Based on the given <code>a2DView</code>
   * flag, a 2D (<code>true</code>) or 3D (<code>false</code>) view will be set on the
   * panel.
   *
   * @param a2DView flag indicating whether the panel will contain a 2D or 3D view
   *
   */
  public ApplicationPanel(boolean a2DView, int aWidth, int aHeight) {
    fContains2DView = a2DView;
    createView(aWidth, aHeight);
    addData();
    buildGUI();
  }

  /**
   * Returns the view contained by this panel.
   *
   * @return the view contained by this panel
   */
  public TLspAWTView getView() {
    return fAWTViewPanel;
  }

  /**
   * Creates the view that is contained by this panel. By default, this method creates a
   * view that has a grid reference with an equidistant cylindrical projection.
   */
  protected void createView(int aWidth, int aHeight) {
    TLcdCopyright.getMessage();
    final Framework framework = Framework.getInstance();

    // Create the view
    fAWTViewPanel = TLspViewBuilder.newBuilder()
                                   .glResourceCacheMaxEntries(Integer.parseInt(framework.getProperty("glResourceCacheMaxEntries", "-1")))
                                   .size(aWidth, aHeight)
                                   .resolutionScale(Double.parseDouble(framework.getProperty("resolutionScale", "1.0")))
                                   .background(new Color(Integer.parseInt(framework.getProperty("background.color", "FFFFFF"), 16)))
                                   .buildAWTView();

    // Set up drag&drop file open support.
    fLspOpenSupport = new LspOpenSupport(fAWTViewPanel);
    fLspOpenSupport.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading data"));

    Iterable<ILspLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILspLayerFactory.class);
    fAWTViewPanel.setLayerFactory(new TLspCompositeLayerFactory(layerFactories) {
      @Override
      public Collection<ILspLayer> createLayers(ILcdModel aModel) {
        Collection<ILspLayer> layers = super.createLayers(aModel);
        Framework.getInstance().registerLayers(
            aModel.getModelDescriptor().getSourceName(),
            fAWTViewPanel,
            layers
        );
        return layers;
      }
    });

    // Set world reference
    TLcdGridReference world_reference = new TLcdGridReference();
    world_reference.setGeodeticDatum(new TLcdGeodeticDatum());
    world_reference.setProjection(new TLcdEquidistantCylindrical());
    world_reference
        .setUnitOfMeasure(Double.parseDouble(framework.getProperty("unitOfMeasure", "1.0")));
    fAWTViewPanel.setXYZWorldReference(world_reference);

    // Add key listener for Undo/Redo
    fAWTViewPanel.getHostComponent().addKeyListener(new UndoRedoListener(framework));
    fAWTViewPanel.getHostComponent().addKeyListener(new FXToggleListener(fAWTViewPanel));

    if (fContains2DView) {
      create2DView();
    } else {
      create3DView();
    }

    Collection<ALspGraphicsEffect> fx = getView().getServices().getGraphicsEffects();
    fx.add(new TLspHeadLight(getView()));
    fx.add(new TLspAmbientLight(new Color(64, 64, 64)));
    fx.add(new TLspFog(getView()));
  }

  private void create2DView() {
    TLcdLonLatHeightPoint llhOrigin = new TLcdLonLatHeightPoint(0, 0, 0);
    TLcdXYZPoint worldOrigin = new TLcdXYZPoint();
    TLcdDefaultModelXYZWorldTransformation transform = new TLcdDefaultModelXYZWorldTransformation();
    transform.setModelReference(new TLcdGeodeticReference());
    transform.setXYZWorldReference(getView().getXYZWorldReference());
    try {
      transform.modelPoint2worldSFCT(llhOrigin, worldOrigin);
    } catch (TLcdOutOfBoundsException e) {
    }

    TLspViewXYZWorldTransformation2D v2w = new TLspViewXYZWorldTransformation2D(getView());
    v2w.lookAt(worldOrigin, new Point(2560 / 2, 1600 / 2), 6.2e-5, 0.0);

    getView().setViewXYZWorldTransformation(v2w);
    getView().getHostComponent().addKeyListener(new WriteCameraStateKeyListener(getView()));

    getView().addViewListener(new ALspViewAdapter() {
      @Override
      public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
        try {
          aView.removeViewListener(this);
          double[] parameters = CameraFileUtil.read2DCamera(ThemeAnimation.get2DCameraSourceName("Default"));
          if (parameters != null) {
            TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) aView.getViewXYZWorldTransformation();
            v2w.lookAt(new TLcdXYPoint(parameters[0], parameters[1]),
                       new Point((int) (parameters[2] * v2w.getWidth()), (int) (parameters[3] * v2w.getHeight())),
                       parameters[4],
                       parameters[5],
                       parameters[6]);
          }
        } catch (Throwable t) {
          sLogger.error("Could not initialize 2D camera" + (t.getMessage() == null ? "" : ", reason: " + t.getMessage()), t);
        }
      }
    });
  }

  private void create3DView() {
    TLcdGeocentricReference geoc = new TLcdGeocentricReference(new TLcdGeodeticDatum());
    getView().setXYZWorldReference(geoc);

    TLcdXYZPoint lookAtWorldPoint = new TLcdXYZPoint();
    TLcdGeoReference2GeoReference geo2geo = new TLcdGeoReference2GeoReference(new TLcdGeodeticReference(), geoc);
    try {
      geo2geo.sourcePoint2destinationSFCT(new TLcdLonLatPoint(-59.25, 17), lookAtWorldPoint);
    } catch (TLcdOutOfBoundsException e) {
      // Look at center of the earth if transformation fails
      lookAtWorldPoint.move3D(0, 0, 0);
    }

    // Do not allow the camera to enter the terrain.
    TLspViewXYZWorldTransformation3D w2v = new TLspViewXYZWorldTransformation3D(getView());
    getView().setViewXYZWorldTransformation(w2v);
    w2v.addConstraint(new TLspAboveTerrainCameraConstraint3D());
    w2v.lookAt(lookAtWorldPoint, 6e6, 30, -60, 0);

    //getView().setGlobalStyle( new TLspDepthTestGLState( true, ILcdGL.GL_LEQUAL ) );
    getView().getHostComponent().addKeyListener(new WriteCameraStateKeyListener(getView()));

    // Add view listener that will initialize camera when rendering view for the first time
    getView().addViewListener(new ALspViewAdapter() {
      @Override
      public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
        try {
          // Remove this listener, since it only needs to perform initialization
          aView.removeViewListener(this);

          // Load and apply camera parameters
          double[] parameters = CameraFileUtil.read3DCamera(ThemeAnimation.get3DCameraSourceName("Default"));
          if (parameters != null) {
            TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) aView.getViewXYZWorldTransformation();
            TLcdXYZPoint lookAtWorldPoint = new TLcdXYZPoint(parameters[0], parameters[1], parameters[2]);
            v2w.lookAt(lookAtWorldPoint, parameters[3], parameters[5], parameters[4], 0);
          }
        } catch (Throwable t) {
          sLogger.error("Could not initialize 3D camera" + (t.getMessage() == null ? "" : ", reason: " + t.getMessage()), t);
        }
      }
    });
  }

  protected void addData() {
    Framework.getInstance().addLayersToView(getView());
  }

  private void buildGUI() {
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    boolean shouldAddNavigationControls = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.navigationControls", "false"));
    if (shouldAddNavigationControls) {
      addNavigationControls();
    }

    boolean shouldAddScaleIndicator = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.scaleIndicator", "false"));
    if (shouldAddScaleIndicator) {
      addScaleIndicator();
    }

    addLuciadLogo();
    addSouthComponentDock();

    fSlideMenuManager = new SlideMenuManager(this);

    // Controllers
    fAWTViewPanel.setController(createDefaultController());

    setLayout(new BorderLayout());

    // Add the view component to this component
    add(fAWTViewPanel.getHostComponent());

    fAWTViewPanel.getHostComponent().requestFocus();
  }

  private void addScaleIndicator() {
    getOverlayPanel().add(new TLspScaleIndicator(getView()).getLabel(), TLcdOverlayLayout.Location.SOUTH_EAST);
  }

  /**
   * Adds navigation controls.
   */
  private void addNavigationControls() {
    getOverlayPanel().add(TLspNavigationControlsBuilder.newBuilder(getView()).build(), TLcdOverlayLayout.Location.NORTH_EAST);
  }

  private void addLuciadLogo() {
    final JLabel logoLabel = new JLabel(new LuciadLogoIcon());
    getOverlayPanel().add(logoLabel, TLcdOverlayLayout.Location.SOUTH);
    ((TLcdOverlayLayout) getOverlayPanel().getLayout()).putConstraint(
        logoLabel, TLcdOverlayLayout.Location.SOUTH, TLcdOverlayLayout.ResolveClash.VERTICAL
    );
  }

  private void addSouthComponentDock() {
    fSouthComponentDock = new JPanel(new BorderLayout());
    final RoundedBorder roundedBorder = new RoundedBorder(15);
    fSouthComponentDock.setBorder(roundedBorder);
    fSouthComponentDock.setBackground(DemoUIColors.PANEL_COLOR);
    fSouthComponentDock.setOpaque(false);

    getOverlayPanel().add(fSouthComponentDock, TLcdOverlayLayout.Location.SOUTH);
    ((TLcdOverlayLayout) getOverlayPanel().getLayout()).putConstraint(
        fSouthComponentDock, TLcdOverlayLayout.Location.SOUTH, TLcdOverlayLayout.ResolveClash.VERTICAL
    );
    fSouthComponentDock.setVisible(false);
  }

  /**
   * Docks the given component on the bottom edge of the view (below the Luciad logo).
   * If set to null, any existing components are removed and the docking bar is hidden.
   * Otherwise, the new component replaces the previous one.
   *
   * @param aComponent the component to be docked at the bottom of the view.
   */
  public void setSouthDockedComponent(JComponent aComponent) {
    fSouthComponentDock.removeAll();
    if (aComponent != null) {
      fSouthComponentDock.add(aComponent, BorderLayout.CENTER);
    }
    fSouthComponentDock.setVisible(aComponent != null);
  }

  protected SlideMenuManager getSlideMenuManager() {
    return fSlideMenuManager;
  }

  /**
   * Get the overlay panel used in this demo panel. <p/> Note: this can be
   * <code>null</code>.
   *
   * @return the overlay panel.
   *
   * @see TLcdOverlayLayout
   */
  public Container getOverlayPanel() {
    return getView().getOverlayComponent();
  }

  private boolean fIn = true;

  public void buildMainMenu() {
    try {
      final Framework framework = Framework.getInstance();

      SlideMenuManager.MenuLocation location = SlideMenuManager.MenuLocation.NORTH;
      boolean vertical = location == SlideMenuManager.MenuLocation.WEST || location == SlideMenuManager.MenuLocation.EAST;
      DefaultFormBuilder builder;
      if (vertical) {
        builder = new DefaultFormBuilder(new FormLayout("p"));
      } else {
        builder = new DefaultFormBuilder(new FormLayout());
      }
      builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      boolean includeThemeButton = Boolean.parseBoolean(framework.getProperty("menu.includeThemeButton", "false"));
      if (includeThemeButton) {
        final TiledThemeMenu themeMenu = new TiledThemeMenu(getView());

        BufferedImage img = themeMenu.getIconRepresentation();
        ImageIcon icon = new ImageIcon(img, "Themes");
        JButton themeButton = new JButton(icon);
        themeButton.setToolTipText("Themes");
        themeButton.setOpaque(false);
        themeButton.setBackground(new Color(0f,0f,0f,0f));

        Icon focusIcon = new FocusIcon(icon);
        themeButton.setRolloverIcon(focusIcon);
        themeButton.setPressedIcon(focusIcon);

        themeButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                if (themeMenu.isInView()) {
                  themeMenu.removeFromView();
                } else {
                  themeMenu.showPanel();
                }
              }
            });
          }
        });

        getOverlayPanel().add(themeButton, TLcdOverlayLayout.Location.NORTH_WEST);
        getOverlayPanel().setComponentZOrder(themeButton, 0);

      }

      boolean includeProjectionChange = Boolean.parseBoolean(framework.getProperty("menu.includeProjectionChange", "true"));
      ScalableButton projectionButton = null;
      if (includeProjectionChange) {
        projectionButton = ScalableButton.createButton("globe.png", "Projection");
        addButton(vertical, builder, projectionButton);
      }

      ScalableButton layersButton = ScalableButton.createButton("layers.png", "Layers");
      addButton(vertical, builder, layersButton);

      boolean includePrintButton = Boolean.parseBoolean(framework.getProperty("menu.includePrintButton", "true"));
      ScalableButton printButton;
      if (includePrintButton) {
        final boolean isMacOS = System.getProperty("os.name").toLowerCase().startsWith("mac");
        printButton = ScalableButton.createButton("print.png", "Print");
        PrintPreviewAction printAction = new PrintPreviewAction(fAWTViewPanel.getHostComponent(), fAWTViewPanel) {

          private boolean fWasSimulationRunning;
          private Window fFullScreenWindow;

          @Override
          protected TLspViewComponentPrintable createViewComponentPrintable(ILspAWTView aView) {
            Component viewComponent = TLspViewComponentPrintable.createViewComponent(aView);
            return new TLspViewComponentPrintable(viewComponent);
          }

          @Override
          protected void beforePrinting() {
            if(isMacOS) {
              GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
              GraphicsDevice[] devices = environment.getScreenDevices();
              fFullScreenWindow = devices[0].getFullScreenWindow();
              if(fFullScreenWindow!=null) {
                devices[0].setFullScreenWindow(null);
              }
            }
            super.beforePrinting();
            fWasSimulationRunning = SimulationSupport.getInstance().isRunning();
            if (fWasSimulationRunning) {
              SimulationSupport.getInstance().pauseSimulator();
            }
          }

          @Override
          protected void afterPrinting() {
            if(isMacOS && fFullScreenWindow!=null) {
              GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
              GraphicsDevice[] devices = environment.getScreenDevices();
              devices[0].setFullScreenWindow(fFullScreenWindow);
            }
            super.afterPrinting();
            if (fWasSimulationRunning) {
              SimulationSupport.getInstance().startSimulator();
            }
          }
        };
        printButton.addActionListener(printAction);
        addButton(vertical, builder, printButton);
      }

      //On OS X, the window decoration buttons allow to switch between full screen and windowed
      if (!TLcdSystemPropertiesUtil.isMacOS()){
        final FullScreenAction fullScreenAction = new FullScreenAction(getView());
        fullScreenAction.putValue(ILcdAction.SMALL_ICON, new TLcdImageIcon("samples/lightspeed/demo/icons/full_screen.png"));
        fullScreenAction.getRestoreAction().putValue(ILcdAction.SMALL_ICON, new TLcdImageIcon("samples/lightspeed/demo/icons/exit_full_screen.png"));
        //avoid tooltips
        fullScreenAction.putValue(ILcdAction.SHORT_DESCRIPTION, null);
        fullScreenAction.getRestoreAction().putValue(ILcdAction.SHORT_DESCRIPTION, null);

        ScalableButton fullScreenButton = ScalableButton.createButton(fullScreenAction, "Full screen");
        addButton(vertical, builder,fullScreenButton);

        ScalableButton windowedButton = ScalableButton.createButton(fullScreenAction.getRestoreAction(), "Windowed");
        addButton(vertical, builder, windowedButton);
      }

      // Create main menu buttons
      boolean includeExitButton = Boolean.parseBoolean(framework.getProperty("menu.includeExitButton", "true"));
      ScalableButton exitButton;
      if (includeExitButton) {
        exitButton = ScalableButton.createButton("exit.png", "Exit");
        exitButton.addActionListener(new ExitAction());
        addButton(vertical, builder, exitButton);
      }

      // Set size of content panel (otherwise the 
      JPanel contentPanel = builder.getPanel();
      contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));

      // Create main menu
      final SlideMenu mainSlideMenu = new SlideMenu(location, contentPanel);
      final MainMenu mainMenu = new MainMenu(mainSlideMenu);
      fSlideMenuManager.registerMainMenu(mainMenu);

      // Create submenus
      SubMenuBuilder subMenuBuilder = new SubMenuBuilder(mainSlideMenu, fAWTViewPanel);

      boolean useToucheUI = Boolean.parseBoolean(framework.getProperty("ui.touch", "false"));

      if (includeProjectionChange) {
        SlideMenu projectionMenu = subMenuBuilder.buildProjectionSubMenu(useToucheUI);
        mainMenu.registerSubMenu(fSlideMenuManager, projectionButton, projectionMenu);
      }

      Boolean linkLayerControls = Boolean
          .parseBoolean(framework.getProperty("menu.layersubmenus.linked", "true"));
      SlideMenu layerMenu = subMenuBuilder
          .buildLayerSubMenu(linkLayerControls ? null : getView(), useToucheUI);
      mainMenu.registerSubMenu(fSlideMenuManager, layersButton, layerMenu);

      /////////////////////

      // Add menu button in case there is no automatic slide out
      boolean autoSlideOut = Boolean
          .parseBoolean(framework.getProperty("menu.slide.panel.autoslideout", "true"));
      if (!autoSlideOut) {
        TLcdInputStreamFactory factory = new TLcdInputStreamFactory();
        BufferedImage inImage = IOUtil.readImage("samples/lightspeed/demo/icons/menu_in_64.png");
        BufferedImage outImage = IOUtil.readImage("samples/lightspeed/demo/icons/menu_out_64.png");
        final TLcdSWIcon inIcon = new TLcdSWIcon(new TLcdImageIcon(inImage));
        final TLcdSWIcon outIcon = new TLcdSWIcon(new TLcdImageIcon(outImage));

        DefaultFormBuilder builder2 = new DefaultFormBuilder(new FormLayout("p"));
        builder2.border(Borders.DIALOG);

        final JButton menuButton = new PopMenuButton(outIcon);
        menuButton.setOpaque(false);
        menuButton.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            if (!fIn) {
              fSlideMenuManager.slideInAll(true);
              menuButton.setIcon(outIcon);
            } else {
              fSlideMenuManager.slideOutAll(true);
              mainMenu.retractAll();
              menuButton.setIcon(inIcon);
            }
            fIn = !fIn;
          }
        });

        addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            menuButton.setIcon(outIcon);
            fIn = true;

          }
        });

        builder2.append(menuButton);
        builder2.nextLine();
        final JPanel panel = builder2.getPanel();
        panel.setOpaque(false);
        panel.setSize(panel.getLayout().preferredLayoutSize(panel));
        boolean shouldAddScaleIndicator = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.scaleIndicator", "false"));
        final int offsetY = shouldAddScaleIndicator ? 30 : 0;
        panel.setLocation(getWidth() - panel.getWidth(), getHeight() - panel.getHeight() - offsetY);

        getView().getHostComponent().addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            panel.setLocation(getWidth() - panel.getWidth(), getHeight() - panel.getHeight() - offsetY);
          }
        });

        getOverlayPanel().add(panel, TLcdOverlayLayout.Location.NO_LAYOUT);
      }

      /////////////////////

      // Show the main menu so people know it's there
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              fSlideMenuManager.slideInAll(true);
            }
          }
      );

    } catch (Exception e) {
      sLogger.error("Failed to build main menu.", e);
    }
  }

  private void addButton(boolean aVertical, DefaultFormBuilder aBuilder, JButton aButton) {
    if (!aVertical) {
      aBuilder.appendColumn("p");
      aBuilder.append(aButton);
      aBuilder.appendColumn("5dlu");
    } else {
      aBuilder.append(aButton);
      aBuilder.nextLine();
    }
  }

  public void buildThemeMenu() {
    Framework framework = Framework.getInstance();
    boolean includeThemeButton = Boolean.parseBoolean(framework.getProperty("menu.includeThemeButton", "false"));
    if (includeThemeButton) {
      return;
    }

    boolean useToucheUI = Boolean.parseBoolean(framework.getProperty("ui.touch", "false"));
    if (!useToucheUI) {
      buildMainThemeMenuForMouseUI();
    } else {
      buildMainThemeMenuForTouchUI();
    }
  }

  /**
   * Creates the main theme menu panel (i.e. the menu panel that lists all the loaded
   * themes) and adds it to the view.
   */
  private void buildMainThemeMenuForMouseUI() {
    final Framework framework = Framework.getInstance();
    // Create binding
    BeanAdapter<Framework> applicationAdapter = new BeanAdapter<Framework>(framework, true);
    BeanAdapter<Framework>.SimplePropertyAdapter activeThemeValueModel = applicationAdapter
        .getValueModel("activeTheme");

    // Create content panel
    DefaultFormBuilder menuBuilder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p"));
    menuBuilder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Themes", 15, true);

    menuBuilder.append(titleLabel, 3);
    menuBuilder.nextLine();

    // Add radio buttons and a label for each theme
    AbstractTheme[] themes = framework.getThemes();

    Map<String, List<AbstractTheme>> themePerCategory = sortByCategory(themes);

    Set<Map.Entry<String, List<AbstractTheme>>> categorizedThemes = themePerCategory.entrySet();
    for (final Map.Entry<String, List<AbstractTheme>> categoryThemeEntry : categorizedThemes) {
      DefaultFormBuilder categoryBuilder;
      categoryBuilder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p"));
      if (categoryThemeEntry.getKey() != null) {
        final RoundedBorder roundedBorder = new RoundedBorder(15, DemoUIColors.SUB_PANEL_COLOR, DemoUIColors.SUB_PANEL_COLOR);
        roundedBorder.setTotalItems(categoryThemeEntry.getValue().size());
        categoryBuilder.border(roundedBorder);
        for (final AbstractTheme theme : categoryThemeEntry.getValue()) {
          final JRadioButton radioButton = new JRadioButton();
          radioButton.setOpaque(false);
          Bindings.bind(radioButton, activeThemeValueModel, theme);
          categoryBuilder.append(radioButton);
          final HaloLabel label = new HaloLabel(theme.getName());
          label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              radioButton.setSelected(true);
            }
          });

          label.setBackground(Color.red);
          categoryBuilder.append(label);
          categoryBuilder.nextLine();
        }
        if (categoryThemeEntry.getKey() != null) {
          HaloLabel categoryLabel = new HaloLabel(categoryThemeEntry.getKey());
          menuBuilder.append(categoryLabel);
          menuBuilder.nextLine();
        }
        JPanel panel = categoryBuilder.getPanel();
        panel.setOpaque(false);
        menuBuilder.append(panel);
        menuBuilder.nextLine();
      } else {
        // The default theme can be activated by double clicking on the title label.
        for (final AbstractTheme theme : categoryThemeEntry.getValue()) {
          if (theme instanceof DefaultTheme) {
            titleLabel.addMouseListener(new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                  framework.setActiveTheme(theme);
                }
              }
            });
          } else {
            sLogger.warn("Theme should have a category: " + theme.getName());
          }
        }
      }
    }

    JPanel contentPanel = menuBuilder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));

    // Create the actual slide menu
    final SlideMenu mainThemeMenu = new SlideMenu(SlideMenuManager.MenuLocation.WEST, contentPanel);
    fSlideMenuManager.addSlideMenu(mainThemeMenu);

    // Slide it in initially so people know it's there
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            mainThemeMenu.startSlideIn();
          }
        }
    );
  }

  private Map<String, List<AbstractTheme>> sortByCategory(AbstractTheme[] aThemes) {
    HashMap<String, List<AbstractTheme>> map = new LinkedHashMap<String, List<AbstractTheme>>();
    for (AbstractTheme theme : aThemes) {
      String category = theme.getCategory();
      List<AbstractTheme> abstractThemes = map.get(category);
      if (abstractThemes == null) {
        abstractThemes = new ArrayList<AbstractTheme>();
        map.put(category, abstractThemes);
      }
      abstractThemes.add(theme);
    }
    return map;
  }

  /**
   * Creates the main theme menu panel (i.e. the menu panel that lists all the loaded
   * themes) and adds it to the view.
   */
  private void buildMainThemeMenuForTouchUI() {
    final Framework framework = Framework.getInstance();

    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Themes", 15, true);
    builder.append(titleLabel, 1);
    builder.nextLine();

    // Add radio buttons and a label for each theme
    AbstractTheme[] themes = framework.getThemes();
    for (final AbstractTheme theme : themes) {
      // Only add a separate label when icon!=null
      if (theme.getIcon() != null) {
        HaloLabel label = new HaloLabel(theme.getName());
        builder.append(label);
        builder.nextLine();
      }

      JButton button = new JButton();

      // Use icon if available on button or use name of theme
      if (theme.getIcon() == null) {
        button.setText(theme.getName());
      } else {
        button.setIcon(theme.getIcon());
      }

      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          framework.setActiveTheme(theme);
        }
      });

      builder.append(button);

      builder.nextLine();
    }

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));

    // Create the actual slide menu
    final SlideMenu mainThemeMenu = new SlideMenu(SlideMenuManager.MenuLocation.WEST, contentPanel);
    fSlideMenuManager.addSlideMenu(mainThemeMenu);

    // Slide it in initially so people know it's there
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            mainThemeMenu.startSlideIn();
          }
        }
    );
  }

  public void buildZSliderMenu() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p"));
    builder.border(Borders.DIALOG);

    HaloLabel titleLabel = new HaloLabel("Z-Scale", 15, true);
    builder.append(titleLabel, 1);
    builder.nextLine();

    builder.append(new ZSlider(JSlider.VERTICAL, getView()));
    builder.nextLine();
    final JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));

    final SlideMenu slideMenu = new SlideMenu(SlideMenuManager.MenuLocation.EAST, contentPanel);

    fSlideMenuManager.addSlideMenu(slideMenu, true);

    // Slide it in initially so people know it's there
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            slideMenu.startSlideIn();
          }
        }
    );
  }

  protected ILspController createDefaultController() {
    return ControllerFactory.createGeneralController(Framework.getInstance().getUndoManager(), getView());
  }

  //////////////////////////////////////////////////////////////////////////////////////////

  private class PopMenuButton extends JButton implements MouseListener {

    private Color fColor;
    private Color fSelectedColor;
    private boolean fPressed;

    public PopMenuButton(TLcdSWIcon aOutIcon) {
      super(aOutIcon);
      fColor = new Color(1.0f, 1.0f, 1.0f, 0.2f);
      fSelectedColor = new Color(1.0f, 1.0f, 1.0f, 0.6f);
      fPressed = false;
      addMouseListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(84, 84);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;

      g2d.setBackground(DemoUIColors.TRANSPARENT);
      g2d.setColor(DemoUIColors.TRANSPARENT);
      g2d.clearRect(0, 0, getWidth(), getHeight());

      // Draw background rectangle
      g2d.setColor(fPressed ? fSelectedColor : fColor);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
      g2d.setColor(DemoUIColors.PANEL_BORDER_COLOR);
      g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

      getIcon().paintIcon(this, g2d, 10, 10);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
      fPressed = true;
      repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      fPressed = false;
      repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
  }

  private static class FXToggleListener implements KeyListener {
    private ILspAWTView fView;
    private Set<ALspGraphicsEffect> fDisabledFX = new HashSet<ALspGraphicsEffect>();
    private boolean fFXEnabled = true;

    public FXToggleListener(ILspAWTView aAWTViewPanel) {
      fView = aAWTViewPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == 'f') {
        fFXEnabled = !fFXEnabled;
        if (fFXEnabled) {
          for (ALspGraphicsEffect effect : fDisabledFX) {
            effect.setEnabled(true);
          }
          fDisabledFX.clear();
        } else {
          Collection<ALspGraphicsEffect> fx = fView.getServices().getGraphicsEffects();
          for (ALspGraphicsEffect effect : fx) {
            if ((effect instanceof ALspLight) || (effect instanceof TLspFog)) {
              if (effect.isEnabled()) {
                effect.setEnabled(false);
                fDisabledFX.add(effect);
              }
            }
          }
        }
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
  }

  private static class UndoRedoListener implements KeyListener {
    private final Framework fFramework;

    public UndoRedoListener(Framework aFramework) {
      fFramework = aFramework;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == 'Z') {
        if (e.getModifiers() == MetaKeyUtil.getCMDMask()) {
          // Undo
          TLcdUndoManager um = fFramework.getUndoManager();
          if (um != null) {
            if (um.getCurrentUndo() != null) {
              um.undo();
            }
          }
        } else if (e.getModifiers() == (MetaKeyUtil.getCMDMask() | KeyEvent.SHIFT_MASK)) {
          // Redo
          TLcdUndoManager um = fFramework.getUndoManager();
          if (um != null) {
            if (um.getCurrentRedo() != null) {
              um.redo();
            }
          }
        }
      }
    }
  }
}
