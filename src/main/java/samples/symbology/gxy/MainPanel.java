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
package samples.symbology.gxy;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.projection.TLcdMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.OptionsPanelScrollPane;
import samples.common.SettingsPanel;
import samples.common.SwingUtil;
import samples.gxy.common.GXYLayerSelectionPanel;
import samples.gxy.common.GXYSample;
import samples.gxy.common.toolbar.ToolBar;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.FlipOrientationAction;
import samples.symbology.common.app6.APP6ModelFactory;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.ms2525.MS2525ModelFactory;
import samples.symbology.common.util.SymbolCustomizerPanelSelectionListener;
import samples.symbology.common.util.SymbologyFavorites;

/**
 * Sample demonstrating how to visualize, create and edit military symbols in a GXY view.
 */
public class MainPanel extends GXYSample {

  protected GXYFavoritesToolbar fFavoritesToolbar;
  protected GXYSymbologyCreationBar fCreationBar;
  protected SymbolCustomizerPanelSelectionListener fSymbolCustomizerPanelSelectionListener;

  protected final PropertyChangeListener getSymbolCustomizerPanelSelectionListener() {
    return fSymbolCustomizerPanelSelectionListener;
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(7.11, 43.55, 4.12, 1.77);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    getView().setXYWorldReference(new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdMercator()));
    getView().setSmartPan(false); // works better with large icons
    fSymbolCustomizerPanelSelectionListener.addComponent(fFavoritesToolbar.getToolBar());
    fSymbolCustomizerPanelSelectionListener.addComponent(fCreationBar);
    getView().addLayeredListener(fSymbolCustomizerPanelSelectionListener);
  }

  @Override
  protected Component[] createToolBars() {
    ToolBar toolBar = new ToolBar(getView(), true, this, getOverlayPanel()) {
      @Override
      protected Component createMouseLocationComponent() {
        return null; // we'll add our mouse location as an overlay, not in the toolbar
      }

      @Override
      protected ILcdAction[] createEditActions() {
        //Display the FlipOrientationAction on the context menu
        //when a suitable military symbol (arrows, lines etc.) is selected.
        List<ILcdAction> actions = new ArrayList<>(Arrays.asList(super.createEditActions()));
        actions.add(new FlipOrientationAction(getView()));
        return actions.toArray(new ILcdAction[actions.size()]);
      }
    };

    ArrayList<Component> toolBars = new ArrayList<>();
    toolBars.add(toolBar);

    TLcdUndoManager undoManager = toolBar.getUndoManager();

    // Creation and editing supports undo-redo.
    toolBar.addSpace();
    toolBar.addAction(new TLcdUndoAction(undoManager));
    toolBar.addAction(new TLcdRedoAction(undoManager));
    toolBar.addSpace(5);

    toolBars.add(createNewSymbolToolBar(toolBar, undoManager));
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  protected JToolBar createNewSymbolToolBar(ToolBar aToolBar, TLcdUndoManager aUndoManager) {

    // A toolbar with a search field and browse button
    fCreationBar = new GXYSymbologyCreationBar(getView(), aToolBar.getUndoManager(), aToolBar.getSnappables());
    // A toolbar with favorite symbols
    fFavoritesToolbar = new GXYFavoritesToolbar(getView(), aUndoManager, aToolBar.getSnappables(), new SymbologyFavorites());

    JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
    SwingUtil.makeFlat(toolBar);
    toolBar.add(fCreationBar);
    toolBar.addSeparator();
    JToolBar favoritesToolBar = fFavoritesToolbar.getToolBar();
    if(TLcdSystemPropertiesUtil.isMacOS()) {
      favoritesToolBar.setOpaque(true);
    }
    JScrollPane scrollPane = new OptionsPanelScrollPane(favoritesToolBar);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    toolBar.add(scrollPane);
    return toolBar;
  }

  @Override
  protected JPanel createSettingsPanel() {
    // Create and configure a symbology selection panel and a symbol customizer panel.
    AbstractSymbolCustomizer symbolCustomizerPanel = createSymbolCustomizerPanel();
    fSymbolCustomizerPanelSelectionListener = new SymbolCustomizerPanelSelectionListener(symbolCustomizerPanel);
    GXYLayerSelectionPanel symbologySelectionPanel = createSymbologySelectionPanel(fSymbolCustomizerPanelSelectionListener);
    getView().getRootNode().addHierarchySelectionListener(fSymbolCustomizerPanelSelectionListener);

    // Add a title and put them on top of each other.
    return new SettingsPanel()
        .contentBuilder()
        .pane("Symbology", symbologySelectionPanel)
        .pane("Symbol", symbolCustomizerPanel.getComponent())
        .build();
  }

  protected AbstractSymbolCustomizer createSymbolCustomizerPanel() {
    AbstractSymbolCustomizer symbolCustomizerPanel = SymbolCustomizerFactory.createCustomizer(
        EnumSet.allOf(SymbolCustomizerFactory.Part.class), // show all parts of the customizer
        true, // for model elements
        fFavoritesToolbar.getFavorites(),
        true, // show titles
        null, // browse all symbols
        null // no translations
    );
    symbolCustomizerPanel.setSymbol(EMilitarySymbology.APP6A, null, null);
    symbolCustomizerPanel.getComponent().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    return symbolCustomizerPanel;
  }

  protected GXYLayerSelectionPanel createSymbologySelectionPanel(PropertyChangeListener aPropertyChangeListener) {
    GXYLayerSelectionPanel panel = new GXYLayerSelectionPanel(getView(), getOverlayPanel());
    APP6LayerFactory app6LayerFactory = new APP6LayerFactory(aPropertyChangeListener);
    MS2525LayerFactory ms2525LayerFactory = new MS2525LayerFactory(aPropertyChangeListener);
    ILcdGXYLayer layer = app6LayerFactory.createGXYLayer(new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6C));
    panel.addLayer("APP-6C", layer);
    layer = app6LayerFactory.createGXYLayer(new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6B));
    panel.addLayer("APP-6B", layer);
    layer = app6LayerFactory.createGXYLayer(new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6A));
    panel.addLayer("APP-6A", layer);
    layer = ms2525LayerFactory.createGXYLayer(new MS2525ModelFactory(ELcdMS2525Standard.MIL_STD_2525c).createModel());
    panel.addLayer("MIL-STD-2525c", layer);
    layer = ms2525LayerFactory.createGXYLayer(new MS2525ModelFactory(ELcdMS2525Standard.MIL_STD_2525b).createModel());
    panel.addLayer("MIL-STD-2525b", layer);
    return panel;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Military Symbology");
  }

}

