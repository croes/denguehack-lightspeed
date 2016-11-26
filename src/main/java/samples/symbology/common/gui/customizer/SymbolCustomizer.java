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

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import samples.common.TitledCollapsiblePane;
import samples.common.TwoColumnPanel;
import samples.common.UIColors;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.SymbolSelectionBar;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.model.ILcdModel;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.TLcdChangeEvent;

public class SymbolCustomizer extends AbstractSymbolCustomizer {
  private static final Object KEY_COLLAPSIBLE = new Object();

  private static final List<String> fDefaultBasicModifierNames = Arrays.asList(ILcdAPP6ACoded.sAffiliation,
                                                                               ILcdAPP6ACoded.sStatus,
                                                                               ILcdAPP6ACoded.sCountry,
                                                                               ILcdAPP6ACoded.sOrderOfBattle,
                                                                               ILcdAPP6ACoded.sEchelon);
  private static final EnumMap<EMilitarySymbology, List<String>> fBasicModifierNames;

  static {
    fBasicModifierNames = new EnumMap<>(EMilitarySymbology.class);
    fBasicModifierNames.put(EMilitarySymbology.APP6C, Arrays.asList(
        ILcdAPP6ACoded.sSector1,
        ILcdAPP6ACoded.sSector2,
        ILcdAPP6ACoded.sStandardIdentity1,
        ILcdAPP6ACoded.sStandardIdentity2,
        ILcdAPP6ACoded.sStatus,
        ILcdAPP6ACoded.sCountry,
        ILcdAPP6ACoded.sEchelon,
        ILcdAPP6ACoded.sMobility,
        ILcdAPP6ACoded.sHqTaskForceDummy
    ));
  }

  private final boolean fFireModelChange;
  private final ILcdStringTranslator fStringTranslator;
  private final ILcdChangeListener fChangeListener;

  private final JPanel fComponent;

  private final TwoColumnPanel fBasicPanel;
  private final TwoColumnPanel fAdvancedPanel;
  private final JComponent fStylePanel;
  private final AbstractSymbolCustomizer fStyleCustomizer;
  private final SymbolHierarchyCustomizer fSymbolHierarchyCustomizer;

  private final JLabel fSelectionHint;

  private MilitarySymbolFacade.Modifier fFocusedModifier;
  private final Collection<AbstractSymbolCustomizer> fActiveCustomizers = new ArrayList<>();

  /**
   * @param aParts           describes which sub-panels should be shown
   * @param aFireModelChange true if model changes should be fired. Typically this is only false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aFavorites       the instance responsible for managing the favorite symbols
   * @param aDisplayPropertiesTitle if true, a title will be displayed for in the regular symbol properties sub-panel.
   *                                Use false if the panel is already contained in, for example, a titled dialog or panel.
   * @param aHierarchyFilter an optional filter to reduce the amount of symbols when browsing through the symbol hierarchy.
   *                         See {@link SymbolSelectionBar}
   */
  public SymbolCustomizer(EnumSet<SymbolCustomizerFactory.Part> aParts, boolean aFireModelChange, SymbologyFavorites aFavorites, boolean aDisplayPropertiesTitle, ILcdFilter<String> aHierarchyFilter, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fFireModelChange = aFireModelChange;
    fStringTranslator = aStringTranslator;
    fChangeListener = new MyChangeListener();
    fComponent = new JPanel();
    fComponent.setLayout(new BoxLayout(fComponent, BoxLayout.Y_AXIS));
    if (aParts.contains(SymbolCustomizerFactory.Part.REGULAR)) {
      fBasicPanel = new TwoColumnPanel();
      if (aDisplayPropertiesTitle) {
        makeCollapsible(fBasicPanel, translate("Symbol properties"), false);
      }
    } else {
      fBasicPanel = null;
    }
    if (aParts.contains(SymbolCustomizerFactory.Part.ADVANCED)) {
      fAdvancedPanel = new TwoColumnPanel();
      makeCollapsible(fAdvancedPanel, translate("More properties"), true);
    } else {
      fAdvancedPanel = null;
    }
    if (aParts.contains(SymbolCustomizerFactory.Part.STYLE)) {
      fStyleCustomizer = new SymbolStyleCustomizer(aFireModelChange, aStringTranslator);
      fStyleCustomizer.addChangeListener(fChangeListener);
      fStylePanel = fStyleCustomizer.getComponent();
      fStylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      makeCollapsible(fStylePanel, translate("Symbol style"), true);
    } else {
      fStyleCustomizer = null;
      fStylePanel = null;
    }
    fSymbolHierarchyCustomizer = new SymbolHierarchyCustomizer(aFavorites, aFireModelChange, aHierarchyFilter, aStringTranslator);
    fSymbolHierarchyCustomizer.getComponent().setAlignmentX(Component.LEFT_ALIGNMENT);
    fSymbolHierarchyCustomizer.addChangeListener(fChangeListener);
    fSelectionHint = new JLabel("Select a symbol to see its properties");
    fSelectionHint.setForeground(UIColors.fgHint());
  }

