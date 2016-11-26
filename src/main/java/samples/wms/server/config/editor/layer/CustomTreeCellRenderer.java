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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * Custom tree cell painter that grays out cells that correspond to an
 * invisible WMS layer.
 */
class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

  public Component getTreeCellRendererComponent(JTree aTree, Object aValue, boolean aIsSelected, boolean aIsExpanded, boolean aIsLeaf, int aRow, boolean aHasFocus) {
    Component cr = super.getTreeCellRendererComponent(aTree, aValue, aIsSelected, aIsExpanded, aIsLeaf, aRow, aHasFocus);

    if (aValue instanceof ALcdWMSLayer) {
      ALcdWMSLayer layer = (ALcdWMSLayer) aValue;

      // We use the layer's title in the tree.
      this.setText(layer.getTitle());

      // Depending on whether the layer is visible, we change the foreground color.
      if (layer.isNameVisible()) {
        if (!aIsSelected) {
          this.setForeground(Color.BLACK);
        } else {
          this.setForeground(Color.WHITE);
        }
      } else {
        this.setForeground(Color.GRAY);
      }
    }

    return cr;
  }
}
