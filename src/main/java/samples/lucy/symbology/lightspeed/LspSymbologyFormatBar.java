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

import samples.lucy.symbology.common.SymbolPopupAction;
import samples.lucy.symbology.common.SymbologyFormatBar;
import samples.lucy.symbology.gxy.GXYSymbologyFormatBar;
import samples.lucy.util.LayerUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.ILspView;

/**
 * Format bar for lightspeed symbology layers
 */
class LspSymbologyFormatBar extends SymbologyFormatBar<ILspView> {

  private final LspSymbologyCreateControllerModel fCreateControllerModel;
  private final ILcyLspMapComponent fMapComponent;
  private final String fDialogFavoritesToolbarID;
  private final String fDialogSearchToolbarID;
  private final EMilitarySymbology fSymbology;
  private final SymbologyFavorites fFavorites;
  private final ILcyLucyEnv fLucyEnv;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;
  private final ALcyFormat fSymbologyFormat;
  private final SymbolPopupAction fSymbolPopupAction;

  public LspSymbologyFormatBar(ALcyFormat aSymbologyFormat,
                               ILcyLspMapComponent aMapComponent,
                               String aToolBarID,
                               String aDialogFavoritesToolbarID,
                               String aDialogSearchToolbarID,
                               EMilitarySymbology aSymbology,
                               SymbologyFavorites aFavorites,
                               ALcyProperties aProperties,
                               String aPropertiesPrefix,
                               ILcyLucyEnv aLucyEnv) {
    super(aMapComponent, aToolBarID, aProperties, aPropertiesPrefix, aLucyEnv);
    fSymbologyFormat = aSymbologyFormat;
    fMapComponent = aMapComponent;
    fDialogFavoritesToolbarID = aDialogFavoritesToolbarID;
    fDialogSearchToolbarID = aDialogSearchToolbarID;
    fSymbology = aSymbology;
    fFavorites = aFavorites;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv = aLucyEnv;
    fCreateControllerModel = createNewControllerModel(aMapComponent, aSymbology);

    fSymbolPopupAction = new SymbolPopupAction(fMapComponent, fProperties, fPropertiesPrefix, fDialogFavoritesToolbarID, fDialogSearchToolbarID, fLucyEnv);
    fSymbolPopupAction.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + GXYSymbologyFormatBar.POPUP_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(fSymbolPopupAction,
                                                   aMapComponent,
                                                   fLucyEnv.getUserInterfaceManager().getActionBarManager(),
                                                   fProperties);

    addFavoritesAction(aMapComponent);
    addCreateObjectAction(aMapComponent);
  }

  ILcyLspMapComponent getMapComponent() {
    return fMapComponent;
  }

  private LspSymbologyCreateControllerModel createNewControllerModel(ILcyLspMapComponent aMapComponent, EMilitarySymbology aAdapter) {
    TLcyLspCreateLayerAction createLayerAction = LayerUtil.insertCreateLayerAction(fProperties, fPropertiesPrefix, fSymbologyFormat, aMapComponent);
    return new LspSymbologyCreateControllerModel(createLayerAction, aMapComponent, aAdapter, fLucyEnv);
  }

  private void addFavoritesAction(ILcyLspMapComponent aMapComponent) {
    LspFavoritesAction action = new LspFavoritesAction(fCreateControllerModel, fSymbology, this, fFavorites, fProperties, fPropertiesPrefix, fLucyEnv) {
      @Override
      protected void createSymbol(String aSIDC) {
        super.createSymbol(aSIDC);
        //close all the pop-ups when a symbol is chosen
        fSymbolPopupAction.closePopups();
      }
    };
    action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + GXYSymbologyFormatBar.FAVORITES_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        fLucyEnv.getUserInterfaceManager().getActionBarManager(),
        fProperties);
  }

  private void addCreateObjectAction(ILcyLspMapComponent aMapComponent) {
    ILcyCustomizableRepresentationAction action = new LspCreationSearchFieldAction(fCreateControllerModel, fSymbology, this, fProperties, fPropertiesPrefix, fLucyEnv) {
      @Override
      protected void createSymbol(String aSIDC) {
        super.createSymbol(aSIDC);
        //close all the pop-ups when a symbol is chosen
        fSymbolPopupAction.closePopups();
      }
    };
    action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + GXYSymbologyFormatBar.CREATE_OBJECT_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        fLucyEnv.getUserInterfaceManager().getActionBarManager(),
        fProperties);
  }
}
