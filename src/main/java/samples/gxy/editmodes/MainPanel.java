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
package samples.gxy.editmodes;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.EnumMap;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.decoder.MapSupport;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.editing.ShapeGXYLayerFactory;

/**
 * This sample illustrates the use of a {@link MultiModeController}</code> and {@link
 * MultiModePainterWrapper} / {@link MultiModeEditorWrapper}. Depending on {@link
 * MultiModeController.Mode}, a different painter / editor is used.
 * There are 2 modes available:
 * <ul>
 *   <li>{@link MultiModeController.Mode#DEFAULT DEFAULT}</li>
 *   <li>{@link MultiModeController.Mode#ROTATION ROTATION}</li>
 * </ul>
 * <p/>
 * You can switch between modes by clicking on an already selected object. When a previously
 * unselected object is clicked, the controller's mode is reset to default.
 */
public class MainPanel extends GXYSample {

  private MultiModeController fMultiController = new MultiModeController();

  @Override
  protected Component[] createToolBars() {
    Component[] toolBars = super.createToolBars();
    ToolBar toolBar = (ToolBar) toolBars[0];
    toolBar.setGXYControllerEdit(fMultiController);
    getView().setGXYController(toolBar.getGXYCompositeEditController());
    return toolBars;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    ILcdGXYLayer lonLatPolygonLayer = createMultiEditModeLayer(createRotatableLonLatPolygonModel(), true);
    ILcdGXYLayer xyPolygonLayer = createMultiEditModeLayer(createRotatableXYPolygonModel(), false);

    GXYLayerUtil.addGXYLayer(getView(), lonLatPolygonLayer, false, false);
    GXYLayerUtil.addGXYLayer(getView(), xyPolygonLayer, true, false);

    GXYLayerUtil.fitGXYLayers(getView(), new ILcdGXYLayer[]{xyPolygonLayer, lonLatPolygonLayer});

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        getView().setScale(getView().getScale() / 2); // zooms out a bit after fitting
      }
    });
  }

  private ILcdGXYLayer createMultiEditModeLayer(ILcdModel aModel, boolean aLonLat) {

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel.getModelDescriptor().getDisplayName());

    // Regular painter/editor.
    TLcdGXYPointListPainter painterEditor = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.OUTLINED_FILLED);
    painterEditor.setLineStyle(ShapeGXYLayerFactory.LINE_STYLE);
    painterEditor.setFillStyle(ShapeGXYLayerFactory.FILL_STYLE);

    // Rotation painter/editor.
    RotationSupport rotationSupport = aLonLat ? new LonLatPolygonRotationSupport() : new XYPolygonRotationSupport();
    RotatingPainterWrapper rotationPainter = new RotatingPainterWrapper((ILcdGXYPainter) painterEditor, rotationSupport);
    RotatingEditorWrapper rotationEditor = new RotatingEditorWrapper((ILcdGXYEditor) painterEditor, rotationSupport);

    // Maps the painters onto MultiModeController's modes.
    EnumMap<MultiModeController.Mode, ILcdGXYPainter> painterMap = new EnumMap<MultiModeController.Mode, ILcdGXYPainter>(MultiModeController.Mode.class);
    painterMap.put(MultiModeController.Mode.DEFAULT, painterEditor);
    painterMap.put(MultiModeController.Mode.ROTATION, rotationPainter);

    EnumMap<MultiModeController.Mode, ILcdGXYEditor> editorMap = new EnumMap<MultiModeController.Mode, ILcdGXYEditor>(MultiModeController.Mode.class);
    editorMap.put(MultiModeController.Mode.DEFAULT, painterEditor);
    editorMap.put(MultiModeController.Mode.ROTATION, rotationEditor);

    layer.setGXYPainterProvider(new MultiModePainterWrapper(fMultiController, painterMap));
    layer.setGXYEditorProvider(new MultiModeEditorWrapper(fMultiController, editorMap));
    layer.setModel(aModel);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
    layer.setSelectable(true);
    layer.setEditable(true);

    return layer;
  }

  private ILcdModel createRotatableLonLatPolygonModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();

    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor("Sample code", "MyPolygon", "LonLat Polygon"));
    model.addElement(createLonLatPolygon(datum.getEllipsoid()), ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createRotatableXYPolygonModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();

    model.setModelReference(new TLcdGridReference(datum, new TLcdEquidistantCylindrical()));
    model.setModelDescriptor(new TLcdModelDescriptor("Sample code", "MyPolygon", "XY Polygon"));
    model.addElement(createXYPolygon(), ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private Object createXYPolygon() {
    ILcd2DEditablePoint[] pointArray = {
        new TLcdXYZPoint(589993.30120435, 5621634.285060315, 0.0),
        new TLcdXYZPoint(567729.4030456952, 5554842.590584352, 0.0),
        new TLcdXYZPoint(512069.65764905844, 5554842.590584352, 0.0),
        new TLcdXYZPoint(567729.4030456952, 5521446.743346369, 0.0),
        new TLcdXYZPoint(534333.5558077131, 5454655.048870405, 0.0),
        new TLcdXYZPoint(589993.30120435, 5499182.845187714, 0.0),
        new TLcdXYZPoint(645653.0466009867, 5454655.048870405, 0.0),
        new TLcdXYZPoint(612257.1993630046, 5521446.743346369, 0.0),
        new TLcdXYZPoint(667916.9447596414, 5554842.590584352, 0.0),
        new TLcdXYZPoint(612257.1993630046, 5554842.590584352, 0.0),
        };
    ILcd2DEditablePointList pointList = new TLcd2DEditablePointList(pointArray, false);
    return new XYPolygonWithRotation(pointList);
  }

  private Object createLonLatPolygon(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] pointArray = {
        new TLcdLonLatPoint(3.3, 50.5),
        new TLcdLonLatPoint(3.1, 49.9),
        new TLcdLonLatPoint(2.6, 49.9),
        new TLcdLonLatPoint(3.1, 49.6),
        new TLcdLonLatPoint(2.8, 49.0),
        new TLcdLonLatPoint(3.3, 49.4),
        new TLcdLonLatPoint(3.8, 49.0),
        new TLcdLonLatPoint(3.5, 49.6),
        new TLcdLonLatPoint(4.0, 49.9),
        new TLcdLonLatPoint(3.5, 49.9)
    };
    ILcd2DEditablePointList pointList = new TLcd2DEditablePointList(pointArray, false);
    return new LonLatPolygonWithRotation(pointList, aEllipsoid);
  }

  /**
   * Runs the multi-mode sample.
   */
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Multi-mode painter/editor");
  }
}
