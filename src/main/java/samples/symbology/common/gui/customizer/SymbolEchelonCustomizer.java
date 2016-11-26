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

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.symbology.app6a.model.TLcdAPP6AEchelonNode;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bEchelonNode;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a combo box to customize a military symbol's echelon modifier.
 */
class SymbolEchelonCustomizer extends AbstractSymbolComboBoxCustomizer<String> {

  private final MilitarySymbolFacade.Modifier fModifier;

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public SymbolEchelonCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, MilitarySymbolFacade.Modifier aModifier, EMilitarySymbology aSymbology) {
    super(aFireModelChange, new EchelonIconProvider(aSymbology), aStringTranslator);
    fModifier = aModifier;
    getContent().setWidthFactor(2.0);
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
  protected String getDisplayName(String aValue) {
    return fModifier.getDisplayName(aValue);
  }

  @Override
  public List<String> retrieveValues(Object aSymbol) {
    return new ArrayList<>(fModifier.getPossibleValues());
  }

  private static class EchelonIconProvider implements ILcdObjectIconProvider {

    private final EMilitarySymbology fSymbology;

    private EchelonIconProvider(EMilitarySymbology aSymbology) {
      fSymbology = aSymbology;
    }

    @Override
    public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
      Object node = retrieveEchelonNode(aObject);
      if (node instanceof TLcdMS2525bEchelonNode) {
        return ((TLcdMS2525bEchelonNode) node).getPreviewIcon();
      } else {
        return ((TLcdAPP6AEchelonNode) node).getPreviewIcon();
      }
    }

    @Override
    public boolean canGetIcon(Object aObject) {
      return retrieveEchelonNode(aObject) != null;
    }

    public Object retrieveEchelonNode(Object aEchelon) {
      List<Object> echelons = new ArrayList<>();
      MilitarySymbolFacade.getAllPossibleEchelonsSFCT(fSymbology, echelons, new ArrayList<String>());
      for (Object echelon : echelons) {
        if (echelon instanceof TLcdMS2525bEchelonNode) {
          TLcdMS2525bEchelonNode node = (TLcdMS2525bEchelonNode) echelon;
          if (node.getName().equals(aEchelon)) {
            return node;
          }
        } else if (echelon instanceof TLcdAPP6AEchelonNode) {
          TLcdAPP6AEchelonNode node = (TLcdAPP6AEchelonNode) echelon;
          if (node.getName().equals(aEchelon)) {
            return node;
          }
        }
      }
      return null;
    }
  }
}
