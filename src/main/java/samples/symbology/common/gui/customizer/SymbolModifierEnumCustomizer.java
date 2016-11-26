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

import java.util.ArrayList;
import java.util.List;

import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a combo box to customize an enumerated text modifier value of a military symbol.
 */
class SymbolModifierEnumCustomizer extends AbstractSymbolComboBoxCustomizer<String> {

  private final MilitarySymbolFacade.Modifier fModifier;

  public SymbolModifierEnumCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, MilitarySymbolFacade.Modifier aModifier) {
    super(aFireModelChange, aStringTranslator);
    fModifier = aModifier;
  }

  @Override
  protected void setValue(Object aSymbol, String aValue) {
    MilitarySymbolFacade.setModifierValue(aSymbol, fModifier, aValue);
  }

  @Override
  protected String getValue(Object aSymbol) {
    return MilitarySymbolFacade.getModifierValue(aSymbol, fModifier);
  }

  @Override
  public List<String> retrieveValues(Object aSymbol) {
    return new ArrayList<>(fModifier.getPossibleValues());
  }

  @Override
  protected String getDisplayName(String aValue) {
    return fModifier.getDisplayName(aValue);
  }
}
