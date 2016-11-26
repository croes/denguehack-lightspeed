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
package samples.symbology.nvg.lightspeed;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.symbology.nvg.common.INVGControllerModel;
import samples.symbology.nvg.common.NVGUtilities;

/**
 * Controller model factory for NVG symbols on LSP view
 */
public abstract class NVGLspSymbolCreateControllerModelFactory {

  private NVGLspSymbolCreateControllerModelFactory() {
    throw new AssertionError("No instances allowed for NVGLspSymbolCreateControllerModelFactory");
  }

  /**
   * Factory method for a controller model that creates a symbol for the given SIDC.
   * @param aContent NVG Geometry which will be cloned for creation of symbol
   * @param aSIDC the symbol code to apply to the new symbol
   * @param aSymbology desired symbology of the symbol to be created
   */
  public static ALspCreateControllerModel newInstanceForSymbol(final TLcdNVG20Content aContent, ILspInteractivePaintableLayer aLayer, final String aSIDC, final EMilitarySymbology aSymbology) {
    return new ANVGLSPControllerModel(aLayer, aSIDC, aSymbology, aContent) {
      @Override
      protected Object createSymbol(ILspLayer aLayer) {
        Object symbol = MilitarySymbolFacade.newElement(getSymbology(), true);
        MilitarySymbolFacade.setSIDC(symbol, getSIDC());
        return NVGUtilities.createNVG20Content((TLcdNVG20SymbolizedContent) aContent.clone(), aSymbology, symbol);
      }
    };
  }

  /**
   * Factory method for a controller model that creates a symbol for the given hierarchy mask.
   * @param aContent NVG Geometry which will be cloned for creation of symbol
   * @param aHierarchyMask the hierarchy code to apply to the new symbol
   */
  public static ALspCreateControllerModel newInstanceForHierarchy(final TLcdNVG20Content aContent, ILspInteractivePaintableLayer aLayer, final String aHierarchyMask, final EMilitarySymbology aSymbology) {
    return new ANVGLSPControllerModel(aLayer, aHierarchyMask, aSymbology, aContent) {
      @Override
      protected Object createSymbol(ILspLayer aLayer) {
        Object symbol = MilitarySymbolFacade.newElement(getSymbology(), true);
        MilitarySymbolFacade.changeHierarchy(symbol, getSIDC());
        return NVGUtilities.createNVG20Content((TLcdNVG20SymbolizedContent) aContent.clone(), aSymbology, symbol);
      }
    };
  }

  /**
   * Factory method for a controller model that creates a NVG shape without a symbol
   * @param aContent NVG Geometry which will be cloned for creation
   */
  public static ALspCreateControllerModel newInstanceForNVGContent(final TLcdNVG20Content aContent, ILspInteractivePaintableLayer aLayer) {
    return new ANVGLSPControllerModel(aLayer, null, null, aContent) {
      @Override
      protected Object createSymbol(ILspLayer aLayer) {
        return aContent.clone();
      }
    };
  }

  /**
   * Abstract for NVGLSP Controllers
   */
  private static abstract class ANVGLSPControllerModel extends ALspCreateControllerModel implements
                                                                                         INVGControllerModel {
    private final ILspInteractivePaintableLayer fLayer;
    private final String fSIDC;
    private final EMilitarySymbology fSymbology;
    private final TLcdNVG20Content fContent;

    ANVGLSPControllerModel(ILspInteractivePaintableLayer aLayer, String aSIDC, EMilitarySymbology aSymbology, TLcdNVG20Content aContent) {
      fLayer = aLayer;
      fSIDC = aSIDC;
      fSymbology = aSymbology;
      fContent = aContent;
    }

    @Override
    public ILspInteractivePaintableLayer getLayer(ILspView aView) {
      return fLayer;
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

    @Override
    public String getSIDC() {
      return fSIDC;
    }

    @Override
    public EMilitarySymbology getSymbology() {
      return fSymbology;
    }

    @Override
    public TLcdNVG20Content getContent() {
      return fContent;
    }
  }

}
