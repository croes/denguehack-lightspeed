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
import com.luciad.format.kml22.model.TLcdKML22RenderableModel;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainerListener;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelContainerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>The main model used to represent the model content tree of KML.</p>
 * <p>Sets up a <code>TreeModel</code> based on a given layerd view. This <code>TreeModel</code>
 * registers listeners on the view to keep itself up to date.</p>
 * <p>This model uses {@linkplain TreeModelObject} instances
 * exclusively as node objects.</p>
 */
public class ModelNodeTreeModel implements TreeModel {
  private TreeModelObject fRootNode = new TreeModelObject( null, null, null, null, null );
  private List<ILcdLayer> fRootContainer;
  private List<TreeModelListener> fListeners;
  private ILcdView fView;

  private final List<TreeModelObject> fTreeModelObjects = new ArrayList<TreeModelObject>( );
  private ModelUpdateListener fModelContainerListener = new ModelUpdateListener(this);

  /**
   * <p>Creates a new <code>ModelNodeTreeModel</code> instance</p>
   * <p>This constructor does the following: </p>
   * <ul>
   * <li>Scan the given <code>ILcdLayered</code> for existent KML layers, and adds them to this model.</li>
   * <li>It registers model listeners on all KML models to keep this <code>ModelNodeTreeModel</code> up to date.</li>
   * <li>It registers a layered listener for the given <code>ILcdLayered</code> to be informed of new KML layers being
   * added to the view.</li>
   * </ul>
   *
   * @param aLayered A layered on which this model node tree model can base its model on
   */
  public ModelNodeTreeModel( ILcdLayered aLayered) {
    fListeners = new CopyOnWriteArrayList<TreeModelListener>();
    fRootContainer = new LinkedList<ILcdLayer>( );
    if(aLayered instanceof ILcdView){
      fView = ( ILcdView ) aLayered;
    }

    setupRootContainer(fRootContainer, aLayered);

    setupLayeredListener(aLayered);
  }

  /**
   * Setup up the layered listener that listens to new layers being added
   * @param aLayered a layered that should be listened to
   */
  private void setupLayeredListener( ILcdLayered aLayered ) {
    aLayered.addLayeredListener( new LayeredListener(this) );
  }

  /**
   * Fills the root node with all the relevant information from the given <code>aLayered</code>
   * @param aRootContainer a root node
   * @param aLayered a layered that may or may not contain KML layers
   */
  private void setupRootContainer( List<ILcdLayer> aRootContainer, ILcdLayered aLayered ) {
    Enumeration enumeration = aLayered.layers();
    while ( enumeration.hasMoreElements() ) {
      Object object = enumeration.nextElement();
      if(object instanceof ILcdLayer ){
        ILcdLayer layer = ( ILcdLayer ) object;
        if(layer.getModel() instanceof TLcdKML22RenderableModel ){
          aRootContainer.add( 0, layer );
        }
      }                              
    }
  }

  public Object getRoot() {
    return fRootNode;
  }

  public Object getChild( Object aParent, int aIndex ) {
    if ( aIndex < 0 ) {
      return null;
    }
    if ( aParent == fRootNode ) {
      final ILcdLayer layer = fRootContainer.get( aIndex );
      if ( hasChildren( ( ( TLcdKML22RenderableModel ) layer.getModel() ).getDelegateModel() ) ) {
        Object child = getModelTreeNodeChild(((( TLcdKML22RenderableModel ) layer.getModel() ).getDelegateModel() ),0);
        return generateTreeModelObject( child , layer.getModel(), layer, fView,fRootNode );
      }
      else {
        //This should only happen if a kml document contains nothing inside it.
        return generateTreeModelObject( (((( TLcdKML22RenderableModel ) layer.getModel() ).getDelegateModel() )), layer.getModel(), layer, fView, fRootNode );
      }
    }
    if ( aParent instanceof TreeModelObject) {
      Object domainObject = ( ( TreeModelObject ) aParent ).getTreeModelObject();
      domainObject = transformDomainObject( domainObject );
      if ( domainObject instanceof ILcdModelTreeNode ) {
        return generateTreeModelObject( getModelTreeNodeChild( (ILcdModelTreeNode)domainObject,aIndex ),( ( TreeModelObject ) aParent ).getModel(), ( ( TreeModelObject ) aParent ).getLayer(), ( ( TreeModelObject ) aParent ).getView(), ( TreeModelObject ) aParent );
      }
    }
    return null;
  }

