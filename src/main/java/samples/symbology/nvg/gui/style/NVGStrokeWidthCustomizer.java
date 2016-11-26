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

import com.luciad.format.nvg.model.TLcdNVGStyle;
import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20MultiPoint;
import com.luciad.util.ILcdStringTranslator;

public class NVGStrokeWidthCustomizer extends AbstractNVGStyleCustomizer {

  public NVGStrokeWidthCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
  }

  @Override
  protected void setValue(Object aSymbol, String aValue) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null) {
      style = new TLcdNVGStyle();
    }
    try {
      style.setStrokeWidth(Float.parseFloat(aValue));
    } catch (NumberFormatException nfe) {
      style.setStrokeWidth(TLcdNVGStyle.DEFAULT_STROKE_WIDTH);
    }
    content.setStyle(style);
  }

  @Override
  protected String getValue(Object aSymbol) {
    TLcdNVG20Content content = (TLcdNVG20Content) aSymbol;
    TLcdNVGStyle style = content.getStyle();
    if (style == null || style.getStrokeWidth() == null) {
      return "" + TLcdNVGStyle.DEFAULT_STROKE_WIDTH;
    }
    return "" + style.getStrokeWidth();
  }

  @Override
  protected boolean hasValue(Object aSymbol) {
    if (aSymbol instanceof TLcdNVG20MultiPoint) {
      TLcdNVGSymbol symbol = ((TLcdNVG20MultiPoint) aSymbol).getSymbol();
      return TLcdNVGSymbol.isAPP6ASymbol(symbol) || TLcdNVGSymbol.isMS2525bSymbol(symbol);
    }
    return NVGStyleCustomizer.isStyledSymbolizedContent(aSymbol);
  }
}
