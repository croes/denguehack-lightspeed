package com.luciad.dengue.timeview;

import com.luciad.dengue.timeview.styling.ColorPalette;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wraps a {@link TimeView} into a Swing component that adds
 */
public class TimeSlider extends JPanel {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
//  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

  private static final TLcdSWIcon PLAY_ICON = new TLcdSWIcon(new TLcdImageIcon("images/play.png"));
  private static final TLcdSWIcon PAUSE_ICON = new TLcdSWIcon(new TLcdImageIcon("images/pause.png"));

  private final JLabel fCurrentTimeLabel;
  private final JButton fPlayPauseButton;
  private final ALcdAnimation fPlayAnimation;
  private final TimeView fTimeView;

  private double fSpeedFactor = 60;

  public TimeSlider(TimeView aTimeView) {
    fTimeView = aTimeView;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(fTimeView.getView().getHostComponent());
//    setMaximumSize(new Dimension(100000, 150));

    JPanel timePanel = new JPanel();
    timePanel.setOpaque(false);
    timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));

    fCurrentTimeLabel = new JLabel("");
    fCurrentTimeLabel.setFont(Font.decode("SansSerif").deriveFont(16f));
    fCurrentTimeLabel.setForeground(ColorPalette.green);

    fPlayPauseButton = new JButton(PLAY_ICON);
    fPlayPauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playPause();
      }
    });
    timePanel.add(fPlayPauseButton);

    final JComboBox<Double> speedFactors = new JComboBox<>();
    speedFactors.addItem(60d);
    speedFactors.addItem(120d);
    speedFactors.addItem(240d);
    speedFactors.addItem(600d);
    speedFactors.addItem(1200d);
    speedFactors.addItem(6000d);

    speedFactors.addItem(12000d);
    speedFactors.addItem(60000d);
    speedFactors.addItem(100000d);
    speedFactors.addItem(1000000d);

    speedFactors.setToolTipText("Configure how much seconds are played per second in real time.");
    speedFactors.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list,
                                                    Object value,
                                                    int index,
                                                    boolean isSelected,
                                                    boolean cellHasFocus) {
        return super.getListCellRendererComponent(list,
                                                  String.valueOf(value) + "x",
                                                  index,
                                                  isSelected,
                                                  cellHasFocus);
      }
    });
    speedFactors.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        fSpeedFactor = (Double) speedFactors.getSelectedItem();
      }
    });
    timePanel.add(speedFactors);

    timePanel.add(fCurrentTimeLabel);

    fTimeView.getView().getOverlayComponent().add(timePanel, TLcdOverlayLayout.Location.NORTH_WEST);

    fTimeView.addChangeListener(new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
        fCurrentTimeLabel.setText(DATE_FORMAT.format(new Date(fTimeView.getTime())).toUpperCase());
      }
    });

    fPlayAnimation = new PlayAnimation();
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

  private class PlayAnimation extends ALcdAnimation {

    private double fPreviousTimeStep = 0;

    PlayAnimation() {
      super(1, fTimeView.getView());
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
    protected void setTimeImpl(double aNewTimeStep) {
      double newTime = (aNewTimeStep - fPreviousTimeStep) * 1000 * fSpeedFactor;
      fTimeView.setTime(fTimeView.getTime() + newTime);
      fPreviousTimeStep = aNewTimeStep;
    }
  }
}
