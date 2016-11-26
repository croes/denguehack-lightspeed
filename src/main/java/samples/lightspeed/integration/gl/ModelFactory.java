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
package samples.lightspeed.integration.gl;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatArcBand;
import com.luciad.shape.shape2D.TLcdLonLatBuffer;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape3D.TLcdLonLatHeightBuffer;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;

import java.io.IOException;

class ModelFactory {

  public static final String ELLIPSE_MODEL_DISPLAY_NAME = "Ellipse";
  public static final String ELLIPSE_MODEL_TYPE_NAME = ELLIPSE_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static final String POINT3D_MODEL_DISPLAY_NAME = "Point3D";
  public static final String POINT3D_MODEL_TYPE_NAME = "Point3D";

  public static final String POINT2D_MODEL_DISPLAY_NAME = "Point2D";
  public static final String POINT2D_MODEL_TYPE_NAME = "Point2D";

  public static final String SHP_MODEL_DISPLAY_NAME = "SHP";
  public static final String SHP_MODEL_TYPE_NAME = "SHP";

  public static final String ARCBAND_MODEL_DISPLAY_NAME = "ArcBand";
  public static final String ARCBAND_MODEL_TYPE_NAME = "ArcBand";

  public static final String COMPLEXPOLY_MODEL_DISPLAY_NAME = "ComplexPolygon";
  public static final String COMPLEXPOLY_MODEL_TYPE_NAME = "ComplexPolygon";

  public static final String LLH_BUFFER_MODEL_DISPLAY_NAME = "LLHBuffer";
  public static final String LLH_BUFFER_MODEL_TYPE_NAME = "LLHBuffer";

  public static final String LL_BUFFER_MODEL_DISPLAY_NAME = "LLBuffer";
  public static final String LL_BUFFER_MODEL_TYPE_NAME = "LLBuffer";

  public static final String SPHERE_MODEL_DISPLAY_NAME = "Sphere";
  public static final String SPHERE_MODEL_TYPE_NAME = "Sphere";

  public static final String DOME_MODEL_DISPLAY_NAME = "Dome";
  public static final String DOME_MODEL_TYPE_NAME = "Dome";

  public static final String CIRCLE_MODEL_DISPLAY_NAME = "Circle";
  public static final String CIRCLE_MODEL_TYPE_NAME = "Circle";

  public static final String POLYGON_MODEL_DISPLAY_NAME = "Polygon";
  public static final String POLYGON_MODEL_TYPE_NAME = "Polygon";


  private ILcdModel createEllipseModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        aSourceName,
        ELLIPSE_MODEL_DISPLAY_NAME,
        ELLIPSE_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatEllipse ellipse1 =
        new TLcdLonLatEllipse( 0, 30, 1000000, 500000, 10, datum.getEllipsoid() );
    model.addElement( ellipse1, ILcdFireEventMode.NO_EVENT );
    TLcdLonLatEllipse ellipse2 =
        new TLcdLonLatEllipse( 10, 10, 400000, 200000, 0, datum.getEllipsoid() );
    model.addElement( ellipse2, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createPoint3DModel( String aSourcename ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        aSourcename,
        POINT3D_MODEL_DISPLAY_NAME,
        POINT3D_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    OrientedLonLatHeightPoint p;

    for ( int i = 0; i < 10; i++ ) {
      double alpha = Math.toRadians( i * 36.0 );
      p = new OrientedLonLatHeightPoint(
          -5 + 3 * Math.cos( alpha ),
          -25 + 3 * Math.sin( alpha ),
          15000 + 3000 * Math.sin( alpha * 2 ),
          -Math.toDegrees( alpha ),
          20 * Math.sin( alpha * 2 ),
          0
      );
      model.addElement( p, ILcdFireEventMode.NO_EVENT );
    }
    return model;
  }

  private ILcdModel createPoint2DModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        aSourceName,
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
    for ( int i = 0; i < 10; i++ ) {
      double alpha = Math.toRadians( i * 36.0 );
      p = new TLcdLonLatHeightPoint(
          x0 + r * Math.cos( alpha ),
          y0 + r * Math.sin( alpha ),
          0
      );
      model.addElement( p, ILcdFireEventMode.NO_EVENT );
    }

    return model;
  }

  private ILcdModel createSHPModel( String aSource ) {
    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    try {
      return decoder.decode( aSource );
    } catch ( IOException e ) {
      return null;
    }
  }

