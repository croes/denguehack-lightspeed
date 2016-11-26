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
package samples.lightspeed.common.controller;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdText;
import com.luciad.shape.ILcdVariableGeoBuffer;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.*;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * ALspCreateControllerModel for shape creation.
 */
public class LonLatCreateControllerModel extends ALspCreateControllerModel {

  /**
   * Enumeration of shapes that can be created using this creation controller model.
   */
  public enum Type {
    POLYLINE("Polyline"),
    RHUMB_POLYLINE("Rhumb Polyline"),
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
    ARC_BAND_3D("3D Arc Band"),
    BUFFER("Buffer"),
    GEOBUFFER("GeoBuffer"),
    BOUNDS("Bounds"),
    TEXT("Text"),
    LONLATHEIGHTBUFFER("LonLatHeightBuffer"),
    POLYLINE_3D("3D Polyline"),
    COMPOSITECURVE("Composite Curve"),
    COMPOSITERING("Composite Ring"),
    COMPLEXPOLYGON("Complex Polygon"),
    DOME("Dome"),
    SPHERE("Sphere"),
    VARIABLE_GEO_BUFFER("Variable Geo Buffer");

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

  public LonLatCreateControllerModel(Type aType, ILspInteractivePaintableLayer aLayer) {
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
        return createLonLatPoint();
      } else {
        return createLonLatHeightPoint();
      }
    case POLYLINE:
      if (!isCreateExtrudedShape()) {
        return createLonLatPolyline();
      } else {
        return createExtrudedShape(createLonLatPolyline());
      }
    case RHUMB_POLYLINE:
      if (!isCreateExtrudedShape()) {
        return createLonLatRhumbPolyline();
      } else {
        return createExtrudedShape(createLonLatRhumbPolyline());
      }
    case POLYGON:
      if (!isCreateExtrudedShape()) {
        return createLonLatPolygon();
      } else {
        return createExtrudedShape(createLonLatPolygon());
      }
    case CIRCLE:
      if (!isCreateExtrudedShape()) {
        return createLonLatCircle();
      } else {
        return createExtrudedShape(createLonLatCircle());
      }
    case CIRCLE_BY_3_POINTS:
      if (!isCreateExtrudedShape()) {
        return createLonLatCircleBy3Points();
      } else {
        return createExtrudedShape(createLonLatCircleBy3Points());
      }
    case ELLIPSE:
      if (!isCreateExtrudedShape()) {
        return createLonLatEllipse();
      } else {
        return createExtrudedShape(createLonLatEllipse());
      }
    case ARCBAND:
      if (!isCreateExtrudedShape()) {
        return createLonLatArcBand();
      } else {
        return createExtrudedShape(createLonLatArcBand());
      }
    case ARC_BAND_3D:
        return create3DArcBand();
    case ARC:
      if (!isCreateExtrudedShape()) {
        return createLonLatArc();
      } else {
        return createExtrudedShape(createLonLatArc());
      }
    case ARC_BY_3_POINTS:
      if (!isCreateExtrudedShape()) {
        return createLonLatArcBy3Points();
      } else {
        return createExtrudedShape(createLonLatArcBy3Points());
      }
    case ARC_BY_BULGE:
      if (!isCreateExtrudedShape()) {
        return createLonLatArcByBulge();
      } else {
        return createExtrudedShape(createLonLatArcByBulge());
      }
    case ARC_BY_CENTER:
      if (!isCreateExtrudedShape()) {
        return createLonLatArcByCenter();
      } else {
        return createExtrudedShape(createLonLatArcByCenter());
      }
    case BUFFER:
      if (!isCreateExtrudedShape()) {
        return createLonLatBuffer();
      } else {
        return createExtrudedShape(createLonLatBuffer());
      }
    case GEOBUFFER:
      if (!isCreateExtrudedShape()) {
        return createLonLatGeoBuffer();
      } else {
        return createExtrudedShape(createLonLatGeoBuffer());
      }
    case VARIABLE_GEO_BUFFER:
      return createVariableGeoBuffer();
    case BOUNDS:
      if (!isCreateExtrudedShape()) {
        return createLonLatBounds();
      } else {
        return createLonLatHeightBounds();
      }
    case TEXT:
      if (!isCreateExtrudedShape()) {
        return createText(aView);
      } else {
        return createExtrudedShape(createText(aView));
      }
    case LONLATHEIGHTBUFFER:
      return createLonLatHeightBuffer();
    case POLYLINE_3D:
      return create3DPolyline();
    case DOME:
      return createLonLatHeightDome();
    case SPHERE:
      return createLonLatHeightSphere();
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

  private TLcdLonLatPoint createLonLatPoint() {
    return new TLcdLonLatPoint();
  }

  private TLcdLonLatHeightPoint createLonLatHeightPoint() {
    return new TLcdLonLatHeightPoint();
  }

