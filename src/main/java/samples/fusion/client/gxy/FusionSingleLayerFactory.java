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
package samples.fusion.client.gxy;

import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

/**
 * A layer factory for single fusion layers, which delegates to a raster, vector and non-tiled layer factory.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class FusionSingleLayerFactory implements ILcdGXYLayerFactory {

  private final RasterFusionLayerFactory fRasterFusionLayerFactory = new RasterFusionLayerFactory();
  private final VectorFusionLayerFactory fVectorFusionLayerFactory = new VectorFusionLayerFactory();
  private final NonTiledFusionLayerFactory fNonTiledFusionLayerFactory = new NonTiledFusionLayerFactory();

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLfnTileStoreModelDescriptor)) {
      return null;
    }
    // First try to create a raster layer.
    ILcdGXYLayer layer = fRasterFusionLayerFactory.createGXYLayer(aModel);
    // Then try to create a vector layer.
    if (layer == null) {
      layer = fVectorFusionLayerFactory.createGXYLayer(aModel);
    }
    // Then try to create a non-tiled layer.
    if (layer == null) {
      layer = fNonTiledFusionLayerFactory.createGXYLayer(aModel);
    }
    // Return the layer, possibly null.
    return layer;
  }
}
