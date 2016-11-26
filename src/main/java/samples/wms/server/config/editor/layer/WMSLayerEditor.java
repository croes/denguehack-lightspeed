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
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.wms.server.config.editor.WMSEditListener;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;

/**
 * The main GUI component for layer editing. Contains a layer attribute editor
 * for changing the layer name and other general attributes and, depending on
 * the chosen settings, optional editors for labels and paint styles.
 */
class WMSLayerEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;
  private JScrollPane fMainContainer = null;

  public WMSLayerEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout());
    fLayer = aLayer;
    rebuild();
  }

  private void rebuild() {

    if (fMainContainer != null) {
      remove(fMainContainer);
    }

    LayerEditListener listener = new LayerEditListener();

    // Add the attribute editor.
    JPanel mainContainerPanel = new JPanel(new BorderLayout(4, 4));
    mainContainerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    WMSEditorPanel attribpanel = new WMSLayerAttributeEditor(fLayer);
    attribpanel.addEditListener(listener);
    mainContainerPanel.add(BorderLayout.NORTH, attribpanel);
    WMSEditorHelp.registerComponent(mainContainerPanel, "layers.selected");

    if (fLayer.getSourceName() != null) {

      JPanel subcontainer = new JPanel(new GridBagLayout());
      mainContainerPanel.add(BorderLayout.CENTER, subcontainer);
      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);

      // If the layer is labeled, add the label editor.
      Boolean labeled = (Boolean) fLayer.getProperty("labeled");
      if ((labeled != null) && labeled.booleanValue()) {
        WMSEditorPanel labelpanel = new WMSLayerLabelEditor(fLayer);
        labelpanel.addEditListener(listener);
        subcontainer.add(labelpanel, gbc);
        gbc.gridy++;
      }

      // If the layer has a paint style, add the paint style editor.
      Boolean haspaintstyle = (Boolean) fLayer.getProperty("haspaintstyle");
      if ((haspaintstyle != null) && (haspaintstyle.booleanValue())) {
        WMSEditorPanel paintstylepanel = new WMSLayerPaintStyleEditor(fLayer);
        paintstylepanel.addEditListener(listener);
        subcontainer.add(paintstylepanel, gbc);
        gbc.gridy++;
      }
      gbc.weighty = 1;
      subcontainer.add(Box.createVerticalGlue(), gbc);
    }

    fMainContainer = new JScrollPane(mainContainerPanel);
    fMainContainer.setBorder(null);

    add(BorderLayout.CENTER, fMainContainer);
    revalidate();
  }

  /**
   * A listener that rebuilds the GUI whenever the layer source is changed.
   */
  private class LayerEditListener implements WMSEditListener {
    public void editPerformed(Object aEditedObject) {
      // If the layer source is changed, rebuild user interface.
      if (aEditedObject.equals(fLayer.getSourceName()) || !(aEditedObject instanceof String)) {
        rebuild();
      }

      // Also fire the edit listeners associated with this panel.
      fireEditListeners(fLayer);
    }
  }
}
