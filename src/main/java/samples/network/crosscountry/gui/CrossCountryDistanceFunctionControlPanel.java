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

import com.luciad.network.function.ALcdCrossCountryRasterDistanceFunction;

import samples.gxy.common.TitledPanel;
import samples.network.crosscountry.graph.CrossCountryRasterGraphManager;
import samples.network.crosscountry.graph.EuclideanRasterFunctionFactory;
import samples.network.crosscountry.graph.SlopeRasterEdgeValueFunctionFactory;

/**
 * GUI control panel for configuring the distance function.
 */
public class CrossCountryDistanceFunctionControlPanel extends JPanel {

  private CrossCountryRasterGraphManager fRasterGraphManager;

  public CrossCountryDistanceFunctionControlPanel(CrossCountryRasterGraphManager aRasterGraphManager) {
    fRasterGraphManager = aRasterGraphManager;

    JRadioButton edge_cartesian = new JRadioButton("Cartesian", true);
    edge_cartesian.setToolTipText("Computes a 2D cartesian distance");
    edge_cartesian.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fRasterGraphManager.setEdgeValueFunctionFactory(new EuclideanRasterFunctionFactory());
        }
      }
    });
    edge_cartesian.setSelected(false);
    JRadioButton edge_slope = new JRadioButton("Slope");
    edge_slope.setToolTipText("Computes a time cost where the time increases with the 3D cartesian distance and slope");
    edge_slope.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fRasterGraphManager.setEdgeValueFunctionFactory(
              new SlopeRasterEdgeValueFunctionFactory(
                  ALcdCrossCountryRasterDistanceFunction.ComputationMode.PRECISE
              )
                                                         );
        }
      }
    });
    edge_slope.setSelected(false);
    JRadioButton edge_slope_approx = new JRadioButton("Slope (approx.)");
    edge_slope_approx.setToolTipText("Computes a time cost where the time increases with the distance and slope");
    edge_slope_approx.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fRasterGraphManager.setEdgeValueFunctionFactory(
              new SlopeRasterEdgeValueFunctionFactory(
                  ALcdCrossCountryRasterDistanceFunction.ComputationMode.APPROXIMATE
              )
                                                         );
        }
      }
    });
    edge_slope_approx.setSelected(false);

    ButtonGroup bg = new ButtonGroup();
    bg.add(edge_cartesian);
    bg.add(edge_slope);
    bg.add(edge_slope_approx);
    edge_slope.setSelected(true);

    JPanel center_panel = new JPanel(new GridLayout(3, 1));
    center_panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    center_panel.add(edge_cartesian);
    center_panel.add(edge_slope);
    center_panel.add(edge_slope_approx);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Distance function", center_panel));
  }

}

