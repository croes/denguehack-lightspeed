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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.TLcyCompositeLayerStyleProvider;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayer;

/**
 * Class which keeps the style of a number of layers in sync. Use the {@link #addLayer(ILcdLayer)}
 * and {@link #removeLayer(ILcdLayer)} methods to add/remove layers to the set of layers for which
 * the style should be kept in sync.
 */
public class LayerStyleSynchronizer implements ILcdChangeListener {

  private final ILcyLucyEnv fLucyEnv;
  private final Map<ILcdLayer, ILcyLayerStyle> fLayerToLayerStyleMap = new HashMap<ILcdLayer, ILcyLayerStyle>();

  private boolean fListenerActive = true;

  public LayerStyleSynchronizer(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public void stateChanged(TLcdChangeEvent aChangeEvent) {
    if (fListenerActive) {
      fListenerActive = false;//turn off the listener since we manually adjust the styles
      try {
        //since the listener is only added to the layer styles, we can perform this cast
        ILcyLayerStyle changedLayerStyle = (ILcyLayerStyle) aChangeEvent.getSource();
        syncStyles(changedLayerStyle);
      } finally {
        fListenerActive = true;//make sure the listener is turned on again
      }
    }
  }

  void addLayer(ILcdLayer aLayer) {
    TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);

    if (!(fLayerToLayerStyleMap.isEmpty())) {
      //make sure this new layer has the same style as the other layers
      copyLayerStyle(fLayerToLayerStyleMap.keySet().iterator().next(), aLayer);
    }

    if (layerStyleProvider.canGetStyle(aLayer)) {
      ILcyLayerStyle style = layerStyleProvider.getStyle(aLayer);
      fLayerToLayerStyleMap.put(aLayer, style);
      style.addChangeListener(this);
    }
  }

  void removeLayer(ILcdLayer aLayer) {
    ILcyLayerStyle layerStyle = fLayerToLayerStyleMap.remove(aLayer);
    if (layerStyle != null) {
      layerStyle.removeChangeListener(this);
    }
  }

  int getLayerCount() {
    return fLayerToLayerStyleMap.size();
  }

  /**
   * Synchronize the styles of the different layers by applying the changed style to all the layers
   * @param aChangedLayerStyle The changed layer style which should be copied on all layers
   */
  private void syncStyles(ILcyLayerStyle aChangedLayerStyle) {
    TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    Set<ILcdLayer> layers = fLayerToLayerStyleMap.keySet();
    for (ILcdLayer layer : layers) {
      if (layerStyleProvider.canApplyStyle(aChangedLayerStyle, layer)) {
        layerStyleProvider.applyStyle(aChangedLayerStyle, layer);
      }
    }
  }

  private void copyLayerStyle(ILcdLayer aLayerToCopyFrom, ILcdLayer aLayerToCopyToSFCT) {
    TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    if (layerStyleProvider.canGetStyle(aLayerToCopyFrom)) {
      ILcyLayerStyle layerStyle = layerStyleProvider.getStyle(aLayerToCopyFrom);
      if (layerStyleProvider.canApplyStyle(layerStyle, aLayerToCopyToSFCT)) {
        layerStyleProvider.applyStyle(layerStyle, aLayerToCopyToSFCT);
      }
    }
  }
}
