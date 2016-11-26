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
package samples.decoder.kml22.common.timetoolbar.advanced;

import com.luciad.realtime.TLcdSimulator;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import samples.decoder.kml22.common.timetoolbar.common.ConversionUtil;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayer;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayerSlider;
import samples.gxy.common.TitledPanel;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Represents a panel that can alter simulation options in the advanced time toolbar.
 */
public class AdvancedTimeToolbarOptionsPanel extends JPanel {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( AdvancedTimeToolbarOptionsPanel.class.getName() );

  private static final long DURATION_AT_MAX_SPEED = 1000;//in milliseconds
  private static final int BUTTON_WIDTH = 16;
  private static final int SLIDER_WIDTH = 11 * BUTTON_WIDTH;
  private static final double MINIMUM_SPEED_UP = 0.1;
  private static final String MAXIMUM_STRING = "10000 days 00:00:00 ";

  private TLcdSimulator fSimulator;
  private int fLabelWidth = 75;
  private int fLabelHeight = 20;
  private DateDifferenceFormat fDateDifferenceFormat = new DateDifferenceFormat();
  private JTextField fSpeedupLabel;
  private JTextField fPlayTimeLabel;
  private MediaPlayer fPlayTimeMediaPlayer;
  private MediaPlayer fSpeedUpMediaPlayer;
  private NumberFormat fSpeedupNumberFormat;
  private PropertyChangeListener fPlayTimeSliderListener;
  private PropertyChangeListener fSpeedupSliderListener;

  /**
   * Creates a new options panel for the advanced time toolbar.
   * @param aSimulator a simulator on which options can be set
   * @param aFontMetrics a <code>FontMetrics</code> instance
   */
  public AdvancedTimeToolbarOptionsPanel( TLcdSimulator aSimulator, FontMetrics aFontMetrics ) {
    fSimulator = aSimulator;
    setLayout( new GridLayout( 2, 1 ) );

    fLabelWidth = aFontMetrics.stringWidth( MAXIMUM_STRING );
    fLabelHeight = aFontMetrics.getAscent()+aFontMetrics.getDescent();

    add( createSpeedUpPanel() );
    add( createPlayTimePanel() );
    fSimulator.addPropertyChangeListener( new TimeFactorListener() );
    updateForTimeFactor( fSimulator.getTimeFactor() );
  }

  /**
   * Creates the speed up panel
   * @return a JPanel
   */
  private JPanel createSpeedUpPanel() {
    double minimum = MINIMUM_SPEED_UP;
    double maximum = ( fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime() ) /
                     ( double ) DURATION_AT_MAX_SPEED;
    double value = fSimulator.getTimeFactor();
    String title = "Speed-up";
    fSpeedUpMediaPlayer = createMediaPlayer( minimum, maximum, value);

    fSpeedupNumberFormat = new DecimalFormat( "#.#" );
    fSpeedupSliderListener = new SpeedupSliderListener();
    KeyListener speedupLabelListener = new SpeedupLabelKeyListener();
    fSpeedupLabel = new JTextField();
    return createSliderPanel( fSpeedUpMediaPlayer, title, fSpeedupNumberFormat, fSpeedupSliderListener, speedupLabelListener, fSpeedupLabel );
  }

  /**
   * Creates the play time panel
   * @return a JPanel
   */
  private JPanel createPlayTimePanel() {
    double duration = fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime();
    double maximum = duration / MINIMUM_SPEED_UP;
    double minimum = DURATION_AT_MAX_SPEED;
    double value = duration / fSimulator.getTimeFactor();
    String title = "Play-time";
    final MediaPlayer mediaPlayer = createMediaPlayer( minimum, maximum, value);

    fPlayTimeSliderListener = new PlayTimeSliderListener();
    KeyListener playTimeLabelListener = new PlaytimeLabelKeyListener();
    fPlayTimeLabel = new JTextField();
    fPlayTimeMediaPlayer = mediaPlayer;
    return createSliderPanel( fPlayTimeMediaPlayer, title, fDateDifferenceFormat, fPlayTimeSliderListener, playTimeLabelListener, fPlayTimeLabel );
  }

  /**
   * Creates a media player with given minimum, maximum and current values
   * @param aMinimum a minimum value
   * @param aMaximum a maximum value
   * @param aValue a current value
   * @return a MediaPlayer
   */
  private MediaPlayer createMediaPlayer( double aMinimum, double aMaximum, double aValue) {
    MediaPlayer mediaPlayer = new MediaPlayer( aMinimum, aMaximum);
    mediaPlayer.setValue( aValue );
    return mediaPlayer;
  }

