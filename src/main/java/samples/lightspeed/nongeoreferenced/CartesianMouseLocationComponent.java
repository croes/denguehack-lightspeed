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
package samples.lightspeed.nongeoreferenced;

import java.util.Collections;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdFormatter;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.measure.ILcdLayerMeasureProviderFactory;
import com.luciad.util.measure.ILcdModelMeasureProviderFactory;
import com.luciad.view.lightspeed.ALspAWTView;

import samples.common.model.CartesianReference;
import samples.lightspeed.common.MouseLocationComponent;

/**
 * Mouse location component for cartesian views.
 */
public class CartesianMouseLocationComponent extends MouseLocationComponent {

  public CartesianMouseLocationComponent(ALspAWTView aView) {
    super(aView,
          Collections.<ILcdModelMeasureProviderFactory>emptyList(),
          Collections.<ILcdLayerMeasureProviderFactory>emptyList());
    setModelReference(CartesianReference.getInstance());
    setCoordinateFormats(new ILcdFormatter[]{new PointFormatter()});
  }

  @Override
  protected TLcdISO19103Measure[] getValues(ILcdPoint aPoint, ILcdModelReference aPointReference) {
    return new TLcdISO19103Measure[0];
  }

  private static class PointFormatter implements ILcdFormatter {
    @Override
    public String format(Object aObject) {
      ILcdPoint p = (ILcdPoint) aObject;
      return String.format("%d, %d", (int) p.getX(), (int) p.getY());
    }
  }
}
