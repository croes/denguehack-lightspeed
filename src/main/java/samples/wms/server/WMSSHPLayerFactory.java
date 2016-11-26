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
package samples.wms.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYDataObjectPolygonLabelPainter;
import com.luciad.view.gxy.TLcdGXYDataObjectPolylineLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;
import com.luciad.wms.server.ILcdWMSGXYLayerFactory;
import com.luciad.wms.server.TLcdWMSRequestContext;
import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * Implementation of <code>ILcdWMSGXYLayerFactory</code> that supports creating layers for SHP
 * models.
 */
class WMSSHPLayerFactory implements ILcdWMSGXYLayerFactory {

  private static final boolean PAINT_CACHE_ENABLED = false;
  private static final ILcdIcon DEFAULT_SYMBOL = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 5, new Color(200, 150, 190));
  private static final TLcdSymbol DEFAULT_SELECTION_ICON = new TLcdSymbol(TLcdSymbol.RECT, 7, Color.red);

  /**
   * Creates and returns a <code>TLcdGXYLayer</code> object to display a <code>ILcdModel</code>
   * object which model descriptor is an instance of <code>TLcdSHPModelDescriptor</code>.
   *
   * @param aModel    the model to be displayed
   * @param aWMSLayer the WMS layer entity from the capabilities
   * @param aStyleID  an optional style identifier requested by the WMS client
   * @param aRequestContext the request context associated with this operation
   *
   * @return a <code>ILcdGXYLayer</code> instance that displays the given SHP model in a GXY view
   */
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel, ALcdWMSLayer aWMSLayer, String aStyleID, TLcdWMSRequestContext aRequestContext) {
    if (!(aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor)) {
      throw new IllegalArgumentException("Cannot create a layer for[" + aModel + "]: not a SHP ILcdModel !");
    }

    ILcdInterval scaleRange = (ILcdInterval) aWMSLayer.getProperty("scaleRange");

    String label = aWMSLayer.getTitle();

    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    layer.setLabel(label);
    layer.setSelectable(false);
    layer.setEditable(false);
    layer.setVisible(true);
    layer.setScaleRange(scaleRange);

    TLcdSHPModelDescriptor modelDescriptor = (TLcdSHPModelDescriptor) aModel.getModelDescriptor();
    int[] shapeTypes = modelDescriptor.getShapeTypes();

    WMSLayerStyleUtil.MyLabelStyle labelStyle = WMSLayerStyleUtil.getLabelStyle(aWMSLayer);
    boolean isLabeled = (labelStyle != null);
    layer.setLabeled(isLabeled);

    // We assume in the next lines that aModel contains only one of the 3 following
    // shapes: point (TLcdSHPModelDescriptor.Point), polyline and polygon

    if ((shapeTypes[0] == TLcdSHPModelDescriptor.POINT) ||
        (shapeTypes[0] == TLcdSHPModelDescriptor.POINT_M) ||
        (shapeTypes[0] == TLcdSHPModelDescriptor.POINT_Z)) {
      // Setting the ILcdGXYPainter for ILcdShape which are ILcdPoint.
      // We use a TLcdGXYIconPainter which will draw an Icon for each point.
      TLcdGXYIconPainter iconPainter = new TLcdGXYIconPainter();
      ILcdIcon icon = (ILcdIcon) aWMSLayer.getProperty("pointstyle.icon", DEFAULT_SYMBOL);
      iconPainter.setIcon(icon);
      iconPainter.setSelectionIcon(DEFAULT_SELECTION_ICON);
      layer.setGXYPainterProvider(iconPainter);

      if (isLabeled) {
        DataObjectLabelPainter labelPainter = new DataObjectLabelPainter();
        labelPainter.setBackground(labelStyle.fBackground);
        labelPainter.setForeground(labelStyle.fForeground);
        labelPainter.setFrame(labelStyle.fFramed);
        labelPainter.setFilled(labelStyle.fFilled);
        labelPainter.setWithPin(labelStyle.fWithPin);
        if (labelStyle.fFont != null) {
          labelPainter.setFont(labelStyle.fFont);
        }
        if (labelStyle.fExpressions != null) {
          labelPainter.setExpressions(labelStyle.fExpressions);
        }
        layer.setGXYLabelPainterProvider(labelPainter);
      }
    } else if ((shapeTypes[0] == TLcdSHPModelDescriptor.POLYLINE) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.POLYLINE_M) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.POLYLINE_Z)) {
      // Setting the ILcdGXYPainter for ILcdShapeList which ILcdShape are
      // ILcdPointList and need to be displayed as polylines
      TLcdGXYPointListPainter pointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
      pointListPainter.setPaintCache(PAINT_CACHE_ENABLED);
      pointListPainter.setLineStyle(WMSLayerStyleUtil.getLineStyle(aWMSLayer));

      TLcdGXYShapeListPainter shapeListPainter = new TLcdGXYShapeListPainter();
      shapeListPainter.setShapeGXYPainterProvider(pointListPainter);
      layer.setGXYPainterProvider(shapeListPainter);

      if (isLabeled) {
        DataObjectPolylineLabelPainter labelPainter = new DataObjectPolylineLabelPainter();
        labelPainter.setMaxNumberPossibilities(1);
        labelPainter.setBackground(labelStyle.fBackground);
        labelPainter.setForeground(labelStyle.fForeground);
        labelPainter.setFrame(labelStyle.fFramed);
        labelPainter.setFilled(labelStyle.fFilled);
        if (labelStyle.fFont != null) {
          labelPainter.setFont(labelStyle.fFont);
        }
        if (labelStyle.fExpressions != null) {
          labelPainter.setExpressions(labelStyle.fExpressions);
        }
        layer.setGXYLabelPainterProvider(labelPainter);
      }
    } else if ((shapeTypes[0] == TLcdSHPModelDescriptor.POLYGON) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.POLYGON_M) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.POLYGON_Z)) {

      ILcdGXYPainterStyle fillStyle = null;
      ILcdGXYPainterStyle lineStyle = null;

      String strMode = (String) aWMSLayer.getProperty("mode");
      int mode = TLcdGXYPointListPainter.OUTLINED_FILLED;
      if (strMode != null) {
        if (strMode.equalsIgnoreCase("polygon")) {
          mode = TLcdGXYPointListPainter.POLYGON;
          lineStyle = WMSLayerStyleUtil.getLineStyle(aWMSLayer);
        } else if (strMode.equalsIgnoreCase("filled")) {
          mode = TLcdGXYPointListPainter.FILLED;
          fillStyle = WMSLayerStyleUtil.getFillStyle(aWMSLayer);
        } else {  // default mode is OUTLINED_FILLED
          fillStyle = WMSLayerStyleUtil.getFillStyle(aWMSLayer);
          lineStyle = WMSLayerStyleUtil.getLineStyle(aWMSLayer);
        }
      }

      // Setting the ILcdGXYPainter for ILcdShapeList which ILcdShape are
      // ILcdPointList and need to be displayed as area
      TLcdGXYShapeListPainter shapeListPainter = new TLcdGXYShapeListPainter();
      TLcdGXYPointListPainter polygonPainter = new TLcdGXYPointListPainter(mode);
      polygonPainter.setAnchorPointLocation(TLcdGXYPointListPainter.INSIDE_POLYGON);
      if (fillStyle != null) {
        polygonPainter.setFillStyle(fillStyle);
      }
      if (lineStyle != null) {
        polygonPainter.setLineStyle(lineStyle);
      }
      polygonPainter.setPaintCache(PAINT_CACHE_ENABLED);
      shapeListPainter.setShapeGXYPainterProvider(polygonPainter);
      layer.setGXYPainterProvider(shapeListPainter);

      if (isLabeled) {
        DataObjectPolygonLabelPainter labelPainter = new DataObjectPolygonLabelPainter();
        labelPainter.setBackground(labelStyle.fBackground);
        labelPainter.setForeground(labelStyle.fForeground);
        labelPainter.setFrame(labelStyle.fFramed);
        labelPainter.setFilled(labelStyle.fFilled);
        if (labelStyle.fFont != null) {
          labelPainter.setFont(labelStyle.fFont);
        }
        if (labelStyle.fExpressions != null) {
          labelPainter.setExpressions(labelStyle.fExpressions);
        }
        layer.setGXYLabelPainterProvider(labelPainter);
      }
    } else if ((shapeTypes[0] == TLcdSHPModelDescriptor.MULTI_POINT) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.MULTI_POINT_M) ||
               (shapeTypes[0] == TLcdSHPModelDescriptor.MULTI_POINT_Z)) {
      TLcdGXYShapeListPainter shapeListPainter = new TLcdGXYShapeListPainter();
      TLcdGXYPointListPainter pointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POINT);
      shapeListPainter.setShapeGXYPainterProvider(pointListPainter);
      layer.setGXYPainterProvider(shapeListPainter);
      if (isLabeled) {
        DataObjectLabelPainter labelPainter = new DataObjectLabelPainter();
        labelPainter.setBackground(labelStyle.fBackground);
        labelPainter.setForeground(labelStyle.fForeground);
        labelPainter.setFrame(labelStyle.fFramed);
        labelPainter.setFilled(labelStyle.fFilled);
        labelPainter.setWithPin(labelStyle.fWithPin);
        if (labelStyle.fFont != null) {
          labelPainter.setFont(labelStyle.fFont);
        }
        if (labelStyle.fExpressions != null) {
          labelPainter.setExpressions(labelStyle.fExpressions);
        }
        layer.setGXYLabelPainterProvider(labelPainter);
      }
    }

    return layer;
  }

  /**
   * Extension of <code>TLcdGXYDataObjectLabelPainter</code> that only paints labels when the
   * object's anchor point is visible in the requested map. The anchor point is the point to which
   * the label pin is attached. This approach is taken to avoid duplicate labels when querying the
   * WMS using a tiled approach.
   */
  private static class DataObjectLabelPainter extends TLcdGXYDataObjectLabelPainter {

    @Override
    public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext,
                                  Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
      Point point = new Point();
      super.anchorPointSFCT(aGraphics, aMode, aGXYContext, point);

      if (point.getX() < 0 || point.getX() > aGXYContext.getGXYView().getWidth() ||
          point.getY() < 0 || point.getY() > aGXYContext.getGXYView().getHeight()) {
        throw new TLcdNoBoundsException();
      } else {
        return super.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
      }
    }
  }

  /**
   * Extension of <code>TLcdGXYDataObjectPolylineLabelPainter</code> that only paints labels when
   * the object's anchor point is visible in the requested map. The anchor point is the point to
   * which the label pin is attached. This approach is taken to avoid duplicate labels when querying
   * the WMS using a tiled approach.
   */
  private static class DataObjectPolylineLabelPainter extends TLcdGXYDataObjectPolylineLabelPainter {

    @Override
    public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext,
                                  Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
      Point point = new Point();
      super.anchorPointSFCT(aGraphics, aMode, aGXYContext, point);

      if (point.getX() < 0 || point.getX() > aGXYContext.getGXYView().getWidth() ||
          point.getY() < 0 || point.getY() > aGXYContext.getGXYView().getHeight()) {
        throw new TLcdNoBoundsException();
      } else {
        return super.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
      }
    }
  }

  /**
   * Extension of <code>TLcdGXYDataObjectPolygonLabelPainter</code> that only paints labels when the
   * object's anchor point is visible in the requested map. The anchor point is the point to which
   * the label pin is attached. This approach is taken to avoid duplicate labels when querying the
   * WMS using a tiled approach.
   */
  private static class DataObjectPolygonLabelPainter extends TLcdGXYDataObjectPolygonLabelPainter {

    @Override
    public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext,
                                  Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
      Point point = new Point();
      super.anchorPointSFCT(aGraphics, aMode, aGXYContext, point);

      if (point.getX() < 0 || point.getX() > aGXYContext.getGXYView().getWidth() ||
          point.getY() < 0 || point.getY() > aGXYContext.getGXYView().getHeight()) {
        throw new TLcdNoBoundsException();
      } else {
        return super.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
      }
    }
  }

}
