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
package samples.gxy.painterstyles;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;

/**
 * A polyline with style settings.
 * It also implements <code>ILcdDataObject</code> to allow polylines with 1 feature.
 */
class StyledPolyline extends TLcdLonLatPolyline implements ILcdDataObject, StyledShape {

  private String fStreetName = "Testname";

  private static final String DATA_MODEL_NAME = "StyledPolyline";
  private static final TLcdDataType DATA_TYPE;
  private static final String DATA_TYPE_NAME = "StyledPolylineType";
  public static final TLcdDataProperty STREET_NAME;
  private static final String STREET_NAME_STRING = "StreetName";

  private final ShapeStyle fShapeStyle = new ShapeStyle();

  static {
    DATA_TYPE = createDataType();
    STREET_NAME = DATA_TYPE.getDeclaredProperty(STREET_NAME_STRING);
  }

  private static TLcdDataType createDataType() {
    TLcdDataModelBuilder modelBuilder = new TLcdDataModelBuilder(DATA_MODEL_NAME);
    TLcdDataTypeBuilder styledPolylineType = modelBuilder.typeBuilder(DATA_TYPE_NAME).instanceClass(StyledPolyline.class);
    styledPolylineType.addProperty(STREET_NAME_STRING, TLcdCoreDataTypes.STRING_TYPE);
    return modelBuilder.createDataModel().getDeclaredType(DATA_TYPE_NAME);
  }

  /**
   * Constructs a new <code>StyledPolyline</code> with the given point list.
   * The ellipsoid on which the polyline is defined is {@link TLcdEllipsoid#DEFAULT}.
   *
   * @param aList The point list to be used for the polyline.
   */
  public StyledPolyline(ILcd2DEditablePointList aList) {
    this(aList, TLcdEllipsoid.DEFAULT);
  }

  /**
   * Constructs a new <code>StyledPolyline</code> with the given point list and ellipsoid.
   *
   * @param aList      The point list to be used for the polyline.
   * @param aEllipsoid The ellipsoid on which the polyline is defined.
   */
  public StyledPolyline(ILcd2DEditablePointList aList, ILcdEllipsoid aEllipsoid) {
    super(aList, aEllipsoid);
  }

  public ShapeStyle getShapeStyle() {
    return fShapeStyle;
  }

  public TLcdDataType getDataType() {
    return DATA_TYPE;
  }

  public Object getValue(TLcdDataProperty aProperty) {
    if (aProperty == STREET_NAME) {
      return fStreetName;
    } else {
      throw new IllegalArgumentException("Not a valid property for StyledPolyline");
    }
  }

  public Object getValue(String aPropertyName) {
    return getValue(getDataType().getProperty(aPropertyName));
  }

  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    if (aProperty == STREET_NAME) {
      if (aValue instanceof String) {
        fStreetName = (String) aValue;
      } else {
        throw new IllegalArgumentException("Given value must be of type String");
      }
    } else {
      throw new IllegalArgumentException("Not a valid property for StyledPolyline");
    }
  }

  public void setValue(String aPropertyName, Object aValue) {
    setValue(getDataType().getProperty(aPropertyName), aValue);
  }

  public boolean hasValue(TLcdDataProperty aProperty) {
    return getValue(aProperty) != null;
  }

  public boolean hasValue(String aPropertyName) {
    return hasValue(getDataType().getProperty(aPropertyName));
  }
}
