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
package samples.tea.gxy.visibility;

import java.awt.Color;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolygon;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.decoder.MapSupport;

/**
 * A simple factory class which creates the input layers for the visibility computations.
 */
class InputLayerFactory {

  private static final ILcdIcon fPointIcon          = new TLcdSymbol( TLcdSymbol.FILLED_TRIANGLE, 10, Color.white, Color.blue );
  private static final ILcdIcon fPointSelectionIcon = new TLcdSymbol( TLcdSymbol.FILLED_TRIANGLE, 10, Color.white, Color.red  );

  /**
   * Returns a layer with a model containing points.
   * @return a layer with a model containing points.
   */
  public static ILcdGXYLayer createPointLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Point", "Point",  "Point" ) );

    // Add a point.
    model.addElement( new TLcdLonLatHeightPoint( 10.620756, 46.436819, 0 ) {
      public String toString() {
        return "Point";
      }
    }, ILcdFireEventMode.NO_EVENT );

    // Create icon painter.
    TLcdGXYIconPainter icon_painter = new TLcdGXYIconPainter();
    icon_painter.setIcon( fPointIcon );
    icon_painter.setSelectionIcon( fPointSelectionIcon );

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setGXYPainterProvider( new VisibilityPainterPolyline() );
    layer.setSelectable( true );
    layer.setEditable( true );
    layer.setGXYPainterProvider( icon_painter );
    layer.setGXYEditorProvider( icon_painter );
    addLabelPainter( layer );
    return layer;
  }

  /**
   * Returns a layer with a model containing polylines.
   * @return a layer with a model containing polylines.
   */
  public static ILcdGXYLayer createPolylineLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Polyline", "Polyline",  "Polyline" ) );

    // Add a polyline 1.
    TLcdLonLatHeightPoint[] points1 = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 10.25, 47.0, 0 ),
            new TLcdLonLatHeightPoint( 10.75, 46.5, 0 ),
            new TLcdLonLatHeightPoint( 10.25, 46.5, 0 ),
            new TLcdLonLatHeightPoint( 10.75, 45.5, 0 ),
    };
    TLcd3DEditablePointList point_list1 = new TLcd3DEditablePointList( points1, false );
    TLcdLonLatHeightPolyline polyline1 = new TLcdLonLatHeightPolyline( point_list1 ) {
      public String toString() {
        return "Polyline 1";
      }
    };
    model.addElement( polyline1, ILcdFireEventMode.NO_EVENT );

    // Add a polyline 2.
    TLcdLonLatHeightPoint[] points2 = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint(  9.75, 47.15, 0 ),
            new TLcdLonLatHeightPoint( 10.25, 46.65, 0 ),
            new TLcdLonLatHeightPoint(  9.75, 46.65, 0 ),
            new TLcdLonLatHeightPoint( 10.25, 46.15, 0 ),
    };
    TLcd3DEditablePointList point_list2 = new TLcd3DEditablePointList( points2, false );
    TLcdLonLatHeightPolyline polyline2 = new TLcdLonLatHeightPolyline( point_list2 ) {
      public String toString() {
        return "Polyline 2";
      }
    };
    model.addElement( polyline2, ILcdFireEventMode.NO_EVENT );

    // Create line style.
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setColor( Color.blue );
    line_style.setSelectionColor( Color.red );
    line_style.setLineWidth( 2 );
    line_style.setSelectionLineWidth( 2 );

    // Create polyline painter.
    TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter( TLcdGXYPointListPainter.POLYLINE );
    painter.setLineStyle( line_style );

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setGXYPen( createPen() );
    layer.setSelectable( true );
    layer.setEditable( true );
    layer.setGXYPainterProvider( painter );
    layer.setGXYEditorProvider( painter );
    addLabelPainter( layer );
    return layer;
  }

  /**
   * Returns a layer with a model containing polygons.
   * @return a layer with a model containing polygons.
   */
  public static ILcdGXYLayer createPolygonLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Polygon", "Polygon",  "Polygon" ) );

    // Add a polygon 1.  
    TLcdLonLatHeightPoint[] points1 = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 10.50, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 46.50, 0 ),
    };
    TLcd3DEditablePointList point_list1 = new TLcd3DEditablePointList( points1, false );
    TLcdLonLatHeightPolygon polygon1 = new TLcdLonLatHeightPolygon( point_list1 ) {
      public String toString() {
        return "Polygon 1";
      }
    };
    model.addElement( polygon1, ILcdFireEventMode.NO_EVENT );

    // Add a polygon 2.
    TLcdLonLatHeightPoint[] points2 = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 11.50, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.50, 46.65, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 46.65, 0 ),
    };
    TLcd3DEditablePointList point_list2 = new TLcd3DEditablePointList( points2, false );
    TLcdLonLatHeightPolygon polygon2 = new TLcdLonLatHeightPolygon( point_list2 ) {
      public String toString() {
        return "Polygon 2";
      }
    };
    model.addElement( polygon2, ILcdFireEventMode.NO_EVENT );

    // Create line style.
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setColor( Color.blue );
    line_style.setSelectionColor( Color.red );
    line_style.setLineWidth( 2 );
    line_style.setSelectionLineWidth( 2 );

    // Create polygon painter.
    TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter( TLcdGXYPointListPainter.POLYGON );
    painter.setLineStyle( line_style );

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setGXYPen( createPen() );
    layer.setSelectable( true );
    layer.setEditable( true );
    layer.setGXYPainterProvider( painter );
    layer.setGXYEditorProvider( painter );
    addLabelPainter( layer );
    return layer;
  }

  private static ILcdGXYPen createPen() {
    TLcdGeodeticPen pen = new TLcdGeodeticPen( false );
    pen.setHotPointIcon(MapSupport.sHotPointIcon);
    pen.setAngleThreshold( 0.5 );
    pen.setWorldDistanceThreshold( 1250 );
    return pen;
  }

  private static void addLabelPainter( TLcdGXYLayer layer ) {
    TLcdGXYLabelPainter label_painter = new TLcdGXYLabelPainter();
    label_painter.setForeground( Color.black );
    label_painter.setHaloEnabled( true );

    layer.setLabeled( true );
    layer.setSelectionLabeled( true );
    layer.setGXYLabelPainterProvider( label_painter );
  }

}
