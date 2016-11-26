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

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYArc;
import com.luciad.shape.shape2D.TLcdXYCompositeRing;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.util.ILcdCache;
import com.luciad.util.TLcdCache;
import com.luciad.util.TLcdConstant;

/**
 * An <code>ILcd2DEditableShape</code> implementation that represents a hippodrome in grid coordinates.
 * <p/>
 * This class is for sample purposes only.
 * If you need to model this shape, please refer to {@link com.luciad.shape.shape2D.TLcdXYGeoBuffer}.
 */
public class XYHippodrome implements ILcdCache, IHippodrome, Cloneable {

  private TLcdXYPoint fStartPoint;
  private TLcdXYPoint fStartUpperPoint;
  private TLcdXYPoint fStartLowerPoint;
  private TLcdXYPoint fEndUpperPoint;
  private TLcdXYPoint fEndLowerPoint;
  private TLcdXYPoint fEndPoint;
  private double fWidth = 0;
  private double fStartEndAzimuth = 0.0;
  private double fEndStartAzimuth = 0.0;

  private TLcdXYPoint fTempModelPoint1 = new TLcdXYPoint();

  private ILcdCompositeCurve fOutline = null;

  // indicates whether the state of the points and angles which are computed internally is valid.
  // any move or resize operation should set the value to false.
  private boolean fInternalPointsCornersValid = false;

  // Cache-member
  private transient TLcdCache fCache;

  public XYHippodrome() {
    // Uses Double.NaN to help painters and editors decide how to render and paint the hippodrome during creation
    fStartPoint = new TLcdXYPoint(Double.NaN, Double.NaN);
    fEndPoint = new TLcdXYPoint(Double.NaN, Double.NaN);
    fWidth = Double.NaN;

    fStartUpperPoint = new TLcdXYPoint();
    fStartLowerPoint = new TLcdXYPoint();
    fEndUpperPoint = new TLcdXYPoint();
    fEndLowerPoint = new TLcdXYPoint();
  }

  public XYHippodrome(TLcdXYPoint aStartPoint, TLcdXYPoint aEndPoint, float aWidth) {
    this();
    fStartPoint.move2D(aStartPoint);
    fEndPoint.move2D(aEndPoint);
    fWidth = aWidth;
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
    case START_UPPER_POINT: {
      result = fStartUpperPoint;
      break;
    }
    case START_LOWER_POINT: {
      result = fStartLowerPoint;
      break;
    }
    case END_UPPER_POINT: {
      result = fEndUpperPoint;
      break;
    }
    case END_LOWER_POINT: {
      result = fEndLowerPoint;
      break;
    }
    default:
      throw new IllegalArgumentException(" aContourPoint should be START_UPPER_POINT, START_LOWER_POINT, END_UPPER_POINT or END_LOWER_POINT.");

    }

