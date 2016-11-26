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
package samples.gxy.shapes;

import java.awt.Color;
import java.io.IOException;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample shows how to programmatically create and display a model (ILcdModel)
 * by manually creating domain objects (ILcdShape) and adding them to the model.
 */
public class MainPanel extends GXYSample {

  private static final double MIN_LAYER_SCALE = 0.00001; // scale ( 1:100000 )
  private static final double MAX_LAYER_SCALE = 1;       // scale ( 1:1 )

  @Override
  public void createGUI() {
    super.createGUI();
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    ILcdGXYView map = getView();
    LayerFactory factory = new LayerFactory();
    GXYDataUtil.instance().model(createPolygonModel()).layer(factory).addToView(map).fit();
    GXYDataUtil.instance().model(createPolylineModel()).layer(factory).addToView(map);
    GXYDataUtil.instance().model(createPointModel()).layer(factory).addToView(map);
  }

  private static class LayerFactory implements ILcdGXYLayerFactory {

    @Override
    public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      TLcdGeodeticPen pen = new TLcdGeodeticPen();
      pen.setStraightLineMode(false);
      layer.setGXYPen(pen);
      layer.setSelectable(true);
      layer.setEditable(true);
      layer.setScaleRange(new TLcdInterval(MIN_LAYER_SCALE, MAX_LAYER_SCALE));

      // TLcdGXYShapePainter can visualize a point list as a polyline or polygon.
      TLcdGXYShapePainter painter = new TLcdGXYShapePainter() {
        @Override
        public ILcdGXYPainter getGXYPainter(Object aObject) {
          TLcdGXYShapePainter painter = (TLcdGXYShapePainter) super.getGXYPainter(aObject);
          painter.setMode(aObject instanceof ILcdPolygon ? ALcdGXYAreaPainter.OUTLINED_FILLED : ALcdGXYAreaPainter.OUTLINED);
          return painter;
        }
      };
      painter.setLineStyle(new TLcdG2DLineStyle(Color.green, Color.red));
      painter.setFillStyle(new TLcdGXYPainterColorStyle(new Color(40, 40, 40, 80)));

      painter.setIcon(new TLcdImageIcon("images/mif/mif20_airplane.gif"));
      painter.setSelectedIcon(new TLcdSymbol(TLcdSymbol.CIRCLE, 22, Color.red));

      layer.setGXYPainterProvider(painter);
      layer.setGXYEditorProvider(painter);
      return layer;
    }
  }

  private ILcdModel createPolygonModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPolygon",      // data type
        "Polygon"         // display name (user)
    ));

    model.addElement(createLonLatPolygon(datum.getEllipsoid()),
                     ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createPolylineModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPolyline",     // data type
        "Polyline"        // display name (user)
    ));

    model.addElement(createLonLatPolyline(datum.getEllipsoid()),
                     ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createPointModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "Point"           // display name (user)
    ));

    model.addElement(createLonLatPoint(),
                     ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private Object createLonLatPolyline(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] points = {
        new TLcdLonLatPoint(3.3, 50.4),
        new TLcdLonLatPoint(3.4, 50.5),
        new TLcdLonLatPoint(3.7, 50.8),
        new TLcdLonLatPoint(4.0, 50.9),
        new TLcdLonLatPoint(4.6, 51.2),
        new TLcdLonLatPoint(5.2, 51.9),
    };
    ILcd2DEditablePointList pointList =
        new TLcd2DEditablePointList(points, false);
    return new TLcdLonLatPolyline(pointList, aEllipsoid);
  }


  private Object createLonLatPolygon(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] points = {
        new TLcdLonLatPoint(3.4, 51.5),
        new TLcdLonLatPoint(4.3, 51.8),
        new TLcdLonLatPoint(5.2, 51.6),
        new TLcdLonLatPoint(5.2, 50.5),
        new TLcdLonLatPoint(3.7, 50.4),
    };
    ILcd2DEditablePointList pointList =
        new TLcd2DEditablePointList(points, false);
    return new TLcdLonLatPolygon(pointList, aEllipsoid);
  }

  private Object createLonLatPoint() {
    return new TLcdLonLatPoint(3.6, 51.2);
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Displaying shapes");
  }

}
