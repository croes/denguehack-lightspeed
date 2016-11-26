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
package samples.symbology.nvg.gui;

import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Text;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.util.SymbolCustomizerPanelSelectionListener;
import com.luciad.util.ILcdSelection;
import com.luciad.util.TLcdPair;
import com.luciad.util.TLcdSelectionChangedEvent;

/**
 * Whenever the user selects a symbol on the map, this listener sets the symbol on the given
 * customizer so that the user can view and edit its properties.
 *
 */
public class NVGSymbolCustomizerPanelSelectionListener extends SymbolCustomizerPanelSelectionListener {

  public NVGSymbolCustomizerPanelSelectionListener(AbstractSymbolCustomizer aStylePanel) {
    super(aStylePanel);
  }

  @Override
  protected TLcdPair<EMilitarySymbology, Object> getSymbologyAndSymbol(TLcdSelectionChangedEvent aEvent) {
    ILcdSelection selection = aEvent.getSelection();
    EMilitarySymbology symbology = null;

    if (selection.getSelectionCount() == 1) {
      Object object = selection.selectedObjects().nextElement();
      if (object instanceof TLcdNVG20SymbolizedContent) {
        TLcdNVGSymbol nvgSymbol = TLcdNVGSymbol.getSymbol(object);
        if (TLcdNVGSymbol.isAPP6ASymbol(nvgSymbol)) {
          symbology = EMilitarySymbology.fromStandard(TLcdNVGSymbol.getAPP6Standard(nvgSymbol.getStandardName()));
        } else if (TLcdNVGSymbol.isMS2525bSymbol(nvgSymbol)) {
          symbology = EMilitarySymbology.fromStandard(TLcdNVGSymbol.getMS2525Standard(nvgSymbol.getStandardName()));
        }
        if (symbology == null) {
          return new TLcdPair<>(null, object);
        } else {
          return new TLcdPair<>(symbology, object);
        }
      } else if (object instanceof TLcdNVG20Text) {
        return new TLcdPair<>(null, object);
      } else if (object instanceof TLcdNVG20Content) {
        return new TLcdPair<>(null, object);
      }
    }
    return null;

  }

}
