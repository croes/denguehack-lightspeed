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

import java.io.IOException;
import java.io.InputStream;
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
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.service.LcdService;


/**
 * A model decoder that reads vector data stored in a custom text format.
 * The file contains records (way points) separated by an empty line.
 * Each record has one line with the identifier of the way point, followed by
 * a line containing a longitude/latitude/height set of coordinates.
 * The coordinates are expressed as double values and are separated by a space.
 *
 * For example:
 *
 * Way Point WP001
 * 10 10 5000
 *
 * Way Point WP002
 * 30 20 3000
 * <EOF>
 */

@LcdService(service = ILcdModelDecoder.class)
public class WayPointModelDecoder implements ILcdModelDecoder {
  public static final String DISPLAY_NAME = "Way Points";
  private static final String TYPE_NAME = "Custom Waypoint";

  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.endsWith(".cwp");
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    if (!canDecodeSource(aSourceName)) {
      throw new IOException("Cannot decode " + aSourceName);
    }
    //Create the model
    TLcd2DBoundsIndexedModel model = createEmptyModel();
    //Set a descriptor that specifies the meta-data.
    model.setModelDescriptor(createModelDescriptor(aSourceName));

    // Create an input stream from the file name.
    TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
    InputStream inputStream = inputStreamFactory.createInputStream(aSourceName);
    TLcdDataInputStream dataInputStream = new TLcdDataInputStream(inputStream);
    try {
      // Read the records from the file one by one and add them to the model.
      ILcdDataObject waypoint;
      while ((waypoint = readRecord(dataInputStream)) != null) {
        model.addElement(waypoint, ILcdFireEventMode.NO_EVENT);
      }

    } finally {
      dataInputStream.close();
    }
    return model;
  }

  public static TLcd2DBoundsIndexedModel createEmptyModel() {
    // Create a spatially indexed model, with a geodetic (lon/lat) reference system
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(new TLcdGeodeticReference());
    return model;
  }

  public static ILcdModelDescriptor createModelDescriptor(String aSourceName) {
    return new TLcdDataModelDescriptor(
        aSourceName,
        TYPE_NAME,
        DISPLAY_NAME,
        WayPointDataTypes.getDataModel(),

        // ILcdModel.elements only returns objects of type WAY_POINT_DATA_TYPE
        Collections.singleton(WayPointDataTypes.WAY_POINT_DATA_TYPE),

        WayPointDataTypes.getDataModel().getTypes());
  }

  public static ILcdDataObject createWayPoint() {
    return createWayPoint("WayPoint (no name)", 0, 0, 0);
  }

  public static ILcdDataObject createWayPoint(String aWayPointIdentifier, double aLon, double aLat, double aHeight) {
    TLcdDataObject wayPoint = new TLcdDataObject(WayPointDataTypes.WAY_POINT_DATA_TYPE);
    wayPoint.setValue(WayPointDataTypes.NAME, aWayPointIdentifier);
    wayPoint.setValue(WayPointDataTypes.POINT, new TLcdLonLatHeightPoint(aLon, aLat, aHeight));
    return wayPoint;
  }

  /**
   * Reads one single record (way point) from the input file.
   * @param aInputStream: a reader for the input file
   * @return a single way point record
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

    // The first non-empty line contains the way point name.

    String wayPointIdentifier = line;

    // The next non-empty line contains a (lon,lat,height) tuple, separated by a space.

    do {
      line = aInputStream.readLine();
      if (line == null) {
        throw new IOException("Unexpected end of file encountered.");
      }
      line = line.trim();
    } while (line.length() == 0);

    String strings[] = line.split(" +");
    if (strings.length != 3) {
      throw new IOException("Expected <lon lat height>, but found " + line);
    }
    try {
      double lon = Double.parseDouble(strings[0]);
      double lat = Double.parseDouble(strings[1]);
      double height = Double.parseDouble(strings[2]);
      if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
        throw new NumberFormatException("The longitude and latitude must be in the interval " +
                                        "[-180, 180] and [-90, 90], respectively");
      }
      if (height < 0) {
        throw new NumberFormatException("The altitude of the way point must be positive");
      }
      return createWayPoint(wayPointIdentifier, lon, lat, height);
    } catch (NumberFormatException ex) {
      IOException io = new IOException();
      io.initCause(ex);
      throw io;
    }
  }

}
