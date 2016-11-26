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
package samples.lightspeed.clustering;

import java.awt.Color;
import java.io.IOException;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.common.AScaleSupport;
import samples.common.SampleData;
import samples.gxy.clustering.CountryClassifier;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.decoder.UnstyledLayerFactory;

public class MainPanel extends LightspeedSample {

  private static final double CLUSTER_SIZE = 200.0;
  private static final int MIN_CLUSTER_COUNT = 2;

  @Override
  protected void addData() throws IOException {
    super.addData();
    addEventsLayer();
  }

  private void addEventsLayer() throws IOException {
    ILspLayer eventsLayer = createEventsLayer();
    eventsLayer.setLabel("Clustered events");
    getView().addLayer(eventsLayer);
    FitUtil.fitOnLayers(this, eventsLayer);
  }

  private ILspLayer createEventsLayer() throws IOException {
    UnstyledLayerFactory countriesLayerFactory = new UnstyledLayerFactory();
    countriesLayerFactory.setLineStyle(TLspLineStyle.newBuilder().width(2).build());
    countriesLayerFactory.setFillStyle(TLspFillStyle.newBuilder().color(new Color(153, 153, 153, 128)).build());
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer(countriesLayerFactory).label("Countries").labeled(false).selectable(false).addToView(getView());
    ILcdModel originalModel = new TLcdSHPModelDecoder().decode(SampleData.HUMANITARIAN_EVENTS);
    TLcdClusteringTransformer clusteringTransformer =
        createClusteringTransformer(getView().getXYZWorldReference());
    ILcdModel transformingModel = TLcdTransformingModelFactory.createTransformingModel(originalModel, clusteringTransformer);
    ILspLayer eventsLayer = LspDataUtil.instance().model(originalModel).layer().label("Events").editable(true).getLayer();
    eventsLayer.setVisible(false);
    eventsLayer.setVisible(TLspPaintRepresentation.LABEL, false);
    getView().addLayer(eventsLayer);

    return new ClusterLayerFactory().createLayer(transformingModel);
  }

  /**
   * Creates a clustering transformer for the {@link SampleData#HUMANITARIAN_EVENTS} data set
   *
   * @param aMapReference The map reference
   *
   * @return a clustered model
   */
  public static TLcdClusteringTransformer createClusteringTransformer(Object aMapReference) {
    //When zoomed in, cluster the events per country and avoid grouping events
    //happening in different countries in different clusters.
    TLcdClusteringTransformer zoomedInClusteringTransformer =
        TLcdClusteringTransformer.newBuilder()
                                 .classifier(new CountryClassifier())
                                 .defaultParameters()
                                   .clusterSize(CLUSTER_SIZE)
                                   .minimumPoints(MIN_CLUSTER_COUNT)
                                   .build()
                                 .build();
    //When zoomed out, all the events can be clustered together.
    //Otherwise, we would end up with overlapping clusters as the countries become rather small
    TLcdClusteringTransformer zoomedOutClusteringTransformer =
        TLcdClusteringTransformer.newBuilder()
                                 .defaultParameters()
                                   .clusterSize(CLUSTER_SIZE)
                                   .minimumPoints(MIN_CLUSTER_COUNT)
                                   .build()
                                 .build();

    //Switching between the two clustering approaches should happen at a scale 1 : 25 000 000
    double mapScaleDenominator = 25e6;
    double internalScaleDenominator =
        1d / AScaleSupport.mapScale2InternalScale(1d / mapScaleDenominator, -1, aMapReference);

    TLcdClusteringTransformer scaleDependentClusteringTransformer =
        TLcdClusteringTransformer.createScaleDependent(
            new double[]{internalScaleDenominator},
            new TLcdClusteringTransformer[]{zoomedInClusteringTransformer, zoomedOutClusteringTransformer}
        );
    return scaleDependentClusteringTransformer;
  }

  public static void main(String[] args) {
    LightspeedSample.startSample(MainPanel.class, "Clustering");
  }

}
