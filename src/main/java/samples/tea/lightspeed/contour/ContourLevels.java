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
package samples.tea.lightspeed.contour;

import java.awt.Color;

import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;

class ContourLevels {

  private static final Color[] CONTOUR_COLORS = new Color[]{
      Color.BLUE,
      new Color(140, 150, 210),
      new Color(132, 178, 100),
      new Color(237, 181, 79),
      new Color(229, 108, 53),
      new Color(154, 78, 46),
      new Color(120, 58, 30),
      new Color(90, 43, 22),
      new Color(45, 20, 10),
  };

  private static final Color[] CONTOUR_COLORS_INTERVAL = new Color[]{
      Color.BLUE,
      new Color(140, 150, 210),
      new Color(132, 178, 100),
      new Color(237, 181, 79),
      new Color(229, 108, 53),
      new Color(154, 78, 46),
      new Color(120, 58, 30),
      new Color(90, 43, 22),
      new Color(45, 20, 10),
      Color.BLUE
  };

  private static final double[] CONTOUR_LEVELS = new double[]{
      0.01, 360.0, 700, 1050, 1400, 2100, 2800, 5000
  };

  private static final double[] CONTOUR_LEVELS_POLYLINE = new double[]{
      0.01, 350, 700, 1500, 2500, 3500
  };

  private static final double[] CONTOUR_LEVELS_INTERVAL = new double[]{
      -20000, 0.01, 360.0, 700, 1050, 1400, 2100, 2800, 5000
  };

  private static final double[] CONTOUR_LEVELS_SPECIAL = new double[]{
      ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,    // value used for unknown visibilities
  };

  private static final Color[] CONTOUR_COLORS_SPECIAL = new Color[]{
      Color.BLUE,
  };

  static {
    double[] contourLevels = getContourLevelsInterval();
    COLORMAP = new TLcdColorMap(new TLcdInterval(contourLevels[0], contourLevels[contourLevels.length - 1]),
                                contourLevels,
                                getContourColorsInterval());
  }

  private static final TLcdColorMap COLORMAP;

  private ContourLevels() {}

  public static TLcdColorMap getColorMap() {
    return COLORMAP;
  }

  public static Color[] getContourColors() {
    return CONTOUR_COLORS;
  }

  private static Color[] getContourColorsInterval() {
    return CONTOUR_COLORS_INTERVAL;
  }

  public static double[] getContourLevelsInterval() {
    return CONTOUR_LEVELS_INTERVAL;
  }

  public static double[] getContourLevels() {
    return CONTOUR_LEVELS;
  }

  public static double[] getContourLevelsPolyline() {
    return CONTOUR_LEVELS_POLYLINE;
  }

  public static double[] getContourLevelsSpecial() {
    return CONTOUR_LEVELS_SPECIAL;
  }

  public static Color[] getContourColorsSpecial() {
    return CONTOUR_COLORS_SPECIAL;
  }

}
