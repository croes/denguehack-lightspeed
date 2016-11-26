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

import com.luciad.shape.ILcdTimeBounds;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayer;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayerSlider;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

/**
 * Class that extends the standard <code>MediaPlayerSlider</code> by adding functionality to display
 * areas with valid information within the slider range. This class can also listen to a simulator model
 * and update its values accordingly. It also updates the simulator model based on changes in the slider
 * values.
 */
public class AdvancedTimeSlider extends MediaPlayerSlider {
  private static final Color VALID_INTERVAL_COLOR = new Color( 38, 255, 112 );
  private SimulatorModel fSimulatorModel;
  private MediaPlayer fMediaPlayer;
  private SimulatorModelListener fSimulatorModelListener;
  private List<ILcdTimeBounds> fValidBoundsList;
  private TimeSliderListener fTimeSliderListener = new TimeSliderListener();

  /**
   * Creates a default slider with minimum and maximum value equal to zero.
   *
   * @param aMediaPlayer the media player to synchronize with.
   */
  public AdvancedTimeSlider( MediaPlayer aMediaPlayer ) {
    super( aMediaPlayer );
    fMediaPlayer = aMediaPlayer;
    fMediaPlayer.addPropertyChangeListener( fTimeSliderListener );
    setEnableInterval( true );
  }

  /**
   * Creates a default slider that listens to a simulator for updates, as well as propagate changes
   * in value to the given simulator.
   *
   * @param aSimulator   The simulator that should be attached to this slider. The simulator can
   *                     change the slider, and the slider can adjust the simulator.
   */
  public AdvancedTimeSlider( SimulatorModel aSimulator ) {
    this( new MediaPlayer( 0, 0 ) );
    setSimulatorModel( aSimulator );
  }

  /**
   * Sets the simulator which will be controlled by this slider. This slider will update
   * the value of the simulator when the its value changes.
   */
  public void setSimulatorModel( SimulatorModel aSimulator ) {
    if ( aSimulator == null && fSimulatorModelListener != null ) {
      fSimulatorModel.removePropertyChangeListener( fSimulatorModelListener );
    }
    fSimulatorModel = aSimulator;
    if ( fSimulatorModel != null ) {
      updateSliderValues();
      fSimulatorModelListener = new SimulatorModelListener( this, fSimulatorModel, fMediaPlayer, fTimeSliderListener );
      fSimulatorModel.addPropertyChangeListener( fSimulatorModelListener );
    }
    else {
      fMediaPlayer.setMinimum( 0 );
      fMediaPlayer.setValue( 0 );
      fMediaPlayer.setMaximum( 0 );
      fMediaPlayer.setIntervalSize( 0 );
    }
  }

  private void updateSliderValues() {
    fMediaPlayer.setMinimum( fSimulatorModel.getBeginDate().getTime() );
    fMediaPlayer.setMaximum( fSimulatorModel.getEndDate().getTime() );
    fMediaPlayer.setValue( fSimulatorModel.getDate().getTime() );
    fMediaPlayer.setIntervalSize( fSimulatorModel.getIntervalLength() );
  }

