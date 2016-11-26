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
package samples.gxy.common;

import com.luciad.format.magneticnorth.ILcdMagneticNorthMap;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.transformation.TLcdDefaultModelXYWorldTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.ILcdRotationCapableGXYView;

/**
 * Offers reference-specific methods:
 * <ul>
 *   <li>calculation of a pixels/meter ratio for a model location
 *   <li>calculation of a view azimuth for a certain model azimuth
 * </ul>
 */
public class ModelViewUtil {

  private static final double PREDICT_DISTANCE = 1000;

  private final TLcdGeodeticReference fGeodeticReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
  private final TLcdGeoReference2GeoReference fGeodeticTransformation = new TLcdGeoReference2GeoReference();

  private final TLcdXYZPoint fXYZPoint1 = new TLcdXYZPoint();
  private final TLcdXYZPoint fXYZPoint2 = new TLcdXYZPoint();
  private final TLcdLonLatHeightPoint fModelPoint1 = new TLcdLonLatHeightPoint();
  private final TLcdLonLatHeightPoint fModelPoint2 = new TLcdLonLatHeightPoint();
  private final TLcdXYZPoint fWorldPoint1 = new TLcdXYZPoint();
  private final TLcdXYZPoint fWorldPoint2 = new TLcdXYZPoint();
  private final TLcdXYPoint fViewPoint1 = new TLcdXYPoint();
  private final TLcdXYPoint fViewPoint2 = new TLcdXYPoint();

  public ModelViewUtil() {
    fGeodeticTransformation.setSourceReference((ILcdGeoReference) fGeodeticReference);
  }

  /**
   * Calculates how many pixels a meter contains at the given location.
   *
   * @param aGXYContext the context
   * @param aModelPoint the model location around which to measure
   *
   * @return the number of pixels a meter occupies at the given location
   *
   * @throws TLcdOutOfBoundsException if the meter distance cannot be calculated
   */
  public double pixelsPerMeterRatio(ILcdGXYContext aGXYContext, ILcdPoint aModelPoint) throws TLcdOutOfBoundsException {

    // Since all geodetic distances are expressed in meters, we compare with a geodetic distance.

    // Defines 2 points on the world reference that are 1 world unit apart.
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    mwt.modelPoint2worldSFCT(aModelPoint, fWorldPoint1);
    fWorldPoint2.move2D(fWorldPoint1);
    fWorldPoint2.translate2D(1, 0);
    double worldDistance = 1;

    // Measures the world unit distance on a default model reference - any will do.
    fGeodeticTransformation.setDestinationReference((ILcdGeoReference) aGXYContext.getGXYView().getXYWorldReference());
    fGeodeticTransformation.destinationPoint2sourceSFCT(fWorldPoint1, fModelPoint1);
    fGeodeticTransformation.destinationPoint2sourceSFCT(fWorldPoint2, fModelPoint2);
    double meterDistance = fGeodeticReference.getGeodeticDatum().getEllipsoid().geodesicDistance(fModelPoint1, fModelPoint2);

    return (aGXYContext.getGXYView().getScale() * worldDistance) / meterDistance;
  }

  /**
   * Calculates the view azimuth, i.e. the direction of north, relative to the top of the screen.
   * @param aMagneticNorthMap if not null, calculates the direction of the magnetic north. If null, true north is used.
   * @return the north direction in degrees clockwise 12 o'clock, or Double.NaN
   *         if the view's center was close to the projection boundary or outside the projection boundary
   */
  public double viewAzimuth(ILcdGXYView aView, ILcdMagneticNorthMap aMagneticNorthMap) {
    ILcdGXYViewXYWorldTransformation vwt = aView.getGXYViewXYWorldTransformation();
    ILcdXYWorldReference worldReference = aView.getXYWorldReference();
    if (!(worldReference instanceof ILcdGeoReference)) {
      return viewRotation(aView);
    }

    // find a good reference world point
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    vwt.viewXYPoint2worldSFCT(new TLcdXYPoint(aView.getWidth() / 2.0, aView.getHeight() / 2.0), worldPoint);
    worldPoint.move3D(worldPoint.getX(), worldPoint.getY(), 0.0);

    // transform the point to lon lat coordinates
    TLcdLonLatHeightPoint modelPoint = new TLcdLonLatHeightPoint();
    TLcdDefaultModelXYWorldTransformation modelXYWorldTransformation = new TLcdDefaultModelXYWorldTransformation();
    modelXYWorldTransformation.setModelReference(fGeodeticReference);
    modelXYWorldTransformation.setXYWorldReference(worldReference);

    try {
      modelXYWorldTransformation.worldPoint2modelSFCT(worldPoint, modelPoint);
      double viewAzimuth = viewAzimuth(modelPoint, 0, modelXYWorldTransformation, vwt);
      // if a compass is rotated 45 degrees to the east, its bearing is 45 degrees to the west
      double bearing = -viewAzimuth;
      if (aMagneticNorthMap != null) {
        // to display the magnetic bearing instead of the true bearing, subtract the magnetic declination
        bearing -= aMagneticNorthMap.retrieveDeclinationAt(modelPoint);
      }
      // convert the bearing back to the view angle
      return -bearing;
    } catch (TLcdOutOfBoundsException e) {
      return viewRotation(aView);
    }
  }

