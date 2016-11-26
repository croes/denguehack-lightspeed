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
package samples.common.action;

import com.luciad.util.ILcdDisposable;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;

/**
 * Base class for customization when layers are added, removed or selected.
 */
public abstract class LayerCustomizerSupport<S extends ILcdLayered, T extends ILcdLayer> implements ILcdDisposable {

  private final S fLayered;
  private final ILcdCollection<ILcdLayer> fSelectedLayers;
  private final SelectedLayersListener fSelectedLayersListener ;
  private final LayeredListener fLayeredListener;

  protected LayerCustomizerSupport(S aLayered, ILcdCollection<ILcdLayer> aSelectedLayers) {
    fLayered = aLayered;
    fSelectedLayers = aSelectedLayers;
    fSelectedLayersListener = new SelectedLayersListener();
    fSelectedLayers.addCollectionListener(fSelectedLayersListener);
    fLayeredListener = new LayeredListener();
    aLayered.addLayeredListener(fLayeredListener);
  }

  protected abstract void layerAdded(S aView, T aLayer);

  protected abstract void layerRemoved(S aView, T aLayer);

  protected abstract void layerSelected(S aView, T aLayer);

  @Override
  public void dispose() {
    fLayered.removeLayeredListener(fLayeredListener);
    fSelectedLayers.removeCollectionListener(fSelectedLayersListener);
  }

  private class LayeredListener implements ILcdLayeredListener {
    @SuppressWarnings("unchecked")
    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      if (e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        layerAdded(fLayered, (T) e.getLayer());
      } else if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        layerRemoved(fLayered, (T) e.getLayer());
      }
    }
  }

  private class SelectedLayersListener implements ILcdCollectionListener {
    @Override
    public void collectionChanged(TLcdCollectionEvent aCollectionEvent) {
      if (aCollectionEvent.getType() == TLcdCollectionEvent.Type.ELEMENT_ADDED) {
        layerSelected(fLayered, (T) aCollectionEvent.getElement());
      }
    }
  }
}
