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
package samples.lightspeed.nongeoreferenced;

import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdText;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.ILcd3DEditablePolyline;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.shape.shape3D.TLcdXYZPolyline;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * ALspCreateControllerModel for shape creation.
 */
public class CartesianCreateControllerModel extends ALspCreateControllerModel {

  /**
   * Enumeration of shapes that can be created using this creation controller model.
   */
  public static enum Type {
    POLYLINE("Polyline"),
    POLYGON("Polygon"),
    CIRCLE("Circle"),
    CIRCLE_BY_3_POINTS("Circle By 3 Points"),
    ELLIPSE("Ellipse"),
    POINT2D("2D Point"),
    ARC("Arc"),
    ARC_BY_3_POINTS("Arc By 3 Points"),
    ARC_BY_BULGE("Arc By Bulge"),
    ARC_BY_CENTER("Arc By Center Point"),
    ARCBAND("ArcBand"),
    GEOBUFFER("GeoBuffer"),
    BOUNDS("Bounds"),
    TEXT("Text"),
    POLYLINE_3D("3D Polyline"),
    COMPOSITECURVE("Composite Curve"),
    COMPOSITERING("Composite Ring"),
    COMPLEXPOLYGON("Complex Polygon");

    private String fName;

    Type(String aName) {
      fName = aName;
    }

    @Override
    public String toString() {
      return fName;
    }
  }

  // Controller model attributes
  private ILspInteractivePaintableLayer fLayer;
  private Type fType;
  private boolean fCreateExtrudedShape;

  public CartesianCreateControllerModel(Type aType, ILspInteractivePaintableLayer aLayer) {
    fLayer = aLayer;
    fType = aType;
    fCreateExtrudedShape = false;
  }

  public boolean isCreateExtrudedShape() {
    return fCreateExtrudedShape;
  }

  public void setCreateExtrudedShape(boolean aExtrudedMode) {
    fCreateExtrudedShape = aExtrudedMode;
  }

  public ILspInteractivePaintableLayer getLayer(ILspView aView) {
    return fLayer;
  }

  @Override
  public TLspPaintRepresentation getPaintRepresentation(ILspInteractivePaintableLayer aLayer, ILspView aView) {
    return TLspPaintRepresentation.BODY;
  }

