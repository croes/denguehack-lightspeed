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
package samples.encoder.gml3;

import com.luciad.format.gml31.xml.TLcdGML31ModelEncoder;
import com.luciad.format.mif.TLcdMAPModelDecoder;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;

/**
 * This utility decodes a vector file in SHP, MIF or MAP format, and
 * encodes it in a GML3 file.
 * <p/>
 * Usage: java samples.encoder.gml3.Convertor
 * [-gmlschema     s] the location of the GML schema to be encoded in
 * the xsi:schemaLocations attribute
 * [-xmlschema     s] the location of the XML-SCHEMA schema to be encoded in
 * the xsi:schemaLocations attribute
 * [-namespace     s] the target namespace, if none is available.
 * vector_file gml3_file
 * <p/>
 */
public class Converter {

  private static final ILcdModelDecoder[] DECODERS = new ILcdModelDecoder[]{
      new TLcdSHPModelDecoder(),
      new TLcdMIFModelDecoder(),
      new TLcdMAPModelDecoder(),
  };

  private ILcdOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();

  public static void main(String[] aArgs) {
    if (aArgs.length != 2) {
      System.out.println("Decodes a vector file and encodes it to a GML3 file.");
      System.out.println("Arguments: [-namespace <target-namespace>] <input-file-name> <output-file-name>");
      System.err.println("Supported input file types:");
      for (ILcdModelDecoder decoder : DECODERS) {
        System.err.println("   - " + decoder.getDisplayName());
      }
      return;
    }

    // Default target namespace
    String target_namespace = "http://www.mynamespace.com";

    // Read command line arguments
    int index = 0;
    while (true) {
      String arg = aArgs[index];
      if (arg.equals("-namespace")) {
        target_namespace = aArgs[++index];
      } else {
        break;
      }

      index++;
    }
    // Get the file names.
    String filename_in = aArgs[index++];
    String filename_out = aArgs[index++];

    new Converter().convert(filename_in, filename_out, target_namespace);
  }

  public void setOutputStreamFactory(ILcdOutputStreamFactory aOutputStreamFactory) {
    fOutputStreamFactory = aOutputStreamFactory;
  }

  public void convert(String aSource, String aTarget, String aNamespace) {
    // Look up a valid decoder
    ILcdModelDecoder decoder = findDecoder(aSource);

    // Create and initialize a GML3 encoder
    TLcdGML31ModelEncoder encoder = new TLcdGML31ModelEncoder();
    encoder.setDefaultTargetNamespace(aNamespace);

    // Perform the actual decoding and encoding.
    try {
      System.out.println("Decoding [" + aSource + "] as [" + decoder.getDisplayName() + "]");
      ILcdModel model = decoder.decode(aSource);

      System.out.println("Encoding [" + aTarget + "] as [" + encoder.getDisplayName() + "]");
      encoder.setOutputStreamFactory(fOutputStreamFactory);
      encoder.export(model, aTarget);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns a decoder that is suitable for decoding the given file.
   */
  private static ILcdModelDecoder findDecoder(String aFilename) {

    for (int index = 0; index < DECODERS.length; index++) {
      ILcdModelDecoder decoder = DECODERS[index];
      if (decoder.canDecodeSource(aFilename)) {
        return decoder;
      }
    }

    throw new IllegalArgumentException("Can't find decoder for [" + aFilename + "]");
  }
}
