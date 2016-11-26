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
package samples.gxy.clustering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import com.luciad.gui.ILcdIcon;

/**
 * <p>
 *   {@code ILcdIcon} used to display a cluster.
 *   The icon is a circle, and allows to show a text in the middle of the circle.
 *   This can for example be used to display the number of elements in the cluster.
 * </p>
 */
final class ClusterIcon implements ILcdIcon {

  private static final Font DEFAULT_FONT = new Font("SansSerif", Font.BOLD, 13);

  private Color fFillColor = ClusterIconProvider.DEFAULT_FILL_COLOR;
  private Color fOutlineColor = ClusterIconProvider.DEFAULT_OUTLINE_COLOR;
  private Color fTextColor = ClusterIconProvider.DEFAULT_TEXT_COLOR;
  private Font fFont = DEFAULT_FONT;
  private int fOuterSize = 25;
  private int fInnerSize = 19;
  private String fText = "";

  public Color getFillColor() {
    return fFillColor;
  }

  public void setFillColor(Color aFillColor) {
    fFillColor = aFillColor;
  }

  public Color getOutlineColor() {
    return fOutlineColor;
  }

  public void setOutlineColor(Color aOutlineColor) {
    fOutlineColor = aOutlineColor;
  }

  public Color getTextColor() {
    return fTextColor;
  }

  public void setTextColor(Color aTextColor) {
    fTextColor = aTextColor;
  }

  public Font getFont() {
    return fFont;
  }

  public void setFont(Font aFont) {
    fFont = aFont;
  }

  public void setSize(int aOuterSize, int aInnerSize) {
    fOuterSize = aOuterSize;
    fInnerSize = aInnerSize;
  }

  public void setText(String aText) {
    fText = aText;
  }

  @Override
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    Graphics2D g = (Graphics2D) aGraphics;
    Object previousAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    Object previousTextAA = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      paintIconImpl(g, aX, aY);
    } finally {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousAA);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, previousTextAA);
    }
  }

  private void paintIconImpl(Graphics2D aGraphics, int aX, int aY) {
    int iconSize = getIconWidth();
    int borderSize = (fOuterSize - fInnerSize) / 2;

    int o = (int) Math.ceil(borderSize * 0.5);
    Stroke oldStroke = aGraphics.getStroke();
    try {
      aGraphics.setStroke(new BasicStroke(borderSize));
      aGraphics.setColor(fOutlineColor);
      aGraphics.drawOval(aX + o, aY + o, iconSize - 2 * o - 1, iconSize - 2 * o - 1);
    } finally {
      aGraphics.setStroke(oldStroke);
    }
    aGraphics.setColor(fFillColor);
    aGraphics.fillOval(aX + borderSize, aY + borderSize, iconSize - 2 * borderSize, iconSize - 2 * borderSize);

    if (!(fText == null || fText.isEmpty())) {
      aGraphics.setColor(fTextColor);
      aGraphics.setFont(fFont);
      FontMetrics fontMetrics = aGraphics.getFontMetrics();
      int x = (int) ((getIconWidth() - fontMetrics.stringWidth(fText)) / 2d + 0.5);
      aGraphics.drawString(fText, aX + x, aY + (int) ((getIconHeight() + fontMetrics.getAscent()) / 2d) - 1);
    }
  }

  @Override
  public int getIconWidth() {
    return fOuterSize;
  }

  @Override
  public int getIconHeight() {
    return getIconWidth();
  }

  @Override
  public Object clone() {
    try {
      ClusterIcon clone = (ClusterIcon) super.clone();
      clone.fFillColor = new Color(fFillColor.getRGB());
      clone.fOutlineColor = new Color(fOutlineColor.getRGB());
      clone.fTextColor = new Color(fTextColor.getRGB());
      clone.fFont = new Font(fFont.getName(), fFont.getStyle(), fFont.getSize());
      clone.fOuterSize = fOuterSize;
      clone.fInnerSize = fInnerSize;
      clone.fText = fText;
      return clone;
    } catch (CloneNotSupportedException aE) {
      throw new RuntimeException(aE);
    }
  }
}
