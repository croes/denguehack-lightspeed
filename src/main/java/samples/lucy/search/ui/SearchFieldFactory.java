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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXSearchField;

import samples.common.MacUtil;
import samples.common.search.SearchResultPopup;
import samples.lucy.search.ISearchResult;
import samples.lucy.search.SearchManager;

/**
 * Factory class to creates a search component. This component is composed of a text field in which the user can
 * enter a search operation. Whenever results are found, a popup menu is displayed showing all intermediate search
 * results from which the user can choose.
 */
public final class SearchFieldFactory {
  private SearchFieldFactory() {
  }

  /**
   * Creates the search field component.
   *
   * @param aSearchManager the search manager that is connected with the created search field.
   * @param aDefaultSearchText the default search text in the search field
   * @return the search component
   */
  public static JComponent createSearchField(final SearchManager aSearchManager, String aDefaultSearchText) {
    JTextComponent searchComponent;

    ActionListener cancelActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent aEvent) {
        aSearchManager.cancelAndClearCurrentSearchResults();
      }
    };

    if (MacUtil.isAquaLookAndFeel()) {
      searchComponent = createAquaLookAndFeelSearchField(aSearchManager, cancelActionListener);
    } else {
      SearchField searchField = new SearchField(aDefaultSearchText, aSearchManager);
      searchField.setSearchMode(JXSearchField.SearchMode.INSTANT);
      searchField.setInstantSearchDelay(200);
      searchField.setLayoutStyle(JXSearchField.LayoutStyle.MAC);
      JButton cancelButton = searchField.getCancelButton();
      cancelButton.addActionListener(cancelActionListener);
      searchComponent = searchField;
    }

    SearchResultPopup popup = SearchResultPopup.install(searchComponent, new SearchResultPopup.Backend() {
      @Override
      public TableModel getTableModel() {
        return aSearchManager.getSearchResultsUIModel();
      }

      @Override
      public TableCellRenderer getTableCellRenderer() {
        return new SearchResultRenderer();
      }

      @Override
      public void inputEntered(String aUserInput) {
        aSearchManager.search(aUserInput);
      }

      @Override
      public void resultSelected(Object aResult) {
        aSearchManager.handleResult((ISearchResult) aResult);
      }

      @Override
      public String retrieveStringRepresentation(Object aResult) {
        return ((ISearchResult) aResult).getStringRepresentation();
      }

      @Override
      public boolean popupMayShow() {
        return true;
      }

      @Override
      public boolean isClearText() {
        return false;
      }
    });
    popup.setWidthFactor(2);

    return searchComponent;
  }

  /**
   * <p>
   *   The {@code JXSearchField} does not work on OS X in combination with the Aqua look-and-feel.
   *   Use a regular text field instead, and configure it to behave like a search field (Aqua look-and-feel specific options).
   * </p>
   *
   * <p>
   *   See <a href="https://java.net/jira/browse/SWINGX-1597">the SwingX bug tracker</a> for the bug.
   * </p>
   *
   * @return The search field
   */
  private static JTextField createAquaLookAndFeelSearchField(final SearchManager aSearchManager, ActionListener aCancelSearchActionListener) {
    JTextField searchField = new JTextField();

    //See https://developer.apple.com/library/mac/technotes/tn2007/tn2196.html for documentation about those options
    searchField.putClientProperty("JTextField.variant", "search");
    searchField.putClientProperty("JTextField.Search.CancelAction", aCancelSearchActionListener);

    searchField.setColumns(15);

    return searchField;
  }
}
