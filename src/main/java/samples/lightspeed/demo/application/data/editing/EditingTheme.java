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
package samples.lightspeed.demo.application.data.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayered;
import com.luciad.view.TLcdSelectionMediator;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;

import samples.lightspeed.demo.application.gui.menu.EditingPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Theme that demonstrates the 2D- and 3D editing capabilities of LuciadLightspeed.
 */
public class EditingTheme extends AbstractTheme {

  private EditingPanelFactory fPanelFactory;

  public EditingTheme() {
    setName("Editing");
    setCategory("Shapes");
    fPanelFactory = new EditingPanelFactory();
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    ShapesModelFactory shapesModelFactory = new ShapesModelFactory("shapes");
    shapesModelFactory.configure(getThemeProperties());
    ILcdModel shapesModel = shapesModelFactory.createModel("null");

    ShapesLayerFactory layerFactory = new ShapesLayerFactory();
    layerFactory.configure(getThemeProperties());

    List<ILspLayer> layers = new ArrayList<ILspLayer>();
    ILspLayer previousLayer = null;
    for (ILspView view : aViews) {
      ILspLayer layer = layerFactory.createLayer(shapesModel);
      layer.setLabel("Editing - Shapes");
      view.addLayer(layer);
      layers.add(layer);
      Framework.getInstance().registerLayers("layer.id.shapes", view, Collections.singleton(layer));

      if (previousLayer != null) {
        linkSelection(previousLayer, layer);
        linkSelection(layer, previousLayer);
      }
      previousLayer = layer;
    }

    return layers;
  }

  @Override
  public void deactivate() {
    List<ILspLayer> layers = getLayers();
    for (ILspLayer layer : layers) {
      layer.clearSelection(ILcdFireEventMode.FIRE_NOW);
    }
  }

  private void linkSelection(ILspLayer aSourceLayer, ILspLayer aSlaveLayer) {
    TLspLayerTreeNode srcNode = new TLspLayerTreeNode("Selection mediator - source node: " + aSourceLayer.getLabel() + " -> " + aSlaveLayer.getLabel());
    srcNode.addLayer(aSourceLayer);
    TLspLayerTreeNode slaveNode = new TLspLayerTreeNode("Selection mediator - slave node: " + aSourceLayer.getLabel() + " -> " + aSlaveLayer.getLabel());
    slaveNode.addLayer(aSlaveLayer);
    TLcdSelectionMediator mediator = new TLcdSelectionMediator();
    mediator.setSourceLayered(new ILcdLayered[]{srcNode});
    mediator.setSlaveLayered(new ILcdLayered[]{slaveNode});
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fPanelFactory.createThemePanels(this);
  }

  @Override
  public JComponent getSouthDockedComponent() {
    return fPanelFactory.getSouthDockedComponent();
  }

}
