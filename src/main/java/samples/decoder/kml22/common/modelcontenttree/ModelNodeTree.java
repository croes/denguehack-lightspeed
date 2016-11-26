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
package samples.decoder.kml22.common.modelcontenttree;

import javax.swing.AbstractButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.Enumeration;

/**
 * <p>Extension of a <code>JTree</code> that propagates <code>MouseEvent</code> to the deepest component
 * in the tree. This can be used to make checkboxes selectable.</p> 
 */
public class ModelNodeTree extends JTree {

  /**
   * <p>Create a new tree with the default {@link JTree JTree} cell renderer and with
   * the default {@link JTree JTree} model.</p>
   */
  public ModelNodeTree() {
    super();
    initTree();
  }

  private void initTree() {
    addMouseListener( new VisibilityMouseListener() );
    setExpandsSelectedPaths( false );
    ToolTipManager.sharedInstance().registerComponent(this);
    setLargeModel( true );
  }

  @Override
  protected TreeModelListener createTreeModelListener() {
    return new ExpansionCorrectingListener(super.createTreeModelListener(),this);
  }

  public void updateUI() {
    super.updateUI();
    updateRowHeight();
  }

  @Override
  public void setLargeModel( boolean aNewValue ) {
    super.setLargeModel( aNewValue );
    updateRowHeight();
  }

  @Override
  public void setCellRenderer( TreeCellRenderer aTreeCellRenderer ) {
    super.setCellRenderer( aTreeCellRenderer );
    updateRowHeight();
  }

  private void updateRowHeight() {
    if ( isLargeModel() ) {
      TreeModel model = getModel();
      Object root = model.getRoot();
      Component renderer = getCellRenderer().getTreeCellRendererComponent( this, root, false, false, model.isLeaf( root ), 0, false );
      setRowHeight( renderer.getPreferredSize().height );
    }
    else {
      //Let JTree automatically define the row height
      setRowHeight( 0 );
    }
  }

  /**
   * <p>Mouse listener which will toggle the visibility when the click occurred on the checkbox.</p>
   */
  private class VisibilityMouseListener extends MouseAdapter {

    private WeakReference<AbstractButton> fButton = new WeakReference<AbstractButton>( null );
    private boolean fIsSelected = false;

    public void mousePressed( MouseEvent e ) {
      //remember on which button the mouse was originally pressed
      super.mousePressed( e );
      if ( SwingUtilities.isLeftMouseButton(e) ) {
        fButton = new WeakReference<AbstractButton>( getButton( e ) );
        fIsSelected = getButton( e ) != null && getButton( e ).isSelected();
      }
    }

    public void mouseReleased( MouseEvent e ) {
      //only react on left mouse button clicks and only when the mouse is released at the same button where
      //it was pressed
      if ( SwingUtilities.isLeftMouseButton(e) ) {
        AbstractButton button = getButton( e );
        AbstractButton abstractButton = fButton.get();
        if (button != null &&
            button == abstractButton &&
            fIsSelected==button.isSelected()) {
          button.doClick(  );
        }
      }
      //remove the stored value of the button
      fButton = new WeakReference<AbstractButton>( null );
    }

    private AbstractButton getButton( MouseEvent e ) {
      ModelNodeTree tree = ( ModelNodeTree ) e.getSource();
      //searching which row has been clicked
      int row = tree.getRowForLocation( ( ( Double ) e.getPoint().getX() ).intValue(), ( ( Double ) e.getPoint().getY() ).intValue() );
      if ( row != -1 ) {
        //check whether the checkbox has been clicked
        Rectangle rowBounds = tree.getRowBounds( row );
        TreePath path = tree.getPathForRow( row );
        Component renderer = tree.getCellRenderer().getTreeCellRendererComponent(
            tree, path.getLastPathComponent(), tree.isPathSelected( path ), tree.isExpanded( path ),
            tree.getModel().isLeaf( path.getLastPathComponent() ), row, false );
        renderer.setBounds( rowBounds );
        renderer.doLayout(); //make sure the check box layout is correct
        Point p = new Point( e.getX() - rowBounds.x, e.getY() - rowBounds.y );
        Component comp = SwingUtilities.getDeepestComponentAt( renderer, p.x, p.y );
        if ( comp instanceof AbstractButton ) {
          return ( ( AbstractButton ) comp );
        }
      }
      return null;
    }
  }

  /**
   * A class that corrects expansions due to changes in the model. It restores collapsed
   * nodes.
   */
  private static class ExpansionCorrectingListener implements TreeModelListener {
    private TreeModelListener fTreeModelListener;
    private JTree fTree;

    public ExpansionCorrectingListener( TreeModelListener aTreeModelListener, JTree aTree ) {
      fTreeModelListener = aTreeModelListener;
      fTree = aTree;
    }

    public void treeNodesChanged( final TreeModelEvent e ) {
      fTreeModelListener.treeNodesChanged( e );
    }

    public void treeNodesInserted( final TreeModelEvent e ) {
      fTreeModelListener.treeNodesInserted( e );
    }

    public void treeNodesRemoved( final TreeModelEvent e ) {
      fTreeModelListener.treeNodesRemoved( e );
    }

    public void treeStructureChanged( final TreeModelEvent e ) {
      final Enumeration enumeration = TreeUtil.saveExpansionState( fTree );

      EventQueue.invokeLater( new Runnable() {
        public void run() {
          fTreeModelListener.treeStructureChanged( e );
          TreeUtil.loadExpansionState( fTree, enumeration );
          fTree.invalidate();
          fTree.revalidate();
          fTree.repaint();
        }
      } );
    }
  }
}
