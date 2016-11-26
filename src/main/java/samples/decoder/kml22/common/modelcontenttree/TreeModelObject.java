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
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainerListener;
import com.luciad.model.TLcdModelContainerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Represents a tree model object</p>
 * <p>This class is used by the JTree of the model content tree in each of its nodes</p>
 * <p>A tree model object acts a wrapper around an object that is part of the KML hierarchy. This object can be retrieved through
 * the {@linkplain #getTreeModelObject()} method. An instance of this class also contains information on the model, layer and
 * view that contain the object.</p>
 * @see #getTreeModelObject()
 * @since 10.0
 */
public class TreeModelObject {
  private Object fTreeModelObject;
  private ILcdLayer fLayer;
  private ILcdModel fModel;
  private ILcdView fView;
  private TreeModelObject fParent;
  private List<TreeModelObject> fChildren;
  private Map<ILcdModelContainerListener, ModelNodeContainerListener> fListenerMap = new HashMap<ILcdModelContainerListener, ModelNodeContainerListener>();

  /**
   * Creates a new tree model object
   * @param aTreeModelObject A tree model object. See {@linkplain #getTreeModelObject()} for more information.
   * @param aModel A model that is directly contained by <code>aLayer</code>. This is always an instance of {@linkplain com.luciad.format.kml22.model.TLcdKML22RenderableModel}
   * @param aLayer A KML layer
   * @param aView A view that directly contains <code>aLayer</code>
   * @param aParent The parent node of this <code>TreeModelObject</code>
   */
  public TreeModelObject( Object aTreeModelObject, ILcdModel aModel, ILcdLayer aLayer, ILcdView aView, TreeModelObject aParent ) {
    fTreeModelObject = aTreeModelObject;
    fLayer = aLayer;
    fModel = aModel;
    fView = aView;
    fParent = aParent;
    fChildren = Collections.synchronizedList( new ArrayList<TreeModelObject>( ) );
  }

  /**
   * <p>Returns an object that represents an element in the model content tree of KML.</p>
   * <p>This is always one of the following:</p>
   * <ul>
   *  <li>{@linkplain TLcdKML22DynamicModel} for all non-leaf elements, such as KML containers.</li>
   *  <li>{@linkplain com.luciad.format.kml22.model.feature.TLcdKML22PaintableGroundOverlay} for all leaf-elements that represent <code>GroundOverlay</code>s.
   *  <li>{@linkplain com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature} for all other leaf elements.</li>
   * </ul>
   * @return a tree model object
   */
  public Object getTreeModelObject() {
    return fTreeModelObject;
  }

  /**
   * <p>Returns a layer that has the following properties:</p>
   * <ul>
   *  <li>It is contained by the <code>ILcdView</code> returned by {@linkplain #getView()}  </li>
   *  <li>It contains the <code>ILcdModel</code> returned by {@linkplain #getModel()}</li>
   * </ul>
   * @return a KML layer
   */
  public ILcdLayer getLayer() {
    return fLayer;
  }

  /**
   * <p>Returns a layer that has the following properties:</p>
   * <ul>
   *  <li>It is contained by the <code>ILcdLayer</code> returned by {@linkplain #getLayer()} ()}  </li>
   *  <li>It contains the tree model object returned by {@linkplain #getTreeModelObject()}} somewhere in its underlying hierarchy</li>
   * </ul>
   * @return a KML layer
   */
  public ILcdModel getModel() {
    return fModel;
  }

  /**
   * <p>Returns a layer that has the following properties:</p>
   * <ul>
   *  <li>It contains the <code>ILcdLyaer</code> returned by {@linkplain #getLayer()} ()}</li>
   * </ul>
   * @return a KML layer
   */
  public ILcdView getView() {
    return fView;
  }

  /**
   * <p>Returns the parent of this <code>TreeModelObject</code></p>
   * @return The parent of this <code>TreeModelObject</code>; null if this node is
   *         the root node and has no parent.
   */
  public TreeModelObject getParent() {
    return fParent;
  }

  /**
   * <p>Gets the children of this <code>TreeModelObject</code></p>
   * <p>If a child is added or removed, this list should be updated as well</p>
   * @return the children of this <code>TreeModelObject</code>
   */
  public List<TreeModelObject> getChildren() {
    return fChildren;
  }

  /**
   * Adds a <code>ILCdModelContainerListener</code> to this <code>TreeModelObject</code>. The
   * model container listener will be given <code>ModelNodeContainerEvent</code> instances.
   * @param aModelContainerListener a model container listener
   * @see samples.decoder.kml22.common.modelcontenttree.ModelNodeContainerEvent
   */
  public synchronized void addModelNodeContainerListener(ILcdModelContainerListener aModelContainerListener){
    if(fTreeModelObject instanceof TLcdKML22DynamicModel ){
      ModelNodeContainerListener listener = new ModelNodeContainerListener( aModelContainerListener, this, ( TLcdKML22DynamicModel ) fTreeModelObject );
      ( ( TLcdKML22DynamicModel ) fTreeModelObject ).addModelContainerListener( listener );
      fListenerMap.put(aModelContainerListener,listener);
    }
  }

  /**
   * Removes a <code>ILcdModelContainerListener</code> from this <code>TreeModelobject</code>.
   * @param aModelContainerListener a model container listener
   * @see #addModelNodeContainerListener(ILcdModelContainerListener)
   */
  public synchronized void removeModelNodeContainerListener(ILcdModelContainerListener aModelContainerListener){
    if(fListenerMap.containsKey( aModelContainerListener )){
      ModelNodeContainerListener listener = fListenerMap.remove( aModelContainerListener );
      TLcdKML22DynamicModel dynamicModel = listener.getDynamicModel();
      if(dynamicModel!=null){
        dynamicModel.removeModelContainerListener( listener );
      }
    }
  }
  
  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof TreeModelObject ) ) return false;

    TreeModelObject that = ( TreeModelObject ) o;

    return !( fLayer != null ? !fLayer.equals( that.fLayer ) : that.fLayer != null )
           && !( fModel != null ? !fModel.equals( that.fModel ) : that.fModel != null )
           && !( fTreeModelObject != null ? !fTreeModelObject.equals( that.fTreeModelObject ) : that.fTreeModelObject != null )
           && !( fView != null ? !fView.equals( that.fView ) : that.fView != null );

  }

  @Override
  public int hashCode() {
    int result = fTreeModelObject != null ? fTreeModelObject.hashCode() : 0;
    result = 31 * result + ( fLayer != null ? fLayer.hashCode() : 0 );
    result = 31 * result + ( fModel != null ? fModel.hashCode() : 0 );
    result = 31 * result + ( fView != null ? fView.hashCode() : 0 );
    return result;
  }

  /**
   * Destroys this tree model object by removing all internal state. The tree model object is no longer
   * valid after this call.
   */
  public void destroy() {
    fLayer = null;
    fTreeModelObject = null;
    fLayer = null;
    fModel = null;
    fView = null;
    fParent = null;
    fChildren = null;
    if (fListenerMap != null) {
      fListenerMap.clear();
    }
    fListenerMap = null;
  }

  /**
   * An inner model container listener that converts <code>TLcdModelContainerEvents</code> events
   * into <code>ModelNodeContainerEvent</code> instances.
   */
  private static class ModelNodeContainerListener implements ILcdModelContainerListener {
    private ILcdModelContainerListener fModelContainerListener;
    private TreeModelObject fTreeModelObject;
    private WeakReference<TLcdKML22DynamicModel> fModelObject;

    public ModelNodeContainerListener( ILcdModelContainerListener aModelContainerListener, TreeModelObject aTreeModelObject, TLcdKML22DynamicModel aModelObject ) {
      fModelContainerListener = aModelContainerListener;
      fTreeModelObject = aTreeModelObject;
      fModelObject = new WeakReference<TLcdKML22DynamicModel>(aModelObject);
    }

    public void modelContainerStateChanged( TLcdModelContainerEvent aModelContainerEvent ) {
      TLcdKML22DynamicModel dynamicModel = fModelObject.get();
      if ( dynamicModel!=null ) {
        fModelContainerListener.modelContainerStateChanged( new ModelNodeContainerEvent( fTreeModelObject,aModelContainerEvent.getModelContainer(), aModelContainerEvent.getID(), aModelContainerEvent.getModel() ) );
      }else{
        aModelContainerEvent.getModelContainer().removeModelContainerListener( this );
      }
    }

    public TLcdKML22DynamicModel getDynamicModel(){
      return fModelObject.get();
    }

  }
}
