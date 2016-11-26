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
package samples.symbology.nvg.lightspeed;

import static samples.symbology.nvg.common.CivilianIconProvider.CUSTOM_CIVILIAN_DOMAIN_NAME;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import com.luciad.format.nvg.lightspeed.TLspNVGLayerBuilder;
import com.luciad.format.nvg.lightspeed.TLspNVGStyler;
import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.ILcdClassifier;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.projection.TLcdMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

import samples.common.AnimatedLayoutManager;
import samples.common.OptionsPanelScrollPane;
import samples.common.SwingUtil;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.ToolBar;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.util.SymbolCustomizerPanelSelectionListener;
import samples.symbology.common.util.SymbologyFavorites;
import samples.symbology.nvg.common.CivilianIconProvider;
import samples.symbology.nvg.common.NVGClassifier;
import samples.symbology.nvg.common.NVGModelFactory;
import samples.symbology.nvg.common.NVGUtilities;
import samples.symbology.nvg.gui.NVGCustomizerFactory;
import samples.symbology.nvg.gui.NVGSymbolCustomizerPanelSelectionListener;
import samples.symbology.nvg.lightspeed.gui.NVGLSPCreateContentAction;
import samples.symbology.nvg.lightspeed.gui.NVGLspFavoritesToolBar;
import samples.symbology.nvg.lightspeed.gui.NVGLspSymbologyCreationBar;

