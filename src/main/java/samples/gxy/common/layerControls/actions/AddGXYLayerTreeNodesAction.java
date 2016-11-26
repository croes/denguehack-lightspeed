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
package samples.gxy.common.layerControls.actions;

import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;

import samples.common.layerControls.actions.AbstractAddLayerTreeNodesAction;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * <p>Action to add GXYLayerTreeNodes to a layer tree.</p>
 */
public class AddGXYLayerTreeNodesAction extends AbstractAddLayerTreeNodesAction {
  /**
   * <p>Create an action to add a <code>TLcdGXYLayerTreeNode</code> to an
   * <code>ILcdTreeLayered</code>.</p>
   *
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public AddGXYLayerTreeNodesAction(ILcdTreeLayered aLayered) {
    super(aLayered);
  }

  protected ILcdLayerTreeNode createNode(String aLabel) {
    return new TLcdGXYLayerTreeNode(aLabel);
  }

  protected void addLayer(ILcdLayerTreeNode aParentNode, ILcdLayer aLayer) {
    if (aLayer instanceof ILcdGXYLayer) {
      GXYLayerUtil.addGXYLayer(aParentNode, (ILcdGXYLayer) aLayer, (ILcdGXYView) getLayered(), true);
    }
  }
}
