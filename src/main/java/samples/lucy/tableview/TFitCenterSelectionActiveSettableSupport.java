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

import java.awt.Container;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.TLcyGenericMapUtil;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.util.ALcdWeakSelectionListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdPropertyChangeSource;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * <p>Support class for the active settables which fit/center the view on the selected objects of
 * the table, and the active settable which applies the selection of the table view on the
 * view.</p>
 */
public class TFitCenterSelectionActiveSettableSupport implements ILcdFilter, ILcdPropertyChangeSource {

  private JTable fTable;
  private TLcyModelContext fModelContext;

  private boolean fAutoFit = false;
  private boolean fAutoCenter = false;
  private boolean fAutoSelect = false;
  private boolean fSelectionSynchronizationActive = true;
  private final ITableViewLogic fTableViewLogic;

  private final PropertyChangeSupport fPropertyChangeSupport;

  private final ILcyLucyEnv fLucyEnv;

  /**
   * Create a new TFitCenterSelectionActiveSettableSupport instance. You need to call {@link
   * #setModelContextAndTable(TLcyModelContext, JTable)}
   * @param aTableViewLogic Object containing the actual logic needed to perform the fitting / centering
   * @param aLucyEnv The Lucy back-end
   */
  TFitCenterSelectionActiveSettableSupport(ITableViewLogic aTableViewLogic, ILcyLucyEnv aLucyEnv) {
    fTableViewLogic = aTableViewLogic;
    fLucyEnv = aLucyEnv;
    fPropertyChangeSupport = new PropertyChangeSupport(this);
  }

  /**
   * Set the model context and table on which this instance should work.
   *
   * @param aModelContext The model context
   * @param aTable        The table
   */
  void setModelContextAndTable(TLcyModelContext aModelContext, JTable aTable) {
    if ((fModelContext != null && !(fModelContext.equals(aModelContext))) ||
        (fTable != null && !(fTable.equals(aTable)))) {
      throw new UnsupportedOperationException("Can set the model context and/or table only once");
    }
    if (!accept(aModelContext)) {
      throw new IllegalArgumentException("The model context is not accepted by this filter");
    }

    //make sure the listeners are only added once
    boolean addListeners = fModelContext == null && aModelContext != null;

    fModelContext = aModelContext;
    fTable = aTable;

    if (addListeners) {
      if (fModelContext.getLayer() != null) {
        fModelContext.getLayer().addSelectionListener(new LayerToTableSelectionListener(this));
      }
      fTable.getSelectionModel().addListSelectionListener(new TableSelectionListener());
    }
  }

