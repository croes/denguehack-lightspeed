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
public class TSasTracker extends ASasSensor {

  private static HashMap<Integer, TSasTracker> sTrackers = new HashMap<Integer, TSasTracker>();

  public TSasTracker(int aSensorId, double aLon, double aLat, String aname, String ashortName) {
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
      String name = (String) aInputStream.readObject();
      float latitude = aInputStream.readFloat();
      float longitude = aInputStream.readFloat();
      String shortname = (String) aInputStream.readObject();
      aInputStream.skipBytes(4);
      addTracker(new TSasTracker(sensorId, longitude, latitude, name, shortname));
    }
    return totalSensor;
  }

  public static void clear() {
    sTrackers.clear();
  }

  public static void addTracker(TSasTracker aTracker) {
    sTrackers.put(aTracker.getSensorId(), aTracker);
  }

  public static TSasTracker getTracker(int aId) {
    return sTrackers.get(aId);
  }

  public static Collection<Integer> getTrackerIds() {
    return Collections.unmodifiableCollection(sTrackers.keySet());
  }

  public static Collection<TSasTracker> getAllTrackers() {
    return Collections.unmodifiableCollection(sTrackers.values());
  }
}
