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
package samples.tea.gxy.viewshed.positional;

import com.luciad.format.raster.TLcdDEMModelDescriptor;
import com.luciad.format.raster.TLcdMultilevelRasterPainter;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import samples.gxy.decoder.MapSupport;
import samples.gxy.decoder.raster.dmed.CustomColorModelRasterLayerFactory;

import java.awt.Color;

/**
 * This is an implementation of <code>ILcdGXYLayerFactory</code> to create a layer
 * for the various models in the positional viewshed sample.
 */
class ViewshedLayerFactory implements ILcdGXYLayerFactory {

  // For point layer ...
  TLcdGXYIconPainter fIconPainter1 = new TLcdGXYIconPainter();
  ILcdIcon fPointIcon1 = new TLcdSymbol( TLcdSymbol.FILLED_CIRCLE, 12, Color.black, Color.green );
  ILcdIcon fSnapIcon1 = new TLcdSymbol( TLcdSymbol.RECT, 18, Color.cyan );

  public ViewshedLayerFactory() {
    // For point layer ...
    fIconPainter1.setIcon( fPointIcon1 );
    fIconPainter1.setSnapIcon( fSnapIcon1 );
  }

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    return createGXYLayerImpl( aModel );
  }

  private ILcdGXYLayer createGXYLayerImpl( ILcdModel aModel ) {
    ILcdModelDescriptor model_descriptor = aModel.getModelDescriptor();
    if ( "Eye Position".equals( model_descriptor.getTypeName() ) ) {
      return createEyePointLayer( aModel );
    }
    if ( "Viewshed".equals( model_descriptor.getTypeName() ) ) {
      return createViewshedLayer( aModel );
    }
    if( model_descriptor instanceof TLcdDEMModelDescriptor ){
      ILcdGXYLayer layer = new CustomColorModelRasterLayerFactory().createGXYLayer( aModel );
      layer.setSelectable( false );
      return layer;
    }
    return null;
  }

  private ILcdGXYLayer createEyePointLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( fIconPainter1 );
    layer.setGXYEditorProvider( fIconPainter1 );
    return layer;
  }

  private ILcdGXYLayer createViewshedLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setSelectable( false );
    TLcdMultilevelRasterPainter painter = new TLcdMultilevelRasterPainter();
    painter.setLevelSwitchFactor( 0.05 );
    painter.setUseSubTileImageCaching( true );
    layer.setGXYPainterProvider( painter );
    return layer;
  }

  private TLcdGXYLayer createLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = new TLcdGXYLayer() {
      public String toString() {
        return getLabel();
      }
    };
    layer.setModel( aModel );
    layer.setLabel( aModel.getModelDescriptor().getDisplayName() );
    layer.setGXYPen( MapSupport.createPen( aModel.getModelReference() ) );
    layer.setSelectable( true );
    layer.setEditable( true );
    return layer;
  }
}
