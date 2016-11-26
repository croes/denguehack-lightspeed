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
package samples.lightspeed.limitnavigation;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import samples.lightspeed.LspModelViewUtil;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

/**
 * This class add a few constraints to the navigation:
 * <ul>
 *   <li>min/max scale (see {@link #setUseMinMaxScale}, {@link #setMinScale}, {@link #setMaxScale}).</li>
 *   <li>area of interest (see {@link #setUseAreaOfInterest}, {@link #setAreaOfInterest}).</li>
 *   <li>north always up (see {@link #setUseAlwaysNorthUp(boolean)}).</li>
 * </ul>
 */
public class LimitNavigationConstraint2D extends ALspCameraConstraint<TLspViewXYZWorldTransformation2D> {

  private boolean fUseMinMaxScale = false;
  private boolean fUseAreaOfInterest = false;
  private boolean fUseAlwaysNorthUp = false;

  private double fMinScale = 0;
  private double fMaxScale = Double.MAX_VALUE;

  private ILcd3DEditablePoint fLastValidWorldOrigin;
  private Point fLastValidViewOrigin;
  private ILcdBounds fAreaOfInterest = new TLcdLonLatBounds(-180, -90, 360, 180);
  private TLcdGeoReference2GeoReference fModelWorldTransformation = new TLcdGeoReference2GeoReference();
  private TLspViewXYZWorldTransformation2D fViewXYZWorldTransformation;
  private final LspModelViewUtil fModelViewUtil = new LspModelViewUtil();

