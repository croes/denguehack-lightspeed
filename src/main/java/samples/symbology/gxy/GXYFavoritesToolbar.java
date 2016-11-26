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

import com.luciad.gui.ILcdUndoableListener;
import samples.symbology.common.gui.FavoritesToolbar;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.editing.GXYCreateAction;

/**
 * A toolbar for selecting often-used military symbols.
 * It shows buttons for every symbol in the {@link #getFavorites} collection.
 * Clicking on a symbol creates and inserts it in the topmost compatible layer of the given view.
 *
 * @see SymbologyFavorites
 * @see SymbolCustomizerFactory
 */
public class GXYFavoritesToolbar extends FavoritesToolbar implements ILcdLayeredListener {

  private final ILcdGXYView fView;
  private ILcdGXYLayerSubsetList fSnappables;

  public GXYFavoritesToolbar(ILcdGXYView aView, ILcdUndoableListener aUndoManager, ILcdGXYLayerSubsetList aSnappables, SymbologyFavorites aFavorites) {
    this(aView, aUndoManager, aSnappables, aFavorites, true);
  }

  public GXYFavoritesToolbar(ILcdGXYView aView, ILcdUndoableListener aUndoManager, ILcdGXYLayerSubsetList aSnappables, SymbologyFavorites aFavorites, boolean aLayeredListener) {
    super(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(aView), aFavorites, aUndoManager);
    fView = aView;
    fSnappables = aSnappables;
    if (aLayeredListener) {
      fView.addLayeredListener(this);
    }
  }

  @Override
  protected void symbolSelected(final String aSIDC) {
    new GXYCreateAction(
        GXYMilitarySymbolNewControllerModel.newInstanceForSymbol(aSIDC),
        fView,
        getUndoManager(),
        fSnappables
    ).actionPerformed(null);
  }

  protected ILcdGXYView getView() {
    return fView;
  }

  public ILcdGXYLayerSubsetList getSnappables() {
    return fSnappables;
  }

  @Override
  public void layeredStateChanged(TLcdLayeredEvent e) {
    setSymbology(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(fView));
  }

}
