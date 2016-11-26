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
package samples.lightspeed.datamodel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.view.ILcdLayer;

import samples.common.dataModelDisplayTree.DataModelTreeCellRenderer;
import samples.common.dataModelDisplayTree.DataModelTreeModel;

/**
 * Panel that shows a tree visualizing the data model of the given layer.
 * The layer's model must implement <code>ILcdDataModelDescriptor</code>.
 */
public class DataModelTree extends JPanel {

  private JTree fDataModelTree;

  public DataModelTree() {
    super(new BorderLayout());
    fDataModelTree = createDataModelTree();
    JScrollPane scrollPane = new JScrollPane(fDataModelTree);
    add(scrollPane, BorderLayout.CENTER);
  }

  public void setLayer(ILcdLayer aLayer) {
    fDataModelTree.setModel(createTreeModel(aLayer));
  }

  public JTree getDataModelTree() {
    return fDataModelTree;
  }

  /**
   * Creates a data model tree with the default tree model and tree cell renderer.
   *
   * @return a valid <code>JTree</code>
   */
  private JTree createDataModelTree() {
    JTree dataModelTree = new JTree();
    dataModelTree.setModel(new EmptyTreeModel());
    dataModelTree.setCellRenderer(new DataModelTreeCellRenderer());
    return dataModelTree;
  }

  /**
   * Creates a <code>TreeModel</code> for a given layer. This model list will be either a
   * <code>DataModelTreeModel</code> if the layer has a model that has an
   * <code>ILcdDataModelDescriptor</code>.
   * Else, this method will return a
   * <code>TreeModel</code> with a user message that states what to do to create a correct
   * <code>TreeModel</code>.
   *
   * @param aLayer the layer
   * @return Either a <code>DataModelTreeModel</code> or a default <code>TreeModel</code> with a
   * user message.
   */
  private TreeModel createTreeModel(ILcdLayer aLayer) {
    if (aLayer == null ||
        aLayer.getModel() == null ||
        !(aLayer.getModel().getModelDescriptor() instanceof ILcdDataModelDescriptor)) {
      return new EmptyTreeModel();
    } else {
      return new DataModelTreeModel((ILcdDataModelDescriptor) aLayer.getModel().getModelDescriptor(), true);
    }
  }

  /**
   * Inner class that represents a simple <code>TreeModel</code> with a single leaf-node, that
   * contains a message for the user.
   */
  private class EmptyTreeModel extends DefaultTreeModel {
    public EmptyTreeModel() {
      super(new DefaultMutableTreeNode("Select a single layer with a data model", false));
    }
  }

}
