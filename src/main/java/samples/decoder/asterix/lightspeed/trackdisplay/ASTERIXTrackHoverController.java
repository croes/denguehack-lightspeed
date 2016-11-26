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

import com.luciad.view.ALcdWeakLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;

import samples.lightspeed.customization.style.highlighting.HighlightController;

/**
 * This controller keeps track of which label is located under the mouse cursor. It stores this information in
 * the ASTERIXTrackAdditionalData object.
 */
class ASTERIXTrackHoverController extends ALspController {

  private final HighlightController fDelegateController;
  private final ASTERIXTrackAdditionalData fAdditionalData;

  private ILspInteractivePaintableLayer fTrackLayer = null;

  private final LayeredListener fLayeredListener = new LayeredListener(this);

  public ASTERIXTrackHoverController(ASTERIXTrackAdditionalData aAdditionalData) {
    fAdditionalData = aAdditionalData;
    fDelegateController = new HighlightController();
    fDelegateController.addHighlightListener(new OnHoverHighlightListener());
  }

  @Override
  protected void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    ILspInteractivePaintableLayer trackLayer = findTrackLayer(aView);
    if (trackLayer != null) {
      setTrackLayer(trackLayer);
    }
    aView.addLayeredListener(fLayeredListener);
    fDelegateController.startInteraction(aView);
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    super.terminateInteractionImpl(aView);
    if (fTrackLayer != null) {
      removeTrackLayer();
    }
    aView.removeLayeredListener(fLayeredListener);
    fDelegateController.terminateInteraction(aView);
  }

  private void setTrackLayer(ILspInteractivePaintableLayer aTrackLayer) {
    fTrackLayer = aTrackLayer;
    fDelegateController.registerLayer(aTrackLayer, TLspPaintRepresentation.LABEL);
  }

  private void removeTrackLayer() {
    fDelegateController.unregisterLayer(fTrackLayer);
    fTrackLayer = null;
    fAdditionalData.clearHoveredLabel();
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

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {
    fDelegateController.handleAWTEvent(aEvent);
    super.handleAWTEvent(aEvent);
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aEvent) {
    // Don't consume events. The other controllers still need them.
    return aEvent;
  }

  private class OnHoverHighlightListener implements HighlightController.HighlightListener {

    @Override
    public void objectHighlighted(Object aObject, TLspPaintRepresentationState aPaintRepresentationState, TLspContext aContext) {
    }

    @Override
    public void labelHighlighted(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
      if (fTrackLayer == null) {
        return;
      }
      if (aLabelID != null) {
        fAdditionalData.setHoveredLabel(aLabelID, aPaintState, aContext.getView());
      } else {
        fAdditionalData.clearHoveredLabel();
      }
    }
  }

  private static class LayeredListener extends ALcdWeakLayeredListener<ASTERIXTrackHoverController> {

    public LayeredListener(ASTERIXTrackHoverController aController) {
      super(aController);
    }

    @Override
    protected void layeredStateChangeImpl(ASTERIXTrackHoverController aController, TLcdLayeredEvent aLayeredEvent) {
      if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        if (MainPanel.LAYER_FILTER.accept((ILspLayer) aLayeredEvent.getLayer())) {
          aController.setTrackLayer((ILspInteractivePaintableLayer) aLayeredEvent.getLayer());
        }
      } else if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        if (aLayeredEvent.getLayer() == aController.fTrackLayer) {
          aController.removeTrackLayer();
        }
      }
    }
  }
}
