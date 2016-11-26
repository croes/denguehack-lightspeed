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

import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;

class AxisValue {

  private final TLcdDimensionAxis<?> fDimensionAxis;
  private final TLcdDimensionInterval<?> fValue;

  public AxisValue(TLcdDimensionAxis<?> aDimensionAxis, TLcdDimensionInterval<?> value) {
    fDimensionAxis = aDimensionAxis;
    fValue = value;
  }

  public TLcdDimensionAxis<?> getDimensionAxis() {
    return fDimensionAxis;
  }

  public TLcdDimensionInterval<?> getValue() {
    return fValue;
  }

  @Override
  public String toString() {
    return "Axis type: " + fDimensionAxis.getType() + ", Axis unit: " + fDimensionAxis.getUnit() + ", Value: " + fValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AxisValue axisValue = (AxisValue) o;

    if (!fDimensionAxis.equals(axisValue.fDimensionAxis)) {
      return false;
    }
    return fValue.equals(axisValue.fValue);

  }

  @Override
  public int hashCode() {
    int result = fDimensionAxis.hashCode();
    result = 31 * result + fValue.hashCode();
    return result;
  }

}
