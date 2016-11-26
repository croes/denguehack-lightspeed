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
package samples.decoder.sqlite.lrdb;

import com.luciad.earth.view.gxy.TLcdEarthGXYElevationRasterPainter;
import com.luciad.format.sqlite.lrdb.TLcdLRDBModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
 * Layer factory for LRDB elevation data.
 * For LRDB image data and LVDB (vector) data the usual raster and shape layers are sufficient.
 */
@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.HIGH_PRIORITY)
public class LRDBElevationLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    if (TLcdLRDBModelDecoder.ELEVATION_TYPE_NAME.equals(modelDescriptor.getTypeName())) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPainterProvider(new TLcdEarthGXYElevationRasterPainter());
      return layer;
    }
    return null;
  }
}
