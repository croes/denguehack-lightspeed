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
package samples.lucy.tableview;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import com.luciad.util.ILcdDisposable;

/**
 * <p>
 *   An extension of JXTable that allows per cell rendering and editing by delegating to registered
 *   implementations of {@code ITableCellRendererProvider} and {@code ITableCellEditorProvider}.
 * </p>
 * <p>
 *   This extension also tries to keep the selection before and after a model change consistent.
 *   The default {@link JXTable#tableChanged(TableModelEvent)} implementation will often clear the selection in the
 *   table, even when the rows which were selected before the incoming change are still present after the change.<br/>
 *   This extension has adjusted that method, and tries to restore the selection after the handling of the model event
 *   is complete.
 * </p>
 * <p>
 *   This is very convenient if you open a table view of a realtime model, and sync the selection between table view
 *   and layer on the map.
 *   Without this feature, the selection in the table (and hence the selection on the map) would get lost each time
 *   the model updates.
 * </p>
 */
public class EditableTable extends JXTable {

  private List<Object> fSelectedObjects;
  private ListSelectionListener fSelectionListener;

  private ITableCellRendererProvider fRendererProvider;
  private ITableCellEditorProvider fEditorProvider;
  private boolean fValidSelection = true;

  public EditableTable(IExtendedTableModel aTableModel) {
    super(aTableModel);

    // Nimbus look and feel doesn't show the grid as it uses alternate row coloring, but then the columns don't
    // show up really well, so enable the vertical lines. If the grid lines are already painted, re-enabling the
    // vertical lines doesn't harm.
    setShowVerticalLines(true);
  }

  public ITableCellRendererProvider getRendererProvider() {
    return fRendererProvider;
  }

  public ITableCellEditorProvider getEditorProvider() {
    return fEditorProvider;
  }

  /*
   * The method getStringAt should return a string that is exactly what a user sees in the table
   * (WYSIWYG). Since we potentially have a different renderer for each cell, we must directly refer
   * to that renderer to retrieve this String.
   */
  @Override
  public String getStringAt(int row, int column) {
    TableCellRenderer aRenderer = getCellRenderer(row, column);
    Object aValue = getModel().getValueAt(convertRowIndexToModel(row),
                                          convertColumnIndexToModel(column));

    if (aRenderer instanceof DefaultTableRenderer) { //the default swing-x renderer, used in the customizer
      return ((DefaultTableRenderer) aRenderer).getString(aValue);
    } else if (aRenderer instanceof DefaultTableCellRenderer) { //DefaultTableCellRenderer is an extension of JLabel
      Component rendererComponent = aRenderer.getTableCellRendererComponent(this, aValue, false, false, row, column);
      if (rendererComponent instanceof JLabel) {
        return ((JLabel) rendererComponent).getText();
      }
    }
    //Fall back (for instance for checkboxes).
    return aValue != null ? aValue.toString() : ""; //this can happen for a boolean value that is not set.
  }

  public void setRendererProvider(ITableCellRendererProvider aRendererProvider) {
    fRendererProvider = aRendererProvider;
  }

  public void setEditorProvider(ITableCellEditorProvider aEditorProvider) {
    fEditorProvider = aEditorProvider;
  }

  /*
   * Delegates to the registered providers. There should always be a renderer, so one should
   * make sure that for every cell, there is a registered provider that offers a renderer for that
   * cell.
   */
  @Override
  public TableCellRenderer getCellRenderer(int aRow, int aColumn) {
    TableCellRenderer renderer = fRendererProvider.provideRenderer(this, aRow, aColumn);
    if (renderer == null) {
      throw new IllegalStateException("There should always be a renderer!");
    }
    return renderer;
  }

  /*
   * A cell is editable if the model allows it (super) and if an editor can be created for it. This
   * also allows us to highlight the cells that can be edited using an editor.
   */
  @Override
  public boolean isCellEditable(int aRow, int aColumn) {
    return super.isCellEditable(aRow, aColumn) &&
           fEditorProvider.canProvideEditor(this, aRow, aColumn);
  }

  /*
   * Returns an editor for an editable cell.
   */
  @Override
  public TableCellEditor getCellEditor(int aRow, int aColumn) {
    if (!fEditorProvider.canProvideEditor(this, aRow, aColumn)) {
      return null;
    }
    return fEditorProvider.provideEditor(this, aRow, aColumn);
  }

  /*
   * Our editor implementation needs to be cleaned up once the table is done with it.
   */
  @Override
  public void removeEditor() {
    if (getCellEditor() instanceof ILcdDisposable) {
      ((ILcdDisposable) getCellEditor()).dispose();
    }
    super.removeEditor();
  }

  @Override
  protected JTableHeader createDefaultTableHeader() {
    return new TooltipTableHeader(columnModel, getModel());
  }

  @Override
  public void setModel(TableModel dataModel) {
    if (!(dataModel instanceof IExtendedTableModel)) {
      throw new IllegalArgumentException("The EditableTable only accepts IExtendedTableModel instances as table model");
    }
    super.setModel(dataModel);
    storeCurrentSelection();
  }

  private List<Object> getCurrentSelectedObjects() {
    IExtendedTableModel model = (IExtendedTableModel) getModel();
    int[] currentSelectedRows = getSelectedRows();
    List<Object> currentSelection = new ArrayList<>(currentSelectedRows.length);
    for (int currentSelectedRow : currentSelectedRows) {
      currentSelection.add(model.getObjectAtRow(convertRowIndexToModel(currentSelectedRow)));
    }
    return currentSelection;
  }

