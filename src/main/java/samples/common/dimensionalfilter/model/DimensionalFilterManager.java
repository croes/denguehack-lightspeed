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
package samples.common.dimensionalfilter.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.*;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * The dimensional filter manager provides an overview of and a means to change all dimensional filters that are applicable to the registered
 * set of layers.Dimensional Filters are grouped per type (e.g. depth, time,...). This allows you to for example implement a UI
 * panel with a slider for every filter type.
 * <p/>
 * To use this class:
 * <ol>
 *   <li>implement a {@link DimensionalFilterProvider}: this provider will create instances of {@link DimensionalFilter} for a format
 *   specific layer type</li>
 *   <li>create an instance of this class, passing the provider to the constructor</li>
 *   <li>{@linkplain #registerLayer register} the layer that needs filtering/slicing</li>
 * </ol>
 *
 * @since 2015.0
 */
public class DimensionalFilterManager {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(DimensionalFilterManager.class);
  public static final String FILTER_GROUPS_PROPERTY_NAME = "filterGroups";

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  private final Map<DimensionalFilterGroup, FilterGroupState> fGroups = new HashMap<>();
  private final Map<DimensionalFilterGroup, FilterGroupState> fInvisibleGroups = new HashMap<>();
  private final DimensionalFilterProvider fDimensionalFilterProvider;

  private final Map<ILcdLayer, List<VisibilityPropertyChangeListener>> fVisibilityListeners = new WeakHashMap<>();
  private final Map<ILcdLayer, ModelFilterValueChangeListener> fModelValueListeners = new WeakHashMap<>();

  public static final String MIN_PROPERTY_NAME = "min";
  public static final String MAX_PROPERTY_NAME = "max";
  public static final String CURRENT_PROPERTY_NAME = "current";
  public static final String POSSIBLE_VALUES_PROPERTY_NAME = "possibleValues";

  /**
   * Creates a new service.
   *
   * @param aDimensionalFilterProvider the filter provider used to retrieve {@link DimensionalFilter Filter} implementations for a layer.
   */
  public DimensionalFilterManager(DimensionalFilterProvider aDimensionalFilterProvider) {
    fDimensionalFilterProvider = aDimensionalFilterProvider;
    fDimensionalFilterProvider.addPropertyChangeListener(new WeakFilterProviderListener(this));
  }

  /**
   * Registers the given property change listener.
   *
   * @param aListener a listener
   */
  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    if (aListener == null) {
      return;
    }
    fPropertyChangeSupport.addPropertyChangeListener(aListener);
  }

  /**
   * Unregisters the given property change listener.
   *
   * @param aListener a listener.
   */
  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    if (aListener == null) {
      return;
    }
    fPropertyChangeSupport.removePropertyChangeListener(aListener);
  }

  /**
   * Returns the currently visible filter groups (i.e.<!-- --> depth, time, ...)
   * @return the filter groups
   */
  public List<DimensionalFilterGroup> getFilterGroups() {
    ArrayList<DimensionalFilterGroup> list = new ArrayList<>(fGroups.keySet());
    Collections.sort(list);
    return list;
  }

  /**
   * Returns all of the filter groups (i.e.<!-- --> depth, time, ...)
   * @return the filter groups
   */
  public List<DimensionalFilterGroup> getAllFilterGroups() {
    ArrayList<DimensionalFilterGroup> list = new ArrayList<>(fGroups.keySet());
    list.addAll(fInvisibleGroups.keySet());
    Collections.sort(list);
    return list;
  }

  /**
   * Returns if there is any visible filter
   * @return true if there is any visible filter, false otherwise
   */
  public boolean hasVisibleFilters() {
    Collection<FilterGroupState> groupStates = fGroups.values();
    for (FilterGroupState groupState : groupStates) {
      if (!groupState.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Register a layer to the service. If the service has a factory that can handle the layer, the factory will create
   * a {@link DimensionalFilter} with the given layer as a target.
   *
   * @param aLayer a layer whose content may be filtered.
   * @param aLayered the view for which the filtering applies
   * @return <code>true</code> if the layer was effectively registered, <code>false</code> otherwise.
   */
  public boolean registerLayer(ILcdLayer aLayer, ILcdLayered aLayered) {
    List<VisibilityPropertyChangeListener> listeners = fVisibilityListeners.get(aLayer);
    if (listeners == null) {
      listeners = new ArrayList<>();
      fVisibilityListeners.put(aLayer, listeners);
    }
    VisibilityPropertyChangeListener listener = new VisibilityPropertyChangeListener(this, aLayered);
    listeners.add(listener);
    aLayer.addPropertyChangeListener(listener);
    return registerLayerImpl(aLayer, aLayered);
  }

  /**
   * Unregister the given layer. All {@link DimensionalFilter Filter} instances whose target is the given layer will be removed
   * from the filter group.
   *
   * @param aLayer a layer whose content may be filtered.
   * @param aLayered the view for which the filtering applies
   * @return <code>true</code> if some {@link DimensionalFilterGroup} was removed
   * as a side-effect of the layer being unregistered.
   */
  public boolean unregisterLayer(ILcdLayer aLayer, ILcdLayered aLayered) {
    List<VisibilityPropertyChangeListener> listeners = fVisibilityListeners.get(aLayer);
    if (listeners != null) {
      Iterator<VisibilityPropertyChangeListener> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        VisibilityPropertyChangeListener listener = iterator.next();
        if (listener.getLayered() == aLayered) {
          iterator.remove();
          aLayer.removePropertyChangeListener(listener);
        }
      }
    }
    return unregisterLayerImpl(aLayer);
  }

  /**
   * @param aDimensionalFilterGroup filter group whose max int value is requested
   * @return max integer value (max position of the slider) of the specified filter group,
   *         0 if aFilterGroup is null or is not added to this manager
   */
  public int getMaxValueInt(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? 0 : getFilterGroupState(aDimensionalFilterGroup).getIntegerMax();
  }

  /**
   * @param aDimensionalFilterGroup filter group whose min int value is requested
   * @return min integer value (min position of the slider) of the specified filter group,
   *         0 if aFilterGroup is null or is not added to this manager
   */
  public int getMinValueInt(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? 0 : getFilterGroupState(aDimensionalFilterGroup).getIntegerMin();
  }

  /**
   * @param aDimensionalFilterGroup filter group whose min value is requested
   * @return min value of the specified filter group,
   *         null if aFilterGroup is null or is not added to this manager
   */
  public Comparable getMinValue(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? null : getFilterGroupState(aDimensionalFilterGroup).getMinValue();
  }

  /**
   * @param aDimensionalFilterGroup filter group whose max value is requested
   * @return max value of the specified filter group,
   *         null if aFilterGroup is null or is not added to this manager
   */
  public Comparable getMaxValue(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? null : getFilterGroupState(aDimensionalFilterGroup).getMaxValue();
  }

  /**
   * @param aDimensionalFilterGroup filter group whose current int value is requested
   * @return current int value (current position of the slider) of the specified filter group,
   *         0 if aFilterGroup is null or is not added to this manager
   */
  public int getCurrentValueInt(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? 0 : getFilterGroupState(aDimensionalFilterGroup).getIntegerValue();
  }

  /**
   * @param aDimensionalFilterGroup filter group whose value by index is requested
   * @param aValue index value (slider position) of the filter group
   * @return value on index aValue of the specified filter group,
   *         null if aFilterGroup is null or is not added to this manager
   */
  public Comparable getValue(DimensionalFilterGroup aDimensionalFilterGroup, int aValue) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? null : getFilterGroupState(aDimensionalFilterGroup).getValue(aValue);
  }

  /**
   * @param aDimensionalFilterGroup filter group whose current value is requested
   * @return current value of the specified filter group,
   *         null if aFilterGroup is null or is not added to this manager
   */
  public Comparable getCurrentValue(DimensionalFilterGroup aDimensionalFilterGroup) {
    return aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null ? null : getFilterGroupState(aDimensionalFilterGroup).getCurrentValue();
  }

  /**
   * Returns the name of all layers which are filtered by aFilterGroup
   * @param aDimensionalFilterGroup the filter group
   * @return the name of all layers which are filtered by aFilterGroup
   */
  public List<String> getFilterTargetNames(DimensionalFilterGroup aDimensionalFilterGroup) {
    return (aDimensionalFilterGroup == null || getFilterGroupState(aDimensionalFilterGroup) == null) ?
           Collections.<String>emptyList() : getFilterGroupState(aDimensionalFilterGroup).getFilterTargetNames();
  }

  /**
   * Sets the Integer value for aFilterGroup.
   * @param aDimensionalFilterGroup the filterer group for which to set the Integer value.
   * @param aValue the new Integer value
   */
  public void setIntegerValue(DimensionalFilterGroup aDimensionalFilterGroup, int aValue) {
    if (aDimensionalFilterGroup != null && fGroups.get(aDimensionalFilterGroup) != null) {
      fGroups.get(aDimensionalFilterGroup).setIntegerValue(aValue);
    }
  }

  /**
   * Adds a new PropertyChangeListener to listen to changes to the given group of filters.
   */
  public void addPropertyChangeListener(DimensionalFilterGroup aDimensionalFilterGroup, PropertyChangeListener aPropertyChangeListener) {
    if (aDimensionalFilterGroup != null && getFilterGroupState(aDimensionalFilterGroup) != null) {
      getFilterGroupState(aDimensionalFilterGroup).addPropertyChangeListener(aPropertyChangeListener);
    }
  }

  public void removePropertyChangeListener(DimensionalFilterGroup aDimensionalFilterGroup, PropertyChangeListener aPropertyChangeListener) {
    if (aDimensionalFilterGroup != null) {
      if (getFilterGroupState(aDimensionalFilterGroup) != null) {
        getFilterGroupState(aDimensionalFilterGroup).removePropertyChangeListener(aPropertyChangeListener);
      }
    }
  }

  private FilterGroupState getFilterGroupState(DimensionalFilterGroup aDimensionalFilterGroup) {
    FilterGroupState filterGroupState = fGroups.get(aDimensionalFilterGroup);
    if (filterGroupState == null) {
      filterGroupState = fInvisibleGroups.get(aDimensionalFilterGroup);
    }
    return filterGroupState;
  }

  /**
   * Stores the current value for a filter group and triggers the filters if it changes.
   */
  private static class FilterGroupState {

    private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(DimensionalFilterGroup.class);

    private final DimensionalFilterGroup fDimensionalFilterGroup;
    private final List<DimensionalFilter> fDimensionalFilters = new ArrayList<>();
    private final Set<DimensionalFilter> fInvisibleDimensionalFilters = new HashSet<>();
    private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
    private final List<Comparable> fPossibleValues = new ArrayList<>();
    private Comparable fCurrent;
    private int fCurrentInt;

    private FilterGroupState(DimensionalFilterGroup aDimensionalFilterGroup) {
      fDimensionalFilterGroup = aDimensionalFilterGroup;
      setIntegerValue(0);
    }

    private Comparable convertToValue(int aCurrentInt) {
      return fPossibleValues.get(aCurrentInt);
    }

    private int convertToInt(Comparable aValue) {
      int index = 0;
      for (Comparable possibleValue : fPossibleValues) {
        if (possibleValue.equals(aValue)) {
          return index;
        }
        index++;
      }
      return -1;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addFilter(DimensionalFilter aListener, boolean aVisible) {
      //don't add null filters or filters without possible values
      if (aListener == null || aListener.getPossibleValues() == null || aListener.getPossibleValues().size() == 0) {
        return;
      }
      if (aVisible) {
        fDimensionalFilters.add(aListener);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(fDimensionalFilterGroup.toString() + ": Registered new filter. New filter count = " + fDimensionalFilters.size());
        }
        updateMeasureValues();
      } else {
        fInvisibleDimensionalFilters.add(aListener);
      }
    }

    public void restoreInvisibleFilters(ILcdFilter<DimensionalFilter> aFilter) {
      boolean updated = false;
      for (Iterator<DimensionalFilter> iterator = fInvisibleDimensionalFilters.iterator(); iterator.hasNext(); ) {
        DimensionalFilter filter = iterator.next();
        if (aFilter.accept(filter)) {
          iterator.remove();
          fDimensionalFilters.add(filter);
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(fDimensionalFilterGroup.toString() + ": Registered new filter. New filter count = " + fDimensionalFilters.size());
          }
          updated = true;
        }
      }
      if (updated) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(fDimensionalFilterGroup.toString() + ": Registered new filter. New filter count = " + fDimensionalFilters.size());
        }
        updateMeasureValues();
      }
    }

    public void makeFiltersInvisible(ILcdFilter<DimensionalFilter> aFilter) {
      boolean removed = false;
      for (Iterator<DimensionalFilter> iterator = fDimensionalFilters.iterator(); iterator.hasNext(); ) {
        DimensionalFilter next = iterator.next();
        if (aFilter.accept(next)) {
          iterator.remove();
          fInvisibleDimensionalFilters.add(next);
          removed = true;
        }
      }
      if (removed) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Removed listener. New listener count = " + fDimensionalFilters.size());
        }
        updateMeasureValues();
      }
    }

    public boolean removeFilters(ILcdFilter<DimensionalFilter> aFilter, boolean aMakeInvisible) {
      boolean removed = false;
      for (Iterator<DimensionalFilter> iterator = fDimensionalFilters.iterator(); iterator.hasNext(); ) {
        DimensionalFilter next = iterator.next();
        if (aFilter.accept(next)) {
          iterator.remove();
          if (aMakeInvisible) {
            fInvisibleDimensionalFilters.add(next);
          }
          removed = true;
        }
      }

      //if we really want to remove (not make visible) the filters, we will remove those from invisiblefilters if they are in the invisible list.
      if (!aMakeInvisible) {
        for (Iterator<DimensionalFilter> iterator = fInvisibleDimensionalFilters.iterator(); iterator.hasNext(); ) {
          DimensionalFilter next = iterator.next();
          if (aFilter.accept(next)) {
            iterator.remove();
            removed = true;
          }
        }
      }

      if (removed) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Removed listener. New listener count = " + fDimensionalFilters.size());
        }
        updateMeasureValues();
      }
      return removed;
    }

    public boolean hasInvisibleFilters() {
      return !fInvisibleDimensionalFilters.isEmpty();
    }

    public boolean isEmpty() {
      return fDimensionalFilters.isEmpty();
    }

    // Compute the new possible values from registered listeners
    private void updateMeasureValues() {
      TreeSet<Comparable> possibleValues = new TreeSet<>();
      for (DimensionalFilter filter : fDimensionalFilters) {
        for (Comparable possibleValue : filter.getPossibleValues()) {
          if (!possibleValues.contains(possibleValue)) {
            possibleValues.add(possibleValue);
          }
        }
      }

      setPossibleValues(new ArrayList<>(possibleValues));
    }

    public int getIntegerMin() {
      return 0;
    }

    public int getIntegerMax() {
      int size = fPossibleValues.size() - 1;
      return size >= 0 ? size : 0;
    }

    public int getIntegerValue() {
      return fCurrentInt;
    }

    @SuppressWarnings("unchecked")
    public void setIntegerValue(int aCurrent) {
      if (aCurrent != fCurrentInt) {
        fCurrentInt = aCurrent;
        Comparable value = convertToValue(aCurrent);
        if (getMinValue().compareTo(value) > 0) {
          value = getMinValue();
        }
        if (value.compareTo(getMaxValue()) > 0) {
          value = getMaxValue();
        }
        setCurrentValue(value);
        fPropertyChangeSupport.firePropertyChange(DimensionalFilterManager.CURRENT_PROPERTY_NAME, null, null);
      }
    }

    public Comparable getMinValue() {
      return fPossibleValues.get(getIntegerMin());
    }

    public Comparable getMaxValue() {
      return fPossibleValues.get(getIntegerMax());
    }

    public Comparable getValue(int aIntValue) {
      return fPossibleValues.get(aIntValue);
    }

    public Comparable getCurrentValue() {
      return fCurrent;
    }

    private void setCurrentValue(Comparable aCurrent) {
      // Current value will be set as an integer
      boolean shouldFireEvent = false;
      int currentInt = convertToInt(aCurrent);

      if (currentInt == -1) {
        return;
      }

      if (aCurrent != fCurrent) {
        fCurrent = aCurrent;
        shouldFireEvent = true;
      }

      if (shouldFireEvent) {
        fPropertyChangeSupport.firePropertyChange(DimensionalFilterManager.POSSIBLE_VALUES_PROPERTY_NAME, null, null);
        for (DimensionalFilter dimensionalFilter : fDimensionalFilters) {
          dimensionalFilter.setFilterValue(fCurrent);
        }
      }

      setIntegerValue(currentInt);
    }

    /**
     * Sets value of all filters from a model's current filter value.
     * This is used when a model is shared among different layers.
     * @param aModel The model whose value will be used to update the filters and other models
     */
    private void setValueFromModel(ILcdModel aModel) {
      for (DimensionalFilter dimensionalFilter : fDimensionalFilters) {
        if (dimensionalFilter.canGetFilterValueFromModel(aModel)) {
          Comparable value = dimensionalFilter.getFilterValueFromModel(aModel);
          setCurrentValue(value);
          return;
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void setPossibleValues(List<Comparable> aPossibleValues) {
      //there are no possible values to filter, so unfilter everything
      if (aPossibleValues.size() == 0) {
        for (DimensionalFilter listener : fDimensionalFilters) {
          listener.setFilterValue(null);
        }
        return;
      }

      fPossibleValues.clear();
      fPossibleValues.addAll(aPossibleValues);

      Comparable newMin = aPossibleValues.get(0);
      Comparable newMax = aPossibleValues.get(aPossibleValues.size() - 1);
      boolean currentValueFromModel = false;
      if (fCurrent == null) {
        if (fDimensionalFilters.size() == 1) {
          DimensionalFilter filter = fDimensionalFilters.get(0);
          ILcdModel model = filter.getLayer().getModel();
          if (filter.canGetFilterValueFromModel(model)) {
            fCurrent = filter.getFilterValueFromModel(model);
            currentValueFromModel = fCurrent != null;
          }
        }
        if (fCurrent == null) {
          fCurrent = newMin;
          currentValueFromModel = false;
        }
      }
      if (newMin.compareTo(fCurrent) > 0 || fCurrent.compareTo(newMax) > 0) {
        setCurrentValue(newMin);
      }

      // Current value will be set as an integer
      int currentInt = convertToInt(fCurrent);

      if (-1 == currentInt) {
        currentInt = 0;
      }
      // adjust integer values
      setIntegerValue(currentInt);

      fPropertyChangeSupport.firePropertyChange(DimensionalFilterManager.POSSIBLE_VALUES_PROPERTY_NAME, null, null);

      if (!currentValueFromModel) {
        for (DimensionalFilter listener : fDimensionalFilters) {
          listener.setFilterValue(fCurrent);
        }
      }
    }

    /**
     * @return layer labels of the filters
     */
    public List<String> getFilterTargetNames() {
      List<String> names = new ArrayList<>(fDimensionalFilters.size());
      for (DimensionalFilter listener : fDimensionalFilters) {
        names.add(listener.getLayer().getLabel());
      }
      return names;
    }

  }

  private boolean registerLayerImpl(ILcdLayer aLayer, ILcdLayered aLayered) {
    boolean registeredFilters = false;

    if (fDimensionalFilterProvider.canHandleLayer(aLayer, aLayered)) {
      List<DimensionalFilter> dimensionalFilters = fDimensionalFilterProvider.createFilters(aLayer, aLayered);
      for (DimensionalFilter dimensionalFilter : dimensionalFilters) {
        DimensionalFilterGroup dimensionalFilterGroup = getCompatibleFilterGroup(dimensionalFilter);
        boolean visibilityCheck = false;
        if (dimensionalFilterGroup == null) {
          dimensionalFilterGroup = new DimensionalFilterGroup(dimensionalFilter.getName(), dimensionalFilter.getType(), dimensionalFilter.getUnit(), dimensionalFilter.isPositive());
          if (aLayer.isVisible()) {
            addNewFilterGroup(dimensionalFilterGroup);
          } else {
            FilterGroupState data = new FilterGroupState(dimensionalFilterGroup);
            fInvisibleGroups.put(dimensionalFilterGroup, data);
          }
        } else {
          visibilityCheck = true;
        }
        FilterGroupState filterGroupState = fGroups.get(dimensionalFilterGroup);
        addFilterToGroup(aLayer, dimensionalFilter, filterGroupState);
        filterGroupState = fInvisibleGroups.get(dimensionalFilterGroup);
        addFilterToGroup(aLayer, dimensionalFilter, filterGroupState);
        if (visibilityCheck) {
          if (aLayer.isVisible()) {
            layerVisible(aLayer);
          } else {
            layerInvisible(aLayer);
          }
        } else {
          fPropertyChangeSupport.firePropertyChange(FILTER_GROUPS_PROPERTY_NAME, null, getFilterGroups());
        }
        registeredFilters = true;
      }
    }

    return registeredFilters;
  }

  private void addFilterToGroup(ILcdLayer aLayer, DimensionalFilter dimensionalFilter, FilterGroupState aFilterGroupState) {
    if (aFilterGroupState != null) {
      aFilterGroupState.addFilter(dimensionalFilter, aLayer.isVisible());
      ILcdModel model = aLayer.getModel();
      if (model != null) {
        ModelFilterValueChangeListener modelValListener = new ModelFilterValueChangeListener(aFilterGroupState);
        model.addModelListener(modelValListener);
        fModelValueListeners.put(aLayer, modelValListener);
      }
    }
  }

  private void addNewFilterGroup(DimensionalFilterGroup aGroup) {
    FilterGroupState data = new FilterGroupState(aGroup);
    fGroups.put(aGroup, data);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Added new filter group for '" + aGroup.getName() + "' measure. New group count = " + fGroups.size());
    }
  }

  private DimensionalFilterGroup getCompatibleFilterGroup(DimensionalFilter aDimensionalFilter) {
    for (DimensionalFilterGroup group : getFilterGroups()) {
      if (group.isCompatibleWith(aDimensionalFilter)) {
        return group;
      }
    }
    for (DimensionalFilterGroup group : fInvisibleGroups.keySet()) {
      if (group.isCompatibleWith(aDimensionalFilter)) {
        return group;
      }
    }
    return null;
  }

  private void layerVisible(final ILcdLayer aLayer) {
    for (DimensionalFilterGroup dimensionalFilterGroup : fGroups.keySet()) {
      FilterGroupState filterGroupState = fGroups.get(dimensionalFilterGroup);
      filterGroupState.restoreInvisibleFilters(new ILcdFilter<DimensionalFilter>() {
        @Override
        public boolean accept(DimensionalFilter aAbstractDimensionFilterListener) {
          return aLayer == aAbstractDimensionFilterListener.getLayer();
        }
      });
    }

    boolean someFilterGroupAdded = false;
    for (Iterator<DimensionalFilterGroup> it = fInvisibleGroups.keySet().iterator(); it.hasNext(); ) {
      DimensionalFilterGroup dimensionalFilterGroup = it.next();
      FilterGroupState filterGroupState = fInvisibleGroups.get(dimensionalFilterGroup);
      filterGroupState.restoreInvisibleFilters(new ILcdFilter<DimensionalFilter>() {
        @Override
        public boolean accept(DimensionalFilter aAbstractDimensionFilterListener) {
          return aLayer == aAbstractDimensionFilterListener.getLayer();
        }
      });
      if (!filterGroupState.isEmpty()) {
        it.remove();
        fGroups.put(dimensionalFilterGroup, filterGroupState);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Added new filter group for '" + dimensionalFilterGroup.getName() + "' measure. New group count = " + fGroups.size());
        }
        someFilterGroupAdded = true;
      }
    }
    if (someFilterGroupAdded) {
      fPropertyChangeSupport.firePropertyChange(FILTER_GROUPS_PROPERTY_NAME, null, getFilterGroups());
    }
  }

  private boolean unregisterLayerImpl(final ILcdLayer aLayer) {
    boolean someFilterGroupRemoved = false;
    ModelFilterValueChangeListener filterValListener = fModelValueListeners.get(aLayer);
    if (filterValListener != null) {
      aLayer.getModel().removeModelListener(filterValListener);
    }
    for (Iterator<DimensionalFilterGroup> it = fGroups.keySet().iterator(); it.hasNext(); ) {
      DimensionalFilterGroup dimensionalFilterGroup = it.next();
      FilterGroupState filterGroupState = fGroups.get(dimensionalFilterGroup);
      filterGroupState.removeFilters(new ILcdFilter<DimensionalFilter>() {
        @Override
        public boolean accept(DimensionalFilter aAbstractDimensionFilterListener) {
          return aLayer == aAbstractDimensionFilterListener.getLayer();
        }
      }, false);
      if (filterGroupState.isEmpty()) {
        it.remove();
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Removed filter group for '" + dimensionalFilterGroup.getName() + "' measure. New group count = " + fGroups.size());
        }
        someFilterGroupRemoved = true;
        if (filterGroupState.hasInvisibleFilters()) {
          fInvisibleGroups.put(dimensionalFilterGroup, filterGroupState);
        } else {
          fInvisibleGroups.remove(dimensionalFilterGroup);
        }
      }
    }
    if (someFilterGroupRemoved) {
      fPropertyChangeSupport.firePropertyChange(FILTER_GROUPS_PROPERTY_NAME, null, getFilterGroups());
    }

    return someFilterGroupRemoved;
  }

  private boolean layerInvisible(final ILcdLayer aLayer) {
    boolean someFilterGroupRemoved = false;
    for (Iterator<DimensionalFilterGroup> it = fGroups.keySet().iterator(); it.hasNext(); ) {
      DimensionalFilterGroup dimensionalFilterGroup = it.next();
      FilterGroupState filterGroupState = fGroups.get(dimensionalFilterGroup);
      filterGroupState.makeFiltersInvisible(new ILcdFilter<DimensionalFilter>() {
        @Override
        public boolean accept(DimensionalFilter aAbstractDimensionFilterListener) {
          return aLayer == aAbstractDimensionFilterListener.getLayer();
        }
      });
      if (filterGroupState.isEmpty() && filterGroupState.hasInvisibleFilters()) {
        it.remove();
        fInvisibleGroups.put(dimensionalFilterGroup, filterGroupState);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Removed filter group for '" + dimensionalFilterGroup.getName() + "' measure. New group count = " + fGroups.size());
        }
        someFilterGroupRemoved = true;
      }
    }
    if (someFilterGroupRemoved) {
      fPropertyChangeSupport.firePropertyChange(FILTER_GROUPS_PROPERTY_NAME, null, getFilterGroups());
    }

    return someFilterGroupRemoved;
  }

  private static class VisibilityPropertyChangeListener implements PropertyChangeListener {

    private final WeakReference<DimensionalFilterManager> fFilterManager;
    private final WeakReference<ILcdLayered> fLayered;

    private VisibilityPropertyChangeListener(DimensionalFilterManager aDimensionalFilterManager, ILcdLayered aLayered) {
      fFilterManager = new WeakReference<>(aDimensionalFilterManager);
      fLayered = new WeakReference<>(aLayered);
    }

    public ILcdLayered getLayered() {
      return fLayered.get();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      DimensionalFilterManager dimensionalFilterManager = fFilterManager.get();
      ILcdLayered layered = fLayered.get();
      ILcdLayer layer = (ILcdLayer) evt.getSource();
      if (dimensionalFilterManager == null || layered == null) {
        layer.removePropertyChangeListener(this);
        return;
      }
      if ("visible".equals(evt.getPropertyName())) {
        boolean isVisible = (Boolean) evt.getNewValue();
        if (isVisible) {
          dimensionalFilterManager.layerVisible(layer);
        } else {
          dimensionalFilterManager.layerInvisible(layer);
        }
      }
    }
  }

  private static class ModelFilterValueChangeListener implements ILcdModelListener {
    private final WeakReference<FilterGroupState> fFilterGroupStateRef;

    public ModelFilterValueChangeListener(FilterGroupState aFilterGroupStateRef) {
      fFilterGroupStateRef = new WeakReference<>(aFilterGroupStateRef);
    }

    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      FilterGroupState filterGroupState = fFilterGroupStateRef.get();
      if (filterGroupState != null) {
        filterGroupState.setValueFromModel(aEvent.getModel());
      }
    }
  }

  /**
   * Listens to property change events on a DimensionalFilterProvider. When new filters are created or some filters are deleted
   * by a DimensionalFilterProvider, this listener is triggered to update the state of the DimensionalFilterManager
   */
  private static class WeakFilterProviderListener extends ALcdWeakPropertyChangeListener<DimensionalFilterManager> {

    protected WeakFilterProviderListener(DimensionalFilterManager aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    protected void propertyChangeImpl(DimensionalFilterManager aToModify, PropertyChangeEvent evt) {
      if (evt.getSource() instanceof ADimensionalFilterProvider && ADimensionalFilterProvider.UPDATE_FILTERS.equals(evt.getPropertyName())) {
        List<DimensionalFilter> newFilters = (List<DimensionalFilter>) evt.getNewValue();
        List<DimensionalFilter> oldFilters = (List<DimensionalFilter>) evt.getOldValue();
        boolean updated = false;

        //remove the old filters
        for (final DimensionalFilter oldFilter : oldFilters) {
          if (oldFilter != null) {
            DimensionalFilterGroup compatibleFilterGroup = aToModify.getCompatibleFilterGroup(oldFilter);
            FilterGroupState filterGroupState = aToModify.getFilterGroupState(compatibleFilterGroup);
            //call removeFilters method with an ILcdFilter instance which removes oldFilter
            //if it matches any elements in the filterGroupState.
            if (filterGroupState != null) {
              updated |= filterGroupState.removeFilters(new ILcdFilter<DimensionalFilter>() {
                @Override
                public boolean accept(DimensionalFilter aObject) {
                  return aObject == oldFilter;
                }
              }, false);
            }
          }
        }

        //add the new filters
        for (final DimensionalFilter newFilter : newFilters) {
          DimensionalFilterGroup compatibleFilterGroup = aToModify.getCompatibleFilterGroup(newFilter);
          FilterGroupState filterGroupState = null;
          //if the group is not found, create a new one
          if (compatibleFilterGroup == null) {
            compatibleFilterGroup = new DimensionalFilterGroup(newFilter.getName(), newFilter.getType(), newFilter.getUnit(), newFilter.isPositive());
            filterGroupState = new FilterGroupState(compatibleFilterGroup);
            aToModify.fGroups.put(compatibleFilterGroup, filterGroupState);
          } else {
            filterGroupState = aToModify.getFilterGroupState(compatibleFilterGroup);
          }
          //if layer is visible but filter group is in the invisible groups, move it to the visible groups
          if (newFilter.getLayer().isVisible() && aToModify.fInvisibleGroups.containsKey(compatibleFilterGroup)) {
            aToModify.fInvisibleGroups.remove(compatibleFilterGroup);
            aToModify.fGroups.put(compatibleFilterGroup, filterGroupState);
          }
          filterGroupState.addFilter(newFilter, newFilter.getLayer().isVisible());
          updated = true;
        }

        if (updated) {
          aToModify.fPropertyChangeSupport.firePropertyChange(FILTER_GROUPS_PROPERTY_NAME, null, aToModify.getFilterGroups());
        }

      }
    }
  }

}
