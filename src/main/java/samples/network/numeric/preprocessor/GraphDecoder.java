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
package samples.network.numeric.preprocessor;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.network.algorithm.partitioning.TLcdClusteredPartitioningAlgorithm;
import com.luciad.network.graph.ILcdGraph;

/**
 * Sample implementation of {@code IGraphDecoder}.
 * <p/>
 * This implementation will create a graph by performing the following steps:
 * <p/>
 * <ul>
 * <li>Decode the model using a model decoder</li>
 * <li>Convert the model to a graph using a graph factory</li>
 * <li>Convert this graph to a partitioned graph using the clustered partitioning algorithm.</li>
 * </ul>
 */
public class GraphDecoder implements IGraphDecoder {

  private ILcdModelDecoder fModelDecoder;
  private IGraphFactory fGraphFactory;

  private PerformanceLogger fLogger = new PerformanceLogger();

  public GraphDecoder(ILcdModelDecoder aModelDecoder, IGraphFactory aGraphFactory) {
    fModelDecoder = aModelDecoder;
    fGraphFactory = aGraphFactory;
  }

  public ILcdGraph decodeGraph(String aSourceName) throws IOException {
    fLogger.logStart("Building graph " + aSourceName);

    fLogger.logStart("Decoding model ");
    ILcdModel model = fModelDecoder.decode(aSourceName);
    fLogger.logEnd();

    fLogger.logStart("Converting model to graph");
    ILcdGraph graph = fGraphFactory.createGraph(model);
    fLogger.logEnd();

    fLogger.logStart("Partitioning graph");
    TLcdClusteredPartitioningAlgorithm partitioningAlgorithm = new TLcdClusteredPartitioningAlgorithm();
    // The following parameters can be tweaked to obtain better partitioning results or to control
    // the computation time. Please refer to the documentation of
    // {@code TLcdClusteredPartitioningAlgorithm} for more details on the partitioning parameters.
    partitioningAlgorithm.setNumberOfIterations(2);
    ILcdGraph partition = partitioningAlgorithm.partition(graph, null, 5, 400, 10, 30);
    fLogger.logEnd();

    fLogger.logEnd();
    return partition;
  }

}