  private TLcdLonLatPolyline createLonLatPolyline() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatPolyline result = new TLcdLonLatPolyline();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatRhumbPolyline createLonLatRhumbPolyline() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatRhumbPolyline result = new TLcdLonLatRhumbPolyline();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatPolygon createLonLatPolygon() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatPolygon result = new TLcdLonLatPolygon();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatCircle createLonLatCircle() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatCircle result = new TLcdLonLatCircle();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatCircleBy3Points createLonLatCircleBy3Points() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatCircleBy3Points result = new TLcdLonLatCircleBy3Points();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatEllipse createLonLatEllipse() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatEllipse result = new TLcdLonLatEllipse();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatArcBand createLonLatArcBand() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatArcBand result = new TLcdLonLatArcBand();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private ILcd3DEditableArcBand create3DArcBand() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatHeight3DArcBand arcBand = new TLcdLonLatHeight3DArcBand();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      arcBand.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    //start with a nice default value for the pitch arc angle
    arcBand.setPitchArcAngle(30.0d);
    return arcBand;
  }

  private TLcdLonLatArc createLonLatArc() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatArc result = new TLcdLonLatArc();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatCircularArcBy3Points createLonLatArcBy3Points() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatCircularArcBy3Points result = new TLcdLonLatCircularArcBy3Points();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatCircularArcByBulge createLonLatArcByBulge() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatCircularArcByBulge result = new TLcdLonLatCircularArcByBulge();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatCircularArcByCenterPoint createLonLatArcByCenter() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatCircularArcByCenterPoint result = new TLcdLonLatCircularArcByCenterPoint();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatBuffer createLonLatBuffer() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatBuffer result = new TLcdLonLatBuffer();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatGeoBuffer createLonLatGeoBuffer() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    TLcdLonLatGeoBuffer result = new TLcdLonLatGeoBuffer();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      result.setEllipsoid(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return result;
  }

  private TLcdLonLatBounds createLonLatBounds() {
    return new TLcdLonLatBounds();
  }

  private TLcdLonLatHeightBounds createLonLatHeightBounds() {
    return new TLcdLonLatHeightBounds();
  }

  private TLcdXYText createText(ILspView aView) {
    Component parent = aView instanceof ILspAWTView ? ((ILspAWTView) aView).getHostComponent() : null;
    String text = JOptionPane.showInputDialog(
        parent,
        "Please type the text to place on the map.",
        "Create a text object",
        JOptionPane.PLAIN_MESSAGE);
    return new TLcdXYText(text, 0, 0, 3, 3, ILcdText.ALIGNMENT_LEFT, ILcdText.ALIGNMENT_TOP, 0);
  }

  private TLcdCompositeCurve createCompositeCurve() {
    return new TLcdCompositeCurve();
  }

  private TLcdCompositeCurve createCompositeRing() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      return new TLcdLonLatCompositeRing(geodeticReference.getGeodeticDatum().getEllipsoid());
    }
    return null;
  }

  private ILcdComplexPolygon createComplexPolygon() {
    return new TLcdComplexPolygon();
  }

  private TLcdLonLatHeightBuffer createLonLatHeightBuffer() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      ILcdEllipsoid ell = geodeticReference.getGeodeticDatum().getEllipsoid();
      return new TLcdLonLatHeightBuffer(new TLcdLonLatHeightPolyline(), 1e5, 1e5, ell);
    } else {
      // TLcdLonLatHeightBuffer cannot be used in Cartesian model references.
      return null;
    }
  }

  private ILcdVariableGeoBuffer createVariableGeoBuffer() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodeticReference = (ILcdGeodeticReference) ref;
      ILcdEllipsoid ellipsoid = geodeticReference.getGeodeticDatum().getEllipsoid();
      return new TLcdLonLatHeightVariableGeoBuffer(ellipsoid);
    } else {
      return null;
    }
  }

  private ILcd3DEditablePolyline create3DPolyline() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      return new TLcdLonLatHeightPolyline();
    } else {
      return new TLcdXYZPolyline();
    }
  }

  private TLcdLonLatHeightDome createLonLatHeightDome() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      return new TLcdLonLatHeightDome(new TLcdLonLatCircle(), 1000.0);
    } else {
      // TLcdLonLatHeightDome cannot be used in Cartesian model references.
      return null;
    }
  }

  private TLcdLonLatHeightSphere createLonLatHeightSphere() {
    ILcdModelReference ref = fLayer.getModel().getModelReference();
    if (ref instanceof ILcdGeodeticReference) {
      return new TLcdLonLatHeightSphere(new TLcdLonLatCircle(), 1000.0);
    } else {
      // TLcdLonLatHeightDome cannot be used in Cartesian model references.
      return null;
    }
  }

  private Object createExtrudedShape(ILcdShape aBase) {
    return new TLcdExtrudedShape(aBase);
  }
}
