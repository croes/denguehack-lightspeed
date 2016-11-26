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
package samples.lightspeed.common.controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.shape.ALcdBounds;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.selection.TLspSelectControllerModel;
import com.luciad.view.lightspeed.controller.selection.TLspSelectPointInput;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

/**
 * An action that selects an object and triggers a animated fly to to fit on its bounds.
 *
 * @since 2012.0
 */
public class FlyToObjectAction extends ALcdAction {

  private ILspView fView;
  private TLspSelectControllerModel fSelectControllerModel = new TLspSelectControllerModel();

  public FlyToObjectAction(ILspView aView) {
    fView = aView;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e instanceof TLcdActionAtLocationEvent) {
      // Find the list of potential selection candidates based on the current location.
      Point location = ((TLcdActionAtLocationEvent) e).getLocation();
      List<TLspDomainObjectContext> candidates = fSelectControllerModel.selectionCandidates(
          new TLspSelectPointInput(location),
          Collections.singleton(TLspPaintRepresentation.BODY),
          false,
          fView);

      // Fly to the first candidate that is selectable and is spatially bounded.
      for (TLspDomainObjectContext candidate : candidates) {
        if (candidate.getLayer().isSelectable()) {
          ILcdBounds bounds = ALcdBounds.fromDomainObject(candidate.getObject());
          if (ALcdBounds.isDefined(bounds)) {
            clearSelection(fView);
            candidate.getLayer().selectObject(candidate.getObject(), true, ILcdFireEventMode.FIRE_NOW);
            try {
              TLspViewNavigationUtil flyTo = new TLspViewNavigationUtil(fView);
              flyTo.animatedFitOnModelBounds(bounds, candidate.getLayer().getModel().getModelReference());
              return;
            } catch (TLcdOutOfBoundsException e1) {
              // ignore
            }
          }
        }
      }
    }
  }

  private void clearSelection(ILspView aView) {
    for (int i = 0; i < aView.layerCount(); i++) {
      ILspLayer layer = aView.getLayer(i);

      if (layer.isSelectableSupported()) {
        layer.clearSelection(ILcdFireEventMode.FIRE_NOW);
      }
    }
  }
}
