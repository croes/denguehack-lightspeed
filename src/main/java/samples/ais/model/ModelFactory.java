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
package samples.ais.model;

import com.luciad.ais.model.TLcdAISDataObjectFactory;
import com.luciad.ais.model.TLcdAISDataTypes;
import com.luciad.ais.model.aerodrome.*;
import com.luciad.ais.model.aerodrome.type.TLcdRunwaySurfaceType;
import com.luciad.ais.model.airspace.TLcdAirspaceModelDescriptor;
import com.luciad.ais.model.airspace.TLcdAirspaceSegment;
import com.luciad.ais.model.airspace.TLcdFeaturedAirspace;
import com.luciad.ais.model.airspace.type.TLcdAirspaceClass;
import com.luciad.ais.model.airspace.type.TLcdAirspaceType;
import com.luciad.ais.model.navaid.ILcdEditableWayPoint;
import com.luciad.ais.model.navaid.TLcdWayPointModelDescriptor;
import com.luciad.ais.model.procedure.ILcdEditableProcedure;
import com.luciad.ais.model.procedure.ILcdEditableProcedureLeg;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectory;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectoryModelDescriptor;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegFixOverflyType;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegType;
import com.luciad.ais.model.route.ILcdEditableATSRoute;
import com.luciad.ais.model.route.ILcdEditableRouteSegment;
import com.luciad.ais.model.route.TLcdATSRouteModelDescriptor;
import com.luciad.ais.model.util.TLcdAltitudeReference;
import com.luciad.ais.shape.ILcdGeoPathLeg;
import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdAltitudeUnit;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates models containing AIS objects that use geodetic coordinates. The data
 * of the AIS objects is hardcoded in the 'create...Model' methods.
 * <p/>
 * The created models are of the type <code>ILcd2DBoundsIndexedModel</code>.
 * Such a model uses a spatial data structure (2DBoundsIndexed) for efficient
 * retrieval of objects based on specified 2D bounds.
 */
public class ModelFactory {

  private static final ILcdModelReference WGS_84 = new TLcdGeodeticReference(new TLcdGeodeticDatum());
  private TLcdAISDataObjectFactory fObjectFactory;

  public ModelFactory() {
    // Create an AIS object factory. Such a factory makes it easy to create a number of
    // AIS domain objects with a number of specified properties. In this sample, the factory
    // is configures such that objects with a geodetic reference (Lon-Lat-Height objects) are created.
    fObjectFactory = new TLcdAISDataObjectFactory();
  }


  /**
   * Creates a model containing an aerodrome with a few features.
   *
   * @return a model containing an aerodrome with a few features.
   */
  public ILcd2DBoundsIndexedModel createAerodromeModel() {
    TLcd2DBoundsIndexedModel aerodrome_model
        = new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    aerodrome_model.setModelReference(WGS_84);

    // Create a number of aerodromes with a custom type that is declared in our
    // own data model
    ILcdEditableAerodrome aerodrome = fObjectFactory.createAerodrome(CustomAISDataTypes.MyAerodromeType);
    aerodrome.move2D(4.5, 50.9);

    aerodrome.setValue(CustomAISDataTypes.MyAerodromeType.getProperty("Identification"), "EBBR");
    aerodrome.setValue(CustomAISDataTypes.MyAerodromeType.getProperty("Name"), "BRUSSELS NATIONAL");

    aerodrome_model.addElement(aerodrome, ILcdFireEventMode.NO_EVENT);

    // Create an aerodrome model descriptor that describes this model
    TLcdAerodromeModelDescriptor descriptor = new TLcdAerodromeModelDescriptor(
        "Hardcoded", "Aerodrome", "Aerodrome", CustomAISDataTypes.MyAerodromeType
    );
    aerodrome_model.setModelDescriptor(descriptor);

    return aerodrome_model;
  }

