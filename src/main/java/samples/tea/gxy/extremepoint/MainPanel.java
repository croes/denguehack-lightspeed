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
package samples.tea.gxy.extremepoint;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.tea.TLcdDensityBasedRasterElevationProvider;
import com.luciad.tea.TLcdGXYViewBasedTerrainElevationProvider;
import com.luciad.tea.TLcdTerrainProfileController;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * In this sample, we demonstrate the functionality of the extreme point finder.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel( new TLcdLonLatBounds( 8, 43, 6, 5 ) );

  private ILcdGXYLayer fHighestPointsLayer = ExtremePointLayerFactory.createExtremeMaximaLayer();
  private ILcdGXYLayer fLowestPointsLayer  = ExtremePointLayerFactory.createExtremeMinimaLayer();
  private ILcdGXYLayer fPolygonLayer       = ExtremePointLayerFactory.createPolygonLayer();

  private TLcdGXYViewBasedTerrainElevationProvider fTerrainElevationProvider
          = new TLcdGXYViewBasedTerrainElevationProvider( new TLcdDensityBasedRasterElevationProvider() );

  private ExtremePointAction fExtremePointAction;

  protected void createGUI() {
    // create a terrain elevation provider for DTED and DEM data.
    fTerrainElevationProvider.setGXYView( fMapJPanel );
    fTerrainElevationProvider.setUseOnlyVisibleLayers( true );

    // Create the default tool bar and layer control.
    ToolBar tool_bar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );

    tool_bar.addGXYController( new TLcdTerrainProfileController() );

    JPanel east_panel = new JPanel( new BorderLayout() );
    east_panel.add( BorderLayout.NORTH, createExtremePointPanel() );
    east_panel.add( BorderLayout.CENTER, layer_control );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH,  tool_bar   );
    add( BorderLayout.CENTER, map_panel  );
    add( BorderLayout.EAST,   east_panel );
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    final ILcdGXYLayer dtedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(fMapJPanel).getLayer();

    // Once the DTED data is available, we can compute a better raster step size for
    // the extraction of the highest N points.
    Object object = dtedLayer.getModel().elements().nextElement();
    if ( object instanceof ILcdMultilevelRaster ) {
      ILcdMultilevelRaster multilevel_raster = (ILcdMultilevelRaster) object;
      ILcdRaster raster;

      // Note that the step size is computed based on the raster's pixel density.
      // For DTED, the multilevel raster will return 4 DTED levels, in spite of
      // the fact that only fewer may be available. In this sample, the raster
      // has only 2 levels, so we will use the second. If we set a more "detailed"
      // level, we won't get better accuracy, but the computation time will be
      // greatly increased (or it may even throw an OutOfMemoryException).
      if ( multilevel_raster.getRasterCount() >= 2 )
        raster = multilevel_raster.getRaster( 1 );  // Use DTED level 1
      else
        raster = multilevel_raster.getRaster( 0 );  // Use DTED level 0

      fExtremePointAction.setRaster( raster );
      fExtremePointAction.setRasterReference( (ILcdGeoReference) dtedLayer.getModel().getModelReference() );
      if (fExtremePointAction.isEnabled()) {
        fExtremePointAction.actionPerformed( null );
      }
    }

    GXYLayerUtil.addGXYLayer( fMapJPanel, fPolygonLayer, false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, fHighestPointsLayer, false, false );
    GXYLayerUtil.addGXYLayer( fMapJPanel, fLowestPointsLayer,  false, false );
    GXYLayerUtil.fitGXYLayer( fMapJPanel, fPolygonLayer );

    // Zoom out a bit, for a better view on the polygon layer.
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        fMapJPanel.setScale( fMapJPanel.getScale() / 1.1 );
        fMapJPanel.repaint();
      }
    } );
  }

  /**
   * Creates a panel to allow the user to configure the extreme point computation.
   * @return a panel to allow the user to configure the extreme point computation.
   */
  private JPanel createExtremePointPanel() {
    JLabel requested_points_label    = new JLabel( "Number of points:    " );
    JLabel separation_distance_label = new JLabel( "Separation distance: " );
    JLabel separation_height_label   = new JLabel( "Separation height:   " );
    JCheckBox separation_distance_infinity = new JCheckBox( "+Infinity" );
    JCheckBox separation_height_infinity   = new JCheckBox( "+Infinity" );
    JButton compute_button = new JButton( "Compute" );

    final MyTextField requested_points    = new MyTextField(   30, 5 );
    final MyTextField separation_distance = new MyTextField( 2500, 5 );
    final MyTextField separation_height   = new MyTextField(  100, 5 );

    requested_points   .setHorizontalAlignment( JTextField.RIGHT );
    separation_distance.setHorizontalAlignment( JTextField.RIGHT );
    separation_height  .setHorizontalAlignment( JTextField.RIGHT );

    GridBagConstraints constraints = new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0,0,0,0 ), 0, 0
    );

    JPanel panel = new JPanel( new GridBagLayout() );
    panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    constraints.gridx     = 0;
    constraints.gridy     = 0;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( requested_points_label, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( requested_points, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 1;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( separation_distance_label, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( separation_distance, constraints );
    constraints.gridx++;
    constraints.weightx   = 0;
    panel.add( Box.createHorizontalStrut( 5 ), constraints );
    constraints.gridx++;
    panel.add( separation_distance_infinity, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 2;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( separation_height_label, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( separation_height, constraints );
    constraints.gridx++;
    constraints.weightx   = 0;
    panel.add( Box.createHorizontalStrut( 5 ), constraints );
    constraints.gridx++;
    panel.add( separation_height_infinity, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 3;
    constraints.weightx   = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add( Box.createVerticalStrut( 5 ), constraints );

    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add( compute_button, constraints );

    // Setup the action
    ILcdPolygon polygon           = (ILcdPolygon)      fPolygonLayer.getModel().elements().nextElement();
    ILcdGeoReference polygon_reference = (ILcdGeoReference) fPolygonLayer.getModel().getModelReference();

    fExtremePointAction = new ExtremePointAction(
            fMapJPanel,
            polygon,
            polygon_reference,
            fTerrainElevationProvider
    );
    fExtremePointAction.setMaximumPointModel( fHighestPointsLayer.getModel() );
    fExtremePointAction.setMinimumPointModel( fLowestPointsLayer .getModel() );
    fExtremePointAction.setRequestedPoints   ( ( (Number) requested_points    .getValue() ).intValue()    );
    fExtremePointAction.setSeparationDistance( ( (Number) separation_distance .getValue() ).doubleValue() );
    fExtremePointAction.setSeparationHeight  ( ( (Number) separation_height   .getValue() ).doubleValue() );

    // Listen to changes in the polygon.
    fPolygonLayer.getModel().addModelListener(new ILcdModelListener() {
      public void modelChanged( TLcdModelChangedEvent aModelChangedEvent ) {
        ILcdModel model = fPolygonLayer.getModel();
        try (TLcdLockUtil.Lock readLock = TLcdLockUtil.readLock(model)) {
          Enumeration element = model.elements();
          if (element.hasMoreElements()) {
            ILcdShape firstShape = (ILcdShape) element.nextElement();
            fExtremePointAction.setShape(firstShape);
          }
        }
      }
    });

    separation_distance_infinity.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent aEvent ) {
        separation_distance.setEnabled( aEvent.getStateChange() == ItemEvent.DESELECTED );
      }
    } );

    separation_height_infinity.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent aEvent ) {
        separation_height.setEnabled( aEvent.getStateChange() == ItemEvent.DESELECTED );
      }
    } );

    compute_button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent aEvent ) {
        fExtremePointAction.setRequestedPoints   ( ( (Number) requested_points.getValue() ).intValue() );
        fExtremePointAction.setSeparationDistance( separation_distance.isEnabled() ? ( (Number) separation_distance.getValue() ).doubleValue() : Double.POSITIVE_INFINITY );
        fExtremePointAction.setSeparationHeight  ( separation_height  .isEnabled() ? ( (Number) separation_height  .getValue() ).doubleValue() : Double.POSITIVE_INFINITY );

        if ( fExtremePointAction.getRequestedPoints() < 0 ) {
          JOptionPane.showMessageDialog( fMapJPanel, "The number of points should be strict positive", "Error", JOptionPane.ERROR_MESSAGE );
        }
        else if ( fExtremePointAction.getSeparationDistance() < 0 ) {
          JOptionPane.showMessageDialog( fMapJPanel, "The separation distance should be strict positive", "Error", JOptionPane.ERROR_MESSAGE );
        }
        else if ( fExtremePointAction.getSeparationHeight() < 0 ) {
          JOptionPane.showMessageDialog( fMapJPanel, "The separation height should be strict positive", "Error", JOptionPane.ERROR_MESSAGE );
        }
        else {
          if (fExtremePointAction.isEnabled()) {
            fExtremePointAction.actionPerformed( aEvent );
          }
        }
      }
    } );

    return TitledPanel.createTitledPanel( "Find extreme points", panel );
  }

  private static class MyTextField extends JFormattedTextField {
    public MyTextField( int aValue, int aColumns ) {
      super( NumberFormat.getIntegerInstance() );
      setValue( new Integer( aValue ) );
      setColumns( aColumns );
    }
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        new LuciadFrame( new MainPanel(), "Extreme points" );
      }
    } );
  }

}
