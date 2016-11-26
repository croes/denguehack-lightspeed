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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import samples.decoder.ecdis.common.filter.columns.ARowObjectTableColumn;

/**
 * Table model containing object classes. This model implementation allows configuring the visible object classes on a
 * {@link TLcdS52DisplaySettings S-52 display settings instance}.
 */
class RowObjectTableModel extends AbstractTableModel {

  private final TableColumnDescriptor fColumnDescriptor = new TableColumnDescriptor();
  private final List fRowObjects;

  /**
   * Creates a new instance.
   *
   * @param aRowObjects the row objects
   */
  public RowObjectTableModel(List aRowObjects) {
    fRowObjects = aRowObjects;
  }

  @Override
  public String getColumnName(int aColumnIndex) {
    return fColumnDescriptor.getColumnName(aColumnIndex);
  }

  @Override
  public Class<?> getColumnClass(int aColumnIndex) {
    return fColumnDescriptor.getColumnClass(aColumnIndex);
  }

  @Override
  public boolean isCellEditable(int aRowIndex, int aColumnIndex) {
    return aColumnIndex == 0;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    Object objectClass = fRowObjects.get(rowIndex);
    fColumnDescriptor.setValue(aValue, objectClass, columnIndex);
    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  @Override
  public int getRowCount() {
    return fRowObjects.size();
  }

  @Override
  public int getColumnCount() {
    return fColumnDescriptor.getColumnCount();
  }

  @Override
  public Object getValueAt(int aRowIndex, int aColumnIndex) {
    Object rowObject = fRowObjects.get(aRowIndex);
    return fColumnDescriptor.getColumnValue(aColumnIndex, rowObject);
  }

  public Object getRowObject(int aRowIndex) {
    if (0 <= aRowIndex && aRowIndex < fRowObjects.size()) {
      return fRowObjects.get(aRowIndex);
    }

    return null;
  }

  private void addColumn(ARowObjectTableColumn aColumn) {
    fColumnDescriptor.addColumn(aColumn);
  }

  private static final class TableColumnDescriptor {
    private List<ARowObjectTableColumn> columns = new ArrayList<>(10);

    public int getColumnCount() {
      return columns.size();
    }

    public String getColumnName(int aColumnIndex) {
      return columns.get(aColumnIndex).getName();
    }

    public Object getColumnValue(int aColumnIndex, Object aRowObject) {
      return columns.get(aColumnIndex).getColumnValue(aRowObject);
    }

    public Class getColumnClass(int aColumnIndex) {
      return columns.get(aColumnIndex).getColumnClass();
    }

    public void addColumn(ARowObjectTableColumn aColumn) {
      columns.add(aColumn);
    }

    public void setValue(Object aValue, Object aRowObject, int aColumnIndex) {
      columns.get(aColumnIndex).setColumnValue(aValue, aRowObject);
    }
  }

  /**
   * Builder that builds the table model. This builder class allows defining the available columns in the table using
   * the resulting table model.
   */
  public static class Builder {

    private RowObjectTableModel fTableModel;

    public static Builder newBuilder(List aData) {
      return new Builder(new RowObjectTableModel(aData));
    }

    private Builder(RowObjectTableModel aRowObjectTableModel) {
      fTableModel = aRowObjectTableModel;
    }

    public Builder column(ARowObjectTableColumn aColumn) {
      fTableModel.addColumn(aColumn);
      return this;
    }

    public RowObjectTableModel build() {
      return fTableModel;
    }
  }
}
