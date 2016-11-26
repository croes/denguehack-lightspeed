package com.luciad.dengue.timeview.styling;

import com.luciad.dengue.timeview.model.TimeTick;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

import java.awt.*;
import java.util.Collection;

/**
 * Styler for the labels on the time axis
 */
public final class TimeTickLabelStyler extends ALspLabelStyler {

  private static final TLspTextStyle P10 = TLspTextStyle.newBuilder()
                                                        .font(Font.decode("SansSerif").deriveFont(20f))
                                                        .textColor(ColorPalette.green.darker().darker())
                                                        .haloThickness(0)
                                                        .build();
  private static final TLspTextStyle P100 = TLspTextStyle.newBuilder()
                                                         .font(Font.decode("SansSerif").deriveFont(16f))
                                                         .textColor(ColorPalette.green.darker().darker())
                                                         .haloThickness(0)
                                                         .build();
  private static final TLspTextStyle P1000 = TLspTextStyle.newBuilder()
                                                          .font(Font.decode("SansSerif").deriveFont(10f))
                                                          .textColor(ColorPalette.green.darker().darker())
                                                          .haloThickness(0)
                                                          .build();
  private static final TLspTextStyle P10000 = TLspTextStyle.newBuilder()
                                                           .font(Font.decode("SansSerif").deriveFont(8f))
                                                           .textColor(ColorPalette.green.darker().darker())
                                                           .haloThickness(0)
                                                           .build();

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aCollector, TLspContext aTLspContext) {
    for (Object object : aObjects) {
      if (object instanceof TimeTick) {
        int priority = ((TimeTick) object).getPriority();
        TLspTextStyle style = priority < 10 ? P10 :
                              priority < 100 ? P100 :
                              priority < 1000 ? P1000 :
                              P10000;

        aCollector.object(object)
                  .style(style)
                  .priority(new PriorityProvider())
                  .locations(25 - style.getFont().getSize(), TLspLabelLocationProvider.Location.SOUTH)
                  .submit();
      }
    }
  }

  private class PriorityProvider implements ILspLabelPriorityProvider {
    @Override
    public int getPriority(TLspLabelID aLabelId, TLspPaintState aTLspPaintState, TLspContext aTLspContext) {
      return aLabelId.getDomainObject() instanceof TimeTick ? ((TimeTick) aLabelId.getDomainObject()).getPriority() : 0;
    }
  }
}
