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
package samples.lightspeed.integration.gxy;

import java.io.IOException;

import com.luciad.format.magneticnorth.TLcdWMMModelDecoder;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYOnPathLabelingAlgorithm;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.integration.gxy.TLspGXYLayerAdapter;

import samples.common.model.GeodeticModelFactory;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to visualize and interact with ILcdGXYLayer instances in a
 * Lightspeed view.
 * This is achieved using TLspGXYLayerAdapter and TLspGXYLayerTreeNodeAdapter,
 * which render the GXY layer(s) into an off-screen ILcdGXYView and then draw the
 * resulting image into the Lightspeed view.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected ILspAWTView createView() {
    // The view is initialized in 2D.
    // Note that interaction (selection & editing) is not supported in 3D mode.
    return super.createView(ILspView.ViewType.VIEW_2D);
  }

  protected void addData() throws IOException {
    // The TLspGXYLayerAdapter logic is in AdaptedLayerFactory, we can simply give it a
    // composite ILcdGXYLayerFactory.

    AdaptedLayerFactory adaptedLayerFactory = new AdaptedLayerFactory(new TLcdCompositeGXYLayerFactory(ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class)));

    // A regular Lightspeed grid layer.
    LspDataUtil.instance().grid().addToView(getView());

    // Use ShpGXYLayerFactory for .shp models to enable label editing
    ServiceRegistry.getInstance().register(new ShpGXYLayerFactory());

    // A vector GXY layer with some standard shapes.
    ILcdModel model = new GeodeticModelFactory().createSimpleModel();
    getView().addLayer(adaptedLayerFactory.createLayer(model));

    // Our layer factory can also create TLcdGXYLayerTreeNodes, which are automatically adapted
    // by the AdaptedLayerFactory.
    ILcdModelTreeNode shpModel = (ILcdModelTreeNode) new TreeShpModelDecoder().decode("Data/Shp/World/world.shp");
    ILcdModel geotiffModel = new TLcdGeoTIFFModelDecoder().decode("Data/GeoTIFF/BlueMarble/bluemarble.tif");
    shpModel.addModel(geotiffModel);
    ILcdModel magneticModel = new TLcdWMMModelDecoder().decode("Data/magneticnorth/WMM2015.COF");
    shpModel.addModel(magneticModel);

    ILspLayer layerTreeNode = adaptedLayerFactory.createLayer(shpModel);

    // Add the layer tree node to the bottom of the view.
    getView().getRootNode().addLayer(layerTreeNode, 0);

    // We add custom label placement algorithms to the SHP vector layer and magnetic north layer.
    // These algorithm will be used to declutter the layer's labels and do not affect labels of other layers.

    TLspGXYLayerAdapter shpModelAdapter = (TLspGXYLayerAdapter) getView().layerOf(shpModel);
    TLcdGXYLocationListLabelingAlgorithm shpAlgorithm = new TLcdGXYLocationListLabelingAlgorithm();
    shpModelAdapter.setGXYLayerLabelingAlgorithm(shpAlgorithm);

    TLspGXYLayerAdapter magneticAdapter = (TLspGXYLayerAdapter) getView().layerOf(magneticModel);
    magneticAdapter.setVisible(TLspPaintRepresentation.LABEL, false);
    TLcdGXYOnPathLabelingAlgorithm magneticAlgorithm = new TLcdGXYOnPathLabelingAlgorithm();
    magneticAdapter.setGXYLayerLabelingAlgorithm(magneticAlgorithm);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "GXY integration");
  }

}
