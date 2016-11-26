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
package samples.lightspeed.common;

import static samples.common.SwingUtil.createButtonForAction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.CompoundBorder;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.measure.ILcdLayerMeasureProviderFactory;
import com.luciad.util.measure.ILcdModelMeasureProviderFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.swing.TLspScaleIndicator;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;

import samples.common.AnimatedLayoutManager;
import samples.common.HorizontalSplitPane;
import samples.common.LayerPaintExceptionHandler;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.StatusBar;
import samples.common.UIColors;
import samples.common.VerticalScrollPane;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.layercontrols.LayerControlPanelFactoryLsp;
import samples.lightspeed.common.touch.TouchToolBar;
import samples.lightspeed.fundamentals.step1.Main;

/**
 * Base class for ILspView based samples.<br/>
 * For a step-by-step explanation of how to set up an application with a Lightspeed view,
 * refer to the {@link Main fundamentals samples}.
 */
public abstract class LightspeedSample extends SamplePanel {

  static {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
      }
    });
  }

  private ILspAWTView fView;
  private final ILcdList<ILcdLayer> fSelectedLayers = new TLcdArrayList<>();
  private Component[] fToolBars;
  private final ILcdStatusListener fStatusBar = new StatusBar();
  private JSplitPane fSplitPane;
  private final JPanel fEastPanel;
  private final boolean fUseTouchToolBar;
  private FullScreenAction fFullScreenAction;

  /**
   * Creates a Lightspeed sample panel with a regular or touch tool bar.
   * @param aUseTouchToolBar specifies whether this panel features a touch tool bar
   *                         (<code>true</code>) or a regular tool bar (<code>false</code>)
   * @param aAnimateSideBar  specifies if changes in the sidebar's layout should be animated
   */
  protected LightspeedSample(boolean aUseTouchToolBar, boolean aAnimateSideBar) {
    super();
    fUseTouchToolBar = aUseTouchToolBar;
    fEastPanel = new JPanel(aAnimateSideBar ? AnimatedLayoutManager.create(new BorderLayout()) : new BorderLayout());
  }

  protected LightspeedSample(boolean aUseTouchToolBar) {
    this(aUseTouchToolBar, true);
  }

  protected LightspeedSample() {
    this(false, true);
  }

  @Override
  protected void triggerClass() {
    TLspViewBuilder.newBuilder().buildOffscreenView().destroy();
  }

  /**
   * Creates the layer factory that is used to create layers for models added to the view.
   *
   * @return the layer factory
   */
  protected ILspLayerFactory createLayerFactory() {
    // Pick up all other layer factories
    Iterable<ILspLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILspLayerFactory.class);
    return new TLspCompositeLayerFactory(layerFactories);
  }

  /**
   * Get the view which is used in this sample.
   *
   * @return the view.
   */
  public ILspAWTView getView() {
    return fView;
  }

  /**
   * Returns the layers selected by the user, if any.
   *
   * @return the selected layers
   */
  public final ILcdList<ILcdLayer> getSelectedLayers() {
    return fSelectedLayers;
  }

  /**
   * Get the regular tool bars used in this sample.
   *
   * @return the regular tool bars.
   */
  public ToolBar[] getToolBars() {
    ArrayList<ToolBar> result = new ArrayList<>();
    for (Component tb : fToolBars) {
      if (tb instanceof ToolBar) {
        result.add((ToolBar) tb);
      }
    }
    return result.toArray(new ToolBar[result.size()]);
  }

  /**
   * Get the tool bars that are specifically targeted for touch controls.
   *
   * @return the touch related tool bars
   */
  public TouchToolBar[] getTouchToolBars() {
    ArrayList<TouchToolBar> result = new ArrayList<>();
    for (Component tb : fToolBars) {
      if (tb instanceof TouchToolBar) {
        result.add((TouchToolBar) tb);
      }
    }
    return result.toArray(new TouchToolBar[result.size()]);
  }

  public ILcdStatusListener getStatusBar() {
    return fStatusBar;
  }

  /**
   * Adds the given component at the bottom part of the GUI.
   *
   * @param aComponent the component to be added
   */
  public void addComponentBelow(Component aComponent) {
    add(BorderLayout.SOUTH, aComponent);
  }

  /**
   * Adds the given component at the top of the panel on the right of the GUI.
   *
   * @param aComponent the component to be added
   */
  public void addComponentToRightPanel(Component aComponent) {
    addComponentToRightPanel(aComponent, BorderLayout.NORTH);
  }

  /**
   * Adds the given component to the panel on the right of the GUI.
   *
   * @param aComponent the component to be added
   * @param aConstraint the constraint to use when adding the component
   */
  public void addComponentToRightPanel(Component aComponent, Object aConstraint) {
    fEastPanel.add(aComponent, aConstraint);
  }

  /**
   * Create the ILspView for this sample.
   *
   * @param aViewType determines whether a 2D or 3D view should be created
   *
   * @return the resulting view
   */
  protected ILspAWTView createView(ILspView.ViewType aViewType) {
    ILspAWTView view = TLspViewBuilder.newBuilder()
                                      .viewType(aViewType)
                                      .background(UIColors.bgMap())
                                      .addAtmosphere(true)
                                      .paintExceptionHandler(new LayerPaintExceptionHandler())
                                      .buildAWTView();
    view.getRootNode().setInitialLayerIndexProvider(new InitialLayerIndexProvider());
    return view;
  }

  /**
   * By default calls createView(ViewType.VIEW_2D).
   * You can override this method to create a 3D view.
   *
   * @return a view with the proper view type.
   */
  protected ILspAWTView createView() {
    return createView(ILspView.ViewType.VIEW_2D);
  }

  /**
   * Creates a tool bar for the given view.
   * <p/>
   * If the system is touch capable, a <code>TouchToolBar</code> will be created, which offers more
   * appropriate controls for manipulating the view with touch gestures. When this is not the case,
   * a <code>ToolBar</code> will be added with the regular controllers.
   *
   * @param aView   the view to which the tool bar will be associated
   * @return a tool bar that provides controls for working with the given view
   */
  protected Component[] createToolBars(ILspAWTView aView) {
    if (fUseTouchToolBar) {
      return new JToolBar[]{new TouchToolBar(aView, this, true, true)};
    } else {
      return new JToolBar[]{new ToolBar(aView, this, true, true) {
        @Override
        protected ILcdFilter<ILspLayer> createStickyLabelsLayerFilter() {
          return LightspeedSample.this.createStickyLabelsLayerFilter();
        }
      }};
    }
  }

  protected ILcdFilter<ILspLayer> createStickyLabelsLayerFilter() {
    return new ILcdFilter<ILspLayer>() {
      @Override
      public boolean accept(ILspLayer aObject) {
        return false;
      }
    };
  }

  @Override
  protected void addData() throws IOException {
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(getView()).fit();
    LspDataUtil.instance().grid().addToView(getView());
  }

  @Override
  protected void createGUI() {
    initializeView();

    // Create the tool bar.
    setLayout(new BorderLayout());
    fToolBars = createToolBars(fView);
    //On the Mac, the whole frame can be switched to full screen using the main frame buttons. No need for an extra button
    if (!TLcdSystemPropertiesUtil.isMacOS()) {
      if (fToolBars != null && fToolBars.length > 0 && fToolBars[0] != null && fToolBars[0] instanceof JToolBar) {
        ((JToolBar) fToolBars[0]).add(createButtonForAction((JToolBar) fToolBars[0], fFullScreenAction, false), -1);
      }
    }

    if (fToolBars != null) {
      JPanel toolBarPanel = new JPanel();
      toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.Y_AXIS));
      for (Component toolBar : fToolBars) {
        toolBarPanel.add(toolBar);
        if (toolBar instanceof JComponent) {
          ((JComponent) toolBar).setAlignmentX(Component.LEFT_ALIGNMENT);
          ((JComponent) toolBar).setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        }
      }
      add(toolBarPanel, BorderLayout.NORTH);
    }

    // Add the overlay components.
    if (fView.getOverlayComponent() != null) {
      addOverlayComponents(getOverlayPanel());
    }

    JComponent layerControlPanel = createLayerPanel();
    fEastPanel.add(BorderLayout.CENTER, layerControlPanel);
