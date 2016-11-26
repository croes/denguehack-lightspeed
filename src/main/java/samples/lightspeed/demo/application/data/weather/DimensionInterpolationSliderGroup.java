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
import java.util.List;

/**
 * This class is used together with a set of DimensionInterpolationSlider.
 * Using this class, a DimensionInterpolationSlider can ask the model if it needs to add a new contour because
 * it can retrieve the dimension values of the other sliders.
 */
class DimensionInterpolationSliderGroup {

  private final Collection<DimensionInterpolationSlider<?>> fDimensionInterpolationSliders;

  public DimensionInterpolationSliderGroup(DimensionInterpolationSlider<?>... aDimensionInterpolationSliders) {
    List<DimensionInterpolationSlider<?>> sliders = new ArrayList<>(aDimensionInterpolationSliders.length);
    for (DimensionInterpolationSlider<?> slider : aDimensionInterpolationSliders) {
      slider.setDimensionInterpolationSliderGroup(this);
      sliders.add(slider);
    }
    fDimensionInterpolationSliders = sliders;
  }

  public MultiDimensionalValue toMultiDimensionalValue(InterpolatedAxisValue aInterpolatedAxisValue) {
    List<InterpolatedAxisValue> axisValues = new ArrayList<>(fDimensionInterpolationSliders.size());
    for (DimensionInterpolationSlider<?> slider : fDimensionInterpolationSliders) {
      if (slider.getDimensionAxis().equals(aInterpolatedAxisValue.getDimensionAxis())) {
        axisValues.add(aInterpolatedAxisValue);
      } else {
        axisValues.add(slider.getInterpolatedAxisValue());
      }
    }
    return new MultiDimensionalValue(axisValues);
  }
}
