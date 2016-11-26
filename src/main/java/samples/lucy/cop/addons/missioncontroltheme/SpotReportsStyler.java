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
package samples.lucy.cop.addons.missioncontroltheme;

import java.util.Collection;

import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * {@code ILspStyler} for the spot reports
 */
final class SpotReportsStyler extends ALspStyler {

  private final MS2525IconProvider fIconProvider = new MS2525IconProvider();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object domainObject : aObjects) {
      if (domainObject instanceof GeoJsonRestModelElement) {
        GeoJsonRestModelElement geoJsonRestModelElement = (GeoJsonRestModelElement) domainObject;
        String ms2525code = (String) geoJsonRestModelElement.getValue(SpotReportsModel.CODE_PROPERTY);
        if (ms2525code == null || ms2525code.isEmpty()) {
          ms2525code = "GFCPMOLAM---***";
        }
        if (fIconProvider.canGetIcon(ms2525code)) {
          ILcdMS2525bCoded ms2525bCoded = fIconProvider.convertStringToMS2525Object(ms2525code);
          aStyleCollector
              .object(domainObject)
              .geometry(geoJsonRestModelElement.getShape(0))
              .style(TLspMS2525bSymbolStyle.newBuilder().ms2525bCoded(ms2525bCoded).build())
              .submit();
        }
      }
    }
  }
}
