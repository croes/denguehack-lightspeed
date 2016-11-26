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
package samples.wms.client.ecdis.gxy.s52;

class S52Tooltips {

  public static final String COLOR_TYPE_TOOLTIP =
      "Select one of the predefined S-52 color schemes.";
  public static final String POINT_SYMBOL_TOOLTIP =
      "Select one of the predefined S-52 symbol types to be used for the rendering of point objects.";
  public static final String AREA_SYMBOL_TOOLTIP =
      "Select one of the predefined S-52 symbol types to be used for the rendering of area objects.";
  public static final String LIGHT_SECTOR_LINES_TOOLTIP = "<html>" +
                                                          "<p>Sets whether sector lines of lights should be displayed at full length. If false,</p>" +
                                                          "<p>only short sector lines with a fixed length in screen coordinates will be drawn.</p>" +
                                                          "</html>";
  public static final String DISPLAY_CATEGORY_TOOLTIP = "<html>" +
                                                        "<p>Selects the S-52 display category to be rendered.</p>" +
                                                        "<p>Each S-57 object falls into one of three display categories, defined by the S-52 symbology.</p>" +
                                                        "<p>These categories can be used as a coarse filter to control the amount of data that is rendered on a map.</p>" +
                                                        "</html>";
  public static final String DISPLAY_SOUNDINGS_TOOLTIP = "<html>" +
                                                         "<p>Sets whether depth soundings should be displayed or not.</p>" +
                                                         "<p>Depth soundings are measurements of the sea depth at a given location.</p>" +
                                                         "<p>They are rendered as small numbers on top of depth areas.</p>" +
                                                         "</html>";
  public static final String DISPLAY_METADATA_TOOLTIP = "<html>" +
                                                        "<p>Sets whether metadata should be displayed or not. Metadata provide additional meta information about an S-57 object, such as data quality information.</p>" +
                                                        "<p>Metadata is only rendered when the display category is set to Other. Metadata is never shown for other categories, independent of this setting.</p>" +
                                                        "<p>Turning on metadata rendering may clutter the map significantly, and it is therefore disabled by default.</p>" +
                                                        "</html>";
  public static final String DISPLAY_LAND_AREAS_TOOLTIP = "<html>" +
                                                          "<p>Sets whether land and built-up areas should be displayed or not.</p>" +
                                                          "<p>Hiding these areas can be use to see layers below the S-52 layer.</p>" +
                                                          "</html>";
  public static final String DISPLAY_TEXT_TOOLTIP = "<html>" +
                                                    "<p>Sets whether textual information of objects should be displayed or not.</p>" +
                                                    "<p>Turning on textual information rendering may clutter the map significantly,</p>" +
                                                    "<p>and may also reduce the rendering performance. It is therefore disabled by default.</p>" +
                                                    "</html>";
  public static final String USE_ABBREVIATIONS_TOOLTIP = "<html>" +
                                                         "<p>Sets whether to use abbreviations for text rendering.</p>" +
                                                         "<p>Enabling abbreviations may reduce cluttering on the map significantly.</p>" +
                                                         "<p>This setting currently only affects the NATSUR attribute.</p>" +
                                                         "</html>";
  public static final String USE_NATIONAL_LANGUAGE_TOOLTIP = "<html>" +
                                                             "<p>Sets whether to use national language text labels.</p>" +
                                                             "<p>When deselected, the international name will be used.</p>" +
                                                             "</html>";
  public static final String SAFETY_DEPTH_TOOLTIP = "<html>" +
                                                    "<p>Sets the ship's safety depth (in meter).</p>" +
                                                    "<p>The safety depth affects the way depth soundings are rendered:</p>" +
                                                    "<p>depth soundings with a value less than the safety depth will be rendered in a dark color, while</p>" +
                                                    "<p>depth soundings with a value more than the safety depth will be rendered in a light color.</p>" +
                                                    "<p>The safety contour depth and safety depth are independent settings; the former affects</p>" +
                                                    "<p>the safety contour line and the depth area fill colors, the latter affects the depth soundings colors.</p>" +
                                                    "</html>";
  public static final String SAFETY_CONTOUR_TOOLTIP = "<html>" +
                                                      "<p>Sets the ship's safety contour depth (in meter).</p>" +
                                                      "<p>The safety contour is a bold line indicating the boundaries of the area in which the ship can safely navigate.</p>" +
                                                      "<p>All areas with a depth less than the safety contour depth (on one side of the safety contour) will be rendered with</p>" +
                                                      "<p>a darker color than the areas with a depth more than the safety contour depth (on the other side of the safety contour).</p>" +
                                                      "<p>The safety contour depth and safety depth are independent settings; the former affects the safety contour line and</p>" +
                                                      "<p>the depth area fill colors, the latter affects the depth soundings colors.</p>" +
                                                      "</html>";
  public static final String SHALLOW_CONTOUR_TOOLTIP = "<html>" +
                                                       "<p>Sets the depth of the shallow contour (in meter).</p>" +
                                                       "<p>Areas with a depth less than the shallow contour depth will be rendered in a darker color</p>" +
                                                       "<p>than areas with a depth more than the shallow contour depth.</p>" +
                                                       "<p>The shallow contour is only relevant if the two shades setting is turned off.</p>" +
                                                       "</html>";
  public static final String DEEP_CONTOUR_TOOLTIP = "<html>" +
                                                    "<p>Sets the depth of the deep contour (in meter).</p>" +
                                                    "<p>Areas with a depth less than the deep contour depth will be rendered in a darker color</p>" +
                                                    "<p>than areas with a depth more than the deep contour depth.</p>" +
                                                    "<p>The deep contour is only relevant if the two shades setting is turned off.</p>" +
                                                    "</html>";
  public static final String TWO_SHADES_TOOLTIP = "<html>" +
                                                  "<p>Sets whether to use a 2- or 4-color schema for depth area rendering.</p>" +
                                                  "<p>When set to the 2-shades schema, only 2 colors will be used (darker for depths less than the safety contour,</p>" +
                                                  "<p>lighter for depths more than the safety contour). When set to the 4-shades schema, 4 colors will be used</p>" +
                                                  "<p>(the shallow and deep contour depth will also be taken into account).</p>" +
                                                  "</html>";
  public static final String DISPLAY_SHALLOW_PATTERN_TOOLTIP =
      "Sets whether to fill areas with a depth less than the shallow depth with a special fill pattern.";
  public static final String DISPLAY_ISOLATED_DANGERS_TOOLTIP =
      "Sets whether to display isolated dangers in shallow water.";
  public static final String DISPLAY_CHART_BOUNDARIES_TOOLTIP = "<html>" +
                                                                "<p>Sets whether to display chart boundaries. When active, the boundaries of S-57 cells will be rendered in a special</p>" +
                                                                "<p>linestyle. This setting is only applicable to cells in catalogues, not for cells which are loaded individually.</p>" +
                                                                "</html>";
  public static final String DISPLAY_OVERSCALE_INDICATOR_TOOLTIP = "<html>" +
                                                                   "<p>Sets whether to display an overscale indication for cells which are being viewed on a scale larger than the</p>" +
                                                                   "<p>intended scale range. The overscale indication is a vertical bar pattern that is drawn on top of the cell.</p>" +
                                                                   "<p>This setting is only applicable to cells in catalogues, not for cells which are loaded individually.</p>" +
                                                                   "</html>";
  public static final String DISPLAY_UNDERSCALE_INDICATOR_TOOLTIP = "<html>" +
                                                                    "<p>Sets whether to display an underscale indication for cells which are not visible yet (the map scale is smaller</p>" +
                                                                    "<p>than the intended scale range of the cell). The underscale indication is a rectangle showing the bounds of the cell.</p>" +
                                                                    "<p>It is not part of the S-52 specification. This setting is only applicable to cells in catalogues, not for cells</p>" +
                                                                    "<p>which are loaded individually.</p>" +
                                                                    "</html>";
  public static final String OBJECT_SELECTION_TOOLTIP = "<html>" +
                                                        "<p>Selects the S-57 object classes to be rendered.</p>" +
                                                        "<p>Each S-57 object belongs to exactly one object class. This can be used as a fine-grained filter to display or hide groups of similar objects.</p>" +
                                                        "<p>All filters are combined using an AND operator: an object is only rendered if it is accepted by all filters (display category filter, object class filter, specific type filters, ...)</p>" +
                                                        "</html>";
}
