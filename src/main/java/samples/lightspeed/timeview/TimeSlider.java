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
package samples.lightspeed.timeview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;

import samples.common.UIColors;
import samples.lightspeed.timeview.model.TimeReference;

/**
 * Wraps a {@link TimeView} into a Swing component that adds a play/pause button.
 * <p>
 *   The replay speed is dependent on the zoom level of the time line.
 * </p>
 * <p>
 * Notes:
 * <ul>
 *   <li>
 *     Use {@link #getView()} to get the Lightspeed {@link ILspView view} and add layers to it.
 *     The geometry (either in the model, or submitted through a {@link com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider style target provider})
 *     should be in the {@link TimeReference#INSTANCE time reference}.
 *   </li>
 *   <li>Use {@link #setTime} to programmatically move the time line.</li>
 *   <li>Use {@link #getTime()} and {@link #addChangeListener change events} to listen to time line changes.</li>
 *   <li>Use {@link #setValidRange} to set the valid time and Y range.  Navigation will be constrained to that range.</li>
 *   <li>Use {@link #getVisibleRange} to get the time and Y extent currently visible on the view.</li>
 * </ul>
 * </p>
 */
public class TimeSlider extends JPanel implements ILcdChangeSource {

  private static final Color LABEL_COLOR = UIColors.fgAccent();

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

  private static final TLcdSWIcon PLAY_ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PLAY_ICON));
  private static final TLcdSWIcon PAUSE_ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PAUSE_ICON));

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private final JLabel fCurrentSpeedLabel;
  private final JLabel fCurrentTimeLabel;
  private final JButton fPlayPauseButton;
  private final ALcdAnimation fPlayAnimation;
  private final TimeView fTimeView;

  public TimeSlider() {
    fTimeView = new TimeView();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(fTimeView.getView().getHostComponent());

    JPanel timePanel = new JPanel();
    timePanel.setOpaque(false);

    fCurrentTimeLabel = new JLabel("");
    fCurrentTimeLabel.setFont(Font.decode("SansSerif").deriveFont(16f));
    fCurrentTimeLabel.setForeground(LABEL_COLOR);

    fCurrentSpeedLabel = new JLabel("");
    fCurrentSpeedLabel.setFont(Font.decode("SansSerif").deriveFont(16f));
    fCurrentSpeedLabel.setForeground(LABEL_COLOR);

    fPlayPauseButton = new JButton(PLAY_ICON);
    fPlayPauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playPause();
      }
    });

    timePanel.add(fPlayPauseButton);
    timePanel.add(fCurrentSpeedLabel);
    timePanel.add(fCurrentTimeLabel);

    fTimeView.addChangeListener(new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aEvent) {
        fCurrentTimeLabel.setText(DATE_FORMAT.format(new Date(getTime())).toUpperCase());
        fChangeSupport.fireChangeEvent(aEvent);
      }
    });

    fPlayAnimation = new PlayAnimation();

    Component zoomControls = TLspNavigationControlsBuilder.newBuilder(getView()).smallImagePath().zoomNavigationControl().build();

    Container overlay = fTimeView.getView().getOverlayComponent();
    overlay.add(zoomControls, TLcdOverlayLayout.Location.EAST);
    overlay.add(timePanel, TLcdOverlayLayout.Location.NORTH_WEST);
  }

  /**
   * Returns the Lightspeed view that you can add layers to.
   * The view's world reference is {@link TimeReference#INSTANCE}.
   */
  public ILspAWTView getView() {
    return fTimeView.getView();
  }

  /**
   * Move the time line to the given time.
   */
  public void setTime(long aCurrentTime) {
    fTimeView.setTime(aCurrentTime);
  }

  /**
   * Get the time that is currently in the center of the time line.
   * <p/>
   * Use {@link #addChangeListener change events} to get notified of changes.
   */
  public long getTime() {
    return fTimeView.getTime();
  }

  /**
   * Get the time and Y range that is currently visible on the time view.
   * <p/>
   * Use {@link #addChangeListener change events} to get notified of changes.
   */
  public ILcdBounds getVisibleRange() {
    return fTimeView.getVisibleRange();
  }

  /**
   * Set the valid time and Y range.
   * <p/>
   * Navigation will be constrained to these ranges.
   */
  public void setValidRange(long aBeginTime, long aEndTime, double aYMin, double aYMax) {
    fTimeView.setValidRange(aBeginTime, aEndTime, aYMin, aYMax);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  private void playPause() {
    if (ALcdAnimationManager.getInstance().getAnimation(fPlayPauseButton) == null) {
      ALcdAnimationManager.getInstance().putAnimation(fPlayPauseButton, fPlayAnimation);
      fPlayPauseButton.setIcon(PAUSE_ICON);
    } else {
      ALcdAnimationManager.getInstance().removeAnimation(fPlayPauseButton);
      fPlayPauseButton.setIcon(PLAY_ICON);
    }
  }

  /**
   * This animation adapts the speed of the replay on the visible time range.
   */
  private class PlayAnimation extends ALcdAnimation {

    private double fPreviousTimeStep = 0;

    PlayAnimation() {
      super(25, fTimeView.getView());
    }

    @Override
    public void restart() {
      fPreviousTimeStep = 0;
    }

    @Override
    public boolean isLoop() {
      return true;
    }

    @Override
    public void stop() {
      fPreviousTimeStep = 0;
      fCurrentSpeedLabel.setText("");
    }

    @Override
    protected void setTimeImpl(double currentTimeStep) {
      double elapsedTime = (currentTimeStep - fPreviousTimeStep) * 1000;
      double visibleTime = getVisibleRange().getWidth();
      double replayTime = getDuration() * 1000;
      double speedFactor = visibleTime / replayTime;
      double increment = speedFactor * elapsedTime;

      fTimeView.setTime(fTimeView.getTime() + (long) increment);

      double cleanFactor = (speedFactor > 500) ? Math.round(speedFactor / 100) * 100 :
                           (speedFactor > 100) ? Math.round(speedFactor / 50) * 50 :
                           (speedFactor > 50) ? Math.round(speedFactor / 10) * 10 :
                           (speedFactor > 10) ? Math.round(speedFactor / 5) * 5 :
                           speedFactor;
      fCurrentSpeedLabel.setText((int) cleanFactor + "x");

      fPreviousTimeStep = currentTimeStep;
    }
  }
}
