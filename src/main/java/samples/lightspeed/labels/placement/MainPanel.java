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
package samples.lightspeed.labels.placement;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to configure labels.
 * For more information, see
 * <ul>
 *   - CitiesLayerFactory</li>
 *   - RiversLayerFactory
 *   - StatesLayerFactory
 * </ul>
 * <p>
 *   Label placement takes into account the labels that are already placed to avoid overlap. A layer
 *   will therefore most likely have less labels painted when it is at the bottom than when it is at
 *   the top of the view.
 * </p>
 * <p>
 *   To see the effects of the label placement, select a layer by clicking it in the layer
 *   control and:
 *   - change the layers visibility
 *   - set the layer labeled/not labeled
 *   - move the layer up/down
 *   Zooming in/out will also have an effect on the labeling.
 * <p/>
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.US_STATES).layer(new StatesLayerFactory()).label("States").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_RIVERS).layer(new RiversLayerFactory()).label("Rivers").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_CITIES).layer(new CitiesLayerFactory()).label("Cities").addToView(getView()).fit();
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Add a panel containing a few label styling options
    JPanel optionsPanel = createOptionsPanel();
    addComponentToRightPanel(TitledPanel.createTitledPanel("Label options", optionsPanel));
  }

  @SuppressWarnings("unchecked")
  private JPanel createOptionsPanel() {
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new GridBagLayout());

    // Add 'Opacity' label
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.LINE_START;
    optionsPanel.add(new JLabel(" Opacity "), c);

    // Add 'Font' label
    c.gridx = 0;
    c.gridy = 1;
    optionsPanel.add(new JLabel(" Font "), c);

    // Add 'Opacity' slider
    final JSlider opacitySlider = new JSlider(0, 100);
    opacitySlider.setName("Opacity");
    opacitySlider.setPaintLabels(false);
    opacitySlider.setToolTipText("Opacity");
    opacitySlider.setValue(100);
    c.gridx = 1;
    c.gridy = 0;
    optionsPanel.add(opacitySlider, c);

    // Add 'Font' combobox
    final JComboBox labelFontComboBox = new JComboBox(new String[]{"Default-BOLD-9", "Default-BOLD-12", "Default-BOLD-18"});
    labelFontComboBox.setSelectedIndex(1);
    c.gridx = 1;
    c.gridy = 1;
    optionsPanel.add(labelFontComboBox, c);

    // Font and opacity change actions
    opacitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        float opacity = (float) opacitySlider.getValue() / 100f;
        Font font = Font.decode((String) labelFontComboBox.getSelectedItem());
        modifyStyle(opacity, font);
      }
    });

    labelFontComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        float opacity = (float) opacitySlider.getValue() / 100f;
        Font font = Font.decode((String) labelFontComboBox.getSelectedItem());
        modifyStyle(opacity, font);
      }
    });

    return optionsPanel;
  }

  private void modifyStyle(float aOpacity, Font aFont) {
    // Iterate over all styled layers
    Enumeration layers = getView().layers();
    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();
      if (layer instanceof ILspStyledLayer) {
        ILspStyledLayer styledLayer = (ILspStyledLayer) layer;

        // Iterate over all paint representations states
        Collection<TLspPaintRepresentation> paintRepresentations = styledLayer.getPaintRepresentations();
        for (TLspPaintRepresentation paintRepresentation : paintRepresentations) {
          for (TLspPaintState paintState : TLspPaintState.values()) {
            TLspPaintRepresentationState prs = TLspPaintRepresentationState.getInstance(paintRepresentation, paintState);

            // Adjust all customizable stylers
            ILspStyler styler = styledLayer.getStyler(prs);
            if (styler instanceof ILspCustomizableStyler) {
              ILspCustomizableStyler customizableStyler = (ILspCustomizableStyler) styler;
              modifyStyle(customizableStyler, aOpacity, aFont);
            }
          }
        }
      }
    }
  }

  private void modifyStyle(ILspCustomizableStyler aCustomizableStyler, float aOpacity, Font aFont) {
    for (TLspCustomizableStyle customizableStyle : aCustomizableStyler.getStyles()) {
      ALspStyle style = customizableStyle.getStyle();

      if (style instanceof TLspTextStyle) {
        TLspTextStyle oldTextStyle = (TLspTextStyle) style;
        TLspTextStyle newTextStyle = oldTextStyle.asBuilder().font(aFont).build();
        customizableStyle.setStyle(newTextStyle);
      }

      if (style instanceof TLspLabelOpacityStyle) {
        TLspLabelOpacityStyle oldModulationColorStyle = (TLspLabelOpacityStyle) style;
        TLspLabelOpacityStyle newModulationColorStyle = oldModulationColorStyle.asBuilder().opacity(aOpacity).build();

        customizableStyle.setStyle(newModulationColorStyle);
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Label placement");
  }

}
