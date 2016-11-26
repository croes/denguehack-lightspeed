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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.TLcdLayerTreeNodeUtil;

/**
 * <p>An <code>ALcdAction</code> that changes the position of a given  <code>ILcdLayer</code> in an
 * <code>ILcdTreeLayered</code>. The position change depends on the move command associated with
 * this <code>ILcdAction</code>.</p>
 */
public class MoveLayersAction extends AbstractLayerTreeAction {
  /**
   * Constant to indicate an action that moves a layer to the top of the list.
   */
  public static final String MOVE_TOP = "Top";
  /**
   * Constant to indicate an action that moves a layer to the bottom of the list.
   */
  public static final String MOVE_BOTTOM = "Bottom";
  /**
   * Constant to indicate an action that moves a layer one position up in the list.
   */
  public static final String MOVE_UP = "Up";
  /**
   * Constant to indicate an action that moves a layer one position down in the list.
   */
  public static final String MOVE_DOWN = "Down";

  private String fMoveCommand = MOVE_TOP;

  /**
   * Constructor with an <code>ILcdTreeLayered</code>, an <code>ILcdLayer</code> array and a
   * <code>MoveCommand</code>.
   *
   * @param aLayered     the layered on which actions will be performed.
   * @param aMoveCommand the action to perform on the layers given.
   */
  public MoveLayersAction(ILcdTreeLayered aLayered, String aMoveCommand) {
    super(aLayered);
    fMoveCommand = aMoveCommand;
    setName(aMoveCommand);
    setLongDescription("Move layer " + aMoveCommand);
    //the icon and short description depend on the move command
    String description = "";
    if (MOVE_TOP.equals(aMoveCommand)) {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.TO_TOP_ICON));
      description = "Move selected layer to top";
    } else if (MOVE_BOTTOM.equals(aMoveCommand)) {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.TO_BOTTOM_ICON));
      description = "Move selected layer to bottom";
    } else if (MOVE_UP.equals(aMoveCommand)) {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.MOVE_UP_ICON));
      description = "Move selected layer up";
    } else if (MOVE_DOWN.equals(aMoveCommand)) {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.MOVE_DOWN_ICON));
      description = "Move selected layer down";
    }
    setShortDescription(description);
  }

  protected boolean shouldBeEnabled() {
    return canMove();
  }

  public void actionPerformed(ActionEvent e) {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    ILcdTreeLayered layered = getLayered();
    if (layers.size() == 0) {
      throw new NullPointerException("At least one ILcdLayer must be set");
    }
    if (layered == null) {
      throw new NullPointerException("An ILcdTreeLayered must be set");
    }
    if (fMoveCommand == null) {
      throw new NullPointerException("A MoveCommand must be set");
    }
    //when moving multiple layers at once, the layers must be moved in the correct order,
    //so the list is sorted first
    ArrayList<ILcdLayer> sortedLayers = sortTreeListLayers();
    ILcdLayerTreeNode rootNode = getLayered().getRootNode();
    move(sortedLayers, rootNode);
  }

  /**
   * <p>This method performs the actual move operation. The <code>aLayersInOrder</code>-list
   * contains all the layers which will be moved, in the order they must be moved (see {@link
   * #sortTreeListLayers()}. The index to which those layers are moved depends on the move
   * command (see {@link #getMoveCommand()}.</p>
   *
   * @param aLayersInOrder a list containing all the layers which should be moved in the order they
   *                       must be moved
   * @param aRootNode      the root node of the tree containing those layers
   */
  protected void move(ArrayList<ILcdLayer> aLayersInOrder, ILcdLayerTreeNode aRootNode) {
    for (ILcdLayer aLayer : aLayersInOrder) {
      ILcdLayerTreeNode aLayered = TLcdLayerTreeNodeUtil.getParent(aLayer, aRootNode);
      int new_index;
      String moveCommand = fMoveCommand;
      if (moveCommand.startsWith(MOVE_TOP)) {
        new_index = aLayered.layerCount() - 1;
        aLayered.moveLayerAt(new_index, aLayer);
      } else if (moveCommand.startsWith(MOVE_UP)) {
        new_index = aLayered.indexOf(aLayer) + 1;
        aLayered.moveLayerAt(new_index, aLayer);
      } else if (moveCommand.startsWith(MOVE_DOWN)) {
        new_index = aLayered.indexOf(aLayer) - 1;
        aLayered.moveLayerAt(new_index, aLayer);
      } else if (moveCommand.startsWith(MOVE_BOTTOM)) {
        new_index = 0;
        aLayered.moveLayerAt(new_index, aLayer);
      }
    }
  }

  /**
   * <p>Returns a list containing all the layers to be moved in the order they should be moved.</p>
   *
   * @return a list containing all the layers to be moved in the order they should be moved.
   */
  protected ArrayList<ILcdLayer> sortTreeListLayers() {
    final boolean ascendingOrder = fMoveCommand.startsWith(MOVE_TOP) ||
                                   fMoveCommand.startsWith(MOVE_DOWN);
    //the move operation will only move layers on the current level, and not
    //move the parent node
    //therefore all the layers with the same parent must be sorted by their index
    //in the parent node
    //the order of the layers with different parents is irrelevant
    HashMap<ILcdLayerTreeNode, ArrayList<ILcdLayer>> parentListMap = new HashMap<ILcdLayerTreeNode, ArrayList<ILcdLayer>>();
    //first group all the layers with the same parent
    ArrayList<ILcdLayer> selectedLayers = getFilteredLayers();
    for (ILcdLayer layer : selectedLayers) {
      ILcdLayerTreeNode parentNode = TLcdLayerTreeNodeUtil.getParent(layer, getLayered().getRootNode());
      if (parentListMap.containsKey(parentNode)) {
        parentListMap.get(parentNode).add(layer);
      } else {
        ArrayList<ILcdLayer> list = new ArrayList<ILcdLayer>();
        list.add(layer);
        parentListMap.put(parentNode, list);
      }
    }
    Set<ILcdLayerTreeNode> parents = parentListMap.keySet();
    ArrayList<ILcdLayer> result = new ArrayList<ILcdLayer>();
    //then order the layers with the same parent
    ILcdLayer[] emptyLayerArray = {};
    for (final ILcdLayerTreeNode parent : parents) {
      ArrayList<ILcdLayer> list = parentListMap.get(parent);
      ILcdLayer[] layers = list.toArray(emptyLayerArray);
      Arrays.sort(layers, new Comparator<ILcdLayer>() {
        public int compare(ILcdLayer o1, ILcdLayer o2) {
          int index1 = parent.indexOf(o1);
          int index2 = parent.indexOf(o2);
          int order = 0;
          if (index1 < index2) {
            order = -1;
          } else if (index2 < index1) {
            order = 1;
          }
          return ascendingOrder ? order : -1 * order;
        }
      });
      result.addAll(Arrays.asList(layers));
    }
    return result;
  }

  /**
   * <p>Returns <code>true</code> if and only if the move operation can be performed on at least one
   * of the layers set on this action after filtering the layers.</p>
   *
   * @return <code>true</code> if and only if the move operation can be performed on at least one of
   *         the layers set son this action after filtering the layers.
   */
  protected boolean canMove() {
    return fMoveCommand.startsWith(MOVE_TOP) || fMoveCommand.startsWith(MOVE_UP) ?
           canMoveUp() : canMoveDown();
  }

  private boolean canMoveUp() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    if (layers.size() == 0) {
      return false;
    }
    boolean result = true;
    ILcdLayerTreeNode rootNode = getLayered().getRootNode();
    for (int i = 0; i < layers.size() && result; i++) {
      ILcdLayer layer = layers.get(i);
      result = canMoveLayerUp(rootNode, layer);
    }
    return result;
  }

  /**
   * <p>Returns <code>true</code> if and only if the layer <code>aLayer</code> can be moved up in
   * the hierarchical layer structure with root node <code>aRootNode</code>.</p>
   *
   * @param aRootNode the root node of the hierarchical layer structure
   * @param aLayer    the layer for which the move has to be checked
   *
   * @return <code>true</code> if and only if the layer <code>aLayer</code> can be moved up in the
   *         hierarchical layer structure
   */
  protected boolean canMoveLayerUp(ILcdLayerTreeNode aRootNode, ILcdLayer aLayer) {
    boolean result;
    ILcdLayerTreeNode parent = TLcdLayerTreeNodeUtil.getParent(aLayer, aRootNode);
    //root nodes cannot be moved
    result = parent != null && parent.indexOf(aLayer) < parent.layerCount() - 1;
    return result;
  }

  private boolean canMoveDown() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    if (layers.size() == 0) {
      return false;
    }
    boolean result = true;
    ILcdLayerTreeNode rootNode = getLayered().getRootNode();
    for (int i = 0; i < layers.size() && result; i++) {
      ILcdLayer layer = layers.get(i);
      result = canMoveLayerDown(rootNode, layer);
    }
    return result;
  }

  /**
   * <p>Returns <code>true</code> if and only if the layer <code>aLayer</code> can be moved down in
   * the hierarchical layer structure with root node <code>aRootNode</code>.</p>
   *
   * @param aRootNode the root node of the hierarchical layer structure
   * @param aLayer    the layer for which the move has to be checked
   *
   * @return <code>true</code> if and only if the layer <code>aLayer</code> can be moved down in the
   *         hierarchical layer structure
   */
  protected boolean canMoveLayerDown(ILcdLayerTreeNode aRootNode, ILcdLayer aLayer) {
    boolean result;
    ILcdLayerTreeNode parent = TLcdLayerTreeNodeUtil.getParent(aLayer, aRootNode);
    //root nodes cannot be moved
    result = parent != null && parent.indexOf(aLayer) > 0;
    return result;
  }

  /**
   * <p>Returns the move command of this action.</p>
   *
   * @return the move command of this action
   */
  public String getMoveCommand() {
    return fMoveCommand;
  }
}
