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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

/**
 * Container class for predefined PatternLibrary.Pattern objects.
 * A Pattern object holds a java.awt.Shape array,
 * which can be used to construct complex strokes.
 */
class PatternLibrary {

  /**
   * Default font used to convert a string to a shape representation.
   */
  private static final Font sFONT = new Font("Arial", Font.BOLD, 12);

  /**
   * Pattern specifying a circle shape.
   */
  public final static Pattern sCIRCLE_PATTERN;

  /**
   * Pattern specifying a square shape.
   */
  public final static Pattern sSQUARE_PATTERN;

  /**
   * Pattern specifying a rectangle, stretched in the x direction.
   */
  public final static Pattern sRECTANGLE_1_PATTERN;

  /**
   * Pattern specifying a rectangle, stretched in the y direction.
   */
  public final static Pattern sRECTANGLE_2_PATTERN;

  /**
   * Pattern containing a T shape.
   */
  public final static Pattern sT_PATTERN;

  /**
   * Pattern containing a chevron shape.
   */
  public final static Pattern sCHEVRON_PATTERN;

  /**
   * Pattern containing a shape array based on the text "LUCY".
   * Each character is a separate shape.
   */
  public final static Pattern sTEXT_PATTERN_1;

  /**
   * Pattern containing a shape array based on the text "POWERED BY LUCIAD".
   * Each character is a separate shape.
   */
  public final static Pattern sTEXT_PATTERN_2;

  // Initialize all predefined patterns.
  static {
    sCIRCLE_PATTERN = new Pattern(new Shape[]{createCircleShape()}, "Circle");

    sSQUARE_PATTERN = new Pattern(new Shape[]{createSquareShape()}, "Square");

    sRECTANGLE_1_PATTERN = new Pattern(new Shape[]{createRectangle1Shape()}, "Rectangle 1");

    sRECTANGLE_2_PATTERN = new Pattern(new Shape[]{createRectangle2Shape()}, "Rectangle 2");

    sT_PATTERN = new Pattern(new Shape[]{createTShape()}, "T");

    sCHEVRON_PATTERN = new Pattern(new Shape[]{createChevronShape()}, "Chevron");

    sTEXT_PATTERN_1 = new Pattern(createTextShape("LUCY", true), "LUCY");

    sTEXT_PATTERN_2 = new Pattern(createTextShape("POWERED BY LUCIAD", true), "POWERED BY LUCIAD");
  }

  private static Shape createCircleShape() {
    Ellipse2D circle = new Ellipse2D.Float(0, 0, 1, 1);
    circle.setFrame(0, -2, 4, 4);
    return circle;
  }

  private static Shape createSquareShape() {
    Rectangle2D square = new Rectangle2D.Float(0, 0, 1, 1);
    square.setFrame(0, -2, 4, 4);
    return square;
  }

  private static Shape createRectangle1Shape() {
    Rectangle2D rectangle = new Rectangle2D.Float(0, 0, 1, 1);
    rectangle.setFrame(0, -2, 10, 4);
    return rectangle;
  }

  private static Shape createRectangle2Shape() {
    Rectangle2D rectangle = new Rectangle2D.Float(0, 0, 1, 1);
    rectangle.setFrame(0, -5, 4, 10);
    return rectangle;
  }

  private static Shape createTShape() {
    int x[] = {0, 2, 2, 3, 3, 5, 5, 0};
    int y[] = {1, 1, 6, 6, 1, 1, 0, 0};
    return new Polygon(x, y, x.length);
  }

  private static Shape createChevronShape() {
    int[] x = new int[]{16, 2, 0, 0, 20, 20, 18, 15, 13, 10, 7, 5, 2, 4, 5, 7, 10, 13, 15, 16};
    int[] y = new int[]{2, 2, 2, 0, 0, 2, 2, 7, 9, 10, 9, 7, 2, 2, 5, 7, 8, 7, 5, 2};
    return new Polygon(x, y, x.length);
  }

