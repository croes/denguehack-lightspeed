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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

import samples.lucy.fundamentals.waypoints.model.WayPointsModelAddOn;

/**
 * {@link ALcyFormatBarFactory} implementation for way point layers
 */
final class WayPointsFormatBarFactory extends ALcyFormatBarFactory {
  static final String TOOLBAR_ID = "wayPointsToolBar";

  private final ILcyLucyEnv fLucyEnv;
  private final ALcyProperties fProperties;
  private final String fShortPrefix;

  WayPointsFormatBarFactory(ILcyLucyEnv aLucyEnv, ALcyProperties aProperties, String aShortPrefix) {
    fLucyEnv = aLucyEnv;
    fProperties = aProperties;
    fShortPrefix = aShortPrefix;
  }

  @Override
  public boolean canCreateFormatBar(ILcdView aView, ILcdLayer aLayer) {
    // TLcyLspSafeGuardFormatWrapper already checks the layer
    return findLspMapComponent(aView) != null;
  }

  @Override
  public ALcyFormatBar createFormatBar(ILcdView aView, ILcdLayer aLayer) {
    WayPointsModelAddOn wayPointsModelAddOn = fLucyEnv.retrieveAddOnByClass(WayPointsModelAddOn.class);
    return new WayPointsFormatBar(findLspMapComponent(aView),
                                  fProperties,
                                  fShortPrefix,
                                  wayPointsModelAddOn.getFormat().getDefaultModelDescriptorFactories()[0],
                                  fLucyEnv);
  }

  private TLcyLspMapComponent findLspMapComponent(ILcdView aView) {
    ILcyGenericMapComponent mapComponent = fLucyEnv.getCombinedMapManager().findMapComponent(aView);
    return mapComponent instanceof TLcyLspMapComponent ? (TLcyLspMapComponent) mapComponent : null;
  }
}
