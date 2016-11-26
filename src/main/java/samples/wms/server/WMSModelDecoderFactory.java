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
package samples.wms.server;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.format.mif.TLcdMAPModelDecoder;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.raster.TLcdCADRGModelDecoder;
import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdETOPOModelDecoder;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.format.raster.TLcdRasterModelDecoder;
import com.luciad.format.raster.TLcdTABRasterModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdMultilevelTiledModelDecoder;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.wms.server.ILcdModelDecoderFactory;

import samples.gxy.decoder.custom1.Custom1ModelDecoder;

/**
 * The <code>ILcdModelDecoderFactory</code> in the WMS is responsible for
 * decoding the data backend. For instance, if a raster file in the GeoTIFF format
 * is to be served via the WMS, the <code>ILcdModelDecoderFactory</code>
 * must be able to provide a decoder (<code>ILcdModelDecoder</code> for the GeoTIFF format.
 * <p/>
 * This sample implementation provides decoders for a number of raster and vector formats.
 **/
public class WMSModelDecoderFactory implements ILcdModelDecoderFactory {

  private static final int sNUMBER_OF_DECODERS = 13;

  private ILcdModelDecoder createModelDecoder(int aIndex) {
    switch (aIndex) {
    case 0: // Decoder for shape files.
      return new TLcdSHPModelDecoder();
    case 1: // Decoder for LuciadLightspeed Earth repositories.
      return new TLcdEarthRepositoryModelDecoder();
    case 2: // A decoder for a custom format.
      return new Custom1ModelDecoder();
    case 3: // Decoder for rasters.
      return new TLcdRasterModelDecoder(TLcdSharedBuffer.getBufferInstance());
    case 4: // Decoder for MIF files.
      return new TLcdMIFModelDecoder();
    case 5: // Decoder for MAP files.
      return new TLcdMAPModelDecoder();
    case 6: // Decoder for multilevel tiled (.mtm) files.
      return new TLcdMultilevelTiledModelDecoder();
    case 7: // Decoder for CADRG files.
      return new TLcdCADRGModelDecoder(TLcdSharedBuffer.getBufferInstance());
    case 8: // Decoder for DMED/DTED files.
      return new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance());
    case 9: // Decoder for DEM files.
      return new TLcdDEMModelDecoder(TLcdSharedBuffer.getBufferInstance());
    case 10: // Decoder for ETOPO files.
      return new TLcdETOPOModelDecoder(TLcdSharedBuffer.getBufferInstance());
    case 11: // Decoder for GeoTIFF files.
      return new TLcdGeoTIFFModelDecoder(new TLcdModelReferenceDecoder());
    case 12: // Decoder for TAB files.
      return new TLcdTABRasterModelDecoder();
    }
    return null;
  }

  public ILcdModelDecoder createModelDecoder(String aSource) throws IllegalArgumentException {
    for (int i = 0; i < sNUMBER_OF_DECODERS; i++) {
      ILcdModelDecoder modelDecoder = createModelDecoder(i);
      // Check which decoder can decode the given source, and return it.
      if (modelDecoder != null &&
          modelDecoder.canDecodeSource(aSource)) {
        return modelDecoder;
      }
    }
    return null;
  }
}
