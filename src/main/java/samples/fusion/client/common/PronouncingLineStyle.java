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
package samples.fusion.client.common;

import static java.awt.BasicStroke.*;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.pow;

import static com.luciad.util.TLcdDistanceUnit.METRE_UNIT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.luciad.util.ILcdCloneable;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterStyle;

/**
 * A line style that pronounces the width of a line with the zoom level.
 * It does almost the same as {@link com.luciad.view.map.TLcdMapG2DLineStyle}, but it adds a width-dependent exaggeration as a secret ingredient.
 * The result is a much better visual result visualizing vector data.
 * <p/>
 * The problem we're solving is that lines of different width are all rounded to a width of 1 pixel, unless you've really zoomed in a lot.
 * We want a pleasing visual difference between lines of different width, even when zoomed out.
 * Use the (debug) client sample against the Tiger data set to test it out.
 */
public class PronouncingLineStyle implements ILcdGXYPainterStyle, ILcdCloneable {

  private Color fColor = LIGHT_GRAY;

  private Color fSelectedColor = WHITE;

  private float fLineWidth = 10;

  private float fSelectedLineWidth = 10;

  private TLcdDistanceUnit fUnit = METRE_UNIT;

  private float fToMeterFactor = (float) METRE_UNIT.getToMetreFactor();

  private boolean fAntiAliasing = false;

  private transient double fLastScale; // scale

  private transient float[] fPattern = null;

  private transient Stroke fLastStroke;

  private transient Stroke fLastSelectedStroke;

  /**
   * The secret ingredient.
   */
  private double fExaggeration = exaggerationFromWidth(fLineWidth);

  public Color getColor() {
    return fColor;
  }

  public void setColor(Color aColor) {
    fColor = aColor;
  }

  public Color getSelectedColor() {
    return fSelectedColor;
  }

  public void setSelectedColor(Color aColor) {
    fSelectedColor = aColor;
  }

  public float getLineWidth() {
    return fLineWidth;
  }

  public void setLineWidth(float aWidth) {
    fLineWidth = aWidth;
    fExaggeration = exaggerationFromWidth(aWidth);
  }

  public double getSelectedLineWidth() {
    return fSelectedLineWidth;
  }

  public void setSelectedLineWidth(float aWidth) {
    fSelectedLineWidth = aWidth;
  }

  public boolean isAntiAliasing() {
    return fAntiAliasing;
  }

  public void setAntiAliasing(boolean aOn) {
    fAntiAliasing = aOn;
  }

  public TLcdDistanceUnit getLineWidthUnit() {
    return fUnit;
  }

  public void setLineWidthUnit(TLcdDistanceUnit aUnit) {
    fUnit = aUnit;
    fToMeterFactor = (float) fUnit.getToMetreFactor();
  }

  public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aContext) {
    if (fLastScale != aContext.getGXYView().getScale()) {
      updateLastScale(aMode, aContext);
    }
    Graphics2D graphics = (Graphics2D) aGraphics;
    if ((aMode & ILcdGXYPainter.SELECTED) != 0) {
      aGraphics.setColor(getSelectedColor());
      graphics.setStroke(fLastSelectedStroke);
    } else {
      aGraphics.setColor(getColor());
      graphics.setStroke(fLastStroke);
    }
    if (isAntiAliasing()) {
      graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    }
  }

  private void updateLastScale(int aMode, ILcdGXYContext aContext) {
    fLastScale = aContext.getGXYView().getScale();
    // Exaggerate the scale so that the difference in line width is more pronounced at less detailed zoom levels
    float exaggeratedScale = (float) fLastScale;
    if (fLastScale < 1f) {
      exaggeratedScale = (float) pow(fLastScale, fExaggeration);
    }
    float selectedLineWidth = exaggeratedScale * fSelectedLineWidth * fToMeterFactor;
    float lineWidth = exaggeratedScale * fLineWidth * fToMeterFactor;
    lineWidth = (float) Math.max(lineWidth, 1.0);
    if (fPattern != null) {
      fLastStroke = new BasicStroke(lineWidth, CAP_BUTT, JOIN_MITER, 1f, fPattern, 0);
      fLastSelectedStroke = new BasicStroke(selectedLineWidth, CAP_ROUND, JOIN_MITER, 1f, fPattern, 0);
    } else {
      fLastStroke = new BasicStroke(lineWidth, CAP_ROUND, JOIN_MITER, 1f);
      fLastSelectedStroke = new BasicStroke(selectedLineWidth, CAP_ROUND, JOIN_MITER, 1f);
    }
  }

  // Implementations for Object

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // This should never occur.
      throw new InternalError("Clone not supported for class " + this.getClass().getName());
    }
  }

  private static double exaggerationFromWidth(float aWidth) {
    return 10. / aWidth;
  }

  public void setStroking(float[] aPattern) {
    fPattern = aPattern;
  }
}
