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
package samples.lightspeed.vertical;

import com.luciad.earth.view.vertical.TLcdVVTerrainModel;
import com.luciad.shape.ILcdPointList;

import samples.gxy.vertical.Flight;

/**
 * Sample implementation of <code>ILcdVVModel</code> to model a flight, its air route, and the underlying terrain.
 *
 * The main profile of the model is the flight itself.
 * <p/>
 * The air route is modeled as a sub-profile. For every segment of the flight,
 * a corresponding air route {@link #subProfilePointCount segment} is defined,
 * with {@link #minZ minimum} and {@link #maxZ maximum} altitude.
 * <p/>
 * The terrain is also modeled as a subprofile. The super class calculates the necessary altitude values.
 */
public class TerrainFlightVVModel extends TLcdVVTerrainModel {

  private Flight getFlight() {
    Object object = getObject();
    return object instanceof Flight ? (Flight) object : null;
  }

  @Override
  public int getSubProfileCount() {
    // Add a sub-profile for the flight's air route.
    return super.getSubProfileCount() + (getFlight() == null ? 0 : 1);
  }

  @Override
  public int subProfilePointCount(int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (aSubProfileIndex == 0) {
      return super.subProfilePointCount(aSegmentIndex, aSubProfileIndex);
    } else {
      // For each segment, the air route sub-profile has just 2 points: the begin point and
      // the end point of the segment
      return getFlight() == null ? 0 : 2;
    }
  }

  @Override
  public float stepLenghtRatio(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (aSubProfileIndex == 0) {
      return super.stepLenghtRatio(aSubProfilePointIndex, aSegmentIndex, aSubProfileIndex);
    } else {
      // The air route sub-profile contains one step for each segment so the ratio of the step length is always 1
      return 1;
    }
  }

  @Override
  public double minZ(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (aSubProfileIndex == 0) {
      return super.minZ(aSubProfilePointIndex, aSegmentIndex, aSubProfileIndex);
    } else {
      Flight flight = getFlight();
      return flight == null ? 0.0 : flight.getRouteMinAltitude(aSegmentIndex);
    }
  }

  @Override
  public double maxZ(int aSubProfilePointIndex, int aSegmentIndex, int aSubProfileIndex)
      throws IndexOutOfBoundsException {
    if (aSubProfileIndex == 0) {
      return super.maxZ(aSubProfilePointIndex, aSegmentIndex, aSubProfileIndex);
    } else {
      Flight flight = getFlight();
      return flight == null ? 0.0 : flight.getRouteMaxAltitude(aSegmentIndex);
    }
  }

  @Override
  public ILcdPointList retrievePointList(Object aObject) {
    return super.retrievePointList(aObject);
  }
}
