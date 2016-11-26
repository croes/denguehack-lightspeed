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
package samples.common.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdAssociationClassAnnotation;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103MeasureAnnotation;

/**
 * Allows {@link #iterateDataObject iterating} and {@link #searchDataObject filtering} the properties exposed by the {@link ILcdDataObject} interface.
 * The value-to-string conversion is trivial by default but can be {@link #convertToString(Object) overriden}.
 */
public class DataObjectSearchIterator {

  private static final String DATA_TYPE = "DataType";

  /**
   * Iterates recursively over the given object's data properties.
   * <ul>
   *   <li><b>Primitive properties:</b> in this case, the visitor is called with the value of the property.
   *   <li><b>{@code ILcdDataObject} properties:</b> the visitor is called once with {@code null}
   *       as value of the {@code ILcdDataObject} property, and subsequently called recursively.
   *   <li><b>Elements of a {@code List} or {@code Set} property:</b> the visitor is called
   *       only for the individual values in the collection.
   *       In such case, the value represents an individual value of the collection.
   *   <li><b>Associated properties:</b> the visitor is called with the associated properties as well.
   * </ul>
   *
   * @param aDataObject The object to iterate over
   * @param aCallback The visitor to call when iterating
   */
  public final void iterateDataObject(ILcdDataObject aDataObject, DataObjectPropertyVisitor aCallback) {
    iterateDataObject(aDataObject, aCallback, new HashSet<>(), new ArrayList<String>());
  }

  /**
   * Filters recursively over the given object's data properties.
   * The method is similar to {@link #iterateDataObject}, but matches property type names and property values with the
   * given pattern.
   *
   * @param aObject the object to search
   * @param aSearchPattern the pattern to match
   * @param aCallback The visitor to call when a match has been found
   */
  public final void searchDataObject(final ILcdDataObject aObject, final Pattern aSearchPattern, final DataObjectPropertyVisitor aCallback) {
    iterateDataObject(aObject, new DataObjectPropertyVisitor() {
      @Override
      public void visit(TLcdDataType aDataType, TLcdDataProperty aDataProperty, Object aValue, List<String> aPathToProperty) {
        String displayName = aDataType.getDisplayName();
        if (matchValue(aSearchPattern, displayName)) {
          String[] path = aPathToProperty.toArray(new String[aPathToProperty.size()]);
          path[path.length - 1] = DATA_TYPE;
          aCallback.visit(aDataType, aDataProperty, displayName, aPathToProperty);
          return;
        }
        String propertyName = aDataProperty.getDisplayName();
        if (matchValue(aSearchPattern, propertyName)) {
          aCallback.visit(aDataType, aDataProperty, aValue, aPathToProperty);
          return;
        }

        if (aDataProperty.getType().isEnumeration() && aValue != null) {
          try {
            aValue = aDataProperty.getType().getDisplayName(aValue);
          } catch (Exception e) {
            // There are certain data formats (e.g. KML) where they put values in the data model
            // which are not allowed according to type#getPossibleValues()
            // The getDisplayName method will throw an IllegalArgumentException in that case
            // Fall back to the default behavior in that case
          }
        }

        //Make sure ISO-annotated measurements have ISO values.
        if (aDataProperty.isAnnotationPresent(TLcdISO19103MeasureAnnotation.class) && aValue instanceof Number) {
          TLcdISO19103MeasureAnnotation annotation = aDataProperty.getAnnotation(TLcdISO19103MeasureAnnotation.class);
          aValue = new TLcdISO19103Measure(((Number) aValue).doubleValue(), annotation.getUnitOfMeasure());
        }

        if (matchValue(aSearchPattern, aValue)) {
          aCallback.visit(aDataType, aDataProperty, aValue, aPathToProperty);
        }
      }
    });
  }

  private boolean matchValue(Pattern aSearchPattern, Object aValue) {
    if (aValue == null) {
      return false;
    }
    Matcher matcher = aSearchPattern.matcher(convertToString(aValue));
    return matcher.find();
  }

  protected String convertToString(Object aValue) {
    return aValue.toString();
  }

  private static void iterateDataObject(ILcdDataObject aDataObject, DataObjectPropertyVisitor aCallback, Set<Object> aSeenObjects, List<String> aPropertyPath) {
    for (TLcdDataProperty property : aDataObject.getDataType().getProperties()) {
      aPropertyPath.add(property.getDisplayName());
      int index = aPropertyPath.size() - 1;
      try {
        Object value = aDataObject.getValue(property);
        handlePropertyValue(aCallback, property, value, aSeenObjects, aPropertyPath);
      } finally {
        aPropertyPath.remove(index);
      }
    }
  }

  private static void handlePropertyValue(DataObjectPropertyVisitor aCallback, TLcdDataProperty aDataProperty, Object aValue, Set<Object> aSeenObjects, List<String> aPropertyPath) {
    //avoid searching the same object twice
    //this also makes the code safe against loops in the data model
    if (aValue != null && aSeenObjects.contains(aValue)) {
      return;
    }

    //visiting the same object twice does not harm
    //we just want to avoid that while iterating through a certain property, we end up in an infinite loop
    //therefore, we make a clone of the set.
    aSeenObjects = new HashSet<>(aSeenObjects);

    if (aDataProperty.getType().isAnnotationPresent(TLcdAssociationClassAnnotation.class)) {
      aCallback.visit(aDataProperty.getType(), aDataProperty, null, aPropertyPath);
      TLcdAssociationClassAnnotation annotation = aDataProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class);
      TLcdDataProperty roleProperty = annotation.getRoleProperty();
      aPropertyPath.add(roleProperty.getDisplayName());
      int index = aPropertyPath.size() - 1;
      try {
        handlePropertyValue(aCallback, roleProperty, aValue, aSeenObjects, aPropertyPath);
      } finally {
        aPropertyPath.remove(index);
      }
    } else if (aValue instanceof ILcdDataObject) {
      aSeenObjects.add(aValue);
      aCallback.visit(aDataProperty.getType(), aDataProperty, null, aPropertyPath);
      iterateDataObject((ILcdDataObject) aValue, aCallback, aSeenObjects, aPropertyPath);
    } else if (aValue instanceof List) {
      aCallback.visit(aDataProperty.getType(), aDataProperty, null, aPropertyPath);
      for (Object listElement : (List) aValue) {
        handlePropertyValue(aCallback, aDataProperty, listElement, aSeenObjects, aPropertyPath);
      }
    } else if (aValue instanceof Set) {
      aCallback.visit(aDataProperty.getType(), aDataProperty, null, aPropertyPath);
      for (Object setElement : (Set) aValue) {
        handlePropertyValue(aCallback, aDataProperty, setElement, aSeenObjects, aPropertyPath);
      }
    } else {
      aSeenObjects.add(aValue);
      aCallback.visit(aDataProperty.getType(), aDataProperty, aValue, aPropertyPath);
    }
  }

  public interface DataObjectPropertyVisitor {
    void visit(TLcdDataType aDataType, TLcdDataProperty aDataProperty, Object aValue, List<String> aPathToProperty);
  }
}
