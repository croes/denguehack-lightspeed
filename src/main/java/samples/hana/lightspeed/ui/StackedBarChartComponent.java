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
package samples.hana.lightspeed.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.common.FontStyle;
import samples.hana.lightspeed.domain.InsuranceCompany;

/**
 * Creates a simple chart component that is capable of showing statistics for insurance companies in a stacked bar chart.
 */
public class StackedBarChartComponent {

  public static JComponent createChart(Map<InsuranceCompany, List<Double>> aData) {
    return new ChartComponent(aData);
  }

  private static class ChartComponent extends JLabel {

    private static final int DOMAIN_AXIS_OFFSET = 3;
    private static final int RANGE_AXIS_OFFSET = 3;
    private static final int CHART_CONTENT_OFFSET = 6;
    private static final int TICK_LENGTH = 3;

    private static final int BAR_HEIGHT = 12;
    private static final int BAR_LABEL_OFFSET = 2;
    private static final int BAR_DROP_SHADOW_OFFSET = 4;
    private static final Color[] BAR_COLORS = new Color[]{
        ColorPalette.a,
        ColorPalette.b,
        ColorPalette.c,
        ColorPalette.d
    };

    private final Font fAxisFont;
    private final Font fLabelFont;
    private final double fMaxValue;
    private final Map<InsuranceCompany, List<Double>> fData;

    private final int fAxisFontHeight;
    private final int fLabelFontHeight;

    public ChartComponent(Map<InsuranceCompany, List<Double>> aData) {
      Graphics2D tempGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
      fAxisFont = FontStyle.getFont(FontStyle.NORMAL);
      fAxisFontHeight = tempGraphics.getFontMetrics(fAxisFont).getHeight();
      fLabelFont = FontStyle.getFont(FontStyle.NORMAL);
      fLabelFontHeight = tempGraphics.getFontMetrics(fLabelFont).getHeight();
      fMaxValue = getMaxValue(aData);
      fData = aData;
    }

