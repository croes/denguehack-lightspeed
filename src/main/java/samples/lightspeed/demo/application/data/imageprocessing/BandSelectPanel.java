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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import samples.common.OptionsPanelBuilder;

/**
 * Panel that allows selecting individual bands and common band
 * combinations for LandSat 7 imagery.
 */
class BandSelectPanel extends JPanel {

  private OperatorModelExtended fOperatorModel;

  private Map<AbstractButton, int[]> fPresetsMapping;

  private static final String[] BAND_TOOLTIPS = new String[]{
      "<html>Band 1: Blue.<br>Corresponds to 0.45-0.515 &micro;m.</html>",
      "<html>Band 2: Green.<br>Corresponds to 0.525-0.605 &micro;m.</html>",
      "<html>Band 3: Red.<br>Corresponds to 0.63-0.69 &micro;m.</html>",
      "<html>Band 4: Near Infrared.<br>Corresponds to 0.75-0.90 &micro;m.</html>",
      "<html>Band 5: Shortwave Infrared-1.<br>Corresponds to 1.55-1.75 &micro;m.</html>",
      "<html>Band 6: Thermal Infrared.<br>Corresponds to 10.4-12.5 &micro;m.</html>",
      "<html>Band 7: Shortwave Infrared-2.<br>Corresponds to 2.09-2.35 &micro;m.</html>"
  };

  public BandSelectPanel(OperatorModelExtended aAOperatorModel) {
    setLayout(new BorderLayout());
    fOperatorModel = aAOperatorModel;
    fPresetsMapping = new HashMap<AbstractButton, int[]>();

    JToolBar selectionBar = new JToolBar();
    selectionBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    selectionBar.setFloatable(false);

    ButtonGroup presetsGroup = new ButtonGroup();

    for (int i = 0; i < 7; i++) {
      final AbstractButton bandToggle = OptionsPanelBuilder.createUnderlinedButton(String.valueOf(i + 1));
      fPresetsMapping.put(bandToggle, new int[]{i});
      presetsGroup.add(bandToggle);
      selectionBar.add(bandToggle);
      bandToggle.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == bandToggle) {
            fOperatorModel.setNormalizedDifference(false);
            fOperatorModel.setSelectedBands(fPresetsMapping.get(bandToggle));
          }
        }
      });
      bandToggle.setToolTipText(BAND_TOOLTIPS[i]);
    }

    final AbstractButton trueColorToggle = OptionsPanelBuilder.createUnderlinedButton("TRUE");
    trueColorToggle.setToolTipText("<html>True or natural color.<br>" +
                                   "Selects bands 3, 2, and 1 which correspond to the visual bands.<br>" +
                                   "In this combination it is difficult to distinguish clouds and snow.</html>");
    final AbstractButton nirToggle = OptionsPanelBuilder.createUnderlinedButton("NIR");
    nirToggle.setToolTipText("<html>Near infrared, or the standard false color composite.<br>" +
                             "Selects bands 4, 3, and 2, where band 4 corresponds to the near infrared band.<br>" +
                             "In this combination, vegetation appears in shades of red.</html>");
    final AbstractButton swirToggle = OptionsPanelBuilder.createUnderlinedButton("SWIR");
    swirToggle.setToolTipText("<html>Short wavelength infrared or pseudo natural color.<br>" +
                              "Selects bands 7, 4, and 2.<br>" +
                              "Provides striking colors for desert areas.<br>" +
                              "In this combination " +
                              "regions that were recently burned appear bright red.<br>" +
                              "This combination also allows distinguishing" +
                              " clouds from snow.</html>");

    final AbstractButton ndviToggle = OptionsPanelBuilder.createUnderlinedButton("NDVI");
    ndviToggle.setToolTipText("<html>Normalized difference vegetation index. <br>Computes (NIR-RED)/(NIR+RED) or (band 4 - band 3)/(band 4 + band 3)<br>" +
                              "and visualizes the result by mapping the resulting [-1,1] range to a color map.<br>" +
                              "NDVI indicates the presence of live green vegetation (green shades).</html>");

    int[] trueColor = new int[]{2, 1, 0};
    fPresetsMapping.put(trueColorToggle, trueColor);
    int[] nearInfared = new int[]{3, 2, 1};
    fPresetsMapping.put(nirToggle, nearInfared);
    int[] swirColor = new int[]{6, 3, 1};
    fPresetsMapping.put(swirToggle, swirColor);

    presetsGroup.add(trueColorToggle);
    trueColorToggle.setSelected(true);
    fOperatorModel.setSelectedBands(fPresetsMapping.get(trueColorToggle));
    presetsGroup.add(nirToggle);
    presetsGroup.add(swirToggle);
    presetsGroup.add(ndviToggle);

    selectionBar.addSeparator();

    selectionBar.add(trueColorToggle);
    selectionBar.add(nirToggle);
    selectionBar.add(swirToggle);
    selectionBar.add(ndviToggle);
    selectionBar.setBorderPainted(false);

    trueColorToggle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == trueColorToggle) {
          fOperatorModel.setNormalizedDifference(false);
          fOperatorModel.setSelectedBands(fPresetsMapping.get(trueColorToggle));
        }
      }
    });

    nirToggle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nirToggle) {
          fOperatorModel.setNormalizedDifference(false);
          fOperatorModel.setSelectedBands(fPresetsMapping.get(nirToggle));
        }
      }
    });

    swirToggle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == swirToggle) {
          fOperatorModel.setNormalizedDifference(false);
          fOperatorModel.setSelectedBands(fPresetsMapping.get(swirToggle));
        }
      }
    });

    ndviToggle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fOperatorModel.resetCurves();
        fOperatorModel.setNormalizedDifference(ndviToggle.isSelected());
      }
    });

    add(selectionBar, BorderLayout.CENTER);
  }

}
