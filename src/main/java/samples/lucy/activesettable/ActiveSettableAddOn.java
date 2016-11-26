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
package samples.lucy.activesettable;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.ILcyMapManagerListener;
import com.luciad.lucy.map.TLcyMapManagerEvent;
import com.luciad.lucy.map.action.TLcyGXYSetControllerActiveSettable;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.tooltip.ToolTipController;

/**
 * Add-on which creates an active settable for a custom controller. The
 * active settable is inserted in all action bars included in the configuration
 * file of this add-on.
 */
public class ActiveSettableAddOn extends ALcyPreferencesAddOn {
  /**
   * Default constructor
   */
  public ActiveSettableAddOn() {
    super(ALcyTool.getLongPrefix(ActiveSettableAddOn.class),
          ALcyTool.getShortPrefix(ActiveSettableAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    //init all existing and future maps
    MapComponentListener mapComponentListener = new MapComponentListener();
    aLucyEnv.getMapManager().addMapManagerListener(mapComponentListener, true);
  }

  /**
   * Initializes the given <code>ILcyMapComponent</code>.<p>
   * <p/>
   * It adds a continuous pan controller to the toolbar and menu bar of the given map.
   *
   * @param aMapComponent The map component to initialize.
   */
  private void initMapComponent(ILcyMapComponent aMapComponent) {
    initToolTipController(aMapComponent);
  }

  private void initToolTipController(ILcyMapComponent aMapComponent) {
    ILcdGXYView gxyView = aMapComponent.getMainGXYView();

    //We use the tool tip controller from another sample
    ToolTipController toolTipController = new ToolTipController();

    //Combine the tool tip controller with Lucy's navigate behavior (zoom + pan with mouse wheel).
    //Note that this is completely optional.
    ILcdGXYController controller = TLcyGXYSetControllerActiveSettable.combineWithNavigateController(aMapComponent, toolTipController);

    ControllerActiveSettable controllerActiveSettable = new ControllerActiveSettable(
        controller, gxyView);

    //insert the controller in all action bars as defined in the configuration file
    controllerActiveSettable.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "tooltipControllerActiveSettable");
    TLcyActionBarManager actionBarManager = getLucyEnv().getUserInterfaceManager().getActionBarManager();
    TLcyActionBarUtil.insertInConfiguredActionBars(controllerActiveSettable,
                                                   // The active settable should only be inserted in the UI for that
                                                   // specific map component, so pass the map component as context
                                                   // object
                                                   aMapComponent,
                                                   actionBarManager,
                                                   // The location(s) where the active settable should be inserted are
                                                   // defined in the preferences of this add-on, so we pass those as well
                                                   getPreferences(),
                                                   // The 'false' indicates that the controller cannot be deactivated by
                                                   // using the gui widget that represents the active settable.  E.g.
                                                   // you cannot unpress the button. This is the same difference as
                                                   // the difference between a checkbox and a radio button.
                                                   false);

  }

  private class MapComponentListener implements ILcyMapManagerListener {
    @Override
    public void mapManagerChanged(TLcyMapManagerEvent aMapManagerEvent) {
      if (aMapManagerEvent.getId() == TLcyMapManagerEvent.MAP_COMPONENT_ADDED) {
        initMapComponent(aMapManagerEvent.getMapComponent());
      }
    }
  }
}
