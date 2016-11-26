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

import static samples.lightspeed.demo.application.data.weather.TemperatureContourLayerFactory.getDimensionFilter;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.model.ILcdModel;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * A slider for a dimension of the temperature model of the WeatherModel.
 * This slider creates new contours for the WeatherModel for dimension values between those of the temperature model.
 * @param <T> the type of a TLcdDimensionAxis.
 */
class DimensionInterpolationSlider<T> extends AbstractDimensionSlider {

  private static final int FACTOR = 70;

  private final WeatherModel fWeatherModel;
  private final TLcdDimensionAxis<T> fDimensionAxis;
  private final OperationMode fOperationMode;
  private final List<ILspLayer> fTemperatureContourLayers;
  private DimensionInterpolationSliderGroup fDimensionInterpolationSliderGroup;

  public DimensionInterpolationSlider(WeatherModel aWeatherModel, TLcdDimensionAxis<T> aDimensionAxis, List<ILspLayer> aTemperatureContourLayers) {
    this(aWeatherModel, aDimensionAxis, OperationMode.NORMAL, aTemperatureContourLayers);
  }

  public void setDimensionInterpolationSliderGroup(DimensionInterpolationSliderGroup aDimensionInterpolationSliderGroup) {
    fDimensionInterpolationSliderGroup = aDimensionInterpolationSliderGroup;
  }

  public DimensionInterpolationSlider(WeatherModel aWeatherModel, TLcdDimensionAxis<T> aDimensionAxis, OperationMode aOperationMode, List<ILspLayer> aTemperatureContourLayers) {
    super(HORIZONTAL, 0, toSliderValue(getMaxModelValueIndex(aWeatherModel, aDimensionAxis)),
          toSliderValue(getIndexOfCurrentModelValueAccordingToOperationMode(aWeatherModel, aDimensionAxis, aOperationMode, aTemperatureContourLayers)));
    fWeatherModel = aWeatherModel;
    fDimensionAxis = aDimensionAxis;
    fOperationMode = aOperationMode;
    fTemperatureContourLayers = aTemperatureContourLayers;
    setOpaque(false);
    setPaintTicks(true);
    setMajorTickSpacing(FACTOR);
    setSnapToTicks(false);
    addChangeListener(new SliderChangeListener());
  }

  private static int getIndexOfCurrentModelValueAccordingToOperationMode(WeatherModel aWeatherModel, TLcdDimensionAxis aDimensionAxis, OperationMode aOperationMode, List<ILspLayer> aTemperatureContourLayers) {
    return aOperationMode.getValue(getMaxModelValueIndex(aWeatherModel, aDimensionAxis), getIndexOfCurrentModelValue(aWeatherModel, aDimensionAxis, aTemperatureContourLayers));
  }

  private static int getIndexOfCurrentModelValue(WeatherModel aWeatherModel, TLcdDimensionAxis aDimensionAxis, List<ILspLayer> aTemperatureContourLayers) {
    return aWeatherModel.getIndexOfValue(aDimensionAxis, getFilterValue(aWeatherModel.getTemperatureModel(), aDimensionAxis, aTemperatureContourLayers));
  }

  private static int getMaxModelValueIndex(WeatherModel aWeatherModel, TLcdDimensionAxis aDimensionAxis) {
    return aWeatherModel.getNumberOfValues(aDimensionAxis) - 1;
  }

  public TLcdDimensionAxis<T> getDimensionAxis() {
    return fDimensionAxis;
  }

  private MultiDimensionalValue getInterpolatedAxisValueAsMultiDimensionalValue() {
    return toMultiDimensionalValue(getInterpolatedAxisValue());
  }

  public InterpolatedAxisValue getInterpolatedAxisValue() {
    int sliderValue = getSliderValue();
    int indexOfLowerBoundAxisValue = fromSliderValue(sliderValue);
    int indexOfUpperBoundAxisValue = fromSliderValue(sliderValue + FACTOR);
    if (isSliderSetToOriginalValue()) {
      TLcdDimensionInterval<T> value = fWeatherModel.getAxisValue(fDimensionAxis, indexOfLowerBoundAxisValue);
      return toInterpolatedAxisValue(value);
    } else {
      TLcdDimensionInterval<T> lowerBoundAxisValue = fWeatherModel.getAxisValue(fDimensionAxis, indexOfLowerBoundAxisValue);
      TLcdDimensionInterval<T> upperBoundAxisValue = fWeatherModel.getAxisValue(fDimensionAxis, indexOfUpperBoundAxisValue);
      double blendFactor = (toSliderValue(indexOfUpperBoundAxisValue) - sliderValue) / (double) FACTOR;
      return toInterpolatedAxisValue(lowerBoundAxisValue, upperBoundAxisValue, blendFactor);
    }
  }

  public TLcdDimensionInterval<T> getClosestAxisValueOfTemperatureModel() {
    int sliderValue = getSliderValue();
    int indexOfLowerBoundAxisValue = fromSliderValue(sliderValue);
    int indexOfUpperBoundAxisValue = fromSliderValue(sliderValue + FACTOR);
    if (isSliderSetToOriginalValue()) {
      return fWeatherModel.getAxisValue(fDimensionAxis, indexOfLowerBoundAxisValue);
    } else {
      TLcdDimensionInterval<T> lowerBoundAxisValue = fWeatherModel.getAxisValue(fDimensionAxis, indexOfLowerBoundAxisValue);
      TLcdDimensionInterval<T> upperBoundAxisValue = fWeatherModel.getAxisValue(fDimensionAxis, indexOfUpperBoundAxisValue);
      double blendFactor = (toSliderValue(indexOfUpperBoundAxisValue) - sliderValue) / (double) FACTOR;
      if (blendFactor < 0.5d) {
        return upperBoundAxisValue;
      } else {
        return lowerBoundAxisValue;
      }
    }
  }

