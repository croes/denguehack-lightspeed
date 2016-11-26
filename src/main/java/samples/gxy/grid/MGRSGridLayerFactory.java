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

import static com.luciad.view.map.TLcdMGRSGridStyle.MGRSLevel.*;
import static com.luciad.view.map.TLcdMGRSGridStyle.MGRSType.UPS;
import static com.luciad.view.map.TLcdMGRSGridStyle.MGRSType.UTM;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.map.TLcdMGRSGridLayer;
import com.luciad.view.map.TLcdMGRSGridOverlayLabelBuilder;
import com.luciad.view.map.TLcdMGRSGridStyle;

public class MGRSGridLayerFactory {

  private static final double MAX_ZOOMED_IN = Double.MAX_VALUE;
  private static final double MAX_ZOOMED_OUT = 0.0;

  private static final double START_GRID_ZONE = 6.0e-5;
  private static final double START_100K = 1.0e-3;
  private static final double START_10K = 4.0e-3;
  private static final double START_1K = 4.0e-2;
  private static final double START_100M = 4.0e-1;
  private static final double START_10M = 4.0;
  private static final double START_1M = 40.0;

  public static final String MGRS_FORMAT_100000M = "S";
  public static final String MGRS_FORMAT_10000M = "X1 Y1";
  public static final String MGRS_FORMAT_1000M = "X2 Y2";
  public static final String MGRS_FORMAT_100M = "X3 Y3";
  public static final String MGRS_FORMAT_10M = "X4 Y4";
  public static final String MGRS_FORMAT_1M = "X5 Y5";

  public static final String UTM_FORMAT_1000000M = "U1 V1";
  public static final String UTM_FORMAT_100000M = "U2 V2";
  public static final String UTM_FORMAT_10000M = "U6 V6";
  public static final String UTM_FORMAT_1000M = "U4 V4";
  public static final String UTM_FORMAT_100M = "U5 V5";
  public static final String UTM_FORMAT_10M = "U6 V6";
  public static final String UTM_FORMAT_1M = "U7 V7";

  private final GridStyleSettings fStyleSettings = new GridStyleSettings();

  public GridStyleSettings getStyleSettings() {
    return fStyleSettings;
  }

  public ILcdGXYLayer createMGRSGridLayer() {
    TLcdMGRSGridLayer gridLayer = new TLcdMGRSGridLayer();
    gridLayer.setMGRSGridStyle(createGridStyle(true));

    // Add an overlay label that gives more context of at which coordinates the view is looking
    TLcdMGRSGridOverlayLabelBuilder overlayLabelBuilder = TLcdMGRSGridOverlayLabelBuilder.newBuilder()
                                                                           .content(TLcdMGRSGridOverlayLabelBuilder.Content.COMMON_VIEW_COORDINATE)
                                                                           .style(createOverlayLabelStyle(true));
    gridLayer.addOverlayLabel(overlayLabelBuilder, TLcdOverlayLayout.Location.NORTH);
    return gridLayer;
  }

