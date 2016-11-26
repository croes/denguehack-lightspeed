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
package samples.common.shape;

import com.luciad.shape.*;

/**
 * Utility code to detect if shapes are points, polyline-like or polygon-like.
 */
public class ShapeUtil {

  public static boolean isPolygonShape(Object aAnchor) {
    return aAnchor instanceof ILcdGeoBuffer ||
           aAnchor instanceof ILcdComplexPolygon ||
           aAnchor instanceof ILcdPolygon ||
           aAnchor instanceof ILcdCircle ||
           aAnchor instanceof ILcdEllipse ||
           aAnchor instanceof ILcdArcBand ||
           aAnchor instanceof ILcdBounds ||
           aAnchor instanceof ILcdSurface ||
           aAnchor instanceof ILcdRing ||
           isPolygonShapeList(aAnchor);
  }

  private static boolean isPolygonShapeList(Object aAnchor) {
    if (!(aAnchor instanceof ILcdShapeList)) {
      return false;
    }
    ILcdShapeList shapeList = (ILcdShapeList) aAnchor;

    boolean allPolygon = true;
    for (int i = 0; i < shapeList.getShapeCount(); i++) {
      ILcdShape shape = shapeList.getShape(i);
      allPolygon &= isPolygonShape(shape);
    }

    return allPolygon;
  }

  public static boolean isPolylineShape(Object aAnchor) {
    return aAnchor instanceof ILcdPointList ||
           aAnchor instanceof ILcdArc ||
           aAnchor instanceof ILcdCircularArc ||
           aAnchor instanceof ILcdCurve ||
           isPolygonShape(aAnchor) ||
           isPolylineShapeList(aAnchor);
  }

  private static boolean isPolylineShapeList(Object aAnchor) {
    if (!(aAnchor instanceof ILcdShapeList)) {
      return false;
    }
    ILcdShapeList shapeList = (ILcdShapeList) aAnchor;

    boolean allPolyline = true;
    for (int i = 0; i < shapeList.getShapeCount(); i++) {
      ILcdShape shape = shapeList.getShape(i);
      allPolyline &= isPolylineShape(shape);
    }

    return allPolyline;
  }

  public static boolean isPointShape(Object aAnchor) {
    return aAnchor instanceof ILcdPoint ||
           aAnchor instanceof ILcdText ||
           isSinglePointPointList(aAnchor) ||
           isPointShapeList(aAnchor);
  }

  private static boolean isSinglePointPointList(Object aAnchor) {
    if (!(aAnchor instanceof ILcdPointList)) {
      return false;
    }
    ILcdPointList pointList = (ILcdPointList) aAnchor;
    return pointList.getPointCount() == 1;
  }

  private static boolean isPointShapeList(Object aAnchor) {
    if (!(aAnchor instanceof ILcdShapeList)) {
      return false;
    }
    ILcdShapeList shapeList = (ILcdShapeList) aAnchor;

    if (shapeList.getShapeCount() != 1) {
      return false;
    }
    ILcdShape shape = shapeList.getShape(0);
    return isPointShape(shape);
  }
}
