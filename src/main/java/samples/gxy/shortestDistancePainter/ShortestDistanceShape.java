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
package samples.gxy.shortestDistancePainter;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoidUtil;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditableShape;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;

/**
 * The class ShortestDistanceShape is a composition of a
 * TLcdLonLatPolyline and a TLcdLonLatPoint
 * around this polyline.
 * <p/>
 * The class ShortestDistanceShape knows how to calculate
 * the point on the geodesic code>TLcdLonLatPolyline that is the
 * closest to the TLcdLonLatPoint.
 * The method closestPointOnPolylineSFCT returns this closest
 * point on the geodesic polyline as a side effect parameter.
 * Also the distance is returned.
 * <p/>
 * Further, there are methods available to either edit the polyline or the point.
 */

public class ShortestDistanceShape implements ILcd2DEditableShape, Cloneable {

  private TLcdLonLatPolyline fPolyline;
  private TLcdLonLatPoint fPoint;
  private ILcdEllipsoid fEllipsoid;

  /**
   * Constructor of the ShortestDistanceShape object.
   */
  public ShortestDistanceShape(TLcdLonLatPolyline aPolyline,
                               TLcdLonLatPoint aPoint,
                               ILcdEllipsoid aEllipsoid) {

    fPolyline = aPolyline;
    fPoint = aPoint;
    fEllipsoid = aEllipsoid;

  }

  /**
   * Returns the ILcdPolyline of this ShortestDistanceShape.
   *
   * @return the ILcdPolyline of this ShortestDistanceShape.
   */
  public ILcdPolyline getPolyline() {
    return fPolyline;
  }

  /**
   * Returns the ILcdPoint of this ShortestDistanceShape.
   *
   * @return the ILcdPoint of this ShortestDistanceShape.
   */
  public ILcdPoint getPoint() {
    return fPoint;
  }

  /**
   * Returns the ILcdEllipsoid of this ShortestDistanceShape.
   *
   * @return the ILcdEllipsoid of this ShortestDistanceShape.
   */
  public ILcdEllipsoid getEllipsoid() {
    return fEllipsoid;
  }

  /**
   * Moves the point of this ShortestDistanceShape to the latlon location
   * <tt>(aX,aY)</tt>.
   */
  public void movePoint2D(double aX, double aY) {
    fPoint.move2D(aX, aY);
  }

  /**
   * Translates the whole ShortestDistanceShape with the given <tt>(aX,aY).
   */
  public void translate2D(double aX, double aY) {
    fPolyline.translate2D(aX, aY);
    fPoint.translate2D(aX, aY);
  }

  /**
   * Moves the whole ShortestDistanceShape to the given <tt>(aX,aY).
   */
  public void move2D(double aX, double aY) {
    fPolyline.move2D(aX, aY);
    fPoint.move2D(aX, aY);
  }

  /**
   * Moves the whole ShortestDistanceShape to the given ILcdPoint
   * <tt>aPoint.
   */
  public void move2D(ILcdPoint aPoint) {
    fPolyline.move2D(aPoint);
    fPoint.move2D(aPoint);
  }

  /**
   * Checks whether this ShortestDistanceShape contains the 3D point <tt>(aX,aY,aZ).
   */
  public boolean contains3D(double aX, double aY, double aZ) {
    return fPolyline.contains3D(aX, aY, aZ) || fPoint.contains3D(aX, aY, aZ);
  }

  /**
   * Checks whether this ShortestDistanceShape contains the 3D point <tt>aPoint.
   */
  public boolean contains3D(ILcdPoint aPoint) {
    return fPolyline.contains3D(aPoint) || fPoint.contains3D(aPoint);
  }

  /**
   * Checks whether this ShortestDistanceShape contains the 2D point <tt>(aX,aY).
   */
  public boolean contains2D(double aX, double aY) {
    return fPolyline.contains2D(aX, aY) || fPoint.contains2D(aX, aY);
  }

  /**
   * Checks whether this ShortestDistanceShape contains the 2D point <tt>aPoint.
   */
  public boolean contains2D(ILcdPoint aPoint) {
    return fPolyline.contains2D(aPoint) || fPoint.contains2D(aPoint);
  }

