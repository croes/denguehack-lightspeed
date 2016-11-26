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
package samples.lucy.fundamentals.waypoints;

import java.util.Objects;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.lightspeed.ALcyLspFormatAddOn;
import com.luciad.lucy.format.lightspeed.ALcyLspFormat;
import com.luciad.lucy.format.lightspeed.TLcyLspSafeGuardFormatWrapper;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.fundamentals.waypoints.model.WayPointsModelAddOn;
import samples.lucy.util.LayerUtil;

/**
 * <p>
 *   Add-on which adds support to Lucy for the visualization of way points data.
 * </p>
 */
public final class WayPointsAddOn extends ALcyLspFormatAddOn {
  public WayPointsAddOn() {
    super(ALcyTool.getLongPrefix(WayPointsAddOn.class),
          ALcyTool.getShortPrefix(WayPointsAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    final WayPointsModelAddOn wayPointsModelAddOn = aLucyEnv.retrieveAddOnByClass(WayPointsModelAddOn.class);
    Objects.requireNonNull(wayPointsModelAddOn, "Please load the " + WayPointsModelAddOn.class.getSimpleName() + " before loading this add-on");
    super.plugInto(aLucyEnv);

    TLcyCombinedMapManager combinedMapManager = aLucyEnv.getCombinedMapManager();
    combinedMapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> mapComponent = aMapManagerEvent.getMapComponent();
          if (mapComponent.getMainView() instanceof ILspView) {
            LayerUtil.insertCreateLayerAction(getPreferences(),
                                              getShortPrefix(),
                                              wayPointsModelAddOn.getFormat(),
                                              (ILcyGenericMapComponent<ILspView, ILspLayer>) mapComponent);
          }
        }
      }
    }, true);
  }

  @Override
  protected ALcyLspFormat createBaseFormat() {
    return new WayPointsFormat(getLucyEnv(),
                               getLongPrefix(),
                               getShortPrefix(),
                               getPreferences());
  }

  @Override
  protected ALcyLspFormat createFormatWrapper(ALcyLspFormat aBaseFormat) {
    return new TLcyLspSafeGuardFormatWrapper(aBaseFormat);
  }
}
