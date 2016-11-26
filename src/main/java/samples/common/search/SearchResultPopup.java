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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import samples.common.gui.PopupPanel;

/**
 * <p>
 *   This class allows to decorate a {@code JTextComponent} with a pop-up, for
 *   example to offer auto-complete options or display search results.
 * </p>
 *
 * <p>
 *   You can decorate a {@code JTextComponent} by calling the {@link #install(JTextComponent, Backend)}
 *   method.
 *   The actual logic which determines the contents of the pop-up, and how to render that contents,
 *   is contained in the {@link Backend} class and passed as parameter to the {@code install} method.
 * </p>
 */
public final class SearchResultPopup {

  /**
   * The {@code SearchResultPopup} class shows a pop-up, but it is the back-end which decides what
   * to show in the pop-up and how to deal with the user selecting a value from the pop-up.
   */
  public interface Backend {
    /**
     * Returns a {@code TableModel} instance containing all entries which must be shown in the pop-up.
     *
     * @return a {@code TableModel} instance containing all entries which must be shown in the pop-up.
     */
    TableModel getTableModel();

    /**
     * Returns a {@code TableCellRenderer} which will be used to render the values from the {@link #getTableModel() TableModel}
     * in the pop-up menu
     *
     * @return the renderer
     */
    TableCellRenderer getTableCellRenderer();

    /**
     * This method will be called when the user inputs a search query.
     * As this method is called on the Event Dispatch Thread, it must return quickly.
     * If the handling of the input takes a long time, this method should start a background thread
     * and perform the handling on that thread.
     * An example of such a long running implementation would be to perform a search on the internet with the input.
     *
     * Note that this method returns no results.
     * When results are available, the implementation of this interface must update the {@link #getTableModel() TableModel}
     * to include those results.
     * The pop-up will then be updated with the new contents of the {@code TableModel}
     *
     * @param aUserInput The user input
     */
    void inputEntered(String aUserInput);

    /**
     * This method will be called when the user selected a value from the pop-up.
     * As this method is called on the Event Dispatch Thread, it must return quickly.
     *
     * @param aResult The result selected by the user.
     *                This will be a value obtained from the {@link #getTableModel() TableModel}.
     */
    void resultSelected(Object aResult);

    /**
     * When the user selected a value, a String representation of the value will be shown in the
     * text component.
     * This string representation is obtained by calling this method.
     *
     * @param aResult The result chosen by the user
     *
     * @return A string representation which will be shown in the text component for {@code aResult}
     */
    String retrieveStringRepresentation(Object aResult);

    /**
     * This method will be called when an attempt is made to show the pop-up.
     * This allows an external party to suppress the appearance of the pop-up.
     * An example use-case of this method is when you want to call {@link JTextComponent#setText(String)}
     * on the text component yourself, and do not want the pop-up to appear.
     *
     * @return {@code false} when you want to suppress the appearance of the pop-up menu.
     */
    boolean popupMayShow();

    /**
     * This method will be called each time a result is selected, and determines whether the text component is cleared
     * or populated with the {@link #retrieveStringRepresentation(Object)} instead.
     *
     * @return {@code true} when the text component should be cleared when a search result is selected.
     */
    boolean isClearText();
  }

  private final JTextComponent fTextComponent;
  private final Backend fBackend;
  private final PopupContent fPopupContent;
  private final PopupPanel fPopup;
  private final FocusAdapter fFocusListener = new FocusAdapter() {
    @Override
    public void focusGained(FocusEvent e) {
      fTextComponent.selectAll(); // allows easy replacement of the current value
    }

    @Override
    public void focusLost(FocusEvent e) {
      fTextComponent.setCaretPosition(0);
      if (fPopup.isPopupVisible()) {
        addSelected();
      }
    }
  };
  private double fWidthFactor = 1.0;
  private DocumentListener fSearchFieldDocumentListenerListener;
  private KeyAdapter fSearchFieldKeyAdapter;

