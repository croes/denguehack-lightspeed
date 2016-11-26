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
package samples.lightspeed.demo.application.data.sassc.icons;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.luciad.gui.ILcdIcon;

public abstract class SASSCIcon implements ILcdIcon {

  protected static final Color NULL_COLOR = new Color(1, 1, 1, 0);

  /**
   * A constant for an empty diamond.
   */
  public static final String DIAMOND = "diamond";
  /**
   * A constant for an empty diamond with a cross inside.
   */
  public static final String DIAMOND_CROSS = "diamond_cross";
  /**
   * A constant for an empty diamond with a vertical line inside.
   */
  public static final String DIAMOND_V_LINE = "diamond_v";
  /**
   * A constant for an empty diamond with a horizontal line inside.
   */
  public static final String DIAMOND_H_LINE = "diamond_h";

  public static final String SQUARE_V_LINE_EMPTY = "square_v_empty";
  public static final String SQUARE_V_LINE_LEFT = "square_v_left";
  public static final String SQUARE_V_LINE_RIGHT = "square_v_right";
  public static final String SQUARE_H_LINE_EMPTY = "square_h_empty";
  public static final String SQUARE_H_LINE_UP = "square_h_up";
  public static final String SQUARE_H_LINE_DOWN = "square_h_down";
  public static final String SQUARE_CROSS = "square_cross";
  public static final String SQUARE_DIAG_U_EMPTY = "square_diag_up_e";
  public static final String SQUARE_DIAG_U_L = "square_diag_up_l";
  public static final String SQUARE_DIAG_U_R = "square_diag_up_r";
  public static final String SQUARE_DIAG_D_EMPTY = "square_diag_down_e";
  public static final String SQUARE_DIAG_D_L = "square_diag_down_l";
  public static final String SQUARE_DIAG_D_R = "square_diag_down_r";
  public static final String SQUARE_DIAG_CROSS = "square_diag_cross";

  public static final String CIRCLE_H_LINE_EMPTY = "circle_h_empty";
  public static final String CIRCLE_H_LINE_TOP = "circle_h_top";
  public static final String CIRCLE_H_LINE_BOTTOM = "circle_h_bottom";
  public static final String CIRCLE_V_LINE_EMPTY = "circle_v_empty";
  public static final String CIRCLE_V_LINE_LEFT = "circle_v_left";
  public static final String CIRCLE_V_LINE_RIGHT = "circle_v_right";
  public static final String CIRCLE_CROSS = "circle_cross";

  public static final String HOURS_GLASS_H_FILL = "hours_glass_h_fill";
  public static final String HOURS_GLASS_H_EMPTY = "hours_glass_h_empty";
  public static final String HOURS_GLASS_H_LEFT = "hours_glass_h_left";
  public static final String HOURS_GLASS_H_RIGHT = "hours_glass_h_right";
  public static final String HOURS_GLASS_V_FILL = "hours_glass_v_fill";
  public static final String HOURS_GLASS_V_EMPTY = "hours_glass_v_empty";
  public static final String HOURS_GLASS_V_TOP = "hours_glass_v_top";
  public static final String HOURS_GLASS_V_BOTTOM = "hours_glass_v_bottom";

  public static final String TRIANGLE_FULL = "triangle_full";
  public static final String TRIANGLE_EMPTY = "triangle_emtpy";
  public static final String TRIANGLE_HALF_TOP = "triangle_half_top";
  public static final String TRIANGLE_HALF_BOTTOM = "triangle_half_bottom";
  public static final String TRIANGLE_HALF_LEFT = "triangle_half_left";
  public static final String TRIANGLE_HALF_RIGHT = "triangle_half_right";

  public static final String TRIANGLE_INVERTED_FULL = "triangle_inverted_fill";
  public static final String TRIANGLE_INVERTED_EMPTY = "triangle_inverted_empty";
  public static final String TRIANGLE_INVERTED_HALF_TOP = "triangle_inverted_half_top";
  public static final String TRIANGLE_INVERTED_HALF_BOTTOM = "triangle_inverted_half_bottom";
  public static final String TRIANGLE_INVERTED_HALF_LEFT = "triangle_inverted_half_left";
  public static final String TRIANGLE_INVERTED_HALF_RIGHT = "triangle_inverted_half_right";

