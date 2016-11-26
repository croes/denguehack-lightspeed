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
package samples.lucy.syncstyle;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;

/**
 * <code>ILcyLayeredListener</code> which makes sure the style synchronization is activated for
 * all layers.
 */
public class StyleSynchronizationLayeredListener implements ILcdLayeredListener {

  private Map<ILcdModel, LayerStyleSynchronizer> fModelToStyleSynchronizerMap = new HashMap<ILcdModel, LayerStyleSynchronizer>();
  private final Set<ILcyGenericMapComponent<?, ?>> fMapComponents = new HashSet<ILcyGenericMapComponent<?, ?>>();
  private final ILcyLucyEnv fLucyEnv;

  public StyleSynchronizationLayeredListener(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public void layeredStateChanged(TLcdLayeredEvent aLayeredEvent) {
    if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
      ILcdLayer layer = aLayeredEvent.getLayer();
      layerAdded(layer);
    } else if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
      ILcdLayer layer = aLayeredEvent.getLayer();
      layerRemoved(layer);
    }
  }

  void addMapComponent(ILcyGenericMapComponent<?, ?> aMapComponent) {
    fMapComponents.add(aMapComponent);
    ILcdView mainView = aMapComponent.getMainView();
    if (mainView instanceof ILcdLayered) {
      Enumeration layers = ((ILcdLayered) mainView).layers();
      while (layers.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();
        layerAdded(layer);
      }
      ((ILcdLayered) mainView).addLayeredListener(this);
    }
  }

  void removeMapComponent(ILcyGenericMapComponent<?, ?> aMapComponent) {
    boolean removed = fMapComponents.remove(aMapComponent);
    if (removed) {
      ILcdView mainView = aMapComponent.getMainView();
      if (mainView instanceof ILcdLayered) {
        ((ILcdLayered) mainView).removeLayeredListener(this);
        Enumeration layers = ((ILcdLayered) mainView).layers();
        while (layers.hasMoreElements()) {
          ILcdLayer layer = (ILcdLayer) layers.nextElement();
          layerRemoved(layer);
        }
      }
    }
  }

  void removeAllMapComponents() {
    //call removeMapComponent for each map component to make sure all listeners are cleaned up
    //make a copy of the set of map components, allowing us to call removeMapComponent for each those map components
    Set<ILcyGenericMapComponent<?, ?>> mapComponents = new HashSet<ILcyGenericMapComponent<?, ?>>();
    mapComponents.addAll(fMapComponents);
    for (ILcyGenericMapComponent<?, ?> mapComponent : mapComponents) {
      removeMapComponent(mapComponent);
    }
  }

  private void layerAdded(ILcdLayer aLayer) {
    ILcdModel model = aLayer.getModel();
    LayerStyleSynchronizer layerStyleSynchronizer = fModelToStyleSynchronizerMap.get(model);

    if (layerStyleSynchronizer == null) {
      layerStyleSynchronizer = new LayerStyleSynchronizer(fLucyEnv);
      fModelToStyleSynchronizerMap.put(model, layerStyleSynchronizer);
    }

    layerStyleSynchronizer.addLayer(aLayer);
  }

  private void layerRemoved(ILcdLayer aLayer) {
    ILcdModel model = aLayer.getModel();
    LayerStyleSynchronizer layerStyleSynchronizer = fModelToStyleSynchronizerMap.get(model);
    if (layerStyleSynchronizer != null) {
      layerStyleSynchronizer.removeLayer(aLayer);
      if (layerStyleSynchronizer.getLayerCount() == 0) {
        fModelToStyleSynchronizerMap.remove(model);
      }
    }
  }
}
