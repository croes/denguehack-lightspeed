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

import samples.common.SwingUtil;
import com.luciad.model.*;
import com.luciad.reference.*;
import com.luciad.shape.ILcdPoint;
import com.luciad.tea.*;
import com.luciad.util.*;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.gxy.common.TitledPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

/**
 * A panel to choose input points for point-to-point intervisibility computations.
 */
class P2PPanel extends JPanel {

  public static  final int PROPAGATION_FUNCTION_UNKNOWN        = -1;
  public static  final int PROPAGATION_FUNCTION                =  0;
  public static  final int PROPAGATION_FUNCTION_SKY_BACKGROUND =  1;
  private static final Dimension PREFERRED_LABEL_SIZE = new Dimension( 100, 15 );

  private ILcdPoint[]    fPoints      = new ILcdPoint[2];

  private ILcdGXYLayer[] fPointLayers = new ILcdGXYLayer[2];
  private JLabel[] fPointXLabels = new JLabel[2];
  private JLabel[] fPointYLabels = new JLabel[2];
  private JLabel[] fPointLayerLabels = new JLabel[2];
  private JLabel[] fPointReferenceLabels = new JLabel[2];

  private JTextField[] fPointHeightAboveGroundFields = new JTextField[2];
  // selection listener to select the points
  private ILcdSelectionListener fSelectionListener = new PointSelectionListener();

  // model listener to keep track of the selected points
  private ILcdModelListener fModelListener = new PointMovedListener();
  // target layers
  private JComboBox fTargetLayerComboBox = new JComboBox();
  private JTextField fStepInMetersField = new JTextField( "250", 5 );

  private TLcdLonLatFormatter fLonLatFormatter = new TLcdLonLatFormatter( TLcdLonLatFormatter.DEC_DEG_2 );

  P2PPanel() {
    initialize();
    setLayout( new BorderLayout() );
    add( buildPointsPanel(), BorderLayout.CENTER );
    add( buildTargetPanel(), BorderLayout.SOUTH  );
  }

  void setCenterLayers( ILcdGXYLayer[] aCenterLayers ) {
    for ( int point_layer_index = 0; point_layer_index < aCenterLayers.length ; point_layer_index++ ) {
      ILcdGXYLayer pointLayer = aCenterLayers[ point_layer_index ];
      pointLayer.addSelectionListener( fSelectionListener );
      pointLayer.getModel().addModelListener( fModelListener );
    }
  }

  void setTargetLayers( ILcdGXYLayer[] aTargetLayers ) {
    for ( int target_layer_index = 0; target_layer_index < aTargetLayers.length ; target_layer_index++ ) {
      fTargetLayerComboBox.addItem( aTargetLayers[ target_layer_index ] );
    }
  }

  ILcdP2PCoverage createP2PCoverage() {
    ILcdPoint                aStartPoint             = getPoint( 0 );
    ILcdGeoReference         aStartPointReference    = getPointGeoReference( 0 );
    double                   aStartPointAltitude     = retrievePointHeightAboveGround( 0 );
    TLcdCoverageAltitudeMode aStartPointAltitudeMode = TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL;

    ILcdPoint                aEndPoint             = getPoint( 1 );
    ILcdGeoReference         aEndPointReference    = getPointGeoReference( 1 );
    double                   aEndPointAltitude     = retrievePointHeightAboveGround( 1 );
    TLcdCoverageAltitudeMode aEndPointAltitudeMode = TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL;

    double aStepSize = retrieveStepInMeters();

    ILcdP2PCoverage p2p_coverage = createP2PCoverage(
            aStartPoint,
            aStartPointReference,
            aStartPointAltitude,
            aStartPointAltitudeMode,
            aEndPoint,
            aEndPointReference,
            aEndPointAltitude,
            aEndPointAltitudeMode,
            aStepSize
    );

    return p2p_coverage;
  }

  ILcdGXYLayer retrieveTargetLayer() {
    return (ILcdGXYLayer) fTargetLayerComboBox.getSelectedItem();
  }

  private ILcdPoint getPoint( int aIndex ) {
    return fPoints[ aIndex ];
  }

  private ILcdGeoReference getPointGeoReference( int aIndex ) {
    if ( fPointLayers[ aIndex ] == null ) {
      throw new IllegalArgumentException( "Please select two points on the map to compute intervisibility." );
    }
    return (ILcdGeoReference) fPointLayers[ aIndex ].getModel().getModelReference();
  }

