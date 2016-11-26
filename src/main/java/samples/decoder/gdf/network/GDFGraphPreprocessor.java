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
package samples.decoder.gdf.network;

import java.util.Enumeration;
import java.util.Vector;

import com.luciad.format.gdf.ILcdGDFFeature;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.network.algorithm.partitioning.TLcdClusteredPartitioningAlgorithm;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;

import samples.decoder.gdf.filter.GDFTurnRestrictedEdgeFilter;
import samples.network.common.graph.GraphManager;

/**
 * Prepares decoded models to be used by the network algorithms:
 * - a graph is generated from the model
 * - the graph is partitioned into subgraphs
 * - the resulting partitioned graph is preprocessed by the TLcdPartitionedShortestRoutePreprocessor.
 * <p/>
 * Finally, the application is configured to use the new model and its graph.
 */
class GDFGraphPreprocessor implements ILcdModelProducerListener, ILcdStatusSource {

  private final GDFGraphFactory fGraphFactory = new GDFGraphFactory();
  private final GraphManager fGraphManager;

  private ILcdGraph fCurrentGraph;

  private final TLcdStatusEventSupport fStatusEventSupport = new TLcdStatusEventSupport();

  public GDFGraphPreprocessor(GraphManager aGraphManager) {
    fGraphManager = aGraphManager;
  }

  public void modelProduced(TLcdModelProducerEvent event) {
    preprocessModel(event.getModel());
  }

  public void preprocessModel(ILcdModel aModel) {

    // When a new model is produced, we check whether the graph in the graph manager
    // is still the old one or not. If it is, a graph should be generated for the
    // new model, if not, the decoder has generated a graph itself.
    if (fCurrentGraph == fGraphManager.getGraph()) {
      TLcdStatusEvent.Progress progress = TLcdStatusEvent.startIndeterminateProgress(fStatusEventSupport.asListener(), this, "Creating graph");

      ILcdGraph graph = fGraphFactory.createGraph(aModel);

      // Create constrained edges vector: the edges that are part of a turn restriction must not
      // become separated into different partitions, otherwise, the correct result of the routing
      // algorithm cannot be guaranteed.
      Vector constrainedEdges = new Vector();
      GDFTurnRestrictedEdgeFilter turn_filter = new GDFTurnRestrictedEdgeFilter();
      for (Enumeration edges_enum = graph.getEdges(); edges_enum.hasMoreElements(); ) {
        ILcdGDFFeature feature = (ILcdGDFFeature) edges_enum.nextElement();
        if (turn_filter.accept(feature)) {
          constrainedEdges.addElement(feature);
        }
      }

      // Partitioning the graph.
      TLcdClusteredPartitioningAlgorithm partitioningAlgorithm = new TLcdClusteredPartitioningAlgorithm();
      partitioningAlgorithm.setNumberOfIterations(5);
      ILcdPartitionedGraph partitionedGraph = partitioningAlgorithm.partition(graph, constrainedEdges);

      // Configure the graph manager to use the new graph.
      fGraphManager.setGraph(partitionedGraph);
      fGraphManager.updateDistanceTables();
      progress.end();
    }
    fCurrentGraph = fGraphManager.getGraph();
  }

  public void addStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.addStatusListener(aListener);
  }

  public void removeStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.removeStatusListener(aListener);
  }

}
