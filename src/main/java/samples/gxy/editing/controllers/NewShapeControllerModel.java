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
/*
 *
 * Copyright (c) 1999-2007 Luciad NV All Rights Reserved.
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

package samples.gxy.editing.controllers;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdText;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightBuffer;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdLonLatHeightVariableGeoBuffer;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;

import samples.gxy.editing.ShapeGXYLayerFactory;

/**
 * This is an implementation of ALcdGXYNewControllerModel2 that helps a
 * TLcdGXYNewController2 to create shapes with mouse interactions.
 * @see com.luciad.view.gxy.controller.TLcdGXYNewController2#setNewControllerModel
 */
public class NewShapeControllerModel extends ALcdGXYNewControllerModel2 implements ILcdChangeSource {

  public static enum ShapeType {
    POLYLINE,
    RHUMBPOLYLINE,
    POLYGON,
    RHUMBPOLYGON,
    CIRCLE,
    CIRCLE_BY_3_POINTS,
    ARCBAND,
    GEO_BUFFER_POLYLINE,
    GEO_BUFFER_POLYGON,
    VARIABLE_GEO_BUFFER,
    POINT,
    TEXT,
    ARC,
    ARCBY3POINTS,
    ARCBYBULGE,
    ARCBYCENTERPOINT,
    BOUNDS,
    ELLIPSE,
    COMPOSITE_CURVE,
    COMPOSITE_RING,
  }

  // Determines whether to create geodetic shapes (lonlat-based) or grid (xy-based) shapes.
  private boolean fGeodetic = true;

  // Determines what shape to create.
  private final ShapeType fShapeType;

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();

  public NewShapeControllerModel(ShapeType aShapeType, ControllerSettingsNotifier aNotifier) {
    fShapeType = aShapeType;
    aNotifier.addListener(this);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  public void setGeodetic(boolean aGeodetic) {
    fGeodetic = aGeodetic;
    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }

  public void setSubCurveType(ShapeType aShapeType, ILcdCompositeCurve aObject, ILcdGXYLayer aGXYLayer) {
    // this model doesn't have sub-curves
  }

  @Override
  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    return createShapeForType(fShapeType, aGXYContext.getGXYLayer().getModel().getModelReference());
  }

