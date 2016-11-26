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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.tea.ILcdLOSCoverage;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.TLcdCoverageFillMode;
import com.luciad.tea.TLcdLOSCoverage;
import com.luciad.util.ILcdSelection;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.SwingUtil;
import samples.gxy.common.TitledPanel;
import samples.tea.Legend;

/**
 * A input panel for line-of-sight computations.
 */
class LOSPanel extends JPanel {

  public static final int PROPAGATION_FUNCTION_UNKNOWN        = -1;
  public static final int PROPAGATION_FUNCTION                =  0;
  public static final int PROPAGATION_FUNCTION_SKY_BACKGROUND =  1;
  public static final int PROPAGATION_FUNCTION_FIXED_HEIGHT   =  2;

  private ILcdPoint    fCenterPoint;
  private ILcdGXYLayer fCenterPointLayer;
  private boolean fAutomaticSteps = false;
  private boolean fAdvancedMode = false;

  private CenterPointSelectionListener fCenterPointSelectionListener = new CenterPointSelectionListener();
  private CenterPointFollower fCenterPointFollower = new CenterPointFollower();
  private RasterPixelDensityProvider fRasterPixelDensityProvider;
  private RasterLayerRasterProvider fRasterLayerRasterProvider;

  // GUI
  // input filled in
  private JTextField fStartAngleField         = new JTextField( "0.0" );
  private JTextField fArcAngleField           = new JTextField( "360.0" );
  private JTextField fAngleStepsizeField      = new JTextField( "1.00" );
  private JTextField fMaxRadiusField          = new JTextField( "50000" );
  private JTextField fRadiusStepsizeField     = new JTextField( "500.00" );
  private JTextField fHeightAboveGroundField  = new JTextField( "100" );
  private JTextField fMinVerticalAngleField   = new JTextField( "85" );
  private JTextField fMaxVerticalAngleField   = new JTextField( "95" );
  private JTextField fPixelDensityTextField   = new JTextField();
  private JComboBox  fTargetLayerComboBox     = new JComboBox();
  private JButton fAdvancedSimpleButton       = new JButton( "Advanced >>>" );
  private JButton fPixelDensityAdvancedButton = new JButton( "Help" );
  private JButton fPixelDensityHelpButton     = new JButton( "Compute" );
  private JLabel fAngleStepLabel;
  private JLabel fRadiusStepLabel;
  private JLabel fRadiusStepUnitLabel;
  private JTextField fRadarTiltAngleField   = new JTextField( "0.0" );
  private JTextField fRadarTiltAzimuthField   = new JTextField( "0.0" );
  private JTextField fKFactorField   = new JTextField( "1.0" );

  // result type
  private JComboBox fOutputComboBox = new JComboBox();
  private static final String RASTER = "Raster";
  private static final String CONTOURS = "Contours";

  // input selected from view
  private JTextField fCenterXField = new JTextField();
  private JTextField fCenterYField = new JTextField();
  private JLabel fCenterLayerLabel = new JLabel( "layer label" );
  private JLabel fCenterReferenceLabel = new JLabel( "model ref" );

  // automatic computation of steps
  private JCheckBox fAutomaticCheckBox = new JCheckBox( "Use best arc/radius step for sample density" );

  // fill mode
  private JRadioButton fMinimumRadioButton = new JRadioButton( "Minimum" );
  private JRadioButton fMaximumRadioButton = new JRadioButton( "Maximum" );
  private JRadioButton fNearestNeighbourRadioButton = new JRadioButton( "Nearest" );

  // altitude mode
  private JRadioButton fAboveTerrainRadioButton   = new JRadioButton( "Above terrain" );
  private JRadioButton fAboveGeoidRadioButton     = new JRadioButton( "Above geoid" );
  private JRadioButton fAboveEllipsoidRadioButton = new JRadioButton( "Above ellipsoid" );

  // propagation function
  private JRadioButton fPropagationFunctionNormal        = new JRadioButton( "Normal" );
  private JRadioButton fPropagationFunctionSkyBackground = new JRadioButton( "Sky in background" );
  private JRadioButton fPropagationFunctionFixedHeight   = new JRadioButton( "Fixed height:" );
  private JTextField fFixedHeightAboveEllipsoidField = new JTextField( "2000" );

  private JPanel fLegendPanel;

  // pixel density computation
  private JDialog fPixelDensityDialog;
  private RasterTableModel fRasterTableModel = new RasterTableModel();
  private JComboBox fMultilevelComboBox;
  private JLabel fStepLevelSelectionLabel;
  private int fSelectedRow = -1;
  private NumberFormat fNumberFormat1 = new DecimalFormat( "0.00E00" );
  private NumberFormat fNumberFormat2 = new DecimalFormat( "###.##" );
  private JTextField fComputedPixelDensityTextField;
  private JPanel fTargetHostPanel;
  private JTable fRasterTable;

  LOSPanel() {

    fOutputComboBox.addItem( RASTER );
    fOutputComboBox.addItem( CONTOURS );

    initialize();
    buildPanel();
    fRasterPixelDensityProvider = new RasterPixelDensityProvider();
  }

  void setCenterLayers( ILcdGXYLayer[] aCenterLayers ) {
    for ( int center_point_layer_index = 0; center_point_layer_index < aCenterLayers.length ; center_point_layer_index++ ) {
      aCenterLayers[ center_point_layer_index ].addSelectionListener( fCenterPointSelectionListener );
    }
  }

  void setTargetLayers( ILcdGXYLayer[] aTargetLayers ) {
    for ( int target_layer_index = 0; target_layer_index < aTargetLayers.length ; target_layer_index++ ) {
      fTargetLayerComboBox.addItem( aTargetLayers[ target_layer_index ] );
    }
  }

  void setRasterLayerRasterProvider( RasterLayerRasterProvider aRasterLayerRasterProvider ) {
    fRasterLayerRasterProvider = aRasterLayerRasterProvider;
  }

