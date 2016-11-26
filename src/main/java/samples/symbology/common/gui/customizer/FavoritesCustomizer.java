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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;

/**
 * Provides a "star" button to toggle the selected symbol as a favorite.
 * The favorites are stored in a SymbologyFavorites instance.
 * This could be linked to, for example, populate a toolbar with buttons to rapidly create these favorites.
 */
class FavoritesCustomizer extends AbstractSymbolCustomizer implements ActionListener {

  private static final String STARRED = "images/star_filled.png";
  private static final String UNSTARRED = "images/star.png";

  private final SymbologyFavorites fFavorites;
  private final JButton fButton;
  private final TLcdSWIcon fUnstarred = new TLcdSWIcon(new TLcdGreyIcon(new TLcdImageIcon(UNSTARRED)));
  private final CollectionListener fCollectionListener;

  private boolean fExistsInFavorites;

  /**
   * @param aFavorites the instance managing the list of favorite symbols
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public FavoritesCustomizer(SymbologyFavorites aFavorites, boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fFavorites = aFavorites;
    fButton = new JButton(fUnstarred);
    fButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    fButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    fButton.setContentAreaFilled(false);
    fButton.addActionListener(this);
    fButton.setToolTipText(translate("Click to toggle as favorite"));
    if (fFavorites == null) {
      fButton.setVisible(false);
    }
    fCollectionListener = new CollectionListener(this);
  }

  @Override
  public void setSymbol(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    EMilitarySymbology previous = getSymbology();
    if (previous != null && fFavorites != null) {
      fFavorites.get(previous).removeCollectionListener(fCollectionListener);
    }
    super.setSymbol(aMilitarySymbology, aModel, aSymbol);
    if (aMilitarySymbology != null && fFavorites != null) {
      fFavorites.get(aMilitarySymbology).addCollectionListener(fCollectionListener);
    }
  }

  @Override
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    updateButton(aSymbol, aMilitarySymbology);
  }

  private void updateButton(Object aSymbol, EMilitarySymbology aMilitarySymbology) {
    if (aSymbol == null || fFavorites == null) {
      fButton.setVisible(false);
    } else {
      fButton.setVisible(true);
      // used to ensure button behaviour is consistent with text (and not necessarily with actual favorites)
      fExistsInFavorites = fFavorites.get(aMilitarySymbology).contains(MilitarySymbolFacade.getSIDC(aSymbol));
      if (fExistsInFavorites) {
        fButton.setIcon(new TLcdSWIcon(new TLcdImageIcon(STARRED)));
      } else {
        fButton.setIcon(fUnstarred);
      }
    }
  }

  @Override
  public JComponent getComponent() {
    return fButton;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fButton.setEnabled(aEnabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EMilitarySymbology symbology = getSymbology();
    Object symbol = getSymbol();
    if (fExistsInFavorites) {
      fFavorites.get(symbology).remove(MilitarySymbolFacade.getSIDC(symbol));
    } else {
      fFavorites.get(symbology).add(MilitarySymbolFacade.getSIDC(symbol));
    }
    updateButton(symbol, symbology);
  }

  private static class CollectionListener implements ILcdCollectionListener<String> {

    private final WeakReference<FavoritesCustomizer> fCustomizer;

    private CollectionListener(FavoritesCustomizer aCustomizer) {
      // the favorites collection is a shared object that should not prevent garbage collection of this widget
      fCustomizer = new WeakReference<>(aCustomizer);
    }

    @Override
    public void collectionChanged(TLcdCollectionEvent<String> aCollectionEvent) {
      FavoritesCustomizer customizer = fCustomizer.get();
      if (customizer == null) {
        aCollectionEvent.getSource().removeCollectionListener(this);
      } else {
        switch (aCollectionEvent.getType()) {
        case ELEMENT_ADDED:
          customizer.updateButton(customizer.getSymbol(), customizer.getSymbology());
          break;
        case ELEMENT_REMOVED:
          customizer.updateButton(customizer.getSymbol(), customizer.getSymbology());
          break;
        }
      }
    }
  }

}
