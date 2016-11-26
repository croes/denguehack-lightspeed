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
package samples.opengl.openflight;

import com.luciad.format.object3d.openflight.TLcdOpenFlightModelDescriptor;
import com.luciad.format.object3d.openflight.opengl.TLcdGLOpenFlightMeshStyleProvider;
import com.luciad.format.object3d.openflight.opengl.TLcdGLOpenFlightPainter;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import samples.opengl.common.GLViewSupport;

import java.io.File;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( modelDescriptor instanceof TLcdOpenFlightModelDescriptor ) {
      return createOpenFlightLayer( aModel, aTargetView );
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
            ( modelDescriptor instanceof TLcdOpenFlightModelDescriptor ) ||
            typeName.equals( ModelFactory.GRID_MODEL_TYPE_NAME )
    );
  }

  private ILcdGLLayer createOpenFlightLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    File sourceFile = new File( aModel.getModelDescriptor().getSourceName() );
    TLcdGLOpenFlightMeshStyleProvider styleProvider = new TLcdGLOpenFlightMeshStyleProvider();
    /*
    */
    
    TLcdGLOpenFlightPainter painter = new TLcdGLOpenFlightPainter();
    painter.setStyleProvider( styleProvider );
    layer.setPainter( painter );
    layer.setLabel( sourceFile.getName() );
    return layer;
  }
}
