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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;

import samples.decoder.asterix.lightspeed.trackdisplay.ASTERIXTrackAdditionalData.HoveredLabel;

/**
 * Edit controller extension that makes it possible to move track labels without having to select them first.
 */
class ASTERIXTrackNoSelectionEditController extends TLspEditController {

  private TLspLabelID fEditableLabelID = null;
  private TLspPaintState fEditableLabelPaintState;

  // Used to make sure we can still select objects using a mouse click on the label. This is needed because
  // non-selected objects can now be edited, so the edit controller consumes mouse click events.
  private boolean fHandleClick;

  public ASTERIXTrackNoSelectionEditController(ASTERIXTrackAdditionalData aAdditionalData) {
    aAdditionalData.addPropertyChangeListener(new LabelHoveredListener(this));
  }

  @Override
  protected List<TLspDomainObjectContext> getEditingCandidates(ILspView aView) {
    // By default, TLspEditController#getEditingCandidates only returns selected objects. This behavior is overridden
    // to allow editing non-selected objects/label. The new behavior is that any label under the mouse cursor can be
    // moved, even if it is not selected.
    List<TLspDomainObjectContext> editingCandidates = super.getEditingCandidates(aView);
    if (fEditableLabelID != null) {
      if (editingCandidates == null) {
        editingCandidates = new ArrayList<>();
      }
      TLspPaintRepresentationState prs = TLspPaintRepresentationState.getInstance(TLspPaintRepresentation.LABEL, fEditableLabelPaintState);
      editingCandidates.add(new TLspDomainObjectContext(fEditableLabelID.getDomainObject(), aView, (ILspInteractivePaintableLayer) fEditableLabelID.getLayer(), prs));
    }
    return editingCandidates;
  }

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {
    fHandleClick = false;
    super.handleAWTEvent(aEvent);
    // Make sure we can still select objects when clicking on an interactive label
    if (fHandleClick && getNextController() != null) {
      MouseEvent me = (MouseEvent) aEvent;

      // Generate a mouse pressed - released - clicked event chain to pass on to the select controller
      MouseEvent pressedEvent = new MouseEvent((Component) me.getSource(), MouseEvent.MOUSE_PRESSED, me.getWhen(), me.getModifiers(), me.getX(), me.getY(), 0, me.isPopupTrigger(), me.getButton());
      MouseEvent releasedEvent = new MouseEvent((Component) me.getSource(), MouseEvent.MOUSE_RELEASED, me.getWhen(), me.getModifiers(), me.getX(), me.getY(), 0, me.isPopupTrigger(), me.getButton());
      getNextController().handleAWTEvent(pressedEvent);
      getNextController().handleAWTEvent(releasedEvent);
      getNextController().handleAWTEvent(aEvent);
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aEvent) {
    AWTEvent event = super.handleAWTEventImpl(aEvent);
    if (fEditableLabelID != null && aEvent instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) aEvent;
      if (me.getID() == MouseEvent.MOUSE_CLICKED && me.getClickCount() == 1) {
        fHandleClick = true;
        return null;
      }
    }
    fHandleClick = false;
    return event;
  }

  private static class LabelHoveredListener extends ALcdWeakPropertyChangeListener<ASTERIXTrackNoSelectionEditController> {

    public LabelHoveredListener(ASTERIXTrackNoSelectionEditController aController) {
      super(aController);
    }

    @Override
    protected void propertyChangeImpl(ASTERIXTrackNoSelectionEditController aController, PropertyChangeEvent evt) {
      HoveredLabel oldValue = (HoveredLabel) evt.getNewValue();
      if (oldValue != null) {
        ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) oldValue.getLabelID().getLayer();
        layer.getObjectsWithPaintState(TLspPaintState.EDITED).remove(oldValue.getLabelID().getDomainObject());
      }
      HoveredLabel newValue = (HoveredLabel) evt.getNewValue();
      if (newValue != null) {
        aController.fEditableLabelID = newValue.getLabelID();
        aController.fEditableLabelPaintState = newValue.getPaintState();
        ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) newValue.getLabelID().getLayer();
        layer.getObjectsWithPaintState(TLspPaintState.EDITED).add(newValue.getLabelID().getDomainObject());
      } else {
        aController.fEditableLabelID = null;
        aController.fEditableLabelPaintState = null;
      }
    }
  }
}
