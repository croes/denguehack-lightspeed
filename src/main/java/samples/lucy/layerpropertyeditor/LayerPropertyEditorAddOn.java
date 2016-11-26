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
package samples.lucy.layerpropertyeditor;

import java.util.IdentityHashMap;
import java.util.Map;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyAddOn;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * This addon allows the editing of the properties of a selected layer.  When active, it will try
 * to create a {@link com.luciad.lucy.gui.customizer.ILcyCustomizerPanel ILcyCustomizerPanel} that
 * can show and/or modify the properties of the selected layer.
 * <p/>
 * It tries to create such an <code>ILcyCustomizerPanel</code> by iterating over all registered
 * <code>ILcyCustomizerPanelFactory</code> objects with a <code>TLcyLayerContext</code> that contains
 * the selected layer and the view in which it is contained.
 */
public class LayerPropertyEditorAddOn extends ALcyAddOn {

  private ILcyLucyEnv fLucyEnv;
  private MapComponentListener fMapComponentListener = new MapComponentListener();
  public boolean fShowReadMe = true;

  private Map<ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer>, LayerPropertyEditorActiveSettable> fMapToActiveSettableMap = new IdentityHashMap<ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer>, LayerPropertyEditorActiveSettable>();

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;

    //initialize existing maps and maps that will be created afterwards
    aLucyEnv.getCombinedMapManager().addMapManagerListener(fMapComponentListener, true);
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    TLcyCombinedMapManager map_manager = aLucyEnv.getCombinedMapManager();
    map_manager.removeMapManagerListener(fMapComponentListener);
    for (ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> map_component : map_manager.getMapComponents()) {
      cleanupForMapComponent(map_component);
    }
    fMapComponentListener = null;
  }

  private void initMapComponent(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {

    LayerPropertyEditorActiveSettable active_settable = new LayerPropertyEditorActiveSettable(fLucyEnv, aMapComponent);

    TLcyActionBarManager actionBarManager = fLucyEnv.getUserInterfaceManager().getActionBarManager();
    ILcyActionBar mapMenu = actionBarManager.getActionBar("menuBar", aMapComponent);
    //Insert the active settable in the menu bar.
    mapMenu.insertActiveSettable(
        active_settable,
        new TLcyGroupDescriptor("SelectionEditorGroup"),

        //These strings needs to be translated to make sure that when Lucy is started
        //in another language (e.g. French), the menu name is translated as well
        new String[]{TLcyLang.getString("Map")},

        new TLcyGroupDescriptor[]{
            new TLcyGroupDescriptor("MapGroup"),
            new TLcyGroupDescriptor("SelectionEditorGroup")},

        //Use a checkbox.
        true
                                );

    active_settable.setActive(true);

    fMapToActiveSettableMap.put(aMapComponent, active_settable);
  }

  private void cleanupForMapComponent(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    ILcyActiveSettable active_settable = fMapToActiveSettableMap.remove(aMapComponent);
    if (active_settable.isActive()) {
      active_settable.setActive(false);
    }
    aMapComponent.getMenuBar().removeActiveSettable(active_settable);
  }

  private class MapComponentListener implements ILcyGenericMapManagerListener<ILcdView, ILcdLayer> {

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
      if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
        initMapComponent(aMapManagerEvent.getMapComponent());
      } else if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED) {
        cleanupForMapComponent(aMapManagerEvent.getMapComponent());
      }
    }
  }

}
