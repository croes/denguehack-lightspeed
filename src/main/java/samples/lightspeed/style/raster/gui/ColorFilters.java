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
package samples.lightspeed.style.raster.gui;

import java.awt.Color;

import com.luciad.util.ILcdColorFilter;

/**
 * Some example color filters.
 */
public class ColorFilters {

  private ColorFilters() {
  }

  /**
   * Creates a no-op filter: {@code outColor = inColor}.
   *
   * @return a no-op filter
   */
  public static ILcdColorFilter noOp() {
    return new ILcdColorFilter() {
      @Override
      public void apply(float[] aRGBAColorSFCT) {
      }
    };
  }

  /**
   * Creates a negative filter: {@code outColor = 1.0 - inColor}.
   *
   * @return a negative filter
   */
  public static ILcdColorFilter negative() {
    return new ILcdColorFilter() {
      @Override
      public void apply(float[] aRGBAColorSFCT) {
        aRGBAColorSFCT[0] = 1.f - aRGBAColorSFCT[0];
        aRGBAColorSFCT[1] = 1.f - aRGBAColorSFCT[1];
        aRGBAColorSFCT[2] = 1.f - aRGBAColorSFCT[2];
      }
    };
  }

  /**
   * Creates a filter that adjusts the hue/saturation/brightness of a color.
   *
   * @param aHue        the hue in {@code [0, 1]} where {@code 0 or 1} means no change
   * @param aSaturation the saturation in {@code [0, 1]} where {@code 1} means no change
   * @param aBrightness the brightness in {@code [0, 1]} where {@code 1} means no change
   *
   * @return a hue-saturation-brightness filter
   *
   * @see Color#RGBtoHSB(int, int, int, float[])
   * @see Color#getHSBColor(float, float, float)
   */
  public static ILcdColorFilter hueSaturationBrightness(final float aHue, final float aSaturation, final float aBrightness) {
    return new ILcdColorFilter() {
      @Override
      public void apply(float[] aRGBAColorSFCT) {
        final float[] hsb = new float[3];
        Color.RGBtoHSB((int) (aRGBAColorSFCT[0] * 255 + 0.5), (int) (aRGBAColorSFCT[1] * 255 + 0.5), (int) (aRGBAColorSFCT[2] * 255 + 0.5), hsb);
        hsb[0] += aHue;
        hsb[1] = Math.min(hsb[1] * aSaturation, 1.f);
        hsb[2] = Math.min(hsb[2] * aBrightness, 1.f);
        Color color = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        color.getRGBColorComponents(aRGBAColorSFCT);
      }
    };
  }

  /**
   * Creates a filter that hides a certain color.
   *
   * @param aColor     the color to hide
   * @param aThreshold the threshold for hiding in [0,1]
   *
   * @return a color hiding filter
   */
  public static ILcdColorFilter hideColor(final Color aColor, final float aThreshold) {
    final float[] refHsb = Color.RGBtoHSB(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), null);
    return new ILcdColorFilter() {
      @Override
      public void apply(float[] aRGBAColorSFCT) {
        final float[] hsb = new float[3];
        Color.RGBtoHSB((int) (aRGBAColorSFCT[0] * 255 + 0.5), (int) (aRGBAColorSFCT[1] * 255 + 0.5), (int) (aRGBAColorSFCT[2] * 255 + 0.5), hsb);

        float dHue = refHsb[0] - hsb[0];
        float dSat = (refHsb[1] - hsb[1]) / 2f;
        float dBri = (refHsb[2] - hsb[2]) / 2f;
        float distance = (float) Math.sqrt(dHue * dHue + dSat * dSat + dBri * dBri);
        if (distance < aThreshold) {
          float relAngle = (aThreshold - distance) / aThreshold;
          float w = Math.max(0.f, 1.f - relAngle * 5.f);
          aRGBAColorSFCT[3] *= w; // make colors that do no match more transparent
        }
      }
    };
  }

}
