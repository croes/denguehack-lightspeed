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
package samples.decoder.grib.gxy;

import java.awt.image.ColorModel;

import com.luciad.format.grib.TLcdGRIBModelDescriptor;
import com.luciad.format.grib.gxy.ILcdGRIBIcon;
import com.luciad.format.grib.gxy.TLcdGRIBGXYPainterProvider;
import com.luciad.format.raster.TLcdMultivaluedRasterModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * A layer factory for GRIB 1 and GRIB 2 models. It creates and sets up a TLcdGXYLayer
 * for displaying objects contained in models with descriptors that are instances of
 * TLcdGRIBModelDescriptor or TLcdMultivaluedRasterModelDescriptor.
 */
@LcdService
public class GRIBLayerFactory implements ILcdGXYLayerFactory {

  private ColorModel fColorModel;
  private ILcdGRIBIcon fGRIBIcon;
  private int fIconSpacing = -1;

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();

    if ( modelDescriptor instanceof TLcdGRIBModelDescriptor ||
         modelDescriptor instanceof TLcdMultivaluedRasterModelDescriptor &&
         ( ( TLcdMultivaluedRasterModelDescriptor ) modelDescriptor ).getModelDescriptorCount() > 0 &&
        ( ( TLcdMultivaluedRasterModelDescriptor ) modelDescriptor ).getModelDescriptor( 0 ) instanceof TLcdGRIBModelDescriptor ) {

      // Create the layer.
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel) {
        @Override
        public boolean isSelectableSupported() {
          return false;
        }
      };
      layer.setSelectable( false );

      // Set a suitable pen on the layer.
      layer.setGXYPen( MapSupport.createPen( aModel.getModelReference() ) );

      // Create and configure the painter.
      TLcdGRIBGXYPainterProvider painter = new TLcdGRIBGXYPainterProvider( modelDescriptor );
      if ( fColorModel != null ) {
        painter.setColorModel( fColorModel );
      }
      if ( fGRIBIcon != null ) {
        painter.setGRIBIcon( fGRIBIcon );
      }
      if ( fIconSpacing != -1 ) {
        painter.setIconSpacing( fIconSpacing );
      }
      layer.setGXYPainterProvider( painter );

      return layer;
    }
    return null;
  }

  public void setColorModel( ColorModel aColorModel ) {
    fColorModel = aColorModel;
  }

  public void setGRIBIcon( ILcdGRIBIcon aGRIBIcon ) {
    fGRIBIcon = aGRIBIcon;
  }

  public void setIconSpacing( int aIconSpacing ) {
    fIconSpacing = aIconSpacing;
  }

  public int getIconSpacing() {
    return fIconSpacing;
  }

}
