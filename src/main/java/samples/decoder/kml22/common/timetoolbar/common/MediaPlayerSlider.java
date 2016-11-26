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
package samples.decoder.kml22.common.timetoolbar.common;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class to represent a slider with a given value, interval and range.
 */
public class MediaPlayerSlider extends JPanel {
  private static final Color LUCIAD_PURPLE = new Color( 38, 0, 112 );
  private static final Color LUCIAD_ORANGE = new Color( 255, 153, 0 );
  private static final int MINIMUM_HEIGHT = 16;
  private static final int MINIMUM_WIDTH = 64;
  private static final int MINIMUM_INTERVAL_WIDTH = 8;
  private static final int INTERVAL_SLIDER_TRIGGER_RANGE = 8;
  private static final int HANDLE_WIDTH = 4;

  private boolean fPaintHandle = false;
  private int fBoundary = 4;
  private boolean fEnableInterval = false;
  private boolean fPaintIntervalHandle = false;
  private boolean fDraggingIntervalHandle = false;
  private boolean fDraggingValueHandle = false;
  private boolean fEnableDragging = true;
  private MediaPlayer fMediaPlayer;
  private MouseListener fMouseAdapter;

  /**
   * Constructs a new horizontal slider with the given width in pixels, and the given minimum and maximum values which
   * define the range of values that can be taken by this slider.
   * <p/>
   * The value of this slider will be set to <code>aMinimum </code>.
   * @param aMediaPlayer The media player to synchronize the GUI with.
   */
  public MediaPlayerSlider( MediaPlayer aMediaPlayer ) {
    setOpaque( false );
    fMediaPlayer = aMediaPlayer;
    setMinimumSize( new Dimension( MINIMUM_WIDTH, MINIMUM_HEIGHT ) );

    repaint();

    MouseListener adapter = new MouseListener();

    fMouseAdapter = adapter;
    addMouseMotionListener( fMouseAdapter );
    addMouseListener( adapter );
    fMediaPlayer.addPropertyChangeListener( new MediaPlayerListener() );
  }

  private boolean isIntervalSliderTriggered( MouseEvent aMouseEvent ) {
    final int intervalStart = getIntervalStart();

    return ( aMouseEvent.getX() < intervalStart + INTERVAL_SLIDER_TRIGGER_RANGE &&
             aMouseEvent.getX() > intervalStart - INTERVAL_SLIDER_TRIGGER_RANGE &&
             fEnableInterval );
  }

  /**
   * Returns the boundary of the media player slider
   * @return a width in pixels
   */
  public int getBoundary() {
    return fBoundary;
  }

  /**
   * Sets whether intervals should be enabled.
   *
   * @param aEnableInterval True if intervals should be enabled, false to disable.
   */
  public void setEnableInterval( boolean aEnableInterval ) {
    fEnableInterval = aEnableInterval;
  }

  /**
   * Returns whether dragging is enabled or not.
   * @return true if dragging of the slider is enabled; false otherwise.
   */
  public boolean isEnableDragging() {
    return fEnableDragging;
  }

  /**
   * Sets whether dragging is enabled or not.
   * @param aEnableDragging true if dragging of the slider is enabled; false otherwise. 
   */
  public void setEnableDragging( boolean aEnableDragging ) {
    if ( fEnableDragging!=aEnableDragging ) {
      if(!aEnableDragging){
        removeMouseListener( fMouseAdapter );
        removeMouseMotionListener( fMouseAdapter );
      }else{
        addMouseListener( fMouseAdapter );
        addMouseMotionListener( fMouseAdapter );
      }
      fEnableDragging = aEnableDragging;
    }
  }

  protected void paintComponent( Graphics aGraphics ) {
    super.paintComponent( aGraphics );
    //Now also paint the slider
    Color old_color = aGraphics.getColor();
    aGraphics.setColor( Color.white );
    aGraphics.fillRoundRect( fBoundary, fBoundary, getInnerWidth() + 2, getInnerHeight() + 2, 2, 1 );
    aGraphics.setColor( LUCIAD_PURPLE );
    aGraphics.fillRoundRect( 1 + fBoundary, 1 + fBoundary, getInnerWidth(), getInnerHeight(), 2, 1 );
    aGraphics.setColor( LUCIAD_ORANGE );
    paintIntervalPointer( aGraphics );
    aGraphics.setColor( old_color );
  }

