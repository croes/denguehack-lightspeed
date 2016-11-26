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
package samples.network.common.view.gxy;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

import samples.network.common.graph.GraphManager;
import samples.network.common.graph.GraphParameterChangedListener;

/**
 * Abstract super class for painter providers of edges in graphs.
 * It provides functionality for detecting whether an edge is a normal edge, start edge, end edge,
 * route edge or destroyed edge.
 */
public abstract class AGraphEdgePainterProvider implements ILcdGXYPainterProvider {

  public enum GraphEdgeMode {
    NORMAL_EDGE,
    START_EDGE,
    END_EDGE,
    ROUTE_EDGE,
    DESTROYED_EDGE,
  }

  protected GraphManager fGraphManager;
  private EnumSet<GraphEdgeMode> fModes;
  protected Set<Object> fRouteEdges = new HashSet<Object>();
  protected Set<Object> fDestroyedEdges = new HashSet<Object>();
  private String fIdPropertyName;

  protected AGraphEdgePainterProvider() {
  }

  protected AGraphEdgePainterProvider(GraphManager aGraphManager,
                                      EnumSet<GraphEdgeMode> aModes,
                                      String aIdPropertyName) {
    fGraphManager = aGraphManager;
    fModes = aModes;
    fIdPropertyName = aIdPropertyName;
    if (fGraphManager != null) {
      fGraphManager.addGraphParameterChangedListener(new GraphParameterChangedListener() {
        public void graphParameterChanged(GraphParameter aParameter, Object aOldValue, Object aNewValue) {
          if (aParameter == GraphParameter.EDGE_DESTROYED) {
            fDestroyedEdges.add(getIdentifier(aOldValue));
          }
          if (aParameter == GraphParameter.ROUTES) {
            clearRoutes();
            List<ILcdRoute> routes = (List<ILcdRoute>) aNewValue;
            if (routes != null) {
              for (ILcdRoute route : routes) {
                addRoute(route);
              }
            }
          }
        }
      });
    }
  }

  private Object getIdentifier(Object aEdge) {
    if (fIdPropertyName != null) {
      return ((ILcdDataObject) aEdge).getValue(fIdPropertyName);
    } else {
      return aEdge;
    }
  }

  public GraphManager getGraphManager() {
    return fGraphManager;
  }

  protected boolean isRouteEdge(Object aEdge) {
    return fRouteEdges.contains(getIdentifier(aEdge));
  }

  protected boolean isDestroyedEdge(Object aEdge) {
    return fDestroyedEdges.contains(getIdentifier(aEdge));
  }

  public void addRoute(ILcdRoute aRoute) {
    for (int i = 0; i < aRoute.getEdgeCount(); i++) {
      fRouteEdges.add(getIdentifier(aRoute.getEdge(i)));
    }
  }

  public void clearRoutes() {
    fRouteEdges.clear();
  }

  // Implementations for ILcdGXYPainterProvider.

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    GraphEdgeMode edgeMode = getGraphEdgeMode(aObject);
    return edgeMode != null ? getGXYPainter(aObject, edgeMode) : null;
  }

  /**
   * Returns a painter for the specified edge, that can render the edge in the specified mode.
   *
   * @param aEdge the edge for which to return a painter.
   * @param aMode the mode in which to render the edge.
   *
   * @return a painter that can render the edge in the specified mode.
   */
  protected abstract ILcdGXYPainter getGXYPainter(Object aEdge,
                                                  GraphEdgeMode aMode);

  private GraphEdgeMode getGraphEdgeMode(Object aEdge) {
    if (fGraphManager == null) {
      return GraphEdgeMode.NORMAL_EDGE;
    }
    if (fGraphManager.getStartEdge() == aEdge && fModes.contains(GraphEdgeMode.START_EDGE)) {
      return GraphEdgeMode.START_EDGE;
    } else if (fGraphManager.getEndEdge() == aEdge && fModes.contains(GraphEdgeMode.END_EDGE)) {
      return GraphEdgeMode.END_EDGE;
    } else if (isRouteEdge(aEdge) && fModes.contains(GraphEdgeMode.ROUTE_EDGE)) {
      return GraphEdgeMode.ROUTE_EDGE;
    } else if (isDestroyedEdge(aEdge) && fModes.contains(GraphEdgeMode.DESTROYED_EDGE)) {
      return GraphEdgeMode.DESTROYED_EDGE;
    } else if (fModes.contains(GraphEdgeMode.NORMAL_EDGE)) {
      return GraphEdgeMode.NORMAL_EDGE;
    }
    return null;
  }

}
