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
package samples.decoder.grib.lightspeed;

import com.luciad.format.grib.lightspeed.TLspGRIBLayerBuilder;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.style.TLspParameterizedRasterIconStyle;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.Collection;
import java.util.Collections;

/**
 * A layer factory for GRIB 1 and GRIB 2 models. It creates and sets up a Lightspeed layer
 * for displaying objects contained in models with descriptors that are instances of
 * TLcdGRIBModelDescriptor or TLcdMultivaluedRasterModelDescriptor.
 */
@LcdService
public class GRIBLayerFactory implements ILspLayerFactory {

  private ColorModel fColorModel;
  private ILcdParameterizedIcon fIcon;
  private int fIconSpacing = -1;

  @Override
  public boolean canCreateLayers( ILcdModel aModel ) {
    try {
      TLspGRIBLayerBuilder.newBuilder().model( aModel );
      return true;
    } catch ( IllegalArgumentException e ) {
      return false;
    }
  }

  @Override
  public Collection<ILspLayer> createLayers( ILcdModel aModel ) {
    TLspParameterizedRasterIconStyle iconStyle = TLspParameterizedRasterIconStyle.newBuilder()
        .icon( fIcon )
        .spacing( fIconSpacing )
        .haloColor( new Color( 180, 180, 180, 100 ) )
        .build();
    ILspLayer layer = TLspGRIBLayerBuilder.newBuilder()
        .model( aModel )
        .colorModel( fColorModel )
        .iconStyle( iconStyle )
        .build();
    return Collections.singletonList( layer );
  }

  public void setColorModel( ColorModel aColorModel ) {
    fColorModel = aColorModel;
  }

  public ColorModel getColorModel() {
    return fColorModel;
  }

  public void setIcon( ILcdParameterizedIcon aIcon ) {
    fIcon = aIcon;
  }

  public ILcdParameterizedIcon getIcon() {
    return fIcon;
  }

  public void setIconSpacing( int aSpacing ) {
    fIconSpacing = aSpacing;
  }

  public int getIconSpacing() {
    return fIconSpacing;
  }

}
