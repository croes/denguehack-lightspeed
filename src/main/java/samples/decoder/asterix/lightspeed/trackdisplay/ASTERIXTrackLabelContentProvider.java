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
package samples.decoder.asterix.lightspeed.trackdisplay;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.asterix.TLcdASTERIXDataTypes;
import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.util.TLcdSpeedUnit;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103Measure;

/**
 * Provides the content for TLspAsterixTrack objects, either regular or in a highlighted state.
 */
class ASTERIXTrackLabelContentProvider {

  private static final String EMPTY_VALUE = "-";

  private static final TLcdSpeedUnit GROUND_SPEED_UNIT = TLcdSpeedUnit.KmH;

  /**
   * Provides the label content for the given track. Currently supported are cat 21 and cat 62 tracks. More can be added.
   * At this moment, this method assumes that for {@code aHighlighted == false}, 2 Strings are returned, and for
   * {@code aHighlighted == true}, 3 Strings are returned.
   * @param aTrack       the track for which to provide content.
   * @param aExtraInfo if the track is currently highlighted or not.
   * @return the label content for the given track
   */
  public static String[] provideContent(TLcdASTERIXTrack aTrack, boolean aExtraInfo) {
    if (aTrack.getDataType() == TLcdASTERIXDataTypes.Category21TrackType) {
      Integer targetAddress = (Integer) aTrack.getValue("TargetAddress");
      String label1 = targetAddress == null ? EMPTY_VALUE : Integer.toHexString(targetAddress).toUpperCase();

      Object verticalRate = getPropertyValue(aTrack, "BarometricVerticalRate");
      String climbDescendString = getClimbDescendStringCat21(verticalRate);
      Object flightLevel = getPropertyValue(aTrack, "FlightLevel");
      String label2 = getFlightLevelString(flightLevel) + climbDescendString;

      Object groundVector = getPropertyValue(aTrack, "AirborneGroundVector");
      String label3 = getGroundSpeedStringCat21(groundVector);

      if (!aExtraInfo) {
        return new String[] {label1, label2, label3};
      }
      Object mode3AReply = getPropertyValue(aTrack, "Mode3AReply");
      String label4 = mode3AReply == null ? EMPTY_VALUE : mode3AReply.toString();
      return new String[]{label1, label2, label3, label4};
    } else if (aTrack.getDataType() == TLcdASTERIXDataTypes.Category62TrackType) {
      Object trackNumber = getPropertyValue(aTrack, "TrackNumber");
      String label1 = trackNumber == null ? EMPTY_VALUE : trackNumber.toString();

      Object modeOfMovement = getPropertyValue(aTrack, "ModeOfMovement");
      String climbDescendString = getClimbDescendStringCat62(modeOfMovement);
      Object flightLevel = getPropertyValue(aTrack, "MeasuredFlightLevel");
      String label2 = getFlightLevelString(flightLevel) + climbDescendString;

      Object velocity = getPropertyValue(aTrack, "CalculatedTrackVelocityCartesian");
      String label3 = getGroundSpeedStringCat62(velocity);

      if (!aExtraInfo) {
        return new String[] {label1, label2, label3};
      }
      Object trackMode3ACode = getPropertyValue(aTrack, "TrackMode3ACode");
      String label4 = getTrackMode3ACodeString(trackMode3ACode);
      return new String[]{label1, label2, label3, label4};
    } else {
      throw new IllegalArgumentException("Unknown category, implementation can be added in ASTERIXTrackLabelRegular#configureComponent");
    }
  }

  private static Object getPropertyValue(TLcdASTERIXTrack aTrack, String aPropertyName) {
    Object mode3AReply = aTrack.getValue(aPropertyName);
    if (mode3AReply == null) {
      TLcdASTERIXTrack newTrack = new TLcdASTERIXTrack(aTrack.getTrajectory());
      for (int i = aTrack.getTrajectory().getPointCount() - 1; i > 0; i--) {
        newTrack.updateForIndex(i);
        mode3AReply = newTrack.getValue(aPropertyName);
        if (mode3AReply != null) {
          break;
        }
      }
    }
    return mode3AReply;
  }
  private static String getFlightLevelString(Object aFlightLevel) {
    if (aFlightLevel == null) {
      return EMPTY_VALUE;
    }
    if (aFlightLevel instanceof ILcdISO19103Measure) {
      ILcdISO19103Measure measure = (ILcdISO19103Measure) aFlightLevel;
      return "FL " + Integer.toString((int) measure.getValue());
    } else if (aFlightLevel instanceof Number) {
      Number fl = (Number) aFlightLevel;
      return "FL " + fl.intValue();
    } else {
      return aFlightLevel.toString();
    }
  }

