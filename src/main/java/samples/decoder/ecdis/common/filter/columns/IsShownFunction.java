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
package samples.decoder.ecdis.common.filter.columns;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import samples.decoder.ecdis.common.ObjectClass;

/**
 * Function that calculates whether a given object class is rendered or not. This means that the function will check
 * whether the current {@link TLcdS52DisplaySettings#getObjectClasses()} contains a given object class
 * code.
 */
public class IsShownFunction implements IFunction<ObjectClass, Boolean> {

  private final TLcdS52DisplaySettings fDisplaySettings;

  /**
   * Creates a new instance.
   * @param aDisplaySettings the display settings.
   */
  public IsShownFunction(TLcdS52DisplaySettings aDisplaySettings) {
    fDisplaySettings = aDisplaySettings;
  }

  @Override
  public Boolean apply(ObjectClass aObjectClass) {
    int[] objectClasses = fDisplaySettings.getObjectClasses();
    if (objectClasses == null) {
      return false;
    }
    for (int code : objectClasses) {
      if (code == aObjectClass.getCode()) {
        return true;
      }
    }
    return false;
  }
}
