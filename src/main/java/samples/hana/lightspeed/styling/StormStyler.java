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

import static java.awt.Color.red;
import static java.awt.Color.yellow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ILspTexturedStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styles storm shapes based on their wind speed.
 */
public class StormStyler extends ALspStyler {

  private static final Color orange = new Color(255, 134, 0);

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aTLspContext) {
    for (Object object : aObjects) {
      Color color = red;
      int radii = ((Number) ((ILcdDataObject) object).getValue("SPEED")).intValue();
      if (radii == 34) {
        color = yellow;
      }
      if (radii == 50) {
        color = orange;
      }
      if (radii == 64) {
        color = red;
      }
      aStyleCollector.object(object).styles(TLspLineStyle.newBuilder().color(color).width(2.5).zOrder(radii).build(),
                                            TLspFillStyle.newBuilder().color(color).opacity(0.1f).zOrder(radii - 1).textureIcon(new HatchIcon()).textureCoordinatesMode(ILspTexturedStyle.TextureCoordinatesMode.VIEW_SCALED).repeatTexture(true).build()).submit();
    }
  }

  /**
   * A fill pattern with hatched lines.
   */
  private static class HatchIcon implements ILcdIcon {
    @Override
    public int getIconWidth() {
      return 15;
    }

    @Override
    public int getIconHeight() {
      return 15;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(Color.gray);
      g.fillRect(x, y, x + getIconWidth(), y + getIconHeight());
      g.setColor(Color.white);
      g.drawLine(x + getIconWidth(), y, x, y + getIconHeight());
      g.drawLine(x, y, x, y);
      g.drawLine(x + getIconWidth(), y + getIconHeight(), x + getIconWidth(), y + getIconHeight());
    }

    @Override
    public Object clone() {
      return this;
    }
  }
}
