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
package samples.encoder.kml22;

import static com.luciad.shape.ILcdTimeBounds.Boundedness.BOUNDED;
import static com.luciad.shape.ILcdTimeBounds.Boundedness.UNDEFINED;

import java.util.Calendar;
import java.util.Collections;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdTimeBounded;
import com.luciad.shape.ILcdTimeBounds;
import com.luciad.shape.TLcdTimeBounds;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatFloatPolypoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.util.TLcdAltitudeUnit;

/**
 * Create a model with airspaces of different geometries.
 */
final class AirspaceModelFactory {

  private static final TLcdDataModel DATA_MODEL;

  private static final Calendar CALENDAR;
  /**
   * Airspace data type
   */
  private static final TLcdDataType AIRSPACE;

  static {
    CALENDAR = Calendar.getInstance();
    CALENDAR.set(Calendar.YEAR, 1970);
    CALENDAR.set(Calendar.MONTH, Calendar.JANUARY);
  }

  /**
   * Returns the data model for <code>Airspaces</code>.
   *
   * @return the data model
   */
  public static TLcdDataModel getDataModel() {
    return DATA_MODEL;
  }

  public static ILcd2DBoundsIndexedModel createModel() {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor(new AirspaceModelDescriptor("hardcoded", "Airspaces"));
    model.setModelReference(new TLcdGeodeticReference());

    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon(new TLcdLonLatFloatPolypoint(new float[]{-78.04138888888889f, 38.95472222222223f,
                                                                                               -78.15f, 39.49999999999999f,
                                                                                               -77.60222222222221f, 39.74333333333333f,
                                                                                               -77.00000000000001f, 39.724444444444444f,
                                                                                               -76.47722222222221f, 39.60222222222222f,
                                                                                               -76.06777777777779f, 39.32722222222223f,
                                                                                               -76.24082031249995f, 38.94306640625007f,
                                                                                               -77.06416666666665f, 38.455000000000005f,
                                                                                               -77.61833333333333f, 38.586666666666666f}, false, false, false));
    Airspace airspace = createAirspace(polygon, "Polygonal Airspace 1", "B", 0, 700, 19, 23);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    polygon = new TLcdLonLatPolygon(new TLcdLonLatFloatPolypoint(new float[]{-77.79583333333335f, 37.06444444444444f,
                                                                             -77.63222222222221f, 36.93f,
                                                                             -76.93222222222222f, 37.44472222222222f,
                                                                             -76.93861111111113f, 37.577222222222225f,
                                                                             -77.54416666666667f, 37.82361111111112f,
                                                                             -77.69555555555557f, 37.462500000000006f,
                                                                             -77.6902777777778f, 37.1975f}, false, false, false));
    airspace = createAirspace(polygon, "Polygonal Airspace 2", "C", 0, 500, 20, 22);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    polygon = new TLcdLonLatPolygon(new TLcdLonLatFloatPolypoint(new float[]{-76.76277777777777f, 36.5575f,
                                                                             -76.76804305905186f, 36.7154394130628f,
                                                                             -76.58472222222221f, 36.86472222222223f,
                                                                             -76.93916666666665f, 37.24166666666667f,
                                                                             -76.88722222222223f, 37.62583333333333f,
                                                                             -76.37138888888889f, 37.71888888888889f,
                                                                             -75.88305555555553f, 36.85111111111111f,
                                                                             -75.96638888888889f, 36.74444444444445f,
                                                                             -75.84194444444444f, 36.555f}, false, false, false));
    airspace = createAirspace(polygon, "Polygonal Airspace 3", "A", 0, 500, 18, 24);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    polygon = new TLcdLonLatPolygon(new TLcdLonLatFloatPolypoint(new float[]{-75.29805555555555f, 38.403888888888886f,
                                                                             -75.25194444444445f, 38.34083333333333f,
                                                                             -75.25748702470398f, 38.30456948171617f,
                                                                             -75.24192421132588f, 38.260726263862466f,
                                                                             -75.21333407713689f, 38.231989357965666f,
                                                                             -75.15179794280301f, 38.2073491153241f,
                                                                             -75.094101039351f, 38.207788236896086f,
                                                                             -75.04202318256259f, 38.22740169954508f,
                                                                             -75.01102934346535f, 38.2545429917778f,
                                                                             -74.99080979718755f, 38.30659908241949f,
                                                                             -75.0013164027512f, 38.35136712684091f,
                                                                             -75.02657377602962f, 38.38200518705712f,
                                                                             -75.07425231605434f, 38.40778646752115f,
                                                                             -75.10803966118337f, 38.41464536921036f,
                                                                             -75.13611111111112f, 38.415f,
                                                                             -75.17611111111111f, 38.471111111111114f}, false, false, false));
    airspace = createAirspace(polygon, "Polygonal Airspace 4", "C", 200, 700, 18, 23);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    polygon = new TLcdLonLatPolygon(new TLcdLonLatFloatPolypoint(new float[]{-78.16388888888889f, 38.14305555555556f,
                                                                             -78.04055555555556f, 38.1075f,
                                                                             -78.02111111111111f, 38.14388888888889f,
                                                                             -77.95945928337024f, 38.16703854048786f,
                                                                             -77.92455216247964f, 38.203275560069194f,
                                                                             -77.92053012919321f, 38.283698427835574f,
                                                                             -78.00067015448795f, 38.346257233348055f,
                                                                             -78.09200746884572f, 38.34585802312912f,
                                                                             -78.14055166054585f, 38.32115823057582f,
                                                                             -78.17468353979848f, 38.27383435436321f,
                                                                             -78.1768007644808f, 38.2283437935991f,
                                                                             -78.14722222222223f, 38.179166666666674f}, false, false, false));
    airspace = createAirspace(polygon, "Polygonal Airspace 5", "B", 0, 700, 20, 22);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    TLcdLonLatCircle circle = new TLcdLonLatCircle();
    circle.setRadius(18270);
    circle.move2D(-78.45885273434524, 37.3695618061369);

    airspace = createAirspace(circle, "Circular Airspace 1", "B", 200, 600, 20, 24);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    circle = new TLcdLonLatCircle();
    circle.setRadius(10862);
    circle.move2D(-77.98454743413458, 37.08417471872202);

    airspace = createAirspace(circle, "Circular Airspace 2", "C", 0, 700, 20, 24);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    circle = new TLcdLonLatCircle();
    circle.setRadius(14171);
    circle.move2D(-78.16247711914536, 39.13871810109298);

    airspace = createAirspace(circle, "Circular Airspace 3", "C", 0, 700, 18, 23);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    circle = new TLcdLonLatCircle();
    circle.setRadius(10814);
    circle.move2D(-75.96618609267964, 37.799516255687045);

    airspace = createAirspace(circle, "Circular Airspace 4", "A", 0, 700, 17, 23);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    circle = new TLcdLonLatCircle();
    circle.setRadius(10663);
    circle.move2D(-77.24914646674925, 39.828062476940865);

    airspace = createAirspace(circle, "Circular Airspace 5", "B", 500, 1000, 21, 25);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    circle = new TLcdLonLatCircle();
    circle.setRadius(20201);
    circle.move2D(-77.39128461358649, 39.42304217893472);

    airspace = createAirspace(circle, "Circular Airspace 6", "C", 0, 700, 19, 24);

    model.addElement(airspace, ILcdModel.NO_EVENT);

    return model;
  }

