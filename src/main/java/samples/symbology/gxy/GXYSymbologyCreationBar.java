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
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolSelectionBar;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.TopmostSymbologyLayeredListener;
import com.luciad.view.ILcdLayered;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.editing.GXYCreateAction;


/**
 * Lets the user create and insert a military symbol by typing in a search field or
 * by clicking on a button that opens a symbol hierarchy tree.
 */
public class GXYSymbologyCreationBar extends SymbolSelectionBar {

  private final ILcdGXYView fView;
  private final ILcdUndoableListener fUndoManager;
  private ILcdGXYLayerSubsetList fSnappables;

  public GXYSymbologyCreationBar(final ILcdGXYView aView, ILcdUndoableListener aUndoManager, ILcdGXYLayerSubsetList aSnappables) {
    this(aView, aUndoManager, aSnappables, true);
  }

  public GXYSymbologyCreationBar(final ILcdGXYView aView, ILcdUndoableListener aUndoManager, ILcdGXYLayerSubsetList aSnappables, boolean aLayeredListener) {
    super(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(aView), null);
    fView = aView;
    fUndoManager = aUndoManager;
    fSnappables = aSnappables;
    if (aLayeredListener) {
      aView.addLayeredListener(new LayeredListener(fView, this));
    }
    setSearchHint("Type name to create symbol");
    setSelectButtonText("Create");
    setSelectButtonText("Create");
  }


  /**
   * Called when the user selects a symbol from the symbol hierarchy.
   * @param aSIDC the hierarchy code of the symbol
   */
  @Override
  protected void symbolSelected(final String aSIDC) {
    new GXYCreateAction(
        GXYMilitarySymbolNewControllerModel.newInstanceForHierarchy(aSIDC),
        fView,
        fUndoManager,
        fSnappables
    ).actionPerformed(null);
  }

  protected ILcdGXYView getView() {
    return fView;
  }

  protected ILcdUndoableListener getUndoManager() {
    return fUndoManager;
  }

  public ILcdGXYLayerSubsetList getSnappables() {
    return fSnappables;
  }

  private static class LayeredListener extends TopmostSymbologyLayeredListener<GXYSymbologyCreationBar> {

    public LayeredListener(ILcdLayered aView, GXYSymbologyCreationBar aField) {
      super(aView, aField);
    }

    @Override
    protected void setSymbology(GXYSymbologyCreationBar aField, EMilitarySymbology aSymbology) {
      aField.setSymbology(aSymbology, null);
    }
  }

}
