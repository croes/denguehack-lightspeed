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
package samples.lightspeed.nongeoreferenced;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRenderedImageTile;
import com.luciad.format.raster.TLcdSingleTileRaster;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.shape2D.TLcdXYBounds;

import samples.common.model.CartesianReference;

/**
 * A simple image model decoder that uses {@link ImageIO} for decoding an image that
 * is not geographically referenced.
 */
public class SimpleImageModelDecoder implements ILcdModelDecoder {
  @Override
  public String getDisplayName() {
    return "Simple image";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    int index = aSourceName.lastIndexOf('.');
    String suffix = index == -1 ? aSourceName : aSourceName.substring(index + 1);
    return ImageIO.getImageReadersBySuffix(suffix).hasNext();
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    // Decode the image
    TLcdIOUtil ioUtil = new TLcdIOUtil();
    ioUtil.setSourceName(aSourceName);
    InputStream is = ioUtil.retrieveInputStream();
    BufferedImage image = ImageIO.read(is);
    if (image == null) {
      throw new IOException("Could not read " + aSourceName);
    }

    // Create a raster from the image
    TLcdSingleTileRaster raster = new TLcdSingleTileRaster(
        new TLcdXYBounds(0, 0, image.getWidth(), image.getHeight()),
        new TLcdRenderedImageTile(image),
        0,
        image.getColorModel()
    );

    // Create a model with a cartesian reference
    String displayName = new File(aSourceName).getName();
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel(
        CartesianReference.getInstance(),
        new TLcdRasterModelDescriptor(aSourceName, displayName, getDisplayName())
    );
    model.addElement(raster, ILcdModel.NO_EVENT);
    return model;
  }
}
