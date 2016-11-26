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
package samples.gxy.editing;

import com.luciad.gui.ILcdIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ALcdGXYPen;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.TLcdStrokeLineStyle;

import samples.common.MapColors;
import samples.common.model.GeodeticModelFactory;
import samples.gxy.decoder.MapSupport;

/**
 * Creates layers that supports painting, creating and editing all the shapes that are created in
 * this sample.
 */
public class ShapeGXYLayerFactory
    implements ILcdGXYLayerFactory {

  public static final ILcdGXYPainterStyle LINE_STYLE = TLcdStrokeLineStyle.newBuilder()
                                                                          .lineWidth(1.5f)
                                                                          .color(MapColors.INTERACTIVE_OUTLINE)
                                                                          .build();

  public static final ILcdGXYPainterStyle FILL_STYLE = new TLcdGXYPainterColorStyle(
      MapColors.INTERACTIVE_FILL,
      MapColors.SELECTION);

  public static final ILcdIcon SNAP_ICON = MapColors.createSnapIcon();

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!GeodeticModelFactory.isModelOfFormat(aModel)) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setModel(aModel);
    layer.setGXYPen(createPen(aModel));

    TLcdGXYShapePainter painterEditor = createPainterEditor();
    layer.setGXYPainterProvider(painterEditor);
    layer.setGXYEditorProvider(painterEditor);

    painterEditor.setFillStyle(FILL_STYLE);
    painterEditor.setLineStyle(LINE_STYLE);
    painterEditor.setIcon(MapColors.createInteractiveIcon(false));
    painterEditor.setSelectedIcon(MapColors.createInteractiveIcon(true));

    layer.setSelectable(true);
    layer.setEditable(true);

    return layer;
  }

  private TLcdGXYShapePainter createPainterEditor() {
    // The painter/editor is responsible for drawing, editing and creating all shapes in the layer.
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setSnapIcon(SNAP_ICON);
    painter.setAntiAliased(true);
    return painter;
  }

  private ALcdGXYPen createPen(ILcdModel aModel) {

    // The pen is used by the painters as a painting tool, for example to
    // discretize lines, or to draw hot points
    ALcdGXYPen pen = MapSupport.createPen(aModel.getModelReference(), false);

    // The adaptive drawing of geodetic lines, polylines and arcs can be tuned
    // by means of four different parameters: the minimum and maximum recursion
    // depth of the adaptive algorithm, thresholds on the distances between
    // consecutive points, and a threshold on the angles between two consecutive
    // line segments. This allows to set the right balance between performance
    // and accuracy.
//    pen.setMinRecursionDepth( 0 );
//    pen.setMaxRecursionDepth( 10 );
//    pen.setWorldDistanceThreshold( 10.0 );
//    pen.setViewDistanceThreshold( 6 );
//    pen.setAngleThreshold( 0.5 );

    return pen;
  }

  public static TLcdGXYShapePainter retrieveGXYPainterEditor(ILcdGXYLayer aLayer) {
    if (isGXYLayerOfFormat(aLayer)) {
      return (TLcdGXYShapePainter) ((TLcdGXYLayer) aLayer).getGXYPainterProvider();
    }
    return null;
  }

  public static boolean isGXYLayerOfFormat(ILcdGXYLayer aLayer) {
    return (aLayer instanceof TLcdGXYLayer && ((TLcdGXYLayer) aLayer).getGXYPainterProvider() instanceof TLcdGXYShapePainter);
  }
}
