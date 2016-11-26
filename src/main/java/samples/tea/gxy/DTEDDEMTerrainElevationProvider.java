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
package samples.tea.gxy;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.tea.ALcdModelBasedTerrainElevationProvider;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * A terrain elevation provider that can retrieve elevation values from DTED and DEM models.
 */
public class DTEDDEMTerrainElevationProvider extends ALcdModelBasedTerrainElevationProvider {

  private ILcdModelModelTransformation fGeo2GeoTransformation = new TLcdGeoReference2GeoReference();

  public double retrieveElevationAt( ILcdPoint aPoint, ILcdGeoReference aPointReference ) {

    // First we check if we have a model for which we don't have to transform anything.
    // If we are lucky the point is inside the bounds of the model.
    boolean containing_raster_found = false;
    for ( int model_index = 0 ; model_index < getModelCount() ; model_index++ ) {
      ILcdModel model = getModel( model_index );
      if ( model.getModelReference().equals( aPointReference ) ) {
        // we assume the model is 2D bounds indexed, this is the case for all models loaded in this sample
        ILcd2DBoundsIndexedModel bounds_indexed_model = (ILcd2DBoundsIndexedModel) model;
        if ( bounds_indexed_model.getBounds().contains2D( aPoint ) ) {
          // we have found a model that contains data at the given point
          // this assumes that the model bounds are strict, which is usually the case for DTED and DEM
          containing_raster_found = true;

          // find the elevation value in the current model.
          double elevation = retrieveElevation( model, aPoint );
          if ( isValidElevation( elevation ) ) {
            return elevation;
          }
        }
      }
    }

    // we haven't found a model with the same model reference, so we'll have to transform the
    // bounds of all models to the given reference until we find one that contains the given point
    // So we need a bounds in the given reference.
    fGeo2GeoTransformation.setDestinationReference( (ILcdModelReference) aPointReference );
    for ( int model_index = 0 ; model_index < getModelCount() ; model_index++ ) {
      ILcdModel model = getModel( model_index );
      fGeo2GeoTransformation.setSourceReference( model.getModelReference() );
      try {
        // we transform the given point to a point in the data models reference
        ILcd3DEditablePoint point = model.getModelReference().makeModelPoint().cloneAs3DEditablePoint();
        fGeo2GeoTransformation.destinationPoint2sourceSFCT( aPoint, point );

        // we assume the model is 2D bounds indexed, this is the case for all models loaded in this sample
        ILcd2DBoundsIndexedModel bounds_indexed_model = (ILcd2DBoundsIndexedModel) model;
        if ( bounds_indexed_model.getBounds().contains2D( point ) ) {
          // we have found a model that contains data at the given point
          // this assumes that the model bounds are strict, which is usually the case for DTED and DEM
          containing_raster_found = true;

          // find the elevation value in the current model.
          double elevation = retrieveElevation( model, point );
          if ( isValidElevation( elevation ) ) {
            return elevation;
          }
        }
      }
      catch ( TLcdOutOfBoundsException e ) {
        // this should not happen since we have checked that the point is inside the bounds of the model.
        // we don't do anything, just progress to the next model.
      }
    }

    return containing_raster_found ? getUnknownElevation() : getOutOfRasterBoundsValue();
  }

  private double retrieveElevation( ILcdModel aModel, ILcdPoint aPoint ) {
    // DEM and DTED models contain only 1 object, an ILcd(Multilevel)Raster
    Object object = aModel.elements().nextElement();
    if ( object instanceof ILcdRaster ) {
      ILcdRaster raster = (ILcdRaster) object;
      int raster_value = raster.retrieveValue( aPoint.getX(), aPoint.getY() );
      if ( isValidRasterValue( raster_value ) ) {
        return raster_value;
      }
    }
    else if ( object instanceof ILcdMultilevelRaster ) {
      ILcdMultilevelRaster multilevel_raster = (ILcdMultilevelRaster) object;
      // find the highest level raster.
      // we assume here that the raster is complete at the level found, i.e.
      // that no tile is missing at any level.
      int level_count = multilevel_raster.getRasterCount();
      // take the raster at the highest level
      int level_index = level_count, raster_value;
      ILcdRaster raster = null;
      while ( raster == null && level_index > 0 ) {
        level_index--;
        raster = multilevel_raster.getRaster( level_index );
        raster_value = raster.retrieveValue( aPoint.getX(), aPoint.getY() );
        if ( isValidRasterValue( raster_value ) ) {
          return raster_value;
        }
      }
    }
    return getUnknownElevation();
  }

}
