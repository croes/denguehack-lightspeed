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
package samples.gxy.concurrent.painting;

import java.util.ArrayList;
import java.util.Enumeration;

import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerTreeNodeWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerTreeNodeWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

/**
 * Utility class that wraps an existing layer in an ILcdGXYAsynchronousLayerWrapper.
 * Before adding asynchronous layers to a view, ensure that the view has a paint queue manager:
 * <code>
 * <pre>
 * TLcdGXYAsynchronousPaintQueueManager manager = new TLcdGXYAsynchronousPaintQueueManager();
 * manager.setGXYView( view );
 * </pre>
 * </code>
 */
public class AsynchronousLayerFactory {

  public static ILcdGXYLayer createAsynchronousLayer(ILcdGXYLayer aLayer) {
    if (aLayer instanceof ILcdGXYAsynchronousLayerWrapper) {
      return aLayer;
    }
    if (aLayer instanceof ILcdLayerTreeNode) {
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) aLayer;
      ILcdLayerTreeNode asynchronousNode = aLayer instanceof ILcdGXYEditableLabelsLayer ?
                                           new TLcdGXYAsynchronousEditableLabelsLayerTreeNodeWrapper((ILcdGXYEditableLabelsLayer) aLayer) :
                                           new TLcdGXYAsynchronousLayerTreeNodeWrapper(aLayer);

      // Make all the leaf layers asynchronous as well.
      ArrayList<ILcdGXYLayer> leafLayers = new ArrayList<ILcdGXYLayer>();
      for (Enumeration layers = node.layers(); layers.hasMoreElements(); ) {
        leafLayers.add((ILcdGXYLayer) layers.nextElement());
      }
      asynchronousNode.removeAllLayers();
      for (ILcdGXYLayer leaf : leafLayers) {
        asynchronousNode.addLayer(createAsynchronousLayer(leaf), asynchronousNode.layerCount());
      }

      return (ILcdGXYLayer) asynchronousNode;
    }
    return aLayer instanceof ILcdGXYEditableLabelsLayer ?
           new TLcdGXYAsynchronousEditableLabelsLayerWrapper((ILcdGXYEditableLabelsLayer) aLayer) :
           new TLcdGXYAsynchronousLayerWrapper(aLayer);
  }
}
