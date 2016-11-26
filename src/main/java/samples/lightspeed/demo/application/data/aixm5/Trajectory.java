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
package samples.lightspeed.demo.application.data.aixm5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdXYZPoint;

/**
 * Extension of {@code TLcdLonLatHeightPolyline} which adds a time component to it. This class is
 * used to describe the trajectory of an airplane.
 *
 * @see TrajectoryTrack
 */
final class Trajectory extends TLcdLonLatHeightPolyline {

  private float fDefaultSpeed = 1;
  private Date fStartDate;

  private final List<Float> fSpeeds = new ArrayList<Float>();

  private ILcdPolyline fTrajectory;
  private long[] fDateArray;

  /**
   * Creates a new {@code Trajectory} instance.
   *
   * @param a3DEditablePointArray The trajectory described as a an array of points
   * @param aDefaultSpeed         The default speed, expressed in m/s
   * @param aStartDate            The start date for the trajectory
   */
  public Trajectory(ILcd3DEditablePoint[] a3DEditablePointArray, float aDefaultSpeed, Date aStartDate) {
    super(new TLcd3DEditablePointList(a3DEditablePointArray, false));
    fDefaultSpeed = aDefaultSpeed;
    fStartDate = aStartDate;
    for (int i = 0; i < a3DEditablePointArray.length; i++) {
      if (i == 0) {
        fSpeeds.add((float) 0);
      } else {
        fSpeeds.add(fDefaultSpeed);
      }
    }
  }

  private void flushCache() {
    fTrajectory = null;
  }

  private boolean isCacheValid() {
    return fTrajectory != null;
  }

  private void calculateCache() {
    fTrajectory = this;

    // copy time over point of all points to array, to be able to perform binary search
    fDateArray = new long[fTrajectory.getPointCount()];
    for (int i = 0; i < fDateArray.length; i++) {
      fDateArray[i] = getTimeOverPoint(i);
    }
  }

  private float getSpeedOnPoint(int aPointIndex) {
    return fSpeeds.get(aPointIndex);
  }

  private float getAverageSpeedOnLeg(int aLegIndex) {
    // We assume linear accelleration between points, so the average speed is
    // simply average of the start and end speed
    return (getSpeedOnPoint(aLegIndex) + getSpeedOnPoint(aLegIndex + 1)) / 2;
  }

  private float getCourse(int aLegIndex) {
    if (aLegIndex >= (fTrajectory.getPointCount() - 1)) {
      aLegIndex--;   //after the last segment, we use the same course as the course of the last segment (#1490)
    }
    try {
      return (float) getEllipsoid().forwardAzimuth2D(fTrajectory.getPoint(aLegIndex), fTrajectory.getPoint(aLegIndex + 1));
    } catch (IndexOutOfBoundsException e) {
      return 0;
    }
  }

  //returns ms
  private long getTimeOverPoint(int aPointIndex) {
    long time = fStartDate.getTime();

    for (int i = 0; i < aPointIndex; i++) {
      int added_time = (int) (1000 * getLegLength(i) / getAverageSpeedOnLeg(i));
      time += added_time;
    }
    return time;
  }

  private float getPitch(int aLegIndex) {
    if (aLegIndex >= (fTrajectory.getPointCount() - 1)) {
      aLegIndex--;
    }
    ILcdPoint p1 = fTrajectory.getPoint(aLegIndex);
    ILcdPoint p2 = fTrajectory.getPoint(aLegIndex + 1);
    double ground_dist = getEllipsoid().geodesicDistance(p1, p2);
    double delta_z = p2.getZ() - p1.getZ();
    double tan_pitch = delta_z / ground_dist;
    return (float) Math.atan(tan_pitch);
  }

  private float getLegLength(int aLegIndex) {
    if (aLegIndex < 0 || aLegIndex > fTrajectory.getPointCount() - 2) {
      return 0;
    }
    return (float) getEllipsoid().geodesicDistance(fTrajectory.getPoint(aLegIndex), fTrajectory.getPoint(aLegIndex + 1));
  }

  private float getLegLengthTotal() {
    float length = 0;
    for (int i = 0; i < fTrajectory.getPointCount() - 1; i++) {
      length += getLegLength(i);
    }
    return length;
  }

