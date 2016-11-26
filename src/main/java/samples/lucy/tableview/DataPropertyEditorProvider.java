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

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import samples.lucy.treetableview.ExpressionUtility;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDataPropertyValueContext;

/**
 * Implementation of <code>ITableCellEditorProvider</code> that can create editors for columns with
 * a <code>TLcdDataProperty[]</code> as column descriptor, provided that a customizer panel can be
 * created by the lucy composite customizer panel factory.
 */
class DataPropertyEditorProvider implements ITableCellEditorProvider {

  private final ILcyLucyEnv fLucyEnv;
  private final TLcyModelContext fModelContext;

  public DataPropertyEditorProvider(ILcyLucyEnv aLucyEnv, TLcyModelContext aModelContext) {
    if (aLucyEnv == null) {
      throw new IllegalArgumentException("The Lucy environment can't be null!");
    }
    if (aModelContext == null) {
      throw new IllegalArgumentException("The model context can't be null!");
    }
    fLucyEnv = aLucyEnv;
    fModelContext = aModelContext;
  }

  @Override
  public boolean canProvideEditor(JTable aTable, int aRow, int aColumn) {
    if (!(aTable.getModel() instanceof IExtendedTableModel)) {
      return false;
    }
    if (!isLayerEditable()) {
      return false;
    }
    IExtendedTableModel tableModel = (IExtendedTableModel) aTable.getModel();
    TLcdDataProperty[] dataProperties = getDataProperties(aTable, tableModel, aColumn);
    TLcdDataPropertyValueContext propertyValue = createPropertyValue(aTable, tableModel, aRow, dataProperties);
    return propertyValue != null &&
           new TLcyCompositeCustomizerPanelFactory(fLucyEnv).canCreateCustomizerPanel(propertyValue);
  }

  @Override
  public TableCellEditor provideEditor(JTable aTable, int aRow, int aColumn) {
    if (!canProvideEditor(aTable, aRow, aColumn)) {
      throw new IllegalArgumentException("Can't provide an editor, call canProvideEditor first!");
    }
    IExtendedTableModel tableModel = (IExtendedTableModel) aTable.getModel();
    TLcdDataProperty[] dataProperties = getDataProperties(aTable, tableModel, aColumn);
    TLcdDataPropertyValueContext propertyValue = createPropertyValue(aTable, tableModel, aRow, dataProperties);
    ILcyCustomizerPanel customizerPanel = new TLcyCompositeCustomizerPanelFactory(fLucyEnv).createCustomizerPanel(propertyValue);
    customizerPanel.setObject(propertyValue);
    return new TableEditorCustomizerPanelAdapter(customizerPanel, fModelContext.getLayer(),
                                                 dataProperties[dataProperties.length - 1]);
  }

  private boolean isLayerEditable() {
    return fModelContext.getLayer() != null ?
           fModelContext.getLayer().isEditableSupported() && fModelContext.getLayer().isEditable() :
           true;
  }

  private TLcdDataPropertyValueContext createPropertyValue(JTable aTable, IExtendedTableModel aTableModel,
                                                           int aRow, TLcdDataProperty[] aDataProperties) {
    if (aDataProperties == null || aDataProperties.length == 0) {
      return null;
    }
    ILcdDataObject dataObject = getDataObject(aTable, aTableModel, aRow);
    if (dataObject == null) {
      return null;
    }
    ILcdLayer layer = fModelContext.getLayer();

    ILcdView view = fModelContext.getView();
    ILcdIntegerIndexedModel model = aTableModel.getOriginalModel();

    //We use expressions to retrieve the value.
    String expression = ExpressionUtility.createExpression(dataObject.getDataType(), aDataProperties);
    Object value = ExpressionUtility.retrieveValue(dataObject, expression);
    return new TLcdDataPropertyValueContext(
        value, expression, dataObject, model, layer, view);
  }

  private ILcdDataObject getDataObject(JTable aTable, IExtendedTableModel aTableModel, int aRow) {
    int index = aTable.convertRowIndexToModel(aRow);
    Object object = aTableModel.getObjectAtRow(index);
    if (object instanceof ILcdDataObject) {
      return (ILcdDataObject) object;
    } else {
      return null;
    }
  }

  private TLcdDataProperty[] getDataProperties(JTable aTable, IExtendedTableModel aTableModel, int aColumn) {
    int index = aTable.convertColumnIndexToModel(aColumn);
    Object columnDescriptor = aTableModel.getColumnDescriptor(index);
    if (columnDescriptor instanceof TLcdDataProperty[]) {
      return (TLcdDataProperty[]) columnDescriptor;
    }
    return null;
  }
}
