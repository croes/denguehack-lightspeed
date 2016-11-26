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

public class DiamondIcon extends SASSCIcon {

  protected int axisModificator;

  protected DiamondIcon(int aSize, Color aColor) {
    super(aSize, aColor, aColor);
    axisModificator = getAxisModificator(aSize);
  }

  protected static int getAxisModificator(int aSize) {
//		double v = aSize;
//		v = Math.pow(v, 2);
//		v /= 2;
//		v = Math.sqrt(v);
//		return (int)v;
    return (aSize - 1) / 2;
  }

  protected static void paint(int aSize, Graphics aGraphics, Color aColor, int x, int y) {
    aGraphics.setColor(aColor);
    Polygon p = createPolygon(x, y, getAxisModificator(aSize));
    aGraphics.drawPolygon(p);
  }

  public void paintIcon(Component c, Graphics graphics, int x, int y) {
    paint(size, graphics, lineColor, x, y);
  }

  protected static Polygon createPolygon(int x, int y, int anAxisModificator) {
    Polygon p = new Polygon();
    p.addPoint(x + 2 * anAxisModificator, y + anAxisModificator);
    p.addPoint(x + anAxisModificator, y);
    p.addPoint(x, y + anAxisModificator);
    p.addPoint(x + anAxisModificator, y + 2 * anAxisModificator);
    return p;
  }

  public Object clone() {
    return null;
  }

}
