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
package samples.symbology.common.util;

import java.util.HashSet;
import java.util.Set;

import com.luciad.model.transformation.clustering.TLcdCluster;

/**
 *  Utility methods for clustering military symbols.
 */
public class MilitarySymbolClusteringUtil {

  private MilitarySymbolClusteringUtil() {
  }

  public static boolean allSameSymbol(TLcdCluster<?> aCluster) {
    return new AllSameChecker(aCluster, new AllSameChecker.Property() {
      @Override
      public String getMilitarySymbologyProperty(Object aObject) {
        String sidc = MilitarySymbolFacade.getSIDC(aObject);
        return MilitarySymbolFacade.getSIDCMask(MilitarySymbolFacade.retrieveSymbology(aObject), sidc);
      }
    }).isAllSame();
  }

  public static boolean allSameCountry(TLcdCluster<?> aCluster) {
    return new AllSameChecker(aCluster, new AllSameChecker.Property() {
      @Override
      public String getMilitarySymbologyProperty(Object aObject) {
        return MilitarySymbolFacade.getCountry(aObject);
      }
    }).isAllSame();
  }

  private static class AllSameChecker {

    private final TLcdCluster<?> fCluster;
    private final Property fProperty;

    private static abstract class Property {

      public String getProperty(Object aObject) {
        if (MilitarySymbolFacade.isMilitarySymbol(aObject)) {
          return getMilitarySymbologyProperty(aObject);
        } else {
          return null;
        }
      }

      public abstract String getMilitarySymbologyProperty(Object aObject);

    }

    public AllSameChecker(TLcdCluster<?> aCluster, Property aProperty) {
      fCluster = aCluster;
      fProperty = aProperty;
    }

    public boolean isAllSame() {
      Set<String> properties = new HashSet<>();
      for (Object o : fCluster.getComposingElements()) {
        String property = fProperty.getProperty(o);
        if (property != null) {
          properties.add(property);
        } else {
          return false;
        }
      }
      return properties.size() == 1;
    }

  }

}