/**
 * Sample demonstrating how to visualize, create and edit NVG symbols in a Lightspeed view.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private NVGLspSymbologyCreationBar fCreationBar;
  private NVGLspFavoritesToolBar fFavoritesToolbar;
  private ILspInteractivePaintableLayer fLayer;
  private SymbolCustomizerPanelSelectionListener fSymbolCustomizerPanelSelectionListener;

  public MainPanel() {
    super(false, false);
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    // Add a double click action to the default controller allowing to customize
    // the selected symbol.
    ToolBar toolBar = new ToolBar(getView(), this, true, true);
    final TLcdUndoManager undoManager = toolBar.getUndoManager();

    // Creation and editing supports undo-redo.
    toolBar.addAction(new TLcdUndoAction(undoManager), 3);
    toolBar.addAction(new TLcdRedoAction(undoManager), 4);
    toolBar.addSpace(5);

    JToolBar creationBar = createNewSymbolToolBar(undoManager);
    JToolBar geometryToolBar = createGeometryToolBar(undoManager);
    add(geometryToolBar, BorderLayout.WEST);
    return new Component[]{toolBar, creationBar};
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

  protected JToolBar createNewSymbolToolBar(ILcdUndoableListener aUndoableListener) {
    // A toolbar with a search field and browse button
    fCreationBar = new NVGLspSymbologyCreationBar(getView(), aUndoableListener, getLayer());

    // A toolbar with favorite symbols
    fFavoritesToolbar = new NVGLspFavoritesToolBar(getView(), aUndoableListener, getLayer(), new SymbologyFavorites());

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

  private ILspInteractivePaintableLayer getLayer() {
    if (null == fLayer) {
      try {

        ILcdModel model = NVGModelFactory.createNVGModel();
        ILcdClassifier classifier = new NVGClassifier();
        TLcdClusteringTransformer transformer = TLcdClusteringTransformer.newBuilder()
                                                                         .classifier(classifier)
                                                                         .defaultParameters()
                                                                           .clusterSize(150)
                                                                           .build()
                                                                         .forClass("SEA")
                                                                           .noClustering()
                                                                           .build()
                                                                         .forClass(NVGClassifier.OTHER)
                                                                           .noClustering()
                                                                           .build()
                                                                         .build();
        model = TLcdTransformingModelFactory.createTransformingModel(model, transformer);
        ILspStyler styler = new DomainSpecificNVGStyler(TLspPaintState.REGULAR);
        ILspStyler selectedStyler = new DomainSpecificNVGStyler(TLspPaintState.SELECTED);
        ILspStyler editedStyler = new DomainSpecificNVGStyler(TLspPaintState.EDITED);
        styler = new ClusterAwareNVGSymbolStyler(styler, TLspPaintState.REGULAR);
        selectedStyler = new ClusterAwareNVGSymbolStyler(selectedStyler, TLspPaintState.SELECTED);
        editedStyler = new ClusterAwareNVGSymbolStyler(editedStyler, TLspPaintState.EDITED);
        ClusterAwareNVGSymbolLabelStylerWrapper labelStyler = new ClusterAwareNVGSymbolLabelStylerWrapper(new TLspNVGStyler(), TLspPaintState.REGULAR);
        ClusterAwareNVGSymbolLabelStylerWrapper selectedLabelStyler = new ClusterAwareNVGSymbolLabelStylerWrapper(new TLspNVGStyler(TLspPaintState.SELECTED), TLspPaintState.SELECTED);
        ClusterAwareNVGSymbolLabelStylerWrapper editedLabelStyler = new ClusterAwareNVGSymbolLabelStylerWrapper(new TLspNVGStyler(TLspPaintState.EDITED), TLspPaintState.EDITED);
        ILspLayer layer = TLspNVGLayerBuilder.newBuilder()
                                             .bodyStyler(TLspPaintState.REGULAR, styler)
                                             .bodyStyler(TLspPaintState.SELECTED, selectedStyler)
                                             .bodyStyler(TLspPaintState.EDITED, editedStyler)
                                             .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                             .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
                                             .labelStyler(TLspPaintState.EDITED, editedLabelStyler)
                                             .model(model)
                                             .build();

        fLayer = (ILspInteractivePaintableLayer) layer;
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    return fLayer;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    getView().addLayer(fLayer);
    FitUtil.fitOnLayers(this, fLayer);
  }

  protected JPanel createSettingsPanel() {
    JPanel panels = new JPanel(new BorderLayout());
    AbstractSymbolCustomizer symbolCustomizerPanel = createSymbolCustomizerPanel();
    fSymbolCustomizerPanelSelectionListener = new NVGSymbolCustomizerPanelSelectionListener(symbolCustomizerPanel);
    getView().getRootNode().addHierarchySelectionListener(fSymbolCustomizerPanelSelectionListener);
    panels.add(symbolCustomizerPanel.getComponent(), BorderLayout.CENTER);
    return panels;
  }

  private AbstractSymbolCustomizer createSymbolCustomizerPanel() {
    AbstractSymbolCustomizer symbolCustomizerPanel = NVGCustomizerFactory.createCustomizer(
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

  private JToolBar createGeometryToolBar(ILcdUndoableListener aUndoManager) {
    samples.lightspeed.common.AToolBar toolBar = new samples.lightspeed.common.AToolBar(getView()) {
      @Override
      protected void insertAction(ILcdAction aAction, int aIndex) {
        boolean toggleButton = aAction.getValue(ILcdAction.SELECTED_KEY) != null;
        AbstractButton button = SwingUtil.createButtonForAction(this, aAction, toggleButton);
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
      }
    };

    toolBar.setOrientation(JToolBar.VERTICAL);
    toolBar.setLayout(AnimatedLayoutManager.create(toolBar.getLayout()));
    toolBar.add(Box.createVerticalGlue());
    NVGLSPCreateContentAction.Builder builder = NVGLSPCreateContentAction.newBuilder();
    builder.view(getView())
           .layer(getLayer())
           .undoableListener(aUndoManager);
    NVGUtilities.populateNVGActions(toolBar, builder);
    return toolBar;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding and Editing of Nato Vector Graphics (NVG)");
  }

  private static class DomainSpecificNVGStyler extends TLspNVGStyler {

    private ILcdObjectIconProvider fDomainSpecificIconProvider = new CivilianIconProvider();

    public DomainSpecificNVGStyler(TLspPaintState aPaintState) {
      super(aPaintState);
    }

    public void style(Collection<?> aObjects, ALspStyleCollector aCollector, TLspContext aContext) {
      List<Object> classicNVGObjects = new ArrayList<>();
      for (Object object : aObjects) {
        if (isDomainSpecificObject(object)) {
          aCollector.object(object).style(retrieveStyle(object)).submit();
        } else {
          classicNVGObjects.add(object);
        }
      }
      super.style(classicNVGObjects, aCollector, aContext);
    }

    private boolean isDomainSpecificObject(Object object) {
      boolean result = false;
      if (object instanceof TLcdNVG20SymbolizedContent) {
        TLcdNVG20SymbolizedContent symbolizedContent = (TLcdNVG20SymbolizedContent) object;
        TLcdNVGSymbol symbol = symbolizedContent.getSymbol();
        if (CUSTOM_CIVILIAN_DOMAIN_NAME.equals(symbol.getStandardName())) {
          result = true;
        }
      }
      return result;
    }

    private ALspStyle retrieveStyle(Object aObject) {
      return TLspIconStyle.newBuilder()
                          .icon(fDomainSpecificIconProvider.getIcon(aObject))
                          .build();
    }

  }

}
