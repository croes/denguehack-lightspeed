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
import com.luciad.view.ILcdLayered;
import samples.decoder.kml22.common.timetoolbar.TimeToolbarFactory;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayerDateLabelsPanel;
import samples.decoder.kml22.common.timetoolbar.common.MediaPlayerIntervalDateLabelsPanel;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.TimeMediator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A transparant panel which controls a <code>TLcdSimulator</code>.
 */
public class AdvancedTimeToolbar extends JPanel {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( AdvancedTimeToolbar.class.getName() );
  private static final int BUTTON_WIDTH = 16;
  private static final int SLIDER_WIDTH = 11 * BUTTON_WIDTH;

  private static final String IMAGES_DIR = "images/icons/";
  private static final String ICON_PLAY = "play_16.png";
  private static final String ICON_STOP = "stop_16.png";
  private static final String ICON_PAUSE = "pause_16.png";
  private static final String ICON_ALL_LEFT = "rewind_to_start_16.png";
  private static final String ICON_ALL_RIGHT = "forward_to_end_16.png";
  private static final String ICON_OPTIONS = "settings_16.png";
  private static final String ICON_LOOP = "loop_16.png";

  private TLcdSimulator fSimulator;
  private SimulatorModel fSimulatorModel;
  private JToggleButton fStartPauseButton;
  private AdvancedTimeSlider fTimeSlider;

  //animation variables
  private float fTimeBarAlpha = 0.5f;
  private Timer fAnimationTimer = new Timer("Timebar animation timer");
  private AnimationRunnable fAnimationTask;
  private final int fAnimationDelay = 5000;
  private final int fAnimationFrameDelay = 20;


  private AdvancedTimeToolbar( TLcdSimulator aSimulator, SimulatorModel aSimulatorModel) {
    fSimulator = aSimulator;
    fSimulatorModel = aSimulatorModel;

    //adds a property change listener that updates the animation
    fSimulator.addPropertyChangeListener( new SimulatorPropertyChangeListener() );
    AnimationListener listener = new AnimationListener();
    aSimulatorModel.addPropertyChangeListener( listener );
    this.addMouseMotionListener( listener );

    createGUI();
    setCursor( Cursor.getDefaultCursor() );
  }

  /**
   * <p>Creates an advanced time toolbar for a given view. This method will create all necessary
   * listeners and set all properties to default values.</p>
   * <p>Note: The advanced time toolbar is dependant on the RealTime optional module. It features
   * additional buttons over the simple time toolbar to simulate a KML file in real time or
   * at accelerated speeds.</p>
   * @param aLayered an <code>ILcdLayered</code>
   * @param aComponent a <code>Component</code>
   * @return An JPanel that represents an advanced time toolbar
   */
  public static AdvancedTimeToolbar createAdvancedTimeToolbar( ILcdLayered aLayered, Component aComponent ) {
    TLcdSimulator simulator = new TLcdSimulator();
    AdvancedTimeToolbarSimulatorModel simulatorModel = new AdvancedTimeToolbarSimulatorModel( ( ILcdLayered ) aLayered );
    simulator.setSimulatorModel( simulatorModel );
    AdvancedTimeToolbar advancedTimeToolbar = new AdvancedTimeToolbar( simulator, simulatorModel );

    advancedTimeToolbar.setOpaque( false );
    advancedTimeToolbar.setVisible( false );
    advancedTimeToolbar.setLocation( ( int ) ( ( ( Component ) aComponent ).getWidth() / 2 - advancedTimeToolbar.getWidth() / 2. ),
                                     ( ( Component ) aComponent ).getHeight() - advancedTimeToolbar.getHeight() - 10 );
    advancedTimeToolbar.setSize( advancedTimeToolbar.getPreferredSize() );
    advancedTimeToolbar.setMaximumSize( advancedTimeToolbar.getPreferredSize() );

    simulatorModel.addPropertyChangeListener( new TimeToolbarFactory.TimeToolbarVisibilityUpdater( advancedTimeToolbar ) );
    simulatorModel.addPropertyChangeListener( new TimeToolbarFactory.ViewUpdateListener( new TimeMediator( ( ILcdLayered ) aLayered ), simulatorModel ) );

    return advancedTimeToolbar;
  }

