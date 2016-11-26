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
package samples.symbology.gxy;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.view.gxy.ALcdAPP6AGXYNewControllerModel;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.view.gxy.ALcdMS2525bGXYNewControllerModel;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;

/**
 * A controller model for creating new military symbols on an ILcdGXYView,
 * based on the topmost compatible layer.
 * Implement the {@link #createSymbol} method to determine which symbol to create.
 */
public abstract class GXYMilitarySymbolNewControllerModel extends ALcdGXYNewControllerModel2 {

  /**
   * Factory method for a controller model that creates a symbol for the given SIDC.
   * @param aSIDC the symbol code to apply to the new symbol
   */
  public static GXYMilitarySymbolNewControllerModel newInstanceForSymbol(final String aSIDC) {
    return new GXYMilitarySymbolNewControllerModel() {
      @Override
      protected Object createSymbol(ILcdGXYLayer aGXYLayer) {
        Object symbol = MilitarySymbolFacade.newSymbol(aGXYLayer, true);
        MilitarySymbolFacade.setSIDC(symbol, aSIDC);
        return symbol;
      }
    };
  }

  /**
   * Factory method for a controller model that creates a symbol for the given hierarchy mask.
   * @param aHierarchyMask the hierarchy code to apply to the new symbol
   */
  public static GXYMilitarySymbolNewControllerModel newInstanceForHierarchy(final String aHierarchyMask) {
    return new GXYMilitarySymbolNewControllerModel() {
      @Override
      protected Object createSymbol(ILcdGXYLayer aGXYLayer) {
        Object symbol = MilitarySymbolFacade.newSymbol(aGXYLayer, true);
        MilitarySymbolFacade.changeHierarchy(symbol, aHierarchyMask);
        return symbol;
      }
    };
  }

  private final ALcdGXYNewControllerModel2 fAPP6Model = new ALcdAPP6AGXYNewControllerModel() {
    @Override
    public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
      return createSymbol(aContext.getGXYLayer());
    }
  };
  private final ALcdGXYNewControllerModel2 fMS2525Model = new ALcdMS2525bGXYNewControllerModel() {
    @Override
    public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
      return createSymbol(aContext.getGXYLayer());
    }
  };

  protected abstract Object createSymbol(ILcdGXYLayer aGXYLayer);

  @Override
  public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    ALcdGXYNewControllerModel2 model = retrieveDelegateControllerModel(aContext);
    return model == null ? null : model.getGXYLayer(aGraphics, aMouseEvent, aSnappables, aContext);
  }

  @Override
  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    ALcdGXYNewControllerModel2 model = retrieveDelegateControllerModel(aContext);
    return model == null ? null : model.create(aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
  }

  private ALcdGXYNewControllerModel2 retrieveDelegateControllerModel(ILcdGXYContext aGXYContext) {
    EMilitarySymbology symbology = MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(aGXYContext.getGXYView());
    if (symbology.getStandard() instanceof ELcdAPP6Standard) {
      return fAPP6Model;
    } else if (symbology.getStandard() instanceof ELcdMS2525Standard) {
      return fMS2525Model;
    }
    return null;
  }

}
