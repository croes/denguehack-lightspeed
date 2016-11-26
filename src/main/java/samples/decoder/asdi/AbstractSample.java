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
package samples.decoder.asdi;

import java.io.IOException;
import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;

import samples.gxy.common.GXYSample;
import samples.realtime.gxy.tracksimulator.FirstTouchedLabelEditController;

/**
 * Abstract base class that provides the common functionality for ASDI samples.
 */
public abstract class AbstractSample extends GXYSample {

  private DataObjectDisplay fDataObjectDisplay;
  private ILcdFilter fTrackModelFilter;
  private ILcdFilter fFlightPlanModelFilter;

  protected AbstractSample(ILcdFilter aTrackModelFilter, ILcdFilter aFlightPlanModelFilter) {
    super();
    fTrackModelFilter = aTrackModelFilter;
    fFlightPlanModelFilter = aFlightPlanModelFilter;
  }

  protected void createGUI() {
    initializeDecoders();
    super.createGUI();
    getToolBars()[0].setGXYControllerEdit(createEditController());
    getView().setGXYController(getToolBars()[0].getGXYCompositeEditController());

    //create the display window that will show the properties of the track.
    fDataObjectDisplay = new DataObjectDisplay();
    fDataObjectDisplay.setSize(300, 400);
    fDataObjectDisplay.setVisible(true);

    //add the selection listener that will pop up the frame with the data object information
    getView().addLayerSelectionListener(new DataObjectDisplaySelectionListener());
  }

  //Please refer to the realtime developer guide for more information about this.
  private TLcdGXYEditController2 createEditController() {
    FirstTouchedLabelEditController edit_controller = new FirstTouchedLabelEditController();
    edit_controller.setStickyLabelsLayerFilter(new MySelectableRealtimeLayersFilter());
    edit_controller.setInstantEditing(true);
    edit_controller.setEditFirstTouchedLabelOnly(true);
    return edit_controller;
  }

  /**
   * Filter that only accepts layers if
   *  - they are ILcdGXYEditableLabelsLayer
   *  - they are selectable
   *  - they are realtime layers (defined by the number of cached background layers)
   */
  private class MySelectableRealtimeLayersFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdGXYEditableLabelsLayer) {
        ILcdGXYEditableLabelsLayer layer = (ILcdGXYEditableLabelsLayer) aObject;
        return layer.isSelectableSupported() && layer.isSelectable() &&
               getView().indexOf(layer) >= getView().getNumberOfCachedBackgroundLayers();
      } else {
        return false;
      }
    }
  }

  protected void initializeDecoders() {
  }

  public void addData() throws IOException {
    super.addData();
    //If a track gets selected, select matching flightplan and trajectory too
    TrackSelectionMediator.install(getView());
  }

  private void updateTrackDataObjectDisplay(ILcdModel aModel, Object aTrack) {
    //set the specified track on the popup frame.
    fDataObjectDisplay.setTrackModel(aModel);
    fDataObjectDisplay.setTrack(aTrack);
    if (!fDataObjectDisplay.isVisible()) {
      fDataObjectDisplay.setVisible(true);
    }
  }

  private void updateFlightPlanDataObjectDisplay(ILcdModel aModel, Object aFlightPlan) {
    //set the specified flight plan on the popup frame.
    fDataObjectDisplay.setFlightPlanModel(aModel);
    fDataObjectDisplay.setFlightPlan(aFlightPlan);
    if (!fDataObjectDisplay.isVisible()) {
      fDataObjectDisplay.setVisible(true);
    }
  }

  /**
   * This listener updates the data object display with the lastly selected track.
   */
  private class DataObjectDisplaySelectionListener implements ILcdSelectionListener {
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionChangedEvent) {
      ILcdLayer layer = (ILcdLayer) aSelectionChangedEvent.getSelection();
      ILcdModel model = layer.getModel();
      if (fTrackModelFilter.accept(model)) {
        //get the first track that was selected and show that in the data object display.
        Enumeration selected = aSelectionChangedEvent.selectedElements();
        if (selected.hasMoreElements()) {
          updateTrackDataObjectDisplay(model, selected.nextElement());
        } else {
          updateTrackDataObjectDisplay(model, null);
        }
      }
      if (fFlightPlanModelFilter.accept(model)) {
        //get the first flight plan that was selected and show that in the data object display.
        Enumeration selected = aSelectionChangedEvent.selectedElements();
        if (selected.hasMoreElements()) {
          updateFlightPlanDataObjectDisplay(model, selected.nextElement());
        } else {
          updateFlightPlanDataObjectDisplay(model, null);
        }
      }
    }
  }
}
