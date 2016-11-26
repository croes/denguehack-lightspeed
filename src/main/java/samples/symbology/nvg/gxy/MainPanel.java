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
package samples.symbology.nvg.gxy;

import static samples.symbology.nvg.common.CivilianIconProvider.CUSTOM_CIVILIAN_DOMAIN_NAME;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.format.nvg.gxy.TLcdNVGGXYPainterProvider;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.projection.TLcdMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.common.AnimatedLayoutManager;
import samples.common.OptionsPanelScrollPane;
import samples.common.SwingUtil;
import samples.gxy.common.toolbar.AToolBar;
import samples.gxy.common.toolbar.ToolBar;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.util.SymbolCustomizerPanelSelectionListener;
import samples.symbology.common.util.SymbologyFavorites;
import samples.symbology.nvg.common.CivilianIconProvider;
import samples.symbology.nvg.common.NVGModelFactory;
import samples.symbology.nvg.common.NVGUtilities;
import samples.symbology.nvg.gui.NVGCustomizerFactory;
import samples.symbology.nvg.gui.NVGSymbolCustomizerPanelSelectionListener;
import samples.symbology.nvg.gxy.gui.NVGGXYCreateContentAction;
import samples.symbology.nvg.gxy.gui.NVGGXYFavoritesToolBar;
import samples.symbology.nvg.gxy.gui.NVGGXYSymbologyCreationBar;

/**
 * Sample demonstrating how to visualize, create and edit NVG symbols in a GXY view.
 */
public class MainPanel extends samples.gxy.decoder.MainPanel {

