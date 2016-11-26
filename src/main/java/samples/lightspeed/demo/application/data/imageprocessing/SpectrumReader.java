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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;

import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdImagingEngine;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdDisposable;

/**
 * Allows reading the values from a rectangle region in a 7-band LandSat7 image.
 */
class SpectrumReader implements ILcdDisposable {

  private ALcdBasicImage fImage;
  private double fPixelScaleX, fPixelScaleY;
  private ILcdPoint fImageLocation;
  private ALcdImagingEngine fImagingEngine;

  public SpectrumReader() {
    fImagingEngine = ALcdImagingEngine.createEngine();
  }

  /**
   * Get the spectrum values at a specific location. Multiple pixels around the location are sampled.
   *
   * @param aPoint the location in model coordinates
   * @return a float array containing the spectrum values
   */
  public List<float[]> retrieveSpectrumFromArea(ILcdPoint aPoint) {
    if (fImagingEngine == null) {
      throw new IllegalStateException("Already disposed.");
    }

    Point pixel = modelPointToPixel(aPoint);
    int x = (int) pixel.getX();
    int y = (int) pixel.getY();

    return getSpectrumAtPixel(x - 1, y - 1, 3, 3);
  }

  /**
   * Convert a point in model coordinates to pixel coordinates in the image.
   *
   * @param aPoint the point in model coordinates.
   * @return the location in pixel coordinates
   */
  private Point modelPointToPixel(ILcdPoint aPoint) {
    int x = (int) ((aPoint.getX() - fImageLocation.getX()) * fPixelScaleX);
    int y = (int) ((aPoint.getY() - fImageLocation.getY()) * fPixelScaleY);

    y = fImage.getConfiguration().getHeight() - 1 - y;
    return new Point(x, y);
  }

  /**
   * Get the spectrum values for the requested pixel and the area around it if necessary.
   *
   * @param aX the x coordinate of the center pixel
   * @param aY the y coordinate of the center pixel
   * @param aWidth the width of the area that must be sampled
   * @param aHeight the height of the area that must be sampled
   * @return a list with all the spectrum values for te requested area
   */
  private List<float[]> getSpectrumAtPixel(int aX, int aY, int aWidth, int aHeight) {
    List<float[]> list = new ArrayList<float[]>();

    Rectangle imageROI = new Rectangle(0, 0, fImage.getConfiguration().getWidth(), fImage.getConfiguration().getHeight());
    Rectangle roi = new Rectangle(aX, aY, aWidth, aHeight);
    if (!imageROI.contains(roi)) {
      return list;
    }

    //split in 2 images to be able to read all bands
    ALcdImage bands1234 = TLcdBandSelectOp.bandSelect(fImage, new int[]{0, 1, 2, 3});
    ALcdImage bands456 = TLcdBandSelectOp.bandSelect(fImage, new int[]{4, 5, 6});

    float[] valuesBand1234 = new float[4];
    float[] valuesBand567 = new float[3];

    Raster raster1 = fImagingEngine.getImageDataReadOnly((ALcdBasicImage) bands1234, roi);
    Raster raster2 = fImagingEngine.getImageDataReadOnly((ALcdBasicImage) bands456, roi);

    for (int j = 0; j < aHeight; j++) {
      for (int i = 0; i < aWidth; i++) {
        raster1.getPixel(raster1.getMinX() + i, raster1.getMinY() + j, valuesBand1234);
        raster2.getPixel(raster2.getMinX() + i, raster2.getMinY() + j, valuesBand567);

        float[] result = new float[7];
        System.arraycopy(valuesBand1234, 0, result, 0, valuesBand1234.length);
        System.arraycopy(valuesBand567, 0, result, valuesBand1234.length, valuesBand567.length);
        list.add(result);
      }
    }

    return list;
  }

  @Override
  public void dispose() {
    if (fImagingEngine != null) {
      fImagingEngine.dispose();
      fImagingEngine = null;
    }
  }

  public ALcdBasicImage getImage() {
    return fImage;
  }

  public void setImage(ALcdBasicImage aImage) {
    fImage = aImage;
    fPixelScaleX = 1.0 / fImage.getConfiguration().getPixelSizeX();
    fPixelScaleY = 1.0 / fImage.getConfiguration().getPixelSizeY();
    fImageLocation = fImage.getBounds().getLocation();
  }
}

