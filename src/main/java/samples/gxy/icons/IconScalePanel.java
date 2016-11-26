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
package samples.gxy.icons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.gxy.common.TitledPanel;

class IconScalePanel extends JPanel {

  private static final double PIXEL_MIN = 0.1;
  private static final double PIXEL_MAX = 10.0;
  private static final double PIXEL_SLIDER_SCALE = 10.0;

  private static final double WORLD_MIN = 100.0;
  private static final double WORLD_MAX = 500000.0;
  private static final double WORLD_SLIDER_SCALE = 1.0;
  private final TLcdGXYIconPainter[] fPainters;
  private final ILcdGXYView fView;

  private boolean fPixelScaling = true;
  private double fPixelScale = 1.0;
  private double fWorldScale = 100000.0;

  private JSlider fSlider;
  private JLabel fSliderLabel;

  public IconScalePanel(TLcdGXYIconPainter[] aPainters, ILcdGXYView aView) {
    fPainters = aPainters;
    fView = aView;
    // Create new panel for the check boxes.
    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    // Create a radio button panel.
    JPanel radioPanel = new JPanel(new GridLayout(2, 1));
    panel.add(radioPanel);

    // Add a radio button to choose the pixel scaling mode.
    JRadioButton pixelButton = new JRadioButton("Pixel scaling mode");
    pixelButton.setSelected(true);
    pixelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!fPixelScaling) {
          updateScalingMode(true);
          fView.invalidate(true, this, "Set pixel scaling mode");
        }
      }
    });
    radioPanel.add(pixelButton);

    // Add a radio button to choose the world scaling mode.
    JRadioButton worldButton = new JRadioButton("World scaling mode");
    worldButton.setSelected(false);
    worldButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (fPixelScaling) {
          updateScalingMode(false);
          fView.invalidate(true, this, "Set world scaling mode");
        }
      }
    });
    radioPanel.add(worldButton);

    // Group the radio buttons.
    ButtonGroup group = new ButtonGroup();
    group.add(pixelButton);
    group.add(worldButton);

    // Add slider to choose the scale.
    fSlider = new JSlider(JSlider.HORIZONTAL,
                          (int) (PIXEL_MIN * PIXEL_SLIDER_SCALE),
                          (int) (PIXEL_MAX * PIXEL_SLIDER_SCALE),
                          (int) (fPixelScale * PIXEL_SLIDER_SCALE));
    fSlider.setMajorTickSpacing(10);
    fSlider.setPaintTicks(true);
    fSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (fSlider.getValueIsAdjusting()) {
          if (fPixelScaling) {
            fPixelScale = (double) fSlider.getValue() / PIXEL_SLIDER_SCALE;
            for (TLcdGXYIconPainter painter : fPainters) {
              painter.setScale(fPixelScale);
            }
            fView.invalidate(true, this, "Set icon scale");
          } else {
            fWorldScale = (double) fSlider.getValue() / WORLD_SLIDER_SCALE;
            for (TLcdGXYIconPainter painter : fPainters) {
              painter.setScale(fWorldScale / painter.getIcon().getIconWidth());
            }
            fView.invalidate(true, this, "Set icon scale");
          }
          updateSliderLabel();
        }
      }
    });

    fSliderLabel = new JLabel();
    fSliderLabel.setPreferredSize(new Dimension(100, 25));
    fSliderLabel.setHorizontalAlignment(JLabel.LEFT);
    updateSliderLabel();

    JPanel sliderPanel = new JPanel(new GridLayout(2, 1));
    sliderPanel.add(fSlider);
    sliderPanel.add(fSliderLabel);
    panel.add(sliderPanel);

    // Add the newly created panel.
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Icon scaling options", panel));
  }

  private void updateScalingMode(boolean aPixelScaling) {
    fPixelScaling = aPixelScaling;
    if (aPixelScaling) {
      for (TLcdGXYIconPainter painter : fPainters) {
        painter.setScalingMode(TLcdGXYIconPainter.ScalingMode.PIXEL);
        painter.setScale(fPixelScale);
      }
      fSlider.setMinimum((int) (PIXEL_MIN * PIXEL_SLIDER_SCALE));
      fSlider.setMaximum((int) (PIXEL_MAX * PIXEL_SLIDER_SCALE));
      fSlider.setValue((int) (fPixelScale * PIXEL_SLIDER_SCALE));
      fSlider.setMajorTickSpacing(10);
    } else {
      for (TLcdGXYIconPainter painter : fPainters) {
        painter.setScalingMode(TLcdGXYIconPainter.ScalingMode.METER);
        painter.setScale(fWorldScale / painter.getIcon().getIconWidth());
      }
      fSlider.setMinimum((int) (WORLD_MIN * WORLD_SLIDER_SCALE));
      fSlider.setMaximum((int) (WORLD_MAX * WORLD_SLIDER_SCALE));
      fSlider.setValue((int) (fWorldScale * WORLD_SLIDER_SCALE));
      fSlider.setMajorTickSpacing(50000);
    }
    updateSliderLabel();
  }

  private void updateSliderLabel() {
    if (fPixelScaling) {
      fSliderLabel.setText("Scale: " + fPixelScale);
    } else {
      fSliderLabel.setText(fWorldScale / 1000 + " km");
    }
  }
}