  /**
   * Returns the focus point of the ILcdPolyline of this object.
   *
   * @return the focus point of the ILcdPolyline of this object.
   */
  public ILcdPoint getFocusPoint() {
    return fPolyline.getFocusPoint();
  }

  /**
   * Returns the ILcdBounds of this Object.
   *
   * @return the ILcdBounds of this Object.
   */
  public ILcdBounds getBounds() {
    TLcdLonLatBounds bounds = new TLcdLonLatBounds(fPolyline.getBounds());
    bounds.setToIncludePoint2D(fPoint);
    return bounds;
  }

  /**
   * Makes a clone.
   */
  public Object clone() {
    try {
      Object object = super.clone();
      ShortestDistanceShape shortest_distance_shape = (ShortestDistanceShape) object;
      shortest_distance_shape.fPoint = new TLcdLonLatPoint(fPoint);
      shortest_distance_shape.fPolyline = new TLcdLonLatPolyline(fPolyline, true);
      return shortest_distance_shape;
    } catch (CloneNotSupportedException e) {
      throw new InternalError("ShortestDistanceShape does not support clone");
    }
  }

  /**
   * Calculates the point on the geodesic polyline that is the closest from the
   * ILcdPoint aPoint. The resulting point is returned
   * within the side effect parameter aResultSFCT which is an
   * ILcd2DEditablePoint object.
   * The distance from a point aPoint to the geodesic polyline is
   * also returned.
   * <p/>
   * The result is accurate to within 1 meter if values are expressed in meters
   * and provided that the ellipsoidal method geodesicDistance is
   * accurate enough.
   *
   * @return the shortest distance from aPoint to the geodesic line segment
   */
  public double closestPointOnPolylineSFCT(ILcdPoint aPoint, ILcd2DEditablePoint aResultSFCT) {

    // get the number of points of the polyline
    int npoints = fPolyline.getPointCount();

    // easy cases : degenerate polylines
    if (npoints <= 1) {
      if (npoints == 0) {
        // degenerate case : point with polyline that has no points
        aResultSFCT.move2D(aPoint);
      } else {
        // degenerate case : point with polyline that has a single point
        aResultSFCT.move2D(fPolyline.getPoint(0));
      }
      return fEllipsoid.geodesicDistance(aPoint, aResultSFCT);
    }

    // the polyline has at least 2 points ( or 1 line segment )

    // get the closest point on the first line segment of the polyline
    double dist = closestPointOnGeodesic(fPolyline.getPoint(0),
                                         fPolyline.getPoint(1),
                                         aPoint,
                                         aResultSFCT);

    // if there are no more line segments we are done
    if (npoints == 2) {
      return dist;
    }

    // save the best result so far
    double lon = aResultSFCT.getX();
    double lat = aResultSFCT.getY();
    double min_dist = dist;

    // make a loop over the line segments we didn't look at yet
    for (int i = 1; i < npoints - 1; i++) {
      final ILcdPoint p1 = fPolyline.getPoint(i);
      final ILcdPoint p2 = fPolyline.getPoint(i + 1);
      dist = closestPointOnGeodesic(p1, p2, aPoint, aResultSFCT);
      // check if we have a better result
      if (dist < min_dist) {
        // save the best result so far
        lon = aResultSFCT.getX();
        lat = aResultSFCT.getY();
        min_dist = dist;
      }
    }

    // store the best result within the side effect parameter aResultSFCT
    aResultSFCT.move2D(lon, lat);
    // return the distance
    return min_dist;
  }

  private double closestPointOnGeodesic(ILcdPoint aP1, ILcdPoint aP2, ILcdPoint aPoint, ILcd2DEditablePoint aResultSFCT) {
    double dist = TLcdEllipsoidUtil.closestPointOnGeodesic(aP1,
                                                           aP2,
                                                           aPoint,
                                                           fEllipsoid,
                                                           1e-10,
                                                           1.0,
                                                           aResultSFCT);
// to use spherical implementation instead of ellipsoidal uncomment following lines
//    dist = com.luciad.geodesy.TLcdSphereUtil.closestPointOnGeodesic( aP1,
//                                                  aP2,
//                                                  aPoint,
//                                                  true,
//                                                  aResultSFCT )  * com.luciad.util.TLcdConstant.DEG2RAD * com.luciad.util.TLcdConstant.EARTH_RADIUS;
    return dist;
  }

}
