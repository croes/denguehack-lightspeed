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
package samples.decoder.asdi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

/**
 * Painter for tracks that represents them with an icon as simple as possible, useful when zoomed
 * out and a lot of tracks are potentially visible.
 */
public class TrackGXYPainterZoomedOut extends TLcdGXYIconPainter {

  public TrackGXYPainterZoomedOut() {
    setIcon(new SimpleFastIcon(Color.black, Color.gray.brighter()));
    setSelectionIcon(new SimpleFastIcon(Color.orange, Color.black));
  }

  private static class SimpleFastIcon implements ILcdIcon, Cloneable {
    private static final int SIZE = 6;
    private Color fColor;
    private final Color fOutlineColor;

    public SimpleFastIcon(Color aColor, Color aOutlineColor) {
      fColor = aColor;
      fOutlineColor = aOutlineColor;
    }

    public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
      aGraphics.setColor(fColor);
      aGraphics.fillRect(aX + 1, aY + 1, SIZE - 1, SIZE - 1);
      aGraphics.setColor(fOutlineColor);
      aGraphics.drawRect(aX, aY, SIZE - 1, SIZE - 1);
    }

    public int getIconWidth() {
      return SIZE;
    }

    public int getIconHeight() {
      return SIZE;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        //Implements Cloneable and extends from Object, CloneNotSupportedException is impossible
        throw new RuntimeException(e);
      }
    }
  }
}
