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
package samples.gxy.grid;

import java.awt.Font;
import java.text.DecimalFormat;

import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.text.GridPointFormat;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.map.TLcdXYGridLayer;
import com.luciad.view.map.TLcdXYGridOverlayLabelBuilder;
import com.luciad.view.map.TLcdXYGridStyle;

public class XYGridLayerFactory {

  // Interval constants
  private static final double MAX_ZOOMED_OUT = 0;
  private static final double MAX_ZOOMED_IN = Double.MAX_VALUE;

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
  private static final double[] SPACING_SUBDIVISIONS = new double[]{5.0, 2.0, 1.0};

  private final GridStyleSettings fStyleSettings = new GridStyleSettings();

  public GridStyleSettings getStyleSettings() {
    return fStyleSettings;
  }

  public ILcdGXYLayer createXYGridLayer() {
    TLcdXYGridLayer gridLayer = new TLcdXYGridLayer();
    gridLayer.setXYGridStyle(createGridStyle());

    TLcdXYGridOverlayLabelBuilder overlayLabelBuilder = TLcdXYGridOverlayLabelBuilder.newBuilder()
                                                                                     .content(TLcdXYGridOverlayLabelBuilder.Content.COORDINATE_AT_CENTER)
                                                                                     .style(createOverlayLabelStyle());
    gridLayer.addOverlayLabel(overlayLabelBuilder, TLcdOverlayLayout.Location.NORTH);
    return gridLayer;
  }

  public TLcdXYGridStyle createGridStyle() {
    DecimalFormat format = new DecimalFormat();
    format.setMaximumFractionDigits(0);

    TLcdXYGridStyle.Builder<?> builder = TLcdXYGridStyle.newBuilder();
    builder.lineAntiAliasing(true);
    builder.labelAntiAliasing(true);
    for (int i = 0; i < SPACINGS.length; i++) {
      double spacing = SPACINGS[i];
      // Look ahead 1 or 2 spacings. This is needed to determine if a secondary or tertiary style is used. E.g. the
      // last spacing (1m spacing) is ALWAYS shown using a primary style. Other lines can be shown using secondary
      // or tertiary styles.
      boolean hasNextSpacing = i < SPACINGS.length - 1;
      boolean hasNextNextSpacing = i < SPACINGS.length - 2;

      // Subdivides each spacing into 3 more spacings (1, 2, 5), but makes sure that the prominent lines behave as expected.
      for (double levelSubdivision : SPACING_SUBDIVISIONS) {
        double subSpacing = spacing * levelSubdivision;

        // Scale (=pixel/world unit) for the non-prominent lines
        double startPrimaryScale = MINIMUM_SPACING_PIXELS / subSpacing;

        // Scale (=pixel/world unit) for the more prominent lines
        double startSecondaryScale = hasNextSpacing ? MINIMUM_SPACING_PIXELS / (SPACINGS[i + 1] * levelSubdivision) : MAX_ZOOMED_IN;

        // Scale (=pixel/world unit) for the prominent lines
        double startTertiaryScale = hasNextNextSpacing ? MINIMUM_SPACING_PIXELS / (SPACINGS[i + 2] * levelSubdivision) : MAX_ZOOMED_IN;

        // Configure the line styles for each spacing
        builder.body(subSpacing);
        builder.interval(startPrimaryScale, startSecondaryScale).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
        if (hasNextSpacing) {
          builder.interval(startSecondaryScale, startTertiaryScale).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
        }
        if (hasNextNextSpacing) {
          builder.interval(startTertiaryScale, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
        }

        // Configure the label styles for each spacing
        builder.label(subSpacing);
        builder.interval(startPrimaryScale, startSecondaryScale).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness()).format(format);
        if (hasNextSpacing) {
          builder.interval(startSecondaryScale, startTertiaryScale).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness()).format(format);
        }
        if (hasNextNextSpacing) {
          builder.interval(startTertiaryScale, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness()).format(format);
        }
      }
    }
    return builder.build();
  }

  public TLcdXYGridStyle createOverlayLabelStyle() {
    GridPointFormat format = GridPointFormat.DEFAULT;
    return TLcdXYGridStyle.newBuilder()
                          .overlay()
                          .interval(MAX_ZOOMED_OUT, MAX_ZOOMED_IN).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format)
                          .build();
  }

  private static Font createDefaultFont(int aFontSize) {
    return new Font("Default", Font.BOLD, aFontSize);
  }
}