  private static Airspace createAirspace(ILcdShape aBaseShape, String aName, String aClass, long aMinAltitude, long aMaxAltitude, int aBeginDay, int aEndDay) {
    Airspace airspace = new Airspace(aBaseShape);

    airspace.setValue("Name", aName);
    airspace.setValue("Class", aClass);
    airspace.setValue("MinimumAltitude", aMinAltitude);
    airspace.setValue("MaximumAltitude", aMaxAltitude);

    CALENDAR.set(Calendar.DAY_OF_MONTH, aBeginDay);
    airspace.setValue("BeginTime", CALENDAR.getTimeInMillis());

    CALENDAR.set(Calendar.DAY_OF_MONTH, aEndDay);
    airspace.setValue("EndTime", CALENDAR.getTimeInMillis());

    return airspace;
  }

  static {
    DATA_MODEL = createDataModel();
    AIRSPACE = DATA_MODEL.getDeclaredType("Airspace");
  }

  private AirspaceModelFactory() {
  }

  public static class AirspaceModelDescriptor extends TLcdDataModelDescriptor {

    public AirspaceModelDescriptor(String aSourceName, String aDisplayName) {
      super(aSourceName, "Airspace", aDisplayName, DATA_MODEL, Collections.singleton(AIRSPACE), DATA_MODEL.getTypes());
    }
  }

