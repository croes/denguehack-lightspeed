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
package samples.encoder.kml22.converter;

import java.util.ArrayList;
import java.util.List;

import com.luciad.format.kml22.model.geometry.TLcdKML22AbstractGeometry;
import com.luciad.format.object3d.TLcd3DPrimitiveType;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.projection.ILcdAzimuthal;
import com.luciad.projection.ILcdConic;
import com.luciad.projection.ILcdCylindrical;
import com.luciad.projection.ILcdObliqueCylindrical;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.ILcdTransverseCylindrical;
import com.luciad.projection.TLcdPolarStereographic;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.*;
import com.luciad.shape.shape3D.*;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspOffscreenView;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DPrimitive;
import com.luciad.view.lightspeed.geometry.discretization.ALspEditable3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationMode;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Extension of {@link KML22ShapeConverter} to support extra shapes. To do so, it uses {@link TLspShapeDiscretizer} to
 * transform shapes into polygon. The discretization depends on a view and its world reference, <code>KML22ShapeConverter</code>
 * creates its own view and allow you to set the world reference.
 */
public class LspKML22ShapeConverter extends KML22ShapeConverter {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LspKML22ShapeConverter.class);

  private TLspShapeDiscretizer fShapeDiscretizer = new TLspShapeDiscretizer();
  private TLspOffscreenView fOffscreenView;
  private TLspContext fContext;

  /**
   * Creates a new instance of <code>LspKML22ShapeConverter</code>.
   *
   * @param aLayer a layer for the model to be converted
   */
  public LspKML22ShapeConverter(ILspLayer aLayer) {
    fOffscreenView = new TLspOffscreenView(1, 1);
    fOffscreenView.addLayer(aLayer);
    fContext = new TLspContext(aLayer, fOffscreenView);
  }

  /**
   * Returns the world reference of the <code>LspKML22ShapeConverter</code>'s view.
   *
   * @return a world reference.
   */
  public ILcdXYZWorldReference getXYZWorldReference() {
    return fOffscreenView.getXYZWorldReference();
  }

  /**
   * Set a world reference that is also a {@link ILcdGridReference} to the <code>LspKML22ShapeConverter</code>'s view.
   * Note that the center of the projection can be changed during discretization.
   *
   * @param aXYZWorldReference a world reference
   * @throws IllegalArgumentException if the world reference is not a grid reference.
   */
  public void setXYZWorldReference(ILcdXYZWorldReference aXYZWorldReference) {
    if (!(aXYZWorldReference instanceof ILcdGridReference)) {
      throw new IllegalArgumentException("Only grid reference are supported");
    }
    fOffscreenView.setXYZWorldReference(aXYZWorldReference);
  }

  /**
   * Destroy the view of the <code>LspKML22ShapeConverter</code>. This instance will not be able to convert shape anymore.
   */
  public void dispose() {
    fOffscreenView.destroy();
    fContext = null;
    fOffscreenView = null;
  }

  @Override
  protected boolean canConvertShape(ILcdShape aShape) {
    return super.canConvertShape(aShape)
           || aShape instanceof ILcdArc
           || aShape instanceof ILcdEllipse
           || aShape instanceof ILcdBounds
           || aShape instanceof ILcdArcBand
           || (aShape instanceof ILcdGeoBuffer && !(aShape instanceof ILcdVariableGeoBuffer));
  }

  @Override
  public TLcdKML22AbstractGeometry convertIntoShape(ILcdModel aModel, Object aObject) {
    if (!canConvertIntoShape(aModel, aObject)) {
      throw new IllegalArgumentException("Cannot convert object into shape.");
    }
    if (fOffscreenView == null) {
      throw new RuntimeException("The view was previously dispoded");
    }
    if (fContext.getModel() != aModel) {
      throw new IllegalArgumentException("The given model isn't the same as the layer one");
    }

    TLcdKML22AbstractGeometry geometry = super.canConvertShape((ILcdShape) aObject) ? super.convertIntoShape(aModel, aObject) : null;
    if (geometry == null) {
      ILcdShape discretizedShape = discretize(aObject);
      return discretizedShape != null ? convertIntoShape(aModel, discretizedShape) : null;
    }
    return geometry;
  }

  private ILcdShape discretize(Object aObject) {
    if (fOffscreenView == null) {
      throw new IllegalStateException("This shape converter has been disposed.");
    }
    ILcdShape shape = (ILcdShape) aObject;
    if (shape instanceof ILcdExtrudedShape) {
      ILcdShape baseShape = discretize(((ILcdExtrudedShape) shape).getBaseShape());
      if (baseShape == null) {
        return null;
      }
      TLcdExtrudedShape extrudedShape = new TLcdExtrudedShape(baseShape,
                                                              ((ILcdExtrudedShape) shape).getMinimumZ(),
                                                              ((ILcdExtrudedShape) shape).getMaximumZ());
      return extrudedShape;
    } else if (shape instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) shape;
      TLcdShapeList resultShapeList = new TLcdShapeList();
      for (int i = 0; i < shapeList.getShapeCount(); i++) {
        ILcdShape subShape = shapeList.getShape(i);
        // Some subshapes can be directly converted without discretization
        if (super.canConvertShape(subShape)) {
          resultShapeList.addShape(subShape);
        } else {
          ILcdShape discretizedSubShape = discretize(subShape);
          if (discretizedSubShape != null) {
            resultShapeList.addShape(discretizedSubShape);
          } else {
            return null;
          }
        }
      }
      return resultShapeList;
    }
    TLspShapeDiscretizationParameters.Builder parameterBuilder = new TLspShapeDiscretizationParameters.Builder();
    parameterBuilder.modes(TLspShapeDiscretizationMode.OUTLINE);
    parameterBuilder.allowPrimitiveType(TLcd3DPrimitiveType.LINES);

    ALspEditable3DMesh mesh = new Float3DMesh();
    try {
      recenter(shape);
      fShapeDiscretizer.discretizeSFCT(aObject, parameterBuilder.build(), fContext, mesh);
      return convertMeshToShape(mesh, fContext, aObject);
    } catch (TLspDiscretizationException e) {
      sLogger.error("Couldn't discretize the shape", e);
      return null;
    } catch (TLcdOutOfBoundsException e) {
      sLogger.error("Couldn't discretize the shape", e);
      return null;
    }
  }

  private void recenter(ILcdShape aShape) {

    double lon = aShape.getFocusPoint().getX();
    double lat = aShape.getFocusPoint().getY();

    if (fOffscreenView.getXYZWorldReference() instanceof ILcdGridReference) {
      ILcdGridReference gridRef = (ILcdGridReference) fOffscreenView.getXYZWorldReference();
      ILcdProjection projection = gridRef.getProjection();
      ILcdGeodeticDatum geodeticDatum = gridRef.getGeodeticDatum();
      projection = (ILcdProjection) projection.clone();
      geodeticDatum = (ILcdGeodeticDatum) geodeticDatum.clone();
      TLcdGridReference newGridRef =
          new TLcdGridReference(geodeticDatum, projection,
                                gridRef.getFalseEasting(),
                                gridRef.getFalseNorthing(),
                                gridRef.getScale(),
                                gridRef.getUnitOfMeasure(),
                                gridRef.getRotation());

      // We now look what kind of projection it is and set it up depending on
      // its type
      if (projection instanceof ILcdAzimuthal) {
        ILcdAzimuthal azimuthal = (ILcdAzimuthal) projection;
        azimuthal.setOriginLon(lon);
        azimuthal.setOriginLat(lat);
      } else if (projection instanceof ILcdCylindrical) {
        ILcdCylindrical cylindrical = (ILcdCylindrical) projection;
        cylindrical.setCentralMeridian(lon);
      } else if (projection instanceof ILcdTransverseCylindrical) {
        ILcdTransverseCylindrical transverse_cylindrival = (ILcdTransverseCylindrical) projection;
        transverse_cylindrival.setCentralMeridian(lon);
        transverse_cylindrival.setOriginLat(lat);
      } else if (projection instanceof ILcdObliqueCylindrical) {
        ILcdObliqueCylindrical oblique_cylindrical = (ILcdObliqueCylindrical) projection;
        oblique_cylindrical.setCentralMeridian(lon);
        oblique_cylindrical.setStandardParallel(lat);
      } else if (projection instanceof ILcdConic) {
        ILcdConic conic = (ILcdConic) projection;
        conic.setOriginLon(lon);
        conic.setOriginLat(lat);
      } else if (projection instanceof TLcdPolarStereographic) {
        ((TLcdPolarStereographic) projection).setCentralMeridian(lon);
      } else {
        throw new RuntimeException("Unknown projection.");
      }
      fOffscreenView.setXYZWorldReference(newGridRef);
    }
  }

  private static class Float3DMesh extends ALspEditable3DMesh {

    List<ILcd3DEditablePoint> fVertices = new ArrayList<>();
    List<ALsp3DPrimitive> fPrimitives = new ArrayList<>();

    @Override
    public void addPrimitive(ALsp3DPrimitive aPrimitive) {
      fPrimitives.add(aPrimitive);
    }

    @Override
    public int addVertices(int aNbVertices) {
      int previousSize = fVertices.size();
      for (int i = 0; i < aNbVertices; i++) {
        fVertices.add(new TLcdXYZFloatPoint());
      }
      return previousSize;
    }

    @Override
    public void setPosition(int aIndex, ILcdPoint aVertex) {
      fVertices.get(aIndex).move3D(aVertex);
    }

    @Override
    public int getVertexCount() {
      return fVertices.size();
    }

    @Override
    public void getPositionSFCT(int aIndex, ILcd3DEditablePoint aPositionSFCT) {
      aPositionSFCT.move3D(fVertices.get(aIndex));
    }

    @Override
    public int getPrimitiveCount() {
      return fPrimitives.size();
    }

    @Override
    public ALsp3DPrimitive getPrimitive(int aIndex) {
      return fPrimitives.get(aIndex);
    }

    @Override
    public ILcdBounds getBounds() {
      return null;
    }
  }

  private ILcdShape convertMeshToShape(ALsp3DMesh aMesh, TLspContext aContext, Object aObject) throws TLcdOutOfBoundsException {

    TLcdXYZPoint tempPoint = new TLcdXYZPoint();
    TLcdXYZPoint tempPoint2 = new TLcdXYZPoint();

    ILcd3DEditablePointList pointList = aContext.getModelReference() instanceof ILcdGeodeticReference ? new TLcdLonLatHeightPolypoint()
                                                                                                      : new TLcdXYZPolypoint();

    ALsp3DPrimitive primitive = aMesh.getPrimitive(0);

    for (int j = 0; j < primitive.getElementCount(); j++) {
      aMesh.getPositionSFCT(primitive.getElement(j), tempPoint2);
      aContext.getModelXYZWorldTransformation().worldPoint2modelSFCT(tempPoint2, tempPoint);
      pointList.insert3DPoint(j, tempPoint.getX(), tempPoint.getY(), tempPoint.getZ());
    }

    return getShape(aObject, aContext, pointList);
  }

  private ILcdShape getShape(Object aObject, TLspContext aContext, ILcd3DEditablePointList aPointList) {
    if (aObject instanceof ILcdArc) {
      return aContext.getModelReference() instanceof ILcdGeodeticReference ?
             new TLcdLonLatHeightPolyline(aPointList) : new TLcdXYZPolyline(aPointList);
    } else {
      return aContext.getModelReference() instanceof ILcdGeodeticReference ?
             new TLcdLonLatHeightPolygon(aPointList) : new TLcdXYZPolygon(aPointList);
    }
  }
}
