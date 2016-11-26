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
 * <p>Action to remove one or more <code>ILcdLayer</code>s. Removing an <code>ILcdLayerTreeNode</code> will
 * also remove the whole subtree underneath that particular node. It is however impossible to delete
 * the root node of a tree.</p>
 */
public abstract class AbstractRemoveLayersAction extends AbstractLayerTreeAction {
  private static String STRING_REMOVE_NODE = "Remove a layer";

  /**
   * <p>Create an action which removes a layer from an <code>ILcdTreeLayered</code>.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public AbstractRemoveLayersAction(ILcdTreeLayered aLayered) {
    super(aLayered);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.DELETE_ICON));
    setShortDescription(STRING_REMOVE_NODE);
  }

  public void actionPerformed(ActionEvent e) {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    ILcdTreeLayered layered = getLayered();
    if (layers.size() > 0 && layered != null) {
      ILcdLayerTreeNode rootNode = layered.getRootNode();
      for (ILcdLayer node : layers) {
        ILcdLayerTreeNode parent = TLcdLayerTreeNodeUtil.getParent(node, rootNode);
        removeLayer(parent, node);
      }
    }
  }

  /**
   * <p>Method which performs the actual removal of <code>aLayer</code> from the parent node
   * <code>aParentNode</code>.</p>
   *
   * @param aParentNode the parent node from which the layer <code>aLayer</code> should be removed
   * @param aLayer      the layer to be removed
   */
  protected abstract void removeLayer(ILcdLayerTreeNode aParentNode, ILcdLayer aLayer);

  protected boolean shouldBeEnabled() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    //should be enabled as soon as 1 layer which is not the root node is selected
    for (ILcdLayer layer : layers) {
      if (layer != getLayered().getRootNode()) {
        return true;
      }
    }
    return false;
  }
}
