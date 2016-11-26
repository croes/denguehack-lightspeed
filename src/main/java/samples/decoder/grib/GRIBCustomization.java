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

import com.luciad.format.grib.TLcdGRIBModelDescriptor;
import com.luciad.format.grib.gxy.ALcdGRIBIcon;
import com.luciad.format.grib.gxy.TLcdGRIBNumericIcon;
import com.luciad.format.raster.ILcdRasterValueConverter;
import com.luciad.format.raster.TLcdMultivaluedRasterModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdIndexColorModel;
import com.luciad.util.TLcdInterval;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.ColorModel;

public class GRIBCustomization {

  public static ALcdGRIBIcon createNumericTemperatureIcon( ILcdModel aModel, int aEdgeRadius ) {
    TLcdMultivaluedRasterModelDescriptor descriptor = ( TLcdMultivaluedRasterModelDescriptor ) aModel.getModelDescriptor();
    TLcdGRIBModelDescriptor gribDescriptor = ( TLcdGRIBModelDescriptor ) descriptor.getModelDescriptor( 0 );

    // Create a numeric icon.
    TLcdGRIBNumericIcon icon = new TLcdGRIBNumericIcon();
    icon.setEdgeRadius( aEdgeRadius );
    icon.setFont( new Font( "Dialog", Font.BOLD, 12 ) );

    // Set the unit of the parameter.
    String unit = gribDescriptor.getParameterUnit();

    if ( unit.equalsIgnoreCase( "K" ) ) {
      // Convert the Kelvin values to degrees Celsius.
      TemperatureConversionIcon conversion_icon =
          new TemperatureConversionIcon( icon, TemperatureConversionIcon.KELVIN_TO_CELSIUS );

      // Set a unit after the numeric value.
      icon.setUnitString( "\u00b0C" );
      return conversion_icon;
    }
    return icon;
  }

  /**
   * Creates a color model for displaying percentage values.
   * The percentage values are displayed with increasing opacity.
   *
   * @param aGRIBModelDescriptor the descriptor that contains information on the
   *                             level and the conversion between internal
   *                             and actual values.
   * @param aColor               the fully opaque color.
   */
  public static ColorModel createPercentageColorModel( final TLcdGRIBModelDescriptor aGRIBModelDescriptor, Color aColor ) {
    int bits = aGRIBModelDescriptor.getBitCount();
    Color[] colors = {
        new Color( aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 0 ),
        new Color( aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 130 )
    };
    double[] values = {0.0, 100.1};
    TLcdColorMap colorMap = new TLcdColorMap(
        new TLcdInterval( values[ 0 ], values[ values.length - 1 ], null ), values, colors );
    return new TLcdIndexColorModel(
        bits, 1 << bits, colorMap, new GRIBRasterValueConverter( aGRIBModelDescriptor ) );
  }

  private static class GRIBRasterValueConverter implements ILcdRasterValueConverter {

    private final TLcdGRIBModelDescriptor fGRIBModelDescriptor;

    public GRIBRasterValueConverter( TLcdGRIBModelDescriptor aGRIBModelDescriptor ) {
      fGRIBModelDescriptor = aGRIBModelDescriptor;
    }

    @Override
    public int dataToRaster( double aDataValue ) {
      return fGRIBModelDescriptor.getInternalValue( aDataValue );
    }

    @Override
    public double rasterToData( int aRasterValue ) {
      return fGRIBModelDescriptor.getActualValue( aRasterValue );
    }

    @Override
    public ILcdISO19103UnitOfMeasure getUnit() {
      return null;
    }
  }
}
