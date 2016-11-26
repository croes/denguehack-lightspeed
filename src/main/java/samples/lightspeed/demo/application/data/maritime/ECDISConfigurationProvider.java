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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.Color;

import com.luciad.format.s52.ILcdS52Symbology;
import com.luciad.format.s52.TLcdS52DisplaySettings;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * Utility class allowing to share the s52/s57 product configuration and
 * s52 display settings between the UI, model factory and layer factory
 *
 * @since 2013.1
 */
public final class ECDISConfigurationProvider {

  private static final String S52_DISPLAY_SETTINGS_KEY = "ecdis.s52DisplaySettings";

  private ECDISConfigurationProvider() {
    //private constructor, class only contains utility methods
  }

  /**
   * Retrieves the shared S52 display settings from the framework
   * @return the shared S52 display settings from the framework. Will never be {@code null}
   */
  public static TLcdS52DisplaySettings getS52DisplaySettings() {
    Framework framework = Framework.getInstance();
    TLcdS52DisplaySettings result = (TLcdS52DisplaySettings) framework.getSharedValue(S52_DISPLAY_SETTINGS_KEY);
    if (result == null) {
      result = new TLcdS52DisplaySettings();

      result.setColorType(ILcdS52Symbology.DAY_BRIGHT_COLORS);
      result.setUseTwoShades(false);
      result.setShallowContour(2);
      result.setSafetyContour(30);
      result.setDeepContour(60);
      result.setSafetyDepth(30);
      result.setDisplayOverscaleIndication(false);
      result.setUnderscaleIndicationColor(new Color(191, 182, 122, 210));

      framework.storeSharedValue(S52_DISPLAY_SETTINGS_KEY, result);
    }
    return result;
  }

}
