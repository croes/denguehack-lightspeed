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
package samples.lucy.lightspeed.style.loading;

import java.awt.Color;

import com.luciad.gui.ALcdAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.lightspeed.common.MapComponentUtil;

/**
 * Add-on which adds actions which load a layer style and tries to apply it on the
 * selected layers.
 */
public class LoadStyleAddOn extends ALcyPreferencesAddOn {

  public LoadStyleAddOn() {
    super(ALcyTool.getLongPrefix(LoadStyleAddOn.class),
          ALcyTool.getShortPrefix(LoadStyleAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    //make sure the actions are added for each Lightspeed map component
    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    if (mapManager == null) {
      throw new UnsupportedOperationException("No TLcyLspMapManager found. Please load the TLcyLspMapAddOn before this add-on");
    }
    mapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILspView, ILspLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> mapComponent = aMapManagerEvent.getMapComponent();
          //create an action for each style and add it to the action bars
          String[] actions = new String[]{"defaultStyleAction", "airtrackStyleAction", "marineStyleAction", "lightStyleAction"};
          for (String actionName : actions) {
            String styleFile = getPreferences().getString(getShortPrefix() + actionName + ".styleFile", null);
            if (styleFile != null) {
              ALcdAction action = new LoadStyleAction(mapComponent,
                                                      styleFile,
                                                      getPreferences().getColor(getShortPrefix() + actionName + ".selectionLineColor", Color.ORANGE),
                                                      getPreferences().getColor(getShortPrefix() + actionName + ".selectionFillColor", Color.ORANGE),
                                                      getPreferences().getColor(getShortPrefix() + actionName + ".selectionTextColor", Color.ORANGE),
                                                      getLucyEnv());
              action.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + actionName);
              TLcyActionBarUtil.insertInConfiguredActionBars(action, mapComponent, getLucyEnv().getUserInterfaceManager().getActionBarManager(), getPreferences());
            }
          }
        }
      }
    }, true);

    //make sure a Lightspeed map is available from the start
    MapComponentUtil.activateLightspeedMap(aLucyEnv);
  }
}
