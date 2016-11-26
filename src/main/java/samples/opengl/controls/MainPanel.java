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
package samples.opengl.controls;

import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.terrain.preprocessor.ALcdElevationProvider;
import com.luciad.format.raster.terrain.preprocessor.TLcdInterpolatingRasterElevationProvider;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;
import com.luciad.view.opengl.TLcdGLViewLocation;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import com.luciad.view.opengl.controller.composite.TLcdGLCameraValidator;
import com.luciad.view.opengl.controller.composite.TLcdGLCompositeController;
import com.luciad.view.opengl.controller.composite.TLcdGLPanControllerAction;
import samples.opengl.common.Abstract3DPanel;
import samples.opengl.common.Toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This sample demonstrates the use of TLcdGLCompositeController. In addition
 * to the standard mouse interaction, the sample provides a simple GUI to
 * control the camera with. Furthermore, the sample shows how the use of an
 * ILcdElevationProvider allows the controller to keep the camera above ground
 * at all times.
 */
class MainPanel extends Abstract3DPanel {

  private static final float UNIT_OF_MEASURE = 1.0f;

  TLcdGLCameraValidator fCameraValidator;
  ALcdElevationProvider fElevationProvider;

  public MainPanel() {
    super(true, TLcdGLViewLocation.LocationMode.CLOSEST_SURFACE);
  }

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
    camera.setAltitudeExaggerationFactor( 2.5 );

    return canvas;
  }

  protected void createGUI() {
    super.createGUI();

    Toolbar    toolbar = getToolbar();
    ILcdGLView canvas  = getCanvas();

    // Configure a TLcdGLCompositeController.
    TLcdGLCompositeController controller = toolbar.getCompositeController();
    fCameraValidator = new TLcdGLCameraValidator();
    try {
      TLcdDMEDModelDecoder decoder = new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance() );
      fElevationProvider = new TLcdInterpolatingRasterElevationProvider( decoder.decode( "Data/Dted/Alps/dmed" ) );
      fCameraValidator.setElevationProvider( fElevationProvider );
    } catch ( IOException e ) {
      e.printStackTrace();
      fElevationProvider = null;
    }

    // Listen to layer-removal events
    canvas.addLayeredListener( new ILcdLayeredListener() {
      public void layeredStateChanged( TLcdLayeredEvent aTLcdLayeredEvent ) {
        if (aTLcdLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED &&
            "3D Terrain".equalsIgnoreCase( aTLcdLayeredEvent.getLayer().getLabel())) {
          fElevationProvider = null;
          fCameraValidator.setElevationProvider( null );
        }
      }
    });
    
    // Set the area of interest for the controller:
    TLcdGLPanControllerAction panAction = new TLcdGLPanControllerAction();
    TLcdLonLatBounds aoi = new TLcdLonLatBounds( -180, -90, 360, 180 );
    TLcdGeoReference2GeoReference m2w = new TLcdGeoReference2GeoReference(
            new TLcdGeodeticReference( new TLcdGeodeticDatum() ),
            (ILcdGeoReference) canvas.getXYZWorldReference()
    );
    TLcdXYZBounds aoi_world = new TLcdXYZBounds();
    try {
      m2w.sourceBounds2destinationSFCT( aoi, aoi_world );
      panAction.setAreaOfInterest( aoi_world );
    } catch ( TLcdNoBoundsException e ) {
      e.printStackTrace();
    }
    controller.setPanAction( panAction );

    controller.getPanAction()   .setCameraValidator( fCameraValidator );
    controller.getRotateAction().setCameraValidator( fCameraValidator );
    controller.getZoomAction()  .setCameraValidator( fCameraValidator );

    setComponentNorthEast( new CameraControlPanel( canvas, controller ) );
  }

  protected void addData() {
    final ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel() );
    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
    fit.setLayer( (ILcdGLLayer) canvas.getLayer( canvas.layerCount() - 1 ) );
    fit.fit();

    canvas.addViewListener( new ALcdGLViewAdapter() {
      @Override
      public void postRender( TLcdGLViewEvent aViewEvent ) {
        aViewEvent.getView().removeViewListener( this );
        addTerrain( canvas );
        TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
        fit.setLayer( (ILcdGLLayer) canvas.getLayer( canvas.layerCount() - 1 ) );
        fit.fit();
      }
    });
  }

  private void addTerrain( final ILcdGLView aCanvas ) {
    try {
      final String terrainSource = "Data/terrain_sample/alps.trn";
      String terrainSourcePath;
      URL url = getClass().getClassLoader().getResource("Data/terrain_sample/alps.trn");
      if (url != null) {
        try {
          terrainSourcePath = new File(url.toURI()).getPath();
        } catch ( URISyntaxException e ) {
          terrainSourcePath = terrainSource;
        }
      } else {
        terrainSourcePath = terrainSource;
      }
      aCanvas.addModel( ModelFactory.createTerrainModel( terrainSourcePath ) );
      ILcdGLLayer terrainLayer = (ILcdGLLayer)aCanvas.getLayer( aCanvas.layerCount() - 1 );

      terrainLayer.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent evt ) {
          if ( "3D Terrain".equalsIgnoreCase(evt.getSource().toString()) ) {
            if (evt.getPropertyName().equalsIgnoreCase( "visible" )) {
              if (Boolean.valueOf( evt.getNewValue().toString() ).equals(Boolean.FALSE)) {
                fCameraValidator.setElevationProvider( null );
                aCanvas.repaint();
              }
              else if (Boolean.valueOf( evt.getNewValue().toString() ).equals(Boolean.TRUE)) {
                fCameraValidator.setElevationProvider( fElevationProvider );
                aCanvas.repaint();
              }
            }
          }
        }
      });
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}
