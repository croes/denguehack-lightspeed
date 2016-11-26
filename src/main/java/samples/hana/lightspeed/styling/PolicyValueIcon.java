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
package samples.hana.lightspeed.styling;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import com.luciad.gui.ILcdIcon;

/**
 * Icon decoration that shows a colored bar indicating the policy value.
 */
public class PolicyValueIcon implements ILcdIcon {

  private static final int WIDTH = 28;
  private static final int HEIGHT = 28;
  private static final int BAR_HEIGHT = 3;
  private final int fRating;

  public PolicyValueIcon(int aRating) {
    fRating = aRating;
  }

  @Override
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
    if (fRating <= 5) {
      aGraphics.setColor(StatisticsBodyStyler.lerp(Color.green.darker(), Color.orange.darker(), (double) fRating / 5.0));
    } else {
      aGraphics.setColor(StatisticsBodyStyler.lerp(Color.orange.darker(), Color.red.darker(), (double) (fRating - 5) / 5.0));
    }
    int width = (int) ((double) (fRating + 2) / 12.0 * WIDTH);
    aGraphics.fillRect(aX, aY + HEIGHT - BAR_HEIGHT, width, BAR_HEIGHT);
  }

  @Override
  public final int getIconWidth() {
    return WIDTH;
  }

  @Override
  public int getIconHeight() {
    return HEIGHT;
  }

  @Override
  public Object clone() {
    throw new UnsupportedOperationException();
  }

}
