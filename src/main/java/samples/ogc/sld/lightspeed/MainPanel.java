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
package samples.ogc.sld.lightspeed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.ogc.sld.view.lightspeed.TLspSLDStyler;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.ogc.sld.SLDFeatureTypeStyleStore;

/**
 * A sample to demonstrate OGC's Styled Layer Descriptor (SLD) styles in a Lightspeed view,
 * The sample loads various vector and raster layers, styling them using SLD.
 * Styles are created using the {@link SLDFeatureTypeStyleStore} sample class.
 * The created styles are applied to an existing Lightspeed layer by creating and
 * setting a {@link TLspSLDStyler}. This is done in the StylePanel class.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();
    Collection<ILspLayer> layers = new ArrayList<ILspLayer>();
    layers.add(LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit().getLayer());
    ILspLayer lasVegasLayer = LspDataUtil.instance().model(SampleData.LAS_VEGAS_2003).layer().label("Las Vegas").addToView(getView()).getLayer();
    layers.add(lasVegasLayer);

    for (ILspLayer layer : layers) {
      getView().addLayer(layer);
      // SLD also styles labels, so add the LABEL paint representation if possible.
      if (layer instanceof TLspLayer &&
          !layer.getPaintRepresentations().contains(TLspPaintRepresentation.LABEL)) {
        ((TLspLayer) layer).addPaintRepresentation(TLspPaintRepresentation.LABEL);
      }
    }

    // Move the Las Vegas layer on top of the other layers,
    // to make sure that it is not covered by US states and countries data.
    getView().moveLayerAt(layers.size(), lasVegasLayer);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    addComponentToRightPanel(new StylePanel(getView(), getSelectedLayers()));
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, "Styling with a Styled Layer Descriptor (SLD)");
  }

}
