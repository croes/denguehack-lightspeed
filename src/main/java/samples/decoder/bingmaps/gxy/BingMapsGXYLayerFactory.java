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
package samples.decoder.bingmaps.gxy;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.bingmaps.TLcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsModelDescriptor;
import com.luciad.earth.view.gxy.TLcdEarthGXYRasterPainter;
import com.luciad.model.ILcdDataSource;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import samples.decoder.bingmaps.DataSourceFactory;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Layer factory for BingMaps 2D.
 */
public class BingMapsGXYLayerFactory implements ILcdGXYLayerFactory {

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    if ( aModel.getModelDescriptor() instanceof TLcdBingMapsModelDescriptor ) {
      TLcdGXYLayer layer = createGXYLayer();
      layer.setLabel( aModel.getModelDescriptor().getDisplayName() );
      layer.setSelectable( false );
      layer.setModel( aModel );

      TLcdEarthGXYRasterPainter painter = createEarthPainter();
      // Improves legibility of labeled content.
      double quality = 0.6;
      if ( DataSourceFactory.containsText( ( TLcdBingMapsModelDescriptor ) aModel.getModelDescriptor() ) ) {
        quality = 0.3;
        painter.setOversamplingRate( 2 );
      }
      painter.setQuality( quality );

      layer.setGXYPainterProvider( painter );
      return layer;
    }
    return null;
  }

  protected TLcdEarthGXYRasterPainter createEarthPainter() {
    return new TLcdEarthGXYRasterPainter() {
      public void paint( Graphics aGraphics, int aMode, ILcdGXYContext aContext ) {
        // Improves general image quality.
        Object hint = null;
        if ( aGraphics instanceof Graphics2D ) {
           hint = ( ( Graphics2D ) aGraphics ).getRenderingHint( RenderingHints.KEY_INTERPOLATION );
          ( ( Graphics2D ) aGraphics ).setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        }

        // Performs the actual painting.
        super.paint( aGraphics, aMode, aContext );

        // Restores Graphics changes.
        if ( hint != null  ) {
          ( ( Graphics2D ) aGraphics ).setRenderingHint( RenderingHints.KEY_INTERPOLATION, hint );
        }
      }
    };
  }

  /**
   * This factory method creates a new, uninitialized <code>TLcdGXYLayer</code>. This method can be extended to for instance return an extension of <code>TLcdGXYLayer</code>.
   * @return A new, uninitialized <code>TLcdGXYLayer</code>
   */
  protected TLcdGXYLayer createGXYLayer( ){
    return new TLcdGXYLayer(  ){
      @Override
      public boolean isSelectableSupported() {
        return false;
      }
    };
  }

}
