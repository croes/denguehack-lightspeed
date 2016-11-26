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
package samples.tea.gxy.extremepoint;

import java.awt.Color;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolygon;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.decoder.MapSupport;
import samples.tea.InvertedTriangleIcon;

/**
 * A simple factory class which creates a layer that will contain extreme minima and
 * extreme maxima points. This factory also creates a layer containing a single polygon.
 */
class ExtremePointLayerFactory {

  private static final ILcdIcon fIconMaximum = new TLcdSymbol        ( TLcdSymbol.FILLED_TRIANGLE, 10, Color.black, Color.red   );
  private static final ILcdIcon fIconMinimum = new InvertedTriangleIcon( TLcdSymbol.FILLED_TRIANGLE, 10, Color.black, Color.green );

  /**
   * Returns a layer with an empty model that can contain extreme minimum points.
   * @return a layer with an empty model that can contain extreme minimum points.
   */
  public static ILcdGXYLayer createExtremeMinimaLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "LowestPoints", "LowestPoints", "LowestPoints" ) );

    // Create icon painter.
    TLcdGXYIconPainter icon_painter = new TLcdGXYIconPainter();
    icon_painter.setIcon( fIconMinimum );

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setSelectable( false );
    layer.setEditable( false );
    layer.setGXYPainterProvider( icon_painter );
    addLabelPainter( layer );
    return layer;
  }

  /**
   * Returns a layer with an empty model that can contain extreme maximum points.
   * @return a layer with an empty model that can contain extreme maximum points.
   */
  public static ILcdGXYLayer createExtremeMaximaLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "HighestPoints", "HighestPoints", "HighestPoints" ) );

    // Create icon painter.
    TLcdGXYIconPainter icon_painter = new TLcdGXYIconPainter();
    icon_painter.setIcon( fIconMaximum );

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setSelectable( false );
    layer.setEditable( false );
    layer.setGXYPainterProvider( icon_painter );
    addLabelPainter( layer );
    return layer;
  }

  /**
   * Returns a layer with a model containing one polygon.
   * @return a layer with a model containing one polygon.
   */
  public static ILcdGXYLayer createPolygonLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    model.setModelReference(modelReference);
    model.setModelDescriptor( new TLcdModelDescriptor( "Polygon", "Polygon",  "Polygon" ) );

    // Add a polygon.
    TLcdLonLatHeightPoint[] points = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 10.50, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 46.50, 0 ),
    };
    TLcd3DEditablePointList point_list = new TLcd3DEditablePointList( points, false );
    TLcdLonLatHeightPolygon polygon = new TLcdLonLatHeightPolygon( point_list );
    model.addElement( polygon, ILcdFireEventMode.NO_EVENT );

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
    layer.setGXYPen(MapSupport.createPen(modelReference, false));
    layer.setSelectable( true );
    layer.setEditable( true );
    layer.setGXYPainterProvider( painter );
    layer.setGXYEditorProvider( painter );
    layer.selectObject( polygon, true, ILcdFireEventMode.NO_EVENT );
    return layer;
  }

  private static void addLabelPainter( TLcdGXYLayer layer ) {
    TLcdGXYLabelPainter label_painter = new TLcdGXYLabelPainter() {
      protected String[] retrieveLabels( int aMode, ILcdGXYContext aGXYContext ) {
        if ( getObject() instanceof ILcdPoint ) {
          return new String[] { ((int)( (ILcdPoint) getObject() ).getZ()) + "m" };
        }
        return super.retrieveLabels( aMode, aGXYContext );
      }
    };
    label_painter.setForeground( Color.black );
    label_painter.setHaloEnabled( true );

    layer.setSelectable( true );
    layer.setSelectionLabeled( true );
    layer.setGXYLabelPainterProvider( label_painter );
  }

}
