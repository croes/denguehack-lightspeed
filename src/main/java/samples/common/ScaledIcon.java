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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.luciad.gui.ILcdIcon;

/**
 * An icon that applies a scale to an icon before painting it.
 */
public final class ScaledIcon implements ILcdIcon, Cloneable {

  private ILcdIcon fDelegateIcon;
  private final double fScale;

  public ScaledIcon(ILcdIcon aDelegateIcon, double aScale) {
    fDelegateIcon = aDelegateIcon;
    fScale = aScale;
  }

  public ILcdIcon getDelegateIcon() {
    return fDelegateIcon;
  }

  public double getScale() {
    return fScale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ScaledIcon that = (ScaledIcon) o;

    if (Double.compare(that.fScale, fScale) != 0) {
      return false;
    }
    if (fDelegateIcon != null ? !fDelegateIcon.equals(that.fDelegateIcon) : that.fDelegateIcon != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = fDelegateIcon != null ? fDelegateIcon.hashCode() : 0;
    long temp = Double.doubleToLongBits(fScale);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2d = (Graphics2D) g;

    AffineTransform transform_to_restore = g2d.getTransform();

    g2d.translate(x + (getIconWidth()) / 2, y + (getIconHeight()) / 2);
    g2d.scale(fScale, fScale);
    fDelegateIcon.paintIcon(c, g2d, (-fDelegateIcon.getIconWidth()) / 2, (-fDelegateIcon.getIconHeight()) / 2);

    g2d.setTransform(transform_to_restore);
  }

  @Override
  public int getIconWidth() {
    return (int) (Math.ceil(fDelegateIcon.getIconWidth() * fScale));
  }

  @Override
  public int getIconHeight() {
    return (int) (Math.ceil(fDelegateIcon.getIconHeight() * fScale));
  }

  @Override
  public Object clone() {
    try {
      ScaledIcon clone = (ScaledIcon) super.clone();
      clone.fDelegateIcon = fDelegateIcon == null ? null : (ILcdIcon) fDelegateIcon.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e); //this can't happen
    }
  }
}
