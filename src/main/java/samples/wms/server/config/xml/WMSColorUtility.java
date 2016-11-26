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
package samples.wms.server.config.xml;

import java.awt.Color;

/**
 * Utility class for creating <code>Color</color> instances.
 */
public final class WMSColorUtility {

  private WMSColorUtility() {
    // No need to instantiate this class, as all utility methods are static.
  }

  /**
   * Converts a hexadecimal string representing a color to a <code>Color</code> instance.
   *
   * @param aColorString a hexadecimal string representing a color.
   * @return a <code>Color</code> instance or <code>null</code> if the given string is
   *         <code>null</code> or empty.
   */
  public static Color toColor(String aColorString) {
    if (aColorString == null || aColorString.equals("")) {
      return null;
    }
    String hex = aColorString.substring(1, 7);
    return new Color(Integer.parseInt(hex, 16));
  }

  /**
   * Converts a color to a transparent color using the given opacity.
   *
   * @param aBaseColor the base <code>Color<code> instance.
   * @param aOpacity   the opacity as a double.
   * @return the transparent <code>Color<code> instance.
   */
  public static Color toTransparentColor(Color aBaseColor, double aOpacity) {
    return new Color(aBaseColor.getRed(), aBaseColor.getGreen(), aBaseColor.getBlue(), (int) (255 * aOpacity));
  }
}
