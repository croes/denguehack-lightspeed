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
package samples.lightspeed.demo.framework.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {
  private final Color fBackgroundColor;
  private final Color fBorderColor;
  private final Color fHoverColor = new Color(219, 216, 208, 255);
  private final Color fPressDownColor = new Color(254, 249, 250, 255);
  private final Color fSelectColor = new Color(191, 213, 227, 255);
  private int fCurve = 10;
  private int fHoverItem = -1;
  private int fTotalItems = 0;
  private int fPressDownItem = -1;
  private int fSelectedItem = -1;

  public RoundedBorder(int aCurve) {
    this(aCurve, DemoUIColors.PANEL_COLOR, DemoUIColors.PANEL_BORDER_COLOR);
  }

  /**
   *
   * @param aCurve
   * @param aBackgroundColor Typically {@link DemoUIColors#PANEL_COLOR} or {@link DemoUIColors#SUB_PANEL_COLOR}
   * @param aBorderColor Typically {@link DemoUIColors#PANEL_BORDER_COLOR}
   */
  public RoundedBorder(int aCurve, Color aBackgroundColor, Color aBorderColor) {
    fCurve = aCurve;
    fBackgroundColor = aBackgroundColor;
    fBorderColor = aBorderColor;
  }

  public int getTotalItems() {
    return fTotalItems;
  }

  public void setTotalItems(int aTotalItems) {
    fTotalItems = aTotalItems;
  }

  /**
   * Sets the hover of this border.
   *
   * @param aHoverItem
   */
  public void setHoverItem(int aHoverItem) {
    fHoverItem = aHoverItem;
  }

  /**
   * Sets the pressdown of this border.
   *
   * @param aPressDown
   */
  public void setPressDownItem(int aPressDown) {
    fPressDownItem = aPressDown;
  }

  /**
   * Sets the selection of this border.
   * @param aSelection
   */
  public void setSelectedItem(int aSelection) {
    fSelectedItem = aSelection;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    ((Graphics2D) g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(fBackgroundColor);
    g.fillRoundRect(x, y, w - 1, h - 1, fCurve, fCurve);
    g.setColor(fBorderColor);
    g.drawRoundRect(x, y, w - 1, h - 1, fCurve, fCurve);
    g.setColor(fBackgroundColor);
    if (fTotalItems > 0 && fSelectedItem >= 0) {
      g.setColor(fSelectColor);
      int selectionHeight = h / fTotalItems;
      int selectionY = y + selectionHeight * fSelectedItem;
      g.fillRoundRect(x, selectionY, w, selectionHeight, fCurve, fCurve);
    }
    if (fTotalItems > 0 && fHoverItem >= 0) {
      g.setColor(fHoverColor);
      int selectionHeight = h / fTotalItems;
      int selectionY = y + selectionHeight * fHoverItem;
      g.fillRoundRect(x, selectionY, w, selectionHeight, fCurve, fCurve);
    }
    if (fTotalItems > 0 && fPressDownItem >= 0) {
      g.setColor(fPressDownColor);
      int selectionHeight = h / fTotalItems;
      int selectionY = y + selectionHeight * fPressDownItem;
      g.fillRoundRect(x, selectionY, w, selectionHeight, fCurve, fCurve);
    }

  }

  public Insets getBorderInsets(Component c) {
    int inset = (int) Math.sqrt(fCurve);
    return new Insets(inset, inset, inset, inset);
  }

  public Insets getBorderInsets(Component aComponent, Insets aInsets) {
    int inset = (int) Math.sqrt(fCurve);
    aInsets.left = inset;
    aInsets.right = inset;
    aInsets.top = inset;
    aInsets.bottom = inset;
    return aInsets;
  }

  public boolean isBorderOpaque() {
    return true;
  }

  public int getCurve() {
    return fCurve;
  }

  public Color getBackgroundColor() {
    return fBackgroundColor;
  }
}
