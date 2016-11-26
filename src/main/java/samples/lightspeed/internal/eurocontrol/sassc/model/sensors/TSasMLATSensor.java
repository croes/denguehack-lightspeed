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
package samples.lightspeed.internal.eurocontrol.sassc.model.sensors;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 */
public class TSasMLATSensor extends ASasSensor {

  private static HashMap<Integer, TSasMLATSensor> sMLATSensors = new HashMap<Integer, TSasMLATSensor>();

  public TSasMLATSensor(int aSensorId, double aLon, double aLat, String aname, String ashortName) {
    super(aSensorId, aLon, aLat, aname, ashortName);
  }

  public static int load(ObjectInputStream aInputStream, int aNumberOfSensor, boolean aClear)
      throws IOException, ClassNotFoundException {
    if (aClear) {
      clear();
    }

    int totalSensor = aInputStream.readInt() + aNumberOfSensor;
    for (int i = aNumberOfSensor; i < totalSensor; i++) {
      int sensorId = aInputStream.readInt();
      float latitude = aInputStream.readFloat();
      float longitude = aInputStream.readFloat();
      aInputStream.skipBytes(16);
      String name = (String) aInputStream.readObject();
      String shortName = (String) aInputStream.readObject();
      addMLATSensor(new TSasMLATSensor(sensorId, longitude, latitude, name, shortName));
    }
    return totalSensor;
  }

  public static void clear() {
    sMLATSensors.clear();
  }

  public static void addMLATSensor(TSasMLATSensor aSensor) {
    sMLATSensors.put(aSensor.getSensorId(), aSensor);
  }

  public static TSasMLATSensor getMLATSensor(int aId) {
    return sMLATSensors.get(aId);
  }

  public static Collection<Integer> getMLATSensorIds() {
    return Collections.unmodifiableCollection(sMLATSensors.keySet());
  }

  public static Collection<TSasMLATSensor> getAllMLATSensors() {
    return Collections.unmodifiableCollection(sMLATSensors.values());
  }
}
