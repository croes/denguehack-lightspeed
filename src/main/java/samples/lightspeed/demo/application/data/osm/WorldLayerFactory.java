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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * @author Dieter Meeus
 * @since 2012.1
 */
public class WorldLayerFactory extends ALspSingleLayerFactory {
  public WorldLayerFactory() {

  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    layerBuilder
        .model(aModel)
        .label("World")
        .selectable(false)
        .bodyStyler(TLspPaintState.REGULAR, createStyler())
        .bodyScaleRange(new TLcdInterval(0.00001, 0.012))
        .labelScaleRange(new TLcdInterval(0.00001, 0.005))
        .labelStyler(TLspPaintState.REGULAR, new WorldLabelStyler());

    return layerBuilder.build();
  }

  private ILspStyler createStyler() {
    return TLspLineStyle.newBuilder()
                        .color(Color.white)
                        .opacity(0.5f)
                        .width(1.5f)
                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                        .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("world.shp");
  }

  private static class WorldLabelStyler extends ALspLabelStyler {
    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      int lod = LODSupport.getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      for (Object object : aObjects) {
        if (GLOBAL_PRIORITIES[lod] < 0) {
          continue;
        }

        aStyleCollector.object(object)
                       .styles(TEXT_STYLE)
                       .priority(getPriority(object, 0, 1000, lod))
                       .submit();
      }
    }

    private static final ILcdInterval[] DETAIL_LEVELS = {
        new TLcdInterval(5e-5, 1e-4),
        new TLcdInterval(1e-4, 2e-4),
        new TLcdInterval(2e-4, 5e-4),
        new TLcdInterval(5e-4, 1e-3),
        new TLcdInterval(1e-3, 5e-3),
        new TLcdInterval(5e-3, 1e-2),
        new TLcdInterval(1e-2, 2e-2),
        new TLcdInterval(2e-2, 1e-1),
        new TLcdInterval(1e-1, Double.MAX_VALUE)
    };

    private static final TLspTextStyle TEXT_STYLE = TLspTextStyle.newBuilder().font("Dialog-BOLD-16").textColor(Color.black).build();

    private static double[] GLOBAL_PRIORITIES = {
        0.8,
        0.65,
        0.4,
        0.25,
        0.0,
        0.0,
        0.0,
        -1,
        -1
    };

    private static int getPriority(Object aObject, int aLargestPriority, int aSmallestPriority, int aLOD) {
      double relative_priority = getRelativePriority(aObject);
      double global_priority = getGlobalPriority(aLOD);
      double priority = global_priority * 0.5 + relative_priority * 0.5;
      return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
    }

    private static double getRelativePriority(Object aObject) {
      // Retrieve the priority of a label relative to an other label of the same layer.
      ILcdDataObject data_object = (ILcdDataObject) aObject;
      int population = (Integer) data_object.getValue("POP_1994");

      double min = 0.0;
      double max = 1.0;
      double f = 1.0 - Math.min(population, 50000000) / 50000000;

      return (min + ((max - min) * f));
    }

    private static double getGlobalPriority(int aLOD) {
      return GLOBAL_PRIORITIES[aLOD];
    }
  }
}
