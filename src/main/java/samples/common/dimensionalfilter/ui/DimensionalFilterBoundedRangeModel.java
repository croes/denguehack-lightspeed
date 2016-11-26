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
package samples.common.dimensionalfilter.ui;

import static samples.common.dimensionalfilter.model.DimensionalFilterManager.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.common.dimensionalfilter.model.DimensionalFilterGroup;
import samples.common.dimensionalfilter.model.DimensionalFilterManager;

/**
 * A bounded range model to adjust {@link samples.common.dimensionalfilter.model.DimensionalFilterGroup} from a
 * JSlider.
 *
 * @since 2015.0
 */
class DimensionalFilterBoundedRangeModel implements BoundedRangeModel {

  private final DimensionalFilterManager fDimensionalFilterManager;

  private final List<ChangeListener> fListeners = new ArrayList<>();
  private final FilterGroupPropertyChangeListener fFilterGroupListener;

  private boolean fValueIsAdjusting;

  private DimensionalFilterGroup fCurrentDimensionalFilterGroup;
  private int fMinimum;
  private int fMaximum;
  private int fCurrent;

  public DimensionalFilterBoundedRangeModel(DimensionalFilterManager aDimensionalFilterManager, DimensionalFilterGroup aACurrentFilterGroup) {
    fDimensionalFilterManager = aDimensionalFilterManager;
    fCurrentDimensionalFilterGroup = aACurrentFilterGroup;
    adjustMinMaxValues();
    adjustCurrentValue();
    fFilterGroupListener = new FilterGroupPropertyChangeListener();
    if (fCurrentDimensionalFilterGroup != null) {
      fDimensionalFilterManager.addPropertyChangeListener(fCurrentDimensionalFilterGroup, fFilterGroupListener);
    }
  }

  public void destroy() {
    setFilterGroup(null);
  }

  public void setFilterGroup(DimensionalFilterGroup aSelectedDimensionalFilterGroup) {
    if (aSelectedDimensionalFilterGroup != fCurrentDimensionalFilterGroup) {
      if (fCurrentDimensionalFilterGroup != null) {
        fDimensionalFilterManager.removePropertyChangeListener(fCurrentDimensionalFilterGroup, fFilterGroupListener);
      }
      fCurrentDimensionalFilterGroup = aSelectedDimensionalFilterGroup;
      if (fCurrentDimensionalFilterGroup != null) {
        fDimensionalFilterManager.addPropertyChangeListener(fCurrentDimensionalFilterGroup, fFilterGroupListener);
      }
    }
    boolean adjusted = adjustMinMaxValues();
    adjusted |= adjustCurrentValue();
    if (adjusted) {
      notifyListeners();
    }
  }

  private boolean adjustMinMaxValues() {
    int oldMinimum = fMinimum;
    int oldMaximum = fMaximum;
    fMinimum = fDimensionalFilterManager.getMinValueInt(fCurrentDimensionalFilterGroup);
    fMaximum = fDimensionalFilterManager.getMaxValueInt(fCurrentDimensionalFilterGroup);
    return oldMinimum != fMinimum || oldMaximum != fMaximum;
  }

  private boolean adjustCurrentValue() {
    int oldCurrent = fCurrent;
    fCurrent = fDimensionalFilterManager.getCurrentValueInt(fCurrentDimensionalFilterGroup);
    return oldCurrent != fCurrent;
  }

  @Override
  public void addChangeListener(ChangeListener x) {
    fListeners.add(x);
  }

  @Override
  public void removeChangeListener(ChangeListener x) {
    fListeners.remove(x);
  }

  private void notifyListeners() {
    final ChangeEvent event = new ChangeEvent(this);
    for (ChangeListener listener : fListeners) {
      listener.stateChanged(event);
    }
  }

  @Override
  public int getMinimum() {
    return fMinimum;
  }

  @Override
  public void setMinimum(int newMinimum) {
    // do nothing;
  }

  @Override
  public int getMaximum() {
    return fMaximum;
  }

  @Override
  public void setMaximum(int newMaximum) {
    // do nothing
  }

  @Override
  public int getValue() {
    return fCurrent;
  }

  @Override
  public void setValue(int newValue) {
    if (newValue < getMinimum()) {
      newValue = getMinimum();
    }
    if (newValue > getMaximum()) {
      newValue = getMaximum();
    }
    if (newValue > newValue + getExtent()) {
      newValue = newValue + getExtent();
    }
    if (newValue != fCurrent) {
      fCurrent = newValue;
      fDimensionalFilterManager.setIntegerValue(fCurrentDimensionalFilterGroup, fCurrent);
      notifyListeners();
    }
  }

  @Override
  public void setValueIsAdjusting(boolean b) {
    setRangeProperties(fCurrent, 0, fMinimum, fMaximum, b);
  }

  @Override
  public boolean getValueIsAdjusting() {
    return fValueIsAdjusting;
  }

  @Override
  public int getExtent() {
    return 0;
  }

  @Override
  public void setExtent(int newExtent) {
    // do nothing
  }

  @Override
  public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
    fCurrent = value;
    fMinimum = min;
    fMaximum = max;
    fValueIsAdjusting = adjusting;
    notifyListeners();
  }

  public DimensionalFilterGroup getCurrentDimensionalFilterGroup() {
    return fCurrentDimensionalFilterGroup;
  }

  private class FilterGroupPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (MIN_PROPERTY_NAME.equals(evt.getPropertyName()) ||
          MAX_PROPERTY_NAME.equals(evt.getPropertyName()) ||
          POSSIBLE_VALUES_PROPERTY_NAME.equals(evt.getPropertyName())) {
        if (adjustMinMaxValues()) {
          notifyListeners();
        }
      }
      if (CURRENT_PROPERTY_NAME.equals(evt.getPropertyName())) {
        if (adjustCurrentValue()) {
          notifyListeners();
        }
      }
    }
  }
}
