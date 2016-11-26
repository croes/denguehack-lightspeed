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
package samples.opengl.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLNavigationControlsFactory;

import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.serviceregistry.CollectionServiceRegistry;
import samples.common.serviceregistry.CompositeServiceRegistry;
import samples.common.serviceregistry.EditableServiceRegistry;
import samples.common.serviceregistry.ServiceLoaderRegistry;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.TitledPanel;
import samples.opengl.common.layerControls.swing.LayerControlPanelFactory3D;

/**
 * The main panel of the first sample application.
 */
public abstract class Abstract3DPanel extends JPanel {

  private static final float UNIT_OF_MEASURE = 1000f;
  private static final Color BACKGROUND_COLOR = new Color( 123, 174, 231 );

  private TLcdGLViewCanvas fCanvas;
  private Toolbar fToolbar;

  private JPanel fEastPanel = new JPanel( new BorderLayout() );

  private boolean fAltitudeExaggerationEnabled = true;
  private ZSlider fZSlider = null;
  private TLcdGLViewLocation.LocationMode fMouseLocationMode = TLcdGLViewLocation.LocationMode.SEA_LEVEL;
  private TLcdGLMapOverlayPanel fOverlayPanel = null;
  private TitledPanel fMapPanel = null;
  private InitViewPanel fInitViewPanel = null;
  private boolean fInitViewFirstPaint = true;

  private EditableServiceRegistry fEditableServiceRegistry = new CollectionServiceRegistry();
  private ServiceRegistry fServiceRegistry = new CompositeServiceRegistry( Arrays.asList( fEditableServiceRegistry, new ServiceLoaderRegistry() ) );

  /**
   * The constructor of the Abstract3DPanel. This constructor will first call the method
   * <code>createCanvas</code> to create the ILcdGLView, then it will call the function
   * <code>addData</code> before creating the GUI with <code>createGUI</code>.
   *
   * @param aAltitudeExaggerationEnabled if true, the altitude exaggeration slider is created
   */
  public Abstract3DPanel( boolean aAltitudeExaggerationEnabled ) {
    this( aAltitudeExaggerationEnabled, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
  }

  /**
   * The constructor of the Abstract3DPanel. This constructor will first call the method
   * <code>createCanvas</code> to create the ILcdGLView, then it will call the function
   * <code>addData</code> before creating the GUI with <code>createGUI</code>.
   *
   * @param aAltitudeExaggerationEnabled if true, the altitude exaggeration slider is created
   * @param aMouseLocationMode           the location mode to be used for the mouse-location
   *                                     component
   */
  public Abstract3DPanel( boolean aAltitudeExaggerationEnabled, TLcdGLViewLocation.LocationMode aMouseLocationMode ) {
    fAltitudeExaggerationEnabled = aAltitudeExaggerationEnabled;
    fMouseLocationMode = aMouseLocationMode;
    fCanvas = createCanvas();
    JPanel viewContainer = new JPanel();
    viewContainer.setLayout( new BoxLayout( viewContainer, BoxLayout.Y_AXIS ) );
    viewContainer.add( fCanvas );
    fOverlayPanel = new TLcdGLMapOverlayPanel( fCanvas, viewContainer, new JPanel( new TLcdOverlayLayout() ) );
    addData();
    createGUI();
    GLLogoIconUtil.setupLogo( fCanvas );
  }

  /**
   * The constructor of the Abstract3DPanel. This constructor will first call the method
   * <code>createCanvas</code> to create the ILcdGLView, then it will call the function
   * <code>addData</code> before creating the GUI with <code>createGUI</code>.
   */
  public Abstract3DPanel() {
    this( true );
  }

  public abstract ILcdGLLayerFactory getGLLayerFactory();

  /**
   * Get the 3D map (<code>ILcdGLView</code>) which is used in this sample.
   *
   * @return the 3D map.
   */
  public TLcdGLViewCanvas getCanvas() {
    return fCanvas;
  }

  /**
   * Get the toolbar used in this sample.
   *
   * @return the toolbar.
   */
  public Toolbar getToolbar() {
    return fToolbar;
  }

  /**
   * Get the overlay panel used in this sample.
   * <p/>
   * Note: this can be null.
   *
   * @return the overlay panel.
   *
   * @see TLcdOverlayLayout
   */

  public Container getOverlayPanel() {
    if ( fOverlayPanel != null ) {
      return fOverlayPanel.getContentPane();
    }
    else {
      return null;
    }
  }

  /**
   * Sets the given component in the <code>BorderLayout.SOUTH</code> part
   * of the GUI.
   */
  public void setComponentSouth( Component aComponent ) {
    add( BorderLayout.SOUTH, aComponent );
  }

  /**
   * Sets the given component in the <code>BorderLayout.NORTH</code> part
   * of the <code>BorderLayout.EAST</code> of the GUI.
   */
  public void setComponentNorthEast( Component aComponent ) {
    fEastPanel.add( BorderLayout.NORTH, aComponent );
  }

  /**
   * Returns a service registry that can be used to query services, such as model decoders or
   * layer factories.
   * @return a service registry
   */
  public ServiceRegistry getServiceRegistry() {
    return fServiceRegistry;
  }

  public void registerService( Object aService ) {
    fEditableServiceRegistry.register( aService );
  }

  public void registerService( Object aService, int aPriority ) {
    fEditableServiceRegistry.register( aService, aPriority );
  }

  /**
   * Create the ILcdGLView for this sample.
   */
  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = new TLcdGLViewCanvas();
    TLcdGridReference world_reference = new TLcdGridReference();
    world_reference.setGeodeticDatum( new TLcdGeodeticDatum() );
    world_reference.setProjection( new TLcdEquidistantCylindrical() );
    world_reference.setUnitOfMeasure( UNIT_OF_MEASURE );
    canvas.setXYZWorldReference( world_reference );
    canvas.setLayerFactory( getGLLayerFactory() );
    canvas.setBackground( BACKGROUND_COLOR );
    return canvas;
  }

