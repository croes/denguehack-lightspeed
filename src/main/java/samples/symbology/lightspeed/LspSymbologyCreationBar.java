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
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolSelectionBar;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.TopmostSymbologyLayeredListener;
import com.luciad.view.ILcdLayered;
import com.luciad.view.lightspeed.ILspView;

import samples.lightspeed.editing.LspCreateAction;


/**
 * Lets the user create and insert a military symbol by typing in a search field
 * or by clicking on a button that opens a symbol hierarchy tree.
 */
public class LspSymbologyCreationBar extends SymbolSelectionBar {

  private final List<ILspView> fViews;
  private final ILcdUndoableListener fUndoManager;

  public LspSymbologyCreationBar(final ILspView aView, ILcdUndoableListener aUndoManager) {
    this(Collections.singletonList(aView), aUndoManager);
  }

  public LspSymbologyCreationBar(List<ILspView> aViews, ILcdUndoableListener aUndoManager) {
    super(MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(aViews.get(0)), null);
    fViews = new ArrayList<>(aViews);
    fUndoManager = aUndoManager;
    ILspView view = aViews.get(0);
    view.addLayeredListener(new LayeredListener(view, this));
    setSearchHint("Type name to create symbol");
    setSelectButtonText("Create");
  }


  /**
   * Called when the user selects a symbol from the symbol hierarchy.
   * @param aSIDC the hierarchy code of the symbol
   */
  @Override
  protected void symbolSelected(String aSIDC) {
    new LspCreateAction(
        LspMilitarySymbolCreateControllerModel.newInstanceForHierarchy(aSIDC),
        fViews,
        fUndoManager
    ).actionPerformed(null);
  }

  protected List<ILspView> getViews() {
    return fViews;
  }

  protected ILcdUndoableListener getUndoManager() {
    return fUndoManager;
  }

  private static class LayeredListener extends TopmostSymbologyLayeredListener<LspSymbologyCreationBar> {

    public LayeredListener(ILcdLayered aView, LspSymbologyCreationBar aField) {
      super(aView, aField);
    }

    @Override
    protected void setSymbology(LspSymbologyCreationBar aField, EMilitarySymbology aSymbology) {
      aField.setSymbology(aSymbology, null);
    }
  }

}
