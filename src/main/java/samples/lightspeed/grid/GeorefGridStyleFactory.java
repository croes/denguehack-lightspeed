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
package samples.lightspeed.grid;

import static com.luciad.view.lightspeed.painter.grid.TLspGeorefGridStyle.GeorefLevel.*;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import samples.common.MapColors;
import com.luciad.view.lightspeed.painter.grid.TLspGeorefGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspGeorefGridStyle.Orientation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

public final class GeorefGridStyleFactory {

  public static final double DEFAULT_LABEL_GRID_OFFSET = 3;
  public static final double DEFAULT_LABEL_EDGE_OFFSET = 6;

  // Interval constants
  private static final double MAX_ZOOMED_IN = Double.MAX_VALUE;
  private static final double MAX_ZOOMED_OUT = 0.0;

  public static final double START_15_DEGREE_LABELS = 2e-5;
  public static final double START_1_DEGREE = 4.0e-4;
  public static final double START_10_MINUTES = 2.0e-3;
  public static final double START_1_MINUTE = 2.0e-2;
  public static final double START_1_OVER_10_MINUTE = 2.0e-1;
  public static final double START_1_OVER_100_MINUTE = 2;
  public static final double START_1_OVER_1000_MINUTE = 20.0;
  public static final double START_1_OVER_10000_MINUTE = 200.0;

  // Grid density constants. One of these is multiplied with the above interval constants. When the multiplier is
  // larger, it means that grid lines will appear later
  private static final double DEFAULT_MULTIPLIER = 1.0;
  private static final double COARSE_MULTIPLIER = 3.0;

  public static TLspGeorefGridStyle createGridStyle(boolean aCoarse) {
    return createGridStyle(aCoarse, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET);
  }

  public static TLspGeorefGridStyle createGridStyle(boolean aCoarse,
                                                    Orientation aOrientation,
                                                    double aGridOffset,
                                                    double aEdgeOffset) {
    return createGridStyle(aCoarse, aOrientation, aGridOffset, aEdgeOffset, "Primary", "Secondary", "Tertiary");
  }

  public static TLspGeorefGridStyle createGridStyle(String aPrimaryStyleName,
                                                    String aSecondaryStyleName,
                                                    String aTertiaryStyleName) {
    return createGridStyle(false, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET, aPrimaryStyleName, aSecondaryStyleName, aTertiaryStyleName);
  }

