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

package samples.opengl.labeling;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.shape.shape3D.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

import java.io.IOException;

/**
 * A utility class that can create some hard coded models. These models are used
 * as sample data in the sample application.
 */
class ModelFactory {

  public static final String ELLIPSE_MODEL_DISPLAY_NAME = "Ellipse";
  public static final String ELLIPSE_MODEL_TYPE_NAME = ELLIPSE_MODEL_DISPLAY_NAME;
  public static final String POINT_MODEL_DISPLAY_NAME = "Point";
  public static final String POINT_MODEL_TYPE_NAME = POINT_MODEL_DISPLAY_NAME;
  public static final String POLYLINE_MODEL_DISPLAY_NAME = "Polyline";
  public static final String POLYLINE_MODEL_TYPE_NAME = POLYLINE_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static ILcdModel createEllipseModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            ELLIPSE_MODEL_DISPLAY_NAME,
            ELLIPSE_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatEllipse ellipse1 =
            new TLcdLonLatEllipse( 0, 0, 100000, 50000, 10, datum.getEllipsoid() );
    TLcdExtrudedShape extruded_ellipse1 = new TLcdExtrudedShape( ellipse1, 0, 10000 );
    model.addElement( extruded_ellipse1, ILcdFireEventMode.NO_EVENT );
    TLcdLonLatEllipse ellipse2 =
            new TLcdLonLatEllipse( 2, 2, 40000, 20000, 0, datum.getEllipsoid() );
    TLcdExtrudedShape extruded_ellipse2 = new TLcdExtrudedShape( ellipse2, 0, 8000 );
    model.addElement( extruded_ellipse2, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createPointModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            POINT_MODEL_DISPLAY_NAME,
            POINT_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatHeightPoint p = new TLcdLonLatHeightPoint( 3, 0, 0 );
    model.addElement( p, ILcdFireEventMode.NO_EVENT );

    p = new TLcdLonLatHeightPoint( 3.4, 0.2, 5000 );
    model.addElement( p, ILcdFireEventMode.NO_EVENT );

    p = new TLcdLonLatHeightPoint( 3.4, -0.3, 1000 );
    model.addElement( p, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createPolylineModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            POLYLINE_MODEL_DISPLAY_NAME,
            POLYLINE_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    ILcd3DEditablePoint[] point_array = {
            new TLcdLonLatHeightPoint( -3, 0.1, 100 ),
            new TLcdLonLatHeightPoint( -3.1, 0.5, 1000 ),
            new TLcdLonLatHeightPoint( -2.8, 1, 10000 ),
            new TLcdLonLatHeightPoint( -3, 1.5, 20000 ),
    };

    ILcd3DEditablePointList point_list = new TLcd3DEditablePointList( point_array, false );
    TLcdLonLatHeightPolyline polyline = new TLcdLonLatHeightPolyline( point_list );
    model.addElement( polyline, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createWorldModel() {
    TLcdSHPModelDecoder shp_decoder = new TLcdSHPModelDecoder();
    try {
      return shp_decoder.decode("Data/Shp/World/world.shp");
    }
    catch (IOException e) {
      return null;
    }
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
