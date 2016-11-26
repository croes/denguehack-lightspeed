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
package samples.decoder.kml22.common.timetoolbar.common;

import com.luciad.format.kml22.model.TLcdKML22RenderableModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.shape.ILcdTimeBounds;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.Enumeration;

/**
 * <p>The model representation of the timetoolbar</p>
 * <p>Note: This <code>TimeToolbarModel</code> works exclusively with {@linkplain TLcdKML22RenderableModel}</p>
 */
public abstract class TimeToolbarModel{
  private static final long DEFAULT_INTERVAL_LENGTH = 10;

  private long fGlobalBeginDate;
  private long fGlobalEndDate;
  private long fIntervalLength;
  private long fIntervalEndDate;
  private boolean fHasTimeData = false;

  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport( this );
  private TimeMediator fTimeMediator;

  /**
   * Creates a new <code>TimeToolbarModel</code> given a <code>ILcdLayered</code>. This constructor
   * will scan through the given <code>ILcdLayered</code> and register necessary listeners to make
   * this SimulatorModel completely self sufficient.
   * @param aLayered an <code>ILcdLayered</code>
   */
  public TimeToolbarModel( ILcdLayered aLayered) {
    //Create an inner time mediator that is used for calculating the time bounds of a
    //layered interface
    fTimeMediator = new TimeMediator( aLayered );
    //setup model listeners for all models in the layered interface
    setupModelListeners(aLayered);
    //setup a listener for the layered interface
    setupLayeredListener(aLayered);
    //initialize the parameters of this TimeToolbarModel
    initializeParameters( fTimeMediator );
  }

  /**
   * Returns a time mediator used by this TimeToolbarModel
   * @return a <code>TimeMediator</code>
   */
  public TimeMediator getTimeMediator() {
    return fTimeMediator;
  }

  /**
   * Setup the model listeners to each renderable model in the given layered interface
   * @param aLayered a <code>ILcdLayered</code>
   */
  private void setupModelListeners( ILcdLayered aLayered ) {
    Enumeration layers = aLayered.layers();
    while ( layers.hasMoreElements() ) {
      ILcdLayer layer = ( ILcdLayer ) layers.nextElement();
      if(layer.getModel() instanceof TLcdKML22RenderableModel){
        setupModelListener( layer.getModel() );
        layer.addPropertyChangeListener( new LayerVisibilityPropertyChangeListener( layer, this ) );
      }
    }
  }

  /**
   * Setup up the layered listener that listens to new layers being added
   * @param aLayered a layered that should be listened to
   */
  private void setupLayeredListener( ILcdLayered aLayered ) {
    aLayered.addLayeredListener( new LayeredListener(this) );
  }

  /**
   * Sets up a model listener for a single model
   * @param aModel a <code>ILcdModel</code>
   */
  private void setupModelListener( ILcdModel aModel){
    aModel.addModelListener( new ModelListener( this ) );
  }

  private void recalculateDates(){
    initializeParameters( fTimeMediator );
  }

  /**
   * Initializes the parameters of this timetoolbar, given a time mediator
   * @param aTimeMediator a valid <code>TimeMediator</code>
   */
  private void initializeParameters( TimeMediator aTimeMediator ) {
    ILcdTimeBounds usefulGlobalBounds = aTimeMediator.getAvailableTimeBounds();
    if ( usefulGlobalBounds.getBeginTimeBoundedness() == ILcdTimeBounds.Boundedness.UNDEFINED ||
         usefulGlobalBounds.getEndTimeBoundedness() == ILcdTimeBounds.Boundedness.UNDEFINED ||
         usefulGlobalBounds.getBeginTimeBoundedness() == ILcdTimeBounds.Boundedness.UNBOUNDED ||
         usefulGlobalBounds.getEndTimeBoundedness() == ILcdTimeBounds.Boundedness.UNBOUNDED ) {
      //if the returned bounds were not useful, do nothing
      setHasTimeData( false );
    }
    else {
      setGlobalBeginDate( usefulGlobalBounds.getBeginTime() );
      setGlobalEndDate( usefulGlobalBounds.getEndTime() );
      updateInterval( usefulGlobalBounds );
      setHasTimeData( true );
    }
  }

