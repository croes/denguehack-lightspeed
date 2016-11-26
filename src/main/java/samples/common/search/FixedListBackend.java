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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import com.luciad.gui.ILcdObjectIconProvider;
import samples.common.search.SearchResultPopup.Backend;

/**
 * <p>
 *   {@link Backend} implementation for a search of an in-memory list of values.
 *   This service can be used in combination with the {@link SearchResultPopup#install(JTextComponent, SearchResultPopup.Backend)}
 *   method to complement a {@code JTextComponent} with a popup list of the possible values.
 *   The list will narrow down as the user types.
 * </p>
 *
 * <p>
 *  Call {@link #setSearchableContent(String[])} to set the list of possible values.
 *  Override {@link #valueSelected} to determine what to do when the user has selected a value
 *  from the list.
 * </p>
 *
 * @see SearchResultPopup#install(JTextComponent, Backend)
 */
public abstract class FixedListBackend implements SearchResultPopup.Backend {

  private String[] fSearchContent = new String[0];
  private final Matcher fMatcher = new Matcher();

  private final DefaultTableModel fDefaultTableModel = new DefaultTableModel(0, 1) {
    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }
  };

  private final IconMatchRenderer fRenderer;
  private boolean fEnabled = true;
  private boolean fClearText = false;
  /**
   * <p>
   *   Creates a new search service for a fixed list of values.
   *   These values can be set using the {@link #setSearchableContent(String[])} method.
   * </p>
   *
   * <p>
   *   The search service can be used to complement a {@code JTextComponent}
   *   with a popup list of possible values, which will narrow down as the user types.
   *   Use the {@link SearchResultPopup#install(JTextComponent, Backend)} method
   *   for this.
   * </p>
   */
  protected FixedListBackend() {
    this(null);
  }

  /**
   * <p>
   *   Creates a new search service for a fixed list of values.
   *   These values can be set using the {@link #setSearchableContent(String[])} method.
   * </p>
   *
   * <p>
   *   The search service can be used to complement a {@code JTextComponent}
   *   with a popup list of possible values, which will narrow down as the user types.
   *   Use the {@link SearchResultPopup#install(JTextComponent, Backend)} method
   *   for this.
   * </p>
   *
   * @param aObjectIconProvider An icon provider.
   *                            When not {@code null}, it will be used to show icons in the pop-up.
   */
  protected FixedListBackend(ILcdObjectIconProvider aObjectIconProvider) {
    fRenderer = new IconMatchRenderer(aObjectIconProvider);
  }

  /**
   * @param aRows the values to choose from
   */
  public final void setSearchableContent(String[] aRows) {
    boolean old = fEnabled;
    setEnabled(false);
    try {
      fSearchContent = aRows;
      fDefaultTableModel.setRowCount(0);
      for (String row : aRows) {
        fDefaultTableModel.addRow(new Object[]{row});
      }
    } finally {
      fEnabled = old;
    }
  }

  /**
   * @return the values to choose from
   */
  public final String[] getSearchContent() {
    return fSearchContent;
  }

  private ArrayList<String> getMatchingRows(String aSearchString) {
    ArrayList<String> matchedRows = new ArrayList<String>();
    if (fSearchContent != null) {
      for (String row : fSearchContent) {
        if (fMatcher.matches(aSearchString, row)) {
          matchedRows.add(row);
        }
      }
    }
    Collections.sort(matchedRows, fMatcher.createMatchComparator(aSearchString));
    return matchedRows;
  }

  /**
   * Called whenever the user chooses an item from the list of possible values.
   * Can be null to allow clearing the value.
   *
   * @param aRow the chosen value
   */
  protected abstract void valueSelected(String aRow);

  /**
   * Set the font used for rendering
   *
   * @param aFont The font
   */
  public final void setFont(Font aFont) {
    fRenderer.setFont(aFont);
  }

  /**
   * @param aEnabled true if the popup should automatically appear when the user starts typing
   */
  public void setEnabled(boolean aEnabled) {
    fEnabled = aEnabled;
  }

  /**
   * @return true if the given string partially matches one of the possible values
   */
  public final boolean matches(String aValue) {
    return !getMatchingRows(aValue).isEmpty();
  }

  @Override
  public final TableModel getTableModel() {
    return fDefaultTableModel;
  }

  @Override
  public final TableCellRenderer getTableCellRenderer() {
    return fRenderer;
  }

  @Override
  public final void inputEntered(String aUserInput) {
    fRenderer.setSearchInformation(aUserInput, fMatcher);
    ArrayList<String> matchingRows = getMatchingRows(aUserInput);
    fDefaultTableModel.setRowCount(0);
    for (String s : matchingRows) {
      fDefaultTableModel.addRow(new Object[]{s});
    }
  }

  @Override
  public final void resultSelected(Object aResult) {
    valueSelected((String) aResult);
  }

  @Override
  public final String retrieveStringRepresentation(Object aResult) {
    return (String) aResult;
  }

  @Override
  public final boolean isClearText() {
    return fClearText;
  }

  public final void setClearText(boolean aClearText) {
    fClearText = aClearText;
  }

  @Override
  public final boolean popupMayShow() {
    return fEnabled;
  }
}
