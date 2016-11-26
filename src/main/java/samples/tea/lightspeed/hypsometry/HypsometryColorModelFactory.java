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
package samples.tea.lightspeed.hypsometry;

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;

import java.awt.Color;
import java.awt.image.IndexColorModel;

/**
 * Contains factory methods for creating color models that can be used with
 * hypsometric shaders.
 */
public class HypsometryColorModelFactory {

  private static final int MINIMUM_HYPSOMETRIC_VALUE = 0;
  private static final int MAXIMUM_HYPSOMETRIC_VALUE = 255;

  private HypsometryColorModelFactory() {
  }

  /**
   * Utility method that overrides the alpha value of a color.
   * @param rgb the input color
   * @param a the alpha value to be applied (between 0 and 1)
   * @return a color with the same RGB as the input but with the specified alpha
   */
  private static Color alpha( Color rgb, float a ) {
    return new Color( rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) ( a * 255 ) );
  }

  /**
   * Creates a color model that is suitable for displaying azimuths.
   * The color model maps the circle of azimuths on the circle of hues
   * (cyan to magenta, to yellow, to cyan again).
   */
  public static IndexColorModel createAzimuthColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 0 / 3, alpha( Color.cyan, 0.75f) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 1 / 3, alpha( Color.magenta, 0.75f)  );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 2 / 3, alpha( Color.yellow, 0.75f)  );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 3 / 3, alpha( Color.cyan, 0.75f ) );

    return ( IndexColorModel ) factory.createColorModel();
  }

  /**
   * Creates a color model that is suitable for displaying orientations.
   * The color model maps orientations to the North (the reference direction)
   * on blue, and orientations to the South on red.
   */
  public static IndexColorModel createOrientationColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MINIMUM_HYPSOMETRIC_VALUE, alpha( Color.red, 0.75f ) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE, alpha( Color.blue, 0.75f ) );

    return ( IndexColorModel ) factory.createColorModel();
  }

  /**
   * Creates a color model that is suitable for displaying ridges and valleys.
   * The color model maps ridges to red and valleys to blue.
   */
  public static IndexColorModel createRidgeValleyColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 0 / 4, alpha( Color.red,1f)   );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 1 / 4, alpha( Color.red,0.7f)   );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 2 / 4, alpha(Color.white,0.0f) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 3 / 4, alpha( Color.blue,0.7f)  );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 4 / 4, alpha( Color.blue,1f)  );

    return ( IndexColorModel ) factory.createColorModel();
  }

  /**
   * Creates a color model that is suitable for displaying slopes.
   * The color model maps horizontal areas to white and increasingly steep
   * areas to shades of grey.
   */
  public static IndexColorModel createSlopeAngleColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE *  0 / 90, alpha( Color.green, 0.75f)  );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE *  5 / 90, alpha(Color.yellow,0.75f) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 10 / 90, alpha(Color.orange,0.75f) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 20 / 90, alpha(Color.red ,0.75f)    );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 40 / 90, alpha(Color.black,0.75f)   );

    return ( IndexColorModel ) factory.createColorModel();
  }

  /**
   * Creates a color model that is suitable for displaying shading.
   * All colors are black, with partial opacity for slopes that are
   * oriented away from the reference direction, up to full transparency
   * or slopes that are oriented in the reference direction.
   */
  public static IndexColorModel createShadingColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MINIMUM_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0.5f ) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0f ) );

    return ( IndexColorModel ) factory.createColorModel();
  }
}
