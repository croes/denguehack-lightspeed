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
package samples.opengl.terrain.aboveground;

import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import samples.opengl.common.Abstract3DPanel;

import java.io.IOException;

class MainPanel extends Abstract3DPanel {

  private static final float UNIT_OF_MEASURE = 1.0f;

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    ILcdXYZWorldReference world_reference = canvas.getXYZWorldReference();
    if ( world_reference instanceof TLcdGridReference ) {
      ((TLcdGridReference)world_reference).setUnitOfMeasure( UNIT_OF_MEASURE );
    }
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupSkybox( canvas );
    ILcdGLCamera camera = canvas.getCamera();
    camera.setAltitudeExaggerationFactor( 2 );
    return canvas;
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    try {
      ILcdModel terrain_model = ModelFactory.createTerrainModel();
      canvas.addModel( terrain_model );
      canvas.setXYZWorldReference( (ILcdXYZWorldReference) terrain_model.getModelReference() );

      canvas.addModel( ModelFactory.createPolylineModel() );
    } catch ( IOException e ) {
      throw new RuntimeException( e.getMessage() );
    }

    canvas.addLayeredListener( new ILcdLayeredListener() {
      public void layeredStateChanged( TLcdLayeredEvent aTLcdLayeredEvent ) {
        ILcdLayer layer = aTLcdLayeredEvent.getLayer();
        if (aTLcdLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED &&
            LayerFactory.ALPS.equals(layer.getLabel())) {
          //Alps (terrain) removed, so update rivers
          LayerFactory.getAboveGroundPainter().setTerrainPaintable( null );
        }
      }
    });

    // Fit on the terrain layer
    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
    fit.setLayer( (ILcdGLLayer) canvas.getLayer( canvas.layerCount() - 2 ) );
    fit.fit();
  }
}
