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
package samples.common.search;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.swing.TLcdSWIcon;

public final class IconMatchRenderer extends MatchRenderer {

  private final ILcdObjectIconProvider fObjectIconProvider;
  private final JPanel fPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
  private final JLabel fIconLabel = new JLabel();

  public IconMatchRenderer(ILcdObjectIconProvider aObjectIconProvider) {
    fObjectIconProvider = aObjectIconProvider;
    fPanel.setBorder(BorderFactory.createEmptyBorder());
    fPanel.setOpaque(false);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    fPanel.removeAll();
    Icon swIcon = null;
    if (fObjectIconProvider != null && fObjectIconProvider.canGetIcon(value)) {
      ILcdIcon icon = fObjectIconProvider.getIcon(value);
      if (icon != null) {
        swIcon = new TLcdSWIcon(icon);
      }
    }
    fIconLabel.setIcon(swIcon);
    fPanel.add(fIconLabel);
    fPanel.add(component);
    if (table.getRowHeight(row) < fPanel.getPreferredSize().height) {
      table.setRowHeight(row, fPanel.getPreferredSize().height);
    }
    return fPanel;
  }
}