  private static TLcdDataModel createDataModel() {

    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("CustomAirspace");
    TLcdDataTypeBuilder typeBuilder = builder.typeBuilder("Airspace");

    typeBuilder.addProperty("Name", TLcdCoreDataTypes.STRING_TYPE);
    typeBuilder.addProperty("Class", TLcdCoreDataTypes.STRING_TYPE);
    typeBuilder.addProperty("MinimumAltitude", TLcdCoreDataTypes.LONG_TYPE);
    typeBuilder.addProperty("MaximumAltitude", TLcdCoreDataTypes.LONG_TYPE);
    typeBuilder.addProperty("BeginTime", TLcdCoreDataTypes.LONG_TYPE);
    typeBuilder.addProperty("EndTime", TLcdCoreDataTypes.LONG_TYPE);

    return builder.createDataModel();
  }

  private static class Airspace extends TLcdDataObject implements ILcdTimeBounded, ILcdExtrudedShape {

    private ILcdShape fBaseShape;
    private TLcdTimeBounds fTimeBounds;

    public Airspace(ILcdShape aBaseShape) {
      super(AIRSPACE);
      fBaseShape = aBaseShape;
      fTimeBounds = new TLcdTimeBounds();
    }

    @Override
    public void setValue(TLcdDataProperty aProperty, Object aValue) {
      super.setValue(aProperty, aValue);
      resetTimeBounds();
    }

    @Override
    public void setValue(String aPropertyName, Object aValue) {
      super.setValue(aPropertyName, aValue);
      resetTimeBounds();
    }

    @Override
    public ILcdTimeBounds getTimeBounds() {
      return fTimeBounds;
    }

    private void resetTimeBounds() {
      Long beginTime = (Long) getValue("BeginTime");
      Long endTime = (Long) getValue("EndTime");
      if (beginTime != null) {
        fTimeBounds.setBeginTime(beginTime);
        fTimeBounds.setBeginTimeBoundedness(BOUNDED);
      } else {
        fTimeBounds.setBeginTimeBoundedness(UNDEFINED);
      }
      if (endTime != null) {
        fTimeBounds.setEndTime(endTime);
        fTimeBounds.setEndTimeBoundedness(BOUNDED);
      } else {
        fTimeBounds.setEndTimeBoundedness(UNDEFINED);
      }
    }

    @Override
    public boolean contains2D(ILcdPoint aPoint) {
      return fBaseShape.contains2D(aPoint);
    }

    @Override
    public boolean contains2D(double aX, double aY) {
      return fBaseShape.contains2D(aX, aY);
    }

    @Override
    public boolean contains3D(ILcdPoint aPoint) {
      return fBaseShape.contains3D(aPoint);
    }

    @Override
    public boolean contains3D(double aX, double aY, double aZ) {
      return fBaseShape.contains3D(aX, aY, aZ);
    }

    @Override
    public ILcdPoint getFocusPoint() {
      return fBaseShape.getFocusPoint();
    }

    @Override
    public ILcdBounds getBounds() {
      return fBaseShape.getBounds();
    }

    @Override
    public ILcdShape getBaseShape() {
      return fBaseShape;
    }

    @Override
    public Airspace clone() {
      Airspace clone = (Airspace) super.clone();
      clone.fBaseShape = (ILcdShape) fBaseShape.clone();
      clone.fTimeBounds = new TLcdTimeBounds(fTimeBounds);
      return clone;
    }

    @Override
    public double getMinimumZ() {
      Long flightLevel = (Long) getValue("MinimumAltitude");
      return flightLevel != null ? TLcdAltitudeUnit.FLIGHT_LEVEL.convertToStandard(flightLevel) : 0;
    }

    @Override
    public double getMaximumZ() {
      Long flightLevel = (Long) getValue("MaximumAltitude");
      return flightLevel != null ? TLcdAltitudeUnit.FLIGHT_LEVEL.convertToStandard(flightLevel) : 0;
    }
  }
}
