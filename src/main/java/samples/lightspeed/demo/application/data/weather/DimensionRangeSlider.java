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

import com.luciad.gui.swing.TLcdRangeSlider;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.iso19103.TLcdISO19103Measure;

/**
 * A range slider for a dimension of a MultiDimensionalModel.
 */
class DimensionRangeSlider extends TLcdRangeSlider {

  private final MultiDimensionalModel fModel;
  private final TLcdDimensionAxis<? extends TLcdISO19103Measure> fDimensionAxis;
  private final DimensionRangeListener fListener;
  private final List<MultiDimensionalRangeFilter> fMultiDimensionalRangeFilters;

  public DimensionRangeSlider(MultiDimensionalModel aMultiDimensionalModel, TLcdDimensionAxis<? extends TLcdISO19103Measure> aDimensionAxis, List<MultiDimensionalRangeFilter> aMultiDimensionalRangeFilters) {
    super(0, aMultiDimensionalModel.getNumberOfValues(aDimensionAxis) - 1);
    fModel = aMultiDimensionalModel;
    fDimensionAxis = aDimensionAxis;
    fMultiDimensionalRangeFilters = aMultiDimensionalRangeFilters;
    setPaintMajorTicks(false);
    setPaintMinorTicks(false);
    setSnapToTicks(false);
    setOpaque(false);
    fListener = new DimensionRangeListener();
    addChangeListener(fListener);
  }

  public TLcdDimensionInterval<? extends TLcdISO19103Measure> getMinimumAxisValue() {
    return fModel.getAxisValue(fDimensionAxis, toInt(getRangeMinimum()));
  }

  public TLcdDimensionInterval<? extends TLcdISO19103Measure> getMaximumAxisValue() {
    return fModel.getAxisValue(fDimensionAxis, toInt(getRangeMaximum()));
  }

  public String getTextFormattedAxisValue() {
    return WeatherUtil.format(getMinimumAxisValue().getMax()) + " to " + WeatherUtil.format(getMaximumAxisValue().getMax());
  }

  private int toInt(double aValue) {
    return WeatherUtil.round(aValue, 0);
  }

  public DimensionRangeListener getListener() {
    return fListener;
  }

  class DimensionRangeListener implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
      updateModel();
    }

    private void updateModel() {
      TLcdDimensionInterval<?> min = getMinimumAxisValue();
      TLcdDimensionInterval<?> max = getMaximumAxisValue();

      for (MultiDimensionalRangeFilter filter : fMultiDimensionalRangeFilters) {
        filter.setMinimumValue(fDimensionAxis, min);
        filter.setMaximumValue(fDimensionAxis, max);
      }
    }

  }

}
