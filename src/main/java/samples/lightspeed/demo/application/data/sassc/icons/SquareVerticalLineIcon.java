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

public class SquareVerticalLineIcon extends SquareIcon {

  private Color leftColor;
  private Color rightColor;

  protected SquareVerticalLineIcon(int aSize, Color aLineColor, Color aLeftSideColor, Color aRightSideColor) {
    super(aSize, NULL_COLOR, aLineColor);
    leftColor = aLeftSideColor;
    rightColor = aRightSideColor;
  }

  public Object clone() {
    return new SquareVerticalLineIcon(size, lineColor, leftColor, rightColor);
  }

  @Override
  public void paintIcon(Component c, Graphics aGraphics, int x, int y) {
    paint(size, aGraphics, lineColor, leftColor, rightColor, x, y);
  }

  private static Polygon createSide(int x, int y, int aSize, boolean left) {
    Polygon p = new Polygon();
    if (left) {
      p.addPoint(x, y);
      p.addPoint(x + aSize / 2, y);
      p.addPoint(x + aSize / 2, y + aSize);
      p.addPoint(x, y + aSize);
    } else {
      p.addPoint(x + aSize / 2, y);
      p.addPoint(x + aSize, y);
      p.addPoint(x + aSize, y + aSize);
      p.addPoint(x + aSize / 2, y + aSize);
    }
    return p;
  }

  public static void paint(int aSize, Graphics aGraphics, Color aLineColor, Color aLeftColor, Color aRightColor, int x, int y) {
    aGraphics.setColor(aLeftColor);
    aGraphics.fillPolygon(createSide(x, y, aSize, true));

    aGraphics.setColor(aRightColor);
    aGraphics.fillPolygon(createSide(x, y, aSize, false));

    aGraphics.setColor(aLineColor);
    aGraphics.drawPolygon(createPolygon(x, y, aSize));
    aGraphics.drawLine(x + aSize / 2, y, x + aSize / 2, y + aSize);
  }

}
