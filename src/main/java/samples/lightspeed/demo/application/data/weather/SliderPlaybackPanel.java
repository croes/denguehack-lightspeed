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
package samples.lightspeed.demo.application.data.weather;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.data.IOUtil;

/**
 * Panel that contains a slider with label to indicate the current value and a play and stop button for playback capabilities.
 */
class SliderPlaybackPanel extends JPanel implements ActionListener {

  private final JLabel fValueIndicator;
  private final AbstractDimensionSlider fSlider;
  private final JButton fPlay;
  private final Timer fTimer;

  SliderPlaybackPanel(AbstractDimensionSlider aSlider, int aDelay) {
    super(new BorderLayout(0, 0));
    fSlider = aSlider;
    fValueIndicator = new HaloLabel(fSlider.getTextFormattedAxisValue(), 11, true);
    fSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fValueIndicator.setText(fSlider.getTextFormattedAxisValue());
      }
    });
    fTimer = new Timer(aDelay, this);

    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    toolBar.setFloatable(false);

    final TLcdSWIcon playIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PLAY_ICON));
    final TLcdSWIcon pauseIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PAUSE_ICON));

    final boolean[] playing = new boolean[]{false};

    fPlay = new JButton(playIcon);
    fPlay.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (!playing[0]) {
          fPlay.setIcon(pauseIcon);
          play();
        } else {
          fPlay.setIcon(playIcon);
          pause();
        }
        playing[0] = !playing[0];
      }
    });

    JButton stop = new JButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.STOP_ICON)));
    stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playing[0] = false;
        fPlay.setIcon(playIcon);
        stop();
      }
    });

    toolBar.add(fValueIndicator);
    toolBar.add(fPlay);
    toolBar.add(stop);

    toolBar.setBorderPainted(false);

    add(toolBar, BorderLayout.NORTH);
    add(fSlider, BorderLayout.CENTER);
    setOpaque(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    fSlider.setValue((fSlider.getValue() + 1) % (fSlider.getMaximum() + 1));
  }

  public void play() {
    fTimer.start();
  }

  public void pause() {
    fTimer.stop();
  }

  public void stop() {
    fTimer.stop();
    fSlider.setValue(0);
  }

  public int getTimePosition() {
    return fSlider.getValue();
  }

  public void setTimePosition(int aTimePosition) {
    fSlider.setValue(aTimePosition);
  }

  public void deactivate() {
    if (isRunning()) {
      fPlay.doClick();
    }
    stop();
  }

  private boolean isRunning() {
    return fTimer.isRunning();
  }

  private BufferedImage createImage(String aSourceName) {
    try {
      return IOUtil.readImage(aSourceName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
