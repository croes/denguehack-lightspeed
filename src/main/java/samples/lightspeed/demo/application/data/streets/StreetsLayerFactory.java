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
package samples.lightspeed.demo.application.data.streets;

import static samples.lightspeed.demo.application.data.streets.DetailLevelStyler.getLevelOfDetail;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspCurvedPathLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspWorldSizedLineStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * A layer factory for streets.
 */
public class StreetsLayerFactory extends ALspSingleLayerFactory {

  private static final Color STREET_COLOR = new Color(0xD6DBE7);
  private static final Color STREET_HALO_COLOR = STREET_COLOR.darker();

  private static final Color MAJOR_STREET_COLOR = new Color(240, 200, 80);
  private static final Color MAJOR_STREET_HALO_COLOR = MAJOR_STREET_COLOR.darker();

  private static final Color STREET_LABEL_TEXT_COLOR = new Color(0xD6DBE7);
  private static final Color STREET_LABEL_HALO_COLOR = Color.black;

  private static final Color SELECTED_HALO = Color.red;
  private static final int STREET_WIDTH = 8;

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(0.05, 0.1),
      new TLcdInterval(0.1, 0.2),
      new TLcdInterval(0.2, 0.5),
      new TLcdInterval(0.5, 1.0),
      new TLcdInterval(1.0, Double.MAX_VALUE)
  };

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    boolean isMajorRoadsLayer = aModel.getModelDescriptor().getSourceName().endsWith("roads.shp");
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    ILspLabelingAlgorithm labelingAlgorithm = new TLspCurvedPathLabelingAlgorithm();
    ILspLabelPriorityProvider priorityProvider = new StreetsLabelPriorityProvider(0, 1000);

    layerBuilder.model(aModel)
                .label("Streets")
                .selectable(true)
                .bodyEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, createStreetStyler(isMajorRoadsLayer, false))
                .bodyStyler(TLspPaintState.SELECTED, createStreetStyler(isMajorRoadsLayer, true))
                .labelEditable(false)
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder().priority(priorityProvider).algorithm(labelingAlgorithm).styles(TLspTextStyle.newBuilder().textColor(STREET_LABEL_TEXT_COLOR).haloColor(STREET_LABEL_HALO_COLOR).build()).build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder().priority(priorityProvider).algorithm(labelingAlgorithm).styles(TLspTextStyle.newBuilder().textColor(Color.red).haloColor(STREET_LABEL_HALO_COLOR).build()).build())
                .labelScaleRange(new TLcdInterval(0.4, Double.MAX_VALUE))
                .bodyScaleRange(new TLcdInterval(0.01, Double.MAX_VALUE));

    return layerBuilder.build();
  }

  private static ILspStyler createStreetStyler(boolean aIsMajorRoadStyler, boolean aIsSelectedStyler) {
    TLspWorldSizedLineStyle.Builder<?> lineStyleBuilder = TLspWorldSizedLineStyle.newBuilder()
                                                                                 .color(aIsSelectedStyler ? SELECTED_HALO : (aIsMajorRoadStyler ? MAJOR_STREET_HALO_COLOR : STREET_HALO_COLOR))
                                                                                 .width(STREET_WIDTH)
                                                                                 .zOrder(0)
                                                                                 .elevationMode(ElevationMode.ON_TERRAIN);

    return new TLspStyler(lineStyleBuilder.build(),
                          lineStyleBuilder.color(aIsMajorRoadStyler ? MAJOR_STREET_COLOR : STREET_COLOR)
                                          .width(STREET_WIDTH - 2)
                                          .zOrder(1)
                                          .elevationMode(ElevationMode.ON_TERRAIN)
                                          .build()
    );
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("streets.shp") ||
           aModel.getModelDescriptor().getSourceName().endsWith("roads.shp");
  }

  private class StreetsLabelPriorityProvider implements ILspLabelPriorityProvider {
    private int fHighestPriority;
    private int fLowestPriority;

    public StreetsLabelPriorityProvider(int aHighestPriority, int aLowestPriority) {
      fHighestPriority = aHighestPriority;
      fLowestPriority = aLowestPriority;
    }

    @Override
    public int getPriority(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
      double relativePriority = getRelativePriority();
      double globalPriority = getGlobalPriority(aContext);
      double priority = globalPriority * 0.5 + relativePriority * 0.5;

      return fHighestPriority + (int) (priority * (double) (fLowestPriority - fHighestPriority));
    }

    private double getRelativePriority() {
      // Retrieve the priority of a label relative to an other label of the same layer.
      return 1.0;
    }

    private double getGlobalPriority(TLspContext aContext) {
      // Retrieve the global priority of street labels. This priority is based on the view scale,
      // and makes sure labels of different layers interact correctly.
      int detailLevel = getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      if (detailLevel == -1) {
        return 1.0;
      }

      if (detailLevel == 0) {
        return 1.0;
      }
      if (detailLevel == 1) {
        return 0.9;
      }
      if (detailLevel == 2) {
        return 0.8;
      }
      if (detailLevel == 3) {
        return 0.7;
      }
      if (detailLevel == 4) {
        return 0.6;
      }

      return 1.0;
    }
  }
}
