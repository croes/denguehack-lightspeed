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
package samples.fusion.client.metadata.panel;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for custom ISO metadata properties.
 */
public class MetadataTableModel extends AbstractTableModel {

  public static final String RESOURCE_NAME = "Resource Name";

  public static final String CITATION_TITLE = "ISO 19115 - Identification Title";

  public static final String CITATION_SERIES_NAME = "ISO 19115 - Identification Series Name";

  public static final String SECURITY_CLASSIFICATION = "ISO 19115 - Security Classification";

  private final ArrayList<TableProperty> fProperties = new ArrayList<>();

  private final String[] fColumnNames = new String[]{"Property", "Value"};

  public static final int RESOURCE_NAME_ROW = 0;

  public static final int CITATION_TITLE_ROW = 1;

  public static final int CITATION_SERIES_NAME_ROW = 2;

  public static final int SECURITY_CLASSIFICATION_ROW = 3;

  private boolean fEnabled = false;

  public MetadataTableModel() {
    insertRow(MetadataTableModel.RESOURCE_NAME_ROW, new Object[]{MetadataTableModel.RESOURCE_NAME, null});
    insertRow(MetadataTableModel.CITATION_TITLE_ROW, new Object[]{MetadataTableModel.CITATION_TITLE, null});
    insertRow(MetadataTableModel.CITATION_SERIES_NAME_ROW, new Object[]{MetadataTableModel.CITATION_SERIES_NAME, null});
    insertRow(MetadataTableModel.SECURITY_CLASSIFICATION_ROW,
              new Object[]{MetadataTableModel.SECURITY_CLASSIFICATION, null});
  }

  public void reset() {
    for (int i = 0; i < getRowCount(); i++) {
      setValueAt(null, i, 1);
    }
  }

  public void insertRow(int aRow, Object[] aRowData) {
    fProperties.add(aRow, new TableProperty((String) aRowData[0], (String) aRowData[1]));
  }

  @Override
  public void setValueAt(Object aValue, int aRow, int aColumn) {
    if (aColumn == 1) {
      fProperties.get(aRow).setValue(aColumn, (String) aValue);
      fireTableCellUpdated(aRow, aColumn);
    }
  }

  @Override
  public boolean isCellEditable(int aRow, int aColumn) {
    return aColumn == 1;
  }

  @Override
  public String getColumnName(int aColumn) {
    return fColumnNames[aColumn];
  }

  public int getRowCount() {
    return (fEnabled && fProperties != null ? fProperties.size() : 0);
  }

  public int getColumnCount() {
    return 2;
  }

  public Object getValueAt(int aRowIndex, int aColumnIndex) {
    return fProperties.get(aRowIndex).getValue(aColumnIndex);
  }

  public String getValue(int rowIndex) {
    return fProperties.get(rowIndex).getValue(1);
  }

  public void setEnabled(boolean aEnabled) {
    fEnabled = aEnabled;
    fireTableDataChanged();
  }

  /**
   * A representation of a property row with 2 columns: the name and the value.
   */
  private static class TableProperty {

    private String fPropertyName;
    private String fPropertyValue;

    public TableProperty(String aPropertyName, String aValue) {
      fPropertyName = aPropertyName;
      fPropertyValue = aValue;
    }

    public String getValue(int aIndex) {
      if (aIndex == 0) {
        return fPropertyName;
      }
      if (aIndex == 1) {
        return fPropertyValue;
      }
      return null;
    }

    public void setValue(int aIndex, String aValue) {
      if (aIndex == 0) {
        fPropertyName = aValue;
      }
      if (aIndex == 1) {
        fPropertyValue = aValue;
      }
    }
  }
}
