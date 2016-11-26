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
import com.luciad.network.algorithm.routing.ILcdTracingAlgorithm;

import samples.network.common.graph.GraphManager;

/**
 * This action calculates, prints and selects the shortest path between 2 vertices in a graph.
 */
public class TraceAction extends ALcdAction {

  private static final String NAME_FORWARD = "Calculate the trace";
  private static final String NAME_BACKWARD = "Calculate the backward trace";

  private static final ILcdIcon ICON_FORWARD = new TLcdImageIcon("samples/images/trace_forward.png");
  private static final ILcdIcon ICON_BACKWARD = new TLcdImageIcon("samples/images/trace_backward.png");

  public static enum Type {
    FORWARD, BACKWARD;
  }

  public static final int START_VERTEX = 0;
  public static final int END_VERTEX = 1;

  private Component fParentFrame;

  private GraphManager fGraphManager;
  private ILcdTracingAlgorithm fTracingAlgorithm;
  private Type fType;

  /**
   * Constructs a new ShortestRouteAction.
   */
  public TraceAction(Component aParentFrame,
                     GraphManager aGraphManager,
                     ILcdTracingAlgorithm aTracingAlgorithm,
                     Type aType) {
    fParentFrame = aParentFrame;
    fGraphManager = aGraphManager;
    fTracingAlgorithm = aTracingAlgorithm;
    fType = aType;
    setName(aType == Type.FORWARD ? NAME_FORWARD : NAME_BACKWARD);
    setShortDescription(aType == Type.FORWARD ? NAME_FORWARD : NAME_BACKWARD);
    setIcon(aType == Type.FORWARD ? ICON_FORWARD : ICON_BACKWARD);
  }

  public void actionPerformed(ActionEvent aEvent) {
    if ((isForward() && fGraphManager.getStartNode() == null) ||
        (!isForward() && fGraphManager.getEndNode() == null)) {
      JOptionPane.showMessageDialog(fParentFrame,
                                    "Please select a" + (isForward() ? " start" : "n end") + " node before routing.");
      return;
    }

    fGraphManager.setTracingAlgorithm(fTracingAlgorithm);
    if (isForward()) {
      fGraphManager.computeForwardTraces();
    } else {
      fGraphManager.computeBackwardTraces();
    }
  }

  private boolean isForward() {
    return fType == Type.FORWARD;
  }

}
