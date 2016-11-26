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
package samples.gxy.statisticalPainter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;

/**
 * This panel shows the different possible ranges of value for
 * the density and the corresponding colorcode.
 */
class PopulationDensityLegend extends JPanel {

  // preferred dimension
  private static int fWidth = 110;
  private static int fHeight = 140;

  public PopulationDensityLegend() {
    setPreferredSize(new Dimension(fWidth, fHeight));
    setMinimumSize(new Dimension(fWidth, fHeight));
  }

  public void paintComponent(Graphics aGraphics) {
    int x = Math.max(0, (getWidth() - fWidth) / 2);
    int y = Math.max(0, (getHeight() - fHeight) / 2);

    // nr of different range-code pairs
    int nr_of_range_code_pairs = 10;
    // difference in density between two range-code pairs
    double density_delta = PopulationUtil.getMaxDensity() / nr_of_range_code_pairs;
    // background color of LegendIcon
    Color background_color = new Color(244, 216, 103);
    // height of individual pair
    int pair_height = (fHeight - 5) / nr_of_range_code_pairs;

    // icon itself
    TLcdAWTUtil.drawReliefFrame(aGraphics, x, y, fWidth, fHeight);
    aGraphics.setColor(background_color);
    aGraphics.fillRect(x + 1, y + 1, fWidth - 2, fHeight - 2);
    aGraphics.setPaintMode();

    // paint header
    aGraphics.setColor(Color.black);
//      aGraphics.setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
//      TLcdAWTUtil.drawString( "Pop Density", TLcdAWTUtil.CENTER, aGraphics, x + 1, y + 1, fWidth, header_height );

    // set font for range
    aGraphics.setFont(new Font("Dialog", Font.PLAIN, 10));

    // starting height
    y = y + 5;
    // max of next range
    int delta2 = (int) Math.pow(10, density_delta);
    // min of next range
    int delta1;

    drawRange("< " + delta2, Color.white, aGraphics, x, y, pair_height);
    for (int i = 2; i < nr_of_range_code_pairs; i++) {
      // new y coordinate for drawing
      y = y + pair_height;
      // next range
      delta1 = delta2;
      delta2 = (int) Math.pow(10, i * density_delta);
      drawRange(
          delta1 + " - " + delta2,
          PopulationDensityPainter.colorForFactor((double) i / (double) nr_of_range_code_pairs),
          aGraphics,
          x, y, pair_height
               );
    }
    y = y + pair_height;
    drawRange("> " + delta2, new Color(55, 55, 55), aGraphics, x, y, pair_height);
  }

  private void drawRange(String aLabel,
                         Color aColor,
                         Graphics aGraphics,
                         int aX,
                         int aY,
                         int aPairHeight) {
    // x coord of string with range
    int x = 52;
    // x coord with rectangle colored in according color
    int width = fWidth - x - 5;

    aGraphics.setColor(Color.black);
    TLcdAWTUtil.drawString(aLabel, TLcdAWTUtil.CENTER, aGraphics, aX + 2, aY, x, aPairHeight);
    aGraphics.setColor(aColor);
    aGraphics.fillRect(aX + x, aY, width, aPairHeight);
  }
}
