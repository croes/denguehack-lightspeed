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
package samples.encoder.geojson;

import com.luciad.format.geojson.TLcdGeoJsonModelEncoder;
import com.luciad.format.mif.TLcdMAPModelDecoder;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.pol.TLcdPOLModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;

/**
 * This utility decodes a vector file in a of a number of formats and
 * encodes it as a GeoJson file.
 * <p/>
 * Usage: java samples.encoder.geojson.Convertor vector_file geojson_file
 */
public class Converter {

  private static final ILcdModelDecoder[] DECODERS = new ILcdModelDecoder[]{
      new TLcdMIFModelDecoder(),
      new TLcdMAPModelDecoder(),
      new TLcdPOLModelDecoder(),
      new TLcdSHPModelDecoder()
  };

  public static void main(String[] aArgs) {
    if (aArgs.length != 2) {
      System.out.println("Decodes a vector file and encodes it to a MIF file.");
      System.out.println("Arguments: <input-file-name> <output-file-name>");
      System.err.println("Supported input file types:");
      for (ILcdModelDecoder decoder : DECODERS) {
        System.err.println("   - " + decoder.getDisplayName());
      }
      return;
    }

    if (aArgs.length < 2) {
      System.out.println("\"Please run this script with 2 arguments: (1) the name of the input vector file, and, (2) the name of the output geojson file.");
      return;
    }

    // Get the file names.
    String filename_in = aArgs[0];
    String filename_out = aArgs[1];

    // Create a decoder and the encoder.
    ILcdModelDecoder decoder = findDecoder(filename_in);
    TLcdGeoJsonModelEncoder encoder = new TLcdGeoJsonModelEncoder();

    // Perform the actual decoding and encoding.
    try {
      System.out.println("Decoding [" + filename_in + "] as [" + decoder.getDisplayName() + "]");
      ILcdModel model = decoder.decode(filename_in);

      System.out.println("Encoding [" + filename_out + "] as [" + encoder.getDisplayName() + "]");
      encoder.export(model, filename_out);

      System.out.println("Success.");

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
