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
package samples.decoder.ecdis.common;

class S52Tooltips {

  public static final String COLOR_TYPE_TOOLTIP = lineBreak(
      "The day color scheme uses bright colors. Dusk and night use darker colors, ideal in low-light " +
      "conditions.");
  public static final String POINT_SYMBOL_TOOLTIP = lineBreak(
      "Traditional paper chart symbols are best for printing. The simplified icons require less " +
      "detail to be distinguished, ideal for screens.");
  public static final String AREA_SYMBOL_TOOLTIP = lineBreak(
      "Symbolized boundaries provide most information, plain boundaries only use solid or dashed lines.");
  public static final String LIGHT_SECTOR_LINES_TOOLTIP = lineBreak(
      "Short light sector lines have a fixed size on screen. Real length means they match " +
      "the actual light visibility distance.");
  public static final String DISPLAY_CATEGORY_TOOLTIP = lineBreak(
      "The display category acts as a coarse filter for the amount of information on the map. " +
      "Each object (anchor, beacon ...) falls into one of these three display categories, defined by " +
      "the S-52 symbology.");
  public static final String OBJECT_CLASS_FILTER_TOOLTIP = lineBreak(
      "The object class filter can be used to show or hide specific classes." +
      "The filter is only active when display category is \"Other\".");
  public static final String DISPLAY_SOUNDINGS_TOOLTIP = lineBreak(
      "Depth soundings are measurements of the sea depth at a given location. When zoomed in far " +
      "enough, they appear as small numbers on top of the depth areas. Soundings of unsafe depth are colored darker.");
  public static final String DISPLAY_METADATA_TOOLTIP = lineBreak(
      "Metadata provides for example data quality information. " +
      "Metadata is only visible when the display category is set to Other, this setting is ignored otherwise.");
  public static final String DISPLAY_LAND_AREAS_TOOLTIP = lineBreak(
      "Sets whether land and built-up areas should be displayed or not. " +
      "Hiding these areas can be used to see other data on land, for example imagery.");
  public static final String DISPLAY_TEXT_TOOLTIP = lineBreak(
      "Provides additional textual information such as the names of objects. " +
      "Turning it on may clutter the map, and may reduce the rendering performance.");
  public static final String USE_ABBREVIATIONS_TOOLTIP = lineBreak(
      "When text is displayed this setting allows the use of abbreviations, to reduce map clutter. " +
      "This setting currently only affects the NATSUR attribute.");
  public static final String USE_NATIONAL_LANGUAGE_TOOLTIP = lineBreak(
      "When text is displayed, either the national language can be used, or the international names.");
  public static final String SAFETY_CONTOUR_TOOLTIP = lineBreak(
      "Sets the ship's safety contour depth (in meter). " +
      "The safety contour is a bold line indicating the boundaries of the area in which the ship can safely navigate.");
  public static final String SHALLOW_CONTOUR_TOOLTIP = lineBreak(
      "Sets the depth of the shallow contour (in meter). It should be a smaller value than the safety contour.");
  public static final String DEEP_CONTOUR_TOOLTIP = lineBreak(
      "Sets the depth of the deep contour (in meter). It should be a larger value than the safety contour.");
  public static final String TWO_SHADES_TOOLTIP = lineBreak(
      "Sets how many shades should be used to color the depth areas. Two shades allow to distinguish " +
      "between safe and unsafe water. When using four shades, both safe and unsafe water are again divided in two different shades.");
  public static final String DISPLAY_SHALLOW_PATTERN_TOOLTIP = lineBreak(
      "All shades of unsafe water can additionally be hatched using slanted lines, to avoid solely relying on colors.");
  public static final String DISPLAY_ISOLATED_DANGERS_TOOLTIP = lineBreak(
      "Isolated dangers (e.g. rocks) can be displayed even if they are in unsafe water that one isn't allowed to navigate in anyway.");
  public static final String DISPLAY_CHART_BOUNDARIES_TOOLTIP = lineBreak(
      "Each cell has a dedicated navigational purpose. This setting shows a solid border between different navigational purposes. " +
      "It is only applicable to cells in catalogues, not for cells which are loaded individually.");
  public static final String DISPLAY_OVERSCALE_INDICATOR_TOOLTIP = lineBreak(
      "Uses a vertical hatching pattern when cells are being viewed on a scale larger than the " +
      "intended scale range (zoomed in too far). This setting is only applicable to cells in " +
      "catalogues, not for cells which are loaded individually.");
  public static final String DISPLAY_UNDERSCALE_INDICATOR_TOOLTIP = lineBreak(
      "Shows the outline of cells which are not yet visible because the map scale is smaller " +
      "than the intended scale range of the cell (zoomed out too far). This setting is only " +
      "applicable to cells in catalogues, not for cells which are loaded individually. It is not part of the S-52 specification.");
  public static final String OBJECT_SELECTION_TOOLTIP = lineBreak(
      "Selects the S-57 object classes to be rendered. " +
      "Each S-57 object belongs to exactly one object class. This can be used as a fine-grained filter to display or hide groups of similar objects." +
      "All filters are combined using an AND operator: an object is only rendered if it is accepted by all filters (display category filter, object class filter, specific type filters, ...)");

  private static String lineBreak(String aToolTip) {
    StringBuilder tt = new StringBuilder("<html><p>");

    StringBuilder line = new StringBuilder();
    for (String word : aToolTip.split(" ")) {
      line.append(word).append(" ");
      if (line.length() > 80) {
        tt.append(line).append("<br/>");
        line = new StringBuilder();
      }
    }
    return tt.append(line).append("</p></html>").toString();
  }
}
