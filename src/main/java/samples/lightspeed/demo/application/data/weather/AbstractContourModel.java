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

import static samples.lightspeed.demo.application.data.weather.TemperatureUnit.CELSIUS;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.util.iso19103.TLcdISO19103Measure;

import samples.lightspeed.imaging.ImageMeasureMatrixView;

abstract class AbstractContourModel extends TLcdVectorModel {

  private static final int TEMPERATURE_INTERVAL = 5;

  private final double[] fLevels;
  private final TLcdNetCDFFilteredModel fModel;

  public AbstractContourModel(TLcdNetCDFFilteredModel aModel) {
    fModel = aModel;
    setModelReference(aModel.getModelReference());
    MatrixViews imageMatrixViews = new MatrixViews(aModel);
    fLevels = calculateLevels(imageMatrixViews.getMinimumValue(), imageMatrixViews.getMaximumValue());
    createContours(imageMatrixViews);
  }

  private double[] calculateLevels(double aMin, double aMax) {
    double newMin = CELSIUS.toKelvin(getClosestLowerMultipleOfTemperatureInterval(CELSIUS.fromKelvin(aMin)));
    double newMax = CELSIUS.toKelvin(getClosestHigherMultipleOfTemperatureInterval(CELSIUS.fromKelvin(aMax)));
    int range = (int) Math.round(newMax - newMin);
    int numberOfBuckets = range / TEMPERATURE_INTERVAL;
    double[] levels = new double[numberOfBuckets + 1];
    for (int i = 0; i < levels.length; i++) {
      levels[i] = newMin + i * TEMPERATURE_INTERVAL;
    }
    return levels;
  }

  private double getClosestLowerMultipleOfTemperatureInterval(double aCelsius) {
    return Math.floor(aCelsius / TEMPERATURE_INTERVAL) * TEMPERATURE_INTERVAL;
  }

  private double getClosestHigherMultipleOfTemperatureInterval(double aCelsius) {
    return Math.ceil(aCelsius / TEMPERATURE_INTERVAL) * TEMPERATURE_INTERVAL;
  }

  protected abstract void createContours(MatrixViews aMatrixViews);

  public double[] getLevels() {
    return fLevels;
  }

  public TLcdNetCDFFilteredModel getModel() {
    return fModel;
  }

  static class MatrixViews implements Iterable<Map.Entry<MultiDimensionalValue, ILcdMatrixView>> {

    //Each point (MultiDimensionalValue) has a corresponding ILcdMatrixView with which contours can be calculated
    private final Map<MultiDimensionalValue, ILcdMatrixView> fMatrixViews;
    private double fMinimumValue;
    private double fMaximumValue;

    public MatrixViews(TLcdNetCDFFilteredModel aNetCDFFilteredModel) {
      fMatrixViews = createImageMatrixViews(aNetCDFFilteredModel);
      fMinimumValue = getMinimumValue(fMatrixViews);
      fMaximumValue = getMaximumValue(fMatrixViews);
    }

    public double getMinimumValue() {
      return fMinimumValue;
    }

    public double getMaximumValue() {
      return fMaximumValue;
    }

    private static Map<MultiDimensionalValue, ILcdMatrixView> createImageMatrixViews(TLcdNetCDFFilteredModel aModel) {
      TLcdDimensionAxis<Date> timeAxis = WeatherUtil.getTimeAxis(aModel);
      TLcdDimensionAxis<TLcdISO19103Measure> depthAxis = WeatherUtil.getPressureAxis(aModel);

      Iterable<TLcdDimensionInterval<Date>> timeValues = aModel.getDimension(timeAxis).getValues();
      Iterable<TLcdDimensionInterval<TLcdISO19103Measure>> depthValues = aModel.getDimension(depthAxis).getValues();

      Map<MultiDimensionalValue, ILcdMatrixView> matrixViews = new ConcurrentHashMap<>();
      for (TLcdDimensionInterval<Date> timeValue : timeValues) {
        for (TLcdDimensionInterval<TLcdISO19103Measure> depthValue : depthValues) {
          TLcdDimensionFilter.Builder filterBuilder = TLcdDimensionFilter.newBuilder();
          filterBuilder.filterDimension(timeAxis, timeValue);
          filterBuilder.filterDimension(depthAxis, depthValue);
          TLcdDimensionFilter snappedFilter = filterBuilder.build().createSnappingFilter(aModel, TLcdDimensionFilter.SnapMode.NEAREST);
          aModel.applyDimensionFilter(snappedFilter, ILcdModel.FIRE_NOW);
          ILcdMatrixView matrixView = new CachingMatrixViewDecorator(new ImageMeasureMatrixView(aModel));
          if (isValid(matrixView)) {
            MultiDimensionalValue multiDimensionalValue = new MultiDimensionalValue(new InterpolatedAxisValue(timeAxis, timeValue), new InterpolatedAxisValue(depthAxis, depthValue));
            matrixViews.put(multiDimensionalValue, matrixView);
          }
        }
      }
      setTimeBackToStart(aModel, timeAxis);
      return matrixViews;
    }

