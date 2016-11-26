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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.swing.TLcdLayerTree;

/**
 * <p>Transfer handler for the <code>TLcdLayerTree</code>.</p>
 */
public class LayerTreeTransferHandler extends TransferHandler {
  /**
   * The used data flavor
   */
  public final static DataFlavor LAYER_TREE_FLAVOR = createLayerTreeNodeDataFlavor();

  private static DataFlavor createLayerTreeNodeDataFlavor() {
    try {
      return new DataFlavor(
          DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ILcdLayerTreeNode.class.getName(),
          "LayerTreeNode",
          LayerTreeTransferHandler.class.getClassLoader()
      );
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public int getSourceActions(JComponent c) {
    if (c instanceof TLcdLayerTree) {
      return MOVE;
    }
    return super.getSourceActions(c);
  }

  protected Transferable createTransferable(JComponent c) {
    if (c instanceof TLcdLayerTree) {
      TLcdLayerTree tree = (TLcdLayerTree) c;
      TreePath[] treePaths = tree.getSelectionPaths();
      ArrayList<ILcdLayer> nodeList = new ArrayList<ILcdLayer>();
      for (TreePath treePath : treePaths) {
        ILcdLayer node = (ILcdLayer) treePath.getLastPathComponent();
        nodeList.add(node);
      }
      return new LayerTreeTransferable(nodeList, tree.getModel());

    }
    return super.createTransferable(c);
  }

  public boolean importData(JComponent comp, Transferable t) {
    //we will insert the nodes as a new child of the selected node
    if (comp instanceof TLcdLayerTree) {
      TLcdLayerTree tree = (TLcdLayerTree) comp;
      ILcdLayer targetNode = (ILcdLayer) tree.getSelectionPath().getLastPathComponent();
      TreeModel targetModel = tree.getModel();
      ILcdLayerTreeNode parentNode = TLcdLayerTreeNodeUtil.getParent(targetNode, (ILcdLayerTreeNode) targetModel.getRoot());
      List<ILcdLayer> childNodes;
      try {
        TransferableData data = (TransferableData) t.getTransferData(LAYER_TREE_FLAVOR);
        childNodes = data.getLayers();
        TreeModel sourceModel = data.getSourceTreeModel();
        List<ILcdLayer> path = TLcdLayerTreeNodeUtil.getPath((ILcdLayerTreeNode) tree.getModel().getRoot(), targetNode);
        if (path == null) {
          return false;
        }
        int count = 0;
        for (ILcdLayer childNode : childNodes) {
          //a node must not be inserted into one of its childs
          if (path.indexOf(childNode) != -1 || childNode == targetNode) {
            return false;
          }
          //remove the node from the tree
          ILcdLayerTreeNode parent = TLcdLayerTreeNodeUtil.getParent(childNode, (ILcdLayerTreeNode) sourceModel.getRoot());
          parent.removeLayer(childNode);

          if (targetModel.isLeaf(targetNode) && parentNode != null) {
            //the targetnode is a leaf node and cannot accept children
            int index = parentNode.indexOf(targetNode);
            parentNode.addLayer(childNode, Math.max(index - count, 0));
            count++;
          } else {
            //add the node in its new parent
            ((ILcdLayerTreeNode) targetNode).addLayer(childNode);
          }
        }
        tree.invalidate();
        return true;
      } catch (UnsupportedFlavorException e) {
        throw new RuntimeException("Drag and drop failed: " + e);
      } catch (IOException e) {
        throw new RuntimeException("Drag and drop failed: " + e);
      }
    } else {
      return super.importData(comp, t);
    }
  }

  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    if (comp instanceof TLcdLayerTree) {
      boolean result = false;
      for (int i = 0; !result && i < transferFlavors.length; i++) {
        DataFlavor flavor = transferFlavors[i];
        result = flavor.equals(LAYER_TREE_FLAVOR);
      }
      return result;
    } else {
      return super.canImport(comp, transferFlavors);
    }
  }
}
