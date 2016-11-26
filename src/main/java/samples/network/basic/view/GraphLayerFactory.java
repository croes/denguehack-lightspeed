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
package samples.network.basic.view;

import static samples.network.common.view.gxy.AGraphEdgePainterProvider.GraphEdgeMode.*;
import static samples.network.common.view.gxy.AGraphNodePainterProvider.GraphNodeMode.*;

import java.util.EnumSet;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.network.basic.graph.Edge;
import samples.network.basic.graph.Node;
import samples.network.common.graph.GraphManager;

/**
 * <code>ILcdGXYLayerFactory</code> that creates layers for graph models.
 */
public class GraphLayerFactory implements ILcdGXYLayerFactory {

  private GraphManager fGraphManager;

  public GraphLayerFactory(GraphManager aGraphManager) {
    fGraphManager = aGraphManager;
  }

  // Implementations for ILcdGXYLayerFactory.

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    TLcdGXYLayerTreeNode layerNode = new TLcdGXYLayerTreeNode("Graph ");

    // Edge layer
    ILcdGXYPainterProvider[] edgePainterProviders = {
        new EdgePainterProvider(fGraphManager, EnumSet.of(NORMAL_EDGE)),
        new EdgePainterProvider(fGraphManager, EnumSet.of(START_EDGE, END_EDGE)),
        new EdgePainterProvider(fGraphManager, EnumSet.of(ROUTE_EDGE))
    };
    ILcdFilter edgeFilter = new ILcdFilter() {
      public boolean accept(Object aObject) {
        return aObject instanceof Edge;
      }
    };
    layerNode.addLayer(createSubLayer(aModel, "Edges", edgePainterProviders, edgeFilter));

    // Node layer
    ILcdGXYPainterProvider[] nodePainterProviders = {
        new NodePainterProvider(fGraphManager, EnumSet.of(NORMAL_NODE)),
        new NodePainterProvider(fGraphManager, EnumSet.of(START_NODE, END_NODE))
    };
    ILcdFilter nodeFilter = new ILcdFilter() {
      public boolean accept(Object aObject) {
        return aObject instanceof Node;
      }
    };
    layerNode.addLayer(createSubLayer(aModel, "Nodes", nodePainterProviders, nodeFilter));

    return layerNode;
  }

  // Private helper methods.

  private TLcdGXYLayer createSubLayer(ILcdModel aModel,
                                      String aLabel,
                                      ILcdGXYPainterProvider[] aPainterProviders,
                                      ILcdFilter aFilter) {
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    layer.setSelectable(true);
    layer.setEditable(false);
    layer.setLabel(aLabel);
    layer.setLabeled(true);
    layer.setGXYPen(new TLcdGeodeticPen(true));
    layer.getGXYPen().setHotPointSize(4);
    layer.setGXYLabelPainterProvider(new TLcdGXYLabelPainter());
    layer.setGXYPainterProviderArray(aPainterProviders);
    layer.setFilter(aFilter);
    return layer;
  }

}
