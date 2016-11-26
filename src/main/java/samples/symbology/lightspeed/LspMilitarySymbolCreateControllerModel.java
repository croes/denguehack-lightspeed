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

import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * A controller model for creating new military symbols on an ILcdGXYView,
 * based on the topmost compatible layer.
 * Implement the {@link #createSymbol} method to determine which symbol to create.
 */
public abstract class LspMilitarySymbolCreateControllerModel extends ALspCreateControllerModel {

  /**
   * Factory method for a controller model that creates a symbol for the given SIDC.
   * @param aSIDC the symbol code to apply to the new symbol
   */
  public static LspMilitarySymbolCreateControllerModel newInstanceForSymbol(final String aSIDC) {
    return new LspMilitarySymbolCreateControllerModel() {
      @Override
      protected Object createSymbol(ILspLayer aLayer) {
        Object symbol = MilitarySymbolFacade.newSymbol(aLayer, true);
        MilitarySymbolFacade.setSIDC(symbol, aSIDC);
        return symbol;
      }
    };
  }

  /**
   * Factory method for a controller model that creates a symbol for the given hierarchy mask.
   * @param aHierarchyMask the hierarchy code to apply to the new symbol
   */
  public static LspMilitarySymbolCreateControllerModel newInstanceForHierarchy(final String aHierarchyMask) {
    return new LspMilitarySymbolCreateControllerModel() {
      @Override
      protected Object createSymbol(ILspLayer aLayer) {
        Object symbol = MilitarySymbolFacade.newSymbol(aLayer, true);
        MilitarySymbolFacade.changeHierarchy(symbol, aHierarchyMask);
        return symbol;
      }
    };
  }

  @Override
  public ILspInteractivePaintableLayer getLayer(ILspView aView) {
    return (ILspInteractivePaintableLayer) MilitarySymbolFacade.retrieveCompatibleEditableLayer(aView);
  }

  @Override
  public TLspPaintRepresentation getPaintRepresentation(ILspInteractivePaintableLayer aLayer, ILspView aView) {
    return TLspPaintRepresentation.BODY;
  }

  @Override
  public Object create(ILspView aView, ILspLayer aLayer) {
    // Creates a new, uninitialized MS2525 or APP6 symbol of the desired type
    return createSymbol(aLayer);
  }

  protected abstract Object createSymbol(ILspLayer aLayer);

}
