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
// #disclaimer
// #includefilefor AIS
package samples.ais.model;

import com.luciad.ais.model.TLcdAISDataTypes;
import com.luciad.ais.model.aerodrome.TLcdFeaturedAerodrome;
import com.luciad.ais.model.aerodrome.TLcdFeaturedRunway;
import com.luciad.ais.model.airspace.TLcdFeaturedAirspace;
import com.luciad.ais.model.navaid.TLcdFeaturedWayPoint;
import com.luciad.ais.model.procedure.TLcdFeaturedProcedure;
import com.luciad.ais.model.route.TLcdFeaturedATSRoute;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;

/**
 * This class provides access to a custom AIS data model and the corresponding custom data types. These types are
 * extensions of AIS base types.
 */
public class CustomAISDataTypes {

  private static final TLcdDataModel DATAMODEL;

  /**
   * Custom aerodrome type, extending from the AIS base type {@link TLcdAISDataTypes#Aerodrome}.
   */
  public static final TLcdDataType   MyAerodromeType;

  /**
   * Custom waypoint type, extending from the AIS base type {@link TLcdAISDataTypes#WayPoint}.
   */
  public static final TLcdDataType   MyWaypointType;

  /**
   * Custom route type, extending from the AIS base type {@link TLcdAISDataTypes#Route}.
   */
  public static final TLcdDataType   MyRouteType;

  /**
   * Custom procedure type, extending from the AIS base type {@link TLcdAISDataTypes#Procedure}.
   */
  public static final TLcdDataType   MyProcedureType;

  /**
   * Custom airspace type, extending from the AIS base type {@link TLcdAISDataTypes#Airspace}.
   */
  public static final TLcdDataType   MyAirspaceType;

  /**
   * Custom runway type, extending from the AIS base type {@link TLcdAISDataTypes#Runway}.
   */
  public static final TLcdDataType   MyRunwayType;

  /**
   * Returns an AIS data model with custom data types.
   * 
   * @return an AIS data model with custom data types.
   */
  public static TLcdDataModel getDataModel() {
    return DATAMODEL;
  }

  /**
   * Creates an AIS data model with custom data types.
   * 
   * @return an AIS data model with custom data types.
   */
  private static TLcdDataModel createDataModel() {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("http://www.custom.com/datamodel/Aerodrome");

    TLcdDataTypeBuilder typeBuilder;
    typeBuilder = builder.typeBuilder("MyAerodromeType");
    typeBuilder.superType(TLcdAISDataTypes.Aerodrome).instanceClass(TLcdFeaturedAerodrome.class);
    typeBuilder.addProperty("Identification", TLcdCoreDataTypes.STRING_TYPE);
    typeBuilder.addProperty("Name", TLcdCoreDataTypes.STRING_TYPE);

    typeBuilder = builder.typeBuilder("MyWaypointType");
    typeBuilder.superType(TLcdAISDataTypes.WayPoint).instanceClass(TLcdFeaturedWayPoint.class);
    typeBuilder.addProperty("Identifier", TLcdCoreDataTypes.STRING_TYPE);

    typeBuilder = builder.typeBuilder("MyRouteType");
    typeBuilder.superType(TLcdAISDataTypes.ATSRoute).instanceClass(TLcdFeaturedATSRoute.class);
    typeBuilder.addProperty("Identifier", TLcdCoreDataTypes.STRING_TYPE);

    typeBuilder = builder.typeBuilder("MyProcedureType");
    typeBuilder.superType(TLcdAISDataTypes.Procedure).instanceClass(TLcdFeaturedProcedure.class);
    typeBuilder.addProperty("Designator", TLcdCoreDataTypes.STRING_TYPE);

    typeBuilder = builder.typeBuilder("MyAirspaceType");
    typeBuilder.superType(TLcdAISDataTypes.Airspace).instanceClass(TLcdFeaturedAirspace.class);
    typeBuilder.addProperty("Name", TLcdCoreDataTypes.STRING_TYPE);
    typeBuilder.addProperty("Type", TLcdAISDataTypes.AirspaceType);
    typeBuilder.addProperty("Class", TLcdAISDataTypes.AirspaceClass);
    typeBuilder.addProperty("Lower_Limit", TLcdCoreDataTypes.FLOAT_TYPE);
    typeBuilder.addProperty("Lower_Limit_Reference", TLcdAISDataTypes.AltitudeReference);
    typeBuilder.addProperty("Lower_Limit_Unit", TLcdAISDataTypes.AltitudeUnit);
    typeBuilder.addProperty("Upper_Limit", TLcdCoreDataTypes.FLOAT_TYPE);
    typeBuilder.addProperty("Upper_Limit_Reference", TLcdAISDataTypes.AltitudeReference);
    typeBuilder.addProperty("Upper_Limit_Unit", TLcdAISDataTypes.AltitudeUnit);

    typeBuilder = builder.typeBuilder("MyRunwayType");
    typeBuilder.superType(TLcdAISDataTypes.Runway).instanceClass(TLcdFeaturedRunway.class);
    typeBuilder.addProperty("Width", TLcdCoreDataTypes.DOUBLE_TYPE);
    typeBuilder.addProperty("SurfaceType", TLcdAISDataTypes.RunwaySurfaceType);

    return builder.createDataModel();
  }

  static {
    DATAMODEL = createDataModel();
    MyAerodromeType = DATAMODEL.getDeclaredType("MyAerodromeType");
    MyWaypointType = DATAMODEL.getDeclaredType("MyWaypointType");
    MyRouteType = DATAMODEL.getDeclaredType("MyRouteType");
    MyProcedureType = DATAMODEL.getDeclaredType("MyProcedureType");
    MyAirspaceType = DATAMODEL.getDeclaredType("MyAirspaceType");
    MyRunwayType = DATAMODEL.getDeclaredType("MyRunwayType");
  }
}
