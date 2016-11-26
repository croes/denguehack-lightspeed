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
package samples.wms.common;

import java.io.InputStream;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * <p>Declares the data model and data type that is used when converting a GetFeatureInfo response to a model.
 * This model will contain the response of the GetFeatureInfo request as an array of bytes. See also
 * {@link WMSGetFeatureInfoModelFactory#convertToModel(InputStream, String, ILcdPoint, ILcdModelReference)}.</p>
 *
 * <p>The resulting model contains {@code ILcdDataObject} instances as elements, with {@link #DATA_TYPE} as type.
 * These model elements contain following properties:</p>
 * <ul>
 *   <li>{@link #LOCATION_PROPERTY}: The locations in model coordinates at which the GetFeatureInfo request was
 *   performed. This property is also exposed through {@link TLcdHasGeometryAnnotation TLcdHasGeometryAnnotation},
 *   so the location can be accessed using {@link ALcdShape#fromDomainObject(Object) ALcdShape#fromDomainObject}.
 *   This means that the GetFeatureInfo objects can be visualized on the map.</li>
 *   <li>{@link #CONTENT_PROPERTY}: The actual content of the response as a byte array.</li>
 *   <li>{@link #CONTENT_FORMAT_PROPERTY}: The content format of the GetFeatureInfo response. Depending on this format,
 *   the content should be interpreted differently.</li>
 * </ul>
 *
 * @since 2016.1
 */
public class WMSGetFeatureInfoModelDataTypes {

  private static final String GET_FEATURE_INFO_DATA_MODEL_NAME = "getFeatureInfo";
  private static final String GET_FEATURE_INFO_TYPE_NAME = "getFeatureInfoType";
  public static final String SHAPE_TYPE_NAME = "ShapeType";

  public static final String LOCATION_PROPERTY_NAME = "GetFeatureInfoLocation";
  private static final String CONTENT_PROPERTY_NAME = "Content";
  private static final String CONTENT_FORMAT_PROPERTY_NAME = "ContentFormat";
  private static final String NAME_PROPERTY_NAME = "Name";

  /**
   * The data model that declares the data type that is used for the decoded GetFeatureInfo responses.
   */
  public static final TLcdDataModel DATA_MODEL;
  /**
   * The data type of the decoded GetFeatureInfo responses.
   */
  public static final TLcdDataType DATA_TYPE;
  /**
   * The data type used for the {@linkplain #LOCATION_PROPERTY location property}.
   */
  public static final TLcdDataType SHAPE_DATA_TYPE;

  /**
   * Property that defines the location of the GetFeatureInfo request in model coordinates. Can be {@code null}
   * if the location is unknown. This property has {@link #SHAPE_DATA_TYPE} as type.
   */
  public static final TLcdDataProperty LOCATION_PROPERTY;
  /**
   * Property that defines the content format of the GetFeatureInfo request. This property has {@code String} values.
   * See also {@code ALcdWMSProxy#getFeatureInfoFormat()} and {@code TLcdWMSGetFeatureInfoParameters#getFeatureInfoFormat()}.
   */
  public static final TLcdDataProperty CONTENT_FORMAT_PROPERTY;
  /**
   * Property that contains a name for the FeatureInfo object. This property has {@code String} values.
   */
  public static final TLcdDataProperty NAME_PROPERTY;
  /**
   * Property that defines the content of the GetFeatureInfo response. This property has {@code byte[]} values.
   */
  public static final TLcdDataProperty CONTENT_PROPERTY;

  static {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder(GET_FEATURE_INFO_DATA_MODEL_NAME);

    TLcdDataTypeBuilder typeBuilder = dataModelBuilder.typeBuilder(GET_FEATURE_INFO_TYPE_NAME);

    // Add location information to the returned data object to make sure it can be visualized
    TLcdDataTypeBuilder shapeType = dataModelBuilder.typeBuilder(SHAPE_TYPE_NAME);
    shapeType.primitive(true).instanceClass(ILcdPoint.class);
    typeBuilder.addProperty(LOCATION_PROPERTY_NAME, shapeType);

    typeBuilder.addProperty(CONTENT_PROPERTY_NAME, TLcdCoreDataTypes.BYTE_ARRAY_TYPE);
    typeBuilder.addProperty(CONTENT_FORMAT_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE);
    typeBuilder.addProperty(NAME_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE);

    DATA_MODEL = dataModelBuilder.createDataModel();
    DATA_TYPE = DATA_MODEL.getDeclaredType(GET_FEATURE_INFO_TYPE_NAME);
    SHAPE_DATA_TYPE = DATA_MODEL.getDeclaredType(SHAPE_TYPE_NAME);


    LOCATION_PROPERTY = DATA_TYPE.getDeclaredProperty(LOCATION_PROPERTY_NAME);
    DATA_TYPE.addAnnotation(new TLcdHasGeometryAnnotation(LOCATION_PROPERTY));
    CONTENT_FORMAT_PROPERTY = DATA_TYPE.getDeclaredProperty(CONTENT_FORMAT_PROPERTY_NAME);
    NAME_PROPERTY = DATA_TYPE.getDeclaredProperty(NAME_PROPERTY_NAME);
    CONTENT_PROPERTY = DATA_TYPE.getDeclaredProperty(CONTENT_PROPERTY_NAME);
  }
}