  /**
   * <p>This method takes te given domain object and checks if it is a wrapper for a NetworkLink. If it is, it
   * will return the first child of the NetworkLink. If the given domain object is not a wrapper for a NetworkLink,
   * this method will return the given domain object.</p>
   * <p>This method can be used to skip top-level KML containers that would only produce redundant nodes
   * in the tree model.</p>
   * @param aDomainObject A domain object from a <code>TreeModelObject</code>
   * @return a transformed domain object
   */
  private Object transformDomainObject( Object aDomainObject ) {
    if ( aDomainObject instanceof TLcdKML22DynamicModel && ( ( TLcdKML22DynamicModel ) aDomainObject ).getKMLNetworkLink() != null ) {
      if ( ( ( TLcdKML22DynamicModel ) aDomainObject ).modelCount() > 0 ) {
        aDomainObject = ( ( TLcdKML22DynamicModel ) aDomainObject ).getModel( 0 );
      }
    }
    return aDomainObject;
  }

  /**
   * <p>Returns the child for a given <code>ILcdModelTreeNode</code></p>
   * @param aModelTreeNode an <code>ILcdModelTreeNode</code>
   * @param aIndex the index of the wanted child
   * @return Either the child of the given model tree node; or null if the index
   * could not be found.
   */
  private Object getModelTreeNodeChild( ILcdModelTreeNode aModelTreeNode, int aIndex ) {
    if ( aIndex < aModelTreeNode.modelCount() ) {
      return aModelTreeNode.getModel( aIndex );
    }
    else {
      aIndex -= aModelTreeNode.modelCount();
      return elementAt( aModelTreeNode, aIndex );
    }
  }

  /**
   * Generates a <code>TreeModelObject</code>, or fetches existent instance if it already
   * exists for the given parameters. This method caches new <code>TreeModelObject</code> instances
   * in a <code>List</code>.
   * @param aTreeModelObject The domain object of the tree model object
   * @param aModel a model
   * @param aLayer a layer
   * @param aView a view
   * @param aParent the parent of the <code>TreeModelObject</code>
   * @return a <code>TreeModelObject</code>, either new or retrieved from a previously stored list.
   * @see TreeModelObject
   */
  private TreeModelObject generateTreeModelObject( Object aTreeModelObject, ILcdModel aModel, ILcdLayer aLayer, ILcdView aView, TreeModelObject aParent ) {
    synchronized ( fTreeModelObjects ) {
      TreeModelObject treeModelObject = new TreeModelObject( aTreeModelObject, aModel, aLayer, aView, aParent );
      int index = fTreeModelObjects.indexOf( treeModelObject );
      if ( index!=-1) {
        return fTreeModelObjects.get( index );
      }else{
        fTreeModelObjects.add(treeModelObject);
        treeModelObject.addModelNodeContainerListener( fModelContainerListener );
        treeModelObject.getParent().getChildren().add( treeModelObject );
        return treeModelObject;
      }
    }
  }

  public int getChildCount( Object aParent ) {
    if ( aParent == fRootNode ) {
      return ( fRootContainer.size() );
    }
    if ( aParent instanceof TreeModelObject) {
      Object domainObject = (( TreeModelObject ) aParent).getTreeModelObject();
      domainObject = transformDomainObject( domainObject );
      if ( domainObject instanceof ILcdModelTreeNode ) {
        ILcdModelTreeNode modelTreeNode = ( ILcdModelTreeNode ) domainObject;
        return getModelTreeNodeChildCount( modelTreeNode );
      }
    }
    return 0;
  }

  /**
   * <p>Returns the child count of a given <code>ILcdModelTreeNode</code></p>
   * @param aModelTreeNode an <code>ILcdModelTreeNode</code>
   * @return the amount of models and elements reside the given model tree node; 0 is returned
   *         when the given model tree node was null.
   */
  private int getModelTreeNodeChildCount( ILcdModelTreeNode aModelTreeNode ) {
    if(aModelTreeNode==null){
      return 0;
    }
    int childCount = 0;
    childCount += aModelTreeNode.modelCount();
    childCount += countElements( aModelTreeNode );
    return childCount;
  }

