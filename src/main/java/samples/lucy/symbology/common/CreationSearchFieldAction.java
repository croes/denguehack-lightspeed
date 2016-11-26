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
package samples.lucy.symbology.common;

import java.awt.Component;
import java.awt.event.ActionEvent;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolSelectionBar;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Action that creates a new symbology object, either by typing in a search field, or
 * by browsing through the hierarchy.
 */
public abstract class CreationSearchFieldAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  private final EMilitarySymbology fSymbology;

  public CreationSearchFieldAction(EMilitarySymbology aSymbology) {
    fSymbology = aSymbology;
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {

    // the search field allows you to type a code or description of the symbol you want to create
    SymbolSelectionBar creationBar = new SymbolSelectionBar(fSymbology, TLcyLang.getStringTranslator()) {
      @Override
      protected void symbolSelected(final String aSIDC) {
        createSymbol(aSIDC);
      }
    };
    creationBar.setSearchHint(TLcyLang.getString("Type name to create symbol"));
    creationBar.setSelectButtonText(TLcyLang.getString("Create"));
    return creationBar;
  }

  protected abstract void createSymbol(final String aSIDC);

  @Override
  public void actionPerformed(ActionEvent e) {
    // nothing to do
  }
}
