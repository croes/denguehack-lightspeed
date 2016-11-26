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
package samples.lucy.symbology.common;

import java.text.MessageFormat;

import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.TLcdStatusEvent;

/**
 * Utility method that makes it possible to show or remove status messages for the creation
 * of symbology objects.
 */
public final class SymbologyStatusMessageUtil {

  private SymbologyStatusMessageUtil() {
  }

  public static void showStatusMessage(Object aInstance, ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent aMapComponent) {
    int requiredClicks = MilitarySymbolFacade.getRequiredNumberOfClicks(aInstance);
    String message;
    if (requiredClicks >= 0) {
      if (requiredClicks == 1) {
        message = TLcyLang.getString("Creation of this object requires 1 click on the map");
      } else {
        message = MessageFormat.format(TLcyLang.getString("Creation of this object requires {0} clicks on the map"),
                                       requiredClicks);
      }
    } else {
      message = MessageFormat.format(TLcyLang.getString("Creation of this object at least requires {0} clicks on the map.\nA double click or right mouse click ends the creation."),
                                     -requiredClicks);
    }
    sendMessage(message, aLucyEnv, aMapComponent);
  }

  public static void removeStatusMessage(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent aMapComponent) {
    sendMessage("", aLucyEnv, aMapComponent);
  }

  @SuppressWarnings("unchecked")
  private static void sendMessage(String aMessage, ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent aMapComponent) {
    TLcdStatusEvent.sendMessage(aLucyEnv, aMapComponent.getComponent(), aMessage, TLcdStatusEvent.Severity.INFO);
  }
}
