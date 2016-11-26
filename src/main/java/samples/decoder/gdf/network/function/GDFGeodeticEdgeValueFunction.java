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
package samples.decoder.gdf.network.function;

import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.ILcdRoute;

import samples.network.basic.function.GeodeticEdgeValueFunction;
import samples.network.numeric.preprocessor.ISimpleEdgeValueFunction;

/**
 * This sample function returns for each GDF Line Feature its geodetic lenth
 * (i.e. the sum of the geodetic lengths of its constituing edges), or
 * Double.POSITIVE_INFINITY if traffic is not allowed in the given direction.
 */
public class GDFGeodeticEdgeValueFunction implements ILcdEdgeValueFunction, ISimpleEdgeValueFunction {

  private GDFDirectionOfFlowEdgeValueFunction fDirectionFunction = new GDFDirectionOfFlowEdgeValueFunction();
  private GeodeticEdgeValueFunction fGeodeticEdgeValueFunction = new GeodeticEdgeValueFunction();

  public double computeEdgeValue(ILcdGraph aGraph, ILcdRoute aRoute, Object aNextEdge, TLcdTraversalDirection aDirection)
      throws IllegalArgumentException {

    return Math.max(fDirectionFunction.computeEdgeValue(aGraph, aRoute, aNextEdge, aDirection),
                    fGeodeticEdgeValueFunction.computeEdgeValue(aGraph, aRoute, aNextEdge, aDirection));
  }

  public double getEdgeValue(Object aEdge, TLcdTraversalDirection aTraversalDirection) {
    return fGeodeticEdgeValueFunction.getEdgeValue(aEdge, aTraversalDirection);
  }

  public int getOrder() {
    return 0;
  }

}
