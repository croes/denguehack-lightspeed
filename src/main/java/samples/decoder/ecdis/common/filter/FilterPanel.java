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

import static com.luciad.format.s52.TLcdS52DisplaySettings.DISPLAY_CATEGORY_PROPERTY;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.format.s52.ELcdS52DisplayCategory;
import com.luciad.format.s52.TLcdS52DisplaySettings;
import samples.decoder.ecdis.common.ObjectClass;
import samples.decoder.ecdis.common.S57ObjectClassLookup;
import samples.decoder.ecdis.common.filter.columns.ApplicableCategoriesFunction;
import samples.decoder.ecdis.common.filter.columns.ObjectClassCodeFunction;
import samples.decoder.ecdis.common.filter.columns.ObjectClassDescriptionFunction;
import samples.decoder.ecdis.common.filter.columns.ObjectClassSelectionTableColumn;
import samples.decoder.ecdis.common.filter.columns.ReadOnlyRowObjectTableColumn;
import com.luciad.util.collections.TLcdIntArrayList;

/**
 * Panel that allows configuring a {@link TLcdS52DisplaySettings}'s visible object classes. This panel allows a user to
 * add/remove details of ECDIS data to/from the view.
 */
public class FilterPanel extends JPanel {

  private static final String WINDOW_TITLE = "Filter object classes";
  private static final String INFO_LABEL_PATTERN = "{0} of {1} selected";
  private static final String RESET_FILTER_LABEL = "Reset filter";
  private static final String RESET_FILTER_TOOLTIP = "Reset the filter to show the object classes associated with a certain display category.";


  /**
   * Method that shows the filter configuration panel as a non-modal dialog.
   *
   * @param aDisplaySettings the display settings that needs be altered.
   * @param aParent the parent window
   * @return the resulting dialog containing the filter panel
   */
  public static JDialog showDialog(TLcdS52DisplaySettings aDisplaySettings, final Window aParent) {
    final JDialog dialog = new JDialog(aParent);
    dialog.setTitle(WINDOW_TITLE);
    FilterPanel filterPanel = new FilterPanel(aDisplaySettings);
    dialog.getContentPane().add(filterPanel);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.pack();
    dialog.setSize(dialog.getWidth(), filterPanel.getHeight());
    dialog.setLocationRelativeTo(aParent);
    dialog.setVisible(true);

    final WindowAdapter parentWindowMonitor = new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent aEvent) {
        if (dialog.isVisible()) {
          dialog.dispose();
        }
        aParent.removeWindowListener(this);
      }

