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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.util.ILcdFilter;
import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.gxy.common.TitledPanel;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.LabeledPopupEditorTextField;
import samples.wms.server.config.editor.util.LabeledTextField;

/**
 * An editor panel for the WMS root layer.
 */
class WMSRootLayerAttributeEditor extends WMSEditorPanel {

  private static final int LAYER_TITLE = 1;

  /**
   * The layer that we're editing.
   */
  private TLcdWMSLayer fLayer;

  /**
   * A container for all subcomponents.
   */
  private JPanel fEditorPanel = null;

  public WMSRootLayerAttributeEditor(TLcdWMSLayer aLayer) {
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
    fEditorPanel = new JPanel(new GridLayout(1, 1, 2, 2));
    fEditorPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    // Root title.
    LabeledPopupEditorTextField popupField;
    popupField = new LabeledPopupEditorTextField("Title", fLayer.getTitle());
    popupField.addValidator(new LayerTitleValidator());
    popupField.setLabelWidth(80);
    popupField.addDocumentListener(new LayerAttribTextChangeListener(popupField, WMSRootLayerAttributeEditor.LAYER_TITLE));
    popupField.setToolTipText("End-user visible layer name");
    fEditorPanel.add(popupField);
    WMSEditorHelp.registerComponent(popupField, "layers.selected.title");

    // Add a title to the panel.
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Root attributes", fEditorPanel));
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
      String text = fTextField.getText();
      if (fTarget == WMSRootLayerAttributeEditor.LAYER_TITLE) {
        fLayer.setTitle(text);
      }
      fireEditListeners(text);
    }

    public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
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
