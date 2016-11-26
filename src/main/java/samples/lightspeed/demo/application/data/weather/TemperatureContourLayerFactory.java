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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.luciad.model.ILcdModel;
import com.luciad.util.ALcdDynamicFilter;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

class TemperatureContourLayerFactory {

  public ILspLayer createLayer(ILcdModel aILcdModel) {
    ContourModel contourModel = (ContourModel) aILcdModel;
    MultiDimensionalValue currentModelValue = contourModel.createMultiDimensionalValueForCurrentFilterParameters();
    MultiDimensionalFilter rasterMultiDimensionalFilter = new MultiDimensionalFilter(currentModelValue);
    ContourRasterValueFilter rasterValueFilter = new ContourRasterValueFilter();
    CompositeFilter<Contour> filter = new CompositeFilter<>(Arrays.<ALcdDynamicFilter<? super Contour>>asList(rasterMultiDimensionalFilter, rasterValueFilter));

    return TLspShapeLayerBuilder.newBuilder().model(contourModel)
                                             .bodyStyler(TLspPaintState.REGULAR, new ContourBodyStyler())
                                             .labelScaleRange(new TLcdInterval(0, Double.MAX_VALUE))
                                             .filter(filter)
                                             .selectable(false)
                                             .label("Weather Temperature Contours")
                                             .build();
  }

  static MultiDimensionalFilter getDimensionFilter(ILspLayer aLayer) {
    return getFilter(aLayer, MultiDimensionalFilter.class);
  }

  static ContourRasterValueFilter getContourRasterValueFilter(ILspLayer aLayer) {
    return getFilter(aLayer, ContourRasterValueFilter.class);
  }

  private static <T> T getFilter(ILspLayer aLayer, Class<T> aFilterClass) {
    if (aLayer instanceof ALspLayer) {
      ALspLayer filteredLayer = (ALspLayer) aLayer;
      CompositeFilter<?> compositeFilter = (CompositeFilter<?>) filteredLayer.getFilter();
      return compositeFilter.getFilter(aFilterClass);
    }
    return null;
  }

  static class CompositeFilter<T> extends ALcdDynamicFilter<T> {

    private final Collection<ALcdDynamicFilter<? super T>> fFilters;

    public CompositeFilter(Collection<ALcdDynamicFilter<? super T>> filters) {
      fFilters = filters;
      for (ALcdDynamicFilter<? super T> filter : fFilters) {
        filter.addChangeListener(new ILcdChangeListener() {
          @Override
          public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
            CompositeFilter.this.fireChangeEvent();
          }
        });
      }
    }

    @Override
    public boolean accept(T aObject) {
      for (ALcdDynamicFilter<? super T> filter : fFilters) {
        if (!filter.accept(aObject)) {
          return false;
        }
      }
      return true;
    }

    public Collection<ALcdDynamicFilter<? super T>> getFilters() {
      return Collections.unmodifiableCollection(fFilters);
    }

    public <F> F getFilter(Class<F> aFilterClass) {
      for (ALcdDynamicFilter<? super T> filter : getFilters()) {
        if (aFilterClass.isAssignableFrom(filter.getClass())) {
          return aFilterClass.cast(filter);
        }
      }

      return null;
    }

  }

}
