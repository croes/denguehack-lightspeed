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
package samples.tea.lightspeed.los.gui;

import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import samples.tea.lightspeed.los.model.LOSCoverageInputShape;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

/**
 * <p>
 *   An extension of JPanel that can read and modify the properties of a LOS
 *  Coverage Input Shape that aren't part of its direct shape.
 * </p>
 * <p>
 *   This panel is used in the sample to modify various parameters of the LOS
 *   calculation, such as:
 *   <ul>
 *     <li>Angle Step (in degrees)</li>
 *     <li>Radius Step (in metres)</li>
 *     <li>Altitude from ground (in metres)</li>
 *   </ul>
 *   The panel itself contains 3 sliders that are used to modify the <code>LOSCoverageInputShape</code>
 *   set by the {@link #setActiveCoverageInputShape(ILcdModel, samples.tea.lightspeed.los.model.LOSCoverageInputShape)
 *   setActiveCoverageInputShape} method.
 * </p>
 */
public class LOSParametersPanel extends JPanel {

  private static final String RADIUS_STEP           = "radiusStep";
  private static final String CENTER_POINT_ALTITUDE = "centerPointAltitude";
  private static final String ANGLE_STEP            = "angleStep";
  private static final String MIN_VERTICAL_ANGLE    = "minVerticalAngle";
  private static final String MAX_VERTICAL_ANGLE    = "maxVerticalAngle";
  private static final String degree = "" + (char)176;

  private JLabel         fRadiusStepLabel                   = new JLabel( "Radius Step [100m, 1000m]:"     );
  private JLabel         fCenterPointAltitudeLabel          = new JLabel( "Center Altitude [1m, 1000m]:" );
  private JLabel         fAngleStepLabel                    = new JLabel( "Angle Step [1"+degree+", 10"+degree+"]:"      );
  private JLabel         fMinVerticalAngleLabel             = new JLabel( "Minimum angle [0"+degree+", 180"+degree+"]:"      );
  private JLabel         fMaxVerticalAngleLabel             = new JLabel( "Maximum angle [0"+degree+", 180"+degree+"]:"      );
  private JSlider        fRadiusStepSlider                  = new JSlider( JSlider.HORIZONTAL, 100, 1000,200 );
  private JSlider        fCenterPointAltitudeSlider         = new JSlider( JSlider.HORIZONTAL, 1, 1000, 10 );
  private JSlider        fMinVerticalAngleSlider            = new JSlider( JSlider.HORIZONTAL, 0,180,0 );
  private JSlider        fMaxVerticalAngleSlider            = new JSlider( JSlider.HORIZONTAL, 0,180,180 );
  private JSlider        fAngleStepSlider                   = new JSlider( JSlider.HORIZONTAL, 1,10,3 );
  private ChangeListener fRadiusStepSliderListener          = new MyChangeListener( RADIUS_STEP           );
  private ChangeListener fCenterPointAltitudeSliderListener = new MyChangeListener( CENTER_POINT_ALTITUDE );
  private ChangeListener fAngleStepSliderListener           = new MyChangeListener( ANGLE_STEP            );
  private ChangeListener fMinVerticalAngleSliderListener           = new MyChangeListener( MIN_VERTICAL_ANGLE            );
  private ChangeListener fMaxVerticalAngleSliderListener           = new MyChangeListener( MAX_VERTICAL_ANGLE );
  private Box            fFillerBox1                        = new Box( BoxLayout.Y_AXIS );
  private Box            fFillerBox2                        = new Box( BoxLayout.X_AXIS );
  private Box            fFillerBox3                        = new Box( BoxLayout.X_AXIS );

  private LOSCoverageInputShape fActiveCoverageInputShape;
  private ILcdModel             fLOSInputModel;


  public LOSParametersPanel( LayoutManager layout, boolean isDoubleBuffered ) {
    super( layout, isDoubleBuffered );
    initPanel();
  }

  public LOSParametersPanel( LayoutManager layout ) {
    super( layout );
    initPanel();
  }

  public LOSParametersPanel( boolean isDoubleBuffered ) {
    super( isDoubleBuffered );
    initPanel();
  }

