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

import java.util.ArrayList;
import java.util.List;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.util.collections.TLcdFloatArrayList;
import com.luciad.view.lightspeed.geometry.discretization.ALsp3DPrimitive;
import com.luciad.view.lightspeed.geometry.discretization.ALspEditable3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;

/**
 * An implementation of ALspEditable3DMesh to represent the result of the {@link TLspShapeDiscretizer#discretizeSFCT discretization} of a shape.
 */
public class Mesh extends ALspEditable3DMesh {

  private List<ALsp3DPrimitive> fPrimitives = new ArrayList<>();
  private TLcdFloatArrayList fValues;
  private TLcdXYZBounds fBounds;

  public Mesh() {
    this(100);
  }

  public Mesh(int aVertexCount) {
    fValues = new TLcdFloatArrayList(aVertexCount * 3);
  }

  @Override
  public int addVertices(int aNbVertices) {
    if (aNbVertices <= 0) {
      throw new IllegalArgumentException("cannot add" + aNbVertices + " vertices");
    }
    return getVertexCount();
  }

  @Override
  public void setPosition(int aIndex, ILcdPoint aVertex) {
    if (aIndex < getVertexCount()) {
      fValues.setFloat(3 * aIndex, (float) aVertex.getX());
      fValues.setFloat(3 * aIndex + 1, (float) aVertex.getY());
      fValues.setFloat(3 * aIndex + 2, (float) aVertex.getZ());
    } else {
      fValues.addFloat((float) aVertex.getX());
      fValues.addFloat((float) aVertex.getY());
      fValues.addFloat((float) aVertex.getZ());
    }
    fBounds = null;
  }

  public void addPrimitive(ALsp3DPrimitive aPrimitive) {
    fPrimitives.add(aPrimitive);
  }

  public int getPrimitiveCount() {
    return fPrimitives.size();
  }

  public ALsp3DPrimitive getPrimitive(int i) {
    return fPrimitives.get(i);
  }

  public int getVertexCount() {
    return fValues.size() / 3;
  }

  @Override
  public void getPositionSFCT(int aIndex, ILcd3DEditablePoint aPositionSFCT) {
    aPositionSFCT.move3D(fValues.get(3 * aIndex), fValues.get(3 * aIndex + 1), fValues.get(3 * aIndex + 2));
  }

  @Override
  public ILcdBounds getBounds() {
    if (fBounds == null && getVertexCount() > 0) {
      fBounds = new TLcdXYZBounds(fValues.get(0), fValues.get(1), fValues.get(2), 0, 0, 0);
      for (int i = 1; i < getVertexCount(); i++) {
        fBounds.setToIncludePoint3D(fValues.get(3 * i), fValues.get(3 * i + 1), fValues.get(3 * i + 2));
      }
    }
    return fBounds;
  }

}