  /**
   * Updates the interval to match the given global bounds. If the interval is outside
   * of the given bounds, it will be put on the outer edges of the bounds. Otherwise,
   * it will not alter the bounds.
   * @param aGlobalBounds a global bounds to fit the interval into. It is assumed this
   *                      global bounds has boundedness "BOUNDED".
   * @see ILcdTimeBounds.Boundedness
   */
  private void updateInterval( ILcdTimeBounds aGlobalBounds ) {
    if(aGlobalBounds.getBeginTime()>getIntervalBeginDate() &&
       aGlobalBounds.getEndTime()>getIntervalEndDate() &&
       aGlobalBounds.getBeginTime()<getIntervalEndDate()){
      //if the end interval bounds are inside the range, but the begin bounds lie outside of it
      setIntervalLength( getIntervalEndDate()-aGlobalBounds.getBeginTime() );
    }else if(aGlobalBounds.getEndTime()<getIntervalEndDate() &&
             aGlobalBounds.getEndTime()<getIntervalBeginDate() &&
             aGlobalBounds.getBeginTime()>getIntervalEndDate()){
      //if the end interval bounds are outside of range, but the begin bounds lie inside of it.
      long begin = getIntervalBeginDate();
      setIntervalEndDate( aGlobalBounds.getEndTime() );
      setIntervalLength( getIntervalEndDate()-begin );
    }else if(aGlobalBounds.getEndTime()<getIntervalEndDate() &&
             aGlobalBounds.getBeginTime()>getIntervalBeginDate()){
      //if the end interval is outside of the range on the right, and the begin bounds is outside
      //of the range on the left.
      setIntervalEndDate( aGlobalBounds.getEndTime() );
      setIntervalLength( aGlobalBounds.getEndTime()-aGlobalBounds.getBeginTime() );
    }else if ( aGlobalBounds.getEndTime()<getIntervalEndDate() &&
               aGlobalBounds.getEndTime()<getIntervalBeginDate()) {
      //if the interval bounds are completely outside of the global bounds to the right
      setIntervalEndDate( aGlobalBounds.getEndTime() );
      setIntervalLength( DEFAULT_INTERVAL_LENGTH );
    }else if(aGlobalBounds.getBeginTime()>getIntervalBeginDate() &&
             aGlobalBounds.getBeginTime()>getIntervalEndDate()){
      //if the interval bounds are completely outside of the global bounds to the left
      setIntervalLength( DEFAULT_INTERVAL_LENGTH);
      setIntervalEndDate( aGlobalBounds.getBeginTime()+DEFAULT_INTERVAL_LENGTH );
    }
  }

  /**
   * A convenience method for retrieving the begin date of the interval.
   * @return the beginning of the interval
   */
  public long getIntervalBeginDate(){
    return getIntervalEndDate()-getIntervalLength();
  }


  public void addPropertyChangeListener( PropertyChangeListener aListener ) {
    fPropertyChangeSupport.addPropertyChangeListener( aListener );
  }

  public void removePropertyChangeListener( PropertyChangeListener aListener ) {
    fPropertyChangeSupport.removePropertyChangeListener( aListener );
  }

