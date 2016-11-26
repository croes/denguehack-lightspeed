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
package samples.gxy.decoder.raster.hgt;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;

import com.luciad.format.raster.ILcdTileDecoder;
import com.luciad.format.raster.ILcdTileInfo;
import com.luciad.format.raster.TLcdBILTileDecoder;
import com.luciad.format.raster.TLcdDEMTileDecoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ELcdImageSamplingMode;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.imaging.TLcdBandMeasurementSemanticsBuilder;
import com.luciad.imaging.TLcdImageBuilder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdIOUtil;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdBuffer;
import com.luciad.util.ILcdBufferSegment;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdCodecException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.TLcdStringUtil;
import com.luciad.util.iso19103.TLcdUnitOfMeasureFactory;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;
import com.luciad.util.service.LcdService;
import com.sun.media.imageioimpl.common.SimpleRenderedImage;

/**
 * This custom raster model decoder decodes elevation data in NASA's SRTM HGT
 * format.
 *
 * @see <a href="http://www2.jpl.nasa.gov/srtm/">Shuttle Radar Topography Mission</a>
 * @see <a href="http://dds.cr.usgs.gov/srtm/">SRTM height data</a>
 */
@LcdService(service = ILcdModelDecoder.class)
public class HGTModelDecoder
    implements ILcdModelDecoder, ILcdInputStreamFactoryCapable {

  public static final String DEFAULT_DISPLAY_NAME = "SRTM Height";
  public static final String DEFAULT_EXTENSION = "hgt";

  private static final ColorModel DEFAULT_COLOR_MODEL =
      TLcdDTEDColorModelFactory.getSharedInstance().createColorModel();

  private static final TLcdGeodeticReference MODEL_REFERENCE = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  private ILcdBuffer fBuffer;
  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();
  private ColorModel fColorModel = DEFAULT_COLOR_MODEL;

  /**
   * Creates a new TLcdHGTModelDecoder with a globally shared buffer for
   * caching tiles.
   * @see TLcdSharedBuffer#getBufferInstance()
   */
  public HGTModelDecoder() {
    this(TLcdSharedBuffer.getBufferInstance());
  }

  /**
   * Creates a new TLcdHGTModelDecoder with the given shared buffer for
   * caching tiles.
   */
  public HGTModelDecoder(ILcdBuffer aBuffer) {
    fBuffer = aBuffer;
  }

  /**
   * Sets the input stream factory that will be used for creating input streams
   * given source names.
   */
  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  /**
   * Returns the input stream factory that is used for creating input streams
   * given source names.
   */
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fInputStreamFactory;
  }

  /**
   * Sets the color model that is attached to decoded rasters.
   * @param aColorModel a 16-bit IndexColorModel.
   */
  public void setColorModel(ColorModel aColorModel) {
    fColorModel = aColorModel;
  }

  /**
   * Returns the color model that is attached to decoded rasters.
   */
  public ColorModel getColorModel() {
    return fColorModel;
  }

  // Implementations for ILcdModelDecoder.

  public String getDisplayName() {
    return DEFAULT_DISPLAY_NAME;
  }

  /**
   * @return {@code true} if the given source name ends with "hgt" and it
   *         defines a proper origin.
   */
  public boolean canDecodeSource(String aSourceName) {
    return TLcdStringUtil.endsWithIgnoreCase(aSourceName, DEFAULT_EXTENSION) &&
           parseOrigin(aSourceName) != null;
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    // Create an image.
    ALcdImage image = decodeImage(aSourceName);

    // Create a model for the image.
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();

    // Attach a model descriptor.
    model.setModelDescriptor(new HGTModelDescriptor(aSourceName));

    // HGT data are always in the WGS84 geodetic reference.
    model.setModelReference(MODEL_REFERENCE);

    // Add the raster to the model.
    model.addElement(image, ILcdFireEventMode.NO_EVENT);

    return model;
  }

  /**
   * Decodes the specified SRTM HGT tile as an image.
   */
  private ALcdBasicImage decodeImage(String aSourceName) throws IOException {
    // Due to the SRTM HGT format, we are choosing to represent each pixel by
    // the sample in its North-West corner. The last row and column of a tile
    // are then ignored, since they are duplicated across tiles. For more
    // traditional pixel-based (cell-based) raster formats, this is not
    // necessary.

    // For simplicity, we're assuming SRTM3 tiles. With the SRTM HGT format, we
    // can only detect the difference based on file names or file sizes.

    // The number of samples for SRTM1 tiles (sample spacing 1 arc-second).
    //final int tile_width  = 3601;
    //final int tile_height = 3601;

    // The number of samples for SRTM3 tiles (sample spacing 3 arc-seconds).
    final int tile_width = 1201;
    final int tile_height = 1201;

    final int sub_tile_width = 256;
    final int sub_tile_height = 256;

    // The format is a straightforward uncompressed dump of the raster data,
    // which an ordinary BIL tile decoder can handle.
    TLcdBILTileDecoder decoder = new TLcdBILTileDecoder(tile_width,
                                                        tile_height,
                                                        1,
                                                        16,
                                                        false,
                                                        0,
                                                        2 * tile_width,
                                                        fColorModel);

    decoder.setInputStreamFactory(fInputStreamFactory);

    // Wrap the tile in a rendered image.
    HGTRenderedImage renderedImage = new HGTRenderedImage(aSourceName, 0, decoder, fBuffer,
                                                          tile_width,
                                                          tile_height,
                                                          sub_tile_width,
                                                          sub_tile_height);

    // The HGT format encodes the origin of the tile in the file name. Parse it.
    ILcdPoint origin = parseOrigin(aSourceName);
    if (origin == null) {
      throw new IOException("Can't parse origin from file name [" + aSourceName + "]");
    }

    // HGT tiles are always the same size, in geodetic coordinates.
    ILcdBounds bounds = new TLcdLonLatBounds(origin, 1.0, 1.0);

    // Create the image.
    return TLcdImageBuilder
        .newBuilder()
        .image(renderedImage)
        .bounds(bounds)
        .imageReference(MODEL_REFERENCE)
        .samplingMode(ELcdImageSamplingMode.POINT)
        .semantics(
            Collections.<ALcdBandSemantics>singletonList(
                TLcdBandMeasurementSemanticsBuilder
                    .newBuilder()
                    .dataType(ALcdBandSemantics.DataType.SIGNED_SHORT)
                    .noDataValue(TLcdDEMTileDecoder.UNKNOWN_ELEVATION)
                    .unitOfMeasure(TLcdUnitOfMeasureFactory.deriveUnitOfMeasure(TLcdAltitudeUnit.METRE, TLcdISO19103MeasureTypeCodeExtension.TERRAIN_HEIGHT))
                    .build()
            )
        )
        .buildBasicImage();
  }

  /**
   * Parses and returns the origin of the given SRTM HGT file name.
   */
  private ILcdPoint parseOrigin(String aSourceName) {
    String base_name = TLcdIOUtil.getFileName(aSourceName);

    try {
      int lat = Integer.parseInt(base_name.substring(1, 3));
      char south_north = Character.toUpperCase(base_name.charAt(0));
      switch (south_north) {
      case 'S':
        lat = -lat;
        break;
      case 'N':
        break;
      default:
        return null;
      }

      int lon = Integer.parseInt(base_name.substring(4, 7));
      char west_east = Character.toUpperCase(base_name.charAt(3));
      switch (west_east) {
      case 'W':
        lon = -lon;
        break;
      case 'E':
        break;
      default:
        return null;
      }

      return new TLcdLonLatPoint(lon, lat);

    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static class HGTModelDescriptor extends TLcdModelDescriptor implements ILcdImageModelDescriptor {
    public HGTModelDescriptor(String aSourceName) {
      super(aSourceName, TLcdIOUtil.getFileName(aSourceName), HGTModelDecoder.DEFAULT_DISPLAY_NAME);
    }
  }

  private static class HGTRenderedImage extends SimpleRenderedImage {
    private final String fFileName;
    private final ILcdTileDecoder fDecoder;
    private final int fImageIndex;
    private final ILcdBuffer fBuffer;

    public HGTRenderedImage(String aFileName, int aImageIndex, ILcdTileDecoder aDecoder, ILcdBuffer aBuffer, int aWidth, int aHeight, int aTileWidth, int aTileHeight) {
      fDecoder = aDecoder;
      width = aWidth;
      height = aHeight;
      tileWidth = aTileWidth;
      tileHeight = aTileHeight;
      sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_SHORT, aWidth, aHeight, 1, aWidth, new int[]{0});
      fFileName = aFileName;
      fImageIndex = aImageIndex;
      fBuffer = aBuffer;
    }

    @Override
    public Raster getTile(int tileX, int tileY) {
      try {
        // Read the data
        ILcdTileInfo tileInfo = fDecoder.readTile(fFileName, fImageIndex, tileWidth, tileHeight, tileY, tileX, fBuffer);

        // Create an AWT raster
        int currTileWidth = Math.min(tileWidth, width - tileX * tileWidth);
        int currTileHeight = Math.min(tileHeight, height - tileY * tileHeight);
        SampleModel tileSampleModel = sampleModel.createCompatibleSampleModel(currTileWidth, currTileHeight);
        WritableRaster raster = WritableRaster.createWritableRaster(tileSampleModel, new Point(tileX * tileWidth, tileY * tileHeight));

        // Copy the data
        ILcdBufferSegment bufferSegment = tileInfo.getBufferSegment();
        ShortBuffer inBuffer = ByteBuffer.wrap(
            bufferSegment.getBuffer().getByte(),
            bufferSegment.getOffset(),
            bufferSegment.getSize()
        ).asShortBuffer();
        ShortBuffer outBuffer = ShortBuffer.wrap(((DataBufferShort) raster.getDataBuffer()).getData());
        if (currTileWidth == tileWidth && currTileHeight == tileHeight) {
          outBuffer.put(inBuffer);
        } else {
          int bufferPos = inBuffer.position();
          for (int y = 0; y < currTileHeight; y++) {
            inBuffer.limit(bufferPos + currTileWidth);
            inBuffer.position(bufferPos);
            outBuffer.put(inBuffer);
            bufferPos += tileWidth;
          }
        }

        return raster;
      } catch (TLcdCodecException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
