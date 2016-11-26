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
package samples.network.basic.function;

import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.ILcdRoute;

import samples.network.basic.graph.Edge;

/**
 * An <code>ILcdEdgeValueFunction</code> that returns for each edge the
 * time it takes to traverse the edge, based on its length and the maximum
 * speed that is allowed.
 */
public class TimeCostEdgeValueFunction implements ILcdEdgeValueFunction {

  private ILcdEdgeValueFunction fEdgeValueFunction;

  public TimeCostEdgeValueFunction(ILcdEdgeValueFunction aEdgeValueFunction) {
    fEdgeValueFunction = aEdgeValueFunction;
  }

  public double computeEdgeValue(ILcdGraph aGraph, ILcdRoute aRoute, Object aNextEdge, TLcdTraversalDirection aTraversalDirection)
      throws IllegalArgumentException {

    int max_speed = ((Edge) aNextEdge).getMaxSpeed();
    double distance = fEdgeValueFunction.computeEdgeValue(aGraph, aRoute, aNextEdge, aTraversalDirection);
    return distance / max_speed;
  }

  public int getOrder() {
    return 0;
  }

}
