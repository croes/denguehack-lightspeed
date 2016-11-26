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
package samples.common.undo;

import java.util.Map;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * Utility to help save the state of shapes.
 */
public class StateUtil {

  protected StateUtil() {
    //should not be instantiated, contains only static utility methods
  }

  public static void storePointLocation(
      ILcdModelReference aSourceModelReference, ILcdPoint aPoint,
      Map aMap, String aPrefix) {

    ILcd3DEditablePoint pointToStore = aSourceModelReference.makeModelPoint().cloneAs3DEditablePoint();
    pointToStore.move3D(aPoint);
    aMap.put(aPrefix + "location", pointToStore);
  }

  public static void restorePointLocation(
      ILcdModelReference aSourceModelReference, ILcdPoint aStoredPoint,
      ILcdModelReference aTargetModelReference, ILcd3DEditablePoint aPointToMove) throws TLcdOutOfBoundsException {

    if (aStoredPoint != null) {
      ILcdPoint targetPoint = calculateTargetPoint(
          aTargetModelReference, aSourceModelReference, aStoredPoint);

      if (targetPoint != null) {
        aPointToMove.move3D(targetPoint);
      }
    }
  }

  public static ILcdPoint calculateTargetPoint(
      ILcdModelReference aTargetModelReference,
      ILcdModelReference aSourceModelReference, ILcdPoint aStoredPoint) throws TLcdOutOfBoundsException {

    ILcd3DEditablePoint targetPoint = aTargetModelReference.makeModelPoint().cloneAs3DEditablePoint();
    if (aStoredPoint != null) {
      if (aSourceModelReference == null ||
          aSourceModelReference.equals(aTargetModelReference)) {
        //no fancy calculations required
        targetPoint.move3D(aStoredPoint);
      } else {
        TLcdGeoReference2GeoReference sTransformer = new TLcdGeoReference2GeoReference();
        //convert from different source model reference
        sTransformer.setSourceReference(aSourceModelReference);
        sTransformer.setDestinationReference(aTargetModelReference);

        sTransformer.sourcePoint2destinationSFCT(aStoredPoint, targetPoint);
      }

      return targetPoint;
    } else {
      return null;
    }
  }

  public static ILcdPoint calculateTargetPoint(Map aMap, String aPrefix, ILcdModelReference aSourceModelReference, ILcdModelReference aTargetModelReference) throws TLcdOutOfBoundsException {
    ILcdPoint storedPoint = (ILcdPoint) aMap.get(aPrefix + "location");
    return calculateTargetPoint(aTargetModelReference, aSourceModelReference, storedPoint);
  }

  public static ILcdPoint[] calculateTargetPoints(ILcdModelReference aSourceModelReference, ILcdModelReference aTargetModelReference, Map aMap, String aPrefix) throws TLcdOutOfBoundsException {
    ILcdPoint[] points = (ILcdPoint[]) aMap.get(aPrefix + "points");
    if (points != null) {
      ILcdPoint[] targetPoints = new ILcdPoint[points.length];
      for (int i = 0; i < points.length; i++) {
        ILcdPoint point = points[i];
        targetPoints[i] = calculateTargetPoint(aTargetModelReference, aSourceModelReference, point);
      }
      return targetPoints;
    } else {
      return null;
    }
  }

  public static void storePointLocations(ILcdPointList aPolyline, ILcdModelReference aSourceModelReference, Map aMap, String aPrefix) {
    int count = aPolyline.getPointCount();
    ILcdPoint[] points = new ILcdPoint[count];
    for (int i = 0; i < count; i++) {
      ILcd3DEditablePoint point = aSourceModelReference.makeModelPoint().cloneAs3DEditablePoint();
      point.move3D(aPolyline.getPoint(i));
      points[i] = point;
    }

    aMap.put(aPrefix + "points", points);
  }

  public static void adjustNumberOf3DPoints(ILcd3DEditablePointList aPointList, ILcdPoint[] aTargetPoints) {
    while (aPointList.getPointCount() < aTargetPoints.length) {
      aPointList.insert3DPoint(0, 0.0d, 0.0d, 0.0d);
    }
    while (aPointList.getPointCount() > aTargetPoints.length) {
      aPointList.removePointAt(0);
    }
  }

  public static void adjustNumberOf2DPoints(ILcd2DEditablePointList aPointList, ILcdPoint[] aTargetPoints) {
    while (aPointList.getPointCount() < aTargetPoints.length) {
      aPointList.insert2DPoint(0, 0.0d, 0.0d);
    }
    while (aPointList.getPointCount() > aTargetPoints.length) {
      aPointList.removePointAt(0);
    }
  }

  public static void storeBounds(ILcdModelReference aSourceModelReference, ILcdBounds aSourceBounds, Map aMap, String aPrefix) {
    ILcd2DEditableBounds bounds = aSourceModelReference.makeModelPoint().getBounds().cloneAs2DEditableBounds();
    bounds.move2D(aSourceBounds.getLocation());
    bounds.setWidth(aSourceBounds.getWidth());
    bounds.setHeight(aSourceBounds.getHeight());
    aMap.put(aPrefix + "bounds", bounds);
  }

  public static void restoreBounds(Map aMap, String aPrefix, ILcdModelReference aSourceModelReference, ILcdModelReference aTargetModelReference, ILcd2DEditableBounds aTargetBoundsSFCT) throws TLcdNoBoundsException {
    ILcdBounds bounds = (ILcdBounds) aMap.get(aPrefix + "bounds");
    restoreBounds(aSourceModelReference, bounds, aTargetModelReference, aTargetBoundsSFCT);
  }

  public static void restoreBounds(ILcdModelReference aSourceModelReference, ILcdBounds aSourceBounds, ILcdModelReference aTargetModelReference, ILcd2DEditableBounds aTargetBoundsSFCT) throws TLcdNoBoundsException {
    if (aSourceBounds != null) {
      ILcdBounds boundsToRestore;
      if (aSourceModelReference == null ||
          aSourceModelReference.equals(aTargetModelReference)) {
        //no fancy calculations required
        boundsToRestore = aSourceBounds;
      } else {
        TLcdGeoReference2GeoReference sTransformer = new TLcdGeoReference2GeoReference();
        sTransformer.setSourceReference(aSourceModelReference);
        sTransformer.setDestinationReference(aTargetModelReference);
        TLcdXYZBounds transformedBounds = new TLcdXYZBounds();
        sTransformer.sourceBounds2destinationSFCT(aSourceBounds, transformedBounds);
        boundsToRestore = transformedBounds;
      }
      aTargetBoundsSFCT.move2D(boundsToRestore.getLocation());
      aTargetBoundsSFCT.setWidth(boundsToRestore.getWidth());
      aTargetBoundsSFCT.setHeight(boundsToRestore.getHeight());
    }
  }

}
