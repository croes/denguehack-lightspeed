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
package samples.lightspeed.demo.application.data.sassc.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;

public class SquareHorizontalLineIcon extends SquareIcon {

  private Color topColor;
  private Color bottomColor;

  protected SquareHorizontalLineIcon(int aSize, Color aLineColor, Color aTopSideColor, Color aBottomSideColor) {
    super(aSize, aLineColor, aLineColor);
    topColor = aTopSideColor;
    bottomColor = aBottomSideColor;
  }

  @Override
  public Object clone() {
    return new SquareHorizontalLineIcon(size, lineColor, topColor, bottomColor);
  }

  @Override
  public void paintIcon(Component c, Graphics aGraphics, int x, int y) {
    paint(size, aGraphics, lineColor, topColor, bottomColor, x, y);
  }

  private static Polygon createSide(int x, int y, int aSize, boolean top) {
    Polygon p = new Polygon();
    if (top) {
      p.addPoint(x, y);
      p.addPoint(x + aSize, y);
      p.addPoint(x + aSize, y + aSize / 2);
      p.addPoint(x, y + aSize / 2);
    } else {
      p.addPoint(x, y + aSize / 2);
      p.addPoint(x + aSize, y + aSize / 2);
      p.addPoint(x + aSize, y + aSize);
      p.addPoint(x, y + aSize);
    }
    return p;
  }

  public static void paint(int aSize, Graphics aGraphics, Color aLineColor, Color aTopColor, Color aBottomColor, int x, int y) {
    aGraphics.setColor(aTopColor);
    aGraphics.fillPolygon(createSide(x, y, aSize, true));

    aGraphics.setColor(aBottomColor);
    aGraphics.fillPolygon(createSide(x, y, aSize, false));

    aGraphics.setColor(aLineColor);
    aGraphics.drawPolygon(createPolygon(x, y, aSize));
    aGraphics.drawLine(x, y + aSize / 2, x + aSize, y + aSize / 2);
  }

}
