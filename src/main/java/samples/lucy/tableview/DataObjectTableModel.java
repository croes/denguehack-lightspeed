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

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.ILcdUndoableSource;
import com.luciad.gui.TLcdUndoSupport;
import samples.lucy.treetableview.DataObjectNodeContext;
import samples.lucy.treetableview.ExpressionUtility;
import samples.lucy.undo.DataPropertyValueUndoable;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103MeasureAnnotation;

/**
 * <p>This is an implementation of a swing TableModel for displaying, in a JTable, Objects contained in
 * an ILcdIntegerIndexedModel that implement ILcdDataObject, i.e. Objects with a set of properties
 * associated to them.</p>
 *
 * <p>This table model will sync itself with the {@code ILcdModel} for which it was constructed. When
 * the {@code ILcdModel} is adjusted, the table model will automatically be adjusted and fire the
 * necessary events to warn the {@code JTable} it has been updated.</p>
 */
class DataObjectTableModel extends LuciadModelWrapper implements ILcdUndoableSource {


  /**
   * Verify whether a {@code DataObjectTableModel} can be created for {@code aModel}
   *
   * @param aModel The model
   *
   * @return {@code true} when a {@code DataObjectTableModel} can be created for
   *         {@code aModel}, {@code false} otherwise.
   */
  public static boolean acceptsDataObjectModel(ILcdModel aModel) {
    if (aModel != null) {
      aModel = retrieveModelForTableView(aModel);
    }
    if (aModel == null) {
      return false;
    }
    if (!(aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor) ||
        !(LuciadModelWrapper.acceptsModel(aModel))) {
      return false;
    }
    Set<TLcdDataType> dataTypes = ((ILcdDataModelDescriptor) aModel.getModelDescriptor()).getModelElementTypes();
    return dataTypes != null && dataTypes.size() == 1;
  }

  private final TLcdUndoSupport fUndoSupport = new TLcdUndoSupport(this);
  private final List<TLcdDataProperty[]> fPropertyMapping;

  /**
   * Creates a new {@code DataObjectTableModel} for the given {@code ILcdIntegerIndexedModel}.
   *
   * @param aModel The model to create the table model for. The mode must pass the {@link
   *               #acceptsDataObjectModel(ILcdModel)} check.
   */
  public DataObjectTableModel(ILcdModel aModel) {
    super(aModel);
    if (!acceptsDataObjectModel(aModel)) {
      throw new IllegalArgumentException("No DataObjectTableModel can be constructed for [" + aModel + "].");
    }
    fPropertyMapping = createPropertyMapping(getOriginalModel());
  }

  /**
   * <p>Method creates a mapping from column indexes to properties. </p>
   *
   * @param aModel A model which passes the {@link #acceptsDataObjectModel(ILcdModel)}
   *               check.
   *
   * @return Returns a list that maps its values to a property
   *
   * @see #acceptsDataObjectModel(ILcdModel)
   */
  private List<TLcdDataProperty[]> createPropertyMapping(ILcdIntegerIndexedModel aModel) {
    Set<TLcdDataType> dataTypes = ((ILcdDataModelDescriptor) aModel.getModelDescriptor()).getModelElementTypes();
    TLcdDataType dataType = dataTypes.iterator().next();
    List<TLcdDataProperty> propertiesList = dataType.getProperties();
    ArrayList<TLcdDataProperty[]> propertyMapping = new ArrayList<TLcdDataProperty[]>();
    ArrayList<TLcdDataProperty> parentProperties = new ArrayList<TLcdDataProperty>();
    Set<TLcdDataProperty> includedProperties = new HashSet<TLcdDataProperty>();
    for (TLcdDataProperty property : propertiesList) {
      add(property, propertyMapping, parentProperties, includedProperties);
    }
    moveNonNestedDataPropertiesToFront(propertyMapping);
    return propertyMapping;
  }

  private void moveNonNestedDataPropertiesToFront(List<TLcdDataProperty[]> aPropertyMappingSFCT) {
    final List<TLcdDataProperty[]> originalPropertyMapping = new ArrayList<TLcdDataProperty[]>(aPropertyMappingSFCT);
    Collections.sort(aPropertyMappingSFCT, new Comparator<TLcdDataProperty[]>() {
      @Override
      public int compare(TLcdDataProperty[] aFirst, TLcdDataProperty[] aSecond) {
        if (aFirst.length == 1 || aSecond.length == 1) {
          if (aFirst.length == 1 && aSecond.length == 1) {
            return originalPropertyMapping.indexOf(aFirst) - originalPropertyMapping.indexOf(aSecond);
          } else if (aFirst.length == 1) {
            return -1;
          }
          return 1;
        }
        return originalPropertyMapping.indexOf(aFirst) - originalPropertyMapping.indexOf(aSecond);
      }
    });
  }

  /*
   * We add properties of complex typed values in a recursive manner.
   */
  private void add(TLcdDataProperty aProperty,
                   ArrayList<TLcdDataProperty[]> aPropertyMapping,
                   ArrayList<TLcdDataProperty> aParentProperties,
                   Set<TLcdDataProperty> aIncludedProperties) {
    if (aParentProperties.contains(aProperty)) {
      //cyclical dependency, we stop to avoid an endless loop.
      return;
    }
    if (aIncludedProperties.contains(aProperty)) {
      //avoid including the same property multiple times
      //this can be the case when different types have the same property
//      return;
    }
    if (aProperty.isCollection()) {
      //We can't handle collection properties in the table view.
      return;
    }
    TLcdDataType type = aProperty.getType();
    if (type.getProperties().isEmpty()) {
      if (shouldAddToModel(aProperty)) {
        ArrayList<TLcdDataProperty> properties = new ArrayList<TLcdDataProperty>(aParentProperties);
        properties.add(aProperty);
        aPropertyMapping.add(properties.toArray(new TLcdDataProperty[properties.size()]));
        aIncludedProperties.add(aProperty);
      }
    } else {
      for (TLcdDataProperty property : type.getProperties()) {
        ArrayList<TLcdDataProperty> parents = new ArrayList<TLcdDataProperty>(aParentProperties);
        parents.add(aProperty);
        add(property, aPropertyMapping, parents, aIncludedProperties);
      }
    }
  }

