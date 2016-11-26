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
package samples.common.layerControls.swing.datatransfer;

import java.util.ArrayList;

import javax.swing.tree.TreeModel;

import com.luciad.view.ILcdLayer;

/**
 * <p>Class containing all the data that is transferred when dragging and dropping a layer, with the
 * necessary getters and setters.</p>
 */
public class TransferableData {
  private ArrayList<ILcdLayer> fLayerNodes;
  private TreeModel fSourceTreeModel;

  /**
   * <p>Create a new data object for the nodes <code>aLayerNodes</code>.</p>
   * @param aLayerNodes the layer nodes which are dragged and dropped
   */
  public TransferableData(ArrayList<ILcdLayer> aLayerNodes) {
    fLayerNodes = aLayerNodes;
  }

  /**
   * <p>Create a new data object for the nodes <code>aLayerNodes</code></p>
   * @param aLayerNodes the layer nodes which are dragged and dropped
   * @param aSourceTreeModel the source model of the nodes
   */
  public TransferableData(ArrayList<ILcdLayer> aLayerNodes, TreeModel aSourceTreeModel) {
    fLayerNodes = aLayerNodes;
    fSourceTreeModel = aSourceTreeModel;
  }

  /**
   * <p>Returns all the layers which are dragged</p>
   * @return all the layers which are dragged
   */
  public ArrayList<ILcdLayer> getLayers() {
    return fLayerNodes;
  }

  /**
   * <p>Returns the source model of the dragged layers.</p>
   * @return the source model of the dragged layers
   */
  public TreeModel getSourceTreeModel() {
    return fSourceTreeModel;
  }
}