  public TLcdMGRSGridStyle createGridStyle(boolean aMGRSCoordinates) {
    String format1000000m = aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_1000000M;
    String format100000m = aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_100000M;
    String format10000m = aMGRSCoordinates ? MGRS_FORMAT_10000M : UTM_FORMAT_10000M;
    String format1000m = aMGRSCoordinates ? MGRS_FORMAT_1000M : UTM_FORMAT_1000M;
    String format100m = aMGRSCoordinates ? MGRS_FORMAT_100M : UTM_FORMAT_100M;
    String format10m = aMGRSCoordinates ? MGRS_FORMAT_10M : UTM_FORMAT_10M;
    String format1m = aMGRSCoordinates ? MGRS_FORMAT_1M : UTM_FORMAT_1M;

    TLcdMGRSGridStyle.Builder<?> builder = TLcdMGRSGridStyle.newBuilder();
    builder.lineAntiAliasing(true);
    builder.labelAntiAliasing(true);
    builder.body(ZONES);
    builder.interval(MAX_ZOOMED_OUT, START_100K).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_100K, START_10K).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_10K, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.body(UTM, BANDS);
    builder.interval(START_GRID_ZONE, START_100K).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_100K, START_10K).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_10K, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(UTM, BANDS);
    builder.interval(START_GRID_ZONE, START_100K).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_100K, START_10K).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_10K, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.label(UPS, BANDS);
    builder.interval(START_GRID_ZONE, START_100K).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.body(SQUARES_1000000M);
    builder.interval(START_100K, START_10K).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_10K, START_1K).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_1K, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(SQUARES_1000000M);
    builder.interval(START_100K, START_10K).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format1000000m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_10K, START_1K).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format1000000m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_1K, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).format(format1000000m).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.body(SQUARES_100000M);
    builder.interval(START_100K, START_10K).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_10K, START_1K).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_1K, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(SQUARES_100000M);
    builder.interval(START_100K, START_10K).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format100000m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_10K, START_1K).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format100000m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_1K, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).format(format100000m).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.body(SQUARES_10000M);
    builder.interval(START_10K, START_1K).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_1K, START_100M).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_100M, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(SQUARES_10000M);
    builder.interval(START_10K, START_1K).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format10000m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_1K, START_100M).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format10000m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_100M, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).format(format10000m).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.body(SQUARES_1000M);
    builder.interval(START_1K, START_100M).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_100M, START_10M).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_10M, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(SQUARES_1000M);
    builder.interval(START_1K, START_100M).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format1000m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_100M, START_10M).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format1000m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_10M, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).format(format1000m).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.body(SQUARES_100M);
    builder.interval(START_100M, START_10M).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_10M, START_1M).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.interval(START_1M, MAX_ZOOMED_IN).lineColor(fStyleSettings.getTertiaryLineColor()).lineWidth(fStyleSettings.getTertiaryLineWidth());
    builder.label(SQUARES_100M);
    builder.interval(START_100M, START_10M).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format100m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_10M, START_1M).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format100m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.interval(START_1M, MAX_ZOOMED_IN).font(fStyleSettings.getTertiaryLabelFont()).labelColor(fStyleSettings.getTertiaryLabelTextColor()).format(format100m).labelHaloColor(fStyleSettings.getTertiaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getTertiaryLabelHaloThickness());
    builder.body(SQUARES_10M);
    builder.interval(START_10M, START_1M).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.interval(START_1M, MAX_ZOOMED_IN).lineColor(fStyleSettings.getSecondaryLineColor()).lineWidth(fStyleSettings.getSecondaryLineWidth());
    builder.label(SQUARES_10M);
    builder.interval(START_10M, START_1M).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format10m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    builder.interval(START_1M, MAX_ZOOMED_IN).font(fStyleSettings.getSecondaryLabelFont()).labelColor(fStyleSettings.getSecondaryLabelTextColor()).format(format10m).labelHaloColor(fStyleSettings.getSecondaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getSecondaryLabelHaloThickness());
    builder.body(SQUARES_1M);
    builder.interval(START_1M, MAX_ZOOMED_IN).lineColor(fStyleSettings.getPrimaryLineColor()).lineWidth(fStyleSettings.getPrimaryLineWidth());
    builder.label(SQUARES_1M);
    builder.interval(START_1M, MAX_ZOOMED_IN).font(fStyleSettings.getPrimaryLabelFont()).labelColor(fStyleSettings.getPrimaryLabelTextColor()).format(format1m).labelHaloColor(fStyleSettings.getPrimaryLabelHaloColor()).labelHaloThickness(fStyleSettings.getPrimaryLabelHaloThickness());
    return builder.build();
  }

  public TLcdMGRSGridStyle createOverlayLabelStyle(boolean aMGRSCoordinates) {
    String format1000000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_1000000M);
    String format100000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100000M : UTM_FORMAT_100000M);
    String format10000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_10000M : UTM_FORMAT_10000M);
    String format1000m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_1000M : UTM_FORMAT_1000M);
    String format100m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_100M : UTM_FORMAT_100M);
    String format10m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_10M : UTM_FORMAT_10M);
    String format1m = "ZB " + (aMGRSCoordinates ? MGRS_FORMAT_1M : UTM_FORMAT_1M);
    return TLcdMGRSGridStyle.newBuilder()
                            .overlay()
                            .interval(MAX_ZOOMED_OUT, START_GRID_ZONE).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format("ZB")
                            .interval(START_GRID_ZONE, START_100K).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format1000000m)
                            .interval(START_100K, START_10K).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format100000m)
                            .interval(START_10K, START_1K).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format10000m)
                            .interval(START_1K, START_100M).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format1000m)
                            .interval(START_100M, START_10M).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format100m)
                            .interval(START_10M, START_1M).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format10m)
                            .interval(START_1M, MAX_ZOOMED_IN).font(fStyleSettings.getOverlayLabelFont()).labelColor(fStyleSettings.getOverlayLabelTextColor()).labelHaloColor(fStyleSettings.getOverlayLabelHaloColor()).labelHaloThickness(fStyleSettings.getOverlayLabelHaloThickness()).format(format1m)
                            .build();
  }
}
