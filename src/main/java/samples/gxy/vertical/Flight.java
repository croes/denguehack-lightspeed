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
package samples.gxy.vertical;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;

/**
 * Models a flight and its associated air route altitudes.
 */
public class Flight extends TLcdLonLatHeightPolyline {

  private int[] fRouteMinAltitude;
  private int[] fRouteMaxAltitude;

  public Flight() {
    fRouteMinAltitude = new int[0];
    fRouteMaxAltitude = new int[0];
  }

  public Flight(ILcd3DEditablePointList a3DEditablePointList,
                ILcdEllipsoid aEllipsoid,
                int[] aRouteMinAltitude,
                int[] aRouteMaxAltitude) {
    super(a3DEditablePointList, aEllipsoid);
    int segments = a3DEditablePointList.getPointCount() - 1;
    if (aRouteMinAltitude.length != segments || aRouteMaxAltitude.length != segments) {
      throw new IllegalArgumentException("The air route altitude should be defined for every flight segment.");
    }
    fRouteMinAltitude = aRouteMinAltitude;
    fRouteMaxAltitude = aRouteMaxAltitude;
  }

  public double getRouteMinAltitude(int aSegmentIndex) {
    return fRouteMinAltitude[aSegmentIndex];
  }

  public double getRouteMaxAltitude(int aSegmentIndex) {
    return fRouteMaxAltitude[aSegmentIndex];
  }

  @Override
  public void insert2DPoint(int aIndex, double aX, double aY) {
    super.insert2DPoint(aIndex, aX, aY);

    // Make sure the min/max altitudes are adjusted as well
    adjustMinMaxAltitudeAfterInsertion(aIndex);
  }

  @Override
  public void insert3DPoint(int aIndex, double aX, double aY, double aZ) {
    super.insert3DPoint(aIndex, aX, aY, aZ);

    // Make sure the min/max altitudes are adjusted as well
    adjustMinMaxAltitudeAfterInsertion(aIndex);
  }

  @Override
  public void removePointAt(int aIndex) {
    super.removePointAt(aIndex);

    // Make sure the min/max altitudes are adjusted as well
    adjustMinMaxAltitudeAfterRemoval(aIndex);
  }

  private void adjustMinMaxAltitudeAfterInsertion(int aIndex) {
    int segmentCount = getPointCount() - 1;
    if (segmentCount > 0) {
      int[] routeMinAltitude = new int[fRouteMinAltitude.length + 1];
      int[] routeMaxAltitude = new int[fRouteMaxAltitude.length + 1];

      if (aIndex - 1 > 0) {
        System.arraycopy(fRouteMinAltitude, 0, routeMinAltitude, 0, aIndex - 1);
        System.arraycopy(fRouteMaxAltitude, 0, routeMaxAltitude, 0, aIndex - 1);
      }

      if (segmentCount - aIndex - 1 > 0) {
        System.arraycopy(fRouteMinAltitude, aIndex, routeMinAltitude, aIndex + 1, segmentCount - aIndex - 1);
        System.arraycopy(fRouteMaxAltitude, aIndex, routeMaxAltitude, aIndex + 1, segmentCount - aIndex - 1);
      }

      if (aIndex == 0) {
        routeMinAltitude[0] = (int) (Math.min(getPoint(0).getZ(), getPoint(1).getZ()));
        routeMaxAltitude[0] = (int) (Math.max(getPoint(0).getZ(), getPoint(1).getZ()));
      } else if (aIndex == getPointCount() - 1) {
        routeMinAltitude[segmentCount - 1] = (int) (Math.min(getPoint(segmentCount - 1).getZ(), getPoint(segmentCount).getZ()));
        routeMaxAltitude[segmentCount - 1] = (int) (Math.max(getPoint(segmentCount - 1).getZ(), getPoint(segmentCount).getZ()));
      } else {
        routeMinAltitude[aIndex - 1] = (int) (Math.min(getPoint(aIndex - 1).getZ(), getPoint(aIndex).getZ()));
        routeMinAltitude[aIndex] = (int) (Math.min(getPoint(aIndex).getZ(), getPoint(aIndex + 1).getZ()));
        routeMaxAltitude[aIndex - 1] = (int) (Math.max(getPoint(aIndex - 1).getZ(), getPoint(aIndex).getZ()));
        routeMaxAltitude[aIndex] = (int) (Math.max(getPoint(aIndex).getZ(), getPoint(aIndex + 1).getZ()));
      }

      fRouteMinAltitude = routeMinAltitude;
      fRouteMaxAltitude = routeMaxAltitude;
    }
  }

  private void adjustMinMaxAltitudeAfterRemoval(int aIndex) {
    int segmentCount = getPointCount() - 1;
    if (segmentCount <= 0) {
      fRouteMinAltitude = new int[0];
      fRouteMaxAltitude = new int[0];
    } else {
      int[] routeMinAltitude = new int[fRouteMinAltitude.length + 1];
      int[] routeMaxAltitude = new int[fRouteMaxAltitude.length + 1];

      if (aIndex - 2 > 0) {
        System.arraycopy(fRouteMinAltitude, 0, routeMinAltitude, 0, aIndex - 2);
        System.arraycopy(fRouteMaxAltitude, 0, routeMaxAltitude, 0, aIndex - 2);
      }
      if (segmentCount - aIndex > 0) {
        System.arraycopy(fRouteMinAltitude, aIndex + 1, routeMinAltitude, aIndex, segmentCount - aIndex);
        System.arraycopy(fRouteMaxAltitude, aIndex + 1, routeMaxAltitude, aIndex, segmentCount - aIndex);
      }

      if (aIndex > 0 && aIndex < getPointCount() - 1) {
        routeMinAltitude[aIndex - 1] = (int) (Math.min(getPoint(aIndex - 1).getZ(), getPoint(aIndex).getZ()));
        routeMaxAltitude[aIndex - 1] = (int) (Math.max(getPoint(aIndex - 1).getZ(), getPoint(aIndex).getZ()));
      }

      fRouteMinAltitude = routeMinAltitude;
      fRouteMaxAltitude = routeMaxAltitude;
    }
  }
}
