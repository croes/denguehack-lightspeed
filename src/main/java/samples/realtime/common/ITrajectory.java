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

/**
 * An <code>ITrajectory</code> is the representation of a trajectory or
 * flight profile of a track.
 * It has utility methods to return the position along the trajectory at a
 * given time, and to return whether or not the track is on the ground at a
 * given time.
 */
public interface ITrajectory {

  /**
   * Moves aPointSFCT to the position corresponding to aTime.
   *
   * @param aTime      The time to which the new position has to correspond. 
   *                   Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *                   similar to {@link java.util.Date#getTime}
   * @param aPointSFCT A side-effect point that will be moved to the position
   *                   corresponding to aTime.
   */
  public void getPositionAtTimeSFCT(long aTime, ILcd3DEditablePoint aPointSFCT);

  /**
   * Moves aPointSFCT to the position corresponding to aTime.
   *
   * @param aTime The time to which the new position has to correspond.
   *              Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *              similar to {@link java.util.Date#getTime}
   * @return The orientation at the time {@code aTime}.
   */
  public double getOrientationAtTimeSFCT(long aTime);

  /**
   * Returns whether the track corresponding to this trajectory is on the ground
   * at aTime.
   *
   * @param aTime The time to check on.
   *             Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *             similar to {@link java.util.Date#getTime}
   * @return <tt>true</tt> if the track is on the ground at aTime, <tt>false</tt>
   *         otherwise.
   */
  public boolean isGroundedAtTime(long aTime);

  /**
   * Returns the begin time of the track. This is useful to initialize the track.
   *
   * @return the begin time of the track.
   *         Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *         similar to {@link java.util.Date#getTime}
   */
  public long getBeginTime();

  /**
   * Returns the end time of the track.
   *
   * @return the end time of the track.
   *         Defined as the number of milliseconds since January 1, 1970, 00:00:00 GMT,
   *         similar to {@link java.util.Date#getTime}
   */
  public long getEndTime();

  public ILcdPoint getDeparture();
  public ILcdPoint getDestination();

  /**
   * @return the ellipsoid
   */
  ILcdEllipsoid getEllipsoid();

  /**
   * @return the ID of this trajectory
   */
  public String getID();
}
