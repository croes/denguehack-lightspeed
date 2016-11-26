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

import java.util.Calendar;

import com.luciad.shape.ALcdPoint;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;

/**
 * A <code>TrackPlot</code> is a track. i.e. a position and orientation along a
 * {@link samples.realtime.common.ITrajectory} at a specific instant of time.
 * The methods {@link #prepareTimeStamp} and {@link #applyChanges()} update the position to correspond to a
 * specific time.
 */
public class TrackPlot extends ALcdPoint implements ILcdOriented {

  private final ITrajectory fTrajectory;
  private final long fTime;
  private final double fX, fY, fZ;
  private final double fOrientation;

  /**
   * Creates a new <code>TrackPlot</code> with <code>ITrajectory</code>
   * aTrajectory.
   *
   * @param aTrajectory The <code>ITrajectory</code> of this track.
   *
   */
  public TrackPlot(ITrajectory aTrajectory, long aTime, double aX, double aY, double aZ, double aOrientation) {
    fTrajectory = aTrajectory;
    fTime = aTime;
    fX = aX;
    fY = aY;
    fZ = aZ;
    fOrientation = aOrientation;
  }

  /**
   * Returns the ID of this track, as given in the constructor.
   * @return the ID of this track.
   */
  public String getID() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(fTime);
    return fTrajectory.getID() + ", time = " + calendar.getTime();
  }

  public long getTime() {
    return fTime;
  }

  public boolean isGrounded() {
    return fTrajectory.isGroundedAtTime(fTime);
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
    return getID();
  }

  @Override
  public double getOrientation() {
    return fOrientation;
  }

  @Override
  public double getX() {
    return fX;
  }

  @Override
  public double getY() {
    return fY;
  }

  @Override
  public double getZ() {
    return fZ;
  }

  @Override
  public ILcd2DEditableBounds cloneAs2DEditableBounds() {
    return new TLcdLonLatBounds((ILcdPoint) this);
  }

  @Override
  public ILcd3DEditableBounds cloneAs3DEditableBounds() {
    return new TLcdLonLatHeightBounds((ILcdPoint) this);
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    return new TLcdLonLatPoint(this);
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    return new TLcdLonLatHeightPoint(this);
  }

  public ITrajectory getTrajectory() {
    return fTrajectory;
  }
}
