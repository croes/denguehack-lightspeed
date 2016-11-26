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

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdByteArrayTile;
import com.luciad.format.raster.TLcdIntArrayTile;
import com.luciad.format.raster.TLcdRaster;
import com.luciad.format.raster.TLcdShortArrayTile;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;

/**
 * This class combines several compatible raster models into a single model
 * containing a single raster. Such a model is more efficient for display
 * and it can also be saved into a single GeoTIFF file, for instance.
 * <p>
 * Conditions:
 * <ul>
 * <li>The raster models must have equal model references.
 * <li>The rasters must lie on a regular grid.
 * <li>The rasters must all have equal tile sizes.
 * </ul>
 * This means that irregular layouts and truncated tiles are not supported.
 */
public class RasterOrganizer {

  private static final double EPSILON = 1e-6;

  /**
   * Creates a model containing a single raster that is constructed from the
   * rasters in the given models.
   */
  public ILcdModel createModel(ILcdModel[] aModels) {

    ILcdModel first_model = aModels[0];
    ILcdModelReference model_reference = first_model.getModelReference();
    ILcdRaster[] rasters = new ILcdRaster[aModels.length];

    // Check that the raster models are compatible and create a list of rasters.
    for (int index = 0; index < aModels.length; index++) {
      ILcdModel model = aModels[index];
      ILcdRaster raster = getRaster(model);

      if (!model.getModelReference().equals(model_reference)) {
        throw new IllegalArgumentException("All rasters must have the same model reference");
      }

      rasters[index] = raster;
    }

    // Create a global raster.
    ILcdRaster raster = createRaster(rasters);

    // Create the model and add the global raster.
    ILcdModel model = new TLcdVectorModel(model_reference,
                                          first_model.getModelDescriptor());

    model.addElement(raster, ILcdModel.NO_EVENT);

    return model;
  }

  /**
   * Creates a single raster from the given rasters.
   */
  public ILcdRaster createRaster(ILcdRaster[] aRasters) {

    ILcdRaster first_raster = aRasters[0];
    double tile_width = first_raster.getTileWidth();
    double tile_height = first_raster.getTileHeight();

    // Check that the rasters are compatible.
    for (int index = 0; index < aRasters.length; index++) {
      ILcdRaster raster = aRasters[index];

      if (Math.abs(raster.getTileWidth() / tile_width - 1.0) > EPSILON ||
          Math.abs(raster.getTileHeight() / tile_height - 1.0) > EPSILON) {
        throw new IllegalArgumentException("All rasters must have the same tile size (" + raster.getTileWidth() + "x" + raster.getTileHeight() + " differs from " + tile_width + "x" + tile_height + ")");
      }

      if (Math.abs(raster.getTileColumnCount() * tile_width / raster.getBounds().getWidth() - 1.0) > EPSILON ||
          Math.abs(raster.getTileRowCount() * tile_height / raster.getBounds().getHeight() - 1.0) > EPSILON) {
        throw new IllegalArgumentException("Rasters must not have truncated tiles");
      }
    }

    // Compute the global raster bounds.
    double raster_x_min = Double.MAX_VALUE;
    double raster_x_max = -Double.MAX_VALUE;
    double raster_y_min = Double.MAX_VALUE;
    double raster_y_max = -Double.MAX_VALUE;

    for (int index = 0; index < aRasters.length; index++) {
      ILcdRaster raster = aRasters[index];
      ILcdBounds bounds = raster.getBounds();
      ILcdPoint location = bounds.getLocation();

      if (raster_x_min > location.getX()) {
        raster_x_min = location.getX();
      }
      if (raster_x_max < location.getX() + bounds.getWidth()) {
        raster_x_max = location.getX() + bounds.getWidth();
      }
      if (raster_y_min > location.getY()) {
        raster_y_min = location.getY();
      }
      if (raster_y_max < location.getY() + bounds.getHeight()) {
        raster_y_max = location.getY() + bounds.getHeight();
      }
    }

    // Compute the global number of tiles.
    int tile_col_count = (int) Math.round((raster_x_max - raster_x_min) / tile_width);
    int tile_row_count = (int) Math.round((raster_y_max - raster_y_min) / tile_height);

    // Fill out the tiles in the raster.
    ILcdTile[][] tiles = new ILcdTile[tile_row_count][tile_col_count];

    // Create an suitable empty tile, in case some tiles are missing.
    int default_value = first_raster.getDefaultValue();
    ILcdTile empty_tile =
        first_raster.getType() == ILcdTile.BYTE ? new TLcdByteArrayTile(new byte[]{(byte) default_value}, 1, 1) :
        first_raster.getType() == ILcdTile.SHORT ? new TLcdShortArrayTile(new short[]{(short) default_value}, 1, 1) :
        new TLcdIntArrayTile(new int[]{default_value}, 1, 1);

    for (int col = 0; col < tile_col_count; col++) {
      for (int row = 0; row < tile_row_count; row++) {
        tiles[row][col] = empty_tile;
      }
    }

    for (int index = 0; index < aRasters.length; index++) {
      ILcdRaster raster = aRasters[index];
      ILcdBounds bounds = raster.getBounds();
      ILcdPoint location = bounds.getLocation();

      // Compute the location of the input raster tiles in the global raster.
      int first_col = (int) Math.round((location.getX() - raster_x_min) / tile_width);
      int first_row = tile_row_count - 1 - (int) Math.round((location.getY() - raster_y_min) / tile_height);

      // Copy the input raster tiles to the global raster.
      for (int col = 0; col < raster.getTileColumnCount(); col++) {
        for (int row = 0; row < raster.getTileRowCount(); row++) {
          tiles[first_row + row][first_col + col] = raster.retrieveTile(row, col);
        }
      }
    }

    // Create the global bounds.
    ILcdBounds bounds = new TLcdXYBounds(raster_x_min,
                                         raster_y_min,
                                         raster_x_max - raster_x_min,
                                         raster_y_max - raster_y_min);

    // Create the global raster.
    return new TLcdRaster(bounds,
                          tiles,
                          first_raster.getPixelDensity(),
                          default_value,
                          first_raster.getColorModel());
  }

  /**
   * Retrieves the raster from the given model, assuming that there is only
   * one raster.
   */
  private ILcdRaster getRaster(ILcdModel aModel) {

    Object element = aModel.elements().nextElement();

    // Return the raster itself, or the most detailed raster, in case of a
    // multi-level raster.
    return element instanceof ILcdMultilevelRaster ?
           ((ILcdMultilevelRaster) element).getRaster(((ILcdMultilevelRaster) element).getRasterCount() - 1) :
           (ILcdRaster) element;
  }
}
