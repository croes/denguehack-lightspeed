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

import static samples.symbology.common.util.MilitarySymbolFacade.getPossibleTextModifiers;

import java.util.Collection;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a text field to customize a text modifier value.
 *
 * @see MilitarySymbolFacade.Modifier
 */
class SymbolModifierCustomizer extends AbstractSymbolTextFieldCustomizer {

  private final String fModifierName;
  private MilitarySymbolFacade.Modifier fCurrentModifier;

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   * @param aModifier        the text modifier to customize
   */
  public SymbolModifierCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, MilitarySymbolFacade.Modifier aModifier) {
    this(aFireModelChange, aStringTranslator, aModifier.getName());
  }

  protected SymbolModifierCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, String aName) {
    super(aName, aFireModelChange, aStringTranslator);
    fModifierName = aName;
    setHint(null);
  }

  @Override
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    // Look up the appropriate modifier.
    MilitarySymbolFacade.Modifier modifier = null;
    if (aSymbol != null) {
      Collection<MilitarySymbolFacade.Modifier> modifiers = getPossibleTextModifiers(aSymbol);
      for (MilitarySymbolFacade.Modifier m : modifiers) {
        if (m.getName().equals(fModifierName)) {
          modifier = m;
        }
      }
    }
    fCurrentModifier = modifier;
    super.setSymbolImpl(aMilitarySymbology, aModel, aSymbol);
  }

  @Override
  protected void setValue(Object aSymbol, String aValue) {
    MilitarySymbolFacade.setModifierValue(aSymbol, fCurrentModifier, aValue);
  }

  @Override
  protected String getValue(Object aSymbol) {
    return MilitarySymbolFacade.getModifierValue(aSymbol, fCurrentModifier);
  }

  @Override
  protected boolean hasValue(Object aSymbol) {
    Collection<MilitarySymbolFacade.Modifier> modifiers = MilitarySymbolFacade.getPossibleTextModifiers(aSymbol);
    return fCurrentModifier != null && modifiers.contains(fCurrentModifier);
  }

}
