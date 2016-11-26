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
package samples.network.crosscountry.data;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.decoder.MapSupport;
import samples.gxy.common.layers.factories.RasterLayerFactory;

/**
 * The layer factory of the raster routing sample.
 */
public class RoutingLayerFactory implements ILcdGXYLayerFactory {

  private RasterLayerFactory fRasterLayerFactory;

  public RoutingLayerFactory() {
    fRasterLayerFactory = new RasterLayerFactory();
  }

  public ILcdGXYLayer createGXYLayer(final ILcdModel aModel) {
    if (ModelFactory.ROUTE_TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName())) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel, "Route");

      layer.setSelectable(false);
      layer.setEditable(false);
      layer.setLabeled(false);
      layer.setVisible(true);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      layer.setGXYPainterProvider(new RoutePainter());

      return layer;
    }
    return null;
  }

  /**
   * A painter for <code>ILcdRoute</code> objects.
   */
  private static class RoutePainter extends ALcdGXYPainter {

    private static final TLcdImageIcon START_ICON = new TLcdImageIcon("samples/images/flag_green.png");
    private static final TLcdImageIcon END_ICON = new TLcdImageIcon("samples/images/flag_red.png");

    private TLcdGXYPointListPainter fPointListPainter;
    private TLcdGXYIconPainter fIconPainter;
    private ILcdRoute fRoute;

    public RoutePainter() {
      fPointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
      TLcdG2DLineStyle lineStyle = new TLcdG2DLineStyle();
      lineStyle.setColor(Color.cyan);
      lineStyle.setLineWidth(3);
      lineStyle.setAntiAliasing(true);
      fPointListPainter.setLineStyle(lineStyle);
      fIconPainter = new TLcdGXYIconPainter();
    }

    public void setObject(Object o) {
      fRoute = (ILcdRoute) o;
    }

    public Object getObject() {
      return fRoute;
    }

    public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
      for (int i = 0; i < fRoute.getEdgeCount(); i++) {
        fPointListPainter.setObject(fRoute.getEdge(i));
        fPointListPainter.paint(aGraphics, aMode, aContext);
      }
      if (fRoute.getNodeCount() > 0) {
        fIconPainter.setIcon(START_ICON);
        fIconPainter.setObject(fRoute.getNode(0));
        fIconPainter.paint(aGraphics, aMode, aContext);
      }
      if (fRoute.getNodeCount() > 1) {
        fIconPainter.setIcon(END_ICON);
        fIconPainter.setObject(fRoute.getNode(fRoute.getNodeCount() - 1));
        fIconPainter.paint(aGraphics, aMode, aContext);
      }
    }

    public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
      ILcd2DEditableBounds temp_bounds = aBoundsSFCT.cloneAs2DEditableBounds();
      for (int i = 0; i < fRoute.getNodeCount(); i++) {
        fPointListPainter.setObject(fRoute.getNode(i));
        if (i == 0) {
          fPointListPainter.boundsSFCT(aGraphics, aMode, aContext, aBoundsSFCT);
        } else {
          fPointListPainter.boundsSFCT(aGraphics, aMode, aContext, temp_bounds);
          aBoundsSFCT.setTo2DUnion(temp_bounds);
        }
      }
      if (fRoute.getNodeCount() > 0) {
        fIconPainter.setIcon(START_ICON);
        fIconPainter.setObject(fRoute.getNode(0));
        fIconPainter.boundsSFCT(aGraphics, aMode, aContext, temp_bounds);
        aBoundsSFCT.setTo2DUnion(temp_bounds);
      }
      if (fRoute.getNodeCount() > 1) {
        fIconPainter.setIcon(END_ICON);
        fIconPainter.setObject(fRoute.getNode(fRoute.getNodeCount() - 1));
        fIconPainter.boundsSFCT(aGraphics, aMode, aContext, temp_bounds);
        aBoundsSFCT.setTo2DUnion(temp_bounds);
      }
    }

    public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
      for (int i = 0; i < fRoute.getEdgeCount(); i++) {
        fPointListPainter.setObject(fRoute.getEdge(i));
        if (fPointListPainter.isTouched(aGraphics, aMode, aContext)) {
          return true;
        }
      }
      if (fRoute.getNodeCount() > 0) {
        fIconPainter.setIcon(START_ICON);
        fIconPainter.setObject(fRoute.getNode(0));
        if (fIconPainter.isTouched(aGraphics, aMode, aContext)) {
          return true;
        }
      }
      if (fRoute.getNodeCount() > 1) {
        fIconPainter.setIcon(END_ICON);
        fIconPainter.setObject(fRoute.getNode(fRoute.getNodeCount() - 1));
        if (fIconPainter.isTouched(aGraphics, aMode, aContext)) {
          return true;
        }
      }
      return false;
    }
  }
}
