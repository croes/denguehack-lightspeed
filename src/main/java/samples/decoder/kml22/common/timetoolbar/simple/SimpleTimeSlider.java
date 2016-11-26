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
package samples.decoder.kml22.common.timetoolbar.simple;

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
 * A class that represents a simple time slider. This time slider will update the simulator model
 * it is given, and will update itself based on changes from the simulator model. It will also
 * visualize the available and valid time bounds of the time slider as green rectangles on the time slider.
 */
public class SimpleTimeSlider extends MediaPlayerSlider {
  private static final Color VALID_INTERVAL_COLOR = new Color( 38, 255, 112 );
  private MediaPlayer fMediaPlayer;
  private List<ILcdTimeBounds> fValidBoundsList;
  private TimeSliderListener fTimeSliderListener = new TimeSliderListener();
  private SimulatorModel fSimulatorModel;

  /**
   * Creates a default slider with minimum and maximum value equal to zero.
   *
   * @param aMediaPlayer the media player to synchronize with.
   */
  public SimpleTimeSlider( MediaPlayer aMediaPlayer ) {
    super( aMediaPlayer );
    fMediaPlayer = aMediaPlayer;
    fMediaPlayer.addPropertyChangeListener( fTimeSliderListener );
    setEnableInterval( true );
  }

  /**
   * Creates a new simple time slider with a given simulator model
   * @param aSimulatorModel a simulator model
   */
  public SimpleTimeSlider( SimulatorModel aSimulatorModel ) {
    this( new MediaPlayer( 0, 0 ) );
    setSimulatorModel( aSimulatorModel );
  }

  /**
   * Sets the simulator model of this simple time slider
   * @param aSimulatorModel a simulator model
   */
  public void setSimulatorModel( SimulatorModel aSimulatorModel ) {
    fSimulatorModel = aSimulatorModel;
    if ( fSimulatorModel !=null ) {
      updateSliderValues();
      fMediaPlayer.setIntervalSize( fSimulatorModel.getIntervalLength() );
      SimulatorModelListener simulatorModelListener = new SimulatorModelListener( this, fSimulatorModel, fMediaPlayer, fTimeSliderListener );
      fSimulatorModel.addPropertyChangeListener( simulatorModelListener );
    }
  }

  /**
   * Updates the slider values of this time slider, based on the simulator model
   */
  private void updateSliderValues() {
    fMediaPlayer.setMinimum( fSimulatorModel.getBeginDate().getTime() );
    fMediaPlayer.setMaximum( fSimulatorModel.getEndDate().getTime() );
    fMediaPlayer.setValue( fSimulatorModel.getDate().getTime() );
    fMediaPlayer.setIntervalSize( fSimulatorModel.getIntervalLength() );
  }

  /**
   * Inner class that listens to changes in the slider, and updates the simulator model accordingly.
   */
  private class TimeSliderListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( fSimulatorModel == null ) {
        return;
      }
      if ( "value".equalsIgnoreCase( evt.getPropertyName() ) ) {
        fSimulatorModel.setDate( new Date( (( Double ) evt.getNewValue()).longValue() ) );
      }
      else if ( "interval".equalsIgnoreCase( evt.getPropertyName() ) ) {
        fSimulatorModel.setIntervalLength( (( Double ) evt.getNewValue()).longValue() );
      }
    }
  }

  /**
   * Inner class that listens to the simulator model, and updates the slider visualisation accordingly.
   */
  private static class SimulatorModelListener implements PropertyChangeListener {
    private WeakReference<SimpleTimeSlider> fSlider;
    private WeakReference<SimulatorModel> fSimulatorModel;
    private WeakReference<MediaPlayer> fMediaPlayer;
    private WeakReference<TimeSliderListener> fTimeSliderListener;

    private SimulatorModelListener( SimpleTimeSlider aSlider,
                               SimulatorModel aSimulatorModel,
                               MediaPlayer aMediaPlayer,
                               TimeSliderListener aTimeSliderListener ) {
      fSlider = new WeakReference<SimpleTimeSlider>( aSlider );
      fSimulatorModel = new WeakReference<SimulatorModel>( aSimulatorModel );
      fMediaPlayer = new WeakReference<MediaPlayer>( aMediaPlayer );
      fTimeSliderListener = new WeakReference<TimeSliderListener>( aTimeSliderListener );
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      SimpleTimeSlider slider = fSlider.get();
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
        }else if("modelChanged".equals( evt.getPropertyName() )){
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
    for ( ILcdTimeBounds bounds : fValidBoundsList ) {
      Rectangle validArea = transformTimeBoundsToRectangle( bounds );
      aGraphics.setColor( VALID_INTERVAL_COLOR );
      g2d.fill( validArea );
    }
    aGraphics.setColor( oldColor );
    paintIntervalPointer( aGraphics );
  }

  /**
   * Updates the time bounds visualisation of this simple time slider. It collects
   * a valid bounds list that will be used while paining to see which areas
   * of the time slider have valid time bounds.
   */
  public void updateBoundsVisualisation() {
    updateSliderValues();
    fValidBoundsList = fSimulatorModel.getTimeMediator().getAllValidTimeBounds();
  }

  /**
   * Transforms a time bounds to relative pixel coordinates, returned in the form of a rectangle.
   * @param aTimeBounds a time bounds
   * @return a rectangle that represents a relative area on the time slider, equal to the given time bounds
   */
  private Rectangle transformTimeBoundsToRectangle( ILcdTimeBounds aTimeBounds ) {
    int transformedBegin = transformTimeBoundsToScreenCoordinates( aTimeBounds.getBeginTime() );
    int transformedEnd = transformTimeBoundsToScreenCoordinates( aTimeBounds.getEndTime() );
    int y = 1 + getBoundary();
    int width = transformedEnd == transformedBegin ? 1 : transformedEnd - transformedBegin;
    int height = getInnerHeight();
    return new Rectangle( transformedBegin, y, width, height );
  }

  /**
   * Transforms a single coordinate into a horizontal pixel coordinate relative to the time slider
   * @param aTimeBoundsCoordinate time in milliseconds since the epoch
   * @return a relative coordinate expressed in pixels 
   */
  private int transformTimeBoundsToScreenCoordinates( long aTimeBoundsCoordinate ) {
    int start = getBoundary() + 1;
    int size = getInnerWidth();

    long startDate = fSimulatorModel.getBeginDate().getTime();
    long endDate = fSimulatorModel.getEndDate().getTime();

    return ( int ) ( ( ( double ) ( aTimeBoundsCoordinate - startDate ) / ( double ) ( endDate - startDate ) ) *
                     ( ( double ) size ) ) + start;
  }


}
