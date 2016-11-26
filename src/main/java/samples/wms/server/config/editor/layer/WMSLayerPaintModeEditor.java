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
package samples.wms.server.config.editor.layer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;

/**
 * An editor panel for point paint styles. You can choose to use a TLcdSymbol
 * or a TLcdImageIcon, and change the attributes for either case.
 */
class WMSLayerPaintModeEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;

  private JRadioButton fOutlineFilledButton;
  private JRadioButton fFilledButton;
  private JRadioButton fPolygonButton;

  private static final String MODE_PROPERTY = "mode";

  /**
   * A listener for all point attributes.
   */
  private class ChangeModeListener
      implements ActionListener {

    private void updateLayerProperties() {
      if (fOutlineFilledButton.isSelected()) {
        // Update the layer property.
        fLayer.putProperty(MODE_PROPERTY, "outline_filled");
      } else if (fFilledButton.isSelected()) {
        // Update the layer property.
        fLayer.putProperty(MODE_PROPERTY, "filled");
      } else if (fPolygonButton.isSelected()) {
        // Update the layer property.
        fLayer.putProperty(MODE_PROPERTY, "polygon");
      }

      fireEditListeners(MODE_PROPERTY);
    }

    public void actionPerformed(ActionEvent e) {
      updateLayerProperties();
    }
  }

  public WMSLayerPaintModeEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    JPanel mode = new JPanel(new GridLayout(3, 1, 2, 2));
    mode.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    ChangeModeListener listener = new ChangeModeListener();
    ButtonGroup group = new ButtonGroup();

    Object currentMode = fLayer.getProperty(MODE_PROPERTY);

    fOutlineFilledButton = new JRadioButton("Outline filled", "outline_filled".equals(currentMode));
    fOutlineFilledButton.setToolTipText("Set the paint mode to outline filled");
    fOutlineFilledButton.addActionListener(listener);
    mode.add(fOutlineFilledButton);
    group.add(fOutlineFilledButton);
    WMSEditorHelp.registerComponent(fOutlineFilledButton, "layers.selected.mode.outlinefilled");

    fFilledButton = new JRadioButton("Filled", "filled".equals(currentMode));
    fFilledButton.setToolTipText("Set the paint mode to filled");
    fFilledButton.addActionListener(listener);
    mode.add(fFilledButton);
    group.add(fFilledButton);
    WMSEditorHelp.registerComponent(fFilledButton, "layers.selected.mode.filled");

    fPolygonButton = new JRadioButton("Polygon", "polygon".equals(currentMode));
    fPolygonButton.setToolTipText("Set the paint mode to polygon");
    fPolygonButton.addActionListener(listener);
    mode.add(fPolygonButton);
    group.add(fPolygonButton);
    WMSEditorHelp.registerComponent(fPolygonButton, "layers.selected.mode.polygon");

    listener.updateLayerProperties();

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Paint mode", mode));
  }
}
