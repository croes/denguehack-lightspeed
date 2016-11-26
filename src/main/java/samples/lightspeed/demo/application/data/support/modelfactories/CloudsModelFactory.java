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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.format.raster.TLcdMultilevelRaster;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRaster;
import com.luciad.format.raster.TLcdSingleTileRaster;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Creates a model for the cloud coverage layer.
 * @since 2013.0
 */
public class CloudsModelFactory extends AbstractModelFactory {

  public CloudsModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel(
        new TLcdGeodeticReference(),
        new TLcdMultilevelRasterModelDescriptor("Clouds", "Clouds", "Clouds")
    );

    TLcdGeoTIFFModelDecoder md = new TLcdGeoTIFFModelDecoder();
    ILcdModel geoTiffModel = md.decode(aSource);
    TLcdLonLatHeightBounds bounds = new TLcdLonLatHeightBounds(-180, -90, 50e3, 360, 180, 0);
    for (Object elem : Collections.list(geoTiffModel.elements())) {
      model.addElement(adapt(elem, bounds), ILcdModel.NO_EVENT);
    }

    return model;
  }

  private static Object adapt(Object aElem, ILcdBounds aBounds) {
    if (aElem instanceof ILcdMultilevelRaster) {
      ILcdMultilevelRaster mlr = (ILcdMultilevelRaster) aElem;
      ILcdRaster[] rasters = new ILcdRaster[mlr.getRasterCount()];
      for (int i = 0; i < rasters.length; i++) {
        rasters[i] = (ILcdRaster) adapt(mlr.getRaster(i), aBounds);
      }
      return new TLcdMultilevelRaster(aBounds, rasters);
    } else if (aElem instanceof ILcdRaster) {
      ILcdRaster raster = (ILcdRaster) aElem;
      ILcdTile[][] tiles = new ILcdTile[raster.getTileRowCount()][raster.getTileColumnCount()];
      for (int r = 0; r < raster.getTileRowCount(); r++) {
        for (int c = 0; c < raster.getTileColumnCount(); c++) {
          tiles[r][c] = raster.retrieveTile(r, c);
        }
      }
      if (raster.getTileRowCount() == 1 && raster.getTileColumnCount() == 1) {
        return new TLcdSingleTileRaster(aBounds, raster.getTileWidth(), raster.getTileHeight(), tiles[0][0], raster.getDefaultValue(), raster.getColorModel());
      } else {
        return new TLcdRaster(aBounds, raster.getTileWidth(), raster.getTileHeight(), tiles, raster.getPixelDensity(), raster.getDefaultValue(), raster.getColorModel());
      }
    } else {
      throw new IllegalArgumentException("Unsupported element: " + aElem);
    }
  }

  @Override
  public void configure(Properties aProperties) {
  }
}
