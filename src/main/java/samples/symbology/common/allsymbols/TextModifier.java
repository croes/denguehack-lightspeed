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
package samples.symbology.common.allsymbols;

import java.util.Collection;

import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;

public enum TextModifier {

  sQuantity(ILcdAPP6ACoded.sQuantityOfEquipment, ILcdMS2525bCoded.sQuantity, "7"),
  sReinforcedOrReduced(ILcdAPP6ACoded.sReinforcedOrReduced, ILcdMS2525bCoded.sReinforcedOrReduced, "RD"),
  sStaffComments(ILcdAPP6ACoded.sStaffComments, ILcdMS2525bCoded.sStaffComments, "Staff comment"),
  sAdditionalInformation(ILcdAPP6ACoded.sAdditionalInformation, ILcdMS2525bCoded.sAdditionalInformation, "H:H1:H2"),
  sEvaluationRating(ILcdAPP6ACoded.sEvaluationRating, ILcdMS2525bCoded.sEvaluationRating, "A1"),
  sCombatEffectiveness(ILcdAPP6ACoded.sCombatEffectiveness, ILcdMS2525bCoded.sCombatEffectiveness, "Effectiveness"),
  sSignatureEquipment(ILcdAPP6ACoded.sSignatureEquipment, ILcdMS2525bCoded.sSignatureEquipment, "!"),
  sHigherFormation(ILcdAPP6ACoded.sHigherFormation, ILcdMS2525bCoded.sHigherFormation, "HigherFormation"),
  sHostile(ILcdAPP6ACoded.sHostile, ILcdMS2525bCoded.sHostile, "ENY"),
  sIFFSIF(ILcdAPP6ACoded.sIFFSIF, ILcdMS2525bCoded.sIFFSIF, "IFF"),
  sMobilityIndicator(null, ILcdMS2525bCoded.sMobilityIndicator, "Amphibious"),
  sUniqueDesignation(ILcdAPP6ACoded.sUniqueDesignation, ILcdMS2525bCoded.sUniqueDesignation, "T:T1"),
  sTypeLabel(ILcdAPP6ACoded.sTypeOfEquipment, ILcdMS2525bCoded.sTypeLabel, "Type"),
  sDateTimeGroup(ILcdAPP6ACoded.sDateTimeGroup, ILcdMS2525bCoded.sDateTimeGroup, "W"),
  sAltitudeDepth(ILcdAPP6ACoded.sAltitudeDepth, ILcdMS2525bCoded.sAltitudeDepth, "X:X1"),
  sLocationLabel(ILcdAPP6ACoded.sLocationLabel, ILcdMS2525bCoded.sLocationLabel, "Location"),
  sSpeedLabel(ILcdAPP6ACoded.sSpeedLabel, ILcdMS2525bCoded.sSpeedLabel, "10"),
  sSpecialHeadquarters(ILcdAPP6ACoded.sSpecialHeadquarters, ILcdMS2525bCoded.sSpecialHeadquarters, "SHQ"),
  sPlatformType(ILcdAPP6ACoded.sPlatformType, ILcdMS2525bCoded.sPlatformType, "ELNOT"),
  sTeardownTime(ILcdAPP6ACoded.sTeardownTime, ILcdMS2525bCoded.sTeardownTime, "TearDownTime"),
  sCommonIdentifier(ILcdAPP6ACoded.sCommonIdentifier, ILcdMS2525bCoded.sCommonIdentifier, "Hawk"),
  sEffectiveTime(ILcdAPP6ACoded.sEffectiveTime, ILcdMS2525bCoded.sEffectiveTime, "W1"),
  sMovementDirection(ILcdAPP6ACoded.sMovementDirection, ILcdMS2525bCoded.sMovementDirection, "20"),
  sCapacity(ILcdAPP6ACoded.sCapacity, null, "Cap"),
  sCountry(ILcdAPP6ACoded.sCountry, null, "CAN"),
  sHeadquartersElement(ILcdAPP6ACoded.sHeadquartersElement, null, "TOC"),
  sInstallationComposition(ILcdAPP6ACoded.sInstallationComposition, null, "Research"),
  sPositionAndMovement(ILcdAPP6ACoded.sPositionAndMovement, null, "Position and Movement"),
  sTrackNumber(ILcdAPP6ACoded.sTrackNumber, null, "TN456"),
  sName(ILcdAPP6ACoded.sName, null, "Track Name");

  private final String fAPP6TextModifier;
  private final String fMS2525TextModifier;
  private final String fExampleValue;

  TextModifier(String aAPP6TextModifier, String aMS2525TextModifier, String aExampleValue) {
    fAPP6TextModifier = aAPP6TextModifier;
    fMS2525TextModifier = aMS2525TextModifier;
    fExampleValue = aExampleValue;
  }

  public void putTextModifier(Object aSymbol) {
    String textModifierName = getTextModifierName(aSymbol);
    Collection<MilitarySymbolFacade.Modifier> possibleModifiers = MilitarySymbolFacade.getPossibleTextModifiers(aSymbol);
    for (MilitarySymbolFacade.Modifier modifier : possibleModifiers) {
      if (modifier.getName().equals(textModifierName)) {
        MilitarySymbolFacade.setModifierValue(aSymbol, modifier, fExampleValue);
      }
    }
  }

  private String getTextModifierName(Object aSymbol) {
    return aSymbol instanceof ILcdMS2525bCoded ? fMS2525TextModifier : fAPP6TextModifier;
  }
}
