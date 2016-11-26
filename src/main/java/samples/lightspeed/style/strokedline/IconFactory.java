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
package samples.lightspeed.style.strokedline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;

import samples.common.ScaledIcon;

class IconFactory {

  static List<ALspStyle> createNavaidIconStyle() {
    ILcdIcon navaidIcon = new NavaidIcon();
    TLspIconStyle style = TLspIconStyle.newBuilder()
                                       .icon(new ScaledIcon(navaidIcon, 4))
                                       .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                       .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING)
                                       .worldSize(5 * navaidIcon.getIconWidth())
                                       .useOrientation(true)
                                       .build();
    return Collections.<ALspStyle>singletonList(style);
  }

  static List<ALspStyle> createTriangleIconStyle() {
    ILcdIcon triangleIcon = new TLcdSymbol(TLcdSymbol.TRIANGLE, 9, Color.black);
    TLspIconStyle style = TLspIconStyle.newBuilder()
                                       .icon(new ScaledIcon(triangleIcon, 16))
                                       .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                       .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING)
                                       .worldSize(10 * triangleIcon.getIconWidth())
                                       .useOrientation(true)
                                       .build();
    return Collections.<ALspStyle>singletonList(style);
  }

  static List<ALspStyle> createCrossIconStyle() {
    ILcdIcon crossIcon = new TLcdSymbol(TLcdSymbol.CROSS, 9, Color.black);
    TLspIconStyle style = TLspIconStyle.newBuilder()
                                       .icon(new ScaledIcon(crossIcon, 16))
                                       .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                       .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING)
                                       .worldSize(10 * crossIcon.getIconWidth())
                                       .useOrientation(true)
                                       .build();
    return Collections.<ALspStyle>singletonList(style);
  }

  static List<ALspStyle> createDestinationIconStyle() {
    ILcdIcon destinationIcon = new DestinationIcon();
    TLspIconStyle style = TLspIconStyle.newBuilder()
                                       .icon(new ScaledIcon(destinationIcon, 2))
                                       .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                       .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING)
                                       .worldSize(1000.0 * destinationIcon.getIconWidth())
                                       .useOrientation(true)
                                       .build();
    return Collections.<ALspStyle>singletonList(style);
  }

  private static class NavaidIcon implements ILcdIcon {

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(Color.black);
      g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      g.drawArc(x + 2, y + 2, 56, 56, 0, 360);
      g.drawArc(x + 26, y + 26, 8, 8, 0, 360);

      for (int i = 0; i < 12; i++) {
        double cosAngle = Math.cos(Math.toRadians(30 * i));
        double sinAngle = Math.sin(Math.toRadians(30 * i));
        int innerRadius = i % 3 == 0 ? 20 : 24;
        g2d.setStroke(new BasicStroke(2));
        g.drawLine((int) (x + 30 + cosAngle * 27),
                   (int) (y + 30 + sinAngle * 27),
                   (int) (x + 30 + cosAngle * innerRadius),
                   (int) (y + 30 + sinAngle * innerRadius));

        g2d.setStroke(new BasicStroke(3f));
      }

      g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      g.drawArc(x + 14, y + 14, 32, 32, 0, 360);

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    @Override
    public int getIconWidth() {
      return 64;
    }

    @Override
    public int getIconHeight() {
      return 64;
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        // Should not happen
        throw new RuntimeException(e);
      }
    }
  }

  private static class DestinationIcon implements ILcdIcon {

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(new Color(48, 48, 48));
      g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      g.drawArc(x + 1, y + 1, 78, 78, 0, 360);
      g.drawArc(x + 37, y + 37, 6, 6, 0, 360);

      for (int i = 0; i < 36; i++) {
        double cosAngle = Math.cos(Math.toRadians(10 * i));
        double sinAngle = Math.sin(Math.toRadians(10 * i));
        int innerRadius = i % 9 == 0 ? 25 : (i % 3 == 0 ? 29 : 33);
        g2d.setStroke(i % 9 == 0 ? new BasicStroke(3) : new BasicStroke(2));
        g.drawLine((int) (x + 40 + cosAngle * 38),
                   (int) (y + 40 + sinAngle * 38),
                   (int) (x + 40 + cosAngle * innerRadius),
                   (int) (y + 40 + sinAngle * innerRadius));
      }
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    @Override
    public int getIconWidth() {
      return 80;
    }

    @Override
    public int getIconHeight() {
      return 80;
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        // Should not happen
        throw new RuntimeException(e);
      }
    }
  }
}
