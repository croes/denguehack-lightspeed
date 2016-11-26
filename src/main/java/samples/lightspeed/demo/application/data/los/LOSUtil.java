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
package samples.lightspeed.demo.application.data.los;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdLOSCoverageMatrix;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * Some utility code for LOS calculations.
 */
public class LOSUtil {

  /**
   * Returns true if the given point is both within the maximum radius and
   * visible in the given LOS matrix coverage (that is the
   * result of a LOS calculation); returns false otherwise.
   *
   * Note that this method assumes the given los coverage matrix wraps around.
   *
   * @param aLOSCoverageMatrix a LOS coverage matrix
   * @param aAltitudeProvider  an altitude provider
   * @param aAltitudeMode      the altitude mode in which the visibility calculation is performed
   * @param aHeight            the height of the point with respect to the terrain
   * @param aPoint             a point
   * @param aPointReference    a point reference
   *
   * @return true if the given point is visible according to the given parameters; false otherwise.
   *
   * @throws TLcdOutOfBoundsException
   *          if the given point could not be expressed in the reference
   *          of the LOS coverage matrix
   */
  public static boolean isPointVisible(ILcdLOSCoverageMatrix aLOSCoverageMatrix,
                                       ILcdAltitudeProvider aAltitudeProvider,
                                       TLcdCoverageAltitudeMode aAltitudeMode,
                                       double aHeight, ILcdPoint aPoint,
                                       ILcdGeoReference aPointReference) throws TLcdOutOfBoundsException {
    if (aAltitudeMode == TLcdCoverageAltitudeMode.ABOVE_GEOID ||
        aAltitudeMode == TLcdCoverageAltitudeMode.ABOVE_OBJECT) {
      // We currently do not support these two altitude modes.
      return false;
    }

    ILcdPoint pointInReferenceOfCoverage;
    if (!aLOSCoverageMatrix.getCenterPointReference().equals(aPointReference)) {
      TLcdGeoReference2GeoReference transformation = new TLcdGeoReference2GeoReference();
      transformation.setDestinationReference(aLOSCoverageMatrix.getCenterPointReference());
      transformation.setSourceReference(aPointReference);
      ILcd3DEditablePoint newPoint = aLOSCoverageMatrix.getCenterPoint().cloneAs3DEditablePoint();
      transformation.sourcePoint2destinationSFCT(aPoint, newPoint);
      pointInReferenceOfCoverage = newPoint;
    } else {
      pointInReferenceOfCoverage = aPoint;
    }
    double distance, azimuth;
    //Calculate distance and azimuth in the correct reference
    if (aLOSCoverageMatrix.getCenterPointReference() instanceof ILcdGeodeticReference) {
      ILcdEllipsoid ellipsoid = aLOSCoverageMatrix.getCenterPointReference().getGeodeticDatum().getEllipsoid();
      distance = ellipsoid.geodesicDistance(aLOSCoverageMatrix.getCenterPoint(), pointInReferenceOfCoverage);
      if (distance >= aLOSCoverageMatrix.getRadiusMax()) {
        return false;
      }
      azimuth = ellipsoid.forwardAzimuth2D(aLOSCoverageMatrix.getCenterPoint(), pointInReferenceOfCoverage) * TLcdConstant.RAD2DEG;
    } else if (aLOSCoverageMatrix.getCenterPointReference() instanceof ILcdGridReference) {
      distance = TLcdCartesian.distance2D(aLOSCoverageMatrix.getCenterPoint(), pointInReferenceOfCoverage);
      if (distance > aLOSCoverageMatrix.getRadiusMax()) {
        return false;
      }
      azimuth = TLcdCartesian.forwardAzimuth2D(aLOSCoverageMatrix.getCenterPoint(), pointInReferenceOfCoverage);
    } else {
      throw new IllegalArgumentException("This method only works for a LOS coverage matrix that is defined in " +
                                         "either a geodetic or a grid reference");
    }
    //use distance and azimuth to get closest LOS coverage value
    int row = aLOSCoverageMatrix.getRow(distance);
    if (row >= aLOSCoverageMatrix.getRowCount()) {
      return false;
    }
    int column = aLOSCoverageMatrix.getColumn(azimuth);
    if (aLOSCoverageMatrix.getAngleArc() == 360) {
      //wrap around where needed
      column %= aLOSCoverageMatrix.getColumnCount();
    }
    if (column >= aLOSCoverageMatrix.getColumnCount()) {
      return false;
    }
    double neededAltitudeBeforeVisible = aLOSCoverageMatrix.getValue(column, row);

    if (aAltitudeMode == TLcdCoverageAltitudeMode.ABOVE_ELLIPSOID) {
      ILcdPoint pointInLOSCoverage = new TLcdLonLatPoint(aLOSCoverageMatrix.retrieveAssociatedPointX(column, row),
                                                         aLOSCoverageMatrix.retrieveAssociatedPointY(column, row));
      double altitudeOnTerrain = aAltitudeProvider.retrieveAltitudeAt(pointInLOSCoverage, aPointReference);
      return altitudeOnTerrain + aHeight > neededAltitudeBeforeVisible;
    } else if (aAltitudeMode == TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL) {
      return aHeight >= neededAltitudeBeforeVisible;
    } else {
      return false;
    }
  }

}

