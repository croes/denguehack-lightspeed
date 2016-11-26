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

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import com.luciad.gui.ALcdAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.lspmap.TLcyLspMapAddOn;
import com.luciad.lucy.addons.lspmap.TLcyLspMapBackEnd;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Action which creates a preview map
 */
class PreviewMapAction extends ALcdAction {

  private final ILcyLucyEnv fLucyEnv;

  public PreviewMapAction(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    //update the enabled state of the action so that the UI only allows to create one preview map
    TLcyLspMapManager mapManager = fLucyEnv.getService(TLcyLspMapManager.class);
    mapManager.addMapManagerListener(new MapManagerListener(this), true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TLcyLspMapBackEnd mapBackEnd = fLucyEnv.retrieveAddOnByClass(TLcyLspMapAddOn.class).getMapBackEnd();
    mapBackEnd.createMapComponent(MissionMapAddOn.MISSION_PREVIEW_STRING);
  }

  /**
   * Listener which updates the enabled state of the action when map components are added/removed, to prevent
   * opening multiple preview maps through the UI
   */
  private static class MapManagerListener implements ILcyGenericMapManagerListener<ILspView, ILspLayer> {

    private WeakReference<PreviewMapAction> fPreviewMapAction;

    private MapManagerListener(PreviewMapAction aPreviewMapAction) {
      fPreviewMapAction = new WeakReference<PreviewMapAction>(aPreviewMapAction);
    }

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
      PreviewMapAction previewMapAction = fPreviewMapAction.get();
      if (previewMapAction == null) {
        aMapManagerEvent.getMapManager().removeMapManagerListener(this);
      } else {
        ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> mapComponent = aMapManagerEvent.getMapComponent();
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED &&
            mapComponent instanceof ILcyLspMapComponent &&
            MissionMapAddOn.MISSION_PREVIEW_STRING.equals(((ILcyLspMapComponent) mapComponent).getType())) {
          previewMapAction.setEnabled(false);
        } else if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED &&
                   mapComponent instanceof ILcyLspMapComponent &&
                   MissionMapAddOn.MISSION_PREVIEW_STRING.equals(((ILcyLspMapComponent) mapComponent).getType())) {
          previewMapAction.setEnabled(true);
        }
      }
    }
  }
}
