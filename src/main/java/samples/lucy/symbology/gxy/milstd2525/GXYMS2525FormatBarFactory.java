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
package samples.lucy.symbology.gxy.milstd2525;

import com.luciad.gui.TLcdIconFactory;
import samples.lucy.symbology.gxy.GXYSymbologyFormatBarFactory;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * {@link ALcyFormatBarFactory} that creates the format bar for
 * MIL-STD 2525 layers.
 */
public class GXYMS2525FormatBarFactory extends GXYSymbologyFormatBarFactory {

  public GXYMS2525FormatBarFactory(ALcyFormat aSymbologyFormat,
                                   ALcyProperties aProperties,
                                   String aPropertiesPrefix,
                                   ILcyLucyEnv aLucyEnv,
                                   ELcdMS2525Standard aStandard,
                                   SymbologyFavorites aFavorites) {
    super(aSymbologyFormat,
          aProperties,
          aPropertiesPrefix,
          aLucyEnv,
          "ms2525bToolBar",
          "ms2525bFavoritesToolBar",
          "ms2525bSearchToolBar",
          EMilitarySymbology.fromStandard(aStandard),
          aFavorites);
  }

  @Override
  public ALcyFormatBar createFormatBar(ILcdView aView, ILcdLayer aLayer) {
    ALcyFormatBar formatBar = super.createFormatBar(aView, aLayer);
    formatBar.putValue(ALcyFormatBar.NAME, "MIL-STD 2525");
    formatBar.putValue(ALcyFormatBar.SHORT_DESCRIPTION, TLcyLang.getString("Draw and modify MS2525 objects"));
    formatBar.putValue(ALcyFormatBar.SMALL_ICON, TLcdIconFactory.create(TLcdIconFactory.MILITARY_SYMBOL_ICON));
    return formatBar;
  }

}
