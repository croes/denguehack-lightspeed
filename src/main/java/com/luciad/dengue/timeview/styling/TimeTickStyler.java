package com.luciad.dengue.timeview.styling;

import com.luciad.dengue.timeview.model.TimeTick;
import com.luciad.gui.TLcdSymbol;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.util.Collection;

/**
 * Styler for the tick marks on the time axis
 */
public class TimeTickStyler extends ALspStyler {

  private static final TLspLineStyle DEFAULT = TLspLineStyle.newBuilder().color(ColorPalette.green).width(2.5).build();
  private static final TLspIconStyle ICON_STYLE = TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.PLUS, 10, ColorPalette.green)).build();

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aStyleCollector, TLspContext aTLspContext) {
    for (Object object : aCollection) {
      if (object instanceof TimeTick) {
        if (((TimeTick) object).getPriority() <= TimeTick.PRIO_3) {
          aStyleCollector.object(object).style(ICON_STYLE).submit();
        }
      } else {
        aStyleCollector.object(object).style(DEFAULT).submit();
      }
    }
  }
}
