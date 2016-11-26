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
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * Camera animation for geocentric world reference in 3D.
 */
class GeocentricCameraAnimation3D extends AbstractCameraAnimation {

  // 3D
  private ILcdPoint fLocation1, fLocation2;
  private double fDistance1, fDistance2;
  private double fPitch1, fPitch2;
  private double fYaw1, fYaw2;
  private double fTotalGroundDistance;

  private TLspViewXYZWorldTransformation3D fViewXYZWorldTransformation;
  private TLcdLonLatHeightPoint fLonLatA;
  private TLcdLonLatHeightPoint fLonLatB;
  private TLcdLonLatHeightPoint fTemp;

  public GeocentricCameraAnimation3D(ThemeAnimation aAnimation, ILspView aView, Interpolator aInterpolator, double aDuration) {
    super(aAnimation, aView, aInterpolator, aDuration);
    if (!(aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D)) {
      throw new IllegalArgumentException("View for a 3D animation should be a 3D view");
    }
    fViewXYZWorldTransformation = (TLspViewXYZWorldTransformation3D) aView.getViewXYZWorldTransformation();

    fLocation1 = fViewXYZWorldTransformation.getReferencePoint().cloneAs3DEditablePoint();
    fDistance1 = fViewXYZWorldTransformation.getDistance();
    fYaw1 = fViewXYZWorldTransformation.getYaw();
    fPitch1 = fViewXYZWorldTransformation.getPitch();

    fLonLatA = new TLcdLonLatHeightPoint();
    fLonLatB = new TLcdLonLatHeightPoint();
    fTemp = new TLcdLonLatHeightPoint();
  }

  public void setGoalLocation(ILcdPoint aPoint) {
    fLocation2 = aPoint;

    TLcdEllipsoid.DEFAULT.geoc2llhSFCT(fLocation1, fLonLatA);
    TLcdEllipsoid.DEFAULT.geoc2llhSFCT(fLocation2, fLonLatB);

    fTotalGroundDistance = TLcdEllipsoid.DEFAULT.geodesicDistance(fLonLatA, fLonLatB);
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
    if (Math.abs(fYaw2 - fYaw1) > 180) {
      if (fYaw1 < fYaw2) {
        fYaw1 += 360;
      } else {
        fYaw2 += 360;
      }
    }
  }

  public void update(double aAlpha) {
    if (Double.compare(fTotalGroundDistance, 0.0) == 0) {
      return;
    }

    double yaw = (1 - aAlpha) * fYaw1 + aAlpha * fYaw2;
    double pitch = (1 - aAlpha) * fPitch1 + aAlpha * fPitch2;

    // calculate new lon lat coordinates
    double x = getLon(aAlpha, fLonLatA, fLonLatB);
    double y = (1 - aAlpha) * fLonLatA.getY() + aAlpha * fLonLatB.getY();
    double z = (1 - aAlpha) * fLonLatA.getZ() + aAlpha * fLonLatB.getZ();
    fTemp.move3D(x, y, z);
    TLcdXYZPoint newPoint = new TLcdXYZPoint();
    TLcdEllipsoid.DEFAULT.llh2geocSFCT(fTemp, newPoint);

    // Calculate distance (using a parabolic equation)
    double t = TLcdEllipsoid.DEFAULT.geodesicDistance(fLonLatA, fTemp);
    double a1 = fDistance1;
    double a2 = fDistance2;
    double D = fTotalGroundDistance;
    double distance = parabolic(getA(a1, a2, D), getB(a1, a2, D), a1, t);

    fViewXYZWorldTransformation.lookAt(newPoint, distance, yaw, pitch, 0);
  }

  private double getLon(double aAlpha, ILcdPoint aA, ILcdPoint aB) {
    double oldX1 = aA.getX();
    double oldX2 = aB.getX();
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

