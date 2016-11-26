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
package samples.lucy.cop.addons.airpicturetheme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import com.luciad.gui.ILcdIcon;

/**
 * {@code ILcdIcon} which represents a track with a heading symbol. The heading
 * indicator points to the right.
 */
final class HeadingIcon implements ILcdIcon {
  private static final int TRACK_ICON_BORDER_SIZE = 2;
  private static final int TRACK_ICON_TOTAL_SIZE = 8;
  private static final int HEADING_LENGTH = 8;
  private static final int HEADING_THICKNESS = 2;
  private static final int EXTRA_EMPTY_BORDER = 2;

  private Color fIconColor;
  private Color fIconBorderColor;
  private Color fHeadingColor;

  public HeadingIcon() {
    this(new Color(0, 0, 255, (int) (0.9 * 255)),
         new Color(255, 255, 255),
         new Color(255, 209, 25));
  }

  public HeadingIcon(Color aIconColor, Color aIconBorderColor, Color aHeadingColor) {
    fIconColor = aIconColor;
    fIconBorderColor = aIconBorderColor;
    fHeadingColor = aHeadingColor;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color originalColor = g.getColor();
    g.translate(EXTRA_EMPTY_BORDER + x, EXTRA_EMPTY_BORDER + y);

    g.setColor(fIconBorderColor);
    g.fillRect(0, 0, TRACK_ICON_TOTAL_SIZE, TRACK_ICON_TOTAL_SIZE);
    g.setColor(fIconColor);
    g.fillRect(TRACK_ICON_BORDER_SIZE, TRACK_ICON_BORDER_SIZE, TRACK_ICON_TOTAL_SIZE - 2 * TRACK_ICON_BORDER_SIZE, TRACK_ICON_TOTAL_SIZE - 2 * TRACK_ICON_BORDER_SIZE);

    g.setColor(fHeadingColor);
    int headingStart = (TRACK_ICON_TOTAL_SIZE - HEADING_THICKNESS) / 2;
    g.fillRect(TRACK_ICON_TOTAL_SIZE, headingStart, HEADING_LENGTH, HEADING_THICKNESS);

    g.translate(-EXTRA_EMPTY_BORDER - x, -EXTRA_EMPTY_BORDER - y);
    g.setColor(originalColor);
  }

  @Override
  public int getIconWidth() {
    return TRACK_ICON_TOTAL_SIZE + HEADING_LENGTH + 2 * EXTRA_EMPTY_BORDER;
  }

  @Override
  public int getIconHeight() {
    return TRACK_ICON_TOTAL_SIZE + 2 * EXTRA_EMPTY_BORDER;
  }

  @Override
  public Object clone() {
    try {
      HeadingIcon clone = (HeadingIcon) super.clone();
      clone.fHeadingColor = fHeadingColor;
      clone.fIconBorderColor = fIconBorderColor;
      clone.fIconColor = fIconColor;
      return clone;
    } catch (CloneNotSupportedException e) {
      //should never happen
      throw new RuntimeException(e);
    }
  }
}
