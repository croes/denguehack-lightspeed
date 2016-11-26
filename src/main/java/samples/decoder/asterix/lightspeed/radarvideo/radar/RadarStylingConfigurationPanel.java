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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * Panel with GUI components to configure the radar visualization.
 */
public class RadarStylingConfigurationPanel extends JPanel {

  final private RadarStyleProperties fRadarProperties;

  public RadarStylingConfigurationPanel(RadarStyleProperties aRadarProperties) {
    fRadarProperties = aRadarProperties;
    setOpaque(false);

    FormLayout layout = new FormLayout("fill:pref:grow", "pref, 10px, pref, 10px, pref");
    setLayout(layout);
    CellConstraints cc = new CellConstraints();

    add(createColorPanel(), cc.xy(1, 1));
    add(createBlipPanel(), cc.xy(1, 3));
    add(createEnableGridPanel(), cc.xy(1, 5));
  }

  private JPanel createEnableGridPanel() {
    final JCheckBox gridCheckBox = new JCheckBox("Grid enabled");
    gridCheckBox.setSelected(fRadarProperties.isGridEnabled());
    gridCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRadarProperties.setGridEnabled(gridCheckBox.isSelected());
      }
    });

    final JCheckBox sweepCheckBox = new JCheckBox("Sweep line enabled");
    sweepCheckBox.setSelected(fRadarProperties.isSweepLineEnabled());
    sweepCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRadarProperties.setSweepLineEnabled(sweepCheckBox.isSelected());
      }
    });

    FormLayout layout = new FormLayout("left:pref", "pref, 2px, pref");
    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(layout);
    panel.add(gridCheckBox, cc.xy(1, 1));
    panel.add(sweepCheckBox, cc.xy(1, 3));
    panel.setOpaque(false);
    return panel;
  }

  private JPanel createColorPanel() {
    JButton backGroundColorButton = createColorChooserButton("Choose background color", fRadarProperties.getBackgroundColor(), new ColorFunction() {
      @Override
      public void applyColor(Color aChosenColor) {
        fRadarProperties.setBackgroundColor(aChosenColor);
      }
    });
    JButton blipColorButton = createColorChooserButton("Choose blip color", fRadarProperties.getBlipColor(), new ColorFunction() {
      @Override
      public void applyColor(Color aChosenColor) {
        fRadarProperties.setBlipColor(aChosenColor);
      }
    });
    JButton afterglowColorButton = createColorChooserButton("Choose afterglow color", fRadarProperties.getAfterglowColor(), new ColorFunction() {
      @Override
      public void applyColor(Color aChosenColor) {
        fRadarProperties.setAfterglowColor(aChosenColor);
      }
    });
    JButton gridColorButton = createColorChooserButton("Choose grid color", fRadarProperties.getGridColor(), new ColorFunction() {
      @Override
      public void applyColor(Color aChosenColor) {
        fRadarProperties.setGridColor(aChosenColor);
      }
    });
    JButton sweepColorButton = createColorChooserButton("Choose sweep line color", fRadarProperties.getSweepLineColor(), new ColorFunction() {
      @Override
      public void applyColor(Color aChosenColor) {
        fRadarProperties.setSweepLineColor(aChosenColor);
      }
    });

    FormLayout layout = new FormLayout("left:pref, 10px, left:pref, 10px, left:pref, 10px, left:pref, 10px, left:pref", "pref");
    CellConstraints cc = new CellConstraints();

    JPanel colorPanel = new JPanel(layout);
    colorPanel.add(createButtonWithLabel(backGroundColorButton, "Fill"), cc.xy(1, 1));
    colorPanel.add(createButtonWithLabel(gridColorButton, "Grid"), cc.xy(3, 1));
    colorPanel.add(createButtonWithLabel(sweepColorButton, "Sweep"), cc.xy(5, 1));
    colorPanel.add(createButtonWithLabel(blipColorButton, "Blip"), cc.xy(7, 1));
    colorPanel.add(createButtonWithLabel(afterglowColorButton, "Afterglow"), cc.xy(9, 1));
    colorPanel.setOpaque(false);
    return colorPanel;
  }

  private static JButton createColorChooserButton(final String aLabel, final Color aIntialColor, final ColorFunction aApplyColorFunction) {
    JButton colorButton = new JButton();
    final TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 20, aIntialColor, aIntialColor);
    final TLcdSWIcon icon = new TLcdSWIcon(symbol);
    colorButton.setToolTipText(aLabel);
    colorButton.setIcon(icon);
    colorButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Color c = JColorChooser.showDialog(TLcdAWTUtil.findParentWindow(e), aLabel, aIntialColor);
        if (c != null) {
          aApplyColorFunction.applyColor(c);
          symbol.setFillColor(c);
          symbol.setBorderColor(c);
        }
      }
    });
    return colorButton;
  }

  private JPanel createBlipPanel() {
    final DecimalFormat glowFormat = new DecimalFormat("#.##");
    glowFormat.setPositiveSuffix("s");
    final DecimalFormat intensityFormat = new DecimalFormat("#.##");
    final DecimalFormat thresholdFormat = new DecimalFormat("#.##");
    thresholdFormat.setPositiveSuffix("%");

    String afterGlowToolTip = "Set the blip afterglow duration. Increasing this lets blips persist longer on the screen.";
    String intensityToolTip = "Set the blip intensity. Increasing this makes blips with a low amplitude stand out more.";
    String threshHoldToolTip = "Set the amplitude threshold. Increasing this removes unwanted noise from the radar feed.";

    JLabel labelAfterGlow = new JLabel("Afterglow");
    labelAfterGlow.setToolTipText(afterGlowToolTip);
    JLabel labelIntensity = new JLabel("Intensity");
    labelIntensity.setToolTipText(intensityToolTip);
    JLabel labelThreshHold = new JLabel("Threshold");
    labelThreshHold.setToolTipText(threshHoldToolTip);

    final JLabel labelGlowSlider = new JLabel();
    labelGlowSlider.setText(glowFormat.format(fRadarProperties.getBlipAfterglowDuration()));
    final JLabel labelIntensitySlider = new JLabel();
    labelIntensitySlider.setText(intensityFormat.format(fRadarProperties.getIntensity()));
    final JLabel labelThresholdSlider = new JLabel();
    labelThresholdSlider.setText(thresholdFormat.format(fRadarProperties.getThreshold() * 100));

    final JSlider blipAfterGlowSlider = createSlider(1, 90, 150);
    blipAfterGlowSlider.setToolTipText(afterGlowToolTip);
    blipAfterGlowSlider.setValue((int) (fRadarProperties.getBlipAfterglowDuration()));
    blipAfterGlowSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int value = blipAfterGlowSlider.getValue();
        fRadarProperties.setBlipAfterglowDuration(value);
        labelGlowSlider.setText(glowFormat.format(value));
      }
    });

    final JSlider intensitySlider = createSlider(1, 80, 150);
    intensitySlider.setToolTipText(intensityToolTip);
    intensitySlider.setValue((int) (fRadarProperties.getIntensity() * 10));
    intensitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        double value = intensitySlider.getValue() * 0.1;
        fRadarProperties.setIntensity(value);
        labelIntensitySlider.setText(intensityFormat.format(value));
      }
    });

    final JSlider thresholdSlider = createSlider(1, 100, 150);
    thresholdSlider.setSize(new Dimension(40, 20));
    thresholdSlider.setToolTipText(threshHoldToolTip);
    thresholdSlider.setValue((int) (fRadarProperties.getThreshold() * 100));
    thresholdSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int value = thresholdSlider.getValue();
        fRadarProperties.setThreshold(value * 0.01);
        labelThresholdSlider.setText(thresholdFormat.format(value));
      }
    });

    FormLayout layout = new FormLayout("left:pref, 5px, fill:pref:grow, 5px, left:pref", "pref, 2px, pref, 2px, pref");
    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(layout);
    panel.add(labelAfterGlow, cc.xy(1, 1));
    panel.add(blipAfterGlowSlider, cc.xy(3, 1));
    panel.add(labelGlowSlider, cc.xy(5, 1));

    panel.add(labelIntensity, cc.xy(1, 3));
    panel.add(intensitySlider, cc.xy(3, 3));
    panel.add(labelIntensitySlider, cc.xy(5, 3));

    panel.add(labelThreshHold, cc.xy(1, 5));
    panel.add(thresholdSlider, cc.xy(3, 5));
    panel.add(labelThresholdSlider, cc.xy(5, 5));

    panel.setOpaque(false);
    return panel;
  }

  private JSlider createSlider(int aMin, int aMax, final int aWidth) {
    return new JSlider(aMin, aMax) {
      public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = Math.min(aWidth, pref.width);
        return pref;
      }
    };
  }

  private JPanel createButtonWithLabel(JButton aButton, String aLabel) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(aButton, BorderLayout.NORTH);
    JLabel label = new JLabel(aLabel);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(label, BorderLayout.SOUTH);
    panel.setOpaque(false);
    return panel;
  }

  private interface ColorFunction {
    void applyColor(Color aChosenColor);
  }
}
