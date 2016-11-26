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
package samples.decoder.ecdis.lightspeed;

import static com.luciad.format.s57.ELcdS57ProductType.AML;
import static com.luciad.format.s57.ELcdS57ProductType.ENC;

import java.io.IOException;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s52.lightspeed.TLspS52LayerBuilder;
import com.luciad.format.s57.ELcdS57ProductType;
import com.luciad.format.s57.TLcdS57CatalogueModelDescriptor;
import com.luciad.format.s57.TLcdS57ModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.decoder.ecdis.common.S52DisplaySettingsSingleton;

/**
 * <p>An {@code ILspLayerFactory} implementation which is capable of creating layers for S-57
 * models. The
 * layer factory has a default constructor which allows to register it in the service
 * mechanism.</p>
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class S52LayerFactory extends ALspSingleLayerFactory {
  private final TLcdS52DisplaySettings fS52DisplaySettings;

  /**
   * <p>Default constructor, needed to register the layer factory as a service.</p>
   *
   * <p>This constructor calls {@link #S52LayerFactory(TLcdS52DisplaySettings)}
   * with
   * default instances. The created instances can be retrieved afterwards using the available
   * getters on this class.</p>
   */
  public S52LayerFactory() throws IOException {
    this(S52DisplaySettingsSingleton.getSettings());
  }

  S52LayerFactory(TLcdS52DisplaySettings aS52DisplaySettings) {
    fS52DisplaySettings = aS52DisplaySettings;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspInteractivePaintableLayer layer = TLspS52LayerBuilder.newBuilder()
                                                             .model(aModel)
                                                             .s52DisplaySettings(fS52DisplaySettings)
                                                             .build();
    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    if (modelDescriptor instanceof TLcdS57ModelDescriptor ||
        modelDescriptor instanceof TLcdS57CatalogueModelDescriptor) {
      ELcdS57ProductType productType = getProductType(aModel);
      if (ENC.equals(productType) || AML.equals(productType)) {
        return true;
      } else {
        System.err.println("Warning: could not determine product type for S-57 model " + aModel.getModelDescriptor().getSourceName());
      }
    }
    return false;
  }

  private static ELcdS57ProductType getProductType(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdS57ModelDescriptor) {
      return ((TLcdS57ModelDescriptor) aModel.getModelDescriptor()).getProductType();
    } else if (aModel instanceof ILcdModelContainer) {
      for (int i = 0; i < ((ILcdModelContainer) aModel).modelCount(); i++) {
        ILcdModel subModel = ((ILcdModelContainer) aModel).getModel(i);
        ELcdS57ProductType productType = getProductType(subModel);
        if (productType != null) {
          return productType;
        }
      }
    }
    return null;
  }
}
