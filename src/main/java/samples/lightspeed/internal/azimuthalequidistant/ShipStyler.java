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
package samples.lightspeed.internal.azimuthalequidistant;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

/**
 * Draws an icon at the ship position and 10 concentric circles with increasing radii.
 */
class ShipStyler extends ALspStyler {

  private TLspIconStyle fIconStyle = TLspIconStyle.newBuilder()
                                                  .icon(new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 17, Color.blue, Color.orange))
                                                  .useOrientation(true)
                                                  .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                  .build();

  private TLspLineStyle fLineStyle = TLspLineStyle.newBuilder()
                                                  .color(Color.yellow)
                                                  .opacity(0.3f)
                                                  .build();

  private ALspStyleTargetProvider fStyleTargetProvider = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      for (int i = 0; i < 10; i++) {
        TLcdLonLatCircle circle = new TLcdLonLatCircle((ILcdPoint) aObject, (i + 1) * Compare3DGeocentricWithAzimuthalCylindrical.NAVIGATION_LIMIT / 10, TLcdEllipsoid.DEFAULT);
        aResultSFCT.add(circle);
      }
    }
  };

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object ship : aObjects) {
      // Center icon
      aStyleCollector.object(ship).style(fIconStyle).submit();
      aStyleCollector.object(ship).geometry(fStyleTargetProvider).style(fLineStyle).submit();
    }
  }
}
