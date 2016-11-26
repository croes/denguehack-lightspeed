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
package samples.lightspeed.imaging.multispectral.normalize;

import java.awt.image.Raster;
import java.util.List;

import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.model.ILcdModel;

import samples.lightspeed.imaging.multispectral.general.ImageOperatorUtil;

/**
 * Calculates the shifts and scales that can be used to normalize an image.
 */
public class NormalizationUtil {

  private static final int MAX_IMAGE_SIZE = 1024;

  public static double[][] calculateNormalizingPixelRescale(ILcdModel aModel) {
    final ALcdBasicImage image = ImageOperatorUtil.getImage(aModel, MAX_IMAGE_SIZE);
    List<ALcdBandSemantics> semantics = image.getConfiguration().getSemantics();

    Raster data = ImageOperatorUtil.getImageData(image);

    int nrOfBands = semantics.size();
    double[] scales = new double[nrOfBands];
    double[] offsets = new double[nrOfBands];

    for (int band = 0; band < nrOfBands; band++) {
      ALcdBandSemantics bandSemantics = semantics.get(band);
      Number noDataValue = bandSemantics.getNoDataValue();

      double minValue = Double.MAX_VALUE;
      double maxValue = -Double.MAX_VALUE;
      long noDataLongBits = noDataValue != null ? Double.doubleToLongBits(noDataValue.doubleValue()) : 0;
      for (int y = 0; y < data.getHeight(); y++) {
        for (int x = 0; x < data.getWidth(); x++) {
          double value = data.getSample(x, y, band);
          if (noDataValue == null || Double.doubleToLongBits(value) != noDataLongBits) {
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
          }
        }
      }

      ALcdBandSemantics.DataType dataType = bandSemantics.getDataType();
      if (bandSemantics.getDataType() == ALcdBandSemantics.DataType.FLOAT || bandSemantics.getDataType() == ALcdBandSemantics.DataType.DOUBLE) {
        // Map to [0,1]
        scales[band] = getScale(minValue, maxValue, 0.0, 1.0);
        offsets[band] = getOffset(minValue, 0.0, scales[band]);
      } else {
        // Map to full data type range
        double minDataValue = dataType.isSigned() ? -(1L << (bandSemantics.getNumSignificantBits() - 1)) : 0;
        double maxDataValue = dataType.isSigned() ? (1L << (bandSemantics.getNumSignificantBits() - 1)) - 1 : (1L << bandSemantics.getNumSignificantBits()) - 1;
        if (!bandSemantics.isNormalized()) {
          double normalizedMin = bandSemantics.getNormalizedRangeMinValue().doubleValue();
          double normalizedMax = bandSemantics.getNormalizedRangeMaxValue().doubleValue();
          double normalizeScale = getScale(minDataValue, maxDataValue, normalizedMin, normalizedMax);
          double normalizeOffset = getOffset(minDataValue, normalizedMin, normalizeScale);
          minValue = minValue * normalizeScale + normalizeOffset;
          maxValue = maxValue * normalizeScale + normalizeOffset;
          minDataValue = normalizedMin;
          maxDataValue = normalizedMax;
        }
        scales[band] = getScale(minValue, maxValue, minDataValue, maxDataValue);
        offsets[band] = getOffset(minValue, minDataValue, scales[band]);
      }
    }

    return new double[][]{scales, offsets};
  }

  private static double getScale(double aMin1, double aMax1, double aMin2, double aMax2) {
    return (aMax2 - aMin2) / (aMax1 - aMin1);
  }

  private static double getOffset(double aMin1, double aMin2, double aScale) {
    return aMin2 - aMin1 * aScale;
  }

  public interface Callback {
    void parametersAvailable(double[] aScales, double[] aOffsets);
  }
}