  /**
   * Creates a slider panel with the given parameters, composed of a slider and a text label.
   * @param aMediaPlayer a MediaPlayer that models data.
   * @param aTitle a title for the panel
   * @param aFormat a Format in which the text label should be formatted
   * @param aSliderListener a listener for slider changes
   * @param aLabelListener a listener for label changes
   * @param aLabel a JTextLabel
   * @return a JPanel
   */
  private JPanel createSliderPanel( final MediaPlayer aMediaPlayer, String aTitle, final Format aFormat, PropertyChangeListener aSliderListener, KeyListener aLabelListener, JTextField aLabel ) {
    JPanel panel = new JPanel( new BorderLayout() );

    MediaPlayerSlider slider = new MediaPlayerSlider( aMediaPlayer );
    Dimension speedSliderDimension = new Dimension( SLIDER_WIDTH, ( int ) slider.getMinimumSize().getHeight() );
    slider.setSize( speedSliderDimension );
    slider.setMaximumSize( speedSliderDimension );
    slider.setMinimumSize( speedSliderDimension );
    slider.setPreferredSize( speedSliderDimension );
    aMediaPlayer.addPropertyChangeListener( aSliderListener );
    slider.setEnableInterval( false );

    aLabel.setMinimumSize( new Dimension( fLabelWidth, fLabelHeight ) );
    aLabel.setMaximumSize( new Dimension( fLabelWidth, fLabelHeight ) );
    aLabel.setPreferredSize( new Dimension( fLabelWidth, fLabelHeight ) );
    aLabel.addKeyListener( aLabelListener );

    panel.add( BorderLayout.CENTER, slider );
    panel.add( BorderLayout.EAST, aLabel );
    return TitledPanel.createTitledPanel( aTitle, panel );
  }

  /**
   * A format for time
   */
  private class DateDifferenceFormat extends Format {
    public StringBuffer format( Object aValue, StringBuffer aStringBuffer, FieldPosition aFieldPosition ) {
      if ( aValue instanceof Double ) {
        int days = ( int ) ( ( Double ) aValue / ( 1000d * 60d * 60d * 24d ) );
        aStringBuffer.append( days );
        aStringBuffer.append( " days " );
        double rest = ( Double ) aValue - days * 1000d * 60d * 60d * 24d;

        int hours = ( int ) ( rest / ( 1000d * 60d * 60d ) );
        if ( hours < 10 ) {
          aStringBuffer.append( "0" );
        }
        aStringBuffer.append( hours );
        aStringBuffer.append( ":" );

        rest = rest - hours * 1000d * 60d * 60d;

        int minutes = ( int ) ( rest / ( 1000d * 60d ) );
        if ( minutes < 10 ) {
          aStringBuffer.append( "0" );
        }
        aStringBuffer.append( minutes );
        aStringBuffer.append( ":" );

        rest = rest - minutes * 1000d * 60d;

        int seconds = ( int ) ( rest / ( 1000d ) );
        if ( seconds < 10 ) {
          aStringBuffer.append( "0" );
        }
        aStringBuffer.append( seconds );

      }
      return aStringBuffer;
    }

    public Object parseObject( String aString, ParsePosition aParsePosition ) {
      String[] parts = aString.split( "days" );
      parts[ 0 ] = parts[ 0 ].replaceAll( " ", "" );
      long time = new Long( parts[ 0 ] ) * 1000 * 60 * 60 * 24;
      String[] clockParts = parts[ 1 ].split( ":" );

      clockParts[ 0 ] = clockParts[ 0 ].replaceAll( " ", "" );
      clockParts[ 0 ] = clockParts[ 0 ].replaceAll( "^0", "" );

      clockParts[ 1 ] = clockParts[ 1 ].replaceAll( " ", "" );
      clockParts[ 1 ] = clockParts[ 1 ].replaceAll( "^0", "" );

      clockParts[ 2 ] = clockParts[ 2 ].replaceAll( " ", "" );
      clockParts[ 2 ] = clockParts[ 2 ].replaceAll( "^0", "" );
      time = time + new Long( clockParts[ 0 ] ) * 1000 * 60 * 60;
      time = time + new Long( clockParts[ 1 ] ) * 1000 * 60;
      time = time + new Long( clockParts[ 2 ] ) * 1000;
      aParsePosition.setIndex( aString.length() );
      return ( double ) time;
    }
  }

