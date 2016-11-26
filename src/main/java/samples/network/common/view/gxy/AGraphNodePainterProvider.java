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

import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

import samples.network.common.graph.GraphManager;

/**
 * Abstract super class for painter providers of nodes in graphs.
 * It provides functionality for detecting whether a node is a normal node, start node or end node.
 */
public abstract class AGraphNodePainterProvider implements ILcdGXYPainterProvider {

  public enum GraphNodeMode {
    NORMAL_NODE,
    START_NODE,
    END_NODE,
  }

  private GraphManager fGraphManager;
  private EnumSet<GraphNodeMode> fModes;

  protected AGraphNodePainterProvider() {
  }

  protected AGraphNodePainterProvider(GraphManager aGraphManager,
                                      EnumSet<GraphNodeMode> aModes) {
    fGraphManager = aGraphManager;
    fModes = aModes;
  }

  public GraphManager getGraphManager() {
    return fGraphManager;
  }

  // Implementations for ILcdGXYPainterProvider.

  public Object clone() {
    return null;
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    GraphNodeMode mode = getGraphNodeMode(aObject);
    return mode != null ? getGXYPainter(aObject, mode) : null;
  }

  /**
   * Returns a painter for the specified node, that can render the node in the specified mode.
   *
   * @param aNode the node for which to return a painter.
   * @param aMode the mode in which to render the edge.
   * @return a painter that can render the node in the specified mode.
   */
  protected abstract ILcdGXYPainter getGXYPainter(Object aNode,
                                                  GraphNodeMode aMode);

  private GraphNodeMode getGraphNodeMode(Object aNode) {
    if (fGraphManager == null) {
      return GraphNodeMode.NORMAL_NODE;
    }
    if (fGraphManager.getStartNode() == aNode && fModes.contains(GraphNodeMode.START_NODE)) {
      return GraphNodeMode.START_NODE;
    } else if (fGraphManager.getEndNode() == aNode && fModes.contains(GraphNodeMode.END_NODE)) {
      return GraphNodeMode.END_NODE;
    } else if (fModes.contains(GraphNodeMode.NORMAL_NODE)) {
      return GraphNodeMode.NORMAL_NODE;
    } else {
      return null;
    }
  }

}
