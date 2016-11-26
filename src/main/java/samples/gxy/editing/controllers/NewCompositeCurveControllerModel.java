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
import java.util.LinkedList;
import java.util.List;

import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.ILcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdCurve;
import com.luciad.util.ILcdCloneable;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;

import samples.gxy.editing.CompositeCurveUtil;

/**
 * Extension of NewShapeControllerModel that handles composite curves.
 * This model defines the following behavior:
 * <ul>
 * <li>when a subcurve is completed, a new curve is automatically added;
 * <li>a double click or right-click commits a (non-empty) curve, discarding any unfinished subcurve.
 * </ul>
 * It relies on the layer providing an editor for the subcurves.
 */
public class NewCompositeCurveControllerModel extends NewShapeControllerModel {

  private CompositeCurveUtil fCompositeCurveUtil;

  // The edit counts for the created sub-curves of our composite curve.
  private LinkedList<Integer> fSubCurveEditCounts = new LinkedList<Integer>();
  // Determines what curves to add.
  private ShapeType fSubCurveType;

  public NewCompositeCurveControllerModel(ShapeType aLonLatShapeType,
                                          ControllerSettingsNotifier aNotifier
                                         ) {
    super(aLonLatShapeType, aNotifier);
    fCompositeCurveUtil = new CompositeCurveUtil();
  }

