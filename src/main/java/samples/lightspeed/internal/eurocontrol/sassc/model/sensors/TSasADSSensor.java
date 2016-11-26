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
public class TSasADSSensor extends ASasSensor {

  private static HashMap<Integer, TSasADSSensor> sADSSensors = new HashMap<Integer, TSasADSSensor>();

  public TSasADSSensor(int aSensorId, double aLon, double aLat, String aname, String ashortName) {
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
      aInputStream.skipBytes(4);
      String name = (String) aInputStream.readObject();
      String shortName = (String) aInputStream.readObject();
      addADSSensor(new TSasADSSensor(sensorId, longitude, latitude, name, shortName));
    }
    return totalSensor;
  }

  public static void clear() {
    sADSSensors.clear();
  }

  public static void addADSSensor(TSasADSSensor aSensor) {
    sADSSensors.put(aSensor.getSensorId(), aSensor);
  }

  public static TSasADSSensor getADSSensor(int aId) {
    return sADSSensors.get(aId);
  }

  public static Collection<Integer> getADSSensorIds() {
    return Collections.unmodifiableCollection(sADSSensors.keySet());
  }

  public static Collection<TSasADSSensor> getAllADSSensors() {
    return Collections.unmodifiableCollection(sADSSensors.values());
  }

}