  private ILcdModel createArcBandModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, ARCBAND_MODEL_DISPLAY_NAME, ARCBAND_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatArcBand arcband = new TLcdLonLatArcBand( -25, 25, 750000, 1000000, 123, 234, datum.getEllipsoid() );
    model.addElement( arcband, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createComplexPolyModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, COMPLEXPOLY_MODEL_DISPLAY_NAME, COMPLEXPOLY_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatPolygon polys[] = new TLcdLonLatPolygon[ 7 ];
    double[] radii = new double[]{8, 2.5, 1.5, 2, 2.5, 1.5, 2};
    for ( int i = 0; i < 7; i++ ) {
      TLcdLonLatPoint[] points = new TLcdLonLatPoint[ 36 ];
      double dx = 0, dy = 0;
      if ( i > 0 ) {
        double da = Math.toRadians( i * 60 );
        dx = radii[ 0 ] * Math.cos( da );
        dy = radii[ 0 ] * Math.sin( da );
      }
      dx -= 30;
      dy -= 20;
      for ( int p = 0; p < 36; p++ ) {
        double alpha = Math.toRadians( p * 10 );
        double r = radii[ i ];
        double x = r * Math.cos( alpha );
        double y = r * Math.sin( alpha );
        points[ p ] = new TLcdLonLatPoint( dx + x, dy + y );
      }

      polys[ i ] = new TLcdLonLatPolygon( new TLcd2DEditablePointList( points, true ) );
    }

    TLcdComplexPolygon complexpoly = new TLcdComplexPolygon( polys );
    model.addElement( complexpoly, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createLonLatHeightBufferModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, LLH_BUFFER_MODEL_DISPLAY_NAME, LLH_BUFFER_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatHeightPoint[] points = new TLcdLonLatHeightPoint[]{
        new TLcdLonLatHeightPoint( 21, -1, 40000 ),
        new TLcdLonLatHeightPoint( 26, -4, 60000 ),
        new TLcdLonLatHeightPoint( 31, 0, 80000 ),
        new TLcdLonLatHeightPoint( 36, 3, 100000 ),
        new TLcdLonLatHeightPoint( 41, -2, 150000 ),
        new TLcdLonLatHeightPoint( 46, 1, 200000 ),
    };

    TLcdLonLatHeightBuffer buffer = new TLcdLonLatHeightBuffer( points, 150000, 80000 );
    model.addElement( buffer, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createLonLatBufferModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, LL_BUFFER_MODEL_DISPLAY_NAME, LL_BUFFER_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatPoint[] points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint( -55, -3 ),
        new TLcdLonLatPoint( -50, 0 ),
        new TLcdLonLatPoint( -45, -1 ),
        new TLcdLonLatPoint( -40, 6 ),
        new TLcdLonLatPoint( -35, -2 ),
        new TLcdLonLatPoint( -30, -1 ),
        new TLcdLonLatPoint( -25, 4 ),
        new TLcdLonLatPoint( -20, 0 ),
        new TLcdLonLatPoint( -15, -3 ),
    };

    TLcdLonLatBuffer buffer = new TLcdLonLatBuffer( points, 200000, datum.getEllipsoid() );
    model.addElement( buffer, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createSphereModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, SPHERE_MODEL_DISPLAY_NAME, SPHERE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle sphere = new TLcdLonLatCircle( -7, 7, 200000, datum.getEllipsoid() );
    model.addElement( sphere, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createDomeModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, DOME_MODEL_DISPLAY_NAME, DOME_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle dome = new TLcdLonLatCircle( -10, -10, 500000, datum.getEllipsoid() );
    model.addElement( dome, ILcdFireEventMode.NO_EVENT );

    return model;
  }


  private ILcdModel createCircleModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, CIRCLE_MODEL_DISPLAY_NAME, CIRCLE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference reference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    TLcdLonLatCircle circle = new TLcdLonLatCircle( 12, -12, 1000000, datum.getEllipsoid() );
    model.addElement( circle, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private ILcdModel createPolygonModel( String aSourceName ) {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( aSourceName, POLYGON_MODEL_DISPLAY_NAME, POLYGON_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );

    model.addElement( createPolygon(), ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private TLcdLonLatPolygon createPolygon() {
    double l = 9;
    double w = 2;
    TLcdLonLatPoint[] points = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint( -l, -w ),
        new TLcdLonLatPoint( -l, w ),
        new TLcdLonLatPoint( -w, w ),
        new TLcdLonLatPoint( -w, l ),
        new TLcdLonLatPoint( w, l ),
        new TLcdLonLatPoint( w, w ),
        new TLcdLonLatPoint( l, w ),
        new TLcdLonLatPoint( l, -w ),
        new TLcdLonLatPoint( w, -w ),
        new TLcdLonLatPoint( w, -l ),
        new TLcdLonLatPoint( -w, -l ),
        new TLcdLonLatPoint( -w, -w ),
    };
    return new TLcdLonLatPolygon( new TLcd2DEditablePointList( points, false ) );
  }

  public ILcdModel createModel( String aType, String aSourceName ) {
    if ( aType.equals( ELLIPSE_MODEL_TYPE_NAME ) )
      return createEllipseModel( aSourceName );
    else if ( aType.equals( POINT2D_MODEL_TYPE_NAME ) )
      return createPoint2DModel( aSourceName );
    else if ( aType.equals( POINT3D_MODEL_TYPE_NAME ) )
      return createPoint3DModel( aSourceName );
    else if ( aType.equals( SHP_MODEL_TYPE_NAME ) )
      return createSHPModel( aSourceName );
    else if ( aType.equals( ARCBAND_MODEL_TYPE_NAME ) )
      return createArcBandModel( aSourceName );
    else if ( aType.equals( COMPLEXPOLY_MODEL_TYPE_NAME ) )
      return createComplexPolyModel( aSourceName );
    else if ( aType.equals( LLH_BUFFER_MODEL_TYPE_NAME ) )
      return createLonLatHeightBufferModel( aSourceName );
    else if ( aType.equals( LL_BUFFER_MODEL_TYPE_NAME ) )
      return createLonLatBufferModel( aSourceName );
    else if ( aType.equals( SPHERE_MODEL_TYPE_NAME ) )
      return createSphereModel( aSourceName );
    else if ( aType.equals( DOME_MODEL_TYPE_NAME ) )
      return createDomeModel( aSourceName );
    else if ( aType.equals( CIRCLE_MODEL_TYPE_NAME ) )
      return createCircleModel( aSourceName );
    else if ( aType.equals( POLYGON_MODEL_TYPE_NAME ) )
      return createPolygonModel( aSourceName );
    else
      return null;
  }

}