    /**
     * The constructed ILcdMatrixView cannot be used to calculate contours if all values are NaNs.
     * If that is the case, the minimum and the maximum will be Double.NaN
     * and this interval is not valid.
     * @return boolean indicating whether this class can be used to calculate contours.
     */
    private static boolean isValid(ILcdMatrixView aMatrixView) {
      return !Double.isNaN(calculateMin(aMatrixView)) && !Double.isNaN(calculateMax(aMatrixView));
    }

    private static void setTimeBackToStart(TLcdNetCDFFilteredModel aNetCDFModel, TLcdDimensionAxis<Date> aTimeAxis) {
      TLcdDimensionFilter.Builder filterBuilder = aNetCDFModel.getDimensionFilter().asBuilder();
      filterBuilder.filterDimension(aTimeAxis, aNetCDFModel.getDimension(aTimeAxis).getValues().iterator().next());
      aNetCDFModel.applyDimensionFilter(filterBuilder.build(), ILcdModel.FIRE_NOW);
    }

    private static double getMinimumValue(Map<MultiDimensionalValue, ILcdMatrixView> aMatrixViews) {
      double min = Double.POSITIVE_INFINITY;
      Collection<ILcdMatrixView> views = aMatrixViews.values();
      for (ILcdMatrixView view : views) {
        double viewMin = calculateMin(view);
        if (viewMin < min) {
          min = viewMin;
        }
      }
      return min == Double.POSITIVE_INFINITY ? Double.NaN : min;
    }

    private static double getMaximumValue(Map<MultiDimensionalValue, ILcdMatrixView> aMatrixViews) {
      double max = Double.NEGATIVE_INFINITY;
      Collection<ILcdMatrixView> views = aMatrixViews.values();
      for (ILcdMatrixView view : views) {
        double viewMax = calculateMax(view);
        if (viewMax > max) {
          max = viewMax;
        }
      }
      return max == Double.NEGATIVE_INFINITY ? Double.NaN : max;
    }

    private static double calculateMin(ILcdMatrixView aMatrixView) {
      if (aMatrixView instanceof CachingMatrixViewDecorator) {
        CachingMatrixViewDecorator cachingMatrixView = (CachingMatrixViewDecorator) aMatrixView;
        return cachingMatrixView.getMin();
      }
      double result = Double.POSITIVE_INFINITY;
      for (int i = 0; i < aMatrixView.getColumnCount(); i++) {
        for (int j = 0; j < aMatrixView.getRowCount(); j++) {
          double value = aMatrixView.getValue(i, j);
          if (!Double.isNaN(value) && value < result) {
            result = value;
          }
        }
      }
      return result == Double.POSITIVE_INFINITY ? Double.NaN : result;
    }

    private static double calculateMax(ILcdMatrixView aMatrixView) {
      if (aMatrixView instanceof CachingMatrixViewDecorator) {
        CachingMatrixViewDecorator cachingMatrixView = (CachingMatrixViewDecorator) aMatrixView;
        return cachingMatrixView.getMax();
      }
      double result = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < aMatrixView.getColumnCount(); i++) {
        for (int j = 0; j < aMatrixView.getRowCount(); j++) {
          double value = aMatrixView.getValue(i, j);
          if (!Double.isNaN(value) && value > result) {
            result = value;
          }
        }
      }
      return result == Double.NEGATIVE_INFINITY ? Double.NaN : result;
    }

    public ILcdMatrixView getMatrixView(MultiDimensionalValue aMultiDimensionalValue) {
      return fMatrixViews.get(aMultiDimensionalValue);
    }

    public void addMatrixView(MultiDimensionalValue aMultiDimensionalValue, ILcdMatrixView aMatrixView) {
      double viewMin = calculateMin(aMatrixView);
      if (viewMin < getMinimumValue()) {
        fMinimumValue = viewMin;
      }
      double viewMax = calculateMax(aMatrixView);
      if (viewMax > getMaximumValue()) {
        fMaximumValue = viewMax;
      }
      fMatrixViews.put(aMultiDimensionalValue, aMatrixView);
    }

    @Override
    public Iterator<Map.Entry<MultiDimensionalValue, ILcdMatrixView>> iterator() {
      return fMatrixViews.entrySet().iterator();
    }

  }

}
