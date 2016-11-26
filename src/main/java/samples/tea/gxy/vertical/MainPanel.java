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
package samples.tea.gxy.vertical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Enumeration;

import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.geodesy.TLcdGeodeticDatum;
import samples.common.LuciadFrame;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.tea.TLcdVVTerrainProfileModel;
import com.luciad.tea.TLcdVVTerrainProfileRenderer;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.map.TLcdMapJPanel;
import com.luciad.view.vertical.TLcdAltitudeRangeSliderAdapter;
import com.luciad.view.vertical.TLcdVVJPanel;
import com.luciad.view.vertical.TLcdVVWithControllersJPanel;

import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.tea.gxy.TeaLayerFactory;

/**
 * This class illustrates the use of a vertical view with the terrain VV model.
 */
public class MainPanel extends SamplePanel {


  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel( new TLcdLonLatBounds( 9.00, 44.50, 4.00, 3.00 ) );

  private TLcdVVTerrainProfileModel fVVTerrainModel;

  protected void createGUI() {
    // make a terrain VV model.
    fVVTerrainModel = new TLcdVVTerrainProfileModel();
    fVVTerrainModel.setGXYView( fMapJPanel );

    // Add a vertical view to display the terrain beneath a polyline
    TLcdVVJPanel vv_panel = new TLcdVVJPanel();
    vv_panel.setAdjustTopGridLineWhenEqual( true );
    vv_panel.setAdjustBottomGridLineWhenEqual( true );
    vv_panel.setAltitudeUnit( TLcdAltitudeUnit.METRE );
    vv_panel.setVVModel( fVVTerrainModel );
    vv_panel.setVVRenderer( new TLcdVVTerrainProfileRenderer() );
    vv_panel.setBuffered( true );

    TLcdVVWithControllersJPanel vv_controllers = new TLcdVVWithControllersJPanel(
            vv_panel, TLcdVVWithControllersJPanel.LEFT_RIGHT_OFFSET | TLcdVVWithControllersJPanel.TOP_BOTTOM_OFFSET
    ) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension( 0, 200 );
      }

      @Override
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };
    // create an adapter that adapts the slider when the model of the vertical view changes. 
    new TLcdAltitudeRangeSliderAdapter(vv_panel, vv_controllers.getAltitudeRangeSliderPanel());

    // Create the default toolbar and layer control.
    ToolBar tool_bar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    // Create a titled panel around the vertical view panel
    TitledPanel vv_titled_panel = TitledPanel.createTitledPanel( "Vertical View", vv_controllers );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH, tool_bar );
    add( BorderLayout.CENTER, map_panel );
    add( BorderLayout.EAST, layer_control );
    add( BorderLayout.SOUTH, vv_titled_panel );
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    final ILcdGXYLayer dtedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(fMapJPanel).getLayer();
    if ( dtedLayer != null ) {
      // Set world reference to the model reference
      ILcdModelReference modelReference = dtedLayer.getModel().getModelReference();
      if ( modelReference instanceof ILcdXYWorldReference ) {
        fMapJPanel.setXYWorldReference( (ILcdXYWorldReference)modelReference );
      }
    }

    // Create a polyline model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "The polyline to draw in the vertical view.",   // source name (is used as tooltip text)
            "Polyline",       // type name
            "Polyline"        // display name
    ) );

    // Add a polyline to the model.
    TLcdLonLatHeightPoint[] points = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint(  9.5, 45, 200 ),
            new TLcdLonLatHeightPoint( 10.5, 47, 400 ),
            new TLcdLonLatHeightPoint( 12.5, 45, 400 ),
            new TLcdLonLatHeightPoint( 12.5, 47, 350 )
    };
    TLcd3DEditablePointList point_list = new TLcd3DEditablePointList( points, false );
    TLcdLonLatHeightPolyline polyline = new TLcdLonLatHeightPolyline( point_list );
    model.addElement( polyline, ILcdFireEventMode.NO_EVENT );
    
    // Create a layer for the polyline model.
    TeaLayerFactory layer_factory = new TeaLayerFactory();
    ILcdGXYLayer polyline_layer = layer_factory.createGXYLayer( model );
    polyline_layer.addSelectionListener( new PolylineSelectionListener() );

    // Select the polyline.
    polyline_layer.selectObject( polyline, true, ILcdFireEventMode.FIRE_NOW );

    // Add the layer to the map.
    GXYLayerUtil.addGXYLayer( fMapJPanel, polyline_layer, true, false );
  }

  private class PolylineSelectionListener implements ILcdSelectionListener {
    public void selectionChanged( TLcdSelectionChangedEvent event ) {
      Enumeration  selected_objects  = event.selectedElements();
      ILcdPolyline selected_polyline = null;
      while ( selected_objects.hasMoreElements() && selected_polyline == null ) {
        Object selected_object = selected_objects.nextElement();
        if ( selected_object instanceof ILcdPolyline ) {
          selected_polyline = (ILcdPolyline) selected_object;
        }
      }

      fVVTerrainModel.setPointList( selected_polyline );
      fVVTerrainModel.setPointListGXYLayer( (ILcdGXYLayer) event.getSource() );
      fVVTerrainModel.update( true );
    }
  }

  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Vertical view with terrain" );
      }
    } );
  }
}
