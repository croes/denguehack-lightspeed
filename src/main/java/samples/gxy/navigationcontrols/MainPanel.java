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
package samples.gxy.navigationcontrols;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.navigationcontrols.ALcdCompassNavigationControl;
import com.luciad.gui.swing.navigationcontrols.TLcdMouseOverGroup;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.swing.navigationcontrols.TLcdGXYCompassNavigationControl;
import com.luciad.view.gxy.swing.navigationcontrols.TLcdGXYZoomNavigationControl;

import samples.common.MouseCursorFactory;
import samples.gxy.common.GXYSample;

/**
 * This sample demonstrates how to customize and add navigation controls to a view.
 */
public class MainPanel extends GXYSample {

  @Override
  protected void createGUI() {
    super.createGUI();
    // The default controls are created in the super class.
    // In this sample, we'll add some customized controls.
    addAdvancedControls(getOverlayPanel());
  }

  protected void addAdvancedControls(Container aOverlayPanel) {
    // Create the two components yourself, this allows more control over where to place each component.
    String imagePath = "images/gui/navigationcontrols/small/";
    Component comp = createDraggableCompass(getView(), imagePath, false);
    if (comp != null) {
      aOverlayPanel.add(comp, TLcdOverlayLayout.Location.NO_LAYOUT);
    }
    comp = createZoom(getView(), imagePath, true);
    if (comp != null) {
      aOverlayPanel.add(comp, TLcdOverlayLayout.Location.WEST);
    }
  }

  public Component createDraggableCompass(ILcdGXYView aGXYView, String aImagePath,
                                          boolean aAlwaysActive) {
    final Component compass;
    try {
      compass = new TLcdGXYCompassNavigationControl(
          aImagePath + ALcdCompassNavigationControl.COMPASSPAN_COMPONENT_DIR, aGXYView);
    } catch (IOException ignored) {
      return null;
    }
    TLcdMouseOverGroup mOG = new TLcdMouseOverGroup(aAlwaysActive);
    mOG.add(compass);
    compass.setCursor(Cursor.getDefaultCursor());

    MouseInputAdapter mIA = new MouseInputAdapter() {

      int fPrevX;
      int fPrevY;
      int fOrigX;
      int fOrigY;

      @Override
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          compass.setCursor(MouseCursorFactory.getMoveCursor());
          fOrigX = compass.getX();
          fOrigY = compass.getY();
          fPrevX = e.getX() + fOrigX;
          fPrevY = e.getY() + fOrigY;
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int x = compass.getX() + e.getX() - fPrevX;
          int y = compass.getY() + e.getY() - fPrevY;
          compass.setLocation(fOrigX + x, fOrigY + y);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          compass.setCursor(Cursor.getDefaultCursor());
        }
      }
    };
    compass.addMouseListener(mIA);
    compass.addMouseMotionListener(mIA);

    Dimension dim = compass.getPreferredSize();
    compass.setBounds(20, 20, dim.width, dim.height);
    return compass;
  }

  public Component createZoom(ILcdGXYView aGXYView, String aImagePath,
                              boolean aAlwaysActive) {
    Component zoom;
    try {
      zoom = new TLcdGXYZoomNavigationControl(
          aImagePath + TLcdGXYZoomNavigationControl.ZOOM_COMPONENT_DIR, aGXYView);
    } catch (IOException ignored) {
      zoom = null;
    }
    TLcdMouseOverGroup mOG = new TLcdMouseOverGroup(aAlwaysActive);
    mOG.add(zoom);
    return zoom;
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "On Map Navigation Controls");
  }

}