  /**
   * Listener for changes in the speed up label. It updates the simulator accordingly.
   */
  private class SpeedupLabelKeyListener extends KeyAdapter {
    public void keyPressed( KeyEvent aKeyEvent ) {
      if ( aKeyEvent.getKeyCode() == KeyEvent.VK_ENTER ) {
        if ( !( "".equals( fSpeedupLabel.getText() ) ) ) {
          try {
            Number timeFactor = ( Number ) fSpeedupNumberFormat.parseObject( fSpeedupLabel.getText() );
            fSimulator.setTimeFactor( timeFactor.doubleValue() );
          }
          catch ( ParseException parseException ) {
            sLogger.error( parseException.getMessage(), parseException );
          }
        }
      }
    }
  }

  /**
   * Listener for changes in the playtime label. It  updates the simulator accordingly. 
   */
  private class PlaytimeLabelKeyListener extends KeyAdapter {
    public void keyPressed( KeyEvent aKeyEvent ) {
      if ( aKeyEvent.getKeyCode() == KeyEvent.VK_ENTER ) {
        if ( !( "".equals( fPlayTimeLabel.getText() ) ) ) {
          try {
            Number playTime = ( Number ) fDateDifferenceFormat.parseObject( fPlayTimeLabel.getText() );
            if ( playTime instanceof Long ) {
              Long number = ( Long ) playTime;
              fSimulator.setTimeFactor( ( fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime() ) / number );
            }else if(playTime instanceof Double){
              Double number = (Double) playTime;
              fSimulator.setTimeFactor( ( fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime() ) / number );
            }
          } catch ( ParseException parseException ) {
            sLogger.error( parseException.getMessage(), parseException );
          }
        }
      }
    }
  }

  /**
   * Listener for changes in the play time slider. Changes the simulator accordingly.
   */
  private class PlayTimeSliderListener implements PropertyChangeListener{
    public void propertyChange( PropertyChangeEvent evt ) {
      if("value".equals( evt.getPropertyName() )){
        double playTime = ConversionUtil.convertToFunction( fPlayTimeMediaPlayer.getMinimum(),fPlayTimeMediaPlayer.getMaximum(),fPlayTimeMediaPlayer.getRelativePosition());
        double timeFactor = ( fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime() ) / playTime;
        fSimulator.setTimeFactor( timeFactor );
      }
    }
  }

  /**
   * Listener for changes int the speed up slider. Changes the simulator accordingly.
   */
  private class SpeedupSliderListener implements PropertyChangeListener{
    public void propertyChange( PropertyChangeEvent evt ) {
      if("value".equals( evt.getPropertyName() )){
        double timeFactor = fSpeedUpMediaPlayer.getRelativePosition();
        fSimulator.setTimeFactor( ConversionUtil.convertToFunction( fSpeedUpMediaPlayer.getMinimum(),fSpeedUpMediaPlayer.getMaximum(),timeFactor ));
      }
    }
  }

  /**
   * Synchronizes the slider and label values to match a given time factor.
   * @param aTimeFactor a time factor
   */
  private void updateForTimeFactor( double aTimeFactor ) {
    double playTime = ( fSimulator.getEndDate().getTime() - fSimulator.getBeginDate().getTime() ) / aTimeFactor;

    fSpeedupLabel.setText( fSpeedupNumberFormat.format( aTimeFactor ) );
    fPlayTimeLabel.setText( fDateDifferenceFormat.format( playTime ) );

    fPlayTimeMediaPlayer.removePropertyChangeListener( fPlayTimeSliderListener );
    fSpeedUpMediaPlayer.removePropertyChangeListener( fSpeedupSliderListener );

    fPlayTimeMediaPlayer.setRelativeValue( ConversionUtil.convertFromFunction( fPlayTimeMediaPlayer.getMinimum(), fPlayTimeMediaPlayer.getMaximum(), playTime ) );
    fSpeedUpMediaPlayer.setRelativeValue( ConversionUtil.convertFromFunction( fSpeedUpMediaPlayer.getMinimum(), fSpeedUpMediaPlayer.getMaximum(), aTimeFactor ) );

    fPlayTimeMediaPlayer.addPropertyChangeListener( fPlayTimeSliderListener );
    fSpeedUpMediaPlayer.addPropertyChangeListener( fSpeedupSliderListener );
  }

  /**
   * Listens to changes in the timeFactor, and updates all the slider and label values accordingly.
   */
  private class TimeFactorListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( "timeFactor".equals( evt.getPropertyName() ) ) {
        double timeFactor = (Double) evt.getNewValue();
        updateForTimeFactor( timeFactor );
      }
    }

  }
}
