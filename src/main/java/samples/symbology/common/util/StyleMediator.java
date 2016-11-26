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
package samples.symbology.common.util;

import java.awt.Color;

/**
 * Mediator class to configure the style of a military symbology object.
 */
public abstract class StyleMediator {

  private Object fObject;

  /**
   * Checks whether the given military symbol object can be configured.
   *
   * @param aObject the military symbol object to configure
   *
   * @return whether the given military symbol object can be configured.
   */
  public abstract boolean canSetObject(Object aObject);

  /**
   * Sets the military symbol object to be configured.
   *
   * @param aObject the object to be configured.
   */
  public void setObject(Object aObject) {
    fObject = aObject;
  }

  /**
   * Returns the currently set military symbol object.
   *
   * @return the currently set military symbol object.
   */
  public Object getObject() {
    return fObject;
  }

  /**
   * Returns the corner smoothness property value of the style of the military symbol object that is
   * currently set.
   *
   * @return the corner smoothness property value of the style of the military symbol object that is
   *         currently set.
   */
  public abstract double getCornerSmoothness();

  /**
   * Sets the value for the corner smoothness property of the style of the military symbol object
   * that is currently set.
   *
   * @param aCornerSmoothness the value for the corner smoothness property of the style of the
   *                          military symbol object that is currently set.
   */
  public abstract void setCornerSmoothness(double aCornerSmoothness);

  /**
   * Sets the fill percentage to fill the frame of icon symbols.
   * This factor must be a value in the interval [0.0, 1.0], with 0.0 indicating no fill and 1.0
   * indicating a complete fill, starting from the bottom of the frame.
   * <p>
   * @param aFillPercentage the fill percentage to fill the frame of icon symbols.
   * @see #getFillPercentage()
   */
  public abstract void setFillPercentage(double aFillPercentage);

  /**
   * Returns the fill percentage used to fill the frame of icon symbols.
   * This factor is a value in the interval [0.0, 1.0].
   *
   * @return the fill percentage to fill the frame of icon symbols.
   * @see #setFillPercentage(double)
   */
  public abstract double getFillPercentage();

  /**
   * Returns the size of the icon symbol that uses this style.
   *
   * @return The size of the icon symbol.
   */
  public abstract int getSizeSymbol();

  /**
   * Sets the size of the icon symbol that uses this style.
   *
   * @param aSize - The size of the icon symbol.
   */
  public abstract void setSizeSymbol(int aSize);

  /**
   * Returns the line width to use for all line symbols and off-set lines of icon symbols.
   *
   * @return the line width to use for all line symbols and off-set lines of icon symbols.
   */
  public abstract int getLineWidth();

  /**
   * Sets the line width to use for all line symbols and off-set lines of icon symbols.
   *
   * @param aWidth the new line width to use for all line symbols and off-set lines of icon
   *               symbols.
   */
  public abstract void setLineWidth(int aWidth);

  /**
   * Returns the frame line width of the icon symbols.
   * @return the frame line width of the icon symbols.
   */
  public abstract int getSymbolFrameLineWidth();

  /**
   * Sets the frame line width of icon symbols
   *
   * @param aWidth the new frame line width of icon symbols
   */
  public abstract void setSymbolFrameLineWidth(int aWidth);

  /**
   * Returns the point size to use for the label font.
   * @return the font size to use for labels.
   */
  public abstract int getFontSize();

  /**
   * Sets the point size to use for the label font.
   * @param aFontSize the font size to use for labels.
   * @see java.awt.Font
   */
  public abstract void setFontSize(int aFontSize);

  /**
   * Returns the thickness that is used for the halo of icon and line symbols.
   *
   * @return the thickness that is used for the halo of icon and line symbols.
   */
  public abstract int getHaloThickness();

  /**
   * Sets the thickness to be used for the halo of icon and line symbols.
   * Setting the thickness to zero disables the halo.
   *
   * @param aThickness the thickness to be used for the halo of icon and line symbols
   */
  public abstract void setHaloThickness(int aThickness);

  /**
   * Returns the color that is used for the halo of the line and symbols
   * @return the color that is used for the halo of the line and symbols
   */
  public abstract Color getHaloColor();

  /**
   * Sets the color to be used for the halo of the line and symbols.
   * @param aColor the color to be used for the halo of the line and symbols.
   */
  public abstract void setHaloColor(Color aColor);

  /**
   * Returns the thickness that is used for the label halo of icon and line symbols.
   *
   * @return the thickness that is used for the label halo of icon and line symbols.
   */
  public abstract int getLabelHaloThickness();

  /**
   * Sets the thickness to be used for the label halo of icon and line symbols.
   * Setting the thickness to zero disables the halo.
   *
   * @param aThickness the thickness to be used for the label halo of icon and line symbols
   */
  public abstract void setLabelHaloThickness(int aThickness);

  /**
   * Returns the color that is used for the label halo of the icon and line symbols
   * @return the color that is used for the label halo of the icon and line symbols
   */
  public abstract Color getLabelHaloColor();

  /**
   * Sets the color to be used for the label halo of the icon and line symbols
   * @param aColor the color to be used for the label halo of the icon and line symbols
   */
  public abstract void setLabelHaloColor(Color aColor);

  /**
   * Returns if the frame for point symbols is enabled.
   * @return if the frame for point symbols is enabled.
   */
  public abstract boolean isSymbolFrameEnabled();

  /**
   * Sets if the frame for point symbols should be enabled.
   * @param aEnabled true to enable
   */
  public abstract void setSymbolFrameEnabled(boolean aEnabled);

  /**
   * Returns if the icon for point symbols is enabled.
   * @return if the icon for point symbols is enabled.
   */
  public abstract boolean isSymbolIconEnabled();

  /**
   * Sets if the icon for point symbols should be enabled.
   * @param aEnabled true to enable
   */
  public abstract void setSymbolIconEnabled(boolean aEnabled);

  /**
   * Returns if the fill for point symbols is enabled.
   * @return if the fill for point symbols is enabled.
   */
  public abstract boolean isSymbolFillEnabled();

  /**
   * Sets if the fill for point symbols should be enabled.
   * @param aEnabled true to enable
   */
  public abstract void setSymbolFillEnabled(boolean aEnabled);

  /**
   * Returns whether the object that is currently set (see {@link #getObject()}) represents a line
   * symbol. This is general term used in the APP-6A and MIL-STD 2525b domain model to indicate a
   * symbol with more than one point; in practice, this is the case for all symbols that are not
   * units.
   *
   * @return whether the set object represents a line symbol.
   */
  public abstract boolean isLine();
}
