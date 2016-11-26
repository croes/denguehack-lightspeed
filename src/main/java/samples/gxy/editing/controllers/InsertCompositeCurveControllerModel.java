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
package samples.gxy.editing.controllers;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdCurve;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;

import samples.gxy.editing.CompositeCurveUtil;

/**
 * Extension of NewCompositeCurveControllerModel for inserting a curve into a existing composite curve.
 */
public class InsertCompositeCurveControllerModel extends NewCompositeCurveControllerModel {

  private ILcdGXYLayer fLayer;
  private ILcdCompositeCurve fCompositeCurveToInsertTo;
  private Object fObjectToInsertTo; // the model element containing the composite curve
  private int fInsertIndex;
  private CompositeCurveUtil fCompositeCurveUtil = new CompositeCurveUtil();

  public InsertCompositeCurveControllerModel(ShapeType aShapeType, ILcdGXYLayer aLayer, ControllerSettingsNotifier aNotifier) {
    super(aShapeType, aNotifier);
    fLayer = aLayer;
  }

  public void init(int aInsertIndex, ILcdCompositeCurve aCompositeCurveToInsertTo, Object aObjectToInsertTo) {
    fInsertIndex = aInsertIndex;
    fCompositeCurveToInsertTo = aCompositeCurveToInsertTo;
    fObjectToInsertTo = aObjectToInsertTo;
  }

  private void reset() {
    fInsertIndex = -1;
    fCompositeCurveToInsertTo = null;
    fObjectToInsertTo = null;
  }

  @Override
  protected boolean isGXYLayerSupported(ILcdGXYLayer aLayer) {
    return aLayer == fLayer;
  }

  @Override
  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    Object object = super.create(aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
    // Note that this initial edit adds 1 to the edit count.
    connectInsertedCompositeCurve(getCompositeCurve(object), aContext.getGXYLayer().getModel().getModelReference());
    return object;
  }

  @Override
  public void paint(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    // Increase the edit count, since we already edited in the create method.
    super.paint(aObject, aEditCount + 1, aGraphics, aMouseEvent, aSnappables, aContext);
  }

  @Override
  public Object edit(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    // Increase the edit count, since we already edited in the create method.
    return super.edit(aObject, aEditCount + 1, aGraphics, aMouseEvent, aSnappables, aContext);
  }

  @Override
  public void commit(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    ILcdCompositeCurve compositeCurve = getCompositeCurve(aObject);
    List<ILcdCurve> curves = compositeCurve.getCurves();

    // Removes any unfinished sub curve.
    CreationStatus lastCreationStatus = getSubCurveCreationStatus(curves.get(curves.size() - 1), aGXYContext.getGXYLayer());
    if (lastCreationStatus == CreationStatus.UNCOMMITTABLE) {
      curves.remove(curves.size() - 1);
    }
    clearEdits();

    ILcdModel model = aGXYContext.getGXYLayer().getModel();
    TLcdLockUtil.writeLock(model);
    try {
      // Appends the newly created curve to the original one.
      int index = fInsertIndex;
      for (ILcdCurve curve : curves) {
        fCompositeCurveToInsertTo.getCurves().add(index++, curve);
      }
      fCompositeCurveUtil.connectCompositeCurve(fCompositeCurveToInsertTo, fInsertIndex, aGXYContext.getGXYLayer().getModel().getModelReference());
      model.elementChanged(fObjectToInsertTo, ILcdModel.FIRE_LATER);
    } finally {
      TLcdLockUtil.writeUnlock(model);
    }
    model.fireCollectedModelChanges();

    reset();
  }

  @Override
  public void cancel(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    super.cancel(aObject, aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
    reset();
  }

  @Override
  protected void updateForNewSubCurveType(ILcdCompositeCurve aCurrentObject, ILcdGXYLayer aGXYLayer) {
    super.updateForNewSubCurveType(aCurrentObject, aGXYLayer);

    if (aCurrentObject.getCurves().size() == 1) {
      connectInsertedCompositeCurve(aCurrentObject, aGXYLayer.getModel().getModelReference());
    }
  }

  // Edits the new curve by adding it to the original curve and connecting it.
  private void connectInsertedCompositeCurve(ILcdCompositeCurve aCompositeCurve, ILcdModelReference aModelReference) {
    // Clones the curve we're appending to; we want to avoid modifying it until we actually commit the new curve.
    ILcdCompositeCurve clone = (ILcdCompositeCurve) fCompositeCurveToInsertTo.clone();
    clone.getCurves().add(fInsertIndex, aCompositeCurve);
    if (fCompositeCurveUtil.isRing(aCompositeCurve) || fInsertIndex != 0) {
      // Connects the new curve to the old one.
      fCompositeCurveUtil.connectSubCurveStartPoint(clone, fInsertIndex, aModelReference);
      logEdit();
    }
  }
}
