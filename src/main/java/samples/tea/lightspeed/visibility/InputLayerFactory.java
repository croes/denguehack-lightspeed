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
package samples.tea.lightspeed.visibility;

import java.awt.Color;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;

/**
 * A simple factory class which creates the input layers for the visibility computations.
 */
class InputLayerFactory {

  private static final ILcdIcon POINT_ICON = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 10, Color.WHITE, Color.BLUE);
  private static final ILcdIcon POINT_SELECTION_ICON = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 10, Color.WHITE, Color.RED);

  /**
   * Returns a layer with a model containing points.
   * @return a layer with a model containing points.
   */
  public static ILspInteractivePaintableLayer createPointLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdModelDescriptor("Point", "Point", "Point"));

    // Add a point.
    model.addElement(new TLcdLonLatPoint(10.617, 46.442) {
      public String toString() {
        return "Point";
      }
    }, ILcdFireEventMode.NO_EVENT);

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                .selectable(true)
                                .bodyEditable(true)
                                .bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(POINT_ICON).build())
                                .bodyStyles(TLspPaintState.SELECTED, TLspIconStyle.newBuilder().icon(POINT_SELECTION_ICON).build())
                                .labelStyles(TLspPaintState.REGULAR, TLspTextStyle.newBuilder().textColor(Color.BLACK).build())
                                .label(model.getModelDescriptor().getDisplayName())
                                .build();
  }

  /**
   * Returns a layer with a model containing polylines.
   * @return a layer with a model containing polylines.
   */
  public static ILspInteractivePaintableLayer createPolylineLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Polyline", "Polyline",  "Polyline" ) );

    // Add a polyline 1.
    TLcdLonLatPoint[] points1 = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(10.25, 47.0),
        new TLcdLonLatPoint(10.75, 46.5),
        new TLcdLonLatPoint(10.25, 46.5),
        new TLcdLonLatPoint(10.75, 46.0),
    };
    TLcd2DEditablePointList pointList1 = new TLcd2DEditablePointList(points1, false);
    TLcdLonLatPolyline polyline1 = new TLcdLonLatPolyline(pointList1) {
      public String toString() {
        return "Polyline 1";
      }
    };
    model.addElement(polyline1, ILcdFireEventMode.NO_EVENT);

    // Add a polyline 2.
    TLcdLonLatPoint[] points2 = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(9.75, 47.15),
        new TLcdLonLatPoint(10.25, 46.65),
        new TLcdLonLatPoint(9.75, 46.65),
        new TLcdLonLatPoint(10.25, 46.15),
    };
    TLcd2DEditablePointList pointList2 = new TLcd2DEditablePointList(points2, false);
    TLcdLonLatPolyline polyline2 = new TLcdLonLatPolyline(pointList2) {
      public String toString() {
        return "Polyline 2";
      }
    };
    model.addElement(polyline2, ILcdFireEventMode.NO_EVENT );

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                .selectable(true)
                                .bodyEditable(true)
                                .bodyStyles(TLspPaintState.REGULAR, TLspLineStyle.newBuilder().color(Color.BLUE).width(2).build())
                                .bodyStyles(TLspPaintState.SELECTED, TLspLineStyle.newBuilder().color(Color.RED).width(2).build())
                                .labelStyles(TLspPaintState.REGULAR, TLspTextStyle.newBuilder().textColor(Color.BLACK).build())
                                .label(model.getModelDescriptor().getDisplayName())
                                .build();
  }

  /**
   * Returns a layer with a model containing polygons.
   * @return a layer with a model containing polygons.
   */
  public static ILspInteractivePaintableLayer createPolygonLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Polygon", "Polygon",  "Polygon" ) );

    // Add a polygon 1.  
    TLcdLonLatPoint[] points1 = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(10.50, 47.00),
        new TLcdLonLatPoint(11.25, 47.00),
        new TLcdLonLatPoint(11.25, 46.00),
        new TLcdLonLatPoint(11.00, 46.00),
        new TLcdLonLatPoint(11.00, 46.50),
    };
    TLcd2DEditablePointList pointList1 = new TLcd2DEditablePointList( points1, false );
    TLcdLonLatPolygon polygon1 = new TLcdLonLatPolygon( pointList1 ) {
      public String toString() {
        return "Polygon 1";
      }
    };
    model.addElement(polygon1, ILcdFireEventMode.NO_EVENT );

    // Add a polygon 2.
    TLcdLonLatPoint[] points2 = new TLcdLonLatPoint[]{
        new TLcdLonLatPoint(11.50, 47.00),
        new TLcdLonLatPoint(11.50, 46.65),
        new TLcdLonLatPoint(11.00, 46.65),
    };
    TLcd2DEditablePointList pointList2 = new TLcd2DEditablePointList( points2, false );
    TLcdLonLatPolygon polygon2 = new TLcdLonLatPolygon( pointList2 ) {
      public String toString() {
        return "Polygon 2";
      }
    };
    model.addElement(polygon2, ILcdFireEventMode.NO_EVENT );

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                .selectable(true)
                                .bodyEditable(true)
                                .bodyStyles(TLspPaintState.REGULAR, TLspLineStyle.newBuilder().color(Color.BLUE).width(2).build())
                                .bodyStyles(TLspPaintState.SELECTED, TLspLineStyle.newBuilder().color(Color.RED).width(2).build())
                                .labelStyles(TLspPaintState.REGULAR, TLspTextStyle.newBuilder().textColor(Color.BLACK).build())
                                .label(model.getModelDescriptor().getDisplayName())
                                .build();
  }

}
