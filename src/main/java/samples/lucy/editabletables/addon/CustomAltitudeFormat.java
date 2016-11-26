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
package samples.lucy.editabletables.addon;

import java.beans.PropertyChangeEvent;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.TLcdAltitudeUnit;

/*
 * Specialized altitude format that uses the current lucy default altitude unit to display values.
 */
class CustomAltitudeFormat extends Format {

  private final ILcyLucyEnv fLucyEnv;
  private TLcdAltitudeFormat fDelegate;

  public CustomAltitudeFormat(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    //Feet is the program unit!
    fDelegate = new TLcdAltitudeFormat(TLcdAltitudeUnit.FEET, aLucyEnv.getDefaultUserAltitudeUnit());
    aLucyEnv.addPropertyChangeListener(new AltitudeFormatChangeListener(this));
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    return fDelegate.format(obj, toAppendTo, pos);
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    return fDelegate.parseObject(source, pos);
  }

  @Override
  public Object parseObject(String source) throws ParseException {
    return fDelegate.parseObject(source);
  }

  public void updateDisplayUnit() {
    fDelegate.setDisplayUnit(fLucyEnv.getDefaultUserAltitudeUnit());
  }

  public void updateDelegate() {
    Format format = fLucyEnv.getDefaultAltitudeFormat();
    if (format instanceof TLcdAltitudeFormat) {
      fDelegate.setFractionDigits(((TLcdAltitudeFormat) format).getFractionDigits());
      fDelegate.setDisplayUnit(((TLcdAltitudeFormat) format).getDisplayUnit());
    }
  }

  /*
   * This listener keeps the CustomAltitudeFormat up to date.
   */
  static class AltitudeFormatChangeListener extends ALcdWeakPropertyChangeListener<CustomAltitudeFormat> {

    public AltitudeFormatChangeListener(CustomAltitudeFormat aCustomAltitudeFormat) {
      super(aCustomAltitudeFormat);
    }

    @Override
    protected void propertyChangeImpl(CustomAltitudeFormat aAltitudeFormat, PropertyChangeEvent aPropertyChangeEvent) {
      String propertyName = aPropertyChangeEvent.getPropertyName();
      if ("defaultUserAltitudeUnit".equals(propertyName)) {
        aAltitudeFormat.updateDisplayUnit();
      } else if ("defaultAltitudeFormat".equals(propertyName)) {
        aAltitudeFormat.updateDelegate();
      }
    }
  }
}
