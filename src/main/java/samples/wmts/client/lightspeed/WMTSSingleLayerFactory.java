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
package samples.wmts.client.lightspeed;

import static samples.wmts.client.gxy.WMTSSingleLayerFactory.getIcon;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.ogc.wmts.client.TLcdWMTSModelDescriptor;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.decoder.raster.MixedRasterLayerFactory;

/**
 * A layer factory that can create layers for single WMTS models. It <i>cannot</i> create layers for WMTS tree models.
 * It is intended as a delegate for {@link WMTSLayerFactory}, which <i>can</i> create
 * layers for both single WMTS models and WMTS tree models.
 *
 * @see WMTSLayerFactory
 * @since 2013.1
 */
public class WMTSSingleLayerFactory extends MixedRasterLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdWMTSModelDescriptor) && !(aModel instanceof
        ILcdModelTreeNode);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspLayer layer = super.createLayer(aModel);
    layer.setIcon(getIcon(aModel));
    return layer;
  }
}
