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
package samples.opengl.terrain.preprocessor;

import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdWarpMultilevelRasterPainter;
import com.luciad.format.raster.TLcdWarpRasterPainter;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import samples.gxy.decoder.MapSupport;

/**
 * A simple ILcdGXYLayerFactory for raster data.
 */
public class RasterLayerFactory implements ILcdGXYLayerFactory {

  private static final String LAYER_RASTER_ICON = "samples/images/layerIcons/mountain.gif";

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {

    // Check the model descriptor to make sure this is a raster model.
    if ( ! ( ( aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor ) ||
            ( aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor ) ) ) {
      throw new IllegalArgumentException( "Cannot create a layer for[" + aModel + "]: not a MultilevelRaster ILcdModel !" );
    }

    // Create a layer.
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer();
    gxy_layer.setModel( aModel );
    gxy_layer.setLabel( aModel.getModelDescriptor().getDisplayName() );
    gxy_layer.setSelectable( true );
    gxy_layer.setEditable( false );
    gxy_layer.setLabeled( false );
    gxy_layer.setVisible( true );
    gxy_layer.setIcon( new TLcdImageIcon( LAYER_RASTER_ICON ) );

    // Set a suitable pen on the layer.
    gxy_layer.setGXYPen( MapSupport.createPen( aModel.getModelReference() ) );

    // Create an ILcdGXYPainter to paint rasters.
    ILcdGXYPainterProvider gxy_painter_provider;
    if (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) {
      TLcdWarpMultilevelRasterPainter rp = new TLcdWarpMultilevelRasterPainter();
      rp.setForcePainting( true );
      gxy_painter_provider = rp;
    } else {
      TLcdWarpRasterPainter rp = new TLcdWarpRasterPainter();
      rp.setForcePainting( true );
      gxy_painter_provider = rp;
    }

    // Set it as an ILcdGXYPainterProvider on the layer.
    gxy_layer.setGXYPainterProvider( gxy_painter_provider );

    return gxy_layer;
  }
}
