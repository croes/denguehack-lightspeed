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
package samples.opengl.firstsample;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.transformation.TLcdCompositeModelXYZWorldTransformation;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.transformation.TLcdLinearModelZWorldTransformation;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLGeodeticPathFactory;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.paintablefactory.TLcdGLExtrudedEllipsePaintableFactory;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.RedBlueFillStyleProvider;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( typeName.equals( ModelFactory.ELLIPSE_MODEL_TYPE_NAME ) ) {
      return createEllipseLayer( aModel);
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

  private ILcdGLLayer createEllipseLayer(ILcdModel aModel) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    TLcdGeodetic2Grid xy_transformation = new TLcdGeodetic2Grid();
    TLcdLinearModelZWorldTransformation z_transformation
            = new TLcdLinearModelZWorldTransformation( 1 );
    TLcdCompositeModelXYZWorldTransformation modelXYZWorldTransformation
            = new TLcdCompositeModelXYZWorldTransformation(
            xy_transformation,
            z_transformation
    );
    layer.setModelXYZWorldTransformation( modelXYZWorldTransformation );
    layer.setPathFactory( new TLcdGLGeodeticPathFactory() );
    TLcdGLExtrudedEllipsePaintableFactory paintableFactory =
            new TLcdGLExtrudedEllipsePaintableFactory();
    paintableFactory.setDefaultMinimumZ( 0 );
    paintableFactory.setDefaultMaximumZ( 50000 );
    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2( paintableFactory );
    painter.setPaintFill( true );
    painter.setPaintOutline( false );
    painter.setFillStyleProvider( new RedBlueFillStyleProvider() );
    layer.setPainter( painter );
    return layer;
  }
}
