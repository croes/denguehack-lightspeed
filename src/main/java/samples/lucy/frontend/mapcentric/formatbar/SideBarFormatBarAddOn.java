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
package samples.lucy.frontend.mapcentric.formatbar;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.action.ALcyCreateLayersAction;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * <p>
 *   Add-on that displays all format bars for a Lightspeed map directly into the sidebar.
 *   All format bars are visible from the start.
 * </p>
 */
public class SideBarFormatBarAddOn extends ALcyPreferencesAddOn {

  private static final String NEW_LAYER_ID = "SideBarFormatBarAddOn.newLayerAction";

  public SideBarFormatBarAddOn() {
    super("samples.lucy.frontend.mapcentric.formatbar.",
          "SideBarFormatBarAddOn.");
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    aLucyEnv.getCombinedMapManager().addMapManagerListener(new MapManagerListener(), true);
  }

  /**
   * Adds the format bars and new layer actions to the given map component
   * @param aLucyEnv The Lucy backend
   * @param aMapComponent The map component
   */
  private void setupForMapComponent(ILcyLucyEnv aLucyEnv, ILcyLspMapComponent aMapComponent) {
    @SuppressWarnings("unused")
    FormatBarMediator<ILspView, ILspLayer> mediator = new FormatBarMediator<>(aLucyEnv, aMapComponent, getPreferences(), getShortPrefix());
    createNewLayerActions(aLucyEnv, aMapComponent);
  }

  /**
   * <p>
   *   Creates and adds actions to the UI which allows to create a new layer using the model factories
   *   registered on the Lucy back-end (e.g. the action to create a new Lucy drawing layer, or a
   *   military symbology layer).
   * </p>
   *
   * @param aLucyEnv The Lucy back-end
   * @param aMapComponent The map component for which the actions must be created
   *
   * @see ALcyCreateLayersAction
   */
  private void createNewLayerActions(ILcyLucyEnv aLucyEnv, ILcyLspMapComponent aMapComponent) {
    ALcyCreateLayersAction<?, ?> action = new TLcyLspCreateLayerAction(aLucyEnv, aMapComponent);
    action.addUndoableListener(aLucyEnv.getUndoManager());
    action.putValue(TLcyActionBarUtil.ID_KEY, NEW_LAYER_ID);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        aLucyEnv.getUserInterfaceManager().getActionBarManager(),
        getPreferences());
  }

  /**
   * <p>
   *   The format bars and new layers actions must be added to each map.
   *   This listener is attached to the map manager, and will be informed each time a new map is added
   *   to the map manager.
   * </p>
   *
   * <p>
   *   LucyMapCentric only has a single map, but loading a workspace will remove the current map and
   *   create a new one.
   *   Therefore we need this listener, even when only one map is available in the UI.
   * </p>
   */
  private class MapManagerListener implements ILcyGenericMapManagerListener<ILcdView, ILcdLayer> {
    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent aMapManagerEvent) {
      ILcyGenericMapComponent mapComponent = aMapManagerEvent.getMapComponent();
      if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED &&
          mapComponent instanceof ILcyLspMapComponent) {
        setupForMapComponent(getLucyEnv(), (ILcyLspMapComponent) mapComponent);
      }
    }
  }
}
