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
package samples.lightspeed.internal.havelsan.tactical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.luciad.gui.ILcdIcon;
import com.luciad.symbology.milstd2525b.view.TLcdMS2525bObjectIconProvider;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * @author tomn
 * @since 2012.0
 */
@SuppressWarnings("deprecation")
public class TacticalObjectStyler extends ALspStyler {

  private Map<String, TLspIconStyle> fCodeToStyle = new HashMap<String, TLspIconStyle>();
  private TLcdMS2525bObjectIconProvider fIconProvider;
  private int fZOrder = 0;

  public TacticalObjectStyler() {
    fIconProvider = new TLcdMS2525bObjectIconProvider();
    fIconProvider.setIconSize(16);
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object o : aObjects) {
      TacticalObject tacticalObject = (TacticalObject) o;
      String code = tacticalObject.getMS2525Code();
      TLspIconStyle style = fCodeToStyle.get(code);
      if (style == null) {
        ILcdIcon icon = fIconProvider.getIcon(tacticalObject);
        style = TLspIconStyle.newBuilder()
                             .icon(icon)
                             .useOrientation(true)
                             .zOrder(fZOrder++)
                             .build();
        fCodeToStyle.put(code, style);
      }
      aStyleCollector.object(tacticalObject).style(style).submit();
    }
  }
}
