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

import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.*;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.transformation.*;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * A utility class which returns pixel density in a given reference. The density
 * returned is equal to the density of the underlying raster.
 */
class RasterPixelDensityProvider {

  private ILcdModelModelTransformation fGeo2Geo = new TLcdGeoReference2GeoReference();

  double retrievePixelDensity( ILcdPoint aPoint,
                               ILcdGeoReference aPointReference,
                               ILcdRaster aRaster,
                               ILcdGeoReference aRasterReference,
                               ILcdGeoReference aTargetReference ) {

    double max_pixel_density = 0;


    double pixel_density = aRaster.getPixelDensity();

    if ( aTargetReference instanceof ILcdGeodeticReference ) {
      if ( aRasterReference instanceof ILcdGeodeticReference ) {
        max_pixel_density = Math.max( max_pixel_density, pixel_density );
      } else {
        // we assume grid reference
        // we have to find out, how much meters one 10th of a degree to the north and
        // one 10th of a degree to the east is.
        ILcdGeodeticReference geodetic_target_reference = (ILcdGeodeticReference) aTargetReference;
        ILcdGridReference grid_raster_reference = (ILcdGridReference) aRasterReference;
        TLcdLonLatPoint point = new TLcdLonLatPoint( aPoint );
        point.translate2D( 0.1, 0.0 );
        double distance_north = geodetic_target_reference.getGeodeticDatum().getEllipsoid().geodesicDistance( aPoint, point );
        point.translate2D( -0.1, 0.1 );
        double distance_east = geodetic_target_reference.getGeodeticDatum().getEllipsoid().geodesicDistance( aPoint, point );
        // number pixels to cover this area in the raster reference
        double pixels_in_area = pixel_density * distance_north * grid_raster_reference.getUnitOfMeasure() * distance_east * grid_raster_reference.getUnitOfMeasure();
        // the area is 0.01 degrees square, so the resulting pixel density is 100 times bigger
        double pixel_density_target = pixels_in_area / ( 0.01 );
        max_pixel_density = Math.max( pixel_density_target, max_pixel_density );
      }
    } else {
      ILcdGridReference target_grid_reference = (ILcdGridReference) aTargetReference;
      if ( aRasterReference instanceof ILcdGeodeticReference ) {
        // we have to find out, how much meters one 10th of a degree to the north and
        // one 10th of a degree to the east is.
        ILcdGridReference grid_target_reference = (ILcdGridReference) aTargetReference;
        ILcdGeodeticReference geodetic_raster_reference = (ILcdGeodeticReference) aRasterReference;
        // transform the center point to the rasters reference
        TLcdLonLatHeightPoint point = new TLcdLonLatHeightPoint();
        fGeo2Geo.setSourceReference( (ILcdModelReference) aPointReference );
        fGeo2Geo.setDestinationReference( (ILcdModelReference) geodetic_raster_reference );
        try {
          fGeo2Geo.sourcePoint2destinationSFCT( aPoint, point );
          ILcdPoint point_orig = (ILcdPoint) point.clone();
          point.translate2D( 0.1, 0.0 );
          double distance_north = geodetic_raster_reference.getGeodeticDatum().getEllipsoid().geodesicDistance( point_orig, point );
          point.translate2D( -0.1, 0.1 );
          double distance_east = geodetic_raster_reference.getGeodeticDatum().getEllipsoid().geodesicDistance( point_orig, point );
          // approximate number of pixels to cover this area in the raster reference
          double pixels_in_area = pixel_density / 100;
          double uom_target = grid_target_reference.getUnitOfMeasure();
          double pixel_density_target = pixels_in_area / ( distance_east * distance_north * uom_target * uom_target );
          max_pixel_density = Math.max( pixel_density_target, max_pixel_density );
        }
        catch ( TLcdOutOfBoundsException e ) {
          // we don't do anything. Normally this should not happen anyhow
        }
      } else {
        // we assume grid reference
        ILcdGridReference grid_raster_reference = (ILcdGridReference) aRasterReference;
        double uom_target = target_grid_reference.getUnitOfMeasure();
        double uom_raster = grid_raster_reference.getUnitOfMeasure();
        double pixel_density_target = pixel_density * ( uom_raster * uom_raster ) / ( uom_target * uom_target );
        max_pixel_density = Math.max( pixel_density_target, max_pixel_density );
      }
    }

    return max_pixel_density;
  }
}
