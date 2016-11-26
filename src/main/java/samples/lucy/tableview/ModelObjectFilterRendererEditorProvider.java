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

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Editor/renderer provider for the visibility column in the table view.
 *
 * @since 2013.0
 */
class ModelObjectFilterRendererEditorProvider implements ITableCellEditorProvider, ITableCellRendererProvider {

  private final ModelObjectFilterTableCellRendererEditor fRendererEditor;

  public ModelObjectFilterRendererEditorProvider(ModelObjectFilterTableCellRendererEditor aRendererEditor) {
    fRendererEditor = aRendererEditor;
  }

  @Override
  public boolean canProvideEditor(JTable aTable, int aRow, int aColumn) {
    return canProvideFor(aTable, aColumn);
  }

  @Override
  public TableCellEditor provideEditor(JTable aTable, int aRow, int aColumn) {
    if (!canProvideEditor(aTable, aRow, aColumn)) {
      throw new IllegalArgumentException("Can't create a editor for this cell. Check with canProvideEditor first.");
    }
    return fRendererEditor;
  }

  @Override
  public boolean canProvideRenderer(JTable aTable, int aRow, int aColumn) {
    return canProvideFor(aTable, aColumn);
  }

  @Override
  public TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn) {
    if (!canProvideRenderer(aTable, aRow, aColumn)) {
      throw new IllegalArgumentException("Can't create a renderer for this cell. Check with canProvideEditor first.");
    }
    return fRendererEditor;
  }

  private boolean canProvideFor(JTable aTable, int aColumn) {
    if (!(aTable.getModel() instanceof IExtendedTableModel)) {
      return false;
    }
    IExtendedTableModel tableModel = (IExtendedTableModel) aTable.getModel();
    return tableModel.getColumnDescriptor(aTable.convertColumnIndexToModel(aColumn)) ==
           ModelFilterTableModelDecorator.VISIBILITY_COLUMN_DESCRIPTOR;
  }
}
