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
package samples.symbology.common.gui.customizer;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import samples.symbology.common.app6.APP6StyleMediator;
import samples.symbology.common.ms2525.MS2525StyleMediator;
import samples.symbology.common.util.StyleMediator;

/**
 * Combines APP6StyleMediator and MS2525StyleMediator.
 */
class SymbologyStyleMediator extends StyleMediator {

  private final List<StyleMediator> fMediators = Arrays.asList(
      new APP6StyleMediator(),
      new MS2525StyleMediator()
  );
  private StyleMediator fCurrentMediator;

  @Override
  public boolean canSetObject(Object aObject) {
    return getStyleMediator(aObject) != null;
  }

  @Override
  public void setObject(Object aObject) {
    fCurrentMediator = getStyleMediator(aObject);
    fCurrentMediator.setObject(aObject);
  }

  private StyleMediator getStyleMediator(Object aObject) {
    StyleMediator objMediator = null;
    for (StyleMediator mediator : fMediators) {
      if (mediator.canSetObject(aObject)) {
        objMediator = mediator;
        break;
      }
    }
    return objMediator;
  }

  @Override
  public double getCornerSmoothness() {
    return fCurrentMediator.getCornerSmoothness();
  }

  @Override
  public double getFillPercentage() {
    return fCurrentMediator.getFillPercentage();
  }

  @Override
  public int getFontSize() {
    return fCurrentMediator.getFontSize();
  }

  @Override
  public int getHaloThickness() {
    return fCurrentMediator.getHaloThickness();
  }

  @Override
  public Color getHaloColor() {
    return fCurrentMediator.getHaloColor();
  }

  @Override
  public int getLabelHaloThickness() {
    return fCurrentMediator.getLabelHaloThickness();
  }

  @Override
  public Color getLabelHaloColor() {
    return fCurrentMediator.getLabelHaloColor();
  }

  @Override
  public int getLineWidth() {
    return fCurrentMediator.getLineWidth();
  }

  @Override
  public int getSymbolFrameLineWidth(){
    return fCurrentMediator.getSymbolFrameLineWidth();
  }

  @Override
  public Object getObject() {
    if (fCurrentMediator == null) {
      return null;
    }
    return fCurrentMediator.getObject();
  }

  @Override
  public int getSizeSymbol() {
    return fCurrentMediator.getSizeSymbol();
  }

  @Override
  public boolean isLine() {
    return fCurrentMediator.isLine();
  }

  @Override
  public void setCornerSmoothness(double aCornerSmoothness) {
    fCurrentMediator.setCornerSmoothness(aCornerSmoothness);
  }

  @Override
  public void setFillPercentage(double aFillPercentage) {
    fCurrentMediator.setFillPercentage(aFillPercentage);
  }

  @Override
  public void setFontSize(int aFontSize) {
    fCurrentMediator.setFontSize(aFontSize);
  }

  @Override
  public void setHaloThickness(int aThickness) {
    fCurrentMediator.setHaloThickness(aThickness);
  }

  @Override
  public void setHaloColor(Color aColor) {
    fCurrentMediator.setHaloColor(aColor);
  }

  @Override
  public void setLabelHaloThickness(int aLabelHaloSize) {
    fCurrentMediator.setLabelHaloThickness(aLabelHaloSize);
  }

  @Override
  public void setLabelHaloColor(Color aColor) {
    fCurrentMediator.setLabelHaloColor(aColor);
  }

  @Override
  public boolean isSymbolFrameEnabled() {
    return fCurrentMediator.isSymbolFrameEnabled();
  }

  @Override
  public void setSymbolFrameEnabled(boolean aEnabled) {
    fCurrentMediator.setSymbolFrameEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolIconEnabled() {
    return fCurrentMediator.isSymbolIconEnabled();
  }

  @Override
  public void setSymbolIconEnabled(boolean aEnabled) {
    fCurrentMediator.setSymbolIconEnabled(aEnabled);
  }

  @Override
  public boolean isSymbolFillEnabled() {
    return fCurrentMediator.isSymbolFillEnabled();
  }

  @Override
  public void setSymbolFillEnabled(boolean aEnabled) {
    fCurrentMediator.setSymbolFillEnabled(aEnabled);
  }

  @Override
  public void setLineWidth(int aLineWidth) {
    fCurrentMediator.setLineWidth(aLineWidth);
  }

  @Override
  public void setSymbolFrameLineWidth(int aWidth) {
    fCurrentMediator.setSymbolFrameLineWidth(aWidth);
  }

  @Override
  public void setSizeSymbol(int aSizeSymbol) {
    fCurrentMediator.setSizeSymbol(aSizeSymbol);
  }
}
