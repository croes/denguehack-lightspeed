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
package samples.decoder.gdf.view.gxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains configurable GDF rendering settings.
 */
public class GDFRenderingSettings {

  private Map<Key, Value> fRenderingSettings = new HashMap<Key, Value>();

  public GDFRenderingSettings() {
    fRenderingSettings.put(Key.QUALITY, Value.QUALITY_HIGH);
  }

  /**
   * Returns the value for the specified setting.
   *
   * @param aSetting the setting for which to return its value.
   *
   * @return the value for the specified setting.
   */
  public Value getSetting(Key aSetting) {
    return fRenderingSettings.get(aSetting);
  }

  /**
   * Sets the specified setting to the given value.
   *
   * @param aSetting the setting to be configured.
   * @param aValue   the value for the specified setting.
   */
  public void setSetting(Key aSetting, Value aValue) {
    fRenderingSettings.put(aSetting, aValue);
  }

  /**
   * Keys for GDF rendering settings.
   */
  public static enum Key {
    QUALITY
  }

  /**
   * Values for GDF rendering settings.
   */
  public static enum Value {
    QUALITY_LOW,
    QUALITY_HIGH
  }

}
