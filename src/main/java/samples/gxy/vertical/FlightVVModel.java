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

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.view.vertical.ALcdVVModel;

/**
 * Sample implementation of <code>ILcdVVModel</code> to model a flight and its air route.
 * <br/>
 * The main profile of the model is the flight itself.
 * The air route is modeled as a sub-profile. For every segment of the flight,
 * a corresponding air route {@link #subProfilePointCount segment} is defined,
 * with {@link #minZ minimum} and {@link #maxZ maximum} altitude.
 */
public class FlightVVModel extends ALcdVVModel {

  private Flight fFlight;


  public FlightVVModel() {
  }

  /**
   * Sets the main-profile points of the flight and the minimum and maximum
   * altitudes of the air routes associated to the segments of the flight.
   * All listeners are notified of the changes.
   *
   * @throws IllegalArgumentException if the number of altitudes isn't one less than the number of flightpoints
   */
  public void setFlight(Flight aFlight) {
    fFlight = aFlight;
    super.fireChangeModel();
  }

  public int getPointCount() {
    if (fFlight != null) {
      return fFlight.getPointCount();
    } else {
      return 0;
    }
  }


  // points of the main profile
  public ILcdPoint getPoint(int aIndex) throws IndexOutOfBoundsException {
    if (fFlight != null) {
      return fFlight.getPoint(aIndex);
    } else {
      return null;
    }
  }

  // there is only one sub-profile: the air route associated to each segment
  public int getSubProfileCount() {
    if (fFlight != null) {
      return 1;
    } else {
      return 0;
    }
  }

  // for each segment, the sub-profile has just 2 points: the begin point and
  // the end point of the segment
  public int subProfilePointCount(int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (fFlight != null) {
      return 2;
    } else {
      return 0;
    }
  }

  // the sub-profile contains one step for each segment so the ratio of the
  // step length is always 1
  public float stepLenghtRatio(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    return 1;
  }

  public double minZ(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (fFlight != null) {
      return fFlight.getRouteMinAltitude(aSegmentIndex);
    } else {
      return 0;
    }
  }

  public double maxZ(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (fFlight != null) {
      return fFlight.getRouteMaxAltitude(aSegmentIndex);
    } else {
      return 0;
    }
  }

  // allows editing the main-profile (by dragging the points)
  public boolean isEditable() {
    return true;
  }

  // defines the edit behavior
  public void setPointZ(int aIndex, double aZ, boolean isLastInRow)
      throws IndexOutOfBoundsException {
    if (fFlight != null) {
      ILcd3DEditablePoint point = (ILcd3DEditablePoint) fFlight.getPoint(aIndex);
      point.move3D(point.getX(), point.getY(), aZ);
      if (isLastInRow) {
        super.fireChangeModel();
      }
    }
  }

  @Override
  public double getDistance(int aPointIndexA, int aPointIndexB) {
    // We use the regular WGS 84 distance.
    return TLcdEllipsoid.DEFAULT.geodesicDistance(getPoint(aPointIndexA), getPoint(aPointIndexB));
  }

  // ...

}
