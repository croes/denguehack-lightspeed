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
package samples.lucy.search.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import samples.lucy.search.ISearchResult;
import samples.lucy.search.ISearchService;

/**
 * {@code TableCellRenderer} based on the renderer from the search service (see {@link ISearchService#getSearchResultRenderer()}).
 */
final class SearchResultRenderer implements TableCellRenderer {

  @Override
  public final Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
                                                       boolean aHasFocus, int aRow, int aColumn) {
    ISearchResult searchResult = (ISearchResult) aValue;
    TableCellRenderer renderer = searchResult.getSearchService().getSearchResultRenderer();
    Component component = renderer.getTableCellRendererComponent(aTable, aValue, aIsSelected, aHasFocus, aRow, aColumn);

    if (component == null) {
      component = createFallbackComponent(searchResult);
    }

    if (aTable.getRowHeight(aRow) != component.getPreferredSize().height) {
      aTable.setRowHeight(aRow, component.getPreferredSize().height);
    }
    if (aIsSelected) {
      component.setBackground(aTable.getSelectionBackground());
    } else {
      component.setBackground(aTable.getBackground());
    }
    return component;
  }

  private JLabel createFallbackComponent(ISearchResult aSearchResult) {
    JLabel label = new JLabel();
    label.setText(aSearchResult == null ? "" : aSearchResult.getResult().toString());
    label.setHorizontalAlignment(SwingConstants.LEFT);
    return label;
  }
}