  private class TimeSliderListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( fSimulatorModel == null ) {
        return;
      }
      if ( "value".equalsIgnoreCase( evt.getPropertyName() ) ) {
        fSimulatorModel.removePropertyChangeListener( fSimulatorModelListener );
        fSimulatorModel.setDate( new Date( (( Double ) evt.getNewValue()).longValue() ) );
        fSimulatorModel.addPropertyChangeListener( fSimulatorModelListener );
      }
      else if ( "interval".equalsIgnoreCase( evt.getPropertyName() ) ) {
        fSimulatorModel.removePropertyChangeListener( fSimulatorModelListener );
        fSimulatorModel.setIntervalLength( (( Double ) evt.getNewValue()).longValue() );
        fSimulatorModel.addPropertyChangeListener( fSimulatorModelListener );
      }
    }
  }

  /**
   * Listens to the simulator model, and updates the visualisation accordingly.
   */
  private static class SimulatorModelListener implements PropertyChangeListener {
    private WeakReference<AdvancedTimeSlider> fSlider;
    private WeakReference<SimulatorModel> fSimulatorModel;
    private WeakReference<MediaPlayer> fMediaPlayer;
    private WeakReference<TimeSliderListener> fTimeSliderListener;

    private SimulatorModelListener( AdvancedTimeSlider aSlider,
                               SimulatorModel aSimulatorModel,
                               MediaPlayer aMediaPlayer,
                               TimeSliderListener aTimeSliderListener ) {
      fSlider = new WeakReference<AdvancedTimeSlider>( aSlider );
      fSimulatorModel = new WeakReference<SimulatorModel>( aSimulatorModel );
      fMediaPlayer = new WeakReference<MediaPlayer>( aMediaPlayer );
      fTimeSliderListener = new WeakReference<TimeSliderListener>( aTimeSliderListener );
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      AdvancedTimeSlider slider = fSlider.get();
      SimulatorModel simulator = fSimulatorModel.get();
      MediaPlayer mediaPlayer = fMediaPlayer.get();
      TimeSliderListener timeSliderListener = fTimeSliderListener.get();
      if ( slider == null ) {
        simulator.removePropertyChangeListener( this );
      }
      else if ( simulator != null ) {
        if ( ( "intervalEndDate".equalsIgnoreCase( evt.getPropertyName() ) ) ) {
          mediaPlayer.removePropertyChangeListener( timeSliderListener );
          mediaPlayer.setValue( simulator.getDate().getTime() );
          mediaPlayer.addPropertyChangeListener( timeSliderListener );
        }
        else if( "intervalLength".equalsIgnoreCase( evt.getPropertyName() )){
          mediaPlayer.removePropertyChangeListener( timeSliderListener );
          mediaPlayer.setIntervalSize( simulator.getIntervalLength() );
          mediaPlayer.addPropertyChangeListener( timeSliderListener );
        }
        else if ( "globalBeginDate".equalsIgnoreCase( evt.getPropertyName() ) ) {
          mediaPlayer.removePropertyChangeListener( timeSliderListener );
          mediaPlayer.setMinimum( simulator.getBeginDate().getTime() );
          slider.updateBoundsVisualisation();
          mediaPlayer.addPropertyChangeListener( timeSliderListener );
        }
        else if ( "globalEndDate".equalsIgnoreCase( evt.getPropertyName() ) ) {
          mediaPlayer.removePropertyChangeListener( timeSliderListener );
          mediaPlayer.setMaximum( simulator.getEndDate().getTime() );
          slider.updateBoundsVisualisation();
          mediaPlayer.addPropertyChangeListener( timeSliderListener );
        }
        else if ( "modelChanged".equalsIgnoreCase( evt.getPropertyName() ) ) {
          //This event is signaled when a KML model has changed
          mediaPlayer.removePropertyChangeListener( timeSliderListener );
          slider.updateBoundsVisualisation();
          slider.repaint();
          mediaPlayer.addPropertyChangeListener( timeSliderListener );
        }
      }
    }

  }

  @Override
  protected void paintComponent( Graphics aGraphics ) {
    super.paintComponent( aGraphics );
    Graphics2D g2d = ( Graphics2D ) aGraphics;
    Color oldColor = aGraphics.getColor();
    if ( fValidBoundsList!=null ) {
      for ( ILcdTimeBounds bounds : fValidBoundsList ) {
        Rectangle validArea = transformTimeBoundsToRectangle( bounds );
        aGraphics.setColor( VALID_INTERVAL_COLOR );
        g2d.fill( validArea );
      }
    }
    aGraphics.setColor( oldColor );
    paintIntervalPointer( aGraphics );
  }

  public void updateBoundsVisualisation() {
    updateSliderValues();
    fValidBoundsList = ( fSimulatorModel ).getTimeMediator().getAllValidTimeBounds();
  }

  private Rectangle transformTimeBoundsToRectangle( ILcdTimeBounds aTimeBounds ) {
    int transformedBegin = transformTimeBoundsToScreenCoordinates( aTimeBounds.getBeginTime() );
    int transformedEnd = transformTimeBoundsToScreenCoordinates( aTimeBounds.getEndTime() );
    int y = 1 + getBoundary();
    int width = transformedEnd == transformedBegin ? 1 : transformedEnd - transformedBegin;
    int height = getInnerHeight();
    return new Rectangle( transformedBegin, y, width, height );
  }

  private int transformTimeBoundsToScreenCoordinates( long aTimeBoundsCoordinate ) {
    int start = getBoundary() + 1;
    int size = getInnerWidth();

    long startDate = fSimulatorModel.getBeginDate().getTime();
    long endDate = fSimulatorModel.getEndDate().getTime();

    return ( int ) ( ( ( double ) ( aTimeBoundsCoordinate - startDate ) / ( double ) ( endDate - startDate ) ) *
                     ( ( double ) size ) ) + start;
  }
}
