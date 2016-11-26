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
package samples.lightspeed.meshencoder;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import com.luciad.format.object3d.TLcd3DPrimitiveType;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdTopocentricReference;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelModelTransformation;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspOffscreenView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DPrimitive;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationMode;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.common.format.JSONMeshEncoder;

public class JSONShapeMeshEncoder {

  /**
   * Creates a JSON file with properties "location", "positions" and "indices" that represents the given {@link ILcdShape} as a mesh.
   *
   * For example:
   * <pre class="code">
   *   {
   *     "location": [6, 50, 100],
   *     "positions": [0, 0, 0,   //Vertex 0
   *                   10, 0, 0,  //Vertex 1
   *                   0, 10, 0], //Vertex 2
   *     "indices": [0, 1, 2]     //Triangle 0 - 1 - 2
   *   }
   * </pre>
   *
   * Note that:
   * <ul>
   *   <li>Unlike in the example above, the file is not formatted in such a readable way.</li>
   *   <li>Only triangle primitives are encoded.</li>
   * </ul>
   *
   * @param aShape the shape to export as a mesh
   * @param aModelReference the reference the shape is defined in
   * @param aFileName the file name
   */
  public void exportShape(ILcdShape aShape, ILcdModelReference aModelReference, String aFileName) {
    ALsp3DMesh meshData = createMeshData(aShape, aModelReference);

    DoubleBuffer positions = DoubleBuffer.allocate(meshData.getVertexCount() * 3);
    ILcd3DEditablePoint point = new TLcdXYZPoint();
    for (int i = 0; i < meshData.getVertexCount(); i++) {
      meshData.getPositionSFCT(i, point);
      positions.put(point.getX());
      positions.put(point.getY());
      positions.put(point.getZ());
    }
    positions.rewind();

    int indexCount = 0;
    for (int i = 0; i < meshData.getPrimitiveCount(); i++) {
      ALsp3DPrimitive primitive = meshData.getPrimitive(i);
      if (TLcd3DPrimitiveType.TRIANGLES.equals(primitive.getPrimitiveType())) {
        indexCount += primitive.getElementCount();
      }
    }

    IntBuffer indices = IntBuffer.allocate(indexCount);
    for (int i = 0; i < meshData.getPrimitiveCount(); i++) {
      ALsp3DPrimitive primitive = meshData.getPrimitive(i);
      if (TLcd3DPrimitiveType.TRIANGLES.equals(primitive.getPrimitiveType())) {
        for (int j = 0; j < primitive.getElementCount(); j++) {
          indices.put(primitive.getElement(j));
        }
      }
    }
    indices.rewind();

    new JSONMeshEncoder().exportMeshData(aFileName, aShape.getFocusPoint(), positions, indices, null);
  }

  private ALsp3DMesh createMeshData(ILcdShape aShape, ILcdModelReference aModelReference) {
    TLcdVectorModel model = new TLcdVectorModel(aModelReference);

    TLcdGeodeticReference geodeticReference = new TLcdGeodeticReference();
    TLcdDefaultModelModelTransformation modelToModelTransformation = new TLcdDefaultModelModelTransformation(aModelReference, geodeticReference);
    TLcdLonLatHeightPoint centerPointLLh = new TLcdLonLatHeightPoint();
    try {
      modelToModelTransformation.sourcePoint2destinationSFCT(aShape.getFocusPoint(), centerPointLLh);
    } catch (TLcdOutOfBoundsException e) {
      throw new RuntimeException("Could not export shape as mesh");
    }
    TLcdTopocentricReference topocentricReference = new TLcdTopocentricReference(geodeticReference.getGeodeticDatum(), centerPointLLh);
    TLcdDefaultModelXYZWorldTransformation modelToWorldTransformation = new TLcdDefaultModelXYZWorldTransformation(aModelReference, topocentricReference);

    ILspLayer layer = TLspShapeLayerBuilder.newBuilder().model(model).build();
    TLspOffscreenView view = TLspViewBuilder.newBuilder().viewType(ILspView.ViewType.VIEW_3D).buildOffscreenView();
    TLspContext context = new TLspContext(layer, view);

    TLspShapeDiscretizationParameters parameters = new TLspShapeDiscretizationParameters.Builder()
        .modes(TLspShapeDiscretizationMode.OUTLINE, TLspShapeDiscretizationMode.INTERIOR)
        .allowPrimitiveType(TLcd3DPrimitiveType.TRIANGLES)
        .modelXYZWorldTransformation(modelToWorldTransformation)
        .build();
    Mesh mesh = new Mesh();
    try {
      new TLspShapeDiscretizer().discretizeSFCT(aShape, parameters, context, mesh);
    } catch (TLspDiscretizationException e) {
      throw new RuntimeException("Could not export shape as mesh");
    }
    view.destroy();
    return mesh;
  }

}
