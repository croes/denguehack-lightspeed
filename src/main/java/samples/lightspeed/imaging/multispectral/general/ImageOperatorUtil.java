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
package samples.lightspeed.imaging.multispectral.general;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.util.TLcdEarthImageBuilder;
import com.luciad.imaging.ALcdBandColorSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdImageMosaic;
import com.luciad.imaging.ALcdImagingEngine;
import com.luciad.imaging.ALcdMultilevelImage;
import com.luciad.imaging.ALcdMultilevelImageMosaic;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.imaging.operator.TLcdCompositeOp;
import com.luciad.imaging.operator.TLcdCropOp;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.util.ELcdInterpolationType;

/**
 * Utility for general image operations.
 */
public class ImageOperatorUtil {

  /**
   * Reads the data out of a given image.
   *
   * @param aImage the image
   * @return the image data
   */
  public static Raster getImageData(ALcdBasicImage aImage) {
    ALcdImagingEngine engine = ALcdImagingEngine.createEngine();

    Raster data;
    try {
      data = engine.getImageDataReadOnly(
          aImage, new Rectangle(0, 0, aImage.getConfiguration().getWidth(), aImage.getConfiguration().getHeight())
      );
    } finally {
      engine.dispose();
    }

    return data;
  }

  /**
   * Returns a single representative image for a model.
   *
   * @param aModel        the model
   * @param aMaxImageSize the maximum image size
   *
   * @return the image or {@code null}
   */
  public static ALcdBasicImage getImage(ILcdModel aModel, int aMaxImageSize) {
    List<ALcdBasicImage> images = getImages(aModel, aMaxImageSize, aMaxImageSize);
    if (images.isEmpty()) {
      return null;
    }
    // Composite the images
    ALcdBasicImage image = images.get(0);
    for (int i = 1; i < images.size(); i++) {
      image = TLcdCompositeOp.composite(image, images.get(i));
    }
    // Convert indexed to color
    ALcdBandSemantics firstBandSemantic = image.getConfiguration().getSemantics().get(0);
    if (firstBandSemantic instanceof ALcdBandColorSemantics) {
      ALcdBandColorSemantics colorSemantic = (ALcdBandColorSemantics) firstBandSemantic;
      if (colorSemantic.getType() == ALcdBandColorSemantics.Type.PALETTE_INDEX) {
        TLcdLookupTable lookupTable = TLcdLookupTable.newBuilder()
                                                     .fromIndexColorModel(colorSemantic.getPalette())
                                                     .interpolation(ELcdInterpolationType.NONE)
                                                     .build();
        image = (ALcdBasicImage) TLcdIndexLookupOp.indexLookup(image, lookupTable);
      }
    }
    // Crop if the image is too large
    int width = image.getConfiguration().getWidth();
    int height = image.getConfiguration().getHeight();
    if (width > aMaxImageSize || height > aMaxImageSize) {
      // Avoid computing a histogram for a huge image, this takes too long for the UI
      int cropWidth = Math.min(aMaxImageSize, width);
      int cropHeight = Math.min(aMaxImageSize, height);
      image = (ALcdBasicImage) TLcdCropOp.crop(
          image,
          (width - cropWidth) / 2,
          (height - cropHeight) / 2,
          cropWidth,
          cropHeight
      );
    }
    return image;
  }

  public static boolean isImageModel(ILcdModel aModel) {
    return aModel != null && aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor;
  }

  /**
   * Helper methods to query images from the first level of the first raster in a layer
   */
  private static List<ALcdBasicImage> getImages(ILcdModel aModel, int aTargetWidth, int aTargetHeight) {
    if ((aModel != null)) {
      Enumeration elements = aModel.elements();
      if (elements.hasMoreElements()) {
        Object element = elements.nextElement();
        ALcdImage image = ALcdImage.fromDomainObject(element);
        if (image == null && element instanceof ILcdEarthTileSet) {
          image = TLcdEarthImageBuilder.newBuilder().tileSet((ILcdEarthTileSet) element).buildMultilevelImageMosaic();
        }
        if (image instanceof ALcdBasicImage) {
          return Collections.singletonList((ALcdBasicImage) image);
        } else if (image instanceof ALcdMultilevelImage) {
          return Collections.singletonList(((ALcdMultilevelImage) image).getLevel(0));
        } else if (image instanceof ALcdImageMosaic) {
          ALcdImageMosaic mosaic = (ALcdImageMosaic) image;
          return getImages(mosaic, aTargetWidth, aTargetHeight);
        } else if (image instanceof ALcdMultilevelImageMosaic) {
          ALcdMultilevelImageMosaic mlim = (ALcdMultilevelImageMosaic) image;
          return getImages(mlim.getLevel(0), aTargetWidth, aTargetHeight);
        }
      }
    }
    return Collections.emptyList();
  }

  private static List<ALcdBasicImage> getImages(ALcdImageMosaic aImageMosaic, int aTargetWidth, int aTargetHeight) {
    ALcdImageMosaic.Configuration cfg = aImageMosaic.getConfiguration();
    int numTilesX = (int) Math.ceil(aTargetWidth * cfg.getPixelSizeX() / cfg.getColumnWidth());
    int numTilesY = (int) Math.ceil(aTargetHeight * cfg.getPixelSizeY() / cfg.getRowHeight());
    int halfTilesX = (numTilesX + 1) / 2;
    int halfTilesY = (numTilesY + 1) / 2;
    long x0 = Math.max(0, Math.min(cfg.getColumnCount() - 1, cfg.getColumnCount() / 2 - halfTilesX));
    long x1 = Math.max(0, Math.min(cfg.getColumnCount() - 1, cfg.getColumnCount() / 2 + halfTilesX));
    long y0 = Math.max(0, Math.min(cfg.getRowCount() - 1, cfg.getRowCount() / 2 - halfTilesY));
    long y1 = Math.max(0, Math.min(cfg.getRowCount() - 1, cfg.getRowCount() / 2 + halfTilesY));
    ArrayList<ALcdBasicImage> result = new ArrayList<>();
    for (long x = x0; x <= x1; x++) {
      for (long y = y0; y <= y1; y++) {
        ALcdBasicImage image = aImageMosaic.getTile(x, y);
        if (image != null) {
          result.add(image);
        }
      }
    }
    return result;
  }
}
