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
import java.io.Serializable;

import com.luciad.view.gxy.ILcdGXYPainterStyle;

/**
 * Encapsulates the following shape style properties:
 * <ul>
 * <li>line style: an ILcdGXYPainterStyle can be set to configure the colors and strokes of the outline</li>
 * <li>fill style: an ILcdGXYPainterStyle can be set to configure how a shape is filled                </li>
 * <li>roundness: a roundness factor can be set to render the shape with rounded corners               </li>
 * <li>haloColor, haloThickness: a halo can be defined to render the shape with a halo effect          </li>
 * </ul>
 */
class ShapeStyle implements Serializable, Cloneable {

  private ILcdGXYPainterStyle fLineStyle;
  private ILcdGXYPainterStyle fFillStyle;
  private double fRoundness = 0.5d;
  private int fHaloThickness;
  private Color fHaloColor;

  public ShapeStyle() {
    this.fHaloColor = Color.black;
  }

  /**
   * Sets a painter style to render the shape outline. A painter style can be used to initialize the
   * properties of the Graphics, such as colors, strokes, etc.
   *
   * @param aLineStyle a painter style to render the shape.
   *
   * @see ILcdGXYPainterStyle
   */
  public void setLineStyle(ILcdGXYPainterStyle aLineStyle) {
    fLineStyle = aLineStyle;
  }

  public ILcdGXYPainterStyle getLineStyle() {
    return fLineStyle;
  }

  /**
   * Sets a painter style to fill the shape. A painter style can be used to initialize the
   * properties of the Graphics, such as colors, strokes, etc.
   *
   * @param aFillStyle a painter style to render the shape.
   *
   * @see ILcdGXYPainterStyle
   */
  public void setFillStyle(ILcdGXYPainterStyle aFillStyle) {
    fFillStyle = aFillStyle;
  }

  public ILcdGXYPainterStyle getFillStyle() {
    return fFillStyle;
  }

  /**
   * Sets the roundness factor that should be used to render the corners of this shape. This
   * factor must be a value in the interval [0.0, 1.0], with 0.0 indicating no rounding and 1.0
   * indicating a maximum rounding, which results in a very smooth corner.
   *
   * @param aRoundness the roundness (or rounding) factor for polygon and shape corners.
   *
   * @throws IllegalArgumentException if aRoundness < 0 || aRoundness > 1.0.
   * @see com.luciad.view.gxy.painter.TLcdGXYRoundedPointListPainter#setRoundness(double)
   */
  public void setRoundness(double aRoundness) {
    if (aRoundness < 0.0 || aRoundness > 1.0) {
      throw new IllegalArgumentException("Factor must be in the interval [0.0 - 1.0]!");
    }
    fRoundness = aRoundness;
  }

  /**
   * Returns the roundness factor that is used to render the corners of this shape. This factor
   * is a value in the interval [0.0, 1.0]. <p/> By default, 0.5 is used.
   *
   * @return the roundness factor for the shape corners.
   *
   * @see #setRoundness(double)
   */
  public double getRoundness() {
    return fRoundness;
  }

  /**
   * Returns the current halo thickness. <p/> By default, the halo thickness is 0.
   *
   * @return the current halo thickness.
   *
   * @see #setHaloThickness(int)
   */
  public int getHaloThickness() {
    return fHaloThickness;
  }

  /**
   * Sets the thickness (in pixels) of the halo to be added around the shape.
   *
   * @param aHaloThickness the new halo thickness.
   *
   * @see com.luciad.view.gxy.painter.TLcdGXYHaloPainter#setHaloThickness(int)
   */
  public void setHaloThickness(int aHaloThickness) {
    fHaloThickness = aHaloThickness;
  }

  /**
   * Returns the current halo color.
   *
   * @return the current halo color.
   *
   * @see #setHaloColor(Color)
   */
  public Color getHaloColor() {
    return fHaloColor;
  }

  /**
   * Sets the color of the halo to be added around the shape.
   *
   * @param aHaloColor the new halo color.
   *
   * @see com.luciad.view.gxy.painter.TLcdGXYHaloPainter#setHaloColor(Color)
   */
  public void setHaloColor(Color aHaloColor) {
    fHaloColor = aHaloColor;
  }
}
