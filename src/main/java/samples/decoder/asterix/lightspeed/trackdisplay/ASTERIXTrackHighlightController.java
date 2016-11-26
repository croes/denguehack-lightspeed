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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.selection.ALspSelectInput;
import com.luciad.view.lightspeed.controller.selection.TLspSelectControllerModel;
import com.luciad.view.lightspeed.controller.selection.TLspSelectPointInput;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * Controller that allows to mark objects by clicking on them, they are then stored in a Set of highlighted objects.
 * Clicking on an already marked object, un-marks it.
 * This controller uses the select controller model internally to pick the object below the mouse cursor.
 */
class ASTERIXTrackHighlightController extends ALspController {

  private final ASTERIXTrackAdditionalData fAdditionalData;
  private final TLspSelectControllerModel fSelectControllerModel = new TLspSelectControllerModel();

  public ASTERIXTrackHighlightController(ASTERIXTrackAdditionalData aAdditionalData) {
    setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());
    fAdditionalData = aAdditionalData;
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aEvent) {
    if (aEvent instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) aEvent;
      switch (me.getID()) {
      case MouseEvent.MOUSE_CLICKED:
        return mouseClicked(me);
      }
    }
    return aEvent;
  }

  private AWTEvent mouseClicked(MouseEvent me) {
    if (me.getClickCount() > 1) {
      return me;
    }

    ALspSelectInput input = new TLspSelectPointInput(me.getPoint());
    List<TLspDomainObjectContext> highlightCandidates = fSelectControllerModel.selectionCandidates(
        input,
        Collections.singleton(TLspPaintRepresentation.BODY),
        false,
        getView()
    );

    applyHighlighting(highlightCandidates);

    return me;
  }

  private void applyHighlighting(List<TLspDomainObjectContext> aHighlightingCandidates) {
    ILspInteractivePaintableLayer trackLayer = findTrackLayer(getView());
    if (trackLayer == null) {
      fAdditionalData.clearHighlighting();
      return;
    }

    List<Object> modifiedObjects = new ArrayList<>();
    for (TLspDomainObjectContext candidate : aHighlightingCandidates) {
      Object domainObject = candidate.getDomainObject();
      fAdditionalData.invertHighlighting(domainObject);
      modifiedObjects.add(domainObject);
    }

    // Fire model change events. This is done to make sure that ASTERIXTrackLabelStyler picks up the changes. The flow
    // is as follows:
    // - fire model change event
    // - ALspSwingLabelStyler receives the model change event
    // - ALspSwingLabelStyler calls shouldInvalidateLabel() (see ASTERIXTrackLabelStyler)
    // - if true, a style change event is thrown (if false, the flow stops)
    // - the styler is called again for the highlighted objects
    // - the label painter now paints the highlighted object differently
    fireModelChangeEvents(trackLayer.getModel(), modifiedObjects);
  }

  private void fireModelChangeEvents(ILcdModel aModel, List<Object> aModifiedObjects) {
    TLcdLockUtil.writeLock(aModel);
    try {
      for (Object modifiedObject : aModifiedObjects) {
        aModel.elementChanged(modifiedObject, ILcdModel.FIRE_LATER);
      }
    } finally {
      TLcdLockUtil.writeUnlock(aModel);
      aModel.fireCollectedModelChanges();
    }
  }

  private ILspInteractivePaintableLayer findTrackLayer(ILspView aView) {
    int layerCount = aView.layerCount();
    for (int i = 0; i < layerCount; i++) {
      ILspLayer layer = aView.getLayer(i);
      if (MainPanel.LAYER_FILTER.accept(layer)) {
        return (ILspInteractivePaintableLayer) layer;
      }
    }
    return null;
  }
}
