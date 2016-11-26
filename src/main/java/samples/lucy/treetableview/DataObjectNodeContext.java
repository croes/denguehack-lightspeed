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

import java.util.Objects;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;

/**
 * A class to encapsulate an object in a data model, together with its
 * data type and its property.  The object doesn't necessarily have to
 * be an {@code ILcdDataObject}.
 */
public class DataObjectNodeContext {
  private final Object fObject;
  private final TLcdDataType fDataType;
  private final TLcdDataProperty fDataProperty;
  private final String fPropertyDisplayName;
  private final TLcdDataProperty fAssociationProperty;

  /**
   * Create a new {@code DataObjectNodeContext}, representing the value of a certain
   * property of an {@code ILcdDataObject}
   *
   * @param aPropertyValue The value of the property
   * @param aDataType The data type of the property
   * @param aDataProperty The property corresponding to this {@code DataObjectNodeContext}
   */
  public DataObjectNodeContext(Object aPropertyValue, TLcdDataType aDataType, TLcdDataProperty aDataProperty) {
    this(aPropertyValue, aDataType, aDataProperty, null);
  }

  /**
   * Create a new {@code DataObjectNodeContext}
   *
   * @param aObject The object contained in the {@code ILcdModel}
   * @param aDataType The data type of the object
   * @param aDataProperty The property corresponding to this
   *          {@code DataObjectNodeContext}
   * @param aPropertyDisplayName The name that is to be used for this node.
   */
  public DataObjectNodeContext(Object aObject, TLcdDataType aDataType, TLcdDataProperty aDataProperty, String aPropertyDisplayName) {
    this(aObject, aDataType, aDataProperty, aPropertyDisplayName, null);
  }

  /**
   * Create a new {@code DataObjectNodeContext} representing the value of an associated property
   *
   * @param aAssociatedPropertyValue The value of the associated property
   * @param aDataType The data type of the associated property
   * @param aDataProperty The property of the associated property
   * @param aPropertyDisplayName The name that is to be used for this node.
   * @param aAssociationProperty the association property if present (the property on which the
   *                             {@link com.luciad.datamodel.TLcdAssociationClassAnnotation} is present.
   */
  public DataObjectNodeContext(Object aAssociatedPropertyValue, TLcdDataType aDataType, TLcdDataProperty aDataProperty, String aPropertyDisplayName, TLcdDataProperty aAssociationProperty) {
    fObject = aAssociatedPropertyValue;
    fDataType = aDataType;
    fDataProperty = aDataProperty;
    fPropertyDisplayName = aPropertyDisplayName;
    fAssociationProperty = aAssociationProperty;
  }

  /**
   * Returns the object represented by this node. This can be one of the following
   * <ul>
   *   <li>The value of a property of an {@code ILcdDataObject}. In this case the
   *   {@link #getDataType()} represents the type of the property and the {@link #getDataProperty()}
   *   the property.</li>
   *   <li>The value of the associated property of an {@code ILcdDataObject}. In this case the
   *   {@link #getDataType()} represents the type of the associated property and the
   *   {@link #getDataProperty()} the property. The {@link #getAssociationProperty()} represents
   *   the original property on which the {@link com.luciad.datamodel.TLcdAssociationClassAnnotation}
   *   annotation was present.</li>
   * </ul>
   * @return the object represented by this node
   */
  public Object getObject() {
    return fObject;
  }

  public TLcdDataType getDataType() {
    return fDataType;
  }

  public TLcdDataProperty getDataProperty() {
    return fDataProperty;
  }

  public TLcdDataProperty getAssociationProperty() {
    return fAssociationProperty;
  }

  public String getTypeDisplayName() {
    if (fObject instanceof ILcdDataObject) {
      return ((ILcdDataObject) fObject).getDataType().getDisplayName();
    } else {
      return fDataType.getDisplayName();
    }
  }

  public String getPropertyDisplayName() {
    if (fPropertyDisplayName != null) {
      return fPropertyDisplayName;
    } else {
      return fDataProperty.getDisplayName();
    }
  }

  /*
   * Returns the display name of the leaf property.
   */
  public static String getDisplayName(TLcdDataProperty[] aDataProperties) {
    if (aDataProperties.length == 0) {
      return "";
    } else {
      return aDataProperties[aDataProperties.length - 1].getDisplayName();
    }
  }

  /*
   * Returns the display names of all the properties in the path.
   */
  public static String getTooltip(TLcdDataProperty[] aDataProperties) {
    if (aDataProperties.length == 0) {
      return "";
    }
    if (aDataProperties.length == 1) {
      return aDataProperties[aDataProperties.length - 1].getDisplayName();
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append("<html>");
    for (int i = 0; i < aDataProperties.length - 1; i++) {
      for (int j = 0; j < i; j++) {
        buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
      }
      buffer.append("+ ");
      buffer.append(aDataProperties[i].getDisplayName());
      buffer.append("<br>");
    }
    for (int j = 0; j < aDataProperties.length - 1; j++) {
      buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    buffer.append("- ");
    buffer.append(aDataProperties[aDataProperties.length - 1].getDisplayName());
    buffer.append("</html>");
    return buffer.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DataObjectNodeContext) {
      return super.equals(obj) ||
             (Objects.equals(fObject, ((DataObjectNodeContext) obj).getObject()) &&
              Objects.equals(fDataType, ((DataObjectNodeContext) obj).getDataType()) &&
              Objects.equals(fDataProperty, ((DataObjectNodeContext) obj).getDataProperty()));
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int result = 31;
    if (fObject != null) {
      result += fObject.hashCode();
    }
    if (fDataType != null) {
      result += fDataType.hashCode();
    }
    if (fDataProperty != null) {
      result += fDataProperty.hashCode();
    }
    return result;
  }
}
