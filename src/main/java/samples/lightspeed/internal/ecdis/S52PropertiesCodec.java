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
package samples.lightspeed.internal.ecdis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.luciad.format.s52.ILcdS52Style;
import com.luciad.format.s52.ILcdS52Symbology;
import com.luciad.format.s52.TLcdS52DisplaySettings;

// TODO copied from ECDIS/dev/testsrc/iho/s64
public class S52PropertiesCodec {

  private static final String COLOR_TYPE = "colorType";
  private static final String POINT_SYMBOL_TYPE = "pointSymbolType";
  private static final String AREA_BOUNDARY_SYMBOL_TYPE = "areaBoundarySymbolType";
  private static final String DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES = "displayFullLengthLightSectorLines";

  private static final String DISPLAY_CATEGORY = "displayCategory";
  private static final String DISPLAY_SOUNDINGS = "isplaySoundings";
  private static final String DISPLAY_METADATA = "displayMetaData";

  private static final String DISPLAY_TEXT = "displayText";
  private static final String DISPLAY_TEXT_GROUPS = "displayTextGroups";
  private static final String USE_ABBREVIATIONS = "useAbbreviations";

  private static final String SAFETY_DEPTH = "safetyDepth";
  private static final String SHALLOW_CONTOUR = "shallowContour";
  private static final String SAFETY_CONTOUR = "safetyContour";
  private static final String DEEP_CONTOUR = "deepContour";

  private static final String USE_TWO_SHADES = "useTwoShades";
  private static final String DISPLAY_SHALLOW_PATTERN = "displayShallowPattern";
  private static final String DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER = "displayIsolatedDangersInShallowWater";

  private static final String DISPLAY_CHART_BOUNDARIES = "displayChartBoundaries";
  private static final String DISPLAY_OVERSCALE_INDICATION = "displayOverscaleIndication";
  private static final String DISPLAY_UNDERSCALE_INDICATION = "displayUnderscaleIndication";

  private static final String TEST_DATA_PATH = System.getProperty("data.directory", "server1 TestData: use system property data.directory");
  private static final String S64_CATALOG = TEST_DATA_PATH + "/ECDIS/ENC/S64/Data/GOODB1/ENC_ROOT/";


  public static void toProperties(TLcdS52DisplaySettings aDisplaySettings, Properties aProperties) {
    aProperties.setProperty(COLOR_TYPE, "" + aDisplaySettings.getColorType());
    aProperties.setProperty(POINT_SYMBOL_TYPE, "" + aDisplaySettings.getPointSymbolType());
    aProperties.setProperty(AREA_BOUNDARY_SYMBOL_TYPE, "" + aDisplaySettings.getAreaBoundarySymbolType());
    aProperties.setProperty(DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES, "" + aDisplaySettings.isDisplayFullLengthLightSectorLines());

    aProperties.setProperty(DISPLAY_CATEGORY, "" + aDisplaySettings.getDisplayCategory());
    aProperties.setProperty(DISPLAY_SOUNDINGS, "" + aDisplaySettings.isDisplaySoundings());
    aProperties.setProperty(DISPLAY_METADATA, "" + aDisplaySettings.isDisplayMetaData());

    aProperties.setProperty(DISPLAY_TEXT, "" + aDisplaySettings.isDisplayText());
    //aProperties.setProperty( DISPLAY_TEXT_GROUPS, "" + aDisplaySettings.getDisplayTextGroups() );
    aProperties.setProperty(USE_ABBREVIATIONS, "" + aDisplaySettings.isUseAbbreviations());

    aProperties.setProperty(SAFETY_DEPTH, "" + aDisplaySettings.getSafetyDepth());
    aProperties.setProperty(SHALLOW_CONTOUR, "" + aDisplaySettings.getShallowContour());
    aProperties.setProperty(SAFETY_CONTOUR, "" + aDisplaySettings.getSafetyContour());
    aProperties.setProperty(DEEP_CONTOUR, "" + aDisplaySettings.getDeepContour());

    aProperties.setProperty(USE_TWO_SHADES, "" + aDisplaySettings.isUseTwoShades());
    aProperties.setProperty(DISPLAY_SHALLOW_PATTERN, "" + aDisplaySettings.isDisplayShallowPattern());
    aProperties.setProperty(DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER, "" + aDisplaySettings.isDisplayIsolatedDangersInShallowWater());

    aProperties.setProperty(DISPLAY_CHART_BOUNDARIES, "" + aDisplaySettings.isDisplayChartBoundaries());
    aProperties.setProperty(DISPLAY_OVERSCALE_INDICATION, "" + aDisplaySettings.isDisplayOverscaleIndication());
    aProperties.setProperty(DISPLAY_UNDERSCALE_INDICATION, "" + aDisplaySettings.isDisplayUnderscaleIndication());
  }