  private NVGGXYFavoritesToolBar fFavoritesToolbar;
  private NVGGXYSymbologyCreationBar fCreationBar;
  private SymbolCustomizerPanelSelectionListener fSymbolCustomizerPanelSelectionListener;
  private ILcdGXYLayer fNVGLayer;

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class);

  public MainPanel() {
    super(false);
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-122.63, 37.66, 0.40, 0.30);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    addUndoRedoButtons();
    getView().setXYWorldReference(new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdMercator()));
    getView().setSmartPan(false); // works better with large icons
    fSymbolCustomizerPanelSelectionListener.addComponent(fFavoritesToolbar.getToolBar());
    fSymbolCustomizerPanelSelectionListener.addComponent(fCreationBar);
    getView().addLayeredListener(fSymbolCustomizerPanelSelectionListener);
    add(createGeometryToolBar(), BorderLayout.WEST);
  }

  private void addUndoRedoButtons() {
    ToolBar toolBar = getToolBars()[0];
    TLcdUndoManager undoManager = toolBar.getUndoManager();
    // Creation and editing supports undo-redo
    toolBar.addSpace();
    toolBar.addAction(new TLcdUndoAction(undoManager));
    toolBar.addAction(new TLcdRedoAction(undoManager));
    toolBar.addSpace(5);
  }

  @Override
  protected Component[] createToolBars() {
    ArrayList<Component> toolBars = new ArrayList<>(Arrays.asList(super.createToolBars()));
    toolBars.add(createNewSymbolToolBar((ToolBar) toolBars.get(0)));
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  protected JToolBar createNewSymbolToolBar(ToolBar aToolBar) {
    TLcdUndoManager undoableListener = aToolBar.getUndoManager();
    // A toolbar with a search field and browse button
    fCreationBar = new NVGGXYSymbologyCreationBar(getView(), getNVGLayer(), undoableListener, aToolBar.getSnappables());
    // A toolbar with favorite symbols
    fFavoritesToolbar = new NVGGXYFavoritesToolBar(getView(), getNVGLayer(), undoableListener, aToolBar.getSnappables(), new SymbologyFavorites());

    JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
    toolBar.add(fCreationBar);
    toolBar.addSeparator();
    JScrollPane scrollPane = new OptionsPanelScrollPane(fFavoritesToolbar.getToolBar());
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    toolBar.add(scrollPane);
    SwingUtil.makeFlat(toolBar);

    return toolBar;
  }

  /**
   *
   * @return create and return the settings panel for symbology
   */
  @Override
  protected JPanel createSettingsPanel() {
    JPanel panels = new JPanel(new BorderLayout());
    AbstractSymbolCustomizer symbolCustomizerPanel = createSymbolCustomizerPanel();
    fSymbolCustomizerPanelSelectionListener = new NVGSymbolCustomizerPanelSelectionListener(symbolCustomizerPanel);
    getView().getRootNode().addHierarchySelectionListener(fSymbolCustomizerPanelSelectionListener);
    panels.add(symbolCustomizerPanel.getComponent(), BorderLayout.NORTH);
    return panels;
  }

  /**
   * Creates the settings panel with custom options. For this sample we use Regular and Advanced options
   * @return settings panel
   */
  protected AbstractSymbolCustomizer createSymbolCustomizerPanel() {
    AbstractSymbolCustomizer symbolCustomizerPanel = NVGCustomizerFactory.createCustomizer(
        true, // for model elements
        fFavoritesToolbar.getFavorites(),
        true, // show titles
        null, // browse all symbols
        null // no translations
    );

    // initially selected symbology
    symbolCustomizerPanel.setSymbol(EMilitarySymbology.APP6A, null, null);
    symbolCustomizerPanel.getComponent().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    return symbolCustomizerPanel;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    getView().addGXYLayer(getNVGLayer());
  }

  /**
   * @return create and return the GXY Layer for NVG objects
   */
  private ILcdGXYLayer getNVGLayer() {
    try {
      if (null == fNVGLayer) {
        fNVGLayer = new DomainSpecificNVGLayerFactory().createGXYLayer(NVGModelFactory.createNVGModel());
      }
    } catch (IOException ex) {
      sLogger.warn(ex.getMessage());
    }
    return fNVGLayer;
  }

  private JToolBar createGeometryToolBar() {
    AToolBar toolBar = new AToolBar(getView()) {
      @Override
      protected AbstractButton insertButton(ILcdAction aAction, int aIndex, boolean aToggle) {
        AbstractButton button = SwingUtil.createButtonForAction(this, aAction, aToggle);
        button.addChangeListener(new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            JToggleButton button = (JToggleButton) e.getSource();

            // When disabled, hide the button by making it have height 0. See AnimatedLayoutManager.
            // When enabled use null as desired size, so that Swing calculates it itself.
            Dimension size = button.isEnabled() ? null : new Dimension(40, 0);
            button.setMinimumSize(size);
            button.setPreferredSize(size);
            button.setMaximumSize(size);
            button.revalidate();
          }
        });
        addComponent(button, aIndex);
        return button;
      }
    };

    toolBar.setOrientation(JToolBar.VERTICAL);
    toolBar.setLayout(AnimatedLayoutManager.create(toolBar.getLayout()));
    toolBar.add(Box.createVerticalGlue());
    NVGGXYCreateContentAction.Builder builder = NVGGXYCreateContentAction.newBuilder();
    builder.view(getView())
           .layer(getNVGLayer())
           .layerSubsetList(getToolBars()[0].getSnappables())
           .undoableListener(getToolBars()[0].getUndoManager());
    NVGUtilities.populateNVGActions(toolBar, builder);
    return toolBar;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding and Editing of Nato Vector Graphics (NVG)");
  }

  /**
   * Custom NVGGXYLayerFactory to register a painter provider for domain specific symbol set.
   */
  private class DomainSpecificNVGLayerFactory extends NVGGXYLayerFactory {
    @Override
    protected TLcdNVGGXYPainterProvider createNVGPainterProvider() {
      TLcdNVGGXYPainterProvider nvgPainterProvider = super.createNVGPainterProvider();
      TLcdGXYIconPainter iconPainter = new TLcdGXYIconPainter();
      iconPainter.setIconProvider(new CivilianIconProvider());
      nvgPainterProvider.registerPainterEditorProviders(CUSTOM_CIVILIAN_DOMAIN_NAME, iconPainter, iconPainter);
      return nvgPainterProvider;
    }
  }
}
