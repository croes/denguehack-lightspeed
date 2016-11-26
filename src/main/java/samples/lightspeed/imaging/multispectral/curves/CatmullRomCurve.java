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
package samples.lightspeed.imaging.multispectral.curves;

import java.util.ArrayList;
import java.util.List;

import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;

/**
 * Class represents a Catmull rom spline. This spline is to be specified with a number of control
 * points. A TLcdXYPolyline is used to store the control points, as this acts as a point list.
 */
public class CatmullRomCurve extends ALcdShape implements ILcdCurve {

  private TLcdXYPolyline fControlPoints;

  /**
   * Creates a new Catmull rom curve
   *
   * @param aControlPoints a polyline containing the controlpoints
   */
  public CatmullRomCurve(TLcdXYPolyline aControlPoints) {
    // Prune duplicate points
    List<ILcd2DEditablePoint> uniquePoints = new ArrayList<ILcd2DEditablePoint>(aControlPoints.getPointCount());
    for (int i = 0; i < aControlPoints.getPointCount(); i++) {
      ILcd2DEditablePoint point = (ILcd2DEditablePoint) aControlPoints.getPoint(i);
      if (i == 0 || !point.equals(aControlPoints.getPoint(i - 1))) {
        uniquePoints.add(point);
      }
    }
    ILcd2DEditablePoint[] array = new ILcd2DEditablePoint[uniquePoints.size()];
    for (int i = 0; i < uniquePoints.size(); i++) {
      array[i] = uniquePoints.get(i);
    }
    fControlPoints = new TLcdXYPolyline(new TLcd2DEditablePointList(array, false));
  }

  @Override
  public ILcdPoint getStartPoint() {
    return fControlPoints.getStartPoint();
  }

  @Override
  public ILcdPoint getEndPoint() {
    return fControlPoints.getEndPoint();
  }

  @Override
  public double getStartTangent2D() {
    return fControlPoints.getStartTangent2D();
  }

  @Override
  public double getEndTangent2D() {
    return fControlPoints.getEndTangent2D();
  }

  @Override
  public double getTangent2D(double aParam) {
    return fControlPoints.getTangent2D(aParam);
  }

  @Override
  public double getLength2D(double aParam1, double aParam2) {
    return fControlPoints.getLength2D(aParam1, aParam2);
  }

  @Override
  public void computePointSFCT(double aParam, ILcd3DEditablePoint aPointSFCT) {
    aPointSFCT.move3D(getPoint((float) aParam));
  }

  @Override
  public String getInterpolation() {
    return INTERPOLATION_LINEAR;
  }

  @Override
  public int getLineSegmentIntersectionCount(ILcdPoint aP1, ILcdPoint aP2) {
    return fControlPoints.getLineSegmentIntersectionCount(aP1, aP2);
  }

  @Override
  public ILcdPoint getFocusPoint() {
    return fControlPoints.getFocusPoint();
  }

  @Override
  public boolean contains2D(double aX, double aY) {
    return fControlPoints.contains2D(aX, aY);
  }

  @Override
  public boolean contains3D(double aX, double aY, double aZ) {
    return fControlPoints.contains3D(aX, aY, aZ);
  }

  @Override
  public ILcdBounds getBounds() {
    return fControlPoints.getBounds();
  }

  //Catmull rom interpolation
  private ILcdPoint getPoint(float aT) {
    int index = 0;
    float coords[] = new float[fControlPoints.getPointCount() * 2 + 4];
    ILcdPoint point0 = fControlPoints.getPoint(0);
    ILcdPoint point1 = fControlPoints.getPoint(1);
    ILcdPoint pointN = fControlPoints.getPoint(fControlPoints.getPointCount() - 1);
    ILcdPoint pointNMin1 = fControlPoints.getPoint(fControlPoints.getPointCount() - 2);

    coords[index++] = (float) (point0.getX() - (point1.getX() - point0.getX()));
    coords[index++] = (float) (point0.getY() - (point1.getY() - point0.getY()));
    coords[coords.length - 2] = (float) (pointN.getX() + (pointN.getX() - pointNMin1.getX()));
    coords[coords.length - 1] = (float) (pointN.getY() + (pointN.getY() - pointNMin1.getY()));

    for (int i = 0; i < fControlPoints.getPointCount(); i++) {
      ILcdPoint point = fControlPoints.getPoint(i);
      coords[index++] = (float) point.getX();
      coords[index++] = (float) point.getY();
    }

    int offset = getOffset(aT, coords);

    float time_i = coords[2 + offset];
    float time_i_plus1 = coords[4 + offset];
    float time_i_min1 = coords[offset];
    float time_i_plus2 = coords[6 + offset];

    float ti1_min_t = time_i_plus1 - aT;
    float t_min_ti = aT - time_i;

    float sqr1 = sqr(aT - time_i_plus1);
    float sqr2 = sqr(time_i - time_i_plus1);
    float cube = cube(time_i - time_i_plus1);
    float div1 = (time_i_plus2 - time_i) * sqr2;

    float p1 = -(t_min_ti * sqr1) /
               ((time_i_plus1 - time_i_min1) * sqr2);

    float p2 = (sqr1 * (time_i - time_i_plus1 + 2 * (time_i - aT)) / cube)
               - ((aT - time_i_plus1) * sqr(time_i - aT)) / div1;

    float p3 = sqr(t_min_ti) * (time_i_plus1 - time_i + 2 * ti1_min_t) / (-cube)
               + (t_min_ti * sqr(ti1_min_t)) / ((time_i_plus1 - time_i_min1) * sqr2);

    float p4 = ((aT - time_i_plus1) * sqr(t_min_ti)) / div1;

    float y = coords[offset + 1] * p1 + coords[offset + 3] * p2 + coords[5 + offset] * p3 + coords[7 + offset] * p4;

    return new TLcdXYPoint(aT, y);
  }

  private static float sqr(float aValue) {
    return aValue * aValue;
  }

  private static float cube(float aValue) {
    return aValue * aValue * aValue;
  }

  private int getOffset(float aT, float control_points[]) {
    int pointCount = fControlPoints.getPointCount();
    if (aT < control_points[2] || aT > control_points[(pointCount * 2)]) {
      return -1;
    }

    for (int i = 2; i < pointCount * 2; i += 2) {
      float first = control_points[i];
      float next = control_points[i + 2];
      if (first <= aT) {
        if (next >= aT) {
          return i - 2;
        }
      }
    }

    return -1;
  }
}