  private static void makeCollapsible(JComponent aComponent, String aTitle, boolean aCollapsed) {
    final TitledCollapsiblePane pane = new TitledCollapsiblePane(aTitle, aComponent);
    pane.setCollapsed(aCollapsed);
    pane.setAlignmentX(Component.LEFT_ALIGNMENT);
    aComponent.putClientProperty(KEY_COLLAPSIBLE, pane);
  }

  @Override
  protected void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (isNewSymbol(aSymbol)) {
     if (isEquivalentSymbol(aMilitarySymbology, aSymbol)) {
        // symbols are equivalent: leave the UI structure intact for performance reasons, e.g. for moving symbols.
        for (AbstractSymbolCustomizer c : fActiveCustomizers) {
          c.setSymbol(aMilitarySymbology, aModel, aSymbol);
        }
       //update favorites customizer
       fSymbolHierarchyCustomizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
       fSymbolHierarchyCustomizer.getFavoritesCustomizer().setSymbol(aMilitarySymbology, aModel, aSymbol);
     } else {
       // symbols are not equivalent: rebuild the UIcd/
      removeAll();
      if (aSymbol == null) {
        fComponent.add(fSelectionHint);
      } else {
        fComponent.add(fSymbolHierarchyCustomizer.getComponent());
        final List<String> basicModifierNames = fBasicModifierNames.containsKey(aMilitarySymbology) ?
                                                fBasicModifierNames.get(aMilitarySymbology) :
                                                fDefaultBasicModifierNames;
        final List<MilitarySymbolFacade.Modifier> basicModifiers = new ArrayList<>();
        final List<MilitarySymbolFacade.Modifier> advancedModifiers = new ArrayList<>();
        filterByName(MilitarySymbolFacade.getPossibleModifiers(aSymbol), basicModifierNames, basicModifiers, advancedModifiers);
        sortBy(basicModifiers, basicModifierNames);
        sortAlphabetically(advancedModifiers);
        populate(fBasicPanel, basicModifiers, aMilitarySymbology, aModel, aSymbol);
        populate(fAdvancedPanel, advancedModifiers, aMilitarySymbology, aModel, aSymbol);
        addCustomizerPanel(fBasicPanel);
        addCustomizerPanel(fAdvancedPanel);
        addCustomizerPanel(fStylePanel);
        fSymbolHierarchyCustomizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
        if (fStyleCustomizer != null) {
          fStyleCustomizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
        }
      }
      fComponent.revalidate();
     }
    }
  }

  private boolean isEquivalentSymbol(EMilitarySymbology aMilitarySymbology, Object aSymbol) {
    return aMilitarySymbology == getSymbology() && aSymbol != null && Objects.equals(getID(aSymbol), getID(getSymbol()));
  }

  private void removeAll() {
    for (AbstractSymbolCustomizer customizer : fActiveCustomizers) {
      customizer.dispose();
    }
    fActiveCustomizers.clear();
    fComponent.removeAll();
  }

  private static void filterByName(Collection<MilitarySymbolFacade.Modifier> aModifiers, List<String> aNames, Collection<MilitarySymbolFacade.Modifier> aAccepted, Collection<MilitarySymbolFacade.Modifier> aRejected) {
    for (MilitarySymbolFacade.Modifier modifier : aModifiers) {
      if (aNames.contains(modifier.getName())) {
        aAccepted.add(modifier);
      } else {
        aRejected.add(modifier);
      }
    }
  }

  private static void sortBy(List<MilitarySymbolFacade.Modifier> aModifiers, final List<String> aSortBy) {
    Collections.sort(aModifiers, new Comparator<MilitarySymbolFacade.Modifier>() {
      @Override
      public int compare(MilitarySymbolFacade.Modifier o1, MilitarySymbolFacade.Modifier o2) {
        return Integer.compare(aSortBy.indexOf(o1.getName()),
                               aSortBy.indexOf(o2.getName()));
      }
    });
  }

  private static void sortAlphabetically(List<MilitarySymbolFacade.Modifier> aModifiers) {
    Collections.sort(aModifiers, new Comparator<MilitarySymbolFacade.Modifier>() {
      @Override
      public int compare(MilitarySymbolFacade.Modifier o1, MilitarySymbolFacade.Modifier o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
  }

  private void populate(TwoColumnPanel aPanel, List<MilitarySymbolFacade.Modifier> aModifiers, EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (aPanel != null) {
      TwoColumnPanel.ContentBuilder builder = aPanel.contentBuilder();
      for (MilitarySymbolFacade.Modifier modifier : aModifiers) {
        final AbstractSymbolCustomizer customizer = createCustomizer(modifier, aMilitarySymbology);
        customizer.addChangeListener(fChangeListener);
        customizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
        fActiveCustomizers.add(customizer);
        builder.row(modifier.getShortDisplayName(),
                    customizer.getComponent());
        customizer.getComponent().addFocusListener(new MyFocusListener(modifier));
        if (modifier.equals(fFocusedModifier)) {
          requestFocus(customizer);
        }
      }
      builder.build();
    }
  }

  private void requestFocus(final AbstractSymbolCustomizer aCustomizer) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        aCustomizer.getComponent().requestFocus();
      }
    });
  }

  private AbstractSymbolCustomizer createCustomizer(final MilitarySymbolFacade.Modifier aModifier, EMilitarySymbology aSymbology) {
    // modifier specific
    switch (aModifier.getName()) {
    case "affiliation":
      return new SymbolAffiliationCustomizer(fFireModelChange, fStringTranslator);
    case "echelon":
    case "mobility":
      return new SymbolEchelonCustomizer(fFireModelChange, fStringTranslator, aModifier, aSymbology);
    default:
      // modifier type specific
      switch (aModifier.getType()) {
      case STRING_BOOLEAN:
        return new SymbolModifierBooleanCustomizer(fFireModelChange, fStringTranslator, aModifier);
      case STRING_ENUM:
        return new SymbolModifierEnumCustomizer(fFireModelChange, fStringTranslator, aModifier);
      case STRING_DATE_TIME:
        return new SymbolModifierDateTimeCustomizer(fFireModelChange, fStringTranslator, aModifier);
      default:
        return new SymbolModifierCustomizer(fFireModelChange, fStringTranslator, aModifier);
      }
    }
  }

  private void addCustomizerPanel(JComponent aComponent) {
    if (aComponent != null) {
      final TitledCollapsiblePane collapsiblePane = (TitledCollapsiblePane) aComponent.getClientProperty(KEY_COLLAPSIBLE);
      if (collapsiblePane != null) {
        fComponent.add(collapsiblePane);
      } else {
        fComponent.add(aComponent);
      }
    }
  }

  @Override
  public JComponent getComponent() {
    return fComponent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fComponent.setEnabled(aEnabled);
  }

  private class MyChangeListener implements ILcdChangeListener {
    @Override
    public void stateChanged(TLcdChangeEvent aChangeEvent) {
      fireChangeEvent(new TLcdChangeEvent(SymbolCustomizer.this));
    }
  }

  /**
   * Tracks which customizer is focused in order to restore it after rebuilding the panel.
   */
  private class MyFocusListener implements FocusListener {
    private final MilitarySymbolFacade.Modifier fModifier;

    private MyFocusListener(MilitarySymbolFacade.Modifier aModifier) {
      fModifier = aModifier;
    }

    @Override
    public void focusGained(FocusEvent e) {
      fFocusedModifier = fModifier;
    }

    @Override
    public void focusLost(FocusEvent e) {
      fFocusedModifier = null;
    }
  }
}
