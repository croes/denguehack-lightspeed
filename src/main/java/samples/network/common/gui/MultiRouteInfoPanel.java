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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.network.graph.route.ILcdRoute;

import samples.network.common.graph.GraphManager;

/**
 * GUI panel displaying information (traced nodes and their route info) of a tracing result.
 */
public class MultiRouteInfoPanel extends JPanel {

  private static final String TRACED_NODES = "End nodes: ";

  private JComboBox fTraceChooser = new JComboBox(new DefaultComboBoxModel());
  private RouteInfoPanel fRoutingInformationPanel;
  private INameProvider fNodeNameProvider;

  public MultiRouteInfoPanel(GraphManager aGraphManager,
                             INameProvider aEdgeNameProvider,
                             INameProvider aNodeNameProvider) {
    fRoutingInformationPanel = new RouteInfoPanel(aGraphManager, aEdgeNameProvider);
    fNodeNameProvider = aNodeNameProvider;
    buildGUI();
    fTraceChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        Route route = (Route) cb.getSelectedItem();
        if (route != null) {
          fRoutingInformationPanel.setRoute(route.getRoute());
        } else {
          fRoutingInformationPanel.setRoute(null);
        }
      }
    });
  }

  public void setNodeNameProvider(INameProvider aNodeNameProvider) {
    fNodeNameProvider = aNodeNameProvider;
  }

  public void setEdgeNameProvider(INameProvider aEdgeNameProvider) {
    fRoutingInformationPanel.setEdgeNameProvider(aEdgeNameProvider);
  }

  public void setRoutes(List<ILcdRoute> aRoutes,
                        Map<ILcdRoute, Double> aDistances,
                        boolean aForward) {
    clearRoutes();
    if (aRoutes != null) {
      for (ILcdRoute trace : aRoutes) {
        addRoute(aForward ? trace.getEndNode() : trace.getStartNode(), trace, aDistances.get(trace));
      }
    }
  }

  public void clearRoutes() {
    ((DefaultComboBoxModel) fTraceChooser.getModel()).removeAllElements();
  }

  public void addRoute(Object aNode, ILcdRoute aShortestRoute, double aDistance) {
    ((DefaultComboBoxModel) fTraceChooser.getModel()).addElement(new Route(aDistance, aNode, aShortestRoute));
  }

  private void buildGUI() {
    setPreferredSize(new Dimension(400, 400));

    setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 5);
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.NONE;

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.weighty = 0;
    add(new JLabel(TRACED_NODES), c);

    c.gridx = 1;
    c.weightx = 1;
    add(fTraceChooser, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    c.weighty = 1;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    add(fRoutingInformationPanel, c);
  }

  private class Route {

    private Object fNode;
    private ILcdRoute fRoute;
    private double fDistance;

    private Route(double aDistance, Object aNode, ILcdRoute aRoute) {
      fDistance = aDistance;
      fNode = aNode;
      fRoute = aRoute;
    }

    public ILcdRoute getRoute() {
      return fRoute;
    }

    @Override
    public String toString() {
      return fNodeNameProvider.getName(fNode) + " (" + fDistance + ")";
    }
  }
}
