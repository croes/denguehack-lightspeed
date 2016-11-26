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
package samples.symbology.gxy.allsymbols;

import static samples.symbology.lightspeed.allsymbols.MainPanel.loadModels;

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
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SettingsPanel;
import samples.gxy.common.GXYLayerSelectionPanel;
import samples.gxy.decoder.MapSupport;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.allsymbols.AllSymbolsModelFactory.Filter;
import samples.symbology.gxy.APP6LayerFactory;
import samples.symbology.gxy.MS2525LayerFactory;

/**
 * Sample demonstrating how to visualize all military symbols in a GXY view.
 */
public class MainPanel extends samples.symbology.gxy.MainPanel {

  private static final TLcdInterval LABEL_SCALE_RANGE = new TLcdInterval(0.008, Double.MAX_VALUE);

  private GXYLayerSelectionPanel fPanel;
  private EMilitarySymbology fCurrentSymbology;
  private Map<EMilitarySymbology, Map<Filter, ILcdModel[]>> fModels;

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel view = super.createMap();
    view.setBackground(new Color(100, 100, 100));
    return view;
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(20.0, 19.8, 0.25, 0.25);
  }

  @Override
  protected void createGUI() {
    fModels = new HashMap<>();
    loadModels(fModels);
    super.createGUI();
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

  @Override
  protected GXYLayerSelectionPanel createSymbologySelectionPanel(PropertyChangeListener aPropertyChangeListener) {
    fPanel = new GXYLayerSelectionPanel(getView(), getOverlayPanel()) {
      @Override
      protected void changeLayer(ILcdGXYLayer aOldLayer, ILcdGXYLayer aNewLayer) {
        super.changeLayer(aOldLayer, aNewLayer);
        if (aNewLayer != null) {
          fCurrentSymbology = EMilitarySymbology.fromName(aNewLayer.getLabel());
        }
      }
    };
    fCurrentSymbology = EMilitarySymbology.APP6C;
    return fPanel;
  }

  private void populateSymbologySelectionPanel(GXYLayerSelectionPanel aPanel, Filter aFilter, PropertyChangeListener aPropertyChangeListener) {
    List<EMilitarySymbology> symbologies = Arrays.asList(EMilitarySymbology.values());
    Collections.reverse(symbologies);
    for (EMilitarySymbology symbology : symbologies) {
      Map<Filter, ILcdModel[]> modelsForSymbology = fModels.get(symbology);
      ILcdModel[] models = modelsForSymbology.get(aFilter);
      ILcdGXYLayer layer = createLayer(symbology, models, aPropertyChangeListener);
      aPanel.addLayer(layer.getLabel(), layer, symbology == fCurrentSymbology);
    }
  }

  private ILcdGXYLayer createLayer(EMilitarySymbology aSymbology, ILcdModel[] aModels, PropertyChangeListener aPropertyChangeListener) {
    ILcdGXYLayer layer = aSymbology.getStandard() instanceof ELcdAPP6Standard ?
                         new APP6LayerFactory(LABEL_SCALE_RANGE, aPropertyChangeListener).createGXYLayer(aModels[0]) :
                         new MS2525LayerFactory(LABEL_SCALE_RANGE, aPropertyChangeListener).createGXYLayer(aModels[0]);
    TLcdGXYLayer labelsLayer = new TLcdGXYLayer(aModels[1], "Labels");
    labelsLayer.setGXYPainterProvider(new TLcdGXYShapePainter());
    labelsLayer.setGXYPen(MapSupport.createPen(layer.getModel().getModelReference()));

    TLcdGXYLayerTreeNode layerTree = new TLcdGXYLayerTreeNode(layer.getLabel());
    layerTree.addLayer(layer);
    layerTree.addLayer(labelsLayer);

    return layerTree;
  }

  @Override
  protected void addData() throws IOException {
    // Don't load backgrounds data
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
    startSample(MainPanel.class, "All Symbols - GXY");
  }
}
