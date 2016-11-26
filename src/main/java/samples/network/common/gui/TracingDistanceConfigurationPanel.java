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
package samples.network.common.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.network.common.graph.GraphManager;

/**
 * GUI control panel for configuring tracing settings.
 */
public class TracingDistanceConfigurationPanel extends JPanel {

  private GraphManager fGraphManager;
  private JSlider fDistanceSlider;

  public TracingDistanceConfigurationPanel(GraphManager aGraphManager, int aMin, int aMax, int aDefault) {
    fGraphManager = aGraphManager;
    fGraphManager.setMaximumTracingDistance(aDefault);

    int range = aMax - aMin;
    fDistanceSlider = new JSlider(aMin, aMax, aDefault);
    fDistanceSlider.setMajorTickSpacing(range / 5);
    fDistanceSlider.setMinorTickSpacing(range / 10);
    fDistanceSlider.setPaintTicks(true);
    fDistanceSlider.setPaintLabels(true);
    fDistanceSlider.setSnapToTicks(false);
    fDistanceSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        fGraphManager.setMaximumTracingDistance(source.getValue());
      }
    });

    JPanel contentPanel = new JPanel(new GridLayout(1, 1));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    contentPanel.add(fDistanceSlider);

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tracing Distance"));
    add(contentPanel);
    setPreferredSize(new Dimension(350, 100));
  }

  public void setMaxDistance(int aDistance) {
    fDistanceSlider.setMaximum(aDistance);
    fDistanceSlider.setLabelTable(null);
    fDistanceSlider.setMajorTickSpacing(aDistance / 2);

    if (aDistance < 20) {
      fDistanceSlider.setMinorTickSpacing(1);
    } else if (aDistance < 60) {
      fDistanceSlider.setMinorTickSpacing(5);
    } else {
      fDistanceSlider.setMinorTickSpacing(10);
    }
    fDistanceSlider.setValue(aDistance / 2);
  }

}