  /**
   * Creates a model containing a few waypoints with a name.
   *
   * @return a model containing a few waypoints with a name.
   */
  public ILcd2DBoundsIndexedModel createWaypointModel() {
    TLcd2DBoundsIndexedModel waypoint_model = new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    waypoint_model.setModelReference(WGS_84);

    TLcdDataProperty identifierProperty = CustomAISDataTypes.MyWaypointType.getDeclaredProperty("Identifier");

    // Create a number of waypoints with 2 features: the ICAO code, and the name
    ILcdEditableWayPoint wp = fObjectFactory.createWayPoint(CustomAISDataTypes.MyWaypointType);
    wp.move2D(3.9, 51.25);
    wp.setValue(identifierProperty, "HELEN");
    waypoint_model.addElement(wp, ILcdFireEventMode.NO_EVENT);

    wp = fObjectFactory.createWayPoint(CustomAISDataTypes.MyWaypointType);
    wp.move2D(3.55, 50.9);
    wp.setValue(identifierProperty, "FERDI");
    waypoint_model.addElement(wp, ILcdFireEventMode.NO_EVENT);

    wp = fObjectFactory.createWayPoint(CustomAISDataTypes.MyWaypointType);
    wp.move2D(3.3, 50.5);
    wp.setValue(identifierProperty, "CMB19");
    waypoint_model.addElement(wp, ILcdFireEventMode.NO_EVENT);

    // Create a waypoint model descriptor that describes this model. 
    TLcdWayPointModelDescriptor descriptor = new TLcdWayPointModelDescriptor(
        "Hardcoded", "WayPoint", "WayPoint", CustomAISDataTypes.MyWaypointType
    );
    waypoint_model.setModelDescriptor(descriptor);

    return waypoint_model;
  }


  /**
   * Creates a model containing a route that connects some specific named waypoints taken from
   * the given waypoint model.
   *
   * @param aWayPointModel a waypoint model.
   *
   * @return a model containing a route that connects some specific named waypoints taken from
   * the given waypoint model.
   */
  public ILcdModel createRouteModel(ILcdModel aWayPointModel
  ) {
    TLcd2DBoundsIndexedModel route_model = new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    route_model.setModelReference(WGS_84);

    // Create a number of routes with 1 features: the name
    ILcdEditableATSRoute route = fObjectFactory.createATSRoute(CustomAISDataTypes.MyRouteType);

    Map<String, ILcd3DEditablePoint> wayPoints = new HashMap<String, ILcd3DEditablePoint>();
    Enumeration elements = aWayPointModel.elements();
    while (elements.hasMoreElements()) {
      ILcdDataObject element = (ILcdDataObject) elements.nextElement();
      wayPoints.put((String) element.getValue("Identifier"), (ILcd3DEditablePoint) element);

    }
    ILcd3DEditablePoint wp1 = wayPoints.get("HELEN");
    ILcd3DEditablePoint wp2 = wayPoints.get("FERDI");
    ILcd3DEditablePoint wp3 = wayPoints.get("CMB19");

    // Create a route of two segments
    ILcdEditableRouteSegment s1 = fObjectFactory.createRouteSegment(TLcdAISDataTypes.RouteSegment, wp1, wp2);
    s1.setSequenceNumber(10);
    route.addSegmentBySequenceNumber(s1);
    ILcdEditableRouteSegment s2 = fObjectFactory.createRouteSegment(TLcdAISDataTypes.RouteSegment, wp2, wp3);
    s2.setSequenceNumber(20);
    route.addSegmentBySequenceNumber(s2);

    // Give the route a name
    route.setValue(CustomAISDataTypes.MyRouteType.getProperty("Identifier"), "A5");

    // Add the route to the model
    route_model.addElement(route, ILcdFireEventMode.NO_EVENT);

    // Create a route model descriptor that describes this model
    TLcdATSRouteModelDescriptor descriptor = new TLcdATSRouteModelDescriptor(
        "Hardcoded", "Routes", "Routes", CustomAISDataTypes.MyRouteType
    );
    route_model.setModelDescriptor(descriptor);

    return route_model;
  }


