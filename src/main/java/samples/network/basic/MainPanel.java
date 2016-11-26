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
package samples.network.basic;

import java.awt.EventQueue;

import com.luciad.model.ILcdModel;
import com.luciad.network.algorithm.partitioning.TLcdClusteredPartitioningAlgorithm;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.LuciadFrame;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.network.basic.function.EdgeValueFunctionChangedListener;
import samples.network.basic.function.SampleCompositeFunction;
import samples.network.basic.graph.GraphFactory;
import samples.network.basic.gui.NetworkConfigurationPanel;
import samples.network.basic.view.GraphLayerFactory;
import samples.network.common.ANetworkSample;

/**
 * This sample shows a simple graph to which several different edge and node
 * functions can be applied. Shortest routes and traces can be calculated,
 * resulting in different routes, depending on the chosen edge and node
 * function.
 */
public class MainPanel extends ANetworkSample {

  // Sample edge value function
  private SampleCompositeFunction fSampleCompositeFunction = new SampleCompositeFunction();

  public void createGUI() {
    super.createGUI();

    // Create the network configuration panel.
    NetworkConfigurationPanel configurationPanel = new NetworkConfigurationPanel(getGraphManager(), fSampleCompositeFunction);
    fToolbar.setNetworkConfigurationPanel(configurationPanel);

    // Make the grid layer invisible for better visibility of network layer.
    fMapJPanel.getGridLayer().setVisible(false);
  }

  protected void addData() {
    addData(new SampleGraphModel());
  }

  protected void addData(ILcdModel aModel) {
    setupGraph(aModel);
    createLayer(aModel);
  }

  private void setupGraph(ILcdModel aModel) {
    // Create the graph.
    final SampleGraphModel model = (SampleGraphModel) aModel;
    GraphFactory graphFactory = new GraphFactory();
    ILcdGraph graph = graphFactory.createGraph(aModel);

    // Configure the edge value functions.
    fSampleCompositeFunction.setTurnFunction(model.getTurnValueFunction());
    getGraphManager().setEdgeValueFunction(fSampleCompositeFunction);

    // Partition the graph.
    TLcdClusteredPartitioningAlgorithm merging = new TLcdClusteredPartitioningAlgorithm();
    ILcdPartitionedGraph partitionedGraph = merging.partition(graph, model.getConstraintedEdges());
    getGraphManager().setGraph(partitionedGraph);

    // Add a listener to the graph manager that recomputes the distance tables
    // each time the edge value function of the graph has changed.
    fSampleCompositeFunction.addEdgeValueFunctionChangedListener(new EdgeValueFunctionChangedListener() {
      public void edgeValueFunctionChanged() {
        // Edge value has been changed internally, re-set it on the graph manager.
        getGraphManager().setEdgeValueFunction(getGraphManager().getEdgeValueFunction());
        getGraphManager().updateDistanceTables();
        // Make sure the turns get repainted on the view.
        model.elementsChanged(model.getConstraintedEdges(), ILcdFireEventMode.FIRE_NOW);
      }
    });

    fSampleCompositeFunction.setEdgeFunctionMode(SampleCompositeFunction.CARTESIAN);
  }

  private void createLayer(ILcdModel aModel) {
    GraphLayerFactory layerFactory = new GraphLayerFactory(getGraphManager());
    ILcdGXYLayer graphLayer = layerFactory.createGXYLayer(aModel);
    GXYLayerUtil.fitGXYLayer(fMapJPanel, (ILcdGXYLayer) ((ILcdLayerTreeNode) graphLayer).getLayer(0));
    GXYLayerUtil.addGXYLayer(fMapJPanel, graphLayer, true, false);
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Network analysis", 900, 700);
      }
    });
  }

}
