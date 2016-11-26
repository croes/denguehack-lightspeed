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
package samples.lucy.gxy.waypoints;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyFormatAddOn;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.format.TLcyAsynchronousFormatWrapper;
import com.luciad.lucy.format.TLcyMutableFileFormatWrapper;
import com.luciad.lucy.format.TLcySafeGuardFormatWrapper;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

import samples.lucy.util.LayerUtil;

public final class GXYWayPointsAddOn extends ALcyFormatAddOn {

  public GXYWayPointsAddOn() {
    super(ALcyTool.getLongPrefix(GXYWayPointsAddOn.class),
          ALcyTool.getShortPrefix(GXYWayPointsAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    TLcyCombinedMapManager mapManager = aLucyEnv.getCombinedMapManager();
    mapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED &&
            aMapManagerEvent.getMapComponent() instanceof ILcyMapComponent) {
          ILcyMapComponent mapComponent = (ILcyMapComponent) aMapManagerEvent.getMapComponent();
          LayerUtil.insertCreateLayerAction(getPreferences(),
                                            getShortPrefix(),
                                            getFormat(),
                                            mapComponent);
        }
      }
    }, true);
  }

  @Override
  protected ALcyFormat createBaseFormat() {
    return new GXYWayPointsModelFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyFormat createFormatWrapper(ALcyFormat aBaseFormat) {
    return new TLcySafeGuardFormatWrapper(
        new TLcyAsynchronousFormatWrapper(
            new TLcyMutableFileFormatWrapper(aBaseFormat)
        ));
  }
}
