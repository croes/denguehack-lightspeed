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
 * An editor panel for layer fill styles (color, opacity).
 */
class WMSLayerFillStyleEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;
  private LabeledColorChooser fFillColor;
  private LabeledTextField fFillAlpha;

  private class ChangeFillAttribsListener implements ChangeListener, DocumentListener {

    private void updateLayerProperties() {
      // Get the RGB fill color.
      Color c = fFillColor.getColor();
      // Get the opacity.
      float a = 1.0f;
      try {
        a = Float.valueOf(fFillAlpha.getText()).floatValue();
      } catch (NumberFormatException nfe) {
        // If an invalid number is entered, use 1.0 instead.
      }

      // Combine the color and opacity into an RGBA quadruplet and set the layer property.
      fLayer.putProperty("fillstyle.color", new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (a * 255)));
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

  public WMSLayerFillStyleEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    JPanel fill = new JPanel(new GridLayout(3, 1, 2, 2));
    fill.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    ChangeFillAttribsListener listener = new ChangeFillAttribsListener();
    Color fillcolor = (Color) fLayer.getProperty("fillstyle.color");

    // Add the fill color editor.
    fFillColor = new LabeledColorChooser("Color", fillcolor != null ? fillcolor : Color.black);
    fFillColor.setLabelWidth(50);
    fFillColor.addChangeListener(listener);
    fFillColor.setToolTipText("Click to change the fill color");
    fill.add(fFillColor);
    WMSEditorHelp.registerComponent(fFillColor, "layers.selected.fillstyle.color");

    // Add the fill opacity editor.
    fFillAlpha = new LabeledTextField("Opacity", fillcolor != null ? "" + (fillcolor.getAlpha() / 255.0f) : "1.0");
    fFillAlpha.setLabelWidth(50);
    fFillAlpha.addDocumentListener(listener);
    fFillAlpha.setToolTipText("The fill opacity (in the [0,1] range)");
    fill.add(fFillAlpha);
    WMSEditorHelp.registerComponent(fFillAlpha, "layers.selected.fillstyle.opacity");

    listener.updateLayerProperties();

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Fill style", fill));
  }
}
