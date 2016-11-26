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
package samples.gxy.fundamentals.step3;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * This class builds the structural description of the way point model, and provides static access to it.
 * The method getDataModel() provides the full way point data model.  The public constant
 * {@link #WAY_POINT_DATA_TYPE} refers to the only defined type of this model: way points.
 */
public final class WayPointDataTypes {

  // The data model for the way points, fully describing the structure of the data.
  private static final TLcdDataModel WAY_POINT_DATA_MODEL;

  // The data model contains a single data type - the way point data type.
  public static final TLcdDataType WAY_POINT_DATA_TYPE;

  public static final String NAME = "name"; //Starts with lower case, same as Java property
  public static final String POINT = "point";

  static final String WAY_POINT_TYPE = "WayPointType"; //Starts with capital, same as Java class

  static {
    // Assign the constants
    WAY_POINT_DATA_MODEL = createDataModel();
    WAY_POINT_DATA_TYPE = WAY_POINT_DATA_MODEL.getDeclaredType(WAY_POINT_TYPE);
  }

  private static TLcdDataModel createDataModel() {
    // Create the builder for the data model.
    // Use some unique name space, to avoid name clashes.  This isn't really needed
    // for the sample but might be useful when exposing it externally.
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder(
        "http://www.mydomain.com/datamodel/WayPointModel");

    TLcdDataTypeBuilder geometryType = builder.typeBuilder("GeometryType");
    geometryType.primitive(true).instanceClass(ILcdPoint.class);

    // Define the types and their properties (only one type and one property here)
    TLcdDataTypeBuilder wayPointBuilder = builder.typeBuilder(WAY_POINT_TYPE);
    wayPointBuilder.addProperty(NAME, TLcdCoreDataTypes.STRING_TYPE);
    wayPointBuilder.addProperty(POINT, geometryType);

    // Finalize the creation
    TLcdDataModel dataModel = builder.createDataModel();

    TLcdDataType type = dataModel.getDeclaredType(WAY_POINT_TYPE);
    // make sure LuciadLightspeed finds the geometry
    type.addAnnotation(new TLcdHasGeometryAnnotation(type.getProperty(POINT)));

    return dataModel;
  }

  public static TLcdDataModel getDataModel() {
    return WAY_POINT_DATA_MODEL;
  }
}
