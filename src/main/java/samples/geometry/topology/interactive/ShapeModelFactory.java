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
package samples.geometry.topology.interactive;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatArc;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightVariableGeoBuffer;
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
    model.addElement( createLonLatArc( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    model.addElement( createLonLatEllipse( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    model.addElement( createVariableGeoBuffer( geodetic_datum.getEllipsoid() ), ILcdFireEventMode.NO_EVENT );
    return model;
  }

  private Object createLonLatPolygon( ILcdEllipsoid aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
            new TLcdLonLatPoint( -77.0, 32.0 ),
            new TLcdLonLatPoint( -78.2, 35.0 ),
            new TLcdLonLatPoint( -74.3, 36.0 ),
            new TLcdLonLatPoint( -72.0, 35.2 ),
            new TLcdLonLatPoint( -73.3, 32.1 ),
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
            new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolygon( point_list_2d, aEllipsoid );
  }

  private Object createLonLatPolygon2( ILcdEllipsoid aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
            new TLcdLonLatPoint( -73.0, 30.0 ),
            new TLcdLonLatPoint( -74.2, 35.0 ),
            new TLcdLonLatPoint( -67.0, 34.0 )
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
            new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolygon( point_list_2d, aEllipsoid );
  }

  private Object createLonLatPolyline( ILcdEllipsoid aEllipsoid ) {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
            new TLcdLonLatPoint( -79.0, 36.0 ),
            new TLcdLonLatPoint( -78.2, 35.0 ),
            new TLcdLonLatPoint( -74, 37.0 ),
            new TLcdLonLatPoint( -72.0, 35.2 ),
            new TLcdLonLatPoint( -66, 36 ),
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
            new TLcd2DEditablePointList( point_2d_array, false );
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdLonLatPolyline( point_list_2d, aEllipsoid );
  }

   private Object createLonLatArc( ILcdEllipsoid aEllipsoid ) {
    return new TLcdLonLatArc( -73, 32, 3e5, 1.5e5, 0, 10, 100, aEllipsoid);
  }

  private Object createLonLatEllipse( ILcdEllipsoid aEllipsoid ) {
    return new TLcdLonLatEllipse( -67.0, 34.0, 3e5, 8e4, 90, aEllipsoid );
  }

  private Object createVariableGeoBuffer( ILcdEllipsoid aEllipsoid ) {
    ILcd3DEditablePoint[] point_3d_array = {
        new TLcdLonLatHeightPoint( -82.0, 31.0, 0 ),
        new TLcdLonLatHeightPoint( -78.2, 30.0, 0 ),
        new TLcdLonLatHeightPoint( -74.0, 32.0, 0 ),
        new TLcdLonLatHeightPoint( -72.0, 30.2, 0 ),
        new TLcdLonLatHeightPoint( -66.0, 31.0, 0 ),
    };
    ILcd3DEditablePointList pointList = new TLcd3DEditablePointList( point_3d_array, false );
    TLcdLonLatHeightVariableGeoBuffer buffer = new TLcdLonLatHeightVariableGeoBuffer(
        pointList,
        50000,
        100,
        aEllipsoid
    );
    buffer.setWidth( 1, 100000 );
    buffer.setWidth( 2, 25000 );
    buffer.setWidth( 4, 150000 );
    return buffer;
  }

}
