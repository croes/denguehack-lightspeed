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
package samples.network.common;

import java.awt.BorderLayout;

import com.luciad.network.graph.ILcdGraph;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SamplePanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.network.common.graph.GraphManager;
import samples.network.common.graph.GraphParameterChangedListener;
import samples.network.common.gui.NetworkToolbar;

/**
 * Abstract super class for samples providing network functionality.
 */
public abstract class ANetworkSample extends SamplePanel {

  // Graph manager
  private GraphManager fGraphManager = createGraphManager();

  // GUI
  protected TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel();
  protected NetworkToolbar fToolbar = new NetworkToolbar(fMapJPanel, fGraphManager, true, this);

  public ANetworkSample() {
    super();

    // Make sure the view gets repainted when a graph parameter has changed.
    fGraphManager.addGraphParameterChangedListener(new GraphParameterChangedListener() {
      public void graphParameterChanged(GraphParameter aParameter, Object aOldValue, Object aNewValue) {
        fMapJPanel.invalidate(true, this, "Graph parameter changed.");
      }
    });
  }

  protected void createGUI() {
    // Create a titled panel around the map panel
    TitledPanel mapPanel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
                                                        );

    setLayout(new BorderLayout());
    add(fToolbar, BorderLayout.NORTH);
    add(mapPanel, BorderLayout.CENTER);
  }

  public GraphManager getGraphManager() {
    return fGraphManager;
  }

  protected GraphManager createGraphManager() {
    return new GraphManager();
  }

  public void setGraph(ILcdGraph aGraph) {
    fGraphManager.setGraph(aGraph);
  }

}
