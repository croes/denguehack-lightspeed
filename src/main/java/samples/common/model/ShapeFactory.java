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
package samples.common.model;

import java.util.ArrayList;
import java.util.List;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdSurface;
import com.luciad.shape.ILcdText;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.TLcdSurface;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.*;
import com.luciad.util.ILcdFireEventMode;

/**
 * Factory class used to create various types of
 * geometric shape objects.
 */
class ShapeFactory {

  /**
   * Creates a Lon-Lat-Height Sphere object.
   */
  public static TLcdLonLatHeightSphere createSphere(ILcdEllipsoid aEllipsoid) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20, 0);
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightSphere(circle, 2e5);
  }

  /**
   * Creates a Lon-Lat-Height Dome object.
   */
  public static TLcdLonLatHeightDome createDome(ILcdEllipsoid aEllipsoid) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20, 10);
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightDome(circle, 2e5);
  }

  /**
   * Creates a text object.
   */
  public static TLcdXYText createText() {
    return new TLcdXYText("Lightspeed",
                          0, 0,
                          1e5, 2e5,
                          ILcdText.ALIGNMENT_CENTER,
                          ILcdText.ALIGNMENT_CENTER,
                          0);
  }

  /**
   * Creates a Lon-Lat-Height Buffer object.
   */
  public static TLcdLonLatHeightBuffer createBuffer(ILcdEllipsoid aEllipsoid) {
    ILcd3DEditablePoint[] axisPoints = new ILcd3DEditablePoint[5];
    axisPoints[0] = new TLcdLonLatHeightPoint(20, 0, 5e4);
    axisPoints[1] = new TLcdLonLatHeightPoint(25, 0, 1e5);
    axisPoints[2] = new TLcdLonLatHeightPoint(25, 5, 1.5e5);
    axisPoints[3] = new TLcdLonLatHeightPoint(30, 5, 2e5);
    axisPoints[4] = new TLcdLonLatHeightPoint(30, 0, 2.5e5);
    return new TLcdLonLatHeightBuffer(axisPoints, 1e5, 1e5, aEllipsoid);
  }

  private static TLcdLonLatHeightVariableGeoBuffer createVariableGeoBuffer(ILcdEllipsoid aEllipsoid) {
    ILcd3DEditablePoint[] axisPoints = new ILcd3DEditablePoint[5];
    axisPoints[0] = new TLcdLonLatHeightPoint(-5, -18, 3.5e5);
    axisPoints[1] = new TLcdLonLatHeightPoint(0, -16, 3e5);
    axisPoints[2] = new TLcdLonLatHeightPoint(5, -16, 4.5e5);
    axisPoints[3] = new TLcdLonLatHeightPoint(10, -16, 5.5e5);
    axisPoints[4] = new TLcdLonLatHeightPoint(15, -18, 8e5);

    return new TLcdLonLatHeightVariableGeoBuffer(
        new TLcd3DEditablePointList(axisPoints, false),
        new double[]{5e4, 1e5, 2e5, 3e5, 4e5},
        new double[]{1e5, 1e5, 1e5, 1e5, 3e5},
        new double[]{1e5, 2e5, 1e5, 2e5, 2e5},
        aEllipsoid
    );
  }

  /**
   * Creates several base shapes and their extruded versions.
   */
  public static void createShapes(ILcdModel aModel, ILcdGeodeticDatum aDatum, boolean aExtruded) {
    // Create the base shapes
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    shapes.add(new TLcdXYText("Lightspeed", 0, 0, 1.2, 2.4, ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_CENTER, 0));
    shapes.add(new TLcdLonLatEllipse(20, 10, 500000, 250000, 50, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatCircle(-10, 0, 300000, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatArc(0, 10, 500000, 250000, 10, 0, 90, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatArcBand(10, 0, 250000, 500000, 50, 90, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatLine(new TLcdLonLatPoint(7.5, -7.5), new TLcdLonLatPoint(12.5, -12.5), aDatum
        .getEllipsoid()));
    ILcd2DEditablePoint[] points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(0, -9),
        new TLcdLonLatPoint(2, -8),
        new TLcdLonLatPoint(1, -10),
        new TLcdLonLatPoint(2, -12),
        new TLcdLonLatPoint(0, -11),
        new TLcdLonLatPoint(-2, -12),
        new TLcdLonLatPoint(-1, -10),
        new TLcdLonLatPoint(-2, -8)
    };
    shapes.add(new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, false), aDatum
        .getEllipsoid()));
    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-10, -9),
        new TLcdLonLatPoint(-8, -8),
        new TLcdLonLatPoint(-9, -10),
        new TLcdLonLatPoint(-8, -12),
        new TLcdLonLatPoint(-10, -11),
        new TLcdLonLatPoint(-12, -12),
        new TLcdLonLatPoint(-11, -10),
        new TLcdLonLatPoint(-12, -8),
        new TLcdLonLatPoint(-10, -9)
    };
    shapes.add(new TLcdLonLatPolyline(new TLcd2DEditablePointList(points, false), aDatum
        .getEllipsoid()));
    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-12.5, 7.5),
        new TLcdLonLatPoint(-11, 10.0),
        new TLcdLonLatPoint(-9, 10.0),
        new TLcdLonLatPoint(-7.5, 12.5)
    };
    shapes.add(new TLcdLonLatBuffer(points, 100000, aDatum.getEllipsoid()));

    if (!aExtruded) {
      TLcdLonLatHeightPolyline llhPolyline = new TLcdLonLatHeightPolyline();
      llhPolyline.insert3DPoint(0, -15.0, -15.0, 1e5);
      llhPolyline.insert3DPoint(0, -20.0, -15.0, 5e5);
      llhPolyline.insert3DPoint(0, -20.0, -20.0, 5e5);
      llhPolyline.insert3DPoint(0, -25.0, -25.0, 5e5);
      shapes.add(llhPolyline);
    }

    TLcdLonLatCompositeRing ring = new TLcdLonLatCompositeRing(aDatum.getEllipsoid());
    ring.getCurves().add(new TLcdLonLatPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(8, 20),
                new TLcdLonLatPoint(10, 15),
                new TLcdLonLatPoint(12, 20),
            },
            false
        ),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(12, 20),
        new TLcdLonLatPoint(14, 21.5),
        new TLcdLonLatPoint(11.5, 23),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(11.5, 23),
        new TLcdLonLatPoint(10, 25),
        new TLcdLonLatPoint(8.5, 23),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(8.5, 23),
        new TLcdLonLatPoint(6, 21.5),
        new TLcdLonLatPoint(8, 20),
        aDatum.getEllipsoid()
    ));
    shapes.add(ring);

    TLcdLonLatPoint[] axisPoints = new TLcdLonLatPoint[18];
    for (int i = 0; i < 18; i++) {
      axisPoints[i] = new TLcdLonLatPoint(
          25 + (4 + i % 4) * Math.cos(Math.toRadians(i * 20)),
          25 + (4 + i % 4) * Math.sin(Math.toRadians(i * 20))
      );
    }
    TLcdLonLatPolyline bufferAxis = new TLcdLonLatPolyline(new TLcd2DEditablePointList(axisPoints, false));
    shapes.add(new TLcdLonLatGeoBuffer(bufferAxis, 50e3, aDatum.getEllipsoid()));

    shapes.add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(-8, 18),
        new TLcdLonLatPoint(-12, 20),
        new TLcdLonLatPoint(-6, 22),
        aDatum.getEllipsoid()
    ));
    shapes.add(new TLcdLonLatCircularArcByBulge(
        new TLcdLonLatPoint(-10, 20),
        new TLcdLonLatPoint(-8, 22),
        0.75,
        aDatum.getEllipsoid()
    ));
    shapes.add(new TLcdLonLatRhumbPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(-9, 21),
                new TLcdLonLatPoint(-6, 18)
            },
            false
        ),
        aDatum.getEllipsoid()
    ));

    shapes.add(createLonLatSurface(aDatum.getEllipsoid()));

    TLcdLonLatPoint[] contourPoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint[] holePoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint center = new TLcdLonLatPoint(-20, 20);
    double outerRadius = 400000;
    double innerRadius = 100000;
    for (int i = 0; i < 5; ++i) {
      TLcdLonLatPoint inner = new TLcdLonLatPoint();
      TLcdLonLatPoint outer = new TLcdLonLatPoint();
      aDatum.getEllipsoid().geodesicPointSFCT(center, outerRadius, i * 72, outer);
      aDatum.getEllipsoid().geodesicPointSFCT(center, innerRadius, i * 72, inner);
      contourPoints[i] = outer;
      holePoints[i] = inner;
    }

    TLcdComplexPolygon complexPolygon = new TLcdComplexPolygon(
        new ILcdPolygon[]{
            new TLcdLonLatPolygon(new TLcd2DEditablePointList(contourPoints, false)),
            new TLcdLonLatPolygon(new TLcd2DEditablePointList(holePoints, false))
        }
    );
    shapes.add(complexPolygon);

    TLcdLonLatBounds bounds = new TLcdLonLatBounds(20, -10, 2, 2);
    shapes.add(bounds);

    if (!aExtruded) {
      ILcdEllipsoid ellipsoid = aDatum.getEllipsoid();
      shapes.add(createSphere(ellipsoid));
      shapes.add(createDome(ellipsoid));
      shapes.add(createBuffer(ellipsoid));

      TLcdLonLatHeightBounds bounds3D = new TLcdLonLatHeightBounds(30, -10, 1e5, 2, 2, 2e5);
      shapes.add(bounds3D);

      shapes.add(createVariableGeoBuffer(ellipsoid));

      // Add the shapes as flat shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        aModel.addElement(shape, ILcdFireEventMode.NO_EVENT);
      }
    } else {
      // Add the shapes as extruded shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        aModel
            .addElement(new TLcdExtrudedShape(shape, 0.5e5, 2.5e5), ILcdFireEventMode.NO_EVENT);
      }
    }
  }

  /**
   * Creates a Lon-Lat Surface object.
   */
  public static ILcdSurface createLonLatSurface(ILcdEllipsoid aEllipsoid) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing(new TLcdLonLatCircle(
                                new TLcdLonLatPoint(0.0, 28.0), 300000, aEllipsoid)
    );
    surface.getInteriorRings().add(new TLcdLonLatCircle(
                                       new TLcdLonLatPoint(1.0, 29.0), 50000, aEllipsoid)
    );
    TLcdLonLatCompositeRing interiorRing = new TLcdLonLatCompositeRing(aEllipsoid);
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0, 26.0),
        new TLcdLonLatPoint(-1.0, 27.5),
        new TLcdLonLatPoint(1.0, 28.0),
        aEllipsoid));
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0, 28.0),
        new TLcdLonLatPoint(2.0, 27.0),
        new TLcdLonLatPoint(1.0, 26.0),
        aEllipsoid));
    surface.getInteriorRings().add(interiorRing);
    return surface;
  }

  /**
   * Creates an ILcdPoint.
   */
  public static ILcdPoint createLonLatHeightPoint() {
    return new TLcdLonLatHeightPoint(20, -5, 1e5);
  }

  public static ILcdPoint createLonLatPoint() {
    return new TLcdLonLatPoint(21, -6);
  }
}
