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
package samples.tea.gxy.los;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.tea.TLcdGXYViewBasedTerrainElevationProvider;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.gxy.common.toolbar.ToolBar;
import samples.tea.gxy.DTEDDEMTerrainElevationProvider;

/**
 * This sample demonstrates the ability of the terrain analysis Industry Specific Component to perform elevation calculations
 * on data in different model references.
 * <p/>
 * The view contains elevation data layers defined in different model references: WGS84 for DTED data, Swiss grid reference
 * for ... . Next to these layers are 2 layers with points defined in different model references.
 * <p/>
 * To make sure the points are at exactly the same location, a icon painter is derived that supports snapping.
 */
public class MainPanel extends SamplePanel {


  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel( new TLcdLonLatBounds( 8, 43, 6, 5 ) );


  private LOSPanel fLOSPanel = new LOSPanel();
  private P2PPanel fP2PPanel = new P2PPanel();
  private ILcdCollection<ILcdGXYLayer> fSnappableLayers;

  protected void createGUI() {
    // create a terrain elevation provider for DTED and DEM data.
    TLcdGXYViewBasedTerrainElevationProvider terrain_elevation_provider =
            new TLcdGXYViewBasedTerrainElevationProvider( new DTEDDEMTerrainElevationProvider() );
    terrain_elevation_provider.setGXYView( fMapJPanel );
    terrain_elevation_provider.setUseOnlyVisibleLayers( true );

    // Create the default toolbar and layer control.
    ToolBar tool_bar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );
    fSnappableLayers = tool_bar.getSnappables().getSnappableLayers();

    // Add action to display the line-of-sight panel
    tool_bar.addAction( new ShowLOSPanelAction( fLOSPanel, TLcdAWTUtil.findParentFrame( fMapJPanel ), terrain_elevation_provider ) );

    // Add action to display the point-to-point panel
    tool_bar.addAction( new ShowP2PPanelAction( fP2PPanel, TLcdAWTUtil.findParentFrame( fMapJPanel ), terrain_elevation_provider ) );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH, tool_bar );
    add( BorderLayout.CENTER, map_panel );
    add( BorderLayout.EAST, layer_control );
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    ILcdGXYLayer alp_layer = null;
    try {
      TLcdDMEDModelDecoder model_decoder = new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance() );
      model_decoder.setColorModel( TLcdDTEDColorModelFactory.getSharedInstance().createColorModel() );
      model_decoder.setSupportGeoidDatums( true );

      ILcdModel model = model_decoder.decode( "" + "Data/Dted/Alps/dmed" );

      ILcdGXYLayerFactory layer_factory = new RasterLayerFactory();
      alp_layer = layer_factory.createGXYLayer( model );
      alp_layer.setLabel( "Alps" );
      alp_layer.setSelectable( false );
      alp_layer.setEditable( false );
      alp_layer.setLabeled( false );
      alp_layer.setVisible( true );
      GXYLayerUtil.addGXYLayer( fMapJPanel, alp_layer );
    }
    catch ( Exception ex ) {
      System.err.println( "FAILED: <init>: decode DMED File: " + ex );
    }

    ModelFactory model_factory = new ModelFactory();
    LayerFactory layer_factory = new LayerFactory();

    // Create layers containing some points.
    ILcdGXYLayer geodetic_point_layer = layer_factory.createGXYLayer( model_factory.createModel( "GeodeticPoint" ) );
    ILcdGXYLayer grid_point_layer     = layer_factory.createGXYLayer( model_factory.createModel( "GridPoint"     ) );

    // Create layers for line-of-sight results.
    ILcdGXYLayer geodetic_los_layer = layer_factory.createGXYLayer( model_factory.createModel( "GeodeticLOS" ) );
    ILcdGXYLayer grid_los_layer     = layer_factory.createGXYLayer( model_factory.createModel( "GridLOS"     ) );

    // Create layers for point-to-point results.
    ILcdGXYLayer geodetic_p2p_layer = layer_factory.createGXYLayer( model_factory.createModel( "GeodeticP2P" ) );
    ILcdGXYLayer grid_p2p_layer     = layer_factory.createGXYLayer( model_factory.createModel( "GridP2P"     ) );

    GXYLayerUtil.addGXYLayer( fMapJPanel, grid_los_layer,   false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, grid_p2p_layer,   false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, grid_point_layer, false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, geodetic_los_layer,   false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, geodetic_p2p_layer,   false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, geodetic_point_layer, false, false );
    fMapJPanel.repaint();

    // add all points in the point layers to the snappables so that the points can snap
    // to each other.
    fSnappableLayers.add(grid_point_layer);
    fSnappableLayers.add(geodetic_point_layer);

    fLOSPanel.setCenterLayers( new ILcdGXYLayer[] { grid_point_layer, geodetic_point_layer } );
    fLOSPanel.setTargetLayers( new ILcdGXYLayer[] { grid_los_layer, geodetic_los_layer } );
    fLOSPanel.setRasterLayerRasterProvider(
            new RasterLayerRasterProvider( new ILcdGXYLayer[] {
                    alp_layer,
            } )
    );

    fP2PPanel.setCenterLayers( new ILcdGXYLayer[] { grid_point_layer, geodetic_point_layer } );
    fP2PPanel.setTargetLayers( new ILcdGXYLayer[] { grid_p2p_layer, geodetic_p2p_layer } );
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Line-of-sight" );
      }
    } );
  }
}
