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
package samples.decoder.ecdis.common;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.luciad.format.s57.TLcdS57CatalogueModelDescriptor;
import com.luciad.format.s57.TLcdS57ModelDescriptor;
import samples.common.dimensionalfilter.model.ADimensionalFilterProvider;
import samples.common.dimensionalfilter.model.DimensionalFilter;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFunction;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;

/**
 * An implementation of {@link DimensionalFilterProvider} for
 * S57 layers. This class creates time filters from the date values of visible ILcdS57Objects in a S57 model.
 */
public class S57DimensionalFilterProvider extends ADimensionalFilterProvider {

  /**
   * Keeps the track of filters created for each layer
   */
  private Map<Object, WeakReference<DimensionalFilter>> fFiltersMap = new TLcdWeakIdentityHashMap<>();

  @Override
  public boolean canHandleLayer(ILcdLayer aLayer, ILcdLayered aLayered) {
    return aLayer.getModel().getModelDescriptor() instanceof TLcdS57CatalogueModelDescriptor
           || aLayer.getModel().getModelDescriptor() instanceof TLcdS57ModelDescriptor;
  }

  @Override
  public List<DimensionalFilter> createFilters(final ILcdLayer aLayer, final ILcdLayered aLayered) {

    if (!(aLayered instanceof ILcdView)) {
      throw new IllegalArgumentException("Layered should be an instance of ILcdView: " + aLayered);
    }

    final DimensionalFilter filter = createFilter(aLayered, aLayer, Collections.<Date>emptyList());

    //This function will run with date values of visible ILcdS57Objects each time the view is panned/zoomed in or out.
    ILcdFunction updateFilterFunction = new ILcdFunction() {
      @Override
      public boolean applyOn(Object aObject) throws IllegalArgumentException {
        List<Date> possibleValues = (List<Date>) aObject;
        WeakReference<DimensionalFilter> weakFilterRef = fFiltersMap.get(aLayer);
        if (null == weakFilterRef) {
          return false;
        }
        DimensionalFilter oldFilter = weakFilterRef.get();
        //if possible values are changed because of the invalidation, create a new filter for the new possible values
        //and swap it with the older one
        if ((oldFilter == null && possibleValues.size() > 0) || (oldFilter != null && !oldFilter.getPossibleValues().equals(possibleValues))) {
          DimensionalFilter updatedFilter = createFilter(aLayered, aLayer, possibleValues);
          fFiltersMap.put(aLayer, new WeakReference<>(updatedFilter));
          firePropertyChange(new PropertyChangeEvent(S57DimensionalFilterProvider.this, UPDATE_FILTERS, Collections.singletonList(oldFilter), Collections.singletonList(updatedFilter)));
        }
        return false;
      }
    };
    fFiltersMap.put(aLayer, new WeakReference<>(filter));
    //register the function.
    S57DateFilterUtil.registerApplyOnCurrentObjects(aLayered, aLayer, updateFilterFunction);
    return Collections.singletonList(filter);
  }

  @SuppressWarnings("unchecked")
  private DimensionalFilter createFilter(ILcdLayered aLayered,
                                         final ILcdLayer aLayer, List<Date> aDates) {

    return new DimensionalFilter(aLayered, aLayer, Date.class, null, true, aDates) {

      @Override
      public void setFilterValue(Comparable aValue) {
        S52DisplaySettingsSingleton.getSettings().setDateFilterValue((Date) aValue);
      }

      @Override
      public String getName() {
        return "S57 Time Filter";
      }

      @Override
      public boolean canGetFilterValueFromModel(ILcdModel aModel) {
        return aModel == aLayer.getModel();
      }

      @Override
      public Comparable getFilterValueFromModel(ILcdModel aModel) {
        return S52DisplaySettingsSingleton.getSettings().getDateFilterValue();
      }
    };
  }
}
