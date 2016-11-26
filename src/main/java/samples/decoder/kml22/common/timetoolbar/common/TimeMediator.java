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
import com.luciad.shape.ILcdTimeBounded;
import com.luciad.shape.ILcdTimeBounds;
import com.luciad.shape.TLcdTimeBounds;
import com.luciad.shape.TLcdTimeBoundsUtil;
import com.luciad.view.ILcdLayered;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * <p>A mediator for all time related aspects of KML</p>
 * <p>It has the ability to set and to retrieve time for all KML models in a given <code>ILcdLayered</code></p>
 * <p>Note: This class works exclusively with <code>TLcdKML22RenderableModel</code> instances</p>
 */
public class TimeMediator {
  private ILcdLayered fLayered;
  private TLcdTimeBounds fTimeBounds = new TLcdTimeBounds();

  /**
   * <p>Creates a new <code>TimeMediator</code> instance.</p>
   * @param aLayered an <code>ILcdLayered</code> instance that contains KML layers.
   */
  public TimeMediator( ILcdLayered aLayered ) {
    fTimeBounds.setBeginTimeBoundedness( ILcdTimeBounds.Boundedness.UNBOUNDED );
    fTimeBounds.setEndTimeBoundedness( ILcdTimeBounds.Boundedness.UNBOUNDED );
    fLayered = aLayered;
  }

  /**
   * Returns the layered that is associated with this <code>TimeMediator</code>
   * @return a <code>ILcdLayered</code>
   */
  public ILcdLayered getLayered() {
    return fLayered;
  }

  /**
   * Sets the <code>ILcdLayered</code> to perform actions on.
   * @param aLayered a <code>ILcdLayered</code>
   */
  public void setLayered( ILcdLayered aLayered ) {
    fLayered = aLayered;
  }

  /**
   * <p>Sets the time bounds for this <code>TimeMediator</code></p>
   * <p>The method will also set the time bounds of all {@linkplain TLcdKML22RenderableModel} instances found in the
    * <code>ILcdLayered</code> of this <code>TimeMediator</code> </p>
   * @param aTimeBounds a valid {@link ILcdTimeBounds}
   */
  public void setTimeBounds( ILcdTimeBounds aTimeBounds ) {
    fTimeBounds = new TLcdTimeBounds( aTimeBounds );
    setTimeOnModels();
  }

  /**
   * <p>Returns the time bounds set by this <code>TimeMediator</code></p>
   * @return the time bounds set by this <code>TimeMediator</code>
   */
  public TLcdTimeBounds getTimeBounds() {
    return fTimeBounds;
  }

  /**
   * <p>Fits all models of the <code>ILcdLayered</code> instance to the available time bounds
   * found with {@link #getAvailableTimeBounds()} ()})</p>
   */
  public void fitOnAvailableTimeBounds() {
    fTimeBounds = new TLcdTimeBounds( getAvailableTimeBounds() );
    setTimeOnModels();
  }

  /**
   * <p>Retrieves the useful global time bounds of the given <code>ILcdLayered</code> instance. The useful
   * time bounds is the smallest subset of the global time bounds that contains changes. This means that
   * the returned {@link ILcdTimeBounds} won't feature any unbounded directions.
   * </p>
   * <p>
   * For more information,
   * please look at {@link TLcdTimeBoundsUtil#union(ILcdTimeBounds, ILcdTimeBounds, boolean)}
   * </p>
   * @return Returns the global useful bounds. If no valid time bounds were found, it will return an instance of {@link ILcdTimeBounds} with
   * undefined bounds.
   */
  public ILcdTimeBounds getAvailableTimeBounds() {
    ILcdTimeBounds unionBounds = null;
    for ( TLcdKML22RenderableModel model : getModels(false) ) {
      ILcdTimeBounds bounds = model.getTimeBounds( null, null, null, true, true, null, true );
      if (bounds != null &&
          bounds.getBeginTimeBoundedness() != ILcdTimeBounds.Boundedness.UNDEFINED &&
          bounds.getEndTimeBoundedness() != ILcdTimeBounds.Boundedness.UNDEFINED) {
        unionBounds = unionBounds == null ? bounds : TLcdTimeBoundsUtil.union( unionBounds, bounds, true );
      }
    }
    return unionBounds != null ? unionBounds : new TLcdTimeBounds( );
  }

  private void setTimeOnModels() {
    for ( TLcdKML22RenderableModel model : getModels(true) ) {
      model.setDefaultFilterTimeBounds( fTimeBounds );
    }
  }

  /**
   * <p>Gets all ordered models in the layered view.</p>
   * @param aIgnoreLayerVisibility If set to true, it will not take layer visibility into account, if
   *                               set to false, it will not return models inside hidden layers.
   * @return A list of ordered models in the given <code>ILcdLayered</code> instance.
   */
  public List<TLcdKML22RenderableModel> getModels(boolean aIgnoreLayerVisibility) {
    List<TLcdKML22RenderableModel> models = new ArrayList<TLcdKML22RenderableModel>();
    for ( int i = 0; i < fLayered.layerCount(); i++ ) {
      ILcdModel model = fLayered.getLayer( i ).getModel();
      if ( (aIgnoreLayerVisibility || fLayered.getLayer( i ).isVisible())
           && model instanceof TLcdKML22RenderableModel ) {
        models.add( ( TLcdKML22RenderableModel ) model );
      }
    }
    return models;
  }

  /**
   * <p>
   * Method retrieves all valid time bounds of a given <code>ILcdLayered</code> instance. The
   * valid time bounds represents all time bounds which contain some time data.
   * </p>
   * <p>
   * <b>Note:</b> Only returns bounds that are bounded in both directions.
   * </p>
   * @return A list of <code>ILcdTimeBounds</code> that contain valid time data.
   */
  public List<ILcdTimeBounds> getAllValidTimeBounds(){
    List<TLcdKML22RenderableModel> models = getModels(false);
    List<ILcdTimeBounds> timeBoundedList = new ArrayList<ILcdTimeBounds>( );
    for ( TLcdKML22RenderableModel model : models ) {
      Enumeration elements = model.elements(null,null,null,null,true,false,false);
      while ( elements.hasMoreElements() ) {
        Object o = elements.nextElement();
        if ( o instanceof ILcdTimeBounded ) {
          ILcdTimeBounded timeBounded = ( ILcdTimeBounded ) o;
          if(timeBounded.getTimeBounds().getBeginTimeBoundedness() == ILcdTimeBounds.Boundedness.BOUNDED &&
             timeBounded.getTimeBounds().getEndTimeBoundedness()   == ILcdTimeBounds.Boundedness.BOUNDED){
            timeBoundedList.add( timeBounded.getTimeBounds() );
          }
        }
      }
    }
    return timeBoundedList;
  }

}
