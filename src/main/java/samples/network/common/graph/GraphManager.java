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
package samples.network.common.graph;

import static samples.network.common.graph.GraphParameterChangedListener.GraphParameter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.network.algorithm.routing.ILcdShortestRouteAlgorithm;
import com.luciad.network.algorithm.routing.ILcdShortestRouteDistanceTableProvider;
import com.luciad.network.algorithm.routing.ILcdTracingAlgorithm;
import com.luciad.network.algorithm.routing.ILcdTracingResultHandler;
import com.luciad.network.algorithm.routing.TLcdPartitionedShortestRouteAlgorithm;
import com.luciad.network.algorithm.routing.TLcdPartitionedShortestRoutePreprocessor;
import com.luciad.network.function.ILcdDistanceFunction;
import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.network.graph.route.TLcdRoute;
import com.luciad.network.graph.route.TLcdRouteUtil;

/**
 * This class stores all relevant graph-related information: the graph itself,
 * the start and/or end nodes/edges that are currently selected, the algorithms
 * and the functions to be used by the algorithms.
 */
public class GraphManager<N, E> {

  public enum RouteType {
    SHORTEST_ROUTE,
    FORWARD_TRACE,
    BACKWARD_TRACE
  }

  private ILcdGraph<N, E> fGraph;

  // Algorithms
  private ILcdShortestRouteAlgorithm fShortestRouteAlgorithm;
  private ILcdTracingAlgorithm fTracingAlgorithm;

  // Algorithm parameters
  private ILcdEdgeValueFunction<N, E> fEdgeValueFunction;
  private ILcdDistanceFunction<N, E> fDistanceFunction;
  private ILcdShortestRouteDistanceTableProvider fShortestRouteDistanceTableProvider;
  private double fMaxDistance;

  // Route parameters
  private N fStartNode;
  private N fEndNode;
  private E fStartEdge;
  private E fEndEdge;

  // Route results
  private List<ILcdRoute<N, E>> fRoutes = new ArrayList<ILcdRoute<N, E>>();
  private Map<ILcdRoute<N, E>, Double> fRouteDistances = new HashMap<ILcdRoute<N, E>, Double>();
  private RouteType fRouteType;

  // Listeners
  private List<GraphParameterChangedListener> fGraphParameterChangedListeners = new ArrayList<GraphParameterChangedListener>();

  /**
   * Constructs a new {@code GraphManager}, without a graph.
   */
  public GraphManager() {
  }

  /**
   * Constructs a new {@code GraphManager} with the specified graph.
   *
   * @param aGraph the graph to be managed by this class.
   */
  public GraphManager(ILcdGraph<N, E> aGraph) {
    fGraph = aGraph;
  }

  /**
   * Returns the graph on which all network computations managed by this class are performed.
   *
   * @return the graph that is managed by this class.
   */
  public ILcdGraph<N, E> getGraph() {
    return fGraph;
  }

  /**
   * Sets the graph on which all network computations managed by this class are to be performed.
   *
   * @param aGraph the graph to be managed by this class.
   */
  public void setGraph(ILcdGraph<N, E> aGraph) {
    Object old_graph = fGraph;
    fGraph = aGraph;
    notifyListeners(GraphParameterChangedListener.GraphParameter.GRAPH, old_graph, aGraph);
  }

  // Routing results

  /**
   * Returns all routes which are currently active.
   *
   * @return all routes which are currently active.
   */
  public List<ILcdRoute<N, E>> getRoutes() {
    return fRoutes;
  }

  /**
   * Returns the distances of all routes which are currently active.
   *
   * @return the distances of all routes which are currently active.
   */
  public Map<ILcdRoute<N, E>, Double> getRouteDistances() {
    return fRouteDistances;
  }

  /**
   * Returns the type of the routes that are currently active.
   *
   * @return the type of the routes that are currently active.
   */
  public RouteType getRouteType() {
    return fRouteType;
  }

  /**
   * Clears the list with routes which are currently active.
   */
  public void clearRoutes() {
    setRoutes(null, null);
  }

  // Algorithms and parameters.

  /**
   * Returns the routing algorithm used by this graph manager.
   *
   * @return the routing algorithm used by this graph manager.
   */
  public ILcdShortestRouteAlgorithm getShortestRouteAlgorithm() {
    return fShortestRouteAlgorithm;
  }

  /**
   * Sets the routing algorithm to be used by this graph manager.
   *
   * @param aShortestRouteAlgorithm the routing algorithm to be used by this graph manager.
   */
  public void setShortestRouteAlgorithm(ILcdShortestRouteAlgorithm aShortestRouteAlgorithm) {
    fShortestRouteAlgorithm = aShortestRouteAlgorithm;
  }

