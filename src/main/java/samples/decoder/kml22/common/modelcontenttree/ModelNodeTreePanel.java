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

import com.luciad.format.kml22.util.TLcdKML22ResourceProvider;
import com.luciad.format.kml22.view.ALcdKML22ViewFitAction;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.swing.ALcdBalloonManager;
import samples.gxy.common.TitledPanel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * <p>The Java Swing component that represents the model node tree panel. The panel will contain a {@link JTree}
 * that represents a model view of all KML layers in a given view.</p>
 */
public class ModelNodeTreePanel extends JPanel {
  private ModelNodeTree fJTree;
  private ILcdView fView;
  private TLcdKML22ResourceProvider fResourceProvider;
  private ModelContentTreeSelectionListener fModelContentTreeSelectionListener;
  private FitActionListener fFitActionListener;

  /**
   * <p>Creates a new <code>ModelNodeTreePanel</code> instance for a given view</p>
   * @param aView The view for which this <code>ModelNodeTreePanel</code> should be created.
   * @param aResourceProvider A valid {@link TLcdKML22ResourceProvider}
   * @param aBalloonManager A balloon manager. Can be null.
   */
  public ModelNodeTreePanel( ILcdView aView,
                             TLcdKML22ResourceProvider aResourceProvider,
                             ALcdBalloonManager aBalloonManager ) {
    fView = aView;
    fResourceProvider = aResourceProvider;
    initTree();
    if(aView instanceof ILcdTreeLayered ){
      fModelContentTreeSelectionListener = new ModelContentTreeSelectionListener( fJTree, aBalloonManager );
      ( ( ILcdTreeLayered ) aView ).getRootNode().addHierarchySelectionListener( fModelContentTreeSelectionListener );
      fJTree.addTreeSelectionListener( fModelContentTreeSelectionListener );
    }
    fFitActionListener = new FitActionListener( fJTree );
    this.add( fJTree );
    fJTree.addMouseListener( fFitActionListener );
    fJTree.setToggleClickCount( 0 );
    JScrollPane scrollPane = new JScrollPane( fJTree );
    scrollPane.setPreferredSize( new Dimension( 250, 400 ));
    scrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
    setLayout( new BorderLayout() );
    add( TitledPanel.createTitledPanel( "Model Content Tree", scrollPane, TitledPanel.NORTH ), BorderLayout.CENTER );
  }

  /**
   * Sets the balloon manager for this model node tree panel.
   * @param aBalloonManager a balloon manager
   */
  public void setBalloonManager( ALcdBalloonManager aBalloonManager){
    fModelContentTreeSelectionListener.setBalloonManager( aBalloonManager );
  }

  /**
   * Adds a view fit action to this model node tree panel that will be
   * notified on double click events.
   * @param aViewFitAction a <code>ALcdKML22ViewFitAction</code>
   */
  public void addViewFitAction( ALcdKML22ViewFitAction aViewFitAction){
    fFitActionListener.addFitAction( aViewFitAction );
  }

  /**
   * Removes a view fit action from this model node tree panel. 
   * @param aViewFitAction a <code>ALcdKML22ViewFitAction</code>
   */
  public void removeViewFitAction( ALcdKML22ViewFitAction aViewFitAction){
    fFitActionListener.removeFitAction( aViewFitAction );
  }

  /**
   * Initializes the jTree of the model node tree panel.
   */
  private void initTree() {
    fJTree = new ModelNodeTree();
    fJTree.setBorder( BorderFactory.createEmptyBorder(5,2,5,2) );
    fJTree.setModel( new ModelNodeTreeModel( ( ILcdLayered ) fView ) );
    fJTree.setRootVisible( false );
    fJTree.setShowsRootHandles( true );
    fJTree.setEditable( false );
    fJTree.setOpaque( false );
    fJTree.setMinimumSize( new Dimension( 175, ( int ) fJTree.getMinimumSize().getHeight() ) );
    fJTree.setCellRenderer( new ModelContentTreeNodeCellRenderer( fResourceProvider,16,16 ) );
    fJTree.setRowHeight( -1 );
    fJTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
  }

}
