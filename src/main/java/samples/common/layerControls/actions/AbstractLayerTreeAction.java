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
package samples.common.layerControls.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import samples.common.action.CompositeAndFilter;
import com.luciad.util.ALcdWeakChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

/**
 * <p>Abstract class which helps in creating actions for a layer tree.</p>
 */
public abstract class AbstractLayerTreeAction extends ALcdAction {

  private List<ILcdLayer> fLayers = new ArrayList<>();
  private boolean fAutoHide = false;

  private boolean fStrict = false;
  private final CompositeAndFilter<ILcdLayer> fLayerFilter = new CompositeAndFilter<>();
  private ILcdTreeLayered fLayered;
  private int fMinObjectCount;
  private int fMaxObjectCount;

  /**
   * <p>Create a new action for the {@link ILcdTreeLayered ILcdTreeLayered}
   * <code>aLayered</code>.</p>
   * The constructor does not yet {@link #updateState() set the initial state} to allow the calling constructor to finish initialization.
   *
   * @param aLayered        the <code>ILcdTreeLayered</code> to create the action for
   * @param aStrict         {@code True} means the action disables itself when one or more objects
   *                        that are selected, were not accepted by the filter. When {@code false},
   *                        the action simply ignores the objects that weren't accepted by the
   *                        filter.
   * @param aMinObjectCount Defines the minimum number of objects that should pass the filter for
   *                        the action to be enabled.
   * @param aMaxObjectCount Defines the maximum number of objects that should pass the filter for
   *                        the action to be enabled. You can use {@code -1} as a short cut for
   *                        {@code Integer.MAX_VALUE}, to leave the maximum unbounded.
   */
  public AbstractLayerTreeAction(ILcdTreeLayered aLayered, boolean aStrict, int aMinObjectCount, int aMaxObjectCount) {
    if (aMaxObjectCount == -1) {
      aMaxObjectCount = Integer.MAX_VALUE;
    }
    Objects.requireNonNull(aLayered, "Given view is not allowed to be null");
    checkTrue(aMinObjectCount <= aMaxObjectCount, "Minimum[" + aMinObjectCount + "] should be less than or equal to maximum[" + aMaxObjectCount + "]");
    checkTrue(aMinObjectCount >= 0, "Minimum[" + aMinObjectCount + "] should be zero or more");
    fLayered = aLayered;
    fStrict = aStrict;
    fMinObjectCount = aMinObjectCount;
    fMaxObjectCount = aMaxObjectCount;
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          updateVisibleState();
        }
      }
    });
    putValue(ILcdAction.VISIBLE, true);
    fLayerFilter.addChangeListener(new WeakFilterListener(this));
  }

  /**
   * <p>Create a new action for the {@link ILcdTreeLayered ILcdTreeLayered}
   * <code>aLayered</code>.</p>
   *
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public AbstractLayerTreeAction(ILcdTreeLayered aLayered) {
    this(aLayered, false, 0, -1);
  }

  public int getMinObjectCount() {
    return fMinObjectCount;
  }

  public int getMaxObjectCount() {
    return (fMaxObjectCount == -1) ? Integer.MAX_VALUE : fMaxObjectCount;
  }

  /**
   * <p>Returns the <code>ILcdTreeLayered</code> for which this action was created.</p>
   *
   * @return the <code>ILcdTreeLayered</code> for which this action was created
   */
  public ILcdTreeLayered getLayered() {
    return fLayered;
  }

  /**
   * <p>Sets the <code>ILcdTreeLayered</code> for which this action was created</p>
   * @param aLayered the <code>ILcdTreeLayered</code> for which this action was created
   */
  public void setLayered(ILcdTreeLayered aLayered) {
    fLayered = aLayered;
  }

  /**
   * <p>Update the enabled state of this action to match {@link #shouldBeEnabled()} and calls {@link
   * #updateOtherProperties()} afterwards to allow to update other properties of this action beside
   * the enabled state.</p>
   *
   * <p>This method is called when the layers on which this action will work are changed by calling
   * {@link #setLayers(List)} or when the filter changes by calling {@link
   * #addLayerFilter(ILcdFilter)} or {@link #removeLayerFilter(ILcdFilter)}.</p>
   *
   * @see #updateOtherProperties()
   * @see #shouldBeEnabled()
   */
  protected void updateState() {
    updateEnabledState();
    updateOtherProperties();
  }

  private void updateEnabledState() {
    if (shouldBeEnabled() != isEnabled()) {
      setEnabled(!isEnabled());
    }
  }

  /**
   * <p>Add an extra <code>ILcdFilter</code> to filter the layers on which this action will perform.
   * The action will only work on the layers that pass all filters. The {@link #updateState()}
   * method is called automatically after adding the filter.</p>
   *
   * @param aFilter the filter to be added
   *
   * @see #removeLayerFilter(ILcdFilter)
   */
  public void addLayerFilter(ILcdFilter<ILcdLayer> aFilter) {
    fLayerFilter.addFilter(aFilter);
    updateState();
  }

  /**
   * <p>Remove the layer filter <code>aFilter</code>. The {@link #updateState()} method is called
   * automatically after the filter has been removed.</p>
   *
   * @param aFilter the filter to remove
   *
   * @see #addLayerFilter(ILcdFilter)
   */
  public void removeLayerFilter(ILcdFilter<ILcdLayer> aFilter) {
    fLayerFilter.removeFilter(aFilter);
    updateState();
  }

  /**
   * @return Returns the object filter as that is in use.
   */
  public ILcdFilter<ILcdLayer> getLayerFilter() {
    return fLayerFilter;
  }

  /**
   * <p>Update other properties beside the enabled state of this action. This method is called from
   * the {@link #updateState()} method in the standard implementation.</p>
   *
   * @see #updateState()
   */
  protected void updateOtherProperties() {
    //do nothing, but allows subclasses to update other properties beside the
    // selected and enabled properties
  }

  /**
   * <p>Returns <code>true</code> if and only if this action should be enabled.</p>
   *
   * @return <code>true</code> if and only if this action should be enabled.
   */
  protected boolean shouldBeEnabled() {
    int i = getFilteredLayers().size();
    if (i < 1 || i < getMinObjectCount() || i > getMaxObjectCount()) {
      return false;
    }
    return !isStrict() || fLayers.size() == i;
  }

  /**
   * <p>Returns the filtered list of layers on which this action will perform. Those layers passed
   * all the filters currently set on this action.</p>
   *
   * @return the filtered list of layers on which this action will perform.
   *
   * @see #addLayerFilter(ILcdFilter)
   * @see #setLayers(List)
   */
  public ArrayList<ILcdLayer> getFilteredLayers() {
    ArrayList<ILcdLayer> layers = new ArrayList<ILcdLayer>();
    for (ILcdLayer layer : fLayers) {
      if (fLayerFilter.accept(layer)) {
        layers.add(layer);
      }
    }
    return layers;
  }

  /**
   * <p>Returns a list containing all the layers currently set on this action. This list includes
   * both the layers passing all the filters and the ones which failed on one (or multiple)
   * filters.</p>
   *
   * @return a list containing all the layers currently set on this action.
   *
   * @see #setLayers(List)
   */
  public List<ILcdLayer> getLayers() {
    return fLayers;
  }

  /**
   * <p>Set the layers on this action. The action will perform on all the layers from the list
   * <code>aLayers</code> which pass all the filters set on this action. This method will
   * automatically call {@link #updateState()} once the new layers are set.</p>
   *
   * @param aLayers the list of layers
   */
  public void setLayers(List<ILcdLayer> aLayers) {
    fLayers = new ArrayList<ILcdLayer>(aLayers);
    updateState();
  }

  /**
   * Returns the auto hide property, see {@link #setAutoHide(boolean)}.
   * @return the auto hide property.
   */
  public boolean isAutoHide() {
    return fAutoHide;
  }

  /**
   * <p>Sets if the action should hide itself when it is disabled. This is convenient when used in a
   * context menu.</p>
   *
   * <p>Hiding means the {@code ILcdAction.VISIBLE} key is {@linkplain #putValue(String, Object)
   * changed} to {@code false}, it is up to the container of this action to effectively hide it from
   * the UI.</p>
   *
   * @param aAutoHide {@code True} to automatically hide the action when it is disabled, {@code
   *                  false} to avoid that the action changes its visibility.
   */
  public void setAutoHide(boolean aAutoHide) {
    boolean oldValue = fAutoHide;
    fAutoHide = aAutoHide;
    updateVisibleState();
    firePropertyChange("autoHide", oldValue, aAutoHide);
  }

  private void updateVisibleState() {
    if (isAutoHide()) {
      putValue(ILcdAction.VISIBLE, isEnabled());
    }
  }

  public void installSelectionListener(ILcdCollection<ILcdLayer> aSelectedLayers) {
    Objects.requireNonNull(aSelectedLayers, "Selected layers cannot be null");
    WeakSelectionListener listener = new WeakSelectionListener(this);
    aSelectedLayers.addCollectionListener(listener);
    setLayers(new ArrayList<>(aSelectedLayers));
  }

  public boolean isStrict() {
    return fStrict;
  }

  public void setStrict(boolean aStrict) {
    fStrict = aStrict;
  }

  public void setMinObjectCount(int aMinObjectCount) {
    fMinObjectCount = aMinObjectCount;
  }

  public void setMaxObjectCount(int aMaxObjectCount) {
    fMaxObjectCount = aMaxObjectCount;
  }

  static class WeakSelectionListener implements ILcdCollectionListener<ILcdLayer>, EventListener {
    private WeakReference<AbstractLayerTreeAction> fAction = null;

    private WeakSelectionListener(AbstractLayerTreeAction aAction) {
      fAction = new WeakReference<>(aAction);
    }

    @Override
    public void collectionChanged(TLcdCollectionEvent aCollectionEvent) {
      AbstractLayerTreeAction selectionAction = fAction.get();
      if (selectionAction == null) {
        aCollectionEvent.getSource().removeCollectionListener(this);
        return;
      }
      // always use the entire selection
      selectionAction.setLayers(new ArrayList<ILcdLayer>(aCollectionEvent.getSource()));
    }
  }

  private static class WeakFilterListener extends ALcdWeakChangeListener<AbstractLayerTreeAction> {
    private WeakFilterListener(AbstractLayerTreeAction aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    public void stateChangedImpl(AbstractLayerTreeAction aToModify, TLcdChangeEvent aChangeEvent) {
      aToModify.updateState();
    }
  }

  private void checkTrue(boolean aToCheck, String aMessage) {
    if (!aToCheck) {
      throw new IllegalArgumentException(aMessage);
    }
  }
}
