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
package samples.lightspeed.integration.gxy;

import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.integration.gxy.TLspGXYLayerAdapter;
import com.luciad.view.lightspeed.layer.integration.gxy.TLspGXYLayerTreeNodeAdapter;

/**
 * Adapts GXY layers created by the delegate ILcdGXYLayerFactory
 * using TLspGXYLayerAdapter, and TLspGXYLayerTreeNodeAdapter,
 * so that they can be used in a Lightspeed view.
 */
public class AdaptedLayerFactory extends ALspSingleLayerFactory {

  private ILcdGXYLayerFactory fDelegate;

  public AdaptedLayerFactory(ILcdGXYLayerFactory aDelegate) {
    fDelegate = aDelegate;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILcdGXYLayer gxyLayer = fDelegate.createGXYLayer(aModel);
    return adapt(gxyLayer);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return fDelegate.createGXYLayer(aModel) != null;
  }

  private static TLspGXYLayerAdapter adapt(ILcdLayer aLayer) {
    if (aLayer instanceof ILcdLayerTreeNode) {
      return adaptNode((ILcdLayerTreeNode) aLayer);
    } else if (aLayer instanceof ILcdGXYLayer) {
      @SuppressWarnings("UnnecessaryLocalVariable")
          TLspGXYLayerAdapter adapter = new TLspGXYLayerAdapter((ILcdGXYLayer) aLayer);
      return adapter;
    } else {
      throw new IllegalArgumentException("Cannot handle : " + aLayer.getClass());
    }
  }

  private static TLspGXYLayerTreeNodeAdapter adaptNode(ILcdLayerTreeNode aLayerTree) {

    // adapt the node
    TLspGXYLayerTreeNodeAdapter adapter = new TLspGXYLayerTreeNodeAdapter((ILcdGXYLayer) aLayerTree);
    adapter.setPaintOnTopOfChildrenHint(aLayerTree.isPaintOnTopOfChildrenHint());
    adapter.setInitialLayerIndexProvider(aLayerTree.getInitialLayerIndexProvider());
    for (int i = 0, n = aLayerTree.layerCount(); i != n; i++) {
      ILcdLayer layer = aLayerTree.getLayer(i);
      // adapt the child layers as well
      adapter.addLayer(adapt(layer));
    }
    return adapter;
  }

}