  private double retrievePointHeightAboveGround( int aIndex ) throws IllegalArgumentException {
    try {
      return Double.parseDouble( fPointHeightAboveGroundFields[ aIndex ].getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for height above ground for point " + aIndex + ": [" + fPointHeightAboveGroundFields[ aIndex ].getText() + "]" );
    }
  }

  private double retrieveStepInMeters() {
    try {
      return Double.parseDouble( fStepInMetersField.getText() );
    }
    catch ( NumberFormatException e ) {
      throw new IllegalArgumentException( "Invalid value for step (meters): [" + fStepInMetersField.getText() + "]" );
    }
  }

  private void initialize() {
    ActionListener action_listener = new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        updateP2PCoverage();
      }
    };

    FocusListener focus_listener = new FocusListener() {
      public void focusGained( FocusEvent e ) {
      }

      public void focusLost( FocusEvent e ) {
        updateStepSizeLimit();
        updateP2PCoverage();
      }
    };

    for ( int point_index = 0; point_index < 2 ; point_index++ ) {
      fPointXLabels[ point_index ] = new JLabel() {
        @Override
        public Dimension getPreferredSize() {
          return PREFERRED_LABEL_SIZE;
        }
      };
      fPointYLabels[ point_index ] = new JLabel() {
        @Override
        public Dimension getPreferredSize() {
          return PREFERRED_LABEL_SIZE;
        }
      };
      fPointLayerLabels[ point_index ] = new JLabel();
      fPointReferenceLabels[ point_index ] = new JLabel();

      JTextField text_field = new JTextField( "100", 6 );
      text_field.addActionListener( action_listener );
      text_field.addFocusListener ( focus_listener  );
      fPointHeightAboveGroundFields[ point_index ] = text_field;
    }

