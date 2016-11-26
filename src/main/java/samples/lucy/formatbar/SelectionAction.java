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
package samples.lucy.formatbar;

import static com.luciad.util.concurrent.TLcdLockUtil.*;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.gui.TLcdAWTUtil;
import samples.lucy.undo.ClearSelectionCompositeUndoable;
import samples.lucy.undo.CompositeUndoable;
import samples.lucy.undo.UndoableAction;
import samples.lucy.util.LayerUtil;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;

/**
 * <p>{@link UndoableAction} that changes the selected objects.</p>
 *
 * <p>This class first computes a subset of the selected objects containing
 * all objects for which {@link #canApply(Object, ILcdLayer)} returns true.
 * Then the action is applied to all these objects.</p>
 *
 * <p>This class takes care of enabling the layers and appropriate
 * read and write locking.</p>
 */
public abstract class SelectionAction<T extends ILcdView & ILcdTreeLayered> extends UndoableAction {

  private final T fView;

  public SelectionAction(T aView) {
    fView = aView;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Frame parentFrame = TLcdAWTUtil.findParentFrame(e);
    Map<ILcdLayer, List<Object>> selectedElements = getSelectedObjects(parentFrame);
    if (!validateSelection(selectedElements, parentFrame)) {
      return;
    }
    if (!LayerUtil.canEdit(selectedElements.keySet(), parentFrame)) {
      return;
    }
    apply(selectedElements, parentFrame);
  }

  protected abstract boolean canApply(Object aObject, ILcdLayer aLayer);

  protected void apply(Map<ILcdLayer, List<Object>> aSelection, Frame aParent) {
    ClearSelectionCompositeUndoable<T> undoable = new ClearSelectionCompositeUndoable<T>(getName(), fView);
    undoable.setClearOnRedo(false);
    undoable.setUseWrappedDisplayName(false);
    for (Map.Entry<ILcdLayer, List<Object>> entry : aSelection.entrySet()) {
      ILcdLayer layer = entry.getKey();
      try (Lock autoUnlock = writeLock(layer.getModel())) {
        List<Object> selectedElementsInLayer = entry.getValue();
        applyOnLayer(selectedElementsInLayer, layer, undoable, aParent);
      }
      layer.getModel().fireCollectedModelChanges();
    }
    undoable.finish();
    if (undoable.canUndo()) {
      fireUndoableHappened(undoable);
    }
  }

  protected boolean validateSelection(Map<ILcdLayer, List<Object>> aSelectedObjects, Frame aParent) {
    return !aSelectedObjects.isEmpty();
  }

  @SuppressWarnings("unchecked")
  protected Map<ILcdLayer, List<Object>> getSelectedObjects(Frame aParent) {
    Map<ILcdLayer, List<Object>> result = new HashMap<ILcdLayer, List<Object>>();
    Enumeration<?> layers = fView.layers();
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      if (considerObjectsFromLayer(layer)) {
        try (Lock autoUnlock = readLock(layer.getModel())) {
          Enumeration<?> enumeration = layer.selectedObjects();
          List<Object> selectedElementsInLayer = new ArrayList<Object>();
          while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (canApply(obj, layer)) {
              selectedElementsInLayer.add(obj);
            }
          }
          if (!selectedElementsInLayer.isEmpty()) {
            result.put(layer, selectedElementsInLayer);
          }
        }
      }
    }
    return result;
  }

  protected boolean considerObjectsFromLayer(ILcdLayer layer) {
    return true;
  }

  protected abstract void applyOnObject(Object aObject,
                                        ILcdLayer aLayer,
                                        CompositeUndoable aUndoable,
                                        Frame aParentFrame);

  protected void applyOnLayer(List<?> aSelectedElementsInLayer,
                              ILcdLayer aLayer,
                              CompositeUndoable aUndoable,
                              Frame aParentFrame) {
    for (int i = 0; i < aSelectedElementsInLayer.size(); i++) {
      Object element = aSelectedElementsInLayer.get(i);
      applyOnObject(element, aLayer, aUndoable, aParentFrame);
    }
    aLayer.getModel().fireCollectedModelChanges();
    aLayer.fireCollectedSelectionChanges();
  }

}
