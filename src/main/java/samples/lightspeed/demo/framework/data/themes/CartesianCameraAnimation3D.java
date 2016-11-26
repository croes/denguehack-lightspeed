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
package samples.lightspeed.demo.framework.data.themes;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * Camera animation for cartesian world reference in 3D.
 */
class CartesianCameraAnimation3D extends AbstractCameraAnimation {
  // 3D
  private ILcdPoint fLocation1, fLocation2;
  private double fDistance1, fDistance2;
  private double fPitch1, fPitch2;
  private double fYaw1, fYaw2;
  private double fTotalGroundDistance;

  private ILspView fView;
  private TLspViewXYZWorldTransformation3D fViewXYZWorldTransformation;

  public CartesianCameraAnimation3D(ThemeAnimation aThemeAnimation, ILspView aView, Interpolator aInterpolator, double aDuration) {
    super(aThemeAnimation, aView, aInterpolator, aDuration);
    if (!(aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D)) {
      throw new IllegalArgumentException("View for a 3D animation should be a 3D view");
    }
    fView = aView;
    fViewXYZWorldTransformation = (TLspViewXYZWorldTransformation3D) aView
        .getViewXYZWorldTransformation();

    fLocation1 = fViewXYZWorldTransformation.getReferencePoint().cloneAs3DEditablePoint();
    fDistance1 = fViewXYZWorldTransformation.getDistance();
    fYaw1 = fViewXYZWorldTransformation.getYaw();
    fPitch1 = fViewXYZWorldTransformation.getPitch();
  }

  public void setGoalLocation(ILcdPoint aPoint) {
    fLocation2 = aPoint;
    fTotalGroundDistance = TLcdEllipsoid.DEFAULT.geodesicDistance(fLocation1, fLocation2);
  }

  public void setGoalDistance(double aDistance) {
    fDistance2 = aDistance;
  }

  public void setGoalPitch(double aPitch) {
    fPitch2 = aPitch;
    fPitch1 = getBestAngle(fPitch1, fPitch2);
  }

  public void setGoalYaw(double aYaw) {
    fYaw2 = aYaw;
    fYaw1 = getBestAngle(fYaw1, fYaw2);
  }

  public void update(double aAlpha) {
    if (Double.compare(fTotalGroundDistance, 0.0) == 0) {
      return;
    }

    double yaw = (1 - aAlpha) * fYaw1 + aAlpha * fYaw2;
    double pitch = (1 - aAlpha) * fPitch1 + aAlpha * fPitch2;

    double x = getLon(aAlpha);
    double y = (1 - aAlpha) * fLocation1.getY() + aAlpha * fLocation2.getY();
    double z = (1 - aAlpha) * fLocation1.getZ() + aAlpha * fLocation2.getZ();
    TLcdLonLatHeightPoint newPoint = new TLcdLonLatHeightPoint(x, y, z);

    // Calculate distance (using a parabolic equation)
    double t = TLcdEllipsoid.DEFAULT.geodesicDistance(fLocation1, newPoint);
    double a1 = fDistance1;
    double a2 = fDistance2;
    double D = fTotalGroundDistance;
    double distance = parabolic(getA(a1, a2, D), getB(a1, a2, D), a1, t);

    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    TLcdGeoReference2GeoReference geo2geo = new TLcdGeoReference2GeoReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()),
                                                                              (ILcdGeoReference) fView
                                                                                  .getXYZWorldReference()
    );
    try {
      geo2geo.sourcePoint2destinationSFCT(newPoint, worldPoint);
    } catch (TLcdOutOfBoundsException e) {
      worldPoint.move3D(0, 0, 0);
    }

    fViewXYZWorldTransformation.lookAt(worldPoint, distance, yaw, pitch, 0);
  }

  private double getLon(double aAlpha) {
    double oldX1 = fLocation1.getX();
    double oldX2 = fLocation2.getX();
    if (Math.abs(oldX1 - oldX2) > 180) {
      if (oldX1 > oldX2) {
        oldX2 += 360;
      } else {
        oldX1 += 360;
      }
    }
    double x = (1 - aAlpha) * oldX1 + aAlpha * oldX2;
    if (x > 180) {
      x -= 360;
    }

    return x;
  }
}
