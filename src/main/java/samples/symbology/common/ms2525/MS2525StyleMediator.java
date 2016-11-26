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
package samples.symbology.common.ms2525;

import java.awt.Color;

import samples.symbology.common.util.StyleMediator;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bShape;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyled;

/**
 * Mediator class to configure the style of a MIL-STD 2525 object.
 */
public class MS2525StyleMediator extends StyleMediator {

  private ILcdMS2525bStyle fMS2525bStyle;
  private boolean fIsLine;

  public boolean canSetObject(Object aObject) {
    return aObject instanceof ILcdMS2525bShape &&
           aObject instanceof ILcdMS2525bStyled;
  }

  public void setObject(Object aObject) {
    if (canSetObject(aObject)) {
      super.setObject(aObject);
      fMS2525bStyle = ((ILcdMS2525bStyled) aObject).getMS2525bStyle();
      fIsLine = ((ILcdMS2525bShape) aObject).isLine();
    } else {
      super.setObject(null);
      fMS2525bStyle = null;
    }
  }

  public double getCornerSmoothness() {
    return fMS2525bStyle.getCornerSmoothness();
  }

  public void setCornerSmoothness(double aCornerSmoothness) {
    fMS2525bStyle.setCornerSmoothness(aCornerSmoothness);
  }

  public double getFillPercentage() {
    return fMS2525bStyle.getFillPercentage();
  }

  public void setFillPercentage(double aFillPercentage) {
    fMS2525bStyle.setFillPercentage(aFillPercentage);
  }

  public int getSizeSymbol() {
    return fMS2525bStyle.getSizeSymbol();
  }

  public void setSizeSymbol(int aSizeSymbol) {
    fMS2525bStyle.setSizeSymbol(aSizeSymbol);
  }

  public int getLineWidth() {
    return fMS2525bStyle.getLineWidth();
  }

  public void setLineWidth(int aLineWidth) {
    fMS2525bStyle.setLineWidth(aLineWidth);
  }

  @Override
  public int getSymbolFrameLineWidth() {
    return fMS2525bStyle.getSymbolFrameLineWidth();
  }

  @Override
  public void setSymbolFrameLineWidth(int aWidth) {
    fMS2525bStyle.setSymbolFrameLineWidth(aWidth);
  }

  public int getFontSize() {
    return (fMS2525bStyle.getLabelFont() != null) ? fMS2525bStyle.getLabelFont().getSize() : -1;
  }

  public void setFontSize(int aFontSize) {
    if ((fMS2525bStyle.getLabelFont() != null)) {
      fMS2525bStyle.setLabelFont(fMS2525bStyle.getLabelFont().deriveFont((float) aFontSize));
    }
  }

  @Override
  public int getHaloThickness() {
    return !fMS2525bStyle.isHaloEnabled() ? 0 : fMS2525bStyle.getHaloThickness();
  }

  @Override
  public void setHaloThickness(int aThickness) {
    fMS2525bStyle.setHaloEnabled(aThickness != 0);
    fMS2525bStyle.setHaloThickness(aThickness);
  }

  @Override
  public Color getHaloColor() {
    return fMS2525bStyle.getHaloColor();
  }

  @Override
  public void setHaloColor(Color aColor) {
    fMS2525bStyle.setHaloColor(aColor);
  }

  public int getLabelHaloThickness() {
    return !fMS2525bStyle.isLabelHaloEnabled() ? 0 : fMS2525bStyle.getLabelHaloThickness();
  }

  public void setLabelHaloThickness(int aLabelHaloThickness) {
    fMS2525bStyle.setLabelHaloEnabled(aLabelHaloThickness != 0);
    fMS2525bStyle.setLabelHaloThickness(aLabelHaloThickness);
  }

  @Override
  public Color getLabelHaloColor() {
    return fMS2525bStyle.getLabelHaloColor();
  }

  @Override
  public void setLabelHaloColor(Color aColor) {
    fMS2525bStyle.setLabelHaloColor(aColor);
  }

  @Override
  public boolean isSymbolFrameEnabled() {
    return fMS2525bStyle.isSymbolFrameEnabled();
  }

  @Override
  public void setSymbolFrameEnabled(boolean aEnabled) {
    fMS2525bStyle.setSymbolFrameEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolIconEnabled() {
    return fMS2525bStyle.isSymbolIconEnabled();
  }

  @Override
  public void setSymbolIconEnabled(boolean aEnabled) {
    fMS2525bStyle.setSymbolIconEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolFillEnabled() {
    return fMS2525bStyle.isSymbolFillEnabled();
  }

  @Override
  public void setSymbolFillEnabled(boolean aEnabled) {
    fMS2525bStyle.setSymbolFillEnabled(aEnabled);
  }

  public boolean isLine() {
    return fIsLine;
  }
}
