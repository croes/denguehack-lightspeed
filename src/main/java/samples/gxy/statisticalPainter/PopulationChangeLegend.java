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
 * the change in density and the corresponding filled circle.
 */
class PopulationChangeLegend extends JPanel {

  // preferred dimension
  private static int fWidth = 110;
  private static int fHeight = 140;

  public PopulationChangeLegend() {
    setPreferredSize(new Dimension(fWidth, fHeight));
    setMinimumSize(new Dimension(fWidth, fHeight));
  }

  public void paintComponent(Graphics aGraphics) {
    int x = Math.max(0, (getWidth() - fWidth) / 2);
    int y = Math.max(0, (getHeight() - fHeight) / 2);
    int header = 10;

    // background color of LegendIcon
    Color background_color = new Color(244, 216, 103);

    TLcdAWTUtil.drawReliefFrame(aGraphics, x, y, fWidth, fHeight);
    aGraphics.setColor(background_color);
    aGraphics.fillRect(x + 1, y + 1, fWidth - 2, fHeight - 2);
    aGraphics.setPaintMode();

    // paint header
    aGraphics.setColor(Color.black);
    aGraphics.setFont(new Font("Dialog", Font.PLAIN, 12));
    TLcdAWTUtil.drawString("+   Change   -", TLcdAWTUtil.CENTER, aGraphics, x + 1, y + 3, fWidth, header);

    int n = 5;
    int dy = (fHeight - header - 5) / n;
    int dx = fWidth - 2 * dy;

    y = y + 5 + header;

    aGraphics.setFont(new Font("Dialog", Font.PLAIN, 10));

    int change;
    for (int i = 0; i < n; i++) {
      change = (int) (Math.exp(((i * 3 - 1)) * PopulationUtil.getMaxDensityChange()));
      int dm = (dy - (i * 3 + 1)) / 2;
      aGraphics.setColor(PopulationChangePainter.POSITIVE_COLOR);
      aGraphics.fillRect(x + 5 + dm, y + dm, i * 3 + 1, i * 3 + 1);
      aGraphics.setColor(Color.black);
      TLcdAWTUtil.drawString("" + change, TLcdAWTUtil.CENTER, aGraphics, x + dy, y, dx, dy);
      aGraphics.setColor(PopulationChangePainter.NEGATIVE_COLOR);
      aGraphics.fillRect(x - 5 + dy + dx + dm, y + dm, i * 3 + 1, i * 3 + 1);
      y = y + dy;
    }
  }
}
