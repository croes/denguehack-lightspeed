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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import com.luciad.earth.repository.codec.ILcdEarthTileDataCodec;
import com.luciad.earth.tileset.TLcdEarthTileFormat;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;

/**
 * @author tomn
 * @since 2012.1
 */
public class EarthTileCodec implements ILcdEarthTileDataCodec {

  public static final TLcdEarthTileFormat DEC_FORMAT = new TLcdEarthTileFormat(
      ILcd2DBoundsIndexedModel.class
  );
  public static final TLcdEarthTileFormat ENC_FORMAT = new TLcdEarthTileFormat(
      "ExactAIS"
  );

  static final TLcdGeodeticReference MODEL_REFERENCE = new TLcdGeodeticReference();

  @Override
  public Object decodeTileData(InputStream aSource) throws IOException {
    DataInputStream dis = new DataInputStream(aSource);

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(MODEL_REFERENCE);

    long tmin = Long.MAX_VALUE;
    long tmax = Long.MIN_VALUE;

    int counter = dis.readInt();
    for (int i = 0; i < counter; i++) {
      int id = dis.readInt();
      int navigationalStatus = dis.readInt();
      long timeStamp = dis.readLong();
      double x = dis.readDouble();
      double y = dis.readDouble();
      double orientation = dis.readDouble();

      AISPlot plot = new AISPlot(
          x, y, orientation, timeStamp, id, navigationalStatus
      );
      model.addElement(plot, ILcdModel.NO_EVENT);

      tmin = Math.min(tmin, timeStamp);
      tmax = Math.max(tmax, timeStamp);
    }

    ExactAISModelDescriptor desc = new ExactAISModelDescriptor("ExactAIS");
    desc.setMinTime(tmin);
    desc.setMinTime(tmax);
    model.setModelDescriptor(desc);

    return model;
  }

  @Override
  public void encodeTileData(Object aTileData, OutputStream aDestination) throws IOException {
    ILcd2DBoundsIndexedModel model = (ILcd2DBoundsIndexedModel) aTileData;

    DataOutputStream dos = new DataOutputStream(aDestination);

    int counter = 0;
    Enumeration count = model.elements();
    while (count.hasMoreElements()) {
      count.nextElement();
      counter++;
    }
    dos.writeInt(counter);

    Enumeration plots = model.elements();
    while (plots.hasMoreElements()) {
      AISPlot plot = (AISPlot) plots.nextElement();

      int id = plot.getID();
      int navigationalStatus = plot.getNavigationalStatus();
      long timeStamp = plot.getTimeStamp();
      double x = plot.getX();
      double y = plot.getY();
      double orientation = plot.getOrientation();

      dos.writeInt(id);
      dos.writeInt(navigationalStatus);
      dos.writeLong(timeStamp);
      dos.writeDouble(x);
      dos.writeDouble(y);
      dos.writeDouble(orientation);
    }

    dos.flush();
  }

  @Override
  public TLcdEarthTileFormat getDecodedTileFormat() {
    return DEC_FORMAT;
  }

  @Override
  public TLcdEarthTileFormat getEncodedDataFormat() {
    return ENC_FORMAT;
  }
}
