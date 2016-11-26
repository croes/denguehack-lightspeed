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
package samples.tea.gxy.los;

import com.luciad.gui.*;
import com.luciad.model.*;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import samples.gxy.decoder.MapSupport;

import java.awt.*;

/**
 * This is an implementation of <code>ILcdGXYLayerFactory</code> to create a
 * layer for the objects created and used in the LOS sample.
 */
class LayerFactory implements ILcdGXYLayerFactory {

  // For point layer ...
  TLcdGXYIconPainter fIconPainter1 = new TLcdGXYIconPainter();
  ILcdIcon fPointIcon1 = new TLcdSymbol( TLcdSymbol.FILLED_TRIANGLE, 10, Color.white, Color.black );
  ILcdIcon fSnapIcon1  = new TLcdSymbol( TLcdSymbol.RECT, 12, Color.cyan );

  TLcdGXYIconPainter fIconPainter2 = new TLcdGXYIconPainter();
  ILcdIcon fPointIcon2 = new TLcdSymbol( TLcdSymbol.FILLED_TRIANGLE, 10, Color.black, Color.white );
  ILcdIcon fSnapIcon2  = new TLcdSymbol( TLcdSymbol.RECT, 12, Color.cyan );

  public LayerFactory() {
    // For point layer ...
    fIconPainter1.setIcon( fPointIcon1 );
    fIconPainter1.setSnapIcon( fSnapIcon1 );
    fIconPainter2.setIcon( fPointIcon2 );
    fIconPainter2.setSnapIcon( fSnapIcon2 );

  }

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    ILcdModelDescriptor model_descriptor = aModel.getModelDescriptor();
    if ( "Geodetic points".equals( model_descriptor.getTypeName() ) ) {
      return createGeodeticPointLayer( aModel );
    }
    if ( "Grid points".equals( model_descriptor.getTypeName() ) ) {
      return createGridPointLayer( aModel );
    }
    if ( "LOS".equals( model_descriptor.getTypeName() ) ) {
      return createLOSLayer( aModel );
    }
    if ( "P2P".equals( model_descriptor.getTypeName() ) ) {
      return createP2PLayer( aModel );
    }
    throw new IllegalArgumentException( "Unsupported model descriptor type.");
  }

  private ILcdGXYLayer createGeodeticPointLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( fIconPainter1 );
    layer.setGXYEditorProvider ( fIconPainter1 );
    return layer;
  }

  private ILcdGXYLayer createGridPointLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( fIconPainter2 );
    layer.setGXYEditorProvider ( fIconPainter2 );
    return layer;
  }

  private ILcdGXYLayer createLOSLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( new LOSPainter() );
    layer.setSelectable( false );
    return layer;
  }

  private ILcdGXYLayer createP2PLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( new P2PIntervisibilityPainter() );
    layer.setSelectable( false );
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
