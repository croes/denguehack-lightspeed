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

import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.network.algorithm.routing.TLcdPartitionedShortestRouteAlgorithm;
import com.luciad.network.algorithm.routing.TLcdShortestRouteAlgorithm;
import com.luciad.network.algorithm.routing.TLcdTracingAlgorithm;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.common.toolbar.ToolBar;
import samples.network.common.action.ShortestRouteAction;
import samples.network.common.action.ShowDialogAction;
import samples.network.common.action.TraceAction;
import samples.network.common.controller.RouteElementSelectController;
import samples.network.common.graph.GraphManager;
import samples.network.common.graph.GraphParameterChangedListener;

/**
 * Extension of the default sample toolbar adding extra functionality for network analysis.
 */
public class NetworkToolbar extends ToolBar {

  private static final ILcdIcon ICON_SIMPLE = new TLcdImageIcon("samples/images/tool.png");

  private RouteElementSelectController fSetStartNodeController;
  private RouteElementSelectController fSetStartEdgeController;
  private RouteElementSelectController fSetEndNodeController;
  private RouteElementSelectController fSetEndEdgeController;

  private ShortestRouteAction fRouteAction;
  private ShortestRouteAction fPartitionedRouteAction;

  private TraceAction fForwardTraceAction;
  private TraceAction fBackwardTraceAction;

  private ShowDialogAction fShowRouteInfoPanelAction;

  private ShowDialogAction fShowConfigurationPanelAction;

  private MultiRouteInfoPanel fRouteInfoPanel;

  public NetworkToolbar(ILcdGXYView aGXYView,
                        GraphManager aGraphManager,
                        boolean aIsStandAlone,
                        Component aParent) {
    this(aGXYView, aGraphManager, aIsStandAlone, aParent, new DefaultNameProvider(), new DefaultNameProvider());
  }

  public NetworkToolbar(ILcdGXYView aGXYView,
                        final GraphManager aGraphManager,
                        boolean aIsStandAlone,
                        Component aParent,
                        INameProvider aEdgeNameProvider,
                        INameProvider aNodeNameProvider) {

    super(aGXYView, aIsStandAlone, aParent);

    // ---------- Create all actions ----------

    // Graph selection controllers
    fSetStartNodeController = new RouteElementSelectController(aGraphManager,
                                                               RouteElementSelectController.Mode.START_NODE);
    fSetEndNodeController = new RouteElementSelectController(aGraphManager,
                                                             RouteElementSelectController.Mode.END_NODE);
    fSetStartEdgeController = new RouteElementSelectController(aGraphManager,
                                                               RouteElementSelectController.Mode.START_EDGE);
    fSetEndEdgeController = new RouteElementSelectController(aGraphManager,
                                                             RouteElementSelectController.Mode.END_EDGE);

    // Routing algorithms
    fRouteAction = new ShortestRouteAction(aParent,
                                           aGraphManager,
                                           new TLcdShortestRouteAlgorithm(),
                                           ShortestRouteAction.Type.SIMPLE);
    fPartitionedRouteAction = new ShortestRouteAction(aParent,
                                                      aGraphManager,
                                                      new TLcdPartitionedShortestRouteAlgorithm(),
                                                      ShortestRouteAction.Type.PARTITIONED);

    // Tracing algorithms
    fForwardTraceAction = new TraceAction(aParent,
                                          aGraphManager,
                                          new TLcdTracingAlgorithm(),
                                          TraceAction.Type.FORWARD);
    fBackwardTraceAction = new TraceAction(aParent,
                                           aGraphManager,
                                           new TLcdTracingAlgorithm(),
                                           TraceAction.Type.BACKWARD);
    fRouteInfoPanel = new MultiRouteInfoPanel(aGraphManager, aEdgeNameProvider, aNodeNameProvider);
    fShowRouteInfoPanelAction = new ShowDialogAction(fRouteInfoPanel,
                                                     "Routing information",
                                                     "Show routing information",
                                                     TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON));

    // Network configuration
    fShowConfigurationPanelAction = new ShowDialogAction(new TracingDistanceConfigurationPanel(aGraphManager, 0, 5000, 2000),
                                                         "Network configuration",
                                                         "Show network configuration",
                                                         ICON_SIMPLE);

    // ---------- Add actions to toolbar  ----------

    addGXYController(fSetStartNodeController);
    addGXYController(fSetEndNodeController);
    addGXYController(fSetStartEdgeController);
    addGXYController(fSetEndEdgeController);
    addSpace();
    addAction(fRouteAction);
    addAction(fPartitionedRouteAction);
    addSpace();
    addAction(fForwardTraceAction);
    addAction(fBackwardTraceAction);
    addSpace();
    addAction(fShowRouteInfoPanelAction);
    addSpace();
    addAction(fShowConfigurationPanelAction);

    // ---------- Register handlers on actions  ----------

    aGraphManager.addGraphParameterChangedListener(new GraphParameterChangedListener() {
      public void graphParameterChanged(GraphParameter aParameter, Object aOldValue, Object aNewValue) {
        if (aParameter == GraphParameter.ROUTES) {
          fRouteInfoPanel.setRoutes((List<ILcdRoute>) aNewValue,
                                    aNewValue != null ? aGraphManager.getRouteDistances() : null,
                                    aGraphManager.getRouteType() == GraphManager.RouteType.SHORTEST_ROUTE ||
                                    aGraphManager.getRouteType() == GraphManager.RouteType.FORWARD_TRACE);

        }
      }
    });
  }

  /**
   * Sets the name provider to be used for retrieving textual representations of edges.
   *
   * @param aEdgeNameProvider the name provider to be used for retrieving textual representations of edges.
   */
  public void setEdgeNameProvider(INameProvider aEdgeNameProvider) {
    fRouteInfoPanel.setEdgeNameProvider(aEdgeNameProvider);
  }

  /**
   * Sets the name provider to be used for retrieving textual representations of nodes.
   *
   * @param aNodeNameProvider the name provider to be used for retrieving textual representations of nodes.
   */
  public void setNodeNameProvider(INameProvider aNodeNameProvider) {
    fRouteInfoPanel.setNodeNameProvider(aNodeNameProvider);
  }

  /**
   * Sets the JComponent containing the configuration options for the network.
   * If {@code null}, the network configuration button will be disabled.
   *
   * @param aComponent the JComponent containing the configuration options for the network.
   */
  public void setNetworkConfigurationPanel(JComponent aComponent) {
    fShowConfigurationPanelAction.setContent(aComponent);
  }

  public ShortestRouteAction getPartitionedRouteAction() {
    return fPartitionedRouteAction;
  }

  public ShortestRouteAction getRouteAction() {
    return fRouteAction;
  }

  public TraceAction getBackwardTraceAction() {
    return fBackwardTraceAction;
  }

  public TraceAction getForwardTraceAction() {
    return fForwardTraceAction;
  }

  /**
   * Default implementation returing toString().
   */
  private static class DefaultNameProvider implements INameProvider {

    // Implementations for INameProvider.

    public String getName(Object aObject) {
      return aObject.toString();
    }
  }

}
