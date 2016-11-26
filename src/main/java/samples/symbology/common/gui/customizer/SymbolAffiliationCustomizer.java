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
 * Provides a combo box to customize a military symbol's affiliation modifier.
 */
class SymbolAffiliationCustomizer extends AbstractSymbolComboBoxCustomizer<String> {

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public SymbolAffiliationCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
  }

  @Override
  protected void setValue(Object aSymbol, String aValue) {
    MilitarySymbolFacade.setAffiliationValue(aSymbol, aValue);
  }

  @Override
  protected String getValue(Object aSymbol) {
    return MilitarySymbolFacade.getAffiliationValue(aSymbol);
  }

  @Override
  protected String getDisplayName(String aValue) {
    return aValue;
  }

  @Override
  public List<String> retrieveValues(Object aSymbol) {
    return new ArrayList<String>(MilitarySymbolFacade.getPossibleAffiliationValues(aSymbol));
  }
}