  public void pointAtDateSFCT(Date aDate, ILcd3DEditablePoint a3DEditablePointSFCT) {
    if (!isCacheValid()) {
      calculateCache();
    }

    if (aDate == null) {
      return;
    }

    long beginTime = fDateArray[0];
    long endTime = fDateArray[fTrajectory.getPointCount() - 1];
    long timeToSet = aDate.getTime();

    if (timeToSet < beginTime) {
      a3DEditablePointSFCT.move3D(fTrajectory.getPoint(0));
    } else {
      timeToSet = Math.min(Math.max(timeToSet, beginTime), endTime);

      int pos = Arrays.binarySearch(fDateArray, timeToSet);
      if (pos >= 0) {
        a3DEditablePointSFCT.move3D(fTrajectory.getPoint(pos));
      } else {
        pos = -(pos + 2);

        //assuming linear acceleration between two successive points

        double leg_end_time = fDateArray[pos + 1] / 1000d; //convert to seconds
        double leg_start_time = fDateArray[pos] / 1000d; //convert to seconds
        double total_leg_time = leg_end_time - leg_start_time;

        double leg_start_speed = getSpeedOnPoint(pos);
        double leg_end_speed = getSpeedOnPoint(pos + 1);
        double acceleration = (leg_end_speed - leg_start_speed) / total_leg_time;

        //The time to reach from point 'pos' the point at time 'aDate'
        double time_to_point = (timeToSet / 1000d) - leg_start_time;

        //The distance traveled from point 'pos' until time 'aDate' is reached.
        double distance_to_point = (leg_start_speed * time_to_point) + ((acceleration * time_to_point * time_to_point) / 2);

        double total_leg_distance = getLegLength(pos);

        double distance_fraction = distance_to_point / total_leg_distance;

        double time_fraction = time_to_point / total_leg_time;

        //update xy
        if (total_leg_distance < 100) {
          //approximate linearly for very small segments
          ILcdPoint point1 = fTrajectory.getPoint(pos);
          ILcdPoint point2 = fTrajectory.getPoint(pos + 1);

          double dx = point2.getX() - point1.getX();
          double dy = point2.getY() - point1.getY();

          a3DEditablePointSFCT.move2D(point1.getX() + (distance_fraction * dx), point1.getY() + (distance_fraction * dy));
        } else {
          this.getEllipsoid().geodesicPointSFCT(
              fTrajectory.getPoint(pos),
              fTrajectory.getPoint(pos + 1),
              distance_fraction,
              a3DEditablePointSFCT
          );
        }

        //update the altitude
        double delta_altitude = fTrajectory.getPoint(pos + 1).getZ() - fTrajectory.getPoint(pos).getZ();
        a3DEditablePointSFCT.move3D(
            a3DEditablePointSFCT.getX(),
            a3DEditablePointSFCT.getY(),
            fTrajectory.getPoint(pos).getZ() + (time_fraction * delta_altitude)
        );
      }
    }
  }

  public double getHeadingAtDate(Date aDate) {
    if (!isCacheValid()) {
      calculateCache();
    }

    if (aDate != null) {
      if (aDate.getTime() < fDateArray[0]) {
        return Math.toDegrees(this.getCourse(0));
      }
      if (aDate.getTime() >= fDateArray[fTrajectory.getPointCount() - 1]) {
        return Math.toDegrees(this.getCourse(fTrajectory.getPointCount() - 1));
      }

        final int delta_t = 5 * 1000; // multiplier is for milliseconds
        TLcdXYZPoint p0 = new TLcdXYZPoint(), p1 = new TLcdXYZPoint();
        Date d1 = new Date(aDate.getTime() + delta_t);
        pointAtDateSFCT(aDate, p0);
        pointAtDateSFCT(d1, p1);
        return Math.toDegrees(getEllipsoid().forwardAzimuth2D(p0, p1));
    }
    return 0;
  }

