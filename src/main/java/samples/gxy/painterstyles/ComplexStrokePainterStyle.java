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

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Vector;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.TLcdGXYComplexStroke;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;

/**
 * Extension of TLcdGXYPainterColorStyle that applies
 * a complex stroke on the Graphics before painting an object.
 */
class ComplexStrokePainterStyle extends TLcdGXYPainterColorStyle {

  // Anti-aliasing property.
  // For optimal rendering quality, anti-aliasing should be enabled.
  private boolean fAntiAliasing = true;

  // List of patterns to be used for the complex stroke.
  // Each pattern contains an array of java.awt.Shape objects,
  // which are combined in one TLcdGXYComplexStroke.
  private PatternLibrary.Pattern[] fPatterns;

  // Repetition factors for each pattern.
  // In the resulting TLcdGXYComplexStroke, it defines the number of occurrences
  // of each pattern in the resulting TLcdGXYComplexStroke.
  private int[] fRepetitions;

  // The width of the gaps between the individual shapes of each pattern.
  // Each entry defines the gap width that is applied for a whole pattern.
  private int[] fGapWidths;

  // Controls whether the resulting TLcdGXYComplexStroke only contains complete sequences
  // of the combined patterns, or that it can also contains partial patterns (e.g., if
  // there is not enough space to draw a complete sequence.
  private boolean fAllowSplit;

  // The fallback stroke to be used by TLcdGXYComplexStroke.
  private BasicStroke fFallbackStroke = new BasicStroke(3);

  // The tolerance factor to be used by TLcdGXYComplexStroke.
  private double fTolerance = 5f;

  // The combined array of java.awt.Shape objects from all patterns.
  private Shape[] fShapes;

  // The width of each single java.awt.Shape object combined with the gap width.
  private double[] fWidths;

  /**
   * Sets the pattern configuration for the complex stroke.
   *
   * @param aPattern           The array of Pattern objects to be used for the complex stroke.
   * @param aPatternRepetition The repetition factors for each Pattern object.  The length of the array must
   *                           correspond to the number of Pattern objects.
   * @param aPatternGapWidth   The gap widths for each Pattern object. The length of the array must
   *                           correspond to the number of Pattern objects.
   */
  public void setPatterns(PatternLibrary.Pattern[] aPattern, int[] aPatternRepetition, int[] aPatternGapWidth) {
    fPatterns = aPattern;
    fRepetitions = aPatternRepetition;
    fGapWidths = aPatternGapWidth;

    // Combine the shapes of all patterns, taking into account the repetition factor.
    Vector shape_list = new Vector();
    for (int i = 0; i < aPattern.length; i++) {
      for (int j = 0; j < aPatternRepetition[i]; j++) {
        for (int k = 0; k < aPattern[i].getShapes().length; k++) {
          shape_list.add(aPattern[i].getShapes()[k]);
        }
      }
    }
    fShapes = (Shape[]) shape_list.toArray(new Shape[shape_list.size()]);

    // Construct the pattern width array.
    fWidths = new double[fShapes.length];
    int index = 0;
    for (int i = 0; i < aPattern.length; i++) {
      for (int j = 0; j < aPatternRepetition[i]; j++) {
        for (int k = 0; k < aPattern[i].getShapes().length; k++) {
          fWidths[index] = fShapes[index].getBounds2D().getWidth() + aPatternGapWidth[i];
          index++;
        }
      }
    }
  }

  /**
   * Returns the array of Pattern objects to be used for the complex stroke.
   *
   * @return the array of Pattern objects to be used for the complex stroke.
   */
  public PatternLibrary.Pattern[] getPatterns() {
    return fPatterns;
  }

  /**
   * Returns the repetition factors for each pattern.
   *
   * @return the repetition factors for each pattern.
   */
  public int[] getPatternRepetitions() {
    return fRepetitions;
  }

  /**
   * Returns the width of the gaps between the individual shapes of each pattern.
   *
   * @return the width of the gaps between the individual shapes of each pattern.
   */
  public int[] getGapWidths() {
    return fGapWidths;
  }

  /**
   * Returns whether the combined patterns may be splitted or not.
   *
   * @return true if the combined patterns may be splitted or not.
   * @see TLcdGXYComplexStroke
   * @see #setAllowSplit(boolean)
   */
  public boolean isAllowSplit() {
    return fAllowSplit;
  }

  /**
   * Sets whether TLcdGXYComplexStroke may only use complete sequences
   * of the combined patterns, or that it can also use partial patterns (e.g., if
   * there is not enough space to draw a complete sequence).
   *
   * @param aAllowSplit whether the combined patterns may be splitted or not.
   * @see TLcdGXYComplexStroke
   * @see #isAllowSplit()
   */
  public void setAllowSplit(boolean aAllowSplit) {
    fAllowSplit = aAllowSplit;
  }

  /**
   * Returns the tolerance factor that is used by the complex stroke.
   * <p/>
   * By default, 5.0 is returned.
   *
   * @return the tolerance factor that is used by the complex stroke.
   * @see TLcdGXYComplexStroke
   * @see #setTolerance(double)
   */
  public double getTolerance() {
    return fTolerance;
  }

  /**
   * Sets the tolerance factor to be used by the complex stroke.
   *
   * @param aTolerance the tolerance factor to be used by the complex stroke.
   * @see TLcdGXYComplexStroke
   * @see #getTolerance()
   */
  public void setTolerance(double aTolerance) {
    fTolerance = aTolerance;
  }

  /**
   * Returns whether anti-aliasing is enabled on the Graphics.
   * <p/>
   * By default, true is returned.
   *
   * @return true if anti-aliasing is enabled on the Graphics.
   */
  public boolean isAntiAliasing() {
    return fAntiAliasing;
  }

  /**
   * Sets whether anti-aliasing should be enabled on the Graphics.
   *
   * @param aAntiAliasing whether anti-aliasing should be enabled on the Graphics.
   */
  public void setAntiAliasing(boolean aAntiAliasing) {
    fAntiAliasing = aAntiAliasing;
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Sets a TLcdGXYComplexStroke on the Graphics2D object, initialized with the current settings
   * of this ComplexPainterStyle. If {@link #isAntiAliasing()} returns true, anti-aliasing is also enabled.
   */
  public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aGXYContext) {
    super.setupGraphics(aGraphics, aObject, aMode, aGXYContext);

    Graphics2D graphics_2d = (Graphics2D) aGraphics;

    // Apply a complex stroke, based on the current configuration parameters.
    // Note: we recreate a TLcdGXYComplexStroke object for each invocation of this method,
    // to be able to apply the current view boundaries as a clip on the TLcdGXYComplexStroke object.
    // By applying such a clip, TLcdGXYComplexStroke only applies stroking within the clip,
    // which results in a higher performance. This is especially noticeable when closely zoomed in on a long line.
    Rectangle clip = new Rectangle(0, 0, aGXYContext.getGXYView().getWidth(), aGXYContext.getGXYView().getHeight());
    TLcdGXYComplexStroke complex_stroke = new TLcdGXYComplexStroke(fShapes, fWidths, fAllowSplit, fFallbackStroke, fTolerance, clip);
    graphics_2d.setStroke(complex_stroke);

    if (isAntiAliasing()) {
      graphics_2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
  }
}
