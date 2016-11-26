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
package samples.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.GeneralPath;

import com.luciad.gui.ILcdIcon;

/**
 * A downward pointing triangle to indicate pop-up behavior.
 */
public class PopupTriangleIcon implements ILcdIcon {

  private final int fIconHeight;
  private final int fTriangleHeight;
  private final int fTriangleWidth;

  private Color fColor;

  /**
   * Creates an icon of the given height containing a triangle.
   * The triangle height will be a third of the given height.
   * @param aIconHeight the height of the icon
   */
  public PopupTriangleIcon(int aIconHeight) {
    fIconHeight = aIconHeight;
    fTriangleHeight = aIconHeight / 3;
    fTriangleWidth = fTriangleHeight * 2;
  }

  public void setColor(Color aColor) {
    fColor = aColor;
  }

  @Override
  public int getIconWidth() {
    return fTriangleWidth;
  }

  @Override
  public int getIconHeight() {
    return fIconHeight;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    y += (fIconHeight - fTriangleHeight) / 2;
    Graphics2D g2d = (Graphics2D) g;
    Paint oldPaint = g2d.getPaint();
    if (fColor == null) {
      g2d.setColor(UIColors.fg());
    } else {
      g2d.setPaint(fColor);
    }
    GeneralPath triangle = new GeneralPath();
    triangle.moveTo(x, y);
    triangle.lineTo(x + fTriangleHeight, y + fTriangleHeight);
    triangle.lineTo(x + fTriangleWidth, y);
    triangle.closePath();
    g2d.fill(triangle);
    g2d.setPaint(oldPaint);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
