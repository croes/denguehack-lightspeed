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
package samples.lightspeed.internal.editing;

import java.util.ArrayList;
import java.util.List;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
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
import com.luciad.util.TLcdConstant;

/**
 * @see #createLonLatModel(boolean)
 * @see #createViewModel(boolean)
 * @see #createXYModel(boolean)
 */
public class ModelFactory {

  static final String SHAPES_TYPE = "IsAShape";
  static final String HAS_A_SHAPES_TYPE = "HasAShape";

  public ILcdModel createLonLatModel(boolean aHasAShape) {
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();
    TLcdVectorModel vectorModel = createVectorModel(aHasAShape, modelReference);
    TLcdXYPoint scale = new TLcdXYPoint(1, 1);
    createLonLatShapes(vectorModel, false, scale, new TLcdLonLatPoint(20, 0), modelReference.getGeodeticDatum().getEllipsoid(), aHasAShape);
    createLonLatShapes(vectorModel, true, scale, new TLcdLonLatPoint(-20, 1), modelReference.getGeodeticDatum().getEllipsoid(), aHasAShape);
    return vectorModel;
  }

  public ILcdModel createXYModel(boolean aHasAShape) {
    TLcdGridReference modelReference = new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical());
    TLcdVectorModel vectorModel = createVectorModel(aHasAShape, modelReference);
    TLcdXYPoint scale = new TLcdXYPoint(100000, 100000);
    createXYShapes(vectorModel, false, scale, new TLcdXYPoint(20 * scale.getX(), 0), aHasAShape, false);
    createXYShapes(vectorModel, true, scale, new TLcdXYPoint(-20 * scale.getX(), 1 * scale.getY()), aHasAShape, false);
    return vectorModel;
  }

  public ILcdModel createViewModel(boolean aHasAShape) {
    TLcdVectorModel vectorModel = createVectorModel(aHasAShape, null);
    TLcdXYPoint scale = new TLcdXYPoint(18, -16);
    createXYShapes(vectorModel, false, scale, new TLcdXYPoint(28 * scale.getX(), -32 * scale.getY()), aHasAShape, true);
    createXYShapes(vectorModel, true, scale, new TLcdXYPoint(20 * scale.getX(), -29 * scale.getY()), aHasAShape, true);
    return vectorModel;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // private stuff
  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the geodetic datum from the given model. If the given model does not have a geodetic
   * model reference, an exception will be thrown.
   *
   * @param aModel a model
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

  private TLcdVectorModel createVectorModel(boolean aHasAShape, ILcdModelReference aModelReference) {
    String referenceString = null;
    if (aModelReference instanceof ILcdGridReference) {
      referenceString = "-XY";
    } else if (aModelReference instanceof ILcdGeodeticReference) {
      referenceString = "-LonLat";
    } else {
      referenceString = "-ViewSpace";
    }
    TLcdVectorModel vectorModel = null;
    if (aHasAShape) {
      vectorModel = new TLcdVectorModel(aModelReference, new TLcdModelDescriptor(HAS_A_SHAPES_TYPE, HAS_A_SHAPES_TYPE, HAS_A_SHAPES_TYPE + referenceString));
    } else {
      vectorModel = new TLcdVectorModel(aModelReference, new TLcdModelDescriptor(SHAPES_TYPE, SHAPES_TYPE, SHAPES_TYPE + referenceString));
    }
    return vectorModel;
  }

  /**
   * Creates and adds various shapes to the given model.
   *
   * @param aModel    the model to add the shapes in
   * @param aExtruded whether or not to extrude the shapes
   * @param aOffset   the offset for the shapes, in the reference of the model.
   * @param aEllipsoid the ellipsoid
   * @param aHasAShape
   */
  private static void createLonLatShapes(TLcdVectorModel aModel, boolean aExtruded, ILcdPoint aScale, ILcdPoint aOffset, ILcdEllipsoid aEllipsoid, boolean aHasAShape) {
    // Create the base shapes
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    shapes.add(new TLcdXYText("Lightspeed", 0 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 1.2, 2.4, ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_CENTER, 0));
    shapes.add(new TLcdLonLatEllipse(20 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), 500000, 250000, 50, aEllipsoid));
    shapes.add(new TLcdLonLatCircle(-10 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 300000, aEllipsoid));
    shapes.add(new TLcdLonLatArc(0 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), 500000, 250000, 10, 0, 90, aEllipsoid));
    shapes.add(new TLcdLonLatArcBand(10 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 250000, 500000, 50, 90, aEllipsoid));
    shapes.add(new TLcdLonLatHeight3DArcBand(30 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), 1e5, 250000, 500000, 50, 90, 0, 30, 0, 0, aEllipsoid));
    shapes
        .add(new TLcdLonLatLine(new TLcdLonLatPoint(7.5 * aScale.getX() + aOffset.getX(), -7.5 * aScale.getY() + aOffset.getY()), new TLcdLonLatPoint(12.5 * aScale.getX() + aOffset.getX(), -12.5 * aScale.getY() + aOffset.getY()), aEllipsoid));
    ILcd2DEditablePoint[] points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(0 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(2 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(1 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(2 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(0 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-2 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-1 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-2 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY())
    };
    shapes.add(new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, false), aEllipsoid));

    if (!aExtruded) {
      TLcdLonLatHeightPoint[] points3D = new TLcdLonLatHeightPoint[]{
          new TLcdLonLatHeightPoint(8 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY(), 10000),
          new TLcdLonLatHeightPoint(10 * aScale.getX() + aOffset.getX(), -10.5 * aScale.getY() + aOffset.getY(), 11000),
          new TLcdLonLatHeightPoint(9 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), 27000),
          new TLcdLonLatHeightPoint(10 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY(), 15000),
          new TLcdLonLatHeightPoint(8 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY(), 11500),
      };
      shapes.add(new TLcdLonLatHeightPolygon(new TLcd3DEditablePointList(points3D, false), aEllipsoid));
    }

    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-10 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-8 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-9 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-8 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-10 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-12 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-11 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-12 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-10 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY())
    };
    shapes.add(new TLcdLonLatPolyline(new TLcd2DEditablePointList(points, false), aEllipsoid));
    points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(-12.5 * aScale.getX() + aOffset.getX(), 7.5 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-11 * aScale.getX() + aOffset.getX(), 10.0 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-9 * aScale.getX() + aOffset.getX(), 10.0 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-7.5 * aScale.getX() + aOffset.getX(), 12.5 * aScale.getY() + aOffset.getY())
    };
    shapes.add(new TLcdLonLatBuffer(points, 100000, aEllipsoid));

    if (!aExtruded) {
      TLcdLonLatHeightPolyline llhPolyline = new TLcdLonLatHeightPolyline();
      llhPolyline.insert3DPoint(0, -15.0 * aScale.getX() + aOffset.getX(), -15.0 * aScale.getY() + aOffset.getY(), 1e5);
      llhPolyline.insert3DPoint(0, -20.0 * aScale.getX() + aOffset.getX(), -15.0 * aScale.getY() + aOffset.getY(), 5e5);
      llhPolyline.insert3DPoint(0, -20.0 * aScale.getX() + aOffset.getX(), -20.0 * aScale.getY() + aOffset.getY(), 5e5);
      llhPolyline.insert3DPoint(0, -25.0 * aScale.getX() + aOffset.getX(), -25.0 * aScale.getY() + aOffset.getY(), 5e5);
      shapes.add(llhPolyline);
    }

    TLcdLonLatCompositeRing ring = new TLcdLonLatCompositeRing(aEllipsoid);
    ring.getCurves().add(new TLcdLonLatPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(8 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
                new TLcdLonLatPoint(10 * aScale.getX() + aOffset.getX(), 15 * aScale.getY() + aOffset.getY()),
                new TLcdLonLatPoint(12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
            },
            false
        ),
        aEllipsoid
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(14 * aScale.getX() + aOffset.getX(), 21.5 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(11.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        aEllipsoid
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(11.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(10 * aScale.getX() + aOffset.getX(), 25 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(8.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        aEllipsoid
    ));
    ring.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(8.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(6 * aScale.getX() + aOffset.getX(), 21.5 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(8 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        aEllipsoid
    ));
    shapes.add(ring);

    TLcdLonLatPoint[] axisPoints = new TLcdLonLatPoint[18];
    for (int i = 0; i < 18; i++) {
      axisPoints[i] = new TLcdLonLatPoint(
          25 + (4 + i % 4) * Math.cos(Math.toRadians(i * 20)) * aScale.getX() + aOffset.getX(),
          25 + (4 + i % 4) * Math.sin(Math.toRadians(i * 20)) * aScale.getY() + aOffset.getY()
      );
    }
    TLcdLonLatPolyline bufferAxis = new TLcdLonLatPolyline(new TLcd2DEditablePointList(axisPoints, false));
    shapes.add(new TLcdLonLatGeoBuffer(bufferAxis, 50e3, aEllipsoid));

    shapes.add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(-8 * aScale.getX() + aOffset.getX(), 18 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-6 * aScale.getX() + aOffset.getX(), 22 * aScale.getY() + aOffset.getY()),
        aEllipsoid
    ));
    shapes.add(new TLcdLonLatCircularArcByBulge(
        new TLcdLonLatPoint(-10 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-8 * aScale.getX() + aOffset.getX(), 22 * aScale.getY() + aOffset.getY()),
        0.75,
        aEllipsoid
    ));
    shapes.add(new TLcdLonLatRhumbPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[]{
                new TLcdLonLatPoint(-9 * aScale.getX() + aOffset.getX(), 21 * aScale.getY() + aOffset.getY()),
                new TLcdLonLatPoint(-6 * aScale.getX() + aOffset.getX(), 18 * aScale.getY() + aOffset.getY())
            },
            false
        ),
        aEllipsoid
    ));

    shapes.add(createLonLatSurface(aEllipsoid, aOffset, aScale));

    TLcdLonLatPoint[] contourPoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint[] holePoints = new TLcdLonLatPoint[5];
    TLcdLonLatPoint center = new TLcdLonLatPoint(-20 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY());
    double outerRadius = 400000;
    double innerRadius = 100000;
    for (int i = 0; i < 5; ++i) {
      TLcdLonLatPoint inner = new TLcdLonLatPoint();
      TLcdLonLatPoint outer = new TLcdLonLatPoint();
      aEllipsoid.geodesicPointSFCT(center, outerRadius, i * 72, outer);
      aEllipsoid.geodesicPointSFCT(center, innerRadius, i * 72, inner);
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

    TLcdLonLatBounds bounds = new TLcdLonLatBounds(20 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), 2, 2);
    shapes.add(bounds);

    shapes.add(new TLcdLonLatCircleBy3Points(
        4 + aOffset.getX(), 30 + aOffset.getY(), 4 + aOffset.getX(), 31 + aOffset.getY(), 5 + aOffset.getX(), 30 + aOffset.getY(), TLcdEllipsoid.DEFAULT
    ));

    shapes.add(new TLcdLonLatCircularArcByCenterPoint(
        4.5 + aOffset.getX(), 32.5 + aOffset.getY(), 70000, 30, 135, aEllipsoid));

    if (!aExtruded) {
      ILcdEllipsoid ellipsoid = getDatum(aModel).getEllipsoid();
      shapes.add(createLonLatSphere(ellipsoid, aOffset, aScale));
      shapes.add(createLonLatDome(ellipsoid, aOffset, aScale));
      shapes.add(createLonLatHeightBuffer(ellipsoid, aOffset, aScale));
      shapes.add(new TLcdLonLatPoint(21 * aScale.getX() + aOffset.getX(), -6 * aScale.getY() + aOffset.getY()));
      shapes.add(new TLcdLonLatHeightPoint(20 * aScale.getX() + aOffset.getX(), -5 * aScale.getY() + aOffset.getY(), 1e5));

      TLcdLonLatHeightBounds bounds3D = new TLcdLonLatHeightBounds(30 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), 1e5, 2, 2, 2e5);
      shapes.add(bounds3D);

      // Add the shapes as flat shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        if (aHasAShape) {
          aModel.addElement(new HasAShape(shape), ILcdFireEventMode.NO_EVENT);
        } else {
          aModel.addElement(shape, ILcdFireEventMode.NO_EVENT);
        }
      }
    } else {
      // Add the shapes as extruded shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        if (aHasAShape) {
          aModel.addElement(new HasAShape(new TLcdExtrudedShape(shape, 0.5e5, 2.5e5)), ILcdFireEventMode.NO_EVENT);
        } else {
          aModel.addElement(new TLcdExtrudedShape(shape, 0.5e5, 2.5e5), ILcdFireEventMode.NO_EVENT);
        }
      }
    }
  }

  /**
   * Creates and adds various shapes to the given model.
   *
   * @param aModel    the model to add the shapes in
   * @param aExtruded whether or not to extrude the shapes
   * @param aOffset   the offset for the shapes, in the reference of the model.
   * @param aHasAShape
   * @param aViewSize
   */
  private static void createXYShapes(TLcdVectorModel aModel, boolean aExtruded, ILcdPoint aScale, ILcdPoint aOffset, boolean aHasAShape, boolean aViewSize) {
    // Create the base shapes
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    shapes.add(new TLcdXYText("Lightspeed", 0 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 1.2 * aScale.getX(), 2.4 * aScale.getY(), ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_CENTER, 0));
    shapes.add(new TLcdXYEllipse(20 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), aViewSize ? 50 : 500000, aViewSize ? 25 : 250000, 50));
    shapes.add(new TLcdXYCircle(-10 * aScale.getX() + aOffset.getX(), 0.5 * aScale.getY() + aOffset.getY(), aViewSize ? 30 : 300000));
    shapes.add(new TLcdXYArc(0 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), aViewSize ? 50 : 500000, aViewSize ? 25 : 250000, 10, 0, 90));
    shapes.add(new TLcdXYArcBand(10 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), aViewSize ? 25 : 250000, aViewSize ? 50 : 500000, 50, 90));
    if (!aExtruded) {
      shapes.add(new TLcdXYZ3DArcBand(30 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY(), 0,
                                    aViewSize ? 25 : 250000, aViewSize ? 50 : 500000,
                                    50, 90,
                                    10, 30,
                                    0, 0));
    }
    shapes
        .add(new TLcdXYLine(new TLcdXYPoint(7.5 * aScale.getX() + aOffset.getX(), -7.5 * aScale.getY() + aOffset.getY()), new TLcdXYPoint(12.5 * aScale.getX() + aOffset.getX(), -12.5 * aScale.getY() + aOffset.getY())));
    ILcd2DEditablePoint[] points = new TLcdXYPoint[]{
        new TLcdXYPoint(0 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(2 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(1 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(2 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(0 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-2 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-1 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-2 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY())
    };
    shapes.add(new TLcdXYPolygon(new TLcd2DEditablePointList(points, false)));

    if (!aExtruded) {
      TLcdXYZPoint[] points3D = new TLcdXYZPoint[]{
          new TLcdXYZPoint(8 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 10000),
          new TLcdXYZPoint(10 * aScale.getX() + aOffset.getX(), -10.5 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 11000),
          new TLcdXYZPoint(9 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 27000),
          new TLcdXYZPoint(10 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 15000),
          new TLcdXYZPoint(8 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 11500),
      };
      shapes.add(new TLcdXYZPolygon(new TLcd3DEditablePointList(points3D, false)));
    }

    points = new TLcdXYPoint[]{
        new TLcdXYPoint(-10 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-8 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-9 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-8 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-10 * aScale.getX() + aOffset.getX(), -11 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-12 * aScale.getX() + aOffset.getX(), -12 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-11 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-12 * aScale.getX() + aOffset.getX(), -8 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-10 * aScale.getX() + aOffset.getX(), -9 * aScale.getY() + aOffset.getY())
    };
    shapes.add(new TLcdXYPolyline(new TLcd2DEditablePointList(points, false)));

    if (!aExtruded) {
      TLcdXYZPolyline llhPolyline = new TLcdXYZPolyline();
      llhPolyline.insert3DPoint(0, -15.0 * aScale.getX() + aOffset.getX(), -15.0 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 1e5);
      llhPolyline.insert3DPoint(0, -20.0 * aScale.getX() + aOffset.getX(), -15.0 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 5e5);
      llhPolyline.insert3DPoint(0, -20.0 * aScale.getX() + aOffset.getX(), -20.0 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 5e5);
      llhPolyline.insert3DPoint(0, -25.0 * aScale.getX() + aOffset.getX(), -25.0 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 5e5);
      shapes.add(llhPolyline);
    }

    TLcdXYCompositeRing ring = new TLcdXYCompositeRing();
    ring.getCurves().add(new TLcdXYPolyline(
        new TLcd2DEditablePointList(
            new TLcdXYPoint[]{
                new TLcdXYPoint(8 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
                new TLcdXYPoint(10 * aScale.getX() + aOffset.getX(), 15 * aScale.getY() + aOffset.getY()),
                new TLcdXYPoint(12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
            },
            false
        )
    ));
    ring.getCurves().add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(14 * aScale.getX() + aOffset.getX(), 21.5 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(11.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY())
    ));
    ring.getCurves().add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(11.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(10 * aScale.getX() + aOffset.getX(), 25 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(8.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY())
    ));
    ring.getCurves().add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(8.5 * aScale.getX() + aOffset.getX(), 23 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(6 * aScale.getX() + aOffset.getX(), 21.5 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(8 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY())
    ));
    shapes.add(ring);

    TLcdXYPoint[] axisPoints = new TLcdXYPoint[18];
    for (int i = 0; i < 18; i++) {
      axisPoints[i] = new TLcdXYPoint(
          25 + (4 + i % 4) * Math.cos(Math.toRadians(i * 20)) * aScale.getX() + aOffset.getX(),
          25 + (4 + i % 4) * Math.sin(Math.toRadians(i * 20)) * aScale.getY() + aOffset.getY()
      );
    }
    TLcdXYPolyline bufferAxis = new TLcdXYPolyline(new TLcd2DEditablePointList(axisPoints, false));
    shapes.add(new TLcdXYGeoBuffer(bufferAxis, aViewSize ? 5 : 50e3));

    shapes.add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(-8 * aScale.getX() + aOffset.getX(), 18 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-12 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-6 * aScale.getX() + aOffset.getX(), 22 * aScale.getY() + aOffset.getY())
    ));
    shapes.add(new TLcdXYCircularArcByBulge(
        new TLcdXYPoint(-10 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-8 * aScale.getX() + aOffset.getX(), 22 * aScale.getY() + aOffset.getY()),
        0.75
    ));

    shapes.add(createXYSurface(aOffset, aScale, aViewSize));

    TLcdXYPoint[] contourPoints = new TLcdXYPoint[5];
    TLcdXYPoint[] holePoints = new TLcdXYPoint[5];
    TLcdXYPoint center = new TLcdXYPoint(-20 * aScale.getX() + aOffset.getX(), 20 * aScale.getY() + aOffset.getY());
    double outerRadius = aViewSize ? 40 : 400000;
    double innerRadius = aViewSize ? 10 : 100000;
    for (int i = 0; i < 5; ++i) {
      TLcdXYPoint inner = new TLcdXYPoint();
      TLcdXYPoint outer = new TLcdXYPoint();
      outer.move2D(center.getX() + outerRadius * Math.cos(i * 72 * TLcdConstant.DEG2RAD),
                   center.getY() + outerRadius * Math.sin(i * 72 * TLcdConstant.DEG2RAD));
      inner.move2D(center.getX() + innerRadius * Math.cos(i * 72 * TLcdConstant.DEG2RAD),
                   center.getY() + innerRadius * Math.sin(i * 72 * TLcdConstant.DEG2RAD));
      contourPoints[i] = outer;
      holePoints[i] = inner;
    }
    TLcdComplexPolygon complexPolygon = new TLcdComplexPolygon(
        new ILcdPolygon[]{
            new TLcdXYPolygon(new TLcd2DEditablePointList(contourPoints, false)),
            new TLcdXYPolygon(new TLcd2DEditablePointList(holePoints, false))
        }
    );
    shapes.add(complexPolygon);
    TLcdXYBounds bounds = new TLcdXYBounds(20 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), 2 * aScale.getX(), 2 * aScale.getY());
    shapes.add(bounds);

    shapes.add(new TLcdXYCircleBy3Points(
        4 * aScale.getX() + aOffset.getX(), 30 * aScale.getY() + aOffset.getY(),
        4 * aScale.getX() + aOffset.getX(), 31 * aScale.getY() + aOffset.getY(),
        5 * aScale.getX() + aOffset.getX(), 30 * aScale.getY() + aOffset.getY()
    ));

    shapes.add(new TLcdXYCircularArcByCenterPoint(
        4.5 * aScale.getX() + aOffset.getX(), 32.5 * aScale.getY() + aOffset.getY(), aViewSize ? 70 : 70000, 30, 135));

    if (!aExtruded) {
      shapes.add(new TLcdXYPoint(21 * aScale.getX() + aOffset.getX(), -6 * aScale.getY() + aOffset.getY()));
      shapes.add(new TLcdXYZPoint(20 * aScale.getX() + aOffset.getX(), -5 * aScale.getY() + aOffset.getY(), aViewSize ? 0.01 : 1e5));

      TLcdXYZBounds bounds3D = new TLcdXYZBounds(30 * aScale.getX() + aOffset.getX(), -10 * aScale.getY() + aOffset.getY(), 1e5, 2 * aScale.getX(), 2 * aScale.getY(), aViewSize ? 0.01 : 2e5);
      shapes.add(bounds3D);
      // Add the shapes as flat shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        if (aHasAShape) {
          aModel.addElement(new HasAShape(shape), ILcdFireEventMode.NO_EVENT);
        } else {
          aModel.addElement(shape, ILcdFireEventMode.NO_EVENT);
        }
      }
    } else {
      // Add the shapes as extruded shapes
      for (int i = 0; i < shapes.size(); i++) {
        ILcdShape shape = (ILcdShape) shapes.get(i);
        if (aHasAShape) {
          aModel.addElement(new HasAShape(new TLcdExtrudedShape(shape, 0.5e5, aViewSize ? 0.01 : 2.5e5)), ILcdFireEventMode.NO_EVENT);
        } else {
          aModel.addElement(new TLcdExtrudedShape(shape, 0.5e5, aViewSize ? 0.01 : 2.5e5), ILcdFireEventMode.NO_EVENT);
        }
      }
    }
  }

  /**
   * Creates a Lon-Lat-Height Sphere object.
   *
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset
   * @param aScale
   * @return a sphere
   */
  private static TLcdLonLatHeightSphere createLonLatSphere(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset, ILcdPoint aScale) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY());
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightSphere(circle, 2e5);
  }

  /**
   * Creates a Lon-Lat-Height Dome object.
   *
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset
   * @param aScale
   * @return aSphere
   */
  private static TLcdLonLatHeightDome createLonLatDome(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset, ILcdPoint aScale) {
    TLcdLonLatPoint centerPoint = new TLcdLonLatPoint(-20 * aScale.getX() + aOffset.getX(), 10 * aScale.getY() + aOffset.getY());
    TLcdLonLatCircle circle = new TLcdLonLatCircle(centerPoint, 1e5, aEllipsoid);
    return new TLcdLonLatHeightDome(circle, 2e5);
  }

  /**
   * Creates a Lon-Lat-Height Buffer object.
   *
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset
   * @param aScale
   * @return a buffer
   */
  private static TLcdLonLatHeightBuffer createLonLatHeightBuffer(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset, ILcdPoint aScale) {
    ILcd3DEditablePoint[] axisPoints = new ILcd3DEditablePoint[5];
    axisPoints[0] = new TLcdLonLatHeightPoint(20 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 5e4);
    axisPoints[1] = new TLcdLonLatHeightPoint(25 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 1e5);
    axisPoints[2] = new TLcdLonLatHeightPoint(25 * aScale.getX() + aOffset.getX(), 5 * aScale.getY() + aOffset.getY(), 1.5e5);
    axisPoints[3] = new TLcdLonLatHeightPoint(30 * aScale.getX() + aOffset.getX(), 5 * aScale.getY() + aOffset.getY(), 2e5);
    axisPoints[4] = new TLcdLonLatHeightPoint(30 * aScale.getX() + aOffset.getX(), 0 * aScale.getY() + aOffset.getY(), 2.5e5);
    return new TLcdLonLatHeightBuffer(axisPoints, 1e5, 1e5, aEllipsoid);
  }

  /**
   * Creates a Lon-Lat Surface object.
   *
   *
   * @param aEllipsoid the ellipsoid for which to create a sphere
   * @param aOffset
   * @param aScale
   * @return a surface
   */
  private static ILcdSurface createLonLatSurface(ILcdEllipsoid aEllipsoid, ILcdPoint aOffset, ILcdPoint aScale) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing(new TLcdLonLatCircle(
                                new TLcdLonLatPoint(0.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY()), 300000, aEllipsoid)
    );
    surface.getInteriorRings().add(new TLcdLonLatCircle(
                                       new TLcdLonLatPoint(1.0 * aScale.getX() + aOffset.getX(), 29.0 * aScale.getY() + aOffset.getY()), 50000, aEllipsoid)
    );
    TLcdLonLatCompositeRing interiorRing = new TLcdLonLatCompositeRing(aEllipsoid);
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0 * aScale.getX() + aOffset.getX(), 26.0 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(-1.0 * aScale.getX() + aOffset.getX(), 27.5 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(1.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY()),
        aEllipsoid));
    interiorRing.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(1.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(2.0 * aScale.getX() + aOffset.getX(), 27.0 * aScale.getY() + aOffset.getY()),
        new TLcdLonLatPoint(1.0 * aScale.getX() + aOffset.getX(), 26.0 * aScale.getY() + aOffset.getY()),
        aEllipsoid));
    surface.getInteriorRings().add(interiorRing);
    return surface;
  }

  /**
   * Creates a XY Surface object.
   *
   * @param aOffset
   * @param aScale
   * @param aViewSize
   * @return a surface
   */
  private static ILcdSurface createXYSurface(ILcdPoint aOffset, ILcdPoint aScale, boolean aViewSize) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing(new TLcdXYCircle(
                                new TLcdXYPoint(0.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY()), aViewSize ? 30 : 300000)
    );
    surface.getInteriorRings().add(new TLcdXYCircle(
                                       new TLcdXYPoint(1.0 * aScale.getX() + aOffset.getX(), 29.0 * aScale.getY() + aOffset.getY()), aViewSize ? 5 : 50000)
    );
    TLcdXYCompositeRing interiorRing = new TLcdXYCompositeRing();
    interiorRing.getCurves().add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(1.0 * aScale.getX() + aOffset.getX(), 26.0 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(-1.0 * aScale.getX() + aOffset.getX(), 27.5 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(1.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY())));
    interiorRing.getCurves().add(new TLcdXYCircularArcBy3Points(
        new TLcdXYPoint(1.0 * aScale.getX() + aOffset.getX(), 28.0 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(2.0 * aScale.getX() + aOffset.getX(), 27.0 * aScale.getY() + aOffset.getY()),
        new TLcdXYPoint(1.0 * aScale.getX() + aOffset.getX(), 26.0 * aScale.getY() + aOffset.getY())));
    surface.getInteriorRings().add(interiorRing);
    return surface;
  }

}
