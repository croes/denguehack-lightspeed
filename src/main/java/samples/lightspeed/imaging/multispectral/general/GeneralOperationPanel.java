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
package samples.lightspeed.imaging.multispectral.general;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.common.OptionsPanelBuilder;

import samples.lightspeed.imaging.multispectral.OperatorModel;

/**
 * Panel that allows adjusting the contrast, brightness, opacity and sharpness of the
 * image of the currently selected layer.
 */
public class GeneralOperationPanel extends JPanel {

  private OperatorModel fOperatorModel;
  private final JSlider fSharpeningSlider;
  private final JSlider fContrastSlider;
  private final JSlider fBrightnessSlider;
  private final JSlider fOpacitySlider;

  /**
   * Create a new general options panel with a supplied filter model.
   *
   * @param aOperatorModel the filter model to be used.
   */
  public GeneralOperationPanel(final OperatorModel aOperatorModel) {
    setLayout(new GridLayout(0, 2, 0, 0));
    fOperatorModel = aOperatorModel;
    JLabel lblSharpening = new JLabel("Sharpening");
    JLabel lblContrast = new JLabel("Contrast");
    JLabel lblBrightness = new JLabel("Brightness");
    JLabel lblOpacity = new JLabel("Opacity");

    //slider to adjust sharpening
    fSharpeningSlider = new JSlider(0, 100, 1);

    fSharpeningSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        //set new sharpening value on filter model when slider is used.
        fOperatorModel.setSharpening(fSharpeningSlider.getValue() / 100.0);
      }
    });

    //slider to adjust contrast slider.
    fContrastSlider = new JSlider(0, 200, 1);

    fContrastSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        //set new contrast value on filter model when slider is used.
        fOperatorModel.setContrast(fContrastSlider.getValue() / 100.0f);
      }
    });

    //slider to adjust brightness
    fBrightnessSlider = new JSlider(0, 200, 1);

    fBrightnessSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        //set new brightness value on filter model when slider is used.
        fOperatorModel.setBrightness(fBrightnessSlider.getValue() / 100.0f);
      }
    });

    //slider to adjust opacity
    fOpacitySlider = new JSlider(0, 100, 1);

    fOpacitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        //set new opacity value on filter model when slider is used.
        fOperatorModel.setOpacity(fOpacitySlider.getValue() / 100.0f);
      }
    });

    //reset button to restore default values of all sliders.
    final AbstractButton reset = OptionsPanelBuilder.createUnderlinedButton("Reset");
    reset.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        reset.setSelected(false);
        fOperatorModel.resetGeneralParameters();
        fSharpeningSlider.setValue((int) (fOperatorModel.getSharpening() * 100));
        fContrastSlider.setValue((int) (fOperatorModel.getContrast() * 100));
        fBrightnessSlider.setValue((int) (fOperatorModel.getBrightness() * 100));
        fOpacitySlider.setValue((int) (fOperatorModel.getOpacity() * 100));
      }
    });

    add(lblContrast);
    add(fContrastSlider);
    add(lblBrightness);
    add(fBrightnessSlider);
    add(lblOpacity);
    add(fOpacitySlider);
    add(lblSharpening);
    add(fSharpeningSlider);

    fContrastSlider.setValue((int) fOperatorModel.getContrast());
    fOpacitySlider.setValue((int) fOperatorModel.getOpacity());
    fBrightnessSlider.setValue((int) fOperatorModel.getBrightness());
    fSharpeningSlider.setValue((int) fOperatorModel.getSharpening());

    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    leftPanel.setOpaque(false);
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setOpaque(false);
    rightPanel.add(reset);
    add(leftPanel);
    add(rightPanel);

    setValuesFromFilterModel(aOperatorModel);
  }

  public void setOperatorModel(OperatorModel aOperatorModel) {
    fOperatorModel = aOperatorModel;
    setValuesFromFilterModel(aOperatorModel);
  }

  private void setValuesFromFilterModel(OperatorModel aOperatorModel) {
    fSharpeningSlider.setValue((int) (aOperatorModel.getSharpening() * 100));
    fContrastSlider.setValue((int) (aOperatorModel.getContrast() * 100));
    fBrightnessSlider.setValue((int) (aOperatorModel.getBrightness() * 100));
    fOpacitySlider.setValue((int) (aOperatorModel.getOpacity() * 100));
  }
}
