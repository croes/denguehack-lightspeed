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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * This class add a few constraints to the navigation:
 * <ul>
 *   <li>min/max camera height (see {@link #setUseMinMaxHeight}, {@link #setMinHeight}, {@link #setMaxHeight}).</li>
 *   <li>area of interest (see {@link #setUseAreaOfInterest}, {@link #setAreaOfInterest}).</li>
 *   <li>north always up (see {@link #setUseAlwaysNorthUp}).</li>
 * </ul>
 */
public class LimitNavigationConstraint3D extends ALspCameraConstraint<TLspViewXYZWorldTransformation3D> {

  private boolean fUseMinMaxHeight = false;
  private boolean fUseAreaOfInterest = false;
  private boolean fUseAlwaysNorthUp = false;

  private double fMinHeight = 0.0;
  private double fMaxHeight = Double.MAX_VALUE;

  private ILcd3DEditablePoint fLastValidReferencePoint;
  private ILcdBounds fAreaOfInterest = new TLcdLonLatBounds(-180, -90, 360, 180);
  private TLcdGeoReference2GeoReference fModelWorldTransformation = new TLcdGeoReference2GeoReference();

  /**
   * Creates a new limit navigation constraint for the given view.
   * @param aView a view.
   */
  public LimitNavigationConstraint3D(ILspView aView) {
    fModelWorldTransformation.setSourceReference((ILcdGeoReference) new TLcdGeodeticReference());
    fModelWorldTransformation.setDestinationReference((ILcdGeoReference) aView.getXYZWorldReference());
    aView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("XYZWorldReference".equals(evt.getPropertyName())) {
          fModelWorldTransformation.setDestinationReference((ILcdGeoReference) evt.getNewValue());
          fLastValidReferencePoint = null;
          fireConstraintChangeEvent();
        }
      }
    });
  }

  /**
   * Returns whether or not a minimum and maximum height are used
   * by this constrained world transformation.
   *
   * @return <code>true</code> if a minimum and maximum height are used by
   *         this constrained world transformation <code>false</code> otherwise.
   */
  public boolean isUseMinMaxHeight() {
    return fUseMinMaxHeight;
  }

  /**
   * Sets whether or not a minimum and maximum height are used
   * by this constrained world transformation.
   *
   * @param aUseMinMaxHeight <code>true</code> if a minimum and maximum height are used by
   *                        this constrained world transformation <code>false</code> otherwise.
   */
  public void setUseMinMaxHeight(boolean aUseMinMaxHeight) {
    fUseMinMaxHeight = aUseMinMaxHeight;
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
   *                           this constrained world transformation <code>false</code> otherwise.
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
   * Returns the minimum height used by this constrained world transformation.
   *
   * @return the minimum height.
   */
  public double getMinHeight() {
    return fMinHeight;
  }

  /**
   * Sets the minimum height used by this constrained world transformation.
   *
   * @param aMinHeight the minimum height.
   */
  public void setMinHeight(double aMinHeight) {
    fMinHeight = aMinHeight;
    fireConstraintChangeEvent();
  }

  /**
   * Returns the maximum height used by this constrained world transformation.
   *
   * @return the maximum height.
   */
  public double getMaxHeight() {
    return fMaxHeight;
  }

  /**
   * Sets the maximum height used by this constrained world transformation.
   *
   * @param aMaxHeight the maximum height.
   */
  public void setMaxHeight(double aMaxHeight) {
    fMaxHeight = aMaxHeight;
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
  public void constrain(TLspViewXYZWorldTransformation3D aSource, TLspViewXYZWorldTransformation3D aTargetSFCT) {
    // The height of the eye point can change when clamping the reference point.
    // This is corrected later on.
    double desiredEyeHeight = heightAboveEllipsoid(aTargetSFCT.getEyePoint());

    // Clamp the yaw
    double clampedYaw = retrieveClampedYaw(aTargetSFCT.getYaw());

    // Clamp the yaw and reference point
    TLcdXYZPoint referencePoint = new TLcdXYZPoint();
    boolean wasClamped = retrieveClampedReferencePoint(aTargetSFCT, referencePoint);
    if (wasClamped) {
      // Modify the reference point and the yaw
      aTargetSFCT.lookAt(referencePoint, aTargetSFCT.getDistance(), clampedYaw, aTargetSFCT.getPitch(), aTargetSFCT.getRoll());
    } else if (clampedYaw != aTargetSFCT.getYaw()) {
      // Modify the yaw
      aTargetSFCT.lookAt(aTargetSFCT.getReferencePoint(), aTargetSFCT.getDistance(), clampedYaw, aTargetSFCT.getPitch(), aTargetSFCT.getRoll());
    }

    // Adjust the distance to make sure the height is within the min/max height zone. Also correct
    // the eye point height changes that were done when clamping the reference point.
    double currentEyeHeight = heightAboveEllipsoid(aTargetSFCT.getEyePoint());
    desiredEyeHeight = retrieveClampedHeight(desiredEyeHeight);

    if (wasClamped || Double.compare(currentEyeHeight, desiredEyeHeight) != 0) {
      double referenceHeight = heightAboveEllipsoid(aTargetSFCT.getReferencePoint());
      double t = (desiredEyeHeight - referenceHeight) / (currentEyeHeight - referenceHeight);
      ILcdPoint clampedEyePoint = new TLcdXYZPoint(
          aTargetSFCT.getReferencePoint().getX() + t * (aTargetSFCT.getEyePoint().getX() - aTargetSFCT.getReferencePoint().getX()),
          aTargetSFCT.getReferencePoint().getY() + t * (aTargetSFCT.getEyePoint().getY() - aTargetSFCT.getReferencePoint().getY()),
          aTargetSFCT.getReferencePoint().getZ() + t * (aTargetSFCT.getEyePoint().getZ() - aTargetSFCT.getReferencePoint().getZ())
      );
      double newDistance = TLcdCartesian.distance3D(aTargetSFCT.getReferencePoint(), clampedEyePoint);
      aTargetSFCT.lookAt(aTargetSFCT.getReferencePoint(), newDistance, aTargetSFCT.getYaw(), aTargetSFCT.getPitch(), aTargetSFCT.getRoll());
    }
  }

  private double heightAboveEllipsoid(ILcdPoint aPoint) {
    if (fModelWorldTransformation.getDestinationReference() instanceof TLcdGridReference) {
      return aPoint.getZ();
    } else {
      try {
        TLcdGeoReference2GeoReference transformation = new TLcdGeoReference2GeoReference();
        ILcdGeoReference worldReference = fModelWorldTransformation.getDestinationReference();
        transformation.setSourceReference(worldReference);
        transformation.setDestinationReference((ILcdGeoReference) new TLcdGeodeticReference(worldReference.getGeodeticDatum()));

        TLcdLonLatHeightPoint geodeticPoint = new TLcdLonLatHeightPoint();
        transformation.sourcePoint2destinationSFCT(aPoint, geodeticPoint);

        return geodeticPoint.getZ();
      } catch (TLcdOutOfBoundsException e) {
        // Shouldn't happen
        return 100.0;
      }
    }
  }

  private double retrieveClampedYaw(double aYaw) {
    if (fUseAlwaysNorthUp) {
      aYaw = 0;
    }
    return aYaw;
  }

  private double retrieveClampedHeight(double aHeight) {
    if (fUseMinMaxHeight) {
      if (aHeight < fMinHeight) {
        aHeight = fMinHeight;
      }
      if (aHeight > fMaxHeight) {
        aHeight = fMaxHeight;
      }
    }
    return aHeight;
  }

  private boolean retrieveClampedReferencePoint(TLspViewXYZWorldTransformation3D aTransformation, ILcd3DEditablePoint aClampedReferencePointSFCT) {
    if (fUseAreaOfInterest) {
      try {
        TLspContext viewContext = new TLspContext(null, aTransformation.getView());
        ILcdPoint pointOnTerrain = aTransformation.getView().getServices().getTerrainSupport().intersectTerrain(
            aTransformation.getEyePoint(),
            aTransformation.getReferencePoint(),
            viewContext
        );
        if (pointOnTerrain == null) {
          if (!retrieveLastValidReferencePoint(aClampedReferencePointSFCT)) {
            aClampedReferencePointSFCT.move3D(aTransformation.getReferencePoint());
          }
          return true;
        }
        aClampedReferencePointSFCT.move3D(pointOnTerrain);

        ILcd3DEditablePoint point = new TLcdXYZPoint();
        fModelWorldTransformation.destinationPoint2sourceSFCT(aClampedReferencePointSFCT, point);
        if (!fAreaOfInterest.contains2D(point)) {
          // The view center doesn't lie in the area of interest -> find a better position
          if (retrieveClampedWorldOrigin(point, aClampedReferencePointSFCT)) {
            // Save origins in order to be able to revert to them later if needed
            saveLastValidReferencePoint(aClampedReferencePointSFCT);
          } else {
            retrieveLastValidReferencePoint(aClampedReferencePointSFCT);
          }
        } else {
          // Save reference point in order to be able to revert to it later if needed
          saveLastValidReferencePoint(aClampedReferencePointSFCT);
        }
      } catch (TLcdOutOfBoundsException e) {
        // Revert to position in the area of interest
        retrieveLastValidReferencePoint(aClampedReferencePointSFCT);
      }
      return true;
    } else {
      // Don't modify the world and view origin
      aClampedReferencePointSFCT.move3D(aTransformation.getReferencePoint());
      return false;
    }
  }

  private boolean retrieveClampedWorldOrigin(ILcdPoint aModelPoint, ILcd3DEditablePoint aWorldOriginSFCT) {
    double x = fAreaOfInterest.getLocation().getX();
    double y = fAreaOfInterest.getLocation().getY();
    double w = fAreaOfInterest.getWidth();
    double h = fAreaOfInterest.getHeight();

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

    try {
      fModelWorldTransformation.sourcePoint2destinationSFCT(clampedModelPoint, aWorldOriginSFCT);
      return true;
    } catch (TLcdOutOfBoundsException e) {
      return false;
    }
  }

  private void saveLastValidReferencePoint(ILcdPoint aReferencePoint) {
    if (fLastValidReferencePoint == null) {
      fLastValidReferencePoint = new TLcdXYZPoint();
    }
    fLastValidReferencePoint.move3D(aReferencePoint);
  }

  private boolean retrieveLastValidReferencePoint(ILcd3DEditablePoint aReferencePoint) {
    if (fLastValidReferencePoint == null) {
      fLastValidReferencePoint = new TLcdXYZPoint();
      ILcd3DEditablePoint point = new TLcdXYZPoint(fAreaOfInterest.getLocation());
      point.translate2D(fAreaOfInterest.getWidth() * 0.5, fAreaOfInterest.getHeight() * 0.5);
      try {
        fModelWorldTransformation.sourcePoint2destinationSFCT(point, fLastValidReferencePoint);
      } catch (TLcdOutOfBoundsException e) {
        fLastValidReferencePoint = null;
      }
    }

    if (fLastValidReferencePoint != null) {
      aReferencePoint.move3D(fLastValidReferencePoint);
      return true;
    }
    return false;
  }
}
