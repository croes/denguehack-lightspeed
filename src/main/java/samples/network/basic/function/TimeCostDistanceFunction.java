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

import com.luciad.network.function.ILcdDistanceFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.ILcdRoute;

/**
 * An <code>ILcdDistanceFunction</code> that is compatible with
 * <code>TimeCostEdgeValueFunction</code>.
 */
public class TimeCostDistanceFunction implements ILcdDistanceFunction {

  private ILcdDistanceFunction fDistanceFunction;
  private double fMaxSpeed;

  public TimeCostDistanceFunction(ILcdDistanceFunction aDistanceFunction, double aMaxSpeed) {
    fDistanceFunction = aDistanceFunction;
    fMaxSpeed = aMaxSpeed;
  }

  public double computeDistance(ILcdGraph aGraph,
                                ILcdRoute aPreceedingRoute,
                                ILcdRoute aSucceedingRoute, TLcdTraversalDirection aTraversalDirection) throws IllegalArgumentException,
                                                                                                               NullPointerException {
    return fDistanceFunction.computeDistance(aGraph, aPreceedingRoute, aSucceedingRoute, aTraversalDirection) / fMaxSpeed;

  }

  public int getOrder() {
    return 0;
  }
}
