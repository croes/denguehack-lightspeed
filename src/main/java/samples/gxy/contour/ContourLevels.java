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
package samples.gxy.contour;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.format.raster.TLcdDTEDTileDecoder;

/**
 * Utility representing the contour levels, colors and labels for the contour finder samples.
 */
public class ContourLevels {

  private static double[] sLEVELVALUES = new double[]{0.01, 350, 700, 1500, 2500, 3500};
  private static ColorModel sLEVELCOLORMODEL = (IndexColorModel) TLcdDTEDColorModelFactory.getSharedInstance().createColorModel();
  private static double[] sSPECIALVALUES = new double[]{TLcdDTEDTileDecoder.UNKNOWN_ELEVATION};
  private static Color[] sSPECIALCOLORS = new Color[]{Color.blue};
  private static String[] sSPECIALLABELS = new String[]{"Unknown"};

  private double[] fLevelValuesExtended;
  private Color[] fLevelColors;
  private Color[] fLevelColorsRanged;
  private String[] fLevelLabels;
  private String[] fLevelLabelsRanged;

  /**
   * Get the values for level contours.
   * @param aExtended True to have -infinity added to the front of the array and +infinity at the
   * back, false to get the result without those. If true, the array size is two larger.
   * @return The level values.
   */
  public double[] getLevelValues(boolean aExtended) {
    if (aExtended) {
      //add -infinity and +infinity to the array
      if (fLevelValuesExtended == null) {
        fLevelValuesExtended = new double[sLEVELVALUES.length + 2];
        fLevelValuesExtended[0] = Double.NEGATIVE_INFINITY;
        System.arraycopy(sLEVELVALUES, 0, fLevelValuesExtended, 1, sLEVELVALUES.length);
        fLevelValuesExtended[fLevelValuesExtended.length - 1] = Double.POSITIVE_INFINITY;
      }
      return fLevelValuesExtended;
    } else {
      //the basic level values can be returned unmodified
      return sLEVELVALUES;
    }
  }

  /**
   * Get the colors for level contours. The amount of colors is not necessarily equal to the
   * amount of values.
   * @param aRanged False when the colors are created for isolines on the levels, false to get
   * colors for ranges between each level value, before the first and after the last level value. If
   * true, the array size is one larger.
   * @return The level colors.
   */
  public Color[] getLevelColors(boolean aRanged) {
    if (aRanged) {
      //create colors for area contours
      if (fLevelColorsRanged == null) {
        double[] levels = getLevelValues(false);

        fLevelColorsRanged = new Color[levels.length + 1];
        for (int i = 0; i < fLevelColorsRanged.length; i++) {
          double level;
          //If the colors are used for a range, take the average color between this and the next level to better approximate the average value in that range
          if (i == 0) {
            level = levels[0];
          } else if (i == levels.length) {
            level = levels[i - 1];
          } else {
            level = (levels[i - 1] + levels[i]) / 2;
          }

          fLevelColorsRanged[i] = new Color(sLEVELCOLORMODEL.getRGB((int) level));
        }
      }
      return fLevelColorsRanged;
    } else {
      //create colors for isolines
      if (fLevelColors == null) {
        double[] levels = getLevelValues(false);

        fLevelColors = new Color[levels.length];
        for (int i = 0; i < fLevelColors.length; i++) {
          double level;
          level = levels[i];
          fLevelColors[i] = new Color(sLEVELCOLORMODEL.getRGB((int) level));
        }
      }
      return fLevelColors;
    }
  }

  /**
   * Get the labels for level contours. The amount of labels is not necessarily equal to the
   * amount of values.
   * @param aRanged False when the colors are created for isolines on the levels, false to get
   * labels for ranges between each level value, before the first and after the last level value. If
   * true, the array size is one larger.
   * @return The level colors.
   */
  public String[] getLevelLabels(boolean aRanged) {
    if (aRanged) {
      //create ranged labels for area contours
      if (fLevelLabelsRanged == null) {
        double[] levels = getLevelValues(false);

        fLevelLabelsRanged = new String[levels.length + 1];
        for (int i = 0; i < fLevelLabelsRanged.length; i++) {

          if (i == 0) {
            fLevelLabelsRanged[i] = "< " + levels[0];
          } else if (i == levels.length) {
            fLevelLabelsRanged[i] = "> " + levels[i - 1];
          } else {
            fLevelLabelsRanged[i] = "" + levels[i - 1] + " - " + levels[i];
          }
        }
      }

      return fLevelLabelsRanged;
    } else {
      //create labels for isolines
      if (fLevelLabels == null) {
        double[] levels = getLevelValues(false);

        fLevelLabels = new String[levels.length];
        for (int i = 0; i < fLevelLabels.length; i++) {

          fLevelLabels[i] = "" + levels[i];
        }
      }

      return fLevelLabels;
    }

  }

  /**
   * Get the values for special contours.
   * @return The special values.
   */
  public double[] getSpecialValues() {
    return sSPECIALVALUES;
  }

  /**
   * Get the colors for special contours.
   * @return The special colors.
   */
  public Color[] getSpecialColors() {
    return sSPECIALCOLORS;
  }

  /**
   * Get the labels for special contours.
   * @return The special labels.
   */
  public String[] getSpecialLabels() {
    return sSPECIALLABELS;
  }
}
