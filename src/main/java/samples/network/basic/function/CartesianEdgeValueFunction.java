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

import com.luciad.network.function.ALcdSimpleEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.shape.ILcdPolyline;

/**
 * An <code>ILcdEdgeValueFunction</code> that returns for each edge the
 * sum of the cartesian lengths of the lines making up the edge.
 */
public class CartesianEdgeValueFunction extends ALcdSimpleEdgeValueFunction {

  public double computeEdgeValue(ILcdGraph aGraph, Object aEdge, TLcdTraversalDirection aTraversalDirection) {

    if (!(aEdge instanceof ILcdPolyline)) {
      throw new IllegalArgumentException("The edges must be ILcdPolyline");
    }

    double distance = 0;
    for (int j = 0; j < ((ILcdPolyline) aEdge).getPointCount() - 1; j++) {
      double x = ((ILcdPolyline) aEdge).getPoint(j).getX() - ((ILcdPolyline) aEdge).getPoint(j + 1).getX();
      double y = ((ILcdPolyline) aEdge).getPoint(j).getY() - ((ILcdPolyline) aEdge).getPoint(j + 1).getY();
      distance += Math.sqrt(x * x + y * y);
    }
    return distance;
  }

}
