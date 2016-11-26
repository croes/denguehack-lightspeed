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
package samples.lightspeed.demo.application.data.weather;

import java.awt.Color;
import java.util.Collection;

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

class IcingSLDContourLayerFactory {

  private static final IcingSLDContourStyler STYLER = new IcingSLDContourStyler();

  public ILspLayer createLayer(IcingModel aIcingModel) {
    MultiDimensionalRangeFilter multiDimensionalRangeFilter = new MultiDimensionalRangeFilter(aIcingModel.createMultiDimensionalValueForFirstValue());

    TLcdDimensionAxis<TLcdISO19103Measure> heightAxis = aIcingModel.getHeightAxis();
    multiDimensionalRangeFilter.setMinimumValue(heightAxis, aIcingModel.getAxisValue(heightAxis, 0));
    multiDimensionalRangeFilter.setMaximumValue(heightAxis, aIcingModel.getAxisValue(heightAxis, aIcingModel.getNumberOfValues(heightAxis) - 1));

    CachingShapeDiscretizer shapeDiscretizer = new CachingShapeDiscretizer();
    TLspShapePainter painter = new TLspShapePainter();
    painter.setShapeDiscretizer(shapeDiscretizer);

    return TLspShapeLayerBuilder.newBuilder().model(aIcingModel.getIcingSLDModel())
                                .bodyStyler(TLspPaintState.REGULAR, STYLER)
                                .bodyPainter(painter)
                                .selectable(false)
                                .filter(multiDimensionalRangeFilter)
                                .label("Weather Icing SLD Contours")
                                .build();
  }

  static MultiDimensionalRangeFilter getMultiDimensionalRangeFilter(ILspLayer aLayer) {
    return getFilter(aLayer, MultiDimensionalRangeFilter.class);
  }

  private static <T> T getFilter(ILspLayer aLayer, Class<T> aFilterClass) {
    if (aLayer instanceof ALspLayer) {
      ALspLayer filteredLayer = (ALspLayer) aLayer;
      if (aFilterClass.isAssignableFrom(filteredLayer.getFilter().getClass())) {
        return aFilterClass.cast(filteredLayer.getFilter());
      }
    }
    return null;
  }

  private static class IcingSLDContourStyler extends ALspStyler {

    private static final Color COLOR = new Color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 120);

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (object instanceof IntervalContour) {
          IntervalContour contour = (IntervalContour) object;

          aStyleCollector.object(contour)
                             .geometry(contour.getShape())
                             .style(TLspFillStyle.newBuilder()
                                                 .color(COLOR)
                                                 .stipplePattern(TLspFillStyle.StipplePattern.HATCHED)
                                                 .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                 .build())
                             .submit();
        }
      }
    }

  }

}
