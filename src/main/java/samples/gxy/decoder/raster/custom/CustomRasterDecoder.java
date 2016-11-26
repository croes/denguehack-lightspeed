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
package samples.gxy.decoder.raster.custom;

import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImageMosaic;
import com.luciad.imaging.TLcdBandMeasurementSemanticsBuilder;
import com.luciad.imaging.TLcdImageBuilder;
import com.luciad.imaging.TLcdImageModelDescriptor;
import com.luciad.imaging.TLcdImageMosaicBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;
import com.luciad.util.iso19103.TLcdUnitOfMeasureFactory;

import samples.gxy.decoder.raster.SingleTileRenderedImage;

/**
 * This class demonstrates how to implement a custom raster decoder from scratch. This decoder
 * does not actually decode data from disk. Instead it generates fake elevation data in memory.
 * This has been done to keep the decoder easy to understand.
 */
public class CustomRasterDecoder implements ILcdModelDecoder {
  public boolean canDecodeSource(String aSourceName) {
    return true;
  }

  public String getDisplayName() {
    return "Custom Raster Decoder";
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    // First we obtain the coordinate reference system in which the raster data is encoded.
    // In this sample we simple create a new GeodeticReference instance which is equivalent to
    // WGS-84.
    // Most decoders use an ILcdModelReferenceDecoder implementation in order to decode the
    // CRS from an external source (for instance a .prj file).
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();

    // Next we determine the spatial extent of the data. Again this is typically read either
    // from an external file (for instance a TFW file) or from the raster data file itself; typically
    // from a header section in the file.
    TLcdLonLatBounds bounds = new TLcdLonLatBounds(4, 51, 2, 3);

    // The 'unknown value' indicates the pixel value that represents that no data is available
    // at that point. We use the same value as DTED here.
    int unknownValue = -32767;

    // To indicate that the mosaic contains elevation data, we create measurement semantics.
    List<ALcdBandSemantics> semantics = Collections.<ALcdBandSemantics>singletonList(
        TLcdBandMeasurementSemanticsBuilder
            .newBuilder()
            .unitOfMeasure(TLcdUnitOfMeasureFactory.deriveUnitOfMeasure(TLcdAltitudeUnit.METRE, TLcdISO19103MeasureTypeCodeExtension.TERRAIN_HEIGHT))
            .dataType(ALcdBandSemantics.DataType.SIGNED_SHORT)
            .noDataValue(unknownValue)
            .build()
    );

    // Create an example AWT SampleModel that matches the semantics
    SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_SHORT, 1, 1, 1, 1, new int[]{0});

    // Here we create the mosaic containing the image tiles.
    // This sample generates a 2 x 3 grid of images. Each tile corresponds to a 1 x 1 degree area.
    // The tile grid that should be created depends on the underlying data format. Raster datasets
    // in some formats, like for instance DTED, consist of many individual files. A decoder will
    // then typically create a tile object per file.
    // Simpler formats may only consist of a single file and will only require a single tile object
    int tileColumns = 2;
    int tileRows = 3;
    double tileWidth = bounds.getWidth() / tileColumns;
    double tileHeight = bounds.getHeight() / tileRows;
    TLcdImageMosaicBuilder mosaicBuilder = TLcdImageMosaicBuilder.newBuilder();
    for (int y = 0; y < tileRows; y++) {
      for (int x = 0; x < tileColumns; x++) {
        ILcd2DEditableBounds tileBounds = new TLcdLonLatBounds(
            bounds.getLocation().getX() + tileWidth * x,
            bounds.getLocation().getY() + tileHeight * y,
            tileWidth,
            tileHeight
        );
        ALcdBasicImage tile = generateImage(tileBounds, modelReference, semantics, sampleModel);
        mosaicBuilder.tile(tile, x, y);
      }
    }

    //Configure the required parameters on the mosaic builder and finally build the mosaic.
    ALcdImageMosaic mosaic = mosaicBuilder.bounds(bounds)
                                          .semantics(semantics)
                                          .imageReference(modelReference)
                                          .buildImageMosaic();

    TLcdVectorModel model = new TLcdVectorModel(
        modelReference,
        new TLcdImageModelDescriptor(
            "Memory",
            "Custom mosaic",
            "Custom mosaic"
        )
    );

    model.addElement(mosaic, ILcdFireEventMode.NO_EVENT);

    return model;
  }

  private ALcdBasicImage generateImage(ILcdBounds aBounds, ILcdModelReference aModelReference, List<ALcdBandSemantics> aSemantics, SampleModel aSampleModel) {
    // First we determine our image's pixel size which is fixed to 256 x 256 in this case.
    int pixelWidth = 256;
    int pixelHeight = 256;

    final WritableRaster raster = WritableRaster.createWritableRaster(aSampleModel.createCompatibleSampleModel(pixelWidth, pixelHeight), null);

    // Then we generate the pixel values. Pixel values should be written to the image's raster
    // in row-major order, starting at the upper-left pixel of the tile.
    for (int r = 0; r < pixelHeight; r++) {
      for (int c = 0; c < pixelWidth; c++) {
        short value = (short) (r + c);
        raster.setSample(c, r, 0, value);
      }
    }

    // Then we create a RenderedImageImage with the correct pixel dimensions and a single band. In this case we will be
    // reading/generating 256 x 256 16-bit integer values.
    RenderedImage renderedImage = new SingleTileRenderedImage(raster);

    // Once the data has been decoded we create an image object with the BufferedImage.
    return TLcdImageBuilder.newBuilder()
                           .image(renderedImage)
                           .semantics(aSemantics)
                           .bounds(aBounds)
                           .imageReference(aModelReference)
                           .buildBasicImage();
  }
}