  /**
   * Returns the tracing algorithm used by this graph manager.
   *
   * @return the tracing algorithm used by this graph manager.
   */
  public ILcdTracingAlgorithm getTracingAlgorithm() {
    return fTracingAlgorithm;
  }

  /**
   * Sets the tracing algorithm to be used by this graph manager.
   *
   * @param aTracingAlgorithm the tracing algorithm to be used by this graph manager.
   */
  public void setTracingAlgorithm(ILcdTracingAlgorithm aTracingAlgorithm) {
    fTracingAlgorithm = aTracingAlgorithm;
  }

  /**
   * Returns the edge value function used for evaluating the cost of edges.
   *
   * @return the edge value function used for evaluating the cost of edges.
   */
  public ILcdEdgeValueFunction<N, E> getEdgeValueFunction() {
    return fEdgeValueFunction;
  }

  /**
   * Sets the edge value function to be used for evaluating the cost of edges.
   *
   * @param aEdgeValueFunction the edge value function to be used for evaluating the cost of edges.
   */
  public void setEdgeValueFunction(ILcdEdgeValueFunction<N, E> aEdgeValueFunction) {
    ILcdEdgeValueFunction oldFunction = fEdgeValueFunction;
    fEdgeValueFunction = aEdgeValueFunction;
    clearRoutes();
    notifyListeners(GraphParameterChangedListener.GraphParameter.EDGE_VALUE_FUNCTION, oldFunction, fEdgeValueFunction);
  }

  /**
   * Returns the heuristic estimate function used for optimizing the routing algorithms.
   *
   * @return the heuristic estimate function used for optimizing the routing algorithms.
   */
  public ILcdDistanceFunction<N, E> getHeuristicEstimateFunction() {
    return fDistanceFunction;
  }

  /**
   * Sets the edge value function to be used for optimizing the routing algorithms.
   *
   * @param aDistanceFunction the edge value function to be used for optimizing the routing algorithms.
   */
  public void setHeuristicEstimateFunction(ILcdDistanceFunction<N, E> aDistanceFunction) {
    fDistanceFunction = aDistanceFunction;
  }

  /**
   * Returns the maximum distance of traces to search for.
   *
   * @return the maximum distance of traces to search for
   */
  public double getMaximumTracingDistance() {
    return fMaxDistance;
  }

  /**
   * Sets the maximum distance of traces to search for.
   *
   * @param aMaxDistance the maximum distance of traces to search for.
   */
  public void setMaximumTracingDistance(double aMaxDistance) {
    double oldMaxDistance = fMaxDistance;
    fMaxDistance = aMaxDistance;
    clearRoutes();
    notifyListeners(MAX_TRACING_DISTANCE, oldMaxDistance, aMaxDistance);
  }

  /**
   * Returns the shortest route distance table provider that can be used for optimizing the
   * routing algorithms.
   *
   * @return the shortest route distance table provider that can be used for optimizing the
   *         routing algorithms.
   */
  public ILcdShortestRouteDistanceTableProvider getShortestRouteDistanceTableProvider() {
    return fShortestRouteDistanceTableProvider;
  }

  /**
   * Sets the shortest route distance table provider to be used for optimizing the
   * routing algorithms.
   *
   * @param aShortestRouteDistanceTableProvider
   *         the shortest route distance table provider to be
   *         used for optimizing the routing algorithms.
   */
  public void setShortestRouteDistanceTableProvider(ILcdShortestRouteDistanceTableProvider aShortestRouteDistanceTableProvider) {
    fShortestRouteDistanceTableProvider = aShortestRouteDistanceTableProvider;
  }

  public void updateDistanceTables() {
    if (fGraph != null) {
      TLcdPartitionedShortestRoutePreprocessor preprocessor = new TLcdPartitionedShortestRoutePreprocessor();
      ILcdShortestRouteDistanceTableProvider distanceTableProvider = preprocessor.preprocess(
          (ILcdPartitionedGraph) fGraph, fEdgeValueFunction, fDistanceFunction);
      setShortestRouteDistanceTableProvider(distanceTableProvider);
    }
  }

  /**
   * Returns the start node to be used for routing and tracing algorithms.
   *
   * @return the start node to be used for routing and tracing algorithms.
   */
  public N getStartNode() {
    return fStartNode;
  }

  /**
   * Returns {@code true} if the specified node can be set as start node, {@code false} otherwise.
   *
   * @param aStartNode the node to be verified.
   *
   * @return {@code true} if the specified node can be set as start node, {@code false} otherwise.
   */
  public boolean canSetStartNode(N aStartNode) {
    return fGraph.containsNode(aStartNode) &&
           (fStartEdge == null || fGraph.isConnected(fStartEdge, aStartNode));
  }

