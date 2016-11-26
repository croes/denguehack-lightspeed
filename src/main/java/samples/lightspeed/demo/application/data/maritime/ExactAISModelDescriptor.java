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
package samples.lightspeed.demo.application.data.maritime;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.luciad.model.TLcdModelDescriptor;

/**
 * Model descriptor for Exact AIS models.
 *
 * @see ExactAISCSVModelDecoder
 * @see ExactAISCSVBinModelDecoder
 */
public class ExactAISModelDescriptor extends TLcdModelDescriptor {

  private long fMinTime, fMaxTime;
  private Map<Integer, ShipDescriptor> fShipDescriptors = new HashMap<Integer, ShipDescriptor>();

  public ExactAISModelDescriptor(String aSourceName) {
    super(aSourceName, "CSV", new File(aSourceName).getName());
  }

  public long getMinTime() {
    return fMinTime;
  }

  public void setMinTime(long aMinTime) {
    fMinTime = aMinTime;
  }

  public long getMaxTime() {
    return fMaxTime;
  }

  public void setMaxTime(long aMaxTime) {
    fMaxTime = aMaxTime;
  }

  public void putShipDescriptor(ShipDescriptor aShipDescriptor) {
    fShipDescriptors.put(aShipDescriptor.getMMSI(), aShipDescriptor);
  }

  public ShipDescriptor getShipDescriptor(int aMMSI) {
    return fShipDescriptors.get(aMMSI);
  }

  public Collection<ShipDescriptor> getShipDescriptors() {
    return fShipDescriptors.values();
  }

  public static class ShipDescriptor {
    private final int fMMSI;
    private final String fVesselName;
    private final String fCallSign;
    private final int fShipType;
    private final String fDestination;
    private final int fLength, fWidth;
    private final double fDraught;

    public ShipDescriptor(int aMMSI, String aVesselName, String aCallSign, int aShipType, String aDestination, int aLength, int aWidth, double aDraught) {
      fMMSI = aMMSI;
      fVesselName = aVesselName;
      fCallSign = aCallSign;
      fShipType = aShipType;
      fDestination = aDestination;
      fLength = aLength;
      fWidth = aWidth;
      fDraught = aDraught;
    }

    public int getMMSI() {
      return fMMSI;
    }

    public String getVesselName() {
      return fVesselName;
    }

    public String getCallSign() {
      return fCallSign;
    }

    public int getShipType() {
      return fShipType;
    }

    public String getDestination() {
      return fDestination;
    }

    public int getLength() {
      return fLength;
    }

    public int getWidth() {
      return fWidth;
    }

    /**
     * @return The draught expressed in meters. We assume that the draught in the file is expressed in 0.1 meters
     * (see http://en.wikipedia.org/wiki/Draft_(hull))
     */
    public double getDraught() {
      return fDraught;
    }

    @Override
    public boolean equals(Object aO) {
      if (this == aO) {
        return true;
      }
      if (!(aO instanceof ShipDescriptor)) {
        return false;
      }

      ShipDescriptor that = (ShipDescriptor) aO;

      return fMMSI == that.fMMSI &&
             fShipType == that.fShipType &&
             fLength == that.fLength &&
             fWidth == that.fWidth &&
             Double.compare(that.fDraught, fDraught) == 0 &&
             Objects.equals(fVesselName, that.fVesselName) &&
             Objects.equals(fCallSign, that.fCallSign) &&
             Objects.equals(fDestination, that.fDestination);
    }

    @Override
    public int hashCode() {
      return fMMSI;
    }

    @Override
    public String toString() {
      return "ShipDescriptor{" +
             "MMSI=" + fMMSI +
             ", VesselName='" + fVesselName + '\'' +
             ", CallSign='" + fCallSign + '\'' +
             ", ShipType='" + fShipType + '\'' +
             '}';
    }
  }

  /**
   * The possible values for the ship type (http://www.navcen.uscg.gov/?pageName=AISMessagesAStatic).
   */
  public static class ShipType {
    public static final int VESSEL_CATEGORY = 0;
    public static final int VESSEL_FISHING = 0;
    public static final int VESSEL_TOWING = 1;
    public static final int VESSEL_TOWING_LARGE = 2; // Towing and length of the tow exceeds 200 m or breadth exceeds 25 m
    public static final int VESSEL_DREDGING_OR_UNDERWATER = 3; // Engaged in dredging or underwater operations
    public static final int VESSEL_DIVING = 4; // Engaged in diving operations
    public static final int VESSEL_MILITARY = 5; // Engaged in military operations
    public static final int VESSEL_SAILING = 6;
    public static final int VESSEL_PLEASURE_CRAFT = 7;
    // 8 and 9 are for future use

    public static final int FUTURE_10_CATEGORY = 10; // Category 10 is for future use

    public static final int WINGED_IN_GROUND_CATEGORY = 20;
    public static final int UNNAMED_30_CATEGORY = 30;
    public static final int HIGH_SPEED_CRAFT_CATEGORY = 40;

