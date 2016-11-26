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
package samples.gxy.swingTable;

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdIntegerIndexedModel;

/**
 * This is an implementation of a swing TableModel for displaying, in a JTable,
 * Objects contained an ILcdModel that implement ILcdDataObject, i.e.<!-- --> Objects with
 * a data type with a set of properties associated to them.<p/>
 */
public class DataObjectTableModel extends AbstractTableModel implements TableModel {
  ILcdIntegerIndexedModel fModel;
  ILcdDataModelDescriptor fDataModelDescriptor;

  /**
   * Creates a new <code>DataObjectTableModel</code> with the given argument.
   * @param aModel An integer indexed model that must have an <code>ILcdDataModelDescriptor</code>
   *               with exactly one model element type.
   */
  public DataObjectTableModel(ILcdIntegerIndexedModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor)) {
      throw new IllegalArgumentException("Given model is not supported because it does not have a model descriptor of type ILcdDataModelDescriptor");
    }
    if (((ILcdDataModelDescriptor) aModel.getModelDescriptor()).getModelElementTypes().size() != 1) {
      throw new IllegalArgumentException("Given model must have an ILcdDataModelDescriptor with exactly 1 model element type to be able to show it in a table view.");
    }

    fModel = aModel;
    fDataModelDescriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();

  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    Object object = fModel.elementAt(rowIndex);
    ILcdDataObject dataObject;
    Object value = null;
    if (object instanceof ILcdDataObject) {
      dataObject = (ILcdDataObject) object;
      value = dataObject.getValue(dataObject.getDataType().getProperties().get(columnIndex));
    }
    return value;
  }

  public String getColumnName(int aColumn) {
    return getDataModelProperties().get(aColumn).getDisplayName();
  }

  public int getRowCount() {
    return fModel.size();
  }

  public int getColumnCount() {
    return getDataModelProperties().size();
  }

  private List<TLcdDataProperty> getDataModelProperties() {
    //We know the data model descriptor has exactly one model element type
    //This was checked at construction
    return fDataModelDescriptor.getModelElementTypes().iterator().next().getProperties();
  }
}




