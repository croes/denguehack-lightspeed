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
package samples.network.basic.graph;

import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdGraph;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.util.ILcdFireEventMode;

/**
 * This class can generate an <code>ILcdGraph</code> from an
 * <code>ILcdModel</code>. All <code>ILcdPoint</code> objects in the model's
 * element list are interpreted as nodes, all <code>ILcdPolyline</code> objects
 * are interpreted as edges. The first and last point in the polyline's
 * pointlist are interpreted as the nodes connected by that polyline/edge.
 */
public class GraphFactory {

  /**
   * Creates a graph of the given model.
   *
   * @param aModel
   * @return
   * @throws NullPointerException if the given model is <code>null</code>.
   */
  public ILcdGraph createGraph(ILcdModel aModel) {

    TLcdGraph graph = new TLcdGraph();

    // Adding nodes
    for (Enumeration elements = aModel.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdPoint) {
        graph.addNode(element, ILcdFireEventMode.NO_EVENT);
      }
    }

    // Adding edges
    for (Enumeration elements = aModel.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdPolyline) {
        ILcdPoint start_node = ((ILcdPolyline) element).getPoint(0);
        ILcdPoint end_node = ((ILcdPolyline) element).getPoint(((ILcdPolyline) element).getPointCount() - 1);

        if (!graph.containsNode(start_node)) {
          graph.addNode(start_node, ILcdFireEventMode.NO_EVENT);
        }
        if (!graph.containsNode(end_node)) {
          graph.addNode(end_node, ILcdFireEventMode.NO_EVENT);
        }

        graph.addEdge(element, start_node, end_node, ILcdFireEventMode.NO_EVENT);
      }
    }

    return graph;
  }

}
