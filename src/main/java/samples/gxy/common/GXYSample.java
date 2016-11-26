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
package samples.gxy.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.CompoundBorder;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.measure.ILcdLayerMeasureProviderFactory;
import com.luciad.util.measure.ILcdModelMeasureProviderFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;
import com.luciad.view.gxy.swing.TLcdGXYScaleIndicator;
import com.luciad.view.gxy.swing.navigationcontrols.TLcdGXYNavigationControlsFactory;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.AnimatedLayoutManager;
import samples.common.HorizontalSplitPane;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.StatusBar;
import samples.common.UIColors;
import samples.common.VerticalScrollPane;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.fundamentals.step1.Main;

/**
 * Base class for ILcdGXYView based samples.<br/>
 * For a step-by-step explanation of how to set up an application with a GXY view,
 * refer to the {@link Main fundamentals samples}.
 */
public abstract class GXYSample extends SamplePanel {

  private final boolean fAnimateSideBar;

  private TLcdMapJPanel fMap;
  private Component[] fToolBars;
  private ILcdStatusListener fStatusBar = new StatusBar();
  private JComponent fOverlayPanel;
  private final ILcdList<ILcdLayer> fSelectedLayers = new TLcdArrayList<>();

  public GXYSample() {
    this(true);
  }

  public GXYSample(boolean aAnimateSideBar) {
    super();
    fAnimateSideBar = aAnimateSideBar;
  }

  public TLcdMapJPanel getView() {
    return fMap;
  }

  public JComponent getOverlayPanel() {
    return fOverlayPanel;
  }

  public ToolBar[] getToolBars() {
    ArrayList<ToolBar> result = new ArrayList<ToolBar>();
    for (Component tb : fToolBars) {
      if (tb instanceof ToolBar) {
        result.add((ToolBar) tb);
      }
    }
    return result.toArray(new ToolBar[0]);
  }

  public ILcdStatusListener getStatusBar() {
    return fStatusBar;
  }

  public final ILcdList<ILcdLayer> getSelectedLayers() {
    return fSelectedLayers;
  }

  @Override
  protected void createGUI() {
    fMap = createMap();
    setLayout(new BorderLayout());
    fOverlayPanel = createOverlayPanel();
    JPanel toolBarsPanel = createToolBarsPanel();
    if (toolBarsPanel != null) {
      add(toolBarsPanel, BorderLayout.NORTH);
    }

    JComponent mapPanel = createMapPanel(fOverlayPanel);
    JComponent layerAndSettingsPanel = createLayerAndSettingsPanel();
    JComponent browserPanel = createBrowserPanel();

    JSplitPane rightSplitPane = new HorizontalSplitPane(mapPanel, layerAndSettingsPanel);
    rightSplitPane.setResizeWeight(1.0);
    if (browserPanel != null) {
      JSplitPane leftSplitPane = new HorizontalSplitPane(browserPanel, rightSplitPane);
      leftSplitPane.setResizeWeight(0);
      add(leftSplitPane, BorderLayout.CENTER);
    } else {
      add(rightSplitPane, BorderLayout.CENTER);
    }

    JPanel bottomPanel = createBottomPanel();
    JPanel bottomPanelWithStatusBar = new JPanel(new BorderLayout());
    bottomPanelWithStatusBar.add((Component) fStatusBar, BorderLayout.SOUTH);
    bottomPanelWithStatusBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    if (bottomPanel != null) {
      bottomPanelWithStatusBar.add(bottomPanel, BorderLayout.CENTER);
    }
    add(bottomPanelWithStatusBar, BorderLayout.SOUTH);
  }

