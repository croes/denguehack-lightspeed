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
package samples.lucy.treetableview;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

import samples.lucy.tableview.ITableCellEditorProvider;
import samples.lucy.tableview.ITableCellRendererProvider;
import samples.lucy.tableview.TableTransferHandler;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.ILcdLayer;

/**
 * Extension of JXTreeTable that allows per cell rendering and editing by using registered implementations
 * <code>ITableCellRendererProvider</code> and <code>ITableCellEditorProvider</code>.
 */
class EditableTreeTable extends JXTreeTable {

  private ITableCellRendererProvider fRendererProvider;
  private ITableCellEditorProvider fEditorProvider;

  private final ILcdLayer fLayer;
  private final TreeModelListener fStopEditingListener = new StopEditingListener(this);
  private final PreferredSizeUpdater fPreferredSizeUpdater = new PreferredSizeUpdater(this);
  private final ValueColumnCellRenderer fValueColumnCellRenderer = new ValueColumnCellRenderer();

  EditableTreeTable() {
    this(null);
  }

  /**
   * Creates a new EditableTreeTable, with optional layer.
   *
   * @param aLayer the layer in which the domain object displayed in this tree table is visualized in
   *               a view.
   */
  EditableTreeTable(ILcdLayer aLayer) {
    //use a handler which uses the same String representations as in the renderer.
    setTransferHandler(new TableTransferHandler(this));
    fLayer = aLayer;
  }

  @Override
  public void setTreeTableModel(TreeTableModel treeModel) {
    if (getTreeTableModel() != null) {
      getTreeTableModel().removeTreeModelListener(fStopEditingListener);
      getTreeTableModel().removeTreeModelListener(fPreferredSizeUpdater);
    }
    super.setTreeTableModel(treeModel);
    if (treeModel != null) {
      treeModel.addTreeModelListener(fStopEditingListener);
      treeModel.addTreeModelListener(fPreferredSizeUpdater);
      updatePreferredSize();
    }
  }

  public ITableCellRendererProvider getRendererProvider() {
    return fRendererProvider;
  }

  public ITableCellEditorProvider getEditorProvider() {
    return fEditorProvider;
  }

  public void setRendererProvider(ITableCellRendererProvider aRendererProvider) {
    fRendererProvider = aRendererProvider;
  }

  public void setEditorProvider(ITableCellEditorProvider aEditorProvider) {
    fEditorProvider = aEditorProvider;
  }

  @Override
  protected JTableHeader createDefaultTableHeader() {
    JTableHeader result = super.createDefaultTableHeader();
    result.setTable(this);
    return result;
  }

  @Override
  public TableCellRenderer getCellRenderer(int aRow, int aColumn) {
    //Delegate to the original renderer for the hierarchical column.
    if (isHierarchical(aColumn)) {
      return super.getCellRenderer(aRow, aColumn);
    } else {
      // Use dedicated value column renderer for the other column.
      // Implementation note: SwingX may call this method for row 0, and later on reuse the same
      // renderer for other rows. See org.jdesktop.swingx.table.ColumnFactory.packColumn and
      // org.jdesktop.swingx.table.ColumnFactory.getCellRenderer.
      return fValueColumnCellRenderer;
    }
  }

  @Override
  public Component prepareRenderer(TableCellRenderer aTableCellRenderer, int row, int column) {
    Component rendererComponent = super.prepareRenderer(aTableCellRenderer, row, column);
    updateTooltipForBounds(rendererComponent, getCellRect(row, column, false));
    return rendererComponent;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    //by default JXTreeTable returns null when the value is asked of one of the tree leafs.
    //we override this method so that the object at the tree location is returned.
    //this allows the user to copy-paste the selection of the table to a spreadsheet application,
    //including the names of the features
    if (columnIndex > 0) {
      return super.getValueAt(rowIndex, columnIndex);
    } else {
      return getPathForRow(rowIndex).getLastPathComponent();
    }
  }

  /**
   * If the given renderer component is a JLabel and the given renderer space is too small to let
   * the label show all its text, sets the text of the label as its tooltip.
   *
   * Otherwise the label tooltip is cleared.
   *
   * @param aLabel          The JLabel whose tooltip should be updated. Must not be
   *                        <code>null</code>.
   * @param aRendererBounds The space which the label gets to render its contents.
   */
  private static void updateTooltipForBounds(Component aLabel, Rectangle aRendererBounds) {
    if (aLabel instanceof JLabel) {
      JLabel label = (JLabel) aLabel;
      if (aLabel.getMinimumSize().getWidth() > aRendererBounds.getWidth()) {
        label.setToolTipText(label.getText());
      } else {
        label.setToolTipText(null);
      }
    }
  }

