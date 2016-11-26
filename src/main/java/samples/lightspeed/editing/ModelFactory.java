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
package samples.lightspeed.editing;

import java.util.ArrayList;
import java.util.List;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
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
 * The model factory for the editing sample. Creates a model containing various shapes.
 */
public class ModelFactory {

  static final String SHAPES_TYPE = "Shapes";

  public ILcdModel createShapesModel() {
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();
    TLcdVectorModel vectorModel = new TLcdVectorModel(modelReference, new TLcdModelDescriptor(SHAPES_TYPE, SHAPES_TYPE, SHAPES_TYPE));
    createShapes(vectorModel, modelReference.getGeodeticDatum(), false, new TLcdLonLatPoint(20, 0));
    createShapes(vectorModel, modelReference.getGeodeticDatum(), true, new TLcdLonLatPoint(-20, 1));
    vectorModel.addElement(createLonLatPoint(), ILcdModel.FIRE_NOW);
    vectorModel.addElement(createLonLatHeightPoint(), ILcdModel.FIRE_NOW);

    return vectorModel;
  }

  /**
   * Retrieves the geodetic datum from the given model. If the given model does not have a geodetic
   * model reference, an exception will be thrown.
   *
   * @param aModel a model
   *
   * @return the geodetic datum of the model
   */
  private static ILcdGeodeticDatum getDatum(ILcdModel aModel) {
    ILcdModelReference modelRef = aModel.getModelReference();
    if (modelRef instanceof ILcdGeodeticReference) {
      return ((ILcdGeodeticReference) modelRef).getGeodeticDatum();
    } else {
      throw new IllegalArgumentException("Could not retrieve datum, given model does not have a geodetic reference!");
    }
  }

