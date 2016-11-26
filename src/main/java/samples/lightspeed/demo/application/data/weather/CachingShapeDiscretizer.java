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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.geometry.discretization.ALspEditable3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;

import samples.lightspeed.meshencoder.Mesh;

public class CachingShapeDiscretizer extends TLspShapeDiscretizer {

  private static final Map<Object, Mesh> CACHE = new ConcurrentHashMap<>();

  @Override
  public void discretizeSFCT(Object aDomainObject, TLspShapeDiscretizationParameters aParameters, TLspContext aContext, ALspEditable3DMesh a3DMeshSFCT) throws TLspDiscretizationException {
    if (canCache(aDomainObject, aParameters, aContext, a3DMeshSFCT)) {
      Mesh cachedMesh = CACHE.get(aDomainObject);
      if (cachedMesh == null) {
        super.discretizeSFCT(aDomainObject, aParameters, aContext, a3DMeshSFCT);

        Mesh newMesh = new Mesh(a3DMeshSFCT.getVertexCount());
        createMeshSFCT(a3DMeshSFCT, newMesh);

        CACHE.put(aDomainObject, newMesh);
      } else {
        createMeshSFCT(cachedMesh, a3DMeshSFCT);
      }

    } else {
      super.discretizeSFCT(aDomainObject, aParameters, aContext, a3DMeshSFCT);
    }
  }

  protected boolean canCache(Object aDomainObject, TLspShapeDiscretizationParameters aParameters, TLspContext aContext, ALspEditable3DMesh a3DMeshSFCT) {
    return aContext.getXYZWorldReference() instanceof ILcdGeocentricReference;
  }

  static void createMeshSFCT(ALspEditable3DMesh aSourceMesh, ALspEditable3DMesh aDestinationMesh) {
    for (int i = 0; i < aSourceMesh.getPrimitiveCount(); i++) {
      aDestinationMesh.addPrimitive(aSourceMesh.getPrimitive(i));
    }
    aDestinationMesh.addVertices(aSourceMesh.getVertexCount());
    ILcd3DEditablePoint point = new TLcdXYZPoint();
    for (int i = 0; i < aSourceMesh.getVertexCount(); i++) {
      aSourceMesh.getPositionSFCT(i, point);
      aDestinationMesh.setPosition(i, point.getX(), point.getY(), point.getZ());
    }
  }

  public static void clear() {
    CACHE.clear();
  }

}
