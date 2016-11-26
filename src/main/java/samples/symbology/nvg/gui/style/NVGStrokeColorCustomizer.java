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
package samples.symbology.nvg.gui.style;

import java.awt.Color;

import com.luciad.format.nvg.model.TLcdNVGStyle;
import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20MultiPoint;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.util.ILcdStringTranslator;

class NVGStrokeColorCustomizer extends AbstractNVGStyleColorCustomizer {

  private TLcdDefaultMS2525bStyle fMS2525bStyle = TLcdDefaultMS2525bStyle.getInstance();
  private TLcdDefaultAPP6AStyle fAPP6AStyle = TLcdDefaultAPP6AStyle.getInstance();

  public NVGStrokeColorCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
  }

  protected void setColor(Object aSymbol, Color aValue) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null) {
      style = new TLcdNVGStyle();
    }
    style.setStrokeColor(aValue);
    style.setStrokeOpacity(aValue.getAlpha() / 255f);
    content.setStyle(style);
  }

  protected Color getValue(Object aSymbol) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null) {
      style = new TLcdNVGStyle();
      content.setStyle(style);
    }

    float opacity = style.getStrokeOpacity() != null ? style.getStrokeOpacity() : TLcdNVGStyle.DEFAULT_STROKE_OPACITY;
    Color result = TLcdNVGStyle.DEFAULT_STROKE_COLOR;
    if (style.getStrokeColor() == null) {
      if (content instanceof TLcdNVG20SymbolizedContent) {
        TLcdNVG20SymbolizedContent symbolizedContent = (TLcdNVG20SymbolizedContent) content;
        TLcdNVGSymbol symbol = symbolizedContent.getSymbol();
        if (TLcdNVGSymbol.isAPP6ASymbol(symbol)) {
          String affiliationValue = symbolizedContent.getAPP6CodedFromMapObject().getAffiliationValue();
          Color affiliationColor = fAPP6AStyle.getAffiliationColor(TLcdNVGSymbol.getAPP6Standard(symbol.getStandardName()),
                                                                   affiliationValue);
          result = affiliationColor;
        } else if (TLcdNVGSymbol.isMS2525bSymbol(symbol)) {
          String affiliationValue = symbolizedContent.getMS2525CodedFromMapObject().getAffiliationValue();
          Color affiliationColor = fMS2525bStyle.getAffiliationColor(TLcdNVGSymbol.getMS2525Standard(symbol.getStandardName()),
                                                                     affiliationValue);
          result = affiliationColor;
        }
      }
    } else {
      result = style.getStrokeColor();
    }
    float[] rgb = result.getRGBColorComponents(null);
    return new Color(rgb[0], rgb[1], rgb[2], opacity);
  }

  protected boolean hasValue(Object aSymbol) {
    if (aSymbol instanceof TLcdNVG20MultiPoint) {
      TLcdNVGSymbol symbol = ((TLcdNVG20MultiPoint) aSymbol).getSymbol();
      return TLcdNVGSymbol.isAPP6ASymbol(symbol) || TLcdNVGSymbol.isMS2525bSymbol(symbol);
    }
    return NVGStyleCustomizer.isStyledSymbolizedContent(aSymbol);
  }
}