  @Override
  public boolean isCellEditable(int aRow, int aColumn) {
    return isLayerEditable() &&
           super.isCellEditable(aRow, aColumn) &&
           fEditorProvider.canProvideEditor(this, aRow, aColumn);
  }

  /*
   * Cells will only be editable in the tree table if the layer is editable.
   */
  private boolean isLayerEditable() {
    return fLayer != null ? fLayer.isEditableSupported() && fLayer.isEditable() : true;
  }

  @Override
  public TableCellEditor getCellEditor(int aRow, int aColumn) {
    if (!fEditorProvider.canProvideEditor(this, aRow, aColumn)) {
      return null;
    }
    return fEditorProvider.provideEditor(this, aRow, aColumn);
  }

  @Override
  public void removeEditor() {
    if (getCellEditor() instanceof ILcdDisposable) {
      ((ILcdDisposable) getCellEditor()).dispose();
    }
    super.removeEditor();
  }

  /**
   * This listener stops cell editing when certain changes to the tree model structure are detected.
   * This is necessary because the JXTreeTable does not do this automatically (the JXTable seems to
   * handle it much better). Especially if rows are removed or added, for instance by toggling the
   * hide empty values active settable, the editor will stay at the same row index. This in turn means
   * that en editor for a certain data property is suddenly associated with a different one, with all
   * consequences that entails (setting invalid values etc...).
   */
  private static class StopEditingListener implements TreeModelListener {

    private final WeakReference<EditableTreeTable> fTreeTable;

    public StopEditingListener(EditableTreeTable aTreeTable) {
      fTreeTable = new WeakReference<EditableTreeTable>(aTreeTable);
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
      stopCellEditing(e);
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
      stopCellEditing(e);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
      stopCellEditing(e);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
      stopCellEditing(e);
    }

    private void stopCellEditing(TreeModelEvent e) {
      EditableTreeTable treeTable = fTreeTable.get();
      if (treeTable == null) {
        ((TreeTableModel) e.getSource()).removeTreeModelListener(this);
      } else if (treeTable.getCellEditor() != null) {
        treeTable.getCellEditor().stopCellEditing();
      }
    }
  }

  void updatePreferredSize() {
    if (fRendererProvider != null) {
      packAll();
      setPreferredScrollableViewportSize(getPreferredSize());

      // Request the new size to the parent hierarchy. This needs for example OptionsPanelScrollPane to
      // work properly, so that the request reaches the parents beyond the scroll pane.
      revalidate();
    }
  }

  /**
   * Updates the preferred size of the given JXTreeTable to match the actual content.
   * JXTable's pack feature is used to calculate the best size based on the content, this in turn loops over all
   * cells and asks their cell renderer for their preferred size. The result is than stored using
   * setPreferredScrollableViewportSize, which is the size the panel would like to have when in a scroll pane.
   *
   * It uses a timer to coalesce many events from the tree model into a single update of the preferred size, for
   * performance reasons.
   */
  private static class PreferredSizeUpdater implements TreeModelListener, TreeExpansionListener {
    private final Timer fUpdateTimer = new Timer(50, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fTable.updatePreferredSize();
      }
    });
    private final EditableTreeTable fTable;

    private PreferredSizeUpdater(EditableTreeTable aTable) {
      fTable = aTable;
      fTable.addTreeExpansionListener(this);
      fUpdateTimer.setCoalesce(true);
      fUpdateTimer.setRepeats(false);
    }

    private void invalidate() {
      fUpdateTimer.restart();
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
      invalidate();
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
      invalidate();
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
      invalidate();
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
      invalidate();
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
      invalidate();
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
      invalidate();
    }
  }

  /**
   * Table cell renderer that delegates to the renderer provider to find the best cell renderer.
   */
  private class ValueColumnCellRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      TableCellRenderer renderer = fRendererProvider.provideRenderer(EditableTreeTable.this, row, column);
      if (renderer == null) {
        throw new IllegalStateException("Renderer provider returned null for row[" + row + "], column[" + column + "] and value[" + value + "]. There should always be a renderer!");
      }
      return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
}