  @Override
  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    Object object = super.create(aEditCount, aGraphics, aMouseEvent, aSnappables, aGXYContext);
    // Adds our first sub-curve.
    ILcdCurve curve = createCurve(aGXYContext.getGXYLayer().getModel().getModelReference());
    if (curve != null) {
      getCompositeCurve(object).getCurves().add(curve);
      fSubCurveEditCounts.add(0);
    }
    return object;
  }

  @Override
  public CreationStatus getCreationStatus(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    ILcdCompositeCurve compositeCurve = getCompositeCurve(aObject);
    List<ILcdCurve> curves = compositeCurve.getCurves();

    // We can commit if the last subcurve is committable...
    if (curves.size() > 0 &&
        getSubCurveCreationStatus(curves.get(curves.size() - 1), aGXYContext.getGXYLayer()) == CreationStatus.COMMITTABLE) {
      return CreationStatus.COMMITTABLE;
    }
    // ...or if we've just begun with a new subcurve, meaning that we just finished the previous one.
    if (curves.size() > 1 &&
        fSubCurveEditCounts.getLast() == 1) {
      return CreationStatus.COMMITTABLE;
    }

    return CreationStatus.UNCOMMITTABLE;
  }

  @Override
  public Object edit(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    // Undo preparations. Rather than relying on reverse actions to undo this operation, we simply clone the object.
    Object clonedObject = ((ILcdCloneable) aObject).clone();
    final LinkedList<Integer> undoEditCounts = (LinkedList<Integer>) fSubCurveEditCounts.clone();

    // Edits the last sub curve.
    Object editedObject = super.edit(clonedObject, getEditCountToDelegate(aEditCount), aGraphics, aMouseEvent, aSnappables, aGXYContext);
    if (editedObject == null) {
      return null;
    }
    fSubCurveEditCounts.add(fSubCurveEditCounts.removeLast() + 1);

    ILcdCompositeCurve compositeCurve = getCompositeCurve(editedObject);
    List<ILcdCurve> curves = compositeCurve.getCurves();
    CreationStatus lastCreationStatus = curves.isEmpty() ? CreationStatus.FINISHED :
                                        getSubCurveCreationStatus(curves.get(curves.size() - 1), aGXYContext.getGXYLayer());

    if (lastCreationStatus == CreationStatus.FINISHED) {
      ILcdCurve curve = createCurve(aGXYContext.getGXYLayer().getModel().getModelReference());
      // Adds a new curve and sets the start point.
      if (curve != null) {
        curves.add(curve);
        if (curves.size() == 1) {
          ILcdGXYEditor editor = aGXYContext.getGXYLayer().getGXYEditor(editedObject);
          if (editor != null) {
            editor.edit(aGraphics, ILcdGXYEditor.START_CREATION, aGXYContext);
          } else {
            throw new RuntimeException("No editor found to edit the subcurve.");
          }
        } else {
          fCompositeCurveUtil.connectCompositeCurveAsCurve(compositeCurve, curves.size() - 2, aGXYContext.getGXYLayer().getModel().getModelReference());
        }
        fSubCurveEditCounts.add(1);
      }
    }

    // Fires an undoable, setting the correct state. The controller will know what object to use.
    final LinkedList<Integer> redoEditCounts = (LinkedList<Integer>) fSubCurveEditCounts.clone();
    super.fireUndoableHappened(new ALcdUndoable("Edit") {
      protected void undoImpl() throws TLcdCannotUndoRedoException {
        fSubCurveEditCounts = undoEditCounts;
      }

      protected void redoImpl() throws TLcdCannotUndoRedoException {
        fSubCurveEditCounts = redoEditCounts;
      }
    });
    return editedObject;
  }

  @Override
  protected void fireUndoableHappened(ILcdUndoable aUndoable) {
    // We handle undoables ourselves.
  }

  @Override
  public void commit(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aGXYContext) {
    ILcdCompositeCurve compositeCurve = getCompositeCurve(aObject);
    List<ILcdCurve> curves = compositeCurve.getCurves();

    CreationStatus lastCreationStatus = getSubCurveCreationStatus(curves.get(curves.size() - 1), aGXYContext.getGXYLayer());
    // Removes any unfinished sub curve.
    if (lastCreationStatus == CreationStatus.UNCOMMITTABLE) {
      curves.remove(curves.size() - 1);
    }
    fSubCurveEditCounts.clear();

    // Closes rings
    if (fCompositeCurveUtil.isRing(compositeCurve)) {
      fCompositeCurveUtil.connectCompositeCurve(compositeCurve, 0, aGXYContext.getGXYLayer().getModel().getModelReference());
    }
    super.commit(aObject, aEditCount, aGraphics, aMouseEvent, aSnappables, aGXYContext);
  }

  @Override
  public void cancel(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    super.cancel(aObject, aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
    fSubCurveEditCounts.clear();
  }

  @Override
  public void paint(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    super.paint(aObject, getEditCountToDelegate(aEditCount), aGraphics, aMouseEvent, aSnappables, aContext);
  }

  private int getEditCountToDelegate(int aEditCount) {
    // Just makes sure that 0 is correctly passed.
    return (fSubCurveEditCounts.size() == 1 && fSubCurveEditCounts.getFirst() == 0) ? 0 : aEditCount;
  }

  protected CreationStatus getSubCurveCreationStatus(ILcdCurve aCurve, ILcdGXYLayer aGXYLayer) {
    ILcdGXYEditor editor = aGXYLayer.getGXYEditor(aCurve);
    if (Math.abs(editor.getCreationClickCount()) > fSubCurveEditCounts.getLast()) {
      return CreationStatus.UNCOMMITTABLE;
    }
    if (editor.getCreationClickCount() < 0) {
      return CreationStatus.COMMITTABLE;
    }
    return CreationStatus.FINISHED;
  }

  protected ILcdCurve createCurve(ILcdModelReference aModelReference) {
    return (ILcdCurve) createShapeForType(fSubCurveType, aModelReference);
  }

  /**
   * Changes the type of the currently edited and future sub-curves.
   * @param aType          the new curve type
   * @param aCurrentObject the currently edited composite curve, or null if the object is not yet being edited
   * @param aGXYLayer      the layer of the current object
   */
  public void setSubCurveType(ShapeType aType, ILcdCompositeCurve aCurrentObject, ILcdGXYLayer aGXYLayer) {
    fSubCurveType = aType;
    if (aCurrentObject == null) {
      return;
    }

    updateForNewSubCurveType(aCurrentObject, aGXYLayer);
  }

  protected void updateForNewSubCurveType(ILcdCompositeCurve aCurrentObject, ILcdGXYLayer aLayer) {
    List<ILcdCurve> curves = aCurrentObject.getCurves();
    if (!curves.isEmpty()) {
      CreationStatus lastCreationStatus = getSubCurveCreationStatus(curves.get(curves.size() - 1), aLayer);
      // Removes any unfinished sub curve.
      if (lastCreationStatus == CreationStatus.UNCOMMITTABLE) {
        curves.remove(curves.size() - 1);
        fSubCurveEditCounts.removeLast();
      }
    }

    // Adds a new curve and sets the start point.
    ILcdModelReference modelReference = aLayer.getModel().getModelReference();
    ILcdCurve curve = createCurve(modelReference);
    curves.add(curve);
    if (curves.size() > 1) {
      fCompositeCurveUtil.connectCompositeCurveAsCurve(aCurrentObject, curves.size() - 2, modelReference);
      fSubCurveEditCounts.add(1);
    } else {
      fSubCurveEditCounts.add(0);
    }
  }

  protected ILcdCompositeCurve getCompositeCurve(Object aObject) {
    return (ILcdCompositeCurve) aObject;
  }

  protected void logEdit() {
    fSubCurveEditCounts.addLast(fSubCurveEditCounts.removeLast() + 1);
  }

  protected void clearEdits() {
    fSubCurveEditCounts.clear();
  }
}
