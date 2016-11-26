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
package samples.common.model;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdSurface;
import com.luciad.shape.TLcdComplexPolygon;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.TLcdSurface;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatCircularArcBy3Points;
import com.luciad.shape.shape2D.TLcdLonLatCompositeRing;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.util.ILcdFireEventMode;

public class GeodeticModelFactory {

  public static final String SHAPES_MODEL_NAME = "Shapes";

  public static boolean isModelOfFormat(ILcdModel aModel) {
    return "Shapes".equals(aModel.getModelDescriptor().getTypeName());
  }

  public static ILcdModel createEmptyModel() {
    // We create a longitude/latitude model, based on WGS-84 (the default TLcdGeodeticDatum),
    // and add some default shapes.

    TLcdGeodeticDatum geodeticDatum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference(geodeticDatum);
    TLcdVectorModel model = new TLcdVectorModel(modelReference);

    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the newly created shapes.", // source name (is used as tooltip text)
        "Shapes", // type name
        "Geodetic Shapes"  // display name
    ));
    return model;
  }

  public static ILcdModel createModel() {
    ILcdModel model = createEmptyModel();
    ShapeFactory.createShapes(model, getDatum(model), false);
    return model;
  }

  public ILcdModel createSimpleModel() {
    ILcdModel model = createEmptyModel();
    ILcdGeodeticDatum geodeticDatum = ((ILcdGeodeticReference) model.getModelReference()).getGeodeticDatum();
    model.addElement(createLonLatBounds(), ILcdFireEventMode.NO_EVENT);
    model.addElement(createLonLatPolygon(geodeticDatum.getEllipsoid()), ILcdFireEventMode.NO_EVENT);
    model.addElement(createLonLatSurface(geodeticDatum.getEllipsoid()), ILcdFireEventMode.NO_EVENT);
    model.addElement(createLonLatComplexPolygon(geodeticDatum.getEllipsoid()), ILcdModel.NO_EVENT);
    model.addElement(createLonLatShapeList(geodeticDatum.getEllipsoid()), ILcdModel.NO_EVENT);
    return model;
  }

  /**
   * Retrieves the geodetic datum from the given model. If the given model does not have a geodetic
   * model reference, an exception will be thrown.
   */
  public static ILcdGeodeticDatum getDatum(ILcdModel aModel) {
    ILcdModelReference modelRef = aModel.getModelReference();
    if (modelRef instanceof ILcdGeodeticReference) {
      return ((ILcdGeodeticReference) modelRef).getGeodeticDatum();
    } else {
      throw new IllegalArgumentException("Could not retrieve datum, given model does not have a geodetic reference!");
    }
  }


  private Object createLonLatBounds() {
    return new TLcdLonLatBounds(-62, 36, 3, 3);
  }

  private Object createLonLatPolygon(ILcdEllipsoid aEllipsoid) {
    TLcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
            new TLcdLonLatPoint(-74.0, 35.0),
            new TLcdLonLatPoint(-74.2, 38.0),
            new TLcdLonLatPoint(-71.3, 39.0),
            new TLcdLonLatPoint(-69.0, 38.2),
            new TLcdLonLatPoint(-70.3, 35.1),
        }, false);
    return new TLcdLonLatPolygon(point_list_2d, aEllipsoid);
  }

  private ILcdSurface createLonLatSurface(ILcdEllipsoid aEllipsoid) {
    TLcdSurface surface = new TLcdSurface();
    surface.setExteriorRing(new TLcdLonLatCircle(new TLcdLonLatPoint(-80.0, 36.0), 300000, aEllipsoid));
    surface.getInteriorRings().add(new TLcdLonLatCircle(new TLcdLonLatPoint(-79.0, 37.0), 50000, aEllipsoid));
    surface.getInteriorRings().add(new TLcdLonLatPolygon(new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
        new TLcdLonLatPoint(-82, 37),
        new TLcdLonLatPoint(-81.5, 37.5),
        new TLcdLonLatPoint(-81, 37)
    }, false), aEllipsoid));
    // Rings and surfaces can also contain composite curves.
    TLcdLonLatCompositeRing interiorRing = new TLcdLonLatCompositeRing(aEllipsoid);
    TLcdCompositeCurve innerCurve = new TLcdCompositeCurve();
    innerCurve.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(-79.0, 34.0),
        new TLcdLonLatPoint(-81.0, 35.5),
        new TLcdLonLatPoint(-79.0, 36.0),
        aEllipsoid));
    innerCurve.getCurves().add(new TLcdLonLatCircularArcBy3Points(
        new TLcdLonLatPoint(-79.0, 36.0),
        new TLcdLonLatPoint(-78.0, 35.0),
        new TLcdLonLatPoint(-79.0, 34.0),
        aEllipsoid));
    interiorRing.getCurves().add(innerCurve);
    surface.getInteriorRings().add(interiorRing);
    return surface;
  }

  private TLcdComplexPolygon createLonLatComplexPolygon(ILcdEllipsoid aEllipsoid) {
    return new TLcdComplexPolygon(new TLcdLonLatPolygon[]{
        new TLcdLonLatPolygon(new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
            new TLcdLonLatPoint(-67, 36),
            new TLcdLonLatPoint(-64, 36),
            new TLcdLonLatPoint(-64, 39),
            new TLcdLonLatPoint(-67, 39)}, false), aEllipsoid),
        new TLcdLonLatPolygon(new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
            new TLcdLonLatPoint(-66, 37),
            new TLcdLonLatPoint(-65, 37),
            new TLcdLonLatPoint(-65, 38),
            new TLcdLonLatPoint(-66, 38)}, false), aEllipsoid)
    });
  }

  private Object createLonLatShapeList(ILcdEllipsoid aEllipsoid) {
    TLcd2DEditablePointList point_list_1 =
        new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
            new TLcdLonLatPoint(-65.5, 32.0),
            new TLcdLonLatPoint(-65.5, 35.0),
            new TLcdLonLatPoint(-64.0, 33.5),
        }, false);

    TLcd2DEditablePointList point_list_2 =
        new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
            new TLcdLonLatPoint(-65.5, 32.0),
            new TLcdLonLatPoint(-65.5, 35.0),
            new TLcdLonLatPoint(-67.0, 33.5),
        }, false);

    return new TLcdShapeList(
        new ILcdShape[]{
            new TLcdLonLatPolygon(point_list_1, aEllipsoid),
            new TLcdLonLatPolygon(point_list_2, aEllipsoid)}
    );
  }
}
