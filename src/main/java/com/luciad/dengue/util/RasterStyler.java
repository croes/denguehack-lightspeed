package com.luciad.dengue.util;

import com.luciad.imaging.ALcdImage;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.util.Collection;

/**
 * @author Thomas De Bodt
 */
public class RasterStyler extends ALspStyler {
  private TLcdColorMap fColorMap;

  public RasterStyler() {
  }

  public RasterStyler(TLcdColorMap aColorMap) {
    fColorMap = aColorMap;
  }

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aALspStyleCollector, TLspContext aTLspContext) {
    for(Object o : aCollection) {
      ALcdImage img = ALcdImage.fromDomainObject(o);
      if(img != null) {
        aALspStyleCollector.object(o).style(
            TLspRasterStyle
                .newBuilder()
                .startResolutionFactor(Double.POSITIVE_INFINITY)
                .colorMap(fColorMap)
                .build()
        ).submit();
      }
    }
  }
}
