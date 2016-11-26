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
package samples.decoder.grib;

import com.luciad.format.grib.gxy.ALcdGRIBIcon;

import java.awt.*;

/**
 * This is a wrapper for a <code>ILcdGRIBIcon</code>. It converts the first value in
 * the array of values from Kelvin to Temperature (Celsius or Fahrenheit).
 */
public class TemperatureConversionIcon extends ALcdGRIBIcon {

  public static final int KELVIN_TO_CELSIUS = 0;
  public static final int KELVIN_TO_FAHRENHEIT = 1;

  private ALcdGRIBIcon fGRIBIcon;
  private int fType;

  public TemperatureConversionIcon( ALcdGRIBIcon aGRIBIcon,
                                    int aType ) {
    fGRIBIcon = aGRIBIcon;
    fType = aType;
  }

  public void paintGRIBIcon( Graphics aGraphics, int aX, int aY, double[] aValues ) {
    double[] convertedValues = convertValues( aValues );
    // Delegate the painting to the other ILcdGRIBIcon.
    fGRIBIcon.paintGRIBIcon( aGraphics, aX, aY, convertedValues );
  }

  private double[] convertValues( double[] aValues ) {
    // Do the conversion of temperature.
    switch ( fType ) {
      case KELVIN_TO_CELSIUS:
        return new double[] { aValues[ 0 ] - 273.15 };
      case KELVIN_TO_FAHRENHEIT:
        return new double[] { ( aValues[ 0 ] - 273.15 ) * 1.8 + 32d };
      default:
        return aValues;
    }
  }

  @Override
  public int getIconWidth( double[] aValues ) {
    double[] convertedValues = convertValues( aValues );
    return fGRIBIcon.getIconWidth( convertedValues );
  }

  @Override
  public int getIconHeight( double[] aValues ) {
    double[] convertedValues = convertValues( aValues );
    return fGRIBIcon.getIconHeight( convertedValues );
  }

  @Override
  public void anchorPointSFCT( double[] aValues, Point aPointSFCT ) {
    double[] convertedValues = convertValues( aValues );
    fGRIBIcon.anchorPointSFCT( convertedValues, aPointSFCT );
  }
}
