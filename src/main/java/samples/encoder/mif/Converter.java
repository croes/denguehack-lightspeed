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
package samples.encoder.mif;

import com.luciad.format.mif.TLcdMAPModelDecoder;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.mif.TLcdMIFModelEncoder;
import com.luciad.format.pol.TLcdPOLModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;

/**
 * This utility decodes a vector file in any of a number of formats and
 * encodes it to a MIF file.
 * <p/>
 * Usage: java samples.encoder.mif.Converter vector_file mif_file
 */
public class Converter {

  private static final ILcdModelDecoder[] DECODERS = new ILcdModelDecoder[]{
      new TLcdMIFModelDecoder(),
      new TLcdMAPModelDecoder(),
      new TLcdSHPModelDecoder(),
      new TLcdPOLModelDecoder(),
  };

  private ILcdOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();

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

    // Get the file names.
    String filename_in = aArgs[0];
    String filename_out = aArgs[1];
    new Converter().convert(filename_in, filename_out);

    System.exit(0);
  }

  public void convert(String aFilename_in, String aFilename_out) {
    // Create a decoder and the encoder.
    ILcdModelDecoder decoder = findDecoder(aFilename_in);
    TLcdMIFModelEncoder encoder = new TLcdMIFModelEncoder();

    // Perform the actual decoding and encoding.
    try {
      System.out.println("Decoding [" + aFilename_in + "] as [" + decoder.getDisplayName() + "]");
      ILcdModel model = decoder.decode(aFilename_in);

      System.out.println("Encoding [" + aFilename_out + "] as [" + encoder.getDisplayName() + "]");
      encoder.setOutputStreamFactory(fOutputStreamFactory);
      encoder.export(model, aFilename_out);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void setOutputStreamFactory(ILcdOutputStreamFactory aOutputStreamFactory) {
    fOutputStreamFactory = aOutputStreamFactory;
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
