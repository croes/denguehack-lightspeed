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
package samples.encoder.kml22.converter;

import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.geometry.TLcdKML22AbstractGeometry;
import com.luciad.format.kml22.model.geometry.TLcdKML22Boundary;
import com.luciad.format.kml22.model.geometry.TLcdKML22Coordinates;
import com.luciad.format.kml22.model.geometry.TLcdKML22LineString;
import com.luciad.format.kml22.model.geometry.TLcdKML22LinearRing;
import com.luciad.format.kml22.model.geometry.TLcdKML22MultiGeometry;
import com.luciad.format.kml22.model.geometry.TLcdKML22Point;
import com.luciad.format.kml22.model.geometry.TLcdKML22Polygon;
import com.luciad.format.kml22.model.util.ELcdKML22AltitudeMode;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.*;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdXYPolygon;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 *
 * Utility class to turn various shapes into {@link TLcdKML22AbstractGeometry}.
 */
public final class KML22GeometryUtil {

  private static final TLcdGeodeticReference WGS84 = new TLcdGeodeticReference();

  private static final boolean TESSELLATE = true;
  private static final boolean DONT_TESSELLATE = false;

  private static final boolean DONT_EXTRUDE = false;

  private static final boolean REVERSE = true;
  private static final boolean DONT_REVERSE = false;

  private static final boolean CLOSE = true;
  private static final boolean DONT_CLOSE = false;

  private KML22GeometryUtil() {
  }

  /**
   * Create a 3D extruded shape from the given {@link ILcdExtrudedShape}.
   *
   * @param aModelRef the shape model reference
   * @param aShape the extruded shape
   * @return a {@link TLcdKML22AbstractGeometry} instance
   */
  public static TLcdKML22AbstractGeometry createExtrudedShape(ILcdModelReference aModelRef, ILcdShape aShape) {
    if (aShape instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aShape;
      TLcdKML22MultiGeometry multiGeometry = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
      for (int i = 0; i < shapeList.getShapeCount(); i++) {
        ILcdShape shape = shapeList.getShape(i);
        multiGeometry.addShape(createExtrudedShape(aModelRef, shape));
      }
      return multiGeometry;
    } else if (aShape instanceof ILcdExtrudedShape) {
      ILcdExtrudedShape extrudedShape = (ILcdExtrudedShape) aShape;
      ILcdShape baseShape = extrudedShape.getBaseShape();
      double minimumZ = extrudedShape.getMinimumZ();
      double maximumZ = extrudedShape.getMaximumZ();
      return createExtrudedShape(aModelRef, baseShape, minimumZ, maximumZ);
    } else {
      throw new IllegalArgumentException("Shape must be a ILcdShapeList or an ILcdExtrudedShape. Got : " + aShape.getClass().getName());
    }
  }

  public static TLcdKML22AbstractGeometry createExtrudedShape(ILcdModelReference aModelRef, ILcdShape aShape, double aMinZ, double aMaxZ) {
    if (aShape instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aShape;
      return createExtrudedShapeList(aModelRef, shapeList, aMinZ, aMaxZ);
    } else if (aShape instanceof ILcdCircle) {
      ILcdCircle circle = (ILcdCircle) aShape;
      int numPoint = Math.max(32, (int) (2 * Math.PI * circle.getRadius() / 100000));
      return createExtrudedPolygon(aModelRef, convertToPolygon(aModelRef, circle, numPoint), aMinZ, aMaxZ);
    } else if (aShape instanceof ILcdComplexPolygon) {
      throw new IllegalArgumentException("Unsupported base shape: " + aShape.getClass().getName());
    } else if (aShape instanceof ILcdPolygon) {
      ILcdPolygon polygon = (ILcdPolygon) aShape;
      return createExtrudedPolygon(aModelRef, polygon, aMinZ, aMaxZ);
    } else if (aShape instanceof ILcdPolyline) {
      ILcdPolyline polyline = (ILcdPolyline) aShape;
      return createExtrudedPolyline(aModelRef, polyline, aMinZ, aMaxZ);
    } else {
      throw new IllegalArgumentException("Unsupported base shape: " + aShape.getClass().getName());
    }
  }