  @Override
  public void paint( Graphics g ) {
    Graphics2D g2d = ( Graphics2D ) g;
    g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fTimeBarAlpha ));
    super.paint( g );
  }

  /**
   * Creates the gui for this time toolbar
   */
  private void createGUI() {
    GridBagConstraints c = new GridBagConstraints();


    setOpaque( false );
    setBackground( new Color( 0, 0, 0, 50 ) );
    setVisible( true );
    setLayout( new GridBagLayout() );

    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;       //aligned with column 2
    c.gridwidth = 22;   //22 columns wide
    c.gridy = 0;       //first row
    c.ipady = 5;

    MediaPlayerIntervalDateLabelsPanel intervalDateLabelsPanel = new MediaPlayerIntervalDateLabelsPanel();
    intervalDateLabelsPanel.setOpaque( false );
    add( intervalDateLabelsPanel, c );


    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 1;


    add( createStartPauzeButton(), c );
    c.gridx++;

    JComponent stop_button = createStopButton();
    add( stop_button, c );
    c.gridx++;

    add( createAllLeftButton(), c );
    c.gridx++;

    c.gridwidth = 20;

    fTimeSlider = createSimulatorSlider( fSimulatorModel );
    intervalDateLabelsPanel.setMediaPlayer( fTimeSlider.getMediaPlayer() );
    add( fTimeSlider, c );
    c.gridx = 23;

    c.gridwidth = 1;
    add( createAllRightButton(), c );

    c.gridx++;
    add( createOptionsButton(), c );

    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;       //aligned with column 2
    c.gridwidth = 22;   //22 columns wide
    c.gridy = 2;       //third row
    c.insets = new Insets(0,0,20,0 );

    MediaPlayerDateLabelsPanel dateLabelsPanel = new MediaPlayerDateLabelsPanel();
    dateLabelsPanel.setOpaque( false );
    dateLabelsPanel.setSimulatorModel( fSimulatorModel );
    add( dateLabelsPanel, c );


  }

  /**
   * Creates a time slider
   * @param aSimulatorModel a simulator model for which to create a time slider
   * @return a <code>AdvancedTimeSlider</code>
   */
  private AdvancedTimeSlider createSimulatorSlider( SimulatorModel aSimulatorModel ) {
    AdvancedTimeSlider slider = new AdvancedTimeSlider( aSimulatorModel );
    final Dimension sliderSize = new Dimension( 2 * SLIDER_WIDTH, ( int ) slider.getMinimumSize().getHeight() );
    slider.setMinimumSize( sliderSize );
    slider.setMaximumSize( sliderSize );
    slider.setSize( sliderSize );
    slider.setPreferredSize( sliderSize );
    return slider;
  }

  /**
   * Creates a button that controls the opening of the options panel in the advanced time toolbar.
   * @return a <code>JButton</code> complete with action listener.
   */
  private JButton createOptionsButton() {
    ImageIcon options_icon = null;
    String options_icon_path = IMAGES_DIR + ICON_OPTIONS;
    URL optionsURL = getClass().getClassLoader().getResource( options_icon_path );
    if ( optionsURL != null ) {
      options_icon = new ImageIcon( optionsURL );
    }


    JButton speedButton = new JButton( options_icon );
    speedButton.addActionListener( new OptionsListener() );


    speedButton.setToolTipText( "Options" );
    speedButton.setBackground( new Color( 255, 255, 255, 0 ) );
    speedButton.setBorderPainted( false );
    speedButton.setFocusPainted( false );
    speedButton.setContentAreaFilled( false );
    speedButton.setRolloverEnabled( false );
    speedButton.setSelected( false );
    return speedButton;
  }

  /**
   * Creates the play button for the simulator
   * @return a <code>JButton</code>
   */
  private JToggleButton createStartPauzeButton() {
    ImageIcon pause_icon = null;
    String pause_icon_path = IMAGES_DIR + ICON_PAUSE;
    URL pauseURL = getClass().getClassLoader().getResource( pause_icon_path );
    if ( pauseURL != null ) {
      pause_icon = new ImageIcon( pauseURL );
    }
    ImageIcon start_icon = null;
    String start_icon_path = IMAGES_DIR + ICON_PLAY;
    URL startURL = getClass().getClassLoader().getResource( start_icon_path );
    if ( startURL != null ) {
      start_icon = new ImageIcon( startURL );
    }
    fStartPauseButton = new JToggleButton( start_icon );
    fStartPauseButton.addActionListener( new StartPauseActionListener() );
    fStartPauseButton.setToolTipText( "Start/Pause" );
    fStartPauseButton.setBackground( new Color( 255, 255, 255, 0 ) );
    fStartPauseButton.setBorderPainted( false );
    fStartPauseButton.setFocusPainted( false );
    fStartPauseButton.setContentAreaFilled( false );
    fStartPauseButton.setSelectedIcon( pause_icon );
    fStartPauseButton.setRolloverEnabled( false );
    fStartPauseButton.setSelected( false );
    return fStartPauseButton;
  }

  /**
   * Creates the stop button for the simulator
   * @return a <code>JButton</code>
   */
  private JButton createStopButton() {
    ImageIcon stop_icon = null;
    String stop_icon_path = IMAGES_DIR + ICON_STOP;
    URL stopURL = getClass().getClassLoader().getResource( stop_icon_path );
    if ( stopURL != null ) {
      stop_icon = new ImageIcon( stopURL );
    }
    JButton button = new JButton( stop_icon );
    button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fSimulator.stop();
        fTimeSlider.setEnableDragging( true );
        fStartPauseButton.setSelected( false );
      }
    } );
    button.setToolTipText( "Stop" );
    button.setBackground( new Color( 255, 255, 255, 0 ) );
    button.setBorderPainted( false );
    button.setFocusPainted( false );
    button.setContentAreaFilled( false );
    button.setRolloverEnabled( false );
    button.setSelected( true );
    return button;
  }

  /**
   * Creates a reset button for the simulator. Pressing this button will result in the simulator
   * returning to its leftmost state.
   * @return a <code>JButton</code>
   */
  private JButton createAllLeftButton() {
    ImageIcon left_icon = null;
    String left_icon_path = IMAGES_DIR + ICON_ALL_LEFT;
    URL url = getClass().getClassLoader().getResource( left_icon_path );
    if ( url != null ) {
      left_icon = new ImageIcon( url );
    }
    JButton button = new JButton( left_icon );
    button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fSimulator.stop();
        fSimulator.setDate( fSimulator.getBeginDate() );
        fStartPauseButton.setSelected( false );
      }
    } );
    button.setToolTipText( "Go to start date" );
    button.setBackground( new Color( 255, 255, 255, 0 ) );
    button.setBorderPainted( false );
    button.setFocusPainted( false );
    button.setContentAreaFilled( false );
    button.setRolloverEnabled( false );
    button.setSelected( true );
    return button;
  }

  /**
   * Creates an all-right button for the simulator. Pressing this button will result in the
   * simulator returning to its rightmost state.
   * @return a <code>JButton</code>
   */
  private JButton createAllRightButton() {
    ImageIcon right_icon = null;
    String right_icon_path = IMAGES_DIR + ICON_ALL_RIGHT;
    URL url = getClass().getClassLoader().getResource( right_icon_path );
    if ( url != null ) {
      right_icon = new ImageIcon( url );
    }
    JButton button = new JButton( right_icon );
    button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fSimulator.stop();
        fSimulator.setDate( fSimulator.getEndDate() );
        fStartPauseButton.setSelected( false );
      }
    } );
    button.setToolTipText( "Go to end date" );
    button.setBackground( new Color( 255, 255, 255, 0 ) );
    button.setBorderPainted( false );
    button.setFocusPainted( false );
    button.setContentAreaFilled( false );
    button.setRolloverEnabled( false );
    button.setSelected( true );
    return button;
  }

  /**
   * An action listener for the play/pause button. It manipulates the simulator.
   */
  private class StartPauseActionListener implements ActionListener {

    public void actionPerformed( ActionEvent e ) {
      JToggleButton button = ( JToggleButton ) e.getSource();
      if ( button.isSelected() ) {
        fSimulator.run();
        fTimeSlider.setEnableDragging( false );
      }
      else {
        fSimulator.pause();
        fTimeSlider.setEnableDragging( true );
      }
    }
  }

  /**
   * An action listener for the options button. It opens the options panel.
   */
  private class OptionsListener implements ActionListener {
    public void actionPerformed( ActionEvent aActionEvent ) {
      final boolean simulatorRunning = fSimulator.getStatus() == TLcdSimulator.RUNNING;
      fSimulator.pause();
      fStartPauseButton.setSelected( false );


      JDialog dialog = new JDialog();
      dialog.setTitle( "Options" );
      dialog.setModal( true );

      Container contentPane = dialog.getContentPane();
      contentPane.setLayout( new BorderLayout() );
      contentPane.add( new AdvancedTimeToolbarOptionsPanel( fSimulator, AdvancedTimeToolbar.this.getGraphics().getFontMetrics() ) );


      dialog.setLocationRelativeTo( null );
      dialog.setSize( 400, 150 );
      dialog.setVisible( true );

      dialog.addWindowListener( new WindowAdapter() {
        @Override
        public void windowDeactivated( WindowEvent aWindowEvent ) {
          super.windowDeactivated( aWindowEvent );
          if ( simulatorRunning ) {
            fSimulator.run();
            fStartPauseButton.setSelected( true );
          }
        }
      } );
    }


  }

  /**
   * A listener that resets the play button when the end of the simulation has been reached.
   */
  private class SimulatorPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( "status".equals( evt.getPropertyName() ) ) {
        //If end of time reached
        if ( evt.getNewValue().equals( TLcdSimulator.ENDED )
             && evt.getOldValue().equals( TLcdSimulator.RUNNING ) ) {
          fStartPauseButton.setSelected( false );
        }
      }
    }
  }

  /**
   * A timer task that animates a fade-out effect of the time slider.
   */
  private class AnimationRunnable extends TimerTask {
    private boolean fCancelled = false;
    public void run() {
      while ( fTimeBarAlpha > 0.1f ) {
        if ( fCancelled ) {
          return;
        }
        fTimeBarAlpha -= 0.01f;
        AdvancedTimeToolbar.this.repaint();
        try {
          Thread.sleep( fAnimationFrameDelay );
        } catch ( InterruptedException e ) {
          sLogger.trace( "Thread " + Thread.currentThread() + " was interrupted during a sleep operation." );
        }
      }
      fTimeBarAlpha = 0.1f;
    }

    @Override
    public boolean cancel() {
      boolean b = super.cancel();
      fCancelled = true;
      return b;
    }
  }

  /**
   * A listener that resets the fade-out effect of the timer slider.
   */
  private class AnimationListener extends MouseMotionAdapter implements PropertyChangeListener {
    public void mouseMoved( MouseEvent e ) {
      resetAnimation();
    }

    private void resetAnimation() {
      if ( fAnimationTask != null ) {
        fAnimationTask.cancel();
      }
      fAnimationTask = new AnimationRunnable();
      fAnimationTimer.schedule( fAnimationTask, fAnimationDelay );
      fTimeBarAlpha = 1.0f;
      AdvancedTimeToolbar.this.repaint();
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      resetAnimation();
    }
  }
}
