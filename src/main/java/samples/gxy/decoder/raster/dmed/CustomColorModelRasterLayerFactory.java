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
package samples.gxy.decoder.raster.dmed;

import java.awt.Color;

import com.luciad.format.raster.ILcdColorModelFactory;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;

import samples.gxy.common.layers.factories.RasterLayerFactory;

/**
 * Provides a DMED specific color model.
 */
public class CustomColorModelRasterLayerFactory extends RasterLayerFactory {

  public CustomColorModelRasterLayerFactory() {
    super(createColorModelFactory());
  }

  private static ILcdColorModelFactory createColorModelFactory() {
    TLcdDTEDColorModelFactory factory = new TLcdDTEDColorModelFactory();
    factory.setLevels(LEVELS);
    factory.setColors(COLORS);
    return factory;
  }

  private static final double[] LEVELS = {
      0,
      0.1,
      250,
      1000,
      10000,
  };

  private static final Color[] COLORS = {
      new Color(140, 150, 210),
      new Color(70, 100, 50),
      new Color(255, 200, 150),
      Color.gray,
      Color.white,
  };

//  private static final double[] LEVELS = {
//      0,
//      0.1,
//      200,
//      400,
//      600,
//      800,
//      1000,
//      1200,
//      1400,
//      1600,
//      1800,
//      2000,
//      2200,
//      2400,
//      2600,
//      2800
//  };
//
//  private static final Color[] COLORS = {
//      new Color( 126, 136, 202 ),
//      new Color( 237, 210, 131 ),
//      new Color( 247, 216, 101 ),
//      new Color( 242, 205, 96 ),
//      new Color( 237, 181, 79 ),
//      new Color( 231, 170, 80 ),
//      new Color( 229, 149, 71 ),
//      new Color( 229, 108, 53 ),
//      new Color( 200, 140, 61 ),
//      new Color( 172, 133, 51 ),
//      new Color( 180, 131, 82 ),
//      new Color( 154, 78, 46 ),
//      new Color( 146, 92, 46 ),
//      new Color( 120, 78, 43 ),
//      new Color( 99, 68, 41 ),
//      new Color( 48, 35, 33 )
//  };

}
