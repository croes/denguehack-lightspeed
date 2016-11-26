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
package samples.gxy.labels.placement;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYCurvedPathLabelLocation;
import com.luciad.view.gxy.TLcdGXYCurvedPathLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYCompositeDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYCurvedPathLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYInPathLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdLabelConflictChecker;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;
import samples.gxy.labels.placement.MixedModelLayerFactory.MixedLabelingAlgorithmProvider;

/**
 * This sample demonstrates both the synchronous and asynchronous label placers
 * and the following default labeling algorithms:
 * <ul>
 * <li>TLcdGXYLocationListLabelingAlgorithm
 * <li>TLcdGXYOnPathLabelingAlgorithm, for labeling lines
 * <li>TLcdGXYInPathLabelingAlgorithm, for labeling states
 * <li>TLcdGXYCurvedPathLabelingAlgorithm, for labeling rivers
 * </ul>
 * Labeling algorithms take into account the
 * labels that are already placed to avoid overlap.
 * An ILcdGXYLayer will therefore most likely have less labels painted
 * when it is at the bottom than when it is at the top of the ILcdGXYView.
 * <p>
 * To see the effects of the labeling algorithm select a layer, by clicking it
 * in the layer control and:
 * - change the layers visibility
 * - set the layer labeled/not labeled
 * - move the layer up/down
 * Zooming in/out will also have an effect on the labeling.
 * <p>
 * Labeling algorithm for layers are registered using GXYDataUtil#labelingAlgorithm, which registers them in the
 * ServiceRegistry. GXYLabelingAlgorithmProvider, which is a composite labeling algorithm provider implementation,
 * can pick up the labeling algorithms registered in ServiceRegistry. By default, samples use a label placer that
 * uses a GXYLabelingAlgorithmProvider instance. This is done in SampleMapJPanelFactory.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-92.5, 30.0, 12.00, 9.00);
  }

  @Override
  protected void addData() throws IOException {
    // Creates layers displaying countries, states, rivers and cities.
    // The layers come with a text based label painter.
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        // Add world layer
        GXYDataUtil.instance()
                   .model(SampleData.COUNTRIES)
                   .layer(new GXYUnstyledLayerFactory()).label("Countries")
                   .labelingAlgorithm(new TLcdGXYInPathLabelingAlgorithm())
                   .addToView(getView()).getLayer();

        // Add states layer
        ILcdGXYLayer states = GXYDataUtil.instance()
                                         .model(SampleData.US_STATES)
                                         .layer(new GXYUnstyledLayerFactory()).label("States")
                                         .labelingAlgorithm(new TLcdGXYInPathLabelingAlgorithm())
                                         .addToView(getView()).getLayer();
        ILcdGXYEditableLabelsLayer statesLayer = (ILcdGXYEditableLabelsLayer) states;
        statesLayer.setLabeled(true);
        statesLayer.setSelectable(true);
        statesLayer.setLabelsEditable(true);

        // Add rivers layer
        TLcdGXYCurvedPathLabelingAlgorithm riversLabelingAlgorithm = new TLcdGXYCurvedPathLabelingAlgorithm(createCurvedPathLabelPainter());
        riversLabelingAlgorithm.setReusePreviousLocations(true);
        ILcdGXYLayer rivers = GXYDataUtil.instance()
                                         .model(SampleData.US_RIVERS)
                                         .layer(new GXYUnstyledLayerFactory()).label("Rivers")
                                         .labelingAlgorithm(riversLabelingAlgorithm)
                                         .addToView(getView()).getLayer();
        ILcdGXYEditableLabelsLayer riversLayer = (TLcdGXYLayer) rivers;
        riversLayer.setLabeled(true);
        riversLayer.setSelectable(true);
        riversLayer.setLabelsEditable(false);
        configureCurvedPathLabeling((TLcdGXYLayer) riversLayer);

        GXYUnstyledLayerFactory cityLayerFactory = new GXYUnstyledLayerFactory();
        cityLayerFactory.setLabelsWithPin(true);
        ILcdGXYLayer cities = GXYDataUtil.instance()
                                         .model(SampleData.US_CITIES)
                                         .layer(cityLayerFactory).label("Cities")
                                         .labelingAlgorithm(new TLcdGXYLocationListLabelingAlgorithm())
                                         .addToView(getView()).getLayer();
        ILcdGXYEditableLabelsLayer citiesLayer = (TLcdGXYLayer) cities;
        citiesLayer.setLabeled(true);
        citiesLayer.setSelectable(true);
        citiesLayer.setLabelsEditable(true);

        ILcdModel mixedModel = MixedModelLayerFactory.createMixedModel();
        GXYDataUtil.instance()
                   .model(mixedModel)
                   .layer(new MixedModelLayerFactory())
                   .labelingAlgorithm(new TLcdGXYCompositeDiscretePlacementsLabelingAlgorithm(new MixedLabelingAlgorithmProvider()))
                   .addToView(getView());

        // Configures a synchronous view label placer that will place all labels around the domain
        // object's anchor point.
        // These lines are just for illustration purposes...

        TLcdGXYLabelPlacer label_placer = new TLcdGXYLabelPlacer(new TLcdGXYLocationListLabelingAlgorithm());
        getView().setGXYViewLabelPlacer(label_placer);

        // ...the sample actually uses a more advanced composite labeling algorithm.
        setLabelPlacer();
      }
    });
  }

  private void configureCurvedPathLabeling(TLcdGXYLayer aGXYLayer) {
    // Create a label painter that drapes the labels over a path
    TLcdGXYCurvedPathLabelPainter curved_path_label_painter = createCurvedPathLabelPainter();
    aGXYLayer.setLabelLocations(new TLcdLabelLocations(aGXYLayer, new TLcdGXYCurvedPathLabelLocation()));
    aGXYLayer.setGXYLabelPainterProvider(curved_path_label_painter);
    aGXYLayer.setGXYLabelEditorProvider(null);
    aGXYLayer.setLabeled(true);
  }

  private TLcdGXYCurvedPathLabelPainter createCurvedPathLabelPainter() {
    TLcdGXYCurvedPathLabelPainter curved_path_label_painter = new AntiAliasedCurvedPathLabelPainter();
    curved_path_label_painter.setFont(new Font("Dialog", Font.PLAIN, 13));
    curved_path_label_painter.setForeground(Color.WHITE);
    curved_path_label_painter.setHaloEnabled(true);
    curved_path_label_painter.setHaloColor(Color.BLACK);
    curved_path_label_painter.setExtraCharacterSpacing(1.0);
    return curved_path_label_painter;
  }

  private void setLabelPlacer() {
    ILcdLabelConflictChecker checker = new TLcdLabelConflictChecker();
    GXYLabelingAlgorithmProvider labelingAlgorithmProvider = new GXYLabelingAlgorithmProvider();
    TLcdGXYCompositeLabelingAlgorithm algorithm = new TLcdGXYCompositeLabelingAlgorithm(labelingAlgorithmProvider);
    TLcdGXYAsynchronousLabelPlacer label_placer = new TLcdGXYAsynchronousLabelPlacer(algorithm);
    label_placer.setLabelConflictChecker(checker);
    getView().setGXYViewLabelPlacer(label_placer);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Labeling");
  }
}
