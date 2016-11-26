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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A color chooser
 */
public class ColorChooser extends JPanel {

  private final MyColorLabel fColorLabel;
  private boolean fEnabled = true;

  public ColorChooser(Color aColor, int aWidth, int aHeight) {
    fColorLabel = new MyColorLabel(aColor, aWidth, aHeight);
    fColorLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (fEnabled) {
          Color color = JColorChooser.showDialog(ColorChooser.this, "Choose color...", fColorLabel.getColor());
          if (color != null) {
            Color oldColor = fColorLabel.getColor();
            fColorLabel.setColor(color);
            firePropertyChange("color", oldColor, color);
          }
        }
      }
    });
    add(fColorLabel);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && fEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    fEnabled = enabled;
  }

  public void setColor(Color aColor) {
    fColorLabel.setColor(aColor);
  }

  public Color getColor() {
    return fColorLabel.getColor();
  }

  /**
   * Widget that enables the user to change a color.
   */
  private class MyColorLabel extends JLabel {

    private Color fColor;
    private final int fWidth;
    private final int fHeight;

    private MyColorLabel(Color aColor, int aWidth, int aHeight) {
      fColor = aColor;
      fWidth = aWidth;
      fHeight = aHeight;
    }

    private void setColor(Color aColor) {
      fColor = aColor;
      repaint();
    }

    private Color getColor() {
      return fColor != null ? fColor : Color.white;
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(fWidth, fHeight);
    }

    @Override
    public void paint(Graphics g) {
      if (fColor != null && fEnabled) {
        g.setColor(fColor);
        g.fillRect(0, 0, getWidth(), getHeight());
      } else {
        g.setColor(new Color(240, 240, 240, 255));
        g.fillRect(0, 0, getWidth(), getHeight());
      }
      g.setColor(fEnabled ? Color.black : Color.gray);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

  }
}
