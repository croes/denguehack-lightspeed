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

import static com.luciad.view.lightspeed.layer.ILspLayer.LayerType.BACKGROUND;

import java.util.Collection;
import java.util.Collections;

import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.metadata.ALfnResourceMetadata;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnRasterTileStoreModelDescriptor;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for fusion raster data. It creates layers for earth tile set models. Vector data typically requires styling
 * which is very specific, so we leave it up to special layer factories to do this (for example the OpenStreetMapLayerFactory).
 */
public class FusionLayerFactory extends AbstractLayerFactory {

  @Override
  public final Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ALfnTileStoreModel model = (ALfnTileStoreModel) aModel;
    TLfnTileStoreModelDescriptor descriptor = (TLfnTileStoreModelDescriptor) model.getModelDescriptor();
    ALfnCoverageMetadata metadata = descriptor.getCoverageMetadata();
    ILspLayer layer = TLspRasterLayerBuilder
        .newBuilder()
        .model(aModel)
        .label(getLabelFor(metadata))
        .layerType(BACKGROUND)
        .build();
    return Collections.singleton(layer);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLfnRasterTileStoreModelDescriptor;
  }

  /**
   * This is a copy-paste job from the corresponding method in TLfnMessageUtil.
   */
  private static String getLabelFor(ALfnResourceMetadata aMetadata) {
    String name = aMetadata.getName();
    if (name != null) {
      return name;
    }
    return "[" + aMetadata.getId() + "]";
  }
}