  public boolean isLeaf( Object aNode ) {
    return !(( (aNode instanceof TreeModelObject) && ( ( TreeModelObject ) aNode ).getTreeModelObject() instanceof TLcdKML22DynamicModel ) || (aNode == fRootNode ));
  }

  public void valueForPathChanged( TreePath aPath, Object aNewValue ) {
    throw new RuntimeException( "The user is not allowed to edit this tree" );
  }

  public int getIndexOfChild( Object aParent, Object aChild ) {
    if(!(aChild instanceof TreeModelObject)){
      return -1;
    }
    if ( aParent==fRootNode ) {
      int childCount = getChildCount( fRootNode );
      for ( int i = 0; i < childCount; i++ ) {
        if ( getChild( fRootNode, i ) == ( ( TreeModelObject ) aChild ).getTreeModelObject() ) {
          return i;
        }
      }
      return -1;
    }
    if ( aParent instanceof TreeModelObject) {
      Object domainObject = ( ( TreeModelObject ) aParent ).getTreeModelObject();
      domainObject = transformDomainObject( domainObject );
      if ( domainObject instanceof ILcdModelTreeNode ) {
        ILcdModelTreeNode parentModelTreeNode = ( ILcdModelTreeNode ) domainObject;
        Enumeration modelEnumeration = parentModelTreeNode.models();
        int index = 0;
        while ( modelEnumeration.hasMoreElements() ) {
          Object foundChild = modelEnumeration.nextElement();
          if ( foundChild == ( ( TreeModelObject ) aChild ).getTreeModelObject() ) {
            return index;
          }
          index++;
        }
        Enumeration elementEnumeration = parentModelTreeNode.elements();
        while ( elementEnumeration.hasMoreElements() ) {
          Object foundChild = elementEnumeration.nextElement();
          if ( foundChild == ( ( TreeModelObject ) aChild ).getTreeModelObject() ) {
            return index;
          }
          index++;
        }
        return -1;
      }
    }
    return -1;
  }

  public void addTreeModelListener( TreeModelListener aTreeModelListener ) {
    fListeners.add( aTreeModelListener );
  }

  public void removeTreeModelListener( TreeModelListener aTreeModelListener ) {
    fListeners.remove( aTreeModelListener );
  }

  /**
   * Fires an event that notifies all listeners that some nodes have been added to the TreeModel.
   * @param aTreeModelEvent a tree model event
   */
  public void fireNodeAddedEvent( final TreeModelEvent aTreeModelEvent){
    for ( final TreeModelListener listener : fListeners ) {
      TLcdAWTUtil.invokeLater( new Runnable() {
        public void run() {
          listener.treeNodesInserted(aTreeModelEvent );
        }
      } );
    }
  }

  /**
   * Fires an event that notifies all listeners that some nodes have been removed from the TreeModel.
   * @param aTreeModelEvent a tree model event
   */
  public void fireNodeRemovedEvent( final TreeModelEvent aTreeModelEvent){
    for ( final TreeModelListener listener : fListeners ) {
      TLcdAWTUtil.invokeLater( new Runnable() {
        public void run() {
          listener.treeNodesRemoved( aTreeModelEvent );
        }
      } );
    }
  }

  /**
   * Fires an event that notifies all listeners that some nodes have changed in the TreeModel.
   * @param aTreeModelEvent a tree model event
   */
  public void fireNodeChangedEvent(final TreeModelEvent aTreeModelEvent){
    for ( final TreeModelListener listener : fListeners ) {
      TLcdAWTUtil.invokeLater( new Runnable() {
        public void run() {
          listener.treeNodesChanged( aTreeModelEvent );
        }
      } );
    }
  }

