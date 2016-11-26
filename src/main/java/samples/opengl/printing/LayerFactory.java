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
package samples.opengl.printing;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintablefactory.TLcdGLExtrudedEllipsePaintableFactory;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.TLcdGLFont;
import com.luciad.view.opengl.painter.TLcdGLLabelPainter;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import com.luciad.view.opengl.style.TLcdGLOutlineStyle;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.RedBlueFillStyleProvider;

import java.awt.Color;
import java.awt.Font;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( typeName.equals( ModelFactory.ELLIPSE_MODEL_TYPE_NAME ) ) {
      return createEllipseLayer( aModel, aTargetView );
    }
    else if ( typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME ) ) {
      return GLViewSupport.createGridLayer( aModel, aTargetView );
    }
    else {
      return null;
    }
  }

  public boolean isValidModel( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    return (
            typeName.equals( ModelFactory.ELLIPSE_MODEL_TYPE_NAME ) ||
            typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME    )
    );
  }

  private ILcdGLLayer createEllipseLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    TLcdGLExtrudedEllipsePaintableFactory paintableFactory =
            new TLcdGLExtrudedEllipsePaintableFactory();
    paintableFactory.setDefaultMinimumZ( 0 );
    paintableFactory.setDefaultMaximumZ( 50000 );
    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2( paintableFactory );

    painter.setPaintFill( true );
    painter.setFillStyleProvider( new RedBlueFillStyleProvider() );

    painter.setPaintOutline( true );
    painter.setOutlineStyleProvider( new TLcdGLOutlineStyle( Color.red ) );

    layer.setPainter( painter );

    // A label painter that labels everything with 'Ellipse'
    TLcdGLFont label_font = new TLcdGLFont( new Font( "Helvetica", Font.BOLD, 14 ) );
    TLcdGLLabelPainter label_painter = new TLcdGLLabelPainter( label_font ) {
      protected String[] retrieveLabel( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext ) {
        return new String[] { "Ellipse" };
      }
    };
    label_painter.setOffset( 50, 50, 100 );
    label_painter.setDefaultColor( Color.cyan );
    label_painter.setOverlayLabels( false );

    layer.setLabelPainter( label_painter );
    layer.setLabeledSupported( true );
    layer.setLabeled( true );


    return layer;
  }
}
