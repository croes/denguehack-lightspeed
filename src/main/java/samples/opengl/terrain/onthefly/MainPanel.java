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
package samples.opengl.terrain.onthefly;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.format.raster.terrain.opengl.ILcdGLLODChooser;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.gui.swing.TLcdMemoryCheckPanel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.model.TLcdOpenAction;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;

import samples.common.layerControls.actions.AbstractRemoveLayersAction;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.TitledPanel;
import samples.opengl.common.Abstract3DPanel;

/**
 * This sample demonstrates the Luciad3D terrain package. The terrain texture
 * is generated on the fly rather than being preprocessed.
 */
class MainPanel extends Abstract3DPanel {

  private static final float UNIT_OF_MEASURE = 1.0f;

  private TerrainFactory fTerrainFactory;

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

    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager( new TLcdDialogManagerSW() );

    TLcdOpenAction open_action = new TLcdOpenAction() {
      public void actionPerformed( ActionEvent aEvent ) {
        File file = TLcdUserDialog.chooseFile( ".", ILcdDialogManager.LOAD, this, null );
        if ( file != null ) {
          try {
            TLcdCompositeModelDecoder model_decoder = new TLcdCompositeModelDecoder( getServiceRegistry().query( ILcdModelDecoder.class ) );
            TLcdCompositeGXYLayerFactory layer_factory = new TLcdCompositeGXYLayerFactory( getServiceRegistry().query( ILcdGXYLayerFactory.class ) );
            ILcdModel model = model_decoder.decode( file.getAbsolutePath() );
            if ( model != null ) {
              ILcdGXYLayer layer = layer_factory.createGXYLayer( model );
              getTerrainFactory().getTexture().addGXYLayer( layer );
            }
          }
          catch ( IOException aException ) {
            aException.printStackTrace();
          }
        }
      }
    };
    open_action.putValue( Action.SHORT_DESCRIPTION, "Open a new texture file" );
    getToolbar().addAction( open_action );

    getCanvas().addViewListener( new ALcdGLViewAdapter() {
      public void postRender( TLcdGLViewEvent aViewEvent ) {
        // Create the 2D layer controls:
        LayerControlPanel layerControlPanel2D = new LayerControlPanel(fTerrainFactory.getTexture(), "Textures", new TLcdArrayList<ILcdLayer>()) {
          @Override
          public Dimension getPreferredSize() {
            return new Dimension( 175, 175 );
          }
        };
        LayerControlPanelFactory2D.addMoveLayerButtons( layerControlPanel2D );
        layerControlPanel2D.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
                new AbstractRemoveLayersAction(layerControlPanel2D.getLayered()) {
                  @Override
                  protected void removeLayer(ILcdLayerTreeNode aParentNode, ILcdLayer aLayer) {
                    aParentNode.removeLayer(aLayer);
                  }
                },
                false,
                layerControlPanel2D.getSelectedLayers()));
        setComponentNorthEast( layerControlPanel2D );
        aViewEvent.getView().removeViewListener( this );
      }
    } );

    // Create a panel with the LOD slider and a memory check panel.
    JPanel bottombar = new JPanel( new BorderLayout( 2, 2 ) );
    bottombar.add( BorderLayout.CENTER, createLODSlider( getTerrainFactory().getGeometryLOD(), "Terrain quality" ) );
    bottombar.add( BorderLayout.SOUTH, new TLcdMemoryCheckPanel() );
    setComponentSouth( bottombar );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel() );
    try {
      // Load the terrain data.
      getTerrainFactory().addTerrainToView( canvas, getServiceRegistry() );
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
}
