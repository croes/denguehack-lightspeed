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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * Active settable which allows to activate/deactivate the synchronization of the layer styles
 */
class StyleSynchronizationActiveSettable extends ALcyActiveSettable {

  private final ILcyLucyEnv fLucyEnv;
  private boolean fActive = true;
  private final StyleSynchronizationLayeredListener fStyleSynchronizationLayeredListener;
  private final ILcyGenericMapManagerListener<ILcdView, ILcdLayer> fMapManagerListener;

  public StyleSynchronizationActiveSettable(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fStyleSynchronizationLayeredListener = new StyleSynchronizationLayeredListener(fLucyEnv);
    fMapManagerListener = new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> mapComponent = aMapManagerEvent.getMapComponent();
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          mapComponentAdded(mapComponent);
        } else if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED) {
          mapComponentRemoved(mapComponent);
        }
      }
    };
    startSynchronization();
  }

  private void startSynchronization() {
    fLucyEnv.getCombinedMapManager().addMapManagerListener(fMapManagerListener, true);
  }

  @Override
  public boolean isActive() {
    return fActive;
  }

  @Override
  public void setActive(boolean aActive) {
    if (fActive != aActive) {
      if (aActive) {
        //style synchronization must be switched on since the active settable is activated
        startSynchronization();
      } else {
        //style synchronization must be switched off since the active settable is de-activated
        fLucyEnv.getCombinedMapManager().removeMapManagerListener(fMapManagerListener);
        fStyleSynchronizationLayeredListener.removeAllMapComponents();
      }
      fActive = aActive;
      firePropertyChange("active", !aActive, aActive);
    }
  }

  private void mapComponentAdded(ILcyGenericMapComponent<?, ?> aMapComponent) {
    fStyleSynchronizationLayeredListener.addMapComponent(aMapComponent);
  }

  private void mapComponentRemoved(ILcyGenericMapComponent<?, ?> aMapComponent) {
    fStyleSynchronizationLayeredListener.removeMapComponent(aMapComponent);
  }
}
