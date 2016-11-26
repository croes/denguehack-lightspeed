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
package samples.opengl.terrain.geoid;

import com.luciad.format.raster.terrain.opengl.ILcdGLLODChooser;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdMemoryCheckPanel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLCartesianFixedReferenceCameraAdapter;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import samples.opengl.common.Abstract3DPanel;
import samples.opengl.common.ZSlider;
import samples.gxy.common.TitledPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;

/**
 * This sample demonstrates the Luciad3D terrain package. The terrain is preprocessed in memory
 * rather than being written to a file.
 */
class MainPanel extends Abstract3DPanel {

  private TerrainFactory fTerrainFactory;

  public MainPanel() {
    super();
  }

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    canvas.setXYZWorldReference( new TLcdGeocentricReference( new TLcdGeodeticDatum() ) );
    Abstract3DPanel.setupCamera( canvas );

    ILcdGLCamera camera = canvas.getCamera();
    camera.setAltitudeExaggerationFactor( 10000 );

    // Create cartesian camera adapter to rotate the sample.
    final TLcdGLCartesianFixedReferenceCameraAdapter adapter =
        new TLcdGLCartesianFixedReferenceCameraAdapter( camera );
    adapter.setLocation( new TLcdXYZPoint( 0, 0, 0 ) );
    adapter.setDistance( 21000000 );
    adapter.setPitch( -5 );
    adapter.setRoll( 5 );
    adapter.setYaw( 260 );

//    // Start a timer to rotate the sample.
//    new Timer( 100, new ActionListener() {
//      public void actionPerformed( ActionEvent e ) {
//        adapter.setLocation( new TLcdXYZPoint( 0, 0, 0 ) );
//        adapter.setDistance( 21000000 );
//        adapter.setPitch( -5 );
//        adapter.setRoll( 5 );
//        adapter.setYaw( adapter.getYaw() - 1 );
//      }
//    } ).start();

    return canvas;
  }

  protected void createGUI() {
    super.createGUI();
    // Create a panel with the LOD slider and a memory check panel.
    JPanel bottombar = new JPanel( new BorderLayout( 2, 2 ) );
    bottombar.add( BorderLayout.CENTER, createLODSlider( getTerrainFactory()
                                                             .getGeometryLOD(), "Terrain quality" ) );
    bottombar.add( BorderLayout.SOUTH, new TLcdMemoryCheckPanel() );
    setComponentSouth( bottombar );
  }

  protected ZSlider createZSlider() {
    return new ZSlider( JSlider.VERTICAL, getCanvas().getCamera(), 1, 10000, 500, 2500 );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel() );
    try {
      // Load the terrain data:
      getTerrainFactory().addTerrainToView( canvas );
    } catch ( IOException e ) {
      throw new RuntimeException( e.getMessage() );
    }
  }

  private TerrainFactory getTerrainFactory() {
    if ( fTerrainFactory == null ) {
      fTerrainFactory = new TerrainFactory();
    }
    return fTerrainFactory;
  }

  /**
   * Creates a slider to control the quality settings of the specified ILcdGLLODChooser.
   */
  private Component createLODSlider( final ILcdGLLODChooser aLODChooser, final String aLabel ) {
    // Create text label for the slider.
    final JLabel readout = new JLabel( "100%" );

    // Get the LOD chooser's quality parameter range.
    // Note that max isn't necessarily greater than min!
    double min = aLODChooser.getMinQuality();
    double max = aLODChooser.getMaxQuality();
    double cur = aLODChooser.getQuality();

    // Set up the slider so that it goes from min to max in 0.1% increments.
    JSlider slider = new JSlider(
        JSlider.HORIZONTAL,
        0,
        1000,
        ( int ) ( 1000 * ( ( cur - min ) / ( max - min ) ) )
    );
    double v = ( cur - min ) / ( max - min );
    readout.setText( ( int ) ( v * 100.0 ) + "%" );
    slider.addChangeListener( new ChangeListener() {
      public void stateChanged( ChangeEvent event ) {
        double min_quality = aLODChooser.getMinQuality();
        double max_quality = aLODChooser.getMaxQuality();
        double slider_value = ( ( JSlider ) event.getSource() ).getValue();
        double q = min_quality + ( slider_value / 1000.0 ) * ( max_quality - min_quality );
        aLODChooser.setQuality( q );
        readout.setText( ( int ) ( slider_value / 10.0 ) + "%" );
        getCanvas().repaint();
      }
    } );

    JPanel container = new JPanel( new BorderLayout( 2, 2 ) );
    container.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    container.add( slider, BorderLayout.CENTER );
    container.add( readout, BorderLayout.EAST );

    return TitledPanel.createTitledPanel( aLabel, container );
  }
}
