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
package samples.hana.lightspeed.common;

import java.io.IOException;
import java.util.Properties;

import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * Utility to read configuration options from the {@code config.properties} file.
 */
public class Configuration {

  static {
    try {
      Properties properties = new Properties();
      properties.load(Configuration.class.getResourceAsStream("/samples/hana/lightspeed/config.properties"));
      properties.putAll(System.getProperties());
      System.setProperties(properties);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String get(String aKey, String aDefault) {
    return System.getProperty(aKey, aDefault);
  }

  public static String get(String aKey) {
    String value = get(aKey, null);
    if (value == null) {
      throw new IllegalArgumentException("Property " + aKey + " not set.");
    }
    return value;
  }

  public static boolean is(String aKey) {
    return Boolean.parseBoolean(get(aKey));
  }

  public static boolean is(String aKey, boolean aDefault) {
    return Boolean.parseBoolean(get(aKey, "" + aDefault));
  }

  public static int getInt(String aKey) {
    return Integer.parseInt(get(aKey));
  }

  public static int getInt(String aKey, int aDefault) {
    return Integer.parseInt(get(aKey, "" + aDefault));
  }

  public static double getDouble(String aKey) {
    return Double.parseDouble(get(aKey));
  }

  public static double getDouble(String aKey, double aDefault) {
    return Double.parseDouble(get(aKey, "" + aDefault));
  }

  public static TLcdInterval getScaleRange(String aPrefix, TLspPaintRepresentation aPaintRepresentation) {
    double min = getDouble(aPrefix + "." + aPaintRepresentation.toString().toLowerCase() + ".minscale", 0.0);
    double max = getDouble(aPrefix + "." + aPaintRepresentation.toString().toLowerCase() + ".maxscale", Double.MAX_VALUE);
    return (min == 0.0 && max == Double.MAX_VALUE) ? null : new TLcdInterval(min, max);
  }
}
