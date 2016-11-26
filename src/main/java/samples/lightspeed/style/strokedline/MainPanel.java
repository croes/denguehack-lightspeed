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
package samples.lightspeed.style.strokedline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspLayerSelectionPanel;

/**
 * This sample demonstrates how line data can be visualized using complex strokes. This is achieved
 * by using TLspComplexStrokedLineStyle in conjunction with TLspShapePainter. For more information about
 * how to create complex strokes, see {@link ALspComplexStroke} or at the knowledge base article that
 * explains step-by-step how to create complex strokes.
 */
public class MainPanel extends LightspeedSample {

  private LspLayerSelectionPanel createLayerPanel(Collection<ILspLayer> aLayers) {
    LspLayerSelectionPanel panel = new LspLayerSelectionPanel(getView(), true, this);
    for (ILspLayer layer : aLayers) {
      panel.addLayer(layer.getLabel(), layer);
    }
    return panel;
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    List<ILspLayer> layers = new ArrayList<>();
    LayerFactory layerFactory = new LayerFactory();

    layers.add(layerFactory.createLayer(ModelFactory.createProcedureModel()));
    layers.add(layerFactory.createLayer(ModelFactory.createAirRouteModel()));
    layers.add(layerFactory.createLayer(ModelFactory.createAirspaceModel()));
    layers.add(layerFactory.createLayer(ModelFactory.createMeteoModel()));
    layers.add(layerFactory.createLayer(ModelFactory.createCartographyModel()));

    addComponentToRightPanel(createLayerPanel(layers));
  }

  @Override
  protected void addData() throws IOException {
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(getView());
    LspDataUtil.instance().grid().addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Stroked line styles");
  }

}
