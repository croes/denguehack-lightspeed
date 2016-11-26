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

import java.util.Arrays;
import java.util.List;

import samples.symbology.common.util.MilitarySymbolFacade;

/**
 * Enum representing battle dimensions of military symbols.
 * It groups certain battle dimensions (or symbol sets for APP6C).
 *
 * @since 2016.0
 */
public enum BattleDimension {

  LAND("Land", NonAPP6CBattleDimension.GROUND, APP6CBattleDimension.LAND),
  SEA("Sea", NonAPP6CBattleDimension.SEA_SURFACE, NonAPP6CBattleDimension.SEA_SUBSURFACE, APP6CBattleDimension.SEA),
  AIR("Air", NonAPP6CBattleDimension.AIR, APP6CBattleDimension.AIR),
  SPACE("Space", NonAPP6CBattleDimension.SPACE, APP6CBattleDimension.SPACE),
  UNKNOWN("Unknown", NonAPP6CBattleDimension.UNKNOWN, APP6CBattleDimension.UNKNOWN),
  OTHER("Other", NonAPP6CBattleDimension.OTHER, NonAPP6CBattleDimension.SPECIAL_OPERATIONS, APP6CBattleDimension.OTHER);

  private final List<Enum<?>> fBattleDimensions;
  private final String fName;

  BattleDimension(String aName, Enum<?>... battleDimensions) {
    fName = aName;
    fBattleDimensions = Arrays.asList(battleDimensions);
  }

  public static BattleDimension from(Object aObject) {
    if (MilitarySymbolFacade.isMilitarySymbol(aObject)) {
      EMilitarySymbology militarySymbology = MilitarySymbolFacade.retrieveSymbology(aObject);
      String sidc = MilitarySymbolFacade.getSIDC(aObject);
      if (militarySymbology == EMilitarySymbology.APP6C) {
         return fromEnum(APP6CBattleDimension.getBattleDimension(sidc));
      } else {
        return fromEnum(NonAPP6CBattleDimension.getBattleDimension(sidc));
      }
    }
    return null;
  }

  private static BattleDimension fromEnum(Enum<?> aBattleDimension) {
    for (BattleDimension battleDimension : values()) {
      if (battleDimension.fBattleDimensions.contains(aBattleDimension)) {
        return battleDimension;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return fName;
  }

  /**
   * Battle dimensions of (non-APP6C) military symbols.
   *
   */
  private enum NonAPP6CBattleDimension {

    SPACE('P', "Space"),
    AIR('A', "Air"),
    GROUND('G', "Ground"),
    SEA_SURFACE('S', "Sea surface"),
    SEA_SUBSURFACE('U', "Sea subsurface"),
    SPECIAL_OPERATIONS('F', "Special operations"),
    OTHER('X', "Other"),
    UNKNOWN('Z', "Unknown");

    private final char fBD;
    private final String fName;

    NonAPP6CBattleDimension(char aBD, String aName) {
      fBD = aBD;
      fName = aName;
    }

    @Override
    public String toString() {
      return fName;
    }

    public static NonAPP6CBattleDimension getBattleDimension(String aSIDC) {
      for (NonAPP6CBattleDimension bd : NonAPP6CBattleDimension.values()) {
        if (aSIDC.charAt(2) == bd.fBD) {
          return bd;
        }
      }
      return UNKNOWN;
    }

  }

  /**
   * Battle dimensions (symbol set) of APP6C military symbols.
   *
   * @since 2016.0
   */
  private enum APP6CBattleDimension {

    LAND("10", "11", "15", "20"),
    SEA("30", "35", "36"),
    AIR("01", "02"),
    SPACE("05", "06"),
    UNKNOWN("00"),
    OTHER;

    private final List<String> fCodes;

    APP6CBattleDimension(String... aCodes) {
      fCodes = Arrays.asList(aCodes);
    }

    public static APP6CBattleDimension getBattleDimension(String sidc) {
      String battleDimensionCode = getBattleDimensionCode(sidc);
      return from(battleDimensionCode);
    }

    private static String getBattleDimensionCode(String sidc) {
      return sidc.substring(4, 6);
    }

    private static APP6CBattleDimension from(String aCode) {
      for (APP6CBattleDimension battleDimension : APP6CBattleDimension.values()) {
        if (battleDimension.fCodes.contains(aCode)) {
          return battleDimension;
        }
      }
      return OTHER;
    }

  }

}
