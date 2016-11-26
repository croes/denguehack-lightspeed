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
package samples.opengl.multipainter;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

/**
 * A utility class that can create some hard coded models. These models are used
 * as sample data in the sample application.
 */
class ModelFactory {

  public static final String MIXED_MODEL_DISPLAY_NAME = "Mixed";
  public static final String MIXED_MODEL_TYPE_NAME = MIXED_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static ILcdModel createEllipseModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            MIXED_MODEL_DISPLAY_NAME,
            MIXED_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatEllipse ellipse =
            new TLcdLonLatEllipse( 0, 0, 1000000, 500000, 10, datum.getEllipsoid() );
    model.addElement( ellipse, ILcdFireEventMode.NO_EVENT );
    ILcd2DEditablePoint[] points = new TLcdLonLatPoint[] {
            new TLcdLonLatPoint( -10, 10 ),
            new TLcdLonLatPoint( -15, 12 ),
            new TLcdLonLatPoint( -20, 10 ),
            new TLcdLonLatPoint( -18, 15 ),
            new TLcdLonLatPoint( -20, 20 ),
            new TLcdLonLatPoint( -15, 18 ),
            new TLcdLonLatPoint( -10, 20 ),
            new TLcdLonLatPoint( -12, 15 )
    };
    TLcdLonLatPolygon polygon =
            new TLcdLonLatPolygon(
                    new TLcd2DEditablePointList( points, false )
                    , datum.getEllipsoid() );
    model.addElement( polygon, ILcdFireEventMode.NO_EVENT );

    return model;
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
