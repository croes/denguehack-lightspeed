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
package samples.decoder.gdf.view.gxy;

import java.awt.Component;
import java.awt.Graphics;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.gdf.ILcdGDFPointFeature;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphNodePainterProvider;

/**
 * Painter provider for GDF point features.
 */
public class GDFPointPainterProvider extends AGraphNodePainterProvider {

  private static final ILcdIcon EMPTY_ICON = new EmptyIcon();

  private Map<Integer, ILcdIcon> fPointStyles = new HashMap<Integer, ILcdIcon>();

  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();

  public GDFPointPainterProvider() {
  }

  public GDFPointPainterProvider(GraphManager aGraphManager, EnumSet<GraphNodeMode> aModes) {
    super(aGraphManager, aModes);
  }

  /**
   * Registers the specified icon for the given GDF feature class code.
   *
   * @param aFeatureClassCode the feature class code for which to register the icon.
   * @param aIcon             the icon to be registered.
   */
  public void registerStyle(int aFeatureClassCode, ILcdIcon aIcon) {
    fPointStyles.put(aFeatureClassCode, aIcon);
  }

  // Implementations for AGraphNodePainterProvider.

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphNodeMode aMode) {
    if (aObject instanceof ILcdGDFPointFeature) {
      ILcdGDFPointFeature feature = (ILcdGDFPointFeature) aObject;
      int fcc = feature.getFeatureClass().getFeatureClassCode();
      if (fcc == 4120) {
        // Road junctions.
        switch (aMode) {
        case NORMAL_NODE:
          fIconPainter.setIcon(EMPTY_ICON);
          break;
        case START_NODE:
          fIconPainter.setIcon(FlagIcon.ICON_START_NODE);
          break;
        case END_NODE:
          fIconPainter.setIcon(FlagIcon.ICON_END_NODE);
          break;
        }
        fIconPainter.setObject(aObject);
        return fIconPainter;
      } else {
        // Other point features.
        ILcdIcon icon = fPointStyles.get(fcc);
        if (icon != null) {
          fIconPainter.setObject(aObject);
          fIconPainter.setIcon(icon);
          return fIconPainter;
        }
      }
    }
    return null;
  }

  /**
   * Empty icon.
   */
  private static class EmptyIcon implements ILcdIcon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
    }

    public int getIconWidth() {
      return 8;
    }

    public int getIconHeight() {
      return 8;
    }

    public Object clone() {
      return this;
    }
  }

  /**
   * Flag icon.
   */
  public static class FlagIcon extends TLcdImageIcon {

    public static final ILcdIcon ICON_START_NODE = new FlagIcon("samples/images/flag_green.png");
    public static final ILcdIcon ICON_END_NODE = new FlagIcon("samples/images/flag_red.png");

    private FlagIcon(String aSourceName) {
      super(aSourceName);
    }

    @Override
    public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
      super.paintIcon(aComponent, aGraphics, aX, aY - getIconHeight() / 2);
    }

  }
}

