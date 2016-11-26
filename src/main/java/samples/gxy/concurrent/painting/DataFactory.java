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
package samples.gxy.concurrent.painting;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePolygon;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;

public class DataFactory {
  /**
   * Create a synchronous layer
   *
   * @return a synchronous layer
   */
  public static ILcdGXYLayer createSynchronousLayer() {
    TLcdGXYLayer layer = new TLcdGXYLayer();

    // We create our custom ILcdModel and set it to our ILcdGXYLayer.
    layer.setModel(createPolygonModel(0));
    // We set the other aspects of our ILcdGXYLayer
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setLabel("Polygon [synchronous]");

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setLineStyle(new TLcdG2DLineStyle(Color.magenta, Color.red));
    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);

    return layer;
  }

  /**
   * Create a synchronous layer of which the paint time is at least
   * <code>aMinimalPaintTime</code>
   *
   * @param aMinimalPaintTime the minimum paint time for the layer, in milliseconds
   * @param aDelta            a delta translation to apply to the model element
   * @return an asynchronous layer of which the paint time is at least <code>aMinimalPaintTime</code>
   */
  public static ILcdGXYEditableLabelsLayer createLayerWithMinimalPaintTime(final long aMinimalPaintTime, double aDelta) {
    TLcdGXYLayer layer = new TLcdGXYLayer() {
      @Override
      public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
        if ((aMode & ILcdGXYLayer.ALL) != 0 &&
            (aMode & ILcdGXYLayer.BODIES) != 0) {
          try {
            Thread.sleep(aMinimalPaintTime);
          } catch (InterruptedException e) {
            // simply abort painting
          }
        }
        super.paint(aGraphics, aMode, aGXYView);
      }
    };

    // We create our custom ILcdModel and set it to our ILcdGXYLayer.
    layer.setModel(createPolygonModel(aDelta));
    // We set the other aspects of our ILcdGXYLayer
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setLabel("Polygon [" + aMinimalPaintTime + "]");

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setLineStyle(new TLcdG2DLineStyle(Color.magenta, Color.red));
    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);

    return layer;
  }

  private static ILcdModel createPolygonModel(double aDelta) {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(
        new TLcdModelDescriptor("Sample code",   // Source name
                                "MyPolygon",         // data type
                                "PolygonModel"));  // display name (user)

    ILcd2DEditablePolygon polygon = createLonLatPolygon(datum.getEllipsoid());
    polygon.translate2D(aDelta, aDelta);
    model.addElement(polygon, ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private static ILcd2DEditablePolygon createLonLatPolygon(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] points = {
        new TLcdLonLatPoint(8.20, 43.20),
        new TLcdLonLatPoint(8.20, 44.00),
        new TLcdLonLatPoint(9.20, 44.00),
        new TLcdLonLatPoint(9.20, 43.20),
    };
    return new TLcdLonLatPolygon(new TLcd2DEditablePointList(points, false), aEllipsoid);
  }
}
