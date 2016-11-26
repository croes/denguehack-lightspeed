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

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import samples.common.MapColors;
import samples.common.text.GridPointFormat;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle.Orientation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

public final class XYGridStyleFactory {

  public static final double DEFAULT_LABEL_GRID_OFFSET = 3;
  public static final double DEFAULT_LABEL_EDGE_OFFSET = 6;

  private static final Color OVERLAY_TEXT_COLOR = Color.black;
  private static final Color OVERLAY_HALO_COLOR = Color.white;

  // Interval constants
  private static final double MAX_ZOOMED_IN = Double.MAX_VALUE;
  private static final double MAX_ZOOMED_OUT = 0.0;

  private static final double MINIMUM_SPACING_PIXELS = 100.0; // New levels appear when their lines lie approximately 100 pixels apart
  private static final double[] SPACINGS = new double[] {
      10000000.0, // 10000km
      1000000.0, // 1000km
      100000.0, // 100km
      10000.0, // 10km
      1000.0, // 1km
      100.0, // 100m
      10.0, // 10m
      1.0 // 1m
  };
  private static final double[] LEVEL_SUBDIVISIONS = new double[]{5.0, 2.0, 1.0};

  // Grid density constants. One of these is multiplied with the above interval constants. When the multiplier is
  // larger, it means that grid lines will appear later (e.g. when the multiplier is 3, the 1m lines will appear
  // when 300px represent 1m on the map.
  private static final double DEFAULT_MULTIPLIER = 1.0;
  private static final double COARSE_MULTIPLIER = 3.0;

  public static TLspXYGridStyle createGridStyle(boolean aCoarse) {
    return createGridStyle(aCoarse, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET, "Primary", "Secondary", "Tertiary");
  }

  public static TLspXYGridStyle createGridStyle(boolean aCoarse,
                                                Orientation aOrientation,
                                                double aGridOffset,
                                                double aEdgeOffset) {
    return createGridStyle(aCoarse, aOrientation, aGridOffset, aEdgeOffset, "Primary", "Secondary", "Tertiary");
  }

  public static TLspXYGridStyle createGridStyle(String aPrimaryStyleName,
                                                String aSecondaryStyleName,
                                                String aTertiaryStyleName) {
    return createGridStyle(false, Orientation.ALONG_LINE, DEFAULT_LABEL_GRID_OFFSET, DEFAULT_LABEL_EDGE_OFFSET, aPrimaryStyleName, aSecondaryStyleName, aTertiaryStyleName);
  }

  private static TLspXYGridStyle createGridStyle(boolean aCoarse,
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

    DecimalFormat format = new DecimalFormat();
    format.setMaximumFractionDigits(0);

    TLspXYGridStyle.Builder<?> builder = TLspXYGridStyle.newBuilder();

    double k = aCoarse ? COARSE_MULTIPLIER : DEFAULT_MULTIPLIER;
    for (int i = 0; i < SPACINGS.length; i++) {
      double spacing = SPACINGS[i];
      boolean hasNextSpacing = i < SPACINGS.length - 1;
      boolean hasNextNextSpacing = i < SPACINGS.length - 2;

      // Subdivides each spacing into 3 more spacings (1, 2, 5), but makes sure that the prominent lines behave as expected.
      for (double levelSubdivision : LEVEL_SUBDIVISIONS) {
        double subSpacing = spacing * levelSubdivision;

        // Scale (=pixel/world unit) for the non-prominent lines
        double startPrimaryScale = k * MINIMUM_SPACING_PIXELS / subSpacing;

        // Scale (=pixel/world unit) for the more prominent lines
        double startSecondaryScale = hasNextSpacing ? k * MINIMUM_SPACING_PIXELS / (SPACINGS[i + 1] * levelSubdivision) : MAX_ZOOMED_IN;

        // Scale (=pixel/world unit) for the prominent lines
        double startTertiaryScale = hasNextNextSpacing ? k * MINIMUM_SPACING_PIXELS / (SPACINGS[i + 2] * levelSubdivision) : MAX_ZOOMED_IN;

        // Configure the line styles for each spacing
        builder.body(subSpacing);
        builder.interval(startPrimaryScale, startSecondaryScale).customizableStyles(primaryStyles);
        if (hasNextSpacing) {
          builder.interval(startSecondaryScale, startTertiaryScale).customizableStyles(secondaryStyles);
        }
        if (hasNextNextSpacing) {
          builder.interval(startTertiaryScale, MAX_ZOOMED_IN).customizableStyles(tertiaryStyles);
        }

        // Configure the label styles for each spacing
        builder.label(subSpacing);
        builder.interval(startPrimaryScale, startSecondaryScale).customizableStyles(primaryLabelStyles).format(format).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
        if (hasNextSpacing) {
          builder.interval(startSecondaryScale, startTertiaryScale).customizableStyles(secondaryLabelStyles).format(format).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
        }
        if (hasNextNextSpacing) {
          builder.interval(startTertiaryScale, MAX_ZOOMED_IN).customizableStyles(tertiaryLabelStyles).format(format).labelOrientation(aOrientation).labelGridOffset(aGridOffset).labelEdgeOffset(aEdgeOffset);
        }
      }
    }

    Collection<ALspStyle> overlayStyles = createDefaultOverlayStyles(12, OVERLAY_TEXT_COLOR, OVERLAY_HALO_COLOR);
    builder.overlay();
    builder.interval(MAX_ZOOMED_OUT, MAX_ZOOMED_IN).styles(overlayStyles);

    return builder.build();
  }

  /**
   * Creates a style that can be used for overlay labels. Overlay labels are Swing components that are typically shown
   * on the overlay panel of the view. They can be added to any other UI element though. This happen in this sample for
   * example: a coordinate readout is added to the right panel in the sample.
   */
  public static TLspXYGridStyle createOverlayStyle(Font aFont, Color aTextColor, float aOpacity, Color aHaloColor, int aHaloThickness) {
    GridPointFormat format = new GridPointFormat();
    DecimalFormat distanceFormat = new DecimalFormat();
    distanceFormat.setGroupingUsed(true);
    format.setDistanceFormat(distanceFormat);
    Collection<ALspStyle> overlayStyles = overlayStyles(aFont, aTextColor, aOpacity, aHaloColor, aHaloThickness);
    return TLspXYGridStyle.newBuilder()
                          .overlay()
                          .interval(MAX_ZOOMED_OUT, MAX_ZOOMED_IN).styles(overlayStyles).format(format)
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

  // Use customizable styles to be able to easily adjust the styles e.g. in the UI
  private static TLspCustomizableStyle createCustomizableStyle(float aLineWidth, Color aColor, String aIdentifier, String aDisplayName) {
    return new TLspCustomizableStyle(TLspLineStyle.newBuilder().width(aLineWidth).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).color(aColor).build(),
                                     true,
                                     aIdentifier, aDisplayName);
  }

  // Use customizable styles to be able to easily adjust the styles e.g. in the UI
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
