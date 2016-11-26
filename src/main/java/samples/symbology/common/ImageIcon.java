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
package samples.symbology.common;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import com.luciad.gui.ILcdAnchoredIcon;
import com.luciad.gui.ILcdIcon;

/**
 * Implementation of ILcdIcon that paints an ILcdIcon on a BufferedImage. If the given ILcdIcon is
 * vector-based, painting the BufferedImage will increase the drawing performance significantly.
 */
public class ImageIcon implements ILcdAnchoredIcon {

  private BufferedImage fImage;
  private Point fAnchorPoint = new Point();

  public ImageIcon(ILcdIcon aIcon) {
    if (aIcon instanceof ILcdAnchoredIcon) {
      ((ILcdAnchoredIcon) aIcon).anchorPointSFCT(fAnchorPoint);
    } else {
      fAnchorPoint.move(aIcon.getIconWidth() / 2, aIcon.getIconHeight() / 2);
    }
    fImage = createImage(aIcon);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException exc) {
      // This should never occur.
      throw new InternalError("Clone not supported by " + this.getClass().getName());
    }
  }

  @Override
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    aGraphics.drawImage(fImage, aX, aY, fImage.getWidth(), fImage.getHeight(), null);
  }

  /**
   * Returns a BufferedImage with 8-bit RGBA color components on which the given icon is painted.
   *
   * @param aIcon The ILcdIcon to be painted on a BufferedImage.
   *
   * @return A BufferedImage with 8-bit RGBA color components on which the given icon is painted.
   */
  private BufferedImage createImage(ILcdIcon aIcon) {
    BufferedImage buffered_image = new BufferedImage(aIcon.getIconWidth(), aIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);

    Graphics graphics = buffered_image.getGraphics();
    aIcon.paintIcon(null, graphics, 0, 0);
    graphics.dispose();

    return buffered_image;
  }

  @Override
  public int getIconWidth() {
    return fImage.getWidth();
  }

  @Override
  public int getIconHeight() {
    return fImage.getHeight();
  }

  @Override
  public void anchorPointSFCT(Point aPointSFCT) {
    aPointSFCT.move(fAnchorPoint.x, fAnchorPoint.y);
  }
}
