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

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;

/**
 * {@link ALcyFormatBarFactory} that creates {@link samples.lucy.symbology.common.SymbologyFormatBar} instances for a Lightspeed
 * view.
 */
public abstract class LspSymbologyFormatBarFactory extends ALcyFormatBarFactory {

  private final ALcyFormat fSymbologyFormat;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;
  private final ILcyLucyEnv fLucyEnv;
  private final String fToolBarID;
  private final String fDialogFavoritesToolbarID;
  private final String fDialogSearchToolbarID;
  private final EMilitarySymbology fSymbology;
  private final SymbologyFavorites fFavorites;

  protected LspSymbologyFormatBarFactory(ALcyFormat aSymbologyFormat,
                                         ALcyProperties aProperties,
                                         String aPropertiesPrefix,
                                         ILcyLucyEnv aLucyEnv,
                                         String aToolBarID,
                                         String aDialogFavoritesToolbarID,
                                         String aDialogSearchToolbarID,
                                         EMilitarySymbology aSymbology,
                                         SymbologyFavorites aFavorites) {
    fSymbologyFormat = aSymbologyFormat;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv = aLucyEnv;
    fToolBarID = aToolBarID;
    fDialogFavoritesToolbarID = aDialogFavoritesToolbarID;
    fDialogSearchToolbarID = aDialogSearchToolbarID;
    fSymbology = aSymbology;
    fFavorites = aFavorites;
  }

  private ILcyLspMapComponent findMapComponent(ILspView aView) {
    return (ILcyLspMapComponent) fLucyEnv.getCombinedMapManager().findMapComponent(aView);
  }

  @Override
  public boolean canCreateFormatBar(ILcdView aView, ILcdLayer aLayer) {
    return aView instanceof ILspView && findMapComponent((ILspView) aView) != null;
  }

  @Override
  public ALcyFormatBar createFormatBar(ILcdView aView, ILcdLayer aLayer) {
    ILcyLspMapComponent map = findMapComponent((ILspView) aView);
    return new LspSymbologyFormatBar(fSymbologyFormat,
                                     map,
                                     fToolBarID,
                                     fDialogFavoritesToolbarID,
                                     fDialogSearchToolbarID,
                                     fSymbology,
                                     fFavorites,
                                     fProperties,
                                     fPropertiesPrefix,
                                     fLucyEnv);
  }
}
