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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.util.ELcdHorizontalAlignment;

/**
 * Multi-line text icon. See the setLines method to change the text.
 * The resized method is called if the icon size changes.
 */
public class TextIcon implements ILcdIcon {

  private List<String> fLines = null;
  private Color fColor = new Color(255, 255, 255, 230);
  private Font fFont = new Font("dialog", Font.PLAIN, 13);
  private int fWidth;
  private int fHeight;
  private int fTextWidth;
  private int fWidthIncrement = 20;
  private ELcdHorizontalAlignment fAlignment = ELcdHorizontalAlignment.CENTER;

  private final BufferedImage fImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

  public TextIcon() {
  }

  public TextIcon(String aText) {
    setLines(Collections.singletonList(aText));
  }

  public List<String> getLines() {
    return fLines;
  }

  public void setLines(List<String> aLines) {
    fLines = aLines;
  }

  public Color getColor() {
    return fColor;
  }

  public void setColor(Color aColor) {
    fColor = aColor;
  }

  protected void resized() {
  }

  @Override
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    if (fLines == null || fLines.size() == 0) {
      return;
    }
    Graphics2D g = (Graphics2D) aGraphics;
    Object previous = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    aGraphics.setColor(fColor);
    aGraphics.setFont(fFont);

    recalculateSize(aGraphics);

    FontMetrics fontMetrics = aGraphics.getFontMetrics();
    int iconWidth = getIconWidth();
    if (iconWidth == 0) {
      return;
    }
    for (String line : fLines) {
      int x;
      switch (fAlignment) {
      case RIGHT:
        x = iconWidth - fontMetrics.stringWidth(line);
        break;
      case CENTER:
        x = (iconWidth - fontMetrics.stringWidth(line)) / 2;
        break;
      case LEFT:
      default:
        x = 0;
      }
      aGraphics.drawString(line, aX + x, aY + fontMetrics.getAscent() + fontMetrics.getLeading());
      aY = aY + fontMetrics.getHeight();
    }
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previous);
  }

  /**
   * Checks and changes the size, based on the font, content, and so on.
   * Calling this method after updating the icon state prevents the icon from changing its size during painting.
   */
  public void recalculateSize(Graphics aGraphics) {
    if (fLines != null && !fLines.isEmpty()) {
      aGraphics.setColor(fColor);
      aGraphics.setFont(fFont);
      FontMetrics fm = aGraphics.getFontMetrics();

      int width = 0;
      int height = (fm.getHeight() + 1) * fLines.size();
      for (String line : fLines) {
        if (line.isEmpty()) {
          continue;
        }
        Rectangle2D stringBounds = fm.getStringBounds(line, aGraphics);
        width = Math.max(width, (int) stringBounds.getWidth());
      }
      if (width == 0) {
        height = 0;
      }

      fTextWidth = width;
      if (width > fWidth || height > fHeight || width < fWidth - 20 || height < fHeight) {
        fWidth = width + fWidthIncrement;
        fHeight = height;
        resized();
      }
    }
  }

  @Override
  public int getIconWidth() {
    if (fWidth == 0) {
      recalculateSize(fImage.getGraphics());
    }
    return fWidth;
  }

  @Override
  public int getIconHeight() {
    if (fHeight == 0) {
      recalculateSize(fImage.getGraphics());
    }
    return fHeight;
  }

  public void setAlignment(ELcdHorizontalAlignment aAlignment) {
    fAlignment = aAlignment;
  }

  /**
   * @return the width of the actual text (regardless of the width increment).
   */
  public int getTextWidth() {
    return fTextWidth;
  }

  /**
   * Sets the amount with which to increase the width if new text is wider than the previous one.
   * This avoids jitter for content that often changes (e.g. when showing mouse coordinates).
   * @param aWidthIncrement the new amount, or 0 if the width should just follow the text dimensions.
   *                        The default is 20.
   */
  public void setWidthIncrement(int aWidthIncrement) {
    fWidthIncrement = aWidthIncrement;
  }

  public int getWidthIncrement() {
    return fWidthIncrement;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  public void setFont(Font aFont) {
    fFont = aFont;
  }
}