  @Override
  public void setSelectionModel(ListSelectionModel newModel) {
    if (fSelectionListener == null) {
      //this method is called from the super class in the constructor
      //assigning those fields in the constructor or the declaration is too late
      fSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          storeCurrentSelection();
        }
      };
    }
    if (fSelectedObjects == null) {
      fSelectedObjects = new ArrayList<>();
    }
    ListSelectionModel old = getSelectionModel();
    if (old != null) {
      old.removeListSelectionListener(fSelectionListener);
    }
    super.setSelectionModel(newModel);
    newModel.addListSelectionListener(fSelectionListener);
    storeCurrentSelection();
  }

  private void storeCurrentSelection() {
    fSelectedObjects.clear();
    fSelectedObjects.addAll(getCurrentSelectedObjects());
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   *   This method is overridden to ensure that the selection in the table view is restored
   *   to the previous state when the table model is changed.
   * </p>
   *
   * @see #isSelectionValid()
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    List<Object> currentSelection = new ArrayList<>(fSelectedObjects);
    fValidSelection = false;
    try {
      super.tableChanged(e);
      selectObjectsInTable(currentSelection, this);
    } finally {
      fValidSelection = true;
    }
  }

  /**
   * <p>
   *   Returns {@code false} when the table selection has been temporarily changed, and will be corrected
   *   almost immediately.
   * </p>
   * <p>
   *   When the underlying {@code TableModel} is changed, the {@link #tableChanged(TableModelEvent)} method is triggered.
   *   This method will clear the table selection.
   *   When the table selection is kept in sync with the layer selection, this would mean that the layer selection is cleared as
   *   well.
   *   To avoid this, the {@link #tableChanged(TableModelEvent)} method restores the selection after the {@code JTable} has cleared
   *   it.
   * </p>
   * <p>
   *   However, clearing and restoring the selection in the table will clear and restore the selection on the layer as well when
   *   selection synchronization is active.
   *   This can have unwanted side-effects, like for example the disappearance of a balloon.
   *   To avoid this, the layer selection synchronization code can call this method to see whether it can ignore the
   *   changes in selection.
   *   When this method returns {@code false}, the table is busy altering the selection to an unwanted state, and those
   *   events can be ignored.
   * </p>
   *
   * @return {@code false} when the table selection has been temporarily changed, and will be corrected
   *   almost immediately
   */
  public final boolean isSelectionValid() {
    return fValidSelection;
  }

  /**
   * <p>
   *   Converts a list of integers into a list of continuous intervals.
   * </p>
   * <p>
   *   This can be used when adjusting the selection in the table.
   *   The {@link ListSelectionModel} of a {@code JTable} allows to select multiple rows by
   *   specifying the interval that must be selected.
   *   By selecting multiple rows in one go, we can reduce the number of selection change events
   *   that are fired.
   *   Performance will benefit from a reduced number of events.
   * </p>
   *
   */
  static List<int[]> convertToContinuousIntervals(List<Integer> indices) {
    Collections.sort(indices);

    List<int[]> result = new ArrayList<>();

    int startValue = 0;
    int previousValue = 0;
    for (int i = 0; i < indices.size(); i++) {
      Integer integer = indices.get(i);
      if (i == 0) {
        startValue = integer;
        previousValue = integer;
      } else {
        if (integer == previousValue + 1) {
          previousValue = integer;
        } else {
          result.add(new int[]{startValue, previousValue});
          startValue = integer;
          previousValue = integer;
        }
      }
      if (i == indices.size() - 1) {
        result.add(new int[]{startValue, integer});
      }
    }
    return result;
  }

  /**
   * Sets the table selection to all objects contained in {@code aObjectsToSelect}.
   *
   * @param aObjectsToSelect The objects to select
   * @param aTable The table of which the selection will be altered.
   *               Note that this method assumes the model of the table is a {@link IExtendedTableModel} instance.
   *
   * @return The indices of the selected objects in the table.
   */
  static List<Integer> selectObjectsInTable(List<Object> aObjectsToSelect, JTable aTable) {
    IExtendedTableModel model = (IExtendedTableModel) aTable.getModel();
    List<Integer> indices = new ArrayList<>();
    for (Object selectedObject : aObjectsToSelect) {
      int modelIndex = model.getRowOfObject(selectedObject);
      if (modelIndex != -1) {
        int indexInTable = aTable.convertRowIndexToView(modelIndex);
        if (indexInTable != -1) {
          indices.add(indexInTable);
        }
      }
    }
    List<int[]> intervals = convertToContinuousIntervals(indices);
    ListSelectionModel selectionModel = aTable.getSelectionModel();
    boolean old = selectionModel.getValueIsAdjusting();
    selectionModel.setValueIsAdjusting(true);
    selectionModel.clearSelection();
    try {
      for (int[] interval : intervals) {
        selectionModel.addSelectionInterval(interval[0], interval[1]);
      }
    } finally {
      selectionModel.setValueIsAdjusting(old);
    }
    return indices;
  }

  /*
     * This extension of the JXTableHeader overrides the tooltips.
     */
  private static class TooltipTableHeader extends JXTableHeader {

    private final TableModel fTableModel;

    public TooltipTableHeader(TableColumnModel aColumnModel, TableModel aTableModel) {
      super(aColumnModel);
      fTableModel = aTableModel;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
      int viewIndex = getColumnModel().getColumnIndexAtX(event.getX());
      int modelIndex = getColumnModel().getColumn(viewIndex).getModelIndex();
      if (fTableModel instanceof IExtendedTableModel) {
        return ((IExtendedTableModel) fTableModel).getColumnTooltipText(modelIndex);
      }
      return fTableModel.getColumnName(modelIndex);
    }

  }
}
