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
package samples.lightspeed.demo.application.data.airplots;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdModelReferenceEncoder;
import com.luciad.shape.ILcdPoint;

/**
 * Utility code to convert air plots SHP file to a more compact
 * binary format.
 */
public class AirPlotsSHPToBinConverter {


  public static void shpToBin(String aSource, String aDest) throws IOException {
    final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(aDest)));

    System.out.println("Decoding");
    TLcdSHPModelDecoder modelDecoder = new TLcdSHPModelDecoder();
    long time0 = System.currentTimeMillis();
    ILcdModel model = modelDecoder.decode(aSource);
    long time1 = System.currentTimeMillis();
    System.out.println("Original decoding time: " + (time1 - time0));

    System.out.println("Writing data type");
    // First data type
    writeDataType(out, model);
    System.out.println("Writing data");
    // Then data itself
    writeData(out, model);
    TLcdModelReferenceEncoder refEncoder = new TLcdModelReferenceEncoder();
    refEncoder.save(model.getModelReference(), aDest.replace(".shp", ".ref"));

    System.out.println("Done");

    out.close();

//    // Verify with decoder
//    AirplotsBinModelDecoder decoder = new AirplotsBinModelDecoder( );
//    long time0 = System.currentTimeMillis();
//    ILcdModel model2 = decoder.decode( aDest );
//    long time1 = System.currentTimeMillis();
//    System.out.println("New decoding time: " + (time1-time0));
  }

  private static void writeData(DataOutputStream out, ILcdModel model) throws IOException {
    TLcdDataModel dataModel = ((ILcdDataModelDescriptor) model.getModelDescriptor()).getDataModel();
    TLcdDataType dataType = dataModel.getDeclaredTypes().iterator().next();
    List<TLcdDataProperty> dataProperties = dataType.getProperties();
    Enumeration e = model.elements();
    while (e.hasMoreElements()) {
      Object element = e.nextElement();
      ILcdDataObject dataObject = (ILcdDataObject) element;
      ILcdPoint geometry = (ILcdPoint) element;

      // Geometry
      out.writeDouble(geometry.getX());
      out.writeDouble(geometry.getY());
      out.writeDouble(geometry.getZ());

      for (TLcdDataProperty property : dataProperties) {
        writeProperty(out, dataObject, property);
      }
    }
  }

  private static void writeProperty(DataOutputStream out, ILcdDataObject dataObject, TLcdDataProperty property) throws IOException {
    if (property.getType() == TLcdCoreDataTypes.STRING_TYPE) {
      String value = (String) dataObject.getValue(property);
      writeString(out, value);
    } else if (property.getType() == TLcdCoreDataTypes.DOUBLE_TYPE) {
      out.writeDouble((Double) dataObject.getValue(property));
    } else if (property.getType() == TLcdCoreDataTypes.INTEGER_TYPE) {
      out.writeInt((Integer) dataObject.getValue(property));
    } else if (property.getType() == TLcdCoreDataTypes.LONG_TYPE) {
      out.writeLong((Long) dataObject.getValue(property));
    } else if (property.getType() == TLcdCoreDataTypes.SHORT_TYPE) {
      out.writeShort((Short) dataObject.getValue(property));
    } else {
      throw new UnsupportedOperationException("Not supported: " + property.getType());
    }

  }

  private static void writeDataType(DataOutputStream out, ILcdModel model) throws IOException {
    TLcdDataModel dataModel = ((ILcdDataModelDescriptor) model.getModelDescriptor()).getDataModel();
    TLcdDataType dataType = dataModel.getDeclaredTypes().iterator().next();
    out.writeInt(dataType.getProperties().size());
    for (TLcdDataProperty property : dataType.getProperties()) {
      String name = property.getName();
      writeString(out, name);
      System.out.print(name);
      if (property.getType() == TLcdCoreDataTypes.STRING_TYPE) {
        System.out.println(" string");
        out.writeChar('s');
      } else if (property.getType() == TLcdCoreDataTypes.DOUBLE_TYPE) {
        System.out.println(" double");
        out.writeChar('d');
      } else if (property.getType() == TLcdCoreDataTypes.INTEGER_TYPE) {
        System.out.println(" integer");
        out.writeChar('i');
      } else if (property.getType() == TLcdCoreDataTypes.LONG_TYPE) {
        System.out.println(" long");
        out.writeChar('l');
      } else if (property.getType() == TLcdCoreDataTypes.SHORT_TYPE) {
        System.out.println(" short");
        out.writeChar('h'); // h for half/short
      } else {
        throw new UnsupportedOperationException("Not supported: " + property.getType());
      }
    }
  }

  private static void writeString(DataOutputStream out, String string) throws IOException {
    byte[] name = string.getBytes();
    out.writeInt(name.length);
    out.write(name);
  }
}
