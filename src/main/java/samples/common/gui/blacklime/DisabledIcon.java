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
package samples.common.gui.blacklime;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.plaf.UIResource;

/**
 * Disabled icon that greys out the icons, as usually, but doing it more strongly than default look and feels.
 * This to make the difference between enabled and disabled icons more prominent, useful for a combination of
 * very dark backgrounds and white outlined icons.
 */
class DisabledIcon implements Icon, UIResource {
  private Icon fIcon;

  public DisabledIcon(Icon aIcon) {
    fIcon = aIcon;
  }

  @Override
  public int getIconWidth() {
    return fIcon.getIconWidth();
  }

  @Override
  public int getIconHeight() {
    return fIcon.getIconHeight();
  }

  @Override
  public void paintIcon(Component c, Graphics gr, int x, int y) {
    BufferedImage im = new BufferedImage(fIcon.getIconWidth(), fIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = im.getGraphics();
    fIcon.paintIcon(c, g, 0, 0);
    g.dispose();

    ImageFilter filter = new GrayFilter(true, 10);
    ImageProducer prod = new FilteredImageSource(im.getSource(), filter);
    Image greyImage = Toolkit.getDefaultToolkit().createImage(prod);

    gr.drawImage(greyImage, x, y, null);
  }
}
