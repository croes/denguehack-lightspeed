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
package samples.network.numeric.view.gxy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.EnumSet;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphNodePainterProvider;

public class NodePainterProvider extends AGraphNodePainterProvider {

  public static ILcdIcon ICON_START_NODE = new TLcdImageIcon("samples/images/flag_green.png");
  public static ILcdIcon ICON_END_NODE = new TLcdImageIcon("samples/images/flag_red.png");
  public static CrossingIcon ICON_CROSSING = new CrossingIcon(Color.GRAY, Color.DARK_GRAY);

  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter() {
    @Override
    public void paint(Graphics aGraphics, int aRenderMode, ILcdGXYContext aGXYContext) {
      if (aGXYContext.getGXYView().getScale() > 0.05 || !(getIcon() instanceof CrossingIcon)) {
        super.paint(aGraphics, aRenderMode, aGXYContext);
      }
    }
  };

  public NodePainterProvider(GraphManager aGraphManager, EnumSet<GraphNodeMode> aModes) {
    super(aGraphManager, aModes);
  }

  // Implementations for AGraphNodePainterProvider.

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphNodeMode aMode) {
    switch (aMode) {
    case NORMAL_NODE:
      fIconPainter.setIcon(ICON_CROSSING);
      break;
    case START_NODE:
      fIconPainter.setIcon(ICON_START_NODE);
      break;
    case END_NODE:
      fIconPainter.setIcon(ICON_END_NODE);
      break;
    }

    fIconPainter.setObject(aObject);
    return fIconPainter;
  }

  private static class CrossingIcon implements ILcdIcon {

    private Color fColor;
    private Color fBorderColor;

    private CrossingIcon(Color aColor, Color aBorderColor) {
      fColor = aColor;
      fBorderColor = aBorderColor;
    }

    public Object clone() {
      return this;
    }

    public int getIconHeight() {
      return 7;
    }

    public int getIconWidth() {
      return 7;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(fColor);
      g.fillOval(x, y, 5, 5);
      g.setColor(fBorderColor);
      g.drawOval(x, y, 5, 5);
    }
  }

}
