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
package samples.wmts.client.gxy;

import static java.util.Collections.singleton;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.ogc.wmts.client.TLcdWMTSModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * Layer factory for WMTS models, identified by a {@link TLcdWMTSModelDescriptor}. It creates tree layers for WMTS tree
 * models, and single layers for single WMTS models. It enables the initial visibility of WMTS tree layers up to a max.
 * number of visible layers.
 * <p/>
 * This layer factory will be picked up by the {@link samples.gxy.decoder.MainPanel GXY decoder sample}. You can use that
 * sample to load a WMTS URL.
 *
 * @since 2013.0
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class WMTSLayerFactory extends GXYUnstyledLayerFactory {

  private final ILcdGXYLayerFactory fSingleLayerFactory;
  private final int fMaxVisibleLayers;

  /**
   * Constructs a WMTS layer factory with a given single layer factory and max. visible layers.
   * <p/>
   * The max. visible layers affects tree layers. If set to 0, all layers of a tree layer will initially be invisible.
   *
   * @param aSingleLayerFactory a GXY layer factory for creating single layers, must not be {@code null}
   * @param aMaxVisibleLayers   a max. number of visible layers
   */
  WMTSLayerFactory(ILcdGXYLayerFactory aSingleLayerFactory, int aMaxVisibleLayers) {
    super(singleton(aSingleLayerFactory));
    fSingleLayerFactory = aSingleLayerFactory;
    fMaxVisibleLayers = aMaxVisibleLayers;
  }

  /**
   * Constructs a default WMTS layer factory with a {@link samples.wmts.client.gxy.WMTSSingleLayerFactory} and max.
   * visible layers of 1.
   */
  public WMTSLayerFactory() {
    this(new WMTSSingleLayerFactory(), 1);
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdWMTSModelDescriptor)) {
      return null;
    }
    if (aModel instanceof ILcdModelTreeNode) {
      // Delegate to the superclass to handle WMTS tree layers.
      ILcdGXYLayer layer = super.createGXYLayer(aModel);
      setVisible(layer, fMaxVisibleLayers);
      return layer;
    } else {
      // Delegate to the single layer factory to handle single WMTS layers.
      return fSingleLayerFactory.createGXYLayer(aModel);
    }
  }

}