  private static TLspGeorefGridStyle createGridStyle(boolean aCoarse,
                                                     Orientation aOrientation,
                                                     double aGridOffset,
                                                     double aEdgeOffset,
                                                     String aPrimaryStyleName,
                                                     String aSecondaryStyleName,
                                                     String aTertiaryStyleName) {
    TLspCustomizableStyle primaryStyle = createCustomizableStyle(1f, MapColors.GRID_PROMINENT_COLOR, "PRIMARY", aPrimaryStyleName);
    TLspCustomizableStyle secondaryStyle = createCustomizableStyle(2f, MapColors.GRID_ALTERNATE_COLOR, "SECONDARY", aSecondaryStyleName);
    TLspCustomizableStyle tertiaryStyle = createCustomizableStyle(3f, MapColors.GRID_EXCEPTIONAL_COLOR, "TERTIARY", aTertiaryStyleName);

    TLspCustomizableStyle primaryLabelStyle = createCustomizableLabelStyle(12, MapColors.GRID_PROMINENT_LABEL_COLOR, "PRIMARY", aPrimaryStyleName);
    TLspCustomizableStyle secondaryLabelStyle = createCustomizableLabelStyle(13, MapColors.GRID_ALTERNATE_LABEL_COLOR, "SECONDARY", aSecondaryStyleName);
    TLspCustomizableStyle tertiaryLabelStyle = createCustomizableLabelStyle(14, MapColors.GRID_EXCEPTIONAL_LABEL_COLOR, "TERTIARY", aTertiaryStyleName);

    List<TLspCustomizableStyle> primaryStyles = Collections.singletonList(primaryStyle);
    List<TLspCustomizableStyle> secondaryStyles = Collections.singletonList(secondaryStyle);
    List<TLspCustomizableStyle> tertiaryStyles = Collections.singletonList(tertiaryStyle);

    List<TLspCustomizableStyle> primaryLabelStyles = Collections.singletonList(primaryLabelStyle);
    List<TLspCustomizableStyle> secondaryLabelStyles = Collections.singletonList(secondaryLabelStyle);
    List<TLspCustomizableStyle> tertiaryLabelStyles = Collections.singletonList(tertiaryLabelStyle);

    String format15d = "D";
    String format1d = "d";
    String format10m = "X2 Y2";
    String format1m = "X2 Y2";
    String format1m10 = "X3 Y3";
    String format1m100 = "X4 Y4";
    String format1m1000 = "X5 Y5";
    String format1m10000 = "X6 Y6";

    double k = aCoarse ? COARSE_MULTIPLIER : DEFAULT_MULTIPLIER;
    TLspGeorefGridStyle.Builder<?> builder = TLspGeorefGridStyle.newBuilder();
    builder.body(SQUARES_15_DEGREES);
    builder.interval(MAX_ZOOMED_OUT, START_1_DEGREE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_DEGREE * k, START_10_MINUTES * k).customizableStyles(secondaryStyles);
    builder.interval(START_10_MINUTES * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_15_DEGREES);
    builder.interval(START_15_DEGREE_LABELS, START_1_DEGREE * k).customizableStyles(primaryLabelStyles).format(format15d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_DEGREE * k, START_10_MINUTES * k).customizableStyles(secondaryLabelStyles).format(format15d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10_MINUTES * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format15d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_DEGREE);
    builder.interval(START_1_DEGREE * k, START_10_MINUTES * k).customizableStyles(primaryStyles);
    builder.interval(START_10_MINUTES * k, START_1_MINUTE * k).customizableStyles(secondaryStyles);
    builder.interval(START_1_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1_DEGREE);
    builder.interval(START_1_DEGREE * k, START_10_MINUTES * k).customizableStyles(primaryLabelStyles).format(format1d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10_MINUTES * k, START_1_MINUTE * k).customizableStyles(secondaryLabelStyles).format(format1d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1d).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_10_MINUTES);
    builder.interval(START_10_MINUTES * k, START_1_MINUTE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_MINUTE * k, START_1_OVER_10_MINUTE * k).customizableStyles(secondaryStyles);
    builder.interval(START_1_OVER_10_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_10_MINUTES);
    builder.interval(START_10_MINUTES * k, START_1_MINUTE * k).customizableStyles(primaryLabelStyles).format(format10m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_MINUTE * k, START_1_OVER_10_MINUTE * k).customizableStyles(secondaryLabelStyles).format(format10m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_10_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format10m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_MINUTE);
    builder.interval(START_1_MINUTE * k, START_1_OVER_10_MINUTE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_OVER_10_MINUTE * k, START_1_OVER_100_MINUTE * k).customizableStyles(secondaryStyles);
    builder.interval(START_1_OVER_100_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1_MINUTE);
    builder.interval(START_1_MINUTE * k, START_1_OVER_10_MINUTE * k).customizableStyles(primaryLabelStyles).format(format1m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_10_MINUTE * k, START_1_OVER_100_MINUTE * k).customizableStyles(secondaryLabelStyles).format(format1m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_100_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_OVER_10_MINUTES);
    builder.interval(START_1_OVER_10_MINUTE * k, START_1_OVER_100_MINUTE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_OVER_100_MINUTE * k, START_1_OVER_1000_MINUTE * k).customizableStyles(secondaryStyles);
    builder.interval(START_1_OVER_1000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1_OVER_10_MINUTES);
    builder.interval(START_1_OVER_10_MINUTE * k, START_1_OVER_100_MINUTE * k).customizableStyles(primaryLabelStyles).format(format1m10).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_100_MINUTE * k, START_1_OVER_1000_MINUTE * k).customizableStyles(secondaryLabelStyles).format(format1m10).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_1000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1m10).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_OVER_100_MINUTES);
    builder.interval(START_1_OVER_100_MINUTE * k, START_1_OVER_1000_MINUTE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_OVER_1000_MINUTE * k, START_1_OVER_10000_MINUTE * k).customizableStyles(secondaryStyles);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1_OVER_100_MINUTES);
    builder.interval(START_1_OVER_100_MINUTE * k, START_1_OVER_1000_MINUTE * k).customizableStyles(primaryLabelStyles).format(format1m100).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_1000_MINUTE * k, START_1_OVER_10000_MINUTE * k).customizableStyles(secondaryLabelStyles).format(format1m100).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1m100).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_OVER_1000_MINUTES);
    builder.interval(START_1_OVER_1000_MINUTE * k, START_1_OVER_10000_MINUTE * k).customizableStyles(primaryStyles);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(secondaryStyles);
    builder.label(SQUARES_1_OVER_1000_MINUTES);
    builder.interval(START_1_OVER_1000_MINUTE * k, START_1_OVER_10000_MINUTE * k).customizableStyles(primaryLabelStyles).format(format1m1000).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(secondaryLabelStyles).format(format1m1000).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1_OVER_10000_MINUTES);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(primaryStyles);
    builder.label(SQUARES_1_OVER_10000_MINUTES);
    builder.interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).customizableStyles(primaryLabelStyles).format(format1m10000).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);

    return builder.build();
  }

  public static TLspGeorefGridStyle createOverlayStyle(boolean aCoarse, Font aFont, Color aTextColor, float aOpacity, Color aHaloColor, int aHaloThickness) {
    double k = aCoarse ? COARSE_MULTIPLIER : DEFAULT_MULTIPLIER;
    Collection<ALspStyle> overlayStyles = overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness);
    return TLspGeorefGridStyle.newBuilder()
                              .overlay()
                              .interval(MAX_ZOOMED_OUT, START_1_DEGREE * k).styles(overlayStyles).format("D")
                              .interval(START_1_DEGREE * k, START_10_MINUTES * k).styles(overlayStyles).format("D d")
                              .interval(START_10_MINUTES * k, START_1_MINUTE * k).styles(overlayStyles).format("D d X2 Y2")
                              .interval(START_1_MINUTE * k, START_1_OVER_10_MINUTE * k).styles(overlayStyles).format("D d X2 Y2")
                              .interval(START_1_OVER_10_MINUTE * k, START_1_OVER_100_MINUTE * k).styles(overlayStyles).format("D d X3 Y3")
                              .interval(START_1_OVER_100_MINUTE * k, START_1_OVER_1000_MINUTE * k).styles(overlayStyles).format("D d X4 Y4")
                              .interval(START_1_OVER_1000_MINUTE * k, START_1_OVER_10000_MINUTE * k).styles(overlayStyles).format("D d X5 Y5")
                              .interval(START_1_OVER_10000_MINUTE * k, MAX_ZOOMED_IN).styles(overlayStyles).format("D d X6 Y6")
                              .build();
  }

  private static Collection<ALspStyle> overlayStyles(Font aFont, Color aTextColor, float aAlpha, Color aHaloColor, int aHaloThickness) {
    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font(aFont)
                                           .textColor(aTextColor)
                                           .haloColor(aHaloColor)
                                           .haloThickness(aHaloThickness)
                                           .build();
    TLspLabelOpacityStyle opacityStyle = TLspLabelOpacityStyle.newBuilder()
                                                              .color(Color.white)
                                                              .opacity(aAlpha)
                                                              .build();
    return Arrays.asList(textStyle, opacityStyle);
  }

  private static TLspCustomizableStyle createCustomizableStyle(float aLineWidth, Color aColor, String aIdentifier, String aDisplayName) {
    return new TLspCustomizableStyle(TLspLineStyle.newBuilder().width(aLineWidth).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).color(aColor).build(),
                                     true,
                                     aIdentifier, aDisplayName);
  }

  private static TLspCustomizableStyle createCustomizableLabelStyle(int aFontSize, Color aTextColor, String aIdentifier, String aDisplayName) {
    Font font = new Font("Default", Font.BOLD, aFontSize);
    return new TLspCustomizableStyle(TLspTextStyle.newBuilder().font(font).textColor(aTextColor).haloColor(new Color(1.0f, 1.0f, 1.0f, 0.5f)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build(),
                                     true,
                                     aIdentifier, aDisplayName);
  }
}
