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
package samples.fusion.client.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import com.luciad.earth.util.TLcdEarthMultivaluedDataParameter;
import com.luciad.earth.view.gxy.util.ALcdEarth2DVectorIcon;
import com.luciad.earth.view.gxy.util.ALcdEarthParameterizedIcon;
import com.luciad.earth.view.gxy.util.TLcdEarthWindIcon;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.util.ILcdOriented;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdInterval;

/**
 * Creates mappings from one or more multi-valued data parameters to a visual representation.
 */
public class MultivaluedDataUtil {
  private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

  // The temperatures (expressed in Kelvin) and their colors.
  private static final double[] COLOR_MODEL_TEMPERATURES = {0, 273.15 - 60.0, 273.15 - 30.0, 273.15 - 20.0,
                                                            273.15 - 10.0, 273.15 + 0.0, // 0 deg Celsius
                                                            273.15 + 5.0, 273.15 + 15.0, 273.15 + 25.0, 273.15 + 35.0,
                                                            273.15 + 100.0,};

  private static final Color[] COLOR_MODEL_COLORS = {new Color(0, 0, 0),          // black
                                                     new Color(255, 0, 255),      // purple
                                                     new Color(128, 0, 255), new Color(0, 0, 255),        // blue
                                                     new Color(0, 128, 255),      // light blue
                                                     new Color(128, 255, 255), new Color(255, 255, 128),
                                                     new Color(255, 255, 0),      // yellow
                                                     new Color(255, 128, 0),      // orange
                                                     new Color(255, 0, 0),        // red
                                                     new Color(128, 0, 0),        // dark red
  };

  private MultivaluedDataUtil() {
  }

  public static TLcdColorMap createColorMap(TLcdEarthMultivaluedDataParameter aParameter) {
    if (isPercentage(aParameter)) {
      return createColorMap(aParameter, 0.0, 100.0, TRANSPARENT, Color.BLUE);
    } else if (isTemperature(aParameter)) {
      return createColorMap(aParameter, COLOR_MODEL_TEMPERATURES, COLOR_MODEL_COLORS); // default coloring
    }
    return createColorMap(aParameter, 0.0, 255.0, Color.GREEN, Color.RED); // default coloring
  }

  private static TLcdColorMap createColorMap(TLcdEarthMultivaluedDataParameter aParameter, double aMin, double aMax,
                                             Color aMinColor, Color aMaxColor) {
    return createColorMap(aParameter, new double[]{aMin, aMax}, new Color[]{aMinColor, aMaxColor});
  }

  private static TLcdColorMap createColorMap(TLcdEarthMultivaluedDataParameter aParameter, double[] aValues,
                                             Color[] aColors) {
    return new ColorMapWithNaN(new TLcdInterval(aValues[0], aValues[aValues.length - 1],
                                                aParameter.getName() + " (" + aParameter.getUnit() + ")"), aValues,
                               aColors);
  }

  public static ILcdParameterizedIcon createParameterizedIcon(
      List<TLcdEarthMultivaluedDataParameter> aParameters) {
    if (aParameters.size() == 2 && isMetersPerSecond(aParameters.get(0)) && isMetersPerSecond(aParameters.get(1))) {
      TLcdEarthWindIcon windIcon = new TLcdEarthWindIcon(ALcdEarth2DVectorIcon.Parameterization.STRENGTH_2D);
      double metersPerSecondToKnots = 3.600 / TLcdConstant.NM2KM; // conversion factor for m/s to knots
      return new ScaledParameterizedIcon(windIcon, -metersPerSecondToKnots,
                                         -metersPerSecondToKnots); // the direction should be where the wind blows from
    }
    return new TextParameterizedIcon(aParameters); // default formatting
  }

  private static boolean isPercentage(TLcdEarthMultivaluedDataParameter aParameter) {
    return aParameter.getUnit().equalsIgnoreCase("%");
  }

  private static boolean isTemperature(TLcdEarthMultivaluedDataParameter aParameter) {
    return aParameter.getUnit().equalsIgnoreCase("K"); // Kelvin
  }

  private static boolean isMetersPerSecond(TLcdEarthMultivaluedDataParameter aParameter) {
    return aParameter.getUnit().equalsIgnoreCase("m/s") || aParameter.getUnit().equals("m s-1");
  }

