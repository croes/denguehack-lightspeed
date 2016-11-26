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
package samples.lucy.tableviewext;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Wrapper around a existing renderer which adds some HTML markup in case the component
 * is a <code>JLabel</code>
 */
final class CustomTableRenderer implements TableCellRenderer {
  private String fHtmlPrefix;
  private String fHtmlSuffix;
  private CustomTableViewCustomizerPanel fCustomizerPanel;

  private TableCellRenderer fDelegate;

  public CustomTableRenderer(CustomTableViewCustomizerPanel aCustomizerPanel,
                             TableCellRenderer aTableCellRenderer) {
    fCustomizerPanel = aCustomizerPanel;
    fDelegate = aTableCellRenderer;
    setColor(fCustomizerPanel.getColor());
    fCustomizerPanel.addPropertyChangeListener("colorIndex", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        setColor(fCustomizerPanel.getColor());
        if (fCustomizerPanel.getTable() != null) {
          fCustomizerPanel.getTable().repaint();
        }
      }
    });
  }

  /**
   * Returns a HTML string representation of {@code aColor}
   *
   * @param aColor The color
   *
   * @return a HTML string representation of {@code aColor}
   */
  static String toHtml(Color aColor) {
    return String.format("#%02x%02x%02x", aColor.getRed(), aColor.getGreen(), aColor.getBlue());
  }

  public void setColor(Color aColor) {
    fHtmlPrefix = "<html><font color=\"" + toHtml(aColor) + "\"><b><i>";
    fHtmlSuffix = "</i></b></font></html>";
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component cellRendererComponent = fDelegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    //only change the color of the column at index 1
    if (table.convertColumnIndexToModel(column) == 1 &&
        cellRendererComponent instanceof JLabel) {
      ((JLabel) cellRendererComponent).setText(fHtmlPrefix + ((JLabel) cellRendererComponent).getText() + fHtmlSuffix); //add markup
    }
    return cellRendererComponent;
  }

}