  /**
   * Add the initial 3D data.
   */
  protected void addData() {
  }

  protected void addOverlayComponents() {
    if ( fCanvas.getXYZWorldReference() instanceof ILcdGeocentricReference ) {
      getOverlayPanel().add( TLcdGLNavigationControlsFactory.createNavigationControls(
          fCanvas, "images/gui/navigationcontrols/small/", false, null ) );
    }
  }

  /**
   * Create the GUI of the 3D sample. Adds a toolbar, adds overlay components, adds a Z slider,
   * adds a layer control and adds the ILcdGLView.
   */
  protected void createGUI() {
    // Create the toolbar.
    fToolbar = createToolbar();

    // Create the layer controls.
    LayerControlPanel layer_control = LayerControlPanelFactory3D
        .createDefaultGLLayerControlPanel( fCanvas );

    // Add a overlay panel around the map.
    if ( fOverlayPanel != null ) {
      // Create a titled panel around the map panel
      fMapPanel = TitledPanel.createTitledPanel(
          "Map", fOverlayPanel, TitledPanel.NORTH | TitledPanel.EAST | TitledPanel.WEST
      );
      addOverlayComponents();
    }
    else {
      // Create a titled panel around the map panel
      fMapPanel = TitledPanel.createTitledPanel(
          "Map", fCanvas, TitledPanel.NORTH | TitledPanel.EAST | TitledPanel.WEST
      );
    }
    fEastPanel.add( BorderLayout.CENTER, layer_control );

    // Populate the frame.
    setLayout( new BorderLayout() );
    if ( fToolbar != null ) {
      add( fToolbar, BorderLayout.NORTH );
    }
    if ( fAltitudeExaggerationEnabled ) {
      // Create the vertical exaggeration slider
      fZSlider = createZSlider();
      add( fZSlider, BorderLayout.WEST );
      fCanvas.addPropertyChangeListener( new MyCameraChangeListener() );
    }

    fInitViewPanel = new InitViewPanel();
    SwingUtilities.invokeLater( new Runnable() {
      public void run() {
        Abstract3DPanel.this.remove( fInitViewPanel );
        Abstract3DPanel.this.add( fMapPanel, BorderLayout.CENTER );
      }
    } );
    add( fInitViewPanel, BorderLayout.CENTER );

    add( fEastPanel, BorderLayout.EAST );
    updateUI();

  }

  protected Toolbar createToolbar() {
    return new Toolbar( fCanvas, this, fMouseLocationMode );
  }

  protected ZSlider createZSlider() {
    return new ZSlider( JSlider.VERTICAL, fCanvas.getCamera() );
  }