  ILcdLOSCoverage createLOSCoverage() {
    // Check if a center point was selected.
    if ( getCenterPoint() == null ) {
      throw new IllegalArgumentException( "Please select a center point first." );
    }

    ILcdModel        model               = retrieveTargetLayer().getModel();
    ILcdGeoReference aTargetReference    = (ILcdGeoReference) model.getModelReference();
    double           aTargetPixelDensity = retrievePixelDensity();

    // Check the pixel density. When it is too high, the computation will be too long for this sample.
    if ( ( aTargetReference.getCoordinateType() == ILcdGeoReference.CARTESIAN && aTargetPixelDensity > 0.0002 ) ||
         ( aTargetReference.getCoordinateType() == ILcdGeoReference.GEODETIC  && aTargetPixelDensity > 1.44E8 ) ) {
      throw new IllegalArgumentException( "The pixel density is too high." );
    }

    ILcd3DEditablePoint      aCenterPoint             = getCenterPoint().cloneAs3DEditablePoint();
    ILcdGeoReference         aCenterPointReference    = retrievePointReference();
    double                   aCenterPointAltitude     = retrieveHeightAboveGround();
    TLcdCoverageAltitudeMode aCenterPointAltitudeMode = TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL;

    boolean aAutomaticSteps = retrieveAutomaticSteps();

    // The radius variables should be positive.
    double aRadiusMax  = Math.abs( retrieveMaxRadius() );
    double aRadiusStep = aAutomaticSteps ? 0 : Math.abs( retrieveRadiusStepSize() );

    // The angle step should be positive.
    double aAngleStart = retrieveStartAngle();
    double aAngleArc   = retrieveArcAngle();
    double aAngleStep  = aAutomaticSteps ? 0 : Math.abs( retrieveAngleStepSize() );

    // If the angle arc is negative, make it positive and adjust the start angle.
    if ( aAngleArc < 0 ) {
      aAngleStart = aAngleStart + aAngleArc;
      aAngleArc   = -aAngleArc;
    }

    double  aSampleDensity  = (float)aTargetPixelDensity;
    double  aRadialFraction = 1.0;

    ILcdLOSCoverage los_coverage = createLOSCoverage(
            aCenterPoint,
            aCenterPointReference,
            aCenterPointAltitude,
            aCenterPointAltitudeMode,
            aRadiusMax,
            aRadiusStep,
            aAngleStart,
            aAngleArc,
            aAngleStep,
            aAutomaticSteps,
            aTargetReference,
            aSampleDensity,
            aRadialFraction
    );

    if ( aAutomaticSteps ) {
      fAngleStepsizeField .setText( fNumberFormat2.format( new Double( los_coverage.getAngleStep () ) ) );
      fRadiusStepsizeField.setText( fNumberFormat2.format( new Double( los_coverage.getRadiusStep() ) ) );
    }

    return los_coverage;
  }

