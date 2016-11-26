package com.luciad.dengue.lucy;

import com.luciad.imaging.ALcdImage;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.awt.*;
import java.util.Collection;

/**
 * @author Thomas De Bodt
 */
public class PrecipitationStyler extends ALspStyler {
  private double fMin = 0;
  private double fMax = 1000;

  public void setRangeFilter(double aMin, double aMax) {
    fMin = aMin;
    fMax = aMax;
    fireStyleChangeEvent();
  }

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aALspStyleCollector, TLspContext aTLspContext) {
    for(Object o : aCollection) {
      ALcdImage img = ALcdImage.fromDomainObject(o);
      if(img != null) {
        final double[] levels = new double[]{
            Short.MIN_VALUE,
            fMin,
            fMax,
            Short.MAX_VALUE
        };
        final Color[] colors = new Color[]{
            new Color(0, true),
            new Color(0, true),
            new Color(0, 13, 52, 128),
            new Color(0, true),
            };

        aALspStyleCollector.object(o).style(
            TLspRasterStyle
                .newBuilder()
                .startResolutionFactor(Double.POSITIVE_INFINITY)
                .colorMap(new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), levels, colors))
                .build()
        ).submit();
      }
    }
  }
}
