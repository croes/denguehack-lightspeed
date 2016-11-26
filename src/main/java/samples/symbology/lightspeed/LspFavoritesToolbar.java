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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ILcdUndoableListener;
import samples.symbology.common.gui.FavoritesToolbar;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;

import samples.lightspeed.editing.LspCreateAction;

/**
 * A toolbar for selecting often-used military symbols.
 * It shows buttons for every symbol in the {@link #getFavorites} collection.
 * Clicking on a symbol creates and inserts it in the topmost compatible layer of the given view.
 *
 * @see SymbologyFavorites
 * @see SymbolCustomizerFactory
 */
public class LspFavoritesToolbar extends FavoritesToolbar implements ILcdLayeredListener {

  private final List<ILspView> fViews;

  public LspFavoritesToolbar(ILspView aView, ILcdUndoableListener aUndoManager, SymbologyFavorites aFavorites) {
    this(Collections.singletonList(aView), aUndoManager, aFavorites);
  }

  public LspFavoritesToolbar(List<ILspView> aViews, ILcdUndoableListener aUndoManager, SymbologyFavorites aFavorites) {
    super(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(aViews.get(0)), aFavorites, aUndoManager);
    fViews = new ArrayList<ILspView>(aViews);
    fViews.get(0).addLayeredListener(this);
  }

  @Override
  protected void symbolSelected(final String aSIDC) {
    new LspCreateAction(
        LspMilitarySymbolCreateControllerModel.newInstanceForSymbol(aSIDC),
        fViews,
        getUndoManager()
    ).actionPerformed(null);
  }

  protected List<ILspView> getViews() {
    return fViews;
  }

  @Override
  public void layeredStateChanged(TLcdLayeredEvent e) {
    setSymbology(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(fViews.get(0)));
  }

}