  /**
   * Returns the amount of elements in a <code>ILcdModelTreeNode</code>
   * @param aModelTreeNode a given <code>ILcdModelTreeNode</code>
   * @return the amount of elements in a <code>ILcdModelTreeNode</code>
   */
  private static int countElements( ILcdModelTreeNode aModelTreeNode ) {
    Enumeration elementEnumeration = aModelTreeNode.elements();
    int count = 0;
    while ( elementEnumeration.hasMoreElements() ) {
      elementEnumeration.nextElement();
      count++;
    }
    return count;
  }

  /**
   * Returns the element at a given index inside a <code>ILcdModelTreeNode</code>
   * @param aModelTreeNode a given <code>ILcdModelTreeNode</code>
   * @param aIndex the index at which the element should be retrieved
   * @return The element at the index, or null if no element was found.
   */
  private static Object elementAt( ILcdModelTreeNode aModelTreeNode, int aIndex ) {
    if ( aIndex < 0 ) {
      return null;
    }
    Enumeration elementEnumeration = aModelTreeNode.elements();
    int count = 0;
    while ( elementEnumeration.hasMoreElements() ) {
      Object object = elementEnumeration.nextElement();
      if ( count == aIndex ) {
        return object;
      }
      count++;
    }
    return null;
  }

  /**
   * Determines whether or not a dynamic model has children.
   * @param aDynamicModel a valid <code>TLcdKML22DynamicModel</code>
   * @return true if a the dynamic model has at least one model or at least one element; false otherwise.
   */
  private boolean hasChildren( TLcdKML22DynamicModel aDynamicModel ) {
    return aDynamicModel.modelCount() > 0 ||
           aDynamicModel.elements().hasMoreElements();
  }

  /**
   * <p>Removes a <code>TreeModelObject</code> and all its children recursively from this tree model.
   * Also removes all attached listeners.</p>
   * @param aTreeModelObject a <code>TreeModelObject</code>
   */
  private synchronized void removeTreeModelObject(TreeModelObject aTreeModelObject){
    fTreeModelObjects.remove( aTreeModelObject );
    aTreeModelObject.removeModelNodeContainerListener( fModelContainerListener );
    aTreeModelObject.getParent().getChildren().remove( aTreeModelObject );
    ArrayList<TreeModelObject> children = new ArrayList<TreeModelObject>( aTreeModelObject.getChildren() );
    for ( TreeModelObject child : children ) {
      removeTreeModelObject( child );
    }
    aTreeModelObject.destroy();
  }

  /**
   * A layered listener that checks if a kml layer is added or removed.
   */
  private static class LayeredListener implements ILcdLayeredListener {
    private WeakReference<ModelNodeTreeModel> weakModelNodeTreeModel;

    /**
     * Creates a new <code>LayeredListener</code> instance.
     * @param aModelNodeTreeModel a reference to the <code>ModelNodeTreeModel<code> to update
     */
    private LayeredListener( ModelNodeTreeModel aModelNodeTreeModel ) {
      weakModelNodeTreeModel = new WeakReference<ModelNodeTreeModel>( aModelNodeTreeModel );
    }

    public void layeredStateChanged( TLcdLayeredEvent e ) {
      ModelNodeTreeModel modelNodeTreeModel = weakModelNodeTreeModel.get();
      if ( modelNodeTreeModel != null ) {
        if(e.getID()== TLcdLayeredEvent.LAYER_ADDED){
          if(e.getLayer().getModel() instanceof TLcdKML22RenderableModel ){
            modelNodeTreeModel.fRootContainer.add(0,  e.getLayer()  );
            Object child = modelNodeTreeModel.getChild( modelNodeTreeModel.getRoot(), 0 );
            modelNodeTreeModel.fireNodeAddedEvent(new TreeModelEvent(this,new Object[]{modelNodeTreeModel.getRoot()},
                                                   new int[]{0},
                                                   new Object[]{child}));
          }
        }else if(e.getID() == TLcdLayeredEvent.LAYER_REMOVED){
          if(e.getLayer().getModel() instanceof TLcdKML22RenderableModel){
            int index = modelNodeTreeModel.fRootContainer.indexOf( e.getLayer() );
            if ( index!=-1 ) {
              Object child = modelNodeTreeModel.getChild( modelNodeTreeModel.getRoot(), index );
              modelNodeTreeModel.fRootContainer.remove( e.getLayer() );
              modelNodeTreeModel.removeTreeModelObject( ( TreeModelObject ) child );
              modelNodeTreeModel.fireNodeRemovedEvent(new TreeModelEvent(this, new Object[]{modelNodeTreeModel.getRoot()},
                                                       new int[]{index},
                                                       new Object[]{ child}));
            }
          }
        }
      }else{
        e.getLayered().removeLayeredListener( this );
      }
    }
  }