  /**
   * Sets the start node to be used for routing and tracing algorithms.
   *
   * @param aStartNode the start node to be used for routing and tracing algorithms.
   */
  public void setStartNode(N aStartNode) {
    N oldStartNode = fStartNode;
    fStartNode = aStartNode;
    clearRoutes();
    notifyListeners(GraphParameterChangedListener.GraphParameter.START_NODE, oldStartNode, aStartNode);
  }

  /**
   * Returns the end node to be used for routing and tracing algorithms.
   *
   * @return the end node to be used for routing and tracing algorithms.
   */
  public N getEndNode() {
    return fEndNode;
  }

  /**
   * Returns {@code true} if the specified node can be set as end node, {@code false} otherwise.
   *
   * @param aEndNode the node to be verified.
   *
   * @return {@code true} if the specified node can be set as end node, {@code false} otherwise.
   */
  public boolean canSetEndNode(N aEndNode) {
    return fGraph.containsNode(aEndNode) &&
           (fEndEdge == null || fGraph.isConnected(fEndEdge, aEndNode));
  }

  /**
   * Sets the end node to be used for routing and tracing algorithms.
   *
   * @param aEndNode the end node to be used for routing and tracing algorithms.
   */
  public void setEndNode(N aEndNode) {
    N oldEndNode = fEndNode;
    fEndNode = aEndNode;
    clearRoutes();
    notifyListeners(END_NODE, oldEndNode, aEndNode);
  }

  /**
   * Returns the start edge to be used for routing and tracing algorithms.
   *
   * @return the start edge to be used for routing and tracing algorithms.
   */
  public E getStartEdge() {
    return fStartEdge;
  }

  /**
   * Returns {@code true} if the specified edge can be set as start edge, {@code false} otherwise.
   *
   * @param aStartEdge the node to be verified.
   *
   * @return {@code true} if the specified edge can be set as start edge, {@code false} otherwise.
   */
  public boolean canSetStartEdge(E aStartEdge) {
    return fGraph.containsEdge(aStartEdge) &&
           (fStartNode == null || fGraph.isConnected(aStartEdge, fStartNode));
  }

  /**
   * Sets the start edge to be used for routing and tracing algorithms.
   *
   * @param aStartEdge the start edge to be used for routing and tracing algorithms.
   */
  public void setStartEdge(E aStartEdge) {
    E oldStartEdge = fStartEdge;
    fStartEdge = aStartEdge;
    clearRoutes();
    notifyListeners(START_EDGE, oldStartEdge, aStartEdge);
  }

  /**
   * Returns the ebd edge to be used for routing and tracing algorithms.
   *
   * @return the ebd edge to be used for routing and tracing algorithms.
   */
  public E getEndEdge() {
    return fEndEdge;
  }

  /**
   * Returns {@code true} if the specified edge can be set as ebd edge, {@code false} otherwise.
   *
   * @param aEndEdge the node to be verified.
   *
   * @return {@code true} if the specified edge can be set as ebd edge, {@code false} otherwise.
   */
  public boolean canSetEndEdge(E aEndEdge) {
    return fGraph.containsEdge(aEndEdge) &&
           (fEndNode == null || fGraph.isConnected(aEndEdge, fEndNode));
  }

  /**
   * Sets the ebd edge to be used for routing and tracing algorithms.
   *
   * @param aEndEdge the ebd edge to be used for routing and tracing algorithms.
   */
  public void setEndEdge(E aEndEdge) {
    E oldEndEdge = fEndEdge;
    fEndEdge = aEndEdge;
    clearRoutes();
    notifyListeners(END_EDGE, oldEndEdge, aEndEdge);
  }

  // Graph modifications.

  public boolean canDestroyEdge(E aEdge) {
    // Only supported in numeric graphs.
    throw new UnsupportedOperationException();
  }

  public void destroyEdge(E aEdge) {
    // Only supported in numeric graphs.
    throw new UnsupportedOperationException();
  }

  // Computations.

  /**
   * Computs the shortest route using the parameters (graph, start/end node, algorithm, cost function, ...)
   * that are currently configured on this graph manager.
   */
  public void computeShortestRoute() {
    if (fShortestRouteAlgorithm instanceof TLcdPartitionedShortestRouteAlgorithm) {
      ((TLcdPartitionedShortestRouteAlgorithm) fShortestRouteAlgorithm).setDistanceTableProvider(fShortestRouteDistanceTableProvider);
    }
    ILcdRoute<N, E> shortestRoute = fShortestRouteAlgorithm.getShortestRoute(fGraph,
                                                                             getStartRoute(),
                                                                             getEndRoute(),
                                                                             fEdgeValueFunction,
                                                                             fDistanceFunction);
    fRouteType = RouteType.SHORTEST_ROUTE;
    setRoute(shortestRoute);
  }

