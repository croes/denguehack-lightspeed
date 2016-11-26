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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.LabeledColorChooser;
import samples.wms.server.config.editor.util.LabeledComboBox;
import samples.wms.server.config.editor.util.LabeledTextField;

/**
 * An editor panel for point paint styles. You can choose to use a TLcdSymbol
 * or a TLcdImageIcon, and change the attributes for either case.
 */
class WMSLayerPointStyleEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;
  private final String fSymbolNames[] = {
      "circle",
      "filled_circle",
      "rect",
      "filled_rect",
      "plus",
      "plus_rect",
      "cross",
      "cross_rect",
      "triangle",
      "filled_triangle",
      "polyline",
      "polygon",
      "area",
      "points",
      "outlined_area"
  };

  private LabeledComboBox fSymbolType;
  private LabeledColorChooser fSymbolColor;
  private LabeledTextField fSymbolSize;
  private LabeledTextField fImageSrc;
  private JRadioButton fSymbolButton;

  private static final String POINT_STYLE_ICON_PROPERTY = "pointstyle.icon";
  private static final String POINT_STYLE_ICON_SOURCE_PROPERTY = "pointstyle.icon_src";

  public WMSLayerPointStyleEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    JPanel point = new JPanel(new GridLayout(6, 1, 2, 2));
    point.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    ChangeIconAttribsListener listener = new ChangeIconAttribsListener();
    ButtonGroup group = new ButtonGroup();

    Object icon = fLayer.getProperty(POINT_STYLE_ICON_PROPERTY);
    boolean issymbol = (icon != null) && (icon instanceof TLcdSymbol);
    TLcdSymbol symbol = issymbol ? (TLcdSymbol) icon : null;
    String image = issymbol ? null : (String) fLayer.getProperty(POINT_STYLE_ICON_SOURCE_PROPERTY);

    // Radio button to select a symbol.
    fSymbolButton = new JRadioButton("Symbol", issymbol);
    fSymbolButton.setToolTipText("Use a predefined symbol to display points");
    fSymbolButton.addActionListener(listener);
    point.add(fSymbolButton);
    group.add(fSymbolButton);
    WMSEditorHelp.registerComponent(fSymbolButton, "layers.selected.pointstyle.issymbol");

    // Symbol type chooser.
    fSymbolType = new LabeledComboBox(
        "Type",
        fSymbolNames,
        issymbol ? fSymbolNames[symbol.getShape()] : "circle"
    );
    fSymbolType.setLabelWidth(50);
    fSymbolType.setToolTipText("A list of available symbol shapes to choose from");
    fSymbolType.addChangeListener(listener);
    point.add(fSymbolType);
    WMSEditorHelp.registerComponent(fSymbolType, "layers.selected.pointstyle.symbolshape");

    // Symbol color chooser.
    fSymbolColor = new LabeledColorChooser("Color", issymbol ? symbol.getFillColor() : Color.black);
    fSymbolColor.setLabelWidth(50);
    fSymbolColor.setToolTipText("Click to change the symbol color");
    fSymbolColor.addChangeListener(listener);
    point.add(fSymbolColor);
    WMSEditorHelp.registerComponent(fSymbolColor, "layers.selected.pointstyle.symbolcolor");

    // Symbol size editor.
    fSymbolSize = new LabeledTextField("Size", issymbol ? "" + symbol.getSize() : "10");
    fSymbolSize.setLabelWidth(50);
    fSymbolSize.setToolTipText("The size of the symbol on screen (in pixels)");
    fSymbolSize.addDocumentListener(listener);
    point.add(fSymbolSize);
    WMSEditorHelp.registerComponent(fSymbolSize, "layers.selected.pointstyle.symbolsize");

    // Radio button to select a bitmap icon.
    JRadioButton imageButton = new JRadioButton("Icon", !issymbol);
    imageButton.setToolTipText("Use a bitmap icon loaded from disk to display points");
    imageButton.addActionListener(new ChangeIconAttribsListener());
    point.add(imageButton);
    group.add(imageButton);
    WMSEditorHelp.registerComponent(imageButton, "layers.selected.pointstyle.isimage");

    /* Icon filename editor. We don't use a file chooser here because icons are
       loaded with TLcdIOUtil and may be located anywhere in the classpath,
       including inside JAR files. */
    fImageSrc = new LabeledTextField("Source", (issymbol && (image != null)) ? "" : image);
    fImageSrc.setLabelWidth(50);
    fImageSrc.setToolTipText("The filename of the bitmap icon to be used");
    fImageSrc.addDocumentListener(listener);
    point.add(fImageSrc);
    WMSEditorHelp.registerComponent(fImageSrc, "layers.selected.pointstyle.imagesrc");

    listener.updateLayerProperties();

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Point style", point));
  }

  /**
   * A listener for all point attributes.
   */
  private class ChangeIconAttribsListener
      implements ActionListener, ChangeListener, DocumentListener {

    private void updateLayerProperties() {
      if (fSymbolButton.isSelected()) {
        // Create a TLcdSymbol.
        TLcdSymbol s = new TLcdSymbol();

        // Convert the shape name to an integer ID.
        int shape = 0;
        for (int i = 0; i < fSymbolNames.length; i++) {
          if (fSymbolNames[i].equals(fSymbolType.getSelectedItem())) {
            shape = i;
            break;
          }
        }

        // Get the symbol color.
        Color c = fSymbolColor.getColor();

        // Get the symbol size.
        int size = 10;
        try {
          size = Integer.valueOf(fSymbolSize.getText()).intValue();
        } catch (NumberFormatException nfe) {
        }

        // Update the TLcdSymbol.
        s.setFillColor(c);
        s.setShape(shape);
        s.setSize(size);

        // Update the layer property.
        fLayer.putProperty(POINT_STYLE_ICON_PROPERTY, s);
      } else {
        // Create a TLcdImageIcon.
        String src = fImageSrc.getText();
        /* We don't actually need to load this icon, because we're only
           interested in the filename. The reason we create the TLcdImageIcon
           object here is because it's used in type checks in other places in
           the code (to differentiate TLcdSymbol from TLcdImageIcon). */
        TLcdImageIcon i = new TLcdImageIcon();
        // Update the layer properties.
        fLayer.putProperty(POINT_STYLE_ICON_PROPERTY, i);
        fLayer.putProperty(POINT_STYLE_ICON_SOURCE_PROPERTY, src);
      }

      fireEditListeners(POINT_STYLE_ICON_PROPERTY);
    }

    public void actionPerformed(ActionEvent e) {
      updateLayerProperties();
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
}
