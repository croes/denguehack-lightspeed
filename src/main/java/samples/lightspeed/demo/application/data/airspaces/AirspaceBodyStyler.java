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
package samples.lightspeed.demo.application.data.airspaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.gui.TLcdColor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

/**
 * @author tomn
 * @since 2012.0
 */
public class AirspaceBodyStyler extends AAnimatedHighlightStyler {

  private static final Color FILL_COLOR = new Color(33, 151, 255, 50);
  private static final Color LINE_COLOR = new Color(20, 96, 180, 254);
  private static final Color HIGHLIGHTED_FILL_COLOR = new Color(66, 255, 255, 180);
  private static final Color HIGHLIGHTED_LINE_COLOR = new Color(20, 96, 180, 254);

  public AirspaceBodyStyler() {
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    ArrayList<Object> defaultObjects = new ArrayList<Object>();

    for (Object o : aObjects) {
      float alpha = (float) getAlphaForObject(o);

      if (alpha < 1e-3) {
        defaultObjects.add(o);
      } else {
        aStyleCollector
            .object(o)
            .styles(getStyles(alpha, aContext))
            .submit();
      }
    }

    if (defaultObjects.size() > 0) {
      aStyleCollector
          .objects(defaultObjects)
          .styles(getStyles(0f, aContext))
          .submit();
    }
  }

  private ALspStyle[] getStyles(float aAlpha, TLspContext aContext) {
    double a = aAlpha * aAlpha * (3 - 2 * aAlpha);
    return new ALspStyle[]{
        TLspFillStyle.newBuilder()
                     .color(TLcdColor
                                .interpolate(FILL_COLOR, HIGHLIGHTED_FILL_COLOR, a))
                     .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                     .zOrder((int) (aAlpha * 255))
                     .build(),
        TLspLineStyle.newBuilder()
                     .color(TLcdColor
                                .interpolate(LINE_COLOR, HIGHLIGHTED_LINE_COLOR, a))
                     .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                     .zOrder(1 + (int) (aAlpha * 255))
                     .build()
    };
  }

  @Override
  protected double getFadeInDuration(Object aFadeInObject, ILcdModel aFadeInModel) {
    return 0.25;
  }

  @Override
  protected double getFadeOutDuration(Object aFadeOutObject, ILcdModel aFadeOutModel) {
    return 1;
  }
}