  /**
   * Creates a model containing a procedure. Fixes used in procedures can be any ILcdPoint instance.
   * In this sample, waypoints and aerodromes from the supplied models
   * are used, as well as on the fly created points.
   *
   * @param aAerodromeModel An aerodrome model.
   * @param aWayPointModel  A waypoint model.
   * @return a model containing a procedure.
   */
  public ILcd2DBoundsIndexedModel createProcedureModel(
      ILcd2DBoundsIndexedModel aAerodromeModel,
      ILcd2DBoundsIndexedModel aWayPointModel
  ) {
    TLcd2DBoundsIndexedModel procedure_model =
        new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    procedure_model.setModelReference(WGS_84);

    /*
     * Find waypoints based on the identifier.
     */
    ILcd3DEditablePoint helen = null;
    Enumeration elements = aWayPointModel.elements();
    TLcdDataProperty identifierProperty = CustomAISDataTypes.MyWaypointType.getProperty("Identifier");
    while (elements.hasMoreElements()) {
      ILcdDataObject object = (ILcdDataObject) elements.nextElement();
      if ("HELEN".equals(object.getValue(identifierProperty))) {
        helen = (ILcd3DEditablePoint) object;
        break;
      }
    }

    if (helen == null) {
      System.out.println("Error: cannot find HELEN in waypoints model");
      return null;
    }

    // Find the EBBR aerodrome, but this time the model elements are enumerated explicitly
    Enumeration aerodromes = aAerodromeModel.elements();
    TLcdAerodromeModelDescriptor ad_descriptor =
        (TLcdAerodromeModelDescriptor) aAerodromeModel.getModelDescriptor();
    TLcdDataProperty aerodromeIdentifierProperty = ad_descriptor.getModelElementTypes().iterator().next().getProperty("Identification");
    ILcdAerodrome ebbr = null;
    while (ebbr == null && aerodromes.hasMoreElements()) {
      ILcdDataObject aerodrome = (ILcdDataObject) aerodromes.nextElement();
      String identification = (String) aerodrome.getValue(aerodromeIdentifierProperty);
      if (identification.equalsIgnoreCase("EBBR")) {
        ebbr = (ILcdAerodrome) aerodrome;
      }
    }

    if (ebbr == null) {
      System.out.println("Error: cannot find EBBR in aerodromes model");
      return null;
    }

    // Create a runway point on the fly
    TLcdLonLatHeightPoint runway_point = new TLcdLonLatHeightPoint(4.51, 50.91, 0);

    // Create an intermediate fix on the fly
    TLcdLonLatHeightPoint nicky = new TLcdLonLatHeightPoint(4.15, 51.15, 0);

    // Create the procedure
    ILcdEditableProcedure procedure = fObjectFactory.createProcedure(CustomAISDataTypes.MyProcedureType);

    // Initial fix
    ILcdEditableProcedureLeg leg = fObjectFactory.createProcedureLeg(TLcdAISDataTypes.ProcedureLeg);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(runway_point);
    leg.setCourse(70);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = fObjectFactory.createProcedureLeg(TLcdAISDataTypes.ProcedureLeg);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setRho(133);
    leg.setCourse(66);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Direct to fix
    leg = fObjectFactory.createProcedureLeg(TLcdAISDataTypes.ProcedureLeg);
    leg.setType(TLcdProcedureLegType.DF);
    leg.setFix(nicky);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Track to fix
    leg = fObjectFactory.createProcedureLeg(TLcdAISDataTypes.ProcedureLeg);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFixOverflyType( TLcdProcedureLegFixOverflyType.FLY_OVER );
    leg.setFix(helen);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Track to fix
    leg = fObjectFactory.createProcedureLeg(TLcdAISDataTypes.ProcedureLeg);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(ebbr);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Create a procedure trajectory model descriptor that describes this model.
    TLcdProcedureTrajectoryModelDescriptor descriptor = new TLcdProcedureTrajectoryModelDescriptor(
        "Hardcoded", "Procedures", "Procedures",
        CustomAISDataTypes.MyProcedureType);

    procedure_model.setModelDescriptor(descriptor);

    // Add the procedure to a proceduretrajectory, set the designator value and add it to the model
    TLcdProcedureTrajectory trajectory = fObjectFactory.createProcedureTrajectory(procedure);
    trajectory.setValue(CustomAISDataTypes.MyProcedureType.getProperty("Designator"), "BE12345");
    procedure_model.addElement(trajectory, ILcdFireEventMode.NO_EVENT);

    return procedure_model;
  }

  /**
   * Creates a model containing an airspace.
   *
   * @return a model containing an airspace.
   */
  public ILcd2DBoundsIndexedModel createAirspaceModel() {
    TLcd2DBoundsIndexedModel airspace_model = new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    airspace_model.setModelReference(WGS_84);

    // Create an airspace
    TLcdFeaturedAirspace airspace = new TLcdFeaturedAirspace(CustomAISDataTypes.MyAirspaceType);

    // Create segments
    TLcdAirspaceSegment segment_1 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_2 = new TLcdAirspaceSegment(ILcdGeoPathLeg.ARC);
    TLcdAirspaceSegment segment_3 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_4 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_5 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_6 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_7 = new TLcdAirspaceSegment(ILcdGeoPathLeg.ARC);
    TLcdAirspaceSegment segment_8 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);
    TLcdAirspaceSegment segment_9 = new TLcdAirspaceSegment(ILcdGeoPathLeg.GEODESIC_LINE);

