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
package samples.network.basic;

import java.util.Vector;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdFireEventMode;

import samples.network.basic.function.TurnValueFunction;
import samples.network.basic.graph.Edge;
import samples.network.basic.graph.Node;

/**
 * An <code>ILcdModel</code> containing some <code>ILcdPoint</code>'s (nodes)
 * and <code>ILcdPolyline</code>'s (edges), forming a small sample graph. Each
 * edge has a flag indicating whether it is directed or not, and what its
 * maximum travel speed is. Information about turn restrictions is also
 * included.
 */
public class SampleGraphModel extends TLcd2DBoundsIndexedModel {

  private TurnValueFunction fTurnValueFunction = new TurnValueFunction(0);
  private Vector fConstraintedEdges = new Vector();

  public SampleGraphModel() {
    setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the sample graph",   // source name (is used as tooltip text)
        "SHP",            // type name
        "Sample Graph"    // display name
    ));
    setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    Node node0 = addNode(new TLcdLonLatPoint(0, 0), "n0");
    Node node1 = addNode(new TLcdLonLatPoint(0, 10), "n1");
    Node node2 = addNode(new TLcdLonLatPoint(10, 10), "n2");
    Node node3 = addNode(new TLcdLonLatPoint(0, -15), "n3");
    Node node4 = addNode(new TLcdLonLatPoint(10, -15), "n4");
    Node node5 = addNode(new TLcdLonLatPoint(10, 0), "n5");
    Node node6 = addNode(new TLcdLonLatPoint(20, 0), "n6");
    Node node7 = addNode(new TLcdLonLatPoint(20, 10), "n7");
    Node node8 = addNode(new TLcdLonLatPoint(30, 10), "n8");
    Node node9 = addNode(new TLcdLonLatPoint(40, 10), "n9");
    Node node10 = addNode(new TLcdLonLatPoint(50, 10), "n10");
    Node node11 = addNode(new TLcdLonLatPoint(50, 0), "n11");
    Node node12 = addNode(new TLcdLonLatPoint(20, -20), "n12");
    Node node13 = addNode(new TLcdLonLatPoint(50, -20), "n13");
    Node node14 = addNode(new TLcdLonLatPoint(60, 0), "n14");
    Node node15 = addNode(new TLcdLonLatPoint(30, 20), "n15");
    Node node16 = addNode(new TLcdLonLatPoint(20, -25), "n16");
    Node node17 = addNode(new TLcdLonLatPoint(10, -25), "n17");
    Node node18 = addNode(new TLcdLonLatPoint(10, -20), "n18");

    Edge edge0 = addEdge(node0, node1, true, 5, "e0");
    Edge edge1 = addEdge(node1, node2, true, 5, "e1");
    Edge edge2 = addEdge(node2, node5, true, 5, "e2");
    Edge edge3 = addEdge(node0, node3, true, 5, "e3");
    Edge edge4 = addEdge(node3, node4, true, 5, "e4");
    Edge edge5 = addEdge(node4, node5, true, 5, "e5");
    Edge edge6 = addEdge(node5, node6, true, 5, "e6");
    Edge edge7 = addEdge(node6, node7, true, 5, "e7");
    Edge edge15 = addEdge(node7, node8, false, 5, "e8");
    Edge edge8 = addEdge(node8, node9, false, 5, "e9");
    Edge edge16 = addEdge(node9, node10, false, 5, "e10");
    Edge edge9 = addEdge(node10, node11, false, 5, "e11");
    Edge edge10 = addEdge(node6, node12, false, 10, "e12");
    Edge edge11 = addEdge(node12, node13, false, 10, "e13");
    Edge edge12 = addEdge(node13, node11, false, 10, "e14");
    Edge edge13 = addEdge(node11, node14, false, 5, "e15");
    Edge edge14 = addEdge(node8, node15, false, 5, "e16");
    Edge edge17 = addEdge(node12, node16, false, 10, "e17");
    Edge edge18 = addEdge(node16, node17, false, 10, "e18");
    Edge edge19 = addEdge(node17, node18, false, 10, "e19");
    Edge edge20 = addEdge(node18, node12, false, 10, "e20");

    fTurnValueFunction.setTurnValue(edge9, node11, edge13, Double.POSITIVE_INFINITY);
    fTurnValueFunction.setTurnValue(edge13, node11, edge9, Double.POSITIVE_INFINITY);
    fConstraintedEdges.addElement(edge9);
    fConstraintedEdges.addElement(edge13);

    fTurnValueFunction.setTurnValue(edge14, node8, edge8, Double.POSITIVE_INFINITY);
    fTurnValueFunction.setTurnValue(edge8, node8, edge14, Double.POSITIVE_INFINITY);
    fConstraintedEdges.addElement(edge8);
    fConstraintedEdges.addElement(edge14);

    fTurnValueFunction.setTurnValue(edge6, node6, edge7, Double.POSITIVE_INFINITY);
    fTurnValueFunction.setTurnValue(edge7, node6, edge6, Double.POSITIVE_INFINITY);
    fConstraintedEdges.addElement(edge6);
    fConstraintedEdges.addElement(edge7);
  }

  /**
   * Returns all edges that are involved in turn restrictions.
   *
   * @return
   */
  public Vector getConstraintedEdges() {
    return fConstraintedEdges;
  }

  /**
   * Returns the turn value function associated with this graph model.
   *
   * @return
   */
  public TurnValueFunction getTurnValueFunction() {
    return fTurnValueFunction;
  }

  private Node addNode(TLcdLonLatPoint aLonLatPoint, String aName) {
    Node node = new Node(aLonLatPoint, aName);
    addElement(node, ILcdFireEventMode.NO_EVENT);
    return node;
  }

  private Edge addEdge(Node aNode1,
                       Node aNode2,
                       boolean aDirected,
                       int aMaxSpeed,
                       String aName) {

    TLcdLonLatPoint[] pointlist = {aNode1, aNode2};
    Edge edge = new Edge(new TLcd2DEditablePointList(pointlist, false), aName, aDirected, aMaxSpeed);
    addElement(edge, ILcdFireEventMode.NO_EVENT);
    return edge;
  }

}
