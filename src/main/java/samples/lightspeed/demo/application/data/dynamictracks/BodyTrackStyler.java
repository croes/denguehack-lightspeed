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
package samples.lightspeed.demo.application.data.dynamictracks;

import java.util.Collection;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.common.tracks.AirbornTrackProvider;

public class BodyTrackStyler extends TrackStylerBase {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ILspStyler.class);

  private static final String ICON = "Data/LightspeedDemo/DynamicTracks/Airbus_A321.dae";

  private final ALspStyle fPointStyle;
  private final TLspStyler fHistoryTrailStyler;
  private final ALspStyle fAircraftStyle;

  private final ALspStyleTargetProvider fAirbornTrackProvider = AirbornTrackProvider.getProvider();

  public BodyTrackStyler(ALspStyle aDefaultIconStyle, TLspStyler aHistoryTrailStyler) {
    fPointStyle = aDefaultIconStyle;
    fHistoryTrailStyler = aHistoryTrailStyler;

    int scaleFactor = 50;
    TLsp3DIconStyle iconStyle = null;
    try {
      iconStyle = TLsp3DIconStyle.newBuilder()
                                 .icon(ICON)
                                 .scale(scaleFactor)
                                 .iconSizeMode(TLsp3DIconStyle.ScalingMode.SCALE_FACTOR)
                                 .build();
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Could not load icon " + ICON + ": " + e.getMessage());
    }
    if (iconStyle != null) {
      fAircraftStyle = iconStyle;
    } else {
      fAircraftStyle = aDefaultIconStyle;
    }
  }

  @Override
  protected void styleAsPoint(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).style(fPointStyle).submit();
  }

  @Override
  protected void styleAsTrails(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    fHistoryTrailStyler.style(aObjects, aStyleCollector, aContext);
  }

  @Override
  protected void styleAsAircraft(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.geometry(fAirbornTrackProvider).objects(aObjects).style(fAircraftStyle).submit();
  }
}
