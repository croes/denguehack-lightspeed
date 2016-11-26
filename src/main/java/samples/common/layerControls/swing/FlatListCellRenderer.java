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
package samples.common.layerControls.swing;

import javax.swing.JTree;

import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.swing.TLcdGXYLayerTreeNodeCellRenderer;

/**
 * <p>TreeCellRenderer for a flat representation of a hierarchical layer structure.
 * This renderer will set the checkboxes of empty layer nodes invisible.</p>
 */
public class FlatListCellRenderer extends TLcdGXYLayerTreeNodeCellRenderer {
  /**
   * <p>Create a new renderer for the layers in the <code>ILcdTreeLayered</code> <code>aView</code></p>
   *
   * @param aView the <code>ILcdTreeLayered</code> containing the layers which will be displayed in the layer tree.
   */
  public FlatListCellRenderer(ILcdGXYView aView) {
    super(aView);
  }

  @Override
  protected void updateRendererFromLayer(JTree aTree, ILcdLayer aLayer, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.updateRendererFromLayer(aTree, aLayer, selected, expanded, leaf, row, hasFocus);
    if (TLcdLayerTreeNodeUtil.isEmptyNode(aLayer)) {
      getCheckBox().setVisible(false);
    }
  }
}
