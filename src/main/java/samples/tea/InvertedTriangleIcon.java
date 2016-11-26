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
package samples.tea;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import com.luciad.gui.TLcdSymbol;

public class InvertedTriangleIcon extends TLcdSymbol {

  private int[] fTempXArray = new int[3];
  private int[] fTempYArray = new int[3];

  public InvertedTriangleIcon(int aType, int aSize, Color aBorderColor, Color aFillColor) {
    super(aType, aSize, aBorderColor, aFillColor);
  }

  // Draw inverted triangle...
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    int aType = getShape();
    int aSize = getSize();
    if (aType == FILLED_TRIANGLE) {
      aGraphics.setColor(getFillColor());
      fTempXArray[0] = aX;
      fTempXArray[1] = aX + (aSize / 2);
      fTempXArray[2] = aX + aSize;
      fTempYArray[0] = aY;
      fTempYArray[1] = aY + aSize;
      fTempYArray[2] = aY;
      aGraphics.fillPolygon(fTempXArray, fTempYArray, 3);
      aGraphics.setColor(getBorderColor());
      aGraphics.drawLine(aX, aY, aX + (aSize / 2), aY + aSize);
      aGraphics.drawLine(aX + (aSize / 2), aY + aSize, aX + aSize, aY);
      aGraphics.drawLine(aX + aSize, aY, aX, aY);
    } else if (aType == TRIANGLE) {
      aGraphics.setColor(getBorderColor());
      aGraphics.drawLine(aX, aY, aX + (aSize / 2), aY + aSize);
      aGraphics.drawLine(aX + (aSize / 2), aY + aSize, aX + aSize, aY);
      aGraphics.drawLine(aX + aSize, aY, aX, aY);
    } else {
      super.paintIcon(aComponent, aGraphics, aX, aY);
    }
  }
}