//    fEastPanel.add( BorderLayout.SOUTH, new TLcdMemoryCheckPanel() );

    // Populate the frame.

    // Handle vertical overflow using a scroll pane.
    VerticalScrollPane verticalScrollPane = new VerticalScrollPane(fEastPanel);

    fSplitPane = new HorizontalSplitPane(createMapPanel(fView.getHostComponent()), verticalScrollPane);
    fSplitPane.setResizeWeight(1.0);
    add(fSplitPane, BorderLayout.CENTER);

    JPanel bottomPanelWithStatusBar = new JPanel(new BorderLayout());
    bottomPanelWithStatusBar.add((Component) fStatusBar, BorderLayout.SOUTH);
    bottomPanelWithStatusBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    add(bottomPanelWithStatusBar, BorderLayout.SOUTH);

    updateUI();
  }

  protected void hideLayerPanel() {
    fSplitPane.setDividerLocation(1.0);
  }

  protected void initializeView() {
    fView = createView();
    fView.setLayerFactory(createLayerFactory());
    fFullScreenAction = new FullScreenAction(getView());
  }

  protected JComponent createMapPanel(Component aMapComponent) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(4, 5, 5, 4), BorderFactory.createLineBorder(UIColors.bgSubtle())));
    panel.add(aMapComponent);
    return panel;
  }

  protected JComponent createLayerPanel() {
    return LayerControlPanelFactoryLsp.createDefaultLayerControlPanel(fView, fSelectedLayers);
  }

  /**
   * Override to perform additional tasks on tear down.
   */
  @Override
  protected void tearDown() {
    if (fView != null) {
      fView.removeAllLayers();
      fView.destroy();
      fView = null;
    }
  }

  protected void addAltitudeExaggerationControl(JComponent aOverlayPanel) {
    JComponent altitudeExaggerationControl = (JComponent) TLspNavigationControlsBuilder.newBuilder(getView())
                                                                                       .altitudeExaggerationControl()
                                                                                       .build();
    aOverlayPanel.add(altitudeExaggerationControl, TLcdOverlayLayout.Location.WEST);
  }

  /**
   * Adds the components that should be shown on the map.
   */
  protected void addOverlayComponents(JComponent aOverlayPanel) {
    addNavigationControls(aOverlayPanel);
    addAltitudeExaggerationControl(aOverlayPanel);

    TLcdOverlayLayout layout = (TLcdOverlayLayout) aOverlayPanel.getLayout();

    AbstractButton fullScreenRestoreButton = createButtonForAction(this, getFullScreenAction().getRestoreAction(), false);
    fullScreenRestoreButton.setOpaque(false);
    aOverlayPanel.add(fullScreenRestoreButton);
    layout.putConstraint(fullScreenRestoreButton, TLcdOverlayLayout.Location.NORTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);

    JLabel luciadLogo = new JLabel(new LuciadLogoIcon());
    aOverlayPanel.add(luciadLogo);
    layout.putConstraint(luciadLogo, TLcdOverlayLayout.Location.SOUTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);

    TLspScaleIndicator scaleIndicator = new TLspScaleIndicator(fView);
    // updates the scale indicator depending on where you pan, not only based on the zoom level
    scaleIndicator.setScaleAtCenterOfMap(true);
    JLabel scaleIndicatorLabel = scaleIndicator.getLabel();
    aOverlayPanel.add(scaleIndicatorLabel);
    layout.putConstraint(scaleIndicatorLabel, TLcdOverlayLayout.Location.SOUTH_EAST, TLcdOverlayLayout.ResolveClash.VERTICAL);

    if (getView() instanceof ALspAWTView) {
      Iterable<ILcdModelMeasureProviderFactory> measureProviderFactories = ServiceRegistry.getInstance().query(ILcdModelMeasureProviderFactory.class);
      Iterable<ILcdLayerMeasureProviderFactory> layerMeasureProviderFactories = ServiceRegistry.getInstance().query(ILcdLayerMeasureProviderFactory.class);
      aOverlayPanel.add(new MouseLocationComponent((ALspAWTView) getView(), measureProviderFactories, layerMeasureProviderFactories), TLcdOverlayLayout.Location.SOUTH);
    }
  }

  protected void addNavigationControls(JComponent aOverlayPanel) {
    TLcdOverlayLayout layout = (TLcdOverlayLayout) aOverlayPanel.getLayout();
    Component navigationControls = TLspNavigationControlsBuilder.newBuilder(getView()).build();
    aOverlayPanel.add(navigationControls);
    layout.putConstraint(navigationControls, TLcdOverlayLayout.Location.NORTH_EAST, TLcdOverlayLayout.ResolveClash.VERTICAL);
  }

  /**
   * Returns the overlay panel used in this sample, or null if there is no such panel.
   *
   * @return the overlay panel.
   * @see TLcdOverlayLayout
   */
  public JComponent getOverlayPanel() {
    if ((fView.getOverlayComponent() != null) && (fView.getOverlayComponent() instanceof JComponent)) {
      return (JComponent) fView.getOverlayComponent();
    } else {
      return null;
    }
  }

  /**
   * Returns an action to toggle the view's full screen mode.
   * @return an action to toggle the view's full screen mode.
   */
  protected FullScreenAction getFullScreenAction() {
    return fFullScreenAction;
  }

}
