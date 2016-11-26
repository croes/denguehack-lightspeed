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
package samples.gxy.contour.polyline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;

import com.luciad.contour.TLcdValuedContour;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYComplexStroke;
import com.luciad.view.gxy.TLcdStrokeLineStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.contour.ContourPaintUtil;

/**
 * Painter for polyline contours, each polyline is drawn with a different color depending on its
 * value. This painter uses a complex stroke style that renders the label of the polyline, it does
 * not use a separate label painter, but does not support global label decluttering.
 */
public class StrokePolylineContourPainter extends TLcdGXYPointListPainter {

  private ContourPaintUtil fContourViewUtil;
  private Color[] fLevelColors;
  private Color[] fSpecialColors;
  private String[] fLevelLabels;
  private String[] fSpecialLabels;

  /**
   * Creates a new PolylineContourPainter
   *
   * @param aLevelValues   Values for the level contours.
   * @param aLevelColors   Labels for the level contours. There must be the same amount of level
   *                       values and level labels.
   * @param aLevelLabels   Colors for the level contours. There must be the same amount of level
   *                       values and level colors.
   * @param aSpecialValues Values for the special contours.
   * @param aSpecialColors Colors for the special contours. There must be the same amount of special
   *                       values and special colors.
   * @param aSpecialLabels Labels for the special contours. There must be the same amount of special
   *                       values and special labels.
   */
  public StrokePolylineContourPainter(double[] aLevelValues,
                                      Color[] aLevelColors,
                                      String[] aLevelLabels,
                                      double[] aSpecialValues,
                                      Color[] aSpecialColors,
                                      String[] aSpecialLabels) {
    fLevelColors = aLevelColors;
    fLevelLabels = aLevelLabels;
    fSpecialColors = aSpecialColors;
    fSpecialLabels = aSpecialLabels;

    fContourViewUtil = new ContourPaintUtil(aLevelValues, aSpecialValues);

    setMode(POLYLINE);
  }

  @Override
  public void setObject(Object aObject) {
    // setObject is overridden to customize the painter style based on the height value in the contour object.
    // In addition, the base shape of the contour object needs to be set to the parent painter.
    super.setObject(((TLcdValuedContour) aObject).getShape());

    boolean special = fContourViewUtil.isSpecial(aObject);
    int index = fContourViewUtil.getIndex(aObject);

    if (index >= 0) {
      Color color = special ? fSpecialColors[index] : fLevelColors[index];
      String label = special ? fSpecialLabels[index] : fLevelLabels[index];
      setLineStyle(createLineStyle(color, label));
    }
  }

  private ILcdGXYPainterStyle createLineStyle(Color aColor, String aLabel) {
    MyLineStyle line_style = new MyLineStyle(aLabel);
    line_style.setColor(aColor);
    line_style.setSelectionColor(Color.red);
    line_style.setAntiAliasing(true);
    return line_style;
  }

  private Stroke createStroke(String aLabel, ILcdGXYView aGXYView) {
    Shape rect = new Rectangle.Double(0, 0, 1, 1);
    Font font = new Font("Arial", Font.PLAIN, 12);
    Shape text = font.createGlyphVector(new FontRenderContext(null, true, true), aLabel).getOutline(0, font.getSize() / 2f);

    // Pattern with the label. The rect shape is repeated a lot of times to have space between two successive labels
    Shape[] shapes = new Shape[400];
    double[] widths = new double[shapes.length];
    for (int i = 1; i < shapes.length; i++) {
      shapes[i] = rect;
      widths[i] = 1;
    }
    shapes[0] = text;
    widths[0] = text.getBounds2D().getWidth() + 4;

    // Pattern without the label
    BasicStroke fallback_stroke = new BasicStroke(1);
    Shape[] fallback_shapes = new Shape[]{rect};
    double[] fallback_widths = new double[]{widths[1]};

    // A complex stroke is created, with the current clipping boundaries for performance.
    // The complex stroke gets a complex stroke without the label text as fallback complex stroke
    // That fallback complex stroke gets a simple like stroke as fallback stroke, but its tolerance
    // is set so high that this basic stroke should almost never appear.
    Rectangle clip = new Rectangle(0, 0, aGXYView.getWidth(), aGXYView.getHeight());
    TLcdGXYComplexStroke fallback_complex_stroke = new TLcdGXYComplexStroke(fallback_shapes, fallback_widths, false, fallback_stroke, 1000, clip);
    double TOLERANCE = 4.0;
    return new TLcdGXYComplexStroke(shapes, widths, false, fallback_complex_stroke, TOLERANCE, clip);
  }

  private class MyLineStyle extends TLcdStrokeLineStyle {

    private String fLabel;

    public MyLineStyle(String aLabel) {
      fLabel = aLabel;
    }

    @Override
    public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aGXYContext) {
      Stroke stroke = createStroke(fLabel, aGXYContext.getGXYView());
      setStroke(stroke);
      setSelectionStroke(stroke);
      super.setupGraphics(aGraphics, aObject, aMode, aGXYContext);
    }
  }
}