  @Override
  String getTextFormattedAxisValue() {
    return WeatherUtil.format(getInterpolatedAxisValue().getInterpolatedValue().getMax());
  }

  private boolean isSliderSetToOriginalValue() {
    return getSliderValue() % FACTOR == 0;
  }

  private int getSliderValue() {
    return fOperationMode.getValue(getMaximum(), getValue());
  }

  private InterpolatedAxisValue toInterpolatedAxisValue(TLcdDimensionInterval<T> aLowerBoundAxisValue, TLcdDimensionInterval<T> aUpperBoundAxisValue, double aBlendFactor) {
    return new InterpolatedAxisValue(new AxisValue(fDimensionAxis, aLowerBoundAxisValue), new AxisValue(fDimensionAxis, aUpperBoundAxisValue), aBlendFactor);
  }

  private InterpolatedAxisValue toInterpolatedAxisValue(TLcdDimensionInterval<T> aValue) {
    return new InterpolatedAxisValue(new AxisValue(fDimensionAxis, aValue));
  }

  private MultiDimensionalValue toMultiDimensionalValue(InterpolatedAxisValue aInterpolatedAxisValue) {
    if (fDimensionInterpolationSliderGroup == null) {
      MultiDimensionalValue value = fWeatherModel.createMultiDimensionalValueForCurrentFilterParameters();
      return MultiDimensionalValue.from(value, aInterpolatedAxisValue);
    } else {
      return fDimensionInterpolationSliderGroup.toMultiDimensionalValue(aInterpolatedAxisValue);
    }
  }

  private class SliderChangeListener implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
      updateTemperatureModel();
      updateWindModel();
      updateTemperatureContours();
    }

    private void updateTemperatureModel() {
      TLcdNetCDFFilteredModel temperatureModel = fWeatherModel.getTemperatureModel();
      TLcdDimensionFilter.Builder filterBuilder = temperatureModel.getDimensionFilter().asBuilder();
      try (TLcdLockUtil.Lock ignored = TLcdLockUtil.writeLock(temperatureModel)) {
        filterBuilder.filterDimension(fDimensionAxis, getClosestAxisValueOfTemperatureModel());
        TLcdDimensionFilter snappedFilter = filterBuilder.build().createSnappingFilter(temperatureModel, TLcdDimensionFilter.SnapMode.NEAREST);
        temperatureModel.applyDimensionFilter(snappedFilter, ILcdModel.FIRE_LATER);
      } finally {
        temperatureModel.fireCollectedModelChanges();
      }
    }

    private void updateWindModel() {
      TLcdNetCDFFilteredModel windModel = fWeatherModel.getWindModel();
      TLcdDimensionFilter.Builder filterBuilder = windModel.getDimensionFilter().asBuilder();
      try (TLcdLockUtil.Lock ignored = TLcdLockUtil.writeLock(windModel)) {
        filterBuilder.filterDimension(fDimensionAxis, getClosestAxisValueOfTemperatureModel());
        TLcdDimensionFilter snappedFilter = filterBuilder.build().createSnappingFilter(windModel, TLcdDimensionFilter.SnapMode.NEAREST);
        windModel.applyDimensionFilter(snappedFilter, ILcdModel.FIRE_LATER);
      } finally {
        windModel.fireCollectedModelChanges();
      }
    }

    private void updateTemperatureContours() {
      MultiDimensionalValue multiDimensionalValue = getInterpolatedAxisValueAsMultiDimensionalValue();
      ContourModel contourModel = fWeatherModel.getContourModel();
      if (!contourModel.hasContoursFor(multiDimensionalValue)) {
        try (TLcdLockUtil.Lock ignored = TLcdLockUtil.writeLock(contourModel)) {
          contourModel.createContours(multiDimensionalValue);
        } finally {
          contourModel.fireCollectedModelChanges();
        }
      }

      for (ILspLayer layer : fTemperatureContourLayers) {
        MultiDimensionalFilter filter = getDimensionFilter(layer);
        if (filter != null) {
          filter.setValue(fDimensionAxis, multiDimensionalValue.getValue(fDimensionAxis));
        }
      }
    }

  }

  private static TLcdDimensionInterval<?> getFilterValue(TLcdNetCDFFilteredModel aTemperatureModel, TLcdDimensionAxis<?> aDimensionAxis, List<ILspLayer> aTemperatureContourLayers) {
    ILspLayer layer = aTemperatureContourLayers.iterator().next();
    MultiDimensionalFilter multiDimensionalFilter = getDimensionFilter(layer);
    if (multiDimensionalFilter != null) {
      return multiDimensionalFilter.getValue().getValue(aDimensionAxis);
    }
    return aTemperatureModel.getDimensionFilter().getInterval(aDimensionAxis);
  }

  enum OperationMode {
    NORMAL {
      @Override
      int getValue(int aMaximum, int n) {
        return n;
      }
    }, INVERTED {
      @Override
      int getValue(int aMaximum, int n) {
        return aMaximum - n;
      }

    };

    abstract int getValue(int aMaximum, int n);
  }

  private static int toSliderValue(int aValue) {
    return aValue * FACTOR;
  }

  private static int fromSliderValue(int aValue) {
    return aValue / FACTOR;
  }

}