  private static TLcdKML22MultiGeometry createExtrudedShapeList(ILcdModelReference aModelRef, ILcdShapeList aShapeList, double aMinZ, double aMaxZ) {
    TLcdKML22MultiGeometry multiGeometry = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
    for (int i = 0; i < aShapeList.getShapeCount(); i++) {
      try {
        multiGeometry.addShape(createExtrudedShape(aModelRef, aShapeList.getShape(i), aMinZ, aMaxZ));
      } catch (IllegalArgumentException iae) {
        // ignored
      }
    }
    return multiGeometry;
  }

  private static ILcdPolygon convertToPolygon(ILcdModelReference aModelRef, ILcdShape aShape, int aNumPoint) {
    if (aShape instanceof ILcdRing) {
      ILcdRing ring = (ILcdRing) aShape;
      return convertRingToPolygon(aModelRef, ring, aNumPoint);
    } else {
      throw new IllegalArgumentException("Unsupported shape type: " + aShape.getClass().getName());
    }
  }

  private static ILcdPolygon convertRingToPolygon(ILcdModelReference aModelRef, ILcdRing aRing, int aNumPoint) {
    ILcd2DEditablePointList pointList = discretizeCurve(aModelRef, aRing, aNumPoint);
    return aModelRef instanceof ILcdGeodeticReference ?
           new TLcdLonLatPolygon(pointList) :
           new TLcdXYPolygon(pointList);
  }

  private static ILcd3DEditablePointList discretizeCurve(ILcdModelReference aModelRef, ILcdCurve aCurve, int aNumPoint) {
    TLcd3DEditablePointList pointList = new TLcd3DEditablePointList();
    ILcd3DEditablePoint temp = aModelRef instanceof ILcdGeodeticReference ?
                               new TLcdLonLatHeightPoint() :
                               new TLcdXYZPoint();
    double t = 0.0;
    double stepSize = 1.0d / aNumPoint;
    for (int i = 0; i < aNumPoint; i++) {
      aCurve.computePointSFCT(t, temp);
      pointList.insert3DPoint(i, temp.getX(), temp.getY(), temp.getZ());
      t += stepSize;
    }
    return pointList;
  }

