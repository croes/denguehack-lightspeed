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

import static samples.lightspeed.demo.application.data.weather.WeatherUtil.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdModelTreeNode;
import com.luciad.multidimensional.ILcdDimension;
import com.luciad.multidimensional.TLcdDimension;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.iso19103.TLcdISO19103Measure;

class WeatherModel extends TLcdModelTreeNode implements MultiDimensionalModel {

  private static final Date INVALID_DATE = new DateTime(2004, DateTimeConstants.MARCH, 18, 19, 0, 0).toDate();

  private final ILcdDimension<Date> fTimeDimension;

  public WeatherModel(TLcdNetCDFFilteredModel aTemperatureModel, ContourModel aContourModel, TLcdNetCDFFilteredModel aWindModel) {
    setModelDescriptor(new WeatherModelDescriptor());
    addModel(aTemperatureModel);
    addModel(aContourModel);
    addModel(aWindModel);

    fTimeDimension = createTimeDimension();
    synchronizeWindModelParametersWithTemperatureModel();
  }

  private void synchronizeWindModelParametersWithTemperatureModel() {
    List<? extends ILcdDimension<?>> dimensions = getWindModel().getDimensions();
    TLcdDimensionFilter.Builder filterBuilder = TLcdDimensionFilter.newBuilder();
    for (ILcdDimension<?> dimension : dimensions) {
      TLcdDimensionAxis<?> axis = dimension.getAxis();
      filterBuilder.filterDimension(axis, getTemperatureModel().getDimensionFilter().getInterval(axis));
    }
    getWindModel().applyDimensionFilter(filterBuilder.build(), FIRE_NOW);
  }

  public TLcdNetCDFFilteredModel getTemperatureModel() {
    return (TLcdNetCDFFilteredModel) getModel(0);
  }

  public ContourModel getContourModel() {
    return (ContourModel) getModel(1);
  }

  public TLcdNetCDFFilteredModel getWindModel() {
    return (TLcdNetCDFFilteredModel) getModel(2);
  }

  public double getMinimumRasterValue() {
    return getContourModel().getLevels()[0];
  }

  public double getMaximumRasterValue() {
    double[] levels = getContourModel().getLevels();
    return levels[levels.length - 1];
  }

  public <T> TLcdDimensionInterval<T> getAxisValue(TLcdDimensionAxis<T> aDimensionAxis, int aIndex) {
    return getAxisValues(aDimensionAxis).get(aIndex);
  }

  public int getNumberOfValues(TLcdDimensionAxis<?> aDimensionAxis) {
    return getAxisValues(aDimensionAxis).size();
  }

  public int getIndexOfValue(TLcdDimensionAxis<?> aDimensionAxis, TLcdDimensionInterval<?> aValue) {
    return getAxisValues(aDimensionAxis).indexOf(aValue);
  }

  private <T> List<TLcdDimensionInterval<T>> getAxisValues(TLcdDimensionAxis<T> aDimensionAxis) {
    return list(getOriginalDimensions(aDimensionAxis).getValues());
  }

  public TLcdDimensionAxis<Date> getTimeAxis() {
    return WeatherUtil.getTimeAxis(getTemperatureModel());
  }

  public TLcdDimensionAxis<TLcdISO19103Measure> getPressureAxis() {
    return WeatherUtil.getPressureAxis(getTemperatureModel());
  }

  @Override
  public TLcdDimensionInterval<?> getCurrentValue(TLcdDimensionAxis<?> aDimensionAxis) {
    return getTemperatureModel().getDimensionFilter().getInterval(aDimensionAxis);
  }

  public MultiDimensionalValue createMultiDimensionalValueForCurrentFilterParameters() {
    Collection<InterpolatedAxisValue> axisValues = new ArrayList<>();
    TLcdNetCDFFilteredModel model = getTemperatureModel();
    for (ILcdDimension<?> dimensions : model.getDimensions()) {
      TLcdDimensionAxis<?> dimensionAxis = dimensions.getAxis();
      axisValues.add(new InterpolatedAxisValue(dimensionAxis, model.getDimensionFilter().getInterval(dimensionAxis)));
    }
    return new MultiDimensionalValue(axisValues);
  }

  private <T> ILcdDimension<T> getOriginalDimensions(TLcdDimensionAxis<T> aDimensionAxis) {
    if (Date.class.isAssignableFrom(aDimensionAxis.getType())) {
      return (ILcdDimension<T>) fTimeDimension;
    } else {
      return getTemperatureModel().getDimension(aDimensionAxis);
    }
  }

  private ILcdDimension<Date> createTimeDimension() {
    TLcdDimensionAxis<Date> timeAxis = getTimeAxis();
    TLcdDimension.Builder<Date> axisValuesBuilder = TLcdDimension.<Date>newBuilder().axis(timeAxis);

    ILcdDimension<Date> axisValues = getTemperatureModel().getDimension(timeAxis);
    for (TLcdDimensionInterval<Date> dimensionInterval : axisValues.getValues()) {
      if (isValidDimensionInterval(dimensionInterval)) {
        axisValuesBuilder.addInterval(dimensionInterval);
      }
    }

    return axisValuesBuilder.build();
  }

  private <T> boolean isValidDimensionInterval(TLcdDimensionInterval<T> aDimensionInterval) {
    return !INVALID_DATE.equals(aDimensionInterval.getMax());
  }

  public static class WeatherModelDescriptor extends TLcdModelDescriptor {

    public WeatherModelDescriptor() {
      super("", "WeatherModel", "Weather model");
    }

  }

}
