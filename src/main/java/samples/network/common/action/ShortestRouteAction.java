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
package samples.network.common.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.network.algorithm.routing.ILcdShortestRouteAlgorithm;

import samples.network.common.graph.GraphManager;

/**
 * This action calculates, prints and selects the shortest path between 2 vertices in a graph.
 */
public class ShortestRouteAction extends ALcdAction {

  private static final String NAME_SIMPLE = "Calculate the shortest route";
  private static final String NAME_PARTITIONED = "Calculate the shortest route (partitioned)";

  private static final ILcdIcon ICON_SIMPLE = new TLcdImageIcon("samples/images/shortest_route.png");
  private static final ILcdIcon ICON_PARTITIONED = new TLcdImageIcon("samples/images/shortest_route_partitioned.png");

  public static enum Type {
    SIMPLE, PARTITIONED
  }

  private Component fParentFrame;

  private GraphManager fGraphManager;
  private ILcdShortestRouteAlgorithm fShortestRouteAlgorithm;

  /**
   * Constructs a new ShortestRouteAction.
   */
  public ShortestRouteAction(Component aParentFrame,
                             GraphManager aGraphManager,
                             ILcdShortestRouteAlgorithm aILcdShortestRouteAlgorithm,
                             Type aType) {
    fParentFrame = aParentFrame;
    fGraphManager = aGraphManager;
    fShortestRouteAlgorithm = aILcdShortestRouteAlgorithm;
    setName(aType == Type.SIMPLE ? NAME_SIMPLE : NAME_PARTITIONED);
    setShortDescription(aType == Type.SIMPLE ? NAME_SIMPLE : NAME_PARTITIONED);
    setIcon(aType == Type.SIMPLE ? ICON_SIMPLE : ICON_PARTITIONED);
  }

  public void actionPerformed(ActionEvent aEvent) {

    fGraphManager.setShortestRouteAlgorithm(fShortestRouteAlgorithm);

    if (fGraphManager.getStartNode() == null || fGraphManager.getEndNode() == null) {
      JOptionPane.showMessageDialog(fParentFrame, "Please select a start and end node before routing.");
      return;
    }
    fGraphManager.computeShortestRoute();
    if (fGraphManager.getRoutes() == null) {
      JOptionPane.showMessageDialog(fParentFrame, "No route found.");
    }
  }

}
