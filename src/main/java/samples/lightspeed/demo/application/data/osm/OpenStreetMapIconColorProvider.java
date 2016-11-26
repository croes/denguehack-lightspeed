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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Reads the iconcolor.properties file and determines a color
 */
public class OpenStreetMapIconColorProvider {

  private static Map<String, Color> sColorMap;
  private static Color sDefaultColor;

  static {
    try {
      sColorMap = new HashMap<String, Color>();
      InputStream resourceAsStream = OpenStreetMapIconColorProvider.class.getClassLoader().getResourceAsStream("samples/lightspeed/demo/osm/iconcolors.properties");
      Properties properties = new Properties();
      properties.load(resourceAsStream);
      for (Object category : properties.keySet()) {
        sColorMap.put((String) category, Color.decode((String) properties.get(category)));
      }
      sDefaultColor = Color.decode(properties.getProperty("default"));
      if (sDefaultColor == null) {
        sDefaultColor = Color.gray;
      }
    } catch (IOException e) {
      System.err.println("Could not load iconcolor.properties. All icons will resort to a default color.");
      sDefaultColor = Color.gray;
    }
  }

  public static Color getColor(String aPath) {
    Set<String> keys = sColorMap.keySet();
    for (String key : keys) {
      if (aPath.toLowerCase().contains(key.toLowerCase())) {
        return sColorMap.get(key);
      }
    }
    return sDefaultColor;
  }
}
