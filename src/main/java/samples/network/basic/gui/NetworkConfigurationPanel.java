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
package samples.network.basic.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import samples.network.basic.function.CartesianDistanceFunction;
import samples.network.basic.function.SampleCompositeFunction;
import samples.network.basic.function.TimeCostDistanceFunction;
import samples.network.common.graph.GraphManager;
import samples.network.common.gui.TracingDistanceConfigurationPanel;

/**
 * GUI panel with configuration parameters for the {@code SampleCompositeFunction} edge value function.
 */
public class NetworkConfigurationPanel extends JPanel {

  private static final String CARTESIAN = "Cartesian";
  private static final String TIME_COST = "Time cost";
  private static final String CONSTANT = "Constant (1)";
  private static final String NONE = "None";

  private static final String ENABLED = "Enabled";
  private static final String DISABLED = "Disabled";

  private GraphManager fGraphManager;
  private SampleCompositeFunction fEdgeValueFunction;
  private TracingDistanceConfigurationPanel fTracingDistanceControlPanel;

  public NetworkConfigurationPanel(GraphManager aGraphManager,
                                   SampleCompositeFunction aSampleCompositeFunction) {
    fGraphManager = aGraphManager;
    fEdgeValueFunction = aSampleCompositeFunction;

    fTracingDistanceControlPanel = new TracingDistanceConfigurationPanel(aGraphManager, 0, 120, 60);
    JPanel tracingPanel = new JPanel();
    tracingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tracing"));
    tracingPanel.add(fTracingDistanceControlPanel);

    JPanel routingPanel = new JPanel();
    routingPanel.setLayout(new GridLayout());
    routingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Routing"));
    routingPanel.add(new EdgeValueFunctionControlPanel());
    routingPanel.add(new NodeValueFunctionControlPanel());
    routingPanel.add(new TurnValueFunctionControlPanel());
    routingPanel.add(new DistanceFunctionControlPanel());

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(routingPanel);
    add(tracingPanel);
  }

  class EdgeValueFunctionControlPanel extends OptionPanel {

    public EdgeValueFunctionControlPanel() {
      super("Edge value", new String[]{CARTESIAN, TIME_COST, CONSTANT});
    }

    public void selected(String aText) {
      if (aText.equals(CARTESIAN)) {
        fEdgeValueFunction.setEdgeFunctionMode(SampleCompositeFunction.CARTESIAN);
        fTracingDistanceControlPanel.setMaxDistance(120);
      } else if (aText.equals(TIME_COST)) {
        fEdgeValueFunction.setEdgeFunctionMode(SampleCompositeFunction.TIMECOST);
        fTracingDistanceControlPanel.setMaxDistance(20);
      } else {
        fEdgeValueFunction.setEdgeFunctionMode(SampleCompositeFunction.CONSTANT);
        fTracingDistanceControlPanel.setMaxDistance(10);
      }
    }
  }

  class NodeValueFunctionControlPanel extends OptionPanel {

    public NodeValueFunctionControlPanel() {
      super("Node value", new String[]{CONSTANT, NONE});
    }

    public void selected(String aOption) {
      if (aOption.equals(CONSTANT)) {
        fEdgeValueFunction.setNodeFunctionMode(SampleCompositeFunction.CONSTANT);
      } else {
        fEdgeValueFunction.setNodeFunctionMode(SampleCompositeFunction.NONE);
      }
    }
  }

  class TurnValueFunctionControlPanel extends OptionPanel {

    public TurnValueFunctionControlPanel() {
      super("Turn value", new String[]{ENABLED, DISABLED});
    }

    public void selected(String aOption) {
      if (aOption.equals(ENABLED)) {
        fEdgeValueFunction.setTurnFunctionMode(SampleCompositeFunction.CUSTOM);
      } else {
        fEdgeValueFunction.setTurnFunctionMode(SampleCompositeFunction.NONE);
      }
    }
  }

  class DistanceFunctionControlPanel extends OptionPanel {

    // Distance functions
    private CartesianDistanceFunction fCartesianDistanceFunction = new CartesianDistanceFunction();
    private TimeCostDistanceFunction fTimeCostDistanceFunction = new TimeCostDistanceFunction(fCartesianDistanceFunction, 100);

    public DistanceFunctionControlPanel() {
      super("Distance function", new String[]{CARTESIAN, TIME_COST, NONE});
      fGraphManager.setHeuristicEstimateFunction(fCartesianDistanceFunction);
    }

    public void selected(String aText) {
      if (aText.equals(CARTESIAN)) {
        fGraphManager.setHeuristicEstimateFunction(fCartesianDistanceFunction);
      } else if (aText.equals(TIME_COST)) {
        fGraphManager.setHeuristicEstimateFunction(fTimeCostDistanceFunction);
      } else {
        fGraphManager.setHeuristicEstimateFunction(null);
      }
    }

  }

  /**
   * GUI panel for choosing between different options.
   */
  private abstract class OptionPanel extends JPanel {

    public OptionPanel(String aName, String[] aOptions) {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), aName));
      setOptions(aOptions);
    }

    public void setOptions(JRadioButton... aRadioButtons) {
      ButtonGroup bg = new ButtonGroup();
      JPanel contentPanel = new JPanel(new GridLayout(aRadioButtons.length, 1));
      contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      for (JRadioButton button : aRadioButtons) {
        bg.add(button);
        contentPanel.add(button);
      }
      add(contentPanel);
    }

    public void setOptions(String... aOptions) {
      ButtonGroup bg = new ButtonGroup();
      JPanel contentPanel = new JPanel(new GridLayout(aOptions.length, 1));
      contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      boolean first = true;
      for (final String option : aOptions) {
        JRadioButton button = new JRadioButton(option, first);
        first = false;
        button.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              selected(option);
            }
          }
        });
        bg.add(button);
        contentPanel.add(button);
      }
      add(contentPanel);
    }

    public abstract void selected(String aOption);

  }

}
