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
package samples.symbology.nvg.lightspeed;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.luciad.gui.ILcdIcon;

/**
 * An <code>ILcdIcon</code> that makes a given <code>ILcdIcon</code> transparent.
 */
class TransparentIconWrapper implements ILcdIcon {

  private final float fAlpha;
  private final ILcdIcon fIcon;

  public TransparentIconWrapper(ILcdIcon aIcon, float aAlpha) {
    fIcon = aIcon;
    fAlpha = aAlpha;
  }

  public int getIconHeight() {
    return fIcon.getIconHeight();
  }

  public int getIconWidth() {
    return fIcon.getIconWidth();
  }

  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    Graphics2D g2d = (Graphics2D) aGraphics;

    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fAlpha);
    g2d.setComposite(ac);

    fIcon.paintIcon(aComponent, g2d, aX, aY);
  }

  public Object clone() {
    return new TransparentIconWrapper((ILcdIcon) fIcon.clone(), fAlpha);
  }

}
