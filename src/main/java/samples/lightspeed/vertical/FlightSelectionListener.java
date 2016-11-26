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
package samples.lightspeed.vertical;

import java.util.Enumeration;

import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;

/**
 * This listener updates the vertical view model when the selection is changed.
 */
public class FlightSelectionListener implements ILcdSelectionListener {

  private final TerrainFlightVVModel fVVModel;
  private Object fCurrentVVObject;
  private ILspLayer fCurrentVVObjectLayer;

  /**
   * Creates a new listener based on the given vertical view model.
   *
   * @param aVVModel a vertical view model.
   */
  public FlightSelectionListener(TerrainFlightVVModel aVVModel) {
    fVVModel = aVVModel;
  }

  public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
    Enumeration selectedElements = aSelectionEvent.selectedElements();
    ILspLayer sourceLayer = (ILspLayer) aSelectionEvent.getSource();

    if (selectedElements.hasMoreElements()) {
      // If a new object is selected, use this one
      while (selectedElements.hasMoreElements()) {
        Object selectedElement = selectedElements.nextElement();
        // Only use objects that can effectively be used by the vertical view profile model
        if (fVVModel.retrievePointList(selectedElement) != null) {
          setVVModelObject(selectedElement, sourceLayer);
          return;
        }
      }
    }

    if (fCurrentVVObject != null && fCurrentVVObjectLayer != null) {
      // If the current object is deselected, find a new one (unless it is now being edited)
      Enumeration deselectedElements = aSelectionEvent.deselectedElements();
      while (deselectedElements.hasMoreElements()) {
        Object deselectedElement = deselectedElements.nextElement();
        if (fCurrentVVObject == deselectedElement && fCurrentVVObjectLayer == sourceLayer) {
          if (isEdited(fCurrentVVObject, fCurrentVVObjectLayer)) {
            // Do not change the vertical view object
            return;
          }
          findVerticalViewObject(fCurrentVVObjectLayer);
          return;
        }
      }
    }
    // Do not change the vertical view object
  }

  private void findVerticalViewObject(ILspLayer aLayer) {
    Object editedObject = getFirstObject(aLayer, TLspPaintState.EDITED);
    if (editedObject != null) {
      setVVModelObject(editedObject, aLayer);
      return;
    }

    Object selectedObject = getFirstObject(aLayer, TLspPaintState.SELECTED);
    if (selectedObject != null) {
      setVVModelObject(selectedObject, aLayer);
      return;
    }

    setVVModelObject(null, null);
  }

  private boolean isEdited(Object aObject, ILspLayer aLayer) {
    if (aLayer instanceof ILspInteractivePaintableLayer) {
      ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
      return layer.getObjectsWithPaintState(TLspPaintState.EDITED).contains(aObject);
    }
    return false;
  }

  private Object getFirstObject(ILspLayer aLayer, TLspPaintState aPaintState) {
    if (aLayer instanceof ILspInteractivePaintableLayer) {
      ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
      ILcdCollection<Object> objects = layer.getObjectsWithPaintState(aPaintState);
      for (Object object : objects) {
        if (fVVModel.retrievePointList(object) != null) {
          return object;
        }
      }
      return null;
    }
    return null;
  }

  private void setVVModelObject(Object aObject, ILspLayer aLayer) {
    fCurrentVVObject = aObject;
    fCurrentVVObjectLayer = aLayer;
    fVVModel.setObject(aObject, aLayer == null ? null : aLayer.getModel());
    fVVModel.update();
  }
}
