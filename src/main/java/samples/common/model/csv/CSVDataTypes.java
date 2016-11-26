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
package samples.common.model.csv;

import java.util.List;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataModelDisplayNameProvider;
import com.luciad.datamodel.TLcdDataPropertyBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.shape.ILcdShape;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * Provides information about the data model of decoded CSV files.
 */
public class CSVDataTypes {

  public static final String DATA_MODEL = "PointDataModel";
  public static final String TYPE = "PointType";

  public static final String POINT_GEOMETRY_PROPERTY_NAME = "point_geometry";
  public static final String ID_PROPERTY_NAME = "ID";

  // creates a data model with the given fields as properties, an extra geometry property, and optionally an ID property
  static TLcdDataModel createDataModel(List<CSVModelDecoder.Field> aProperties, boolean aAddID) {
    TLcdDataModelBuilder modelBuilder = new TLcdDataModelBuilder(DATA_MODEL);

    TLcdDataModelDisplayNameProvider displayNameProvider = new TLcdDataModelDisplayNameProvider();
    modelBuilder.displayNameProvider(displayNameProvider);

    TLcdDataTypeBuilder dataTypeBuilder = modelBuilder.typeBuilder(TYPE);
    dataTypeBuilder.instanceClass(CSVRecord.class);

    // add the CSV fields
    for (CSVModelDecoder.Field field : aProperties) {
      TLcdDataPropertyBuilder propertyBuilder = dataTypeBuilder.addProperty(
          field.getName(), TLcdCoreDataTypes.STRING_TYPE);
      displayNameProvider.setDisplayName(propertyBuilder, field.getDisplayName());
    }

    // add an extra ID
    if (aAddID) {
      TLcdDataPropertyBuilder propertyBuilder = dataTypeBuilder.addProperty(
          ID_PROPERTY_NAME, TLcdCoreDataTypes.INTEGER_TYPE);
      displayNameProvider.setDisplayName(propertyBuilder, "ID");
    }

    // expose the LuciadLightspeed geometry
    TLcdDataTypeBuilder geometryType = modelBuilder.typeBuilder("GeometryType");
    geometryType.primitive(true).instanceClass(ILcdShape.class);
    TLcdDataPropertyBuilder propertyBuilder = dataTypeBuilder.addProperty(
        CSVDataTypes.POINT_GEOMETRY_PROPERTY_NAME, geometryType);
    displayNameProvider.setDisplayName(propertyBuilder, "Geometry");

    TLcdDataModel dataModel = modelBuilder.createDataModel();
    TLcdDataType type = dataModel.getDeclaredType(TYPE);
    // make sure LuciadLightspeed finds the geometry
    type.addAnnotation(new TLcdHasGeometryAnnotation(type.getProperty(POINT_GEOMETRY_PROPERTY_NAME)));
    return dataModel;
  }
}
