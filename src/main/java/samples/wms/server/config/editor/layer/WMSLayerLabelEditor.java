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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.GUIIconProvider;
import samples.wms.server.config.editor.util.LabeledColorChooser;
import samples.wms.server.config.editor.util.LabeledComboBox;
import samples.wms.server.config.editor.util.LabeledFontChooser;
import samples.wms.server.config.editor.util.LabeledTextField;

/**
 * An editor panel for WMS layer labels (foreground and background color,
 * filled, framed, pin, font, font size, font style, features).
 */
class WMSLayerLabelEditor extends WMSEditorPanel {

  private static final int FEATURE_ADD = 1;
  private static final int FEATURE_REMOVE = 2;
  private static final int FEATURE_MOVEUP = 3;
  private static final int FEATURE_MOVEDOWN = 4;

  private static final String LABEL_FONT_PROPERTY = "label.font";
  private static final String LABEL_FEATURE_NAMES = "label.feature_names";
  private static final String LABEL_FOREGROUND = "label.foreground";
  private static final String LABEL_BACKGROUND = "label.background";
  private static final String LABEL_FILLED = "label.filled";
  private static final String LABEL_FRAMED = "label.framed";
  private static final String LABEL_WITH_PIN = "label.withPin";

  /**
   * The layer that we're editing.
   */
  private TLcdWMSLayer fLayer;
  private java.util.List fFeatureList;

  /**
   * A listener for the label colors.
   */
  private class ChangeColorListener implements ChangeListener {

    private String fTarget;
    private LabeledColorChooser fChooser;

    public ChangeColorListener(LabeledColorChooser aChooser, String aTarget) {
      fChooser = aChooser;
      fTarget = aTarget;
    }

    public void stateChanged(ChangeEvent e) {
      fLayer.putProperty(fTarget, fChooser.getColor());
      fireEditListeners(fTarget);
    }
  }

  /**
   * A listener for boolean label properties.
   */
  private class ChangeBooleanListener implements ActionListener {

    private JCheckBox fCheckBox;
    private String fTarget;

    public ChangeBooleanListener(JCheckBox aCheckBox, String aTarget) {
      fCheckBox = aCheckBox;
      fTarget = aTarget;
    }

    public void actionPerformed(ActionEvent e) {
      fLayer.putProperty(fTarget, Boolean.valueOf(fCheckBox.isSelected()));
      fireEditListeners(fTarget);
    }
  }

  /**
   * A listener for the feature list.
   */
  private class ChangeFeatureListListener implements ActionListener {

    private JList fList;
    private int fAction;

    public ChangeFeatureListListener(JList aList, int aAction) {
      fList = aList;
      fAction = aAction;
    }

    public void actionPerformed(ActionEvent e) {

      int i = fList.getSelectedIndex();
      // Unless we're adding a new feature, bail out if no feature is selected.
      if ((fAction != FEATURE_ADD) && (i < 0)) {
        return;
      }

      switch (fAction) {
      case FEATURE_ADD:
        // Append a new feature to the list.
        String newfeature = JOptionPane.showInputDialog("Enter a feature name");
        if (newfeature != null) {
          fFeatureList.add(newfeature);
          i = fFeatureList.size() - 1;
        }
        break;
      case FEATURE_REMOVE:
        // Remove the selected feature from the list.
        fFeatureList.remove(i);
        i = -1;
        break;
      case FEATURE_MOVEUP:
        // Move the selected feature up by one position.
        if (i >= 1) {
          Object tmp = fFeatureList.get(i - 1);
          fFeatureList.set(i - 1, fFeatureList.get(i));
          fFeatureList.set(i, tmp);
          i--;
        }
        break;
      case FEATURE_MOVEDOWN:
        // Move the selected feature down by one position.
        if (i < fFeatureList.size() - 1) {
          Object tmp = fFeatureList.get(i + 1);
          fFeatureList.set(i + 1, fFeatureList.get(i));
          fFeatureList.set(i, tmp);
          i++;
        }
        break;
      }

      // Rebuild the list box.
      fList.setListData(fFeatureList.toArray());
      fList.setSelectedIndex(i);

      // Re-set the layer property.
      Object featureArray = fFeatureList.toArray();
      fLayer.putProperty(LABEL_FEATURE_NAMES, featureArray);
      fireEditListeners(featureArray);
    }
  }

  /**
   * A listener for the font name.
   */
  private class ChangeFontListener implements ChangeListener {

    private LabeledComboBox fChooser;

    public ChangeFontListener(LabeledComboBox aChooser) {
      fChooser = aChooser;
    }

