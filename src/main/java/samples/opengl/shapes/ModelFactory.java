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
package samples.opengl.shapes;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdSurface;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.TLcdSurface;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightVariableGeoBuffer;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that can create some hard coded models. These models are used
 * as sample data in the sample application.
 */
class ModelFactory {

  public static final String SHAPES_MODEL_DISPLAY_NAME = "Shapes";
  public static final String SHAPES_MODEL_TYPE_NAME = SHAPES_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static ILcdModel createShapesModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
        "",
        SHAPES_MODEL_DISPLAY_NAME,
        SHAPES_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    createShapes( model, datum );

    return model;
  }

  private static void createShapes( TLcdVectorModel aModel, TLcdGeodeticDatum aDatum ) {
    // Create the base shapes
    List shapes = new ArrayList();
    shapes.add( new TLcdLonLatEllipse( 0, 0, 500000, 250000, 50, aDatum.getEllipsoid() ) );
    shapes.add( new TLcdLonLatCircle( -10, 0, 300000, aDatum.getEllipsoid() ) );
    shapes.add( new TLcdLonLatArc( 0, 10, 500000, 250000, 10, 0, 90, aDatum.getEllipsoid() ) );
    shapes.add( new TLcdLonLatArcBand( 10, 0, 250000, 500000, 50, 90, aDatum.getEllipsoid() ) );
    shapes.add( new TLcdLonLatLine( new TLcdLonLatPoint( 7.5, -7.5 ), new TLcdLonLatPoint( 12.5, -12.5 ), aDatum.getEllipsoid() ) );
    ILcd2DEditablePoint[] points = new TLcdLonLatPoint[] {
        new TLcdLonLatPoint( 0, -9 ),
        new TLcdLonLatPoint( 2, -8 ),
        new TLcdLonLatPoint( 1, -10 ),
        new TLcdLonLatPoint( 2, -12 ),
        new TLcdLonLatPoint( 0, -11 ),
        new TLcdLonLatPoint( -2, -12 ),
        new TLcdLonLatPoint( -1, -10 ),
        new TLcdLonLatPoint( -2, -8 )
    };
    shapes.add( new TLcdLonLatPolygon( new TLcd2DEditablePointList( points, false ), aDatum.getEllipsoid() ) );
    points = new TLcdLonLatPoint[] {
        new TLcdLonLatPoint( -10, -9 ),
        new TLcdLonLatPoint( -8, -8 ),
        new TLcdLonLatPoint( -9, -10 ),
        new TLcdLonLatPoint( -8, -12 ),
        new TLcdLonLatPoint( -10, -11 ),
        new TLcdLonLatPoint( -12, -12 ),
        new TLcdLonLatPoint( -11, -10 ),
        new TLcdLonLatPoint( -12, -8 ),
        new TLcdLonLatPoint( -10, -9 )
    };
    shapes.add( new TLcdLonLatPolyline( new TLcd2DEditablePointList( points, false ), aDatum.getEllipsoid() ) );
    points = new TLcdLonLatPoint[] {
        new TLcdLonLatPoint( -12.5, 7.5 ),
        new TLcdLonLatPoint( -11, 10.0 ),
        new TLcdLonLatPoint( -9, 10.0 ),
        new TLcdLonLatPoint( -7.5, 12.5 )
    };
    shapes.add( new TLcdLonLatBuffer( points, 100000, aDatum.getEllipsoid() ) );

    shapes.add( new TLcdLonLatHeightVariableGeoBuffer(
        new TLcd3DEditablePointList( new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 07.5, -5.5, 300000 ),
            new TLcdLonLatHeightPoint( 09.0, -3.0, 300000 ),
            new TLcdLonLatHeightPoint( 11.0, -3.0, 300000 ),
            new TLcdLonLatHeightPoint( 12.5, -0.5, 300000 )
        }, false ),
        new double[] { 300000, 100000, 100000, 100000 },
        new double[] { 200000, 100000, 80000, 200000 },
        aDatum.getEllipsoid()
    ) );

    shapes.add( new TLcdLonLatHeightVariableGeoBuffer(
        new TLcd3DEditablePointList( new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 07.5, -5.5, 300000 ),
            new TLcdLonLatHeightPoint( 09.0, -3.0, 300000 ),
            new TLcdLonLatHeightPoint( 11.0, -3.0, 300000 ),
            new TLcdLonLatHeightPoint( 12.5, -0.5, 300000 )
        }, false ),
        new double[] { 300000, 100000, 100000, 100000 },
        new double[] { 200000, 100000, 80000, 200000 },
        aDatum.getEllipsoid()
    ) );

    TLcdLonLatCompositeRing ring = new TLcdLonLatCompositeRing( aDatum.getEllipsoid() );
    ring.getCurves().add(new TLcdLonLatPolyline(
        new TLcd2DEditablePointList(
            new TLcdLonLatPoint[] {
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
            new TLcdLonLatPoint[] {
                new TLcdLonLatPoint(-9, 21),
                new TLcdLonLatPoint(-6, 18)
            },
            false
        ),
        aDatum.getEllipsoid()
    ));

    shapes.add(createLonLatSurface(aDatum.getEllipsoid()));

    // Add the shapes as flat shapes
    for ( int i = 0; i < shapes.size() ; i++ ) {
      ILcdShape shape = (ILcdShape) shapes.get( i );
      aModel.addElement( shape, ILcdFireEventMode.NO_EVENT );
    }

    // Add the shapes as extruded shapes
    for ( int i = 0; i < shapes.size() ; i++ ) {
      ILcdShape shape = (ILcdShape) shapes.get( i );
      aModel.addElement( new TLcdExtrudedShape( shape, 100000, 500000 ), ILcdFireEventMode.NO_EVENT );
    }

    // Add a dome and a sphere
    aModel.addElement( new LonLatDome  (  0, 20, 300000, aDatum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    aModel.addElement( new LonLatSphere( 10, 10, 300000, aDatum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
  }

  private static ILcdSurface createLonLatSurface( ILcdEllipsoid aEllipsoid ) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing( new TLcdLonLatCircle(
        new TLcdLonLatPoint( 0.0, 28.0 ), 300000, aEllipsoid )
    );
    surface.getInteriorRings().add( new TLcdLonLatCircle(
        new TLcdLonLatPoint( 1.0, 29.0 ), 50000, aEllipsoid )
    );
    TLcdLonLatCompositeRing interiorRing = new TLcdLonLatCompositeRing( aEllipsoid );
    // Rings and surfaces can also contain composite curves.
    TLcdCompositeCurve innerCurve = new TLcdCompositeCurve();
    innerCurve.getCurves().add( new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint( 1.0, 26.0 ),
        new TLcdLonLatPoint( -1.0, 27.5 ),
        new TLcdLonLatPoint( 1.0, 28.0 ),
        aEllipsoid ) );
    innerCurve.getCurves().add( new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint( 1.0, 28.0 ),
        new TLcdLonLatPoint( 2.0, 27.0 ),
        new TLcdLonLatPoint( 1.0, 26.0 ),
        aEllipsoid ) );
    interiorRing.getCurves().add( innerCurve );
    surface.getInteriorRings().add( interiorRing );
    return surface;
  }

  public static ILcdModel createGridModel() {
    TLcdLonLatGrid grid = createLonLatGrid();
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", GRID_MODEL_DISPLAY_NAME, GRID_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );
    model.addElement( grid, ILcdFireEventMode.NO_EVENT );
    return model;
  }

  private static TLcdLonLatGrid createLonLatGrid() {
    TLcdLonLatGrid grid = new TLcdLonLatGrid( 10, 10 );
    return grid;
  }
}
