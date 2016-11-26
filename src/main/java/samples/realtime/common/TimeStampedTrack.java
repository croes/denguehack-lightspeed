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

import java.util.LinkedList;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;

/**
 * A <code>TimeStampedTrack</code> is a track. i.e. a position and orientation along a
 * {@link ITrajectory} at a specific instant of time.
 * The methods {@link #prepareTimeStamp} and {@link #applyChanges()} update the position to correspond to a
 * specific time.
 */
public class TimeStampedTrack extends TLcdLonLatHeightPoint implements ILcdOriented {

  private final ITrajectory fTrajectory;
  private final LimitedQueue fPreviousLocations;
  private double fOrientation;
  private boolean fGrounded = true;
  private long fTime = -1;

  private boolean fChangesPending;
  private final TLcdLonLatHeightPoint fPendingPosition = new TLcdLonLatHeightPoint();
  private double fPendingOrientation;
  private boolean fPendingAddPreviousLocation;
  private boolean fPendingGrounded;

  private String fDisplayName;

  /**
   * Creates a new <code>TimeStampedTrack</code> with <code>ITrajectory</code>
   * aTrajectory.
   *
   * @param aTrajectory The <code>ITrajectory</code> of this track.
   */
  public TimeStampedTrack(ITrajectory aTrajectory) {
    fTrajectory = aTrajectory;
    fPreviousLocations = new LimitedQueue();
  }

  /**
   * Updates the position of this track to the position on its trajectory that
   * corresponds to aTime.
   *
   * @param aTime The time to update to.
   */
  public void updateToTimeStamp(long aTime) {
    fTrajectory.getPositionAtTimeSFCT(aTime, this);
    fOrientation = fTrajectory.getOrientationAtTimeSFCT(aTime);
    fGrounded = fTrajectory.isGroundedAtTime(aTime);
    fTime = aTime;
    fChangesPending = false;
  }

  /**
   * Prepares this time stamped track for the given time.
   * <p/>
   * This method performs all necessary computations for the time stamp without updating the
   * visible state of this track.
   * @param aTime the time for which to prepare this time stamped track
   */
  public void prepareTimeStamp(long aTime) {
    fTrajectory.getPositionAtTimeSFCT(aTime, fPendingPosition);
    if (!fPendingPosition.contains2D(getLon(), getLat())) {
      long dt = aTime - fTime;
      fChangesPending = true;
      //Only add the point to the list of previous locations if the distance with the last added point
      //is big enough
      synchronized (fPreviousLocations) {
        if (fTime == -1 || dt < 0) {
          fPreviousLocations.clear();
        } else if ((fPreviousLocations.isEmpty()) ||
                   (fTrajectory.getEllipsoid().geodesicDistance(fPreviousLocations.getLast(), this) > 70000)) {
          fPendingAddPreviousLocation = true;
        }
      }
      fPendingGrounded = fTrajectory.isGroundedAtTime(aTime);
      if (fTime == -1) {
        fPendingOrientation = fTrajectory.getOrientationAtTimeSFCT(aTime);
      } else if (dt > 1000) {
        fPendingOrientation = Math.toDegrees(fTrajectory.getEllipsoid().forwardAzimuth2D(this, fPendingPosition));
      } else if (dt < -1000) {
        fPendingOrientation = Math.toDegrees(fTrajectory.getEllipsoid().forwardAzimuth2D(fPendingPosition, this));
      }
      fTime = aTime;
    }
  }

  /**
   * Applies the previously {@link #prepareTimeStamp(long) prepared} time stamp.
   */
  public void applyChanges() {
    if (fChangesPending) {
      if (fPendingAddPreviousLocation) {
        fPreviousLocations.add(cloneAs3DEditablePoint());
        fPendingAddPreviousLocation = false;
      }
      move3D(fPendingPosition);
      fOrientation = fPendingOrientation;
      fGrounded = fPendingGrounded;
      fChangesPending = false;
    }
  }

  /**
   * Returns the ID of this track, as given in the constructor.
   * @return the ID of this track.
   */
  public String getID() {
    return fTrajectory.getID();
  }

  /**
   * Returns a previous position visited along this track
   *
   * @param aIndex the position index
   *
   * @return the previous position visited along this track or {@code null}
   */
  public ILcdPoint getPreviousLocation(int aIndex) {
    synchronized (fPreviousLocations) {
      return aIndex < fPreviousLocations.size() ? fPreviousLocations.get(aIndex) : null;
    }
  }

  public boolean isGrounded() {
    return fGrounded;
  }

  /**
   * Reverted to implementation of Object to avoid that tracks are equal if they
   * happen to be at the same position.
   */
  @Override
  public boolean equals(Object aObject) {
    return aObject == this;
  }

  /**
   * Reverted to implementation of Object to avoid that tracks are equal if they
   * happen to be at the same position.
   */
  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "TimeStampedTrack{" +
           "Id =" + getID() +
           '}';
  }

  @Override
  public double getOrientation() {
    return fOrientation;
  }

  public ITrajectory getTrajectory() {
    return fTrajectory;
  }

  public String getDisplayName() {
    return fDisplayName;
  }

  public void setDisplayName(String aDisplayName) {
    fDisplayName = aDisplayName;
  }

  /**
   * Implementation of a linked list that can only hold 5 ILcd3DEditablePoint instances and will remove
   * the first added point if no more space is available.
   */
  private class LimitedQueue extends LinkedList<ILcd3DEditablePoint> {
    @Override
    public synchronized boolean add(ILcd3DEditablePoint aPoint) {
      super.add(aPoint);
      while (size() > 5) {
        super.remove();
      }
      return true;
    }
  }
}
