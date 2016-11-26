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
import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import samples.gxy.common.TitledPanel;

/**
 * GUI information panel, showing routing results.
 */
public class RouteInfoPanel extends JPanel {

  private static final String STATUS_TEXT = "Status: ";
  private static final String EDGES_TEXT = "Edges: ";
  private static final String DISTANCE_TEXT = "Distance: ";

  private JLabel fStatusLabel = new JLabel();
  private JLabel fEdgesLabel = new JLabel();
  private JLabel fDistanceLabel = new JLabel();

  public RouteInfoPanel() {
    fStatusLabel.setText(STATUS_TEXT);
    fEdgesLabel.setText(EDGES_TEXT);
    fDistanceLabel.setText(DISTANCE_TEXT);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(fStatusLabel);
    panel.add(Box.createVerticalStrut(5));
    panel.add(fEdgesLabel);
    panel.add(Box.createVerticalStrut(5));
    panel.add(fDistanceLabel);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Info", panel));
  }

  public void setStatusComputingRoute() {
    fStatusLabel.setForeground(Color.BLACK);
    fStatusLabel.setText(STATUS_TEXT + "Computing route...");
    fEdgesLabel.setText(EDGES_TEXT);
    fDistanceLabel.setText(DISTANCE_TEXT);
  }

  public void setStatusComputedRoute(int aEdgeCount, double aDistance, long aTime) {
    fStatusLabel.setForeground(Color.BLACK);
    fStatusLabel.setText(STATUS_TEXT + "Computed route (" + formatTime(aTime) + ")");
    fEdgesLabel.setText(EDGES_TEXT + aEdgeCount);
    fDistanceLabel.setText(DISTANCE_TEXT + String.format("%.3f", aDistance));
  }

  private String formatTime(long aTime) {
    int seconds = (int) Math.round(aTime / 1000.0);
    if (seconds == 0) {
      return "< 1 s";
    } else {
      return seconds + " s";
    }
  }

  public void setStatusComputedNoRoute() {
    fStatusLabel.setForeground(Color.BLACK);
    fStatusLabel.setText(STATUS_TEXT + "No route");
    fEdgesLabel.setText(EDGES_TEXT);
    fDistanceLabel.setText(DISTANCE_TEXT);
  }

}