  private static TLcdKML22MultiGeometry createExtrudedPolygon(ILcdModelReference aModelRef, ILcdPolygon aPolygon, double aMinZ, double aMaxZ) {
    TLcdGeoReference2GeoReference transfo = new TLcdGeoReference2GeoReference((ILcdGeoReference) aModelRef, WGS84);
    TLcdLonLatHeightPoint llh = new TLcdLonLatHeightPoint();

    // Transform coordinates
    TLcd3DEditablePointList bottomFacePoints = new TLcd3DEditablePointList();
    TLcd3DEditablePointList topFacePoints = new TLcd3DEditablePointList();
    int pointCount = 0;
    for (int i = 0; i < aPolygon.getPointCount(); i++) {
      try {
        transfo.sourcePoint2destinationSFCT(aPolygon.getPoint(i), llh);
        bottomFacePoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMinZ);
        topFacePoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMaxZ);
        pointCount++;
      } catch (TLcdOutOfBoundsException e) {
        // ignored
      }
    }

    // Create faces
    TLcdKML22Polygon topFace = null;
    TLcdKML22Polygon bottomFace = null;
    int orientation = aPolygon.getOrientation();
    if (orientation == ILcdPolygon.COUNTERCLOCKWISE) {
      topFace = createPolygon(WGS84, topFacePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, DONT_REVERSE, CLOSE);
      bottomFace = createPolygon(WGS84, bottomFacePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, REVERSE, CLOSE);
    } else if (orientation == ILcdPolygon.CLOCKWISE) {
      topFace = createPolygon(WGS84, topFacePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, REVERSE, CLOSE);
      bottomFace = createPolygon(WGS84, bottomFacePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, DONT_REVERSE, CLOSE);
    }

    // Create the fence to
    TLcdKML22MultiGeometry fence = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
    int faceCount = pointCount;
    boolean reverse = orientation == ILcdPolygon.COUNTERCLOCKWISE ?
                      DONT_REVERSE :
                      REVERSE;
    for (int i = 0; i < faceCount; i++) {
      ILcdPoint point0 = bottomFacePoints.getPoint(i);
      ILcdPoint point1 = bottomFacePoints.getPoint((i + 1) % pointCount);
      ILcdPoint point2 = topFacePoints.getPoint((i + 1) % pointCount);
      ILcdPoint point3 = topFacePoints.getPoint(i);
      TLcd3DEditablePointList facePoints = new TLcd3DEditablePointList();
      facePoints.insert3DPoint(0, point0.getX(), point0.getY(), point0.getZ());
      facePoints.insert3DPoint(1, point1.getX(), point1.getY(), point1.getZ());
      facePoints.insert3DPoint(2, point2.getX(), point2.getY(), point2.getZ());
      facePoints.insert3DPoint(3, point3.getX(), point3.getY(), point3.getZ());
      facePoints.insert3DPoint(4, point0.getX(), point0.getY(), point0.getZ());
      TLcdKML22Polygon face = createPolygon(WGS84, facePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, reverse, DONT_CLOSE);
      fence.addShape(face);
    }

    TLcdKML22MultiGeometry multiGeometry = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
    multiGeometry.addShape(topFace);
    multiGeometry.addShape(bottomFace);
    multiGeometry.addShape(fence);

    return multiGeometry;
  }

  private static TLcdKML22MultiGeometry createExtrudedPolyline(ILcdModelReference aModelRef, ILcdPolyline aPolyline, double aMinZ, double aMaxZ) {
    TLcdGeoReference2GeoReference transfo = new TLcdGeoReference2GeoReference((ILcdGeoReference) aModelRef, WGS84);
    TLcdLonLatHeightPoint llh = new TLcdLonLatHeightPoint();

    // Transform coordinates
    TLcd3DEditablePointList bottomPoints = new TLcd3DEditablePointList();
    TLcd3DEditablePointList topPoints = new TLcd3DEditablePointList();
    int pointCount = 0;
    for (int i = 0; i < aPolyline.getPointCount(); i++) {
      try {
        transfo.sourcePoint2destinationSFCT(aPolyline.getPoint(i), llh);
        bottomPoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMinZ);
        topPoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMaxZ);
        if (i == 0) {
          // close the ring by repeating first point
          bottomPoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMinZ);
          topPoints.insert3DPoint(pointCount, llh.getX(), llh.getY(), aMaxZ);
        }
        pointCount++;
      } catch (TLcdOutOfBoundsException e) {
        // ignored
      }
    }

    TLcdKML22MultiGeometry fence = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
    int faceCount = pointCount - 1;
    for (int i = 0; i < faceCount; i++) {
      ILcdPoint point0 = bottomPoints.getPoint(i);
      ILcdPoint point1 = bottomPoints.getPoint((i + 1) % pointCount);
      ILcdPoint point2 = topPoints.getPoint((i + 1) % pointCount);
      ILcdPoint point3 = topPoints.getPoint(i);
      TLcd3DEditablePointList facePoints = new TLcd3DEditablePointList();
      facePoints.insert3DPoint(0, point0.getX(), point0.getY(), point0.getZ());
      facePoints.insert3DPoint(1, point1.getX(), point1.getY(), point1.getZ());
      facePoints.insert3DPoint(2, point2.getX(), point2.getY(), point2.getZ());
      facePoints.insert3DPoint(3, point3.getX(), point3.getY(), point3.getZ());
      facePoints.insert3DPoint(4, point0.getX(), point0.getY(), point0.getZ());
      TLcdKML22Polygon face = createPolygon(WGS84, facePoints, 3, ELcdKML22AltitudeMode.ABSOLUTE, TESSELLATE, DONT_EXTRUDE, DONT_REVERSE, DONT_CLOSE);
      fence.addShape(face);
    }

    return fence;
  }

  /**
   * Create a 3D KML geometry from the given {@link ILcdShape}.
   * Currently support the following shape types:
   * <ul>
   *   <li>ILcdShapeList</li>
   *   <li>ILcdCircle: after conversion to a polygon</li>
   *   <li>ILcdComplexPolygon: the first polygon is assumed to be the outer boundary and all other polygons, holes</li>
   *   <li>ILcdPolygon</li>
   *   <li>ILcdPolyline</li>
   *   <li>ILcdPoint</li>
   * </ul>
   *
   * For {@link ILcdExtrudedShape} use {@link #createExtrudedShape(ILcdModelReference, ILcdShape)}
   * instead.
   *
   * @param aModelRef the shape model reference
   * @param aShape the shape to convert
   * @param aAltMode the altitude mode
   * @param aExtrude {@code true} to extrude down to the ground, {@code false} otherwise.
   * @return a {@link TLcdKML22AbstractGeometry} instance
   * <ul>
   *   <li>ILcdShapeList -> a {@link TLcdKML22MultiGeometry} containing the converted shapes of the list</li>
   *   <li>ILcdCircle -> a {@link TLcdKML22Polygon} whose boundary is derived by discretizing the circle</li>
   *   <li>ILcdComplexPolygon -> a {@link TLcdKML22Polygon}: the first polygon is assumed to be
   *   the outer boundary and all other polygons, holes</li>
   *   <li>ILcdPolygon -> a {@link TLcdKML22Polygon}</li>
   *   <li>ILcdPolyline -> a {@link TLcdKML22LineString}</li>
   *   <li>ILcdPoint -> a {@link TLcdKML22Point}</li>
   * </ul>
   */
  public static TLcdKML22AbstractGeometry createShape3D(ILcdModelReference aModelRef, ILcdShape aShape, ELcdKML22AltitudeMode aAltMode, boolean aExtrude) {
    return createShape(aModelRef, aShape, 3, aAltMode, DONT_TESSELLATE, aExtrude);
  }

  /**
   * Create a 2D KML geometry from the given {@link ILcdShape}.
   * Currently support the following shape types:
   * <ul>
   *   <li>ILcdShapeList</li>
   *   <li>ILcdCircle: after conversion to a polygon</li>
   *   <li>ILcdComplexPolygon: the first polygon is assumed to be the outer boundary and all other polygons, holes</li>
   *   <li>ILcdPolygon</li>
   *   <li>ILcdPolyline</li>
   *   <li>ILcdPoint</li>
   * </ul>
   *
   * @param aModelRef the shape model reference
   * @param aShape the shape to convert.
   * @return a {@link TLcdKML22AbstractGeometry} instance.
   * <ul>
   *   <li>ILcdShapeList -> a {@link TLcdKML22MultiGeometry} containing the converted shapes of the list</li>
   *   <li>ILcdCircle -> a {@link TLcdKML22Polygon} whose boundary is derived by discretizing the circle</li>
   *   <li>ILcdComplexPolygon -> a {@link TLcdKML22Polygon}: the first polygon is assumed to be
   *   the outer boundary and all other polygons, holes</li>
   *   <li>ILcdPolygon -> a {@link TLcdKML22Polygon}</li>
   *   <li>ILcdPolyline -> a {@link TLcdKML22LineString}</li>
   *   <li>ILcdPoint -> a {@link TLcdKML22Point}</li>
   * </ul>
   */
  public static TLcdKML22AbstractGeometry createShape2D(ILcdModelReference aModelRef, ILcdShape aShape) {
    return createShape(aModelRef, aShape, 2, ELcdKML22AltitudeMode.CLAMP_TO_GROUND, TESSELLATE, DONT_EXTRUDE);
  }

  private static TLcdKML22AbstractGeometry createShape(ILcdModelReference aModelRef, ILcdShape aShape, int aDimension, ELcdKML22AltitudeMode aAltMode, boolean aTessellate, boolean aExtrude) {
    if (aShape instanceof ILcdCircle) {
      ILcdCircle circle = (ILcdCircle) aShape;
      int numPoint = Math.max(32, (int) (2 * Math.PI * circle.getRadius() / 100000));
      return createPolygon(aModelRef, convertToPolygon(aModelRef, circle, numPoint), aDimension, aAltMode, aTessellate, aExtrude);
    } else if (aShape instanceof ILcdComplexPolygon) {
      ILcdComplexPolygon complexPolygon = (ILcdComplexPolygon) aShape;
      return createComplexPolygon(aModelRef, complexPolygon, aDimension, aAltMode, aTessellate, aExtrude);
    } else if (aShape instanceof ILcdPolygon) {
      ILcdPolygon polygon = (ILcdPolygon) aShape;
      return createPolygon(aModelRef, polygon, aDimension, aAltMode, aTessellate, aExtrude);
    } else if (aShape instanceof ILcdPolyline) {
      ILcdPolyline polyline = (ILcdPolyline) aShape;
      return createLineString(aModelRef, polyline, aDimension, aAltMode, aTessellate, aExtrude);
    } else if (aShape instanceof ILcdPoint) {
      ILcdPoint point = (ILcdPoint) aShape;
      return createPoint(aModelRef, point, aDimension, aAltMode, aExtrude);
    } else {
      return null;
    }
  }

  private static TLcdKML22Polygon createComplexPolygon(ILcdModelReference aModelRef, ILcdComplexPolygon aComplexPolygon, int aDimension, ELcdKML22AltitudeMode aAltMode, boolean aTessellate, boolean aExtrude) {
    if (aComplexPolygon == null) {
      throw new NullPointerException("Complex polygon is null");
    }
    int polygonCount = aComplexPolygon.getPolygonCount();
    if (polygonCount <= 0) {
      throw new IllegalStateException("Complex polygon has no polygon(s)");
    }

    TLcdKML22Polygon polygon = new TLcdKML22Polygon(TLcdKML22DataTypes.PolygonType);

    ILcdPolygon outerPolygon = aComplexPolygon.getPolygon(0);
    TLcdKML22LinearRing outerRing = createLinearRing(aModelRef, outerPolygon, aDimension, outerPolygon.getOrientation() != ILcdPolygon.COUNTERCLOCKWISE, CLOSE);
    TLcdKML22Boundary outerBoundary = new TLcdKML22Boundary();
    outerBoundary.setLinearRing(outerRing);
    polygon.setOuterBoundary(outerBoundary);
    polygon.setAltitudeMode(aAltMode);
    polygon.setTessellate(aTessellate);
    polygon.setExtrude(aExtrude);
    for (int i = 1; i < polygonCount; i++) {
      ILcdPolygon innerPolygon = aComplexPolygon.getPolygon(i);
      TLcdKML22LinearRing innerRing = createLinearRing(aModelRef, innerPolygon, aDimension, innerPolygon.getOrientation() != ILcdPolygon.COUNTERCLOCKWISE, CLOSE);
      TLcdKML22Boundary innerBoundary = new TLcdKML22Boundary();
      innerBoundary.setLinearRing(innerRing);
      polygon.getInnerBoundaries().add(innerBoundary);
    }

    return polygon;
  }

  /**
   * Create a 3D polygon with the given altitude mode and extrusion flag.
   *
   * @param aModelRef the model reference of the given polygon
   * @param aPolygon a WGS 84 3D polygon
   * @param aAltMode the altitude mode. Either {@link ELcdKML22AltitudeMode#ABSOLUTE} or {@link ELcdKML22AltitudeMode#RELATIVE_TO_GROUND}
   * @param aExtrude {@code true} to extrude down to the ground, {@code false} otherwise.
   * @return a {@link TLcdKML22Polygon} instance.
   */
  public static TLcdKML22Polygon createPolygon3D(ILcdModelReference aModelRef, ILcdPolygon aPolygon, ELcdKML22AltitudeMode aAltMode, boolean aExtrude) {
    return createPolygon(aModelRef, aPolygon, 3, aAltMode, DONT_TESSELLATE, aExtrude);
  }

  /**
   * Create a 2D polygon with {@link ELcdKML22AltitudeMode#CLAMP_TO_GROUND} altitude mode and tessellate set to {@code true}.
   *
   * @param aModelRef the model reference of the given polygon
   * @param aPolygon a WGS 84 2D/3D polygon
   * @return a {@link TLcdKML22Polygon} instance
   */
  public static TLcdKML22Polygon createPolygon2D(ILcdModelReference aModelRef, ILcdPolygon aPolygon) {
    return createPolygon(aModelRef, aPolygon, 2, ELcdKML22AltitudeMode.CLAMP_TO_GROUND, TESSELLATE, DONT_EXTRUDE);
  }

  private static TLcdKML22Polygon createPolygon(ILcdModelReference aModelRef, ILcdPolygon aPolygon, int aDimension, ELcdKML22AltitudeMode aAltMode, boolean aTessellate, boolean aExtrude) {
    if (aPolygon == null) {
      throw new NullPointerException("Polygon is null");
    }
    boolean reverse = aPolygon.getOrientation() == ILcdPolygon.COUNTERCLOCKWISE ?
                      DONT_REVERSE :
                      REVERSE;
    return createPolygon(aModelRef, aPolygon, aDimension, aAltMode, aTessellate, aExtrude, reverse, CLOSE);
  }

  private static TLcdKML22Polygon createPolygon(ILcdModelReference aModelRef, ILcdPointList aPolygon, int aDimension, ELcdKML22AltitudeMode aAltMode, boolean aTessellate, boolean aExtrude, boolean aReverse, boolean aClose) {
    if (aPolygon == null) {
      throw new NullPointerException("Polygon is null");
    }
    TLcdKML22LinearRing outerRing = createLinearRing(aModelRef, aPolygon, aDimension, aReverse, aClose);
    TLcdKML22Boundary outerBoundary = new TLcdKML22Boundary();
    outerBoundary.setLinearRing(outerRing);

    TLcdKML22Polygon polygon = new TLcdKML22Polygon(TLcdKML22DataTypes.PolygonType);
    polygon.setOuterBoundary(outerBoundary);
    polygon.setAltitudeMode(aAltMode);
    polygon.setTessellate(aTessellate);
    polygon.setExtrude(aExtrude);

    return polygon;
  }

  private static TLcdKML22LinearRing createLinearRing(ILcdModelReference aModelRef,
                                                      ILcdPointList aPointList,
                                                      int aDimension,
                                                      boolean aReverse,
                                                      boolean aClose) {
    if (aPointList == null) {
      throw new NullPointerException("Polygon is null");
    }
    if (aPointList.getPointCount() == 0) {
      throw new IllegalArgumentException("Polygon doesn't have point");
    }

    CoordinatesBuilder outerCoords = CoordinatesBuilder.newInstance(aModelRef, aDimension);
    if (aReverse) {
      for (int i = aPointList.getPointCount() - 1; i >= 0; i--) {
        outerCoords.addPoint(aPointList.getPoint(i));
      }
      if (aClose) {
        outerCoords.addPoint(aPointList.getPoint(aPointList.getPointCount() - 1));
      }
    } else {
      for (int i = 0; i < aPointList.getPointCount(); i++) {
        outerCoords.addPoint(aPointList.getPoint(i));
      }
      if (aClose) {
        outerCoords.addPoint(aPointList.getPoint(0));
      }
    }

    TLcdKML22LinearRing outerRing = new TLcdKML22LinearRing(TLcdKML22DataTypes.LinearRingType);
    outerRing.setCoordinates(outerCoords.build());

    return outerRing;
  }

  /**
   * Create a 3D LineString.
   *
   * @param aModelRef the model reference of the given polyline
   * @param aPointList a WGS 84 3D pointList.
   * @param aAltMode the altitude mode. Either {@link ELcdKML22AltitudeMode#ABSOLUTE} or {@link ELcdKML22AltitudeMode#RELATIVE_TO_GROUND}
   * @param aExtrude {@code true} to extrude the polyline down to the ground, {@code false} otherwise.
   * @return a {@link TLcdKML22LineString} instance.
   */
  public static TLcdKML22LineString createLineString3D(ILcdModelReference aModelRef, ILcdPointList aPointList, ELcdKML22AltitudeMode aAltMode, boolean aExtrude) {
    if (aAltMode == ELcdKML22AltitudeMode.CLAMP_TO_GROUND) {
      throw new IllegalArgumentException("Altitude mode must be ABSOLUTE or RELATIVE_TO_GROUND. Got " + aAltMode);
    }
    return createLineString(aModelRef, aPointList, 3, aAltMode, DONT_TESSELLATE, aExtrude);
  }

  /**
   * Create a 2D LineString with {@link ELcdKML22AltitudeMode#CLAMP_TO_GROUND} altitude mode and tessellate set to {@code true}.
   *
   * @param aModelRef the model reference of the given polyline.
   * @param aPointList a WGS 84 2D/3D polyline
   * @return a {@link TLcdKML22LineString} instance.
   */
  public static TLcdKML22LineString createLineString2D(ILcdModelReference aModelRef, ILcdPointList aPointList) {
    return createLineString(aModelRef, aPointList, 2, ELcdKML22AltitudeMode.CLAMP_TO_GROUND, TESSELLATE, DONT_EXTRUDE);
  }

  private static TLcdKML22LineString createLineString(ILcdModelReference aModelRef, ILcdPointList aPointList, int aDimension, ELcdKML22AltitudeMode aAltMode, boolean aTessellate, boolean aExtrude) {
    if (aPointList == null) {
      throw new NullPointerException("PointList/Polyline is null");
    }
    CoordinatesBuilder coords = CoordinatesBuilder.newInstance(aModelRef, aDimension);
    for (int i = 0; i < aPointList.getPointCount(); i++) {
      ILcdPoint point = aPointList.getPoint(i);
      coords.addPoint(point);
    }

    TLcdKML22LineString lineString = new TLcdKML22LineString(TLcdKML22DataTypes.LineStringType);
    lineString.setCoordinates(coords.build());
    lineString.setAltitudeMode(aAltMode);
    lineString.setTessellate(aTessellate);
    lineString.setExtrude(aExtrude);

    return lineString;
  }

  /**
   * Create a 2D KML point.
   * The altitudeMode is set to {@link ELcdKML22AltitudeMode#CLAMP_TO_GROUND}.
   * The extrude element is set to {@code false}.
   *
   * @param aModelRef the model reference of the given point
   * @param aPoint a WGS 84 geodetic point.
   * @return a {@link TLcdKML22Point} instance.
   */
  public static TLcdKML22Point createPoint2D(ILcdModelReference aModelRef, ILcdPoint aPoint) {
    return createPoint(aModelRef, aPoint, 2, ELcdKML22AltitudeMode.CLAMP_TO_GROUND, DONT_EXTRUDE);
  }

  /**
   * Create a 3D KML point.
   *
   * @param aModelRef the model reference of the given point
   * @param aPoint a WGS 84 geodetic point.
   * @param aAltMode the altitude mode. Either {@link ELcdKML22AltitudeMode#ABSOLUTE} or {@link ELcdKML22AltitudeMode#RELATIVE_TO_GROUND}
   * @param aExtrude the extrusion mode.
   * @return a {@link TLcdKML22Point} instance.
   */
  public static TLcdKML22Point createPoint3D(ILcdModelReference aModelRef, ILcdPoint aPoint, ELcdKML22AltitudeMode aAltMode, boolean aExtrude) {
    if (aAltMode == ELcdKML22AltitudeMode.CLAMP_TO_GROUND) {
      throw new IllegalArgumentException("AltitudeMode should be either ABSOLUTE or RELATIVE_T0_GROUND. Got " + aAltMode);
    }
    return createPoint(aModelRef, aPoint, 3, aAltMode, aExtrude);
  }

  private static TLcdKML22Point createPoint(ILcdModelReference aModelRef, ILcdPoint aPoint, int aDimension, ELcdKML22AltitudeMode aAltMode,
                                            boolean aExtrude) {
    if (aPoint == null) {
      throw new NullPointerException("Point is null");
    }
    CoordinatesBuilder coords = CoordinatesBuilder.newInstance(aModelRef, aDimension);
    coords.addPoint(aPoint);

    TLcdKML22Point point = new TLcdKML22Point(TLcdKML22DataTypes.PointType);
    point.setCoordinates(coords.build());
    point.setAltitudeMode(aAltMode);
    point.setExtrude(aExtrude);

    return point;
  }

  /**
   * Builds WGS 84 geodetic coordinates.
   */
  private static abstract class CoordinatesBuilder {

    private static final TLcdGeodeticReference WGS84 = new TLcdGeodeticReference();

    private static CoordinatesBuilder newInstance(ILcdModelReference aModelRef, int aDimension) {
      TLcdGeoReference2GeoReference transformation = new TLcdGeoReference2GeoReference((ILcdGeoReference) aModelRef, WGS84);
      TLcdKML22Coordinates coordinates = new TLcdKML22Coordinates();
      coordinates.setDimension(aDimension);
      if (aDimension == 2) {
        return new CoordinatesBuilder2D(transformation, coordinates);
      } else if (aDimension == 3) {
        return new CoordinatesBuilder3D(transformation, coordinates);
      } else {
        throw new IllegalArgumentException("Dimension should be 2 or 3. Got " + aDimension);
      }
    }

    protected final TLcdGeoReference2GeoReference toWGS84;
    protected final TLcdKML22Coordinates coords;
    private final TLcdLonLatHeightPoint temp = new TLcdLonLatHeightPoint();

    protected CoordinatesBuilder(TLcdGeoReference2GeoReference aToWGS84, TLcdKML22Coordinates aCoords) {
      toWGS84 = aToWGS84;
      coords = aCoords;
    }

    /**
     * Transform the given point using this builder transformation and store the transformed coordinates.
     * If the transformation fails, the point is ignored.
     *
     * @param aPoint a point in model coordinates
     * @return this builder
     */
    public final CoordinatesBuilder addPoint(ILcdPoint aPoint) {
      try {
        toWGS84.sourcePoint2destinationSFCT(aPoint, temp);
      } catch (TLcdOutOfBoundsException e) {
        // ignored
      }
      return storePoint(temp);
    }

    public abstract CoordinatesBuilder storePoint(ILcdPoint aPoint);

    public TLcdKML22Coordinates build() {
      return coords;
    }

  }

  private static class CoordinatesBuilder2D extends CoordinatesBuilder {

    private CoordinatesBuilder2D(TLcdGeoReference2GeoReference aToWGS84, TLcdKML22Coordinates aCoords) {
      super(aToWGS84, aCoords);
    }

    public CoordinatesBuilder2D storePoint(ILcdPoint aPoint) {
      coords.insert2DPoint(coords.getPointCount(), aPoint.getX(), aPoint.getY());
      return this;
    }

  }

  private static class CoordinatesBuilder3D extends CoordinatesBuilder {

    private CoordinatesBuilder3D(TLcdGeoReference2GeoReference aToWGS84, TLcdKML22Coordinates aCoords) {
      super(aToWGS84, aCoords);
    }

    public CoordinatesBuilder3D storePoint(ILcdPoint aPoint) {
      coords.insert3DPoint(coords.getPointCount(), aPoint.getX(), aPoint.getY(), aPoint.getZ());
      return this;
    }

  }

}
