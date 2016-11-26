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
package samples.common.dimensionalfilter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.util.service.LcdService;
import com.luciad.model.ILcdModel;
import com.luciad.multidimensional.ILcdDimension;
import com.luciad.multidimensional.ILcdMultiDimensionalModel;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * A filter provider implementation which creates filters for <code>ILcdMultiDimensionalModel</code> instances.
 * @see ILcdMultiDimensionalModel
 * @since 2016.0
 *
 */
@LcdService(service = DimensionalFilterProvider.class)
public class FilterProvider extends ADimensionalFilterProvider {
  @Override
  public boolean canHandleLayer(ILcdLayer aLayer, ILcdLayered aLayered) {
    return aLayer.getModel() instanceof ILcdMultiDimensionalModel;
  }

  @Override
  public List<DimensionalFilter> createFilters(ILcdLayer aLayer, ILcdLayered aLayered) {
    List<DimensionalFilter> dimensionalFilters = new ArrayList<>();
    ILcdModel model = aLayer.getModel();
    ILcdMultiDimensionalModel filteredModel = (ILcdMultiDimensionalModel) model;
    List<? extends ILcdDimension<?>> dimensions = filteredModel.getDimensions();
    if (dimensions != null) {
      for (ILcdDimension dimension : dimensions) {
        if (shouldHandleDimensionAxis(dimension)) {
          DimensionalFilter dimensionalFilter = createFilter(aLayered, aLayer, dimension);
          dimensionalFilters.add(dimensionalFilter);
        }
      }
    }
    return dimensionalFilters;
  }

  protected boolean shouldHandleDimensionAxis(ILcdDimension aDimension) {
    return !aDimension.getUnionOfValues().isSingleValue();
  }

  @SuppressWarnings("unchecked")
  private DimensionalFilter createFilter(ILcdLayered aLayered,
                                         final ILcdLayer aLayer,
                                         final ILcdDimension<?> aDimension) {
    TLcdDimensionAxis<?> axis = aDimension.getAxis();
    Collection<Comparable> possibleValues = new ArrayList<>();
    for (TLcdDimensionInterval<?> values : aDimension.getValues()) {
      if (values.isSingleValue()) {
        possibleValues.add((Comparable) values.getMin());
      } else {
        if (values.getMin() != null) {
          possibleValues.add((Comparable) values.getMin());
        }
        if (values.getMax() != null) {
          possibleValues.add((Comparable) values.getMax());
        }
      }
    }
    return new DimensionalFilter(aLayered, aLayer, (Class<? extends Comparable>) axis.getType(), axis.getUnit(), axis.isPositive(), possibleValues) {
      @Override
      public void setFilterValue(Comparable aValue) {
        Class type = getType();
        ILcdMultiDimensionalModel model = (ILcdMultiDimensionalModel) aLayer.getModel();
        try (TLcdLockUtil.Lock ignored = TLcdLockUtil.writeLock(model)) {
          TLcdDimensionInterval<?> newValue = TLcdDimensionInterval.createSingleValue(
              type,
              aValue
          );
          TLcdDimensionFilter filter = TLcdDimensionFilter.newBuilder().filterDimension(aDimension.getAxis(), newValue).build();
          TLcdDimensionFilter snappedFilter = filter.createSnappingFilter(model, TLcdDimensionFilter.SnapMode.NEAREST);
          model.applyDimensionFilter(snappedFilter, ILcdModel.FIRE_LATER);
        } finally {
          aLayer.getModel().fireCollectedModelChanges();
        }
      }

      @Override
      public boolean canGetFilterValueFromModel(ILcdModel aModel) {
        return aModel == aLayer.getModel();
      }

      @Override
      public Comparable getFilterValueFromModel(ILcdModel aModel) {
        if (aModel == aLayer.getModel()) {
          TLcdDimensionInterval<?> interval = ((ILcdMultiDimensionalModel) aModel).getDimensionFilter().getInterval(aDimension.getAxis());
          return interval != null ? (Comparable) interval.getMin() : null;
        } else {
          throw new IllegalArgumentException("Model is not filtered by this filter: " + aModel);
        }

      }

      @Override
      public String getName() {
        if (getUnit() != null) {
          return getUnit().getMeasureType().getName();
        }
        return getType().getSimpleName();
      }
    };
  }
}
