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
import com.luciad.format.kml22.model.geometry.TLcdKML22MultiGeometry;
import com.luciad.format.kml22.model.util.ELcdKML22AltitudeMode;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdCircle;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape3D.ILcdDome;
import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.shape.shape3D.ILcdSphere;

/**
 * Basic implementation of {@link IKML22ShapeConverter} to convert Polygons, Polylines, Points, Circles, Extruded Shapes or
 * Shapelists.
 */
public class KML22ShapeConverter implements IKML22ShapeConverter {

  @Override
  public boolean canConvertIntoShape(ILcdModel aModel, Object aObject) {
    return aObject instanceof ILcdShape && canConvertShape((ILcdShape) aObject);
  }

  @Override
  public TLcdKML22AbstractGeometry convertIntoShape(ILcdModel aModel, Object aObject) {
    if (!canConvertIntoShape(aModel, aObject)) {
      throw new IllegalArgumentException("Cannot convert object into shape.");
    }
    return convertShape(aModel.getModelReference(), (ILcdShape) aObject);
  }

  /**
   * Returns whether this converter can convert the shape into a KML abstract geometry.
   *
   * @param aShape the shape that could be converted
   * @return true if it can convert this shape
   */
  protected boolean canConvertShape(ILcdShape aShape) {
    if (aShape instanceof ILcdPoint || aShape instanceof ILcdPointList) {
      return true;
    }
    if (isCircle(aShape)) {
      return true;
    }
    if (aShape instanceof ILcdExtrudedShape) {
      return canConvertShape(((ILcdExtrudedShape) aShape).getBaseShape());
    }
    if (aShape instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aShape;

      for (int i = 0; i < shapeList.getShapeCount(); i++) {
        if (!(canConvertShape(shapeList.getShape(i)))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Convert the shape of the object, picking the 2D or 3D option based on the Z values
   * of the shape's control points.
   *
   * @param aModelRef the model reference of aShape
   * @param aShape the shape to convert.
   *
   * @return a {@link TLcdKML22AbstractGeometry} instance.
   */
  private TLcdKML22AbstractGeometry convertShape(ILcdModelReference aModelRef, ILcdShape aShape) {
    if (aShape instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aShape;
      return convertShapeList(aModelRef, shapeList);
    }
    if (aShape instanceof ILcdExtrudedShape) {
      ILcdExtrudedShape extrudedShape = (ILcdExtrudedShape) aShape;
      return KML22GeometryUtil.createExtrudedShape(aModelRef, extrudedShape);
    }

    boolean hasZ = false;

    if (isCircle(aShape)) {
      ILcdCircle circle = (ILcdCircle) aShape;
      ILcdPoint center = circle.getCenter();
      if (center.getZ() != 0.0) {
        hasZ = true;
      }
    }
    // Note that the following case covers polygon and polyline
    else if (aShape instanceof ILcdPointList) {
      ILcdPointList pointList = (ILcdPointList) aShape;
      if (hasNonZeroZ(pointList)) {
        hasZ = true;
      }
    } else if (aShape instanceof ILcdPoint) {
      ILcdPoint point = (ILcdPoint) aShape;
      if (point.getZ() != 0) {
        hasZ = true;
      }
    }

    if (hasZ) {
      return KML22GeometryUtil.createShape3D(aModelRef, aShape, ELcdKML22AltitudeMode.ABSOLUTE, false);
    } else {
      return KML22GeometryUtil.createShape2D(aModelRef, aShape);
    }
  }

  private boolean isCircle(ILcdShape aShape) {
    return aShape instanceof ILcdCircle &&
           !(aShape instanceof ILcdSphere) &&
           !(aShape instanceof ILcdDome);
  }

  private TLcdKML22AbstractGeometry convertShapeList(ILcdModelReference aModelRef, ILcdShapeList aShapeList) {
    TLcdKML22MultiGeometry multiGeometry = new TLcdKML22MultiGeometry(TLcdKML22DataTypes.MultiGeometryType);
    for (int i = 0; i < aShapeList.getShapeCount(); i++) {
      TLcdKML22AbstractGeometry convertedShape = convertShape(aModelRef, aShapeList.getShape(i));
      if (convertedShape != null) {
        multiGeometry.addShape(convertShape(aModelRef, aShapeList.getShape(i)));
      }
    }
    if (multiGeometry.getShapeCount() == 0) {
      return null;
    } else {
      return multiGeometry;
    }
  }

  /**
   * Check whether at least one height coordinates (z) of all point is the given list is not zero.
   *
   * @param aPointList a point list
   * @return {@code true} if all points in the given pointlist has a zero height(z) coordinate.
   */
  private boolean hasNonZeroZ(ILcdPointList aPointList) {
    for (int i = 0; i < aPointList.getPointCount(); i++) {
      ILcdPoint point = aPointList.getPoint(i);
      if (point.getZ() != 0.0) {
        return true;
      }
    }
    return false;
  }
}
