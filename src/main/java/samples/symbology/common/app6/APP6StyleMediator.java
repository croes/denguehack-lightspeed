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
package samples.symbology.common.app6;

import java.awt.Color;

import samples.symbology.common.util.StyleMediator;
import com.luciad.symbology.app6a.model.ILcdAPP6AShape;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyled;

/**
 * Mediator class to configure the style of an APP-6A object.
 */
public class APP6StyleMediator extends StyleMediator {

  private ILcdAPP6AStyle fAPP6AStyle;
  private boolean fIsLine;

  @Override
  public boolean canSetObject(Object aObject) {
    return aObject instanceof ILcdAPP6AShape &&
           aObject instanceof ILcdAPP6AStyled;
  }

  @Override
  public void setObject(Object aObject) {
    if (canSetObject(aObject)) {
      super.setObject(aObject);
      fAPP6AStyle = ((ILcdAPP6AStyled) aObject).getAPP6AStyle();
      fIsLine = ((ILcdAPP6AShape) aObject).isLine();
    } else {
      super.setObject(null);
      fAPP6AStyle = null;
    }
  }

  @Override
  public double getCornerSmoothness() {
    return fAPP6AStyle.getCornerSmoothness();
  }

  @Override
  public void setCornerSmoothness(double aCornerSmoothness) {
    fAPP6AStyle.setCornerSmoothness(aCornerSmoothness);
  }

  @Override
  public double getFillPercentage() {
    return fAPP6AStyle.getFillPercentage();
  }

  @Override
  public void setFillPercentage(double aFillPercentage) {
    fAPP6AStyle.setFillPercentage(aFillPercentage);
  }

  @Override
  public int getSizeSymbol() {
    return fAPP6AStyle.getSizeSymbol();
  }

  @Override
  public void setSizeSymbol(int aSizeSymbol) {
    fAPP6AStyle.setSizeSymbol(aSizeSymbol);
  }

  @Override
  public int getLineWidth() {
    return fAPP6AStyle.getLineWidth();
  }

  @Override
  public void setLineWidth(int aLineWidth) {
    fAPP6AStyle.setLineWidth(aLineWidth);
  }

  @Override
  public int getSymbolFrameLineWidth() {
    return fAPP6AStyle.getSymbolFrameLineWidth();
  }

  @Override
  public void setSymbolFrameLineWidth(int aWidth) {
    fAPP6AStyle.setSymbolFrameLineWidth(aWidth);
  }

  @Override
  public int getFontSize() {
    return (fAPP6AStyle.getLabelFont() != null) ? fAPP6AStyle.getLabelFont().getSize() : -1;
  }

  @Override
  public void setFontSize(int aFontSize) {
    if ((fAPP6AStyle.getLabelFont() != null)) {
      fAPP6AStyle.setLabelFont(fAPP6AStyle.getLabelFont().deriveFont((float) aFontSize));
    }
  }

  @Override
  public int getHaloThickness() {
    return !fAPP6AStyle.isHaloEnabled() ? 0 : fAPP6AStyle.getHaloThickness();
  }

  @Override
  public void setHaloThickness(int aThickness) {
    fAPP6AStyle.setHaloEnabled(aThickness != 0);
    fAPP6AStyle.setHaloThickness(aThickness);
  }

  @Override
  public Color getHaloColor() {
    return fAPP6AStyle.getHaloColor();
  }

  @Override
  public void setHaloColor(Color aColor) {
    fAPP6AStyle.setHaloColor(aColor);
  }

  @Override
  public int getLabelHaloThickness() {
    return !fAPP6AStyle.isLabelHaloEnabled() ? 0 : fAPP6AStyle.getLabelHaloThickness();
  }

  @Override
  public void setLabelHaloThickness(int aLabelHaloThickness) {
    fAPP6AStyle.setLabelHaloEnabled(aLabelHaloThickness != 0);
    fAPP6AStyle.setLabelHaloThickness(aLabelHaloThickness);
  }

  @Override
  public Color getLabelHaloColor() {
    return fAPP6AStyle.getLabelHaloColor();
  }

  @Override
  public void setLabelHaloColor(Color aColor) {
    fAPP6AStyle.setLabelHaloColor(aColor);
  }

  @Override
  public boolean isSymbolFrameEnabled() {
    return fAPP6AStyle.isSymbolFrameEnabled();
  }

  @Override
  public void setSymbolFrameEnabled(boolean aEnabled) {
    fAPP6AStyle.setSymbolFrameEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolIconEnabled() {
    return fAPP6AStyle.isSymbolIconEnabled();
  }

  @Override
  public void setSymbolIconEnabled(boolean aEnabled) {
    fAPP6AStyle.setSymbolIconEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolFillEnabled() {
    return fAPP6AStyle.isSymbolFillEnabled();
  }

  @Override
  public void setSymbolFillEnabled(boolean aEnabled) {
    fAPP6AStyle.setSymbolFillEnabled(aEnabled);
  }

  @Override
  public boolean isLine() {
    return fIsLine;
  }
}
