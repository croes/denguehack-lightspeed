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

import com.luciad.format.kml22.model.TLcdKML22DynamicModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.swing.ALcdBalloonManager;
import com.luciad.view.swing.TLcdUnanchoredBalloonDescriptor;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.lang.ref.WeakReference;

/**
 * <p>Listener which syncs the selection between model content tree and view. When the selection
 * changes on the tree, the selection on the view is updated. When the selection on the view is
 * changed, the selection on the model content tree is removed.</p>
 */
class ModelContentTreeSelectionListener implements TreeSelectionListener, ILcdSelectionListener {
  /**
   * When the panel is disposed and the view is still alive, it is necessary to remove the listener from the view.
   * The disposal of the panel should remove the one and only hard reference to the JTree.
   */
  private WeakReference<JTree> fTree;
  /**
   * Boolean to avoid that we change selection on the view, and that that change clears the selection
   * on the panel
   */
  private boolean fUpdating = false;
  /**
   * Access point to a balloon manager to set objects that aren't domain objects
   */
  private ALcdBalloonManager fBalloonManager;

  /**
   * Creates a new instance of <code>ModelContentTreeSelectionListener</code> without a
   * balloon manager attached to it. This must be set at a later moment with {@linkplain #setBalloonManager(ALcdBalloonManager)}
   * @param aTree a tree to update on selection changes
   */
  public ModelContentTreeSelectionListener( JTree aTree) {
    fTree = new WeakReference<JTree>( aTree );
  }

  /**
   * Creates a new instance of <code>ModelContentTreeSelectionListener</code> with
   * a given balloon manager
   * @param aTree a tree to update on selection changes
   * @param aBalloonManager a balloon manager that should be notified of model content tree selection
   *                        changes
   */
  public ModelContentTreeSelectionListener( JTree aTree, ALcdBalloonManager aBalloonManager ) {
    fBalloonManager = aBalloonManager;
    fTree = new WeakReference<JTree>( aTree );
  }

  /**
   * Gets the balloon manager that is notified of selection changes in the model content tree
   * selection listener.
   * @return a <code>ALcdBalloonManager</code> 
   */
  public ALcdBalloonManager getBalloonManager() {
    return fBalloonManager;
  }

  /**
   * Sets the balloon manager that is notified whenever a non-visual element, such as a document
   * or a folder, is selected on the model content tree.
   * @param aBalloonManager a <code>ALcdBalloonManager</code>
   */
  public void setBalloonManager( ALcdBalloonManager aBalloonManager ) {
    fBalloonManager = aBalloonManager;
  }

  /**
   * <p>Adjusts the selection on the view if the selection on the model content tree has changed.<p>
   * <p>If a visual element is selected (such as a non-container feature), the view also sets the selection
   * to this object</p>
   * <p>If a non-visual element is selected (such as a container), it clears the selection of the view
   * and notifies the balloon manager if it was set with {@linkplain #setBalloonManager(ALcdBalloonManager)}.</p>
   * @param aEvent a <code>TreeSelectionEvent</code>
   */
  public void valueChanged( TreeSelectionEvent aEvent ) {
    //no need to verify the weak reference
    //when the tree is still firing events, it still exists
    if ( !fUpdating ) {
      fUpdating = true;
      TreePath newPath = aEvent.getNewLeadSelectionPath();
      TreePath oldPath = aEvent.getOldLeadSelectionPath();
      TreeModelObject newDomainObjectContext = newPath==null?null:( TreeModelObject ) newPath.getLastPathComponent();
      TreeModelObject oldDomainObjectContext = oldPath==null?null:( TreeModelObject ) oldPath.getLastPathComponent();

      ILcdLayer layer = null;
      if ( newDomainObjectContext!=null ) {
        layer = newDomainObjectContext.getLayer();
        layer.clearSelection( ILcdFireEventMode.FIRE_LATER );
      }

      if ( newDomainObjectContext!=null ) {
        if ( !( newDomainObjectContext.getTreeModelObject() instanceof TLcdKML22DynamicModel ) ) {
          layer.selectObject( newDomainObjectContext.getTreeModelObject(),
                              true,
                              ILcdFireEventMode.FIRE_LATER );
          layer.fireCollectedSelectionChanges();
        }
        else {
          layer.fireCollectedSelectionChanges();
          if ( fBalloonManager!=null ) {
            fBalloonManager.setBalloonDescriptor( new TLcdUnanchoredBalloonDescriptor( newDomainObjectContext.getTreeModelObject(),
                                                                                       newDomainObjectContext.getModel().getModelDescriptor().getTypeName() ) );
          }
        }
      }else if (oldDomainObjectContext!=null &&
                oldDomainObjectContext.getTreeModelObject() instanceof TLcdKML22DynamicModel){
        fBalloonManager.setBalloonDescriptor( null );
      }


      fUpdating = false;
    }
  }

  /**
   * Clears the selection on the model content tree whenever an element is selected on the view.
   * @param aSelectionChangedEvent a <code>TLcdSelectionChangedEvent</code>
   */
  public void selectionChanged( TLcdSelectionChangedEvent aSelectionChangedEvent ) {
   JTree tree = fTree.get();
    if ( tree == null ){
      //remove the listener from the view, since the panel has been disposed
      aSelectionChangedEvent.getSelection().removeSelectionListener( this );
    } else {
      if ( !fUpdating ) {
        fUpdating = true;
        tree.getSelectionModel().clearSelection();
        fUpdating = false;
      }
    }
  }

}
