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
package samples.opengl.dted;

import com.luciad.format.mif.TLcdMIFModelDescriptor;
import com.luciad.format.raster.*;
import com.luciad.format.raster.opengl.TLcdGLGXYViewTerrainLayerFactory;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.*;
import com.luciad.view.opengl.*;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.opengl.common.GLViewSupport;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    if ( aModel == null )
      return null;

    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME ) ) {
      return GLViewSupport.createGridLayer( aModel, aTargetView );
    }
    else if ( modelDescriptor instanceof TLcdRasterModelDescriptor ||
              modelDescriptor instanceof TLcdMultilevelRasterModelDescriptor ) {
      return createDTEDLayer( aModel, aTargetView );
    }
    else {
      return null;
    }
  }

  public boolean isValidModel( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    return (
            typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME ) ||
                    modelDescriptor instanceof TLcdRasterModelDescriptor ||
                    modelDescriptor instanceof TLcdMultilevelRasterModelDescriptor ||
                    modelDescriptor instanceof TLcdMIFModelDescriptor
    );
  }

  private ILcdGLLayer createDTEDLayer( ILcdModel aDTEDModel, ILcdGLView aView ) {
    TLcdGXYViewBufferedImage map_panel = new TLcdGXYViewBufferedImage();
    map_panel.setXYWorldReference( new TLcdGridReference( new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical() ) );
    RasterLayerFactory layer_factory = new RasterLayerFactory();
    ILcdGXYLayer layer = layer_factory.createGXYLayer( aDTEDModel );
    if ( layer != null ) {
      map_panel.addGXYLayer( layer );
      try {
        TLcdXYBounds layer_bounds = new TLcdXYBounds();
        ILcdBounds model_bounds = ( (ILcd2DBoundsIndexedModel) aDTEDModel ).getBounds();
        TLcdGXYContext context = new TLcdGXYContext( map_panel, layer );
        context.getModelXYWorldTransformation().modelBounds2worldSFCT( model_bounds, layer_bounds );
        TLcdGLGXYViewTerrainLayerFactory terrain_layer_factory = new TLcdGLGXYViewTerrainLayerFactory( map_panel );
        terrain_layer_factory.setWorldBounds( layer_bounds );
        return terrain_layer_factory.createLayer( aView );
      }
      catch ( TLcdNoBoundsException e ) {
        // no bounds, return null
      }
    }
    return null;
  }
}
