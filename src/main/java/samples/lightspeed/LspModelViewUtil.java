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
package samples.lightspeed;

import com.luciad.format.magneticnorth.ILcdMagneticNorthMap;
import samples.gxy.common.ModelViewUtil;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * ModelViewUtil extension for Lightspeed use.
 */
public class LspModelViewUtil extends ModelViewUtil {

  private final TLcdXYZPoint fWorldPoint1 = new TLcdXYZPoint();
  private final TLcdXYZPoint fWorldPoint2 = new TLcdXYZPoint();
  private final TLcdXYZPoint fViewPoint1 = new TLcdXYZPoint();
  private final TLcdXYZPoint fViewPoint2 = new TLcdXYZPoint();

  private final TLcdDefaultModelXYZWorldTransformation fGeodeticTransformation = new TLcdDefaultModelXYZWorldTransformation();

  public LspModelViewUtil() {
    fGeodeticTransformation.setModelReference(new TLcdGeodeticReference());
  }

  /**
   * Calculates the view azimuth, i.e. the direction of north, relative to the top of the screen.
   * @param aVWT a view-to-world transformation. If null, the view's transformation is used.
   * @param aMagneticNorthMap if not null, calculates the direction of the magnetic north. If null, true north is used.
   * @returns the north direction in degrees clockwise 12 o'clock, or Double.NaN
   *          if the view's center was close to the projection boundary or outside the projection boundary
   */
  public double viewAzimuth(ILspView aView, ALspViewXYZWorldTransformation aVWT, ILcdMagneticNorthMap aMagneticNorthMap) {
    if (aVWT == null) {
      aVWT = aView.getViewXYZWorldTransformation();
    }
    // trivial case: the azimuth equals the view rotation
    if (!(aView.getXYZWorldReference() instanceof ILcdGeoReference) ||
        (aView.getXYZWorldReference() instanceof ILcdGeocentricReference && aMagneticNorthMap == null)) {
      return viewRotation(aVWT);
    }

    // find a good reference world point
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    if (aVWT instanceof TLspViewXYZWorldTransformation2D) {
      aVWT.viewPoint2WorldSFCT(new TLcdXYPoint(aView.getWidth() / 2.0, aView.getHeight() / 2.0), worldPoint);
      worldPoint.move3D(worldPoint.getX(), worldPoint.getY(), 0.0);
    } else if (aVWT instanceof TLspViewXYZWorldTransformation3D) {
      TLspViewXYZWorldTransformation3D camera = (TLspViewXYZWorldTransformation3D) aVWT;
      ILcdPoint intersection = aView.getServices().getTerrainSupport().intersectTerrain(
          camera.getEyePoint(), camera.getReferencePoint(), new TLspContext(null, aView));
      if (intersection == null) {
        // we don't know where we're looking at; fall back to the view's yaw
        return viewRotation(aVWT);
      } else {
        worldPoint.move3D(intersection);
      }
    }

    // transform the point to lon lat coordinates
    TLcdLonLatHeightPoint modelPoint = new TLcdLonLatHeightPoint();
    fGeodeticTransformation.setXYZWorldReference(aView.getXYZWorldReference());
    try {
      fGeodeticTransformation.worldPoint2modelSFCT(worldPoint, modelPoint);
      double viewAzimuth = viewAzimuth(modelPoint, 0, fGeodeticTransformation, aVWT);
      // if a compass is rotated 45 degrees to the east, its bearing is 45 degrees to the west
      double bearing = -viewAzimuth;
      if (aMagneticNorthMap != null) {
        // to display the magnetic bearing instead of the true bearing, subtract the magnetic declination
        bearing -= aMagneticNorthMap.retrieveDeclinationAt(modelPoint);
      }
      // convert the bearing back to the view angle
      return -bearing;
    } catch (TLcdOutOfBoundsException e) {
      return viewRotation(aVWT);
    }
  }

  private double viewRotation(ALspViewXYZWorldTransformation aViewWorldTransformation) {
    if (aViewWorldTransformation instanceof TLspViewXYZWorldTransformation2D) {
      return -((TLspViewXYZWorldTransformation2D) aViewWorldTransformation).getRotation();
    } else if (aViewWorldTransformation instanceof TLspViewXYZWorldTransformation3D) {
      return -((TLspViewXYZWorldTransformation3D) aViewWorldTransformation).getYaw();
    } else {
      return 0;
    }
  }

  /**
   * Calculates the equivalent view azimuth of the given model azimuth.
   *
   * @param aModelPoint   the location of the point with an azimuth
   * @param aModelAzimuth degrees clockwise wrt the north direction
   * @param aMWT the view's model 2 world transformation
   * @param aVWT the view's view 2 world transformation
   *
   * @return view reference angle in degrees clockwise 12 o'clock
   *
   * @throws TLcdOutOfBoundsException
   *          if the model point was too close to the projection boundary or outside the projection
   *          boundary
   */
  public double viewAzimuth(ILcdPoint aModelPoint, double aModelAzimuth,
                            ILcdModelXYZWorldTransformation aMWT,
                            ALspViewXYZWorldTransformation aVWT) throws TLcdOutOfBoundsException {

    worldAzimuthPoints(aModelPoint, aModelAzimuth, aMWT, fWorldPoint1, fWorldPoint2);

    // Converts the world azimuth to a view azimuth.
    aVWT.worldPoint2ViewSFCT(fWorldPoint1, fViewPoint1);
    aVWT.worldPoint2ViewSFCT(fWorldPoint2, fViewPoint2);

    double viewAngle = Math.atan2(fViewPoint2.getY() - fViewPoint1.getY(), fViewPoint2.getX() - fViewPoint1.getX());
    return (viewAngle + (Math.PI / 2)) * TLcdConstant.RAD2DEG;
  }

}
