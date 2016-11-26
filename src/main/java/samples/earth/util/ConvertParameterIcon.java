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
package samples.earth.util;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Arrays;

import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.util.ILcdOriented;

/**
 * This icon implementation scales and/or translates the parameter values that are passed to its methods.
 *
 * @since 2015.0
 */
public class ConvertParameterIcon implements ILcdParameterizedIcon, ILcdOriented {

  private final ILcdParameterizedIcon fDelegate;
  private final double[] fScale;
  private final double[] fTranslate;

  /**
   * Creates a new icon that scales and/or translates the parameters for the given delegate icon. Scaling is performed
   * before translating.
   * @param aDelegate  the delegate icon.
   * @param aScale     an array of scale values. If the length of the given array is smaller than the amount of parameters
   *                   passed to the icon methods, only the first parameters are scaled. If the length of the given array
   *                   is larger than the amount of parameters passed to the icon methods, the last scale values are
   *                   ignored. Can be {@code null}.
   * @param aTranslate an array of translate values. If the length of the given array is smaller than the amount of parameters
   *                   passed to the icon methods, only the first parameters are translated. If the length of the given array
   *                   is larger than the amount of parameters passed to the icon methods, the last translation values are
   *                   ignored. Can be {@code null}.
   */
  public ConvertParameterIcon(ILcdParameterizedIcon aDelegate, double[] aScale, double[] aTranslate) {
    fDelegate = aDelegate;
    fScale = aScale == null ? null : Arrays.copyOf(aScale, aScale.length);
    fTranslate = aTranslate == null ? null : Arrays.copyOf(aTranslate, aTranslate.length);
  }

  private double[] getConvertedValues(double[] aOriginalValue) {
    double[] newValues = Arrays.copyOf(aOriginalValue, aOriginalValue.length);
    if (fScale != null) {
      for (int i = 0; i < fScale.length; i++) {
        double scale = fScale[i];
        if (i < newValues.length) {
          newValues[i] *= scale;
        }
      }
    }
    if (fTranslate != null) {
      for (int i = 0; i < fTranslate.length; i++) {
        double translate = fTranslate[i];
        if (i < newValues.length) {
          newValues[i] += translate;
        }
      }
    }
    return newValues;
  }

  @Override
  public void anchorPointSFCT(double[] aParameters, Point aPointSFCT) {
    double[] convertedValues = getConvertedValues(aParameters);
    fDelegate.anchorPointSFCT(convertedValues, aPointSFCT);
  }

  @Override
  public void paintIcon(Graphics aGraphics, int aX, int aY, double[] aParameters) {
    double[] convertedValues = getConvertedValues(aParameters);
    fDelegate.paintIcon(aGraphics, aX, aY, convertedValues);
  }

  @Override
  public int getWidth(double[] aParameters) {
    double[] convertedValues = getConvertedValues(aParameters);
    return fDelegate.getWidth(convertedValues);
  }

  @Override
  public int getHeight(double[] aParameters) {
    double[] convertedValues = getConvertedValues(aParameters);
    return fDelegate.getHeight(convertedValues);
  }

  @Override
  public double getOrientation() {
    return fDelegate instanceof ILcdOriented ? ((ILcdOriented) fDelegate).getOrientation() : Double.NaN;
  }
}
