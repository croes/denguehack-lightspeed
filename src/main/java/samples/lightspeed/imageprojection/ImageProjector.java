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
package samples.lightspeed.imageprojection;

import com.luciad.model.ILcdModelReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjector;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjector;

import samples.lightspeed.icons3d.OrientedPoint;

/**
 * An image projector.
 */
class ImageProjector implements ILcdBounded {

  private final TLcdGeocentricReference fReference;
  private final TLspImageProjector fProjector;

  /**
   * Creates a new image projector.
   *
   * @param aReference the reference of this projector
   * @param aPoint     the initial position of the point projecting the image
   * @param aPointRef  the reference of {@code aPoint}
   */
  public ImageProjector(TLcdGeocentricReference aReference, OrientedPoint aPoint, ILcdModelReference aPointRef) {
    fReference = aReference;
    fProjector = new TLspImageProjector();
    fProjector.setFieldOfView(90);
    setLocation(aPoint, aPointRef);
  }

  /**
   * Sets the aspect ratio of the projector.
   *
   * @param aAspectRatio the aspect ratio
   *
   * @see ILspImageProjector#getAspectRatio()
   */
  public void setAspectRatio(double aAspectRatio) {
    fProjector.setAspectRatio(aAspectRatio);
  }

  /**
   * Updates the location of the image projector.
   *
   * @param aPoint     the point projecting the image
   * @param aReference the reference of {@code aPoint}
   */
  public void setLocation(OrientedPoint aPoint, ILcdModelReference aReference) {
    TLcdDefaultModelXYZWorldTransformation transformation = new TLcdDefaultModelXYZWorldTransformation();
    transformation.setModelReference(aReference);
    transformation.setXYZWorldReference(fReference);
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    try {
      transformation.modelPoint2worldSFCT(aPoint, worldPoint);
    } catch (TLcdOutOfBoundsException e) {
      // not visible
      worldPoint.move3D(Double.NaN, Double.NaN, Double.NaN);
    }
    fProjector.lookFrom(
        worldPoint,
        1000,
        aPoint.getOrientation(),
        Math.max(-90, aPoint.getPitch() - 75),
        aPoint.getRoll(),
        fReference
    );
  }

  /**
   * Returns the projector.
   *
   * @return the projector.
   */
  public TLspImageProjector getProjector() {
    return fProjector;
  }

  @Override
  public ILcdBounds getBounds() {
    return fProjector.getEyePoint().getBounds();
  }
}
