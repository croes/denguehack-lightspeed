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
package samples.symbology.lightspeed.allsymbols;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;

import samples.common.SettingsPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LspLayerSelectionPanel;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.allsymbols.AllSymbolsModelFactory;
import samples.symbology.common.allsymbols.AllSymbolsModelFactory.Filter;
import samples.symbology.common.allsymbols.SymbologyLayout;
import samples.symbology.lightspeed.APP6LayerFactory;
import samples.symbology.lightspeed.MS2525LayerFactory;

/**
 * Sample demonstrating how to visualize all military symbols in a Lightspeed view.
 */
public class MainPanel extends samples.symbology.lightspeed.MainPanel {

  private static final TLcdInterval LABEL_SCALE_RANGE = new TLcdInterval(0.008, Double.MAX_VALUE);

  private LspLayerSelectionPanel fPanel;
  private EMilitarySymbology fCurrentSymbology;
  private Map<EMilitarySymbology, Map<Filter, ILcdModel[]>> fModels;

  @Override
  protected ILspAWTView createView(ILspView.ViewType aViewType) {
    // Use a grey background
    return TLspViewBuilder.newBuilder()
                          .viewType(aViewType)
                          .addAtmosphere(true)
                          .background(new Color(100, 100, 100))
                          .buildAWTView();
  }

  @Override
  protected void createGUI() {
    fModels = new HashMap<>();
    loadModels(fModels);
    super.createGUI();
  }

  public static void loadModels(Map<EMilitarySymbology, Map<Filter, ILcdModel[]>> aModels) {
    for (EMilitarySymbology symbology : EMilitarySymbology.values()) {
      Map<Filter, ILcdModel[]> modelsMap = new HashMap<>();
      for (Filter filter : Filter.values()) {
        AllSymbolsModelFactory modelFactory = new AllSymbolsModelFactory(new SymbologyLayout(35, 0.05, 1.6, 20, 20), filter);
        ILcdModel[] models = modelFactory.createModel(symbology);
        modelsMap.put(filter, models);
      }
      aModels.put(symbology, modelsMap);
    }
  }

  @Override
  protected void addData() throws IOException {
    // Don't load backgrounds data
  }

  @Override
  protected JPanel createSettingsPanel() {
    final JPanel settingsPanel = super.createSettingsPanel();
    final JPanel filterPanel = createFilterPanel();
    return new SettingsPanel()
        .contentBuilder()
        .pad(0)
        .pane("Filter", filterPanel)
        .pane(settingsPanel)
        .build();
  }

  private JPanel createFilterPanel() {
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
    ButtonGroup filterButtonGroup = new ButtonGroup();

    JRadioButton radioButton1 = new JRadioButton(new FilterSelectionAction("Icons", Filter.ICONS));
    filterButtonGroup.add(radioButton1);
    filterPanel.add(radioButton1);

    JRadioButton radioButton2 = new JRadioButton(new FilterSelectionAction("Tactical Graphics", Filter.TACTICAL_GRAPHICS));
    filterButtonGroup.add(radioButton2);
    filterPanel.add(radioButton2);

    radioButton1.setSelected(true);
    radioButton1.getAction().actionPerformed(null);
    return filterPanel;
  }

  protected LspLayerSelectionPanel createSymbologySelectionPanel(PropertyChangeListener aPropertyChangeListener) {
    fPanel = new LspLayerSelectionPanel(getView()) {
      @Override
      protected void changeLayer(ILspLayer aOldLayer, ILspLayer aNewLayer) {
        super.changeLayer(aOldLayer, aNewLayer);
        if (aNewLayer != null) {
          fCurrentSymbology = EMilitarySymbology.fromName(aNewLayer.getLabel());
        }
      }
    };

    fCurrentSymbology = EMilitarySymbology.APP6C;
    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(20.0, 19.8, 0.25, 0.25), new TLcdGeodeticReference());
    return fPanel;
  }

  private void populateSymbologySelectionPanel(LspLayerSelectionPanel aPanel, Filter aFilter, PropertyChangeListener aPropertyChangeListener) {
    List<EMilitarySymbology> symbologies = Arrays.asList(EMilitarySymbology.values());
    Collections.reverse(symbologies);
    for (EMilitarySymbology symbology : symbologies) {
      Map<Filter, ILcdModel[]> modelsForSymbology = fModels.get(symbology);
      ILcdModel[] models = modelsForSymbology.get(aFilter);
      ILspLayer layer = createLayer(symbology, models, aPropertyChangeListener);
      aPanel.addLayer(layer.getLabel(), layer, symbology == fCurrentSymbology);
    }
  }

  private ILspLayer createLayer(EMilitarySymbology aSymbology, ILcdModel[] aModels, PropertyChangeListener aPropertyChangeListener) {

    ILspLayer layer = aSymbology.getStandard() instanceof ELcdAPP6Standard ?
                      new APP6LayerFactory(LABEL_SCALE_RANGE, aPropertyChangeListener, APP6LayerFactory.Clustering.DISABLED).createLayer(aModels[0]) :
                      new MS2525LayerFactory(LABEL_SCALE_RANGE, aPropertyChangeListener, MS2525LayerFactory.Clustering.DISABLED).createLayer(aModels[0]);

    ILspLayer labelsLayer = TLspShapeLayerBuilder.newBuilder()
                                                 .bodyStyler(TLspPaintState.REGULAR, TLspFillStyle.newBuilder().color(Color.black).build())
                                                 .model(aModels[1])
                                                 .build();

    TLspLayerTreeNode layerTree = new TLspLayerTreeNode(layer.getLabel());
    layerTree.addLayer(layer);
    layerTree.addLayer(labelsLayer);

    return layerTree;
  }

  private class FilterSelectionAction extends AbstractAction {

    private final Filter fFilter;

    public FilterSelectionAction(String aName, Filter aFilter) {
      super(aName);
      fFilter = aFilter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fPanel.clear();
      populateSymbologySelectionPanel(fPanel, fFilter, getSymbolCustomizerPanelSelectionListener());
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "All Symbols - Lightspeed");
  }
}
