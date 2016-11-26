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

import static com.luciad.geometry.cartesian.TLcdCartesian.containsAngle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

/**
 * UI component that allows the user to set an angle valid over a limited segment in a user friendly manner.
 */
class AngleSegmentControlUI extends ComponentUI {
  private Polygon arrow = new Polygon(new int[]{0, -4, -4, 0}, new int[]{0, -3, 3, 0}, 4);
  private Dimension fPreferredDimension = new Dimension(25, 25);
  private java.awt.event.MouseMotionListener fFOVMouseMotionListener = new MouseMotionListener();
  private java.awt.event.MouseListener fFOVMouseListener = new MouseListener();
  private ChangeListener fFOVChangeListener = new AngleChangeListener();
  private ComponentListener fFOVComponentListener = new AngleComponentListener();
  private int fCenterX;
  private int fCenterY;

  /**
   * Creates a new, empty <code>AngleSegmentControlUI</code>
   */
  public AngleSegmentControlUI() {
  }

  public Dimension getPreferredSize(JComponent c) {
    return fPreferredDimension;
  }

  public static ComponentUI createUI(JComponent aComponent) {
    return new AngleSegmentControlUI();
  }

  public void installUI(JComponent c) {
    installListeners(c);
  }

  public void uninstallUI(JComponent c) {
    uninstallListeners(c);
  }

  public void paint(Graphics graphics, JComponent c) {
    AngleControlComponent component = (AngleControlComponent) c;
    int max_angle = (int) component.getMaximumAngle();
    int min_angle = (int) component.getMinimumAngle();
    double cur_angle = component.getAngle();
    Graphics2D g = (Graphics2D) graphics;

    double max_sin = Math.sin(Math.toRadians(max_angle));
    double min_sin = Math.sin(Math.toRadians(min_angle));
    double max_cos = Math.cos(Math.toRadians(max_angle));
    double min_cos = Math.cos(Math.toRadians(min_angle));

    double min_x = Math.min(min_cos, max_cos);
    double max_x = Math.max(min_cos, max_cos);
    double min_y = Math.min(min_sin, max_sin);
    double max_y = Math.max(min_sin, max_sin);
    if (containsAngle(min_angle, max_angle - min_angle, 0)) {
      max_x = 1;
    }
    if (containsAngle(min_angle, max_angle - min_angle, 90)) {
      max_y = 1;
    }
    if (containsAngle(min_angle, max_angle - min_angle, 180)) {
      min_x = -1;
    }
    if (containsAngle(min_angle, max_angle - min_angle, 270)) {
      min_y = -1;
    }

    double total_width = max_x - min_x;
    double total_height = max_y - min_y;

    double scaleToPixels = Math.min((component.getWidth() - 1) / total_width, (component.getHeight() - 1) / total_height);
    double dxToPixels = (component.getWidth() - (total_width * scaleToPixels)) * 0.5;
    double dyToPixels = (component.getHeight() - (total_height * scaleToPixels)) * 0.5;

    // Center the hemi-circle inside the widget.
    fCenterX = (int) Math.round(-min_x * scaleToPixels + dxToPixels);
    fCenterY = component.getHeight() - 1 - (int) Math.floor(-min_y * scaleToPixels + dyToPixels);
    int radius = (int) scaleToPixels - 1;

    float alpha = c.isEnabled() ? 1f : 0.1f;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(0.8f, 0.8f, 0.8f, alpha * 0.5f));
    int diameter = radius * 2;
    g.fillArc(-radius + fCenterX, -radius + fCenterY, diameter, diameter, min_angle, max_angle - min_angle);

    g.setColor(new Color(0, 0, 0, alpha * 0.9f));

    g.drawLine(fCenterX, fCenterY, fCenterX + (int) (max_cos * radius), fCenterY - (int) (max_sin * radius));
    g.drawLine(fCenterX, fCenterY, fCenterX + (int) (min_cos * radius), fCenterY - (int) (min_sin * radius));
    g.drawArc(-radius + fCenterX, -radius + fCenterY, diameter, diameter, min_angle, max_angle - min_angle);

    if (c.isEnabled()) {
      g.setColor(Color.blue);
      double angle = Math.toRadians(cur_angle);
      g.rotate(-angle, fCenterX, fCenterY);
      g.drawLine(fCenterX, fCenterY, fCenterX + radius, fCenterY);
      g.translate(fCenterX + radius, fCenterY);
      g.fillPolygon(arrow);
      g.translate(-fCenterX - radius, -fCenterY);
      g.rotate(angle, fCenterX, fCenterY);
    }
  }

  private void installListeners(JComponent c) {
    AngleControlComponent p = (AngleControlComponent) c;
    p.addMouseListener(fFOVMouseListener);
    p.addMouseMotionListener(fFOVMouseMotionListener);
    p.addChangeListener(fFOVChangeListener);
    p.addComponentListener(fFOVComponentListener);
  }

  private void uninstallListeners(JComponent c) {
    AngleControlComponent p = (AngleControlComponent) c;
    p.removeMouseListener(fFOVMouseListener);
    p.removeMouseMotionListener(fFOVMouseMotionListener);
    p.removeChangeListener(fFOVChangeListener);
    p.removeComponentListener(fFOVComponentListener);
  }

  private class MouseListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      AngleControlComponent p = (AngleControlComponent) e.getSource();
      if (p.isEnabled()) {
        p.setAdjusting(true);
        setAngle(e);
      }
    }

    public void mouseReleased(MouseEvent e) {
      AngleControlComponent p = (AngleControlComponent) e.getSource();
      if (p.isEnabled()) {
        p.setAdjusting(false);
        setAngle(e);
      }
    }
  }

  private class MouseMotionListener extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      AngleControlComponent p = (AngleControlComponent) e.getSource();
      if (p.isEnabled()) {
        setAngle(e);
      }
    }
  }

  private void setAngle(MouseEvent e) {
    AngleControlComponent p = (AngleControlComponent) e.getSource();
    int x = e.getX();
    int y = e.getY();
    double alpha = Math.toDegrees(Math.atan2((fCenterY - y), (x - fCenterX)));
    p.setAngle(alpha);
  }

  private class AngleChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      ((JComponent) e.getSource()).repaint();
    }
  }

  private class AngleComponentListener extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      Component component = ((Component) e.getSource());
      component.repaint();
    }
  }
}
