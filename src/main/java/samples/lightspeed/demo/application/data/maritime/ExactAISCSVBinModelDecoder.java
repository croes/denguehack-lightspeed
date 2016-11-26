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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.reference.TLcdGeodeticReference;

/**
 * Reads binary ExactAIS data.
 *
 * @see ExactAISCSVModelDecoder
 * @see CSVToBinConverter
 */
public class ExactAISCSVBinModelDecoder extends ExactAISModelDecoder {

  public ExactAISCSVBinModelDecoder() {
    super();
  }

  public ExactAISCSVBinModelDecoder(ILcdInputStreamFactory aInputStreamFactory) {
    super(aInputStreamFactory);
  }

  @Override
  public String getDisplayName() {
    return "CSV";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.toLowerCase().endsWith(".csv.bin");
  }

  @Override
  public void stream(String aSourceName, Callback aCallback, final ExecutorService aExecutorService) throws IOException {
    long lines = 0;
    try (DataInputStream in = new DataInputStream(new BufferedInputStream(getInputStreamFactory().createInputStream(aSourceName), 1024 * 1024 * 8))) {
      aCallback.modelReference(new TLcdGeodeticReference());

      long tmin = Long.MAX_VALUE;
      long tmax = Long.MIN_VALUE;

      try {
        while (true) {
          lines++;

          int msgID = in.readInt();

          if (msgID == SHIP_DESCRIPTOR_MESSAGE_ID) {
            int id = in.readInt();
            int nameLength = in.readInt();
            byte[] nameBytes = new byte[nameLength];
            in.readFully(nameBytes);
            String name = new String(nameBytes);

            int callSignLength = in.readInt();
            byte[] callSignBytes = new byte[callSignLength];
            in.readFully(callSignBytes);
            String callSign = new String(callSignBytes);

            int destLength = in.readInt();
            byte[] destBytes = new byte[destLength];
            in.readFully(destBytes);
            String dest = new String(destBytes);

            int shipType = in.readInt();

            int length = in.readInt();
            int width = in.readInt();
            int draught = in.readInt();
            aCallback.shipDescriptor(new ExactAISModelDescriptor.ShipDescriptor(
                id, name, callSign, shipType, dest, length, width, draught / 10.0
            ));
          } else {
            int id = in.readInt();
            int status = in.readInt();
            long timestamp = in.readLong();
            double lon = in.readDouble();
            double lat = in.readDouble();
            double heading = in.readDouble();

            tmin = Math.min(tmin, timestamp);
            tmax = Math.max(tmax, timestamp);

            AISPlot plot = new AISPlot(
                lon, lat, heading,
                timestamp, id,
                status
            );

            aCallback.plot(msgID, plot);
          }
        }
      } catch (EOFException e) {
        // eof
      }
      aCallback.timeRange(tmin, tmax);
    }
  }
}
