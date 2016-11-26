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
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.wms.server.config.editor.WMSEditListener;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.SHPFileHeader;

/**
 * An editor panel for layer paint styles. It will consist of a combination of
 * fill style, line style and point style editors, depending on what kind of
 * data the layer contains.
 */
class WMSLayerPaintStyleEditor extends WMSEditorPanel {

  /**
   * The layer that we're editing.
   */
  private TLcdWMSLayer fLayer;

  private JPanel fEditorPanel = null;

  private void rebuild() {

    if (fEditorPanel != null) {
      remove(fEditorPanel);
    }

    fEditorPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

    // Get the shape file header, which was loaded earlier.
    SHPFileHeader shapeHeader = (SHPFileHeader) fLayer.getProperty("shp_header");
    if (shapeHeader != null) {
      WMSEditorPanel ed;
      EditListener listener = new EditListener();

      int numeditors = 0;
      int shapeType = shapeHeader.getShapeType();

      // For polygons, add a paint mode editor.
      if ((shapeType == SHPFileHeader.SHP_POLYGON) ||
          (shapeType == SHPFileHeader.SHP_POLYGONZ) ||
          (shapeType == SHPFileHeader.SHP_POLYGONM)) {
        ed = new WMSLayerPaintModeEditor(fLayer);
        ed.addEditListener(listener);
        fEditorPanel.add(addWestSeparator(gbc, ed), gbc);
        gbc.gridx++;
        numeditors++;
      }

      // For polygons, add a fill style editor.
      if ((shapeType == SHPFileHeader.SHP_POLYGON) ||
          (shapeType == SHPFileHeader.SHP_POLYGONZ) ||
          (shapeType == SHPFileHeader.SHP_POLYGONM)) {
        ed = new WMSLayerFillStyleEditor(fLayer);
        ed.addEditListener(listener);
        fEditorPanel.add(addWestSeparator(gbc, ed), gbc);
        gbc.gridx++;
        numeditors++;
      }

      // For polygons and polylines, add a line style editor.
      if ((shapeType == SHPFileHeader.SHP_POLYLINE) ||
          (shapeType == SHPFileHeader.SHP_POLYLINEZ) ||
          (shapeType == SHPFileHeader.SHP_POLYLINEM) ||
          (shapeType == SHPFileHeader.SHP_POLYGON) ||
          (shapeType == SHPFileHeader.SHP_POLYGONZ) ||
          (shapeType == SHPFileHeader.SHP_POLYGONM)) {
        ed = new WMSLayerLineStyleEditor(fLayer);
        ed.addEditListener(listener);
        fEditorPanel.add(addWestSeparator(gbc, ed), gbc);
        gbc.gridx++;
        numeditors++;
      }

      // For points (i.e. everything else), add a point style editor.
      if (numeditors == 0) {
        ed = new WMSLayerPointStyleEditor(fLayer);
        ed.addEditListener(listener);
        fEditorPanel.add(ed, gbc);
        gbc.gridx++;
        numeditors++;
      }
    }

    add(BorderLayout.CENTER, fEditorPanel);
    revalidate();
  }

  private static JPanel addWestSeparator(GridBagConstraints aGridBagConstraints, WMSEditorPanel aEditorPanel) {
    if (aGridBagConstraints.gridx == 0) {
      return aEditorPanel;
    }
    JPanel separatorPanel = new JPanel(new BorderLayout());
    separatorPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    separatorPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.CENTER);

    JPanel editorPanel = new JPanel(new BorderLayout());
    editorPanel.add(BorderLayout.WEST, separatorPanel);
    editorPanel.add(BorderLayout.CENTER, aEditorPanel);
    return editorPanel;
  }

  public WMSLayerPaintStyleEditor(TLcdWMSLayer aLayer) {
    super(new BorderLayout(2, 2));
    fLayer = aLayer;

    rebuild();
  }

  /**
   * An edit listener for the subcomponents.
   */
  private class EditListener implements WMSEditListener {
    public void editPerformed(Object aEditedObject) {
      // If any of the subcomponents fires this listener, fire my ancestors' listener as well.
      fireEditListeners(aEditedObject);
    }
  }
}