  public LOSParametersPanel() {
    initPanel();
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    super.setEnabled( aEnabled );
    fAngleStepSlider.setEnabled( aEnabled );
    fMaxVerticalAngleSlider.setEnabled( aEnabled );
    fMinVerticalAngleSlider.setEnabled( aEnabled );
    fCenterPointAltitudeSlider.setEnabled( aEnabled );
    fRadiusStepSlider.setEnabled( aEnabled );
    fAngleStepLabel.setEnabled( aEnabled );
    fRadiusStepLabel.setEnabled( aEnabled );
    fCenterPointAltitudeLabel.setEnabled( aEnabled );
    fMinVerticalAngleLabel.setEnabled( aEnabled );
    fMaxVerticalAngleLabel.setEnabled( aEnabled );
  }

  private void initPanel() {
    String angleStepToolTipText = "Expresses the angular discretization of the LOS coverage. "
                                  + "A lower value increases the amount of angular samples for the calculation.";
    String radiusStepToolTipText = "Expresses the radial discretization of the LOS coverage. "
                                   + "A lower value increases the amount of radial samples for the calculation.";
    String altitudeTooltipText = "The altitude of the observer, expressed in meters.";
    String minTooltipText = "The minimum viewing angle of the observer. "
                            + "0 degrees is straight down, 180 degrees is straight up.";
    String maxTooltipText = "The maximum viewing angle of the observer. "
                            + "0 degrees is straight down, 180 degrees is straight up.";

    fAngleStepLabel.setToolTipText(angleStepToolTipText);
    fRadiusStepLabel.setToolTipText(radiusStepToolTipText);
    fCenterPointAltitudeLabel.setToolTipText(altitudeTooltipText);
    fMinVerticalAngleLabel.setToolTipText(minTooltipText);
    fMaxVerticalAngleLabel.setToolTipText(maxTooltipText);

    fAngleStepSlider.setToolTipText(angleStepToolTipText);
    fRadiusStepSlider.setToolTipText(radiusStepToolTipText);
    fCenterPointAltitudeSlider.setToolTipText(altitudeTooltipText);
    fMinVerticalAngleSlider.setToolTipText(minTooltipText);
    fMaxVerticalAngleSlider.setToolTipText(maxTooltipText);

    fAngleStepLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    fRadiusStepLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    fCenterPointAltitudeLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    fMinVerticalAngleLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    fMaxVerticalAngleLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    GridBagLayout gridBagLayout = new GridBagLayout();
    setLayout( gridBagLayout );
    GridBagConstraints gridBagConstraints = new GridBagConstraints();

    fAngleStepSlider.setOpaque( false );
    fRadiusStepSlider.setOpaque( false );
    fCenterPointAltitudeSlider.setOpaque( false );
    fMinVerticalAngleSlider.setOpaque( false );
    fMaxVerticalAngleSlider.setOpaque( false );

    addComponent( fAngleStepLabel, 1, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fRadiusStepLabel          , 3, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fCenterPointAltitudeLabel , 5, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fMinVerticalAngleLabel , 7, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fMaxVerticalAngleLabel , 9, 1, 3, 1, gridBagConstraints, gridBagLayout );

    addComponent( fAngleStepSlider          , 1, 5, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fRadiusStepSlider         , 3, 5, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fCenterPointAltitudeSlider, 5, 5, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fMinVerticalAngleSlider, 7, 5, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fMaxVerticalAngleSlider, 9, 5, 6, 1, gridBagConstraints, gridBagLayout );


    addComponent( fFillerBox1, 1, 4, 1 , 5, gridBagConstraints, gridBagLayout );
    addComponent( fFillerBox2, 2, 1, 10, 1, gridBagConstraints, gridBagLayout );
    addComponent( fFillerBox3, 4, 1, 10, 1, gridBagConstraints, gridBagLayout );
  }

  public void addComponent( Component component, int row, int column, int width, int height, GridBagConstraints aGridBagConstraints, GridBagLayout aGridBagLayout ) {
    aGridBagConstraints.gridx = column;
    aGridBagConstraints.gridy = row;
    aGridBagConstraints.gridwidth = width;
    aGridBagConstraints.gridheight = height;
    aGridBagLayout.setConstraints( component, aGridBagConstraints );
    add( component );
  }

