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

import java.awt.Point;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

/**
 * Camera animation for 2D views.
 */
class CameraAnimation2D extends AbstractCameraAnimation {
  private ILcdPoint fWorldOrigin1, fWorldOrigin2;
  private double fViewOriginX1, fViewOriginX2;
  private double fViewOriginY1, fViewOriginY2;
  private double fScaleX1, fScaleX2;
  private double fScaleY1, fScaleY2;
  private double fRotation1, fRotation2;

  private TLspViewXYZWorldTransformation2D fViewXYZWorldTransformation;

  public CameraAnimation2D(ThemeAnimation aThemeAnimation, ILspView aView, Interpolator aInterpolator, double aDuration) {
    super(aThemeAnimation, aView, aInterpolator, aDuration);
    if (!(aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation2D)) {
      throw new IllegalArgumentException("View for a 2D animation should be a 2D view");
    }
    fViewXYZWorldTransformation = (TLspViewXYZWorldTransformation2D) aView
        .getViewXYZWorldTransformation();

    fWorldOrigin1 = fViewXYZWorldTransformation.getWorldOrigin().cloneAs2DEditablePoint();
    fViewOriginX1 = (double) fViewXYZWorldTransformation
        .getViewOrigin().x / (double) fViewXYZWorldTransformation.getWidth();
    fViewOriginY1 = (double) fViewXYZWorldTransformation
        .getViewOrigin().y / (double) fViewXYZWorldTransformation.getHeight();
    fScaleX1 = fViewXYZWorldTransformation.getScaleX();
    fScaleY1 = fViewXYZWorldTransformation.getScaleY();
    fRotation1 = fViewXYZWorldTransformation.getRotation();
  }

  public void setGoalWorldOrigin(ILcdPoint aWorldOrigin) {
    fWorldOrigin2 = aWorldOrigin;
  }

  public void setGoalViewOrigin(double aX, double aY) {
    fViewOriginX2 = aX;
    fViewOriginY2 = aY;
  }

  public void setGoalScale(double aScaleX, double aScaleY) {
    fScaleX2 = aScaleX;
    fScaleY2 = aScaleY;
  }

  public void setGoalRotation(double aRotation) {
    fRotation2 = aRotation;
//    fRotation1 = getBestAngle( fRotation1, fRotation2 );
    while (fRotation2 > fRotation1 + 180) {
      fRotation2 -= 360;
    }
    while (fRotation2 < fRotation1 - 180) {
      fRotation2 += 360;
    }
  }

  private double scaleToDistance(double aScaleX) {
    return fViewXYZWorldTransformation.getWidth() / aScaleX;
  }

  private double distanceToScale(double aDistance) {
    return fViewXYZWorldTransformation.getWidth() / aDistance;
  }

  public void update(double aFAlphaAlpha) {
    double wx = (1 - aFAlphaAlpha) * fWorldOrigin1.getX() + aFAlphaAlpha * fWorldOrigin2.getX();
    double wy = (1 - aFAlphaAlpha) * fWorldOrigin1.getY() + aFAlphaAlpha * fWorldOrigin2.getY();
    double vx = (1 - aFAlphaAlpha) * fViewOriginX1 + aFAlphaAlpha * fViewOriginX2;
    double vy = (1 - aFAlphaAlpha) * fViewOriginY1 + aFAlphaAlpha * fViewOriginY2;
    double r = (1 - aFAlphaAlpha) * fRotation1 + aFAlphaAlpha * fRotation2;
    TLcdXYPoint worldOrigin = new TLcdXYPoint(wx, wy);
    double D = TLcdCartesian.distance2D(fWorldOrigin1, fWorldOrigin2);

    // Avoiding division by zero in distanceToScale, doing linear interpolation instead
    double newScale;
    if (D < 1e-5) {
      newScale = (1 - aFAlphaAlpha) * fScaleX1 + aFAlphaAlpha * fScaleX2;
    } else {
      double t = TLcdCartesian.distance2D(fWorldOrigin1, worldOrigin);
      double a1 = scaleToDistance(fScaleX1);
      double a2 = scaleToDistance(fScaleX2);
      double distance = parabolic(getA(a1, a2, D), getB(a1, a2, D), a1, t);
      newScale = distanceToScale(distance);
    }

    double width = fViewXYZWorldTransformation.getWidth();
    double height = fViewXYZWorldTransformation.getHeight();

    vx *= width;
    vy *= height;

    Point viewOrigin = new Point((int) vx, (int) vy);

    fViewXYZWorldTransformation.lookAt(worldOrigin, viewOrigin, newScale, newScale, r);

  }

}
