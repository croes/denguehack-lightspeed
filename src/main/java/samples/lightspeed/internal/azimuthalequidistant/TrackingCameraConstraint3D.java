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
package samples.lightspeed.internal.azimuthalequidistant;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookAtTrackingCameraConstraint3D;

class TrackingCameraConstraint3D extends TLspLookAtTrackingCameraConstraint3D {

  private boolean fNorthUp = false;
  private OrientedLonLatPoint fShip;
  private boolean fConstrained = true;
  private ILcdPoint fLastValidReferencePoint = null;
  private double fLastValidDistance = 0;
  private TLspAWTView f3DView;
  private ILcdModel fShipModel;

  TrackingCameraConstraint3D(TLspAWTView a3DView, ILcdModel aShipModel, OrientedLonLatPoint aShip) {
    fShip = aShip;
    f3DView = a3DView;
    fShipModel = aShipModel;
  }

  public void setConstrained(boolean aConstrained) {
    fConstrained = aConstrained;
  }

  public void setNorthUp( boolean aNorthUp ) {
    fNorthUp = aNorthUp;
  }

  @Override
  public void constrain(TLspViewXYZWorldTransformation3D aSource, TLspViewXYZWorldTransformation3D aTargetSFCT) {
    if (fConstrained) {
      // Ensures the camera is always looking at the ship position.
      super.constrain(aSource, aTargetSFCT);
    }

    try (Lock autoUnlock = readLock(fShipModel)) {
      // Limit camera to always look down with correct orientation
      aTargetSFCT.lookAt(
          aTargetSFCT.getReferencePoint(),
          Math.max(1.0, aTargetSFCT.getDistance()),
          0,
          -90,
          fNorthUp?0:fShip.getOrientation()
      );

      // Limit camera to not navigate outside of valid range
      if (rangeBoundaryNotVisible(aTargetSFCT)) {
        fLastValidReferencePoint = aTargetSFCT.getReferencePoint();
        fLastValidDistance = aTargetSFCT.getDistance();
      } else if (fLastValidReferencePoint != null) {
        aTargetSFCT.lookAt(
            fLastValidReferencePoint,
            fLastValidDistance,
            0,
            -90,
            fNorthUp?0:fShip.getOrientation());
      }
    }
  }

  private boolean rangeBoundaryNotVisible(TLspViewXYZWorldTransformation3D aV2W) {
    TLcdXYBounds viewBounds = new TLcdXYBounds(0, 0, f3DView.getWidth(), f3DView.getHeight());

    TLcdDefaultModelXYZWorldTransformation transfo = new TLcdDefaultModelXYZWorldTransformation();
    transfo.setModelReference(fShipModel.getModelReference());
    transfo.setXYZWorldReference(f3DView.getXYZWorldReference());

    ILcdGeoReference geoReference = (ILcdGeoReference) fShipModel.getModelReference();
    ILcdEllipsoid ellipsoid = geoReference.getGeodeticDatum().getEllipsoid();
    ILcd3DEditablePoint modelPoint = fShip.cloneAs3DEditablePoint();
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    TLcdXYZPoint viewPoint = new TLcdXYZPoint();

    for (int i = 0; i < 72; i++) {
      ellipsoid.geodesicPointSFCT(fShip, 1.5 * Compare3DGeocentricWithAzimuthalCylindrical.NAVIGATION_LIMIT, i * 5, modelPoint);
      // model-world-view
      try {
        transfo.modelPoint2worldSFCT(modelPoint, worldPoint);
        aV2W.worldPoint2ViewSFCT(worldPoint, viewPoint);
        if (viewBounds.contains2D((ILcdPoint) viewPoint)) {
          return false;
        }
      } catch (TLcdOutOfBoundsException e) {
      }
    }
    return true;
  }
}
