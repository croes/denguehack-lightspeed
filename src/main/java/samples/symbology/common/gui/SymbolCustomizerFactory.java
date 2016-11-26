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
package samples.symbology.common.gui;

import java.util.EnumSet;

import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.gui.customizer.SymbolCustomizer;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.symbology.app6a.view.swing.TLcdAPP6AObjectCustomizer;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

/**
 * Creates GUI panels with which the user can change the properties of a military symbol.
 */
public class SymbolCustomizerFactory {

  /**
   * The possible contents of the GUI panel.
   */
  public enum Part {
    /**
     * A panel with the symbol name and some basic properties such as name, affiliation,
     * and status.
     * It also allows marking a symbol as a favorite.
     */
    REGULAR,
    /**
     * A panel with less used textual properties.
     */
    ADVANCED,
    /**
     * A panel with style properties, such as halo and size.
     */
    STYLE
  }

  /**
   * Creates a customizer consisting of the given parts.
   * @param aParts           describes which sub-panels should be shown
   * @param aFireModelChangeEvent true if model changes should be fired. Typically this is only false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aFavorites       the instance responsible for managing the favorite symbols
   * @param aTitledSections  if true, a title will be displayed for in the regular symbol properties sub-panel.
   *                         Use false if the panel is already contained in, for example, a titled dialog or panel.
   * @param aSIDCFilter      an optional filter to reduce the amount of symbols when browsing through the symbol hierarchy.
   *                         See {@link TLcdAPP6AObjectCustomizer#getSIDCFilter()}. Can be null.
   * @param aStringTranslator a string translator. Can be null.
   * @return a GUI panel consisting of the given parts
   */
  public static AbstractSymbolCustomizer createCustomizer(
      EnumSet<Part> aParts,
      boolean aFireModelChangeEvent,
      SymbologyFavorites aFavorites,
      boolean aTitledSections,
      ILcdFilter<String> aSIDCFilter,
      ILcdStringTranslator aStringTranslator) {
    return new SymbolCustomizer(aParts, aFireModelChangeEvent, aFavorites, aTitledSections, aSIDCFilter, aStringTranslator);
  }

}