    fTargetLayerComboBox.addActionListener( action_listener );
    fStepInMetersField  .addActionListener( action_listener );
    fStepInMetersField  .addFocusListener ( focus_listener  );
  }

  private void updateStepSizeLimit() {
    double stepInMeters = retrieveStepInMeters();
    if(stepInMeters>250){
      fStepInMetersField.setText("250");
    }
    if(stepInMeters<10){
      fStepInMetersField.setText("10");
    }
  }

  private JPanel buildPointsPanel() {
    JPanel points_panel = new JPanel( new GridLayout( 2, 1 ) );
    points_panel.add( buildPointPanel( 0 ) );
    points_panel.add( buildPointPanel( 1 ) );
    return points_panel;
  }

  private JPanel buildPointPanel( int aIndex ) {
    JPanel point_panel = new JPanel( new GridBagLayout() );
    point_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;

    // line 1
    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 1;
    point_panel.add( new JLabel( "X:" ), gc );

    gc.gridx = 1;
    gc.gridy = 0;
    gc.gridwidth = 3;
    point_panel.add( fPointXLabels[ aIndex ], gc );

    gc.gridx = 4;
    gc.gridy = 0;
    gc.gridwidth = 1;
    point_panel.add( new JLabel( "Y:" ), gc );

    gc.gridx = 5;
    gc.gridy = 0;
    gc.gridwidth = 3;
    point_panel.add( fPointYLabels[ aIndex ], gc );

    // line 2
    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 2;
    point_panel.add( new JLabel( "Layer: " ), gc );

    gc.gridx = 2;
    gc.gridy = 1;
    gc.gridwidth = 6;
    point_panel.add( fPointLayerLabels[ aIndex ], gc );

    // line 3
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 3;
    point_panel.add( new JLabel( "Reference: " ), gc );

    gc.gridx = 3;
    gc.gridy = 2;
    gc.gridwidth = 6;
    point_panel.add( fPointReferenceLabels[ aIndex ], gc );

    // line 4
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 4;
    point_panel.add( new JLabel( "Height above ground: " ), gc );

    gc.gridx = 4;
    gc.gridy = 3;
    gc.gridwidth = 2;
    point_panel.add( fPointHeightAboveGroundFields[ aIndex ], gc );

    gc.gridx = 6;
    gc.gridy = 3;
    gc.gridwidth = 1;
    point_panel.add( new JLabel( "m" ), gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( point_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Point " + ( aIndex + 1 ), panel );
  }

  private JPanel buildTargetPanel() {
    JPanel target_panel = new JPanel( new GridBagLayout() );
    target_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 5, 5 ) );

    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.fill = GridBagConstraints.HORIZONTAL;

    // line 1
    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 5;
    target_panel.add( new JLabel( "Target layer:" ), gc );

    gc.gridx = 6;
    gc.gridy = 0;
    gc.gridwidth = 5;
    target_panel.add( fTargetLayerComboBox, gc );

    // line 2
    gc.gridx = 0;
    gc.gridy = 1;
    gc.gridwidth = 2;
    target_panel.add( new JLabel( "Step:" ), gc );

    gc.gridx = 3;
    gc.gridy = 1;
    gc.gridwidth = 2;
    target_panel.add( fStepInMetersField, gc );

    gc.gridx = 6;
    gc.gridy = 1;
    gc.gridwidth = 1;
    target_panel.add( new JLabel( "m" ), gc );

    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( target_panel, BorderLayout.WEST );
    panel.add( Box.createGlue(), BorderLayout.CENTER );
    return TitledPanel.createTitledPanel( "Target", panel );
  }

  private void setPoint( ILcdPoint aPoint, int aIndex ) {
    fPoints[ aIndex ] = aPoint;
    if ( fPointLayers[ aIndex ].getModel().getModelReference() instanceof ILcdGeodeticReference ) {
      fPointXLabels[ aIndex ].setText( fLonLatFormatter.formatLon( aPoint.getX() ) );
      fPointYLabels[ aIndex ].setText( fLonLatFormatter.formatLat( aPoint.getY() ) );
    } else {
      fPointXLabels[ aIndex ].setText( Double.toString( aPoint.getX() ) );
      fPointYLabels[ aIndex ].setText( Double.toString( aPoint.getY() ) );
    }
  }

  private void setPointLayer( ILcdGXYLayer aLayer, int aIndex ) {
    fPointLayers[ aIndex ] = aLayer;
    fPointLayerLabels[ aIndex ].setText( aLayer.getLabel() );
    fPointReferenceLabels[ aIndex ].setText( aLayer.getModel().getModelReference().toString() );
  }

  private void updateP2PCoverage() {
    try {
      createP2PCoverage();
    }
    catch ( IllegalArgumentException exception ) {
      // exception occurred, cannot compute P2P coverage.
    }
  }

  private ILcdP2PCoverage createP2PCoverage( ILcdPoint                aStartPoint,
                                             ILcdGeoReference         aStartPointReference,
                                             double                   aStartPointAltitude,
                                             TLcdCoverageAltitudeMode aStartPointAltitudeMode,
                                             ILcdPoint                aEndPoint,
                                             ILcdGeoReference         aEndPointReference,
                                             double                   aEndPointAltitude,
                                             TLcdCoverageAltitudeMode aEndPointAltitudeMode,
                                             double                   aStepSize ) {
    return new TLcdP2PCoverage(
            aStartPoint,
            aStartPointReference,
            aStartPointAltitude,
            aStartPointAltitudeMode,
            aEndPoint,
            aEndPointReference,
            aEndPointAltitude,
            aEndPointAltitudeMode,
            aStepSize
    );
  }

  // to choose the points from the layers.
  private class PointSelectionListener implements ILcdSelectionListener {

    private int fIndexPointToSelect = 0;

    public void selectionChanged( TLcdSelectionChangedEvent event ) {
      int index_old_point_to_select = fIndexPointToSelect;
      Enumeration selected_objects = event.elements();
      ILcdGXYLayer selection_layer = (ILcdGXYLayer) event.getSource();
      while ( selected_objects.hasMoreElements() ) {
        Object selected_object = selected_objects.nextElement();
        // we are only interested in points
        if ( selected_object instanceof ILcdPoint ) {
          ILcdPoint selected_point = (ILcdPoint) selected_object;
          // did we select a point that is different from the 'other' point ?
          if ( !selected_point.equals( fPoints[ 1 - fIndexPointToSelect ] ) ) {
            // first set the layer so that we use its model reference to format the
            // coordinates
            setPointLayer( selection_layer, fIndexPointToSelect );
            setPoint     ( selected_point,  fIndexPointToSelect );
            updateP2PCoverage();
            fIndexPointToSelect = 1 - fIndexPointToSelect;
            // we don't want to take more than 2 points from the selection.
            if ( fIndexPointToSelect == index_old_point_to_select ) {
              break;
            }
          }
        }
      }
    }
  }

  // to keep track of moving points.
  private class PointMovedListener implements ILcdModelListener {

    public void modelChanged( TLcdModelChangedEvent event ) {
      for ( int point_index = 0; point_index < fPoints.length ; point_index++ ) {
        ILcdPoint point = fPoints[ point_index ];
        if ( point != null && event.containsElement( point ) ) {
          setPoint( point, point_index );
          updateP2PCoverage();
        }
      }
    }
  }

}