  /**
   * Creates a new limit navigation constraint for the given view.
   * @param aView a given view.
   */
  public LimitNavigationConstraint2D(ILspView aView) {
    fViewXYZWorldTransformation = new TLspViewXYZWorldTransformation2D(aView);
    fModelWorldTransformation.setSourceReference((ILcdGeoReference) new TLcdGeodeticReference());
    fModelWorldTransformation.setDestinationReference((ILcdGeoReference) aView.getXYZWorldReference());
    aView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("XYZWorldReference".equals(evt.getPropertyName())) {
          fModelWorldTransformation.setDestinationReference((ILcdGeoReference) evt.getNewValue());
          fLastValidWorldOrigin = null;
          fLastValidViewOrigin = null;
          fireConstraintChangeEvent();
        }
      }
    });
  }

  /**
   * Returns whether or not a minimum and maximum scale are used
   * by this constrained world transformation.
   *
   * @return <code>true</code> if a minimum and maximum scale are used by
   *         this constrained world transformation <code>false</code> otherwise.
   */
  public boolean isUseMinMaxScale() {
    return fUseMinMaxScale;
  }

  /**
   * Sets whether or not a minimum and maximum scale are used
   * by this constrained world transformation.
   *
   * @param aUseMinMaxScale <code>true</code> if a minimum and maximum scale are used by
   *                        this constrained world transformation <code>false</code> otherwise.
   */
  public void setUseMinMaxScale(boolean aUseMinMaxScale) {
    fUseMinMaxScale = aUseMinMaxScale;
    fireConstraintChangeEvent();
  }

  /**
   * Returns whether or not an area of interest is used by this constrained world transformation.
   *
   * @return <code>true</code> if an area of interest is used by
   *         this constrained world transformation <code>false</code> otherwise.
   */
  public boolean isUseAreaOfInterest() {
    return fUseAreaOfInterest;
  }

  /**
   * Sets whether or not an area of interest is used by this constrained world transformation.
   *
   * @param aUseAreaOfInterest <code>true</code> if an area of interest is used by
   *         this constrained world transformation <code>false</code> otherwise.
   */
  public void setUseAreaOfInterest(boolean aUseAreaOfInterest) {
    fUseAreaOfInterest = aUseAreaOfInterest;
    fireConstraintChangeEvent();
  }

  /**
   * Returns whether or not north is always up in this constrained world transformation.
   *
   * @return <code>true</code> if north is always up in
   *         this constrained world transformation <code>false</code> otherwise.
   */
  public boolean isUseAlwaysNorthUp() {
    return fUseAlwaysNorthUp;
  }

  /**
   * Sets whether or not north is always up in this constrained world transformation.
   *
   * @param aUseAlwaysNorthUp <code>true</code> if north is always up in
   *                          this constrained world transformation <code>false</code> otherwise.
   */
  public void setUseAlwaysNorthUp(boolean aUseAlwaysNorthUp) {
    fUseAlwaysNorthUp = aUseAlwaysNorthUp;
    fireConstraintChangeEvent();
  }

  /**
   * Returns the minimum scale used by this constrained world transformation.
   *
   * @return the minimum scale.
   */
  public double getMinScale() {
    return fMinScale;
  }

  /**
   * Sets the minimum scale used by this constrained world transformation.
   *
   * @param aMinScale the minimum scale.
   */
  public void setMinScale(double aMinScale) {
    fMinScale = aMinScale;
    fireConstraintChangeEvent();
  }

  /**
   * Returns the maximum scale used by this constrained world transformation.
   *
   * @return the maximum scale.
   */
  public double getMaxScale() {
    return fMaxScale;
  }

  /**
   * Sets the maximum scale used by this constrained world transformation.
   *
   * @param aMaxScale the maximum scale.
   */
  public void setMaxScale(double aMaxScale) {
    fMaxScale = aMaxScale;
    fireConstraintChangeEvent();
  }

  /**
   * Returns the area of interest as bounds used by this constrained world transformation.
   *
   * @return the area of interest.
   */
  public ILcdBounds getAreaOfInterest() {
    return fAreaOfInterest;
  }

  /**
   * Returns the area of interest reference used by this constrained world transformation.
   *
   * @return the area of interest reference.
   */
  public ILcdGeoReference getAreaOfInterestReference() {
    return fModelWorldTransformation.getSourceReference();
  }

  /**
   * Sets the area of interest bounds and reference used by this constrained world transformation.
   *
   * @param aAreaOfInterest the area of interest as bounds.
   * @param aAreaOfInterestReference the area of interest reference.
   */
  public void setAreaOfInterest(ILcdBounds aAreaOfInterest, ILcdGeoReference aAreaOfInterestReference) {
    fAreaOfInterest = aAreaOfInterest;
    fModelWorldTransformation.setSourceReference(aAreaOfInterestReference);
    fireConstraintChangeEvent();
  }

  @Override
  public void constrain(TLspViewXYZWorldTransformation2D aSource, TLspViewXYZWorldTransformation2D aTargetSFCT) {
    double clampedScaleX = retrieveClampedScale(aTargetSFCT.getScaleX());
    double clampedScaleY = retrieveClampedScale(aTargetSFCT.getScaleY());

    ILspView view = aSource.getView();

    // correct the rotation using difference in angle
    double clampedRotation = aTargetSFCT.getRotation();
    if (fUseAlwaysNorthUp) {
      double angleToNorth = fModelViewUtil.viewAzimuth(view, aTargetSFCT, null);
      if (!Double.isNaN(angleToNorth)) {
        clampedRotation += angleToNorth;
        while (clampedRotation > 360.0) {
          clampedRotation -= 360.0;
        }
      }
    }

    // Clamp the world and view origin to make sure the center point of the view lies in the area of interest
    ILcd3DEditablePoint clampedWorldOrigin = new TLcdXYZPoint();
    Point clampedViewOrigin = new Point();
    if (retrieveClampedOrigins(aTargetSFCT.getWorldOrigin(), aTargetSFCT.getViewOrigin(), clampedScaleX, clampedScaleY, clampedRotation, view, clampedWorldOrigin, clampedViewOrigin)) {
      // Use the clamped values
      aTargetSFCT.lookAt(clampedWorldOrigin, clampedViewOrigin, clampedScaleX, clampedScaleY, clampedRotation);
    }

    // We adjusted the view/world origin => make sure north is still up
    clampedRotation = aTargetSFCT.getRotation();
    if (fUseAlwaysNorthUp) {
      double angleToNorth = fModelViewUtil.viewAzimuth(view, aTargetSFCT, null);
      if (!Double.isNaN(angleToNorth)) {
        clampedRotation += angleToNorth;
        while (clampedRotation > 360.0) {
          clampedRotation -= 360.0;
        }
      }
    }

    // Use the clamped values
    aTargetSFCT.lookAt(aTargetSFCT.getWorldOrigin(), aTargetSFCT.getViewOrigin(), clampedScaleX, clampedScaleY, clampedRotation);
  }

  private double retrieveClampedScale(double aScale) {
    if (fUseMinMaxScale) {
      if (aScale < fMinScale) {
        aScale = fMinScale;
      }
      if (aScale > fMaxScale) {
        aScale = fMaxScale;
      }
    }
    return aScale;
  }

  private boolean retrieveClampedOrigins(ILcdPoint aWorldOrigin, Point aViewOrigin, double aScaleX, double aScaleY, double aRotation, ILspView aView, ILcd3DEditablePoint aClampedWorldOrigin, Point aClampedViewOrigin) {
    if (fUseAreaOfInterest) {
      // Check if the last valid view origin is still valid
      int centerX = (int) (aView.getWidth() / 2.0);
      int centerY = (int) (aView.getHeight() / 2.0);
      if (fLastValidViewOrigin != null && (fLastValidViewOrigin.x != centerX || fLastValidViewOrigin.y != centerY)) {
        fLastValidViewOrigin = null;
      }
      // Check if the view center still lies inside the area of interest
      TLcdXYZPoint viewCenter = new TLcdXYZPoint(centerX, centerY, 0.0);
      fViewXYZWorldTransformation.setSize(aView.getWidth(), aView.getHeight());
      fViewXYZWorldTransformation.lookAt(aWorldOrigin, aViewOrigin, aScaleX, aScaleY, aRotation);
      fViewXYZWorldTransformation.viewPoint2WorldSFCT(viewCenter, aClampedWorldOrigin);
      aClampedWorldOrigin.move3D(aClampedWorldOrigin.getX(), aClampedWorldOrigin.getY(), 0.0);
      aClampedViewOrigin.setLocation(viewCenter.getX(), viewCenter.getY());

      try {
        ILcd3DEditablePoint modelOrigin = new TLcdXYZPoint();
        fModelWorldTransformation.destinationPoint2sourceSFCT(aClampedWorldOrigin, modelOrigin);
        if (!fAreaOfInterest.contains2D(modelOrigin)) {
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
      } catch (TLcdOutOfBoundsException e) {
        // Revert to position in the area of interest
        return retrieveLastValidOrigins(aClampedWorldOrigin, aClampedViewOrigin, viewCenter);
      }
    } else {
      // Don't modify the world and view origin
      aClampedWorldOrigin.move3D(aWorldOrigin);
      aClampedViewOrigin.setLocation(aViewOrigin);
      return true;
    }
  }

  private boolean retrieveClampedWorldOrigin(ILcdPoint aModelPoint, ILcd3DEditablePoint aWorldOriginSFCT) {
    double x = fAreaOfInterest.getLocation().getX();
    double y = fAreaOfInterest.getLocation().getY();
    double z = fAreaOfInterest.getLocation().getZ();
    double w = fAreaOfInterest.getWidth();
    double h = fAreaOfInterest.getHeight();
    double d = fAreaOfInterest.getDepth();

    ILcd3DEditablePoint clampedModelPoint = aModelPoint.cloneAs3DEditablePoint();

    boolean geodetic = fModelWorldTransformation.getSourceReference().getCoordinateType() == ILcdGeoReference.GEODETIC;

    // Clamp x-coordinate
    if (geodetic) {
      double lon = clampedModelPoint.getX();
      while (lon < x) {
        lon += 360;
      }
      while (lon > x + w) {
        lon -= 360;
      }

      if (lon < x || lon > x + w) {
        // The largest longitude that is smaller than x
        double smallLon = lon;
        while (smallLon > x) {
          smallLon -= 360;
        }

        // The smallest longitude that is larger than x + w
        double largeLon = lon;
        while (largeLon < x + w) {
          largeLon += 360;
        }

        double smallDiff = x - smallLon;
        double largeDiff = largeLon - (x + w);
        if (smallDiff < largeDiff) {
          clampedModelPoint.move3D(x, clampedModelPoint.getY(), clampedModelPoint.getZ());
        } else {
          clampedModelPoint.move3D(x + w, clampedModelPoint.getY(), clampedModelPoint.getZ());
        }
      }
    } else {
      if (clampedModelPoint.getX() < x) {
        clampedModelPoint.move3D(x, clampedModelPoint.getY(), clampedModelPoint.getZ());
      } else if (clampedModelPoint.getX() > x + w) {
        clampedModelPoint.move3D(x + w, clampedModelPoint.getY(), clampedModelPoint.getZ());
      }
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

    try {
      fModelWorldTransformation.sourcePoint2destinationSFCT(clampedModelPoint, aWorldOriginSFCT);
      return true;
    } catch (TLcdOutOfBoundsException e) {
      return false;
    }
  }

  private void saveLastValidOrigins(ILcdPoint aWorldOrigin, Point aViewOrigin) {
    if (fLastValidWorldOrigin == null) {
      fLastValidWorldOrigin = new TLcdXYZPoint();
    }
    if (fLastValidViewOrigin == null) {
      fLastValidViewOrigin = new Point();
    }
    fLastValidWorldOrigin.move3D(aWorldOrigin);
    fLastValidViewOrigin.setLocation(aViewOrigin);
  }

  private boolean retrieveLastValidOrigins(ILcd3DEditablePoint aWorldOrigin, Point aViewOrigin, ILcdPoint aCenterPoint) {
    if (fLastValidViewOrigin == null) {
      fLastValidViewOrigin = new Point((int) aCenterPoint.getX(), (int) aCenterPoint.getY());
    }

    if (fLastValidWorldOrigin == null) {
      fLastValidWorldOrigin = new TLcdXYZPoint();
      ILcd3DEditablePoint point = new TLcdXYZPoint(fAreaOfInterest.getLocation());
      point.translate2D(fAreaOfInterest.getWidth() * 0.5, fAreaOfInterest.getHeight() * 0.5);
      try {
        fModelWorldTransformation.sourcePoint2destinationSFCT(point, fLastValidWorldOrigin);
      } catch (TLcdOutOfBoundsException e) {
        fLastValidWorldOrigin = null;
        fLastValidViewOrigin = null;
      }
    }

    if (fLastValidWorldOrigin != null) {
      aWorldOrigin.move3D(fLastValidWorldOrigin);
      aViewOrigin.setLocation(fLastValidViewOrigin);
      return true;
    }

    return false;
  }

}
