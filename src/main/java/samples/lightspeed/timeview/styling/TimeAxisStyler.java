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

import static samples.lightspeed.timeview.model.TimeAxisModel.TimeTick.Granularity.HOUR;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.common.UIColors;
import samples.lightspeed.timeview.model.TimeAxisModel;

/**
 * Styler for the elements on the time axis (ticks, horizontal and vertical line).
 */
public class TimeAxisStyler extends ALspStyler {

  private static final Color COLOR = UIColors.fgAccent();
  private static final TLspLineStyle LINE_STYLE = TLspLineStyle.newBuilder().color(COLOR).width(2.5).build();
  private static final TLspIconStyle ICON_STYLE = TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.PLUS, 10, COLOR)).build();

  /**
   * Draw the current time marker in the middle of the screen in pixel space.
   * This way, the marker is always in the center of the screen regardless of asynchronous updates.
   */
  private static final ALspStyleTargetProvider CENTER_LINE_PROVIDER = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aList) {
      aList.add(new TLcdXYLine(new TLcdXYPoint(aContext.getView().getWidth() / 2, 0),
                               new TLcdXYPoint(aContext.getView().getWidth() / 2, aContext.getView().getHeight())));
    }

    @Override
    public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
      return null; // we want to draw in screen space
    }
  };

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aStyleCollector, TLspContext aTLspContext) {
    for (Object object : aCollection) {
      if (object instanceof TimeAxisModel.TimeLine) {
        aStyleCollector.object(object).style(LINE_STYLE).submit();
      } else if (object instanceof TimeAxisModel.CurrentTime) {
        aStyleCollector.object(object).style(LINE_STYLE).geometry(CENTER_LINE_PROVIDER).submit();
      } else if (object instanceof TimeAxisModel.TimeTick &&
                 ((TimeAxisModel.TimeTick) object).getGranularity().ordinal() <= HOUR.ordinal()) {
        aStyleCollector.object(object).style(ICON_STYLE).submit();
      }
    }
  }
}
