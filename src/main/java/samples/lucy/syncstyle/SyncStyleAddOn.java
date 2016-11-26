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
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.lightspeed.common.MapComponentUtil;

/**
 * Add-on which adds an active settable allowing to synchronize the styles of different layers,
 * possibly on different maps
 */
public class SyncStyleAddOn extends ALcyPreferencesAddOn {

  public SyncStyleAddOn() {
    super(ALcyTool.getLongPrefix(SyncStyleAddOn.class),
          ALcyTool.getShortPrefix(SyncStyleAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    //create and insert the active settable
    StyleSynchronizationActiveSettable activeSettable = new StyleSynchronizationActiveSettable(aLucyEnv);
    activeSettable.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "styleSynchronizationActiveSettable");
    TLcyActionBarUtil.insertInConfiguredActionBars(activeSettable, null, aLucyEnv.getUserInterfaceManager().getActionBarManager(), getPreferences());

    //make sure a Lightspeed map is available, and add the world.shp file to it
    ILcyGenericMapComponent<ILspView, ILspLayer> lightspeedMap = MapComponentUtil.activateLightspeedMap(aLucyEnv);
    aLucyEnv.getDataFormatManager().handleDataSources(new String[]{"Data/Shp/World/world.shp"}, lightspeedMap, null, null);
  }
}
