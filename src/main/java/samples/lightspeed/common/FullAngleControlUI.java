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
 * UI component that allows the user to set a full angle in a user friendly manner.
 */
class FullAngleControlUI extends ComponentUI {
  private Polygon arrow = new Polygon(new int[]{0, -4, -4, 0}, new int[]{0, -3, 3, 0}, 4);
  private Dimension fPreferredDimension = new Dimension(80, 80);
  private java.awt.event.MouseMotionListener fMouseMotionListener = new MouseMotionListener();
  private java.awt.event.MouseListener fMouseListener = new MouseListener();
  private ChangeListener fChangeListener = new AngleChangeListener();
  private ComponentListener fComponentListener = new AngleComponentListener();
  private int fCenterX = 40;
  private int fCenterY = 40;

  /**
   * Creates a new, empty <code>FullAngleControl</code>
   */
  public FullAngleControlUI() {
  }

  public Dimension getPreferredSize(JComponent c) {
    return fPreferredDimension;
  }

  public static ComponentUI createUI(JComponent aComponent) {
    return new FullAngleControlUI();
  }

  public void installUI(JComponent c) {
    installListeners(c);
    recalculateGeometry(c.getWidth(), c.getHeight());
  }

  public void uninstallUI(JComponent c) {
    uninstallListeners(c);
  }

  public void paint(Graphics graphics, JComponent c) {
    AngleControlComponent component = (AngleControlComponent) c;
    fCenterX = (component.getWidth() - 1) / 2;
    fCenterY = (component.getHeight() - 1) / 2;

    int cur_angle = (int) (90.0 - component.getAngle());
    int x, y;
    double angle;
    Graphics2D g = (Graphics2D) graphics;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    x = fCenterX * 2;
    y = fCenterY * 2;

    float alpha = c.isEnabled() ? 1f : 0.1f;

    g.setColor(new Color(0.8f, 0.8f, 0.8f, alpha * 0.5f));
    g.fillArc(0, 0, x, y, 0, 360);
    g.setColor(new Color(0.4f, 0.4f, 0.4f, alpha * 0.5f));
    g.drawArc(1, 0, x, y, 191, -112);
    g.drawArc(0, 1, x, y, 191, -112);

    g.setColor(new Color(0, 0, 0, alpha * 0.9f));
    g.drawArc(0, 0, x, y, 0, 360);

    if (c.isEnabled()) {
      g.setColor(Color.blue);
      angle = Math.toRadians(cur_angle);
      g.rotate(-angle, fCenterX, fCenterY);
      x = fCenterX + (int) (fCenterX * Math.cos(angle));
      y = fCenterY - (int) (fCenterY * Math.sin(angle));
      g.drawLine(fCenterX, fCenterY, 2 * fCenterX, fCenterY);
      g.translate(2 * fCenterX, fCenterY);
      g.fillPolygon(arrow);
      g.translate(-2 * fCenterX, -fCenterY);
      g.rotate(angle, fCenterX, fCenterY);
    }
  }

  private void installListeners(JComponent c) {
    AngleControlComponent p = (AngleControlComponent) c;
    p.addMouseListener(fMouseListener);
    p.addMouseMotionListener(fMouseMotionListener);
    p.addChangeListener(fChangeListener);
    p.addComponentListener(fComponentListener);
  }

  private void uninstallListeners(JComponent c) {
    AngleControlComponent p = (AngleControlComponent) c;
    p.removeMouseListener(fMouseListener);
    p.removeMouseMotionListener(fMouseMotionListener);
    p.removeChangeListener(fChangeListener);
    p.removeComponentListener(fComponentListener);
  }

  private void recalculateGeometry(int aWidth, int aHeight) {
    int size = Math.min(aWidth, aHeight);
    fCenterX = ((int) size - 1) / 2;
    fCenterY = ((int) size - 1) / 2;
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
    double alpha = 90.0 - Math.toDegrees(Math.atan2((double) (fCenterY - y), (double) (x - fCenterX)));
    if (alpha < 360) {
      alpha += 360;
    }
    if (alpha > 360) {
      alpha -= 360;
    }
    p.setAngle((int) alpha);
  }

  private class AngleChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      ((JComponent) e.getSource()).repaint();
    }
  }

  private class AngleComponentListener extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      Component component = ((Component) e.getSource());
      recalculateGeometry(component.getWidth(), component.getHeight());
      component.repaint();
    }
  }
}
