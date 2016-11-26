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
package samples.gxy.fundamentals.step2;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.shape.shape2D.ILcd2DEditablePolyline;
import com.luciad.util.TLcdHasGeometryAnnotation;


/**
 * This class builds the structural description of the flight plan model, and provides
 * static access to it. The method getDataModel() provides the full flight plan data model.
 * The public constant FLIGHT_PLAN_DATA_TYPE refers to the only defined type of this model:
 * flight plans.
 */
public final class FlightPlanDataTypes {

  // The data model for the flight plans, fully describing the structure of the data.
  private static final TLcdDataModel FLIGHT_PLAN_DATA_MODEL;

  // The data model contains a single data type - the flight plan data type.
  public static final TLcdDataType FLIGHT_PLAN_DATA_TYPE;

  public static final String NAME = "name"; //Starts with lower case, same as Java property
  public static final String POLYLINE = "polyline";

  static final String FLIGHT_PLAN_TYPE = "FlightPlanType"; //Starts with capital, same as Java class

  static {
    // Assign the constants
    FLIGHT_PLAN_DATA_MODEL = createDataModel();
    FLIGHT_PLAN_DATA_TYPE = FLIGHT_PLAN_DATA_MODEL.getDeclaredType(FLIGHT_PLAN_TYPE);
  }

  private static TLcdDataModel createDataModel() {
    // Create the builder for the data model.
    // Use some unique name space, to prevent name clashes.  This isn't really needed
    // for the sample but might be useful when exposing it externally.
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder(
        "http://www.mydomain.com/datamodel/FlightPlanModel");

    TLcdDataTypeBuilder geometryType = builder.typeBuilder("GeometryType");
    geometryType.primitive(true).instanceClass(ILcd2DEditablePolyline.class);

    // Define the types and their properties (only one type and one property here)
    TLcdDataTypeBuilder flightPlanBuilder = builder.typeBuilder(FLIGHT_PLAN_TYPE);
    flightPlanBuilder.addProperty(NAME, TLcdCoreDataTypes.STRING_TYPE);
    flightPlanBuilder.addProperty(POLYLINE, geometryType);

    // Finalize the creation
    TLcdDataModel dataModel = builder.createDataModel();

    TLcdDataType type = dataModel.getDeclaredType(FLIGHT_PLAN_TYPE);
    // make sure LuciadLightspeed finds the geometry
    type.addAnnotation(new TLcdHasGeometryAnnotation(type.getProperty(POLYLINE)));

    return dataModel;
  }

  public static TLcdDataModel getDataModel() {
    return FLIGHT_PLAN_DATA_MODEL;
  }
}
