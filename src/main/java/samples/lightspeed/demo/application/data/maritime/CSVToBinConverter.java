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

import static samples.lightspeed.demo.application.data.maritime.CSVConstants.SHIP_DESCRIPTOR_MESSAGE_ID;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.model.ILcdModelReference;

/**
 * Converts an ExactAIS csv file to a binary format.
 *
 * @see ExactAISCSVModelDecoder
 * @see ExactAISCSVBinModelDecoder
 */
class CSVToBinConverter extends ExactAISCSVModelDecoder {

  public CSVToBinConverter() {
  }

  public CSVToBinConverter(ILcdInputStreamFactory aInputStreamFactory) {
    super(aInputStreamFactory);
  }

  public void convert(String aSourceName, String aTarget) throws IOException {
    InputStream is = getInputStreamFactory().createInputStream(aSourceName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1024 * 1024 * 8);
    final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(aTarget)));

    try {
      super.stream(aSourceName, new Callback() {
        @Override
        public void modelReference(ILcdModelReference aModelReference) {
        }

        @Override
        public void shipDescriptor(ExactAISModelDescriptor.ShipDescriptor aShipDescriptor) throws IOException {
          out.writeInt(SHIP_DESCRIPTOR_MESSAGE_ID);
          out.writeInt(aShipDescriptor.getMMSI());
          byte[] name = aShipDescriptor.getVesselName().getBytes();
          out.writeInt(name.length);
          out.write(name);
          byte[] callSign = aShipDescriptor.getCallSign().getBytes();
          out.writeInt(callSign.length);
          out.write(callSign);
          byte[] destination = aShipDescriptor.getDestination().getBytes();
          out.writeInt(destination.length);
          out.write(destination);
          out.writeInt(aShipDescriptor.getShipType());
          out.writeInt(aShipDescriptor.getLength());
          out.writeInt(aShipDescriptor.getWidth());
          out.writeInt((int) (aShipDescriptor.getDraught() * 10));//convert back to decimeters
        }

        @Override
        public void plot(int aMessageId, AISPlot aAISPlot) throws IOException {
          out.writeInt(aMessageId);
          out.writeInt(aAISPlot.getID());
          out.writeInt(aAISPlot.getNavigationalStatus());
          out.writeLong(aAISPlot.getTimeStamp());
          out.writeDouble(aAISPlot.getX());
          out.writeDouble(aAISPlot.getY());
          out.writeDouble(aAISPlot.getActualOrientation());
        }

        @Override
        public void timeRange(long aMin, long aMax) throws IOException {
        }
      }, null);
    } finally {
      out.close();
      reader.close();
    }
  }

}
