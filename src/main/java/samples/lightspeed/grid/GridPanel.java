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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.painter.grid.TLspGeorefGridOverlayLabelBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspGeorefGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridOverlayLabelBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridOverlayLabelBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle;

import samples.gxy.common.TitledPanel;

class GridPanel extends JPanel {

  private final ILspAWTView fView;

  private final Map<GridType, GridStyleSupport> fGridStyleSupportMap;

  private final GridStylePanel fGridStylePanel;
  private ILspLayer fCurrentGridLayer;

  public GridPanel(ILspAWTView aView) {
    fView = aView;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(createGridLocationPanel(aView));
    add(createGridTypePanel());

    fGridStyleSupportMap = new HashMap<>();
    fGridStyleSupportMap.put(GridType.LON_LAT, new LonLatGridStyleSupport(this));
    fGridStyleSupportMap.put(GridType.MGRS, new MGRSGridStyleSupport(this));
    fGridStyleSupportMap.put(GridType.GEOREF, new GeorefGridStyleSupport(this));
    fGridStyleSupportMap.put(GridType.XY, new XYGridStyleSupport(this));
    fGridStylePanel = new GridStylePanel();
    add(fGridStylePanel);

    fGridStyleSupportMap.get(GridType.LON_LAT).createLayer();
  }

  public void setGridLayer(GridType aGridType, ILspLayer aNewGridLayer) {
    addGridLayerToView(aNewGridLayer);
    fGridStylePanel.setStylePanel(aGridType, aNewGridLayer);
    fGridStylePanel.showStylePanel(aGridType);
  }

  private void addGridLayerToView(ILspLayer aNewGridLayer) {
    if (fCurrentGridLayer != null) {
      fView.removeLayer(fCurrentGridLayer);
    }
    fCurrentGridLayer = aNewGridLayer;
    if (aNewGridLayer != null) {
      fView.addLayer(fCurrentGridLayer);
    }
  }

  private JPanel createGridTypePanel() {
    ButtonGroup group = new ButtonGroup();
    JPanel gridTypePanel = new JPanel(new GridLayout(5, 1));

    for (GridType gridType : GridType.values()) {
      JRadioButton button = new JRadioButton();
      button.setAction(new CreateLayerAction(gridType));
      button.setSelected(gridType == GridType.LON_LAT);
      group.add(button);
      gridTypePanel.add(button);
    }
    return TitledPanel.createTitledPanel("Grid type", gridTypePanel);
  }

  private JPanel createGridLocationPanel(ILspView aView) {
    Font font = new Font("Default", Font.PLAIN, 11);

    // Create a MGRS mouse location readout
    TLspMGRSGridStyle mgrsOverlayStyle = MGRSGridStyleFactory.createOverlayStyle(false, font, Color.black, 0.75f, Color.white, 0, true);
    JComponent mgrsMouseComponent = TLspMGRSGridOverlayLabelBuilder.newBuilder()
                                                                   .view(aView)
                                                                   .content(TLspMGRSGridOverlayLabelBuilder.Content.COORDINATE_AT_MOUSE_CURSOR)
                                                                   .style(mgrsOverlayStyle)
                                                                   .build();

    // Create a UTM/UPS mouse location readout
    TLspMGRSGridStyle utmUpsOverlayStyle = MGRSGridStyleFactory.createOverlayStyle(false, font, Color.black, 0.75f, Color.white, 0, false);
    JComponent utmUpsMouseComponent = TLspMGRSGridOverlayLabelBuilder.newBuilder()
                                                                     .view(aView)
                                                                     .content(TLspMGRSGridOverlayLabelBuilder.Content.COORDINATE_AT_MOUSE_CURSOR)
                                                                     .style(utmUpsOverlayStyle)
                                                                     .build();

    // Create a Georef mouse location readout
    TLspGeorefGridStyle georefOverlayStyle = GeorefGridStyleFactory.createOverlayStyle(false, font, Color.black, 0.75f, Color.white, 0);
    JComponent georefMouseComponent = TLspGeorefGridOverlayLabelBuilder.newBuilder()
                                                                       .view(aView)
                                                                       .content(TLspGeorefGridOverlayLabelBuilder.Content.COORDINATE_AT_MOUSE_CURSOR)
                                                                       .style(georefOverlayStyle)
                                                                       .build();

    // Create an XY mouse location readout
    TLspXYGridStyle xyOverlayStyle = XYGridStyleFactory.createOverlayStyle(font, Color.black, 0.75f, Color.white, 0);
    JComponent xyMouseComponent = TLspXYGridOverlayLabelBuilder.newBuilder()
                                                               .view(aView)
                                                               .content(TLspXYGridOverlayLabelBuilder.Content.COORDINATE_AT_MOUSE_CURSOR)
                                                               .style(xyOverlayStyle)
                                                               .build();

    JPanel gridLocationPanel = new JPanel(new GridBagLayout());

    addMouseComponentToPanel(gridLocationPanel, mgrsMouseComponent, new JLabel("MGRS: "), 0);
    addMouseComponentToPanel(gridLocationPanel, utmUpsMouseComponent, new JLabel("UTM/UPS: "), 1);
    addMouseComponentToPanel(gridLocationPanel, georefMouseComponent, new JLabel("Georef: "), 2);
    addMouseComponentToPanel(gridLocationPanel, xyMouseComponent, new JLabel("XY: "), 3);

    return TitledPanel.createTitledPanel("Grid location", gridLocationPanel);
  }

  private static void addMouseComponentToPanel(JPanel aPanel, JComponent aMouseComponent, JLabel aLabel, int aGridY) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(5, 0, 0, 0);
    constraints.weightx = 0.0;
    constraints.gridy = aGridY;
    aPanel.add(aLabel, constraints);
    constraints.weightx = 1.0;
    aPanel.add(aMouseComponent, constraints);
  }

  private class CreateLayerAction extends AbstractAction {

    private final GridType fGridType;

    public CreateLayerAction(GridType aGridType) {
      super(aGridType.name());
      fGridType = aGridType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fGridStyleSupportMap.get(fGridType).createLayer();
    }
  }

  /**
   * Panel that makes it possible to edit the style of a grid layer.
   */
  private class GridStylePanel extends JPanel {

    private static final String EMPTY_CARD = "empty";

    private final Map<GridType, JPanel> fPanels  = new HashMap<>();
    private final CardLayout fStylePanelLayout = new CardLayout();

    public GridStylePanel() {
      setLayout(fStylePanelLayout);
      add(new JPanel(), EMPTY_CARD);
    }

    public void setStylePanel(GridType aGridType, ILspLayer aLayer) {
      JPanel stylePanel = fPanels.get(aGridType);
      if (stylePanel != null) {
        remove(stylePanel);
      }
      stylePanel = fGridStyleSupportMap.get(aGridType).createStylePanel(aLayer);
      fPanels.put(aGridType, stylePanel);
      add(stylePanel, aGridType.name());
    }

    public void showStylePanel(GridType aGridType) {
      fStylePanelLayout.show(this, aGridType.name());
    }
  }
}