    return result;
  }

  /**
   * Computes the distance to the axis on a plane.
   */
  public double retrieveDistanceToAxis(ILcdPoint aPoint) {

    double xp = aPoint.getX();
    double yp = aPoint.getY();

    double y2 = fEndPoint.getY();
    double y1 = fStartPoint.getY();

    // check if we are 'between' the 2 center points. Then we have to compute the distance to the line
    // otherwise we have to compute the distance to one of the points.
    // first handle some special cases
    double x1 = fStartPoint.getX();
    double x2 = fEndPoint.getX();
    if (x2 == x1) {
      if ((yp >= y1 && yp <= y2) || (yp <= y1 && yp >= y2)) {
        return (yp - y1);
      }
    } else if (y2 == y1) {
      if ((xp >= x1 && xp <= x2) || (xp <= x1 && xp >= x2)) {
        return (xp - x1);
      }
    } else {
      // find the line through the point, orthogonal to the line through start and end point
      double m = (y2 - y1) / (x2 - x1);
      double xc = ((yp - y1) + xp / m + x1 * m) / (m + 1 / m);
      // just check the x coordinate. It has to be between that of the points.
      if ((xc >= x1 && xc <= x2) || (xc <= x1 && xc >= x2)) {
        double yc = ((xp - x1) + yp * m + y1 / m) / (m + 1 / m);
        return Math.sqrt((yc - yp) * (yc - yp) + (xc - xp) * (xc - xp));
      }
    }
    // we are not in the area orthogonal to the line, so we compute the distance to the
    // start and end point and return the smallest one.
    double distance_start = Math.sqrt((yp - y1) * (yp - y1) + (xp - x1) * (xp - x1));
    double distance_end = Math.sqrt((yp - y2) * (yp - y2) + (xp - x2) * (xp - x2));

    return Math.min(distance_start, distance_end);
  }

  public double getWidth() {
    return fWidth;
  }

  public void setWidth(double aWidth) {
    fWidth = aWidth;
    invalidate();
  }

  public synchronized double getStartEndAzimuth() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }
    return fStartEndAzimuth;
  }

  public synchronized double getEndStartAzimuth() {
    if (!fInternalPointsCornersValid) {
      calculateShape();
    }
    return fEndStartAzimuth;
  }

  public void moveReferencePoint(ILcdPoint aPoint, int aReferencePoint) {
    if (aReferencePoint != START_POINT && aReferencePoint != END_POINT) {
      throw new IllegalArgumentException(" aReferencePoint should be START_POINT or END_POINT.");
    }

    if (aReferencePoint == START_POINT) {
      fStartPoint.move2D(aPoint);
    } else {
      fEndPoint.move2D(aPoint);
    }

    invalidate();
  }

  public void move2D(double aX, double aY, boolean aMoveStartPoint) {

    // calculate the delta
    double delta_x = aX - (aMoveStartPoint ? fStartPoint : fEndPoint).getX();
    double delta_y = aY - (aMoveStartPoint ? fStartPoint : fEndPoint).getY();

    // move the start- or endpoint
    fStartPoint.translate2D(delta_x, delta_y);
    fEndPoint.translate2D(delta_x, delta_y);

    invalidate();
  }

  public void move2D(ILcdPoint aILcdPoint) {
    move2D(aILcdPoint.getX(), aILcdPoint.getY(), true);
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

    fTempModelPoint1.move2D(aX, aY);
    double distance = retrieveDistanceToAxis(fTempModelPoint1);

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
      XYHippodrome hippodrome = (XYHippodrome) clone;
      hippodrome.fStartPoint = (TLcdXYPoint) fStartPoint.clone();
      hippodrome.fEndPoint = (TLcdXYPoint) fEndPoint.clone();

      hippodrome.fStartUpperPoint = new TLcdXYPoint();
      hippodrome.fStartLowerPoint = new TLcdXYPoint();
      hippodrome.fEndUpperPoint = new TLcdXYPoint();
      hippodrome.fEndLowerPoint = new TLcdXYPoint();

      hippodrome.fTempModelPoint1 = new TLcdXYPoint();

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

  // ILcdCache implementation
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
   * Updates the internal points and angles with the most recent values of
   * start point, end point and width.
   */
  private void calculateShape() {

    // calculate the azimuth
    double angle = Math.atan2(fEndPoint.getY() - fStartPoint.getY(), fEndPoint.getX() - fStartPoint.getX());

    double upper_angle = angle + Math.PI / 2;
    double lower_angle = angle - Math.PI / 2;

    fStartUpperPoint.move2D(fStartPoint.getX() + fWidth * Math.cos(upper_angle), fStartPoint.getY() + fWidth * Math.sin(upper_angle));
    fStartLowerPoint.move2D(fStartPoint.getX() + fWidth * Math.cos(lower_angle), fStartPoint.getY() + fWidth * Math.sin(lower_angle));

    fEndUpperPoint.move2D(fEndPoint.getX() + fWidth * Math.cos(upper_angle), fEndPoint.getY() + fWidth * Math.sin(upper_angle));
    fEndLowerPoint.move2D(fEndPoint.getX() + fWidth * Math.cos(lower_angle), fEndPoint.getY() + fWidth * Math.sin(lower_angle));

    fStartEndAzimuth = (Math.PI / 2 - angle) * TLcdConstant.RAD2DEG;
    fEndStartAzimuth = (-Math.PI / 2 - angle) * TLcdConstant.RAD2DEG;

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
    TLcdXYCompositeRing result = new TLcdXYCompositeRing();

    // Upper line.
    TLcd2DEditablePointList line = new TLcd2DEditablePointList();
    line.insert2DPoint(0, getContourPoint(START_UPPER_POINT).cloneAs2DEditablePoint());
    line.insert2DPoint(1, getContourPoint(END_UPPER_POINT).cloneAs2DEditablePoint());
    result.getCurves().add(new TLcdXYPolyline(line));

    // Arc.
    result.getCurves().add(new TLcdXYArc(getEndPoint().cloneAs2DEditablePoint(), getWidth(),
                                         getWidth(), 270 - getEndStartAzimuth(),
                                         -getEndStartAzimuth(), -180.0));

    // Lower line.
    line = new TLcd2DEditablePointList();
    line.insert2DPoint(0, getContourPoint(END_LOWER_POINT).cloneAs2DEditablePoint());
    line.insert2DPoint(1, getContourPoint(START_LOWER_POINT).cloneAs2DEditablePoint());
    result.getCurves().add(new TLcdXYPolyline(line));

    // Arc.
    result.getCurves().add(new TLcdXYArc(getStartPoint().cloneAs2DEditablePoint(), getWidth(),
                                         getWidth(), 90.0 - getStartEndAzimuth(),
                                         -getStartEndAzimuth(), -180.0));

    return result;
  }
}
