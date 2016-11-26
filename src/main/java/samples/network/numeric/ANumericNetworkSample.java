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
package samples.network.numeric;

import java.awt.BorderLayout;

import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.SampleData;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.network.common.ANetworkSample;
import samples.network.common.controller.DestroyEdgeController;
import samples.network.common.graph.GraphManager;
import samples.network.numeric.action.LoadGraphConfigurationAction;
import samples.network.numeric.graph.NumericGraphManager;

/**
 * Abstract super class for all numeric graphs demo samples.
 */
public abstract class ANumericNetworkSample extends ANetworkSample {

  private LoadGraphConfigurationAction fLoadGraphConfigurationAction;

  public void setLoadGraphConfigurationAction(LoadGraphConfigurationAction aLoadGraphConfigurationAction) {
    fLoadGraphConfigurationAction = aLoadGraphConfigurationAction;
  }

  public ILcdGXYLayer loadGraph(String aPropertiesSource) {
    return fLoadGraphConfigurationAction.loadGraphConfiguration(aPropertiesSource);
  }

  @Override
  protected GraphManager createGraphManager() {
    NumericGraphManager graphManager = new NumericGraphManager();
    graphManager.setMaximumTracingDistance(5000);
    return graphManager;
  }

  @Override
  public NumericGraphManager getGraphManager() {
    return (NumericGraphManager) super.getGraphManager();
  }

  @Override
  public void createGUI() {
    super.createGUI();

    fToolbar.addSpace();
    fToolbar.addGXYController(new DestroyEdgeController(getGraphManager()));
    if (fLoadGraphConfigurationAction != null) {
      fToolbar.addSpace();
      fToolbar.addAction(fLoadGraphConfigurationAction);
    }

    LayerControlPanel layerControl = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);
    add(layerControl, BorderLayout.EAST);
  }

  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);
  }

}
