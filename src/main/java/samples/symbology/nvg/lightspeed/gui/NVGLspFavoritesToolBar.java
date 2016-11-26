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
package samples.symbology.nvg.lightspeed.gui;

import java.util.Collections;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.gui.ILcdUndoableListener;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.AFavoritesToolBar;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.util.TLcdPair;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.editing.LspCreateAction;
import samples.symbology.nvg.common.NVGUtilities;
import samples.symbology.nvg.lightspeed.NVGLspSymbolCreateControllerModelFactory;

/**
 * A toolbar for selecting often-used military symbols.
 * It shows buttons for every symbol in the {@link #getFavorites} collection.
 * Clicking on a symbol creates and inserts it into the NVG layer of the given view.
 */
public class NVGLspFavoritesToolBar extends AFavoritesToolBar {

  private final ILspView fView;
  private final ILspInteractivePaintableLayer fLayer;

  public NVGLspFavoritesToolBar(ILspView aView, ILcdUndoableListener aUndoManager, ILspInteractivePaintableLayer aLayer, SymbologyFavorites aSymbologyFavorites) {
    super(aSymbologyFavorites, aUndoManager);
    fView = aView;
    fLayer = aLayer;
  }

  @Override
  protected void symbolSelected(TLcdPair<EMilitarySymbology, String> aSymbol) {
    TLcdNVG20SymbolizedContent defaultGeometry = NVGUtilities.getDefaultGeometry(aSymbol.getValue(), aSymbol.getKey());
    new LspCreateAction(
        NVGLspSymbolCreateControllerModelFactory.newInstanceForSymbol(defaultGeometry, fLayer, aSymbol.getValue(), aSymbol.getKey()),
        Collections.singletonList(fView),
        getUndoManager()
    ).actionPerformed(null);
  }

}
