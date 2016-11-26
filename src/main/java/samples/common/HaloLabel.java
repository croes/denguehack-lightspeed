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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.luciad.gui.TLcdHaloIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.gui.TextIcon;
import com.luciad.util.ELcdHorizontalAlignment;
import com.luciad.util.TLcdHaloAlgorithm;


/**
 * A JLabel whose text is painted with a halo.
 * The haloed text is rendered in the JLabel's icon; the class does not support adding an icon on top of that.
 */
public class HaloLabel extends JLabel {

  private static final Color WHITE = new Color(255, 255, 255, 230);
  private static final Color DARK = new Color(40, 40, 40, 230);

  private final TextIcon fTextIcon;
  private final TLcdHaloIcon fHaloIcon;

  private final BufferedImage fImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);


  public HaloLabel() {
    this("");
  }

  public HaloLabel(String aText) {
    this(aText, -1, false);
  }

  public HaloLabel(String aText, int aFontSize, boolean aCentered) {
    super(aText);
    fTextIcon = new TextIcon(aText);
    fTextIcon.setColor(WHITE);
    setOpaque(false);
    fHaloIcon = new TLcdHaloIcon(fTextIcon);
    fHaloIcon.setUseImageCache(false);
    fHaloIcon.setHaloColor(DARK);
    fHaloIcon.setHaloAlgorithm(TLcdHaloAlgorithm.CONVOLUTION);
    super.setIcon(new TLcdSWIcon(fHaloIcon));
    setHorizontalTextPosition(TRAILING);
    if (aFontSize != -1) {
      setFont(new Font(getFont().getName(), getFont().getStyle(), aFontSize));
    }
    if (aCentered) {
      setHorizontalAlignment(JLabel.CENTER);
      fTextIcon.setAlignment(ELcdHorizontalAlignment.CENTER);
    } else {
      fTextIcon.setAlignment(ELcdHorizontalAlignment.LEFT);
    }
  }

  /**
   * Sets the text color of this halo label to the given color
   * @param aColor the text color for this halo label
   */
  public void setTextColor(Color aColor) {
    fTextIcon.setColor(aColor);
    repaint();
  }

  /**
   * Sets the halo color of this halo label to the given color
   * @param aColor the halo color for this halo label
   */
  public void setHaloColor(Color aColor) {
    fHaloIcon.setHaloColor(aColor);
    repaint();
  }

  @Override
  public void setText(String text) {
    super.setText("");
    if (fTextIcon != null ) { // null check because the super constructor call setText, before the fields are initialized
      fTextIcon.setLines(Collections.singletonList(text));
      fTextIcon.recalculateSize(fImage.getGraphics());
      revalidate();
      repaint();
    }
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    if (fTextIcon != null) {
      fTextIcon.setFont(font);
      fTextIcon.recalculateSize(fImage.getGraphics());
      revalidate();
      repaint();
    }
  }

  @Override
  public void setIcon(Icon icon) {
    if (icon != null) {
      throw new UnsupportedOperationException("This implementation does not support icons");
    }
  }

  /**
   * @return the position and dimensions of the text inside the component.
   */
  protected final Rectangle getTextBounds() {
    Insets insets = getInsets();
    // only supports left and center alignment
    int startX = getHorizontalAlignment() == CENTER ? getWidth()/2 - fTextIcon.getTextWidth() : 0;
    return new Rectangle(insets.left + startX, insets.top, fTextIcon.getTextWidth(), fHaloIcon.getIconHeight());
  }
}
