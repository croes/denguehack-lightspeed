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
package samples.ogc.wfs.server;

import javax.xml.namespace.QName;

import com.luciad.format.xml.TLcdXMLName;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.ogc.common.ILcdOGCModelProvider;
import com.luciad.ogc.common.TLcdOGCServiceException;
import com.luciad.ogc.filter.evaluator.ILcdEvaluatorFunction;
import com.luciad.ogc.filter.evaluator.ILcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterContext;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterEvaluator;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeList;
import com.luciad.ogc.wfs.TLcdWFSFilteredModelFactory;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;

/**
 * Extension of <code>TLcdWFSFilteredModelFactory</code> that adds a custom OGC Filter function
 * to calculate the geodesic area of a feature in square meters.
 * Supported features are polygons, complex polygons and shape lists containing them.
 */
class WFSFilteredModelFactory extends TLcdWFSFilteredModelFactory {

  private static final QName CUSTOM_OPERATOR_NAME = new QName("geodesicArea");

  private TLcdEllipsoid fEllipsoid = new TLcdEllipsoid();

  public WFSFilteredModelFactory(ILcdWFSFeatureTypeList aFeatureTypeList, ILcdOGCModelProvider aModelProvider) throws TLcdOGCServiceException {
    super(aFeatureTypeList, aModelProvider);
  }

  @Override
  protected ILcdOGCFilterEvaluator createFilterEvaluator() {
    TLcdOGCFilterEvaluator filterEvaluator = new TLcdOGCFilterEvaluator();

    filterEvaluator.registerFunction(TLcdXMLName.getInstance(CUSTOM_OPERATOR_NAME), new ILcdEvaluatorFunction() {

      public int getArgumentCount() {
        // No client arguments needed for this function.
        // We just calculate the geodesic area based on the features residing in the WFS.
        return 0;
      }

      public Object apply(Object[] aArguments, Object aCurrentObject, TLcdOGCFilterContext aOGCFilterContext) {
        // We assume that the supported objects implement ILcdShape
        if (aCurrentObject instanceof ILcdShape) {
          return calculateArea((ILcdShape) aCurrentObject);
        }
        // In all other cases, we return 0.
        else {
          return 0;
        }

      }

      private double calculateArea(ILcdShapeList aShapeList) {
        double area = 0;
        for (int i = 0; i < aShapeList.getShapeCount(); i++) {
          area += calculateArea(aShapeList.getShape(i));
        }
        return area;
      }

      private double calculateArea(ILcdShape aShape) {
        if (aShape instanceof ILcdShapeList) {
          return calculateArea((ILcdShapeList) aShape);
        } else if (aShape instanceof ILcdComplexPolygon) {
          return calculateArea((ILcdComplexPolygon) aShape);
        } else if (aShape instanceof ILcdPolygon) {
          return calculateArea((ILcdPolygon) aShape);
        } else {
          return 0;
        }
      }

      private double calculateArea(ILcdComplexPolygon aComplexPolygon) {
        double area = 0;
        for (int i = 0; i < aComplexPolygon.getPolygonCount(); i++) {
          area += calculateArea(aComplexPolygon.getPolygon(i));
        }
        return area;
      }

      private double calculateArea(ILcdPolygon aPolygon) {
        ILcdPoint[] points = new ILcdPoint[aPolygon.getPointCount()];
        for (int i = 0; i < aPolygon.getPointCount(); i++) {
          points[i] = aPolygon.getPoint(i);
        }
        return fEllipsoid.geodesicArea(points, points.length);
      }
    });
    return filterEvaluator;
  }
}
