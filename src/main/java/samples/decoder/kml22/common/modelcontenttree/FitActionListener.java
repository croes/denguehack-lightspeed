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

import com.luciad.format.kml22.view.ALcdKML22ViewFitAction;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Listener to trigger the action that is performed when there is a doubleclick event on the model tree.<p>
 * <p>This mouse listener contains a list of <code>ALcdKML22ViewFitAction</code> instances, each one
 * will be queried to see if they can fit on the selected object and the given view.</p>
 */
public class FitActionListener extends MouseAdapter {
  private List<ALcdKML22ViewFitAction> fFitActionList;
  private JTree fTree;

  /**
   * Creates a new FitActionListener based on a tree
   * @param aTree a JTree
   */
  public FitActionListener( JTree aTree ) {
    fTree = aTree;
    fFitActionList = new ArrayList<ALcdKML22ViewFitAction>( );
  }

  /**
   * Add a fit action to this fit action listener
   * @param aFitAction a fit action
   */
  public void addFitAction( ALcdKML22ViewFitAction aFitAction ) {
    fFitActionList.add( aFitAction );
  }

  /**
   * Removes a fit action from this fit action listener
   * @param aFitAction a fit action
   */
  public void removeFitAction( ALcdKML22ViewFitAction aFitAction ) {
    fFitActionList.remove( aFitAction );
  }

  @Override
  public void mouseClicked( MouseEvent aMouseEvent ) {
    if ( aMouseEvent.getClickCount() == 2 ) {
      int selRow = fTree.getRowForLocation(aMouseEvent.getX(), aMouseEvent.getY());
      if( selRow != -1 &&
          fTree.getRowBounds( selRow ).contains( new Point( aMouseEvent.getX(), aMouseEvent.getY() ) )) {
        TreePath selPath = fTree.getPathForLocation(aMouseEvent.getX(), aMouseEvent.getY());

        for ( ALcdKML22ViewFitAction fitAction : fFitActionList ) {
          Object domainObject = selPath.getLastPathComponent();
          if ( domainObject instanceof TreeModelObject &&
               fitAction.canPerformFit( (( ( TreeModelObject ) domainObject ).getTreeModelObject() ),
                                        ( ( TreeModelObject ) domainObject ).getView())) {
            fitAction.performFit( (( ( TreeModelObject ) domainObject ).getTreeModelObject() ),
                                        ( ( TreeModelObject ) domainObject ).getView());
          }
        }
      }
    }
  }
}
