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

import java.util.Enumeration;
import java.util.Vector;

import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.function.TLcdCompositeEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.ILcdRoute;

/**
 * A sample composite <code>ILcdEdgeValueFunction</code>.
 */
public class SampleCompositeFunction implements ILcdEdgeValueFunction {

  public static final int CARTESIAN = 0;
  public static final int GEODETIC = 1;
  public static final int CONSTANT = 2;
  public static final int TIMECOST = 3;
  public static final int NONE = 4;
  public static final int CUSTOM = 5;

  // Edge functions
  private CartesianEdgeValueFunction cartesian_edge_function = new CartesianEdgeValueFunction();
  private GeodeticEdgeValueFunction geodetic_edge_function = new GeodeticEdgeValueFunction();
  private ConstantEdgeValueFunction constant_edge_function = new ConstantEdgeValueFunction(1);
  private TimeCostEdgeValueFunction timecost_edge_Value_function = new TimeCostEdgeValueFunction(cartesian_edge_function);
  private DirectedEdgeValueFunction directed_edge_function = new DirectedEdgeValueFunction();

  // Node functions
  private ConstantNodeValueFunction constant_node_function = new ConstantNodeValueFunction(1);

  // Turn functions
  private TurnValueFunction turn_function = new TurnValueFunction(0);
  private ConstantTurnValueFunction constant_turn_function = new ConstantTurnValueFunction(0);

  private TLcdCompositeEdgeValueFunction fEdgeValueFunction;
  private ILcdEdgeValueFunction[] fEdgeValueFunctionsArray = new ILcdEdgeValueFunction[4];

  private int fEdgeFunctionMode = CARTESIAN;
  private int fNodeFunctionMode = CONSTANT;

  private Vector fEdgeValueFunctionChangedListeners = new Vector();

  public SampleCompositeFunction() {
    fEdgeValueFunctionsArray[0] = cartesian_edge_function;
    fEdgeValueFunctionsArray[1] = directed_edge_function;
    fEdgeValueFunctionsArray[2] = constant_node_function;
    fEdgeValueFunctionsArray[3] = turn_function;

    fEdgeValueFunction = new TLcdCompositeEdgeValueFunction(fEdgeValueFunctionsArray);

  }

  public void setEdgeFunctionMode(int aMode) {
    fEdgeFunctionMode = aMode;
    switch (aMode) {
    case CARTESIAN:
      fEdgeValueFunctionsArray[0] = cartesian_edge_function;
      break;
    case GEODETIC:
      fEdgeValueFunctionsArray[0] = geodetic_edge_function;
      break;
    case CONSTANT:
      fEdgeValueFunctionsArray[0] = constant_edge_function;
      break;
    case TIMECOST:
      fEdgeValueFunctionsArray[0] = timecost_edge_Value_function;
      break;
    }

    fEdgeValueFunction = new TLcdCompositeEdgeValueFunction(fEdgeValueFunctionsArray);
    notifyEdgeValueFunctionChangedListeners();
  }

  public void setNodeFunctionMode(int aMode) {
    fNodeFunctionMode = aMode;
    switch (aMode) {
    case CONSTANT:
      constant_node_function.setConstant(1);
      break;
    case NONE:
      constant_node_function.setConstant(0);
      break;
    }
    notifyEdgeValueFunctionChangedListeners();
  }

  public void setTurnFunctionMode(int aMode) {
    switch (aMode) {
    case CUSTOM:
      fEdgeValueFunctionsArray[3] = turn_function;
      break;
    case NONE:
      fEdgeValueFunctionsArray[3] = constant_turn_function;
      break;
    }
    fEdgeValueFunction = new TLcdCompositeEdgeValueFunction(fEdgeValueFunctionsArray);

    notifyEdgeValueFunctionChangedListeners();
  }

  public int getEdgeFunctionMode() {
    return fEdgeFunctionMode;
  }

  public int getNodeFunctionMode() {
    return fNodeFunctionMode;
  }

  public int getTurnFunctionMode() {
    return fNodeFunctionMode;
  }

  public void setEdgeConstant(double aConstant) {
    constant_edge_function.setConstant(aConstant);
    notifyEdgeValueFunctionChangedListeners();
  }

  public void setNodeConstant(double aConstant) {
    constant_node_function.setConstant(aConstant);
    notifyEdgeValueFunctionChangedListeners();
  }

  public void setTurnFunction(TurnValueFunction aTurnFunction) {
    turn_function = aTurnFunction;
    fEdgeValueFunctionsArray[3] = turn_function;
    fEdgeValueFunction = new TLcdCompositeEdgeValueFunction(fEdgeValueFunctionsArray);
  }

  // Implementations for ILcdEdgeValueFunction

  public double computeEdgeValue(ILcdGraph aGraph, ILcdRoute aPrecedingRoute, Object aEdge, TLcdTraversalDirection aTraversalDirection) {
    return fEdgeValueFunction.computeEdgeValue(aGraph, aPrecedingRoute, aEdge, aTraversalDirection);
  }

  public int getOrder() {
    return fEdgeValueFunction.getOrder();
  }

  // EdgeValueFunctionChanged listeners

  public void addEdgeValueFunctionChangedListener(EdgeValueFunctionChangedListener aListener) {
    fEdgeValueFunctionChangedListeners.add(aListener);
  }

  private void notifyEdgeValueFunctionChangedListeners() {
    for (Enumeration listeners = fEdgeValueFunctionChangedListeners.elements(); listeners.hasMoreElements(); ) {
      ((EdgeValueFunctionChangedListener) listeners.nextElement()).edgeValueFunctionChanged();
    }
  }

}
