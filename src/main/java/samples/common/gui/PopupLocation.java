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
package samples.common.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JPopupMenu;

public abstract class PopupLocation {
  public static PopupLocation DEFAULT = new PopupLocation() {
    @Override
    public Point calculatePopupLocation(Component aAnchor, Component aContent) {
      return calculatePopupLocationImpl(aAnchor, aContent, true);
    }
  };

  public static PopupLocation RIGHT = new PopupLocation() {
    @Override
    public Point calculatePopupLocation(Component aAnchor, Component aContent) {
      return calculatePopupLocationImpl(aAnchor, aContent, false);
    }
  };

  /**
   * Calculates a suitable location for the given popup component, relative to the given anchor component.
   *
   * @param aAnchor The owner or initiator component of this popup, typically a button or a text field that trigger the
   *                popup to appear.
   * @param aContent The content that needs to be displayed. Can for example be used to retrieve the preferred size.
   * @return The location for the popup, in the coordinate space of the owner. This is similar to how
   * {@link JPopupMenu#show(Component, int, int)} defines it.
   */
  public abstract Point calculatePopupLocation(Component aAnchor, Component aContent);

  private static Point calculatePopupLocationImpl(Component aOrigin, Component aPopupContent, boolean aPreferLeft) {
    int popupHeight = (int) aPopupContent.getPreferredSize().getHeight();
    int popupWidth = (int) aPopupContent.getPreferredSize().getWidth();
    int originX = (int) aOrigin.getLocationOnScreen().getX();
    int originY = (int) aOrigin.getLocationOnScreen().getY();
    int popupX;
    int popupY = aOrigin.getHeight();

    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(aOrigin.getGraphicsConfiguration());
    Rectangle screen = retrieveScreenBounds(aOrigin);

    int left = left(screen, insets, popupWidth, originX);
    int right = right(screen, insets, popupWidth, originX, aOrigin.getWidth());
    int edge = screenEdge(screen, insets, popupWidth, originX);

    if ( aPreferLeft ) {
      popupX = left != -1 ? left : right != -1 ? right : edge;
    }
    else {
      popupX = right != -1 ? right : left != -1 ? left : edge;
    }

    int popupLowerBorder = originY + popupHeight + popupY;
    int screenLowerBorder = screen.y + screen.height - insets.bottom;
    if (popupLowerBorder > screenLowerBorder) {
      int deltaY = originY - screen.y;
      if (deltaY - popupHeight > insets.top) {
        popupY = -popupHeight;
      } else {
        popupY = -deltaY;
      }
    }

    return new Point(popupX, popupY);
  }

  private static int left(Rectangle aScreen, Insets aInsets, int aPopupWidth, int aOriginX ) {
    int popupRightBorder = aOriginX + aPopupWidth;
    int screenRightBorder = aScreen.x + aScreen.width - aInsets.right;

    if (popupRightBorder > screenRightBorder) {
      return -1;
    }
    else {
      return 0;
    }
  }

  private static int right(Rectangle aScreen, Insets aInsets, int aPopupWidth, int aOriginX, int aOriginWidth ) {
    //try to right align
    int popupX = aOriginWidth - aPopupWidth;
    int popupLeftBorder = aOriginX + popupX;
    int screenLeftBorder = aScreen.x + aInsets.left;
    if (popupLeftBorder < screenLeftBorder) {
      //right align does not fit
      return -1;
    }
    else {
      return popupX;
    }
  }

  private static int screenEdge(Rectangle aScreen, Insets aInsets, int aPopupWidth, int aOriginX ) {
    int popupRightBorder = aOriginX + aPopupWidth;
    int screenRightBorder = aScreen.x + aScreen.width - aInsets.right;
    return screenRightBorder - popupRightBorder;
  }

  private static Rectangle retrieveScreenBounds(Component source) {
    Rectangle screen = source.getGraphicsConfiguration().getBounds();
    if (screen.contains(source.getLocationOnScreen())) {
      return screen;
    } else {
      // In Linux multi-display configurations, the component does not always report the right screen.
      // Try to find it by manually looping over the screens.
      GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      for (GraphicsDevice device : devices) {
        screen = device.getDefaultConfiguration().getBounds();
        if (screen.contains(source.getLocationOnScreen())) {
          return screen;
        }
      }
      // Last fall back
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      return new Rectangle(0, 0, screenSize.width, screenSize.height);
    }
  }
}
