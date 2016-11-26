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
package samples.fusion.client.lightspeed;

import static java.util.Arrays.asList;

import static samples.fusion.client.gxy.FusionLayerFactory.setVisible;

import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.fusion.client.common.FusionCatalogModelDescriptor;
import samples.lightspeed.decoder.UnstyledLayerFactory;
import samples.lightspeed.imaging.multispectral.MultispectralLayerFactory;

/**
 * Layer factory for LuciadFusion catalog and single models, identified by a
 * {@link FusionCatalogModelDescriptor} and
 * {@link com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor} respectively. It creates tree layers for
 * LuciadFusion catalog models, and single layers for single LuciadFusion models. It enables the initial visibility of
 * LuciadFusion tree layers up to a max. number of visible layers.
 * <p/>
 * This layer factory will be picked up by the {@link samples.lightspeed.decoder.MainPanel Lightspeed decoder sample}.
 * You can use that sample to open a LuciadFusion URL.
 *
 * @since 2013.1
 */
@LcdService(service = ILspLayerFactory.class)
public class FusionLayerFactory extends UnstyledLayerFactory {

  private final int fMaxVisibleLayers;

  /**
   * Constructs a LuciadFusion layer factory with a given single layer factory and max. visible layers.
   * <p/>
   * The max. visible layers affects tree layers. If set to 0, all layers of a tree layer will initially be invisible.
   *
   * @param aMaxVisibleLayers   a max. number of visible layers
   * @param aSingleLayerFactories a layer factory for creating single layers, must not be {@code null}
   */
  FusionLayerFactory(int aMaxVisibleLayers, ALspSingleLayerFactory... aSingleLayerFactories) {
    super(asList(aSingleLayerFactories));
    fMaxVisibleLayers = aMaxVisibleLayers;
  }

  /**
   * Constructs a default LuciadFusion layer factory with a {@link samples.fusion.client.lightspeed.FusionSingleLayerFactory} and max.
   * visible layers of 1.
   */
  public FusionLayerFactory() {
    this(1, new MultispectralLayerFactory(), new FusionSingleLayerFactory());
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof FusionCatalogModelDescriptor;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!canCreateLayers(aModel)) {
      return null;
    }
    // Delegate to the superclass to handle LuciadFusion tree layers.
    ILspLayer layer = super.createLayer(aModel);
    setVisible(layer, fMaxVisibleLayers);
    return layer;
  }
}
