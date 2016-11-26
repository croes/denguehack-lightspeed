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
package samples.hana.lightspeed.styling;

import static samples.hana.lightspeed.domain.AdministrativeLevel.getAdministrativeLevel;
import static samples.hana.lightspeed.domain.AdministrativeLevel.getAdministrativeLevelKey;
import static samples.hana.lightspeed.domain.WindSpeed.*;

import java.awt.Color;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.domain.AdministrativeLevel;
import samples.hana.lightspeed.statistics.Statistics;
import samples.hana.lightspeed.statistics.StatisticsProvider;
import samples.hana.lightspeed.statistics.StormStatistics;

/**
 * Styler that provides a fill color based on the affected customers in the data object (area).
 */
public class StatisticsBodyStyler extends ALspStyler implements ILcdChangeListener {

  private final StatisticsProvider fStatisticsProvider;

  public StatisticsBodyStyler(StatisticsProvider aStatisticsProvider) {
    fStatisticsProvider = aStatisticsProvider;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aTLspContext) {
    for (Object o : aObjects) {
      ILcdDataObject object = (ILcdDataObject) o;
      String key = getAdministrativeLevelKey(object);
      AdministrativeLevel level = getAdministrativeLevel(object);
      Statistics total = fStatisticsProvider.getTotal(level, key);
      StormStatistics affected = fStatisticsProvider.getAffected(level, key);
      Color color = ColorPalette.unknown.brighter();
      if (total != null && affected != null && total.getTotalCount() != 0) {
        double fraction34 = (double) affected.get(S34).getTotalCount() / (double) Math.max(1, total.getTotalCount());
        double fraction50 = (double) affected.get(S50).getTotalCount() / (double) Math.max(1, total.getTotalCount());
        double fraction64 = (double) affected.get(S64).getTotalCount() / (double) Math.max(1, total.getTotalCount());

        if (fraction64 > 0) {
          color = lerp(ColorPalette.b, ColorPalette.a, fraction64);
        } else if (fraction50 > 0) {
          color = lerp(ColorPalette.c, ColorPalette.b, fraction50);
        } else {
          color = lerp(ColorPalette.d, ColorPalette.c, fraction34);
        }
      }
      aStyleCollector.object(object).styles(TLspLineStyle.newBuilder().build(), TLspFillStyle.newBuilder().color(color).build()).submit();
    }
  }

  /**
   * Triggered when the {@link StatisticsProvider} has loaded the statistics based on the active storms.
   */
  @Override
  public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
    fireStyleChangeEvent();
  }

  public static Color lerp(Color aA, Color aB, double aF) {
    aF = Math.max(0f, Math.min(1f, aF));
    float[] a = aA.getRGBComponents(null);
    float[] b = aB.getRGBComponents(null);
    return new Color(
        a[0] + (b[0] - a[0]) * (float) aF,
        a[1] + (b[1] - a[1]) * (float) aF,
        a[2] + (b[2] - a[2]) * (float) aF,
        a[3] + (b[3] - a[3]) * (float) aF
    );
  }
}
