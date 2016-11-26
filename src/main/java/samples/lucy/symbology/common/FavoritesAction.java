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

import javax.swing.JToolBar;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.FavoritesToolbar;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.TLcyAlwaysFitJToolBar;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * <p>Action that provides a set of buttons that shows favorite symbols.
 * Pressing such a button adds a new symbol with the same properties to the map.</p>
 *
 * <p>This action is shown in the UI as a list of buttons, one for each favorite symbol.</p>
 * @see SymbologyFavorites
 */
public abstract class FavoritesAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  private static final String ICON_SIZE = "defaultIconSize";
  private final EMilitarySymbology fSymbology;
  private final SymbologyFavorites fFavorites;
  private final int fSymbolSize;

  public FavoritesAction(EMilitarySymbology aSymbology,
                         SymbologyFavorites aFavorites,
                         ALcyProperties aProperties,
                         String aPropertiesPrefix) {
    fSymbology = aSymbology;
    fFavorites = aFavorites;
    fSymbolSize = aProperties.getInt(aPropertiesPrefix + ICON_SIZE, 16);
  }

  /**
   * Create a panel containing a label followed by a tool bar. The tool bar will be filled dynamically with
   * actions showing favorite symbols.
   */
  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    FavoritesToolbar toolbar = new FavoritesToolbar(fSymbology, fFavorites, null) {
      @Override
      protected void symbolSelected(String aSIDC) {
        createSymbol(aSIDC);
      }

      @Override
      protected JToolBar createToolBar() {
        return new TLcyAlwaysFitJToolBar();
      }
    };
    String name = (String) aWrapperAction.getValue(ILcdAction.NAME);
    setName(name);
    toolbar.setSymbolSize(fSymbolSize);
    return toolbar.getToolBar();
  }

  protected abstract void createSymbol(String aSIDC);

  @Override
  public void actionPerformed(ActionEvent aE) {
    // nothing to do    
  }

}
