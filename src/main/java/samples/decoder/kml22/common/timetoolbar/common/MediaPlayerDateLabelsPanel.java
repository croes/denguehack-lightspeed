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

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A custom JPanel that displays the global bounds of the date in textual form. The panel listens to
 * changes from a simulator model and updates the dates accordingly.
 */
public class MediaPlayerDateLabelsPanel extends JPanel {
  private long fStartTime;
  private long fEndTime;
  private JLabel fStartDateLabel;
  private JLabel fEndDateLabel;

  /**
   * Creates a new panel that represents date for the global dates
   * of a media player
   */
  public MediaPlayerDateLabelsPanel() {
    this.setLayout( new BorderLayout() );
    initializeDateLabels();
  }

  /**
   * Initializes the gui of this panel
   */
  private void initializeDateLabels() {
    Font labelFont = new Font( "Arial", Font.PLAIN, 11 );
    fStartDateLabel = new JLabel();
    fStartDateLabel.setFont( labelFont );
    fStartDateLabel.setForeground( Color.white );
    fStartDateLabel.setVisible( true );
    this.add( fStartDateLabel, BorderLayout.WEST );
    fEndDateLabel = new JLabel();
    fEndDateLabel.setFont( labelFont );
    fEndDateLabel.setForeground( Color.white );
    fEndDateLabel.setVisible( true );
    this.add( fEndDateLabel, BorderLayout.EAST );
  }

  /**
   * Sets the simulator model of this panel and attaches listeners to it, so that
   * the panel is updated whenever the simulator model is updated.
   * @param aSimulatorModel a <code>SimulatorModel</code>
   */
  public void setSimulatorModel( SimulatorModel aSimulatorModel ) {
    aSimulatorModel.addPropertyChangeListener( new SimulatorListener( this, aSimulatorModel ) );
  }

  /**
   * <p>Listener that listens to changes in global begin and end date, and changes the
   * labels accordingly.<p>
   */
  private static class SimulatorListener implements PropertyChangeListener {
    private WeakReference<MediaPlayerDateLabelsPanel> fDateLabels;
    private WeakReference<SimulatorModel> fSimulator;

    private SimulatorListener( MediaPlayerDateLabelsPanel labelpanel, SimulatorModel aTimeManager ) {
      fDateLabels = new WeakReference<MediaPlayerDateLabelsPanel>( labelpanel );
      fSimulator = new WeakReference<SimulatorModel>( aTimeManager );
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      MediaPlayerDateLabelsPanel labelsPanel = fDateLabels.get();
      SimulatorModel simulator = fSimulator.get();
      if ( labelsPanel == null ) {
        simulator.removePropertyChangeListener( this );
      }
      else if ( simulator != null ) {
        if ( "globalBeginDate".equalsIgnoreCase( evt.getPropertyName() ) ) {
          labelsPanel.setBeginDate( simulator.getBeginDate().getTime() );
          labelsPanel.formatLabels();
        }
        else if ( "globalEndDate".equalsIgnoreCase( evt.getPropertyName() ) ) {
          labelsPanel.setEndDate( simulator.getEndDate().getTime() );
          labelsPanel.formatLabels();
        }
      }
    }
  }

  /**
   * Recalculates the text of the labels, based on the state based
   * start and end times that were set with {@linkplain #setEndDate(long)} and
   * {@linkplain #setBeginDate(long)}. A default formatting is applied in the process.
   */
  private void formatLabels() {
    Date startDate = new Date( fStartTime );
    Date endDate = new Date( fEndTime );

    String format = "MM/dd/yyyy HH:mm:ss";

    SimpleDateFormat simpleDateFormat;
    simpleDateFormat = new SimpleDateFormat( format );
    fStartDateLabel.setText( simpleDateFormat.format( startDate ) );
    fEndDateLabel.setText( simpleDateFormat.format( endDate ) );
  }

  /**
   * Sets time that should be displayed by the end date label
   * @param aTime a time in milliseconds since the epoch
   */
  private void setEndDate( long aTime ) {
    fEndTime = aTime;
    fEndDateLabel.setText( new Date( aTime ).toString() );
  }

  /**
   * Sets time that should be displayed by begin date label
   * @param aTime a time in milliseconds since the epoch 
   */
  private void setBeginDate( long aTime ) {
    fStartTime = aTime;
    fStartDateLabel.setText( new Date( aTime ).toString() );
  }

}
