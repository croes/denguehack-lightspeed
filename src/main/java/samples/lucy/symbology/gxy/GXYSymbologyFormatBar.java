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
package samples.lucy.symbology.gxy;

import samples.lucy.symbology.common.SymbolPopupAction;
import samples.lucy.symbology.common.SymbologyFormatBar;
import samples.lucy.util.LayerUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.action.TLcyCreateGXYLayerAction;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Format bar for GXY symbology layers
 */
public class GXYSymbologyFormatBar extends SymbologyFormatBar<ILcdGXYView> {

  public static final String CREATE_OBJECT_ACTION_PROPERTY = "createObjectAction";
  public static final String FAVORITES_ACTION_PROPERTY = "favoritesAction";
  public static final String POPUP_ACTION_PROPERTY = "symbolChooserPopupAction";

  private final GXYSymbologyNewControllerModel fNewControllerModel;
  private final EMilitarySymbology fSymbology;
  private final ALcyFormat fSymbologyFormat;
  private final String fDialogFavoritesToolbarID;
  private final String fDialogSearchToolbarID;
  private final SymbologyFavorites fFavorites;
  private final ILcyLucyEnv fLucyEnv;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;
  private final SymbolPopupAction fSymbolPopupAction;

  public GXYSymbologyFormatBar(ALcyFormat aSymbologyFormat,
                               ILcyMapComponent aMapComponent,
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
    fDialogFavoritesToolbarID = aDialogFavoritesToolbarID;
    fDialogSearchToolbarID = aDialogSearchToolbarID;
    fFavorites = aFavorites;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv = aLucyEnv;
    fNewControllerModel = createNewControllerModel(aMapComponent, aSymbology);
    fSymbology = aSymbology;

    fSymbolPopupAction = new SymbolPopupAction(aMapComponent, fProperties, fPropertiesPrefix, fDialogFavoritesToolbarID, fDialogSearchToolbarID, fLucyEnv);
    fSymbolPopupAction.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + GXYSymbologyFormatBar.POPUP_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(fSymbolPopupAction,
                                                   aMapComponent,
                                                   fLucyEnv.getUserInterfaceManager().getActionBarManager(),
                                                   fProperties);

    addFavoritesAction(aMapComponent);
    addCreateObjectAction(aMapComponent);
  }

  private GXYSymbologyNewControllerModel createNewControllerModel(ILcyMapComponent aMapComponent, EMilitarySymbology aAdapter) {
    TLcyCreateGXYLayerAction newLayerAction = LayerUtil.insertCreateLayerAction(fProperties, fPropertiesPrefix, fSymbologyFormat, aMapComponent);
    return new GXYSymbologyNewControllerModel(newLayerAction, aMapComponent, aAdapter, fLucyEnv);
  }

  private void addFavoritesAction(ILcyMapComponent aMapComponent) {
    GXYFavoritesAction action = new GXYFavoritesAction(fNewControllerModel, fSymbology, this, fFavorites, fProperties, fPropertiesPrefix, fLucyEnv) {
      @Override
      protected void createSymbol(String aSIDC) {
        super.createSymbol(aSIDC);
        fSymbolPopupAction.closePopups();
      }
    };
    action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + FAVORITES_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        fLucyEnv.getUserInterfaceManager().getActionBarManager(),
        fProperties);
  }

  private void addCreateObjectAction(ILcyMapComponent aMapComponent) {
    ILcyCustomizableRepresentationAction action = new GXYCreationSearchFieldAction(fNewControllerModel, fSymbology, this, fProperties, fPropertiesPrefix, fLucyEnv) {
      @Override
      protected void createSymbol(String aSIDC) {
        super.createSymbol(aSIDC);
        fSymbolPopupAction.closePopups();
      }
    };
    action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + CREATE_OBJECT_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        fLucyEnv.getUserInterfaceManager().getActionBarManager(),
        fProperties);
  }
}
