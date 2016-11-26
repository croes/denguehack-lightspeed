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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel that displays the information on the currently set time interval in the time toolbar.
 * Listens to the simulator (if set) and updates the date accordingly.
 * Also listens to the 
 */
public class MediaPlayerIntervalDateLabelsPanel extends JPanel {
  private JLabel fIntervalEndLabel;
  private SimpleDateFormat fDateFormat = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
  private Box.Filler fFiller;

  public MediaPlayerIntervalDateLabelsPanel() {
    this.setLayout( new BoxLayout( this,BoxLayout.X_AXIS) );
    initializeDateLabels();

  }

  private void setDateValue( long aTime ) {
    fIntervalEndLabel.setText( fDateFormat.format( new Date(aTime) ) );
  }

  private void initializeDateLabels() {
    Font labelFont = new Font( "Arial", Font.PLAIN, 11 );
    int height = 20;
    int maxWidth = 95;//this.getGraphics().getFontMetrics().stringWidth( fDateFormat.toPattern() );
    fFiller = new Box.Filler(new Dimension(0,height ),new Dimension(0,height ),new Dimension(this.getWidth()-maxWidth,height ) );
    this.add( fFiller );

    fIntervalEndLabel = new JLabel();
    fIntervalEndLabel.setFont( labelFont );
    fIntervalEndLabel.setForeground( Color.white );
    fIntervalEndLabel.setVisible( true );
    this.add( fIntervalEndLabel );
  }

  public void setMediaPlayer( MediaPlayer aMediaPlayer){
    aMediaPlayer.addPropertyChangeListener(new SimulatorSliderListener(this,aMediaPlayer));
  }
  
  private void setRelativePosition( double aRelativePosition ) {
    int locX = Math.min(( int ) (aRelativePosition*(double)this.getWidth()),this.getWidth()-fIntervalEndLabel.getWidth()-5);
    int height = this.getGraphics()!=null?this.getGraphics().getFontMetrics().getHeight():fFiller.getPreferredSize().height;
    fFiller.changeShape( new Dimension(locX,height),new Dimension(locX,height),new Dimension(locX,height) );
    fFiller.repaint(  );
  }

  private static class SimulatorSliderListener implements PropertyChangeListener {
    private WeakReference<MediaPlayerIntervalDateLabelsPanel> fDateLabels;
    private WeakReference<MediaPlayer> fMediaPlayer;

    public SimulatorSliderListener( MediaPlayerIntervalDateLabelsPanel aLabelPanel, MediaPlayer aMediaPlayer ) {
      fDateLabels = new WeakReference<MediaPlayerIntervalDateLabelsPanel>( aLabelPanel );
      fMediaPlayer = new WeakReference<MediaPlayer>( aMediaPlayer );
    }

    public void propertyChange( PropertyChangeEvent evt ) {
     MediaPlayerIntervalDateLabelsPanel labelsPanel = fDateLabels.get();
      MediaPlayer mediaPlayer = fMediaPlayer.get();
      if(labelsPanel==null){
        mediaPlayer.removePropertyChangeListener( this );
      }
      else if (mediaPlayer!=null){
        if("value".equalsIgnoreCase( evt.getPropertyName() )){
          labelsPanel.setDateValue( (long)mediaPlayer.getValue() );
          labelsPanel.setRelativePosition(mediaPlayer.getRelativePosition());
        }
      }
    }
  }

}