  @Override
  public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    if (aContext.getGXYLayer() != null) {
      return aContext.getGXYLayer();
    }
    // Returns the topmost compatible layer.
    // Instead you could use the selected layer, or ask the user.
    ILcdGXYView view = aContext.getGXYView();
    for (int i = view.layerCount() - 1; i >= 0; i--) {
      ILcdGXYLayer layer = (ILcdGXYLayer) view.getLayer(i);
      if (isGXYLayerSupported(layer)) {
        return layer;
      }
    }
    return null;
  }

  public boolean isSupported() {
    // the XY shapes are a subset of the lon lat shapes, so if the controller model's not geodetic, we need to check
    // if the shape type is supported.
    return fGeodetic || createShapeForType(fShapeType, null) != null;
  }

  protected boolean isGXYLayerSupported(ILcdGXYLayer aLayer) {
    // Check if we can create our configured shape for the given layer.
    return ShapeGXYLayerFactory.isGXYLayerOfFormat(aLayer) &&
           (fGeodetic == aLayer.getModel().getModelReference() instanceof ILcdGeodeticReference) &&
           createShapeForType(fShapeType, aLayer.getModel().getModelReference()) != null;
  }

  protected Object createShapeForType(ShapeType aType, ILcdModelReference aReference) {

    // Lon-lat shapes
    if (aReference instanceof ILcdGeodeticReference) {
      ILcdGeoReference geo_model_reference = (ILcdGeoReference) aReference;
      ILcdEllipsoid ellipsoid = geo_model_reference.getGeodeticDatum().getEllipsoid();
      switch (aType) {
      case POLYLINE:
        return new TLcdLonLatPolyline(new My2DEditableLonLatPointList(), ellipsoid);
      case RHUMBPOLYLINE:
        return new TLcdLonLatRhumbPolyline(new My2DEditableLonLatPointList(), ellipsoid);
      case POLYGON:
        return new TLcdLonLatPolygon(new My2DEditableLonLatPointList(), ellipsoid);
      case RHUMBPOLYGON:
        return new TLcdLonLatRhumbPolygon(new My2DEditableLonLatPointList(), ellipsoid);
      case CIRCLE:
        return new TLcdLonLatCircle(new TLcdLonLatPoint(), 0, ellipsoid);
      case CIRCLE_BY_3_POINTS:
        return new TLcdLonLatCircleBy3Points(ellipsoid);
      case ARCBAND:
        return new TLcdLonLatArcBand(new TLcdLonLatPoint(), 0, 0, 0, 0, ellipsoid);
      case GEO_BUFFER_POLYLINE:
        return new TLcdLonLatGeoBuffer(new TLcdLonLatPolyline(new My2DEditableLonLatPointList(), ellipsoid), 0, ellipsoid);
      case GEO_BUFFER_POLYGON:
        return new TLcdLonLatGeoBuffer(new TLcdLonLatPolygon(new My2DEditableLonLatPointList(), ellipsoid), 0, ellipsoid);
      case VARIABLE_GEO_BUFFER:
        return new TLcdLonLatHeightVariableGeoBuffer(ellipsoid);
      case POINT:
        return new TLcdLonLatPoint();
      case TEXT:
        return new TLcdXYText("", new TLcdLonLatPoint(), 0.0, 0.0, ILcdText.ALIGNMENT_LEFT, ILcdText.ALIGNMENT_BOTTOM, 0.0);
      case ARC:
        return new TLcdLonLatArc();
      case ARCBY3POINTS:
        return new TLcdLonLatCircularArcBy3Points(ellipsoid);
      case ARCBYBULGE:
        return new TLcdLonLatCircularArcByBulge(ellipsoid);
      case ARCBYCENTERPOINT:
        return new TLcdLonLatCircularArcByCenterPoint(ellipsoid);
      case BOUNDS:
        return new TLcdLonLatBounds();
      case ELLIPSE:
        return new TLcdLonLatEllipse();
      case COMPOSITE_CURVE:
        return new TLcdCompositeCurve();
      case COMPOSITE_RING:
        return new TLcdLonLatCompositeRing(ellipsoid);
      }
    } else {
      // XY shapes
      switch (aType) {
      case POLYLINE:
        return new TLcdXYPolyline();
      case POLYGON:
        return new TLcdXYPolygon();
      case CIRCLE:
        return new TLcdXYCircle(new TLcdXYPoint(), 0);
      case CIRCLE_BY_3_POINTS:
        return new TLcdXYCircleBy3Points();
      case ARCBAND:
        return new TLcdXYArcBand(new TLcdXYPoint(), 0, 0, 0, 0);
      case GEO_BUFFER_POLYLINE:
        return new TLcdXYGeoBuffer(new TLcdXYPolyline(), 0);
      case GEO_BUFFER_POLYGON:
        return new TLcdXYGeoBuffer(new TLcdXYPolygon(), 0);
      case POINT:
        return new TLcdXYPoint();
      case TEXT:
        return new TLcdXYText("", new TLcdXYPoint(), 0.0, 0.0, ILcdText.ALIGNMENT_LEFT, ILcdText.ALIGNMENT_BOTTOM, 0.0);
      case ARC:
        return new TLcdXYArc();
      case ARCBY3POINTS:
        return new TLcdXYCircularArcBy3Points();
      case ARCBYBULGE:
        return new TLcdXYCircularArcByBulge();
      case ARCBYCENTERPOINT:
        return new TLcdXYCircularArcByCenterPoint();
      case BOUNDS:
        return new TLcdXYBounds();
      case ELLIPSE:
        return new TLcdXYEllipse();
      case COMPOSITE_CURVE:
        return new TLcdCompositeCurve();
      case COMPOSITE_RING:
        return new TLcdXYCompositeRing();
      }
    }
    return null;
  }

  protected static class My2DEditableLonLatPointList extends TLcd2DEditablePointList {
    // Overridden to make sure that new point are of type TLcdLonLatPoint (iso TLcdXYPoint)
    protected ILcd2DEditablePoint create2DEditablePoint(double aX, double aY) {
      return new TLcdLonLatPoint(aX, aY);
    }
  }


}
