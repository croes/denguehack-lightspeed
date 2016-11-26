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

import static com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle.MGRSLevel.*;
import static com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle.MGRSType.UPS;
import static com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle.MGRSType.UTM;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import samples.common.MapColors;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle.Orientation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

public final class MGRSGridStyleFactory {

  public static final double DEFAULT_LABEL_GRID_OFFSET = 3;
  public static final double DEFAULT_LABEL_EDGE_OFFSET = 6;

  private static final Color OVERLAY_TEXT_COLOR = Color.black;
  private static final Color OVERLAY_HALO_COLOR = Color.white;

  // Interval constants
  private static final double MAX_ZOOMED_IN = Double.MAX_VALUE;
  private static final double MAX_ZOOMED_OUT = 0.0;

  private static final double START_GRID_ZONE = 6.0e-5;
  private static final double START_100K = 1.0e-3; // When 100px represent 100000m on the map
  private static final double START_10K = 4.0e-3; // When 40px represent 10000m on the map
  private static final double START_1K = 4.0e-2; // When 40px represent 1000m on the map
  private static final double START_100M = 4.0e-1; // When 40px represent 100m on the map
  private static final double START_10M = 4.0; // When 40px represent 10m on the map
  private static final double START_1M = 40.0; // When 40px represent 1m on the map

  // Grid density constants. One of these is multiplied with the above interval constants. When the multiplier is
  // larger, it means that grid lines will appear later (e.g. when the multiplier is 3, the 1m lines will appear
  // when 120px represent 1m on the map.
  private static final double DEFAULT_MULTIPLIER = 1.0;
  private static final double COARSE_MULTIPLIER = 3.0;

  // Label formatting constants
  private static final String MGRS_FORMAT_100000M = "S";
  private static final String MGRS_FORMAT_10000M = "X1 Y1";
  private static final String MGRS_FORMAT_1000M = "X2 Y2";
  private static final String MGRS_FORMAT_100M = "X3 Y3";
  private static final String MGRS_FORMAT_10M = "X4 Y4";
  private static final String MGRS_FORMAT_1M = "X5 Y5";

  private static final String UTM_FORMAT_1000000M = "U1 V1";
  private static final String UTM_FORMAT_100000M = "U2 V2";
  private static final String UTM_FORMAT_10000M = "U6 V6";
  private static final String UTM_FORMAT_1000M = "U4 V4";
  private static final String UTM_FORMAT_100M = "U5 V5";
  private static final String UTM_FORMAT_10M = "U6 V6";
  private static final String UTM_FORMAT_1M = "U7 V7";

  public static TLspMGRSGridStyle createDefaultMGRSGridStyle(boolean aMGRSCoordinates) {
    return createGridStyle(aMGRSCoordinates, false, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET);
  }

  public static TLspMGRSGridStyle createGridStyle(boolean aMGRSCoordinates,
                                                  boolean aCoarse,
                                                  Orientation aOrientation,
                                                  double aGridOffset,
                                                  double aEdgeOffset) {
    return createGridStyle(aMGRSCoordinates, aCoarse, aOrientation, aGridOffset, aEdgeOffset, "Primary", "Secondary", "Tertiary");
  }

  public static TLspMGRSGridStyle createGridStyle(boolean aMGRSCoordinates,
                                                  String aPrimaryStyleName,
                                                  String aSecondaryStyleName,
                                                  String aTertiaryStyleName) {
    return createGridStyle(aMGRSCoordinates, false, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET, aPrimaryStyleName, aSecondaryStyleName, aTertiaryStyleName);
  }

