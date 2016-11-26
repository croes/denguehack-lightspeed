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
package samples.lightspeed.common;

import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspInitialLayerIndexProvider;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;

/**
 * TLspInitialLayerIndexProvider that makes sure (one) grid layer stays on top.
 */
public class InitialLayerIndexProvider extends TLspInitialLayerIndexProvider {

  @Override
  public int getInitialLayerIndex(ILcdLayer aLayer, ILcdLayerTreeNode aLayerNode) {
    int initialLayerIndex = super.getInitialLayerIndex(aLayer, aLayerNode);
    if (aLayerNode.layerCount() > 0) {
      if (isGridLayer(aLayer)) {
        return aLayerNode.layerCount();
      } else {
        // check if the computed index would push down the (top) grid layer
        ILcdLayer layer = aLayerNode.getLayer(aLayerNode.layerCount() - 1);
        if (initialLayerIndex == aLayerNode.layerCount() &&
            isGridLayer(layer)) {
          return initialLayerIndex - 1;
        }
      }
    }
    return initialLayerIndex;
  }

  private boolean isGridLayer(ILcdLayer aLayer) {
    return TLspLonLatGridLayerBuilder.GRID_TYPE_NAME.equals(aLayer.getModel().getModelDescriptor().getTypeName());
  }
}
