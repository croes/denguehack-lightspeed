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
package samples.gxy.offscreen;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.media.jai.JAI;

import com.luciad.format.mif.TLcdMAPModelDecoder;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.raster.*;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYViewFitAction;
import com.luciad.view.gxy.TLcdGXYViewPlanarImage;

import samples.common.serviceregistry.ServiceLoaderRegistry;

/**
 * This sample demonstrates the use of an off-screen view. A file is loaded
 * into a view, and the resulting view is then written to a GeoTIFF file.
 */
public class Main {

  private static final ILcdModelDecoder[] MODEL_DECODERS = new ILcdModelDecoder[]{
      new TLcdSHPModelDecoder(),
      new TLcdMIFModelDecoder(),
      new TLcdMAPModelDecoder(),
      new TLcdGeoTIFFModelDecoder(),
      new TLcdJAIRasterModelDecoder(),
      new TLcdTABRasterModelDecoder(),
      new TLcdTFWRasterModelDecoder(),
      new TLcdRasterModelDecoder(TLcdSharedBuffer.getBufferInstance()),
      new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance()),
      new TLcdDTEDModelDecoder(TLcdSharedBuffer.getBufferInstance()),
      new TLcdDEMModelDecoder(TLcdSharedBuffer.getBufferInstance()),
      new TLcdETOPOModelDecoder(TLcdSharedBuffer.getBufferInstance()),
      new TLcdCADRGModelDecoder(TLcdSharedBuffer.getBufferInstance()),
  };

  private static final int IMAGE_WIDTH = 1024;
  private static final int IMAGE_HEIGHT = 1024;
  private static final int TILE_WIDTH = 512;
  private static final int TILE_HEIGHT = 512;
  private static final int COMPRESSION = TLcdGeoTIFFModelEncoder.COMPRESSION_DEFLATE;
  //private static final int    COMPRESSION  = TLcdGeoTIFFModelEncoder.COMPRESSION_JPEG_TTN2;
  private static final float QUALITY = 0.8f;
  private static final int LEVEL_COUNT = 4;
  private static final double SCALE_FACTOR = 0.25;

  public static void main(String[] aArgs) {

    if (aArgs.length != 2) {
      System.out.println("Loads a file into an offscreen view and writes the result to a GeoTIFF file.");
      System.err.println("Arguments: <input-file-name> <output-geotiff-file-name>");
      System.exit(-1);
    }

    String input_filename = aArgs[0];
    String output_filename = aArgs[1];

    // Create the off-screen view.
    TLcdGXYViewPlanarImage view_image = createOffscreenView(input_filename);

    // Save the view as an ordinary TIFF (without georeferencing information).
    //saveViewAsTIFF(view_image, output_filename);

    // Save the view as a GeoTIFF (with georeferencing information).
    saveViewAsGeoTIFF(view_image, output_filename);

    System.exit(0);
  }

  /**
   * Creates an off-screen view containing a layer with data from the specified
   * file and a grid layer.
   */
  private static TLcdGXYViewPlanarImage createOffscreenView(String aInputFilename) {

    // We could create a view based on a BufferedImage.
    //TLcdGXYViewBufferedImage view_image =
    //  new TLcdGXYViewBufferedImage(BufferedImage.TYPE_INT_ARGB);

    // We're creating a view based on a PlanarImage, whose tiles are computed
    // on the fly and as needed.
    TLcdGXYViewPlanarImage view_image =
        new TLcdGXYViewPlanarImage(IMAGE_WIDTH,
                                   IMAGE_HEIGHT,
                                   TILE_WIDTH,
                                   TILE_HEIGHT,
                                   BufferedImage.TYPE_INT_ARGB);

    // Use a transparent background.
    view_image.setBackground(new Color(0, 0, 0, 0));

    // Set a generic layer factory.
    Iterable<ILcdGXYLayerFactory> layerFactories = new ServiceLoaderRegistry().query(ILcdGXYLayerFactory.class);
    view_image.setGXYLayerFactory(new TLcdCompositeGXYLayerFactory(layerFactories));

    // Avoid internal updates to the image that we wouldn't see anyway.
    view_image.setAutoUpdate(false);

    // Pick a world reference for the view.
    TLcdGridReference world_reference =
        new TLcdGridReference(new TLcdGeodeticDatum(),
                              new TLcdEquidistantCylindrical(),
                              0.0,  // false easting
                              0.0,  // false northing
                              1.0,  // scale
                              1.0,  // unit of measure
                              0.0); // rotation

    view_image.setXYWorldReference(world_reference);

    try {
      // Find a decoder that can decode the source file.
      ILcdModelDecoder model_decoder = findModelDecoder(aInputFilename);

      // Load the model.
      ILcdModel model = model_decoder.decode(aInputFilename);

      // Add the model to the view.
      view_image.addModel(model);

      // Fit the view to the model layer.
      TLcdGXYViewFitAction fit_action = new TLcdGXYViewFitAction(view_image);
      fit_action.fitGXYLayer((ILcdGXYLayer) view_image.getLayer(0), view_image);

    } catch (IOException ex) {
      System.err.println("Can't load model file [" + aInputFilename + "]");
    }
    return view_image;
  }

  /**
   * Returns a model decoder that is suitable for the specified file.
   */
  private static ILcdModelDecoder findModelDecoder(String aFileName)
      throws IllegalArgumentException {

    for (int index = 0; index < MODEL_DECODERS.length; index++) {
      if (MODEL_DECODERS[index].canDecodeSource(aFileName)) {
        return MODEL_DECODERS[index];
      }
    }

    throw new IllegalArgumentException("Can't find a decoder for file [" + aFileName + "]");
  }

  /**
   * Saves the given view as an ordinary TIFF with the specified file name.
   */
  private static void saveViewAsTIFF(TLcdGXYViewPlanarImage aViewimage,
                                     String aOutputFilename) {
    try {
      // Save the view a simple TIFF file..
      JAI.create("filestore",
                 aViewimage,
                 aOutputFilename,
                 "tiff",
                 null);
    } catch (IllegalArgumentException ex) {
      System.err.println("Can't save image file [" + aOutputFilename + "] (" + ex.getMessage() + ")");
    }
  }

  /**
   * Saves the given view as a GeoTIFF with the specified file name.
   */
  private static void saveViewAsGeoTIFF(TLcdGXYViewPlanarImage aViewimage,
                                        String aOutputFilename) {
    try {
      // The model reference is the world reference of the view.
      ILcdModelReference model_reference = (ILcdModelReference) aViewimage.getXYWorldReference();

      TLcdRasterModelDescriptor model_descriptor = new TLcdRasterModelDescriptor(aOutputFilename,
                                                                                 aOutputFilename,
                                                                                 "GeoTIFF");

      ILcdModel model = new TLcdVectorModel(model_reference, model_descriptor);

      // Compute the model bounds from the view's AWT bounds.
      ILcd2DEditableBounds view_world_bounds = new TLcdXYBounds();
      aViewimage.getGXYViewXYWorldTransformation().viewAWTBounds2worldSFCT(aViewimage.getBounds(),
                                                                           view_world_bounds);

      // Wrap the view's image in a tile.
      ILcdTile tile = new TLcdRenderedImageTile(aViewimage);

      // Put the tile in a raster.
      double density = tile.getWidth() / view_world_bounds.getWidth() *
                       tile.getHeight() / view_world_bounds.getHeight();

      TLcdRaster raster = new TLcdRaster(view_world_bounds,
                                         new ILcdTile[][]{{tile}},
                                         density,
                                         0,
                                         null);

      // Put the raster in the model.
      model.addElement(raster, ILcdFireEventMode.NO_EVENT);

      // Encode the raster model as a GeoTIFF.
      TLcdGeoTIFFModelEncoder model_encoder = new TLcdGeoTIFFModelEncoder();

      model_encoder.setCompression(COMPRESSION);
      model_encoder.setQuality(QUALITY);
      model_encoder.setLevelCount(LEVEL_COUNT);
      model_encoder.setScaleFactor(SCALE_FACTOR);
      model_encoder.setTileWidth(TILE_WIDTH);
      model_encoder.setTileHeight(TILE_HEIGHT);

      model_encoder.export(model, aOutputFilename);

    } catch (IOException ex) {
      System.err.println("Can't save image file [" + aOutputFilename + "] (" + ex.getMessage() + ")");
    }
  }
}