  private boolean fBackEndIsHandlingInput = false;

  /**
   * Installs this popup on the search field that shows all found search results.
   *
   * @param aSearchField the search field containing the user input
   * @param aBackend the search service
   */
  public static SearchResultPopup install(JTextComponent aSearchField, Backend aBackend) {
    return new SearchResultPopup(aSearchField, aBackend);
  }

  private SearchResultPopup(JTextComponent aTextComponent, Backend aBackend) {
    fTextComponent = aTextComponent;
    fBackend = aBackend;
    setupSearchField();
    fPopupContent = createPopupComponent(aBackend);

    JScrollPane scrollPane = new JScrollPane(fPopupContent, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
      @Override
      public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        // Scale the width of the popup depending on the current width of the text field.
        pref.width = (int) Math.round(fTextComponent.getWidth() * fWidthFactor);
        pref.height = 300;
        return pref;
      }
    };
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    fPopup = PopupPanel.create(aTextComponent, scrollPane, false);
  }

  private PopupContent createPopupComponent(Backend aBackend) {
    final PopupContent table = new PopupContent(aBackend);
    table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
    table.getActionMap().put("Enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent aEvent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
          if (table.getRowCount() == 0) {
            // don't do anything
            return;
          }
          if (fTextComponent.getText().isEmpty()) {
            //pressing enter in an empty field should have no effect when there is nothing selected yet
            return;
          }
          //if the table contains an exact match for the current text, use that one
          //this is for the scenario where the user presses enter in a field where the value is already set
          //without this code, this would select the first value which can be a change
          //see LCD-7920
          int rowCount = table.getModel().getRowCount();
          for (int i = 0; i < rowCount; i++) {
            Object valueAt = table.getModel().getValueAt(i, 0);
            String s = fBackend.retrieveStringRepresentation(valueAt);
            if (fTextComponent.getText().equals(s)) {
              selectedRow = table.convertRowIndexToView(i);
              break;
            }
          }

          // select the very first element in the table
          if (selectedRow == -1) {
            selectedRow = 0;
          }
        }

        Object selectedValue = table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 0);
        handleSearchResultOnSelect(selectedValue, fBackend, table);
      }
    });

    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent aEvent) {
        int selectedRow = table.getSelectedRow();
        Object selectedValue = table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 0);
        handleSearchResultOnSelect(selectedValue, fBackend, table);
      }
    });
    table.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent aEvent) {
        int row = table.rowAtPoint(aEvent.getPoint());
        if (row != -1) {
          table.getSelectionModel().setSelectionInterval(row, row);
        } else {
          table.clearSelection();
        }
      }
    });
    table.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent aEvent) {
        int changeType = aEvent.getType();
        switch (changeType) {
        case TableModelEvent.INSERT:
        case TableModelEvent.UPDATE:
        case TableModelEvent.DELETE:
          //When the back-end is handling input, we already have a call to show the pop-up
          //No need to do it once more in an invokeLater
          if (fBackend.popupMayShow() && !fBackEndIsHandlingInput) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                if (table.getModel().getRowCount() > 0) {
                  showPopup();
                }
              }
            });
          }
          break;
        default:
          // No-op
        }
      }
    });
    return table;
  }

  private void handleSearchResultOnSelect(Object aSelectedValue, Backend aBackend, PopupContent aTable) {
    fTextComponent.getDocument().removeDocumentListener(fSearchFieldDocumentListenerListener);
    fTextComponent.removeKeyListener(fSearchFieldKeyAdapter);
    fTextComponent.removeFocusListener(fFocusListener);

    if (aSelectedValue != null) {
      aBackend.resultSelected(aSelectedValue);
      fTextComponent.setText(fBackend.isClearText() ? "" : aBackend.retrieveStringRepresentation(aSelectedValue));
    }
    hidePopup();
    aTable.clearSelection();

    fTextComponent.getDocument().addDocumentListener(fSearchFieldDocumentListenerListener);
    fTextComponent.addKeyListener(fSearchFieldKeyAdapter);
    fTextComponent.addFocusListener(fFocusListener);
  }

  private void setupSearchField() {
    fSearchFieldDocumentListenerListener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent aEvent) {
        handleChange();
      }

      @Override
      public void removeUpdate(DocumentEvent aEvent) {
        handleChange();
      }

      @Override
      public void changedUpdate(DocumentEvent aEvent) {
        handleChange();
      }

      private void handleChange() {
        boolean old = fBackEndIsHandlingInput;
        fBackEndIsHandlingInput = true;
        try {
          fBackend.inputEntered(fTextComponent.getText());
          showPopup();
        } finally {
          fBackEndIsHandlingInput = old;
        }
      }
    };

    fSearchFieldKeyAdapter = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent aEvent) {
        int keyCode = aEvent.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
          hidePopup();
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP
                   || keyCode == KeyEvent.VK_PAGE_DOWN || keyCode == KeyEvent.VK_PAGE_UP) {
          if (keyCode == KeyEvent.VK_DOWN) {
            showPopup();
          }
          fPopupContent.dispatchEvent(aEvent);
        }
      }
    };

    fTextComponent.getDocument().addDocumentListener(fSearchFieldDocumentListenerListener);
    fTextComponent.addKeyListener(fSearchFieldKeyAdapter);
    fTextComponent.addFocusListener(fFocusListener);
  }

  private void addSelected() {
    int selectedRow = fPopupContent.getSelectedRow();
    Object selectedValue = selectedRow != -1 ? fPopupContent.getModel().getValueAt(fPopupContent.convertRowIndexToModel(selectedRow), 0) : null;
    if (selectedValue != null) {
      handleSearchResultOnSelect(selectedValue, fBackend, fPopupContent);
    } else {
      String text = fTextComponent.getText();
      // if the user doesn't want to clear the value ...
      if (text != null && !text.isEmpty()) {
        // ... she'll want to select a correct value.
        // If there is a literal match, use that one. Otherwise auto-complete with the first entry.
        final TableModel model = fPopupContent.getModel();
        if (model.getRowCount() > 0) {
          for (int i = model.getRowCount() - 1; i >= 0; --i) {
            selectedValue = model.getValueAt(i, 0);
            if (fBackend.retrieveStringRepresentation(selectedValue).equalsIgnoreCase(text) || i == 0) {
              handleSearchResultOnSelect(selectedValue, fBackend, fPopupContent);
              break;
            }
          }
        }
      } else {
        handleSearchResultOnSelect(null, fBackend, fPopupContent);
      }
    }
  }

  public void showPopup() {
    if (!fTextComponent.isEnabled() || !fTextComponent.isShowing() || !fBackend.popupMayShow()) {
      return;
    }
    if (!fPopup.isPopupVisible() && fPopupContent.getModel().getRowCount() > 0) {
      fPopup.setPopupVisible(true);
      fPopupContent.clearSelection();
    }
  }

  public void hidePopup() {
    fPopup.setPopupVisible(false);
  }

  public void toggle() {
    if (fPopup.isPopupVisible()) {
      hidePopup();
    } else {
      showPopup();
    }
  }

  public void setWidthFactor(double aWidthFactor) {
    fWidthFactor = aWidthFactor;
  }

  boolean isPopupShowing() {
    return fPopup.isPopupVisible();
  }

  Component getPopupContent() {
    return fPopupContent;
  }

  /**
   * Contents for the popup containing the search results.
   */
  private static final class PopupContent extends JTable {

    private final TableCellRenderer fRenderer;

    public PopupContent(Backend aBackend) {
      super(aBackend.getTableModel());
      fRenderer = aBackend.getTableCellRenderer();
      setTableHeader(null);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setShowGrid(false);

      // Do not transfer focus to the pop-up content as that applies the current selection. See focusLost method above.
      setFocusable(false);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
      return fRenderer;
    }
  }
}
