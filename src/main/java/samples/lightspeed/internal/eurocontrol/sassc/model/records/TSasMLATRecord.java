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

import com.luciad.util.TLcdDistanceUnit;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasFloatRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasIntRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasLongRecordColumn;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasRecordColumnFactory;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns.TSasStringRecordColumn;

/**
 *
 */
public class TSasMLATRecord extends ASasRecord {
  private static List<TSasMLATRecord> sMLATRecords = new ArrayList<TSasMLATRecord>();
  private static final TSasRecordColumnFactory RECORD_COLUMN_FACTORY = new TSasRecordColumnFactory();

  public TSasMLATRecord(double aLon,
                        double aLat,
                        double aHeight,
                        int aDataSourceId,
                        int aReportType,
                        int aMode3ACode,
                        long aTimeOfDetection,
                        String aCallsign) {
    super(aLon, aLat, aHeight, aDataSourceId, aReportType, aMode3ACode, aTimeOfDetection, aCallsign);
  }

  public static void load(Map<String, TSasDbExpressionDefinition> aDefinitions, boolean aClear) {
    if (aClear) {
      clear();
    }

    TSasFloatRecordColumn longitude = (TSasFloatRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.POS_LONG_DEG"));
    TSasFloatRecordColumn latitude = (TSasFloatRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.POS_LAT_DEG"));
    TSasIntRecordColumn altBaro = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.ALT_GEO_FT"));
    TSasIntRecordColumn dataSourceId = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.DS_ID"));
    TSasIntRecordColumn reportType = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.REPORT_TYPE"));
    TSasIntRecordColumn mode3A = (TSasIntRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.MODE3A_CODE"));
    TSasLongRecordColumn timeOfDetection = (TSasLongRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.TOD"));
    TSasStringRecordColumn callSign = (TSasStringRecordColumn)
        RECORD_COLUMN_FACTORY.createAndLoadRecordColumn(aDefinitions.get("SD_MLAT.CALLSIGN"));

    for (int i = 0; i < longitude.getSize(); i++) {
      addMLATRecord(new TSasMLATRecord(longitude.getValue(i),
                                       latitude.getValue(i),
                                       TLcdDistanceUnit.FT_UNIT.convertToStandard(altBaro.getValue(i)),
                                       dataSourceId.getValue(i),
                                       reportType.getValue(i),
                                       mode3A.getValue(i),
                                       timeOfDetection.getValue(i),
                                       (String) callSign.getObject(i)));

    }

  }

  public static void clear() {
    sMLATRecords.clear();
  }

  public static void addMLATRecord(TSasMLATRecord aMLATRecord) {
    sMLATRecords.add(aMLATRecord);
  }

  public static List<TSasMLATRecord> getMLATRecords() {
    return sMLATRecords;
  }
}
