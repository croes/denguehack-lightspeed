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
package samples.opengl.terrain.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.luciad.format.raster.terrain.opengl.ILcdGLLODChooser;
import com.luciad.format.raster.terrain.opengl.texture.ILcdGLTerrainTexture;
import com.luciad.format.raster.terrain.opengl.texture.TLcdGLCompositeTerrainTexture;
import com.luciad.gui.TLcdListLayout;
import com.luciad.gui.swing.TLcdMemoryCheckPanel;
import com.luciad.model.TLcdOpenAction;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.TLcdStringUtil;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;

import samples.gxy.common.TitledPanel;
import samples.opengl.common.Abstract3DPanel;

/**
 * This sample demonstrates the Luciad3D terrain package. It allows you to load
 * a preprocessed terrain from disk.
 */
class MainPanel extends Abstract3DPanel {

  private static final float UNIT_OF_MEASURE = 1.0f;

  private TerrainFactory fTerrainFactory;
  private JFileChooser fFileChooser;

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
    return canvas;
  }

  protected void createGUI() {
    super.createGUI();

    // Create the file open button
    fFileChooser = new JFileChooser( "." );
    fFileChooser.setFileFilter( new FileFilter() {
      public boolean accept( File f ) {
        return f.isDirectory() || TLcdStringUtil.endsWithIgnoreCase( f.getName(), ".trn" );
      }

      public String getDescription() {
        return "3D model files";
      }
    } );

    TLcdOpenAction open_action = new TLcdOpenAction() {
      public void actionPerformed( ActionEvent aEvent ) {
        if ( fFileChooser.showOpenDialog( MainPanel.this ) == JFileChooser.APPROVE_OPTION ) {
          String name = fFileChooser.getSelectedFile().getAbsolutePath();
          try {
            getTerrainFactory().addTerrainToView( getCanvas(), name );
          }
          catch ( IOException aException ) {
            aException.printStackTrace();
          }
        }
      }
    };
    open_action.putValue( Action.SHORT_DESCRIPTION, "Open a 3D terrain file" );
    getToolbar().addAction(open_action, 4);

    // Create checkboxes to switch individual textures on and off.
    final JPanel texswitch = new JPanel( new TLcdListLayout() );
    texswitch.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    getCanvas().addViewListener( new ALcdGLViewAdapter() {
      public void postRender( TLcdGLViewEvent event ) {
        if ( getTerrainFactory().getTexture() instanceof TLcdGLCompositeTerrainTexture ) {
          final TLcdGLCompositeTerrainTexture composite = (TLcdGLCompositeTerrainTexture) fTerrainFactory.getTexture();
          for ( int t = 0; t < composite.getTextureCount() ; t++ ) {
            ILcdGLTerrainTexture texture = composite.getTexture( t );
            JCheckBox check = new JCheckBox( texture.getDisplayName(), true );
            final int t1 = t;
            check.addActionListener( new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                JCheckBox src = (JCheckBox) e.getSource();
                composite.setTextureEnabled( t1, src.isSelected() );
                getCanvas().repaint();
              }
            } );
            texswitch.add( check );
          }
          getCanvas().removeViewListener( this );
        }
      }
    } );

    setComponentNorthEast( TitledPanel.createTitledPanel(
            "Textures", texswitch, TitledPanel.NORTH
    ) );

    // Create a panel with the LOD sliders and a memory check panel.
    JPanel bottombar = new JPanel( new BorderLayout( 2, 2 ) );
    bottombar.add( BorderLayout.NORTH,  createLODSlider( getTerrainFactory().getGeometryLOD(), "Geometry quality" ) );
    bottombar.add( BorderLayout.CENTER, createLODSlider( getTerrainFactory().getTextureLOD(), "Texture quality"  ) );
    bottombar.add( BorderLayout.SOUTH,  new TLcdMemoryCheckPanel() );
    setComponentSouth( bottombar );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel() );
    try {
      // Load the preprocessed terrain data.
      getTerrainFactory().addTerrainToView( canvas, "Data/terrain_sample/alps.trn" );
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
            (int) ( 1000 * ( ( cur - min ) / ( max - min ) ) )
    );
    double v = ( cur - min ) / ( max - min );
    readout.setText( (int) ( v * 100.0 ) + "%" );
    slider.addChangeListener( new ChangeListener() {
      public void stateChanged( ChangeEvent event ) {
        double min_quality  = aLODChooser.getMinQuality();
        double max_quality  = aLODChooser.getMaxQuality();
        double slider_value = ( (JSlider) event.getSource() ).getValue();
        double q = min_quality + ( slider_value / 1000.0 ) * ( max_quality - min_quality );
        aLODChooser.setQuality( q );
        readout.setText( (int) ( slider_value / 10.0 ) + "%" );
        getCanvas().repaint();
      }
    } );

    JPanel container = new JPanel( new BorderLayout( 2, 2 ) );
    container.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    container.add( slider, BorderLayout.CENTER );
    container.add( readout, BorderLayout.EAST );

    return TitledPanel.createTitledPanel( aLabel, container );
  }

  /**
   * Pops up a file chooser, so that the user can select a terrain to load
   */
  private String getTerrainFileName() {
    JFileChooser c = new JFileChooser();
    c.setFileFilter( new FileFilter() {
      public boolean accept( File f ) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith( ".trn" );
      }

      public String getDescription() {
        return "Luciad 3D Terrain files (*.trn)";
      }
    } );
    c.setCurrentDirectory( new File( "./Data" ) );
    if ( c.showOpenDialog( MainPanel.this ) == JFileChooser.APPROVE_OPTION ) {
      return c.getSelectedFile().getAbsolutePath();
    } else {
      System.out.println( "You must select a terrain file!" );
      System.exit( 0 );
    }
    return null;
  }
}
