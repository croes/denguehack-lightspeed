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

import static java.awt.Color.LIGHT_GRAY;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//import java.io.File;

public class OpenStreetMapStyleUtil {

  private static final String ICON_DIR;

  private static final OpenStreetMapElementStyles.Icon DEFAULT_ICON;
  private static final OpenStreetMapElementStyles.Line DEFAULT_LINE;
  private static final OpenStreetMapElementStyles.Area DEFAULT_AREA;

  private static Map<String, String> sIconPathCache = Collections.synchronizedMap(new HashMap<String, String>());
  private static final String NULL_STRING = "null";

  private OpenStreetMapStyleUtil() {
  }

  static {
    DEFAULT_ICON = new OpenStreetMapElementStyles.Icon();
    DEFAULT_ICON.fAnnotate = true;
    DEFAULT_ICON.fSrc = "default_icon.png";

    DEFAULT_LINE = new OpenStreetMapElementStyles.Line();
    DEFAULT_LINE.fColor = LIGHT_GRAY;
    DEFAULT_LINE.fWidth = 1;
    DEFAULT_LINE.fRealWidth = 1;

    DEFAULT_AREA = new OpenStreetMapElementStyles.Area();
    DEFAULT_AREA.fColor = LIGHT_GRAY;
    DEFAULT_AREA.fClosed = true;

    String osmIconsPath = System.getProperty("osm.icons", "samples/lightspeed/demo/osm/icons/svg2");

    URL resource = OpenStreetMapElementStyles.class.getClassLoader().getResource(osmIconsPath);

    if (resource == null) {
      System.err.println("Cannot find OpenStreetMap icons in " + osmIconsPath + ", adjust classpath or override the system property osm.icons=<path>");
      ICON_DIR = null;
    } else {
      ICON_DIR = osmIconsPath;
    }

  }

  public static OpenStreetMapElementStyles.Icon getDefaultIcon() {
    return DEFAULT_ICON;
  }

  public static OpenStreetMapElementStyles.Line getDefaultLine() {
    return DEFAULT_LINE;
  }

  public static OpenStreetMapElementStyles.Area getDefaultArea() {
    return DEFAULT_AREA;
  }

  public static String getIconPath(OpenStreetMapElementStyles.Icon aIcon) {
    String result = sIconPathCache.get(aIcon.fSrc);
    if (result != null) {
      return result == NULL_STRING ? null : result;
    }

    //convert png to svg
    String srcFile = aIcon.fSrc;
    if (srcFile.toLowerCase().endsWith(".png")) {
      String substring = srcFile.substring(0, srcFile.indexOf(".png"));
      srcFile = substring + ".svg";
    }
    String path = ICON_DIR + "/" + srcFile;

    while (true) {
      URL resource = OpenStreetMapElementStyles.class.getClassLoader().getResource(path);
      if (resource != null) {
        break;
      }

      //System.err.println("Icon not found: " + file + ", falling back to category icon...");
      int index = path.lastIndexOf('/');
      String subPath = path.substring(0, index);
      if (ICON_DIR.equals(subPath)) {
        sIconPathCache.put(aIcon.fSrc, NULL_STRING);
        return null;
      }

      path = subPath + ".svg";
    }

    sIconPathCache.put(aIcon.fSrc, path);
    return path;
  }

  public static void load() {
    // do nothing, the static stuff is enough
  }
}
