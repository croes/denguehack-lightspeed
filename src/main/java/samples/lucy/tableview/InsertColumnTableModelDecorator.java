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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>Implementation of TableModel that follows the decorator design pattern to add a
 * column to a given delegate TableModel.</p>
 */
abstract class InsertColumnTableModelDecorator extends AbstractTableModel implements IExtendedTableModel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(InsertColumnTableModelDecorator.class.getName());
  private IExtendedTableModel fDelegateTableModel;
  private int fNewColumnIndex = 0;

  public InsertColumnTableModelDecorator(IExtendedTableModel aDelegateTableModel, int aNewColumnIndex) {
    fDelegateTableModel = aDelegateTableModel;
    fNewColumnIndex = aNewColumnIndex;

    //convert the events
    fDelegateTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        InsertColumnTableModelDecorator.this.fireTableChanged(new TableModelEvent(
            InsertColumnTableModelDecorator.this,
            e.getFirstRow(),
            e.getLastRow(),
            delegateColumnIndexToColumnIndex(e.getColumn()),
            e.getType()));
      }
    });
  }

  public abstract String getNewColumnName();

  public abstract Class getNewColumnClass();

  public abstract boolean isNewColumnEditable(int aRowIndex);

  public abstract Object getNewColumnValueAt(int aRowIndex);

  public abstract void setNewColumnValueAt(Object aNewValue, int aRowIndex, int aColumnIndex);

  public abstract Object getNewColumnDescriptor();

  public abstract String getNewColumnTooltipText();

  public int getNewColumnIndex() {
    return fNewColumnIndex;
  }

  public void setNewColumnIndex(int aNewColumnIndex) {
    if (aNewColumnIndex != fNewColumnIndex) {
      fNewColumnIndex = aNewColumnIndex;
      fireTableStructureChanged();
    }
  }

  @Override
  public Object getObjectAtRow(int aRowIndex) {
    return fDelegateTableModel.getObjectAtRow(aRowIndex);
  }

  @Override
  public int getRowOfObject(Object aDomainObject) {
    return fDelegateTableModel.getRowOfObject(aDomainObject);
  }

  @Override
  public int getRowCount() {
    return fDelegateTableModel.getRowCount();
  }

  @Override
  public int getColumnCount() {
    if (fNewColumnIndex >= 0) {
      return fDelegateTableModel.getColumnCount() + 1;
    } else {
      return fDelegateTableModel.getColumnCount();
    }
  }

  private int columnIndexToDelegateColumnIndex(int aColumnIndex) {
    if (fNewColumnIndex >= 0) {
      if (aColumnIndex < fNewColumnIndex) {
        return aColumnIndex;
      } else if (aColumnIndex > fNewColumnIndex) {
        return aColumnIndex - 1;
      } else {
        if (sLogger.isDebugEnabled()) {
          sLogger.debug("columnIndexToDelegateColumnIndex: Have to convert index that does not exist in target");
        }
        return aColumnIndex;
      }
    } else {
      return aColumnIndex;
    }
  }

  private int delegateColumnIndexToColumnIndex(int aColumnIndex) {
    if (aColumnIndex != TableModelEvent.ALL_COLUMNS && fNewColumnIndex >= 0) {
      if (aColumnIndex >= fNewColumnIndex) {
        return aColumnIndex + 1;
      } else {
        return aColumnIndex;
      }
    } else {
      return aColumnIndex;
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    if (columnIndex == fNewColumnIndex) {
      return getNewColumnName();
    } else {
      return fDelegateTableModel.getColumnName(columnIndexToDelegateColumnIndex(columnIndex));
    }
  }

  @Override
  public Class getColumnClass(int columnIndex) {
    if (columnIndex == fNewColumnIndex) {
      return getNewColumnClass();
    } else {
      return fDelegateTableModel.getColumnClass(columnIndexToDelegateColumnIndex(columnIndex));
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == fNewColumnIndex) {
      return isNewColumnEditable(rowIndex);
    } else {
      return fDelegateTableModel.isCellEditable(rowIndex, columnIndexToDelegateColumnIndex(columnIndex));
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == fNewColumnIndex) {
      return getNewColumnValueAt(rowIndex);
    } else {
      return fDelegateTableModel.getValueAt(rowIndex, columnIndexToDelegateColumnIndex(columnIndex));
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == fNewColumnIndex) {
      setNewColumnValueAt(aValue, rowIndex, columnIndex);
    } else {
      fDelegateTableModel.setValueAt(aValue, rowIndex, columnIndexToDelegateColumnIndex(columnIndex));
    }
  }

  @Override
  public Object getColumnDescriptor(int aColumnIndex) {
    if (aColumnIndex == fNewColumnIndex) {
      return getNewColumnDescriptor();
    } else {
      return fDelegateTableModel.getColumnDescriptor(columnIndexToDelegateColumnIndex(aColumnIndex));
    }
  }

  @Override
  public String getColumnTooltipText(int aColumnIndex) {
    if (aColumnIndex == fNewColumnIndex) {
      return getNewColumnTooltipText();
    } else {
      return fDelegateTableModel.getColumnTooltipText(columnIndexToDelegateColumnIndex(aColumnIndex));
    }
  }
}
