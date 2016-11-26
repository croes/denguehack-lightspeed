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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.ILcdOutputStreamFactoryCapable;
import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.network.graph.ILcdGraph;

/**
 * Implementation of {@code com.luciad.network.graph.numeric.ILcdNumericGraphMappingHandler} that
 * writes all mappings between nodes/edges and id's to disk in a very simple sequence of int - long
 * pairs.
 * <p/>
 * This implementation expects all nodes to be {@code Integer} objects and all edges to be
 * {@code ValuedEdgge} objects.
 */
public class IntegerIdNumericGraphMappingHandler extends AIdNumericGraphMappingHandler<Integer, Integer>
    implements ILcdOutputStreamFactoryCapable {

  private ILcdOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();
  private DataOutputStream fNodeMap;
  private DataOutputStream fEdgeMap;
  private DataOutputStream fInverseEdgeMap;

  /**
   * Creates a new mapping handler instance that will write all node and edge mappings to the
   * specified destinations.
   *
   * @param aNodeMapDestination the node destination name.
   * @param aEdgeMapDestination the edge destination name.
   *
   * @throws IOException if an I/O exception occurs while creating the output streams.
   */
  public IntegerIdNumericGraphMappingHandler(String aNodeMapDestination,
                                             String aEdgeMapDestination,
                                             String aInverseEdgeMapDestination) throws IOException {
    fNodeMap = new DataOutputStream(new BufferedOutputStream(
        fOutputStreamFactory.createOutputStream(aNodeMapDestination)));
    fEdgeMap = new DataOutputStream(new BufferedOutputStream(
        fOutputStreamFactory.createOutputStream(aEdgeMapDestination)));
    fInverseEdgeMap = new DataOutputStream(new BufferedOutputStream(
        fOutputStreamFactory.createOutputStream(aInverseEdgeMapDestination)));
  }

  public void setOutputStreamFactory(ILcdOutputStreamFactory aOutputStreamFactory) {
    fOutputStreamFactory = aOutputStreamFactory;
  }

  public ILcdOutputStreamFactory getOutputStreamFactory() {
    return fOutputStreamFactory;
  }

  /**
   * Closes the node and edge output streams.
   *
   * @throws IOException if an I/O exception occurs while closing the output streams.
   */
  public void close() throws IOException {
    fNodeMap.close();
    fEdgeMap.close();
    fInverseEdgeMap.close();
  }

  // Implementations for ILcdNumericGraphMappingHandler.

  public void mapGraph(ILcdGraph aGraph, long aNumericId) {
    // Do nothing.
  }

  public void mapNodeId(Integer aNode, long aMappedId) {
    try {
      fNodeMap.writeInt(aNode);
      fNodeMap.writeLong(aMappedId);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void mapEdgeId(Integer aEdge, long aMappedId) {
    try {
      fEdgeMap.writeInt(aEdge);
      fEdgeMap.writeLong(aMappedId);
      fInverseEdgeMap.writeLong(aMappedId);
      fInverseEdgeMap.writeInt(aEdge);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void mapBoundaryEdgeId(Integer aEdge, long aMappedId) {
    try {
      fEdgeMap.writeInt(aEdge);
      fEdgeMap.writeLong(aMappedId);
      fInverseEdgeMap.writeLong(aMappedId);
      fInverseEdgeMap.writeInt(aEdge);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
