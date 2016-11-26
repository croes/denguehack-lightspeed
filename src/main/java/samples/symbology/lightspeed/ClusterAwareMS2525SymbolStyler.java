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
package samples.symbology.lightspeed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyle;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyler;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;

import samples.common.UIColors;

/**
 * A styler that styles MS2525 symbols and clusters of such symbols.
 * If all elements of the cluster are represented by the same symbol, the cluster also adopts this symbol (though a larger version thereof).
 * If not, the cluster is styled as a filled circle in the affiliation color.
 */
public class ClusterAwareMS2525SymbolStyler extends ClusterAwareMilitarySymbolStyler {

  public ClusterAwareMS2525SymbolStyler(TLspPaintState aPaintState) {
    this(TLcdDefaultMS2525bStyle.getNewInstance(), aPaintState);
  }

  public ClusterAwareMS2525SymbolStyler(TLcdDefaultMS2525bStyle aDefaultStyle, TLspPaintState aPaintState) {
    super(new TLspMS2525bSymbolStyler(aDefaultStyle), aPaintState);
  }

  @Override
  protected ALspStyle getAdaptedStyle(ALspStyle aStyle, double aClusterScaleFactor, Color aAffiliationColor) {
    if (aStyle instanceof TLspMS2525bSymbolStyle && ((TLspMS2525bSymbolStyle)aStyle).getMS2525bStyle() instanceof TLcdDefaultMS2525bStyle) {
      TLspMS2525bSymbolStyle style = (TLspMS2525bSymbolStyle) aStyle;
      TLcdDefaultMS2525bStyle ms2525bStyle = (TLcdDefaultMS2525bStyle) ((TLcdDefaultMS2525bStyle) style.getMS2525bStyle()).clone();
      ms2525bStyle.setSizeSymbol((int) (ms2525bStyle.getSizeSymbol() * aClusterScaleFactor));
      ms2525bStyle.setOffset(0, 0);
      if (isSelected()) {
        ms2525bStyle.setForcedColor(UIColors.alpha(aAffiliationColor, 111));
      }
      style = style.asBuilder().ms2525bStyle(ms2525bStyle).zOrder(5).build();
      return style;
    } else {
      return null;
    }
  }

  @Override
  protected List<ALspStyle> getElementStyling(List<ALspStyle> aOriginalSelectedElementStyling) {
    List<ALspStyle> result = new ArrayList<>();
    for (ALspStyle style : aOriginalSelectedElementStyling) {
      if (style instanceof TLspMS2525bSymbolStyle) {
        TLspMS2525bSymbolStyle ms2525bSymbolStyle = (TLspMS2525bSymbolStyle) style;
        ILcdMS2525bStyle ms2525bStyle = ms2525bSymbolStyle.getMS2525bStyle();
        if (ms2525bStyle instanceof TLcdDefaultMS2525bStyle) {
          TLcdDefaultMS2525bStyle defaultMS2525bStyle = (TLcdDefaultMS2525bStyle) ms2525bStyle;
          ILcdMS2525bStyle newMS2525Style = (ILcdMS2525bStyle) defaultMS2525bStyle.clone();
          newMS2525Style.setOffset(0, 0);
          result.add(ms2525bSymbolStyle.asBuilder().ms2525bStyle(newMS2525Style).build());
        } else {
          result.add(ms2525bSymbolStyle);
        }
      } else {
        result.add(style);
      }
    }
    return result;
  }

}