  /**
   * Update the table selection based on the selection of the layer
   */
  private void updateTableSelectionFromLayer() {

    if (fTable == null || fModelContext == null) {
      return;
    }

    boolean active = fSelectionSynchronizationActive;
    //temporarily disable the listeners since we are going to update the selection manually
    fSelectionSynchronizationActive = false;

    IExtendedTableModel tableModel = (IExtendedTableModel) fTable.getModel();

    try {
      List<Object> objectsToSelect = new ArrayList<>();
      for (Enumeration selection = fModelContext.getLayer().selectedObjects(); selection.hasMoreElements(); ) {
        Object selectedObjectInLayer = selection.nextElement();
        Collection<Object> selectedTableDomainObjects = tableModel.transformedToOriginal(selectedObjectInLayer);
        objectsToSelect.addAll(selectedTableDomainObjects);        
      }
      List<Integer> selectedIndices = EditableTable.selectObjectsInTable(objectsToSelect, fTable);

      if (!selectedIndices.isEmpty()) {
        // Scroll to the selected row, if it is not in the view
        final int view_row_index = selectedIndices.get(selectedIndices.size() - 1);

        //request the scrolling in an invokeLater, because otherwise we could get re-entrant
        //painting events which Swing does not allow
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            // we are in an invokeLater here, so the table could have changed. Check that
            // the index is still valid
            if (view_row_index < fTable.getRowCount() && view_row_index >= 0) {
              Rectangle rect = fTable.getCellRect(view_row_index, 0, true);
              Container parent = fTable.getParent();
              //Workaround for the map centric front-end: the selected row would be invisible when the table was hidden
              //See http://stackoverflow.com/q/31834187/1076463
              if (parent != null) {
                rect.height = Math.min(parent.getHeight(), rect.height);
              }
              fTable.scrollRectToVisible(rect);
            }
          }
        };
        TLcdAWTUtil.invokeLater(runnable);
      }
    } finally {
      // Restore the active state
      fSelectionSynchronizationActive = active;
    }
  }

  /**
   * Fits/centers the <code>ILcdLayer</code> on the table selection. This method is performed on the
   * calling thread. Since it adjusts the view, make sure to call it on the EDT
   * @param aFit <code>true</code> when the fitting of the view must be updated
   * @param aCenter <code>true</code> when the centering of the view must be updated
   */
  void fitCenterLayerOnTableSelection(final boolean aFit,
                                      boolean aCenter) {
    if (fTable == null || fModelContext == null) {
      return;
    }
    if (!(aFit || aCenter)) {
      return;
    }
    //do not try to fit/center during workspace decoding. The workspace mechanism will restore the view to its correct position
    if (fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
      return;
    }
    final IExtendedTableModel tableModel = (IExtendedTableModel) fTable.getModel();

    applyOnAllSelectedObjects(fTable, tableModel, true, new ILcdFunction() {
      @Override
      public boolean applyOn(Object aObject) throws IllegalArgumentException {
        List<Object> selectedDomainObjects = (List<Object>) aObject;
        // We will fit on the original model objects, and not on the transformed ones
        // For example with clustering, fitting on the cluster would probably de-cluster (due to the fit),
        // and the actual fit might be on the wrong object
        // However, we only fit if the transformation did not filter out the element on the map
        List<Object> objectsToFitOn = new ArrayList<>(selectedDomainObjects.size());
        for (Object domainObject : selectedDomainObjects) {
          if (!tableModel.originalToTransformed(domainObject).isEmpty()) {
            objectsToFitOn.add(domainObject);
          }
        }
        try {
          // Fit/center the view on the selected object(s)
          if (aFit) {
            new TLcyGenericMapUtil(fLucyEnv).fitOnObjects(fModelContext.getView(),
                                                          fModelContext.getLayer(),
                                                          objectsToFitOn.toArray(new Object[objectsToFitOn.size()]));
          } else {
            new TLcyGenericMapUtil(fLucyEnv).centerOnObjects(fModelContext.getView(),
                                                             fModelContext.getLayer(),
                                                             objectsToFitOn.toArray(new Object[objectsToFitOn.size()]));
          }
        } catch (TLcdOutOfBoundsException | TLcdNoBoundsException e) {
          TLcdStatusEvent.sendMessage(fLucyEnv, this, TLcyLang.getString("Can't fit map, object(s) not visible in current projection"), TLcdStatusEvent.Severity.WARNING);
        }
        return true;
      }
    });
  }

  /**
   * Applies the function once to all selected objects in the table.
   * The function will be called on a {@code List<Object>} which contains all the
   * selected objects.
   *
   * @param aTable The table
   * @param aTableModel The model of the table
   * @param aUseOriginalDomainObjects {@code true} when the function must be called with a list of the original domain objects,
   *                                  {@code false} when the function must be called with the {@link IExtendedTableModel#originalToTransformed(Object) transformed}
   *                                  domain objects
   * @param aFunctionToApply The function to apply, which must accept a {@code List<Object>} representing the
   *                         selected objects.
   *                         When nothing is selected, the function will not be called.
   *                         The return value of this function is ignored.
   */
  public static void applyOnAllSelectedObjects(JTable aTable,
                                               IExtendedTableModel aTableModel,
                                               boolean aUseOriginalDomainObjects,
                                               ILcdFunction aFunctionToApply) {
    ListSelectionModel lsm = aTable.getSelectionModel();
    if (!lsm.isSelectionEmpty()) {

      // retrieve the selected objects from the layer
      int first = lsm.getMinSelectionIndex();
      int last = lsm.getMaxSelectionIndex();
      ILcdIntegerIndexedModel model = aTableModel.getOriginalModel();

      // When all objects are removed from the table, but they were selected in the UI,
      // and new objects are added afterwards (but less objects as before), and one
      // of these new objects is selected, the table seems to fire a selection event
      // whose 'lastIndex' is bigger than the element count of the table model.
      // Below you find a workaround for that bug.
      first = Math.min(first, model.size() - 1);
      last = Math.min(last, model.size() - 1);

      List<Object> selected_objects = new ArrayList<>();
      for (int i = Math.min(first, last); i <= Math.max(first, last); i++) {
        if (lsm.isSelectedIndex(i)) {
          Object tableModelDomainObject = model.elementAt(aTable.convertRowIndexToModel(i));
          if (aUseOriginalDomainObjects) {
            selected_objects.add(tableModelDomainObject);
          } else {
            selected_objects.addAll(aTableModel.originalToTransformed(tableModelDomainObject));
          }
        }
      }
      if (!selected_objects.isEmpty()) {
        aFunctionToApply.applyOn(selected_objects);
      }
    }
  }

  /**
   * Applies the selection of the table onto the <code>ILcdLayer</code>
   *
   */
  void applyTableSelectionOnLayer() {
    if (fTable == null || fModelContext == null) {
      return;
    }
    if (fTable instanceof EditableTable && !((EditableTable) fTable).isSelectionValid()) {
      return;
    }

    final IExtendedTableModel tableModel = (IExtendedTableModel) fTable.getModel();

    final ILcdLayer layer = fModelContext.getLayer();
    if (!layer.isSelectableSupported()) {
      //Don't attempt to select on a layer that does not support selection
      return;
    }

    boolean active = fSelectionSynchronizationActive;
    final ILcdFilter layerFilter = fTableViewLogic.retrieveLayerFilter(layer);

    try {
      //disable the selection synchronization since we are updating the selection ourselfs
      fSelectionSynchronizationActive = false;

      if (fModelContext.getView() instanceof ILcdLayered) {
        //Clear selection of all layers, but the one in the table
        Enumeration layers = ((ILcdLayered) fModelContext.getView()).layers();
        while (layers.hasMoreElements()) {
          ILcdLayer layerInView = (ILcdLayer) layers.nextElement();
          if (layerInView != layer) {
            layerInView.clearSelection(ILcdFireEventMode.FIRE_NOW);
          }
        }

        // Clear selection of layer in table. Using fire later as more selection changes are made below.
        layer.clearSelection(ILcdFireEventMode.FIRE_LATER);
      }

      applyOnAllSelectedObjects(fTable, tableModel, false, new ILcdFunction() {
        @Override
        public boolean applyOn(Object aObject) throws IllegalArgumentException {
          List<Object> selectedTransformedObjects = (List<Object>) aObject;

          // Make sure we can select items on the layer, isSelectableSupported has been verified above
          layer.setSelectable(true);
          for (Object selectedTransformedObject : selectedTransformedObjects) {
            if (layerFilter == null || layerFilter.accept(selectedTransformedObject)) {
              layer.selectObject(selectedTransformedObject, true, ILcdFireEventMode.FIRE_LATER);
            }
          }
          layer.fireCollectedSelectionChanges(); // For performance, fire one big event
          return true;
        }
      });
    } finally {
      //restore the selection synchronization state
      fSelectionSynchronizationActive = active;
    }
  }

  boolean isSelectionSynchronizationActive() {
    return fSelectionSynchronizationActive;
  }

  void setSelectionSynchronizationActive(boolean aSelectionSynchronizationActive) {
    fSelectionSynchronizationActive = aSelectionSynchronizationActive;
  }

  boolean isAutoFit() {
    return fAutoFit;
  }

  boolean isAutoCenter() {
    return fAutoCenter;
  }

  boolean isAutoSelect() {
    return fAutoSelect;
  }

  void setAutoFit(boolean aAutoFit) {
    if (aAutoFit != fAutoFit) {
      fAutoFit = aAutoFit;
      if (fAutoFit) {
        //auto fitting switched from disabled to enabled - perform a fit
        fitCenterLayerOnTableSelection(true, false);
      }
      fPropertyChangeSupport.firePropertyChange("autoFit", !aAutoFit, aAutoFit);
      if (fAutoFit) {
        setAutoCenter(false);
      }
    }

  }

  void setAutoCenter(boolean aAutoCenter) {
    if (fAutoCenter != aAutoCenter) {
      fAutoCenter = aAutoCenter;
      if (fAutoCenter) {
        //auto centering switched from disabled to enabled - perform centering
        fitCenterLayerOnTableSelection(false, true);
      }
      fPropertyChangeSupport.firePropertyChange("autoCenter", !aAutoCenter, aAutoCenter);
      if (fAutoCenter) {
        //centering and fitting must not be enabled at the same time
        setAutoFit(false);
      }
    }
  }

  void setAutoSelect(boolean aAutoSelect) {
    if (fAutoSelect != aAutoSelect) {
      fAutoSelect = aAutoSelect;
      fPropertyChangeSupport.firePropertyChange("autoSelect", !(aAutoSelect), aAutoSelect);
      if (fAutoSelect) {
        //When the selection synchronization is activated, apply the selection from the layer onto the table
        //to ensure that the selection of both is in sync
        //We could have opted to apply the table selection on the layer which would also bring the selection in sync
        //That option however has the drawback that when the table view is created and the active settable is configured
        //to be active on creation, the table selection is still empty. Applying the table selection on the layer
        //would then clear the existing selection from the layer.
        updateTableSelectionFromLayer();
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Filter to check which <code>TLcyModelContext</code> instances are accepted by the concrete
   * implementation.</p>
   */
  @Override
  public boolean accept(Object aObject) {
    return aObject instanceof TLcyModelContext &&
           fTableViewLogic.acceptModelContext((TLcyModelContext) aObject);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Listener which adjusts the view/layer when the table selection changes
   */
  private class TableSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(ListSelectionEvent aSelectionEvent) {
      if (fSelectionSynchronizationActive) {
        //table selection changes happen on the EDT, so no need to explicitly perform the call on the EDT
        fitCenterLayerOnTableSelection(fAutoFit, fAutoCenter);
        if (fSelectionSynchronizationActive &&
            fAutoSelect &&
            !aSelectionEvent.getValueIsAdjusting()) {
          applyTableSelectionOnLayer();
        }
      }
    }

  }

  /**
   * Weak listener which will be added to the layer, and which updates the table when the layer
   * selection changes.
   */
  private static class LayerToTableSelectionListener extends
                                                     ALcdWeakSelectionListener<TFitCenterSelectionActiveSettableSupport> {

    private LayerToTableSelectionListener(TFitCenterSelectionActiveSettableSupport aSupport) {
      super(aSupport);
    }

    @Override
    protected void selectionChangedImpl(final TFitCenterSelectionActiveSettableSupport aActiveSettableSupport, TLcdSelectionChangedEvent aSelectionEvent) {
      if (aActiveSettableSupport.isSelectionSynchronizationActive()) {
        //the change in selection might be invoked by a model change. In such case, the table did not yet
        //receive the model change event. Using an invokeLater works around this problem
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            aActiveSettableSupport.updateTableSelectionFromLayer();
          }
        });

      }
    }
  }
}
