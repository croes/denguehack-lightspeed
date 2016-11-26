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
package samples.gxy.magneticnorth;

import java.awt.Component;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

import com.luciad.format.magneticnorth.TLcdIGRFModelDecoder;
import com.luciad.format.magneticnorth.TLcdWMMModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStatusListener;

public class MagneticNorthModelFactory {

  private static final String[] WMM_DATA_FILE_NAMES = {"Data/magneticnorth/WMM2005.COF", "Data/magneticnorth/WMM2010.COF", "Data/magneticnorth/WMM2015.COF"};
  private static final String WMM_DATA_URL = "http://www.ngdc.noaa.gov/geomag/WMM";
  private static final String IGRF_DATA_FILE_NAME = "Data/magneticnorth/igrf";
  private static final String IGRF_DATA_URL = "http://www.ngdc.noaa.gov/IAGA/vmod/igrf.html";

  public static ILcdModel createWMMMagneticNorthModel(String aBaseDirectory, Component aParentComponent, ILcdStatusListener aStatusListener) {
    TLcdWMMModelDecoder modelDecoder = new TLcdWMMModelDecoder();

    String files = aBaseDirectory + WMM_DATA_FILE_NAMES[0];
    for (int i = 1; i < WMM_DATA_FILE_NAMES.length; i++) {
      files += "," + aBaseDirectory + WMM_DATA_FILE_NAMES[i];
    }

    // Add an indication of progress.
    if (aStatusListener != null) {
      modelDecoder.addStatusListener(aStatusListener);
    }
    try {
      return modelDecoder.decode(files);
    } catch (IOException e) {
      // show a message, that decoding failed.
      JOptionPane.showMessageDialog(
          aParentComponent,
          new String[]{
              "Decoding the magnetic north model failed.",
              "Please check if the files \"" + files + "\" exist",
              "in the LuciadLightspeed samples directory.",
              "This file is not included with the LuciadLightspeed distribution.",
              "If the files do not exist, download them from ",
              WMM_DATA_URL,
              "and copy them to " + files + ".",
          },
          "Error loading",
          JOptionPane.WARNING_MESSAGE
                                   );
      return null;
    } finally {
      if (aStatusListener != null) {
        modelDecoder.removeStatusListener(aStatusListener);
      }
    }
  }

  public static ILcdModel createIGRFMagneticNorthModel(String aBaseDirectory, Component aParentComponent, ILcdStatusListener aStatusListener) {
    TLcdIGRFModelDecoder modelDecoder = new TLcdIGRFModelDecoder();
    // Add an indication of progress.
    if (aStatusListener != null) {
      modelDecoder.addStatusListener(aStatusListener);
    }
    try {
      //--------- Create the model ----------

      modelDecoder.setStep(1);                         // an iso line every degree
      modelDecoder.setPrecision(0.5);                  // accommodate between speed and precision
      modelDecoder.setDate(new GregorianCalendar());   // use today
      return modelDecoder.decode(aBaseDirectory + IGRF_DATA_FILE_NAME);
    } catch (IOException exception) {
      // show a message, that decoding failed.
      JOptionPane.showMessageDialog(
          aParentComponent,
          new String[]{
              "Decoding the magnetic north model failed.",
              "Please check if the file \"" + IGRF_DATA_FILE_NAME + "\" exists",
              "in the LuciadLightspeed samples directory.",
              "This file is not included with the LuciadLightspeed distribution.",
              "If the file does not exist, download it from ",
              IGRF_DATA_URL,
              "and copy it to " + IGRF_DATA_FILE_NAME + ".",
          },
          "Error loading",
          JOptionPane.WARNING_MESSAGE
                                   );
      return null;
    } finally {
      if (aStatusListener != null) {
        modelDecoder.removeStatusListener(aStatusListener);
      }
    }
  }
}
