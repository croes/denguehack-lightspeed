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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.shape.ILcdShape;

/**
 * Model for the spot reports layer
 */
final class SpotReportsModel extends AGeoJsonRestModelWithUpdates {
  static final String SPOT_REPORT_PREFIX = "spotReport.";

  private static final TLcdDataModel DATA_MODEL;
  private static final TLcdDataType SPOT_REPORT_TYPE;
  static final TLcdDataProperty CODE_PROPERTY;
  static final TLcdDataProperty ACTIVITY_PROPERTY;
  static final TLcdDataProperty ATTACHMENTS_PROPERTY;
  static final TLcdDataProperty DATE_PROPERTY;
  private static final TLcdDataProperty UNIQUE_ID_PROPERTY;
  private static final TLcdDataProperty ID_PROPERTY;

  static {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("samples.lucy.cop.addons.missioncontroltheme.SpotReport");
    TLcdDataTypeBuilder spotReport = builder.typeBuilder("spotReport");
    spotReport.addProperty("uuid", TLcdCoreDataTypes.STRING_TYPE);
    spotReport.addProperty("code", TLcdCoreDataTypes.STRING_TYPE);
    spotReport.addProperty("size", TLcdCoreDataTypes.INTEGER_TYPE).nullable(true);
    spotReport.addProperty("activity", TLcdCoreDataTypes.STRING_TYPE).nullable(true);
    spotReport.addProperty("date", TLcdCoreDataTypes.STRING_TYPE);
    spotReport.addProperty("extra", TLcdCoreDataTypes.STRING_TYPE);
    spotReport.addProperty("attachments", TLcdCoreDataTypes.STRING_TYPE).collectionType(TLcdDataProperty.CollectionType.LIST);
    spotReport.addProperty("uid", TLcdCoreDataTypes.INTEGER_TYPE);

    DATA_MODEL = builder.createDataModel();
    SPOT_REPORT_TYPE = DATA_MODEL.getDeclaredType("spotReport");
    CODE_PROPERTY = SPOT_REPORT_TYPE.getProperty("code");
    ACTIVITY_PROPERTY = SPOT_REPORT_TYPE.getProperty("activity");
    ATTACHMENTS_PROPERTY = SPOT_REPORT_TYPE.getProperty("attachments");
    DATE_PROPERTY = SPOT_REPORT_TYPE.getProperty("date");
    UNIQUE_ID_PROPERTY = SPOT_REPORT_TYPE.getProperty("uuid");
    ID_PROPERTY = SPOT_REPORT_TYPE.getProperty("uid");
  }

  SpotReportsModel(String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(DATA_MODEL, SPOT_REPORT_TYPE, DATA_MODEL.getName(), "Spot Reports", aPropertyPrefix + SPOT_REPORT_PREFIX, aProperties, aLucyEnv);
  }

  @Override
  String getServerIDPropertyName() {
    return ID_PROPERTY.getName();
  }

  @Override
  String getMobileUniqueIDPropertyName() {
    return UNIQUE_ID_PROPERTY.getName();
  }

  @Override
  TLcdDataType getDataType() {
    return SPOT_REPORT_TYPE;
  }

  @Override
  GeoJsonRestModelElement createDomainObjectForShape(ILcdShape aShape) {
    GeoJsonRestModelElement domainObjectForShape = super.createDomainObjectForShape(aShape);
    //set a default code
    domainObjectForShape.setValue(CODE_PROPERTY, "SHGPEWRR----***");
    //set the date
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    domainObjectForShape.setValue(DATE_PROPERTY, dateFormat.format(new Date()));
    return domainObjectForShape;
  }
}
