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
package samples.common.gui.blacklime;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.WeakHashMap;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.plaf.UIResource;

/**
 * Painters on behalf of {@link BlackLimeLookAndFeel}.
 */
class Painters {
  public static class Fill implements Painter<JComponent> {
    private final Color fColor;
    private final int fInset;

    public Fill(Color aColor) {
      this(aColor, 1);
    }

    public Fill(Color aColor, int aInset) {
      fColor = aColor;
      fInset = aInset;
    }

    Paint getPaint(JComponent object) {
      return fColor;
    }

    @Override
    public void paint(Graphics2D g, JComponent aComponent, int width, int height) {
      g.setPaint(getPaint(aComponent));
      g.fillRect(fInset, fInset, width - fInset - 1, height - fInset - 1);
    }
  }

  /**
   * Extension of fill that respects the background color of a component in case it
   * was explicitly set. 'Explicitly set' is detected by checking if the color does not
   * implement UIResource, as is the case for say Color.BLUE or new Color(...). Colors
   * originating from the LookAndFeel do implement UIResource, and are overruled by
   * the color given to the constructor (which is the purpose of using a custom painter).
   *
   * Note that the standard Nimbus look and feel supports this partially: it works for
   * example on a JPanel and a text field, but not on a disabled text field.
   */
  public static class Background extends Fill {
    public Background(Color aColor) {
      super(aColor);
    }

    public Background(Color aColor, int aInset) {
      super(aColor, aInset);
    }

    @Override
    Paint getPaint(JComponent aComponent) {
      Paint paint = aComponent.getBackground();
      if (!(paint instanceof UIResource)) {
        return paint;
      } else {
        return super.getPaint(aComponent);
      }
    }
  }


  static class Border implements Painter<JComponent> {
    private final Color fColor;
    private final Insets fInsets;

    public Border(Color aColor) {
      this(aColor, 0);
    }

    public Border(Color aColor, int aInset) {
      this(aColor, new Insets(aInset, aInset, aInset, aInset));
    }

    public Border(Color aColor, Insets aInsets) {
      fColor = aColor;
      fInsets = aInsets;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      g.setColor(fColor);
      g.drawRect(fInsets.left,
                 fInsets.top,
                 width - fInsets.left - fInsets.right - 1,
                 height - fInsets.top - fInsets.bottom - 1);
    }
  }

  static class BottomLine implements Painter<JComponent> {
    private final Color fColor;
    private final int fThickness;
    private final Insets fInsets;

    public BottomLine(Color aColor, int aThickness, Insets aInsets) {
      fColor = aColor;
      fThickness = aThickness;
      fInsets = aInsets;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      g.setColor(fColor);
      g.fillRect(fInsets.left,
                 height - fThickness - 1 - fInsets.bottom,
                 width - fInsets.left - fInsets.right - 1,
                 fThickness);
    }
  }

  static class LeftLine implements Painter<JComponent> {
    private final Color fColor;
    private final int fThickness;
    private final Insets fInsets;

    public LeftLine(Color aColor, int aThickness, Insets aInsets) {
      fColor = aColor;
      fThickness = aThickness;
      fInsets = aInsets;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      g.setColor(fColor);
      g.fillRect(fInsets.left,
                 fInsets.top,
                 fThickness,
                 height - fInsets.top - fInsets.bottom - 1);
    }
  }

  static class CenterBox implements Painter<JComponent> {
    private final Color fColor;
    private final int fWidth;
    private final int fHeight;

    public CenterBox(Color aColor, int aWidth, int aHeight) {
      fColor = aColor;
      fWidth = aWidth;
      fHeight = aHeight;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      g.setColor(fColor);
      g.fillRect((width / 2) - (fWidth / 2),
                 (height / 2) - (fHeight / 2),
                 fWidth,
                 fHeight);
    }
  }

  static class SliderTrack implements Painter<JComponent> {
    private final Color fActiveColor;
    private final Color fColor;
    private final WeakHashMap<JSlider, Double> fPreviousFractions = new WeakHashMap<>();

    public SliderTrack(Color aActiveColor, Color aColor) {
      fActiveColor = aActiveColor;
      fColor = aColor;
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      g.setColor(fColor);
      int mid = (h - 1) / 2;
      g.drawLine(0, mid, w - 1, mid);

      JSlider s = (JSlider) c;
      BoundedRangeModel m = s.getModel();
      double fraction = (m.getValue() - m.getMinimum()) / (double) (m.getMaximum() - m.getMinimum());
      boolean invert = s.getOrientation() == JSlider.VERTICAL ^ s.getInverted();

      g.setColor(fActiveColor);
      if ( invert ) {
        g.drawLine((int) Math.round((1 - fraction) * (w - 1)), mid, w - 1, mid);
      } else {
        g.drawLine(0, mid, (int) Math.round(fraction * (w - 1)), mid);
      }

      // Nimbus optimizes the area to paint, but as we also paint the part of the track that is 'active', which Nimbus
      // doesn't do, we should ignore that. So schedule a full repaint of the slider if the thumb was changed.
      Double previousFraction = fPreviousFractions.get(s);
      if (previousFraction != null && previousFraction != fraction) {
        s.repaint();
      }
      fPreviousFractions.put(s, fraction);
    }
  }

