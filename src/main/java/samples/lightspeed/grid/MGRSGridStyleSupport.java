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
package samples.lightspeed.grid;

import static samples.lightspeed.grid.GridStyleCustomizerPanelUtil.createStyleCombobox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle.Orientation;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.gxy.common.TitledPanel;

/**
 * Support class to style a MGRS grid. It can create an {@code ILspStyler} and a UI to customize the
 * styles returned by that {@code ILspStyler}
 */
final class MGRSGridStyleSupport implements GridStyleSupport {

  private final GridPanel fGridPanel;
  private boolean fCoarse = false;
  private boolean fMGRSCoordinates = true;
  private Orientation fOrientation = Orientation.ALONG_LINE;
  private double fEdgeOffset = 6.0;
  private double fGridOffset = 3.0;
  private Collection<TLspCustomizableStyle> fCustomizableStyles;

  private static final String sMGRS = "MGRS";
  private static final String sUTM_UPS = "UTM / UPS";

  public MGRSGridStyleSupport(GridPanel aGridPanel) {
    fGridPanel = aGridPanel;
  }

  /**
   * Creates a layer with the current style settings.
   */
  @Override
  public void createLayer() {
    ILcdModel mgrsGridModel = MGRSGridLayerFactory.createMGRSGridModel();
    Collection<ILspLayer> layers = new MGRSGridLayerFactory(fCoarse, fMGRSCoordinates, fOrientation, fGridOffset, fEdgeOffset, fCustomizableStyles).createLayers(mgrsGridModel);
    fGridPanel.setGridLayer(GridType.MGRS, layers.iterator().next());
  }

  @Override
  public JPanel createStylePanel(ILspLayer aLayer) {
    ILspStyledLayer styledLayer = (ILspStyledLayer) aLayer;
    ILspCustomizableStyler bodyStyler = (ILspCustomizableStyler) styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    ILspCustomizableStyler labelStyler = (ILspCustomizableStyler) styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL);

    Collection<TLspCustomizableStyle> bodyStyles = bodyStyler.getStyles();
    Collection<TLspCustomizableStyle> labelStyles = labelStyler.getStyles();
    fCustomizableStyles = new ArrayList<>();
    fCustomizableStyles.addAll(bodyStyles);
    fCustomizableStyles.addAll(labelStyles);

    Map<String, List<TLspCustomizableStyle>> styleMap = new LinkedHashMap<>();
    adjustStylesMap(bodyStyles, styleMap);
    adjustStylesMap(labelStyles, styleMap);

    JPanel outerPanel = new JPanel(new BorderLayout());
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    for (Map.Entry<String, List<TLspCustomizableStyle>> entry : styleMap.entrySet()) {
      panel.add(new JLabel(entry.getKey()));
      panel.add(createStyleCombobox(entry.getValue().toArray(new TLspCustomizableStyle[entry.getValue().size()])));
    }
    panel.add(new JLabel("Spacing"));
    panel.add(createSpacingCombobox());
    panel.add(new JLabel("Coordinate Format"));
    panel.add(createCoordinateFormatCombobox());
    panel.add(new JLabel("Orientation"));
    panel.add(createOrientationCombobox());
    panel.add(new JLabel("Line Offset"));
    panel.add(createGridOffsetCombobox());
    panel.add(new JLabel("Border Offset"));
    panel.add(createEdgeOffsetCombobox());
    outerPanel.add(panel, BorderLayout.NORTH);//wrap the panel for better vertical resizing behavior
    return TitledPanel.createTitledPanel("Grid style", outerPanel);
  }

  private void adjustStylesMap(Collection<TLspCustomizableStyle> aBodyStyles, Map<String, List<TLspCustomizableStyle>> aStyleMap) {
    for (TLspCustomizableStyle bodyStyle : aBodyStyles) {
      List<TLspCustomizableStyle> styles = aStyleMap.get(bodyStyle.getDisplayName());
      if (styles == null) {
        styles = new ArrayList<>();
        aStyleMap.put(bodyStyle.getDisplayName(), styles);
      }
      styles.add(bodyStyle);
    }
  }

  private Component createSpacingCombobox() {
    final JComboBox<String> comboBox = new JComboBox<>(new String[]{"Default", "Coarse"});
    comboBox.setSelectedItem(fCoarse ? "Coarse" : "Default");
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fCoarse = "Coarse".equals(comboBox.getSelectedItem());
        createLayer();
      }
    });

    return comboBox;
  }

  private Component createCoordinateFormatCombobox() {
    final JComboBox<String> comboBox = new JComboBox<>(new String[]{sMGRS, sUTM_UPS});
    comboBox.setSelectedItem(fMGRSCoordinates ? sMGRS : sUTM_UPS);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fMGRSCoordinates = sMGRS.equals(comboBox.getSelectedItem());
        createLayer();
      }
    });

    return comboBox;
  }

  private Component createOrientationCombobox() {
    final JComboBox<Orientation> comboBox = new JComboBox<>(Orientation.values());
    comboBox.setSelectedItem(fOrientation);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fOrientation = (Orientation) comboBox.getSelectedItem();
        createLayer();
      }
    });
    return comboBox;
  }

  private Component createGridOffsetCombobox() {
    final JComboBox<Double> comboBox = new JComboBox<>(new Double[]{3.0, 8.0, 16.0});
    comboBox.setSelectedItem(fGridOffset);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fGridOffset = (Double) comboBox.getSelectedItem();
        createLayer();
      }
    });
    return comboBox;
  }

  private Component createEdgeOffsetCombobox() {
    final JComboBox<Double> comboBox = new JComboBox<>(new Double[]{6.0, 12.0, 24.0});
    comboBox.setSelectedItem(fEdgeOffset);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fEdgeOffset = (Double) comboBox.getSelectedItem();
        createLayer();
      }
    });
    return comboBox;
  }
}
