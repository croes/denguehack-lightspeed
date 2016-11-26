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
package samples.network.crosscountry.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import samples.gxy.common.TitledPanel;
import samples.network.crosscountry.graph.CrossCountryRasterGraphManager;
import samples.network.crosscountry.graph.EuclideanRasterFunctionFactory;

/**
 * GUI control panel for configuring the heuristic distance function.
 */
public class CrossCountryHeuristicFunctionControlPanel extends JPanel {

  private CrossCountryRasterGraphManager fRasterGraphManager;

  public CrossCountryHeuristicFunctionControlPanel(CrossCountryRasterGraphManager aRasterGraphManager) {
    fRasterGraphManager = aRasterGraphManager;

    JRadioButton distance_cartesian = new JRadioButton("Cartesian", true);
    distance_cartesian.setToolTipText("Computes a 2D cartesian distance");
    distance_cartesian.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fRasterGraphManager.setHeuristicEstimateFunction(new EuclideanRasterFunctionFactory());
        }
      }
    });
    distance_cartesian.setSelected(false);
    JRadioButton distance_constant = new JRadioButton("None");
    distance_constant.setToolTipText("Disables the heuristic function");
    distance_constant.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fRasterGraphManager.setHeuristicEstimateFunction(null);
        }
      }
    });
    distance_constant.setSelected(false);

    ButtonGroup bg = new ButtonGroup();
    bg.add(distance_cartesian);
    bg.add(distance_constant);
    bg.setSelected(bg.getElements().nextElement().getModel(), true);

    JPanel center_panel = new JPanel(new GridLayout(2, 1));
    center_panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    center_panel.add(distance_cartesian);
    center_panel.add(distance_constant);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Heuristic function", center_panel));
  }

}

