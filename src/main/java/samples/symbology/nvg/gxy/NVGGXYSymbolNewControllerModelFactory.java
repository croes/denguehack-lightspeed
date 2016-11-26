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
package samples.symbology.nvg.gxy;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;

import samples.symbology.nvg.common.INVGControllerModel;
import samples.symbology.nvg.common.NVGUtilities;

/**
 * Controller model factory for NVG symbols on GXY view
 */
public class NVGGXYSymbolNewControllerModelFactory {

  private NVGGXYSymbolNewControllerModelFactory() {
    throw new AssertionError("No instances allowed for NVGGXYSymbolNewControllerModelFactory");
  }

  /**
   * Create a controller for NVG Military Symbols
   * @param aContent NVG Geometry object which will be cloned for symbol creation
   * @param aHierarchyMask hierarchymask of the symbol
   * @param aILcdGXYLayer gxy layer
   * @param aSymbology symbology for the creator
   * @return a controller for NVG Military Symbols
   */
  public static ALcdGXYNewControllerModel2 newInstanceForHierarchy(final ILcdGXYLayer aILcdGXYLayer, final TLcdNVG20SymbolizedContent aContent, final String aHierarchyMask, final EMilitarySymbology aSymbology) {
    return new ANVGGXYControllerModel() {

      @Override
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return aILcdGXYLayer;
      }

      @Override
      public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        Object symbol = MilitarySymbolFacade.newElement(aSymbology, true);
        setSIDC(MilitarySymbolFacade.getSIDCMask(aSymbology, aHierarchyMask));
        setContent(aContent);
        setSymbology(aSymbology);
        MilitarySymbolFacade.changeHierarchy(symbol, aHierarchyMask);
        return NVGUtilities.createNVG20Content((TLcdNVG20SymbolizedContent) aContent.clone(), aSymbology, symbol);
      }
    };
  }

  /**
   * Create a controller for NVG Military Symbols
   * @param aContent NVG Geometry object which will be cloned for symbol creation
   * @param aSIDC the symbol code to apply to the new symbol
   * @param aILcdGXYLayer gxy layer
   * @param aSymbology symbology for the creator
   * @return a controller for NVG Military Symbols
   */
  public static ALcdGXYNewControllerModel2 newInstanceForSymbol(final ILcdGXYLayer aILcdGXYLayer, final TLcdNVG20SymbolizedContent aContent, final String aSIDC, final EMilitarySymbology aSymbology) {
    return new ANVGGXYControllerModel() {

      @Override
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return aILcdGXYLayer;
      }

      @Override
      public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        Object symbol = MilitarySymbolFacade.newElement(aSymbology, true);
        setSIDC(MilitarySymbolFacade.getSIDCMask(aSymbology, aSIDC));
        setContent(aContent);
        setSymbology(aSymbology);
        MilitarySymbolFacade.setSIDC(symbol, aSIDC);
        return NVGUtilities.createNVG20Content((TLcdNVG20SymbolizedContent) aContent.clone(), aSymbology, symbol);
      }
    };
  }

  /**
   * Create a controller for NVG custom drawn geometry
   * @param aILcdGXYLayer gxy layer
   * @param aContent Geometry of the NVG object
   * @return a controller for NVG custom drawn geometry
   */
  public static ALcdGXYNewControllerModel2 newInstanceForNVGContent(final ILcdGXYLayer aILcdGXYLayer, final TLcdNVG20Content aContent) {

    return new ANVGGXYControllerModel() {

      @Override
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return aILcdGXYLayer;
      }

      @Override
      public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        setSIDC(null);
        setContent(aContent);
        setSymbology(null);
        return aContent.clone();
      }
    };

  }

  /**
   * Abstract for NVGGXY Controllers
   */
  private static abstract class ANVGGXYControllerModel extends ALcdGXYNewControllerModel2 implements
                                                                                          INVGControllerModel {
    private String fSIDC;
    private EMilitarySymbology fSymbology;
    private TLcdNVG20Content fContent;

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

    public void setSIDC(String aSIDCMask) {
      fSIDC = aSIDCMask;
    }

    public void setSymbology(EMilitarySymbology aSymbology) {
      fSymbology = aSymbology;
    }

    public void setContent(TLcdNVG20Content aContent) {
      fContent = aContent;
    }
  }

}
