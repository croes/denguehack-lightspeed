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
package samples.symbology.common;

import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;

/**
 * Enumeration class identifying the different supported military symbology standards.
 */
public enum EMilitarySymbology {

  /** US DOD Military Symbology Standard 2525b (January 1999) */
  MILSTD_2525B(ELcdMS2525Standard.MIL_STD_2525b, "MIL-STD 2525b"),

  /** US DOD Military Symbology Standard 2525c (November 2008) */
  MILSTD_2525C(ELcdMS2525Standard.MIL_STD_2525c, "MIL-STD 2525c"),

  /** NATO Military Symbology Standard APP-6A (December 1999, STANAG 2019 edition 4) */
  APP6A(ELcdAPP6Standard.APP_6A, "APP-6A"),

  /** NATO Military Symbology Standard APP-6B (June 2008, STANAG 2019) */
  APP6B(ELcdAPP6Standard.APP_6B, "APP-6B"),

  /** NATO Military Symbology Standard APP-6C (May 2011, STANAG 2019 edition 6) */
  APP6C(ELcdAPP6Standard.APP_6C, "APP-6C");

  private Object fStandard;
  private String fFriendlyName;

  EMilitarySymbology(Object aStandard, String aFriendlyName) {
    fStandard = aStandard;
    fFriendlyName = aFriendlyName;
  }

  /**
   * Returns the API class representing the symbology.
   * @return an instance of ELcdMS2525Standard or ELcdAPP6Standard
   */
  public Object getStandard() {
    return fStandard;
  }

  /**
   * Returns the EMilitarySymbology corresponding to the given API standard.
   * @param aStandard an instance of ELcdMS2525Standard or ELcdAPP6Standard
   * @return the corresponding EMilitarySymbology
   */
  public static EMilitarySymbology fromStandard(Object aStandard) {
    if (aStandard instanceof ELcdMS2525Standard) {
      return aStandard == ELcdMS2525Standard.MIL_STD_2525b ? MILSTD_2525B : MILSTD_2525C;
    } else if (aStandard instanceof ELcdAPP6Standard) {
      return aStandard == ELcdAPP6Standard.APP_6A ? APP6A : (aStandard == ELcdAPP6Standard.APP_6B ? APP6B : APP6C);
    }
    throw new IllegalArgumentException("Given standard must be ELcdMS2525Standard or ELcdAPP6Standard, but got " + aStandard);
  }

  /**
   * Returns the EMilitarySymbology corresponding to the given military symbol.
   * @param aObject an instance of ILcdMS2525bCoded or ILcdAPP6ACoded
   * @return the corresponding EMilitarySymbology
   */
  public static EMilitarySymbology fromObject(Object aObject) {
    if (aObject instanceof ILcdMS2525bCoded) {
      return fromStandard(((ILcdMS2525bCoded) aObject).getMS2525Standard());
    } else if (aObject instanceof ILcdAPP6ACoded) {
      return fromStandard(((ILcdAPP6ACoded) aObject).getAPP6Standard());
    }
    throw new IllegalArgumentException("Given object must be ILcdMS2525bCoded or ILcdAPP6ACoded, but got " + aObject);
  }

  /**
   * Returns the military symbology of which the {@link #toString()} result corresponds with the given name.
   * @param aName the name of the symbology
   * @return the military symbology with the given name.
   */
  public static EMilitarySymbology fromName(String aName) {
    for (EMilitarySymbology symbology : EMilitarySymbology.values()) {
      if (symbology.toString().equals(aName)) {
        return symbology;
      }
    }
    throw new IllegalArgumentException("Unknown symbology name: " + aName);
  }

  public String toString() {
    return fFriendlyName;
  }

}