    TLcdLonLatHeightPoint point_1 = new TLcdLonLatHeightPoint(4.37, 50.26, 0);
    TLcdLonLatHeightPoint point_2 = new TLcdLonLatHeightPoint(4.53, 50.28, 0);
    TLcdLonLatHeightPoint point_3 = new TLcdLonLatHeightPoint(4.73, 50.306, 0);
    TLcdLonLatHeightPoint point_4 = new TLcdLonLatHeightPoint(4.87, 50.32, 0);
    TLcdLonLatHeightPoint point_5 = new TLcdLonLatHeightPoint(4.92, 50.24, 0);
    TLcdLonLatHeightPoint point_6 = new TLcdLonLatHeightPoint(4.89, 50.22, 0);
    TLcdLonLatHeightPoint point_7 = new TLcdLonLatHeightPoint(4.75, 50.21, 0);
    TLcdLonLatHeightPoint point_8 = new TLcdLonLatHeightPoint(4.57, 50.188, 0);
    TLcdLonLatHeightPoint point_9 = new TLcdLonLatHeightPoint(4.40, 50.17, 0);

    TLcdLonLatHeightPoint circle_center_1 = new TLcdLonLatHeightPoint(4.65, 50.23, 0);
    TLcdLonLatHeightPoint circle_center_2 = new TLcdLonLatHeightPoint(4.645, 50.25, 0);

    segment_1.setLocation(point_1);
    segment_1.setSegmentNumber(0);

    segment_2.setLocation(point_2);
    segment_2.setData(new double[]{circle_center_1.getLon(), circle_center_1.getLat(), 0.0, 1.0});
    segment_2.setSegmentNumber(1);

    segment_3.setLocation(point_3);
    segment_3.setSegmentNumber(2);
    segment_4.setLocation(point_4);
    segment_4.setSegmentNumber(3);
    segment_5.setLocation(point_5);
    segment_5.setSegmentNumber(4);
    segment_6.setLocation(point_6);
    segment_6.setSegmentNumber(5);

    segment_7.setLocation(point_7);
    segment_7.setData(new double[]{circle_center_2.getLon(), circle_center_2.getLat(), 0.0, 1.0});
    segment_7.setSegmentNumber(6);

    segment_8.setLocation(point_8);
    segment_8.setSegmentNumber(7);
    segment_9.setLocation(point_9);
    segment_9.setSegmentNumber(8);

    // Add the segments to the airspace
    airspace.addSegment(segment_1);
    airspace.addSegment(segment_2);
    airspace.addSegment(segment_3);
    airspace.addSegment(segment_4);
    airspace.addSegment(segment_5);
    airspace.addSegment(segment_6);
    airspace.addSegment(segment_7);
    airspace.addSegment(segment_8);
    airspace.addSegment(segment_9);

    // Create a model descriptor and set the values

    // 3. create the model descriptor
    TLcdAirspaceModelDescriptor descriptor = new TLcdAirspaceModelDescriptor("Hardcoded", "Airspaces", "Airspaces", CustomAISDataTypes.MyAirspaceType);
    airspace_model.setModelDescriptor(descriptor);

    // 4. set the features
    airspace.setValue(MyAirspaceDataProperties.NAME, "FLORENNES TOWER CTZ");
    airspace.setValue(MyAirspaceDataProperties.TYPE, TLcdAirspaceType.CTR);
    //Note: the classification (CLASS) makes a visual difference
    airspace.setValue(MyAirspaceDataProperties.CLASS, TLcdAirspaceClass.B);
    airspace.setValue(MyAirspaceDataProperties.LOWER_LIMIT, new Float(250.0f));
    airspace.setValue(MyAirspaceDataProperties.LOWER_LIMIT_REFERENCE, TLcdAltitudeReference.ALT);
    airspace.setValue(MyAirspaceDataProperties.LOWER_LIMIT_UNIT, TLcdAltitudeUnit.FEET);
    airspace.setValue(MyAirspaceDataProperties.UPPER_LIMIT, new Float(125.0f));
    airspace.setValue(MyAirspaceDataProperties.UPPER_LIMIT_REFERENCE, TLcdAltitudeReference.ALT);
    airspace.setValue(MyAirspaceDataProperties.UPPER_LIMIT_UNIT, TLcdAltitudeUnit.FEET);
    // Add the airspace to the model
    airspace_model.addElement(airspace, ILcdFireEventMode.NO_EVENT);

