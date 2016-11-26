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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdStringUtil;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.LabeledDoubleTextField;
import samples.wms.server.config.editor.util.LabeledFilenameField;
import samples.wms.server.config.editor.util.LabeledPopupEditorTextField;
import samples.wms.server.config.editor.util.LabeledTextField;
import samples.wms.server.config.editor.util.SHPFileHeader;

/**
 * An editor panel for WMS layer attributes (name, title, abstract, source file
 * name).
 */
class WMSLayerAttributeEditor extends WMSEditorPanel {

  private static final int LAYER_NAME = 1;
  private static final int LAYER_TITLE = 2;
  private static final int LAYER_ABSTRACT = 3;
  private static final int LAYER_SOURCE = 4;

  private static final int LAYER_LABELED = 5;
  private static final int LAYER_HASPAINTSTYLE = 6;
  private static final int LAYER_NAMEVISIBLE = 7;

  private static final int LAYER_MIN_SCALE_DENOMINATOR = 8;
  private static final int LAYER_MAX_SCALE_DENOMINATOR = 9;

  /**
   * The layer that we're editing.
   */
  private TLcdWMSLayer fLayer;

  /**
   * A container for all subcomponents.
   */
  private JPanel fEditorPanel = null;

  public WMSLayerAttributeEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    rebuild();
  }

  /**
   * Rebuild the editor GUI from scratch.
   */
  private void rebuild() {

    if (fEditorPanel != null) {
      remove(fEditorPanel);
    }
    fEditorPanel = new JPanel(new GridLayout(7, 1, 2, 2));
    fEditorPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    /* Only shape files can have a paint style, so check whether
       (a) this layer has a source file, and
       (b) the source file contains shapes
     */
    boolean hasSource = (fLayer.getSourceName() != null) && (fLayer.getSourceName().length() != 0);
    boolean isSHP = false, isORA = false;
    if (hasSource) {
      isSHP =
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".shp") ||
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".shp.gz") ||
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".shp.zip");
      isORA =
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".ora") ||
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".ora.gz") ||
          TLcdStringUtil.endsWithIgnoreCase(fLayer.getSourceName(), ".ora.zip");
    }
    boolean canHaveStyles = isSHP || isORA;
    if (canHaveStyles) {
      /* Read the shape file's header and store it for later use. It contains
         information about the shape type, which we will use to build a paint
         style editor. */
      SHPFileHeader hdr = new SHPFileHeader(fLayer.getSourceName());
      fLayer.putProperty("shp_header", hdr);
    }

    LabeledTextField field;
    LabeledPopupEditorTextField popupField;

    // Layer name.
    popupField = new LabeledPopupEditorTextField("Name", fLayer.getName());
    popupField.addValidator(new LayerNameValidator());
    popupField.setLabelWidth(80);
    popupField.addDocumentListener(new LayerAttribTextChangeListener(popupField, LAYER_NAME));
    popupField.setToolTipText("Internal layer name (must be unique)");
    fEditorPanel.add(popupField);
    WMSEditorHelp.registerComponent(popupField, "layers.selected.name");

    // Layer title.
    popupField = new LabeledPopupEditorTextField("Title", fLayer.getTitle());
    popupField.addValidator(new LayerTitleValidator());
    popupField.setLabelWidth(80);
    popupField.addDocumentListener(new LayerAttribTextChangeListener(popupField, LAYER_TITLE));
    popupField.setToolTipText("End-user visible layer name");
    fEditorPanel.add(popupField);
    WMSEditorHelp.registerComponent(popupField, "layers.selected.title");

    // Layer abstract.
    field = new LabeledTextField("Abstract", fLayer.getAbstract());
    field.setLabelWidth(80);
    field.addDocumentListener(new LayerAttribTextChangeListener(field, LAYER_ABSTRACT));
    field.setToolTipText("Layer description");
    fEditorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "layers.selected.abstract");

    // Layer source.
    field = new LabeledFilenameField("Source", fLayer.getSourceName());
    field.setLabelWidth(80);
    field.addDocumentListener(new LayerAttribTextChangeListener(field, LAYER_SOURCE));
    field.setToolTipText("Layer data source");
    ((LabeledFilenameField) field).setInitialDir(fLayer.getSourceName() != null ?
                                                 fLayer.getSourceName() : samples.wms.server.config.editor.MainPanel.get().getCapabilities().getMapDataFolder()
    );
    fEditorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "layers.selected.source");

    // Layer min scale denominator.
    double minScaleDenominator = fLayer.getMinScaleDenominator();
    field = new LabeledDoubleTextField("Min scale denominator", (minScaleDenominator == 0.0 ? "" : "" + minScaleDenominator));
    field.setLabelWidth(150);
    field.addDocumentListener(new LayerAttribTextChangeListener(field, LAYER_MIN_SCALE_DENOMINATOR));
    field.setToolTipText("Minimum scale denominator");
    fEditorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "layers.selected.minscaledenominator");

    // Layer max scale denominator.
    double maxScaleDenominator = fLayer.getMaxScaleDenominator();
    field = new LabeledDoubleTextField("Max scale denominator", (Double.isInfinite(maxScaleDenominator) ? "" : "" + maxScaleDenominator));
    field.setLabelWidth(150);
    field.addDocumentListener(new LayerAttribTextChangeListener(field, LAYER_MAX_SCALE_DENOMINATOR));
    field.setToolTipText("Maximum scale denominator");
    fEditorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "layers.selected.maxscaledenominator");

    JPanel checks = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0);
    JCheckBox check;

    // Name visible?
    check = new JCheckBox("Name visible");
    check.addActionListener(new LayerAttribBooleanChangeListener(check, LAYER_NAMEVISIBLE));
    check.setSelected(fLayer.isNameVisible());
    check.setToolTipText("Allow the name of this layer to be used in queries");
    checks.add(check, gbc);
    WMSEditorHelp.registerComponent(check, "layers.selected.namevisible");

    if (hasSource && canHaveStyles) {
      // Labeled?
      check = new JCheckBox("Show labels");
      check.addActionListener(new LayerAttribBooleanChangeListener(check, LAYER_LABELED));
      Boolean islabeled = (Boolean) fLayer.getProperty("labeled");
      check.setSelected((islabeled != null) && (islabeled.booleanValue()));
      check.setToolTipText("Display labels for this layer");
      gbc.gridx++;
      checks.add(check, gbc);
      WMSEditorHelp.registerComponent(check, "layers.selected.labeled");

      check = new JCheckBox("Define paint style");
      check.addActionListener(new LayerAttribBooleanChangeListener(check, LAYER_HASPAINTSTYLE));
      Boolean haspaintstyle = (Boolean) fLayer.getProperty("haspaintstyle");
      check.setSelected((haspaintstyle != null) && (haspaintstyle.booleanValue()));
      check.setToolTipText("Define a custom paint style for this layer");
      gbc.gridx++;
      checks.add(check, gbc);
      WMSEditorHelp.registerComponent(check, "layers.selected.haspaintstyle");
    }
    gbc.gridx++;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    checks.add(Box.createHorizontalGlue(), gbc);

    fEditorPanel.add(checks);

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Layer attributes", fEditorPanel));
    revalidate();
  }

  /**
   * A listener for the attribute editing controls.
   */
  private class LayerAttribTextChangeListener implements DocumentListener {
    private LabeledTextField fTextField;
    private int fTarget;

    public LayerAttribTextChangeListener(LabeledTextField aTextField, int aTarget) {
      fTextField = aTextField;
      fTarget = aTarget;
    }

    public void changedUpdate(DocumentEvent e) {
      String s = fTextField.getText();
      switch (fTarget) {
      case LAYER_NAME:
        fLayer.setName(s);
        break;
      case LAYER_TITLE:
        fLayer.setTitle(s);
        break;
      case LAYER_ABSTRACT:
        fLayer.setAbstract(s);
        break;
      case LAYER_SOURCE:
        fLayer.setSourceName(s);
        rebuild();
        break;
      case LAYER_MIN_SCALE_DENOMINATOR:
        fLayer.setMinScaleDenominator(s == null || s.trim().length() == 0 ? 0.0 : Double.parseDouble(s));
        break;
      case LAYER_MAX_SCALE_DENOMINATOR:
        fLayer.setMaxScaleDenominator(s == null || s.trim().length() == 0 ? Double.POSITIVE_INFINITY : Double.parseDouble(s));
        break;
      }
      fireEditListeners(s);
    }

    public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
    }
  }

  /**
   * A listener for the checkbox controls.
   */
  private class LayerAttribBooleanChangeListener implements ActionListener {

    private JCheckBox fCheckBox;
    private int fTarget;

    public LayerAttribBooleanChangeListener(JCheckBox aCheckBox, int aTarget) {
      fCheckBox = aCheckBox;
      fTarget = aTarget;
    }

    public void actionPerformed(ActionEvent e) {
      switch (fTarget) {
      case LAYER_LABELED:
        fLayer.putProperty("labeled", Boolean.valueOf(fCheckBox.isSelected()));
        break;
      case LAYER_HASPAINTSTYLE:
        fLayer.putProperty("haspaintstyle", Boolean.valueOf(fCheckBox.isSelected()));
        break;
      case LAYER_NAMEVISIBLE:
        fLayer.setNameVisible(fCheckBox.isSelected());
        break;
      }
      fireEditListeners(fLayer);
    }
  }

  /**
   * A validator for layer names.
   */
  private class LayerNameValidator implements ILcdFilter {

    private boolean isLayerNameDuplicate(String aLayerName, ALcdWMSLayer aRootLayer) {

      if (!fLayer.equals(aRootLayer) && aLayerName.equals(aRootLayer.getName())) {
        return true;
      }

      for (int i = 0; i < aRootLayer.getChildWMSLayerCount(); i++) {
        ALcdWMSLayer c = aRootLayer.getChildWMSLayer(i);
        if (isLayerNameDuplicate(aLayerName, c)) {
          return true;
        }
      }

      return false;
    }

    private boolean isLayerNameDuplicate(String aLayerName) {
      ALcdWMSLayer c = samples.wms.server.config.editor.MainPanel.get().getCapabilities().getRootWMSLayer(0);
      if (isLayerNameDuplicate(aLayerName, c)) {
        return true;
      }

      return false;
    }

    public boolean accept(Object o) {

      if ((o != null) && (o instanceof String) && ((String) o).trim().length() > 0) {
        return !isLayerNameDuplicate((String) o);
      }

      return false;
    }
  }

  /**
   * A validator for layer titles.
   */
  private static class LayerTitleValidator implements ILcdFilter {
    public boolean accept(Object o) {
      return ((o != null) && (o instanceof String) && (((String) o).trim().length() > 0));
    }
  }
}
