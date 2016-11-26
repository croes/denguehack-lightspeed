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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;

/**
 * Label painter extension that supports the extra options of LabelDetailAlgorithmWrapper.
 * It modifies the font and labeled properties according to the configured LabelDetailLabelLocation.
 */
class LabelDetailLabelPainter extends TLcdGXYDataObjectLabelPainter {

  public LabelDetailLabelPainter(TLcdGXYDataObjectLabelPainter aOldLabelPainter) {
    super(aOldLabelPainter);
  }

  @Override
  public void setLabelLocation(TLcdLabelLocation aLabelLocation) {
    // Modify the properties and the font size, based on the given LabelDetailLabelLocation
    if (aLabelLocation instanceof LabelDetailLabelLocation) {
      LabelDetailLabelLocation label_location = (LabelDetailLabelLocation) aLabelLocation;
      Font font = label_location.getFontSize().getFont();
      if (getFont() != font) {
        setFont(font);
      }
      setPropertyCount(label_location.getPropertyCount());
    } else {
      Font font = LabelDetailLabelLocation.getDefaultFontSize().getFont();
      if (getFont() != font) {
        setFont(font);
      }
      setPropertyCount(LabelDetailLabelLocation.getDefaultPropertyCount());
    }
    super.setLabelLocation(aLabelLocation);
  }

  @Override
  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
    Graphics2D graphics = (Graphics2D) aGraphics;
    try {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      super.paintLabel(aGraphics, aMode, aContext);
    } finally {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
  }

  private void setPropertyCount(int aPropertyCount) {
    if (aPropertyCount == 1) {
      setExpressions("CITY");
    } else if (aPropertyCount == 2) {
      setExpressions("CITY", "STATE");
    } else if (aPropertyCount == 3) {
      setExpressions("CITY", "STATE", "TOT_POP");
    } else {
      throw new IllegalArgumentException("Property count should be in the range [1,3] instead of " + aPropertyCount);
    }
  }
}
