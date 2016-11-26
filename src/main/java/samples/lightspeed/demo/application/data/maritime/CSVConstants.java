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

/**
 *
 */
public class CSVConstants {
  public static final String MSG_TYPE = "Message_ID";
  // For plot records
  public static final String POS_X = "Longitude";
  public static final String POS_Y = "Latitude";
  public static final String HEADING = "COG";
  public static final String ID = "MMSI";
  public static final String TIME_SEC = "Time";
  public static final String TIME_MSEC = "Millisecond";
  public static final String NAV_STATUS = "Navigational_status";
  // For "ship info" records
  public static final String VESSEL_NAME = "Vessel_Name";
  public static final String CALL_SIGN = "Call_sign";
  public static final String SHIP_TYPE = "Ship_Type";
  public static final String DESTINATION = "Destination";
  public static final String DIMENSION_TO_BOW = "Dimension_to_Bow";
  public static final String DIMENSION_TO_STERN = "Dimension_to_stern";
  public static final String DIMENSION_TO_PORT = "Dimension_to_port";
  public static final String DIMENSION_TO_STARBOARD = "Dimension_to_starboard";
  public static final String DRAUGHT = "Draught";
  public static final int SHIP_DESCRIPTOR_MESSAGE_ID = 5;
}
