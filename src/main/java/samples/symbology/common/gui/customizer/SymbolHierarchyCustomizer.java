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
package samples.symbology.common.gui.customizer;

import java.util.EnumSet;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import samples.common.SwingUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolSelectionBar;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a widget to display and change a military symbol's hierarchy type.
 * Also allows marking or un-marking the customized object as a favorite.
 */
class SymbolHierarchyCustomizer extends AbstractSymbolCustomizer {

  private final SymbolSelectionBar fBar;
  private final JToolBar fContent;
  private final FavoritesCustomizer fFavoritesCustomizer;

  /**
   * @param aFavorites       the instance responsible for
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aHierarchyFilter an optional filter to reduce the amount of symbols when browsing through the symbol hierarchy. See {@link SymbolSelectionBar}
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public SymbolHierarchyCustomizer(SymbologyFavorites aFavorites, boolean aFireModelChange, ILcdFilter<String> aHierarchyFilter, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fContent = new JToolBar(); // gives the button a nicer, flat look
    SwingUtil.makeFlat(fContent);
    fBar = new SymbolSelectionBar(EnumSet.of(SymbolSelectionBar.BarComponent.BROWSE), getSymbology(), aHierarchyFilter, aStringTranslator) {
      @Override
      protected void symbolSelected(final String aSIDC) {
        final Object symbol = getSymbol();
        // only apply valid changes
        if (aSIDC == null || symbol == null) {
          return;
        }
        final String oldValue = MilitarySymbolFacade.getSIDC(symbol);
        if ((!aSIDC.equals(oldValue))) {
          // the apply change method takes care of model locking, undoables,
          // and the firing of model and change events.
          applyChange(new Runnable() {
                        @Override
                        public void run() {
                          MilitarySymbolFacade.changeHierarchy(symbol, aSIDC);
                        }
                      },
                      new Runnable() {
                        @Override
                        public void run() {
                          MilitarySymbolFacade.changeHierarchy(symbol, oldValue);
                        }
                      }
          );
        }
      }
    };

    fFavoritesCustomizer = new FavoritesCustomizer(aFavorites, aFireModelChange, aStringTranslator);
    fContent.add(fBar);
    fContent.add(fFavoritesCustomizer.getComponent());
    setSymbol(null, null, null);
  }

  public FavoritesCustomizer getFavoritesCustomizer() {
    return fFavoritesCustomizer;
  }

  @Override
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (aSymbol != null) {
      fBar.setEnabled(true);
      fBar.setSymbology(aMilitarySymbology, MilitarySymbolFacade.getSIDC(aSymbol));
      String name = MilitarySymbolFacade.getDisplayName(aSymbol);
      fBar.getBrowseButton().setVisible(true);
      fBar.getBrowseButton().setText(name);
      fBar.getBrowseButton().setToolTipText("<html>" + name + "<br/>" + translate("Click to view or change the hierarchy.") + "</html>");
    } else {
      fBar.setEnabled(false);
      fBar.getBrowseButton().setVisible(false);
    }
    fFavoritesCustomizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
  }

  @Override
  public JComponent getComponent() {
    return fContent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fBar.setEnabled(aEnabled);
    fFavoritesCustomizer.setEnabled(aEnabled);
  }
}
