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
package samples.lightspeed.icons3d;

import java.lang.ref.WeakReference;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;

/**
 * Animation that takes an OrientedPoint object and animates it to "fly" in a circle. That is,
 * the point's location will be animated so that it moves in a circle. Also the orientation of the
 * point is animated to mimic the movements of an airplane performing the same circular motion.
 *
 * The animation loops until the model is garbage collected.
 */
public class FlyCircleAnimation implements ILcdAnimation {

  // Constant speed at which the point flies in the circle (in meters/second)
  private final double fFlightSpeed;

  private OrientedPoint fPoint;
  private double fRadius;
  private ILcdPoint fCenter;
  private WeakReference<ILcdModel> fModel;
  private boolean fCounterClockWise;

  /**
   * Creates a new fly-circle animation.
   *  @param aModel            the model to which the point belongs
   * @param aPoint            the oriented point that is to be animated. This should be the key
   *                          that is used in the animation manager.
   * @param aCenter           the center point of the circular motion (NOTE: only the X- and Y-
 *                          coordinates are
 *                          used, for the Z-coordinate (or height) the coordinated of aPoint is
 *                          used)
   * @param aRadius           the radius of the circular motion (in meters)
   * @param aCounterClockWise determines the direction
   * @param aFlightSpeed the flight speed
   */
  public FlyCircleAnimation(ILcdModel aModel,
                            OrientedPoint aPoint,
                            ILcdPoint aCenter,
                            double aRadius,
                            boolean aCounterClockWise, double aFlightSpeed) {
    fFlightSpeed = aFlightSpeed;
    fModel = new WeakReference<ILcdModel>(aModel);
    fCenter = aCenter;
    fRadius = aRadius;
    fPoint = aPoint;
    fCounterClockWise = aCounterClockWise;
  }

  @Override
  public double getDuration() {
    return 2.0 * Math.PI * fRadius / fFlightSpeed;
  }

  @Override
  public void start() {
    fPoint.setRoll(fCounterClockWise ? -45.0 : 45.0);
  }

  @Override
  public void stop() {
    // Do nothing
  }

  @Override
  public boolean isLoop() {
    return true;
  }

  @Override
  public void restart() {
  }

  @Override
  public void setTime(double aTime) {

    // interpolate position
    double angle = (fCounterClockWise ? -1.0 : 1.0) * (aTime / getDuration()) * 360.0;

    ILcdModel model = fModel.get();
    if (model == null) {
      // stop the animation if the model has gone
      ALcdAnimationManager.getInstance().removeAnimation(fPoint);
      return;
    }
    if (model.getModelReference() instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geoRef = (ILcdGeodeticReference) model.getModelReference();
      ILcdEllipsoid ellipsoid = geoRef.getGeodeticDatum().getEllipsoid();
      ellipsoid.geodesicPointSFCT(fCenter, fRadius, angle, fPoint);
    } else {
      double x = fCenter.getX() + fRadius * Math.cos(angle);
      double y = fCenter.getY() + fRadius * Math.sin(angle);
      fPoint.move3D(x, y, fPoint.getZ());
    }

    // interpolate orientation
    fPoint.setOrientation(angle + (fCounterClockWise ? -90.0 : 90.0));

    model.elementChanged(fPoint, ILcdModel.FIRE_NOW);
  }
}
