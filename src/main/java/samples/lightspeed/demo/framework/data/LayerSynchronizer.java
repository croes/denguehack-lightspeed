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
package samples.lightspeed.demo.framework.data;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Listener which synchronizes layer between multiple layers
 */
class LayerSynchronizer implements ILcdSelectionListener {

  private boolean fActive = true;

  private final Set<ILspLayer> fLayers = new HashSet<ILspLayer>();

  public LayerSynchronizer(ILspLayer aLayer) {
    addLayers(aLayer);
  }

  /**
   * @param aLayer a layer
   *
   * @return {@code true} if the layer can be synchronized with the other layers
   */
  public boolean canAddLayer(ILspLayer aLayer) {
    return fLayers.isEmpty() ||
           fLayers.iterator().next().getModel() == aLayer.getModel();
  }

  /**
   * Adds a layer to be synchronized.
   *
   * @param aLayer the layer
   */
  public void addLayers(ILspLayer aLayer) {
    if (fLayers.contains(aLayer)) {
      throw new IllegalStateException("Layer already added " + aLayer);
    }
    addListenerToLayer(aLayer, this);
    fLayers.add(aLayer);
  }

  /**
   * @return all layers that are synchronized
   */
  public Set<ILspLayer> getLayers() {
    return fLayers;
  }

  /**
   * {@inheritDoc}
   *
   * <p>It will apply the selection changes on all other <code>ILcdSelection</code> objects by using
   * the abstract methods of this class. Before any selection change is made to any
   * <code>ILcdSelection</code> object, this listener deactivates itself. Afterwards, it will
   * reactive itself.</p>
   *
   * <p>This allows for example to keep two <code>ILcdSelection</code> objects in sync. Adding the
   * same listener to both ILcdSelection instances is possible and will not cause infinite
   * loops.</p>
   */
  public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
    if (isActive()) {
      ILspLayer changedLayer = (ILspLayer) aSelectionEvent.getSelection();
      setActive(false);
      Set<ILspLayer> layers = getLayers();
      for (ILspLayer layer : layers) {
        if (layer == changedLayer) {
          continue;
        }
        Enumeration selectedElements = aSelectionEvent.selectedElements();
        Enumeration deselectedElements = aSelectionEvent.deselectedElements();
        while (selectedElements.hasMoreElements()) {
          Object selectedObject = selectedElements.nextElement();
          //layers have the same model, so we must not check whether the object is part of the layer
          layer.selectObject(selectedObject, true, ILcdFireEventMode.FIRE_LATER);
        }
        while (deselectedElements.hasMoreElements()) {
          Object deselectedObject = deselectedElements.nextElement();
          //layers have the same model, so we must not check whether the object is part of the layer
          layer.selectObject(deselectedObject, false, ILcdFireEventMode.FIRE_LATER);
        }
        layer.fireCollectedSelectionChanges();
      }
      setActive(true);
    }
  }

  private static void initLayer(ILspLayer aLayerToAdjust, LayerSynchronizer aListener) {
    Set<ILspLayer> layers = aListener.getLayers();
    //if the set contains a layer, we must sync the selection of aLayer with the selection of
    //any of the layers in the set
    if (!layers.isEmpty()) {
      synchroniseSelection(layers.iterator().next(), aLayerToAdjust);
    }
  }

  /**
   * <p>Utility method which will add the listener to the layer <code>aLayer</code>. It is safe to call this
   * method multiple times for the same layer, without the risk of adding the listener multiple
   * times.</p>
   *
   * <p>When the listener is active, the selection on the layer will first be initialized before
   * adding the listener, allowing the listener to function correctly.</p>
   * @param aLayer the layer
   * @param aListener the listener to be added
   */
  private static void addListenerToLayer(ILspLayer aLayer,
                                         LayerSynchronizer aListener) {
    //make sure the listener is never added twice
    aLayer.removeSelectionListener(aListener);
    if (aListener.isActive()) {
      //the listener is active, meaning the selection of the layers is synchronized
      //we will first sync the selection on the layer before adding the listener to it
      initLayer(aLayer, aListener);
    }
    aLayer.addSelectionListener(aListener);
  }

  /**
   * Method to synchronise the selection of two layers: the selection of layer <code>aLayer</code>
   * will be put on <code>aLayerSFCT</code> when both layers share the same model.
   *
   * @param aLayer     the layer with the original selection
   * @param aLayerSFCT the layer to copy the selection onto
   */
  private static void synchroniseSelection(ILspLayer aLayer, ILspLayer aLayerSFCT) {
    if (aLayer != null && aLayerSFCT != null &&
        aLayer.getModel() == aLayerSFCT.getModel() &&
        aLayer != aLayerSFCT) {
      aLayerSFCT.clearSelection(ILcdFireEventMode.FIRE_NOW);
      Enumeration selectedObjects = aLayer.selectedObjects();
      while (selectedObjects.hasMoreElements()) {
        Object selectedObject = selectedObjects.nextElement();
        aLayerSFCT.selectObject(selectedObject, true, ILcdFireEventMode.FIRE_LATER);
      }
      aLayerSFCT.fireCollectedSelectionChanges();
    }
  }

  /**
   * Returns <code>true</code> when this listener is activated, e.g. it will synchronise the
   * selection. Notice that during selection synchronization, the listener will switch his active
   * state at the beginning and end of the synchronization
   *
   * @return <code>true</code> when this listener is active
   *
   * @see #setActive(boolean)
   */
  public boolean isActive() {
    return fActive;
  }

  /**
   * Sets the active state of this listener to <code>aActive</code>
   *
   * @param aActive the new active state of this listener
   *
   * @see #isActive()
   */
  public void setActive(boolean aActive) {
    fActive = aActive;
  }
}
