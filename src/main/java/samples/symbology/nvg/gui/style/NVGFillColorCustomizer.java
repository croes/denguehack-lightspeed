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
import com.luciad.format.nvg.nvg20.model.TLcdNVG20AreaContent;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Arrow;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Corridor;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Orbit;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.util.ILcdStringTranslator;

class NVGFillColorCustomizer extends AbstractNVGStyleColorCustomizer {

  public NVGFillColorCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
  }

  protected void setColor(Object aSymbol, Color aValue) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null) {
      style = new TLcdNVGStyle();
    }
    style.setFillColor(aValue);
    style.setFillOpacity(aValue.getAlpha() / 255f);
    content.setStyle(style);
  }

  protected Color getValue(Object aSymbol) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null) {
      style = new TLcdNVGStyle();
      content.setStyle(style);
    }

    if (style.getFillColor() == null) {
      return new Color(0, 0, 0, 0);
    }

    float opacity = style.getFillOpacity() != null ? style.getFillOpacity() : TLcdNVGStyle.DEFAULT_FILL_OPACITY;
    Color fillColor = style.getFillColor();
    float[] rgb = fillColor.getRGBColorComponents(null);
    return new Color(rgb[0], rgb[1], rgb[2], opacity);
  }

  protected boolean hasValue(Object aSymbol) {
    if (aSymbol instanceof TLcdNVG20SymbolizedContent) {
      TLcdNVG20SymbolizedContent symbolizedContent = (TLcdNVG20SymbolizedContent) aSymbol;
      TLcdNVGSymbol symbol = symbolizedContent.getSymbol();
      if (TLcdNVGSymbol.isAPP6ASymbol(symbol) || TLcdNVGSymbol.isMS2525bSymbol(symbol)) {
        return false;
      }
    }
    return aSymbol instanceof TLcdNVG20AreaContent
           || aSymbol instanceof TLcdNVG20Corridor
           || aSymbol instanceof TLcdNVG20Orbit
           || aSymbol instanceof TLcdNVG20Arrow;
  }
}