    public static final int SPECIAL_CRAFT_CATEGORY = 50;
    public static final int SPECIAL_CRAFT_PILOT_VESSEL = 50;
    public static final int SPECIAL_CRAFT_SEARCH_AND_RESCUE_VESSEL = 51;
    public static final int SPECIAL_CRAFT_TUG = 52;
    public static final int SPECIAL_CRAFT_PORT_TENDER = 53;
    public static final int SPECIAL_CRAFT_VESSEL_WITH_ANTI_POLLUTION_FACILITIES_OR_EQUIPMENT = 54;
    public static final int SPECIAL_CRAFT_LAW_ENFORCEMENT_VESSEL = 55;
    public static final int SPECIAL_CRAFT_LOCAL_VESSEL_1 = 56;
    public static final int SPECIAL_CRAFT_LOCAL_VESSEL_2 = 57;
    public static final int SPECIAL_CRAFT_MEDICAL_TRANSPORT = 58; // As defined in the 1949 Geneva Conventions and Additional Protocols
    public static final int SPECIAL_CRAFT_MOB_83 = 59; // Ships according to RR Resolution No. 18 (Mob-83)

    public static final int PASSENGER_SHIP_CATEGORY = 60;
    public static final int CARGO_SHIP_CATEGORY = 70;
    public static final int TANKER_SHIP_CATEGORY = 80;
    public static final int OTHER_CATEGORY = 90;

    private static final Map<Integer, String> sShipCategory = new HashMap<>();
    private static final Map<Integer, String> sShipType = new HashMap<>();

    static {
      sShipCategory.put(VESSEL_CATEGORY / 10, "vessel");
      sShipCategory.put(WINGED_IN_GROUND_CATEGORY / 10, "winged in ground");
      sShipCategory.put(HIGH_SPEED_CRAFT_CATEGORY / 10, "high speed craft");
      sShipCategory.put(SPECIAL_CRAFT_CATEGORY / 10, "special craft");
      sShipCategory.put(PASSENGER_SHIP_CATEGORY / 10, "passenger ship");
      sShipCategory.put(CARGO_SHIP_CATEGORY / 10, "cargo ship");
      sShipCategory.put(TANKER_SHIP_CATEGORY / 10, "tanker");
      sShipCategory.put(OTHER_CATEGORY / 10, "other");

      sShipType.put(VESSEL_FISHING, "fishing");
      sShipType.put(VESSEL_TOWING, "towing");
      sShipType.put(VESSEL_TOWING_LARGE, "towing (large)");
      sShipType.put(VESSEL_DREDGING_OR_UNDERWATER, "engaged in dredging or underwater operations");
      sShipType.put(VESSEL_DIVING, "engaged in diving operations");
      sShipType.put(VESSEL_MILITARY, "engaged in military operations");
      sShipType.put(VESSEL_SAILING, "sailing");
      sShipType.put(VESSEL_PLEASURE_CRAFT, "pleasure craft");
      sShipType.put(SPECIAL_CRAFT_PILOT_VESSEL, "pilot vessel");
      sShipType.put(SPECIAL_CRAFT_SEARCH_AND_RESCUE_VESSEL, "search-and-rescue vessel");
      sShipType.put(SPECIAL_CRAFT_TUG, "TUG");
      sShipType.put(SPECIAL_CRAFT_PORT_TENDER, "port tender");
      sShipType.put(SPECIAL_CRAFT_VESSEL_WITH_ANTI_POLLUTION_FACILITIES_OR_EQUIPMENT, "vessel with anti-pollution facilities or equipment");
      sShipType.put(SPECIAL_CRAFT_LAW_ENFORCEMENT_VESSEL, "law enforcement vessel");
      sShipType.put(SPECIAL_CRAFT_LOCAL_VESSEL_1, "local vessel");
      sShipType.put(SPECIAL_CRAFT_LOCAL_VESSEL_2, "local vessel");
      sShipType.put(SPECIAL_CRAFT_MEDICAL_TRANSPORT, "medical transport");
      sShipType.put(SPECIAL_CRAFT_MOB_83, "ship according to RR Resolution No. 18 (Mob-83)");
    }

    public static String getString(int aShipType) {
      String cat = sShipCategory.get(aShipType / 10);
      String type = sShipType.get(aShipType);
      if (cat == null) {
        return "unknown";
      }
      if (type == null) {
        return cat;
      }
      return cat + ", " + type;
    }

    public static String getCategoryString(int aShipType) {
      String cat = sShipCategory.get(aShipType / 10);
      if (cat == null) {
        return "unknown";
      }
      return cat;
    }

    public static int getCategory(int aShipType) {
      return Math.min(aShipType / 10 * 10, OTHER_CATEGORY);
    }
  }

