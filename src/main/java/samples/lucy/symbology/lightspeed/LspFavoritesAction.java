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
package samples.lucy.symbology.lightspeed;

import samples.lucy.symbology.common.FavoritesAction;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Lightspeed specific favorites action.
 */
class LspFavoritesAction extends FavoritesAction {

  private final LspSymbologyCreateControllerModel fControllerModel;
  private final EMilitarySymbology fSymbology;
  private final LspSymbologyFormatBar fFormatBar;
  private final ILcyLucyEnv fLucyEnv;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;

  public LspFavoritesAction(LspSymbologyCreateControllerModel aControllerModel,
                            EMilitarySymbology aSymbology,
                            LspSymbologyFormatBar aFormatBar,
                            SymbologyFavorites aFavorites,
                            ALcyProperties aProperties,
                            String aPropertiesPrefix,
                            ILcyLucyEnv aLucyEnv) {
    super(aSymbology, aFavorites, aProperties, aPropertiesPrefix);
    fControllerModel = aControllerModel;
    fSymbology = aSymbology;
    fFormatBar = aFormatBar;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv = aLucyEnv;
  }

  @Override
  protected void createSymbol(final String aSIDC) {
    fControllerModel.setCurrentLayer((ILspInteractivePaintableLayer) fFormatBar.getLayer());
    LspCreateControllerActiveSettableFactory.createActiveSettable(
        fFormatBar.getMapComponent(),
        fControllerModel,
        new LspSymbologyCreateControllerModel.CodedSelector() {
          @Override
          public Object selectCoded(ILspView aView, ILspLayer aLayer) {
            Object symbol = MilitarySymbolFacade.newElement(fSymbology, true);
            MilitarySymbolFacade.setSIDC(symbol, aSIDC);
            return symbol;
          }
        },
        true,  // activate the active settable
        fProperties,
        fPropertiesPrefix,
        fLucyEnv);
  }
}