  private static TLspMGRSGridStyle createGridStyle(boolean aMGRSCoordinates,
                                                   boolean aCoarse,
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

    Collection<ALspStyle> overlayStyles = createDefaultOverlayStyles(12, OVERLAY_TEXT_COLOR, OVERLAY_HALO_COLOR);

    String format1000000m = aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_1000000M;
    String format100000m = aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_100000M;
    String format10000m = aMGRSCoordinates ? MGRS_FORMAT_10000M : UTM_FORMAT_10000M;
    String format1000m = aMGRSCoordinates ? MGRS_FORMAT_1000M : UTM_FORMAT_1000M;
    String format100m = aMGRSCoordinates ? MGRS_FORMAT_100M : UTM_FORMAT_100M;
    String format10m = aMGRSCoordinates ? MGRS_FORMAT_10M : UTM_FORMAT_10M;
    String format1m = aMGRSCoordinates ? MGRS_FORMAT_1M : UTM_FORMAT_1M;

    double k = aCoarse ? COARSE_MULTIPLIER : DEFAULT_MULTIPLIER;
    TLspMGRSGridStyle.Builder<?> builder = TLspMGRSGridStyle.newBuilder();
    builder.body(ZONES);
    builder.interval(MAX_ZOOMED_OUT, START_100K * k).customizableStyles(primaryStyles);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(secondaryStyles);
    builder.interval(START_10K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.body(UTM, BANDS);
    builder.interval(START_GRID_ZONE * k, START_100K * k).customizableStyles(primaryStyles);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(secondaryStyles);
    builder.interval(START_10K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(UTM, BANDS);
    builder.interval(START_GRID_ZONE * k, START_100K * k).customizableStyles(primaryLabelStyles);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(primaryLabelStyles);
    builder.interval(START_10K * k, MAX_ZOOMED_IN).customizableStyles(primaryLabelStyles);
    builder.label(UPS, BANDS);
    builder.interval(START_GRID_ZONE * k, START_100K * k).customizableStyles(primaryLabelStyles);
    builder.body(SQUARES_1000000M);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(primaryStyles);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(secondaryStyles);
    builder.interval(START_1K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1000000M);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(primaryLabelStyles).format(format1000000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(secondaryLabelStyles).format(format1000000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1000000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_100000M);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(primaryStyles);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(secondaryStyles);
    builder.interval(START_1K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_100000M);
    builder.interval(START_100K * k, START_10K * k).customizableStyles(primaryLabelStyles).format(format100000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(secondaryLabelStyles).format(format100000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1K * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format100000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_10000M);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(primaryStyles);
    builder.interval(START_1K * k, START_100M * k).customizableStyles(secondaryStyles);
    builder.interval(START_100M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_10000M);
    builder.interval(START_10K * k, START_1K * k).customizableStyles(primaryLabelStyles).format(format10000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1K * k, START_100M * k).customizableStyles(secondaryLabelStyles).format(format10000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_100M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format10000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1000M);
    builder.interval(START_1K * k, START_100M * k).customizableStyles(primaryStyles);
    builder.interval(START_100M * k, START_10M * k).customizableStyles(secondaryStyles);
    builder.interval(START_10M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_1000M);
    builder.interval(START_1K * k, START_100M * k).customizableStyles(primaryLabelStyles).format(format1000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format1000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_100M * k, START_10M * k).customizableStyles(secondaryLabelStyles).format(format1000m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_100M);
    builder.interval(START_100M * k, START_10M * k).customizableStyles(primaryStyles);
    builder.interval(START_10M * k, START_1M * k).customizableStyles(secondaryStyles);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
    builder.label(SQUARES_100M);
    builder.interval(START_100M * k, START_10M * k).customizableStyles(primaryLabelStyles).format(format100m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_10M * k, START_1M * k).customizableStyles(secondaryLabelStyles).format(format100m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format100m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_10M);
    builder.interval(START_10M * k, START_1M * k).customizableStyles(primaryStyles);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(secondaryStyles);
    builder.label(SQUARES_10M);
    builder.interval(START_10M * k, START_1M * k).customizableStyles(primaryLabelStyles).format(format10m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(secondaryLabelStyles).format(format10m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.body(SQUARES_1M);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(primaryStyles);
    builder.label(SQUARES_1M);
    builder.interval(START_1M * k, MAX_ZOOMED_IN).customizableStyles(primaryLabelStyles).format(format1m).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
    builder.overlay();
    builder.interval(MAX_ZOOMED_OUT, MAX_ZOOMED_IN).styles(overlayStyles);
    return builder.build();
  }

  public static TLspMGRSGridStyle createOverlayStyle(boolean aCoarse,
                                                     Font aFont,
                                                     Color aTextColor,
                                                     float aOpacity,
                                                     Color aHaloColor,
                                                     int aHaloThickness,
                                                     boolean aMGRSCoordinates) {
    double k = aCoarse ? COARSE_MULTIPLIER : DEFAULT_MULTIPLIER;
    String format1000000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_1000000M);
    String format100000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_100000M);
    String format10000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_10000M : UTM_FORMAT_10000M);
    String format1000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_1000M : UTM_FORMAT_1000M);
    String format100m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100M : UTM_FORMAT_100M);
    String format10m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_10M : UTM_FORMAT_10M);
    String format1m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_1M : UTM_FORMAT_1M);
    return TLspMGRSGridStyle.newBuilder()
                            .overlay()
                            .interval(MAX_ZOOMED_OUT, START_GRID_ZONE * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format("ZB")
                            .interval(START_GRID_ZONE * k, START_100K * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format1000000m)
                            .interval(START_100K * k, START_10K * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format100000m)
                            .interval(START_10K * k, START_1K * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format10000m)
                            .interval(START_1K * k, START_100M * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format1000m)
                            .interval(START_100M * k, START_10M * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format100m)
                            .interval(START_10M * k, START_1M * k).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format10m)
                            .interval(START_1M * k, MAX_ZOOMED_IN).styles(overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness)).format(format1m)
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

  private static Collection<ALspStyle> createDefaultOverlayStyles(int aFontSize, Color aTextColor, Color aHaloColor) {
    Font font = new Font("Default", Font.PLAIN, aFontSize);
    return Collections.<ALspStyle>singletonList(TLspTextStyle.newBuilder().font(font).textColor(aTextColor).haloColor(aHaloColor).build());
  }
}
