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

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.opengl.ALcdGLLight;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.TLcdGLCartesianFixedReferenceCameraAdapter;
import com.luciad.view.opengl.TLcdGLFog;
import com.luciad.view.opengl.TLcdGLFogMode;
import com.luciad.view.opengl.TLcdGLSpotLight;
import com.luciad.view.opengl.TLcdGLViewCanvas;

import samples.common.layerControls.swing.LayerControlPanel;
import samples.opengl.common.layerControls.swing.LayerControlPanelFactory3D;
import samples.opengl.common.Toolbar;
import samples.opengl.common.ZSlider;
import samples.opengl.common.GLLogoIconUtil;
import samples.gxy.common.TitledPanel;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * The main panel of the first sample application.
 */
class MainPanel extends JPanel {

  static {
    try {
      if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 ) {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      }
    }
    catch ( Exception aException ) {
      // unable to set system look and feel
      System.out.println( "Error while setting the native LookAndFeel: " + aException.getMessage() );
    }
  }

  private static final float UNIT_OF_MEASURE  = 1000f;
  private static final Color BACKGROUND_COLOR = new Color( 123, 174, 231 );

  private TLcdGLViewCanvas fCanvas;

  /**
   * The constructor of the MainPanel. Creates a TLcdGLViewCanvas, adds data to
   * it, and initializes the GUI of the sample application.
   */
  public MainPanel() {
    // Create the 3D view
    fCanvas = createCanvas();
    addData();
    createGUI();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = new TLcdGLViewCanvas();
    GLLogoIconUtil.setupLogo( canvas );
    TLcdGridReference world_reference = new TLcdGridReference();
    world_reference.setGeodeticDatum( new TLcdGeodeticDatum() );
    world_reference.setProjection( new TLcdEquidistantCylindrical() );
    world_reference.setUnitOfMeasure( UNIT_OF_MEASURE );
    canvas.setXYZWorldReference( world_reference );
    canvas.setLayerFactory( new LayerFactory() );
    canvas.setBackground( BACKGROUND_COLOR );
    setupCamera( canvas );
    setupLights( canvas );
    setupFog   ( canvas );
    return canvas;
  }

  /**
   * Add the initial 3D data.
   */
  protected void addData() {
    fCanvas.addModel( ModelFactory.createGridModel   () );
    fCanvas.addModel( ModelFactory.createEllipseModel() );
  }

  /**
   * Create the GUI of the 3D sample. Adds a toolbar, adds a Z slider, adds a layer control
   * and add the ILcdGLView.
   */
  protected void createGUI() {
    // Create the toolbar.
    Toolbar toolbar = new Toolbar( fCanvas, this );

    // Create the vertical exaggeration slider and the layer controls.
    ZSlider             z_slider      = new ZSlider( JSlider.VERTICAL, fCanvas.getCamera() );
    LayerControlPanel layer_control = LayerControlPanelFactory3D.createDefaultGLLayerControlPanel( fCanvas );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fCanvas, TitledPanel.NORTH | TitledPanel.EAST | TitledPanel.WEST
    );

    // Populate the frame.
    setLayout( new BorderLayout() );
    add( toolbar,       BorderLayout.NORTH  );
    add( z_slider,      BorderLayout.WEST   );
    add( map_panel,     BorderLayout.CENTER );
    add( layer_control, BorderLayout.EAST   );
    updateUI();
  }

  public static void setupCamera( TLcdGLViewCanvas aCanvas ) {
    // First we get the camera from the view
    ILcdGLCamera camera = aCanvas.getCamera();

    // Increase the amount of vertical exaggeration
    camera.setAltitudeExaggerationFactor(10.0);

    // Then we set the values of the near and far clipping planes
    camera.setNear( 100000f / UNIT_OF_MEASURE );
    camera.setFar( 5000000f / UNIT_OF_MEASURE );

    // Then we wrap it in a fixed reference adapter. This adapter allows us to specify the camera's
    // position using a reference point (the point which we are looking at) and a pitch and yaw.
    // It is also possible to specify the position of the camera directly on the camera
    // object, but using this adapter is much more intuitive.
    TLcdGLCartesianFixedReferenceCameraAdapter adapter =
            new TLcdGLCartesianFixedReferenceCameraAdapter( camera );

    // Now we set the initial values on the adapter. The adapter will automatically update
    // the camera to reflect the values we have chosen.
    adapter.setLocation( new TLcdXYZPoint( 0, 0, 0 ) );
    adapter.setPitch( -35 );
    adapter.setDistance( 3000000f / UNIT_OF_MEASURE );
  }

  /**
   * Sets up the light settings of the view.
   * @param aCanvas the view to use.
   */
  public static void setupLights( TLcdGLViewCanvas aCanvas ) {
    boolean was_auto_update = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    
    // First we create a viewpoint spot light object.
    // This means that the position and direction of the light are always
    // relative to the position and direction of the camera. In this case
    // this means that our spot light will always shine along the direction
    // the camera is facing.
    TLcdGLSpotLight spotLight = new TLcdGLSpotLight(ALcdGLLight.LightType.VIEWPOINT);

    // Then we add the light to our 3D view;
    // to do that, we retrieve the list of lights associated to the view
    // and we add the light to such list.
    aCanvas.getLights().add( spotLight );

    // Then we set the parameters of the spot light. This spot light is positioned in
    // the origin and is shining along the negative Z axis.
    spotLight.setSpotCutoff( 45 );
    spotLight.setPosition( new float[] { 0, 0, 0 } );
    spotLight.setDirection( new float[] { 0, 0, -1 } );
    // It is very important to enable the light. By default all newly created lights
    // are disabled, so if we forget to enable it it will not be used.
    spotLight.setEnabled( true );

    // Last we enable lighting on the canvas globally. If lighting is not enabled
    // on the canvas itself, no lighting calculations are performed. Objects will
    // then appear unlit, retaining their inherent color.
    aCanvas.setLightingEnabled( true );

    } finally {
      // reset auto update flag
      aCanvas.setAutoUpdate( was_auto_update );
    }
  }

  /**
   * Sets up fog for the given <code>ILcdGLView</code>
   * @param aCanvas the view to use.
   */
  public static void setupFog( TLcdGLViewCanvas aCanvas ) {
    // First we create a fog object and set it up properly.
    TLcdGLFog fog = new TLcdGLFog();
    fog.setFogMode( TLcdGLFogMode.LINEAR );
    fog.setFogStart( 3000000f / UNIT_OF_MEASURE );
    fog.setFogEnd( 5000000f / UNIT_OF_MEASURE );
    fog.setFogColor( BACKGROUND_COLOR );
    fog.setEnabled( true );

    // Here we set the fog object we've created on the canvas. In contrast to the lighting, we
    // do not have to enable the fog on the canvas itself. The enabled state of the fog is only
    // controlled by the fog object itself.
    aCanvas.setFog( fog );
  }
}
