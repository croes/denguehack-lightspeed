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
package samples.lucy.cop.addons.missioncontroltheme;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;

/**
 * Model for the annotation layer
 */
final class AnnotationModel extends AGeoJsonRestModelWithUpdates {

  private static final String ANNOTATION_PREFIX = "annotation.";

  private static final TLcdDataModel DATA_MODEL;
  private static final TLcdDataType ANNOTATION_TYPE;
  private static final TLcdDataProperty ID_PROPERTY;
  static final TLcdDataProperty LINE_CAP_DECORATION_PROPERTY;
  private static final TLcdDataProperty UNIQUE_ID_PROPERTY;
  static final TLcdDataProperty TEXT_PROPERTY;

  static final String ARROW_LINE_CAP_INDICATION = "arrow";

  static {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("samples.lucy.cop.addons.missioncontroltheme.Annotation");
    TLcdDataTypeBuilder annotation = builder.typeBuilder("annotation");
    annotation.addProperty("text", TLcdCoreDataTypes.STRING_TYPE);
    annotation.addProperty("lineCapDecoration", TLcdCoreDataTypes.STRING_TYPE);
    annotation.addProperty("uid", TLcdCoreDataTypes.INTEGER_TYPE);
    annotation.addProperty("uuid", TLcdCoreDataTypes.STRING_TYPE);

    DATA_MODEL = builder.createDataModel();
    ANNOTATION_TYPE = DATA_MODEL.getDeclaredType("annotation");
    ID_PROPERTY = ANNOTATION_TYPE.getProperty("uid");
    LINE_CAP_DECORATION_PROPERTY = ANNOTATION_TYPE.getProperty("lineCapDecoration");
    UNIQUE_ID_PROPERTY = ANNOTATION_TYPE.getProperty("uuid");
    TEXT_PROPERTY = ANNOTATION_TYPE.getProperty("text");
  }

  AnnotationModel(String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(DATA_MODEL, ANNOTATION_TYPE, DATA_MODEL.getName(), "Annotations", aPropertyPrefix + ANNOTATION_PREFIX, aProperties, aLucyEnv);
  }

  AnnotationModel(String aRESTServiceURL, String aWebSocketURL) {
    super(DATA_MODEL, ANNOTATION_TYPE, DATA_MODEL.getName(), "Annotations", aRESTServiceURL, aWebSocketURL);
  }

  @Override
  protected String getServerIDPropertyName() {
    return ID_PROPERTY.getName();
  }

  @Override
  protected String getMobileUniqueIDPropertyName() {
    return UNIQUE_ID_PROPERTY.getName();
  }

  @Override
  TLcdDataType getDataType() {
    return ANNOTATION_TYPE;
  }

  @Override
  GeoJsonRestModelElement createDomainObjectForShape(ILcdShape aShape) {
    GeoJsonRestModelElement domainObjectForShape = super.createDomainObjectForShape(aShape);
    if (aShape instanceof ILcdPolyline && !(aShape instanceof ILcdPolygon)) {
      domainObjectForShape.setValue(LINE_CAP_DECORATION_PROPERTY, ARROW_LINE_CAP_INDICATION);
    }
    return domainObjectForShape;
  }
}
