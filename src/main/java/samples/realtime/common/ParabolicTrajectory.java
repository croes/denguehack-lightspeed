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
package samples.realtime.common;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;

/**
 * A trajectory class that represents a trajectory along a parabolic-like line
 */
public class ParabolicTrajectory extends TLcdLonLatHeightPolyline implements ITrajectory {

  private static final int MAX_HEIGHT = 300000;
  private static final int MIN_HEIGHT = 50000;

  private static final int NUMBER_OF_HEIGHT_STEPS = 100;

  private final ILcdPoint fDeparture;
  private final ILcdPoint fDestination;
  private final long fBeginTime;
  private final long fEndTime;
  private final String fId;
  private final double fMaxHeight;

  private final TLcdLonLatHeightPoint fTmpPt1 = new TLcdLonLatHeightPoint();
  private final TLcdLonLatHeightPoint fTmpPt2 = new TLcdLonLatHeightPoint();

  /**
   * Constructs a trajectory that will move along a parabolic-like line from aBeginPoint to aEndPoint
   * in the time span between aBeginTime and aEndTime
   *
   * @param aDeparture  The start point of the parabolic line
   * @param aDestination The end point of the parabolic line
   * @param aBeginTime  The start time of the trajectory
   * @param aEndTime    The end time of the trajectory
   * @param aEllipsoid  The ellipsoid on which the parabolic line is defined
   * @param aId         The trajectory ID
   */
  public ParabolicTrajectory(ILcdPoint aDeparture, ILcdPoint aDestination, long aBeginTime, long aEndTime, ILcdEllipsoid aEllipsoid, String aId) {
    fDeparture = aDeparture;
    fDestination = aDestination;
    fBeginTime = aBeginTime;
    fEndTime = aEndTime;
    fId = aId;
    fMaxHeight = map(aEndTime - aBeginTime, 2 * 60 * 60 * 1000, 12 * 60 * 60 * 1000, MIN_HEIGHT, MAX_HEIGHT);
    setEllipsoid(aEllipsoid);
    for (int j = 0; j <= NUMBER_OF_HEIGHT_STEPS; j++) {
      getPositionAtFraction(((double) j) / (double) NUMBER_OF_HEIGHT_STEPS, fTmpPt1);
      insert3DPoint(j, fTmpPt1.getX(), fTmpPt1.getY(), fTmpPt1.getZ());
    }
  }

  /**
   * Re-maps a number from one range to another. If the input number is outside the input
   * range, it is first truncated.
   *
   * @param aValue the input number
   * @param aLowerIn the lower limit of the input range
   * @param aUpperIn the upper limit of the input range
   * @param aLowerOut the lower limit of the output range
   * @param aUpperOut the upper limit of the output range
   * @return the remapping of aValue from the input range to the output range.
   */
  private double map(double aValue, double aLowerIn, double aUpperIn, double aLowerOut, double aUpperOut) {
    double value = Math.min(aUpperIn, Math.max(aLowerIn, aValue));
    double factor = (value - aLowerIn) / (aUpperIn - aLowerIn);
    return aUpperOut * factor + (1. - factor) * aLowerOut;
  }

  /**
   * Moves aPointSFCT to the position corresponding to aTime.
   *
   * @param aTime      The time to which the new position has to correspond.
   *                   Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *                   similar to {@link java.util.Date#getTime}
   * @param aPointSFCT A side-effect point that will be moved to the position
   *                   corresponding to aTime.
   */
  @Override
  public void getPositionAtTimeSFCT(long aTime, ILcd3DEditablePoint aPointSFCT) {
    double fraction = ((double) (aTime - fBeginTime)) / ((double) (fEndTime - fBeginTime));
    fraction = Math.min(Math.max(fraction, 0.0), 1.0);
    //Calculate the point at the fraction between begin and end point
    //Note that the height isn't yet correct, we will calculate this in the next step
    getPositionAtFraction(fraction, aPointSFCT);
  }

  /**
   * Moves aPointSFCT to the position along the parabolic trajectory that is at the given fraction
   * between the begin and end point of this parabolic-like trajectory.
   * @param aFraction the fraction between begin and end point along the parabolic trajectory to move aPointSFCT to
   * @param aPointSFCT the point to move
   */
  public void getPositionAtFraction(double aFraction, ILcd3DEditablePoint aPointSFCT) {
    getEllipsoid().geodesicPointSFCT(fDeparture, fDestination, aFraction, aPointSFCT);
    /* This trajectory uses the parabolic equation f(x) = 4.0 * x * (1.0 - x), where f(0.5) = 1.0 is the maximum.
     * The following lines map aFraction values between [0.3, 0.7] to 0.5
     * to keep the altitude to its maximum during most of the fly.
     */
    double fraction;
    if (aFraction < 0.5) {
      fraction = map(aFraction, 0.0, 0.3, 0.0, 0.5);
    } else {
      fraction = map(aFraction, 0.7, 1.0, 0.5, 1.0);
    }

    double height = fMaxHeight * 4.0 * fraction * (1.0 - fraction);
    aPointSFCT.move3D(aPointSFCT.getX(), aPointSFCT.getY(), height);
  }

  /**
   * Returns the time at which this parabolic trajectory starts.
   * @return the time at which this parabolic trajectory starts
   */
  @Override
  public long getBeginTime() {
    return fBeginTime;
  }

  @Override
  public long getEndTime() {
    return fEndTime;
  }

  public ILcdPoint getDeparture() {
    return fDeparture;
  }

  public ILcdPoint getDestination() {
    return fDestination;
  }

  @Override
  public boolean isGroundedAtTime(long aTime) {
    return (aTime <= fBeginTime) || (aTime >= fEndTime);
  }

  @Override
  public synchronized double getOrientationAtTimeSFCT(long aTime) {
    final int dt = 1000;
    getPositionAtTimeSFCT(aTime, fTmpPt1);
    boolean fwd = aTime + dt < getEndTime();
    long time2 = fwd ? aTime + dt : aTime - dt;
    getPositionAtTimeSFCT(time2, fTmpPt2);
    return Math.toDegrees(getEllipsoid().forwardAzimuth2D(fwd ? fTmpPt1 : fTmpPt2, fwd ? fTmpPt2 : fTmpPt1));
  }

  @Override
  public String getID() {
    return fId;
  }

  @Override
  public String toString() {
    return "ParabolicTrajectory{" +
           "Id='" + fId + '\'' +
           '}';
  }

}
