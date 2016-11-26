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
package samples.network.numeric.preprocessor;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.shape.ILcdPoint;

/**
 * Extension of {@code AIdGraphFactory} that works with {@code ILcdDataObject}.
 * <p/>
 * Edge objects should have the following properties:
 * <ul>
 * <li>the unique id of the edge within the graph (Integer)</li>
 * <li>the start node id of the edge (Integer)</li>
 * <li>the end node id of the edge (Integer)</li>
 * </ul>
 * <p/>
 * Nodes should have the following properties:
 * <ul>
 * <li>the unique id of the node within the graph (Integer)</li>
 * </ul>
 * <p/>
 * Elements implementing {@code ILcdPoint} are considered nodes, all other elements are
 * considered edges.
 */
public class DataObjectIdGraphFactory extends AIdGraphFactory {

  private int fNodeIdIndex;
  private int fEdgeIdIndex;
  private int fEdgeStartNodeIndex;
  private int fEdgeEndNodeIndex;

  public DataObjectIdGraphFactory(int aNodeIdIndex,
                                  int aEdgeIdIndex,
                                  int aEdgeStartNodeIndex,
                                  int aEdgeEndNodeIndex,
                                  ISimpleEdgeValueFunction aEdgeValueFunction) {
    super(aEdgeValueFunction);
    fNodeIdIndex = aNodeIdIndex;
    fEdgeIdIndex = aEdgeIdIndex;
    fEdgeStartNodeIndex = aEdgeStartNodeIndex;
    fEdgeEndNodeIndex = aEdgeEndNodeIndex;
  }

  protected boolean isNode(Object aObject) {
    return aObject instanceof ILcdPoint;
  }

  protected boolean isEdge(Object aObject) {
    return !(aObject instanceof ILcdPoint);
  }

  protected Object getNodeId(Object aNativeNode) {
    return ((ILcdDataObject) aNativeNode).getValue(((ILcdDataObject) aNativeNode).getDataType().getProperties().get(fNodeIdIndex));
  }

  protected Object getEdgeId(Object aNativeEdge) {
    return ((ILcdDataObject) aNativeEdge).getValue(((ILcdDataObject) aNativeEdge).getDataType().getProperties().get(fEdgeIdIndex));
  }

  protected Object getStartNodeId(Object aNativeEdge) {
    return ((ILcdDataObject) aNativeEdge).getValue(((ILcdDataObject) aNativeEdge).getDataType().getProperties().get(fEdgeStartNodeIndex));
  }

  protected Object getEndNodeId(Object aNativeEdge) {
    return ((ILcdDataObject) aNativeEdge).getValue(((ILcdDataObject) aNativeEdge).getDataType().getProperties().get(fEdgeEndNodeIndex));
  }

}
