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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelTreeNode;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.iso19103.TLcdISO19103Measure;

class IcingModel extends TLcdModelTreeNode implements MultiDimensionalModel {

  private final TLcdDimensionAxis<Date> fTimeAxis = TLcdDimensionAxis.TIME_AXIS;
  private final TLcdDimensionAxis<TLcdISO19103Measure> fAltitudeAxis = WeatherUtil.createAltitudeAxis();
  private final SortedSet<TLcdDimensionInterval<Date>> fDates = new TreeSet<>();
  private final SortedSet<TLcdDimensionInterval<TLcdISO19103Measure>> fAltitudes = new TreeSet<>();
  private final SortedSet<Double> fProbabilities = new TreeSet<>();
  private final List<Double> fProbabilitiesAsList;

  public IcingModel(IcingSeverityModel aIcingSeverityModel, IcingSLDModel aIcingSLDModel) {
    addModel(aIcingSeverityModel);
    addModel(aIcingSLDModel);

    Enumeration<IcingSeverityContour> elements = aIcingSeverityModel.elements();
    while (elements.hasMoreElements()) {
      IcingSeverityContour contour = elements.nextElement();
      fDates.add(contour.getMultiDimensionalValue().getValue(fTimeAxis));
      fAltitudes.add(contour.getMultiDimensionalValue().getValue(fAltitudeAxis));
      fProbabilities.add(contour.getProbability());
    }
    fProbabilitiesAsList = new ArrayList<>(fProbabilities);
  }

  public IcingSeverityModel getIcingSeverityModel() {
    return (IcingSeverityModel) getModel(0);
  }

  public IcingSLDModel getIcingSLDModel() {
    return (IcingSLDModel) getModel(1);
  }

  public SortedSet<Double> getProbabilities() {
    return fProbabilities;
  }

  public double getProbability(int aIndex) {
    return fProbabilitiesAsList.get(aIndex);
  }

  @Override
  public TLcdDimensionInterval<?> getCurrentValue(TLcdDimensionAxis<?> aDimensionAxis) {
    return createMultiDimensionalValueForFirstValue().getValue(aDimensionAxis);
  }

  @Override
  public TLcdDimensionAxis<Date> getTimeAxis() {
    return fTimeAxis;
  }

  public TLcdDimensionAxis<TLcdISO19103Measure> getHeightAxis() {
    return fAltitudeAxis;
  }

  @Override
  public <T> TLcdDimensionInterval<T> getAxisValue(TLcdDimensionAxis<T> aDimensionAxis, int aIndex) {
    return getAxisValues(aDimensionAxis).get(aIndex);
  }

  @Override
  public int getNumberOfValues(TLcdDimensionAxis<?> aDimensionAxis) {
    return getAxisValues(aDimensionAxis).size();
  }

  @Override
  public int getIndexOfValue(TLcdDimensionAxis<?> aDimensionAxis, TLcdDimensionInterval<?> aValue) {
    return getAxisValues(aDimensionAxis).indexOf(aValue);
  }

  private <T> List<TLcdDimensionInterval<T>> getAxisValues(TLcdDimensionAxis<T> aDimensionAxis) {
    if (Date.class.isAssignableFrom(aDimensionAxis.getType())) {
      return getDates();
    }
    return getAltitudes();
  }

  private <T> List<TLcdDimensionInterval<T>> getDates() {
    List<TLcdDimensionInterval<T>> result = new ArrayList<>();
    for (TLcdDimensionInterval<Date> date : fDates) {
      result.add((TLcdDimensionInterval<T>) date);
    }
    return result;
  }

  private <T> List<TLcdDimensionInterval<T>> getAltitudes() {
    List<TLcdDimensionInterval<T>> result = new ArrayList<>();
    for (TLcdDimensionInterval<TLcdISO19103Measure> altitude : fAltitudes) {
      result.add((TLcdDimensionInterval<T>) altitude);
    }
    return result;
  }

  public MultiDimensionalValue createMultiDimensionalValueForFirstValue() {
    return new MultiDimensionalValue(new InterpolatedAxisValue(getTimeAxis(), getAxisValue(getTimeAxis(), 0)),
                                     new InterpolatedAxisValue(getHeightAxis(), getAxisValue(getHeightAxis(), 0)));
  }

  static class IcingSeverityModel extends TLcd2DBoundsIndexedModel {}
  static class IcingSLDModel extends TLcd2DBoundsIndexedModel {}

}