  @Override
  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").asynchronous().selectable(false).addToView(getView());
  }

  private JPanel createToolBarsPanel() {
    fToolBars = createToolBars();
    if (fToolBars != null) {
      JPanel toolbarPanel = new JPanel();
      toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
      toolbarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
      for (Component toolBar : fToolBars) {
        toolbarPanel.add(toolBar);
        if (toolBar instanceof JComponent) {
          ((JComponent) toolBar).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
      }
      add(toolbarPanel, BorderLayout.NORTH);
      return toolbarPanel;
    }
    return null;
  }

  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = SampleMapJPanelFactory.createMapJPanel(getInitialBounds());
    setupPaintQueueManager(map);
    map.setGXYLayerFactory(createLayerFactory());
    return map;
  }

  /**
   * Creates the layer factory that is used to create layers for models added to the view.
   *
   * @return the layer factory
   */
  protected ILcdGXYLayerFactory createLayerFactory() {
    // Pick up all other layer factories
    Iterable<ILcdGXYLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class);
    return new TLcdCompositeGXYLayerFactory(layerFactories);
  }

  protected void setupPaintQueueManager(ILcdGXYView aMap) {
    new TLcdGXYAsynchronousPaintQueueManager().setGXYView(aMap);
  }

  /**
   * Determines what part of the world is shown at start-up.
   * @return the lon lat bounds to show
   */
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-180, -90, 360, 180);
  }

  protected Component[] createToolBars() {
  ToolBar toolbar = new ToolBar(fMap, true, this, fOverlayPanel) {
      @Override
      protected Component createMouseLocationComponent() {
        return null; // we'll add our mouse location as an overlay, not in the toolbar
      }
    };
    return new Component[]{toolbar};
  }

  private JComponent createMapPanel(JComponent aMap) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 5, 5, 4), BorderFactory.createLineBorder(UIColors.bgSubtle())));
    panel.add(aMap);
    return panel;
  }

  private JComponent createOverlayPanel() {
    // Creates an overlay panel to add extra content, such as navigation controls.
    // OverlayPanel makes sure the map itself is always at the bottom and uses a TLcdOverlayLayout.
    OverlayPanel overlayPanel = new OverlayPanel(fMap);
    addOverlayComponents(overlayPanel);
    return overlayPanel;
  }

  protected void addOverlayComponents(OverlayPanel aOverlayPanel) {
    Component controls = TLcdGXYNavigationControlsFactory.createNavigationControls(fMap, false);
    aOverlayPanel.add(controls, TLcdOverlayLayout.Location.NORTH_EAST);
    Iterable<ILcdModelMeasureProviderFactory> modelMeasureProviderFactories = ServiceRegistry.getInstance().query(ILcdModelMeasureProviderFactory.class);
    Iterable<ILcdLayerMeasureProviderFactory> layerMeasureProviderFactories = ServiceRegistry.getInstance().query(ILcdLayerMeasureProviderFactory.class);
    aOverlayPanel.add(new MouseLocationComponent(getView(), modelMeasureProviderFactories, layerMeasureProviderFactories), TLcdOverlayLayout.Location.SOUTH);
    aOverlayPanel.add(createLogo(), TLcdOverlayLayout.Location.SOUTH_WEST);
    aOverlayPanel.add(createScaleIndicator(), TLcdOverlayLayout.Location.SOUTH_EAST);
  }

  private JLabel createLogo() {
    // remove the previous luciad logo
    fMap.putCornerIcon(null, ILcdGXYView.LOWERLEFT);
    return new JLabel(new LogoIcon());
  }

  private JLabel createScaleIndicator() {
    // remove the previous scale indicator icon
    fMap.putCornerIcon(null, ILcdGXYView.LOWERRIGHT);
    TLcdGXYScaleIndicator scaleIndicator = new TLcdGXYScaleIndicator(fMap);
    // updates the scale indicator depending on where you pan, not only based on the zoom level
    scaleIndicator.setScaleAtCenterOfMap(true);
    scaleIndicator.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    return new JLabel(new TLcdSWIcon(scaleIndicator));
  }

  private JComponent createLayerAndSettingsPanel() {
    JPanel eastPanel = new JPanel(fAnimateSideBar ?
                                  AnimatedLayoutManager.create(new BorderLayout()) :
                                  new BorderLayout());
    JPanel settingsPanel = createSettingsPanel();
    if (settingsPanel != null) {
      eastPanel.add(settingsPanel, BorderLayout.NORTH);
    }
    JComponent layerPanel = createLayerPanel();
    if (layerPanel != null) {
      eastPanel.add(layerPanel, BorderLayout.CENTER);
    }

    JPanel layerSettingsPanel = createLayerSettingsPanel();
    if (layerSettingsPanel != null) {
      eastPanel.add(layerSettingsPanel, BorderLayout.SOUTH);
    }
    // Handle vertical overflow using a scroll pane.
    return eastPanel.getComponentCount() > 0 ? new VerticalScrollPane(eastPanel) : null;
  }

  protected JComponent createLayerPanel() {
    return LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMap, getSelectedLayers());
  }

  protected JPanel createSettingsPanel() {
    return null;
  }

  protected JPanel createLayerSettingsPanel() {
    return null;
  }

  protected JPanel createBottomPanel() {
    return null;
  }

  protected JComponent createBrowserPanel() {
    return null;
  }
}

