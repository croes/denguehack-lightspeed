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
package samples.earth.gxy;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.gxy.decoder.MapSupport;

@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.LOW_PRIORITY - 1)
public class EarthLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor) {
      // Create a layer using a TLcdGXYImagePainter.
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      TLcdGXYImagePainter painter = new TLcdGXYImagePainter();
      layer.setGXYPainterProvider(painter);
      return layer;
    }
    return null;
  }
}
