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
package samples.gxy.hippodromePainter;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoidUtil;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatArc;
import com.luciad.shape.shape2D.TLcdLonLatCompositeRing;
import com.luciad.shape.shape2D.TLcdLonLatGeoBuffer;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdCache;
import com.luciad.util.TLcdCache;
import com.luciad.util.TLcdConstant;

/**
 * An <code>ILcd2DEditableShape</code> implementation that represents a hippodrome in geodetic coordinates.
 * <p/>
 * This class is for sample purposes only.
 * If you need to model this shape, please refer to {@link TLcdLonLatGeoBuffer}.
 */
public class LonLatHippodrome implements ILcdCache, IHippodrome, Cloneable {

  // these are the properties defining the shape
  private TLcdLonLatPoint fStartPoint;
  private TLcdLonLatPoint fEndPoint;
  private double fWidth = 0;

  // these are properties computed internally, which main purpose is efficiency.
  private TLcdLonLatPoint fStartUpperPoint;
  private TLcdLonLatPoint fStartLowerPoint;
  private TLcdLonLatPoint fEndUpperPoint;
  private TLcdLonLatPoint fEndLowerPoint;
  private double fStartEndAzimuth = 0.0;
  private double fEndStartAzimuth = 0.0;

  private ILcdEllipsoid fEllipsoid;

  private TLcdLonLatPoint fTempModelPoint1 = new TLcdLonLatPoint();
  private TLcdLonLatPoint fTempModelPoint2 = new TLcdLonLatPoint();

  private ILcdCompositeCurve fOutline = null;

  // Every time the shape or its internal points is/are changed the shape is invalidated, meaning that when retrieving information about the shape or its reference points,
  // the reference information will first be recalculated before returning the requested info; this is managed by this flag.
  private boolean fInternalPointsCornersValid = false;

  private transient TLcdCache fCache;

  /**
   * Default constructor creating a hippodrome with a default WGS_1984 ellipsoid
   * and undefined width, start point and end point.
   */
  public LonLatHippodrome() {
    this(TLcdEllipsoid.DEFAULT);
  }

  /**
   * Constructs a <code>TLcdLonLatHippodrome</code> with the given <code>ILcdEllipsoid</code>
   * and undefined width, start point and end point.
   */
  public LonLatHippodrome(ILcdEllipsoid aEllipsoid) {
    // Uses Double.NaN to help painters and editors decide how to render and paint the hippodrome during creation
    fStartPoint = new TLcdLonLatPoint(Double.NaN, Double.NaN);
    fEndPoint = new TLcdLonLatPoint(Double.NaN, Double.NaN);
    fWidth = Double.NaN;
    fEllipsoid = aEllipsoid;

    fStartUpperPoint = new TLcdLonLatPoint();
    fStartLowerPoint = new TLcdLonLatPoint();
    fEndUpperPoint = new TLcdLonLatPoint();
    fEndLowerPoint = new TLcdLonLatPoint();
  }

  /**
   * Constructs a <code>TLcdLonLatHippodrome</code> with reference-points <code>aStartPoint</code> and <code>aEndPoint</code>
   * and a width of <code>aWidth</code> meters, using <code>aEllipsoid</code> as ellipsoid-reference.
   */
  public LonLatHippodrome(TLcdLonLatPoint aStartPoint, TLcdLonLatPoint aEndPoint, double aWidth, ILcdEllipsoid aEllipsoid) {
    this(aEllipsoid);
    fStartPoint.move2D(aStartPoint);
    fEndPoint.move2D(aEndPoint);
    fWidth = aWidth;
    fEllipsoid = aEllipsoid;
  }

  public ILcdPoint getStartPoint() {
    return fStartPoint;
  }

  public ILcdPoint getEndPoint() {
    return fEndPoint;
  }

  public synchronized ILcdPoint getContourPoint(int aContourPoint) {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }

    ILcdPoint result;

    switch (aContourPoint) {
    case START_UPPER_POINT:
      result = fStartUpperPoint;
      break;
    case START_LOWER_POINT:
      result = fStartLowerPoint;
      break;
    case END_UPPER_POINT:
      result = fEndUpperPoint;
      break;
    case END_LOWER_POINT:
      result = fEndLowerPoint;
      break;
    default:
      throw new IllegalArgumentException(" aContourPoint should be START_UPPER_POINT, START_LOWER_POINT, END_UPPER_POINT or END_LOWER_POINT.");
    }

