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
package samples.symbology.lightspeed;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

import samples.common.OptionsPanelScrollPane;
import samples.common.SettingsPanel;
import samples.common.SwingUtil;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspLayerSelectionPanel;
import samples.lightspeed.common.ToolBar;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.FlipOrientationAction;
import samples.symbology.common.app6.APP6ModelFactory;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.ms2525.MS2525ModelFactory;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbolCustomizerPanelSelectionListener;
import samples.symbology.common.util.SymbologyFavorites;

/**
 * Sample demonstrating how to visualize, create and edit military symbols in a Lightspeed view.
 */
public class MainPanel extends LightspeedSample {

  private LspFavoritesToolbar fFavoritesToolbar;
  private LspSymbologyCreationBar fCreationBar;
  private SymbolCustomizerPanelSelectionListener fSymbolCustomizerPanelSelectionListener;

  public MainPanel() {
    super(false, false);
  }

  protected final PropertyChangeListener getSymbolCustomizerPanelSelectionListener() {
    return fSymbolCustomizerPanelSelectionListener;
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    TLspViewTransformationUtil.setup2DView(getView(), new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdMercator()));
    addComponentToRightPanel(createSettingsPanel());
    fSymbolCustomizerPanelSelectionListener.addComponent(fFavoritesToolbar.getToolBar());
    fSymbolCustomizerPanelSelectionListener.addComponent(fCreationBar);
    getView().addLayeredListener(fSymbolCustomizerPanelSelectionListener);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    // createSymbologySelectionPanel already adds the necessary data.
    // Simply fit on the current military symbology layer.
    FitUtil.fitOnLayers(this, (ILspLayer) MilitarySymbolFacade.retrieveCompatibleEditableLayer(getView()));
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(getView(), this, true, true) {
      @Override
      protected ILcdAction[] createDefaultControllerActions() {
        List<ILcdAction> actions = new ArrayList<>(Arrays.asList(super.createDefaultControllerActions()));
        //Display the FlipOrientationAction on the context menu
        //when a suitable military symbol (arrows, lines etc.) is selected.
        actions.add(new FlipOrientationAction(getView()));
        return actions.toArray(new ILcdAction[actions.size()]);
      }
    };
    final TLcdUndoManager undoManager = toolBar.getUndoManager();

    // Creation and editing supports undo-redo.
    toolBar.addAction(new TLcdUndoAction(undoManager), 3);
    toolBar.addAction(new TLcdRedoAction(undoManager), 4);
    toolBar.addSpace(5);

    JToolBar creationBar = createNewSymbolToolBar(toolBar, undoManager);
    return new Component[]{toolBar, creationBar};
  }

  private JToolBar createNewSymbolToolBar(ToolBar aToolBar, TLcdUndoManager aUndoManager) {

    // A toolbar with a search field and browse button
    fCreationBar = new LspSymbologyCreationBar(getView(), aToolBar.getUndoManager());
    // A toolbar with favorite symbols
    fFavoritesToolbar = new LspFavoritesToolbar(getView(), aUndoManager, new SymbologyFavorites());

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

  protected JPanel createSettingsPanel() {
    // Create and configure a symbology selection panel and a symbol customizer panel.
    AbstractSymbolCustomizer symbolCustomizerPanel = createSymbolCustomizerPanel();
    fSymbolCustomizerPanelSelectionListener = new SymbolCustomizerPanelSelectionListener(symbolCustomizerPanel);
    LspLayerSelectionPanel symbologySelectionPanel = createSymbologySelectionPanel(fSymbolCustomizerPanelSelectionListener);
    getView().getRootNode().addHierarchySelectionListener(fSymbolCustomizerPanelSelectionListener);

    return new SettingsPanel()
        .contentBuilder()
        .pane("Symbology", symbologySelectionPanel)
        .pane("Symbol", symbolCustomizerPanel.getComponent())
        .build();
  }

  private AbstractSymbolCustomizer createSymbolCustomizerPanel() {
    AbstractSymbolCustomizer symbolCustomizerPanel = SymbolCustomizerFactory.createCustomizer(
        EnumSet.allOf(SymbolCustomizerFactory.Part.class), // show all parts of the customizer
        true, // for model elements
        fFavoritesToolbar.getFavorites(),
        true, // show titles
        null, // browse all symbols
        null // no translations
    );
    symbolCustomizerPanel.setUndoManager(getToolBars()[0].getUndoManager());
    symbolCustomizerPanel.setSymbol(EMilitarySymbology.APP6A, null, null);
    symbolCustomizerPanel.getComponent().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    return symbolCustomizerPanel;
  }

  protected LspLayerSelectionPanel createSymbologySelectionPanel(PropertyChangeListener aPropertyChangeListener) {
    LspLayerSelectionPanel panel = new LspLayerSelectionPanel(getView());
    ILcdModel model = new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6C);
    APP6LayerFactory app6LayerFactory = new APP6LayerFactory(aPropertyChangeListener);
    MS2525LayerFactory ms2525LayerFactory = new MS2525LayerFactory(aPropertyChangeListener);
    ILspLayer layer = app6LayerFactory.createLayer(model);
    panel.addLayer("APP-6C", layer);
    layer = app6LayerFactory.createLayer(new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6B));
    panel.addLayer("APP-6B", layer);
    layer = app6LayerFactory.createLayer(new APP6ModelFactory().createModel(ELcdAPP6Standard.APP_6A));
    panel.addLayer("APP-6A", layer);
    layer = ms2525LayerFactory.createLayer(new MS2525ModelFactory(ELcdMS2525Standard.MIL_STD_2525c).createModel());
    panel.addLayer("MIL-STD-2525c", layer);
    layer = ms2525LayerFactory.createLayer(new MS2525ModelFactory(ELcdMS2525Standard.MIL_STD_2525b).createModel());
    layer.addPropertyChangeListener(aPropertyChangeListener);
    panel.addLayer("MIL-STD-2525b", layer);
    return panel;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Military Symbology");
  }

}
