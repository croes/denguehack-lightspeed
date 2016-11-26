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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;

class MultiDimensionalValue {

  private final Map<TLcdDimensionAxis<?>, InterpolatedAxisValue> fAxisValues;

  public MultiDimensionalValue(InterpolatedAxisValue... aAxisValues) {
    fAxisValues = new HashMap<>(aAxisValues.length);
    for (InterpolatedAxisValue axisValue : aAxisValues) {
      fAxisValues.put(axisValue.getDimensionAxis(), axisValue);
    }
  }

  public MultiDimensionalValue(Collection<InterpolatedAxisValue> aAxisValues) {
    this(aAxisValues.toArray(new InterpolatedAxisValue[aAxisValues.size()]));
  }

  public Set<TLcdDimensionAxis<?>> getDimensionAxes() {
    return Collections.unmodifiableSet(fAxisValues.keySet());
  }

  public InterpolatedAxisValue getAxisValue(TLcdDimensionAxis<?> aDimensionAxis) {
    return fAxisValues.get(aDimensionAxis);
  }

  public <T> TLcdDimensionInterval<T> getValue(TLcdDimensionAxis<T> aDimensionAxis) {
    return (TLcdDimensionInterval<T>) fAxisValues.get(aDimensionAxis).getInterpolatedValue();
  }

  public static MultiDimensionalValue from(MultiDimensionalValue aMultiDimensionalValue, TLcdDimensionAxis<?> aDimensionAxis, TLcdDimensionInterval<?> aValue) {
    List<InterpolatedAxisValue> axisValues = new ArrayList<>(aMultiDimensionalValue.getDimensionAxes().size());
    for (TLcdDimensionAxis<?> dimensionAxis : aMultiDimensionalValue.getDimensionAxes()) {
      if (dimensionAxis.equals(aDimensionAxis)) {
        axisValues.add(new InterpolatedAxisValue(new AxisValue(aDimensionAxis, aValue)));
      } else {
        axisValues.add(new InterpolatedAxisValue(new AxisValue(dimensionAxis, aMultiDimensionalValue.getValue(dimensionAxis))));
      }
    }
    return new MultiDimensionalValue(axisValues);
  }

  public static MultiDimensionalValue from(MultiDimensionalValue aMultiDimensionalValue, InterpolatedAxisValue aInterpolatedAxisValue) {
    List<InterpolatedAxisValue> axisValues = new ArrayList<>(aMultiDimensionalValue.getDimensionAxes().size());
    for (TLcdDimensionAxis<?> dimensionAxis : aMultiDimensionalValue.getDimensionAxes()) {
      if (dimensionAxis.equals(aInterpolatedAxisValue.getDimensionAxis())) {
        axisValues.add(aInterpolatedAxisValue);
      } else {
        axisValues.add(new InterpolatedAxisValue(new AxisValue(dimensionAxis, aMultiDimensionalValue.getValue(dimensionAxis))));
      }
    }
    return new MultiDimensionalValue(axisValues);
  }

  public static boolean rangeContainsValue(MultiDimensionalValue aFirstBound, MultiDimensionalValue aSecondBound, MultiDimensionalValue aValue) {
    for (TLcdDimensionAxis<?> axis : aValue.getDimensionAxes()) {
      TLcdDimensionInterval<?> value = aValue.getValue(axis);
      TLcdDimensionInterval firstBoundValue = aFirstBound.getValue(axis);
      TLcdDimensionInterval secondBoundValue = aSecondBound.getValue(axis);
      if (value.compareTo(firstBoundValue) < 0 || value.compareTo(secondBoundValue) > 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !MultiDimensionalValue.class.isAssignableFrom(o.getClass())) {
      return false;
    }

    MultiDimensionalValue that = (MultiDimensionalValue) o;

    return fAxisValues.equals(that.fAxisValues);
  }

  @Override
  public int hashCode() {
    return fAxisValues.hashCode();
  }

}
