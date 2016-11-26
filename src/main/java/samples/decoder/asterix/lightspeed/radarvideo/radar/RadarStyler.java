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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.realtime.lightspeed.radarvideo.TLspRadarVideoStyle;
import com.luciad.shape.shape2D.TLcdXYCircle;
import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.ILcdOriented;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Configurable styler for the radar visualization.
 */
public class RadarStyler extends ALspStyler {

  private RadarStyleProperties fProperties;
  private boolean fSelected;

  private ALspStyle fRadarStyle;
  private ALspStyle fGridStyle;
  private ALspStyle fSweepStyle;
  private ALspStyle fRingStyle;

  public RadarStyler( RadarStyleProperties aProperties, boolean aSelected ) {
    fProperties = aProperties;
    fSelected = aSelected;
    fProperties.addPropertyChangeListener( new PropertyChangeListener() {
      @Override
      public void propertyChange( PropertyChangeEvent evt ) {
        fireStyleChangeEvent();
      }
    } );
    createStyles();
  }

  private void createStyles() {
    fRadarStyle = TLspRadarVideoStyle
        .newBuilder()
        .blipColor(fProperties.getBlipColor())
        .blipAfterglowColor(fProperties.getAfterglowColor())
        .backgroundColor(fProperties.getBackgroundColor())
        .blipAfterglow(fProperties.getBlipAfterglowDuration())
        .intensity(fProperties.getIntensity())
        .amplitudeThreshold(fProperties.getThreshold())
        .build();

    fGridStyle = TLspLineStyle
        .newBuilder()
        .color(fProperties.getGridColor())
        .width(fSelected ? 2.5 : 1)
        .build();

    fRingStyle = TLspLineStyle
        .newBuilder()
        .color(fProperties.getGridColor())
        .width(fSelected ? 2.5 : 1)
        .build();

    fSweepStyle = TLspLineStyle
        .newBuilder()
        .color(fProperties.getSweepLineColor())
        .width(fSelected ? 3.5 : 2)
        .build();
  }

  @Override
  public void fireStyleChangeEvent() {
    createStyles();
  }

  //Style target provider to generate the angular grid.
  private static final ALspStyleTargetProvider sGridStyleTargetProvider = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      double range = (Double) ((ILcdDataObject) aObject).getValue("RadarRange");

      //Grid lines with 45 degree interval.
      TLcdXYPoint c = new TLcdXYPoint();
      TLcdXYPoint p = new TLcdXYPoint();
      for (int i = 0; i < 360; i += 45) {
        double alpha = Math.toRadians(90 - i);
        p.move2D(range * Math.cos(alpha), range * Math.sin(alpha));
        TLcdXYLine line = new TLcdXYLine(
            c, p.cloneAs2DEditablePoint()
        );
        aResultSFCT.add(line);
      }
    }
  };

  //Style target provider to generate the range rings.
  private static final ALspStyleTargetProvider sRangeRingStyleTargetProvider = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      double range = (Double) ((ILcdDataObject) aObject).getValue("RadarRange");

      //Range circles
      for (int i = 1; i <= 5; i++) {
        TLcdXYCircle c = new TLcdXYCircle(
            0, 0, i * range / 5.0
        );
        aResultSFCT.add(c);
      }
    }
  };

  //Style target provider to generate the sweep line.
  private static final ALspStyleTargetProvider sSweepLineStyleTargetProvider = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      double range = (Double) ((ILcdDataObject) aObject).getValue("RadarRange");
      double orientation = ((ILcdOriented) aObject).getOrientation();
      TLcdXYPoint c = new TLcdXYPoint();
      TLcdXYPoint p = new TLcdXYPoint();
      double alpha = Math.toRadians(90 - orientation);
      p.move2D(range * Math.cos(alpha), range * Math.sin(alpha));
      TLcdXYLine line = new TLcdXYLine(
          c, p.cloneAs2DEditablePoint()
      );
      aResultSFCT.add(line);
    }
  };

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    //Apply the style configuration defined in the RadarStyleProperties object.

    aStyleCollector
        .objects(aObjects)
        .styles(fRadarStyle)
        .submit();

    if (fProperties.isGridEnabled()) {
      aStyleCollector
          .objects(aObjects)
          .geometry(sGridStyleTargetProvider)
          .styles(fGridStyle)
          .submit();

      aStyleCollector
          .objects(aObjects)
          .geometry(sRangeRingStyleTargetProvider)
          .styles(fRingStyle)
          .submit();
    }

    if(fProperties.isSweepLineEnabled()){
      aStyleCollector
          .objects(aObjects)
          .geometry(sSweepLineStyleTargetProvider)
          .styles(fSweepStyle)
          .submit();
    }
  }
}
