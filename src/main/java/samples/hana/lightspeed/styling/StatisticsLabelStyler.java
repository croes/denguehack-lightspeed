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

import static samples.hana.lightspeed.domain.AdministrativeLevel.*;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.domain.AdministrativeLevel;
import samples.hana.lightspeed.domain.WindSpeed;
import samples.hana.lightspeed.statistics.Statistics;
import samples.hana.lightspeed.statistics.StatisticsProvider;
import samples.hana.lightspeed.statistics.StormStatistics;

/**
 * Styler that provides a label based on the affected customers in the data object.
 */
public class StatisticsLabelStyler extends ALspLabelStyler implements ILcdChangeListener {

  private final StatisticsProvider fStatisticsProvider;

  public StatisticsLabelStyler(StatisticsProvider aStatisticsProvider) {
    fStatisticsProvider = aStatisticsProvider;
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aTLspContext) {
    for (Object o : aObjects) {
      ILcdDataObject object = (ILcdDataObject) o;
      String name = getName(object);
      String key = getAdministrativeLevelKey(object);
      AdministrativeLevel level = getAdministrativeLevel(object);
      Statistics total = fStatisticsProvider.getTotal(level, key);
      StormStatistics affected = fStatisticsProvider.getAffected(level, key);

      if (total == null || total.getTotalCount() == 0) {
        continue;
      }

      String[] text = new String[]{name};
      long priority = 10000000;
      Color color = ColorPalette.d_darker;

      if (affected != null) {
        double percentage = ((100.0 * affected.get(WindSpeed.S34).getTotalCount()) / total.getTotalCount());
        if (percentage > 0) {
          text = new String[]{name, "" + Math.round(percentage) + " %"};
          priority = priority - affected.get(WindSpeed.S34).getTotalCount();
          color = ColorPalette.a_darker;
        }
      }
      aStyleCollector.object(object).styles(new Text(text), TLspTextStyle.newBuilder().alignment(TLspTextStyle.Alignment.CENTER).font("Dialog-BOLD-12").textColor(color).haloColor(ColorPalette.halo).build()).priority((int) priority).submit();
    }
  }

  @Override
  public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
    fireStyleChangeEvent();
  }

  public static class Text extends ALspLabelTextProviderStyle {
    private final String[] fText;

    public Text(String... aText) {
      fText = aText;
    }

    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      return fText;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      Text text = (Text) o;

      if (!Arrays.equals(fText, text.fText)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + Arrays.hashCode(fText);
      return result;
    }
  }
}
