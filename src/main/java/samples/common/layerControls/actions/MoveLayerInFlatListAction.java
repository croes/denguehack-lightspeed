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
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdTreeLayered;

/**
 * An ALcdAction that changes the position of a given ILcdLayer in an ILcdLayered.  So it does not
 * move in the layer tree view (ILcdTreeLayered.getRootNode()), but it moves directly in the flat
 * layer representation (by using the given ILcdTreeLayered as its super-interface ILcdLayered).
 *
 * In real applications, layer moves are typically performed on the layer tree structure, so it is
 * better to use MoveLayersAction. The layer moves performed by this action are in fact a
 * best-effort move: the layer index after the move is not always what was requested.  See the
 * Developer's Guide for more information.
 *
 * This class is much simpler than MoveLayersAction because it:
 * - Only supports moving one layer at a time (so no need to sort the layers)
 * - Only has move up/down behavior, not top/bottom
 */
public class MoveLayerInFlatListAction extends AbstractLayerTreeAction {
  private boolean fMoveUp;

  /**
   * Creates a <code>MoveLayerInFlatListAction</code>.
   *
   * @param aLayered   the layered on which actions will be performed.
   * @param aMoveUp    true to move up, false to move down
   */
  public MoveLayerInFlatListAction(ILcdTreeLayered aLayered, boolean aMoveUp) {
    super(aLayered);
    fMoveUp = aMoveUp;
    setName(aMoveUp ? "Move layer up" : "Move layer down");
    setShortDescription(getName() + " in the flat layer list (best-effort move)");
    setIcon(aMoveUp ? TLcdIconFactory.create(TLcdIconFactory.MOVE_UP_ICON) : TLcdIconFactory.create(TLcdIconFactory.MOVE_DOWN_ICON));
  }

  @Override
  protected boolean shouldBeEnabled() {
    return canMove();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ILcdLayer layer = layerToMove();
    if (layer == null) {
      throw new NullPointerException("At least one ILcdLayer must be set");
    }
    move(layer, getLayered());
  }

  private ILcdLayer layerToMove() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    if (layers.size() == 0) {
      return null;
    }
    return layers.get(0);
  }

  /**
   * <p>This method performs the actual move operation.</p>
   *
   * @param aLayer The layer to move.
   * @param aLayered The layered to move the layer in.
   */
  private void move(ILcdLayer aLayer, ILcdLayered aLayered) {
    int newIndex = aLayered.indexOf(aLayer) + (fMoveUp ? 1 : -1);
    aLayered.moveLayerAt(newIndex, aLayer);
  }

  /**
   * @return <code>true</code> if and only if the move operation can be performed on the selected
   * layer.
   */
  protected boolean canMove() {
    ILcdLayered layered = getLayered();
    ILcdLayer layer = layerToMove();
    if (layer != null) {
      int index = layered.indexOf(layer);
      if (fMoveUp) {
        return index < (layered.layerCount() - 1);
      } else {
        return index > 0;
      }
    } else {
      return false;
    }
  }
}
