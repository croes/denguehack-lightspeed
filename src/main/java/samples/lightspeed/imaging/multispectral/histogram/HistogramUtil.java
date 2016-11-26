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
package samples.lightspeed.imaging.multispectral.histogram;

import java.awt.Rectangle;
import java.awt.image.Raster;

import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImagingEngine;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.imaging.operator.TLcdHistogramOp;
import com.luciad.imaging.operator.TLcdPixelTransformOp;
import com.luciad.model.ILcdModel;

import samples.lightspeed.imaging.multispectral.general.ImageOperatorUtil;

/**
 * Computes a Histogram for each band from the first model object that is an ALcdBasicImage, ALcdMultilevelImage,
 * ALcdImageMosaic, or ALcdMultilevelImageMosaic in the layer's model.
 */
public class HistogramUtil {

  private static final int MAX_IMAGE_SIZE = 1024;
  private static final int HISTOGRAM_SIZE = 256;

  private HistogramUtil() {
  }

  public static Histogram[] getLuminanceHistogram(final ILcdModel aModel, final int[] aSelectedBands) {
    final ALcdBasicImage image = ImageOperatorUtil.getImage(aModel, MAX_IMAGE_SIZE);
    if (image == null) {
      return null;
    }

    ALcdBasicImage rgbImage = (ALcdBasicImage) TLcdBandSelectOp.bandSelect(image, aSelectedBands);

    ALcdImagingEngine engine = ALcdImagingEngine.createEngine();
    Histogram[] histograms;
    try {
      histograms = new Histogram[1];
      ALcdBasicImage luminanceImage = createLuminanceImage(rgbImage);
      luminanceImage = (ALcdBasicImage) TLcdBandSelectOp.bandSelect(luminanceImage, new int[]{0});
      histograms[0] = createHistogram(luminanceImage, engine, HISTOGRAM_SIZE);
    } finally {
      // Engine not needed anymore
      engine.dispose();
    }

    return histograms;
  }

  public static ALcdBasicImage createLuminanceImage(ALcdBasicImage aImage) {
    // Put luminance in band 0, 1, and 2.
    int nbBands = aImage.getConfiguration().getSemantics().size();
    if (nbBands == 3) {
      return (ALcdBasicImage) TLcdPixelTransformOp.pixelTransform(aImage, new double[]{
          0.2126, 0.7152, 0.0722,
          0.2126, 0.7152, 0.0722,
          0.2126, 0.7152, 0.0722
      }, new double[]{0, 0, 0});
    } else if (nbBands == 4) {
      return (ALcdBasicImage) TLcdPixelTransformOp.pixelTransform(aImage, new double[]{
          0.2126, 0.7152, 0.0722, 0,
          0.2126, 0.7152, 0.0722, 0,
          0.2126, 0.7152, 0.0722, 0,
          0, 0, 0, 1
      }, new double[]{0, 0, 0, 0});
    } else {
      throw new IllegalArgumentException("Can only handle RGB and RGBA images.");
    }
  }

  /**
   * Query the histograms for the model.
   */
  public static Histogram[] getHistogramsPerBand(final ILcdModel aModel) {
    final ALcdBasicImage image = ImageOperatorUtil.getImage(aModel, MAX_IMAGE_SIZE);
    if (image == null) {
      return null;
    }

    int numBands = image.getConfiguration().getSemantics().size();

    ALcdImagingEngine engine = ALcdImagingEngine.createEngine();
    Histogram[] histograms = new Histogram[numBands];
    try {
      for (int i = 0; i < numBands; i++) {
        ALcdBasicImage bandImage = (ALcdBasicImage) TLcdBandSelectOp.bandSelect(image, new int[]{i});
        histograms[i] = createHistogram(bandImage, engine, HISTOGRAM_SIZE);
      }
    } finally {
      // Engine not needed anymore
      engine.dispose();
    }
    return histograms;
  }

  /**
   * Creates a histogram.
   *
   * @param aImage         the image
   * @param aEngine        the imaging engine
   * @param aHistogramSize the histogram size
   *
   * @return the histogram
   */
  public static Histogram createHistogram(ALcdBasicImage aImage, ALcdImagingEngine aEngine, int aHistogramSize) {
    ALcdBasicImage histogramImage = (ALcdBasicImage) TLcdHistogramOp.histogram(aImage, aHistogramSize);
    Raster histogramRaster = aEngine.getImageDataReadOnly(
        histogramImage, new Rectangle(0, 0, aHistogramSize, 1)
    );
    int[] binValues = new int[aHistogramSize];
    for (int j = 0; j < binValues.length; j++) {
      binValues[j] = histogramRaster.getSample(j, 0, 0);
    }
    return new Histogram(binValues);
  }

  /**
   * Callback to notify that a histogram has been computed.
   */
  public static interface Callback {
    public void histogramsAvailable(Histogram[] aHistograms, Object aSource);

    public void histogramNotAvailable();
  }

}