  /**
   * Creates and adds various shapes to the given model.
   *
   * @param aModel    the model to add the shapes in
   * @param aDatum    the datum
   * @param aExtruded whether or not to extrude the shapes
   * @param aOffset   the offset for the shapes, in the reference of the model
   */
  private static void createShapes(TLcdVectorModel aModel, ILcdGeodeticDatum aDatum, boolean aExtruded, ILcdPoint aOffset) {
    // Create the base shapes
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    shapes.add(new TLcdXYText("Lightspeed", 0 + aOffset.getX(), 0 + aOffset.getY(), 1.2, 2.4, ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_CENTER, 0));
    shapes.add(new TLcdLonLatEllipse(20 + aOffset.getX(), 10 + aOffset.getY(), 500000, 250000, 50, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatCircle(-10 + aOffset.getX(), 0 + aOffset.getY(), 300000, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatArc(0 + aOffset.getX(), 10 + aOffset.getY(), 500000, 250000, 10, 0, 90, aDatum.getEllipsoid()));
    shapes.add(new TLcdLonLatArcBand(10 + aOffset.getX(), 0 + aOffset.getY(), 250000, 500000, 50, 90, aDatum.getEllipsoid()));
    shapes
        .add(new TLcdLonLatLine(new TLcdLonLatPoint(7.5, -7.5), new TLcdLonLatPoint(12.5, -12.5), aDatum
            .getEllipsoid()));
    ILcd2DEditablePoint[] points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(0 + aOffset.getX(), -9 + aOffset.getY()),
        new TLcdLonLatPoint(2 + aOffset.getX(), -8 + aOffset.getY()),
        new TLcdLonLatPoint(1 + aOffset.getX(), -10 + aOffset.getY()),
        new TLcdLonLatPoint(2 + aOffset.getX(), -12 + aOffset.getY()),
        new TLcdLonLatPoint(0 + aOffset.getX(), -11 + aOffset.getY()),
        new TLcdLonLatPoint(-2 + aOffset.getX(), -12 + aOffset.getY()),
        new TLcdLonLatPoint(-1 + aOffset.getX(), -10 + aOffset.getY()),
        new TLcdLonLatPoint(-2 + aOffset.getX(), -8 + aOffset.getY())
    };
    shapes.add(new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, false), aDatum
        .getEllipsoid()));

    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-10 + aOffset.getX(), -9 + aOffset.getY()),
        new TLcdLonLatPoint(-8 + aOffset.getX(), -8 + aOffset.getY()),
        new TLcdLonLatPoint(-9 + aOffset.getX(), -10 + aOffset.getY()),
        new TLcdLonLatPoint(-8 + aOffset.getX(), -12 + aOffset.getY()),
        new TLcdLonLatPoint(-10 + aOffset.getX(), -11 + aOffset.getY()),
        new TLcdLonLatPoint(-12 + aOffset.getX(), -12 + aOffset.getY()),
        new TLcdLonLatPoint(-11 + aOffset.getX(), -10 + aOffset.getY()),
        new TLcdLonLatPoint(-12 + aOffset.getX(), -8 + aOffset.getY()),
        new TLcdLonLatPoint(-10 + aOffset.getX(), -9 + aOffset.getY())
    };
    shapes.add(new TLcdLonLatPolyline(new TLcd2DEditablePointList(points, false), aDatum
        .getEllipsoid()));
    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-12.5 + aOffset.getX(), 7.5 + aOffset.getY()),
        new TLcdLonLatPoint(-11 + aOffset.getX(), 10.0 + aOffset.getY()),
        new TLcdLonLatPoint(-9 + aOffset.getX(), 10.0 + aOffset.getY()),
        new TLcdLonLatPoint(-7.5 + aOffset.getX(), 12.5 + aOffset.getY())
    };
    shapes.add(new TLcdLonLatBuffer(points, 100000, aDatum.getEllipsoid()));

    if (!aExtruded) {
      TLcdLonLatHeightPolyline llhPolyline = new TLcdLonLatHeightPolyline();
      llhPolyline.insert3DPoint(0, -15.0 + aOffset.getX(), -15.0 + aOffset.getY(), 1e5);
      llhPolyline.insert3DPoint(0, -20.0 + aOffset.getX(), -15.0 + aOffset.getY(), 5e5);
      llhPolyline.insert3DPoint(0, -20.0 + aOffset.getX(), -20.0 + aOffset.getY(), 5e5);
      llhPolyline.insert3DPoint(0, -25.0 + aOffset.getX(), -25.0 + aOffset.getY(), 5e5);
      shapes.add(llhPolyline);
    }

    TLcdLonLatCompositeRing ring = new TLcdLonLatCompositeRing(aDatum.getEllipsoid());
    ring.getCurves().add(new TLcdLonLatPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(8 + aOffset.getX(), 20 + aOffset.getY()),
                new TLcdLonLatPoint(10 + aOffset.getX(), 15 + aOffset.getY()),
                new TLcdLonLatPoint(12 + aOffset.getX(), 20 + aOffset.getY()),
            },
            false
        ),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(12 + aOffset.getX(), 20 + aOffset.getY()),
        new TLcdLonLatPoint(14 + aOffset.getX(), 21.5 + aOffset.getY()),
        new TLcdLonLatPoint(11.5 + aOffset.getX(), 23 + aOffset.getY()),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(11.5 + aOffset.getX(), 23 + aOffset.getY()),
        new TLcdLonLatPoint(10 + aOffset.getX(), 25 + aOffset.getY()),
        new TLcdLonLatPoint(8.5 + aOffset.getX(), 23 + aOffset.getY()),
        aDatum.getEllipsoid()
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(8.5 + aOffset.getX(), 23 + aOffset.getY()),
        new TLcdLonLatPoint(6 + aOffset.getX(), 21.5 + aOffset.getY()),
        new TLcdLonLatPoint(8 + aOffset.getX(), 20 + aOffset.getY()),
        aDatum.getEllipsoid()
    ));
    shapes.add(ring);

    TLcdLonLatPoint[] axisPoints = new TLcdLonLatPoint[18];
    for (int i = 0; i < 18; i++) {
      axisPoints[i] = new TLcdLonLatPoint(
          25 + (4 + i % 4) * Math.cos(Math.toRadians(i * 20)) + aOffset.getX(),
          25 + (4 + i % 4) * Math.sin(Math.toRadians(i * 20)) + aOffset.getY()
      );
    }
    TLcdLonLatPolyline bufferAxis = new TLcdLonLatPolyline(new TLcd2DEditablePointList(axisPoints, false));
    shapes.add(new TLcdLonLatGeoBuffer(bufferAxis, 50e3, aDatum.getEllipsoid()));

    shapes.add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(-8 + aOffset.getX(), 18 + aOffset.getY()),
        new TLcdLonLatPoint(-12 + aOffset.getX(), 20 + aOffset.getY()),
        new TLcdLonLatPoint(-6 + aOffset.getX(), 22 + aOffset.getY()),
        aDatum.getEllipsoid()
    ));
    shapes.add(new TLcdLonLatCircularArcByBulge(
        new TLcdLonLatPoint(-10 + aOffset.getX(), 20 + aOffset.getY()),
        new TLcdLonLatPoint(-8 + aOffset.getX(), 22 + aOffset.getY()),
        0.75,
        aDatum.getEllipsoid()
    ));
    shapes.add(new TLcdLonLatRhumbPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(-9 + aOffset.getX(), 21 + aOffset.getY()),
                new TLcdLonLatPoint(-6 + aOffset.getX(), 18 + aOffset.getY())
            },
            false
        ),
        aDatum.getEllipsoid()
    ));

    shapes.add(createLonLatSurface(aDatum.getEllipsoid(), aOffset));

    TLcdLonLatPoint[] contourPoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint[] holePoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint center = new TLcdLonLatPoint(-20 + aOffset.getX(), 20 + aOffset.getY());
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

    TLcdLonLatBounds bounds = new TLcdLonLatBounds(20 + aOffset.getX(), -10 + aOffset.getY(), 2, 2);
    shapes.add(bounds);

    if (!aExtruded) {
      ILcdEllipsoid ellipsoid = getDatum(aModel).getEllipsoid();
      shapes.add(createSphere(ellipsoid, aOffset));
      shapes.add(createDome(ellipsoid, aOffset));
      shapes.add(createBuffer(ellipsoid, aOffset));
      shapes.add(create3DArcBand(ellipsoid, aOffset));

      TLcdLonLatHeightBounds bounds3D = new TLcdLonLatHeightBounds(30 + aOffset.getX(), -10 + aOffset.getY(), 1e5, 2, 2, 2e5);
      shapes.add(bounds3D);

      shapes.add(createVariableGeoBuffer(ellipsoid, aOffset));

      // Add the shapes as flat shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        aModel.addElement(shape, ILcdFireEventMode.NO_EVENT);
      }
    } else {
      // Add the shapes as extruded shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        aModel.addElement(new TLcdExtrudedShape(shape, 0.5e5, 2.5e5), ILcdFireEventMode.NO_EVENT);
      }
    }
  }

  /**
   * Creates a Lon-Lat-Height Sphere object.
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset    the offset for the shapes, in the reference of the model
   *
   * @return a sphere
   */
  private static TLcdLonLatHeightSphere createSphere(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20 + aOffset.getX(), 0 + aOffset.getY());
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightSphere(circle, 2e5);
  }

  /**
   * Creates a Lon-Lat-Height Dome object.
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset    the offset for the shapes, in the reference of the model
   *
   * @return aSphere
   */
  private static TLcdLonLatHeightDome createDome(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20 + aOffset.getX(), 10 + aOffset.getY());
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightDome(circle, 2e5);
  }

  /**
   * Creates a Lon-Lat-Height Buffer object.
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset    the offset for the shapes, in the reference of the model
   *
   * @return a buffer
   */
  private static TLcdLonLatHeightBuffer createBuffer(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    ILcd3DEditablePoint[] axisPoints = new ILcd3DEditablePoint[5];
    axisPoints[0] = new TLcdLonLatHeightPoint(20 + aOffset.getX(), 0 + aOffset.getY(), 5e4);
    axisPoints[1] = new TLcdLonLatHeightPoint(25 + aOffset.getX(), 0 + aOffset.getY(), 1e5);
    axisPoints[2] = new TLcdLonLatHeightPoint(25 + aOffset.getX(), 5 + aOffset.getY(), 1.5e5);
    axisPoints[3] = new TLcdLonLatHeightPoint(30 + aOffset.getX(), 5 + aOffset.getY(), 2e5);
    axisPoints[4] = new TLcdLonLatHeightPoint(30 + aOffset.getX(), 0 + aOffset.getY(), 2.5e5);
    return new TLcdLonLatHeightBuffer(axisPoints, 1e5, 1e5, aEllipsoid);
  }

  private static TLcdLonLatHeightVariableGeoBuffer createVariableGeoBuffer(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    ILcd3DEditablePoint[] axisPoints = new ILcd3DEditablePoint[5];
    axisPoints[0] = new TLcdLonLatHeightPoint(-5 + aOffset.getX(), -18 + aOffset.getY(), 3.5e5);
    axisPoints[1] = new TLcdLonLatHeightPoint(0 + aOffset.getX(), -16 + aOffset.getY(), 3e5);
    axisPoints[2] = new TLcdLonLatHeightPoint(5 + aOffset.getX(), -16 + aOffset.getY(), 4.5e5);
    axisPoints[3] = new TLcdLonLatHeightPoint(10 + aOffset.getX(), -16 + aOffset.getY(), 5.5e5);
    axisPoints[4] = new TLcdLonLatHeightPoint(15 + aOffset.getX(), -18 + aOffset.getY(), 7e5);

    return new TLcdLonLatHeightVariableGeoBuffer(
        new TLcd3DEditablePointList(axisPoints, false),
        new double[]{5e4, 1e5, 2e5, 3e5, 4e5},
        new double[]{1e5, 1e5, 1e5, 1e5, 3e5},
        new double[]{1e5, 2e5, 1e5, 2e5, 2e5},
        aEllipsoid
    );
  }

  private static TLcdLonLatHeight3DArcBand create3DArcBand(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    return new TLcdLonLatHeight3DArcBand(-28 + aOffset.getX(), -10 + aOffset.getY(), 1e5, 250000, 500000, 50, 90, 0, 30, 0, 0, aEllipsoid);
  }

  /**
   * Creates a Lon-Lat Surface object.
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset    the offset for the shapes, in the reference of the model
   *
   * @return a surface
   */
  private static ILcdSurface createLonLatSurface(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing(new TLcdLonLatCircle(
                                new TLcdLonLatPoint(0.0 + aOffset.getX(), 28.0 + aOffset.getY()), 300000, aEllipsoid)
    );
    surface.getInteriorRings().add(new TLcdLonLatCircle(
                                       new TLcdLonLatPoint(1.0 + aOffset.getX(), 29.0 + aOffset.getY()), 50000, aEllipsoid)
    );
    TLcdLonLatCompositeRing interiorRing = new TLcdLonLatCompositeRing(aEllipsoid);
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0 + aOffset.getX(), 26.0 + aOffset.getY()),
        new TLcdLonLatPoint(-1.0 + aOffset.getX(), 27.5 + aOffset.getY()),
        new TLcdLonLatPoint(1.0 + aOffset.getX(), 28.0 + aOffset.getY()),
        aEllipsoid));
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0 + aOffset.getX(), 28.0 + aOffset.getY()),
        new TLcdLonLatPoint(2.0 + aOffset.getX(), 27.0 + aOffset.getY()),
        new TLcdLonLatPoint(1.0 + aOffset.getX(), 26.0 + aOffset.getY()),
        aEllipsoid));
    surface.getInteriorRings().add(interiorRing);
    return surface;
  }

  /**
   * Creates a 3D point.
   *
   * @return a point
   */
  private static ILcdPoint createLonLatHeightPoint() {
    return new TLcdLonLatHeightPoint(20, -5, 1e5);
  }

  /**
   * Creates a 2D point.
   *
   * @return a point
   */
  private static ILcdPoint createLonLatPoint() {
    return new TLcdLonLatPoint(21, -6);
  }

}