  /**
   * Returns true if given property is primitive (if it can easily be displayed in a table)
   *
   * @param aProperty a property to test
   *
   * @return true if the property is primitive, false otherwise.
   */
  private boolean shouldAddToModel(TLcdDataProperty aProperty) {
    Class<?> instanceClass = aProperty.getType().getInstanceClass();
    //check whether it is a primitive type, which can easily be displayed in the table
    boolean primitive = aProperty.getType().isPrimitive() && !ILcdShape.class.isAssignableFrom(instanceClass);
    //allow points as well
    boolean point = ILcdPoint.class.isAssignableFrom(instanceClass);
    //also accept TLcdISO19103Measure objects
    boolean annotation = aProperty.isAnnotationPresent(TLcdISO19103MeasureAnnotation.class);
    return primitive || point || annotation || ILcdISO19103Measure.class.isAssignableFrom(instanceClass);
  }

  @Override
  public Object getValueAt(int aRowIndex, int aColumnIndex) {
    Object object = getObjectAtRow(aRowIndex);
    TLcdDataProperty[] dataProperties = getColumnDescriptor(aColumnIndex);
    return ExpressionUtility.retrieveValue((ILcdDataObject) object, dataProperties);
  }

  @Override
  public String getColumnName(int aColumn) {
    return DataObjectNodeContext.getDisplayName(fPropertyMapping.get(aColumn));
  }

  @Override
  public Class getColumnClass(int aColumnIndex) {
    TLcdDataProperty[] props = fPropertyMapping.get(aColumnIndex);
    TLcdDataProperty dataProperty = props[props.length - 1];
    return dataProperty.getType().getInstanceClass();
  }

  @Override
  public int getColumnCount() {
    return fPropertyMapping.size();
  }

  @Override
  public TLcdDataProperty[] getColumnDescriptor(int aColumnIndex) {
    return fPropertyMapping.get(aColumnIndex);
  }

  @Override
  public String getColumnTooltipText(int aColumnIndex) {
    return DataObjectNodeContext.getTooltip(getColumnDescriptor(aColumnIndex));
  }

  @Override
  public boolean isCellEditable(int aRowIndex, int aColumnIndex) {
    Object object = getObjectAtRow(aRowIndex);
    TLcdDataProperty[] properties = getColumnDescriptor(aColumnIndex);
    return canSetValue(object, properties);
  }

  private boolean canSetValue(Object aObject, TLcdDataProperty[] aProperties) {
    if (aObject instanceof ILcdDataObject &&
        aProperties.length > 0) {
      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      for (int i = 0; i < aProperties.length - 1; i++) {
        Object value = dataObject.getValue(aProperties[i]);
        if (value instanceof ILcdDataObject) {
          dataObject = (ILcdDataObject) value;
        } else {
          //somewhere in the chain is an invalid object (not a data object or null).
          return false;
        }
      }
      //each but the last property has an non null ILcdDataObject value.
      return true;
    }
    return false;
  }

  @Override
  public void setValueAt(Object aValue, int aRowIndex, int aColumnIndex) {
    if (isCellEditable(aRowIndex, aColumnIndex)) {
      ILcdModel model = getOriginalModel();
      ILcdDataObject dataObject = (ILcdDataObject) getObjectAtRow(aRowIndex);
      TLcdDataProperty[] dataProperties = getColumnDescriptor(aColumnIndex);
      setValueInDataObject(aValue, model, dataObject, dataProperties);
    }
  }

  private void setValueInDataObject(Object aValue, ILcdModel aModel,
                                    ILcdDataObject aDataObject, TLcdDataProperty[] aDataProperties) {
    String expression = ExpressionUtility.createExpression(aDataObject.getDataType(), aDataProperties);
    Object oldValue = ExpressionUtility.retrieveValue(aDataObject, expression);
    if (!equal(oldValue, aValue)) {
      try (Lock autoUnlock = writeLock(aModel)) {
        ExpressionUtility.updateValue(aDataObject, expression, aValue);
      }
      fireUndoableHappened(aDataObject, expression, oldValue, aValue);
      aModel.elementChanged(aDataObject, ILcdModel.FIRE_NOW);
    }
  }

  /*
   * This makes sure no redundant events are thrown.
   */
  private boolean equal(Object aOldValue, Object aValue) {
    if (aOldValue == null) {
      return aValue == null;
    }
    return aOldValue.equals(aValue);
  }

  @Override
  public void addUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.addUndoableListener(aUndoableListener);
  }

  @Override
  public void removeUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.removeUndoableListener(aUndoableListener);
  }

  private void fireUndoableHappened(ILcdDataObject aDataObject, String aExpression,
                                    Object aOldValue, Object aNewValue) {
    fUndoSupport.fireUndoableHappened(new DataPropertyValueUndoable(getOriginalModel(),
                                                                    aDataObject, aExpression,
                                                                    aOldValue, aNewValue));
  }

}
