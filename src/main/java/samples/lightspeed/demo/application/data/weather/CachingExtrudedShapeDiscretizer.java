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
package samples.lightspeed.demo.application.data.weather;

import static samples.lightspeed.demo.application.data.weather.CachingShapeDiscretizer.createMeshSFCT;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.luciad.format.object3d.TLcd3DPrimitiveType;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DPrimitive;
import com.luciad.view.lightspeed.geometry.discretization.ALspEditable3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationMode;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;

import samples.lightspeed.meshencoder.Mesh;

/**
 * For some extruded shapes the discretization is slow.  This class caches the discretized shape.
 * The discretization itself is also custom for performance and memory usage reasons.
 */
class CachingExtrudedShapeDiscretizer extends TLspShapeDiscretizer {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(CachingExtrudedShapeDiscretizer.class.getName());

  private final Map<Object, Mesh> fCache = new ConcurrentHashMap<>();

  @Override
  public void discretizeSFCT(Object aDomainObject, TLspShapeDiscretizationParameters aParameters, TLspContext aContext, ALspEditable3DMesh a3DMeshSFCT) throws TLspDiscretizationException {
    if (aContext.getXYZWorldReference() instanceof ILcdGeocentricReference && aDomainObject instanceof ILcdExtrudedShape) {
      ILcdExtrudedShape extrudedShape = (ILcdExtrudedShape) aDomainObject;
      ILcdShape baseShape = extrudedShape.getBaseShape();
      Mesh cachedMesh = fCache.get(baseShape);
      if (cachedMesh == null) {
        Collection<TLspShapeDiscretizationMode> modes = new HashSet<>(aParameters.getModes());
        modes.add(TLspShapeDiscretizationMode.OUTLINE);
        TLspShapeDiscretizationParameters newParameters = new TLspShapeDiscretizationParameters.Builder().all(aParameters).modes(modes).build();
        //Discretize base shape (bottom of eventual extruded shape).
        super.discretizeSFCT(baseShape, newParameters, aContext, a3DMeshSFCT);

        //For all points add a corresponding elevated point.
        TLcdXYZPoint point = new TLcdXYZPoint();
        TLcdXYZPoint modelPoint = new TLcdXYZPoint();
        int vertexCount = a3DMeshSFCT.getVertexCount();
        a3DMeshSFCT.addVertices(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
          a3DMeshSFCT.getPositionSFCT(i, point);
          try {
            aContext.getModelXYZWorldTransformation().worldPoint2modelSFCT(point, modelPoint);
            modelPoint.move3D(modelPoint.getX(), modelPoint.getY(), extrudedShape.getMinimumZ());
            aContext.getModelXYZWorldTransformation().modelPoint2worldSFCT(modelPoint, point);

            a3DMeshSFCT.setPosition(i, point);

            modelPoint.move3D(modelPoint.getX(), modelPoint.getY(), extrudedShape.getMaximumZ());
            aContext.getModelXYZWorldTransformation().modelPoint2worldSFCT(modelPoint, point);

            a3DMeshSFCT.setPosition(i + vertexCount, point);
          } catch (TLcdOutOfBoundsException e) {
            LOGGER.warn("Exception when computing discretization", e);
            throw new TLspDiscretizationException("Exception when computing discretization: " + e.getMessage());
          }
        }

        //Use the added points to construct triangles to create the extruded shape mesh.
        //Because for each point, the corresponding ("extruded") point is at index + vertexCount, there is no need to
        //create new indices for the primitives.  One can calculate the correct index from the original primitives.
        int primitiveCount = a3DMeshSFCT.getPrimitiveCount();
        for (int i = 0; i < primitiveCount; i++) {
          ALsp3DPrimitive original = a3DMeshSFCT.getPrimitive(i);
          if (original.getPrimitiveType() == TLcd3DPrimitiveType.LINES) {
            //The points of the outline
            a3DMeshSFCT.addPrimitive(new SideTrianglesFromOutlinePrimitive(original, vertexCount));
          } else {
            //The points of the triangles of the bottom layer
            a3DMeshSFCT.addPrimitive(new TopTrianglesFromBottomTrianglesPrimitive(original, vertexCount));
          }
        }

        Mesh newMesh = new Mesh(a3DMeshSFCT.getVertexCount());
        createMeshSFCT(a3DMeshSFCT, newMesh);

        fCache.put(baseShape, newMesh);
      } else {
        createMeshSFCT(cachedMesh, a3DMeshSFCT);
      }

    } else {
      super.discretizeSFCT(aDomainObject, aParameters, aContext, a3DMeshSFCT);
    }
  }

  void clear() {
    fCache.clear();
  }

  private static class TopTrianglesFromBottomTrianglesPrimitive extends ALsp3DPrimitive {

    private final ALsp3DPrimitive fALsp3DPrimitive;
    private final int fVertexCount;

    public TopTrianglesFromBottomTrianglesPrimitive(ALsp3DPrimitive aALsp3DPrimitive, int aVertexCount) {
      fALsp3DPrimitive = aALsp3DPrimitive;
      fVertexCount = aVertexCount;
    }

    @Override
    public TLcd3DPrimitiveType getPrimitiveType() {
      return fALsp3DPrimitive.getPrimitiveType();
    }

    @Override
    public int getElementCount() {
      return fALsp3DPrimitive.getElementCount();
    }

    @Override
    public int getElement(int aIndex) {
      return fALsp3DPrimitive.getElement(aIndex) + fVertexCount;
    }

  }

  private static class SideTrianglesFromOutlinePrimitive extends ALsp3DPrimitive {

    private final ALsp3DPrimitive fALsp3DPrimitive;
    private final int fVertexCount;

    public SideTrianglesFromOutlinePrimitive(ALsp3DPrimitive aALsp3DPrimitive, int aVertexCount) {
      fALsp3DPrimitive = aALsp3DPrimitive;
      fVertexCount = aVertexCount;
    }

    @Override
    public TLcd3DPrimitiveType getPrimitiveType() {
      return TLcd3DPrimitiveType.TRIANGLES;
    }

    @Override
    public int getElementCount() {
      return fALsp3DPrimitive.getElementCount() * 3;
    }

    //Simple repeating triangle pattern from 4 points.
    //     _
    //   |\ |
    //   |_\|
    @Override
    public int getElement(int aIndex) {
      int triangleIdentifier = aIndex % 6;
      int triangleElementIdentifier = aIndex % 3;
      int numberOfPreviouslyDefinedTriangles = aIndex / 3;
      if (triangleIdentifier < 3) {
        if (triangleElementIdentifier == 0) {
          return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles);
        } else if (triangleElementIdentifier == 1) {
          return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles + 1);
        } else {
          return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles) + fVertexCount;
        }
      } else if (triangleElementIdentifier == 0) {
        return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles);
      } else if (triangleElementIdentifier == 1) {
        return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles) + fVertexCount;
      } else {
        return fALsp3DPrimitive.getElement(numberOfPreviouslyDefinedTriangles - 1) + fVertexCount;
      }
    }

  }

}