  static class Circle implements Painter<JComponent> {
    private final Color fColor;
    private final int fSize;

    public Circle(Color aColor) {
      this(aColor, -1);

    }
    public Circle(Color aColor, int aSize) {
      fColor = aColor;
      fSize = aSize;
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int size = fSize == -1 ? Math.min(w,h) : fSize;
      g.setColor(fColor);
      g.fillOval((w-size)/2, (h-size)/2, size - 1, size - 1);
    }
  }

  static class Skewed implements Painter<JComponent> {
    private final Color fColor;

    public Skewed(Color aColor) {
      fColor = aColor;
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(fColor);
      int width = w;
      Polygon shape = new Polygon(new int[]{width/3, width, width/3*2, 0}, new int[]{0, 0, h, h}, 4);
      g.fill(shape);
    }
  }

  static class CheckBoxTick implements Painter<JComponent> {
    private final Color fColor;

    public CheckBoxTick(Color aColor) {
      fColor = aColor;
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(fColor);
      int size = 9;
      int dx = ((w-size)/2)+1;
      int dy = (h-size)/2;
      g.translate(dx,dy);
      g.drawLine(0, size/2+1, size/3, size-1);
      g.drawLine(size/3, size-1, size-1, 0);
      g.translate(-dx,-dy);
    }
  }

  static class Composite implements Painter<JComponent> {
    private final Painter<JComponent>[] fPainter;

    @SafeVarargs
    public Composite(Painter<JComponent>... aPainter) {
      fPainter = aPainter;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      for (Painter<JComponent> p : fPainter) {
        p.paint(g, object, width, height);
      }
    }
  }

  static class Triangle implements Painter<JComponent>, SwingConstants {
    private final Color fColor;
    /**
     * The direction of the arrow. One of
     * {@code SwingConstants.NORTH}, {@code SwingConstants.SOUTH},
     * {@code SwingConstants.EAST} or {@code SwingConstants.WEST}.
     */
    private int fDirection;
    private int fXOffSet;
    private int fSize;

    public Triangle(Color aColor, int aDirection) {
      this(aColor, aDirection, 0);
    }

    public Triangle(Color aColor, int aDirection, int aXOffSet) {
      this(aColor, aDirection, aXOffSet, -1);
    }

    public Triangle(Color aColor, int aDirection, int aXOffSet, int aSize) {
      fColor = aColor;
      fDirection = aDirection;
      fXOffSet = aXOffSet;
      fSize = aSize;
    }

    @Override
    public void paint(Graphics2D g, JComponent object, int width, int height) {
      // If there's no room to draw arrow, bail
      if (height < 5 || width < 5) {
        return;
      }

      // Draw the arrow
      int size = fSize == -1 ? Math.min((height - 4) / 3, (width - 4) / 3) : fSize;
      size = Math.max(size, 2);
      g.translate(fXOffSet, 0);
      paintTriangle(g, (width - size) / 2, (height - size) / 2, size, fDirection);
      g.translate(-fXOffSet, 0);
    }

    /**
     * Paints a triangle.
     *
     * @param g the {@code Graphics} to draw to
     * @param x the x coordinate
     * @param y the y coordinate
     * @param size the size of the triangle to draw
     * @param direction the fDirection in which to draw the arrow;
     *        one of {@code SwingConstants.NORTH},
     *        {@code SwingConstants.SOUTH}, {@code SwingConstants.EAST} or
     *        {@code SwingConstants.WEST}
     */
    private void paintTriangle(Graphics g, int x, int y, int size,
                               int direction) {
      Color oldColor = g.getColor();
      int mid, i, j;

      size = Math.max(size, 2);
      mid = (size / 2) - 1;

      g.translate(x, y);
      g.setColor(fColor);

      switch (direction) {
      case NORTH:
        for (i = 0; i < size; i++) {
          g.drawLine(mid - i, i, mid + i, i);
        }
        break;
      case SOUTH:
        j = 0;
        for (i = size - 1; i >= 0; i--) {
          g.drawLine(mid - i, j, mid + i, j);
          j++;
        }
        break;
      case WEST:
        for (i = 0; i < size; i++) {
          g.drawLine(i, mid - i, i, mid + i);
        }
        break;
      case EAST:
        j = 0;
        for (i = size - 1; i >= 0; i--) {
          g.drawLine(j, mid - i, j, mid + i);
          j++;
        }
        break;
      }
      g.translate(-x, -y);
      g.setColor(oldColor);
    }
  }
}
