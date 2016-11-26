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
package samples.tea.gxy.los;

import com.luciad.model.*;
import com.luciad.reference.*;
import com.luciad.shape.*;
import com.luciad.shape.shape3D.*;
import com.luciad.transformation.*;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;

import java.util.*;

/**
 * A class that finds rasters or multilevel rasters beneath a point in a given reference, based on
 * raster layers as input.
 */
class RasterLayerRasterProvider {

  private ILcdGXYLayer[] fRasterLayers;
  private ILcdModelModelTransformation fGeo2Geo = new TLcdGeoReference2GeoReference();

  RasterLayerRasterProvider( ILcdGXYLayer[] aRasterLayers ) {
    fRasterLayers = aRasterLayers;
  }

  /**
   * @param aPoint
   * @param aPointReference
   * @return a vector of referenced objects. The object is an ILcdRaster or an ILcdMultilevelRaster
   */
  Vector retrieveRasters( ILcdPoint aPoint, ILcdGeoReference aPointReference ) {

    // first find the rasters that contain this point.
    Vector rasters = new Vector();
    Vector layers = new Vector();

    fGeo2Geo.setSourceReference( (ILcdModelReference) aPointReference );

    for ( int layer_index = 0; layer_index < fRasterLayers.length ; layer_index++ ) {
      ILcdGXYLayer raster_layer = fRasterLayers[ layer_index ];
      if ( raster_layer == null ) continue;

      // we assume it is a 2D bounds indexed model.
      ILcd2DBoundsIndexedModel raster_model = (ILcd2DBoundsIndexedModel) raster_layer.getModel();

      // first check whether the point is inside the models bounds
      ILcdBounds raster_model_bounds = raster_model.getBounds();
      ILcdModelReference raster_model_reference = raster_model.getModelReference();
      fGeo2Geo.setDestinationReference( raster_model_reference );
      ILcd3DEditablePoint point_raster_reference;
      if ( raster_model_reference instanceof ILcdGeodeticReference ) {
        point_raster_reference = new TLcdLonLatHeightPoint();
      } else {
        point_raster_reference = new TLcdXYZPoint();
      }
      try {
        fGeo2Geo.sourcePoint2destinationSFCT( aPoint, point_raster_reference );
        if ( raster_model_bounds.contains2D( point_raster_reference ) ) {
          // check if one of the rasters contains it.
          Enumeration elements = raster_model.elements();
          while ( elements.hasMoreElements() ) {
            // objects in the model are bounded, since we are working in a 2D bounds indexed model.
            ILcdBounded bounded = (ILcdBounded) elements.nextElement();
            if ( bounded.getBounds().contains2D( point_raster_reference ) ) {
              rasters.add( bounded );
              layers.add( raster_layer );
            }
          }
        }
      }
      catch ( TLcdOutOfBoundsException e ) {
        // we do nothing
        // we could not transform it the raster models reference, so it will surely not be
        // inside the bounds of the model
      }
    }

    Vector result = new Vector( rasters.size() );
    for ( int raster_index = 0; raster_index < rasters.size() ; raster_index++ ) {
      // can be a raster or a multilevel raster.
      Object       raster       = rasters.elementAt( raster_index );
      ILcdGXYLayer raster_layer = (ILcdGXYLayer) layers.elementAt( raster_index );
      ILcdGeoReference geo_reference = (ILcdGeoReference) raster_layer.getModel().getModelReference();
      result.add( new ReferencedObject( raster, geo_reference, raster_layer.getModel().getModelDescriptor().getTypeName(), raster_layer.getLabel() ) );
    }

    return result;
  }

  // a utility to class to combine an object with its reference
  public static class ReferencedObject {

    private Object fObject;
    private ILcdGeoReference fGeoReference;
    private String fLayerName;
    private String fType;

    public ReferencedObject( Object aObject,
                             ILcdGeoReference aGeoReference,
                             String aType,
                             String aLayerName ) {
      fObject = aObject;
      fGeoReference = aGeoReference;
      fType = aType;
      fLayerName = aLayerName;
    }

    public Object getObject() {
      return fObject;
    }

    public ILcdGeoReference getGeoReference() {
      return fGeoReference;
    }

    public String getType() {
      return fType;
    }

    public String getLayerName() {
      return fLayerName;
    }
  }
}