  public double getPitchAtDate(Date aDate) {
    if (!isCacheValid()) {
      calculateCache();
    }

    if (aDate != null) {
      if (aDate.getTime() < fDateArray[0]) {
        return Math.toDegrees(this.getPitch(0));
      }
      if (aDate.getTime() >= fDateArray[fTrajectory.getPointCount() - 1]) {
        return Math.toDegrees(this.getPitch(fTrajectory.getPointCount() - 1));
      }

        final int delta_t = 5 * 1000; // multiplier is for milliseconds
        TLcdXYZPoint p0 = new TLcdXYZPoint(), p1 = new TLcdXYZPoint();
        Date d1 = new Date(aDate.getTime() + delta_t);
        pointAtDateSFCT(aDate, p0);
        pointAtDateSFCT(d1, p1);

        double ground_dist = getEllipsoid().geodesicDistance(p0, p1);
        double delta_z = p1.getZ() - p0.getZ();
        double tan_pitch = delta_z / ground_dist;
        return Math.toDegrees(Math.atan(tan_pitch));
    }
    return 0;
  }

  public double getRollAtDate(Date aDate) {

    if (!true) {
      return 0.0;
    }

    if (!isCacheValid()) {
      calculateCache();
    }

    if (aDate != null) {
      if (aDate.getTime() <= fDateArray[0]) {
        return 0.0;
      }
      if (aDate.getTime() >= fDateArray[fTrajectory.getPointCount() - 1]) {
        return 0.0;
      }

      final int delta_t = 5 * 1000; // multiplier is for milliseconds

      Date tminus = new Date(aDate.getTime() - delta_t);
      Date tplus = new Date(aDate.getTime() + delta_t);

      double azminus = getHeadingAtDate(tminus);
      double azplus = getHeadingAtDate(tplus);

      double roll = getForwardAzimuthDelta(azplus, azminus);
      if (isForwardAzimuthClockwise(azplus, azminus)) {
        roll = -roll;
      }
      return roll;
    }
    return 0;
  }

  public double getSegmentIndicesAtDateSFCT(Date aDate, int[] aSegmentIndicesSFCT) {
    if (!isCacheValid()) {
      calculateCache();
    }

    if (aDate == null) {
      aSegmentIndicesSFCT[0] = 0;
      aSegmentIndicesSFCT[1] = 0;
      return 0;
    }

    double fraction = 0;

    long beginTime = fDateArray[0];
    long endTime = fDateArray[fTrajectory.getPointCount() - 1];
    long timeToSet = aDate.getTime();
    timeToSet = Math.min(Math.max(timeToSet, beginTime), endTime);

    int pos = Arrays.binarySearch(fDateArray, timeToSet);
    if (pos >= 0) {
      aSegmentIndicesSFCT[0] = pos;
      aSegmentIndicesSFCT[1] = Math.min(pos + 1, fTrajectory.getPointCount() - 1);
      fraction = 0;
    } else {
      pos = -(pos + 2);

      //assuming linear acceleration between two successive points

      double leg_end_time = fDateArray[pos + 1] / 1000d; //convert to seconds
      double leg_start_time = fDateArray[pos] / 1000d; //convert to seconds
      double total_leg_time = leg_end_time - leg_start_time;

      double leg_start_speed = getSpeedOnPoint(pos);
      double leg_end_speed = getSpeedOnPoint(pos + 1);
      double acceleration = (leg_end_speed - leg_start_speed) / total_leg_time;

      //The time to reach from point 'pos' the point at time 'aDate'
      double time_to_point = (timeToSet / 1000d) - leg_start_time;

      double time_fraction = time_to_point / total_leg_time;

      aSegmentIndicesSFCT[0] = pos;
      aSegmentIndicesSFCT[1] = pos + 1;
      fraction = time_fraction;
    }

    return fraction;
  }

  public Date getBeginDate() {
    if (!isCacheValid()) {
      calculateCache();
    }
    return new Date(this.getTimeOverPoint(0));
  }

  public Date getEndDate() {
    if (!isCacheValid()) {
      calculateCache();
    }
    return new Date(fDateArray[fTrajectory.getPointCount() - 1]);
  }

  @Override
  public void insert2DPoint(int aIndex, double aLon, double aLon1) {
    flushCache();
    fSpeeds.add(aIndex, calculateSpeed(aIndex));
    super.insert2DPoint(aIndex, aLon, aLon1);
  }

  @Override
  public void insert3DPoint(int aIndex, double aLon, double aLon1, double aLon2) {
    flushCache();
    fSpeeds.add(aIndex, calculateSpeed(aIndex));
    super.insert3DPoint(aIndex, aLon, aLon1, aLon2);
  }

