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
package samples.symbology.nvg.common;

import java.util.HashSet;
import java.util.Set;

import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg15.model.TLcdNVG15MapObject;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;

import samples.symbology.common.util.MilitarySymbolFacade;

/**
 *  Utility methods for clustering nvg objects.
 */
public class NVGSymbolClusteringUtil {

  public static Object getMilitarySymbol(Object aObject) {
    if (hasSymbol(aObject)) {
      TLcdNVGSymbol nvgSymbol = TLcdNVGSymbol.getSymbol(aObject);
      Object militarySymbol = null;
      if (TLcdNVGSymbol.isAPP6ASymbol(nvgSymbol)) {
        militarySymbol = new TLcdEditableAPP6AObject(nvgSymbol.getTextRepresentation(), TLcdNVGSymbol.getAPP6Standard(nvgSymbol.getStandardName()));
      } else if (TLcdNVGSymbol.isMS2525bSymbol(nvgSymbol)) {
        militarySymbol = new TLcdEditableMS2525bObject(nvgSymbol.getTextRepresentation(), TLcdNVGSymbol.getMS2525Standard(nvgSymbol.getStandardName()));
      }
      return militarySymbol;
    }
    return null;
  }

  public static boolean hasMilitarySymbol(Object aObject) {
    return getMilitarySymbol(aObject) != null;
  }

  public static boolean allSameSymbol(TLcdCluster<?> aCluster) {
    Set<String> symbols = new HashSet<>();
    for (Object element : aCluster.getComposingElements()) {
      if (hasMilitarySymbol(element)){
        Object militarySymbol = getMilitarySymbol(element);
        String sidc = MilitarySymbolFacade.getSIDC(militarySymbol);
        symbols.add(MilitarySymbolFacade.getSIDCMask(MilitarySymbolFacade.retrieveSymbology(militarySymbol), sidc));
      } else {
        return false;
      }
    }
    return symbols.size() == 1;
  }

  private static boolean hasSymbol(Object aObject) {
    return (aObject instanceof TLcdNVG15MapObject && ((TLcdNVG15MapObject) aObject).getSymbol() != null) ||
           aObject instanceof TLcdNVG20SymbolizedContent;
  }

}
