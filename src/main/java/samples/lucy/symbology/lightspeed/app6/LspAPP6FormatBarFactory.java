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
package samples.lucy.symbology.lightspeed.app6;

import com.luciad.gui.TLcdIconFactory;
import samples.lucy.symbology.lightspeed.LspSymbologyFormatBarFactory;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * {@link com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory} that creates the format bar for APP-6 layers.
 * This class shows how a format bar factory for APP-6 can be created.
 */
public class LspAPP6FormatBarFactory extends LspSymbologyFormatBarFactory {

  public LspAPP6FormatBarFactory(ALcyFormat aSymbologyFormat,
                                 ALcyProperties aProperties,
                                 String aPropertiesPrefix,
                                 ILcyLucyEnv aLucyEnv,
                                 ELcdAPP6Standard aStandard,
                                 SymbologyFavorites aFavorites) {
    super(aSymbologyFormat,
          aProperties,
          aPropertiesPrefix,
          aLucyEnv,
          "app6aToolBar",
          "app6aFavoritesToolBar",
          "app6aSearchToolBar",
          EMilitarySymbology.fromStandard(aStandard),
          aFavorites);
  }

  @Override
  public ALcyFormatBar createFormatBar(ILcdView aView, ILcdLayer aLayer) {
    ALcyFormatBar formatBar = super.createFormatBar(aView, aLayer);
    formatBar.putValue(ALcyFormatBar.NAME, "APP-6");
    formatBar.putValue(ALcyFormatBar.SHORT_DESCRIPTION, TLcyLang.getString("Draw and modify APP6 objects"));
    formatBar.putValue(ALcyFormatBar.SMALL_ICON, TLcdIconFactory.create(TLcdIconFactory.MILITARY_SYMBOL_ICON));
    return formatBar;
  }
}