  double retrieveMinVerticalAngle() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fMinVerticalAngleField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for minimum vertical angle: [" + fMinVerticalAngleField.getText() + "]" );
    }
  }

  double retrieveMaxVerticalAngle() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fMaxVerticalAngleField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for maximum vertical angle: [" + fMinVerticalAngleField.getText() + "]" );
    }
  }

  TLcdCoverageFillMode retrieveFillMode() {
    if ( fNearestNeighbourRadioButton.isSelected() ) {
      return TLcdCoverageFillMode.NEAREST_NEIGHBOR;
    } else if ( fMinimumRadioButton.isSelected() ) {
      return TLcdCoverageFillMode.MINIMUM;
    } else {
      return TLcdCoverageFillMode.MAXIMUM;
    }
  }

  TLcdCoverageAltitudeMode retrieveAltitudeMode() {
    if ( fAboveTerrainRadioButton.isSelected() ) {
      return TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL;
    } else if ( fAboveGeoidRadioButton.isSelected() ) {
      return TLcdCoverageAltitudeMode.ABOVE_GEOID;
    } else {
      return TLcdCoverageAltitudeMode.ABOVE_ELLIPSOID;
    }
  }

  int retrieveComputationAlgorithm() {
    if ( fPropagationFunctionNormal.isSelected() ) {
      return PROPAGATION_FUNCTION;
    }
    else if ( fPropagationFunctionSkyBackground.isSelected() ) {
      return PROPAGATION_FUNCTION_SKY_BACKGROUND;
    }
    else if ( fPropagationFunctionFixedHeight.isSelected() ) {
      return PROPAGATION_FUNCTION_FIXED_HEIGHT;
    }
    else {
      return PROPAGATION_FUNCTION_UNKNOWN;
    }
  }

  double retrieveFixedHeightAboveEllipsoid() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fFixedHeightAboveEllipsoidField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for fixed height AboveEllipsoid: [" + fFixedHeightAboveEllipsoidField.getText() + "]" );
    }
  }

  ILcdGXYLayer retrieveTargetLayer() {
    return (ILcdGXYLayer) fTargetLayerComboBox.getSelectedItem();
  }

  /**
   * Returns whether the LOS Coverage should be output as contours or as a raster.
   * @return whether the LOS Coverage should be output as contours or as a raster.
   */
  boolean isOutputAsContours() {
    return fOutputComboBox.getSelectedItem() == CONTOURS;
  }

  double retrievePixelDensity() throws IllegalArgumentException {
    double pixel_density;
    if ( !fAdvancedMode ) {
      pixel_density = computePixelDensityDirect();
    } else {
      try {
        Number pixel_density_N = fNumberFormat1.parse( fPixelDensityTextField.getText() );
        pixel_density = pixel_density_N.doubleValue();
      }
      catch ( ParseException e ) {
        throw new IllegalArgumentException( "Invalid value for pixel density: [" + fPixelDensityTextField.getText() + "]" );
      }
    }
    return pixel_density;
  }

  private void initialize() {
    ActionListener action_listener = new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        updateLOSCoverage();
      }
    };
    fStartAngleField        .addActionListener( action_listener );
    fRadarTiltAngleField    .addActionListener( action_listener );
    fRadarTiltAzimuthField  .addActionListener( action_listener );
    fKFactorField           .addActionListener( action_listener );
    fArcAngleField          .addActionListener( action_listener );
    fAngleStepsizeField     .addActionListener( action_listener );
    fMaxRadiusField         .addActionListener( action_listener );
    fRadiusStepsizeField    .addActionListener( action_listener );
    fHeightAboveGroundField .addActionListener( action_listener );
    fPixelDensityTextField  .addActionListener( action_listener );
    fTargetLayerComboBox    .addActionListener( action_listener );
    fOutputComboBox.addActionListener( action_listener );
    fAutomaticCheckBox      .addActionListener( action_listener );

    FocusListener focus_listener = new FocusListener() {
      public void focusGained( FocusEvent e ) {
      }

      public void focusLost( FocusEvent e ) {
        updateLOSCoverage();
      }
    };
    fStartAngleField        .addFocusListener( focus_listener );
    fRadarTiltAngleField    .addFocusListener( focus_listener );
    fRadarTiltAzimuthField  .addFocusListener( focus_listener );
    fKFactorField           .addFocusListener( focus_listener );
    fArcAngleField          .addFocusListener( focus_listener );
    fAngleStepsizeField     .addFocusListener( focus_listener );
    fMaxRadiusField         .addFocusListener( focus_listener );
    fRadiusStepsizeField    .addFocusListener( focus_listener );
    fHeightAboveGroundField .addFocusListener( focus_listener );
    fMinVerticalAngleField  .addFocusListener( focus_listener );
    fMaxVerticalAngleField  .addFocusListener( focus_listener );
    fPixelDensityTextField  .addFocusListener( focus_listener );
  }

  private void prepareTargetPanel() {
    // clear the pixel density when changing the target layer
    fTargetLayerComboBox.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          fPixelDensityTextField.setText( "" );
          updateLOSCoverage();
        }
      }
    } );

    fOutputComboBox.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          fPixelDensityTextField.setText( "" );
          updateLOSCoverage();
        }
      }
    } );

    fAdvancedSimpleButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        switchMode();
      }
    } );

    fAutomaticCheckBox.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        fAutomaticSteps = ( e.getStateChange() == ItemEvent.SELECTED );
        updateEnabledComponents();
      }
    } );
    fAutomaticCheckBox.setToolTipText( "Compute the radial and arc step automatically based on the sample rate." );

    ButtonGroup fill_mode_group = new ButtonGroup();
    fill_mode_group.add( fMinimumRadioButton );
    fill_mode_group.add( fMaximumRadioButton );
    fill_mode_group.add( fNearestNeighbourRadioButton );
    fMaximumRadioButton.setSelected( true );

    ButtonGroup altitude_mode_group = new ButtonGroup();
    altitude_mode_group.add( fAboveTerrainRadioButton   );
    altitude_mode_group.add( fAboveGeoidRadioButton     );
    altitude_mode_group.add( fAboveEllipsoidRadioButton );
    fAboveTerrainRadioButton.setSelected( true );

    ButtonGroup post_processing_group = new ButtonGroup();
    post_processing_group.add( fPropagationFunctionNormal        );
    post_processing_group.add( fPropagationFunctionSkyBackground );
    post_processing_group.add( fPropagationFunctionFixedHeight   );
    fPropagationFunctionNormal.setSelected( true );
    fFixedHeightAboveEllipsoidField.setHorizontalAlignment( JTextField.RIGHT );
    fFixedHeightAboveEllipsoidField.setColumns( 5 );

    fPropagationFunctionNormal.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          switchLegend();
        }
      }
    } );
    fPropagationFunctionSkyBackground.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          switchLegend();
        }
      }
    } );
    fPropagationFunctionFixedHeight.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          switchLegend();
        }
      }
    } );

    fPixelDensityTextField.setColumns( 6 );

    fPixelDensityHelpButton.setToolTipText( "Compute the density for me ..." );
    fPixelDensityHelpButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        try {
          double target_density = computePixelDensityDirect();
          fPixelDensityTextField.setText( fNumberFormat1.format( target_density ) );
          updateLOSCoverage();
        }
        catch ( IllegalArgumentException exception ) {
          JOptionPane.showMessageDialog(
                  LOSPanel.this, exception.getMessage(), "Could not fill in density", JOptionPane.ERROR_MESSAGE
          );
        }
      }
    } );

    fPixelDensityAdvancedButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        try {
          findPixelDensity();
        }
        catch ( IllegalArgumentException exception ) {
          JOptionPane.showMessageDialog(
                  LOSPanel.this, exception.getMessage(), "Could not compute density", JOptionPane.ERROR_MESSAGE 
          );
        }
      }
    } );
  }

  private void buildPanel() {
    setLayout( new BorderLayout() );

    JPanel top_panel_west = new JPanel( new GridLayout( 2, 1 ) );
    top_panel_west.add( buildCenterPointPanel() );
    top_panel_west.add( buildRadiusPanel() );

    JPanel top_panel_east = new JPanel( new GridLayout( 2, 1 ) );
    top_panel_east.add( buildArcPanel() );
    top_panel_east.add( buildVisibilityPanel() );

    JPanel separator_panel = new JPanel( new BorderLayout() );
    separator_panel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 0 ) );
    separator_panel.add( new JSeparator( JSeparator.VERTICAL ), BorderLayout.CENTER );

    JPanel top_panel_east2 = new JPanel( new BorderLayout() );
    top_panel_east2.add( BorderLayout.WEST, separator_panel );
    top_panel_east2.add( BorderLayout.CENTER, top_panel_east );

    JPanel top_panel = new JPanel( new BorderLayout() );
    top_panel.add( BorderLayout.CENTER, top_panel_west );
    top_panel.add( BorderLayout.EAST, top_panel_east2 );

    JPanel center_west_panel = new JPanel( new BorderLayout() );
    center_west_panel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 2, 0 ) );
    center_west_panel.add( buildComputationPanel(), BorderLayout.CENTER );

    JPanel center_east_panel = new JPanel( new BorderLayout() );
    center_east_panel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 2, 0 ) );
    center_east_panel.add( buildRadarPanel(), BorderLayout.CENTER );

    JPanel separator_panel2 = new JPanel( new BorderLayout() );
    separator_panel2.setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 0 ) );
    separator_panel2.add( new JSeparator( JSeparator.VERTICAL ), BorderLayout.CENTER );

    JPanel center_panel = new JPanel( new BorderLayout() );
    center_panel.add( BorderLayout.WEST, center_west_panel );
    center_panel.add( BorderLayout.CENTER, separator_panel2 );
    center_panel.add( BorderLayout.EAST, center_east_panel );

    prepareTargetPanel();

    fLegendPanel = new JPanel( new BorderLayout() ) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size1 = buildLegendDefault().getPreferredSize();
        Dimension size2 = buildLegendFixedHeight().getPreferredSize();
        return new Dimension( Math.max( size1.width, size2.width ), Math.max( size1.height, size2.height ) );
      }
    };
    fLegendPanel.add( buildLegend(), BorderLayout.CENTER );

    fTargetHostPanel = new JPanel( new BorderLayout() );
    fTargetHostPanel.add( fLegendPanel, BorderLayout.CENTER );
    fTargetHostPanel.add( buildTargetPanel( fAdvancedMode ), BorderLayout.EAST );

    add( top_panel, BorderLayout.NORTH );
    add( center_panel, BorderLayout.CENTER );
    add( fTargetHostPanel, BorderLayout.SOUTH );
  }

  private JPanel buildCenterPointPanel() {
    JPanel center_point_panel = new JPanel( new GridBagLayout() );
    center_point_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.ipadx = 4;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 1;
    center_point_panel.add( new JLabel( "X:" ), gc );

    gc.gridx = 1;
    gc.gridy = 0;
    gc.gridwidth = 4;
    fCenterXField.setColumns( 9 );
    fCenterXField.setEditable( false );
    center_point_panel.add( fCenterXField, gc );

    gc.gridx = 6;
    gc.gridy = 0;
    gc.gridwidth = 1;
    center_point_panel.add( new JLabel( "Y:" ), gc );

    gc.gridx = 7;
    gc.gridy = 0;
    gc.gridwidth = 4;
    fCenterYField.setColumns( 9 );
    fCenterYField.setEditable( false );
    center_point_panel.add( fCenterYField, gc );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 3;
    center_point_panel.add( new JLabel( "Layer:" ), gc );

    gc.gridx = 3;
    gc.gridy = 1;
    gc.gridwidth = 8;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.anchor = GridBagConstraints.WEST;
    center_point_panel.add( fCenterLayerLabel, gc );

    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 4;
    gc.anchor = GridBagConstraints.WEST;
    center_point_panel.add( new JLabel( "Reference:" ), gc );

    gc.gridx = 4;
    gc.gridy = 2;
    gc.gridwidth = 7;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.anchor = GridBagConstraints.WEST;
    center_point_panel.add( fCenterReferenceLabel, gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( center_point_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Center point", panel );
  }

  private JPanel buildArcPanel() {
    JPanel arc_panel = new JPanel();
    arc_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    arc_panel.setLayout( new GridBagLayout() );
    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.ipadx = 4;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 3;
    arc_panel.add( new JLabel( "Start:" ), gc );

    gc.gridx = 3;
    gc.gridy = 0;
    gc.gridwidth = 3;
    fStartAngleField.setColumns( 6 );
    arc_panel.add( fStartAngleField, gc );

    gc.gridx = 7;
    gc.gridy = 0;
    gc.gridwidth = 3;
    arc_panel.add( new JLabel( "Extent:" ), gc );

    gc.gridx = 10;
    gc.gridy = 0;
    gc.gridwidth = 3;
    fArcAngleField.setColumns( 6 );
    arc_panel.add( fArcAngleField, gc );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 3;
    fAngleStepLabel = new JLabel( "Step:" );
    arc_panel.add( fAngleStepLabel, gc );

    gc.gridx = 3;
    gc.gridy = 1;
    gc.gridwidth = 3;
    fAngleStepsizeField.setColumns( 6 );
    arc_panel.add( fAngleStepsizeField, gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( arc_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Arc", panel );
  }

  private JPanel buildRadiusPanel() {
    JPanel radius_panel = new JPanel( new GridBagLayout() );
    radius_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.ipadx = 4;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 3;
    radius_panel.add( new JLabel( "Maximum:" ), gc );

    fMaxRadiusField.setColumns( 6 );
    gc.gridx = 3;
    gc.gridy = 0;
    gc.gridwidth = 6;
    radius_panel.add( fMaxRadiusField, gc );

    gc.gridx = 9;
    gc.gridy = 0;
    gc.gridwidth = 1;
    radius_panel.add( new JLabel( " m" ), gc );

    fRadiusStepLabel = new JLabel( "Step:" );
    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 2;
    radius_panel.add( fRadiusStepLabel, gc );

    fRadiusStepsizeField.setColumns( 6 );
    gc.gridx = 2;
    gc.gridy = 1;
    gc.gridwidth = 3;
    radius_panel.add( fRadiusStepsizeField, gc );

    gc.gridx = 5;
    gc.gridy = 1;
    gc.gridwidth = 1;
    fRadiusStepUnitLabel = new JLabel( " m" );
    radius_panel.add( fRadiusStepUnitLabel, gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( radius_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Radius", panel );
  }

  private JPanel buildVisibilityPanel() {
    JPanel visibility_panel = new JPanel( new GridBagLayout() );
    visibility_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.ipadx = 4;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 5;
    visibility_panel.add( new JLabel( "Minimum vertical angle:" ), gc );

    gc.gridx = 5;
    gc.gridy = 0;
    gc.gridwidth = 2;
    fMinVerticalAngleField.setColumns( 3 );
    visibility_panel.add( fMinVerticalAngleField, gc );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 5;
    visibility_panel.add( new JLabel( "Maximum vertical angle:" ), gc );

    gc.gridx = 5;
    gc.gridy = 1;
    gc.gridwidth = 2;
    fMaxVerticalAngleField.setColumns( 3 );
    visibility_panel.add( fMaxVerticalAngleField, gc );

    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 5;
    visibility_panel.add( new JLabel( "Height above ground:" ), gc );

    gc.gridx = 5;
    gc.gridy = 2;
    gc.gridwidth = 3;
    fHeightAboveGroundField.setColumns( 5 );
    visibility_panel.add( fHeightAboveGroundField, gc );

    gc.gridx = 8;
    gc.gridy = 2;
    gc.gridwidth = 1;
    visibility_panel.add( new JLabel( " m" ), gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( visibility_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Visibility", panel );
  }

  private JPanel buildTargetPanel( boolean aAdvancedMode ) {
    JPanel target_panel = new JPanel( new GridBagLayout() );
    target_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = GridBagConstraints.REMAINDER;
    target_panel.add( new JLabel( "Retain values" ), gc );

    JPanel fill_modes = new JPanel( new GridLayout( 1, 0 ) );
    fill_modes.add( fMinimumRadioButton );
    fill_modes.add( fMaximumRadioButton );
    fill_modes.add( fNearestNeighbourRadioButton );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = GridBagConstraints.REMAINDER;
    target_panel.add( fill_modes, gc );

    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = GridBagConstraints.REMAINDER;
    target_panel.add( new JLabel( "Interpret height as" ), gc );

    JPanel altitude_modes = new JPanel( new GridLayout( 1, 0 ) );
    altitude_modes.add( fAboveTerrainRadioButton   );
    altitude_modes.add( fAboveGeoidRadioButton     );
    altitude_modes.add( fAboveEllipsoidRadioButton );

    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = GridBagConstraints.REMAINDER;
    target_panel.add( altitude_modes, gc );

    gc.gridx = 0;
    gc.gridy = 5;
    gc.gridwidth = 2;
    target_panel.add( new JLabel( "Output:" ), gc );

    gc.gridx = 3;
    gc.gridy = 5;
    gc.gridwidth = 3;
    target_panel.add( fOutputComboBox, gc );

    gc.gridx = 0;
    gc.gridy = 6;
    gc.gridwidth = 2;
    target_panel.add( new JLabel( "Layer:" ), gc );

    gc.gridx = 2;
    gc.gridy = 6;
    gc.gridwidth = 1;
    target_panel.add( Box.createHorizontalStrut( 5 ), gc );

    gc.gridx = 3;
    gc.gridy = 6;
    gc.gridwidth = 3;
    target_panel.add( fTargetLayerComboBox, gc );

    gc.gridx = 6;
    gc.gridy = 6;
    gc.gridwidth = 1;
    target_panel.add( Box.createHorizontalStrut( 5 ), gc );

    gc.gridx = 7;
    gc.gridy = 6;
    gc.gridwidth = 3;
    gc.fill = GridBagConstraints.HORIZONTAL;
    target_panel.add( fAdvancedSimpleButton, gc );
    gc.fill = GridBagConstraints.NONE;

    gc.gridx = 0;
    gc.gridy = 7;
    gc.gridwidth = 1;
    target_panel.add( Box.createVerticalStrut( 5 ), gc );

    if ( aAdvancedMode ) {
      JPanel advanced_panel = new JPanel( new GridBagLayout() );

      GridBagConstraints gc2 = new GridBagConstraints();
      gc2.anchor = GridBagConstraints.WEST;

      gc2.gridx = 0;
      gc2.gridy = 8;
      gc2.gridwidth = 3;
      advanced_panel.add( new JLabel( "Raster density:" ), gc2 );

      gc2.gridx = 3;
      gc2.gridy = 8;
      gc2.gridwidth = 1;
      advanced_panel.add( Box.createHorizontalStrut( 5 ), gc2 );

      gc2.gridx = 4;
      gc2.gridy = 8;
      gc2.gridwidth = 2;
      advanced_panel.add( fPixelDensityTextField, gc2 );

      gc2.gridx = 6;
      gc2.gridy = 8;
      gc2.gridwidth = 1;
      advanced_panel.add( Box.createHorizontalStrut( 5 ), gc2 );

      gc2.gridx = 7;
      gc2.gridy = 8;
      gc2.gridwidth = 1;
      advanced_panel.add( fPixelDensityHelpButton, gc2 );

      gc2.gridx = 8;
      gc2.gridy = 8;
      gc2.gridwidth = 1;
      advanced_panel.add( Box.createHorizontalStrut( 5 ), gc2 );

      gc2.gridx = 9;
      gc2.gridy = 8;
      gc2.gridwidth = GridBagConstraints.REMAINDER;
      advanced_panel.add( fPixelDensityAdvancedButton, gc2 );

      // reset the anchor
      gc2.gridx = 0;
      gc2.gridy = 9;
      gc2.gridwidth = GridBagConstraints.REMAINDER;
      advanced_panel.add( fAutomaticCheckBox, gc2 );

      gc.gridx = 0;
      gc.gridy = 8;
      gc.gridwidth  = GridBagConstraints.REMAINDER;
      gc.gridheight = GridBagConstraints.REMAINDER;
      target_panel.add( advanced_panel, gc );
    }
    else {
      // just to fill some whitespace
      gc.gridx = 5;
      gc.gridy = 8;
      gc.gridwidth = 1;
      target_panel.add( Box.createRigidArea( fPixelDensityHelpButton.getPreferredSize() ), gc );

      gc.gridx = 6;
      gc.gridy = 8;
      gc.gridwidth = 1;
      target_panel.add( Box.createHorizontalStrut( 5 ), gc );

      gc.gridx = 7;
      gc.gridy = 8;
      gc.gridwidth = 1;
      target_panel.add( Box.createRigidArea( fPixelDensityAdvancedButton.getPreferredSize() ), gc );

      gc.gridx = 6;
      gc.gridy = 9;
      gc.gridwidth = 1;
      target_panel.add( Box.createVerticalStrut( fAutomaticCheckBox.getPreferredSize().height ), gc );
    }

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( target_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Result", panel, TitledPanel.NORTH | TitledPanel.WEST );
  }

  private JPanel buildComputationPanel() {
    JPanel propagation_panel = new JPanel( new GridBagLayout() );
    propagation_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gbc = new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0
    );

    propagation_panel.add( fPropagationFunctionNormal, gbc );
    gbc.gridy++;
    propagation_panel.add( fPropagationFunctionSkyBackground, gbc );
    gbc.gridy++;
    propagation_panel.add( fPropagationFunctionFixedHeight, gbc );
    gbc.gridx++;
    propagation_panel.add( Box.createHorizontalStrut( 5 ) );
    gbc.gridx++;
    propagation_panel.add( fFixedHeightAboveEllipsoidField, gbc );
    gbc.gridx++;
    propagation_panel.add( new JLabel( " m" ), gbc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( propagation_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Computation algorithm", panel );
  }

  private JPanel buildRadarPanel() {
    JPanel radar_panel = new JPanel( new GridBagLayout() );
    radar_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0
    );

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 3;
    radar_panel.add( new JLabel( "Radar Tilt Angle:" ), gc );

    gc.gridx = 3;
    gc.gridy = 0;
    gc.gridwidth = 3;
    fRadarTiltAngleField.setColumns( 6 );
    radar_panel.add( fRadarTiltAngleField, gc );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 3;
    radar_panel.add( new JLabel( "Radar Tilt Azimuth:" ), gc );

    gc.gridx = 3;
    gc.gridy = 1;
    gc.gridwidth = 3;
    fRadarTiltAzimuthField.setColumns( 6 );
    radar_panel.add( fRadarTiltAzimuthField, gc );

    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 3;
    radar_panel.add( new JLabel( "K Factor:" ), gc );

    gc.gridx = 3;
    gc.gridy = 2;
    gc.gridwidth = 3;
    fKFactorField.setColumns( 6 );
    radar_panel.add( fKFactorField, gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( radar_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Radar", panel );
  }

  private void switchLegend() {
    fLegendPanel.removeAll();
    fLegendPanel.add( buildLegend(), BorderLayout.CENTER );
    fLegendPanel.invalidate();
    fLegendPanel.revalidate();
    repaint();
  }

  private JPanel buildLegend() {
    return fPropagationFunctionFixedHeight.isSelected() ? buildLegendFixedHeight() : buildLegendDefault();
  }

  private JPanel buildLegendDefault() {
    double[] losLevels = LOSPainter.getLosLevelsAll();
    String[] labels = new String[losLevels.length];
    for ( int label_index = 0; label_index < losLevels.length ; label_index++ ) {
      double colorLevel = losLevels[ label_index ];
      labels[ label_index ] = colorLevel != ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE
              ? "\u2264 " + Integer.toString( (int) colorLevel ) + " m"
              : "Unknown";
    }
    return new Legend( LOSPainter.getLosColorsAll(), labels, true );
  }

  private JPanel buildLegendFixedHeight() {
    return new Legend( LOSPainter.getLosColorsFixedHeight(), LOSPainter.LOS_LABELS_FixedHeight, true, true );
  }

  private void switchMode() {
    fAdvancedMode = !fAdvancedMode;
    updateEnabledComponents();

    if ( fAdvancedMode ) {
      fAdvancedSimpleButton.setText( "<<< Simple" );
    }
    else {
      fAdvancedSimpleButton.setText( "Advanced >>>" );
    }

    fTargetHostPanel.removeAll();
    fTargetHostPanel.add( fLegendPanel, BorderLayout.CENTER );
    fTargetHostPanel.add( buildTargetPanel( fAdvancedMode ), BorderLayout.EAST );
    repaint();
  }

  // this computes the pixel density directly.
  private double computePixelDensityDirect() {
    Vector referenced_rasters = findRasters();
    if ( referenced_rasters.size() == 0 ) {
      return 0;
    }
    RasterLayerRasterProvider.ReferencedObject ro = (RasterLayerRasterProvider.ReferencedObject) referenced_rasters.get( 0 );
    Object referenced_object = ro.getObject();
    ILcdRaster raster;
    if ( referenced_object instanceof ILcdRaster ) {
      raster = (ILcdRaster) referenced_object;
    } else {
      raster = ( (ILcdMultilevelRaster) referenced_object ).getRaster( 1 );
    }

    ILcdModelReference  center_reference = fCenterPointLayer.getModel().getModelReference();
    ILcd2DEditablePoint aCenter          = center_reference.makeModelPoint().cloneAs2DEditablePoint();
    aCenter.move2D( retrieveCenterX(), retrieveCenterY() );

    ILcdGeoReference aCenterReference = (ILcdGeoReference) center_reference;
    ILcdGeoReference aTargetReference = (ILcdGeoReference) retrieveTargetLayer().getModel().getModelReference();

    // for DTED this amounts to level 0.
    ILcdGeoReference raster_reference = ro.getGeoReference();
    return fRasterPixelDensityProvider.retrievePixelDensity(
            aCenter,
            aCenterReference,
            raster,
            raster_reference,
            aTargetReference
    );
  }

  private JPanel buildPixelDensityPanel() {
    JPanel pixel_density_panel_out = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

    JPanel pixel_density_panel = new JPanel( new GridBagLayout() );

    GridBagConstraints gc = new GridBagConstraints();
    gc.ipadx = 2;

    JLabel explanation_label = new JLabel( "Four steps to find a suitable sample density value:" );
    JLabel find_rasters_label = new JLabel( " 1. Find all terrain elevation rasters beneath the center point." );
    JButton find_rasters_button = new JButton( "Find" );
    JLabel pick_raster_label = new JLabel( " 2. Select a terrain elevation raster." );
    fStepLevelSelectionLabel = new JLabel( " 3. Select a level from the multilevel raster ." );
    JLabel compute_density_label = new JLabel( " 4. Convert the density of the raster to the target layer." );
    JButton compute_density_button = new JButton( "Convert" );
    JLabel result_label = new JLabel( "Density:" );
    fComputedPixelDensityTextField = new JTextField( 6 );

    gc.anchor = GridBagConstraints.WEST;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 4;
    pixel_density_panel.add( explanation_label, gc );

    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 4;
    pixel_density_panel.add( find_rasters_label, gc );

    find_rasters_button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        Vector referenced_rasters = findRasters();
        fRasterTableModel.clear();
        for ( int index = 0; index < referenced_rasters.size() ; index++ ) {
          RasterLayerRasterProvider.ReferencedObject ro = (RasterLayerRasterProvider.ReferencedObject) referenced_rasters.get( index );
          fRasterTableModel.addReferencedObject( ro );
        }
        if ( referenced_rasters.size() == 1 ) {
          fRasterTable.getSelectionModel().setSelectionInterval( 0, 0 );
        }
      }
    } );
    gc.anchor = GridBagConstraints.EAST;
    gc.gridx = 5;
    gc.gridy = 1;
    gc.gridwidth = 2;
    pixel_density_panel.add( find_rasters_button, gc );

    gc.anchor = GridBagConstraints.WEST;
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 4;
    pixel_density_panel.add( pick_raster_label, gc );

    fRasterTable = new JTable();
    fRasterTable.setModel( fRasterTableModel );
    ListSelectionModel list_selection_model = fRasterTable.getSelectionModel();
    fRasterTable.setRowSelectionAllowed( true );
    fRasterTable.setColumnSelectionAllowed( false );
    fRasterTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    list_selection_model.addListSelectionListener( new ListSelectionListener() {
      public void valueChanged( ListSelectionEvent e ) {
        fSelectedRow = e.getFirstIndex();
        int max_level = fRasterTableModel.getMaxLevelReferencedObject( fSelectedRow );
        adaptToRasterMaxLevel( max_level );
      }
    } );
    fRasterTable.setPreferredScrollableViewportSize( new Dimension( 300, 50 ) );
    gc.anchor = GridBagConstraints.CENTER;
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 4;
    JScrollPane scroll_pane = new JScrollPane( fRasterTable );
    pixel_density_panel.add( scroll_pane, gc );

    gc.anchor = GridBagConstraints.WEST;
    gc.gridx = 0;
    gc.gridy = 4;
    gc.gridwidth = 4;
    pixel_density_panel.add( fStepLevelSelectionLabel, gc );

    fMultilevelComboBox = new JComboBox();
    fMultilevelComboBox.setToolTipText( "Choose the raster level: 0:DMED, 1:DTED level 0, ... " );
    gc.gridx = 4;
    gc.gridy = 4;
    gc.gridwidth = 1;
    pixel_density_panel.add( fMultilevelComboBox, gc );

    gc.anchor = GridBagConstraints.WEST;
    gc.gridx = 0;
    gc.gridy = 5;
    gc.gridwidth = 4;
    pixel_density_panel.add( compute_density_label, gc );

    compute_density_button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        double target_density = computePixelDensity();
        fComputedPixelDensityTextField.setText( fNumberFormat1.format( target_density ) );
      }
    } );
    gc.anchor = GridBagConstraints.EAST;
    gc.gridx = 5;
    gc.gridy = 5;
    gc.gridwidth = 2;
    pixel_density_panel.add( compute_density_button, gc );

    gc.anchor = GridBagConstraints.WEST;
    gc.gridx = 0;
    gc.gridy = 6;
    gc.gridwidth = 2;
    pixel_density_panel.add( result_label, gc );

    gc.gridx = 2;
    gc.gridy = 6;
    gc.gridwidth = 2;
    pixel_density_panel.add( fComputedPixelDensityTextField, gc );

    pixel_density_panel_out.add( pixel_density_panel );

    return pixel_density_panel_out;
  }

  private void updateEnabledComponents() {
    boolean enable = !retrieveAutomaticSteps();
    fAngleStepLabel     .setEnabled( enable );
    fAngleStepsizeField .setEnabled( enable );
    fRadiusStepLabel    .setEnabled( enable );
    fRadiusStepsizeField.setEnabled( enable );
    fRadiusStepUnitLabel.setEnabled( enable );
  }

  private double computePixelDensity() {
    ILcd2DEditablePoint density_point = fCenterPointLayer.getModel().getModelReference().makeModelPoint().cloneAs2DEditablePoint();
    density_point.move2D( retrieveCenterX(), retrieveCenterY() );
    int level = -1;
    if ( fMultilevelComboBox.getSelectedItem() != null ) {
      level = ( (Integer) fMultilevelComboBox.getSelectedItem() ).intValue();
    }
    ILcdRaster raster = fRasterTableModel.getRaster( fSelectedRow, level );
    ILcdGeoReference raster_reference = fRasterTableModel.getGeoReference( fSelectedRow );
    return fRasterPixelDensityProvider.retrievePixelDensity(
            density_point,
            (ILcdGeoReference) fCenterPointLayer.getModel().getModelReference(),
            raster,
            raster_reference,
            (ILcdGeoReference) retrieveTargetLayer().getModel().getModelReference()
    );
  }

  private void adaptToRasterMaxLevel( int aLevelCount ) {
    fMultilevelComboBox.removeAllItems();
    fStepLevelSelectionLabel.setEnabled( ( aLevelCount != 0 ) );
    fMultilevelComboBox.setEnabled( ( aLevelCount != 0 ) );
    for ( int index = 0; index < aLevelCount ; index++ ) {
      fMultilevelComboBox.addItem( new Integer( index ) );
    }
  }

  private ILcdPoint getCenterPoint() {
    return fCenterPoint;
  }

  private ILcdGeoReference retrievePointReference() {
    if ( fCenterPointLayer == null ) {
      throw new IllegalArgumentException( "No center point was chosen yet." );
    }
    return (ILcdGeoReference) fCenterPointLayer.getModel().getModelReference();
  }

  private boolean retrieveAutomaticSteps() {
    return fAutomaticSteps && fAdvancedMode;
  }

  private double retrieveStartAngle() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fStartAngleField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for start angle: [" + fStartAngleField.getText() + "]" );
    }
  }

  public double retrieveRadarTiltAngle() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fRadarTiltAngleField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for radar tilt angle: [" + fRadarTiltAngleField.getText() + "]" );
    }
  }

  public double retrieveRadarTiltAzimuth() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fRadarTiltAzimuthField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for radar tilt azimuth: [" + fRadarTiltAzimuthField.getText() + "]" );
    }
  }

  public double retrieveKFactor() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fKFactorField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for K factor: [" + fKFactorField.getText() + "]" );
    }
  }

  private double retrieveArcAngle() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fArcAngleField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for arc angle: [" + fArcAngleField.getText() + "]" );
    }
  }

  private double retrieveAngleStepSize() throws IllegalArgumentException {
    try {
      return ( ( Number ) fNumberFormat2.parseObject( fAngleStepsizeField.getText() ) ).doubleValue();
    }
    catch ( ParseException e ) {
      throw new IllegalArgumentException( "Invalid value for angle step size: [" + fAngleStepsizeField.getText() + "]" );
    }
  }

  private double retrieveMaxRadius() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fMaxRadiusField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for maximum radius: [" + fMaxRadiusField.getText() + "]" );
    }
  }

  private double retrieveRadiusStepSize() throws IllegalArgumentException {
    try {
      return ( ( Number ) fNumberFormat2.parseObject( fRadiusStepsizeField.getText() ) ).doubleValue();
    }
    catch ( ParseException e ) {
      throw new IllegalArgumentException( "Invalid value for radius step size: [" + fRadiusStepsizeField.getText() + "]" );
    }
  }

  private double retrieveHeightAboveGround() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fHeightAboveGroundField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for height above ground: [" + fHeightAboveGroundField.getText() + "]" );
    }
  }

  private double retrieveCenterX() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fCenterXField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for center point X coordinate: [" + fCenterXField.getText() + "]" );
    }
  }

  private double retrieveCenterY() throws IllegalArgumentException {
    try {
      return Double.parseDouble( fCenterYField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for center point Y coordinate: [" + fCenterYField.getText() + "]" );
    }
  }

  private void setCenterPoint( ILcdPoint aCenterPoint ) {
    fCenterPoint = aCenterPoint;
    fCenterXField.setText( Double.toString( aCenterPoint.getX() ) );
    fCenterYField.setText( Double.toString( aCenterPoint.getY() ) );
  }

  private void setCenterPointLayer( ILcdGXYLayer aCenterPointLayer ) {
    if ( aCenterPointLayer != fCenterPointLayer ) {
      if ( fCenterPointLayer != null ) {
        fCenterPointLayer.getModel().removeModelListener( fCenterPointFollower );
      }
      fCenterPointLayer = aCenterPointLayer;
      fCenterPointLayer.getModel().addModelListener( fCenterPointFollower );
      fCenterLayerLabel.setText( fCenterPointLayer.getLabel() );
      ILcdModelReference reference = fCenterPointLayer.getModel().getModelReference();
      String reference_text;
      if ( reference instanceof ILcdGeodeticReference ) {
        ILcdGeodeticReference geodetic_reference = (ILcdGeodeticReference) reference;
        reference_text = geodetic_reference.getGeodeticDatum().getName();
      } else {
        reference_text = reference.toString();
      }
      if ( reference_text.length() > 25 ) {
        fCenterReferenceLabel.setText( reference_text.substring( 0, 25 ) + "..." );
        fCenterReferenceLabel.setToolTipText( reference_text );
      } else {
        fCenterReferenceLabel.setText( reference_text );
        fCenterReferenceLabel.setToolTipText( null );
      }
    }
  }

  private Vector findRasters() throws IllegalArgumentException {
    if ( fCenterPoint == null ) {
      throw new IllegalArgumentException( "Please select a center point first." );
    }

    ILcd2DEditablePoint center_point = fCenterPointLayer.getModel().getModelReference().makeModelPoint().cloneAs2DEditablePoint();
    center_point.move2D( retrieveCenterX(), retrieveCenterY() );
    return fRasterLayerRasterProvider.retrieveRasters( center_point, (ILcdGeoReference) fCenterPointLayer.getModel().getModelReference() );
  }

  private void findPixelDensity() throws IllegalArgumentException {
    if ( fCenterPoint == null ) {
      throw new IllegalArgumentException( "Please select a center point first." );
    }

    if ( fPixelDensityDialog == null ) {
      buildPixelDensityDialog();
    }
    if ( !fPixelDensityDialog.isVisible() ) {
      fPixelDensityDialog.setVisible( true );
    }
    fPixelDensityDialog.toFront();
  }

  private void buildPixelDensityDialog() {
    JButton close_button = new JButton( "Close" );
    close_button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fPixelDensityDialog.setVisible( false );
      }
    } );
    JButton apply_button = new JButton( "Use value" );
    apply_button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fPixelDensityTextField.setText( fComputedPixelDensityTextField.getText() );
      }
    } );

    JPanel button_panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    button_panel.add( apply_button );
    button_panel.add( close_button );

    fPixelDensityDialog = new JDialog();
    fPixelDensityDialog.setTitle( "Sample density calculator" );
    fPixelDensityDialog.getContentPane().setLayout( new BorderLayout() );
    fPixelDensityDialog.getContentPane().add( buildPixelDensityPanel(), BorderLayout.CENTER );
    fPixelDensityDialog.getContentPane().add( button_panel, BorderLayout.SOUTH );
    fPixelDensityDialog.pack();
  }

  private void updateLOSCoverage() {
    try {
      createLOSCoverage();
    }
    catch ( IllegalArgumentException exception ) {
      // exception occurred, cannot compute P2P coverage.
    }
  }

  private ILcdLOSCoverage createLOSCoverage( ILcdPoint                aCenterPoint,
                                             ILcdGeoReference         aCenterPointReference,
                                             double                   aCenterPointAltitude,
                                             TLcdCoverageAltitudeMode aCenterPointAltitudeMode,
                                             double                   aRadiusMax,
                                             double                   aRadiusStep,
                                             double                   aAngleStart,
                                             double                   aAngleArc,
                                             double                   aAngleStep,
                                             boolean                  aAutomaticSteps,
                                             ILcdGeoReference         aTargetReference,
                                             double                   aSampleDensity,
                                             double                   aRadialFraction ) {
    if ( aAutomaticSteps ) {
      return new TLcdLOSCoverage(
              aCenterPoint,
              aCenterPointReference,
              aCenterPointAltitude,
              aCenterPointAltitudeMode,
              aRadiusMax,
              aAngleStart,
              aAngleArc,
              aTargetReference,
              aSampleDensity,
              aRadialFraction
      );
    }
    return new TLcdLOSCoverage(
            aCenterPoint,
            aCenterPointReference,
            aCenterPointAltitude,
            aCenterPointAltitudeMode,
            aRadiusMax,
            aRadiusStep,
            aAngleStart,
            aAngleArc,
            aAngleStep
    );
  }

  private class CenterPointSelectionListener implements ILcdSelectionListener {

    public void selectionChanged( TLcdSelectionChangedEvent event ) {
      ILcdSelection selection = event.getSelection();
      Enumeration selected_objects = selection.selectedObjects();
      ILcdPoint    center_point      = null;
      ILcdGXYLayer center_point_layer = null;
      while ( center_point == null && selected_objects.hasMoreElements() ) {
        Object selected_object = selected_objects.nextElement();
        if ( selected_object instanceof ILcdPoint ) {
          center_point = (ILcdPoint) selected_object;
          center_point_layer = (ILcdGXYLayer) event.getSource();
        }
      }

      if ( center_point != null ) {
        setCenterPointLayer( center_point_layer );
        setCenterPoint     ( center_point       );
        updateLOSCoverage();
      }
    }
  }

  private class CenterPointFollower implements ILcdModelListener {

    public void modelChanged( TLcdModelChangedEvent event ) {
      if ( event.containsElement( fCenterPoint ) ) {
        fCenterXField.setText( Double.toString( fCenterPoint.getX() ) );
        fCenterYField.setText( Double.toString( fCenterPoint.getY() ) );
        fCenterXField.setCaretPosition( 0 );
        fCenterYField.setCaretPosition( 0 );
        updateLOSCoverage();
      }
    }
  }

  private class RasterTableModel extends AbstractTableModel {

    private Vector fReferencedObjects = new Vector();

    public void addReferencedObject( RasterLayerRasterProvider.ReferencedObject aReferencedObject ) {
      fReferencedObjects.add( aReferencedObject );
      super.fireTableRowsInserted( fReferencedObjects.size() - 2, fReferencedObjects.size() - 1 );
    }

    /**
     * Returns the level count for a multilevel raster. 0 for ILcdRaster objects.
     * @param aIndex the index of the referenced object.
     * @return the level count for a multilevel raster.
     */
    public int getMaxLevelReferencedObject( int aIndex ) {
      if ( aIndex < 0 || aIndex >= fReferencedObjects.size() ) {
        return 0;
      }
      Object object = ( (RasterLayerRasterProvider.ReferencedObject) fReferencedObjects.get( aIndex ) ).getObject();
      if ( object instanceof ILcdMultilevelRaster ) {
        return ( (ILcdMultilevelRaster) object ).getRasterCount();
      } else {
        return 0;
      }
    }

    public ILcdRaster getRaster( int aRowIndex, int aLevelIndex ) {
      if ( aRowIndex < 0 || aRowIndex >= fReferencedObjects.size() ) {
        return null;
      }
      Object object = ( (RasterLayerRasterProvider.ReferencedObject) fReferencedObjects.get( aRowIndex ) ).getObject();
      if ( object instanceof ILcdMultilevelRaster ) {
        return ( (ILcdMultilevelRaster) object ).getRaster( aLevelIndex );
      } else {
        return (ILcdRaster) object;
      }
    }

    public ILcdGeoReference getGeoReference( int aRowIndex ) {
      if ( aRowIndex < 0 || aRowIndex >= fReferencedObjects.size() ) {
        return null;
      }
      return ( (RasterLayerRasterProvider.ReferencedObject) fReferencedObjects.get( aRowIndex ) ).getGeoReference();
    }

    public void clear() {
      int size = fReferencedObjects.size();
      if ( size != 0 ) {
        fReferencedObjects.removeAllElements();
        super.fireTableRowsDeleted( 0, size - 1 );
      }
    }

    public int getRowCount() {
      return fReferencedObjects.size();
    }

    public int getColumnCount() {
      return 3;
    }

    public String getColumnName( int column ) {
      switch ( column ) {
        case 0:
          return "Layer";
        case 1:
          return "Type";
        case 2:
          return "Multilevel";
      }
      return super.getColumnName( column );
    }

    public Object getValueAt( int rowIndex, int columnIndex ) {
      RasterLayerRasterProvider.ReferencedObject referenced_object = (RasterLayerRasterProvider.ReferencedObject) fReferencedObjects.get( rowIndex );
      switch ( columnIndex ) {
        case 0:
          return referenced_object.getLayerName();
        case 1:
          return referenced_object.getType();
        default:
          if ( referenced_object.getObject() instanceof ILcdMultilevelRaster ) {
            return Boolean.TRUE;
          } else {
            return Boolean.FALSE;
          }
      }
    }
  }

}
