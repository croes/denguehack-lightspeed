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

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;

/**
 * A slider for a dimension of a MultiDimensionalModel.
 * @param <T> the type of a TLcdDimensionAxis.
 */
class DimensionSlider<T> extends AbstractDimensionSlider {

  private final MultiDimensionalModel fMultiDimensionalModel;
  private final TLcdDimensionAxis<T> fDimensionAxis;
  private final List<? extends DimensionalFilter> fDimensionalFilters;

  public DimensionSlider(MultiDimensionalModel aMultiDimensionalModel, TLcdDimensionAxis<T> aDimensionAxis, List<? extends DimensionalFilter> aDimensionalFilters) {
    super(HORIZONTAL, 0, getMaxModelValueIndex(aMultiDimensionalModel, aDimensionAxis),
          getIndexOfCurrentModelValue(aMultiDimensionalModel, aDimensionAxis));
    fMultiDimensionalModel = aMultiDimensionalModel;
    fDimensionAxis = aDimensionAxis;
    fDimensionalFilters = aDimensionalFilters;
    setOpaque(false);
    setMajorTickSpacing(1);
    setPaintTicks(true);
    setSnapToTicks(false);
    addChangeListener(new SliderChangeListener());
  }

  private static int getIndexOfCurrentModelValue(MultiDimensionalModel aMultiDimensionalModel, TLcdDimensionAxis aDimensionAxis) {
    return aMultiDimensionalModel.getIndexOfValue(aDimensionAxis, getModelValue(aMultiDimensionalModel, aDimensionAxis));
  }

  private static int getMaxModelValueIndex(MultiDimensionalModel aMultiDimensionalModel, TLcdDimensionAxis aDimensionAxis) {
    return aMultiDimensionalModel.getNumberOfValues(aDimensionAxis) - 1;
  }

  public TLcdDimensionInterval<T> getAxisValue() {
      return fMultiDimensionalModel.getAxisValue(fDimensionAxis, getValue());
  }

  @Override
  String getTextFormattedAxisValue() {
    return WeatherUtil.format(getAxisValue().getMax());
  }

  private static TLcdDimensionInterval<?> getModelValue(MultiDimensionalModel aMultiDimensionalModel, TLcdDimensionAxis<?> aDimensionAxis) {
    return aMultiDimensionalModel.getCurrentValue(aDimensionAxis);
  }

  private class SliderChangeListener implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
      updateExtrudedContourModel();
    }

    private void updateExtrudedContourModel() {
      TLcdDimensionInterval<T> closestAxisValueOfParentModel = getAxisValue();

      for (DimensionalFilter filter : fDimensionalFilters) {
        filter.setValue(fDimensionAxis, closestAxisValueOfParentModel);
      }
    }

  }

}
