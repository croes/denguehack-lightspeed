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
package samples.lightspeed.demo.application.data.weather;

import static com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke.*;

import static samples.lightspeed.demo.application.data.weather.WeatherUtil.round;

import java.awt.Color;
import java.util.Collection;

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

class ContourBodyStyler extends ALspStyler {

  private static final ALspComplexStroke LINE_COMPLEX_STROKE = line().lengthRelative(0.2).lineColor(Color.DARK_GRAY).build();
  private static final ALspComplexStroke GAP = gap(10);

  private TemperatureUnit fTemperatureUnit = TemperatureUnit.CELSIUS;

  @Override
  public void style(Collection<?> aCollection, ALspStyleCollector aALspStyleCollector, TLspContext aTLspContext) {
    for (Object object : aCollection) {
      if (object instanceof Contour) {
        Contour contour = (Contour) object;
        TLspComplexStrokedLineStyle style = TLspComplexStrokedLineStyle.newBuilder()
                                                                       .regular(
                                                                           append(
                                                                               atomic(compose(
                                                                                       GAP,
                                                                                       text(getLabel(contour)).build(),
                                                                                       GAP)),
                                                                               LINE_COMPLEX_STROKE))
                                                                      .fallback(LINE_COMPLEX_STROKE)
                                                                      .build();
        aALspStyleCollector.object(contour)
                           .geometry(contour.getShape())
                           .style(style)
                           .submit();
      }
    }
  }

  public void setTemperatureUnit(TemperatureUnit aTemperatureUnit) {
    fTemperatureUnit = aTemperatureUnit;
    fireStyleChangeEvent();
  }

  private String getLabel(Contour aContour) {
    double temperature = fTemperatureUnit.fromKelvin(aContour.getContourValue());
    return round(temperature, 0) + "  " + fTemperatureUnit.toString();
  }

}