  protected int size;
  protected int size2;
  protected Color fillColor;
  protected Color lineColor;

  protected SASSCIcon(int aSize, Color aFillColor, Color aLineColor) {
    size = aSize - 1;
    size2 = aSize;
    fillColor = aFillColor;
    lineColor = aLineColor;
  }

  protected int getSize() {
    return size;
  }

  public static ILcdIcon createIcon(String aName, int aSize, Color aColor) {
    final ILcdIcon delegate = createIcon2(aName, aSize, aColor);
    if (delegate == null) {
      throw new IllegalArgumentException("Unknown icon: " + aName);
    }
    return new ILcdIcon() {
      @Override
      public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y) {
        Graphics2D graphics2D = (Graphics2D) aGraphics;
        graphics2D.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 2f));
        delegate.paintIcon(aComponent, graphics2D, x, y);
      }

      @Override
      public int getIconWidth() {
        return delegate.getIconWidth();
      }

      @Override
      public int getIconHeight() {
        return delegate.getIconHeight();
      }

      @Override
      public Object clone() {
        throw new IllegalStateException();
      }
    };
  }

  public static SASSCIcon createIcon2(String aIconShape, int aSize, Color aColor) {
    if (aIconShape.equals(DIAMOND)) {
      return new DiamondIcon((int) (aSize * 1.4), aColor);
    } else if (aIconShape.equals(DIAMOND_CROSS)) {
      return new DiamondCrossIcon((int) (aSize * 1.4), aColor);
    } else if (aIconShape.equals(DIAMOND_V_LINE)) {
      return new DiamondVerticalLineIcon((int) (aSize * 1.4), aColor);
    } else if (aIconShape.equals(DIAMOND_H_LINE)) {
      return new DiamondHorizontalLineIcon((int) (aSize * 1.4), aColor);
    } else if (aIconShape.equals(SQUARE_H_LINE_EMPTY)) {
      return new SquareHorizontalLineIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_H_LINE_UP)) {
      return new SquareHorizontalLineIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_H_LINE_DOWN)) {
      return new SquareHorizontalLineIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(SQUARE_V_LINE_EMPTY)) {
      return new SquareVerticalLineIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_V_LINE_LEFT)) {
      return new SquareVerticalLineIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_V_LINE_RIGHT)) {
      return new SquareVerticalLineIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(SQUARE_CROSS)) {
      return new SquareCrossIcon(aSize, aColor);
    } else if (aIconShape.equals(SQUARE_DIAG_U_EMPTY)) {
      return new SquareDiagUpIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_DIAG_U_L)) {
      return new SquareDiagUpIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_DIAG_U_R)) {
      return new SquareDiagUpIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(SQUARE_DIAG_D_EMPTY)) {
      return new SquareDiagDownIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_DIAG_D_L)) {
      return new SquareDiagDownIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(SQUARE_DIAG_D_R)) {
      return new SquareDiagDownIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(SQUARE_DIAG_CROSS)) {
      return new SquareDiagCrossIcon(aSize, aColor);
    } else if (aIconShape.equals(CIRCLE_H_LINE_EMPTY)) {
      return new CircleHorizontalLineIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(CIRCLE_H_LINE_TOP)) {
      return new CircleHorizontalLineIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(CIRCLE_H_LINE_BOTTOM)) {
      return new CircleHorizontalLineIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(CIRCLE_V_LINE_EMPTY)) {
      return new CircleVerticalLineIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(CIRCLE_V_LINE_LEFT)) {
      return new CircleVerticalLineIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(CIRCLE_V_LINE_RIGHT)) {
      return new CircleVerticalLineIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(CIRCLE_CROSS)) {
      return new CircleCrossIcon(aSize, aColor);
    } else if (aIconShape.equals(HOURS_GLASS_V_FILL)) {
      return new HoursGlassVerticalIcon(aSize, aColor, aColor, aColor);
    } else if (aIconShape.equals(HOURS_GLASS_V_EMPTY)) {
      return new HoursGlassVerticalIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(HOURS_GLASS_V_TOP)) {
      return new HoursGlassVerticalIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(HOURS_GLASS_V_BOTTOM)) {
      return new HoursGlassVerticalIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(HOURS_GLASS_H_FILL)) {
      return new HoursGlassHorizontalIcon(aSize, aColor, aColor, aColor);
    } else if (aIconShape.equals(HOURS_GLASS_H_EMPTY)) {
      return new HoursGlassHorizontalIcon(aSize, aColor, NULL_COLOR, NULL_COLOR);
    } else if (aIconShape.equals(HOURS_GLASS_H_LEFT)) {
      return new HoursGlassHorizontalIcon(aSize, aColor, aColor, NULL_COLOR);
    } else if (aIconShape.equals(HOURS_GLASS_H_RIGHT)) {
      return new HoursGlassHorizontalIcon(aSize, aColor, NULL_COLOR, aColor);
    } else if (aIconShape.equals(TRIANGLE_EMPTY)) {
      return new TriangleIcon(aSize, aColor, NULL_COLOR);
    } else if (aIconShape.equals(TRIANGLE_INVERTED_EMPTY)) {
      return new InvertedTriangleIcon(aSize, aColor, NULL_COLOR);
    }
    return null;
  }

  public static void paint(String aIconShape, int aSize, Graphics aGraphics, Color aColor, int x, int y) {
    if (aIconShape.equals(DIAMOND)) {
      DiamondIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(DIAMOND_CROSS)) {
      DiamondCrossIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(DIAMOND_V_LINE)) {
      DiamondVerticalLineIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(DIAMOND_H_LINE)) {
      DiamondHorizontalLineIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_H_LINE_EMPTY)) {
      SquareHorizontalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_H_LINE_UP)) {
      SquareHorizontalLineIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_H_LINE_DOWN)) {
      SquareHorizontalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_V_LINE_EMPTY)) {
      SquareVerticalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_V_LINE_LEFT)) {
      SquareVerticalLineIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_V_LINE_RIGHT)) {
      SquareVerticalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_CROSS)) {
      SquareCrossIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_U_EMPTY)) {
      SquareDiagUpIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_U_L)) {
      SquareDiagUpIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_U_R)) {
      SquareDiagUpIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_D_EMPTY)) {
      SquareDiagDownIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_D_L)) {
      SquareDiagDownIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_D_R)) {
      SquareDiagDownIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(SQUARE_DIAG_CROSS)) {
      SquareDiagCrossIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(CIRCLE_H_LINE_EMPTY)) {
      CircleHorizontalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(CIRCLE_H_LINE_TOP)) {
      CircleHorizontalLineIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(CIRCLE_H_LINE_BOTTOM)) {
      CircleHorizontalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(CIRCLE_V_LINE_EMPTY)) {
      CircleVerticalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(CIRCLE_V_LINE_LEFT)) {
      CircleVerticalLineIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(CIRCLE_V_LINE_RIGHT)) {
      CircleVerticalLineIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(CIRCLE_CROSS)) {
      CircleCrossIcon.paint(aSize, aGraphics, aColor, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_V_FILL)) {
      HoursGlassVerticalIcon.paint(aSize, aGraphics, aColor, aColor, aColor, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_V_EMPTY)) {
      HoursGlassVerticalIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_V_TOP)) {
      HoursGlassVerticalIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_V_BOTTOM)) {
      HoursGlassVerticalIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_H_FILL)) {
      HoursGlassHorizontalIcon.paint(aSize, aGraphics, aColor, aColor, aColor, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_H_EMPTY)) {
      HoursGlassHorizontalIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, NULL_COLOR, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_H_LEFT)) {
      HoursGlassHorizontalIcon.paint(aSize, aGraphics, aColor, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(HOURS_GLASS_H_RIGHT)) {
      HoursGlassHorizontalIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, aColor, x, y);
    } else if (aIconShape.equals(TRIANGLE_EMPTY)) {
      TriangleIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, x, y);
    } else if (aIconShape.equals(TRIANGLE_INVERTED_EMPTY)) {
      InvertedTriangleIcon.paint(aSize, aGraphics, aColor, NULL_COLOR, x, y);
    }
  }

  @Override
  public int getIconHeight() {
    return size2;
  }

  @Override
  public int getIconWidth() {
    return size2;
  }

  public abstract Object clone();
}
