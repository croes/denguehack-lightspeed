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
package samples.network.numeric.view.gxy;

import java.util.EnumSet;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphEdgePainterProvider;
import samples.network.common.view.gxy.AGraphNodePainterProvider;

/**
 * Layer factory for SHP files with edges and nodes.
 */
public class SHPLayerFactory implements ILcdGXYLayerFactory {

  private GraphManager fGraphManager;

  public SHPLayerFactory(GraphManager aGraphManager) {
    fGraphManager = aGraphManager;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    ILcdModelTreeNode model = (ILcdModelTreeNode) aModel;

    TLcdGXYLayerTreeNode layer = new TLcdGXYLayerTreeNode("Roads");

    // Edge layer
    ILcdGXYLayer edgeLayer = createSubLayer(model.getModel(0), "Edges", new ILcdGXYPainterProvider[]{
        new EdgePainterProvider(fGraphManager, EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.NORMAL_EDGE,
                                                          AGraphEdgePainterProvider.GraphEdgeMode.ROUTE_EDGE,
                                                          AGraphEdgePainterProvider.GraphEdgeMode.DESTROYED_EDGE)),
        new EdgePainterProvider(fGraphManager, EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.START_EDGE,
                                                          AGraphEdgePainterProvider.GraphEdgeMode.END_EDGE))
    });

    layer.addLayer(edgeLayer);

    // Node layer
    ILcdGXYLayer nodeLayer = createSubLayer(model.getModel(1), "Nodes", new ILcdGXYPainterProvider[]{
        new NodePainterProvider(fGraphManager, EnumSet.of(AGraphNodePainterProvider.GraphNodeMode.NORMAL_NODE)),
        new NodePainterProvider(fGraphManager, EnumSet.of(AGraphNodePainterProvider.GraphNodeMode.START_NODE,
                                                          AGraphNodePainterProvider.GraphNodeMode.END_NODE))
    });
    layer.addLayer(nodeLayer);

    return layer;
  }

  private ILcdGXYLayer createSubLayer(ILcdModel aModel,
                                      String aLabel,
                                      ILcdGXYPainterProvider[] aPainterProviders) {
    TLcdGXYLayer layer = new TLcdGXYLayer();

    layer.setModel(aModel);
    layer.setLabel(aLabel);

    layer.setSelectable(true);
    layer.setEditable(false);
    layer.setLabeled(false);
    layer.setVisible(true);

    layer.setScaleRange(new TLcdInterval(0.005, Double.POSITIVE_INFINITY));
    layer.setMinimumObjectSizeForPainting(0);

    layer.setGXYPainterProviderArray(aPainterProviders);

    return new TLcdGXYAsynchronousLayerWrapper(layer);
  }

}
