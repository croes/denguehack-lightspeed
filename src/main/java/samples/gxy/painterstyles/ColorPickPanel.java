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
package samples.gxy.painterstyles;

import com.luciad.gui.TLcdAWTUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ColorPickPanel extends JPanel {

  public static final String COLOR_PROPERTY = "color";

  public ColorPickPanel() {
    // Show a Bevel border by default.
    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.gray));

    MouseAdapter mouse_adapter = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (isEnabled() && e.getComponent().contains(e.getPoint())) {
          setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray));
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (isEnabled() && e.getComponent().contains(e.getPoint())) {
          setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.gray));
          showColorChooser(e);
        }
      }
    };
    addMouseListener(mouse_adapter);
    /**
     * Don't let Swing optimize itself by not painting the parent because we need to support transparent colors.
     * It also disables the background painting, that is taken care of by overriding paintComponent.
     */
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
  }

  public Color getColor() {
    return getBackground();
  }

  public void setColor(Color aColor) {
    Color oldColor = getBackground();
    if (!oldColor.equals(aColor)) {
      setBackground(aColor);
      firePropertyChange(COLOR_PROPERTY, oldColor, aColor);
    }
  }

  protected void showColorChooser(AWTEvent aEvent) {
    Color oldColor = getColor();
    Color newColor = JColorChooser.showDialog(TLcdAWTUtil.findParentFrame(aEvent), "Choose a color", oldColor);
    if (newColor != null) {
      setColor(newColor);
    }
  }
}
