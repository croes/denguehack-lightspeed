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
package samples.lightspeed.demo.application.data.streets;

import java.util.Collection;
import java.util.List;

import com.luciad.util.ILcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

/**
 * Styler that specifies styling information for other objects that
 * are being visualized based on level of detail.
 */
public class StaticDetailLevelStyler extends DetailLevelStyler {

  private final ILcdInterval[] fIntervals;
  private final List<List<ALspStyle>> fStyles;

  public StaticDetailLevelStyler(ILcdInterval[] aIntervals, List<List<ALspStyle>> aStyles) {
    fIntervals = aIntervals;
    fStyles = aStyles;
  }

  public StaticDetailLevelStyler(ILcdInterval[] aIntervals) {
    fIntervals = aIntervals;
    fStyles = null;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation());

    // If the objects are visible and have styles configured for them, then submit those styles.
    if (levelOfDetail >= 0 && fStyles != null) {
      final List<ALspStyle> styles = fStyles.get(levelOfDetail);

      if (styles != null) {
        aStyleCollector.objects(aObjects).styles(styles).submit();
      }
    } else {
      aStyleCollector.objects(aObjects).hide().submit();
    }
  }

  @Override
  protected ILcdInterval[] getDetailLevels() {
    return fIntervals;
  }
}