      @Override
      public void windowGainedFocus(WindowEvent e) {
        if(dialog.isVisible()){
          dialog.requestFocusInWindow();
        }
      }
    };
    final WindowAdapter dialogMonitor = new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent aEvent) {
        aParent.removeWindowListener(parentWindowMonitor);
        aParent.removeWindowListener(this);
      }
    };
    aParent.addWindowListener(parentWindowMonitor);
    dialog.addWindowListener(dialogMonitor);
    aParent.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        if (dialog.isVisible()) {
          dialog.requestFocusInWindow();
        }
      }
    });
    return dialog;
  }

  private final SearchableTable fSearchableTable;

  /**
   * Creates a new instance.
   *
   * @param aDisplaySettings the display settings
   */
  private FilterPanel(TLcdS52DisplaySettings aDisplaySettings) {
    setLayout(new BorderLayout());

    // Create searchable table
    fSearchableTable = createTable(aDisplaySettings);

    // Wrap table up in a scroll pane
    JScrollPane scrollPane = new JScrollPane(fSearchableTable);
    JComponent toolBar = createToolbar(fSearchableTable.createSearchField(), aDisplaySettings);
    add(span(toolBar), BorderLayout.NORTH);
    add(span(scrollPane), BorderLayout.CENTER);

    // Add selection indicator at the bottom of the filter panel
    JComponent infoLabel = span(createInfoLabel(aDisplaySettings));
    infoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    add(infoLabel, BorderLayout.SOUTH);

    configureViewPortSize(scrollPane);
  }

  private SearchableTable createTable(final TLcdS52DisplaySettings aDisplaySettings) {
    final SearchableTable table = new SearchableTable();
    ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    RowObjectTableModel tableModel = createTableModel(aDisplaySettings);
    table.setModel(tableModel);
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu popup = new JPopupMenu();
          popup.add(new SelectSelection(aDisplaySettings));
          popup.add(new UnselectSelection(aDisplaySettings));
          popup.show(table, e.getPoint().x, e.getPoint().y);
        }
      }
    });
    adjustTableWidth(table);
    return table;
  }

  /**
   * Creates a {@link JLabel} containing the amount of selected selected object classes. This gives the user an extra
   * indication of the amount of detail the application is currently showing.
   *
   * @param aDisplaySettings the S-52 display settings
   * @return the component
   */
  private Component createInfoLabel(final TLcdS52DisplaySettings aDisplaySettings) {
    final JLabel infoLabel = new JLabel();
    infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
    final TableModel tableModel = fSearchableTable.getModel();
    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent aEvent) {
        int amount = countSelectedObjectClasses(aDisplaySettings);
        updateInfoLabel(infoLabel, amount, tableModel.getRowCount());
      }
    });

    int amount = countSelectedObjectClasses(aDisplaySettings);
    updateInfoLabel(infoLabel, amount, tableModel.getRowCount());

    JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
    statusPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 0));
    statusPanel.add(infoLabel);
    return statusPanel;
  }

  private void updateInfoLabel(JLabel aInfoLabel, int aAmount, int aRowCount) {
    aInfoLabel.setText(MessageFormat.format(INFO_LABEL_PATTERN, aAmount, aRowCount));
  }

  /**
   * Creates a component containing a combo box that allows initializing the standard settings of the filter together
   * with a textfield that allows searching the table specifically.
   *
   * @param aSearchField the textfield that allows a user to search through the table content
   * @param aDisplaySettings the display settings
   * @return the resulting component
   */
  private JComponent createToolbar(JComponent aSearchField, final TLcdS52DisplaySettings aDisplaySettings) {
    MultipleActionButton resetActionButton =
        new MultipleActionButton(RESET_FILTER_LABEL,
                                 new ResetFilterAction("None", null, aDisplaySettings),
                                 createResetFilterAction(aDisplaySettings, ELcdS52DisplayCategory.DISPLAY_BASE),
                                 createResetFilterAction(aDisplaySettings, ELcdS52DisplayCategory.STANDARD),
                                 new ResetFilterAction("All", ELcdS52DisplayCategory.OTHER, aDisplaySettings));
    resetActionButton.setToolTipText(RESET_FILTER_TOOLTIP);
    DefaultFormBuilder formBuilder = new DefaultFormBuilder(new FormLayout("l:p,100dlu,p:g,r:min(150dlu;p)", "p:g"));
    formBuilder.append(resetActionButton);

    formBuilder.nextColumn();
    formBuilder.append(aSearchField);

    JPanel toolBar = formBuilder.getPanel();
    toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return toolBar;
  }

  private ResetFilterAction createResetFilterAction(TLcdS52DisplaySettings aDisplaySettings, ELcdS52DisplayCategory aDisplayCategory) {
    return new ResetFilterAction(DISPLAY_CATEGORY_PROPERTY.getType().getDisplayName(aDisplayCategory), aDisplayCategory, aDisplaySettings);
  }

  private JComponent span(Component aComponent) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(aComponent, BorderLayout.CENTER);
    return panel;
  }

  private int countSelectedObjectClasses(TLcdS52DisplaySettings aDisplaySettings) {
    int[] selectedObjectClasses = aDisplaySettings.getObjectClasses();
    return selectedObjectClasses == null ? 0 : selectedObjectClasses.length;
  }

  private void configureViewPortSize(JScrollPane aScrollPane) {
    JViewport viewPort = aScrollPane.getViewport();
    Dimension viewPortSize = viewPort.getPreferredSize();
    viewPort.setPreferredSize(new Dimension(viewPortSize.width, calculateTableHeight(20)));
  }

  private int calculateTableHeight(int aVisibleRowCount) {
    TableCellRenderer renderer = fSearchableTable.getCellRenderer(0, 0);
    Component component = fSearchableTable.prepareRenderer(renderer, 0, 0);
    Dimension preferredSize = component.getPreferredSize();
    int newHeight = preferredSize.height;
    return aVisibleRowCount * newHeight;
  }

  private static RowObjectTableModel createTableModel(TLcdS52DisplaySettings aDisplaySettings) {
    List<ObjectClass> rowObjects = S57ObjectClassLookup.getLookup().getUniqueObjectClasses();// Don't allow doubles
    Collections.sort(rowObjects);
    return RowObjectTableModel.Builder.newBuilder(rowObjects)
                                      .column(new ObjectClassSelectionTableColumn(aDisplaySettings))
                                      .column(new ReadOnlyRowObjectTableColumn<>("Object class", String.class, new ObjectClassCodeFunction()))
                                      .column(new ReadOnlyRowObjectTableColumn<>("Description", String.class, new ObjectClassDescriptionFunction()))
                                      .column(new ReadOnlyRowObjectTableColumn<>("Display category", String.class, new ApplicableCategoriesFunction()))
                                      .build();
  }

  /**
   * Adjusts the width of the table relative to its content. The width is calculated based on the registered cell
   * renderer of each column.
   *
   * @param aTable the table instance
   */
  private static void adjustTableWidth(JTable aTable) {
    for (int column = 0; column < aTable.getColumnCount(); column++) {
      TableColumn tableColumn = aTable.getColumnModel().getColumn(column);
      int preferredWidth = tableColumn.getMinWidth();
      int maxWidth = tableColumn.getMaxWidth();

      for (int row = 0; row < aTable.getRowCount(); row++) {
        TableCellRenderer cellRenderer = aTable.getCellRenderer(row, column);
        Component c = aTable.prepareRenderer(cellRenderer, row, column);
        int width = c.getPreferredSize().width + aTable.getIntercellSpacing().width;
        preferredWidth = Math.max(preferredWidth, width);

        //  We've exceeded the maximum width, no need to check other rows

        if (preferredWidth >= maxWidth) {
          preferredWidth = maxWidth;
          break;
        }
      }

      tableColumn.setPreferredWidth(Math.max(preferredWidth, tableColumn.getPreferredWidth()));
    }
  }

  /**
   * Action that initializes the S-52 display settings' visible object classes with one of the standard available
   * configurations.
   */
  private class ResetFilterAction extends AbstractAction {
    private final TLcdS52DisplaySettings fDisplaySettings;
    private final ELcdS52DisplayCategory fDisplayCategory;

    public ResetFilterAction(String aLabel, ELcdS52DisplayCategory aDisplayCategory, TLcdS52DisplaySettings aDisplaySettings) {
      super(aLabel, null);
      fDisplayCategory = aDisplayCategory;
      fDisplaySettings = aDisplaySettings;
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      // Determine what to show
      int[] objectClassCodes;
      if (fDisplayCategory == ELcdS52DisplayCategory.OTHER) {
        // Select everything
        objectClassCodes = S57ObjectClassLookup.getLookup().getUniqueObjectClassCodes().toIntArray();
      } else {
        // Only select the specific object class codes applicable for the given display category
        S57ObjectClassLookup lookup = S57ObjectClassLookup.getLookup();
        objectClassCodes = lookup.getObjectClassCodes(fDisplayCategory);
      }

      // Update the display settings
      fDisplaySettings.setObjectClasses(objectClassCodes);
      if (fSearchableTable != null) {
        ((AbstractTableModel) fSearchableTable.getModel()).fireTableDataChanged();
      }
    }
  }

  private final class SelectSelection extends AbstractAction implements ListSelectionListener {

    private final TLcdS52DisplaySettings fDisplaySettings;

    public SelectSelection(TLcdS52DisplaySettings aDisplaySettings) {
      super("Show");
      fDisplaySettings = aDisplaySettings;
      fSearchableTable.getSelectionModel().addListSelectionListener(this);
      updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      int[] selectionIndices = fSearchableTable.getSelectedRows();
      RowObjectTableModel tableModel = (RowObjectTableModel) fSearchableTable.getModel();
      TLcdIntArrayList selectedRows = asObjects(selectionIndices, tableModel);
      for (int classCode : fDisplaySettings.getObjectClasses()) {
        if (!selectedRows.contains(classCode)) {
          selectedRows.add(classCode);
        }
      }

      fDisplaySettings.setObjectClasses(selectedRows.toIntArray());
      tableModel.fireTableDataChanged();
    }

    private TLcdIntArrayList asObjects(int[] aSelectionIndices, RowObjectTableModel aModel) {
      TLcdIntArrayList result = new TLcdIntArrayList();
      for (int selectionIndex : aSelectionIndices) {
        Object rowObject = aModel.getRowObject(fSearchableTable.convertRowIndexToModel(selectionIndex));
        if (rowObject instanceof ObjectClass) {
          result.add(((ObjectClass) rowObject).getCode());
        }
      }

      return result;
    }

    @Override
    public void valueChanged(ListSelectionEvent aEvent) {
      if (!aEvent.getValueIsAdjusting()) {
        updateEnabledState();
      }
    }

    private void updateEnabledState() {
      setEnabled(fSearchableTable.getSelectedRowCount() != 0);
    }
  }

  private final class UnselectSelection extends AbstractAction implements ListSelectionListener {

    private final TLcdS52DisplaySettings fDisplaySettings;

    public UnselectSelection(TLcdS52DisplaySettings aDisplaySettings) {
      super("Hide");
      fDisplaySettings = aDisplaySettings;
      updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      int[] selectionIndices = fSearchableTable.getSelectedRows();
      RowObjectTableModel model = (RowObjectTableModel) fSearchableTable.getModel();
      TLcdIntArrayList selectedRows = asObjects(selectionIndices, model);
      TLcdIntArrayList result = new TLcdIntArrayList();
      for (int classCode : fDisplaySettings.getObjectClasses()) {
        if (!selectedRows.contains(classCode)) {
          result.add(classCode);
        }
      }

      fDisplaySettings.setObjectClasses(result.toIntArray());
      model.fireTableDataChanged();
    }

    private TLcdIntArrayList asObjects(int[] aSelectionIndices, RowObjectTableModel aModel) {
      TLcdIntArrayList result = new TLcdIntArrayList();
      for (int selectionIndex : aSelectionIndices) {
        Object rowObject = aModel.getRowObject(fSearchableTable.convertRowIndexToModel(selectionIndex));
        if (rowObject instanceof ObjectClass) {
          result.add(((ObjectClass) rowObject).getCode());
        }
      }

      return result;
    }

    @Override
    public void valueChanged(ListSelectionEvent aEvent) {
      if (!aEvent.getValueIsAdjusting()) {
        updateEnabledState();
      }
    }

    private void updateEnabledState() {
      setEnabled(fSearchableTable.getSelectedRowCount() != 0);
    }
  }
}