    public void stateChanged(ChangeEvent e) {
      /* Create a new Font object with the size and style of the current font,
         but with the new font name. */
      String name = (String) fChooser.getSelectedItem();
      Font curfont = (Font) fLayer.getProperty(LABEL_FONT_PROPERTY);
      Font newfont = new Font(
          name,
          curfont != null ? curfont.getStyle() : Font.PLAIN,
          curfont != null ? curfont.getSize() : 12
      );
      // Update the layer property.
      fLayer.putProperty(LABEL_FONT_PROPERTY, newfont);
      fireEditListeners(LABEL_FONT_PROPERTY);
    }
  }

  /**
   * A listener for the font size.
   */
  private class ChangeFontSizeListener implements DocumentListener {

    private LabeledTextField fTextField;

    public ChangeFontSizeListener(LabeledTextField aTextField) {
      fTextField = aTextField;
    }

    public void changedUpdate(DocumentEvent e) {
      Font currentFont = (Font) fLayer.getProperty(LABEL_FONT_PROPERTY);

      /* Create a new Font object with the name and style of the current font,
         but with the new font size. */
      String name = currentFont != null ? currentFont.getName() : "Default";
      int style = currentFont != null ? currentFont.getStyle() : Font.PLAIN;

      try {
        int size = Integer.valueOf(fTextField.getText()).intValue();

        Font newfont = new Font(name, style, size);
        // Update the layer property.
        fLayer.putProperty(LABEL_FONT_PROPERTY, newfont);
        fireEditListeners(LABEL_FONT_PROPERTY);
      } catch (NumberFormatException nfe) {
        // Ignore if not a valid number.
      }
    }