    return airspace_model;
  }

  /**
   * Creates a model containing two runways.
   *
   * @return a model containing two runways.
   */
  public ILcd2DBoundsIndexedModel createRunwayModel() {
    TLcd2DBoundsIndexedModel runway_model = new TLcd2DBoundsIndexedModel();

    // WGS 84 reference
    runway_model.setModelReference(WGS_84);

    // Create two runways:

    // 1. create for each runway a polyline with 2 points; these points are the begin and endpoint
    ILcd3DEditablePoint[] point_3d_array_1 = {
        new TLcdLonLatHeightPoint(4.625, 50.241, 0.0),
        new TLcdLonLatHeightPoint(4.672, 50.246, 0.0),
    };
    ILcd3DEditablePoint[] point_3d_array_2 = {
        new TLcdLonLatHeightPoint(4.63, 50.236, 0.0),
        new TLcdLonLatHeightPoint(4.66, 50.239, 0.0),
    };

    ILcd3DEditablePointList point_list_3d_1 =
        new TLcd3DEditablePointList(point_3d_array_1, false);
    ILcd3DEditablePointList point_list_3d_2 =
        new TLcd3DEditablePointList(point_3d_array_2, false);

    ILcdGeodeticDatum datum = ((ILcdGeoReference) WGS_84).getGeodeticDatum();
    TLcdLonLatHeightPolyline polyline_1 = new TLcdLonLatHeightPolyline(point_list_3d_1, datum.getEllipsoid());
    TLcdLonLatHeightPolyline polyline_2 = new TLcdLonLatHeightPolyline(point_list_3d_2, datum.getEllipsoid());

    // 2. create runway directions
    TLcdRunwayDirection direction_1 = new TLcdRunwayDirection(TLcdAISDataTypes.RunwayDirection);
    TLcdRunwayDirection direction_2 = new TLcdRunwayDirection(TLcdAISDataTypes.RunwayDirection);

    // 3. create two runways and set the direction, width and polyline
    // in this example we use the newInstance() method, which can be useful
    // if we want to be able to change the instance class later on
    TLcdRunway runway_1 = (TLcdRunway) CustomAISDataTypes.MyRunwayType.newInstance();
    runway_1.set3DEditablePolyline(polyline_1);
    runway_1.setRunwayDirection(0, direction_1);
    direction_1.setRunway(runway_1);
    runway_1.setWidth(148);

    TLcdRunway runway_2 = (TLcdRunway) CustomAISDataTypes.MyRunwayType.newInstance();
    runway_2.set3DEditablePolyline(polyline_2);
    runway_2.setRunwayDirection(0, direction_2);
    direction_2.setRunway(runway_2);
    runway_2.setWidth(74);

    // Create a model descriptor and set the features

    // 3. create the model descriptor
    TLcdRunwayModelDescriptor descriptor = new TLcdRunwayModelDescriptor("Hardcoded", "Runways", "Runways", CustomAISDataTypes.MyRunwayType);
    runway_model.setModelDescriptor(descriptor);

    // 4. set the values
    runway_1.setValue(MyRunwayDataProperties.WIDTH, runway_1.getWidth());
    // Visually, there is a difference between a hard and a soft surface: a runway with a hard surface
    // is completely filled; a runway with a soft surface is filled with points.  A "concrete" surface is marked as hard.
    runway_1.setValue(MyRunwayDataProperties.SURFACE_TYPE, TLcdRunwaySurfaceType.CON);
    runway_2.setValue(MyRunwayDataProperties.WIDTH, runway_2.getWidth());
    // A "gravel" surface is marked as soft.
    runway_2.setValue(MyRunwayDataProperties.SURFACE_TYPE, TLcdRunwaySurfaceType.GVL);

    // Add the runway to the model
    runway_model.addElement(runway_1, ILcdFireEventMode.NO_EVENT);
    runway_model.addElement(runway_2, ILcdFireEventMode.NO_EVENT);

    return runway_model;
  }
}
