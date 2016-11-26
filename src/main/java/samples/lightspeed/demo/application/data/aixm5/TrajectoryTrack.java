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

import java.util.Date;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcd3DOriented;

/**
 * <p>A {@code TrajectoryTrack} is a position along a {@code Trajectory} at a specific instant of
 * time.</p>
 *
 * <p>The method {@code updateToTimeStamp} updates the position to correspond to a specific {@code
 * Date}.</p>
 *
 * @see Trajectory
 */
final class TrajectoryTrack implements ILcd3DOriented, ILcdPoint {
  private Trajectory fTrajectory;
  private double fHeading, fPitch, fRoll;
  private ILcd3DEditablePoint fTempPoint = new TLcdXYZPoint();

  /**
   * Creates a new track for a {@code Trajectory}
   *
   * @param aTrajectory The <code>Trajectory</code> of this track.
   */
  public TrajectoryTrack(Trajectory aTrajectory) {
    fTrajectory = aTrajectory;
  }

  /**
   * Updates the position of this track to the position on its trajectory that corresponds to
   * aDate.
   *
   * @param aDate The Date to update to.
   */
  public void updateToTimeStamp(Date aDate) {
    fTrajectory.pointAtDateSFCT(aDate, fTempPoint);
    fHeading = fTrajectory.getHeadingAtDate(aDate);
    fPitch = fTrajectory.getPitchAtDate(aDate);
    fRoll = fTrajectory.getRollAtDate(aDate);
  }

  @Override
  public double getOrientation() {
    return fHeading;
  }

  @Override
  public double getPitch() {
    return fPitch;
  }

  @Override
  public double getRoll() {
    return fRoll;
  }

  @Override
  public boolean contains3D(ILcdPoint aPoint) {
    return fTempPoint.contains3D(aPoint);
  }

  @Override
  public boolean contains3D(double aLon, double aLon1, double aLon2) {
    return fTempPoint.contains3D(aLon, aLon1, aLon2);
  }

  @Override
  public ILcdBounds getBounds() {
    return fTempPoint.getBounds();
  }

  @Override
  public ILcdPoint getFocusPoint() {
    return fTempPoint.getFocusPoint();
  }

  @Override
  public boolean contains2D(ILcdPoint aPoint) {
    return fTempPoint.contains2D(aPoint);
  }

  @Override
  public boolean contains2D(double aLon, double aLon1) {
    return fTempPoint.contains2D(aLon, aLon1);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public double getX() {
    return fTempPoint.getX();
  }

  @Override
  public double getY() {
    return fTempPoint.getY();
  }

  @Override
  public double getZ() {
    return fTempPoint.getZ();
  }

  @Override
  public double getCosX() {
    return fTempPoint.getCosX();
  }

  @Override
  public double getCosY() {
    return fTempPoint.getCosY();
  }

  @Override
  public double getSinX() {
    return fTempPoint.getSinX();
  }

  @Override
  public double getSinY() {
    return fTempPoint.getSinY();
  }

  @Override
  public double getTanX() {
    return fTempPoint.getTanX();
  }

  @Override
  public double getTanY() {
    return fTempPoint.getTanY();
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    return new TLcdXYPoint(getX(), getY());
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    return new TLcdXYZPoint(getX(), getY(), getZ());
  }
}