  /**
   * Computs all forward traces using the parameters (graph, start node, algorithm, cost function, ...)
   * that are currently configured on this graph manager.
   */
  public void computeForwardTraces() {
    List<ILcdRoute<N, E>> traces = new ArrayList<ILcdRoute<N, E>>();
    Map<ILcdRoute<N, E>, Double> distances = new HashMap<ILcdRoute<N, E>, Double>();
    fTracingAlgorithm.getSuccessors(fGraph, getStartRoute(),
                                    fEdgeValueFunction,
                                    new TracingResultHandler<N, E>(traces, distances),
                                    fMaxDistance);
    fRouteType = RouteType.FORWARD_TRACE;
    setRoutes(traces, distances);
  }

  /**
   * Computs all backward traces using the parameters (graph, end node, algorithm, cost function, ...)
   * that are currently configured on this graph manager.
   */
  public void computeBackwardTraces() {
    List<ILcdRoute<N, E>> traces = new ArrayList<ILcdRoute<N, E>>();
    Map<ILcdRoute<N, E>, Double> distances = new HashMap<ILcdRoute<N, E>, Double>();
    fTracingAlgorithm.getPredecessors(fGraph, getEndRoute(),
                                      fEdgeValueFunction,
                                      new TracingResultHandler<N, E>(traces, distances),
                                      fMaxDistance);
    fRouteType = RouteType.BACKWARD_TRACE;
    setRoutes(traces, distances);
  }

  // Listener management.

  /**
   * Registers a listener on this graph manager whenever a parameter of this manager changes.
   *
   * @param aListener the listener to be registered.
   */
  public void addGraphParameterChangedListener(GraphParameterChangedListener aListener) {
    fGraphParameterChangedListeners.add(aListener);
  }

  // Private helper methods.

  private void setRoute(ILcdRoute<N, E> aRoute) {
    if (aRoute == null) {
      setRoutes(null, null);
    } else {
      List<ILcdRoute<N, E>> routes = new ArrayList<ILcdRoute<N, E>>();
      routes.add(aRoute);
      Map<ILcdRoute<N, E>, Double> routeDistances = new HashMap<ILcdRoute<N, E>, Double>();
      routeDistances.put(aRoute, TLcdRouteUtil.computeValue(aRoute, fGraph, getStartRoute(), fEdgeValueFunction));
      setRoutes(routes, routeDistances);
    }
  }

  private void setRoutes(List<ILcdRoute<N, E>> aTraces,
                         Map<ILcdRoute<N, E>, Double> aRouteDistances) {
    List<ILcdRoute<N, E>> oldRoutes = fRoutes;
    fRoutes = aTraces;
    fRouteDistances = aRouteDistances;
    notifyListeners(ROUTES, oldRoutes, fRoutes);
  }

  protected TLcdRoute<N, E> getStartRoute() {
    TLcdRoute<N, E> startRoute = new TLcdRoute<N, E>(fGraph, fStartNode);
    if (fStartEdge != null) {
      startRoute.addEdgeAtStart(fStartEdge);
    }
    return startRoute;
  }

  protected TLcdRoute<N, E> getEndRoute() {
    TLcdRoute<N, E> endRoute = new TLcdRoute<N, E>(fGraph, fEndNode);
    if (fEndEdge != null) {
      endRoute.addEdgeAtEnd(fEndEdge);
    }
    return endRoute;
  }

  protected void notifyListeners(GraphParameterChangedListener.GraphParameter aParameter, Object aOldValue, Object aNewValue) {
    for (GraphParameterChangedListener listener : fGraphParameterChangedListeners) {
      listener.graphParameterChanged(aParameter, aOldValue, aNewValue);
    }
  }

  /**
   * Tracing result handler storing all results in the specified list and map.
   *
   * @param <N>
   * @param <E>
   */
  private static class TracingResultHandler<N, E> implements ILcdTracingResultHandler<N, E> {

    private List<ILcdRoute<N, E>> fTraces;
    private Map<ILcdRoute<N, E>, Double> fDistances;

    private TracingResultHandler(List<ILcdRoute<N, E>> aTraces,
                                 Map<ILcdRoute<N, E>, Double> aDistances) {
      fTraces = aTraces;
      fDistances = aDistances;
    }

    public void handleNode(N aNode, ILcdRoute<N, E> aShortestRoute, double aDistance) {
      fTraces.add(aShortestRoute);
      fDistances.put(aShortestRoute, aDistance);
    }
  }

}
