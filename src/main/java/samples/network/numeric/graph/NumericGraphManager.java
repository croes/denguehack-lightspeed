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
package samples.network.numeric.graph;

import com.luciad.network.graph.numeric.TLcdNumericGraph;
import com.luciad.network.graph.route.TLcdRoute;

import samples.network.common.graph.GraphManager;
import samples.network.common.graph.GraphParameterChangedListener;

/**
 * Graph manager that works with numeric graphs. It takes care of the conversion from native
 * domain object to numerical id.
 */
public class NumericGraphManager extends GraphManager {

  private IDataObject2GraphIdMap fNode2IdMap;
  private IDataObject2GraphIdMap fEdge2IdMap;
  private IGraphId2DataIdMap fId2EdgeIdMap;

  @Override
  public TLcdNumericGraph getGraph() {
    return (TLcdNumericGraph) super.getGraph();
  }

  public IDataObject2GraphIdMap getEdge2IdMap() {
    return fEdge2IdMap;
  }

  public void setEdge2IdMap(IDataObject2GraphIdMap aEdge2IdMap) {
    fEdge2IdMap = aEdge2IdMap;
  }

  public IDataObject2GraphIdMap getNode2IdMap() {
    return fNode2IdMap;
  }

  public void setNode2IdMap(IDataObject2GraphIdMap aNode2IdMap) {
    fNode2IdMap = aNode2IdMap;
  }

  public IGraphId2DataIdMap getId2EdgeIdMap() {
    return fId2EdgeIdMap;
  }

  public void setId2EdgeIdMap(IGraphId2DataIdMap aId2EdgeIdMap) {
    fId2EdgeIdMap = aId2EdgeIdMap;
  }

  @Override
  public boolean canSetEndEdge(Object aEndEdge) {
    Long numericEdge = getNumericEdge(aEndEdge);
    Long numericNode = getEndNode() != null ? getNumericNode(getEndNode()) : null;
    return getGraph().containsEdge(numericEdge) &&
           (getEndNode() == null || getGraph().isConnected(numericEdge, numericNode));
  }

  @Override
  public boolean canSetEndNode(Object aEndNode) {
    Long numericEdge = getEndEdge() != null ? getNumericEdge(getEndEdge()) : null;
    Long numericNode = getNumericNode(aEndNode);
    return getGraph().containsNode(numericNode) &&
           (getEndEdge() == null || getGraph().isConnected(numericEdge, numericNode));
  }

  @Override
  public boolean canSetStartEdge(Object aStartEdge) {
    Long numericEdge = getNumericEdge(aStartEdge);
    Long numericNode = getStartNode() != null ? getNumericNode(getStartNode()) : null;
    return getGraph().containsEdge(numericEdge) &&
           (getStartNode() == null || getGraph().isConnected(numericEdge, numericNode));
  }

  @Override
  public boolean canSetStartNode(Object aStartNode) {
    Long numericEdge = getStartEdge() != null ? getNumericEdge(getStartEdge()) : null;
    Long numericNode = getNumericNode(aStartNode);
    return getGraph().containsNode(numericNode) &&
           (getStartEdge() == null || getGraph().isConnected(numericEdge, numericNode));
  }

  @Override
  public boolean canDestroyEdge(Object aEdge) {
    Object edge = getNumericEdge(aEdge);
    return getGraph().containsEdge((Long) edge);
  }

  public void destroyEdge(Object aEdge) {
    Object edge = getNumericEdge(aEdge);
    getGraph().updateEdgeValue((Long) edge, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    clearRoutes();
    notifyListeners(GraphParameterChangedListener.GraphParameter.EDGE_DESTROYED, aEdge, null);
  }

  @Override
  protected TLcdRoute<Long, Long> getStartRoute() {
    Long startNode = getNumericNode(getStartNode());
    Long startEdge = getStartEdge() == null ? null : getNumericEdge(getStartEdge());

    TLcdRoute<Long, Long> startRoute = new TLcdRoute<Long, Long>(getGraph(), startNode);
    if (startEdge != null) {
      startRoute.addEdgeAtStart(startEdge);
    }
    return startRoute;
  }

  @Override
  protected TLcdRoute<Long, Long> getEndRoute() {
    Long endNode = getNumericNode(getEndNode());
    Long endEdge = getEndEdge() == null ? null : getNumericEdge(getEndEdge());

    TLcdRoute<Long, Long> endroute = new TLcdRoute<Long, Long>(getGraph(), endNode);
    if (endEdge != null) {
      endroute.addEdgeAtEnd(endEdge);
    }
    return endroute;
  }

  private Long getNumericNode(Object aNode) {
    return fNode2IdMap.getId(aNode);
  }

  private Long getNumericEdge(Object aEdge) {
    return fEdge2IdMap.getId(aEdge);
  }

}