    @Override
    protected void paintComponent(Graphics g) {
      // Enable anti-aliasing
      Graphics2D g2d = (Graphics2D) g;
      Object old_antialiasing_value = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      Object old_interpolation_value = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

      drawChart(g2d);

      // Disable anti-aliasing
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old_antialiasing_value);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old_interpolation_value == null ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : old_interpolation_value);
    }

    private void drawChart(Graphics2D g) {
      paintDomainAxis(g);
      paintRangeAxis(g);
      paintContentBackground(g);
      paintContent(g);
    }

    private void paintDomainAxis(Graphics2D g) {
      // paint domain axis
      g.setColor(Color.gray);
      g.drawLine(DOMAIN_AXIS_OFFSET, fAxisFontHeight + CHART_CONTENT_OFFSET, DOMAIN_AXIS_OFFSET, getHeight());
      // paint domain axis ticks
      int domainCount = fData.keySet().size();
      int domainHeight = fLabelFontHeight + BAR_HEIGHT + BAR_LABEL_OFFSET;
      int spacingBetweenBars = (getHeight() - fAxisFontHeight - CHART_CONTENT_OFFSET - domainCount * domainHeight) / domainCount;
      int currentY = fAxisFontHeight + CHART_CONTENT_OFFSET;
      for (int i = 0; i < domainCount; i++) {
        int tickY = currentY + domainHeight - BAR_HEIGHT / 2;
        g.drawLine(DOMAIN_AXIS_OFFSET - TICK_LENGTH + 1, tickY, DOMAIN_AXIS_OFFSET, tickY);
        currentY += domainHeight + spacingBetweenBars;
      }
    }

    private void paintRangeAxis(Graphics2D g) {
      // paint range axis
      g.setColor(Color.gray);
      g.drawLine(CHART_CONTENT_OFFSET, fAxisFontHeight + RANGE_AXIS_OFFSET, getWidth(), fAxisFontHeight + RANGE_AXIS_OFFSET);
      g.drawLine(CHART_CONTENT_OFFSET, fAxisFontHeight + RANGE_AXIS_OFFSET - TICK_LENGTH + 1, CHART_CONTENT_OFFSET, fAxisFontHeight + RANGE_AXIS_OFFSET);
      g.drawLine((CHART_CONTENT_OFFSET + getWidth()) / 2, fAxisFontHeight + RANGE_AXIS_OFFSET - TICK_LENGTH + 1, (CHART_CONTENT_OFFSET + getWidth()) / 2, fAxisFontHeight + RANGE_AXIS_OFFSET);

      // paint range axis labels
      String lowestValueString = "0";
      String middleValueString = String.format(Locale.ENGLISH, "%,d", (int) (fMaxValue / 2));
      FontMetrics axisFm = g.getFontMetrics(fAxisFont);
      int lowestStringWidth = axisFm.stringWidth(lowestValueString);
      int middleStringWidth = axisFm.stringWidth(middleValueString);
      g.setFont(fAxisFont);
      g.setColor(Color.gray.brighter());
      g.drawString(lowestValueString, CHART_CONTENT_OFFSET - lowestStringWidth / 2, axisFm.getAscent());
      g.drawString(middleValueString, (CHART_CONTENT_OFFSET + getWidth() - middleStringWidth) / 2, axisFm.getAscent());
    }

    private void paintContentBackground(Graphics2D g) {
      // Draw background
      g.setColor(ColorPalette.ui_background.darker());
      g.fillRect(CHART_CONTENT_OFFSET, fAxisFontHeight + CHART_CONTENT_OFFSET, getWidth(), getHeight());

      // Draw vertical grid line
      g.setColor(Color.gray.darker());
      Stroke oldStroke = g.getStroke();
      g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f));
      g.drawLine((CHART_CONTENT_OFFSET + getWidth()) / 2, fAxisFontHeight + CHART_CONTENT_OFFSET, (CHART_CONTENT_OFFSET + getWidth()) / 2, getHeight());
      g.drawLine((CHART_CONTENT_OFFSET + getWidth()) / 4, fAxisFontHeight + CHART_CONTENT_OFFSET, (CHART_CONTENT_OFFSET + getWidth()) / 4, getHeight());
      g.drawLine((3 * (CHART_CONTENT_OFFSET + getWidth())) / 4, fAxisFontHeight + CHART_CONTENT_OFFSET, (3 * (CHART_CONTENT_OFFSET + getWidth())) / 4, getHeight());
      g.setStroke(oldStroke);
    }

    private void paintContent(Graphics2D g) {
      int domainCount = fData.keySet().size();
      int domainHeight = fLabelFontHeight + BAR_HEIGHT + BAR_LABEL_OFFSET;
      int spacingBetweenBars = (getHeight() - fAxisFontHeight - CHART_CONTENT_OFFSET - domainCount * domainHeight) / domainCount;

      int currentY = fAxisFontHeight + CHART_CONTENT_OFFSET;
      for (Map.Entry<InsuranceCompany, List<Double>> domainEntry : fData.entrySet()) {
        InsuranceCompany domainKey = domainEntry.getKey();
        List<Double> values = domainEntry.getValue();

        paintBar(g, currentY, domainHeight, values, domainKey);

        currentY += domainHeight + spacingBetweenBars;
      }
    }

    private void paintBar(Graphics2D g, int aCurrentY, int aDomainHeight, List<Double> aValues, InsuranceCompany aDomainKey) {
      // Paint the label above the bar
      FontMetrics labelFm = g.getFontMetrics(fLabelFont);
      String domainLabel = aDomainKey.toString().replace('_', ' ');
      g.setColor(Color.gray.brighter().brighter());
      g.setFont(fLabelFont);
      g.drawString(domainLabel, CHART_CONTENT_OFFSET, aCurrentY + labelFm.getAscent());

      int barY = aCurrentY + aDomainHeight - BAR_HEIGHT;

      // Paint a drop shadow for the bar
      double total = 0.0;
      for (Double value : aValues) {
        total += value;
      }
      double valuePerPixel = fMaxValue / (getWidth() - CHART_CONTENT_OFFSET);
      g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
      g.fillRect(CHART_CONTENT_OFFSET, barY + BAR_DROP_SHADOW_OFFSET, (int) (total / valuePerPixel) + BAR_DROP_SHADOW_OFFSET, BAR_HEIGHT);

      // Paint the bar itself
      int barPart = 0;
      double currentTotal = 0.0;
      for (Double value : aValues) {
        Color barColor = BAR_COLORS[barPart % 4];
        int barStartX = CHART_CONTENT_OFFSET + (int) (currentTotal / valuePerPixel);
        currentTotal += value;
        int barEndX = CHART_CONTENT_OFFSET + (int) (currentTotal / valuePerPixel);

        if (barEndX > barStartX) {
          // Base bar fill
          g.setPaint(new GradientPaint(0, barY, barColor.darker(), 0, barY + BAR_HEIGHT, barColor.brighter()));
          g.fillRect(barStartX, barY, barEndX - barStartX, BAR_HEIGHT);

          // Add a shading effect to the bar fill
          g.setPaint(null);
          g.setColor(barColor);
          g.drawLine(barStartX, barY + BAR_HEIGHT - 1, barEndX - 1, barY + BAR_HEIGHT - 1);
          g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
          g.drawLine(barStartX, barY + 1, barEndX - 1, barY + 1);
          g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.25f));
          g.drawLine(barStartX, barY + 2, barEndX - 1, barY + 2);
        }
        barPart++;
      }
    }
  }

  private static double getMaxValue(Map<InsuranceCompany, List<Double>> aData) {
    double max = 0.0;
    for (List<Double> dataMap : aData.values()) {
      double total = 0.0;
      for (Double value : dataMap) {
        total += value;
      }
      max = Math.max(max, total);
    }
    max = findUpper_1_2_5(max);
    return max;
  }

  private static double findUpper_1_2_5(double aInput) {
    double exponent = Math.log(aInput) / Math.log(10);

    int exponent_as_int = (int) Math.floor(exponent);

    double lowest_value = Math.pow(10, exponent_as_int);
    if (aInput < 2 * lowest_value) {
      return 2 * lowest_value;
    } else if (aInput < 5 * lowest_value) {
      return 5 * lowest_value;
    } else {
      return 10 * lowest_value;
    }
  }

}
