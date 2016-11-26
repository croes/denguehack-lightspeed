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
package samples.gxy.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdHaloIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.util.TLcdCopyright;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Luciad logo icon.
 */
public class LogoIcon implements ILcdIcon, Icon {

  private ILcdIcon fDelegate;

  public static void setupLogo(ILcdGXYView aGXYView) {
    TLcdCopyright.setCopyright("");
    if ("".equals(TLcdCopyright.getCopyright())) {
      aGXYView.putCornerIcon(new LogoIcon(), ILcdGXYView.LOWERLEFT);
    }
  }

  public LogoIcon() {
    TLcdResizeableIcon resizeableIcon = new TLcdResizeableIcon(new TLcdImageIcon("images/luciad_logo.png"), 80, -1);
    TLcdHaloIcon haloIcon = new TLcdHaloIcon(resizeableIcon, new Color(220, 220, 220), 1);
    fDelegate = new PaddedIcon(haloIcon, 5);
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    fDelegate.paintIcon(c, g, x, y);
  }

  public int getIconWidth() {
    return fDelegate.getIconWidth();
  }

  public int getIconHeight() {
    return fDelegate.getIconHeight();
  }

  @Override
  public Object clone() {
    try {
      LogoIcon clone = (LogoIcon) super.clone();
      clone.fDelegate = (ILcdIcon) this.fDelegate.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Cloning is not supported for this object : " + this);
    }
  }
}