  public static void fromProperties(Properties aProperties, TLcdS52DisplaySettings aDisplaySettings) {
    aDisplaySettings.setColorType(getIntProperty(aProperties, COLOR_TYPE));
    aDisplaySettings.setPointSymbolType(getIntProperty(aProperties, POINT_SYMBOL_TYPE));
    aDisplaySettings.setAreaBoundarySymbolType(getIntProperty(aProperties, AREA_BOUNDARY_SYMBOL_TYPE));
    aDisplaySettings.setDisplayFullLengthLightSectorLines(getBooleanProperty(aProperties, DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES));

    aDisplaySettings.setDisplayCategory(getIntProperty(aProperties, DISPLAY_CATEGORY));
    aDisplaySettings.setDisplaySoundings(getBooleanProperty(aProperties, DISPLAY_SOUNDINGS));
    aDisplaySettings.setDisplayMetaData(getBooleanProperty(aProperties, DISPLAY_METADATA));

    aDisplaySettings.setDisplayText(getBooleanProperty(aProperties, DISPLAY_TEXT));
    //aDisplaySettings.setDisplayTextGroups(getBooleanProperty(aProperties, DISPLAY_TEXT_GROUPS" ) );
    aDisplaySettings.setUseAbbreviations(getBooleanProperty(aProperties, USE_ABBREVIATIONS));

    aDisplaySettings.setSafetyDepth(getDoubleProperty(aProperties, SAFETY_DEPTH));
    aDisplaySettings.setShallowContour(getDoubleProperty(aProperties, SHALLOW_CONTOUR));
    aDisplaySettings.setSafetyContour(getDoubleProperty(aProperties, SAFETY_CONTOUR));
    aDisplaySettings.setDeepContour(getDoubleProperty(aProperties, DEEP_CONTOUR));

    aDisplaySettings.setUseTwoShades(getBooleanProperty(aProperties, USE_TWO_SHADES));
    aDisplaySettings.setDisplayShallowPattern(getBooleanProperty(aProperties, DISPLAY_SHALLOW_PATTERN));
    aDisplaySettings.setDisplayIsolatedDangersInShallowWater(getBooleanProperty(aProperties, DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER));

    aDisplaySettings.setDisplayChartBoundaries(getBooleanProperty(aProperties, DISPLAY_CHART_BOUNDARIES));
    aDisplaySettings.setDisplayOverscaleIndication(getBooleanProperty(aProperties, DISPLAY_OVERSCALE_INDICATION));
    aDisplaySettings.setDisplayUnderscaleIndication(getBooleanProperty(aProperties, DISPLAY_UNDERSCALE_INDICATION));
  }

  private static int getIntProperty(Properties aProperties, String aName) {
    return Integer.parseInt(aProperties.getProperty(aName));
  }

  private static double getDoubleProperty(Properties aProperties, String aName) {
    return Double.parseDouble(aProperties.getProperty(aName));
  }

  private static boolean getBooleanProperty(Properties aProperties, String aName) {
    return Boolean.parseBoolean(aProperties.getProperty(aName));
  }

}
