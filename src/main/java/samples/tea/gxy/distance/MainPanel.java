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
package samples.tea.gxy.distance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.tea.TLcdFixedLevelBasedRasterElevationProvider;
import com.luciad.tea.TLcdProfileViewJPanel;
import com.luciad.tea.TLcdTerrainProfileController;
import com.luciad.tea.TLcdTerrainRulerController;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayerSubsetList;
import com.luciad.view.map.TLcdAdvancedMapRulerController;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.gxy.common.toolbar.ToolBar;
import samples.tea.gxy.TeaLayerFactory;

/**
 * This sample demonstrates how to calculate distance over terrain.
 */
public class MainPanel extends SamplePanel {


  private TLcdProfileViewJPanel fProfileView;
  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel( new TLcdLonLatBounds( 8, 43, 6, 5 ) );

  private TLcdFixedLevelBasedRasterElevationProvider fTerrainElevationProvider;

  private TLcdTerrainProfileController fTerrainProfileController = new TLcdTerrainProfileController();
  private TLcdTerrainRulerController fTerrainRulerController = new TLcdTerrainRulerController();


  protected void createGUI() {
    fProfileView =  new TLcdProfileViewJPanel() {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension( 500, 80 );
      }

      @Override
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };

    fTerrainElevationProvider = new TLcdFixedLevelBasedRasterElevationProvider();
    fTerrainRulerController.setTerrainElevationProvider( fTerrainElevationProvider );

    fTerrainProfileController.setProfileView( fProfileView );
    fTerrainProfileController.setForeground( new Color( 70, 100, 50 ) );
    fTerrainProfileController.setGhostColor( Color.black );

    // Create the default toolbar and layer control.
    ToolBar tool_bar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );

    // we remove the advanced ruler controller which is in the toolbar by default
    tool_bar.removeGXYController( tool_bar.getAdvancedRulerController() );
    tool_bar.addGXYController( fTerrainProfileController );
    tool_bar.addGXYController( fTerrainRulerController );

    fProfileView.setProfileReference( new TLcdGeodeticReference( new TLcdGeodeticDatum(  ) ) );
    fProfileView.setProfileReference( new TLcdGeodeticReference( new TLcdGeodeticDatum(  ) ) );
    fProfileView.setProfileReference( new TLcdGeodeticReference( new TLcdGeodeticDatum(  ) ) );
    JPanel east_panel = new JPanel( new BorderLayout() );
    east_panel.add( BorderLayout.NORTH,  buildMeasureModePanel() );
    east_panel.add( BorderLayout.CENTER, layer_control );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    // Create a titled panel around the map panel
    TitledPanel profile_panel = TitledPanel.createTitledPanel(
            "Profile view", fProfileView, TitledPanel.NORTH
    );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH,  tool_bar      );
    add( BorderLayout.CENTER, map_panel     );
    add( BorderLayout.EAST,   east_panel    );
    add( BorderLayout.SOUTH,  profile_panel );
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    final ILcdGXYLayer dtedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(fMapJPanel).getLayer();
    if ( dtedLayer != null ) {
      fTerrainElevationProvider.addModel(dtedLayer.getModel());
      // Set world reference to the model reference
      ILcdModelReference model_reference = dtedLayer.getModel().getModelReference();
      if ( model_reference instanceof ILcdXYWorldReference ) {
        fMapJPanel.setXYWorldReference( (ILcdXYWorldReference)model_reference );
      }
    }

    // Create a point model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing some geodetic points",   // source name (is used as tooltip text)
            "Point",          // type name
            "Points"          // display name
    ) );

    // Add some points to the model, to allow comparison between two measurements.
    model.addElement( new TLcdLonLatPoint( 10, 45 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdLonLatPoint( 11, 46 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdLonLatPoint( 11, 45 ), ILcdFireEventMode.NO_EVENT );

    // Create a layer for the point model.
    TeaLayerFactory layer_factory = new TeaLayerFactory();
    ILcdGXYLayer point_layer = layer_factory.createGXYLayer( model );

    // Add the layer to the map.
    GXYLayerUtil.addGXYLayer( fMapJPanel, point_layer, true, false );

    // make the ruler snap to the points in the layer.
    TLcdGXYLayerSubsetList snappables = new TLcdGXYLayerSubsetList();
    Enumeration points_in_layer = point_layer.getModel().elements();
    while ( points_in_layer.hasMoreElements() ) {
      snappables.addElement( points_in_layer.nextElement(), point_layer );
    }
    fTerrainRulerController.setSnappables( snappables );
  }

  private JPanel buildMeasureModePanel() {
    JRadioButton geodetic_measure_mode_radio_button = new JRadioButton( "Geodetic" );
    geodetic_measure_mode_radio_button.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        fTerrainRulerController.setMeasureMode( TLcdAdvancedMapRulerController.MEASURE_GEODETIC );
      }
    } );

    JRadioButton rhumb_line_measure_mode_radio_button = new JRadioButton( "Constant azimuth" );
    rhumb_line_measure_mode_radio_button.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        fTerrainRulerController.setMeasureMode( TLcdAdvancedMapRulerController.MEASURE_RHUMBLINE );
      }
    } );
    geodetic_measure_mode_radio_button.setSelected( true );

    JCheckBox terrain_mode_checkbox = new JCheckBox( "Over terrain" );
    terrain_mode_checkbox.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        boolean useTerrain = e.getStateChange() == ItemEvent.SELECTED;
        fTerrainRulerController.setUseTerrain( useTerrain );
      }
    } );
    terrain_mode_checkbox.setSelected( true );

    ButtonGroup measure_modes_group = new ButtonGroup();
    measure_modes_group.add( geodetic_measure_mode_radio_button );
    measure_modes_group.add( rhumb_line_measure_mode_radio_button );

    JPanel measure_mode_panel = new JPanel( new GridLayout( 3, 1 ) );
    measure_mode_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 5, 5 ) );
    measure_mode_panel.add( geodetic_measure_mode_radio_button );
    measure_mode_panel.add( rhumb_line_measure_mode_radio_button );
    measure_mode_panel.add( terrain_mode_checkbox );

    return TitledPanel.createTitledPanel( "Measure mode", measure_mode_panel );
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Measuring distance over terrain" );
      }
    } );
  }
}
