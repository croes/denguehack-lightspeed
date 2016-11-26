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

public abstract class SquareIcon extends SASSCIcon {

  protected SquareIcon(int aSize, Color aFillColor, Color aLineColor) {
    super(aSize, aFillColor, aLineColor);
  }

  public abstract Object clone();

  protected static void paint(int aSize, Graphics aGraphics, Color aColor, int x, int y) {
    Polygon p = createPolygon(x, y, aSize);
    aGraphics.setColor(aColor);
    aGraphics.fillPolygon(p);
    aGraphics.setColor(aColor);
    aGraphics.drawPolygon(p);
  }

  @Override
  public void paintIcon(Component c, Graphics aGraphics, int x, int y) {
    Polygon p = createPolygon(x, y, size);
    aGraphics.setColor(fillColor);
    aGraphics.fillPolygon(p);
    aGraphics.setColor(lineColor);
    aGraphics.drawPolygon(p);

  }

  protected static Polygon createPolygon(int x, int y, int aSize) {
    Polygon p = new Polygon();
    p.addPoint(x, y);
    p.addPoint(x, y + aSize);
    p.addPoint(x + aSize, y + aSize);
    p.addPoint(x + aSize, y);
    return p;
  }
}