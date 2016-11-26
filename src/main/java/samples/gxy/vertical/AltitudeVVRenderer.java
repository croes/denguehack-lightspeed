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
package samples.gxy.vertical;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.luciad.shape.ILcdPoint;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.view.vertical.TLcdDefaultVVRendererJ2D;
import com.luciad.view.vertical.TLcdVVJPanel;

/**
 * TLcdDefaultVVRendererJ2D for displaying altitudes.
 */
public class AltitudeVVRenderer extends TLcdDefaultVVRendererJ2D {

  private final TLcdAltitudeFormat fAltitudeFormat = new TLcdAltitudeFormat(TLcdAltitudeUnit.METRE);

  public AltitudeVVRenderer(final TLcdVVJPanel aPanel) {
    setPointFormat(new AltitudePointFormat(aPanel));
  }

  private class AltitudePointFormat extends Format {

    private final TLcdVVJPanel fPanel;

    public AltitudePointFormat(TLcdVVJPanel aPanel) {
      fPanel = aPanel;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      TLcdAltitudeUnit altitudeUnit = fPanel.getAltitudeUnit();
      double altitude = ((ILcdPoint) obj).getZ();
      if (altitudeUnit != null) {
        fAltitudeFormat.setDisplayUnit(altitudeUnit);
        boolean showFraction = (altitude - Math.rint(altitude)) >= 0.09;
        fAltitudeFormat.setFractionDigits(showFraction ? 2 : 0);
        toAppendTo.append(fAltitudeFormat.format(altitude));
      } else {
        toAppendTo.append(altitude);
      }
      return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
      return null;
    }
  }
}
