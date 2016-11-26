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
package samples.lightspeed.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.luciad.gui.ILcdIcon;

/**
 * An icon that draws a white arrow.
 */
public class ArrowIcon implements ILcdIcon {

  private final int fSize;

  public ArrowIcon(int aSize) {
    fSize = aSize * 2;
  }

  @Override
  public int getIconHeight() {
    return fSize;
  }

  @Override
  public int getIconWidth() {
    return (fSize / 5);
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    ((Graphics2D) g).setStroke(new BasicStroke(1));
    g.setColor(Color.lightGray);

    int w = getIconWidth();
    int t = getIconWidth() / 2 - 1;
    int h = getIconHeight() / 2;
    int m = w / 2;
    g.drawLine(m, 1, m, h);
    g.drawLine(m, 1, m + t, t + 1);
    g.drawLine(m, 1, m - t, t + 1);
  }

  @Override
  public Object clone() {
    return this;
  }
}