  private static String getTrackMode3ACodeString(Object aCode) {
    if (aCode == null) {
      return EMPTY_VALUE;
    }
    if (!(aCode instanceof ILcdDataObject)) {
      return aCode.toString();
    }
    ILcdDataObject dataObject = (ILcdDataObject) aCode;
    Object reply = dataObject.getValue("Mode3AReply");
    return reply == null ? EMPTY_VALUE : reply.toString();
  }

  private static String getClimbDescendStringCat21(Object aVerticalRate) {
    if (aVerticalRate == null) {
      return "";
    }
    if (!(aVerticalRate instanceof ILcdDataObject)) {
      return "";
    }
    ILcdDataObject dataObject = (ILcdDataObject) aVerticalRate;
    ILcdISO19103Measure verticalRate = (ILcdISO19103Measure) dataObject.getValue("BarometricVerticalRate");
    if (verticalRate == null) {
      return "";
    }
    if (verticalRate.getValue() > 0) {
      return " \u2191";
    } else if (verticalRate.getValue() < 0) {
      return " \u2193";
    } else {
      return "";
    }
  }

  private static String getClimbDescendStringCat62(Object aCode) {
    if (aCode == null) {
      return "";
    }
    if (!(aCode instanceof ILcdDataObject)) {
      return "";
    }
    ILcdDataObject dataObject = (ILcdDataObject) aCode;
    Object verticalRate = dataObject.getValue("VerticalRate");
    if (verticalRate == null) {
      return "";
    }
    if ("climb".equalsIgnoreCase(verticalRate.toString())) {
      return " \u2191";
    } else if ("descent".equalsIgnoreCase(verticalRate.toString())) {
      return " \u2193";
    } else {
      return "";
    }
  }

  private static String getGroundSpeedStringCat21(Object aGroundVector) {
    if (aGroundVector == null || !(aGroundVector instanceof ILcdDataObject)) {
      return EMPTY_VALUE;
    }
    ILcdDataObject dataObject = (ILcdDataObject) aGroundVector;
    Object speedValue = dataObject.getValue("GroundSpeed"); // Expressed in NM/s
    if (!(speedValue instanceof TLcdISO19103Measure)) {
      return EMPTY_VALUE;
    }
    TLcdISO19103Measure speed = (TLcdISO19103Measure) speedValue;
    TLcdISO19103Measure speedKmH = new TLcdISO19103Measure(0.0, GROUND_SPEED_UNIT);
    speed.convert(GROUND_SPEED_UNIT, speedKmH);
    return "G" + (int)speedKmH.getValue();
  }

  private static String getGroundSpeedStringCat62(Object aVelocity) {
    if (aVelocity == null || !(aVelocity instanceof ILcdDataObject)) {
      return EMPTY_VALUE;
    }
    ILcdDataObject dataObject = (ILcdDataObject) aVelocity;
    Object vxValue = dataObject.getValue("Vx"); // Expressed in m/s
    Object vyValue = dataObject.getValue("Vy"); // Expressed in m/s
    if (!(vxValue instanceof TLcdISO19103Measure) || !(vyValue instanceof TLcdISO19103Measure)) {
      return EMPTY_VALUE;
    }
    TLcdISO19103Measure vx = (TLcdISO19103Measure) vxValue;
    TLcdISO19103Measure vy = (TLcdISO19103Measure) vyValue;
    TLcdISO19103Measure vxKmH = new TLcdISO19103Measure(0.0, GROUND_SPEED_UNIT);
    vx.convert(GROUND_SPEED_UNIT, vxKmH);
    TLcdISO19103Measure vyKmH = new TLcdISO19103Measure(0.0, GROUND_SPEED_UNIT);
    vy.convert(GROUND_SPEED_UNIT, vyKmH);
    int velocity = (int)Math.sqrt(vxKmH.doubleValue() * vxKmH.doubleValue() + vyKmH.doubleValue() * vyKmH.doubleValue());
    return "G" + velocity;
  }
}