  /**
   * An <code>ILcdModelContainerListener</code> that turns model container events into tree model
   * events. The <code>TreeModelEvent</code> instances are then given to the given <code>ModelNodeTreeModel</code>. 
   */
  private static class ModelUpdateListener implements ILcdModelContainerListener {
    private ModelNodeTreeModel fModelNodeTreeModel;

    public ModelUpdateListener( ModelNodeTreeModel aModelNodeTreeModel ) {
      fModelNodeTreeModel = aModelNodeTreeModel;
    }

    public void modelContainerStateChanged( TLcdModelContainerEvent aModelContainerEvent ) {
      if ( aModelContainerEvent instanceof ModelNodeContainerEvent) {
        switch(aModelContainerEvent.getID()){
          case TLcdModelContainerEvent.MODEL_ADDED:
            TreeModelEvent addedEvent = createAddedEvent( fModelNodeTreeModel, ( ModelNodeContainerEvent ) aModelContainerEvent );
            if ( addedEvent!=null ) {
              fModelNodeTreeModel.fireNodeAddedEvent( addedEvent );
            }
            break;
          case TLcdModelContainerEvent.MODEL_REMOVED:
            TreeModelEvent removedEvent = createRemovedEvent( fModelNodeTreeModel, ( ModelNodeContainerEvent ) aModelContainerEvent );
            if ( removedEvent!=null ) {
              fModelNodeTreeModel.fireNodeRemovedEvent( removedEvent );
            }
            break;
          case TLcdModelContainerEvent.CONTENT_CHANGED:
            TreeModelEvent changedEvent = createAddedEvent( fModelNodeTreeModel, ( ModelNodeContainerEvent ) aModelContainerEvent );
            if ( changedEvent!=null ) {
              fModelNodeTreeModel.fireNodeChangedEvent( changedEvent );
            }
            break;
        }
      }
    }

    /**
     * Creates an event that signals the removal of a model tree node. This event makes sure the indices and children are
     * the ones given before the removal. This method also synchronizes the <code>TreeModelObject</code> instances to remove
     * non-existent nodes from their internal data.
     * @param aModelNodeTreeModel a <code>ModelNodeTreeModel</code>
     * @param aModelContainerEvent a <code>ModelNodeContainerEvent</code>
     * @return a <code>TreeModelEvent</code> that can be used to signal any <code>TreeModelListener</code> that nodes
     * have been removed from aModelNodeTreeModel.
     */
    private TreeModelEvent createRemovedEvent( ModelNodeTreeModel aModelNodeTreeModel, ModelNodeContainerEvent aModelContainerEvent ) {
      synchronized ( aModelNodeTreeModel ) {
        TreeModelObject parent = aModelContainerEvent.getModelObject();
        if(parent.getChildren().size()==0){
          //this happens when a networklink higher in the hierarchy has been updated before this
          //networklink. In this case, the update has already happened and no further remove
          //is necessary
          return null;
        }

        Object[] treePath = createTreePath( parent ) ;



        ILcdModelTreeNode transformedDomainObject = ( ILcdModelTreeNode ) aModelNodeTreeModel.transformDomainObject( aModelContainerEvent.getModel() );
        int deletedChildCount = aModelNodeTreeModel.getModelTreeNodeChildCount( transformedDomainObject );
        IndexedNode[] nodes = new IndexedNode[deletedChildCount];

        //find the deleted children by creating a comparable object that you can test equality on
        for ( int i = 0; i < deletedChildCount; i++ ) {
          TreeModelObject childToTest = new TreeModelObject( aModelNodeTreeModel.getModelTreeNodeChild( transformedDomainObject, i ), parent.getModel(), parent.getLayer(), parent.getView(), parent );
          List<TreeModelObject> childList = parent.getChildren();
          for ( int j = 0; j < childList.size(); j++ ) {
            TreeModelObject child = childList.get( j );
            if( child.equals( childToTest )){
              nodes[i] = new IndexedNode( j,child);
            }
          }
        }
        //sort array
        Arrays.sort(nodes);

        //extract two seperate arrays
        TreeModelObject[] children = new TreeModelObject[deletedChildCount];
        int[] childIndices = new int[deletedChildCount];

        for ( int i = 0; i < nodes.length; i++ ) {
          IndexedNode node = nodes[ i ];
          children[i]=node.getTreeModelObject();
          childIndices[i]=node.getIndex();
        }

        //remove from children of parent
        for ( TreeModelObject child : children ) {
          aModelNodeTreeModel.removeTreeModelObject( child );
        }

        return new TreeModelEvent(this,treePath,childIndices,children);
      }
    }

