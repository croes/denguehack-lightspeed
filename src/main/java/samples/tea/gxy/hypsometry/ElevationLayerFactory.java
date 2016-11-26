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
package samples.tea.gxy.hypsometry;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.earth.view.gxy.TLcdEarthGXYElevationRasterPainter;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdMultilevelRasterPainter;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterPainter;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
* A layer factory for elevation models
*/
class ElevationLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    return createElevationLayer( aModel );
  }

  /**
   * Creates a layer for a raster, multilevel raster or earth tileset-based model.
   *
   * @param aModel a model containing either a raster, a multilevel raster or an
   *               earth tileset
   *
   * @return a layer capable of visualizing the model
   */
  private ILcdGXYLayer createElevationLayer( ILcdModel aModel ) {

    TLcdGXYLayer gxyLayer = new TLcdGXYLayer();
    gxyLayer.setModel( aModel );
    gxyLayer.setLabel( aModel.getModelDescriptor().getDisplayName() );

    // Create a painter to paint rasters or multilevel rasters.
    ILcdGXYPainter gxyPainter = null;
    if ( aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor ) {
      gxyPainter = new TLcdRasterPainter();
    }
    else if ( aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor ) {
      gxyPainter = new TLcdMultilevelRasterPainter();
    }
    else if ( aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor ) {
      gxyPainter = new TLcdEarthGXYElevationRasterPainter();
    }

    // The painters can act as their own painter providers.
    gxyLayer.setGXYPainterProvider( ( ILcdGXYPainterProvider ) gxyPainter );

    return gxyLayer;
  }
}
