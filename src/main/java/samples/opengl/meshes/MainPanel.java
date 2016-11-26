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
package samples.opengl.meshes;

import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import samples.opengl.common.Abstract3DPanel;

import java.io.IOException;

/**
 * The main panel of the sample application.
 */
class MainPanel extends Abstract3DPanel {

  public ILcdGLLayerFactory getGLLayerFactory() {
    LayerFactory layer_factory = new LayerFactory();
    return layer_factory;
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    canvas.setXYZWorldReference( new TLcdGeocentricReference() );
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupLights( canvas );
    return canvas;
  }

  protected void addData() {
    final ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel () );
    canvas.addModel( ModelFactory.createTrackModel() );
    canvas.addModel( ModelFactory.createWaypointModel() );
    canvas.addModel( ModelFactory.createTargetModel() );
    canvas.addModel( ModelFactory.createArrowModel() );

    /* 3D terrain requires a vertex shader capable graphics card when working
       with a geocentric world reference. Therefore, TerrainFactory checks the
       necessary hardware requirements before attempting to create a terrain
       layer. However, the requirements cannot be checked unless the view has
       been fully initialized, so we use a view listener to postpone the adding
       of the terrain data until the view's first repaint. */
    canvas.addViewListener(new ALcdGLViewAdapter() {
      public void postRender( TLcdGLViewEvent aViewEvent) {
        aViewEvent.getView().removeViewListener(this);
        try {
          TerrainFactory terrainFactory = new TerrainFactory();
          terrainFactory.addTerrainToView(canvas, "Data/terrain_sample/alps.trn");
          TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
          fit.setLayer( (ILcdGLLayer) canvas.getLayer( canvas.layerCount()-1 ) );
          fit.fit();
        }
        catch ( IOException e) {
          e.printStackTrace();
        }
      }
    });

    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
    fit.setLayer( (ILcdGLLayer) canvas.getLayer( 0 ) );
    fit.fit();
  }
}