    /**
     * Creates an event that signals the addition of a model tree node. This event makes sure the indices and children are
     * the ones given after the addition. This method also synchronizes the <code>TreeModelObject</code> instances to add
     * the new nodes to the model by invoking the {@linkplain ModelNodeTreeModel#getChild(Object, int)} method.
     * @param aModelNodeTreeModel a <code>ModelNodeTreeModel</code>
     * @param aModelContainerEvent a <code>ModelNodeContainerEvent</code>
     * @return a <code>TreeModelEvent</code> that can be used to signal any <code>TreeModelListener</code> that nodes
     * have been added to aModelNodeTreeModel.
     */
    private TreeModelEvent createAddedEvent( ModelNodeTreeModel aModelNodeTreeModel, ModelNodeContainerEvent aModelContainerEvent ) {
      synchronized ( aModelNodeTreeModel ) {
        Object[] treePath = createTreePath(aModelContainerEvent.getModelObject());
        int newChildCount = aModelNodeTreeModel.getChildCount( aModelContainerEvent.getModelObject() );
        Object[] children = new Object[newChildCount];
        int[] childIndices = new int[newChildCount];
        for ( int i = 0; i < newChildCount; i++ ) {
          children[i] = aModelNodeTreeModel.getChild( aModelContainerEvent.getModelObject(),i );
          childIndices[i] = i;
        }
        return new TreeModelEvent(this,treePath,childIndices,children);
      }
    }

    /**
     * Creates a tree path from the root node to the given <code>TreeModelObject</code>
     * @param aModelObject the final node in the desired tree path
     * @return a array of objects that represent a tree path
     */
    private Object[] createTreePath( TreeModelObject aModelObject ) {
      int depth = getDepth(aModelObject,1);
      TreeModelObject[] path = new TreeModelObject[depth];
      path[depth-1]=aModelObject;
      for(int i = depth-2;i>=0;i--){
        path[i] = path[i+1].getParent();
      }
      return path;
    }

    /**
     * <p>Gets the depth of the tree path of which the given element is the root. This
     * method works in a recursive way.</p>
     * @param aModelObject the final node in the desired tree path
     * @param depth the initial depth of the tree path. Should be 1 when called externally.
     * @return the depth of the desired tree path
     */
    private int getDepth( TreeModelObject aModelObject, int depth ) {
      if(aModelObject.getParent()!=null){
        return getDepth( aModelObject.getParent(),depth+1 );
      }else{
        return depth;
      }
    }
  }

  /**
   * A class that encapsulates a <code>TreeModelObject</code> and an index, for the purpose of
   * being able to sort them both at the same time.
   */
  private static class IndexedNode implements Comparable<IndexedNode>{
    private int fIndex;
    private TreeModelObject fTreeModelObject;

    /**
     * Creates a new <code>IndexedNode</code> with the given parameters
     * @param aIndex the index of the node in its parent node
     * @param aTreeModelObject the node to represent
     */
    public IndexedNode( int aIndex, TreeModelObject aTreeModelObject ) {
      fIndex = aIndex;
      fTreeModelObject = aTreeModelObject;
    }

    public int getIndex() {
      return fIndex;
    }

    public TreeModelObject getTreeModelObject() {
      return fTreeModelObject;
    }

    public int compareTo( IndexedNode o ) {
      return this.getIndex()-o.getIndex();
    }
  }
}
