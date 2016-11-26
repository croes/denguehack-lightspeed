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
package samples.gxy.painterstyles;

import java.awt.Color;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYEditorProvider;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYHatchedFillStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYHaloPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYRoundedPointListPainter;

import samples.gxy.decoder.MapSupport;

/**
 * This is the TLcdGXYLayer where the StyledPolyShape objects, created with mouse
 * interactions, are displayed.
 */
class StyledShapeLayer extends TLcdGXYLayer {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(StyledShapeLayer.class.getName());

  public StyledShapeLayer() {
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    TLcdVectorModel model = new TLcdVectorModel(modelReference);
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the newly created shapes.", // source name (is used as tooltip text)
        "Shapes", // type name
        "Shapes"  // display name
    ));

    // set the model to this TLcdGXYLayer
    setModel(model);

    // add to Polylines
    ILcd2DEditablePoint[] pointarray = new ILcd2DEditablePoint[]{
        new TLcdLonLatPoint(3.06, 51.0),
        new TLcdLonLatPoint(3.30, 51.1),
        new TLcdLonLatPoint(3.50, 50.9),
        new TLcdLonLatPoint(3.70, 51.0),
    };
    ILcd2DEditablePointList pointlist = new TLcd2DEditablePointList(pointarray, false);
    StyledPolyline polyline = new StyledPolyline(pointlist);
    polyline.setValue(StyledPolyline.STREET_NAME, "polyline_1");
    polyline.getShapeStyle().setRoundness(0.7);
    polyline.getShapeStyle().setHaloThickness(1);
    polyline.getShapeStyle().setHaloColor(Color.black);
    polyline.getShapeStyle().setLineStyle(createComplexLineStyle(
        PatternLibrary.sRECTANGLE_1_PATTERN, 1,
        PatternLibrary.sRECTANGLE_2_PATTERN, 1));
    model.addElement(polyline, ILcdFireEventMode.FIRE_NOW);

    pointarray = new ILcd2DEditablePoint[]{
        new TLcdLonLatPoint(3.10, 51.15),
        new TLcdLonLatPoint(3.30, 51.30),
        new TLcdLonLatPoint(3.40, 51.20),
        new TLcdLonLatPoint(3.51, 51.15),
        new TLcdLonLatPoint(3.49, 51.00),
        new TLcdLonLatPoint(3.70, 50.70),
    };

    pointlist = new TLcd2DEditablePointList(pointarray, false);
    StyledPolygon polygon = new StyledPolygon(pointlist);
    polygon.getShapeStyle().setRoundness(1.0);
    polygon.getShapeStyle().setHaloThickness(1);
    polygon.getShapeStyle().setHaloColor(Color.black);
    polygon.getShapeStyle().setLineStyle(createComplexLineStyle(
        PatternLibrary.sCIRCLE_PATTERN, 8,
        PatternLibrary.sTEXT_PATTERN_1, 1));
    polygon.getShapeStyle().setFillStyle(createHatchedFillStyle());
    model.addElement(polygon, ILcdFireEventMode.FIRE_NOW);

    MyStyledPainterProvider linestyle_painterprovider = new MyStyledPainterProvider();
    setGXYPainterProvider(linestyle_painterprovider);
    setGXYEditorProvider(linestyle_painterprovider);

    setGXYPen(MapSupport.createPen(modelReference));

    setSelectable(true);
    setEditable(true);
    setLabel("Shapes");
  }

  private ILcdGXYPainterStyle createHatchedFillStyle() {
    return new TLcdGXYHatchedFillStyle();
  }

  private ILcdGXYPainterStyle createComplexLineStyle(
      PatternLibrary.Pattern aPattern1, int aRepetition1,
      PatternLibrary.Pattern aPattern2, int aRepetition2) {
    ComplexStrokePainterStyle painter_style = new ComplexStrokePainterStyle();

    // Complex stroke related settings.
    PatternLibrary.Pattern[] patterns = new PatternLibrary.Pattern[]{aPattern1, aPattern2};
    int[] repetitions = new int[]{aRepetition1, aRepetition2};
    int[] gap_widths = new int[]{3, 3};
    painter_style.setPatterns(patterns, repetitions, gap_widths);
    painter_style.setAllowSplit(true);
    painter_style.setTolerance(5);

    // General stroke settings.
    painter_style.setDefaultColor(Color.white);
    painter_style.setSelectionColor(Color.red);
    painter_style.setAntiAliasing(true);

    return painter_style;
  }

  private class MyStyledPainterProvider implements ILcdGXYPainterProvider, ILcdGXYEditorProvider {

    public ILcdGXYPainter getGXYPainter(Object aObject) {
      if (aObject instanceof StyledShape) {
        StyledShape shape = (StyledShape) aObject;

        boolean filled = shape.getShapeStyle().getFillStyle() == null;
        TLcdGXYRoundedPointListPainter spline_pointlist_painter = new TLcdGXYRoundedPointListPainter(
            filled ? TLcdGXYPointListPainter.POLYLINE : TLcdGXYPointListPainter.OUTLINED_FILLED);
        ShapeStyle shapeStyle = shape.getShapeStyle();
        spline_pointlist_painter.setRoundness(shapeStyle.getRoundness());
        spline_pointlist_painter.setLineStyle(shapeStyle.getLineStyle());
        spline_pointlist_painter.setFillStyle(shapeStyle.getFillStyle());
        spline_pointlist_painter.setPaintCache(false);

        if (shapeStyle.getHaloThickness() > 0) {
          TLcdGXYHaloPainter halo_painter = new TLcdGXYHaloPainter((ILcdGXYPainter) spline_pointlist_painter,
                                                                   shapeStyle.getHaloColor(),
                                                                   shapeStyle.getHaloThickness());
          halo_painter.setSelectionHaloEnabled(true);
          halo_painter.setObject(aObject);
          return halo_painter;
        } else {
          spline_pointlist_painter.setObject(aObject);
          return spline_pointlist_painter;
        }

      }
      sLogger.trace("No valid painter found.");
      return null;
    }

    public ILcdGXYEditor getGXYEditor(Object aObject) {
      if (aObject instanceof StyledShape) {
        StyledShape shape = (StyledShape) aObject;

        ShapeStyle shapeStyle = shape.getShapeStyle();
        TLcdGXYRoundedPointListPainter spline_pointlist_painter = new TLcdGXYRoundedPointListPainter(shapeStyle.getFillStyle() == null ? TLcdGXYPointListPainter.POLYLINE : TLcdGXYPointListPainter.OUTLINED_FILLED);
        spline_pointlist_painter.setRoundness(shapeStyle.getRoundness());
        spline_pointlist_painter.setLineStyle(shapeStyle.getLineStyle());
        spline_pointlist_painter.setFillStyle(shapeStyle.getFillStyle());
        spline_pointlist_painter.setPaintCache(false);
        spline_pointlist_painter.setObject(aObject);
        return spline_pointlist_painter;
      }
      sLogger.trace("No valid editor found.");
      return null;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException exc) {
        throw new InternalError("Could not clone " + this);
      }
    }
  }
}
