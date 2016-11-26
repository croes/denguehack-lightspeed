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
package samples.common.layerControls.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.TLcdLayerTreeNodeUtil;

/**
 * <p>Abstract action to create a layer node. Implementations must implement the method which actually
 * creates the node ({@link #createNode(String)})</p>
 *
 * <p>This action will create one new child layer node for every <code>ILcdLayerTreeNode</code> which
 * has been selected. If an <code>ILcdLayer</code> has been selected which is not an <code>ILcdLayerTreeNode</code>
 * a new layer node will be added to the parent of that <code>ILcdLayer</code>. Notice when multiple
 * <code>ILcdLayer</code>s with the same parent have been selected, only one new child node will be
 * added to this parent node.</p>
 */
public abstract class AbstractAddLayerTreeNodesAction extends AbstractLayerTreeAction {
  private static String STRING_ADD_NODE = "Add an empty layer node";

  static int fNumberOfNewNodes = 0;

  /**
   * <p>Create a new action for the {@link ILcdTreeLayered ILcdTreeLayered}
   * <code>aLayered</code>.</p>
   *
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public AbstractAddLayerTreeNodesAction(ILcdTreeLayered aLayered) {
    super(aLayered);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.ADD_EMPTY_LAYER_ICON));
    setShortDescription(STRING_ADD_NODE);
  }

  /**
   * <p>Method which actually creates the node that will be added. Implementations must implement
   * this method.</p>
   *
   * @param aLabel the label for the node which will be created
   *
   * @return the created node
   */
  protected abstract ILcdLayerTreeNode createNode(String aLabel);

  /**
   * <p>Method which adds the layer <code>aLayer</code> to the parent node
   * <code>aParentNode</code>.</p>
   *
   * @param aParentNode the parent node, to which the layer <code>aLayer</code> will be added
   * @param aLayer      the layer to be added
   */
  protected abstract void addLayer(ILcdLayerTreeNode aParentNode, ILcdLayer aLayer);

  public void actionPerformed(ActionEvent e) {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    ArrayList<ILcdLayerTreeNode> parents = getAllParentsFromList(layers);
    for (ILcdLayerTreeNode node : parents) {
      fNumberOfNewNodes++;
      ILcdLayerTreeNode layer = createNode("New node " + fNumberOfNewNodes);
      addLayer(node, layer);
    }
  }

  private ArrayList<ILcdLayerTreeNode> getAllParentsFromList(ArrayList<ILcdLayer> aLayerNodeList) {
    ArrayList<ILcdLayerTreeNode> result = new ArrayList<ILcdLayerTreeNode>();
    for (ILcdLayer node : aLayerNodeList) {
      //when the layer is a node, the new node will be added as a child of the node
      //when the layer is a layer, the new node will be added as a child of the parent node
      ILcdLayerTreeNode parent;
      if (node instanceof ILcdLayerTreeNode) {
        parent = (ILcdLayerTreeNode) node;
      } else {
        parent = TLcdLayerTreeNodeUtil.getParent(node, getLayered().getRootNode());
      }
      if (parent != null && !(result.contains(parent))) {
        result.add(parent);
      }
    }
    return result;
  }

  protected boolean shouldBeEnabled() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    //as soon as at least one layer is selected this button must be enabled
    return (layers.size() > 0);
  }
}