  public void firePropertyChange( final PropertyChangeEvent aEvent ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        fPropertyChangeSupport.firePropertyChange( aEvent );
      }
    } );
  }

  public long getGlobalBeginDate() {
    return fGlobalBeginDate;
  }

  public void setGlobalBeginDate( long aGlobalBeginDate ) {
    long oldValue = fGlobalBeginDate;
    fGlobalBeginDate = aGlobalBeginDate;
    firePropertyChange( new PropertyChangeEvent( this, "globalBeginDate", oldValue, fGlobalBeginDate ) );
  }

  public void setGlobalEndDate( long aGlobalEndDate ) {
    long oldValue = fGlobalEndDate;
    fGlobalEndDate = aGlobalEndDate;
    firePropertyChange( new PropertyChangeEvent( this, "globalEndDate", oldValue, fGlobalEndDate ) );
  }

  public void setIntervalLength( long aIntervalLength ) {
    long oldValue = fIntervalLength;
    fIntervalLength = aIntervalLength;
    firePropertyChange( new PropertyChangeEvent( this, "intervalLength", oldValue, fIntervalLength ) );
  }

  public void setIntervalEndDate( long aIntervalEndDate ) {
    long oldValue = fIntervalEndDate;
    fIntervalEndDate = aIntervalEndDate;
    firePropertyChange( new PropertyChangeEvent( this, "intervalEndDate", oldValue, fIntervalEndDate ) );
  }

  public void setHasTimeData( boolean aHasTimeData ) {
    boolean oldValue = fHasTimeData;
    fHasTimeData = aHasTimeData;
    firePropertyChange( new PropertyChangeEvent( this, "hasTimeData", oldValue, fHasTimeData ) );
  }

  public long getGlobalEndDate() {
    return fGlobalEndDate;
  }

  public long getIntervalLength() {
    return fIntervalLength;
  }

  public long getIntervalEndDate() {
    return fIntervalEndDate;
  }

  public boolean hasTimeData() {
    return fHasTimeData;
  }

  /**
   * Listens to a layered, and attaches model listeners to new layers.
   * Also recalculates the time dates when needed.
   */
  private static class LayeredListener implements ILcdLayeredListener {
    private WeakReference<TimeToolbarModel> fTimeToolbarModel;

    public LayeredListener( TimeToolbarModel aTimeToolbarModel ) {
      fTimeToolbarModel = new WeakReference<TimeToolbarModel>(aTimeToolbarModel);
    }

    public void layeredStateChanged( TLcdLayeredEvent e ) {
      TimeToolbarModel model = fTimeToolbarModel.get();
      if ( model!=null ) {
        handleEvent( e , model);
      }else{
        e.getLayered().removeLayeredListener( this );
      }
    }

    private void handleEvent( TLcdLayeredEvent e,TimeToolbarModel aToolbarModel ) {
      if ( e.getLayer().getModel() instanceof TLcdKML22RenderableModel ) {
        if ( e.getID() == TLcdLayeredEvent.LAYER_ADDED ) {
          e.getLayer().getModel().addModelListener( new ModelListener( aToolbarModel ) );
          e.getLayer().addPropertyChangeListener( new LayerVisibilityPropertyChangeListener( e.getLayer(), aToolbarModel ) );
          aToolbarModel.recalculateDates();
        }
        else if ( e.getID() == TLcdLayeredEvent.LAYER_REMOVED ) {
          aToolbarModel.recalculateDates();
        }
      }
    }
  }

  /**
   * Listens to changes in a <code>ILcdModel</code>, and updates the <code>TimeToolbarModel</code> accordingly.
   */
  private static class ModelListener implements ILcdModelListener {
    private WeakReference<TimeToolbarModel> fTimeToolbarModel;

    public ModelListener( TimeToolbarModel aTimeToolbarModel ) {
      fTimeToolbarModel = new WeakReference<TimeToolbarModel>( aTimeToolbarModel );
    }

    public void modelChanged( TLcdModelChangedEvent aEvent ) {
      TimeToolbarModel model = fTimeToolbarModel.get();
      if(model!=null){
        handleEvent(aEvent,model );
      } else{
        aEvent.getModel().removeModelListener( this );
      }
    }

    private void handleEvent( TLcdModelChangedEvent aEvent,TimeToolbarModel aToolbarModel  ) {
      aToolbarModel.recalculateDates();
      aToolbarModel.firePropertyChange( new PropertyChangeEvent( this, "modelChanged", null,null) );
    }
  }

  /**
   * Listens to changes in visibility on a layer, and updates the dates of the given
   * <code>TimeToolbarModel</code>.
   */
  private static class LayerVisibilityPropertyChangeListener implements PropertyChangeListener {
    private WeakReference<ILcdLayer> fWeakLayer;
    private WeakReference<TimeToolbarModel> fTimeToolbarModel;

      /**
     * <p>Creates a {@link PropertyChangeListener} that recalculates the global time
     * whenever the visibility of a layer changes</p>
     *
     * @param aLayer       The layer who's visibility might change.
     * @param aTimeManager The time manager to update.
     */
    public LayerVisibilityPropertyChangeListener( ILcdLayer aLayer, TimeToolbarModel aTimeManager ) {
      fWeakLayer = new WeakReference<ILcdLayer>( aLayer );
      fTimeToolbarModel = new WeakReference<TimeToolbarModel>( aTimeManager );
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      ILcdLayer layer = fWeakLayer.get();
      TimeToolbarModel toolbarModel = fTimeToolbarModel.get();
      if ( toolbarModel != null ) {
        if ( "visible".equals( evt.getPropertyName() ) ) {
          toolbarModel.recalculateDates();
        }
      }else{
        if(layer!=null){
          layer.removePropertyChangeListener( this );
        }
      }
    }
  }
}