  private double viewRotation(ILcdGXYView aView) {
    if (aView instanceof ILcdRotationCapableGXYView) {
      // convert counter-clockwise
      return -((ILcdRotationCapableGXYView) aView).getRotation();
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
   * @return view reference angle in degrees clockwise 12 o'clock
   *
   * @throws TLcdOutOfBoundsException if the model point was too close to the
   * projection boundary or outside the projection boundary
   */
  public double viewAzimuth(ILcdPoint aModelPoint, double aModelAzimuth,
                            ILcdModelXYWorldTransformation aMWT,
                            ILcdGXYViewXYWorldTransformation aVWT) throws TLcdOutOfBoundsException {

    worldAzimuthPoints(aModelPoint, aModelAzimuth, aMWT, fWorldPoint1, fWorldPoint2);
    return viewAzimuth(fWorldPoint1, fWorldPoint2, aVWT);

  }

  /**
   * Calculates the view azimuth for the given pair of world point.
   *
   * @param aWorldPoint1 the location of the first world point
   * @param aWorldPoint2 the location of the first world point
   * @param aVWT         the view's view 2 world transformation
   *
   * @return view reference angle in degrees clockwise 12 o'clock
   */
  public double viewAzimuth(ILcdPoint aWorldPoint1, ILcdPoint aWorldPoint2,
                            ILcdGXYViewXYWorldTransformation aVWT) {
    // Converts the world azimuth to a view azimuth.
    aVWT.worldPoint2viewXYPointSFCT(aWorldPoint1, fViewPoint1);
    aVWT.worldPoint2viewXYPointSFCT(aWorldPoint2, fViewPoint2);

    double viewAngle = Math.atan2(fViewPoint2.getY() - fViewPoint1.getY(), fViewPoint2.getX() - fViewPoint1.getX());
    return (viewAngle + (Math.PI / 2)) * TLcdConstant.RAD2DEG;
  }

  /**
   * Converts the heading of the given oriented point to an angle in world coordinates.
   *
   * @param aModelPoint   The given point
   * @param aModelAzimuth degrees clockwise wrt the north direction
   * @param aMWT          The model2world transformation for the given point's reference.
   * @return world reference angle in degrees clockwise 12 o'clock
   *
   * @throws TLcdOutOfBoundsException if the model point was too close to the
   * projection boundary or outside the projection boundary
   */
  public double worldAzimuth(ILcdPoint aModelPoint, double aModelAzimuth,
                             Object aMWT) throws TLcdOutOfBoundsException {

    worldAzimuthPoints(aModelPoint, aModelAzimuth, aMWT, fWorldPoint1, fWorldPoint2);
    return Math.atan2(fWorldPoint2.getX() - fWorldPoint1.getX(),
                      fWorldPoint2.getY() - fWorldPoint1.getY()) * TLcdConstant.RAD2DEG;
  }

  public double worldAzimuthFromWorldPoint(ILcdPoint aWorldPoint, double aModelAzimuth, Object aMWT) throws TLcdOutOfBoundsException {

    worldAzimuthPointsFromWorld(aWorldPoint, aModelAzimuth, aMWT, fWorldPoint1, fWorldPoint2);
    return Math.atan2(fWorldPoint2.getX() - fWorldPoint1.getX(), fWorldPoint2.getY() - fWorldPoint1.getY()) * TLcdConstant.RAD2DEG;
  }

  protected void worldAzimuthPointsFromWorld(ILcdPoint aWorldPoint, double aModelAzimuth, Object aMWT, ILcd3DEditablePoint aWorldPoint1SFCT,
                                             ILcd3DEditablePoint aWorldPoint2SFCT) throws TLcdOutOfBoundsException {

    // Since an azimuth of 0 points north, we can use any geodetic reference to
    // find out the base angle of the view.

    // Converts the model point to our geodetic reference and defines another
    // point with the specified azimuth.
    ILcdGeoReference ref;
    if (aMWT instanceof ILcdModelXYWorldTransformation) {
      ref = (ILcdGeoReference) ((ILcdModelXYWorldTransformation) aMWT).getXYWorldReference();
    } else {
      ref = (ILcdGeoReference) ((ILcdModelXYZWorldTransformation) aMWT).getXYZWorldReference();
    }
    aWorldPoint1SFCT.move3D(aWorldPoint);
    fGeodeticTransformation.setDestinationReference(ref);
    fGeodeticTransformation.destinationPoint2sourceSFCT(aWorldPoint1SFCT, fModelPoint1);
    ILcdEllipsoid ellipsoid = fGeodeticReference.getGeodeticDatum().getEllipsoid();
    ellipsoid.geodesicPointSFCT(fModelPoint1, PREDICT_DISTANCE, aModelAzimuth, fModelPoint2);

    // Converts the geodetic azimuth to a world azimuth.
    fGeodeticTransformation.sourcePoint2destinationSFCT(fModelPoint1, aWorldPoint1SFCT);
    fGeodeticTransformation.sourcePoint2destinationSFCT(fModelPoint2, aWorldPoint2SFCT);
  }

  protected void worldAzimuthPoints(ILcdPoint aModelPoint, double aModelAzimuth,
                                    Object aMWT,
                                    ILcd3DEditablePoint aWorldPoint1SFCT, ILcd3DEditablePoint aWorldPoint2SFCT) throws TLcdOutOfBoundsException {

    // Since an azimuth of 0 points north, we can use any geodetic reference to find out the base angle of the view.

    // Converts the model point to our geodetic reference and defines another point with the specified azimuth.
    ILcdGeoReference ref;
    if (aMWT instanceof ILcdModelXYWorldTransformation) {
      ref = (ILcdGeoReference) ((ILcdModelXYWorldTransformation) aMWT).getXYWorldReference();
      ((ILcdModelXYWorldTransformation) aMWT).modelPoint2worldSFCT(aModelPoint, aWorldPoint1SFCT);
    } else {
      ref = (ILcdGeoReference) ((ILcdModelXYZWorldTransformation) aMWT).getXYZWorldReference();
      ((ILcdModelXYZWorldTransformation) aMWT).modelPoint2worldSFCT(aModelPoint, aWorldPoint1SFCT);
    }
    fGeodeticTransformation.setDestinationReference(ref);
    fGeodeticTransformation.destinationPoint2sourceSFCT(aWorldPoint1SFCT, fModelPoint1);
    ILcdEllipsoid ellipsoid = fGeodeticReference.getGeodeticDatum().getEllipsoid();
    ellipsoid.geodesicPointSFCT(fModelPoint1, PREDICT_DISTANCE, aModelAzimuth, fModelPoint2);
    fModelPoint2.move3D(fModelPoint2.getLon(), fModelPoint2.getLat(), fModelPoint1.getZ());

    // Converts the geodetic azimuth to a world azimuth.
    fGeodeticTransformation.sourcePoint2destinationSFCT(fModelPoint1, aWorldPoint1SFCT);
    fGeodeticTransformation.sourcePoint2destinationSFCT(fModelPoint2, aWorldPoint2SFCT);
  }

  public void worldAzimuthPoint(ILcdPoint aWorldPosition, double aModelAzimuth, Object aMWT, ILcd3DEditablePoint aWorldAzimuthPointSFCT) throws TLcdOutOfBoundsException {
    worldAzimuthPoint(null, aWorldPosition, aModelAzimuth, aMWT, aWorldAzimuthPointSFCT);
  }

  public void worldAzimuthPoint(ILcdPoint aModelPosition, ILcdPoint aWorldPosition, double aModelAzimuth, Object aMWT, ILcd3DEditablePoint aWorldAzimuthPointSFCT) throws TLcdOutOfBoundsException {
    worldAzimuthPoint(aModelPosition, aWorldPosition, aModelAzimuth, PREDICT_DISTANCE, aMWT, aWorldAzimuthPointSFCT);
  }

  public void worldAzimuthPoint(ILcdPoint aModelPosition, ILcdPoint aWorldPosition,
                                double aModelAzimuth, double aPredictionDistance,
                                Object aMWT, ILcd3DEditablePoint aWorldAzimuthPointSFCT) throws TLcdOutOfBoundsException {
    // Since an azimuth of 0 points north, we can use any geodetic reference to find out the base angle of the view.

    // Converts the model point to our geodetic reference and defines another point with the specified azimuth.
    Object worldRef;
    Object modelRef;
    if (aMWT instanceof ILcdModelXYWorldTransformation) {
      worldRef = ((ILcdModelXYWorldTransformation) aMWT).getXYWorldReference();
      modelRef = ((ILcdModelXYWorldTransformation) aMWT).getModelReference();
    } else {
      worldRef = ((ILcdModelXYZWorldTransformation) aMWT).getXYZWorldReference();
      modelRef = ((ILcdModelXYZWorldTransformation) aMWT).getModelReference();
    }
    if (worldRef instanceof ILcdGeoReference && modelRef instanceof ILcdGeoReference) {
      fGeodeticTransformation.setDestinationReference((ILcdGeoReference) worldRef);
      if (aModelPosition != null && fGeodeticTransformation.getSourceReference().equals(modelRef)) {
        fModelPoint1.move3D(aModelPosition);
      } else {
        fGeodeticTransformation.destinationPoint2sourceSFCT(aWorldPosition, fModelPoint1);
      }
      ILcdEllipsoid ellipsoid = fGeodeticReference.getGeodeticDatum().getEllipsoid();
      ellipsoid.geodesicPointSFCT(fModelPoint1, aPredictionDistance, aModelAzimuth, fModelPoint2);
      fModelPoint2.move3D(fModelPoint2.getX(), fModelPoint2.getY(), fModelPoint1.getZ());

      // Converts the geodetic azimuth to a world azimuth.
      fGeodeticTransformation.sourcePoint2destinationSFCT(fModelPoint2, aWorldAzimuthPointSFCT);
    } else {
      transformWorldToModel(aMWT, aWorldPosition, fXYZPoint1);
      fXYZPoint2.move3D(fXYZPoint1);
      // Make sure we interpret aModelAzimuth as an azimuth (0 degrees at 12 o'clock).
      double cosAzimuth = Math.cos(Math.toRadians(aModelAzimuth));
      double sinAzimuth = Math.sin(Math.toRadians(aModelAzimuth));
      fXYZPoint2.translate2D(cosAzimuth * aPredictionDistance, sinAzimuth * aPredictionDistance);

      transformModelToWorld(aMWT, fXYZPoint2, aWorldAzimuthPointSFCT);
    }
  }

  private static void transformModelToWorld(Object aMWT, ILcdPoint aModelPoint, ILcd3DEditablePoint aWorldPointSFCT) throws TLcdOutOfBoundsException {
    if (aMWT instanceof ILcdModelXYWorldTransformation) {
      ILcdModelXYWorldTransformation mwt = (ILcdModelXYWorldTransformation) aMWT;
      mwt.modelPoint2worldSFCT(aModelPoint, aWorldPointSFCT);
    } else if (aMWT instanceof ILcdModelXYZWorldTransformation) {
      ILcdModelXYZWorldTransformation mwt = (ILcdModelXYZWorldTransformation) aMWT;
      mwt.modelPoint2worldSFCT(aModelPoint, aWorldPointSFCT);
    } else {
      throw new IllegalArgumentException("Unknown model to world transformation: " + aMWT);
    }
  }

  private static void transformWorldToModel(Object aMWT, ILcdPoint aWorldPoint, ILcd3DEditablePoint aModelPointSFCT) throws TLcdOutOfBoundsException {
    if (aMWT instanceof ILcdModelXYWorldTransformation) {
      ILcdModelXYWorldTransformation mwt = (ILcdModelXYWorldTransformation) aMWT;
      mwt.worldPoint2modelSFCT(aWorldPoint, aModelPointSFCT);
    } else if (aMWT instanceof ILcdModelXYZWorldTransformation) {
      ILcdModelXYZWorldTransformation mwt = (ILcdModelXYZWorldTransformation) aMWT;
      mwt.worldPoint2modelSFCT(aWorldPoint, aModelPointSFCT);
    } else {
      throw new IllegalArgumentException("Unknown model to world transformation: " + aMWT);
    }
  }

}

