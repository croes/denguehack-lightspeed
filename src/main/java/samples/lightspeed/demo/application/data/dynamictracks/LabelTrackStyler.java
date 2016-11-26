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

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.NORTH;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.Collection;

import com.luciad.realtime.lightspeed.labeling.TLspContinuousLabelingAlgorithm;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lightspeed.common.tracks.EnrouteAirwayTrack;

public class LabelTrackStyler extends TrackStylerBase {

  private final TLspLabelStyler fRegularStyler;
  private final TLspLabelStyler fSelectedStyler;
  private final boolean fSelected;

  public LabelTrackStyler(boolean aLabelDecluttering, boolean aSelected) {
    fSelected = aSelected;
    ALspLabelTextProviderStyle textProvider = new ALspLabelTextProviderStyle() {
      @Override
      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
        if (aDomainObject instanceof EnrouteAirwayTrack) {
          return new String[]{((EnrouteAirwayTrack) aDomainObject).getTrackName()};
        } else {
          return null;
        }
      }
    };

    TLspPinLineStyle pinStyle = TLspPinLineStyle.newBuilder()
                                                .color(new Color(50, 50, 200))
                                                .width(1.5f)
                                                .build();

    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font(Font.decode("Default-BOLD-10"))
                                           .textColor(new Color(50, 50, 200))
                                           .haloColor(new Color(255, 255, 255))
                                           .haloThickness(1)
                                           .build();

    TLspTextStyle selectedStyle = textStyle.asBuilder()
                                           .haloColor(new Color(50, 255, 255))
                                           .build();

    ILspLabelingAlgorithm labelingAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(20, NORTH));
    if (!aLabelDecluttering) {
      TLspContinuousLabelingAlgorithm continuousAlgorithm = new TLspContinuousLabelingAlgorithm();
      continuousAlgorithm.setPadding(1);
      continuousAlgorithm.setDesiredRelativeLocation(new Point(0, -20));
      continuousAlgorithm.setMaxDistance(50);
      continuousAlgorithm.setMaxCoverage(0.333);
      labelingAlgorithm = continuousAlgorithm;
    }

    fRegularStyler = TLspLabelStyler.newBuilder()
                                    .group(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP)
                                    .algorithm(labelingAlgorithm)
                                    .styles(textStyle, pinStyle, textProvider)
                                    .build();
    fSelectedStyler = TLspLabelStyler.newBuilder()
                                     .group(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP)
                                     .algorithm(labelingAlgorithm)
                                     .styles(selectedStyle, pinStyle, textProvider)
                                     .build();

  }

  @Override
  protected void styleAsPoint(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    //only show labels when painted as aircraft
    aStyleCollector.objects(aObjects).hide().submit();
  }

  @Override
  protected void styleAsTrails(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (fSelected) {
      fSelectedStyler.style(aObjects, aStyleCollector, aContext);
    } else {
      fRegularStyler.style(aObjects, aStyleCollector, aContext);
    }
  }

  @Override
  protected void styleAsAircraft(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    styleAsTrails(aObjects, aStyleCollector, aContext);
  }
}