  /**
   * Paints the interval of the slider. It paints the two handles and an area between them.
   * @param aGraphics an instance of <code>Graphics</code>
   */
  protected void paintIntervalPointer( Graphics aGraphics ) {
    final int intervalStart = getIntervalStart();
    final int intervalWidth = getIntervalWidth();
    if ( fEnableInterval ) {
      aGraphics.fillRoundRect( intervalStart, 1 + fBoundary, intervalWidth, getInnerHeight(), 2, 1 );
    }

    int middle = intervalStart + intervalWidth;
    paintHandleAt( middle, aGraphics );
    if ( fEnableInterval ) {
      paintHandleAt( intervalStart, aGraphics );
    }
  }

  /**
   * This method paints a handle at the given position.
   *
   * @param aPosition The position in pixels where the handle should be painted.
   * @param aGraphics The graphics with which the handle should be painted.
   */
  private void paintHandleAt( int aPosition, Graphics aGraphics ) {
    aGraphics.setColor( Color.white );
    aGraphics.fillRoundRect( aPosition - 4 + fBoundary, -1 + fBoundary, HANDLE_WIDTH + 4, getInnerHeight() + 6, 1, 2 );
    aGraphics.setColor( LUCIAD_PURPLE );
    aGraphics.fillRoundRect( aPosition - 3 + fBoundary, fBoundary, HANDLE_WIDTH + 2, getInnerHeight() + 4, 1, 2 );
    aGraphics.setColor( LUCIAD_ORANGE );
    aGraphics.fillRoundRect( aPosition - 2 + fBoundary, 1 + fBoundary, HANDLE_WIDTH, getInnerHeight() + 2, 1, 2 );
  }

  /**
   * Returns a position in pixels representing the start of the slider interval. This corresponds to the current value
   * minus the interval size.
   *
   * @return A position in pixels corresponding to the start of the interval.
   */
  protected int getIntervalStart() {
    double relativeEndPosition = fMediaPlayer.getRelativePosition();
    int EndPositionInPixels = ( int ) Math.round( 1 + ( relativeEndPosition * ( getInnerWidth() ) ) );

    int startPositionInPixels = EndPositionInPixels - getIntervalWidth();
    startPositionInPixels = startPositionInPixels < 0 ? 0 : startPositionInPixels;
    return 1 + fBoundary + startPositionInPixels;

  }

  /**
   * Returns the width in pixels of the interval.
   * @return A value in pixels corresponding to the width of the interval.
   */
  protected int getIntervalWidth() {
    double relativeIntervalWidth =
        fMediaPlayer.getIntervalSize() / ( fMediaPlayer.getMaximum() - fMediaPlayer.getMinimum() );
    double intervalEnd = fMediaPlayer.getRelativePosition();
    if ( intervalEnd - relativeIntervalWidth < 0. ) {
      relativeIntervalWidth = intervalEnd;

    }
    int widthInPixels = ( int ) Math.round( 1 + ( relativeIntervalWidth * ( getInnerWidth() ) ) );
    if ( widthInPixels < MINIMUM_INTERVAL_WIDTH ) {
      widthInPixels = MINIMUM_INTERVAL_WIDTH;
    }
    if ( !fEnableInterval ) {
      widthInPixels = 0;
    }
    return widthInPixels;
  }


  /**
   * Returns the width in pixels which corresponds to the range from <code>getMinimum()</code> to
   * <code>getMaximum()</code>.
   *
   * @return The inner width in pixels.
   */
  protected int getInnerWidth() {
    return getWidth() - 2 * fBoundary - 2;
  }

