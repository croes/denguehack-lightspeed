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

import java.util.Date;
import java.util.Objects;

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.util.iso19103.TLcdISO19103Measure;

class InterpolatedAxisValue {

  private final AxisValue fFirstValue;
  private final AxisValue fSecondValue;
  private final double fBlendFactor;
  private final TLcdDimensionInterval<?> fInterpolatedValue;

  public InterpolatedAxisValue(AxisValue aFirstValue, AxisValue aSecondValue, double aBlendFactor) {
    fFirstValue = aFirstValue;
    fSecondValue = aSecondValue;
    fBlendFactor = aBlendFactor;
    validate();
    fInterpolatedValue = calculateInterpolatedValue();
  }

  public InterpolatedAxisValue(AxisValue aValue) {
    fFirstValue = aValue;
    fSecondValue = fFirstValue;
    fBlendFactor = 1;
    validate();
    fInterpolatedValue = fFirstValue.getValue();
  }

  public InterpolatedAxisValue(TLcdDimensionAxis<?> aDimensionAxis, TLcdDimensionInterval<?> aValue) {
    this(new AxisValue(aDimensionAxis, aValue));
  }

  private void validate() {
    Objects.requireNonNull(fFirstValue);
    Objects.requireNonNull(fSecondValue);
    if (!fFirstValue.getDimensionAxis().equals(fSecondValue.getDimensionAxis())) {
      throw new IllegalArgumentException("AxisValues do not have the same dimension axis");
    }
  }

  private TLcdDimensionInterval<?> calculateInterpolatedValue() {
    if (TLcdISO19103Measure.class.isAssignableFrom(getDimensionAxis().getType())) {
      TLcdDimensionInterval<TLcdISO19103Measure> firstValue = (TLcdDimensionInterval<TLcdISO19103Measure>) fFirstValue.getValue();
      TLcdDimensionInterval<TLcdISO19103Measure> secondValue= (TLcdDimensionInterval<TLcdISO19103Measure>) fSecondValue.getValue();
      double newDoubleValue = interpolate(firstValue.getMax().getValue(), secondValue.getMax().getValue(), fBlendFactor);
      TLcdISO19103Measure measure = new TLcdISO19103Measure(newDoubleValue, getDimensionAxis().getUnit());
      return TLcdDimensionInterval.createSingleValue(firstValue.getType(), measure);
    } else if (Date.class.isAssignableFrom(getDimensionAxis().getType())){
      TLcdDimensionInterval<Date> firstValue = (TLcdDimensionInterval<Date>) fFirstValue.getValue();
      TLcdDimensionInterval<Date> secondValue= (TLcdDimensionInterval<Date>) fSecondValue.getValue();
      double newDoubleValue = interpolate(firstValue.getMax().getTime(), secondValue.getMax().getTime(), fBlendFactor);
      return TLcdDimensionInterval.createSingleValue(firstValue.getType(), new Date((long)newDoubleValue));
    } else {
      throw new IllegalArgumentException("Cannot blend values for type " + getDimensionAxis().getType());
    }
  }

  public TLcdDimensionAxis<?> getDimensionAxis() {
    return fFirstValue.getDimensionAxis();
  }

  public AxisValue getFirstValue() {
    return fFirstValue;
  }

  public AxisValue getSecondValue() {
    return fSecondValue;
  }

  public double getBlendFactor() {
    return fBlendFactor;
  }

  public TLcdDimensionInterval<?> getInterpolatedValue() {
    return fInterpolatedValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InterpolatedAxisValue that = (InterpolatedAxisValue) o;

    return fInterpolatedValue.equals(that.fInterpolatedValue);

  }

  @Override
  public int hashCode() {
    return fInterpolatedValue.hashCode();
  }

  private static double interpolate(double aFirstValue, double aSecondValue, double aBlendFactor) {
    return aBlendFactor * aFirstValue + (1 - aBlendFactor) * aSecondValue;
  }

}
