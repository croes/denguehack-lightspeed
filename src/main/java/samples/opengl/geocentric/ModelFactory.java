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
package samples.opengl.geocentric;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

import java.io.IOException;

/**
 * A factory that creates models for the geocentric sample application
 */
class ModelFactory {

  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = "Grid";

  public static final String POINT3D_MODEL_DISPLAY_NAME = "Point3D";
  public static final String POINT3D_MODEL_TYPE_NAME = "Point3D";

  public static final String POINT2D_MODEL_DISPLAY_NAME = "Point2D";
  public static final String POINT2D_MODEL_TYPE_NAME = "Point2D";

  public static final String POLYGON_MODEL_DISPLAY_NAME = "Polygon";
  public static final String POLYGON_MODEL_TYPE_NAME = "Polygon";

  public static final String ELLIPSE_MODEL_DISPLAY_NAME = "Ellipse";
  public static final String ELLIPSE_MODEL_TYPE_NAME = "Ellipse";

  public static final String ARCBAND_MODEL_DISPLAY_NAME = "ArcBand";
  public static final String ARCBAND_MODEL_TYPE_NAME = "ArcBand";

  public static final String CIRCLE_MODEL_DISPLAY_NAME = "Circle";
  public static final String CIRCLE_MODEL_TYPE_NAME = "Circle";

  public static final String LL_BUFFER_MODEL_DISPLAY_NAME = "LLBuffer";
  public static final String LL_BUFFER_MODEL_TYPE_NAME = "LLBuffer";

  public static final String LLH_BUFFER_MODEL_DISPLAY_NAME = "LLHBuffer";
  public static final String LLH_BUFFER_MODEL_TYPE_NAME = "LLHBuffer";

  public static final String COMPLEXPOLY_MODEL_DISPLAY_NAME = "ComplexPolygon";
  public static final String COMPLEXPOLY_MODEL_TYPE_NAME = "ComplexPolygon";

  public static final String DOME_MODEL_DISPLAY_NAME = "Dome";
  public static final String DOME_MODEL_TYPE_NAME = "Dome";

  public static final String SPHERE_MODEL_DISPLAY_NAME = "Sphere";
  public static final String SPHERE_MODEL_TYPE_NAME = "Sphere";
  
  public static final String VARIABLE_GEO_BUFFER_MODEL_DISPLAY_NAME = "VariableGeoBuffer";
  public static final String VARIABLE_GEO_BUFFER_MODEL_TYPE_NAME = "VariableGeoBuffer";

  private ModelFactory() {
  }

  public static ILcdModel createGridModel() {
    TLcdLonLatGrid grid = new TLcdLonLatGrid( 10, 10 );
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", GRID_MODEL_DISPLAY_NAME, GRID_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );
    model.addElement( grid, ILcdFireEventMode.NO_EVENT );
    return model;
  }

