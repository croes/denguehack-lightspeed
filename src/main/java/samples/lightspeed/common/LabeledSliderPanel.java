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
package samples.lightspeed.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.JSlider;

import com.luciad.gui.TLcdAWTUtil;

/**
 *
 */
public class LabeledSliderPanel extends JPanel {

  private static final Color LABEL_COLOR = Color.white;
  private static final Color LABEL_HALO_COLOR = new Color(40, 40, 40);

  private JSlider fSlider;
  private String fDisplayName;

  public LabeledSliderPanel(String aDisplayName, JSlider aSlider) {
    fSlider = aSlider;
    fDisplayName = aDisplayName;
    initGUI();
  }

  /**
   * Returns the slider of this labeled slider panel.
   * @return the slider of this labeled slider panel.
   */
  public JSlider getSlider() {
    return fSlider;
  }

  /**
   * Returns the display name of this labeled slider panel.
   * @return the display name of this labeled slider panel.
   */
  public String getDisplayName() {
    return fDisplayName;
  }

  /**
   * Sets the display name of this labeled slider panel to the given name
   * @param aDisplayName the new display name for this labeled slider panel
   */
  public void setDisplayName(String aDisplayName) {
    fDisplayName = aDisplayName;
  }

  protected void initGUI() {
    setLayout(new BorderLayout());
    JPanel labelPanel = new LabelPanel();
    add(labelPanel, BorderLayout.NORTH);
    add(fSlider, BorderLayout.CENTER);
    fSlider.setEnabled(true);
    setVisible(true);
    setOpaque(false);
    fSlider.setOpaque(false);
    setFocusable(false);
    fSlider.setFocusable(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
  }

  private class LabelPanel extends JPanel {

    @Override
    public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();
      size.height = Math.max(size.height, 20);
      size.width = Math.min(size.width, 100);
      return size;
    }

    public LabelPanel() {

    }

    @Override
    protected void paintComponent(Graphics g) {
      paintLabelCenteredInBox(g, fDisplayName, LABEL_COLOR, LABEL_HALO_COLOR,
                              0, 0);
    }

    private void paintLabelCenteredInBox(Graphics aGraphics, String aLabelText, Color aLabelColor,
                                         Color aHaloColor, int aX, int aY) {
      Graphics2D g2d = (Graphics2D) aGraphics;
      FontMetrics fontMetrics = aGraphics.getFontMetrics();
      int width = fontMetrics.stringWidth(aLabelText) + 2;
      int height = fontMetrics.getHeight();
      Object old_anti_aliasing = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
      if (old_anti_aliasing == null) {
        //This can apparently happen sometimes.
        old_anti_aliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
      }
      try {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        aGraphics.setColor(aHaloColor);
        for (int deltax = -1; deltax <= 1; deltax++) {
          for (int deltay = -1; deltay <= 1; deltay++) {
            if (deltax != 0 || deltay != 0) {
              TLcdAWTUtil.drawString(aLabelText, TLcdAWTUtil.CENTER, aGraphics, aX + deltax, aY + deltay, width, height);
            }
          }
        }
        aGraphics.setColor(aLabelColor);
        TLcdAWTUtil.drawString(aLabelText, TLcdAWTUtil.CENTER, aGraphics, aX, aY, width, height);
      } finally {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, old_anti_aliasing);
      }
    }
  }
}
