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
package samples.lightspeed.internal.eurocontrol.sassc.model.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasFloatRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasIntRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasLongRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasRecordColumnFactory;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasStringRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasRadar;

/**
 *
 */
public class TSasRadarRecord extends ASasRecord {

  private static List<TSasRadarRecord> sRadarRecords = new ArrayList<TSasRadarRecord>();
  private static final TSasRecordColumnFactory RECORD_COLUMN_FACTORY = new TSasRecordColumnFactory();

  public TSasRadarRecord(double aLon,
                         double aLat,
                         double aHeight,
                         int aDataSourceId,
                         int aReportType,
                         int aMode3ACode,
                         long aTimeOfDetection,
                         String aCallsign) {
    super(aLon, aLat, aHeight, aDataSourceId, aReportType, aMode3ACode, aTimeOfDetection, aCallsign);
  }

  private static TLcdLonLatHeightPoint convertToLonLatHeightPoint(int aDataSourceId, double aAzimuthDegree, double aRangeNM, double aModeC) {
    TSasRadar radar = TSasRadar.getRadar(aDataSourceId);
    if (radar == null) {
      return new TLcdLonLatHeightPoint(0, 0, 0);
    } else {
      double range = TLcdDistanceUnit.NM_UNIT.convertToStandard(aRangeNM);
      double azimuth = Math.toRadians(90 - aAzimuthDegree);
      double height = aModeC;
      if (aModeC > 1) {
        height = TLcdDistanceUnit.FT_UNIT.convertToStandard(aModeC);
      }
      double x = Math.cos(azimuth) * range;
      double y = Math.sin(azimuth) * range;
      TLcdXYZPoint point = (height == Long.MIN_VALUE) ? new TLcdXYZPoint(x, y, 0) : new TLcdXYZPoint(x, y, height);
      try {
        radar.radarXYH2LonLatHeightSFCT(point, point);
      } catch (TLcdOutOfBoundsException e) {
        return new TLcdLonLatHeightPoint(0, 0, 0);
      }
      if (Double.isNaN(point.getX()) || Double.isNaN(point.getY()) || Double.isNaN(point.getZ())) {
        return new TLcdLonLatHeightPoint(0, 0, 0);
      } else {
        return new TLcdLonLatHeightPoint(point.getX(), point.getY(), point.getZ());
      }
    }
  }

  public static void load(Map<String, TSasDbExpressionDefinition> aDefinitions, boolean aClear) {
    if (aClear) {
      clear();
    }

    TSasFloatRecordColumn azimuth = (TSasFloatRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.POS_AZM_DEG"));
    TSasFloatRecordColumn range = (TSasFloatRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.POS_RANGE_NM"));
    TSasLongRecordColumn modeC = (TSasLongRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.MODEC_CODE_FT"));
    TSasIntRecordColumn dataSourceId = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.DS_ID"));
    TSasIntRecordColumn reportType = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.DETECTION_TYPE"));
    TSasIntRecordColumn mode3A = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.MODE3A_CODE"));
    TSasLongRecordColumn timeOfDetection = (TSasLongRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.TOD"));
    TSasStringRecordColumn callSign = (TSasStringRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_RADAR.CALLSIGN"));

    for (int i = 0; i < azimuth.getSize(); i++) {
      TLcdLonLatHeightPoint convertedPoint = convertToLonLatHeightPoint(dataSourceId.getValue(i),
                                                                        azimuth.getValue(i),
                                                                        range.getValue(i),
                                                                        modeC.getValue(i));
      addRadarRecord(new TSasRadarRecord(convertedPoint.getLon(),
                                         convertedPoint.getLat(),
                                         convertedPoint.getZ(),
                                         dataSourceId.getValue(i),
                                         reportType.getValue(i),
                                         mode3A.getValue(i),
                                         timeOfDetection.getValue(i),
                                         (String) callSign.getObject(i)));
    }
  }

  public static void clear() {
    sRadarRecords.clear();
  }

  public static void addRadarRecord(TSasRadarRecord aRadarRecord) {
    sRadarRecords.add(aRadarRecord);
  }

  public static List<TSasRadarRecord> getRadarRecords() {
    return sRadarRecords;
  }
}
