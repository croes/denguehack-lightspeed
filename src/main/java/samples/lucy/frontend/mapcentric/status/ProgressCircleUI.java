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
package samples.lucy.frontend.mapcentric.status;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicProgressBarUI;

import samples.common.UIColors;

/**
 * Circular UI for a {@code JProgressBar}
 */
final class ProgressCircleUI extends BasicProgressBarUI {
  private static final int SIZE = 30; //px

  @Override
  protected Dimension getPreferredInnerHorizontal() {
    return new Dimension(SIZE, SIZE);
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    int v = Math.max(d.width, d.height);
    d.setSize(v, v);
    return d;
  }

  @Override
  protected void paintIndeterminate(Graphics g, JComponent c) {
    int animationIndex = getAnimationIndex();
    int frameCount = getFrameCount();
    double progress = (double) animationIndex / (double) frameCount;
    paintForProgress(g, progress, true);
  }

  @Override
  protected void paintDeterminate(Graphics g, JComponent c) {
    paintForProgress(g, progressBar.getPercentComplete(), false);
  }

  /**
   * Paints the icon.
   * @param g The graphics
   * @param aProgress Value between 0.0 and 1.0
   */
  private void paintForProgress(Graphics g, double aProgress, boolean indeterminate) {
    Insets b = progressBar.getInsets(); // area for border
    int barRectWidth = progressBar.getWidth() - b.right - b.left;
    int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
    if (barRectWidth <= 0 || barRectHeight <= 0) {
      return;
    }

    g.translate(b.left, b.top);
    paintForProgressImpl((Graphics2D) g, aProgress, indeterminate, barRectWidth, barRectHeight);
    g.translate(-b.left, -b.top);

    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      g.setColor(progressBar.getForeground());
      paintString(g, b.left, b.top, barRectWidth, barRectHeight, 0, b);
    }
  }

  // Paints the icon at 0,0 and given width and height
  private void paintForProgressImpl(Graphics2D g, double aProgress, boolean indeterminate, int w, int h) {
    // Calculate the sizes
    double degree = 360 * aProgress;
    int size = Math.min(w, h);
    int x = (w - size) / 2;
    int y = (h - size) / 2;

    // Make room for wider line
    int arcX = x + 1;
    int arcY = y + 1;
    int arcSize = size - 2;

    // Use a larger clip, to avoid artifacts with anti-aliasing.
    // It's actual size doesn't matter, as long as it remains square.
    int clipX = x - 5;
    int clipY = y - 5;
    int clipSize = size + 10;

    // Fetch colors
    Color fgColor = UIColors.fgAccent();
    Color bgColor = UIManager.getColor("ProgressBar.circleUIBackgroundColor");
    if (bgColor == null) {
      bgColor = UIColors.mid(UIColors.bg(), fgColor, 0.25);
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)); //use butt to avoid overdrawing ourselves

    // Draw full circle background
    g.setColor(bgColor);
    g.drawOval(arcX, arcY, arcSize - 1, arcSize - 1);

    // Using a clip&drawOval instead of a drawing an arc to avoid artifacts. The arc rendering results in different
    // pixels being colored depending on the angles, related to anti-aliasing.
    if (indeterminate) {
      progressBar.repaint(); // clear the graphics clip, provided by Swing, by scheduling a full repaint for this component
      g.setClip(new Arc2D.Double(clipX, clipY, clipSize - 1, clipSize - 1, -(int) Math.round(degree), 120, Arc2D.PIE));
    } else {
      g.setClip(new Arc2D.Double(clipX, clipY, clipSize - 1, clipSize - 1, 90, -(int) Math.round(degree), Arc2D.PIE));
    }
    g.setColor(fgColor);
    g.drawOval(arcX, arcY, arcSize - 1, arcSize - 1);
  }
}
