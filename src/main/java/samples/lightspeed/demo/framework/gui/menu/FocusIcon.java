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
package samples.lightspeed.demo.framework.gui.menu;

import java.awt.Component;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.Icon;

/**
 * Icon that renders a wrapped icon slightly brighter.
 */
public class FocusIcon implements Icon {

  private final Icon fDelegate;

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D graphics2d = (Graphics2D) g;
    Composite oldComposite = graphics2d.getComposite();
    graphics2d.setComposite(new MyComposite());
    fDelegate.paintIcon(c, g, x, y);
    graphics2d.setComposite(oldComposite);
  }

  @Override
  public int getIconWidth() {
    return fDelegate.getIconWidth();
  }

  @Override
  public int getIconHeight() {
    return fDelegate.getIconHeight();
  }

  public FocusIcon(Icon aDelegate) {
    fDelegate = aDelegate;
  }

  private static class MyComposite implements Composite {
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
      return new CompositeContext() {
        @Override
        public void dispose() {

        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
          for (int y = 0; y < dstOut.getHeight(); y++) {
            for (int x = 0; x < dstOut.getWidth(); x++) {
              int[] srcPixels = new int[4];
              src.getPixel(x, y, srcPixels);
              srcPixels[0] = Math.min(255, (int) (srcPixels[0] * 1.1));
              srcPixels[1] = Math.min(255, (int) (srcPixels[1] * 1.1));
              srcPixels[2] = Math.min(255, (int) (srcPixels[2] * 1.3));
              dstOut.setPixel(x, y, srcPixels);
            }
          }
        }
      };
    }
  }
}
