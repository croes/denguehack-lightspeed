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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import samples.lightspeed.internal.eurocontrol.sassc.TSasEnvironmentFactory;
import samples.lightspeed.internal.eurocontrol.sassc.TSasParameters;

/**
 *
 */
public class TSasSensorLoader {

  public static void loadAll(boolean aClear) {
    int nrOfSensor = 0;
    String directory = getParameters().getCacheDirectory() + File.separator + "sensor";
    ObjectInputStream inputStream = null;
    try {
      inputStream = new ObjectInputStream(
          new BufferedInputStream(
              new FileInputStream(directory))
      );
      nrOfSensor = TSasRadar.load(inputStream, nrOfSensor, aClear);
      nrOfSensor = TSasTracker.load(inputStream, nrOfSensor, aClear);
      nrOfSensor = TSasADSSensor.load(inputStream, nrOfSensor, aClear);
      nrOfSensor = TSasMLATSensor.load(inputStream, nrOfSensor, aClear);
    } catch (Exception aException) {
      aException.printStackTrace();
      //TODO: add proper exception handling here
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
        //Nothing to do
      }
    }
  }

  public static void clearAll() {
    TSasRadar.clear();
    TSasTracker.clear();
    TSasADSSensor.clear();
    TSasMLATSensor.clear();
  }

  private static TSasParameters getParameters() {
    return TSasEnvironmentFactory.getInstance().getEnvironment().getParameters();
  }
}
