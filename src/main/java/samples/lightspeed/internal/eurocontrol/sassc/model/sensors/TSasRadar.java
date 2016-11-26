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
package samples.lightspeed.internal.eurocontrol.sassc.model.sensors;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdTopocentricCoordSysTransformation;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 *
 */
public class TSasRadar extends ASasSensor {

  private static HashMap<Integer, TSasRadar> sRadars = new HashMap<Integer, TSasRadar>();
  private TLcdTopocentricCoordSysTransformation fTopocentricCoordSys;
  private double fEarthRadius;
  private double fMaxPriRange;
  private double fMaxSsrRange;

  public TSasRadar(int aSensorId, double aLon, double aLat, boolean aPositionAvailable,
                   double aMaxPriRange, double aMaxSsrRange, String aname, String ashortName) {
    super(aSensorId, aLon, aLat, aname, ashortName);
    fMaxPriRange = TLcdDistanceUnit.NM_UNIT.convertToStandard(aMaxPriRange);
    fMaxSsrRange = TLcdDistanceUnit.NM_UNIT.convertToStandard(aMaxSsrRange);
    if (aPositionAvailable) {
      fTopocentricCoordSys = new TLcdTopocentricCoordSysTransformation(aLon, aLat, 0);
      TLcdXYZPoint center = new TLcdXYZPoint(aLon, aLat, 0);
      TLcdEllipsoid.DEFAULT.llh2geocSFCT(center, center);
      fEarthRadius = TLcdCartesian.distance3D(0, 0, 0, center.getX(), center.getY(), center.getZ());
    }
  }

  public static int load(ObjectInputStream aInputStream, int aNumberOfSensor, boolean aClear)
      throws IOException, ClassNotFoundException {
    if (aClear) {
      clear();
    }

    int totalSensor = aInputStream.readInt() + aNumberOfSensor;
    for (int i = aNumberOfSensor; i < totalSensor; i++) {
      int sensorId = aInputStream.readInt();
      float latitude = aInputStream.readFloat();
      float longitude = aInputStream.readFloat();
      boolean positionAvailable = (aInputStream.readObject() != null);

      //We don't use the remaining information for now
      aInputStream.skipBytes(96);
      if (aInputStream.readBoolean()) {
        aInputStream.skipBytes(32);
      }
      if (aInputStream.readBoolean()) {
        aInputStream.skipBytes(32);
      }
      if (aInputStream.readBoolean()) {
        aInputStream.skipBytes(32);
      }
      float radarRevolutionTime = aInputStream.readFloat();
      double groundAltitude = aInputStream.readDouble();
      double towerHeight = aInputStream.readDouble();
      double maxPriRange = aInputStream.readDouble();
      double maxSsrRange = aInputStream.readDouble();
      String name = (String) aInputStream.readObject();
      String shortName = (String) aInputStream.readObject();

      addRadar(new TSasRadar(sensorId, longitude, latitude, positionAvailable, maxPriRange, maxSsrRange, name, shortName));
    }
    return aNumberOfSensor;
  }

  public static void clear() {
    sRadars.clear();
  }

  public double getMaxPriRange() {
    return fMaxPriRange;
  }

  public double getMaxSsrRange() {
    return fMaxSsrRange;
  }

  public static void addRadar(TSasRadar aRadar) {
    sRadars.put(aRadar.getSensorId(), aRadar);
  }

  public static TSasRadar getRadar(int aId) {
    return sRadars.get(aId);
  }

  public static Collection<Integer> getRadarIds() {
    return Collections.unmodifiableCollection(sRadars.keySet());
  }

  public static Collection<TSasRadar> getAllRadars() {
    return Collections.unmodifiableCollection(sRadars.values());
  }

  public synchronized void radarXYH2LonLatHeightSFCT(ILcdPoint aXYHPoint, ILcd3DEditablePoint aLLHPointSFCT)
      throws TLcdOutOfBoundsException {
    double z = atRadarXYHToRadarZ(aXYHPoint.getX(), aXYHPoint.getY(), aXYHPoint.getZ(), fEarthRadius);
    aLLHPointSFCT.move3D(aXYHPoint.getX(), aXYHPoint.getY(), z);
    fTopocentricCoordSys.topoc2llhSFCT(aLLHPointSFCT, aLLHPointSFCT);
  }

  private double atRadarXYHToRadarZ(double aX, double aY, double aH, double aEarthRadius) {
    double xh = aX / (aEarthRadius + aH);
    double yh = aY / (aEarthRadius + aH);
    return (aEarthRadius + aH) * Math.sqrt(1.0 - (xh * xh + yh * yh)) - aEarthRadius;
  }

}
