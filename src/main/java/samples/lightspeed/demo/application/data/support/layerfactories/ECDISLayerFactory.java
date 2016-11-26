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

import java.util.ArrayList;
import java.util.Collection;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s52.lightspeed.TLspS52LayerBuilder;
import com.luciad.format.s57.TLcdS57CatalogueModelDescriptor;
import com.luciad.format.s57.TLcdS57ModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.maritime.ECDISConfigurationProvider;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

public class ECDISLayerFactory extends AbstractLayerFactory {

  private final TLcdS52DisplaySettings fS52DisplaySettings;

  // ILcdGXYLayerFactory for ECDIS models

  public ECDISLayerFactory() {
    fS52DisplaySettings = ECDISConfigurationProvider.getS52DisplaySettings();
  }

  @Override
  public final Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ArrayList<ILspLayer> layers = new ArrayList<ILspLayer>();
    ILspLayer ecdisLayer = TLspS52LayerBuilder.newBuilder()
                                              .s52DisplaySettings(fS52DisplaySettings)
                                              .model(aModel)
        .layerType(ILspLayer.LayerType.INTERACTIVE) //Interactive so it can be placed above bing maps
        .selectable(false)
        .build();
    layers.add(ecdisLayer);
    return layers;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdS57ModelDescriptor ||
            aModel.getModelDescriptor() instanceof TLcdS57CatalogueModelDescriptor);
  }
}
