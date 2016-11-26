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
package samples.geometry.constructive;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;

public class ShapeModelFactory {

  public ILcdModel createModel() {

    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdGeodeticDatum geodetic_datum = new TLcdGeodeticDatum();
    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeodeticReference( geodetic_datum )
    );
    model.setModelDescriptor( new TLcdModelDescriptor(
        "Layer containing the newly created shapes.", // source name (is used as tooltip text)
        "Shapes", // type name
        "Shapes"  // display name
    ) );

    model.addElement( createLonLatPolygon( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    model.addElement( createLonLatPolygon2( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    model.addElement( createLonLatPolyline( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    return model;
  }

  private Object createLonLatPolygon( ILcdEllipsoid
    aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdLonLatPoint( -74.0, 35.0 ),
        new TLcdLonLatPoint( -74.2, 38.0 ),
        new TLcdLonLatPoint( -71.3, 39.0 ),
        new TLcdLonLatPoint( -69.0, 38.2 ),
        new TLcdLonLatPoint( -70.3, 35.1 ),
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolygon( point_list_2d, aEllipsoid );
  }

  private Object createLonLatPolygon2( ILcdEllipsoid
    aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdLonLatPoint( -75.99, 33.54 ),
        new TLcdLonLatPoint( -74.85, 37.08 ),
        new TLcdLonLatPoint( -74.64, 33.44 ),
        new TLcdLonLatPoint( -70.24, 34.47 ),
        new TLcdLonLatPoint( -74.39, 32.04 ),
        new TLcdLonLatPoint( -72.82, 28.90 ),
        new TLcdLonLatPoint( -75.46, 31.51 ),
        new TLcdLonLatPoint( -78.43, 29.19 ),
        new TLcdLonLatPoint( -76.46, 32.37 ),
        new TLcdLonLatPoint( -79.57, 34.79 ),
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolygon( point_list_2d, aEllipsoid );
  }

  private Object createLonLatPolyline( ILcdEllipsoid
    aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdLonLatPoint( -72.88 ,31.45 ),
        new TLcdLonLatPoint( -66.86 ,33.35 ),
        new TLcdLonLatPoint( -68.79 ,35.74 ),
        new TLcdLonLatPoint( -65.77 ,38.29 ),
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolyline( point_list_2d, aEllipsoid );
  }
}