  /**
   * A color map that also has a color for <code>NaN</code>.
   */
  private static class ColorMapWithNaN extends TLcdColorMap {
    public ColorMapWithNaN(TLcdInterval aInterval, double[] aLevels, Color[] aColors) {
      super(aInterval, aLevels, aColors);
    }

    @Override
    public Color retrieveColor(double aLevel) {
      if (Double.isNaN(aLevel) || aLevel < getLevelInterval().getMin() || aLevel > getLevelInterval().getMax()) {
        return TRANSPARENT;
      } else {
        return super.retrieveColor(aLevel);
      }
    }
  }

  /**
   * A simple textual visualization.
   */
  private static class TextParameterizedIcon extends ALcdEarthParameterizedIcon {
    private final List<TLcdEarthMultivaluedDataParameter> fParameters;
    private final NumberFormat fFormatter;
    private final Font fFont;

    private ThreadLocal<Graphics2D> fGraphics = new ThreadLocal<Graphics2D>() {
      @Override
      protected Graphics2D initialValue() {
        return (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR_PRE).getGraphics();
      }
    };

    public TextParameterizedIcon(List<TLcdEarthMultivaluedDataParameter> aParameters) {
      fParameters = aParameters;
      fFormatter = new DecimalFormat("##0.0");
      fFont = new Font("Dialog", Font.BOLD, 12);
    }

    public void paintIcon(Graphics aGraphics, int aX, int aY, double[] aParameters) {
      // Paint the text
      Font oldFont = aGraphics.getFont();
      Color oldColor = aGraphics.getColor();
      aGraphics.setFont(fFont);
      aGraphics.setColor(Color.BLACK);

      int w = getWidth(aParameters);
      int h = getHeight(aParameters);
      int x = aX - w / 2;
      int y = aY - h / 2;
      for (int i = 0; i < fParameters.size(); i++) {
        String str = fFormatter.format(aParameters[i]);
        int currWidth = aGraphics.getFontMetrics().stringWidth(str);
        aGraphics.drawString(str, x + (w - currWidth), y);
        y += h;
      }

      aGraphics.setFont(oldFont);
      aGraphics.setColor(oldColor);
    }

    public int getWidth(double[] aParameters) {
      return calculateDimension(aParameters).width;
    }

    public int getHeight(double[] aParameters) {
      return calculateDimension(aParameters).height;
    }

    private Dimension calculateDimension(double[] aParameters) {
      Graphics2D graphics = fGraphics.get();
      graphics.setFont(fFont);
      FontMetrics fm = graphics.getFontMetrics();
      int maxWidth = -1;
      int height = 0;
      for (double parameter : aParameters) {
        String str = fFormatter.format(parameter);
        maxWidth = Math.max(maxWidth, fm.stringWidth(str));
        height += fm.getHeight();
      }

      return new Dimension(maxWidth, height);
    }
  }

  /**
   * An icon that applies a scale factor to the parameters.
   */
  private static class ScaledParameterizedIcon extends ALcdEarthParameterizedIcon implements ILcdOriented {
    private final ILcdParameterizedIcon fDelegate;
    private final double[] fScaleFactors;

    public ScaledParameterizedIcon(ILcdParameterizedIcon aDelegate, double... aScaleFactors) {
      fDelegate = aDelegate;
      fScaleFactors = aScaleFactors;
    }

    public void paintIcon(Graphics aGraphics, int aX, int aY, double[] aParameters) {
      double[] scaledParameters = scaledParameters(aParameters);
      fDelegate.paintIcon(aGraphics, aX, aY, scaledParameters);
    }

    private double[] scaledParameters(double[] aParameters) {
      double[] scaledParameters = aParameters.clone();
      for (int i = 0; i < scaledParameters.length; i++) {
        scaledParameters[i] *= fScaleFactors[i];
      }
      return scaledParameters;
    }

    public int getWidth(double[] aParameters) {
      return fDelegate.getWidth(aParameters);
    }

    public int getHeight(double[] aParameters) {
      return fDelegate.getHeight(aParameters);
    }

    @Override
    public void anchorPointSFCT(double[] aParameters, Point aPointSFCT) {
      double[] scaledParameters = scaledParameters(aParameters);
      fDelegate.anchorPointSFCT(scaledParameters, aPointSFCT);
    }

    @Override
    public double getOrientation() {
      ILcdOriented orientedIcon = fDelegate instanceof ILcdOriented ? (ILcdOriented) fDelegate : null;
      return orientedIcon != null ? orientedIcon.getOrientation() : Double.NaN;
    }
  }
}
