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
package samples.gxy.decoder.gml;

import java.awt.Graphics;

import com.luciad.format.gmlcommon.xml.TLcdGMLModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.shape.*;
import com.luciad.shape.constraints.TLcdDefaultCurveConnectorProvider;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.*;

import samples.common.MapColors;
import samples.gxy.common.labels.GXYLabelPainterFactory;
import samples.gxy.decoder.MapSupport;

/**
 * An <code>ILcdGXYLayerFactory</code> that creates layers for GML models. This layer factory can be
 * used for GML2, GML31 and GML32 models.
 */
@LcdService
public class GMLLayerFactory implements ILcdGXYLayerFactory {

  private boolean fIsEditable;

  public GMLLayerFactory() {
    this(false);
  }

  public GMLLayerFactory(boolean aIsEditable) {
    fIsEditable = aIsEditable;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdGMLModelDescriptor) {
      if (aModel instanceof ILcdModelTreeNode) {
        return createGXYLayerTreeNode((ILcdModelTreeNode) aModel);
      } else {
        return createLeafGXYLayer(aModel);
      }
    }
    return null;
  }

  public ILcdGXYLayer createGXYLayerTreeNode(ILcdModelTreeNode aModel) {
    TLcdGXYLayerTreeNode gxy_layer = new GMLLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
    configureLayer(gxy_layer, aModel);
    for (int i = 0; i < aModel.modelCount(); i++) {
      gxy_layer.addLayer(createGXYLayer(aModel.getModel(i)));
    }
    return gxy_layer;
  }

  public ILcdGXYLayer createLeafGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new GMLLayer();
    configureLayer(gxy_layer, aModel);
    return gxy_layer;
  }

  private void configureLayer(TLcdGXYLayer aLayer, ILcdModel aModel) {
    aLayer.setModel(aModel);
    aLayer.setLabel(aModel.getModelDescriptor().getDisplayName());
    aLayer.setEditable(fIsEditable);
    aLayer.setLabeled(true);
    aLayer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
    aLayer.setGXYLabelPainterProvider(GXYLabelPainterFactory.createGXYLabelPainter(aModel, false));
  }

  private class GMLLayer extends TLcdGXYLayer {

    private GML3PainterProvider fGMLPainter = new GML3PainterProvider();

    public GMLLayer() {
      setGXYPainterProvider(fGMLPainter);
      if (fIsEditable) {
        setGXYEditorProvider(fGMLPainter);
      }
    }

    public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
      if ((aMode & ILcdGXYLayer.BODIES) != 0) {
        // User the mode to ensure a proper paint order
        fGMLPainter.setMode(GML3PainterProvider.AREAS);
        super.paint(aGraphics, aMode, aGXYView);
        fGMLPainter.setMode(GML3PainterProvider.LINES);
        super.paint(aGraphics, aMode, aGXYView);
        fGMLPainter.setMode(GML3PainterProvider.POINTS);
        super.paint(aGraphics, aMode, aGXYView);
        // Set the mode again to default (ex. for selection)
        fGMLPainter.setMode(GML3PainterProvider.ALL);
      } else {
        super.paint(aGraphics, aMode, aGXYView);
      }
    }

  }

  private static class GMLLayerTreeNode extends TLcdGXYLayerTreeNode {

    private GML3PainterProvider fGMLPainter = new GML3PainterProvider();

    private GMLLayerTreeNode(String aLabel) {
      super(aLabel);
      setGXYPainterProvider(fGMLPainter);
    }

    public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
      if ((aMode & ILcdGXYLayer.BODIES) != 0) {
        // User the mode to ensure a proper paint order
        fGMLPainter.setMode(GML3PainterProvider.AREAS);
        super.paint(aGraphics, aMode, aGXYView);
        fGMLPainter.setMode(GML3PainterProvider.LINES);
        super.paint(aGraphics, aMode, aGXYView);
        fGMLPainter.setMode(GML3PainterProvider.POINTS);
        super.paint(aGraphics, aMode, aGXYView);
        // Set the mode again to default (ex. for selection)
        fGMLPainter.setMode(GML3PainterProvider.ALL);
      }
    }
  }

  public static class GML3PainterProvider implements ILcdGXYPainterProvider, ILcdGXYEditorProvider {

    public static final int ALL = 0;
    public static final int POINTS = 1;
    public static final int LINES = 2;
    public static final int AREAS = 3;

    private ILcdGXYPainterStyle fLineStyle = new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_OUTLINE, MapColors.SELECTION);
    private ILcdGXYPainterStyle fFillStyle = new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_FILL, MapColors.SELECTION);
    private int fMode;

    private TLcdGXYShapeListPainter fShapeListPainter;
    private TLcdGXYIconPainter fIconPainter;
    private TLcdGXYPointListPainter fPointListPainter;
    private TLcdGXYCircleBy3PointsPainter fCircleBy3PointsPainter;
    private TLcdGXYCirclePainter fCirclePainter;
    private TLcdGXYCircularArcPainter fCircularArcPainter;
    private TLcdGXYCompositeRingPainter fCompositeRingPainter;
    private TLcdGXYCompositeCurvePainter fCompositeCurvePainter;
    private TLcdGXYSurfacePainter fSurfacePainter;

    public ILcdGXYPainterStyle getLineStyle() {
      return fLineStyle;
    }

    public void setLineStyle(ILcdGXYPainterStyle aLineStyle) {
      fLineStyle = aLineStyle;
    }

    public ILcdGXYPainterStyle getFillStyle() {
      return fFillStyle;
    }

    public void setFillStyle(ILcdGXYPainterStyle aFillStyle) {
      fFillStyle = aFillStyle;
    }

    void setMode(int aMode) {
      fMode = aMode;
    }

    public ILcdGXYEditor getGXYEditor(Object aObject) {
      ILcdGXYPainter painter = getGXYPainter(aObject);
      if (painter instanceof ILcdGXYEditor) {
        return (ILcdGXYEditor) painter;
      } else {
        return null;
      }
    }

    public ILcdGXYPainter getGXYPainter(Object aObject) {
      if (aObject instanceof ILcdShapeList) {
        if (fShapeListPainter == null) {
          fShapeListPainter = new TLcdGXYShapeListPainter();
        }
        GML3PainterProvider gml3PainterProvider = createPainterProvider();
        fShapeListPainter.setShapeGXYPainterProvider(gml3PainterProvider);
        fShapeListPainter.setShapeGXYEditorProvider(gml3PainterProvider);
        fShapeListPainter.setObject(aObject);
        return fShapeListPainter;
      } else if (aObject instanceof ILcdPoint && (fMode == POINTS || fMode == ALL)) {
        if (fIconPainter == null) {
          fIconPainter = new TLcdGXYIconPainter();
          fIconPainter.setIcon(MapColors.createIcon(false));
          fIconPainter.setSelectionIcon(MapColors.createIcon(true));
        }
        fIconPainter.setObject(aObject);
        return fIconPainter;
      } else if (aObject instanceof ILcdPolyline && (fMode == LINES || fMode == ALL)) {
        if (fPointListPainter == null) {
          fPointListPainter = new TLcdGXYPointListPainter();
        }
        fPointListPainter.setLineStyle(getLineStyle());
        fPointListPainter.setMode(TLcdGXYPointListPainter.POLYLINE);
        fPointListPainter.setObject(aObject);
        return fPointListPainter;
      } else if (aObject instanceof ILcdPolygon && (fMode == AREAS || fMode == ALL)) {
        if (fPointListPainter == null) {
          fPointListPainter = new TLcdGXYPointListPainter();
        }
        fPointListPainter.setLineStyle(getLineStyle());
        fPointListPainter.setFillStyle(getFillStyle());
        fPointListPainter.setMode(TLcdGXYPointListPainter.OUTLINED_FILLED);
        fPointListPainter.setObject(aObject);
        return fPointListPainter;
      } else if (aObject instanceof ILcdCircleBy3Points && (fMode == AREAS || fMode == ALL)) {
        if (fCircleBy3PointsPainter == null) {
          fCircleBy3PointsPainter = new TLcdGXYCircleBy3PointsPainter();
        }
        fCircleBy3PointsPainter.setLineStyle(getLineStyle());
        fCircleBy3PointsPainter.setFillStyle(getFillStyle());
        fCircleBy3PointsPainter.setMode(ALcdGXYAreaPainter.OUTLINED_FILLED);
        fCircleBy3PointsPainter.setObject(aObject);
        return fCircleBy3PointsPainter;
      } else if (aObject instanceof ILcdCircle && (fMode == AREAS || fMode == ALL)) {
        if (fCirclePainter == null) {
          fCirclePainter = new TLcdGXYCirclePainter();
        }
        fCirclePainter.setLineStyle(getLineStyle());
        fCirclePainter.setFillStyle(getFillStyle());
        fCirclePainter.setMode(ALcdGXYAreaPainter.OUTLINED_FILLED);
        fCirclePainter.setObject(aObject);
        return fCirclePainter;
      } else if (aObject instanceof ILcdCircularArc && (fMode == LINES || fMode == ALL)) {
        if (fCircularArcPainter == null) {
          fCircularArcPainter = new TLcdGXYCircularArcPainter();
        }
        fCircularArcPainter.setLineStyle(getLineStyle());
        fCircularArcPainter.setObject(aObject);
        return fCircularArcPainter;
      } else if (aObject instanceof ILcdCompositeCurve && aObject instanceof ILcdRing && (fMode == AREAS || fMode == ALL)) {
        if (fCompositeRingPainter == null) {
          fCompositeRingPainter = new TLcdGXYCompositeRingPainter();
        }
        fCompositeRingPainter.setLineStyle(getLineStyle());
        fCompositeRingPainter.setFillStyle(getFillStyle());
        fCompositeRingPainter.setMode(TLcdGXYPointListPainter.OUTLINED_FILLED);
        fCompositeRingPainter.setCurveConnectorProvider(new TLcdDefaultCurveConnectorProvider());
        GML3PainterProvider curvePainterProvider = createPainterProvider();
        if (fMode == AREAS) {
          curvePainterProvider.setMode(LINES);
        }
        fCompositeRingPainter.setCurveGXYPainterProvider(curvePainterProvider);
        fCompositeRingPainter.setCurveGXYEditorProvider(curvePainterProvider);
        fCompositeRingPainter.setObject(aObject);
        return fCompositeRingPainter;
      } else if (aObject instanceof ILcdCompositeCurve && (fMode == LINES || fMode == ALL)) {
        if (fCompositeCurvePainter == null) {
          fCompositeCurvePainter = new TLcdGXYCompositeCurvePainter();
        }
        fCompositeCurvePainter.setCurveConnectorProvider(new TLcdDefaultCurveConnectorProvider());
        GML3PainterProvider gml3PainterProvider = createPainterProvider();
        fCompositeCurvePainter.setCurveGXYPainterProvider(gml3PainterProvider);
        fCompositeCurvePainter.setCurveGXYEditorProvider(gml3PainterProvider);
        fCompositeCurvePainter.setObject(aObject);
        return fCompositeCurvePainter;
      } else if (aObject instanceof ILcdSurface && (fMode == AREAS || fMode == ALL)) {
        if (fSurfacePainter == null) {
          fSurfacePainter = new TLcdGXYSurfacePainter();
        }
        fSurfacePainter.setLineStyle(getLineStyle());
        fSurfacePainter.setFillStyle(getFillStyle());
        fSurfacePainter.setMode(TLcdGXYPointListPainter.OUTLINED_FILLED);
        fSurfacePainter.setCurveConnectorProvider(new TLcdDefaultCurveConnectorProvider());
        fSurfacePainter.setRingGXYPainterProvider(createPainterProvider());
        fSurfacePainter.setObject(aObject);
        return fSurfacePainter;
      } else {
        return null;
      }
    }

    private GML3PainterProvider createPainterProvider() {
      return clone();
    }

    public GML3PainterProvider clone() {
      try {
        GML3PainterProvider clone = (GML3PainterProvider) super.clone();
        clone.fPointListPainter = null;
        clone.fIconPainter = null;
        clone.fShapeListPainter = null;
        clone.fCircleBy3PointsPainter = null;
        clone.fCirclePainter = null;
        clone.fCircularArcPainter = null;
        clone.fCompositeRingPainter = null;
        clone.fCompositeCurvePainter = null;
        clone.fSurfacePainter = null;
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new UnsupportedOperationException();
      }
    }
  }
}
