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
package samples.gxy.geoid;

import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.List;

import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeoidGeodeticDatumFactory;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.TLcdBandMeasurementSemanticsBuilder;
import com.luciad.imaging.TLcdImageBuilder;
import com.luciad.imaging.TLcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;
import com.luciad.util.iso19103.TLcdUnitOfMeasureFactory;

import samples.gxy.decoder.raster.SingleTileRenderedImage;

public class GeoidModelFactory {

  // Create a geodetic datum with geoid heights.
  private static final ILcdGeodeticDatum DATUM =
      new TLcdGeoidGeodeticDatumFactory().createGeodeticDatum(TLcdGeoidGeodeticDatumFactory.EGM96_GRID_BILINEAR);

  // Define the size and resolution of the image.
  private static final ILcdBounds BOUNDS = new TLcdLonLatBounds(-180.0, -90.0, 360.0, 180.0);
  private static final int WIDTH = 2000;
  private static final int HEIGHT = 1000;

  /**
   * Creates a model containing a geoid image.
   */
  public static ILcdModel createGeoidModel(ILcdStatusListener aListener, Object aSource) {
    // Extract a display name.
    String name = DATUM.getName();
    int index = name.indexOf('+');
    String displayName = index >= 0 ?
                         name.substring(index + 1).trim() :
                         name;

    ILcdModelReference modelReference = new TLcdGeodeticReference(DATUM);
    ILcdModelDescriptor modelDescriptor = new TLcdImageModelDescriptor(name,
                                                                  name,
                                                                  displayName);

    ILcdModel model = new TLcdVectorModel(modelReference, modelDescriptor);

    ALcdImage image = createGeoidImage(aListener, aSource, modelReference);
    model.addElement(image, ILcdModel.NO_EVENT);

    return model;
  }

  /**
   * Creates a geoid image.
   */
  @SuppressWarnings("unchecked")
  private static ALcdImage createGeoidImage(ILcdStatusListener aListener, Object aSource, ILcdModelReference aModelReference) {
    TLcdStatusEvent.Progress progress = TLcdStatusEvent.startProgress(aListener, aSource, "Computing geoid heights...");

    // Create an example AWT SampleModel that matches the semantics
    SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_SHORT, WIDTH, HEIGHT, 1, WIDTH, new int[]{0});
    final WritableRaster raster = WritableRaster.createWritableRaster(sampleModel,
                                                                      null);
    for (int y = 0; y < HEIGHT; y++) {
      progress.progress((double) y / HEIGHT);
      for (int x = 0; x < WIDTH; x++) {
        // Compute the lon/lat coordinates.
        double lon = BOUNDS.getWidth() * (x + 0.5) / WIDTH + BOUNDS.getLocation().getX();
        double lat = BOUNDS.getHeight() * (HEIGHT - y - 0.5) / HEIGHT + BOUNDS.getLocation().getY();

        // Retrieve the geoid height from the geodetic datum.
        double height = DATUM.getHeight(lon, lat);
        height = Double.isNaN(height) ?
                 TLcdDTEDTileDecoder.UNKNOWN_ELEVATION :
                 (short) height;
        raster.setSample(x, y, 0, height);
      }
    }

    RenderedImage renderedImage = new SingleTileRenderedImage(raster);

    // To indicate that the mosaic contains elevation data, we create measurement semantics.
    List<ALcdBandSemantics> semantics = Collections.<ALcdBandSemantics>singletonList(
        TLcdBandMeasurementSemanticsBuilder
            .newBuilder()
            .unitOfMeasure(TLcdUnitOfMeasureFactory.deriveUnitOfMeasure(TLcdAltitudeUnit.METRE, TLcdISO19103MeasureTypeCodeExtension.TERRAIN_HEIGHT))
            .dataType(ALcdBandSemantics.DataType.SIGNED_SHORT)
            .noDataValue(TLcdDTEDTileDecoder.UNKNOWN_ELEVATION)
            .build()
    );

    progress.end();
    return TLcdImageBuilder.newBuilder()
                           .image(renderedImage)
                           .semantics(semantics)
                           .bounds(BOUNDS)
                           .imageReference(aModelReference)
                           .buildBasicImage();

  }

  public static ILcdGeodeticDatum getDatum() {
    return DATUM;
  }

}
