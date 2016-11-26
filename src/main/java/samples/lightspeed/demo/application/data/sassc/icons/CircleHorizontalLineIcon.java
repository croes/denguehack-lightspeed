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

public class CircleHorizontalLineIcon extends CircleIcon {

  private Color topColor;
  private Color bottomColor;

  protected CircleHorizontalLineIcon(int aSize, Color aLineColor, Color aTopColor, Color aBottomColor) {
    super(aSize, aLineColor);
    topColor = aTopColor;
    bottomColor = aBottomColor;
  }

  @Override
  public void paintIcon(Component c, Graphics aGraphics, int x, int y) {
    paint(size, aGraphics, lineColor, topColor, bottomColor, x, y);
  }

  protected static void paint(int aSize, Graphics aGraphics, Color aLineColor, Color aTopColor, Color aBottomColor, int x, int y) {
    aGraphics.setColor(aTopColor);
    aGraphics.fillArc(x, y, aSize, aSize, 0, 180);

    aGraphics.setColor(aBottomColor);
    aGraphics.fillArc(x, y, aSize, aSize, 0, -180);

    aGraphics.setColor(aLineColor);
    aGraphics.drawOval(x, y, aSize, aSize);
    aGraphics.drawLine(x, y + aSize / 2, x + aSize, y + aSize / 2);
  }

  @Override
  public Object clone() {
    return new CircleHorizontalLineIcon(size, fillColor, topColor, bottomColor);
  }
}