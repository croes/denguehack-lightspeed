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
package samples.decoder.netcdf.lightspeed;

import static java.util.Collections.singleton;

import static samples.earth.util.WeatherUtil.isWeatherSingleModel;
import static samples.gxy.common.layers.factories.GXYUnstyledLayerFactory.setVisible;

import com.luciad.format.netcdf.TLcdNetCDFModelDescriptor;
import com.luciad.format.netcdf.lightspeed.TLspNetCDFLayerBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.lightspeed.decoder.UnstyledLayerFactory;

/**
 * Layer factory for NetCDF models, identified by a {@link TLcdNetCDFModelDescriptor}. It creates tree layers for NetCDF tree
 * models, and single layers for single NetCDF models. It enables the initial visibility of NetCDF tree layers up to a max.
 * number of visible layers.
 * <p/>
 * This layer factory will be picked up by the {@link samples.lightspeed.decoder.MainPanel Lightspeed decoder sample}.
 *
 */
@LcdService(service = ILspLayerFactory.class)
public class NetCDFLayerFactory extends UnstyledLayerFactory {

  private final ALspSingleLayerFactory fSingleLayerFactory;
  private final int fMaxVisibleLayers;

  /**
   * Constructs a NetCDF layer factory with a given single layer factory and max. visible layers.
   * <p/>
   * The max. visible layers affects tree layers. If set to 0, all layers of a tree layer will initially be invisible.
   *
   * @param aSingleLayerFactory a layer factory for creating single layers, must not be {@code null}
   * @param aMaxVisibleLayers   a max. number of visible layers
   */
  protected NetCDFLayerFactory(ALspSingleLayerFactory aSingleLayerFactory, int aMaxVisibleLayers) {
    super(singleton(aSingleLayerFactory));
    fSingleLayerFactory = aSingleLayerFactory;
    fMaxVisibleLayers = aMaxVisibleLayers;
  }

  /**
   * Constructs a default NetCDF layer factory with a {@link NetCDFSingleLayerFactory} and max.
   * visible layers of 1.
   */
  public NetCDFLayerFactory() {
    this(new NetCDFSingleLayerFactory(), 1);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return TLcdNetCDFModelDescriptor.TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!canCreateLayers(aModel)) {
      return null;
    }
    if (aModel instanceof ILcdModelTreeNode) {
      // Delegate to the superclass to handle NetCDF tree layers.
      ILspLayer layer = super.createLayer(aModel);
      setVisible(layer, fMaxVisibleLayers);
      return layer;
    } else {
      return fSingleLayerFactory.createLayer(aModel);
    }
  }

  public static class NetCDFSingleLayerFactory extends ALspSingleLayerFactory {

    @Override
    public boolean canCreateLayers(ILcdModel aModel) {
      return isWeatherSingleModel(aModel);
    }

    @Override
    public ILspLayer createLayer(ILcdModel aModel) {
      return TLspNetCDFLayerBuilder.newBuilder()
                                   .model(aModel)
                                   .build();
    }
  }

}
