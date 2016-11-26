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
package samples.lightspeed.demo.framework.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.luciad.gui.TLcdAWTUtil;

/**
 * A JComponent that allows you to set a color by sliding all possible hue values.
 */
public class HueSlider extends JComponent {

  //Lower is better quality, but slower.
  private static final int DENSITY = 10;

  private int fValue = 0;
  static final float SATURATION = 0.8f;
  static final float BRIGHTNESS = 1.0f;

  public HueSlider() {
    setPreferredSize(new Dimension(240, 14));
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if (isEnabled()) {
          setValue((int) ((float) e.getX() / (float) getWidth() * 255f));
          setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else {
          setCursor(Cursor.getDefaultCursor());
        }
      }

      public void mouseMoved(MouseEvent e) {
        if (isEnabled() && Math.abs(e.getX() - ((float) (fValue + 1f) / 256f * (float) getWidth())) < 3) {
          setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else {
          setCursor(Cursor.getDefaultCursor());
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }
    };
    addMouseListener(mouseAdapter);
    addMouseMotionListener(mouseAdapter);
    addMouseWheelListener(mouseAdapter);
  }

  public int getValue() {
    return fValue;
  }

  public void setValue(int aValue) {
    int oldValue = fValue;
    if (aValue > 255) {
      aValue = 255;
    }
    if (aValue < 0) {
      aValue = 0;
    }
    fValue = aValue;
    firePropertyChange("value", oldValue, aValue);
    invalidate();
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = ((Graphics2D) g);
    int width = getWidth();
    if (isEnabled()) {
      int controlPointCount = width / DENSITY;
      Color previousGradientColor = new Color(Color.HSBtoRGB(0, 1, 0.8f));
      double increment = 1 / ((double) controlPointCount);
      for (double i = increment; i < 1; i += increment) {
        int color = Color.HSBtoRGB((float) i, SATURATION, BRIGHTNESS);
        Color gradientColor = new Color(color);
        Point2D startPoint = new Point2D.Double((i - increment) * getWidth(), 0);
        Point2D endPoint = new Point2D.Double(i * getWidth(), 0);
        Paint gradientPaint = new GradientPaint(startPoint, previousGradientColor, endPoint, gradientColor, false);
        g2d.setPaint(gradientPaint);
        g2d.fill(new Rectangle2D.Double((i - increment) * getWidth(), 0, increment * getWidth(), getHeight()));
        previousGradientColor = new Color(gradientColor.getRGB());
      }
      int color = Color.HSBtoRGB(1.0f, SATURATION, BRIGHTNESS);
      Color gradientColor = new Color(color);
      Point2D startPoint = new Point2D.Double((1 - increment) * getWidth(), 0);
      Point2D endPoint = new Point2D.Double(getWidth(), 0);
      Paint gradientPaint = new GradientPaint(startPoint, previousGradientColor, endPoint, gradientColor, false);
      g2d.setPaint(gradientPaint);
      g2d.fill(new Rectangle2D.Double((1 - increment) * getWidth(), 0, (increment) * getWidth(), getHeight()));

    } else {
      Color startColor = new Color(Color.HSBtoRGB(0, 0, 0.3f));
      int color = Color.HSBtoRGB(0, 0, 0.8f);
      Color endColor = new Color(color);
      Point2D startPoint = new Point2D.Double(0, 0);
      Point2D endPoint = new Point2D.Double(getWidth(), 0);
      Paint gradientPaint = new GradientPaint(startPoint, startColor, endPoint, endColor, false);
      g2d.setPaint(gradientPaint);
      g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
    }
    g2d.setPaint(Color.gray);
    double position = (fValue + 1) / 256d * getWidth();
    g2d.fill(new Rectangle2D.Double(position - 1, 0, 3, getHeight()));
    g2d.setPaint(Color.black);
    position -= 2;
    g2d.fill(new Rectangle2D.Double(position, 0, 1, getHeight()));
    g2d.setPaint(Color.black);
    position += 4;
    g2d.fill(new Rectangle2D.Double(position, 0, 1, getHeight()));
    g2d.setPaint(Color.black);
    g2d.setStroke(new BasicStroke(1.0f));
    g2d.draw(new Rectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1));
  }


}
