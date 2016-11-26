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
package samples.lucy.lightspeed.missionmap;

import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.grid.lightspeed.TLcyLspGridAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.workspace.ILcyWorkspaceManagerListener;
import com.luciad.lucy.workspace.TLcyWorkspaceManagerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

/**
 * Listener responsible to keep the mission preparation map and the mission preview map in sync
 */
class MapSynchronizerListener implements ILcyGenericMapManagerListener<ILspView, ILspLayer> {

  private ILcyLspMapComponent fPreparationMapComponent;
  private ILcyLspMapComponent fPreviewMapComponent;
  private ILcdLayeredListener fLayeredListener;
  private ILcyLucyEnv fLucyEnv;

  MapSynchronizerListener(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fLucyEnv.getWorkspaceManager().addWorkspaceManagerListener(new ILcyWorkspaceManagerListener() {
      @Override
      public void workspaceStatusChanged(TLcyWorkspaceManagerEvent aEvent) {
        if (aEvent.getID() == TLcyWorkspaceManagerEvent.WORKSPACE_DECODING_ENDED) {
          //synchronize the existing layers when the workspace has been decoded
          synchronizeExistingLayers();
        }
      }
    });
  }

  @Override
  public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
    ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> mapComponent = aMapManagerEvent.getMapComponent();
    if (mapComponent instanceof ILcyLspMapComponent) {
      String type = ((ILcyLspMapComponent) mapComponent).getType();
      if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
        if (MissionMapAddOn.MISSION_PREPARATION_STRING.equals(type) && fPreparationMapComponent == null) {
          fPreparationMapComponent = (ILcyLspMapComponent) mapComponent;
          startSynchronization();
        } else if (MissionMapAddOn.MISSION_PREVIEW_STRING.equals(type) && fPreviewMapComponent == null) {
          fPreviewMapComponent = (ILcyLspMapComponent) mapComponent;
          startSynchronization();
        }
      } else if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED) {
        if (mapComponent == fPreparationMapComponent) {
          stopSynchronization();
          fPreparationMapComponent = null;
        } else if (mapComponent == fPreviewMapComponent) {
          stopSynchronization();
          fPreviewMapComponent = null;
        }
      }
    }
  }

  private void startSynchronization() {
    if (fPreparationMapComponent != null && fPreviewMapComponent != null) {
      //remove all the layers from the preview map component
      if (!fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
        fPreviewMapComponent.getMainView().removeAllLayers();
        synchronizeExistingLayers();
      }

      //Add a listener to make sure that when new layers are added to the preparation map component, they are
      //also added to the execution map component.
      fLayeredListener = new LayeredListener();
      fPreparationMapComponent.getMainView().addLayeredListener(fLayeredListener);

    }
  }

  /**
   * Synchronize the existing layers of the preparation map component with those of the preview map
   * component
   *
   */
  private void synchronizeExistingLayers() {
    if (fPreparationMapComponent != null && fPreviewMapComponent != null) {
      Enumeration layers = fPreparationMapComponent.getMainView().layers();
      ILspView previewView = fPreviewMapComponent.getMainView();

      while (layers.hasMoreElements()) {
        ILspLayer layer = (ILspLayer) layers.nextElement();
        // do not add/synchronize grid layers. The grid model has not been designed to be shared
        // between multiple views
        if (isGridLayer(layer)) {
          continue;
        }
        ILspLayer previewLayer;
        try {
          previewLayer = (ILspLayer) previewView.layerOf(layer.getModel());
        } catch (NoSuchElementException e) {
          previewLayer = createAndAddPreviewLayer(layer);
        }
        synchronizeExistingLayer(layer, previewLayer);
      }
    }
  }

  /**
   * Keep a preparation layer and preview layer in sync
   * @param aPreparationLayer The preparation layer
   * @param aPreviewLayer The preview layer
   */
  private void synchronizeExistingLayer(ILspLayer aPreparationLayer, ILspLayer aPreviewLayer) {
    if (aPreviewLayer != null) {
      new LayerSynchronizerListener(aPreparationLayer, aPreviewLayer, fLucyEnv);
    }
  }

  /**
   * Returns the preview layer created for the model of <code>aPreparationLayer</code>. The layer is
   * also added to the preview map component
   *
   * @param aPreparationLayer The preparation layer
   *
   * @return the created preview layer
   */
  private ILspLayer createAndAddPreviewLayer(ILspLayer aPreparationLayer) {
    ILspLayerFactory layerFactory = fPreparationMapComponent.getMainView().getLayerFactory();
    if (layerFactory != null && layerFactory.canCreateLayers(aPreparationLayer.getModel())) {
      Collection<ILspLayer> previewLayers = fPreviewMapComponent.getMainView().addLayersFor(aPreparationLayer
                                                                                                .getModel());
      return previewLayers.toArray(new ILspLayer[1])[0];
    }
    return null;
  }

  private void stopSynchronization() {
    if (fPreparationMapComponent != null && fPreviewMapComponent != null) {
      if (fLayeredListener != null) {
        fPreparationMapComponent.getMainView().removeLayeredListener(fLayeredListener);
        fLayeredListener = null;
      }
    }
  }

  private boolean isGridLayer(ILspLayer aLayer) {
    TLcyLspGridAddOn gridAddOn = fLucyEnv.retrieveAddOnByClass(TLcyLspGridAddOn.class);
    return gridAddOn.getFormat().isLayerOfFormat(aLayer);
  }

  /**
   * Listener which synchronizes the layers between the preview and preparation map component
   */
  private class LayeredListener implements ILcdLayeredListener {
    @Override
    public void layeredStateChanged(TLcdLayeredEvent aLayeredEvent) {
      //deactivate the listener during workspace decoding
      if (!fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
        ILspLayer preparationLayer = (ILspLayer) aLayeredEvent.getLayer();
        if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
          ILspLayer previewLayer = createAndAddPreviewLayer(preparationLayer);
          synchronizeExistingLayer(preparationLayer, previewLayer);
        } else if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
          ILcdLayer previewLayer = fPreviewMapComponent.getMainView().layerOf(preparationLayer.getModel());
          if (previewLayer != null) {
            fPreviewMapComponent.getMainView().removeLayer(previewLayer);
          }
        }
      }
    }
  }

}
