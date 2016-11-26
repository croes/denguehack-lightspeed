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
package samples.tea.gxy.los;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.tea.TLcdLOSPropagationFunctionFixedHeight;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;

import java.awt.Color;
import java.awt.Graphics;

/**
 * A painter for line of sight. It supports both raster and contour objects. Depending on the type
 * of object, the painting is delegated to a raster or a contour painter.
 */
public class LOSPainter extends ALcdGXYPainter {

  private LOSContourPainter fContourPainter = new LOSContourPainter(new double[0], new Color[0]);
  private LOSRasterPainter fRasterPainter = new LOSRasterPainter();
  private ILcdGXYPainter fActivePainter; // the painter that is currently being used

  /**
   * Interval levels for line-of-sight.
   */
  private static double[] LOS_LEVELS_INTERVAL = new double[] {
          1, 50, 100, 250,
  };
  /**
   * Special levels for line of sight, that is, any value that isn't a height but indicates a special condition.
   */
  private static double[] LOS_LEVELS_SPECIAL = new double[] {
          ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,  // value used for unknown visibilities
          //Note: the value ILcdLineOfSightCoverage.LOS_INVISIBLE_VALUE could be included to get its own color, but this is not done here, it is not painted at all instead.
  };

  /**
   * Alpha value for the LoS colors. Only the contour painter uses it in this sample, the raster painter ignores it.
   */
  private static int ALPHA = 160;

  /**
   * The colors for regular line-of-sight.
   */
  private static Color[] LOS_COLORS_INTERVAL = new Color[] {
          new Color(255, 0, 0, ALPHA ),
          new Color( 220, 100, 10, ALPHA ),
          new Color(255, 255, 0, ALPHA ),
          new Color(0, 255, 0, ALPHA ),
          new Color( 20, 100, 30, ALPHA ),
  };
  private static Color[] LOS_COLORS_SPECIAL = new Color[] {
          new Color(0, 0, 255, ALPHA ),                                 // color used for unknown visibilities
  };

  /**
   * The levels for fixed height line-of-sight.
   */
  private static double[] LOS_LEVELS_FixedHeight = new double[] {
          TLcdLOSPropagationFunctionFixedHeight.UNKNOWN,          // value used for unknown visibilities
          TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE,  // value used for cone of silence
          TLcdLOSPropagationFunctionFixedHeight.VISIBLE,          // value used for visibilities
  };

  /**
   * The colors for fixed height line-of-sight.
   */
  private static Color[] LOS_COLORS_FixedHeight = new Color[] {
          Color.blue,                                 // value used for unknown visibilities
          Color.red,                                  // value used for cone of silence
          Color.green,                                // value used for visibilities
  };

  /**
   * The labels for fixed height line-of-sight.
   */
  static String[] LOS_LABELS_FixedHeight = new String[] {
          "Unknown", "Cone of Silence", "Visible",
  };


  public void setObject( Object aObject ) {
    if(fContourPainter.canSetObject( aObject )) {
      fActivePainter = fContourPainter;
    }
    else {
      fActivePainter = fRasterPainter;
    }

    fActivePainter.setObject( aObject );
  }

  public Object getObject() {
    return fActivePainter.getObject();
  }

  public void paint( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {
    fActivePainter.paint( aGraphics, aMode, aGXYContext );
  }

  public void boundsSFCT( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT ) throws TLcdNoBoundsException {
    fActivePainter.boundsSFCT( aGraphics, aMode, aGXYContext, aBoundsSFCT );
  }

  public boolean isTouched( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {
    return fActivePainter.isTouched( aGraphics, aMode, aGXYContext );
  }

  public LOSContourPainter getContourPainter() {
    return fContourPainter;
  }

  public LOSRasterPainter getRasterPainter() {
    return fRasterPainter;
  }

  // Change the computation algorithm, adjust the color settings.
  public void setComputationAlgorithm( int aComputationAlgorithm ) {

    if(aComputationAlgorithm ==  LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT) {
      fContourPainter.setColors(LOSPainter.LOS_LEVELS_FixedHeight, LOSPainter.LOS_COLORS_FixedHeight);
    } else {
      fContourPainter.setColors(LOSPainter.getLosLevelsInterval(), LOSPainter.getLosColorsInterval(), LOSPainter.getLosLevelsSpecial(), LOSPainter.getLosColorsSpecial());
    }

    fRasterPainter.setComputationAlgorithm( aComputationAlgorithm );
  }
  
  private static double[] concatenate(double[] aArray1, double[] aArray2) {
    double[] result = new double[aArray1.length + aArray2.length];
    System.arraycopy( aArray1, 0, result, 0, aArray1.length );
    System.arraycopy( aArray2, 0, result, aArray1.length, aArray2.length );
    return result;
  }
  
  private static Color[] concatenate(Color[] aArray1, Color[] aArray2) {
    Color[] result = new Color[aArray1.length + aArray2.length];
    System.arraycopy( aArray1, 0, result, 0, aArray1.length );
    System.arraycopy( aArray2, 0, result, aArray1.length, aArray2.length );
    return result;
  }

  /**
   * @return both the special and interval LoS levels, for the raster painter and legend.
   */
  public static double[] getLosLevelsAll() {
    //An extra Double.POSITIVE_INFINITY is added at the end to have an interval for values above 250 meter. Adding a low value in front isn't needed because the legend and raster painter treat the first value as an interval from -infinity to that value.
    return concatenate( LOS_LEVELS_SPECIAL, concatenate(LOS_LEVELS_INTERVAL, new double[]{Double.POSITIVE_INFINITY}) );
  }

  /**
   * @return both the special and interval LoS colors, for the raster painter and legend.
   */
  public static Color[] getLosColorsAll() {
    return concatenate( LOS_COLORS_SPECIAL, LOS_COLORS_INTERVAL );
  }

  /**
   * @return the interval LoS levels, with extra lowest value in front, for the contour painter.
   */
  public static double[] getLosLevelsInterval() {
    //An extra -20000 value is added to have an interval for values below 1 meter. -20000 is the minimum value that isn't treated as a special value by the contour finder.
    //An extra Double.POSITIVE_INFINITY is added at the end to have an interval for values above 250 meter.
    return concatenate( new double[]{-20000}, concatenate(LOS_LEVELS_INTERVAL, new double[]{Double.POSITIVE_INFINITY}) );
  }

  /**
   * @return the interval LoS colors, for the contour painter.
   */
  public static Color[] getLosColorsInterval() {
    return LOS_COLORS_INTERVAL;
  }

  /**
   * @return the special LoS levels, for the contour painter.
   */
  public static double[] getLosLevelsSpecial() {
    return LOS_LEVELS_SPECIAL;
  }

  /**
   * @return the special LoS colors, for the contour painter.
   */
  public static Color[] getLosColorsSpecial() {
    return LOS_COLORS_SPECIAL;
  }

  /**
   * @return the special levels for fixed height (there are no interval levels for fixed height)
   */
  public static double[] getLosLevelsFixedHeight() {
    return LOS_LEVELS_FixedHeight;
  }

  /**
   * @return the special colors for fixed height (there are no interval colors for fixed height)
   */
  public static Color[] getLosColorsFixedHeight() {
    return LOS_COLORS_FixedHeight;
  }
}
