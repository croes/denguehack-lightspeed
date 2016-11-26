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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.wms.server.config.editor.WMSEditListener;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;

/**
 * The main GUI component for layer editing. Contains a layer attribute editor
 * for changing the layer name and other general attributes and, depending on
 * the chosen settings, optional editors for labels and paint styles.
 */
class WMSRootLayerEditor extends WMSEditorPanel {

  private TLcdWMSLayer fLayer;
  private JPanel fMainContainer = null;

  public WMSRootLayerEditor(TLcdWMSLayer aLayer) {
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
    fMainContainer = new JPanel(new BorderLayout(4, 4));
    fMainContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    WMSEditorPanel attribpanel = new WMSRootLayerAttributeEditor(fLayer);
    attribpanel.addEditListener(listener);
    fMainContainer.add(BorderLayout.NORTH, attribpanel);
    WMSEditorHelp.registerComponent(fMainContainer, "layers.selected");

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
