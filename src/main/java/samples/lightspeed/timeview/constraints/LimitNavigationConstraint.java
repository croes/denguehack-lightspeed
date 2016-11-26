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
package samples.lightspeed.timeview.constraints;

import java.awt.Point;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

/**
 * Limits navigation to a certain world extent.
 *
 * This constraint works on a non-georeferenced view.
 * See {@link samples.lightspeed.limitnavigation.LimitNavigationConstraint2D} for a constraint that works on a georeferenced view.
 */
public class LimitNavigationConstraint extends ALspCameraConstraint<TLspViewXYZWorldTransformation2D> {

  private ILcd3DEditablePoint lastValidWorldOrigin;
  private Point lastValidViewOrigin;
  private ILcdBounds areaOfInterest = new TLcdXYBounds(0, 0, 0, 0);
  private TLspViewXYZWorldTransformation2D viewXYZWorldTransformation;

  public LimitNavigationConstraint(ILspView aView) {
    viewXYZWorldTransformation = new TLspViewXYZWorldTransformation2D(aView);
  }

  public void setAreaOfInterest(ILcdBounds aBounds) {
    areaOfInterest = aBounds;
    fireConstraintChangeEvent();
  }

  @Override
  public void constrain(TLspViewXYZWorldTransformation2D aSource, TLspViewXYZWorldTransformation2D aTargetSFCT) {
    double clampedScaleX = aTargetSFCT.getScaleX();

    ILspView view = aSource.getView();

    // Clamp the world and view origin to make sure the center point of the view lies in the area of interest
    ILcd3DEditablePoint clampedWorldOrigin = new TLcdXYZPoint();
    Point clampedViewOrigin = new Point();
    if (retrieveClampedOrigins(aTargetSFCT.getWorldOrigin(),
                               aTargetSFCT.getViewOrigin(),
                               clampedScaleX,
                               view,
                               clampedWorldOrigin,
                               clampedViewOrigin)) {

      // Use the clamped values
      aTargetSFCT.lookAt(clampedWorldOrigin, clampedViewOrigin, clampedScaleX, aTargetSFCT.getScaleY(), 0);
    }

    // Use the clamped values
    aTargetSFCT.lookAt(aTargetSFCT.getWorldOrigin(), aTargetSFCT.getViewOrigin(), clampedScaleX, aTargetSFCT.getScaleY(), 0);
  }

  private boolean retrieveClampedOrigins(ILcdPoint aWorldOrigin,
                                         Point aViewOrigin,
                                         double aScaleX,
                                         ILspView aView,
                                         ILcd3DEditablePoint aClampedWorldOrigin,
                                         Point aClampedViewOrigin) {
    // Check if the last valid view origin is still valid
    int centerX = (int) (aView.getWidth() / 2.0);
    int centerY = (int) (aView.getHeight() / 2.0);
    if (lastValidViewOrigin != null &&
        (lastValidViewOrigin.x != centerX || lastValidViewOrigin.y != centerY)) {
      lastValidViewOrigin = null;
    }
    // Check if the view center still lies inside the area of interest
    TLcdXYZPoint viewCenter = new TLcdXYZPoint(centerX, centerY, 0.0);
    viewXYZWorldTransformation.setSize(aView.getWidth(), aView.getHeight());
    viewXYZWorldTransformation.lookAt(aWorldOrigin, aViewOrigin, aScaleX, 1, 0);
    viewXYZWorldTransformation.viewPoint2WorldSFCT(viewCenter, aClampedWorldOrigin);
    aClampedWorldOrigin.move3D(aClampedWorldOrigin.getX(), aClampedWorldOrigin.getY(), 0.0);
    aClampedViewOrigin.setLocation(viewCenter.getX(), viewCenter.getY());

    ILcd3DEditablePoint modelOrigin = aClampedWorldOrigin.cloneAs3DEditablePoint();
    if (!areaOfInterest.contains2D(modelOrigin)) {
      // The view center doesn't lie in the area of interest -> find a better position
      if (retrieveClampedWorldOrigin(modelOrigin, aClampedWorldOrigin)) {
        // Save origins in order to be able to revert to them later if needed
        saveLastValidOrigins(aClampedWorldOrigin, aClampedViewOrigin);
        return true;
      } else {
        // Fall back on a previous valid origin
        return retrieveLastValidOrigins(aClampedWorldOrigin, aClampedViewOrigin, viewCenter);
      }
    } else {
      // Save origins in order to be able to revert to them later if needed
      saveLastValidOrigins(aClampedWorldOrigin, aClampedViewOrigin);
      return true;
    }
  }

  private boolean retrieveClampedWorldOrigin(ILcdPoint aModelPoint, ILcd3DEditablePoint aWorldOriginSFCT) {
    double x = areaOfInterest.getLocation().getX();
    double y = areaOfInterest.getLocation().getY();
    double z = areaOfInterest.getLocation().getZ();
    double w = areaOfInterest.getWidth();
    double h = areaOfInterest.getHeight();
    double d = areaOfInterest.getDepth();

    ILcd3DEditablePoint clampedModelPoint = aModelPoint.cloneAs3DEditablePoint();

    if (clampedModelPoint.getX() < x) {
      clampedModelPoint.move3D(x, clampedModelPoint.getY(), clampedModelPoint.getZ());
    } else if (clampedModelPoint.getX() > x + w) {
      clampedModelPoint.move3D(x + w, clampedModelPoint.getY(), clampedModelPoint.getZ());
    }

    // Clamp y-coordinate
    if (clampedModelPoint.getY() < y) {
      clampedModelPoint.move3D(clampedModelPoint.getX(), y, clampedModelPoint.getZ());
    } else if (clampedModelPoint.getY() > y + h) {
      clampedModelPoint.move3D(clampedModelPoint.getX(), y + h, clampedModelPoint.getZ());
    }

    // Clamp z-coordinate
    if (clampedModelPoint.getZ() < z) {
      clampedModelPoint.move3D(clampedModelPoint.getX(), clampedModelPoint.getY(), z);
    } else if (clampedModelPoint.getZ() > z + d) {
      clampedModelPoint.move3D(clampedModelPoint.getX(), clampedModelPoint.getY(), z + d);
    }

    aWorldOriginSFCT.move2D(clampedModelPoint);
    return true;
  }

  private void saveLastValidOrigins(ILcdPoint aWorldOrigin, Point aViewOrigin) {
    if (lastValidWorldOrigin == null) {
      lastValidWorldOrigin = new TLcdXYZPoint();
    }
    if (lastValidViewOrigin == null) {
      lastValidViewOrigin = new Point();
    }
    lastValidWorldOrigin.move3D(aWorldOrigin);
    lastValidViewOrigin.setLocation(aViewOrigin);
  }

  private boolean retrieveLastValidOrigins(ILcd3DEditablePoint aWorldOrigin,
                                           Point aViewOrigin,
                                           ILcdPoint aCenterPoint) {
    if (lastValidViewOrigin == null) {
      lastValidViewOrigin = new Point((int) aCenterPoint.getX(), (int) aCenterPoint.getY());
    }

    if (lastValidWorldOrigin == null) {
      lastValidWorldOrigin = new TLcdXYZPoint(areaOfInterest.getLocation());
      lastValidWorldOrigin.translate2D(areaOfInterest.getWidth() * 0.5, areaOfInterest.getHeight() * 0.5);
    }

    if (lastValidWorldOrigin != null) {
      aWorldOrigin.move3D(lastValidWorldOrigin);
      aViewOrigin.setLocation(lastValidViewOrigin);
      return true;
    }

    return false;
  }

}