  public static ILcdModel createPolygonModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", POLYGON_MODEL_DISPLAY_NAME, POLYGON_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    model.addElement( createPolygon(), ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private static TLcdLonLatPolygon createPolygon() {
    double l = 9;
    double w = 2;
    TLcdLonLatPoint[] points = new TLcdLonLatPoint[] {
        new TLcdLonLatPoint(-l, -w),
        new TLcdLonLatPoint(-l, w),
        new TLcdLonLatPoint(-w, w),
        new TLcdLonLatPoint(-w, l),
        new TLcdLonLatPoint(w, l),
        new TLcdLonLatPoint(w, w),
        new TLcdLonLatPoint(l, w),
        new TLcdLonLatPoint(l, -w),
        new TLcdLonLatPoint(w, -w),
        new TLcdLonLatPoint(w, -l),
        new TLcdLonLatPoint(-w, -l),
        new TLcdLonLatPoint(-w, -w),
    };
    return new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, false));
  }

  public static ILcdModel createEllipseModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", ELLIPSE_MODEL_DISPLAY_NAME, ELLIPSE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatEllipse ellipse = new TLcdLonLatEllipse(15, 18, 1500000, 700000, 50, datum.getEllipsoid());
    model.addElement(ellipse, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createArcBandModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", ARCBAND_MODEL_DISPLAY_NAME, ARCBAND_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatArcBand arcband = new TLcdLonLatArcBand(-25, 25, 750000, 1000000, 123, 234, datum.getEllipsoid());
    model.addElement(arcband, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createCircleModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", CIRCLE_MODEL_DISPLAY_NAME, CIRCLE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle circle = new TLcdLonLatCircle(12, -12, 1000000, datum.getEllipsoid());
    model.addElement(circle, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createDomeModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", DOME_MODEL_DISPLAY_NAME, DOME_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle dome = new TLcdLonLatCircle(-10, -10, 500000, datum.getEllipsoid());
    model.addElement(dome, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createSphereModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", SPHERE_MODEL_DISPLAY_NAME, SPHERE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle sphere = new TLcdLonLatCircle(-7, 7, 200000, datum.getEllipsoid());
    model.addElement(sphere, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createLonLatBufferModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", LL_BUFFER_MODEL_DISPLAY_NAME, LL_BUFFER_MODEL_TYPE_NAME);
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatPoint[] points = new TLcdLonLatPoint[] {
        new TLcdLonLatPoint(-55, -3),
        new TLcdLonLatPoint(-50, 0),
        new TLcdLonLatPoint(-45, -1),
        new TLcdLonLatPoint(-40, 6),
        new TLcdLonLatPoint(-35, -2),
        new TLcdLonLatPoint(-30, -1),
        new TLcdLonLatPoint(-25, 4),
        new TLcdLonLatPoint(-20, 0),
        new TLcdLonLatPoint(-15, -3),
    };

    TLcdLonLatBuffer buffer = new TLcdLonLatBuffer(points, 200000, datum.getEllipsoid());
    model.addElement(buffer, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createLonLatHeightBufferModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", LLH_BUFFER_MODEL_DISPLAY_NAME, LLH_BUFFER_MODEL_TYPE_NAME);
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatHeightPoint[] points = new TLcdLonLatHeightPoint[] {
        new TLcdLonLatHeightPoint(21, -1, 40000),
        new TLcdLonLatHeightPoint(26, -4, 60000),
        new TLcdLonLatHeightPoint(31, 0, 80000),
        new TLcdLonLatHeightPoint(36, 3, 100000),
        new TLcdLonLatHeightPoint(41, -2, 150000),
        new TLcdLonLatHeightPoint(46, 1, 200000),
    };

    TLcdLonLatHeightBuffer buffer = new TLcdLonLatHeightBuffer(points, 150000, 80000);
    model.addElement(buffer, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createVariableGeoBufferModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", VARIABLE_GEO_BUFFER_MODEL_DISPLAY_NAME, VARIABLE_GEO_BUFFER_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    ILcd3DEditablePoint[] points = new ILcd3DEditablePoint[] {
        new TLcdLonLatHeightPoint(51, 9, 40000),
        new TLcdLonLatHeightPoint(46, 6, 80000),
        new TLcdLonLatHeightPoint(41, 10, 120000),
        new TLcdLonLatHeightPoint(36, 11, 200000),
        new TLcdLonLatHeightPoint(31, 9, 300000),
        new TLcdLonLatHeightPoint(26, 11, 400000),
    };
    TLcdLonLatHeightVariableGeoBuffer buffer = new TLcdLonLatHeightVariableGeoBuffer(
        new TLcd3DEditablePointList( points, false ),
        new double[] { 100000, 150000, 200000, 200000, 300000, 400000 },
        new double[] { 10000, 20000, 40000, 40000, 40000, 40000},
        new double[] { 10000, 20000, 40000, 240000, 160000, 320000 },
        datum.getEllipsoid()
    );
    model.addElement( buffer, ILcdModel.NO_EVENT );
    return model;
  }

  public static ILcdModel createComplexPolyModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", COMPLEXPOLY_MODEL_DISPLAY_NAME, COMPLEXPOLY_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(datum);
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatPolygon polys[] = new TLcdLonLatPolygon[7];
    double[] radii = new double[] { 8, 2.5, 1.5, 2, 2.5, 1.5, 2 };
    for (int i = 0; i < 7; i++) {
      TLcdLonLatPoint[] points = new TLcdLonLatPoint[36];
      double dx = 0, dy = 0;
      if (i > 0) {
        double da = Math.toRadians(i * 60);
        dx = radii[0] * Math.cos(da);
        dy = radii[0] * Math.sin(da);
      }
      dx -= 30;
      dy -= 20;
      for (int p = 0; p < 36; p++) {
        double alpha = Math.toRadians(p*10);
        double r = radii[i];
        double x = r * Math.cos(alpha);
        double y = r * Math.sin(alpha);
        points[p] = new TLcdLonLatPoint(dx + x, dy + y);
      }

      polys[i] = new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, true));
    }

    TLcdComplexPolygon complexpoly = new TLcdComplexPolygon(polys);
    model.addElement(complexpoly, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createSHPModel(String aSource) {
    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    try {
      return decoder.decode(aSource);
    }
    catch (IOException e) {
      return null;
    }
  }

  public static ILcdModel createPoint3DModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        "",
        POINT3D_MODEL_DISPLAY_NAME,
        POINT3D_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    OrientedLonLatHeightPoint p;

    for (int i = 0; i < 10; i++) {
      double alpha = Math.toRadians(i*36.0);
      p = new OrientedLonLatHeightPoint(
          -5 + 3*Math.cos(alpha),
          -25 + 3*Math.sin(alpha),
          15000 + 3000*Math.sin(alpha*2),
          -Math.toDegrees(alpha),
          20*Math.sin(alpha*2),
          0
      );
      model.addElement(p, ILcdFireEventMode.NO_EVENT);
    }

    return model;
  }

  public static ILcdModel createPoint2DModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        "",
        POINT2D_MODEL_DISPLAY_NAME,
        POINT2D_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    TLcdLonLatHeightPoint p;

    double x0 = 11.75;
    double y0 = 46.75;
    double r = 0.4;
    for (int i = 0; i < 10; i++) {
      double alpha = Math.toRadians(i*36.0);
      p = new TLcdLonLatHeightPoint(
          x0 + r*Math.cos(alpha),
          y0 + r*Math.sin(alpha),
          0
      );
      model.addElement(p, ILcdFireEventMode.NO_EVENT);
    }

    return model;
  }
}
