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
package samples.gxy.clustering;

import java.awt.Color;
import java.io.IOException;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdStrokeLineStyle;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

public class MainPanel extends GXYSample {

  @Override
  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().asynchronous().addToView(getView());
    GXYUnstyledLayerFactory countriesLayerFactory = new GXYUnstyledLayerFactory();
    countriesLayerFactory.setFillStyle(new TLcdGXYPainterColorStyle(new Color(153, 153, 153, 128)));
    countriesLayerFactory.setLineStyle(TLcdStrokeLineStyle.newBuilder().lineWidth(2).color(Color.white).build());
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer(countriesLayerFactory).label("Countries").asynchronous().selectable(false).addToView(getView());

    ILcdModel originalModel = new TLcdSHPModelDecoder().decode(SampleData.HUMANITARIAN_EVENTS);

    ILcdGXYLayer eventsLayer = GXYDataUtil.instance().model(originalModel).layer().label("Events").getLayer();
    eventsLayer.setVisible(false);
    GXYLayerUtil.addGXYLayer(getView(), new TLcdGXYAsynchronousEditableLabelsLayerWrapper((ILcdGXYEditableLabelsLayer) eventsLayer));

    Object mapReference = getView().getXYWorldReference();
    ILcdModel transformingModel =
        TLcdTransformingModelFactory.createTransformingModel(originalModel, samples.lightspeed.clustering.MainPanel.createClusteringTransformer(mapReference));

    ILcdGXYLayer clusterLayer = new ClusterLayerFactory().createGXYLayer(transformingModel);

    GXYLayerUtil.addGXYLayer(getView(), clusterLayer);
    GXYLayerUtil.fitGXYLayers(getView(), new ILcdGXYLayer[]{clusterLayer});
  }

  public static void main(String[] args) {
    GXYSample.startSample(MainPanel.class, "Clustering");
  }

}
