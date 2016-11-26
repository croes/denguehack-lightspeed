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
package samples.opengl.geocentric;

import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.opengl.ALcdGLLight;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.TLcdGLDirectionalLight;
import com.luciad.view.opengl.TLcdGLGeocentricFixedReferenceCameraAdapter;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;
import samples.opengl.common.Abstract3DPanel;
import samples.opengl.common.Atmosphere;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.io.IOException;

/**
 * The main panel of the geocentric sample application.
 */
public class MainPanel extends Abstract3DPanel {

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    // Set a geocentric world reference
    TLcdGeocentricReference ref = new TLcdGeocentricReference();
    canvas.setXYZWorldReference(ref);
    canvas.setBackground(Color.black);
    // Add a view listener that draws an atmospheric glow effect around the globe.
    canvas.addViewListener(new Atmosphere());

    // Set the initial camera position.
    TLcdGLGeocentricFixedReferenceCameraAdapter adapter = new TLcdGLGeocentricFixedReferenceCameraAdapter(ref);
    adapter.setCamera(canvas.getCamera());
    adapter.setCollectChanges(true);
    adapter.setLocation(new TLcdLonLatPoint(3, 13));
    adapter.setPitch(-89);
    adapter.setYaw(0);
    adapter.setDistance(9000000);
    adapter.applyCollectedChanges();
    adapter.setCollectChanges(false);

    setupGeocentricLighting( canvas );

    return canvas;
  }

  protected void addData() {
    final TLcdGLViewCanvas canvas = getCanvas();
    canvas.addModel(ModelFactory.createGridModel());
    
    canvas.addModel(ModelFactory.createSHPModel("Data/Shp/World/world.shp"));
    canvas.addModel(ModelFactory.createPolygonModel());
    canvas.addModel(ModelFactory.createComplexPolyModel());
    canvas.addModel(ModelFactory.createLonLatBufferModel());
    canvas.addModel(ModelFactory.createLonLatHeightBufferModel());
    canvas.addModel(ModelFactory.createVariableGeoBufferModel());
    canvas.addModel(ModelFactory.createCircleModel());
    canvas.addModel(ModelFactory.createEllipseModel());
    canvas.addModel(ModelFactory.createArcBandModel());
    canvas.addModel(ModelFactory.createDomeModel());
    canvas.addModel(ModelFactory.createSphereModel());
    canvas.addModel(ModelFactory.createPoint2DModel());
    canvas.addModel(ModelFactory.createPoint3DModel());

    /* 3D terrain requires a vertex shader capable graphics card when working
       with a geocentric world reference. Therefore, TerrainFactory checks the
       necessary hardware requirements before attempting to create a terrain
       layer. However, the requirements cannot be checked unless the view has
       been fully initialized, so we use a view listener to postpone the adding
       of the terrain data until the view's first repaint. */
    canvas.addViewListener(new ALcdGLViewAdapter() {
      public void postRender(TLcdGLViewEvent aViewEvent) {
        aViewEvent.getView().removeViewListener(this);
        try {
          LayerFactory layerFactory = (LayerFactory) canvas.getLayerFactory();
          /* Pass the TLcdGLAboveGroundPointlistPainter to the terrain factory,
             so that it can be linked to the terrain paintable once it has
             been created. */
          TerrainFactory terrainFactory = new TerrainFactory();
          terrainFactory.addTerrainToView(canvas, "Data/terrain_sample/alps.trn");
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Sets up the light settings of the view.
   * @param aCanvas the view to use.
   */
  private static void setupGeocentricLighting( TLcdGLViewCanvas aCanvas ) {
    boolean was_auto_update = aCanvas.isAutoUpdate();
    aCanvas.setAutoUpdate( false );

    // We create a directional light and we add it to the canvas
    TLcdGLDirectionalLight light = new TLcdGLDirectionalLight( ALcdGLLight.LightType.STATIONARY );
    aCanvas.getLights().add(light);

    // Then we set the light direction. We set the direction as the vector
    // connecting the camera position with the center of the Earth.
    ILcdPoint eye = aCanvas.getCamera().getEyePoint();
    light.setDirection( new float[] { -(float)eye.getX(), -(float)eye.getY(), -(float)eye.getZ() } );

    // We enable the light.
    light.setEnabled( true );

    // Last we enable lighting on the canvas globally. 
    aCanvas.setLightingEnabled( true );

    aCanvas.getCamera().addChangeListener(new LightUpdater(light));

    // reset auto update flag
    aCanvas.setAutoUpdate( was_auto_update );
  }

  /**
   * Updates the light direction when the camera is moved,
   * so that the direction always matches the vector going from the camera to the center of the Earth.
   * This is equivalent to have the sun moving around the globe together with the camera.
   */
  private static class LightUpdater implements ChangeListener {

    private final TLcdGLDirectionalLight fLight;

    public LightUpdater(TLcdGLDirectionalLight aDirectionalLight) {
      fLight = aDirectionalLight;
    }

    public void stateChanged( ChangeEvent e ) {
      if ( e.getSource() instanceof ILcdGLCamera ) {
        ILcdPoint eye = ( ( ILcdGLCamera ) e.getSource() ).getEyePoint();
        fLight.setDirection( new float[] { -(float)eye.getX(), -(float)eye.getY(), -(float)eye.getZ() } );
      }
    }
  }
}
