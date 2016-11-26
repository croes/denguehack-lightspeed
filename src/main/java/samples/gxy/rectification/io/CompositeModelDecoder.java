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
package samples.gxy.rectification.io;

import java.io.IOException;

import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.format.raster.reference.ILcdRasterReferencer;
import com.luciad.format.raster.reference.TLcdPolynomialRasterReferencer;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.reference.TLcdModelReferenceDecoder;

/**
 * A model decoder which delegates to a GeoTIFF decoder (with a polynomial referencer), and if that
 * fails, to a JAI decoder.
 */
public class CompositeModelDecoder implements ILcdModelDecoder {

  private ILcdModelDecoder[] fDecoders;

  public CompositeModelDecoder() {
    // These two decoders will be tried successively. 
    TLcdGeoTIFFModelDecoder geotiff_decoder = new TLcdGeoTIFFModelDecoder();
    JAIModelDecoderWrapper jai_decoder = new JAIModelDecoderWrapper();

    geotiff_decoder.setModelReferenceDecoder(new TLcdModelReferenceDecoder());

    int polynomial_degree = 2;
    ILcdRasterReferencer reference_factory = new TLcdPolynomialRasterReferencer(polynomial_degree);
    geotiff_decoder.setRasterReferencer(reference_factory);

    fDecoders = new ILcdModelDecoder[]{geotiff_decoder, jai_decoder};
  }

  public String getDisplayName() {
    return "TIFF/JAI Model decoder";
  }

  public boolean canDecodeSource(String aSourceName) {
    for (ILcdModelDecoder fDecoder : fDecoders) {
      if (fDecoder.canDecodeSource(aSourceName)) {
        return true;
      }
    }
    return false;
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    for (ILcdModelDecoder fDecoder : fDecoders) {
      if (fDecoder.canDecodeSource(aSourceName)) {
        try {
          return fDecoder.decode(aSourceName);
        } catch (IOException ex) { /* try the next decoder */}
      }
    }
    throw new IOException("No decoder could open the file.");
  }
}