    return result;
  }

  public double getWidth() {
    return fWidth;
  }

  public void setWidth(double aWidth) {
    fWidth = aWidth;
    invalidate();
  }

  /**
   * Returns the azimuth in the start point of the line from the start point to the end point.
   * This value is usually not equal to the endStartAzimuth, since we are computing on an ellipsoid.
   */
  public synchronized double getStartEndAzimuth() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }
    return fStartEndAzimuth;
  }

  /**
   * Returns the azimuth in the endpoint of the line from the end point to the start point.
   * See the remark at  {@link #getStartEndAzimuth()}.
   */
  public synchronized double getEndStartAzimuth() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }
    return fEndStartAzimuth;
  }

  /**
   * Moves either the start or end point, depending on the value of aReferencePoint.
   */
  public void moveReferencePoint(ILcdPoint aILcdPoint, int aReferencePoint) {
    if (aReferencePoint != START_POINT && aReferencePoint != END_POINT) {
      throw new IllegalArgumentException(" aReferencePoint should be START_POINT or END_POINT.");
    }

    if (aReferencePoint == START_POINT) {
      fStartPoint.move2D(aILcdPoint);
    } else {
      fEndPoint.move2D(aILcdPoint);
    }

    invalidate();
  }

  public void move2D(double aX, double aY, boolean aMoveStartPoint) {
    // calculate distance between start- and endpoint
    double dist = fEllipsoid.geodesicDistance(fStartPoint, fEndPoint);

    // move the start- or endpoint
    if (aMoveStartPoint) {
      fStartPoint.move2D(aX, aY);

      // make sure the endPoint remains at the same distance and forward azimuth of the just moved startPoint
      fEllipsoid.geodesicPointSFCT(fStartPoint, dist, fStartEndAzimuth, fEndPoint);

    } else {
      fEndPoint.move2D(aX, aY);

      // make sure the endPoint remains at the same distance and forward azimuth of the just moved startPoint
      fEllipsoid.geodesicPointSFCT(fEndPoint, dist, fEndStartAzimuth, fStartPoint);
    }

    invalidate();
  }

  public void move2D(ILcdPoint aPoint) {
    move2D(aPoint.getX(), aPoint.getY(), true);
  }

  public void move2D(double aX, double aY) {
    move2D(aX, aY, true);
  }

  public void translate2D(double aDeltaX, double aDeltaY) {
    // move the start- and endpoint
    fStartPoint.translate2D(aDeltaX, aDeltaY);
    fEndPoint.translate2D(aDeltaX, aDeltaY);

    invalidate();
  }

  public ILcdPoint getFocusPoint() {
    return fStartPoint.cloneAs3DEditablePoint();
  }

  public boolean contains2D(ILcdPoint aPoint) {
    return contains2D(aPoint.getX(), aPoint.getY());
  }

  public synchronized boolean contains2D(double aX, double aY) {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }

    // move a temporary point to the given location
    fTempModelPoint1.move2D(aX, aY);

    // calculate shortest distance to the baseline
    double distance = TLcdEllipsoidUtil.closestPointOnGeodesic(getStartPoint(), getEndPoint(), fTempModelPoint1, fEllipsoid, 1e-10, 1.0, fTempModelPoint2);

    // return true if this distance is smaller than the width
    return (distance <= getWidth());
  }

  public boolean contains3D(ILcdPoint aILcdPoint) {
    return contains2D(aILcdPoint);
  }

  public boolean contains3D(double aX, double aY, double aZ) {
    return contains2D(aX, aY);
  }

  public Object clone() {
    try {
      Object clone = super.clone();
      LonLatHippodrome hippodrome = (LonLatHippodrome) clone;
      hippodrome.fStartPoint = (TLcdLonLatPoint) fStartPoint.clone();
      hippodrome.fEndPoint = (TLcdLonLatPoint) fEndPoint.clone();

      hippodrome.fStartUpperPoint = new TLcdLonLatPoint();
      hippodrome.fStartLowerPoint = new TLcdLonLatPoint();
      hippodrome.fEndUpperPoint = new TLcdLonLatPoint();
      hippodrome.fEndLowerPoint = new TLcdLonLatPoint();

      hippodrome.fTempModelPoint1 = new TLcdLonLatPoint();
      hippodrome.fTempModelPoint2 = new TLcdLonLatPoint();

      hippodrome.fInternalPointsCornersValid = false;
      hippodrome.fCache = null;

      return hippodrome;
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Clone not supported in " + getClass().getName());
    }
  }

  public synchronized ILcdBounds getBounds() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }

    return fOutline.getBounds();
  }

  public void insertIntoCache(Object aKey, Object aObject) {
    if (fCache == null) {
      fCache = new TLcdCache();
    }
    fCache.insertIntoCache(aKey, aObject);
  }

  public Object getCachedObject(Object aKey) {
    return fCache == null ? null : fCache.getCachedObject(aKey);
  }

  public Object removeCachedObject(Object aKey) {
    Object value = null;
    if (fCache != null) {
      value = fCache.removeCachedObject(aKey);
      if (fCache.size() == 0) {
        fCache = null;
      }
    }
    return value;
  }

  public void clearCache() {
    fCache = null;
  }

  private synchronized void invalidate() {
    fInternalPointsCornersValid = false;
    clearCache();
  }

  /**
   * Updates the internal values with the current values of width, startpoint and endpoint.
   */
  private void calculateShape() {
    // calculate the azimuths
    fStartEndAzimuth = fEllipsoid.forwardAzimuth2D(fStartPoint, fEndPoint) / TLcdConstant.DEG2RAD;
    fEndStartAzimuth = fEllipsoid.forwardAzimuth2D(fEndPoint, fStartPoint) / TLcdConstant.DEG2RAD;

    // calculate the 4 helper-points
    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, fStartEndAzimuth - 90, fStartUpperPoint);
    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, fStartEndAzimuth + 90, fStartLowerPoint);
    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, fEndStartAzimuth + 90, fEndUpperPoint);
    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, fEndStartAzimuth - 90, fEndLowerPoint);

    // the bounds should include all extreme points.
    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, 0.0, fTempModelPoint1);

    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, 90.0, fTempModelPoint1);
    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, 180.0, fTempModelPoint1);
    fEllipsoid.geodesicPointSFCT(fStartPoint, fWidth, 270.0, fTempModelPoint1);

    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, 0.0, fTempModelPoint1);
    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, 90.0, fTempModelPoint1);
    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, 180.0, fTempModelPoint1);
    fEllipsoid.geodesicPointSFCT(fEndPoint, fWidth, 270.0, fTempModelPoint1);

    fInternalPointsCornersValid = true;
    fOutline = calculateOutline();
  }

  @Override
  public synchronized ILcdCompositeCurve getOutline() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }

    return fOutline;
  }

  private ILcdCompositeCurve calculateOutline() {
    TLcdLonLatCompositeRing result = new TLcdLonLatCompositeRing(fEllipsoid);

    // Upper line.
    TLcd2DEditablePointList line = new TLcd2DEditablePointList();
    line.insert2DPoint(0, getContourPoint(IHippodrome.START_UPPER_POINT).cloneAs2DEditablePoint());
    line.insert2DPoint(1, getContourPoint(IHippodrome.END_UPPER_POINT).cloneAs2DEditablePoint());
    result.getCurves().add(new TLcdLonLatPolyline(line, fEllipsoid));

    // Arc.
    result.getCurves().add(new TLcdLonLatArc(getEndPoint().cloneAs2DEditablePoint(), getWidth(),
                                             getWidth(), 270 - getEndStartAzimuth(),
                                             -getEndStartAzimuth(), -180.0, fEllipsoid));

    // Lower line.
    line = new TLcd2DEditablePointList();
    line.insert2DPoint(0, getContourPoint(IHippodrome.END_LOWER_POINT).cloneAs2DEditablePoint());
    line.insert2DPoint(1, getContourPoint(IHippodrome.START_LOWER_POINT).cloneAs2DEditablePoint());
    result.getCurves().add(new TLcdLonLatPolyline(line, fEllipsoid));

    // Arc.
    result.getCurves().add(new TLcdLonLatArc(getStartPoint().cloneAs2DEditablePoint(), getWidth(),
                                             getWidth(), 90.0 - getStartEndAzimuth(),
                                             -getStartEndAzimuth(), -180.0, fEllipsoid));

    return result;
  }
}
