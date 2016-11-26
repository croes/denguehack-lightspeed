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
package samples.lucy.loadstyle;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * AddOn which allows to change the style of the world layer
 *
 * @since 10.1
 */
public class LoadStyleAddOn extends ALcyPreferencesAddOn {

  private static final String[] ACTION_NAMES = new String[]{
      "defaultStyleAction",
      "airtrackStyleAction",
      "marineStyleAction",
      "lightStyleAction"
  };

  public LoadStyleAddOn() {
    super(ALcyTool.getLongPrefix(LoadStyleAddOn.class),
          ALcyTool.getShortPrefix(LoadStyleAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    //add actions to every existing and new map component
    aLucyEnv.getCombinedMapManager().addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          initMapComponent(aMapManagerEvent.getMapComponent(),
                           getLucyEnv());
        }
      }
    }, true);

  }

  private void initMapComponent(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent,
                                ILcyLucyEnv aLucyEnv) {
    for (String ACTION_NAME : ACTION_NAMES) {
      ChangeStyleAction action = new ChangeStyleAction(aMapComponent,
                                                       getPreferences(),
                                                       getShortPrefix() + ACTION_NAME + ".",
                                                       aLucyEnv);
      action.putValue(TLcyActionBarUtil.ID_KEY,
                      getShortPrefix() + ACTION_NAME);

      TLcyActionBarUtil.insertInConfiguredActionBars(action,
                                                     aMapComponent,
                                                     aLucyEnv.getUserInterfaceManager().getActionBarManager(),
                                                     getPreferences());
    }
  }
}
