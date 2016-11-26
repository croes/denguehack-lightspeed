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
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.LabeledColorChooser;
import samples.wms.server.config.editor.util.LabeledTextField;

/**
 * An editor panel for line paint styles (color, width, opacity).
 */
class WMSLayerLineStyleEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;
  private LabeledColorChooser fLineColor;
  private LabeledTextField fLineWidth;
  private LabeledTextField fLineAlpha;

  /**
   * A listener for all the line attributes.
   */
  private class ChangeLineAttribsListener implements ChangeListener, DocumentListener {

    /**
     * Rebuild the line style from the current settings.
     */
    private void updateLayerProperties() {
      // Get the color.
      Color c = fLineColor.getColor();
      // Get the line width.
      int w = 1;
      try {
        w = Integer.valueOf(fLineWidth.getText()).intValue();
      } catch (NumberFormatException nfe) {
      }
      // Get the line opacity.
      float a = 1.0f;
      try {
        a = Float.valueOf(fLineAlpha.getText()).floatValue();
      } catch (NumberFormatException nfe) {
      }

      // Update the layer properties.
      fLayer.putProperty("linestyle.color", new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (a * 255)));
      fLayer.putProperty("linestyle.width", Integer.valueOf(w));
      fireEditListeners("linestyle");
    }

    public void stateChanged(ChangeEvent e) {
      updateLayerProperties();
    }

    public void insertUpdate(DocumentEvent e) {
      updateLayerProperties();
    }

    public void removeUpdate(DocumentEvent e) {
      updateLayerProperties();
    }

    public void changedUpdate(DocumentEvent e) {
      updateLayerProperties();
    }
  }

  public WMSLayerLineStyleEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    JPanel line = new JPanel(new GridLayout(3, 1, 2, 2));
    line.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    ChangeLineAttribsListener listener = new ChangeLineAttribsListener();
    Color linecolor = (Color) fLayer.getProperty("linestyle.color");
    Integer linewidth = (Integer) fLayer.getProperty("linestyle.width");

    // Add the line color editor.
    fLineColor = new LabeledColorChooser("Color", linecolor != null ? linecolor : Color.black);
    fLineColor.setLabelWidth(50);
    fLineColor.addChangeListener(listener);
    fLineColor.setToolTipText("Click to change the line color");
    WMSEditorHelp.registerComponent(fLineColor, "layers.selected.linestyle.color");
    line.add(fLineColor);

    // Add the line width editor.
    fLineWidth = new LabeledTextField("Width", linewidth != null ? linewidth.toString() : "1");
    fLineWidth.setLabelWidth(50);
    fLineWidth.addDocumentListener(listener);
    fLineWidth.setToolTipText("The line width (in pixels)");
    WMSEditorHelp.registerComponent(fLineWidth, "layers.selected.linestyle.width");
    line.add(fLineWidth);

    // Add the line opacity editor.
    fLineAlpha = new LabeledTextField("Opacity", linecolor != null ? "" + (linecolor.getAlpha() / 255.0f) : "1.0");
    fLineAlpha.setLabelWidth(50);
    fLineAlpha.addDocumentListener(listener);
    fLineAlpha.setToolTipText("The line opacity (in the [0,1] range)");
    WMSEditorHelp.registerComponent(fLineAlpha, "layers.selected.linestyle.opacity");
    line.add(fLineAlpha);

    listener.updateLayerProperties();

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Line style", line));
  }
}
