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
package samples.common;

import java.awt.event.InputEvent;

import com.luciad.util.TLcdSystemPropertiesUtil;

/**
 * Contains utility methods that return a proper value for the ctrl key(cmd in Mac OS X)
 * depending to the working OS
 */
public class MetaKeyUtil {
  /**
   * Returns true if CTRL key is down in Windows/Linux or CMD key is down in Mac OS
   * for the given InputEvent
   * @param aInputEvent InputEvent to be checked for modifier key
   * @return true if CTRL key is down in Windows/Linux or CMD key is down in Mac OS, false otherwise
   */
  public static boolean isCMDDown(InputEvent aInputEvent) {
    if (TLcdSystemPropertiesUtil.isMacOS()) {
      return aInputEvent.isMetaDown();
    } else {
      return aInputEvent.isControlDown();
    }
  }

  /**
   * Returns InputEvent.CTRL_DOWN_MASK in Windows/Linux and InputEvent.META_DOWN_MASK in Mac OS X
   * @return InputEvent.CTRL_DOWN_MASK in Windows/Linux and InputEvent.META_DOWN_MASK in Mac OS X
   */
  public static int getCMDDownMask() {
    if (TLcdSystemPropertiesUtil.isMacOS()) {
      return InputEvent.META_DOWN_MASK;
    } else {
      return InputEvent.CTRL_DOWN_MASK;
    }
  }

  /**
   * Returns InputEvent.CTRL_MASK in Windows/Linux and InputEvent.META_MASK in Mac OS X
   * @return InputEvent.CTRL_MASK in Windows/Linux and InputEvent.META_MASK in Mac OS X
   */
  public static int getCMDMask() {
    if (TLcdSystemPropertiesUtil.isMacOS()) {
      return InputEvent.META_MASK;
    } else {
      return InputEvent.CTRL_MASK;
    }
  }

  /**
   * Returns the proper key for CMD key action depending on the OS. "meta" for Mac OS X and "ctrl" for Windows/Linux.
   * @return the proper key for CMD key action depending on the OS. "meta" for Mac OS X and "ctrl" for Windows/Linux.
   */
  public static String getCMDModifierKey() {
    if (TLcdSystemPropertiesUtil.isMacOS()) {
      return "meta";
    } else {
      return "ctrl";
    }
  }
}