  public Type getType() {
    return fType;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Based on this controller model's type, this method creates the corresponding shape.
   *
   * @param aView
   * @param aLayer
   */
  public Object create(ILspView aView, ILspLayer aLayer) {
    switch (fType) {
    case POINT2D:
      if (!isCreateExtrudedShape()) {
        return createXYPoint();
      } else {
        return createXYZPoint();
      }
    case POLYLINE:
      if (!isCreateExtrudedShape()) {
        return createXYPolyline();
      } else {
        return createExtrudedShape(createXYPolyline());
      }
    case POLYGON:
      if (!isCreateExtrudedShape()) {
        return createXYPolygon();
      } else {
        return createExtrudedShape(createXYPolygon());
      }
    case CIRCLE:
      if (!isCreateExtrudedShape()) {
        return createXYCircle();
      } else {
        return createExtrudedShape(createXYCircle());
      }
    case CIRCLE_BY_3_POINTS:
      if (!isCreateExtrudedShape()) {
        return createXYCircleBy3Points();
      } else {
        return createExtrudedShape(createXYCircleBy3Points());
      }
    case ELLIPSE:
      if (!isCreateExtrudedShape()) {
        return createXYEllipse();
      } else {
        return createExtrudedShape(createXYEllipse());
      }
    case ARCBAND:
      if (!isCreateExtrudedShape()) {
        return createXYArcBand();
      } else {
        return createExtrudedShape(createXYArcBand());
      }
    case ARC:
      if (!isCreateExtrudedShape()) {
        return createXYArc();
      } else {
        return createExtrudedShape(createXYArc());
      }
    case ARC_BY_3_POINTS:
      if (!isCreateExtrudedShape()) {
        return createXYArcBy3Points();
      } else {
        return createExtrudedShape(createXYArcBy3Points());
      }
    case ARC_BY_BULGE:
      if (!isCreateExtrudedShape()) {
        return createXYArcByBulge();
      } else {
        return createExtrudedShape(createXYArcByBulge());
      }
    case ARC_BY_CENTER:
      if (!isCreateExtrudedShape()) {
        return createXYArcByCenter();
      } else {
        return createExtrudedShape(createXYArcByCenter());
      }
    case GEOBUFFER:
      if (!isCreateExtrudedShape()) {
        return createXYGeoBuffer();
      } else {
        return createExtrudedShape(createXYGeoBuffer());
      }
    case BOUNDS:
      if (!isCreateExtrudedShape()) {
        return createXYBounds();
      } else {
        return createXYZBounds();
      }
    case TEXT:
      if (!isCreateExtrudedShape()) {
        return createText();
      } else {
        return createExtrudedShape(createText());
      }
    case POLYLINE_3D:
      return create3DPolyline();
    case COMPOSITECURVE:
      if (!isCreateExtrudedShape()) {
        return createCompositeCurve();
      } else {
        return createExtrudedShape(createCompositeCurve());
      }
    case COMPOSITERING:
      if (!isCreateExtrudedShape()) {
        return createCompositeRing();
      } else {
        return createExtrudedShape(createCompositeRing());
      }
    case COMPLEXPOLYGON:
      if (!isCreateExtrudedShape()) {
        return createComplexPolygon();
      } else {
        return createExtrudedShape(createComplexPolygon());
      }
    }
    return null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private TLcdXYPoint createXYPoint() {
    return new TLcdXYPoint();
  }

  private TLcdXYZPoint createXYZPoint() {
    return new TLcdXYZPoint();
  }

  private TLcdXYPolyline createXYPolyline() {
    return new TLcdXYPolyline();
  }

  private TLcdXYPolygon createXYPolygon() {
    return new TLcdXYPolygon();
  }

  private TLcdXYCircle createXYCircle() {
    return new TLcdXYCircle();
  }

  private TLcdXYCircleBy3Points createXYCircleBy3Points() {
    return new TLcdXYCircleBy3Points();
  }

  private TLcdXYEllipse createXYEllipse() {
    return new TLcdXYEllipse();
  }

  private TLcdXYArcBand createXYArcBand() {
    return new TLcdXYArcBand();
  }

  private TLcdXYArc createXYArc() {
    return new TLcdXYArc();
  }

  private TLcdXYCircularArcBy3Points createXYArcBy3Points() {
    return new TLcdXYCircularArcBy3Points();
  }

  private TLcdXYCircularArcByBulge createXYArcByBulge() {
    return new TLcdXYCircularArcByBulge();
  }

  private TLcdXYCircularArcByCenterPoint createXYArcByCenter() {
    return new TLcdXYCircularArcByCenterPoint();
  }

  private TLcdXYGeoBuffer createXYGeoBuffer() {
    return new TLcdXYGeoBuffer();
  }

  private TLcdXYBounds createXYBounds() {
    return new TLcdXYBounds();
  }

  private TLcdXYZBounds createXYZBounds() {
    return new TLcdXYZBounds();
  }

  private TLcdXYText createText() {
    return new TLcdXYText("Text", 0, 0, 3, 3, ILcdText.ALIGNMENT_LEFT, ILcdText.ALIGNMENT_TOP, 0);
  }

  private TLcdCompositeCurve createCompositeCurve() {
    return new TLcdCompositeCurve();
  }

  private TLcdCompositeCurve createCompositeRing() {
    return new TLcdXYCompositeRing();
  }

  private ILcdComplexPolygon createComplexPolygon() {
    return new TLcdComplexPolygon();
  }

  private ILcd3DEditablePolyline create3DPolyline() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      return new TLcdXYZPolyline();
    } else {
      return new TLcdXYZPolyline();
    }
  }

  private Object createExtrudedShape(ILcdShape aBase) {
    return new TLcdExtrudedShape(aBase);
  }
}
