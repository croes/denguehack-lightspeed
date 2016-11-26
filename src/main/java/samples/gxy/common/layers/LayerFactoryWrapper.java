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
package samples.gxy.common.layers;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

/**
 * ILcdGXYLayerFactory wrapper that adds the following functionality:
 * - asynchronous layers, if a paint queue is set
 * - model tree nodes and model lists to layer nodes
 */
public class LayerFactoryWrapper implements ILcdGXYLayerFactory {
  private ILcdGXYAsynchronousPaintQueue fQueue;
  private final ILcdGXYLayerFactory fFactory;
  private boolean fCreateAsynchronousLayers;

  public LayerFactoryWrapper(ILcdGXYLayerFactory aFactory) {
    fFactory = aFactory;
  }

  public void setCreateAsynchronousLayers(boolean aCreateAsynchronousLayers) {
    fCreateAsynchronousLayers = aCreateAsynchronousLayers;
  }

  public void setGXYAsynchronousPaintQueue(ILcdGXYAsynchronousPaintQueue aQueue) {
    fCreateAsynchronousLayers = true;
    fQueue = aQueue;
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    // This works for both ILcdModelTreeNode and ILcdModelList
    if (aModel instanceof ILcdModelContainer &&
        ((ILcdModelContainer) aModel).modelCount() > 0) {
      ILcdModelContainer modelContainer = (ILcdModelContainer) aModel;

      TLcdGXYLayerTreeNode node = new TLcdGXYLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      for (int i = 0; i < modelContainer.modelCount(); i++) {
        ILcdGXYLayer layer = createGXYLayer(modelContainer.getModel(i));
        if (layer == null) {
          return null;
        }
        node.addLayer(layer);
      }
      return node;
    } else {
      ILcdGXYLayer layer = fFactory.createGXYLayer(aModel);
      return layer != null && fCreateAsynchronousLayers ? new TLcdGXYAsynchronousLayerWrapper(layer, fQueue) : layer;
    }
  }
}
