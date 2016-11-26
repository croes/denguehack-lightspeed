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
package samples.encoder.raster.geotiff;

import com.luciad.format.raster.*;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

import samples.common.serviceregistry.ServiceRegistry;

/**
 * This utility decodes a raster file in any of a number of formats and
 * encodes it in a GeoTIFF file.
 * <p/>
 * It can also combine a list of compatible input rasters into a single
 * GeoTIFF file. Rasters are compatible if they have the same reference
 * and if they lie on a regular grid.
 * <p/>
 * Usage: java samples.encoder.raster.geotiff.Convertor
 * [-levels   n]
 * [-tilesize n]
 * [-jpeg     f]
 * [-bigtiff   ]
 * input_raster [...] output_geotiff
 * <p/>
 * Decoders typically perform lazy loading while the encoder is writing out
 * the file, but for large rasters, it may still be necessary to increase the
 * heap size (e.g. -Xmx128m).
 */
public class Converter {

  private static final ILcdModelDecoder[] DECODERS = new ILcdModelDecoder[]{
      new TLcdGeoTIFFModelDecoder(),
      new TLcdTFWRasterModelDecoder(),
      new TLcdTABRasterModelDecoder(),
      new TLcdJAIRasterModelDecoder(),
      new TLcdRasterModelDecoder(),
      new TLcdCADRGModelDecoder(),
      new TLcdDMEDModelDecoder(),
      new TLcdDTEDModelDecoder(),
      new TLcdDEMModelDecoder(),
  };

  public static void main(String[] aArgs) {
    int level_count = 5;
    int tile_size = 256;
    float jpeg_quality = 0f;
    boolean big_tiff = false;

    // Get any arguments (for brevity without any error checking).
    int index = 0;
    try {
      while (index < aArgs.length) {
        String arg = aArgs[index];
        if (arg.equals("-levels")) {
          level_count = Integer.parseInt(aArgs[++index]);
        } else if (arg.equals("-tilesize")) {
          tile_size = Integer.parseInt(aArgs[++index]);
        } else if (arg.equals("-jpeg")) {
          jpeg_quality = Float.parseFloat(aArgs[++index]);
        } else if (arg.equals("-bigtiff")) {
          big_tiff = true;
        } else {
          break;
        }

        index++;
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("Missing numeric argument");
      System.exit(-1);
    } catch (NumberFormatException ex) {
      System.out.println("Invalid numeric argument (" + aArgs[index] + ")");
      System.exit(-1);
    }

    if (index > aArgs.length - 2) {
      System.out.println("Decodes one or more raster files and encodes them to a GeoTIFF file.");
      System.out.println("Arguments:");
      System.out.println("              [-levels   n]");
      System.out.println("              [-tilesize n]");
      System.out.println("              [-jpeg     f]");
      System.out.println("              [-bigtiff   ]");
      System.out.println("              <input-raster-file-name> [...] <output-geotiff-file-name>");
      System.out.println("Supported input file types:");
      for (ILcdModelDecoder decoder : DECODERS) {
        System.out.println("   - " + decoder.getDisplayName());
      }
      System.exit(-1);
    }

    // Perform the actual decoding and encoding.
    try {

      // Decode one or more raster models.
      int model_count = aArgs.length - index - 1;
      ILcdModel[] models = new ILcdModel[model_count];
      for (int model_index = 0; model_index < model_count; model_index++) {
        String filename_in = aArgs[index++];
        ILcdModelDecoder decoder = findDecoder(filename_in);

        System.out.println("Decoding [" + filename_in + "] as [" + decoder.getDisplayName() + "]");
        models[model_index] = decoder.decode(filename_in);
      }

      // Combine all models into a single model, if necessary.
      ILcdModel model = model_count == 1 ?
                        models[0] :
                        new RasterOrganizer().createModel(models);

      // Create an encoder.
      TLcdGeoTIFFModelEncoder encoder = new TLcdGeoTIFFModelEncoder();

      // Set the encoder options.
      if (level_count > 0) {
        encoder.setLevelCount(level_count);
      }

      if (tile_size > 0) {
        encoder.setTileWidth(tile_size);
        encoder.setTileHeight(tile_size);
      }

      if (jpeg_quality > 0f) {
        encoder.setCompression(TLcdGeoTIFFModelEncoder.COMPRESSION_JPEG_TTN2);
        encoder.setQuality(jpeg_quality);
      }

      encoder.setEncodeBigTIFF(big_tiff);

      // Encode the raster model.
      String filename_out = aArgs[index++];
      System.out.println("Encoding [" + filename_out + "] as [" + encoder.getDisplayName() + "]");
      encoder.addStatusListener(new ProgressOutput());
      encoder.export(model, filename_out);
      System.out.println();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
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

    // Fall back to the registered model decoders
    TLcdCompositeModelDecoder compositeModelDecoder = new TLcdCompositeModelDecoder(ServiceRegistry.getInstance().query(ILcdModelDecoder.class));
    if (compositeModelDecoder.canDecodeSource(aFilename)) {
      return compositeModelDecoder;
    }

    throw new IllegalArgumentException("Can't find decoder for [" + aFilename + "]");
  }

  private static class ProgressOutput implements ILcdStatusListener {
    private int fLastProgress = 0;

    @Override
    public void statusChanged(TLcdStatusEvent aStatusEvent) {
      if (aStatusEvent.getID() == TLcdStatusEvent.PROGRESS && !aStatusEvent.isProgressIndeterminate()) {
        int progress = (int) (aStatusEvent.getProgress() * 10);
        if (progress != fLastProgress) {
          System.out.print(progress * 10 + "% ");
          fLastProgress = progress;
        }
      }
    }
  }
}
