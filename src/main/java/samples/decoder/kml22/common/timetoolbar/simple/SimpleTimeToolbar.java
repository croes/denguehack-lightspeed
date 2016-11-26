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

import com.luciad.view.ILcdLayered;
import samples.decoder.kml22.common.timetoolbar.TimeToolbarFactory;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayerDateLabelsPanel;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.TimeMediator;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;

/**
 * A class to represent a simple time toolbar 
 */
public class SimpleTimeToolbar extends JPanel {
  private static final int BUTTON_WIDTH = 16;
  private static final int SLIDER_WIDTH = 11 * BUTTON_WIDTH;

  private SimulatorModel fSimulatorModel = null;

  /**
   * Creates a new <code>TimeToolbar</code> with the given simulator model
   *
   * @param aSimulatorModel A simulator model
   */
  private SimpleTimeToolbar( SimulatorModel aSimulatorModel ) {
    fSimulatorModel = aSimulatorModel;
    createGUI();
    setCursor( Cursor.getDefaultCursor() );
  }

  /**
   * <p>Creates a simple time toolbar for a given view. This method will create all necessary
   * listeners and set all properties to default values.</p>
   * <p>The simple time toolbar is not dependant on the RealTime optional module. It does not
   * have time manipulation controls, such as a play or stop button. (Unlike the advanced
   * time toolbar)</p>
   * @param aLayered an <code>ILcdLayered</code>
   * @param aComponent a <code>Component</code>
   * @return An JPanel that represents a simple time toolbar
   */
  public static SimpleTimeToolbar createSimpleTimeToolbar( ILcdLayered aLayered, Component aComponent ) {
    SimpleTimeToolbarSimulatorModel simulatorModel = new SimpleTimeToolbarSimulatorModel( aLayered );
    SimpleTimeToolbar simpleTimeToolbar = new SimpleTimeToolbar( simulatorModel );

    simpleTimeToolbar.setOpaque( false );
    simpleTimeToolbar.setVisible( false );
    simpleTimeToolbar.setLocation( ( int ) ( aComponent.getWidth() / 2 - simpleTimeToolbar.getWidth() / 2. ),
                                     aComponent.getHeight() - simpleTimeToolbar.getHeight() - 10 );
    simpleTimeToolbar.setSize( simpleTimeToolbar.getPreferredSize() );
    simpleTimeToolbar.setMaximumSize( simpleTimeToolbar.getPreferredSize() );

    simulatorModel.addPropertyChangeListener( new TimeToolbarFactory.TimeToolbarVisibilityUpdater( simpleTimeToolbar ) );
    simulatorModel.addPropertyChangeListener( new TimeToolbarFactory.ViewUpdateListener( new TimeMediator( aLayered ), simulatorModel ) );

    return simpleTimeToolbar;
  }

  /**
   * Creates the graphical user interface for this simple time toolbar
   */
  private void createGUI() {
    this.setLayout( new BorderLayout( ) );
    setOpaque( false );
    setBackground( new Color( 0, 0, 0, 50 ) );
    setVisible( true );

    SimpleTimeSlider timeSlider = createTimeSlider( fSimulatorModel );
    add( timeSlider, BorderLayout.CENTER );

    MediaPlayerDateLabelsPanel mediaPlayerDateLabelsPanel = new MediaPlayerDateLabelsPanel();
    mediaPlayerDateLabelsPanel.setOpaque( false );
    mediaPlayerDateLabelsPanel.setPreferredSize( timeSlider.getSize( ));
    add(mediaPlayerDateLabelsPanel,BorderLayout.SOUTH);

  }

  /**
   * Creates a time slider
   * @param aSimulatorModel a simulator model for which to create a time slider
   * @return a <code>SimpleTimeSlider</code>
   */
  private SimpleTimeSlider createTimeSlider( SimulatorModel aSimulatorModel ) {
    SimpleTimeSlider slider = new SimpleTimeSlider( aSimulatorModel );
    final Dimension sliderSize = new Dimension( 2 * SLIDER_WIDTH, ( int ) slider.getMinimumSize().getHeight() );
    slider.setMinimumSize( sliderSize );
    slider.setMaximumSize( sliderSize );
    slider.setSize( sliderSize );
    slider.setPreferredSize( sliderSize );
    return slider;
  }
}