  /**
   * The possible values for the navigational status.
   */
  public static class NavigationalStatus {
    public static final int USING_ENGINE = 0; // under way using engine,
    public static final int ANCHORED = 1; //  at anchor
    public static final int NOT_UNDER_COMMAND = 2; // not under command
    public static final int MANEUVERABILITY_RESTRICTED = 3; // restricted maneuverability
    public static final int DRAUGHT_CONSTRAINT = 4; // constrained by her draught
    public static final int MOORED = 5; // moored
    public static final int AGROUND = 6; // aground
    public static final int FISHING = 7; // engaged in fishing
    public static final int SAILING = 8; // under way sailing
    // 9 is reserved for future amendment of navigational status for ships carrying DG, HS, or MP, or IMO hazard or pollutant category C, high speed craft (HSC), 10 = reserved for future amendment of navigational status for ships carrying dangerous goods (DG), harmful substances (HS) or marine pollutants (MP), or IMO hazard or pollutant category A, wing in grand (WIG);
    // 11-13 are reserved for future use,
    public static final int AIS_SART = 14; // AIS-SART (active)
    public static final int NOT_DEFINED = 15; // not defined = default (also used by AIS-SART under test)

    private static final Map<Integer, String> sNavigationalStatus = new HashMap<Integer, String>();

    static {
      sNavigationalStatus.put(USING_ENGINE, "under way using engine");
      sNavigationalStatus.put(ANCHORED, "at anchor");
      sNavigationalStatus.put(NOT_UNDER_COMMAND, "not under command");
      sNavigationalStatus.put(MANEUVERABILITY_RESTRICTED, "restricted maneuverability");
      sNavigationalStatus.put(DRAUGHT_CONSTRAINT, "constrained by her draught");
      sNavigationalStatus.put(MOORED, "moored");
      sNavigationalStatus.put(AGROUND, "aground");
      sNavigationalStatus.put(FISHING, "engaged in fishing");
      sNavigationalStatus.put(SAILING, "under way sailing");
      //sNavigationalStatus.put(9, "reserved for future amendment of navigational status for ships carrying DG HS or MP or IMO hazard or pollutant category C high speed craft (HSC)");
      //sNavigationalStatus.put(10, "sNavigationalStatus.put(, "reserved for future amendment of navigational status for ships carrying dangerous goods (DG) harmful substances (HS) or marine pollutants (MP) or IMO hazard or pollutant category A wing in grand (WIG);");
      //sNavigationalStatus.put(11, "sNavigationalStatus.put(-sNavigationalStatus.put(sNavigationalStatus.put(, "reserved for future use");
      //sNavigationalStatus.put(AIS_SART, "sNavigationalStatus.put(, "AIS-SART (active)");
      //sNavigationalStatus.put(NOT_DEFINED, "sNavigationalStatus.put(, "not defined, "default (also used by AIS-SART under test)");
    }

    public static String getString(int aNavigationalStatus) {
      String result = sNavigationalStatus.get(aNavigationalStatus);
      if (result == null) {
        result = "unknown";
      }
      return result;
    }
  }

  /**
   * See https://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity
   */
  public enum MMSICategory {
    COAST_STATION(0, false), // Ship group, coast station, or group of coast stations
    SAR_AIRCRAFT(1, false), // For use by SAR aircraft (111MIDaxx)[note 1][2]
    // MMSI's used by individual ships, beginning with an MID:
    SHIP_EUROPE(2, true), // Europe (e.g., Italy has MID 247; Denmark has MIDs 219 and 220)
    SHIP_NORTH_AMERICA(3, true), // North and Central America and Caribbean (e.g., Canada, 316; Panama, 351, 352, 353, 354, 355, 356, 357, 370, 371, 372, and 373)
    SHIP_ASIA(4, true), // Asia (e.g., PRC, 412, 413, and 414; Maldives, 455)
    SHIP_OCEANIA(5, true), // Oceania (Australia, 503)
    SHIP_AFRICA(6, true), // 6 Africa (Eritrea, 625)
    SHIP_SOUTH_AMERICA(7, true), // 7 South America (Peru, 760)
    VHF(8, false), // Handheld VHF transceiver with DSC and GNSS[3]
    OTHER_DEVICE(9, false); // Devices using a free-form number identity:[2]
    // Search and Rescue Transponders (970yyzzzz)[note 2][4][note 3][5]
    // Man overboard DSC and/or AIS devices (972yyzzzz)[note 2]
    //     406 MHz EPIRBs fitted with an AIS transmitter (974yyzzzz)[note 2]
    // craft associated with a parent ship (98MIDxxxx)[note 4]
    // navigational aids (AtoNs; 99MIDaxxx)[note 5]

    private final int fId;
    private final boolean fShip;

    MMSICategory(int id, boolean aShip) {
      fId = id;
      fShip = aShip;
    }

    public static MMSICategory fromMMSI(int mmsi) {
      int firstDigit = mmsi;
      while (firstDigit > 9) {
        firstDigit /= 10;
      }
      return MMSICategory.values()[firstDigit];
    }

    public boolean isShip() {
      return fShip;
    }
  }
}
