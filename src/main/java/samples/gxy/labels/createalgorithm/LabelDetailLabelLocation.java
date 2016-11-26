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
package samples.gxy.labels.createalgorithm;

import java.awt.Font;

import com.luciad.view.TLcdLabelLocation;

/**
 * This TLcdLabelLocation extension adds an extra property count and font size property that
 * can be interpreted by the label painter.
 */
public class LabelDetailLabelLocation extends TLcdLabelLocation {

  public enum FontSize {
    LARGE(new Font("Dialog", Font.ITALIC, 14)),
    SMALL(new Font("Dialog", Font.BOLD, 8));

    private Font fFont;

    private FontSize(Font aFont) {
      fFont = aFont;
    }

    public Font getFont() {
      return fFont;
    }
  }

  private FontSize fFontSize = getDefaultFontSize();
  private int fPropertyCount = getDefaultPropertyCount();

  public LabelDetailLabelLocation() {
    super();
  }

  public static FontSize getDefaultFontSize() {
    return FontSize.LARGE;
  }

  public static int getDefaultPropertyCount() {
    return 3;
  }

  public FontSize getFontSize() {
    return fFontSize;
  }

  public void setFontSize(FontSize aFontSize) {
    fFontSize = aFontSize;
  }

  public int getPropertyCount() {
    return fPropertyCount;
  }

  public void setPropertyCount(int aPropertyCount) {
    fPropertyCount = aPropertyCount;
  }

  @Override
  public void copyFrom(TLcdLabelLocation aSourceLabelLocation) {
    super.copyFrom(aSourceLabelLocation);
    LabelDetailLabelLocation label_location = (LabelDetailLabelLocation) aSourceLabelLocation;
    fFontSize = label_location.getFontSize();
    fPropertyCount = label_location.getPropertyCount();
  }

  @Override
  public String toString() {
    return super.toString() + "; fFontSize [" + fFontSize + "]; fPropertyCount [" + fPropertyCount + "]";
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    LabelDetailLabelLocation clone = (LabelDetailLabelLocation) super.clone();
    clone.fFontSize = this.fFontSize;
    clone.fPropertyCount = this.fPropertyCount;
    return clone;
  }

}