  private float calculateSpeed(int aNewIndex) {
    int first_index = aNewIndex - 1;

    float speed_sum = 0;
    int speed_count = 0;
    if (first_index >= 0 && first_index < getPointCount()) {
      speed_sum += fSpeeds.get(first_index);
      speed_count++;
    }
    if (aNewIndex >= 0 && aNewIndex < getPointCount()) {
      speed_sum += fSpeeds.get(aNewIndex);
      speed_count++;
    }
    if (speed_count > 0) {
      return speed_sum / (float) speed_count;
    } else {
      return fDefaultSpeed;
    }
  }

  @Override
  protected void set3DEditablePointList(ILcd3DEditablePointList aILcd3DEditablePointList) {
    //flushCache();
    fSpeeds.clear();
    for (int i = 0; i < aILcd3DEditablePointList.getPointCount(); i++) {
      fSpeeds.add(fDefaultSpeed);
    }
    super.set3DEditablePointList(aILcd3DEditablePointList);
  }

  @Override
  protected void set3DEditablePointList(ILcd3DEditablePointList aILcd3DEditablePointList, boolean aIsAdjusting) {
    flushCache();
    super.set3DEditablePointList(aILcd3DEditablePointList, aIsAdjusting);
  }

  @Override
  public void move2DPoint(int aIndex, double aLon, double aLon1) {
    flushCache();
    super.move2DPoint(aIndex, aLon, aLon1);
  }

  @Override
  public void move3DPoint(int aIndex, double aLon, double aLon1, double aLon2) {
    flushCache();
    super.move3DPoint(aIndex, aLon, aLon1, aLon2);
  }

  @Override
  public void translate2DPoint(int aIndex, double aLon, double aLon1) {
    flushCache();
    super.translate2DPoint(aIndex, aLon, aLon1);
  }

  @Override
  public void translate3DPoint(int aIndex, double aLon, double aLon1, double aLon2) {
    flushCache();
    super.translate3DPoint(aIndex, aLon, aLon1, aLon2);
  }

  @Override
  public void removePointAt(int aIndex) {
    flushCache();
    super.removePointAt(aIndex);
    fSpeeds.remove(aIndex);
  }

  @Override
  public void translate2D(double aLon, double aLon1) {
    flushCache();
    super.translate2D(aLon, aLon1);
  }

  @Override
  public void translate3D(double aLon, double aLon1, double aLon2) {
    flushCache();
    super.translate3D(aLon, aLon1, aLon2);
  }

  @Override
  public void move2D(double aLon, double aLon1) {
    flushCache();
    super.move2D(aLon, aLon1);
  }

  @Override
  public void move3D(ILcdPoint aPoint) {
    flushCache();
    super.move3D(aPoint);
  }

  @Override
  public void move3D(double aLon, double aLon1, double aLon2) {
    flushCache();
    super.move3D(aLon, aLon1, aLon2);
  }

  @Override
  public void move2D(ILcdPoint aPoint) {
    flushCache();
    super.move2D(aPoint);
  }

  /**
   * Normalize a given forward azimuth: the result is always in the interval [0;360[.
   *
   * @param aForwardAzimuth Azimuth to normalize (degrees).
   *
   * @return An angle in the range [0;360[.
   */
  private static double normalizeForwardAzimuth(double aForwardAzimuth) {
    if (aForwardAzimuth < 0 || aForwardAzimuth >= 360) {
      return (aForwardAzimuth - Math.floor(aForwardAzimuth / 360.0) * 360.0);
    } else {
      return aForwardAzimuth;
    }
  }

  /**
   * Returns the difference (in degrees) between two forward azimuths. The return value is
   * unsigned!
   */
  private static double getForwardAzimuthDelta(double aAzimuth1, double aAzimuth2) {
    double d = normalizeForwardAzimuth(aAzimuth2 - aAzimuth1);
    if (d > 180) {
      d -= 360;
    } else if (d < -180) {
      d += 360;
    }
    return Math.abs(d);
  }

  /**
   * Return true if a turn from forward azimuth 1 to 2 goes in clockwise direction.
   *
   * @param aFa1 Forward azimuth 1 (degrees).
   * @param aFa2 Forward azimuth 2 (degrees).
   *
   * @return true if a turn from forward azimuth 1 to 2 goes in clockwise direction.
   */
  private static boolean isForwardAzimuthClockwise(double aFa1, double aFa2) {
    return normalizeForwardAzimuth(aFa2 - aFa1) < 180;
  }
}
