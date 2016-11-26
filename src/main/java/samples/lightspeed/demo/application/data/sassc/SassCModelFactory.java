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
package samples.lightspeed.demo.application.data.sassc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.internal.eurocontrol.sassc.TSasEnvironment;
import samples.lightspeed.internal.eurocontrol.sassc.TSasEnvironmentFactory;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.ASasRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasADSRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasMLATRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasRadarRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasRecordLoader;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasTrackRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.ASasSensor;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasADSSensor;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasMLATSensor;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasRadar;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasSensorLoader;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.TSasTracker;

/**
 * Model factory for EuroControl SASS-C models.
 */
public class SassCModelFactory extends AbstractModelFactory {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(SassCModelFactory.class);

  public SassCModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    // Decode the data
    long startTime = System.currentTimeMillis();
    TSasEnvironmentFactory.getInstance().setEnvironment(new TSasEnvironment(IOUtil.resolveSourceName(aSource)));

    TSasSensorLoader.loadAll(true);
    TSasRecordLoader.loadAll(true);

    int recordCount = TSasRadarRecord.getRadarRecords().size() +
                      TSasTrackRecord.getTrackRecords().size() +
                      TSasADSRecord.getADSRecords().size() +
                      TSasMLATRecord.getMLATRecords().size();

    if (sLogger.isDebugEnabled()) {
      int sensorCount = TSasRadar.getAllRadars().size() +
                        TSasTracker.getAllTrackers().size() +
                        TSasADSSensor.getADSSensorIds().size() +
                        TSasMLATSensor.getMLATSensorIds().size();

      StringBuilder sb = new StringBuilder();
      sb.append("Loaded ").append(aSource).append(" in ").append(System.currentTimeMillis() - startTime).append(" ms\n");
      sb.append("    Radars: ").append(TSasRadarRecord.getRadarRecords().size()).append(" records, sensors: ").append(TSasRadar.getRadarIds()).append('\n');
      sb.append("    Trackers: ").append(TSasTrackRecord.getTrackRecords().size()).append(" records, sensors: ").append(TSasTracker.getTrackerIds()).append('\n');
      sb.append("    ADS: ").append(TSasADSRecord.getADSRecords().size()).append(" records, sensors: ").append(TSasADSSensor.getADSSensorIds()).append('\n');
      sb.append("    MLAT: ").append(TSasMLATRecord.getMLATRecords().size()).append(" records, sensors: ").append(TSasMLATSensor.getMLATSensorIds()).append('\n');
      sb.append("    Total: ").append(recordCount).append(" records, ").append(sensorCount).append(" sensors");
      sLogger.debug(sb.toString());
    }

    // Create a model
    startTime = System.currentTimeMillis();

    Vector<ASasRecord> records = new Vector<ASasRecord>(recordCount);
    records.addAll(TSasRadarRecord.getRadarRecords());
    records.addAll(TSasTrackRecord.getTrackRecords());
    records.addAll(TSasADSRecord.getADSRecords());
    records.addAll(TSasMLATRecord.getMLATRecords());

    ArrayList<ASasSensor> sensors = new ArrayList<ASasSensor>();
    sensors.addAll(TSasRadar.getAllRadars());
    sensors.addAll(TSasTracker.getAllTrackers());
    sensors.addAll(TSasADSSensor.getAllADSSensors());
    sensors.addAll(TSasMLATSensor.getAllMLATSensors());

    TSasSensorLoader.clearAll();
    TSasRecordLoader.clearAll();

    if (sLogger.isDebugEnabled()) {
      long minTime = Integer.MAX_VALUE;
      long maxTime = Integer.MIN_VALUE;
      double minHeight = Double.MAX_VALUE;
      double maxHeight = Double.MIN_VALUE;
      for (int i = 0; i < records.size(); i++) {
        ASasRecord record = records.get(i);
        minTime = Math.min(minTime, record.getTimeOfDetection());
        maxTime = Math.max(maxTime, record.getTimeOfDetection());
        minHeight = Math.min(minHeight, record.getZ());
        maxHeight = Math.max(maxHeight, record.getZ());
      }
      StringBuilder sb = new StringBuilder();
      sb.append("Data attribute ranges:\n");
      sb.append("    time: [").append(minTime).append(",").append(maxTime).append("]\n");
      sb.append("    height: [").append(minHeight).append(",").append(maxHeight).append("]\n");
      sb.append("    sensor: [0,").append(sensors.size() - 1).append("]");
      sLogger.debug(sb.toString());
    }

    SassCModelDescriptor modelDescriptor = new SassCModelDescriptor(aSource, Collections.unmodifiableList(sensors));
    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        modelDescriptor
    );

    model.addElements(records, ILcdModel.NO_EVENT);

    if (sLogger.isDebugEnabled()) {
      sLogger.debug("Created model for " + aSource + " in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    return model;
  }

}
