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
package samples.gxy.icons;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdOriented;

/**
 * Oriented lon lat point.
 */
class RotatedPoint extends TLcdLonLatPoint implements ILcdOriented, ILcdDataObject {
  public static final TLcdDataProperty Name;
  public static final TLcdDataProperty Orientation;

  private static final String MY_ROTATED_POINT_MODEL_NAME = "MyRotatedPoint";
  private static final String MY_ROTATED_POINT_TYPE_NAME = "MyRotatedPointType";
  private static final String NAME_PROPERTY_NAME = "Name";
  private static final String ORIENTATION_PROPERTY_NAME = "Orientation";

  private double fOrientation;
  private String fName;
  private static TLcdDataType fDataType;

  static {
    fDataType = createDataType();
    Name = fDataType.getDeclaredProperty(NAME_PROPERTY_NAME);
    Orientation = fDataType.getDeclaredProperty(ORIENTATION_PROPERTY_NAME);
  }

  public RotatedPoint(double aLon, double aLat, String aName, double aOrientation) {
    super(aLon, aLat);
    fOrientation = aOrientation;
    fName = aName;
  }

  private static TLcdDataType createDataType() {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder(MY_ROTATED_POINT_MODEL_NAME);
    TLcdDataTypeBuilder rotatedPointDataType = dataModelBuilder.typeBuilder(MY_ROTATED_POINT_TYPE_NAME).instanceClass(RotatedPoint.class);
    rotatedPointDataType.addProperty(NAME_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE);
    rotatedPointDataType.addProperty(ORIENTATION_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE);
    return dataModelBuilder.createDataModel().getDeclaredType(MY_ROTATED_POINT_TYPE_NAME);
  }

  public double getOrientation() {
    double rotation = fOrientation;

    while (rotation > 360.0) {
      rotation -= 360.0;
    }
    while (rotation < 0) {
      rotation += 360.0;
    }

    return rotation;
  }

  public TLcdDataType getDataType() {
    return fDataType;
  }

  public Object getValue(TLcdDataProperty aProperty) {
    if (aProperty == Name) {
      return fName;
    } else if (aProperty == Orientation) {
      return "Rotation : " + getOrientation() + " degrees";
    } else {
      throw new IllegalArgumentException("Invalid property for MyRotatedPoint");
    }
  }

  public Object getValue(String aPropertyName) {
    return getValue(getDataType().getProperty(aPropertyName));
  }

  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    throw new UnsupportedOperationException("Can't set property for MyRotatedPoint");
  }

  public void setValue(String aPropertyName, Object aValue) {
    throw new UnsupportedOperationException("Can't set property for MyRotatedPoint");
  }

  public boolean hasValue(TLcdDataProperty aProperty) {
    return getValue(aProperty) != null;
  }

  public boolean hasValue(String aPropertyName) {
    return hasValue(getDataType().getProperty(aPropertyName));
  }

}
