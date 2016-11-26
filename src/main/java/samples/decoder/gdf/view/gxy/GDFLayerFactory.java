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
package samples.decoder.gdf.view.gxy;

import java.util.EnumSet;

import com.luciad.format.gdf.TLcdGDFDatasetModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;
import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphEdgePainterProvider;
import samples.network.common.view.gxy.AGraphNodePainterProvider;

/**
 * Layer factory for GDF models.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class GDFLayerFactory implements ILcdGXYLayerFactory {

  private GDFRenderingSettings fRenderingSettings;
  private GraphManager fGraphManager;

  public GDFLayerFactory() {
    this(new GDFRenderingSettings());
  }

  public GDFLayerFactory(GDFRenderingSettings aGdfRenderingSettings) {
    this(aGdfRenderingSettings, null);
  }

  public GDFLayerFactory(GDFRenderingSettings aGdfRenderingSettings, GraphManager aGraphManager) {
    fRenderingSettings = aGdfRenderingSettings;
    fGraphManager = aGraphManager;

  }

  // Implementations for ILcdGXYLayerFactory.
  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdGDFDatasetModelDescriptor)) {
      return null;
    }

    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel);
    gxy_layer.setSelectable(true);
    gxy_layer.setEditable(false);
    gxy_layer.setLabeled(false);
    gxy_layer.setVisible(true);
    gxy_layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
    gxy_layer.setMinimumObjectSizeForPainting(1);

    ILcdGXYPainterProvider[] fPainterProviders = createPainterProviderArray();
    gxy_layer.setGXYPainterProviderArray(fPainterProviders);
    return gxy_layer;
  }

  private ILcdGXYPainterProvider[] createPainterProviderArray() {
    ILcdGXYPainterProvider[] fPainterProviders = new ILcdGXYPainterProvider[]{
        new GDFAreaPainterProvider(),
        new GDFLinePainterProvider(fGraphManager,
                                   EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.NORMAL_EDGE),
                                   GDFLinePainterProvider.Mode.ALL_ROAD_BORDER, fRenderingSettings),
        new GDFLinePainterProvider(fGraphManager,
                                   EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.NORMAL_EDGE),
                                   GDFLinePainterProvider.Mode.ROAD_INNER, fRenderingSettings),
        new GDFLinePainterProvider(fGraphManager,
                                   EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.ROUTE_EDGE,
                                              AGraphEdgePainterProvider.GraphEdgeMode.START_EDGE,
                                              AGraphEdgePainterProvider.GraphEdgeMode.END_EDGE,
                                              AGraphEdgePainterProvider.GraphEdgeMode.DESTROYED_EDGE),
                                   GDFLinePainterProvider.Mode.ROAD_INNER, fRenderingSettings),
        new GDFLinePainterProvider(fGraphManager,
                                   EnumSet.of(AGraphEdgePainterProvider.GraphEdgeMode.NORMAL_EDGE),
                                   GDFLinePainterProvider.Mode.ROAD_SIGNS, fRenderingSettings),
        new GDFPointPainterProvider(fGraphManager,
                                    EnumSet.of(AGraphNodePainterProvider.GraphNodeMode.NORMAL_NODE,
                                               AGraphNodePainterProvider.GraphNodeMode.START_NODE,
                                               AGraphNodePainterProvider.GraphNodeMode.END_NODE))};
    return fPainterProviders;
  }

}
