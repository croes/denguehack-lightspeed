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

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.network.graph.route.TLcdRouteUtil;

import samples.network.common.graph.GraphManager;

/**
 * GUI panel displaying information (edges, route length) of a shortest route that is configured on it.
 */
public class RouteInfoPanel extends JPanel {

  private static final String ROUTE_FIELD = "Route: ";
  private static final String DISTANCE_FIELD = "Distance: ";
  private static final String INFINITE_DISTANCE = "infinite. ";

  private GraphManager fGraphManager;

  private JLabel fDistance = new JLabel("  ");
  private JList fRouteEdgeList = new JList(new DefaultListModel());
  private INameProvider fEdgeNameProvider;

  public RouteInfoPanel(GraphManager aGraphManager,
                        INameProvider aEdgeNameProvider) {
    fGraphManager = aGraphManager;
    fEdgeNameProvider = aEdgeNameProvider;
    buildGUI();
  }

  public void setEdgeNameProvider(INameProvider aEdgeNameProvider) {
    fEdgeNameProvider = aEdgeNameProvider;
  }

  /**
   * Sets the route to be displayed by this information panel. May be {@code null}.
   *
   * @param aRoute the route to be displayed by this information panel.
   */
  public void setRoute(ILcdRoute aRoute) {
    final DefaultListModel listModel = (DefaultListModel) fRouteEdgeList.getModel();
    listModel.clear();
    if (aRoute == null) {
      fDistance.setText(INFINITE_DISTANCE);
    } else {
      double distance = TLcdRouteUtil.computeValue(aRoute, fGraphManager.getGraph(), null, fGraphManager.getEdgeValueFunction());
      fDistance.setText("" + distance);

      listModel.clear();
      for (int i = 0; i < aRoute.getEdgeCount(); i++) {
        listModel.addElement(fEdgeNameProvider.getName(aRoute.getEdge(i)));
      }
    }
  }

  private void buildGUI() {
    setPreferredSize(new Dimension(200, 300));

    JScrollPane routeScrollPane = new JScrollPane(fRouteEdgeList);
    routeScrollPane.setAlignmentX(LEFT_ALIGNMENT);
    routeScrollPane.setAlignmentY(TOP_ALIGNMENT);

    setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 5);
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.NONE;

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.weighty = 0;
    add(new JLabel(DISTANCE_FIELD), c);

    c.gridx = 1;
    c.weightx = 1;
    add(fDistance, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    c.weighty = 1;
    add(new JLabel(ROUTE_FIELD), c);

    c.gridx = 1;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    add(routeScrollPane, c);
  }

}
