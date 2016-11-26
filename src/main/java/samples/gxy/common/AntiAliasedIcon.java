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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.luciad.gui.ILcdIcon;
import com.luciad.util.ILcdCloneable;

/**
 * ILcdIcon wrapper that renders an icon anti-aliased, with decent interpolation.
 */
public class AntiAliasedIcon implements ILcdIcon, ILcdCloneable {

  private ILcdIcon fDelegate;

  public AntiAliasedIcon(ILcdIcon aDelegate) {
    fDelegate = aDelegate;
  }

  public ILcdIcon getDelegate() {
    return fDelegate;
  }

  public void paintIcon(Component c, Graphics aGraphics, int x, int y) {
    Graphics2D g = (Graphics2D) aGraphics;
    Object old_antialiasing_value = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    Object old_interpolation_value = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    fDelegate.paintIcon(c, aGraphics, x, y);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old_antialiasing_value);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old_interpolation_value == null ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : old_interpolation_value);
  }

  public int getIconWidth() {
    return fDelegate.getIconWidth();
  }

  public int getIconHeight() {
    return fDelegate.getIconHeight();
  }

  public Object clone() {
    try {
      AntiAliasedIcon clone = (AntiAliasedIcon) super.clone();
      clone.fDelegate = (ILcdIcon) fDelegate.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