  /**
   * Sets up the initial position of the camera.
   *
   * @param aCanvas the view to use.
   */
  public static void setupCamera( TLcdGLViewCanvas aCanvas ) {
    // First we get the camera from the view
    ILcdGLCamera camera = aCanvas.getCamera();

    if ( aCanvas.getXYZWorldReference() instanceof ILcdGeocentricReference ) {
      TLcdGLGeocentricFixedReferenceCameraAdapter adapter = new TLcdGLGeocentricFixedReferenceCameraAdapter( ( ILcdGeocentricReference ) aCanvas
          .getXYZWorldReference() );
      adapter.setCamera( aCanvas.getCamera() );
      adapter.setCollectChanges( true );
      adapter.setLocation( new TLcdLonLatPoint( 0, 0 ) );
      adapter.setPitch( -60 );
      adapter.setYaw( 330 );
      adapter.setDistance( 5000000f );
      adapter.applyCollectedChanges();
      adapter.setCollectChanges( false );
    }
    else {
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
      adapter.setYaw( 330 );
      adapter.setDistance( 4000000f / UNIT_OF_MEASURE );
    }
  }

  /**
   * Sets up the light settings of the view.
   *
   * @param aCanvas the view to use.
   */
  public static void setupLights( TLcdGLViewCanvas aCanvas ) {
    boolean was_auto_update = aCanvas.isAutoUpdate();
    try {
      aCanvas.setAutoUpdate( false );
      // Now we ask the light factory to give us a viewpoint positional light
      // object. This means that the position of the light is always relative to
      // that of the camera. In this case, we will keep the light source at the
      // same position as the camera.
      TLcdGLPositionalLight light = new TLcdGLPositionalLight( ALcdGLLight.LightType.VIEWPOINT );

      // Then we add the light to the canvas
      aCanvas.getLights().add( light );

      // Then we set the parameters of the positional light. The light is
      // positioned at the origin, which in this case is the camera location.
      light.setPosition( new float[]{0, 0, 0} );
      // It is very important to enable the light. By default all newly created lights
      // are disabled, so if we forget to enable it it will not be used.
      light.setEnabled( true );

      // Last we enable lighting on the canvas globally. If lighting is not enabled
      // on the canvas itself, no lighting calculations are performed. Objects will
      // then appear unlit, retaining their inherent color.
      aCanvas.setLightingEnabled( true );

    } finally {
      // restore auto update flag
      aCanvas.setAutoUpdate( was_auto_update );
    }
  }

  /**
   * Sets up fog for the given <code>ILcdGLView</code>
   *
   * @param aCanvas the view to use.
   */
  public static void setupFog( TLcdGLViewCanvas aCanvas ) {
    // First we create a fog object and set it up properly.
    TLcdGLFog fog = new TLcdGLFog();
    fog.setFogMode( TLcdGLFogMode.LINEAR );
    fog.setFogStart( 3000000f / UNIT_OF_MEASURE );
    fog.setFogEnd( 6000000f / UNIT_OF_MEASURE );
    fog.setFogColor( BACKGROUND_COLOR );
    fog.setEnabled( true );

    // Here we set the fog object we've created on the canvas. In contrast to the lighting, we
    // do not have to enable the fog on the canvas itself. The enabled state of the fog is only
    // controlled by the fog object itself.
    aCanvas.setFog( fog );
  }

  /**
   * Adds a skybox to the given <code>ILcdGLView</code>.
   *
   * @param aCanvas the view to use.
   */
  public static void setupSkybox( TLcdGLViewCanvas aCanvas ) {
    aCanvas.addViewListener( new Skybox( new String[]{
        "background/skybox/default/front.png",
        "background/skybox/default/back.png",
        "background/skybox/default/left.png",
        "background/skybox/default/right.png",
        "background/skybox/default/up.png",
        "background/skybox/default/down.png",
    } ) );
  }

  private class MyCameraChangeListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      JPanel parent = ( JPanel ) fZSlider.getParent();
      parent.remove( fZSlider );
      fZSlider = createZSlider();
      parent.add( fZSlider, BorderLayout.WEST );
    }
  }


  private static class InitViewPanel extends JPanel {

    public InitViewPanel() {
      super( new BorderLayout() );
      JLabel initLabel = new JLabel( "Initializing / updating view..." );
      initLabel.setHorizontalAlignment( JLabel.CENTER );
      add( initLabel, BorderLayout.CENTER );
    }
  }
}
