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
package samples.xml.customdecodingencoding;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedDecoder;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedEncoder;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLSchemaTypeMappingAnnotation;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

import samples.xml.customdomainclasses.Address;
import samples.xml.customdomainclasses.CustomDomainClassesDataTypes;
import samples.xml.customdomainclasses.DataObjectPrintStream;
import samples.xml.customdomainclasses.Model;

public class Main {


  public static void run(DataObjectPrintStream ps) throws XMLStreamException, IOException {
    Model m = createModel();
    ps.println("------------ original model -----------");
    ps.printDataObject(m);
    ps.println("------------ encoded as an XML document -----------");
    byte[] encoded = encode(m);
    ps.println(new String(encoded));
    ps.println("------------ decoded model -----------");
    Model decodedModel = (Model) decode(encoded);
    ps.println("Decoded model with " + decodedModel.getFeatures().size() + " elements.");
    ps.printDataObject(decodedModel);
  }

  private static Object decode(final byte[] aEncoded) throws XMLStreamException, IOException {
    TLcdXMLSchemaBasedDecoder decoder = new TLcdXMLSchemaBasedDecoder();
    decoder.configure(CustomDecodingEncodingDataTypes.getDataModel());
    decoder.setInputStreamFactory(new TLcdInputStreamFactory() {

      @Override
      public InputStream createInputStream(String aSourceName)
          throws IOException {
        if (aSourceName.equals("test")) {
          return new ByteArrayInputStream(aEncoded);
        }
        return super.createInputStream(aSourceName);
      }

    });
    return decoder.decode("test");
  }

  private static byte[] encode(Model aModel) throws XMLStreamException, IOException {
    TLcdXMLSchemaBasedEncoder encoder = new TLcdXMLSchemaBasedEncoder();
    encoder.configure(CustomDecodingEncodingDataTypes.getDataModel());
    // ensures the name space of the sample data types is the default name space
    encoder.registerNamespaceURI(CustomDomainClassesDataTypes.getDataModel().getAnnotation(TLcdXMLSchemaTypeMappingAnnotation.class).getNamespaceURI(), "", null);
    encoder.registerNamespaceURI(CustomDecodingEncodingDataTypes.getDataModel().getAnnotation(TLcdXMLSchemaTypeMappingAnnotation.class).getNamespaceURI(), "cust", null);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    encoder.encode(aModel, bos);
    return bos.toByteArray();
  }

  private static Model createModel() {
    Model result = new Model();
    Address a = new Address();
    a.setCity("Washington DC");
    a.setNumber(2200);
    a.setStreet("Pennsylvania Ave NW");
    result.getFeatures().add(a);
    ExtendedAddress awl = new ExtendedAddress();
    awl.setCity("Washington DC");
    awl.setNumber(11);
    awl.setStreet("Gaston Geenslaan");
    awl.setColor(Color.green);
    awl.setLocation(new TLcdLonLatPoint(50, 4));
    result.getFeatures().add(awl);
    return result;
  }
}
