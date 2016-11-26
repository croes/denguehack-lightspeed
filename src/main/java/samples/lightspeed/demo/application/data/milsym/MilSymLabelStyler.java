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
package samples.lightspeed.demo.application.data.milsym;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ASymbolStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

import samples.lightspeed.demo.framework.data.IOUtil;

public class MilSymLabelStyler extends ALspLabelStyler {

  private static final TLspIconStyle UNKNOWN_FLAG_STYLE = TLspIconStyle.newBuilder().build();
  private static final Object FLAG_SUBLABEL_ID = "FLAG_LABEL";

  private final ILcdAPP6AStyle fStandardLabelStyle;

  private Map<String, TLspIconStyle> fFlags = new HashMap<>();

  public MilSymLabelStyler(ILcdAPP6AStyle aStandardLabelStyle) {
    fStandardLabelStyle = aStandardLabelStyle;
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object o : aObjects) {
      if (o instanceof TLcdEditableAPP6AObject) {
        styleStandardSymbol((TLcdEditableAPP6AObject) o, isInScaleRange(aContext), aStyleCollector);
      }
    }
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (aStyleCollector instanceof ALspLabelStyleCollector) {
      style(aObjects, (ALspLabelStyleCollector) aStyleCollector, aContext);
    } else {
      throw new IllegalArgumentException("Expected ALspLabelStyleCollector, not " + aStyleCollector);
    }
  }

  private boolean isInScaleRange(TLspContext aContext) {
    double scale = aContext.getView().getViewXYZWorldTransformation().getScale();
    return scale >= 5e-4;
  }

  private void styleStandardSymbol(TLcdEditableAPP6AObject aSymbol, boolean aFlagLabel, ALspLabelStyleCollector aStyleCollector) {
    ALspStyle style = createSymbologyStyle(aSymbol);
    aStyleCollector
        .object(aSymbol)
        .geometry(aSymbol)
        .style(style)
        .submit();

    if (aFlagLabel) {
      // Add the country flag when zoomed in
      String countryCode = getCountryCode(aSymbol);
      TLspIconStyle flagStyle = getCountryFlag(countryCode);
      if (flagStyle != UNKNOWN_FLAG_STYLE) {
        aStyleCollector
            .object(aSymbol)
            .geometry(aSymbol)
            .styles(flagStyle)
            .label(FLAG_SUBLABEL_ID)
            .locations(20, TLspLabelLocationProvider.Location.NORTH_EAST)
            .submit();
      }
    }
  }

  private ALspStyle createSymbologyStyle(TLcdEditableAPP6AObject aSymbol) {
    return TLspAPP6ASymbolStyle.newBuilder()
                               .app6aCoded(aSymbol)
                               .app6aStyle(fStandardLabelStyle)
                               .build();
  }

  private TLspIconStyle getCountryFlag(String aCountryCode) {
    String key = aCountryCode.toLowerCase();
    if (key.contains("*")) {
      return UNKNOWN_FLAG_STYLE;
    }

    TLspIconStyle flag = fFlags.get(key);
    if (flag == null) {
      String sourceName = "images/countryflags/" + key + ".png";
      try {
        BufferedImage image = IOUtil.readImage(sourceName);
        flag = TLspIconStyle.newBuilder()
                            .icon(new TLcdImageIcon(
                                image.getScaledInstance(image.getWidth() / 2, image.getHeight() / 2, Image.SCALE_SMOOTH)
                            ))
                            .build();
      } catch (IOException e) {
        flag = UNKNOWN_FLAG_STYLE;
      }
      fFlags.put(key, flag);
    }
    return flag;
  }

  private String getCountryCode(ILcdAPP6ACoded aGeometry) {
    String symbolCode = aGeometry.getAPP6ACode();
    return symbolCode.substring(12, 14);
  }

}
