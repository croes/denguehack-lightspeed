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
package samples.decoder.ecdis.common.filter;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import samples.common.gui.TextHighlighter;

/**
 * Drop in replacement for JTable that:
 * - Enables column sorting by clicking the table header
 * - Allows to narrow the rows using a search string.
 * - Highlights those search matches.
 *
 * There is a convenience method to create a ready-to-go search text box. Alternatively, use one of the filter methods
 * to provide a search string.
 *
 * Assumption: data values don't use html text, as it uses html to do the highlighting. This is rarely an issue.
 */
public class SearchableTable extends JTable {
  private Pattern fPattern;
  private boolean initialized;

  public SearchableTable() {
    setAutoCreateRowSorter(false);

    clearFilter();
    // Don't allow moving columns
    JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);
    TriStateRowSortListener.install(this);
  }

  /**
   * Convenience method to create a search box for this table. It uses case insensitive literal matching
   * of the entered text.
   *
   * @return The component that needs to be added to some part of the UI.
   */
  public JComponent createSearchField() {
    final JTextField searchBox = new SearchTextField();
    searchBox.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        search();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        search();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        search();
      }

      private void search() {
        filterWithContainsText(searchBox.getText());
      }
    });
    return searchBox;
  }

  /**
   * Searches for a literal, case-insensitive occurrence of the given text.
   * Use the empty string to clear the filter, this is convenient in combination with a search text field.
   * @param aText The text to search and highlight.
   */
  public void filterWithContainsText(String aText) {
    filterWithRegEx(Pattern.compile(aText, Pattern.CASE_INSENSITIVE | Pattern.LITERAL));
  }

  /**
   * Similar to {@link #filterWithContainsText(String)}, but with a regex pattern.
   * @param aRegEx The pattern.
   */
  public void filterWithRegEx(Pattern aRegEx) {
    fPattern = aRegEx;
    initSorter();
  }

  @Override
  public void setModel(TableModel aModel) {
    super.setModel(aModel);
    initialized = false; // Force initialization
    initSorter();
    initialized = true;
  }

  private void initSorter() {
    TableRowSorter<TableModel> sorter;
    TableModel tableModel = getModel();
    if (!initialized) {
      /*
        Avoid setting a new row sorter instance over and over again. Just fire a tableDataChanged event if the filter
        rules (i.e. the input text) change
       */
      sorter = new TableRowSorter<>(tableModel);
      sorter.setRowFilter(new MatcherFilter());
      setRowSorter(sorter);
      return;
    }

    if (tableModel instanceof AbstractTableModel) {
      ((AbstractTableModel) tableModel).fireTableDataChanged();
    }
  }

  public void clearFilter() {
    filterWithContainsText("");
  }

  @Override
  public Component prepareRenderer(TableCellRenderer aRenderer, int aRow, int aColumn) {
    // By overriding this method instead of using a custom renderer, the interaction with existing code, look&feel
    // changes etc. is absolutely minimal. The only impact is that the code assumes no html tags are used in the
    // text, because it uses html itself to do the highlighting.
    Component component = super.prepareRenderer(aRenderer, aRow, aColumn);
    highlight(component);
    return component;
  }

  private void highlight(Component aComponent) {
    if (aComponent instanceof JLabel) {
      JLabel label = (JLabel) aComponent;
      label.setText(TextHighlighter.createHighlightedText(fPattern, label.getText()));
    } else if (aComponent instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) aComponent;
      tc.setText(TextHighlighter.createHighlightedText(fPattern, tc.getText()));
    }
  }

  private class MatcherFilter extends RowFilter<Object, Object> {
    @Override
    public boolean include(Entry<?, ?> aEntry) {
      for (int i = 0, c = aEntry.getValueCount(); i < c; i++) {
        Matcher matcher = fPattern.matcher(aEntry.getStringValue(i));
        if (matcher.find()) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   *  Enables Tri-state row sorting on this table. This means that it allows a table to be sorted in ascending and
   *  descending order but also to disable row sorting by clicking a third time on the table header.
   */
  private static final class TriStateRowSortListener extends MouseAdapter {
    private int fMouseClicks;
    private JTable fTable;

    public static void install(JTable aTable) {
      TriStateRowSortListener triStateRowSortListener = new TriStateRowSortListener(aTable);
      aTable.getTableHeader().addMouseListener(triStateRowSortListener);
    }

    private TriStateRowSortListener(JTable aTable) {
      fTable = aTable;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      int columnIdx = fTable.getColumnModel().getColumnIndexAtX(e.getX());
      //build a list of sort keys for this column, and pass it to the sorter
      //you can build the list to fit your needs here
      //for example, you can sort on multiple columns, not just one
      List<RowSorter.SortKey> sortKeys = new ArrayList<>();
      //cycle through all orders; sort is removed every 3rd click
      SortOrder order = SortOrder.values()[fMouseClicks % 3];
      sortKeys.add(new RowSorter.SortKey(columnIdx, order));
      fTable.getRowSorter().setSortKeys(sortKeys);
      ++fMouseClicks;
    }
  }
}
