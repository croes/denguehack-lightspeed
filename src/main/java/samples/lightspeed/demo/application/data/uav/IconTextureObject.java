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
package samples.lightspeed.demo.application.data.uav;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.lightspeed.util.opengl.texture.TLsp2DImageTextureObject;

/**
 * Extension of {@link TLsp2DImageTextureObject}
 * that takes an {@link ILcdIcon} as input rather than a
 * {@code BufferedImage}.
 */
public class IconTextureObject extends TLsp2DImageTextureObject {

  public IconTextureObject(ILcdIcon aIcon) {
    super(iconToImage(aIcon));
  }

  private static BufferedImage iconToImage(ILcdIcon icon) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    // use pre-multiplied to avoid a conversion!
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g = image.createGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();
    return image;
  }

}