  /**
   * Creates a java.awt.Shape representation of a given string.
   * The splitUp parameter allows to choose whether to convert the whole
   * string to a single java.awt.Shape object, or to convert each character
   * to a separate java.awt.Shape object.
   *
   * @param aText    The string to convert to a shape representation.
   * @param aSplitUp Whether each individual character must be converted to a single java.awt.Shape object or not.
   *                 If true, the return shape array contains as many shapes as there are characters in the given string.
   *                 If false, the returned shape array contains exactly one shape, representing the whole string.
   * @return A shape representation of the given string.
   */
  private static Shape[] createTextShape(String aText, boolean aSplitUp) {
    FontRenderContext font_render_context = new FontRenderContext(null, true, true);
    StringBuffer buffer = new StringBuffer(aText);
    Shape[] shapes;
    if (aSplitUp) {
      shapes = new Shape[buffer.length()];
      for (int i = 0; i < buffer.length(); i++) {
        shapes[i] = sFONT.createGlyphVector(font_render_context, String.valueOf(buffer.charAt(i))).getOutline(0, sFONT.getSize() / 2f);
      }
      return shapes;
    } else {
      shapes = new Shape[1];
      shapes[0] = sFONT.createGlyphVector(font_render_context, buffer.toString()).getOutline(0, sFONT.getSize() / 2f);
    }

    return shapes;
  }

  /**
   * Container class for a java.awt.Shape array, representing a pattern
   * that can be used for complex strokes.
   * <p/>
   * To visualize the pattern in a GUI (e.g., in a combo box),
   * an Icon representation can be retrieved via the method {@link #getIcon()}.
   */
  public static class Pattern {

    private Shape[] fShapes;
    private String fName;
    private Icon fIcon;

    private Pattern(Shape[] aShapes, String aName) {
      fShapes = aShapes;
      fName = aName;
      fIcon = new PatternIcon(aShapes);
    }

    public Shape[] getShapes() {
      return fShapes;
    }

    public Icon getIcon() {
      return fIcon;
    }

    public String toString() {
      return fName;
    }
  }

  /**
   * Icon implementation that allows to represent a java.awt.Shape array as an icon. 
   */
  private static class PatternIcon implements Icon {

    private static final int sSHAPE_GAP = 3;
    private static final int sHEIGHT = 13;
    private Shape[] fShapes;

    public PatternIcon(Shape[] aShapes) {
      fShapes = aShapes;
    }

    public void paintIcon(Component aComponent, Graphics aGraphics,
                          int aX, int aY) {
      Graphics2D graphics_2d = (Graphics2D) aGraphics.create();
      graphics_2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
      graphics_2d.setColor(Color.black);

      int offset_y = 0;
      if (fShapes.length > 0) {
        if (fShapes[0].getBounds2D().getMinY() == 0) {
          offset_y = -(int) fShapes[0].getBounds2D().getHeight() / 2;
        } else if (fShapes[0].getBounds2D().getMaxY() == 0) {
          offset_y = (int) fShapes[0].getBounds2D().getHeight() / 2;
        }
      }

      int dx = (aComponent.getWidth() - getIconWidth()) / 2;
      int dy = aComponent.getHeight() / 2 + offset_y;

      for (int i = 0; i < fShapes.length; i++) {
        graphics_2d.translate(dx, dy);
        graphics_2d.fill(fShapes[i]);
        dx = (int) (fShapes[i].getBounds2D().getWidth() + sSHAPE_GAP);
        dy = offset_y;
      }
      graphics_2d.dispose();
    }

    public int getIconWidth() {
      double width = 0d;
      for (int i = 0; i < fShapes.length; i++) {
        width += fShapes[i].getBounds2D().getWidth();
        if (i < fShapes.length - 1) {
          width += sSHAPE_GAP;
        }
      }

      return (int) width;
    }

    public int getIconHeight() {
      return sHEIGHT;
    }
  }
}
