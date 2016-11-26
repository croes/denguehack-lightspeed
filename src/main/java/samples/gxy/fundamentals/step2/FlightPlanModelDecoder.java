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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.io.TLcdDataInputStream;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.service.LcdService;


/**
 * A model decoder that reads vector data stored in a custom text format.
 * The file contains records (flight plans) separated by an empty line.
 * Each record has one line with the name of the flight plan, followed by
 * several lines containing one coordinate pair each.
 * The longitude and latitude coordinates are expressed as double values and
 * are separated by a space.
 *
 * For example:
 *
 * Flight Plan A001
 * 10 10
 * 10 12
 * 10 14
 *
 * Flight Plan A002
 * 20 20
 * 21 20
 * ...
 * 30 20
 * <EOF>
 */
@LcdService
public class FlightPlanModelDecoder implements ILcdModelDecoder {
  private static final String DISPLAY_NAME = "Flight Plans";
  private static final String TYPE_NAME = "Custom";

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.endsWith(".cfp");
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    if (!canDecodeSource(aSourceName)) {
      throw new IOException("Cannot decode " + aSourceName);
    }

    // Create a spatially indexed model, with a geodetic (lon/lat) reference system and 
    // a descriptor for the meta-data
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(new TLcdGeodeticReference());
    model.setModelDescriptor(createModelDescriptor(aSourceName));

    // Create an input stream from the file name.
    TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
    InputStream inputStream = inputStreamFactory.createInputStream(aSourceName);
    TLcdDataInputStream dataInputStream = new TLcdDataInputStream(inputStream);
    try {
      // Read the records from the file one by one and add them to the model.
      ILcdDataObject flightPlan;
      while ((flightPlan = readRecord(dataInputStream)) != null) {
        model.addElement(flightPlan, ILcdFireEventMode.NO_EVENT);
      }

    } finally {
      dataInputStream.close();
    }
    return model;
  }

  private ILcdModelDescriptor createModelDescriptor(String aSourceName) {
    return new TLcdDataModelDescriptor(
        aSourceName,
        TYPE_NAME,
        DISPLAY_NAME,
        FlightPlanDataTypes.getDataModel(),

        // ILcdModel.elements only returns objects of type FLIGHT_PLAN_DATA_TYPE
        Collections.singleton(FlightPlanDataTypes.FLIGHT_PLAN_DATA_TYPE),

        FlightPlanDataTypes.getDataModel().getTypes());
  }

  /**
   * Reads one single record (flight plan) from the input file.
   * @param aInputStream: a reader for the input file
   * @return a single flight plan record
   * @throws IOException In case of I/O failure.
   */

  private ILcdDataObject readRecord(TLcdDataInputStream aInputStream) throws IOException {
    String line;

    // Skip any empty lines.
    do {
      line = aInputStream.readLine();
      if (line == null) {
        return null;
      }
    } while (line.trim().length() == 0);

    // The first non-empty line contains the flight plan name.

    String flightplanName = line;

    // The next non-empty lines contain (lon,lat,height) pairs, separated by a space.

    ArrayList<TLcdLonLatHeightPoint> points = new ArrayList<TLcdLonLatHeightPoint>();
    while ((line = aInputStream.readLine()) != null &&
           line.trim().length() > 0) {
      String[] splitString = line.trim().split(" ");
      if (splitString.length != 3) {
        throw new IOException("Expected <lon lat height>, but found <" + line + ">");
      }
      try {
        double lon = Double.parseDouble(splitString[0]);
        double lat = Double.parseDouble(splitString[1]);
        double height = Double.parseDouble(splitString[2]);
        if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
          throw new NumberFormatException("The longitude and latitude must be in the interval " +
                                          "[-180, 180] and [-90, 90], respectively");
        }
        points.add(new TLcdLonLatHeightPoint(lon, lat, height));
      } catch (NumberFormatException ex) {
        IOException io = new IOException();
        io.initCause(ex);
        throw io;
      }
    }
    ILcd3DEditablePoint[] pointArray = new ILcd3DEditablePoint[points.size()];
    TLcd3DEditablePointList pointList = new TLcd3DEditablePointList(points.toArray(pointArray),
                                                                    false);

    ILcdDataObject plan = new TLcdDataObject(FlightPlanDataTypes.FLIGHT_PLAN_DATA_TYPE);
    plan.setValue(FlightPlanDataTypes.NAME, flightplanName);
    plan.setValue(FlightPlanDataTypes.POLYLINE, new TLcdLonLatPolyline(pointList));
    return plan;
  }

}
