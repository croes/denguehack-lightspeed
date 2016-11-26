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
package samples.opengl.selectionmodel;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

/**
 * A utility class that can create some hard coded models. These models are used as sample data in the sample
 * application.
 */
class ModelFactory {

  public static final String ELLIPSE_MODEL_DISPLAY_NAME = "Ellipse";
  public static final String ELLIPSE_MODEL_TYPE_NAME = ELLIPSE_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static ILcdModel createEllipseModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", ELLIPSE_MODEL_DISPLAY_NAME, ELLIPSE_MODEL_TYPE_NAME );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatEllipse ellipse1 = createBigEllipse( datum );
    model.addElement( ellipse1, ILcdFireEventMode.NO_EVENT );
    TLcdLonLatEllipse ellipse2 = createSmallEllipse( datum );
    model.addElement( ellipse2, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  private static TLcdLonLatEllipse createSmallEllipse( TLcdGeodeticDatum aDatum ) {
    TLcdLonLatEllipse ellipse2 = new TLcdLonLatEllipse( 10, 10, 400000, 200000, 0, aDatum.getEllipsoid() );
    return ellipse2;
  }

  private static TLcdLonLatEllipse createBigEllipse( TLcdGeodeticDatum aDatum ) {
    TLcdLonLatEllipse ellipse1 = new TLcdLonLatEllipse( 0, 0, 1000000, 500000, 10, aDatum.getEllipsoid() );
    return ellipse1;
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
