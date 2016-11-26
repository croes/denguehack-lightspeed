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

import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.util.ILcdFireEventMode;

/**
 * Implementation of {@code IGraphFactory} for creating very compact graph representations, using
 * the id's of objects as nodes and edges.
 */
public abstract class AIdGraphFactory implements IGraphFactory {

  private ISimpleEdgeValueFunction fEdgeValueFunction;

  public AIdGraphFactory(ISimpleEdgeValueFunction aEdgeValueFunction) {

    fEdgeValueFunction = aEdgeValueFunction;
  }

  // Implementations for IGraphFactory.

  public ILcdGraph createGraph(ILcdModel aModel) {
    TLcdGraph graph = new TLcdGraph();

    // First add all nodes to the graph.
    for (Enumeration elementsEnum = aModel.elements(); elementsEnum.hasMoreElements(); ) {
      Object element = elementsEnum.nextElement();
      if (isNode(element)) {
        Object node = getNodeId(element);
        if (graph.canAddNode(node)) {
          graph.addNode(node, ILcdFireEventMode.NO_EVENT);
        }
      }
    }

    // Then add all edges to the graph.
    for (Enumeration elementsEnum = aModel.elements(); elementsEnum.hasMoreElements(); ) {
      Object element = elementsEnum.nextElement();
      if (isEdge(element)) {
        Object edge = getEdgeId(element);
        Object startNodeId = getStartNodeId(element);
        Object endNodeId = getEndNodeId(element);
        if (startNodeId != null && endNodeId != null) {
          ValuedEdge valuedEdge = new ValuedEdge(edge,
                                                 fEdgeValueFunction.getEdgeValue(element, TLcdTraversalDirection.FORWARD),
                                                 fEdgeValueFunction.getEdgeValue(element, TLcdTraversalDirection.BACKWARD));
          if (graph.canAddEdge(valuedEdge, startNodeId, endNodeId)) {
            graph.addEdge(valuedEdge, startNodeId, endNodeId, ILcdFireEventMode.NO_EVENT);
          }
        }
      }
    }

    return graph;
  }

  protected abstract boolean isNode(Object aObject);

  protected abstract boolean isEdge(Object aObject);

  protected abstract Object getNodeId(Object aNativeNode);

  protected abstract Object getEdgeId(Object aNativeEdge);

  protected abstract Object getStartNodeId(Object aNativeEdge);

  protected abstract Object getEndNodeId(Object aNativeEdge);
}
