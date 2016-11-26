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
package samples.lightspeed.timeview.styling;

import static samples.lightspeed.timeview.model.TimeAxisModel.TimeTick.Granularity.*;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

import samples.common.UIColors;
import samples.lightspeed.labels.util.FixedTextProviderStyle;
import samples.lightspeed.timeview.model.TimeAxisModel;

/**
 * Styler for the labels for the ticks on the time axis.
 */
public final class TimeAxisLabelStyler extends ALspLabelStyler {

  private static final Color COLOR = UIColors.fgAccent();

  private static final DateFormat YEAR_FORMAT = new SimpleDateFormat("   yyyy   ");
  private static final DateFormat MONTH_FORMAT = new SimpleDateFormat("  MMM  ");
  private static final DateFormat DAY_FORMAT = new SimpleDateFormat("  MMM dd  ");
  private static final DateFormat HOUR_FORMAT = new SimpleDateFormat("  HH:mm  ");
  private static final int LABEL_POSITION_BELOW_LINE = 25;

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aCollector, TLspContext aTLspContext) {
    for (Object object : aObjects) {
      if (object instanceof TimeAxisModel.TimeTick) {

        TimeAxisModel.TimeTick tick = (TimeAxisModel.TimeTick) object;
        TimeAxisModel.TimeTick.Granularity granularity = tick.getGranularity();

        Calendar time = Calendar.getInstance();
        time.setTime(new Date(tick.getTime()));
        String text = granularity == YEAR ? YEAR_FORMAT.format(time.getTime()) :
                      granularity == MONTH ? MONTH_FORMAT.format(time.getTime()).toUpperCase() :
                      granularity == DAY ? DAY_FORMAT.format(time.getTime()) :
                      HOUR_FORMAT.format(time.getTime());

        TLspTextStyle style = TLspTextStyle.newBuilder()
                                           .font(Font.decode("SansSerif").deriveFont((float) (20 - (4 * granularity.ordinal()))))
                                           .textColor(COLOR)
                                           .haloThickness(0)
                                           .build();

        aCollector.object(object)
                  .styles(FixedTextProviderStyle.newBuilder().text(text).build(), style)
                  .priority(granularity.ordinal())
                  .locations(LABEL_POSITION_BELOW_LINE - style.getFont().getSize(), TLspLabelLocationProvider.Location.SOUTH)
                  .submit();
      }
    }
  }
}
