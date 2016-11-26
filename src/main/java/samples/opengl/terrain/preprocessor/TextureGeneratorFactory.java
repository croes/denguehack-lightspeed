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
package samples.opengl.terrain.preprocessor;

import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.format.raster.terrain.preprocessor.ALcdSampledTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.ILcdTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdBakedMapTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdGXYViewTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdInterpolatingRasterElevationProvider;
import com.luciad.format.raster.terrain.preprocessor.TLcdLightMapTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdNormalMapTextureGenerator;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdBuffer;
import com.luciad.view.gxy.ILcdGXYLayer;

/**
 * A class that creates instances of ILcdTextureGenerator for use with the
 * terrain preprocessor.
 */
class TextureGeneratorFactory {

  private CompositeRasterModelDecoder fDecoder = new CompositeRasterModelDecoder();
  private RasterLayerFactory fLayerFactory = new RasterLayerFactory();

  public void setBuffer( ILcdBuffer aBuffer ) {
    fDecoder.setBuffer( aBuffer );
  }

  private void configureSampledTextureGenerator( ALcdSampledTextureGenerator aGenerator, ILcdModel aModel, boolean aCompress ) {

    TLcdInterpolatingRasterElevationProvider elevation = new TLcdInterpolatingRasterElevationProvider( aModel );

    if ( aGenerator instanceof TLcdNormalMapTextureGenerator ) {
      ( ( TLcdNormalMapTextureGenerator ) aGenerator ).setElevationProvider( elevation );
    }
    else if ( aGenerator instanceof TLcdLightMapTextureGenerator ) {
      ( ( TLcdLightMapTextureGenerator ) aGenerator ).setElevationProvider( elevation );
    }
    else if ( aGenerator instanceof TLcdBakedMapTextureGenerator ) {
      ( ( TLcdBakedMapTextureGenerator ) aGenerator ).setElevationProvider( elevation );
      ( ( TLcdBakedMapTextureGenerator ) aGenerator )
          .setColorModel( TLcdDTEDColorModelFactory.getSharedInstance().createColorModel() );
      ( ( TLcdBakedMapTextureGenerator ) aGenerator ).setCompressTextures( aCompress );
    }
  }

  private void configureTLcdGXYViewTextureGenerator( TLcdGXYViewTextureGenerator aGenerator, ILcdModel aModel, boolean aCompress ) {
    ILcdGXYLayer layer = fLayerFactory.createGXYLayer( aModel );
    aGenerator.addGXYLayer( layer );
    aGenerator.setCompressTextures( aCompress );
  }

  public ILcdTextureGenerator createTextureGenerator( String aSource, String aClassName, boolean aCompress ) {
    try {
      ILcdTextureGenerator tg = ( ILcdTextureGenerator ) Class.forName( aClassName ).newInstance();
      ILcdModel model = fDecoder.decode( aSource );

      if ( tg instanceof ALcdSampledTextureGenerator ) {
        configureSampledTextureGenerator( ( ALcdSampledTextureGenerator ) tg, model, aCompress );
      }
      else if ( tg instanceof TLcdGXYViewTextureGenerator ) {
        configureTLcdGXYViewTextureGenerator( ( TLcdGXYViewTextureGenerator ) tg, model, aCompress );
      }

      return tg;
    } catch ( Exception e ) {
      e.printStackTrace();
      return null;
    }
  }
}
