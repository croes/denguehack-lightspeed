package com.luciad.dengue.timeview.styling;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.util.Collection;
import java.util.List;

/**
 * Styler for the vertical line that indicates the current time in the time view.
 */
public class CurrentTimeStyler extends ALspStyler {

  private static final TLspLineStyle STYLE = TLspLineStyle.newBuilder().color(ColorPalette.green).width(2.5).build();
  private static final ALspStyleTargetProvider TARGET_PROVIDER = new ScreenCoordsTargetProvider();

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aCollector, TLspContext aContext) {
    aCollector.objects(aCollection).geometry(TARGET_PROVIDER).style(STYLE).submit();
  }

  private static class ScreenCoordsTargetProvider extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aList) {
      aList.add(new TLcdXYLine(new TLcdXYPoint(aContext.getView().getWidth() / 2, 0),
                               new TLcdXYPoint(aContext.getView().getWidth() / 2, aContext.getView().getHeight())));
    }

    @Override
    public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
      return null; // we want to draw in screen space
    }
  }

}
