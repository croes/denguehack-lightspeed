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
package samples.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {

  private Color fBorderColor = Color.gray;
  private int fCurve = 10;

  public RoundedBorder(Color aBorderColor, int aCurve) {
    fBorderColor = aBorderColor;
    fCurve = aCurve;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    ((Graphics2D) g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(fBorderColor);
    g.fillRoundRect(x, y, w, h, fCurve, fCurve);
  }

  @Override
  public Insets getBorderInsets(Component c) {
    int inset = (int) Math.sqrt(fCurve);
    return new Insets(inset, inset, inset, inset);
  }

  @Override
  public Insets getBorderInsets(Component aComponent, Insets aInsets) {
    int inset = (int) Math.sqrt(fCurve);
    aInsets.left = inset;
    aInsets.right = inset;
    aInsets.top = inset;
    aInsets.bottom = inset;
    return aInsets;
  }

  @Override
  public boolean isBorderOpaque() {
    return true;
  }
}
