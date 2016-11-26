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

import java.awt.EventQueue;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.luciad.view.swing.TLcdLayerTree;

/**
 * Tree listener that selects newly added nodes.
 */
class SelectNewlyAddedNodeListener implements TreeModelListener {

  private final TLcdLayerTree fTree;

  /**
   * <p>Construct a new <code>SelectionCorrectionListener</code> for the tree <code>aTree</code>.
   * The listener will not automatically be added to the tree.</p>
   *
   * @param aTree the tree
   */
  public SelectNewlyAddedNodeListener(TLcdLayerTree aTree) {
    fTree = aTree;
  }

  public void treeNodesChanged(TreeModelEvent e) {
    //do nothing
  }

  public void treeNodesInserted(final TreeModelEvent e) {
    //set the selection to the inserted nodes
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        //restore the expanded state
        Object[] path = e.getPath();
        Object[] children = e.getChildren();
        TreePath[] selectedPaths = new TreePath[children.length];
        for (int i = 0; i < children.length; i++) {
          Object child = children[i];
          TreePath treePath = new TreePath(path);
          selectedPaths[i] = treePath.pathByAddingChild(child);
        }
        fTree.setSelectionPaths(selectedPaths);
      }
    });
  }

  public void treeNodesRemoved(TreeModelEvent e) {
    //do nothing
  }

  public void treeStructureChanged(TreeModelEvent e) {
    //do nothing
  }
}
