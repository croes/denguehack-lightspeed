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

import static samples.common.dimensionalfilter.model.DimensionalFilterManager.FILTER_GROUPS_PROPERTY_NAME;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import samples.common.dimensionalfilter.model.DimensionalFilterGroup;
import samples.common.dimensionalfilter.model.DimensionalFilterManager;

/**
 * A combobox model for {@link samples.common.dimensionalfilter.model.DimensionalFilterManager#getFilterGroups()}.
 *
 * @since 2015.0
 */
class DimensionalFilterComboBoxModel extends AbstractListModel<DimensionalFilterGroup> implements
                                                                                       ComboBoxModel<DimensionalFilterGroup> {

  private final DimensionalFilterManager fDimensionalFilterManager;
  private final FilterManagerPropertyChangeListener fListener;
  private DimensionalFilterGroup fSelectedDimensionalFilterGroup;
  //list for nontime filtergroups
  private List<DimensionalFilterGroup> fDimensionalFilterGroups;

  public DimensionalFilterComboBoxModel(DimensionalFilterManager aDimensionalFilterManager) {
    fDimensionalFilterManager = aDimensionalFilterManager;
    List<DimensionalFilterGroup> dimensionalFilterGroups = fDimensionalFilterManager.getFilterGroups();
    //get only non-time filtergroups
    fDimensionalFilterGroups = refineFilterGroup(dimensionalFilterGroups);

    if (fDimensionalFilterGroups.size() > 0) {
      fSelectedDimensionalFilterGroup = fDimensionalFilterGroups.get(0);
    }

    fListener = new FilterManagerPropertyChangeListener();
    fDimensionalFilterManager.addPropertyChangeListener(fListener);
  }

  public void destroy() {
    fDimensionalFilterManager.removePropertyChangeListener(fListener);
  }

  /**
   * extracts only non-time filtergroups from the given list
   * @param aInputList
   * @return list of non-time filters
   */
  private List<DimensionalFilterGroup> refineFilterGroup(List<DimensionalFilterGroup> aInputList) {
    List<DimensionalFilterGroup> refinedList = new ArrayList();
    for (DimensionalFilterGroup dimensionalFilterGroup : aInputList) {
      if (!Date.class.isAssignableFrom(dimensionalFilterGroup.getType())) {
        refinedList.add(dimensionalFilterGroup);
      }
    }
    return refinedList;
  }

  private void resetList(List<DimensionalFilterGroup> aOldList, List<DimensionalFilterGroup> aNewList) {
    // get old selection index
    int ix = 0;
    if (fSelectedDimensionalFilterGroup != null && aOldList != null) {
      ix = aOldList.indexOf(fSelectedDimensionalFilterGroup);
    }

    DimensionalFilterGroup selectedItem; // initialize with old selection

    fDimensionalFilterGroups = refineFilterGroup(aNewList);

    if (fDimensionalFilterGroups == null || fDimensionalFilterGroups.size() == 0) {
      selectedItem = null;
      fireContentsChanged(this, -1, -1);
    } else {
      selectedItem = fDimensionalFilterGroups.get((ix + 1) % fDimensionalFilterGroups.size());
      fireContentsChanged(this, 0, aNewList.size());
    }

    setSelectedItem(selectedItem);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    if (!Objects.equals(fSelectedDimensionalFilterGroup, anItem)) {
      fSelectedDimensionalFilterGroup = (DimensionalFilterGroup) anItem;
      fireContentsChanged(this, -1, -1);
    }
  }

  public DimensionalFilterGroup getSelectedItem() {
    return fSelectedDimensionalFilterGroup;
  }

  @Override
  public int getSize() {
    return fDimensionalFilterGroups.size();
  }

  @Override
  public DimensionalFilterGroup getElementAt(int index) {
    return fDimensionalFilterGroups.get(index);
  }

  private class FilterManagerPropertyChangeListener implements PropertyChangeListener {
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (FILTER_GROUPS_PROPERTY_NAME.equals(evt.getPropertyName())) {
        resetList((List<DimensionalFilterGroup>) evt.getOldValue(),
                  (List<DimensionalFilterGroup>) evt.getNewValue());
      }
    }
  }
}
