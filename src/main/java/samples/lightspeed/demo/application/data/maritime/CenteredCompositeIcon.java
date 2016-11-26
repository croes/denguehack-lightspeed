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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Vector;

import com.luciad.gui.ILcdIcon;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * An <code>ILcdResizeableIcon</code> composed from multiple <code>ILcdResizeableIcon</code>s.
 */
public class CenteredCompositeIcon implements ILcdIcon, Cloneable {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CenteredCompositeIcon.class.getName());
  private Vector fIcons = new Vector();
  private int fIconWidth;
  private int fIconHeight;

  /**
   * Add an icon.
   * @param aIcon a ILcdResizeableIcon.
   */
  public void addIcon(ILcdIcon aIcon) {
    fIcons.add(aIcon);
    recalculateSize();
  }

  /**
   * Remove an icon.
   * @param aIcon a ILcdResizeableIcon.
   */
  public void removeIcon(ILcdIcon aIcon) {
    fIcons.remove(aIcon);
    recalculateSize();
  }

  /**
   * Remove all icons.
   */
  public void removeAll() {
    fIcons.clear();
    recalculateSize();
  }

  /**
   * Get number of sub icons.
   * @return number of sub icons.
   */
  public int getIconCount() {
    return fIcons.size();
  }

  @Override
  public int getIconWidth() {
    return fIconWidth;
  }

  @Override
  public int getIconHeight() {
    return fIconHeight;
  }

  /**
   * Get the sub icon at the given index.
   * @param aIndex the index.
   * @return the ILcdResizeableIcon at the given index.
   */
  public ILcdIcon getIcon(int aIndex) {
    return (ILcdIcon) fIcons.get(aIndex);
  }

  private void recalculateSize() {
    int width = 0;
    int height = 0;

    for (int i = 0; i < fIcons.size(); i++) {
      ILcdIcon icon = (ILcdIcon) fIcons.get(i);
      int iconWidth = icon.getIconWidth();
      int iconHeight = icon.getIconHeight();
      if (width < iconWidth) {
        width = iconWidth;
      }
      if (height < iconHeight) {
        height = iconHeight;
      }
    }

    fIconWidth = width;
    fIconHeight = height;
  }

  public Object clone() {
    try {
      Object clone = super.clone();
      CenteredCompositeIcon icon = (CenteredCompositeIcon) clone;
      icon.fIcons = (Vector) fIcons.clone();
      return icon;
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Clone not supported by " + getClass().getName());
    }
  }

  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
//    TLcdLog.debug( this, "paintIcon: X["+ aX +"] Y["+ aY +"] Width["+ getIconWidth() +"] Height["+ getIconHeight() +"] length["+fIcons.size()+"]"  );

    int gw = fIconWidth;
    int gh = fIconHeight;

    for (int i = 0; i < fIcons.size(); i++) {
      ILcdIcon icon = (ILcdIcon) fIcons.get(i);
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();

      int dx = (gw - w) / 2;
      int dy = (gh - h) / 2;

      icon.paintIcon(aComponent, aGraphics, aX + dx, aY + dy);
    }
//    TLcdLog.debug( this, "paintIcon: X["+ aX +"] Y["+ aY +"] Width["+ getIconWidth() +"] Height["+ getIconHeight() +"] DONE"  );
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CenteredCompositeIcon that = (CenteredCompositeIcon) o;

    if (fIconHeight != that.fIconHeight) {
      return false;
    }
    if (fIconWidth != that.fIconWidth) {
      return false;
    }
    if (!fIcons.equals(that.fIcons)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = fIcons.hashCode() + 7 * fIconWidth + 11 * fIconHeight;
    return result;
  }
}