  private void updatePanel(){
    fRadiusStepSlider.removeChangeListener( fRadiusStepSliderListener );
    fCenterPointAltitudeSlider.removeChangeListener( fCenterPointAltitudeSliderListener );
    fAngleStepSlider.removeChangeListener( fAngleStepSliderListener );
    fMinVerticalAngleSlider.removeChangeListener( fMinVerticalAngleSliderListener );
    fMaxVerticalAngleSlider.removeChangeListener( fMaxVerticalAngleSliderListener );
    if ( fActiveCoverageInputShape!=null ) {
      fRadiusStepSlider.setValue( ( int ) fActiveCoverageInputShape.getRadiusStep() );
      fCenterPointAltitudeSlider.setValue( ( int ) fActiveCoverageInputShape.getCenterPointHeightOffset() );
      fAngleStepSlider.setValue( ( int ) fActiveCoverageInputShape.getAngleStep() );
      fRadiusStepSlider.addChangeListener( fRadiusStepSliderListener );
      fCenterPointAltitudeSlider.addChangeListener( fCenterPointAltitudeSliderListener );
      fAngleStepSlider.addChangeListener( fAngleStepSliderListener );
      fMinVerticalAngleSlider.addChangeListener( fMinVerticalAngleSliderListener );
      fMaxVerticalAngleSlider.addChangeListener( fMaxVerticalAngleSliderListener );
    }
  }

  public void setActiveCoverageInputShape( ILcdModel aModel, LOSCoverageInputShape aLOSCoverageInputShape ) {
    fLOSInputModel = aModel;
    fActiveCoverageInputShape = aLOSCoverageInputShape;
    updatePanel();
  }

  private class MyChangeListener implements ChangeListener{
    private String fParameter;

    private MyChangeListener( String aParameter ) {
      if(aParameter==null){
        throw new NullPointerException( "Given parameter must not be null." );
      }
      fParameter = aParameter;
    }

    @Override public void stateChanged( ChangeEvent aEvent ) {
      try {
        TLcdLockUtil.writeLock( fLOSInputModel );
        if(fParameter.equals( RADIUS_STEP )){
          int newRadiusStep = fRadiusStepSlider.getValue();
          fActiveCoverageInputShape.setRadiusStep( newRadiusStep );
          fLOSInputModel.elementChanged( fActiveCoverageInputShape, ILcdModel.FIRE_NOW );
        }else if(fParameter.equals( CENTER_POINT_ALTITUDE )){
          int newCenterPointAltitude = fCenterPointAltitudeSlider.getValue();
          fActiveCoverageInputShape.setCenterPointHeightOffset( newCenterPointAltitude );
          fLOSInputModel.elementChanged( fActiveCoverageInputShape, ILcdModel.FIRE_NOW );
        }else if(fParameter.equals( ANGLE_STEP )){
          int newAngleStep = fAngleStepSlider.getValue();
          fActiveCoverageInputShape.setAngleStep( newAngleStep );
          fLOSInputModel.elementChanged( fActiveCoverageInputShape, ILcdModel.FIRE_NOW );
        }else if(fParameter.equals( MIN_VERTICAL_ANGLE )){
          int newMinVerticalAngle = fMinVerticalAngleSlider.getValue();
          fActiveCoverageInputShape.setMinVerticalAngle( newMinVerticalAngle );
          if (newMinVerticalAngle>fMaxVerticalAngleSlider.getValue()) {
            fActiveCoverageInputShape.setMaxVerticalAngle( newMinVerticalAngle );
            fMaxVerticalAngleSlider.setValue( newMinVerticalAngle );
          }
          fLOSInputModel.elementChanged( fActiveCoverageInputShape, ILcdModel.FIRE_NOW );
        }else if(fParameter.equals( MAX_VERTICAL_ANGLE )){
          int newMaxVerticalAngle = fMaxVerticalAngleSlider.getValue();
          fActiveCoverageInputShape.setMaxVerticalAngle( newMaxVerticalAngle );
          if (newMaxVerticalAngle<fMinVerticalAngleSlider.getValue()) {
            fActiveCoverageInputShape.setMinVerticalAngle( newMaxVerticalAngle );
            fMinVerticalAngleSlider.setValue( newMaxVerticalAngle );
          }
          fLOSInputModel.elementChanged( fActiveCoverageInputShape, ILcdModel.FIRE_NOW );
        }else{
          throw new UnsupportedOperationException( "Can't handle parameter: "+fParameter );
        }
      }
      finally {
        TLcdLockUtil.writeUnlock( fLOSInputModel );
      }
    }
  }

}
