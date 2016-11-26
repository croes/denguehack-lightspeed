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
import java.lang.ref.SoftReference;
import java.util.Enumeration;

import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.ILcdGraphListener;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.network.graph.partition.ILcdPartitionedGraphListener;

/**
 * Implementation of {@code ILcdPartitionedGraph} that lazily loads its graph via an {@code IGraphDecoder}
 * and uses a {@code SoftReference} for storing this graph.
 */
public class SoftPartitionedGraph implements ILcdPartitionedGraph {

  private String fSource;
  private IGraphDecoder fGraphDecoder;
  private SoftReference<ILcdPartitionedGraph> fSoftReferencedGraph;
  private boolean fCorrupted = false;

  protected SoftPartitionedGraph(String aSource,
                                 IGraphDecoder aGraphDecoder) {
    fSource = aSource;
    fGraphDecoder = aGraphDecoder;
  }

  /**
   * Gets the source of this <code>ILcdModel</code>.
   *
   * @return the source of this <code>ILcdModel</code>.
   */
  public String getSource() {
    return fSource;
  }

  /**
   * Gets the <code>ILcdModelDecoder</code> for this <code>ILcdModel</code>.
   *
   * @return the <code>ILcdModelDecoder</code> for this <code>ILcdModel</code>.
   */
  public IGraphDecoder getGraphDecoder() {
    return fGraphDecoder;
  }

  // Implementations for ILcdGraph.

  public boolean containsNode(Object aNode) {
    return getGraphChecked().containsNode(aNode);
  }

  public Enumeration getNodes() {
    return getGraphChecked().getNodes();
  }

  public boolean containsEdge(Object aEdge) {
    return getGraphChecked().containsEdge(aEdge);
  }

  public Enumeration getEdges() {
    return getGraphChecked().getEdges();
  }

  public Enumeration getEdges(Object o) {
    return getGraphChecked().getEdges(o);
  }

  public Object getStartNode(Object o) {
    return getGraphChecked().getStartNode(o);
  }

  public Object getEndNode(Object o) {
    return getGraphChecked().getEndNode(o);
  }

  public Object getOppositeNode(Object o, Object o1) {
    return getGraphChecked().getOppositeNode(o, o1);
  }

  public boolean isConnected(Object o, Object o1) {
    return getGraphChecked().isConnected(o, o1);
  }

  public void addGraphListener(ILcdGraphListener aGraphListener) {
    // Not implemented.
  }

  public void removeGraphListener(ILcdGraphListener aGraphListener) {
    // Not implemented.
  }

  // Implementations for ILcdPartitionedGraph.

  public void addPartitionedGraphListener(ILcdPartitionedGraphListener aPartitionedGraphListener) {
    getGraphChecked().addPartitionedGraphListener(aPartitionedGraphListener);
  }

  public boolean containsPartition(ILcdGraph aPartition) {
    return getGraphChecked().containsPartition(aPartition);
  }

  public Enumeration getBoundaryEdges(ILcdGraph aPartition) {
    return getGraphChecked().getBoundaryEdges(aPartition);
  }

  public ILcdGraph getBoundaryGraph() {
    return getGraphChecked().getBoundaryGraph();
  }

  public Enumeration getBoundaryNodes(ILcdGraph aPartition) {
    return getGraphChecked().getBoundaryNodes(aPartition);
  }

  public ILcdGraph getPartitionForEdge(Object aEdge) {
    return getGraphChecked().getPartitionForEdge(aEdge);
  }

  public ILcdGraph getPartitionForNode(Object aNode) {
    return getGraphChecked().getPartitionForNode(aNode);
  }

  public Enumeration<ILcdGraph> getPartitions() {
    return getGraphChecked().getPartitions();
  }

  public void removePartitionedGraphListener(ILcdPartitionedGraphListener aPartitionedGraphListener) {
    getGraphChecked().removePartitionedGraphListener(aPartitionedGraphListener);
  }

  private ILcdPartitionedGraph getGraphChecked() {
    try {
      ILcdPartitionedGraph graph = getGraph(true);
      return graph;
    } catch (Exception e) {
      IllegalStateException ex = new IllegalStateException("Could not load graph from source " + fSource);
      ex.initCause(e);
      throw ex;
    }
  }

  /**
   * @param aLoadIfUnavailable
   *
   * @return
   */
  protected ILcdPartitionedGraph getGraph(boolean aLoadIfUnavailable) throws IOException {
    if (fCorrupted) {
      return null;
    }
    if (aLoadIfUnavailable) {
      ILcdPartitionedGraph graph = null;

      if ((fSoftReferencedGraph == null || (graph = fSoftReferencedGraph.get()) == null) &&
          getGraphDecoder() != null) {

        try {
          graph = (ILcdPartitionedGraph) getGraphDecoder().decodeGraph(getSource());
          if (graph == null) {
            throw new IOException("Graph could not be decoded: Graph decoder returned null.");
          }
          fSoftReferencedGraph = new SoftReference<ILcdPartitionedGraph>(graph);
        } catch (RuntimeException ex) {
          // For some reason we couldn't load the graph.
          // Don't try loading it ever again.
          fCorrupted = true;
          fSoftReferencedGraph = null;
          throw ex;
        } catch (IOException ex) {
          // For some reason we couldn't load the graph.
          // Don't try loading it ever again.
          fCorrupted = true;
          fSoftReferencedGraph = null;
          throw ex;
        }
      }
      return graph;
    } else {
      if (fSoftReferencedGraph == null) {
        return null;
      } else {
        return fSoftReferencedGraph.get();
      }
    }
  }

}
