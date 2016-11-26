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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.addons.lspmap.TLcyLspMapAddOn;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * This add-on keeps the preparation map components and the preview map components in sync.
 */
public class MissionMapAddOn extends ALcyPreferencesAddOn {

  static final String MISSION_PREPARATION_STRING = "preparation";
  static final String MISSION_PREVIEW_STRING = "preview";

  /**
   * Default constructor
   */
  public MissionMapAddOn() {
    super(ALcyTool.getLongPrefix(MissionMapAddOn.class),
          ALcyTool.getShortPrefix(MissionMapAddOn.class));
  }

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    TLcyLspMapAddOn mapAddOn = aLucyEnv.retrieveAddOnByClass(TLcyLspMapAddOn.class);
    if (mapAddOn == null) {
      throw new UnsupportedOperationException("Please load the TLcyLspMapAddOn before loading this MissionMapAddOn");
    }
    TLcyLspMapManager mapManager = mapAddOn.getLucyEnv().getService(TLcyLspMapManager.class);

    //keep the preview and preparation maps in sync
    mapManager.addMapManagerListener(new MapSynchronizerListener(aLucyEnv), true);

    //add an action to each preparation map to create a preview map
    mapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILspView, ILspLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
        ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> mapComponent = aMapManagerEvent.getMapComponent();
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED &&
            mapComponent instanceof ILcyLspMapComponent &&
            MISSION_PREPARATION_STRING.equals(((ILcyLspMapComponent) mapComponent).getType())) {

          TLcyActionBarManager actionBarManager = aLucyEnv.getUserInterfaceManager().getActionBarManager();

          PreviewMapAction action = new PreviewMapAction(aLucyEnv);
          // Store the identifier for the action as key-value pair in the action
          String actionIdentifier = getShortPrefix() + "openMissionPreviewMap";
          action.putValue(TLcyActionBarUtil.ID_KEY, actionIdentifier);
          // Use the information in the getPreferences (=the config file) to
          // insert the action in all requested action bars
          TLcyActionBarUtil.insertInConfiguredActionBars(action,
                                                         mapComponent,
                                                         actionBarManager,
                                                         getPreferences());
        }
      }
    }, true);

  }

}