  /**
   * Returns the height in pixel of this media player slider.
   * @return a height in pixels
   */
  protected int getInnerHeight() {
    return MINIMUM_HEIGHT - 2 * fBoundary - 2;
  }

  /**
   * A mouse listener that translates mouse input into changes of values in the media player.
   */
  private class MouseListener extends MouseAdapter implements MouseMotionListener {

    @Override
    public void mouseEntered( MouseEvent e ) {
      fPaintHandle = true;
      repaint();
      super.mouseEntered( e );
    }

    @Override
    public void mouseExited( MouseEvent e ) {
      fPaintHandle = false;
      fPaintIntervalHandle = false;
      repaint();
    }

    public void mouseMoved( MouseEvent e ) {
      if ( fMediaPlayer.getMaximum() > fMediaPlayer.getMinimum() && isIntervalSliderTriggered( e ) ) {
        fPaintIntervalHandle = true;
        repaint();
      }
      else if ( fPaintIntervalHandle ) {
        fPaintIntervalHandle = false;
        repaint();
      }
      if ( getParent() != null ) {
        getParent().dispatchEvent( e );
      }
    }

    public void mouseDragged( MouseEvent e ) {
      if ( fDraggingIntervalHandle ) {
        final int intervalStart = getIntervalStart();
        final int intervalWidth = getIntervalWidth();
        if ( e.getX() < 0 ) {
          fMediaPlayer.setIntervalSize( fMediaPlayer.getValue() - fMediaPlayer.getMinimum() );
        }
        else if ( e.getX() - ( 1.0 + fBoundary ) < intervalStart + intervalWidth - MINIMUM_INTERVAL_WIDTH ) {
          int newWidthInPixels = intervalStart + intervalWidth - e.getX();
          double difference = ( double ) newWidthInPixels / ( double ) intervalWidth;
          fMediaPlayer.setIntervalSize( fMediaPlayer.getIntervalSize() * difference );
        }
        else {
          fMediaPlayer.setIntervalSize( MINIMUM_INTERVAL_WIDTH *
                                        ( fMediaPlayer.getMaximum() - fMediaPlayer.getMinimum() ) /
                                        getInnerWidth() );
        }


      }
      else if ( fDraggingValueHandle ) {
        double
            new_ratio =
            ( ( double ) e.getX() - ( 1. + fBoundary + MINIMUM_INTERVAL_WIDTH / 2 ) ) / ( ( double ) getInnerWidth() );
        if ( new_ratio > 1.0 ) {
          new_ratio = 1.0;
        }
        fMediaPlayer.setValue( fMediaPlayer.getMinimum() +
                               new_ratio * ( fMediaPlayer.getMaximum() - fMediaPlayer.getMinimum() ) );
      }
    }

    @Override
    public void mousePressed( MouseEvent e ) {
      if ( fMediaPlayer.getMaximum() > fMediaPlayer.getMinimum() ) {
        if ( fPaintIntervalHandle ) {
          fDraggingIntervalHandle = true;
        }
        else if ( fPaintHandle ) {
          fDraggingValueHandle = true;
        }
      }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
      fDraggingIntervalHandle = false;
      fDraggingValueHandle = false;
    }
  }

  /**
   * Listens to the changes in the MediaPlayer and updates the visualization accordingly.
   */
  private class MediaPlayerListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent aPropertyChangeEvent ) {
      if ( "minimum".equals( aPropertyChangeEvent.getPropertyName() ) ||
           "maximum".equals( aPropertyChangeEvent.getPropertyName() ) ) {
        fMediaPlayer.setIntervalSize( MINIMUM_INTERVAL_WIDTH *
                                      ( fMediaPlayer.getMaximum() - fMediaPlayer.getMinimum() ) /
                                      getInnerWidth() );
        repaint();
      }
      else if ( "value".equals( aPropertyChangeEvent.getPropertyName() ) ||
                "interval".equals( aPropertyChangeEvent.getPropertyName() ) ) {
        repaint();
      }
    }
  }

  public MediaPlayer getMediaPlayer() {
    return fMediaPlayer;
  }
}
