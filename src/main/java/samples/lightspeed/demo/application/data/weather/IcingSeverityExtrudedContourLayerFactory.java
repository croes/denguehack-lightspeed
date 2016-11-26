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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ALcdDynamicFilter;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.application.data.weather.TemperatureContourLayerFactory.CompositeFilter;

class IcingSeverityExtrudedContourLayerFactory {

  public ILspLayer createLayer(IcingModel aIcingModel) {
    MultiDimensionalRangeFilter multiDimensionalRangeFilter = new MultiDimensionalRangeFilter(aIcingModel.createMultiDimensionalValueForFirstValue());
    IcingSeverityFilter icingSeverityFilter = new IcingSeverityFilter();
    IcingProbabilityFilter icingProbabilityFilter = new IcingProbabilityFilter();

    TLcdDimensionAxis<TLcdISO19103Measure> heightAxis = aIcingModel.getHeightAxis();
    multiDimensionalRangeFilter.setMinimumValue(heightAxis, aIcingModel.getAxisValue(heightAxis, 0));
    multiDimensionalRangeFilter.setMaximumValue(heightAxis, aIcingModel.getAxisValue(heightAxis, aIcingModel.getNumberOfValues(heightAxis) - 1));

    CompositeFilter<IcingSeverityContour> compositeFilter = new CompositeFilter<>(Arrays.<ALcdDynamicFilter<? super IcingSeverityContour>>asList(multiDimensionalRangeFilter, icingSeverityFilter, icingProbabilityFilter));

    CachingExtrudedShapeDiscretizer shapeDiscretizer = new CachingExtrudedShapeDiscretizer();
    TLspShapePainter painter = new TLspShapePainter();
    painter.setShapeDiscretizer(shapeDiscretizer);

    return TLspShapeLayerBuilder.newBuilder().model(aIcingModel.getIcingSeverityModel())
                                .bodyStyler(TLspPaintState.REGULAR, new ExtrudedIcingSeverityContourStyler(aIcingModel))
                                .bodyPainter(painter)
                                .selectable(false)
                                .filter(compositeFilter)
                                .label("Weather Icing Severity Extruded Contours")
                                .build();
  }

  static MultiDimensionalRangeFilter getMultiDimensionalRangeFilter(ILspLayer aLayer) {
    return getFilter(aLayer, MultiDimensionalRangeFilter.class);
  }

  static IcingSeverityFilter getSeverityFilter(ILspLayer aLayer) {
    return getFilter(aLayer, IcingSeverityFilter.class);
  }

  static IcingProbabilityFilter getProbabilityFilter(ILspLayer aLayer) {
    return getFilter(aLayer, IcingProbabilityFilter.class);
  }

  private static <T> T getFilter(ILspLayer aLayer, Class<T> aFilterClass) {
    if (aLayer instanceof ALspLayer) {
      ALspLayer filteredLayer = (ALspLayer) aLayer;
      if (filteredLayer.getFilter() instanceof CompositeFilter) {
        CompositeFilter<?> filter = (CompositeFilter<?>) filteredLayer.getFilter();
        return filter.getFilter(aFilterClass);
      }
    }
    return null;
  }

  static class ExtrudedIcingSeverityContourStyler extends ALspStyler {

    private static final int UNIT_OF_MEASURE = 1000;
    private static final double DISTANCE_BETWEEN_2_ALTITUDES_IN_KM = 0.3048;

    private final IcingModel fIcingModel;

    public ExtrudedIcingSeverityContourStyler(IcingModel aIcingModel) {
      fIcingModel = aIcingModel;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (object instanceof IcingSeverityContour) {
          IcingSeverityContour contour = (IcingSeverityContour) object;
          aStyleCollector.object(contour)
                             .geometry(createExtrudedShape(fIcingModel, contour))
                             .styles(createStyles(contour))
                             .submit();
        }
      }
    }

    static List<ALspStyle> createStyles(IcingSeverityContour aContour) {
      List<ALspStyle> styles = new ArrayList<>();

      Color color = new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), getAlpha(aContour));
      styles.add(TLspFillStyle.newBuilder().color(color).build());

      return styles;
    }

    private static int getAlpha(IcingSeverityContour aContour) {
      return (int) Math.pow(3, aContour.getSeverity().getCode());
    }

    static ILcdShape createExtrudedShape(IcingModel aIcingModel, IntervalContour aContour) {
      TLcdDimensionAxis<TLcdISO19103Measure> altitudeAxis = aIcingModel.getHeightAxis();
      TLcdDimensionInterval<TLcdISO19103Measure> altitude = aContour.getMultiDimensionalValue().getValue(altitudeAxis);


      double minZ = altitude.getMin().doubleValue() / UNIT_OF_MEASURE;
      double maxZ = minZ + DISTANCE_BETWEEN_2_ALTITUDES_IN_KM;

      return new TLcdExtrudedShape(aContour.getShape(), minZ, maxZ);
    }

  }

}
