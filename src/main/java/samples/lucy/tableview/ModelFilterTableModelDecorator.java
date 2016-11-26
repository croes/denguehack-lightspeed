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
package samples.lucy.tableview;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TableModelEvent;

import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.util.ALcdWeakChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdChangeEvent;

/**
 * <p>Extension of the abstract class InsertColumnTableModelDecorator that adds the
 * display column. The data in the added column is a <code>Boolean</code>. The value is
 * based on whether a <code>TLcyModelObjectFilter</code> accepts the domain object
 * corresponding to that table row or not.</p>
 *
 */
public class ModelFilterTableModelDecorator extends InsertColumnTableModelDecorator implements
                                                                                    CustomizerPanelTableModel {

  /**
   * The index of the display column
   */
  public static final int DISPLAY_COLUMN_INDEX = 0;

  /**
   * The column descriptor of the display column
   */
  public static final String VISIBILITY_COLUMN_DESCRIPTOR = "visibilityColumnDescriptor";

  private final IExtendedTableModel fDelegate;
  private final TLcyModelObjectFilter fTableFilter;
  private final ILcdIntegerIndexedModel fModel;
  /**
   * In order to implement {@link CustomizerPanelTableModel} we keep a <code>List</code> of modified values,
   * allowing to easily implement the apply method
   */
  private final List<NewValueContainer> fUpdatedValues = new LinkedList<>();

  private boolean fListenerActive = true;

  private final PropertyChangeSupport fPropertyChangeSupport;
  private boolean fChangesPending = false;
  private int fMinRowIndex = -1;
  private int fMaxRowIndex = -1;
  private boolean fUpdateAll = false;

  /**
   * Creates a new <code>ModelFilterTableModelDecorator</code> for the given
   * table model and <code>ILcdIntegerIndexedModel</code> and filter.
   *
   * @param aModel The model for which <code>aTableFilter</code> is created
   * @param aDelegate The table model to add the display column to.
   * @param aTableFilter The filter that knows which objects need to be filtered. Must not be <code>null</code>
   * @param aLayerFilter The filter which is placed on the layer. Must not be <code>null</code>
   */
  public ModelFilterTableModelDecorator(ILcdIntegerIndexedModel aModel,
                                        IExtendedTableModel aDelegate,
                                        TLcyModelObjectFilter aTableFilter,
                                        ILcdFilter aLayerFilter) {
    super(aDelegate, DISPLAY_COLUMN_INDEX);

    fModel = aModel;
    fDelegate = aDelegate;

    fTableFilter = aTableFilter;
    fPropertyChangeSupport = new PropertyChangeSupport(this);
    fTableFilter.addChangeListener(new FilterListener(this));
    if (aLayerFilter instanceof ILcdChangeSource) {
      ((ILcdChangeSource) aLayerFilter).addChangeListener(new FilterListener(this));
    }
  }

  public ILcdIntegerIndexedModel getModel() {
    return fModel;
  }

  public void changeDisplayStatusAll(boolean aShowAll) {
    int rowCount = getRowCount();
    fUpdateAll = true;
    for (int i = 0; i < rowCount; i++) {
      updateRow(aShowAll, i, i != rowCount - 1);
    }
  }

  @Override
  public String getNewColumnName() {
    return TLcyLang.getString("Visible");
  }

  @Override
  public Class getNewColumnClass() {
    return Boolean.class;
  }

  @Override
  public boolean isNewColumnEditable(int aRowIndex) {
    //make all rows editable, even when changing the checkbox has no effect
    //the renderer will add a visual indication for these cases
    return true;
  }

  @Override
  public Object getNewColumnValueAt(int aRowIndex) {
    if (fUpdatedValues.contains(new NewValueContainer(aRowIndex, true))) {
      NewValueContainer newValueContainer = fUpdatedValues.get(fUpdatedValues.indexOf(new NewValueContainer(aRowIndex, true)));
      return newValueContainer.fNewValue ? Boolean.TRUE : Boolean.FALSE;
    }
    Object object = fModel.elementAt(aRowIndex);
    return fTableFilter.accept(object) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public void setNewColumnValueAt(Object aNewValue, int aRowIndex, int aColumnIndex) {
    setValueAt(aNewValue, aRowIndex, aColumnIndex, false);
  }

  @Override
  public Object getNewColumnDescriptor() {
    return VISIBILITY_COLUMN_DESCRIPTOR;
  }

  @Override
  public String getNewColumnTooltipText() {
    return "<html>Use the checkboxes in this column to toggle the visibility of individual elements.<br>" +
           "The checkbox in this header toggles the visibility of all elements.<html>";
  }

  @Override
  public void setValueAt(Object aNewValue, int aRowIndex, int aColumnIndex, boolean aIsAdjusting) {
    updateRow((Boolean) aNewValue, aRowIndex, aIsAdjusting);
  }

  @Override
  public ILcdIntegerIndexedModel getOriginalModel() {
    return fDelegate.getOriginalModel();
  }

  @Override
  public ILcdModel getTransformedModel() {
    return fDelegate.getTransformedModel();
  }

  @Override
  public Collection<Object> originalToTransformed(Object aOriginalModelElement) {
    return fDelegate.originalToTransformed(aOriginalModelElement);
  }

  @Override
  public Collection<Object> transformedToOriginal(Object aTransformedModelElement) {
    return fDelegate.transformedToOriginal(aTransformedModelElement);
  }

  private void updateRow(Boolean aNewValue, int aRowIndex, boolean aIsAdjusting) {
    NewValueContainer newValueContainer = new NewValueContainer(aRowIndex, aNewValue);
    fUpdatedValues.remove(newValueContainer);
    fUpdatedValues.add(newValueContainer);
    fMinRowIndex = fMinRowIndex == -1 ? aRowIndex : Math.min(fMinRowIndex, aRowIndex);
    fMaxRowIndex = fMaxRowIndex == -1 ? aRowIndex : Math.max(fMaxRowIndex, aRowIndex);
    if (!aIsAdjusting) {
      fireTableRowsUpdated(fMinRowIndex, fMaxRowIndex);
      fMinRowIndex = -1;
      fMaxRowIndex = -1;
      setChangesPending(true);
    }
  }

  @Override
  public boolean applyChangesOnObject() {
    if (isChangesPending()) {
      try {
        //make sure the change listener of the filter does not fires events when updating the filters manually
        fListenerActive = false;
        int minIndex = -1;
        int maxIndex = -1;
        if (fUpdateAll) {
          fTableFilter.changeFilterAllObjects(fUpdatedValues.get(0).fNewValue);
          minIndex = 0;
          maxIndex = getModel().size() - 1;
          fUpdateAll = false;
        } else {
          List<Object> objectsTrue = new ArrayList<>();
          List<Object> objectsFalse = new ArrayList<>();
          for (NewValueContainer newValueContainer : fUpdatedValues) {
            if (newValueContainer.fNewValue) {
              objectsTrue.add(fModel.elementAt(newValueContainer.fRowIndex));
            } else {
              objectsFalse.add(fModel.elementAt(newValueContainer.fRowIndex));
            }
            minIndex = minIndex == -1 ? newValueContainer.fRowIndex : Math.min(minIndex, newValueContainer.fRowIndex);
            maxIndex = maxIndex == -1 ? newValueContainer.fRowIndex : Math.max(maxIndex, newValueContainer.fRowIndex);
          }
          if (!objectsTrue.isEmpty()) {
            fTableFilter.changeFilter(objectsTrue, true);
          }
          if (!objectsFalse.isEmpty()) {
            fTableFilter.changeFilter(objectsFalse, false);
          }
        }
        fUpdatedValues.clear();
        setChangesPending(false);
        fireTableRowsUpdated(minIndex, maxIndex);
        return true;
      } finally {
        fListenerActive = true;
      }
    }
    return true;
  }

  private boolean isListenerActive() {
    return fListenerActive;
  }

  @Override
  public boolean isChangesPending() {
    return fChangesPending;
  }

  private void setChangesPending(boolean aChangesPending) {
    if (aChangesPending != fChangesPending) {
      fChangesPending = aChangesPending;
      fPropertyChangeSupport.firePropertyChange("changesPending", !fChangesPending, fChangesPending);
    }
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aListener);
  }

  private static class FilterListener extends ALcdWeakChangeListener<ModelFilterTableModelDecorator> {

    public FilterListener(ModelFilterTableModelDecorator aDisplayColumnTableModelDecorator) {
      super(aDisplayColumnTableModelDecorator);
    }

    @Override
    protected void stateChangedImpl(ModelFilterTableModelDecorator aDecorator, TLcdChangeEvent aChangeEvent) {
      if (aDecorator.isListenerActive()) {
        int lastRow = aDecorator.getModel().size() - 1;
        if (lastRow >= 0) {
          aDecorator.fireTableChanged(new TableModelEvent(aDecorator, 0, lastRow, DISPLAY_COLUMN_INDEX));
        }
      }
    }
  }

  private static class NewValueContainer {
    private final int fRowIndex;
    private final boolean fNewValue;

    private NewValueContainer(int aRowIndex, boolean aNewValue) {
      fRowIndex = aRowIndex;
      fNewValue = aNewValue;
    }

    @Override
    public boolean equals(Object obj) {
      //override the equals method allowing to set a new value multiple times before the changes are applied
      return obj instanceof NewValueContainer &&
             fRowIndex == ((NewValueContainer) obj).fRowIndex;
    }

    @Override
    public int hashCode() {
      return 31 * fRowIndex;
    }
  }
}
