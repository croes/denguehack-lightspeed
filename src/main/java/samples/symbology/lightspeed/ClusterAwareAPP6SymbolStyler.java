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

import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ASymbolStyle;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ASymbolStyler;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;

import samples.common.UIColors;

/**
 * A styler that styles APP6 symbols and clusters of such symbols.
 * If all elements of the cluster are represented by the same symbol, the cluster also adopts this symbol (though a larger version thereof).
 * If not, the cluster is styled as a filled circle in the affiliation color.
 */
public class ClusterAwareAPP6SymbolStyler extends ClusterAwareMilitarySymbolStyler {

  public ClusterAwareAPP6SymbolStyler(TLspPaintState aPaintState) {
    this(TLcdDefaultAPP6AStyle.getNewInstance(), aPaintState);
  }

  public ClusterAwareAPP6SymbolStyler(TLcdDefaultAPP6AStyle aDefaultStyle, TLspPaintState aPaintState) {
    super(new TLspAPP6ASymbolStyler(aDefaultStyle), aPaintState);
  }

  @Override
  protected ALspStyle getAdaptedStyle(ALspStyle aStyle, double aClusterScaleFactor, Color aAffiliationColor) {
    if (aStyle instanceof TLspAPP6ASymbolStyle && ((TLspAPP6ASymbolStyle)aStyle).getAPP6AStyle() instanceof TLcdDefaultAPP6AStyle) {
      TLspAPP6ASymbolStyle style = (TLspAPP6ASymbolStyle) aStyle;
      TLcdDefaultAPP6AStyle app6AStyle = (TLcdDefaultAPP6AStyle) ((TLcdDefaultAPP6AStyle) style.getAPP6AStyle()).clone();
      app6AStyle.setSizeSymbol((int) (app6AStyle.getSizeSymbol() * aClusterScaleFactor));
      app6AStyle.setOffset(0, 0);
      if (isSelected()) {
        app6AStyle.setForcedColor(UIColors.alpha(aAffiliationColor, 111));
      }
      style = style.asBuilder().app6aStyle(app6AStyle).zOrder(5).build();
      return style;
    } else {
      return null;
    }
  }

  @Override
  protected List<ALspStyle> getElementStyling(List<ALspStyle> aOriginalSelectedElementStyling) {
    List<ALspStyle> result = new ArrayList<>();
    for (ALspStyle style : aOriginalSelectedElementStyling) {
      if (style instanceof TLspAPP6ASymbolStyle) {
        TLspAPP6ASymbolStyle app6ASymbolStyle = (TLspAPP6ASymbolStyle) style;
        ILcdAPP6AStyle app6AStyle = app6ASymbolStyle.getAPP6AStyle();
        if (app6AStyle instanceof TLcdDefaultAPP6AStyle) {
          TLcdDefaultAPP6AStyle defaultAPP6AStyle = (TLcdDefaultAPP6AStyle) app6AStyle;
          ILcdAPP6AStyle newAPP6AStyle = (ILcdAPP6AStyle) defaultAPP6AStyle.clone();
          newAPP6AStyle.setOffset(0, 0);
          result.add(app6ASymbolStyle.asBuilder().app6aStyle(newAPP6AStyle).build());
        } else {
          result.add(app6ASymbolStyle);
        }
      } else {
        result.add(style);
      }
    }
    return result;
  }

}