    public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
    }
  }

  /**
   * A listener for the font style.
   */
  private class ChangeFontStyleListener implements ChangeListener {

    private LabeledComboBox fChooser;

    public ChangeFontStyleListener(LabeledComboBox aChooser) {
      fChooser = aChooser;
    }

    public void stateChanged(ChangeEvent e) {
      String stylename = (String) fChooser.getSelectedItem();

      // Turn the combo box selection into a Font style flag.
      int style = 0;
      if (stylename.equals("bold")) {
        style = Font.BOLD;
      } else if (stylename.equals("italic")) {
        style = Font.ITALIC;
      } else if (stylename.equals("bold_italic")) {
        style = Font.BOLD | Font.ITALIC;
      }

      /* Create a new Font object with the name and size of the current font,
         but with the new font style. */
      Font curfont = (Font) fLayer.getProperty(LABEL_FONT_PROPERTY);
      Font newfont = new Font(
          curfont != null ? curfont.getName() : "Default",
          style,
          curfont != null ? curfont.getSize() : 12
      );
      // Update the layer property.
      fLayer.putProperty(LABEL_FONT_PROPERTY, newfont);
      fireEditListeners(LABEL_FONT_PROPERTY);
    }
  }

  public WMSLayerLabelEditor(TLcdWMSLayer aLayer) {

    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    JPanel editorpanel = new JPanel(new GridBagLayout());
    editorpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    // The left column will hold the visual label properties (color, font, ...)
    JPanel left = new JPanel(new GridLayout(5, 1, 2, 2));
    // The right column will hold the label feature list.
    JPanel right = new JPanel(new BorderLayout(2, 2));

    GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0);
    editorpanel.add(left, gbc);
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    editorpanel.add(right, gbc);

    // Build the left column.
    JCheckBox check;
    LabeledTextField field;

    // Foreground and background color.
    JPanel colorpane = new JPanel(new GridLayout(1, 2, 4, 4));

    LabeledColorChooser chooser;
    LabeledComboBox combo;

    chooser = new LabeledColorChooser("Foreground", (Color) fLayer.getProperty(LABEL_FOREGROUND));
    chooser.setLabelWidth(80);
    chooser.addChangeListener(new ChangeColorListener(chooser, LABEL_FOREGROUND));
    chooser.setToolTipText("Click to set the label text color");
    colorpane.add(chooser);
    WMSEditorHelp.registerComponent(chooser, "layers.selected.label.foreground");

    chooser = new LabeledColorChooser("Background", (Color) fLayer.getProperty(LABEL_BACKGROUND));
    chooser.setLabelWidth(80);
    chooser.addChangeListener(new ChangeColorListener(chooser, LABEL_BACKGROUND));
    chooser.setToolTipText("Click to set the label background color");
    colorpane.add(chooser);
    WMSEditorHelp.registerComponent(chooser, "layers.selected.label.background");

    left.add(colorpane);

    // Boolean properties.
    JPanel checks = new JPanel(new GridLayout(1, 3, 2, 2));
    Boolean b;

    b = (Boolean) fLayer.getProperty("label.filled");
    check = new JCheckBox("Filled", (b != null) && b.booleanValue());
    check.addActionListener(new ChangeBooleanListener(check, LABEL_FILLED));
    check.setToolTipText("Display the labels on a filled background");
    checks.add(check);
    WMSEditorHelp.registerComponent(check, "layers.selected.label.filled");

    b = (Boolean) fLayer.getProperty("label.framed");
    check = new JCheckBox("Framed", (b != null) && b.booleanValue());
    check.addActionListener(new ChangeBooleanListener(check, LABEL_FRAMED));
    check.setToolTipText("Draw a frame around the labels");
    checks.add(check);
    WMSEditorHelp.registerComponent(check, "layers.selected.label.framed");

    b = (Boolean) fLayer.getProperty("label.withPin");
    check = new JCheckBox("With pin", (b != null) && b.booleanValue());
    check.addActionListener(new ChangeBooleanListener(check, LABEL_WITH_PIN));
    check.setToolTipText("Draw a pin from the shape to the label");
    checks.add(check);
    WMSEditorHelp.registerComponent(check, "layers.selected.label.withpin");

    left.add(checks);

    // Font properties.
    Font f = (Font) fLayer.getProperty(LABEL_FONT_PROPERTY);
    final String styles[] = {
        "plain",
        "bold",
        "italic",
        "bold_italic"
    };

    combo = new LabeledFontChooser("Font", (f != null ? f.getName() : "Default"));
    combo.setLabelWidth(80);
    combo.addChangeListener(new ChangeFontListener(combo));
    combo.setToolTipText("The font used for the label text");
    left.add(combo);
    WMSEditorHelp.registerComponent(combo, "layers.selected.label.fontname");

    field = new LabeledTextField("Font size", (f != null ? "" + f.getSize() : "10"));
    field.setLabelWidth(80);
    field.addDocumentListener(new ChangeFontSizeListener(field));
    field.setToolTipText("The font size used for the label text");
    left.add(field);
    WMSEditorHelp.registerComponent(field, "layers.selected.label.fontsize");

    combo = new LabeledComboBox("Font style", styles, (f != null ? styles[f.getStyle()] : styles[0]));
    combo.setLabelWidth(80);
    combo.addChangeListener(new ChangeFontStyleListener(combo));
    combo.setToolTipText("The font style used for the label text");
    left.add(combo);
    WMSEditorHelp.registerComponent(combo, "layers.selected.label.fontstyle");

    // The right half contains a list of features.
    JLabel label = new JLabel("Features:");
    right.add(BorderLayout.NORTH, label);

    Object features[] = (Object[]) fLayer.getProperty(LABEL_FEATURE_NAMES);
    fFeatureList = new ArrayList();
    if (features != null) {
      for (int i = 0; i < features.length; i++) {
        fFeatureList.add(features[i]);
      }
    }
    JList list = new JList(fFeatureList.toArray());
    list.setToolTipText("The list of features used to create the label text");
    JScrollPane scroll = new JScrollPane(list);
    scroll.setMinimumSize(new Dimension(50, 50));
    scroll.setPreferredSize(new Dimension(50, 50));
    right.add(BorderLayout.CENTER, scroll);
    WMSEditorHelp.registerComponent(list, "layers.selected.label.features.names");

    JPanel buttons = new JPanel(new GridLayout(1, 4, 2, 2));
    JButton btn;

    btn = new JButton(GUIIconProvider.getIcon("images/icons/add_item_16.png"));
    btn.addActionListener(new ChangeFeatureListListener(list, FEATURE_ADD));
    btn.setToolTipText("Add a new feature to the list");
    buttons.add(btn);
    WMSEditorHelp.registerComponent(btn, "layers.selected.label.features.add");

    btn = new JButton(GUIIconProvider.getIcon("images/icons/remove_item_16.png"));
    btn.addActionListener(new ChangeFeatureListListener(list, FEATURE_REMOVE));
    btn.setToolTipText("Remove the selected feature from the list");
    buttons.add(btn);
    WMSEditorHelp.registerComponent(btn, "layers.selected.label.features.remove");

    btn = new JButton(GUIIconProvider.getIcon("images/icons/move_up_16.png"));
    btn.addActionListener(new ChangeFeatureListListener(list, FEATURE_MOVEUP));
    btn.setToolTipText("Move the selected feature up in the list");
    buttons.add(btn);
    WMSEditorHelp.registerComponent(btn, "layers.selected.label.features.moveup");

    btn = new JButton(GUIIconProvider.getIcon("images/icons/move_down_16.png"));
    btn.addActionListener(new ChangeFeatureListListener(list, FEATURE_MOVEDOWN));
    btn.setToolTipText("Move the selected feature down in the list");
    buttons.add(btn);
    WMSEditorHelp.registerComponent(btn, "layers.selected.label.features.movedown");

    right.add(BorderLayout.SOUTH, buttons);
    WMSEditorHelp.registerComponent(right, "layers.selected.label.features");

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Labels", editorpanel));
  }

}
