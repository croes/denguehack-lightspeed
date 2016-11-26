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
package samples.network.crosscountry.graph;

import java.util.ArrayList;
import java.util.List;

import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYLine;

/**
 * An <code>ILcdRoute</code> that automatically generates the edges between subsequent nodes.
 */
public class ResultRoute implements ILcdRoute {

  private List<ILcd2DEditablePoint> fNodes;

  public ResultRoute() {
    fNodes = new ArrayList<ILcd2DEditablePoint>();
  }

  public ResultRoute(int aEstimatedNodeCount) {
    fNodes = new ArrayList<ILcd2DEditablePoint>(aEstimatedNodeCount);
  }

  public int getEdgeCount() {
    return getNodeCount() - 1;
  }

  public Object getEdge(int aIndex) {
    return new TLcdXYLine((ILcd2DEditablePoint) getNode(aIndex), (ILcd2DEditablePoint) getNode(aIndex + 1));
  }

  public void addNode(Object aNode) {
    fNodes.add((ILcd2DEditablePoint) aNode);
  }

  public int getNodeCount() {
    return fNodes.size();
  }

  public Object getNode(int aIndex) {
    return fNodes.get(aIndex);
  }

  public Object getStartNode() {
    return fNodes.get(0);
  }

  public Object getEndNode() {
    return fNodes.get(fNodes.size() - 1);
  }

  public String toString() {
    return fNodes.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ResultRoute that = (ResultRoute) o;

    return fNodes.equals(that.fNodes);
  }

  @Override
  public int hashCode() {
    return fNodes.hashCode();
  }

}

