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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.util.Collection;
import java.util.Collections;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for models containing raster, multi-level raster objects or earth data.
 */
public class RasterLayerFactory extends AbstractLayerFactory {

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    ILcdModelDescriptor desc = aModel.getModelDescriptor();

    TLspRasterLayerBuilder layerBuilder = TLspRasterLayerBuilder.newBuilder()
                                                                .model(aModel)
                                                                .layerType(ILspLayer.LayerType.BACKGROUND);

    if (((desc instanceof TLcdRasterModelDescriptor) &&
         ((TLcdRasterModelDescriptor) desc).isElevation()) ||
        ((desc instanceof TLcdMultilevelRasterModelDescriptor) &&
         ((TLcdMultilevelRasterModelDescriptor) desc).isElevation())) {
      // Do not paint elevation data as a texture
      layerBuilder.styler(TLspPaintRepresentationState.REGULAR_BODY, TLspRasterStyle.newBuilder().opacity(0).build());
    } else {
      // Use default style
    }
    return layerBuilder.build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor);
  }
}
