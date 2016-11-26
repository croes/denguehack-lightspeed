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
package samples.opengl.icon3d;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.painter.TLcdGL3DIconPainter;
import samples.opengl.common.GLViewSupport;

import java.awt.*;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  private ILcdGLObjectIconProvider fIconProvider;

  public void setIconProvider( ILcdGLObjectIconProvider aIconProvider ) {
    fIconProvider = aIconProvider;
  }

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( typeName.equals( ModelFactory.POINT_MODEL_TYPE_NAME ) ) {
      return createPointLayer( aModel, aTargetView );
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
            typeName.equals( ModelFactory.POINT_MODEL_TYPE_NAME ) ||
                    typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME )
    );
  }

  private ILcdGLLayer createPointLayer(ILcdModel aModel, ILcdGLView aTargetView) {

    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    // Set up a 3D icon painter for the layer.
    TLcdGL3DIconPainter painter = new TLcdGL3DIconPainter();
    // The icon size is specified in world coordinates.
    painter.setIconScale( 250 );
    // The icon provider will return an ILcdGL3DIcon for every object in the layer.
    painter.setIconProvider( fIconProvider );
    painter.setDrawVerticalLine( true );
    painter.setVerticalLineColor( Color.green );
    /* A vertical offset of 1.0 means the icon will be positioned on top of the
       point being drawn, instead of being centered on it. */
    painter.setVerticalOffsetFactor( 1.0 );
    layer.setPainter( painter );

    return layer;
  }
}
