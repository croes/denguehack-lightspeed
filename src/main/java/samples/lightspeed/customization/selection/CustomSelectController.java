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
package samples.lightspeed.customization.selection;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.luciad.gui.ILcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ALcdBounds;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.selection.ALspSelectInput;
import com.luciad.view.lightspeed.controller.selection.ILspSelectionCandidateHandler;
import com.luciad.view.lightspeed.controller.selection.TLspSelectChoice;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectMode;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.action.DialogSelectionHandler;
import samples.common.action.ShowPopupAction;

/**
 * An extension of TLspSelectController with some custom behaviour. The choose behaviour is modified
 * so a dialog opens up when a choose is detected. Furthermore, the rectangle selection behaviour
 * is always active. Last but not least, selecting with the left mouse button adds elements to the
 * selection, selecting with the right mouse button removes them.
 */
public class CustomSelectController extends TLspSelectController {

  public CustomSelectController() {
    super(new CustomSelectControllerModel());
    // add a custom selection candidate handler that shows a dialog.
    getControllerModel().setSelectionCandidateHandlerFor(TLspSelectChoice.CHOOSE,
                                                         new ILspSelectionCandidateHandler() {
                                                           @Override
                                                           public void handleSelectionCandidates(ALspSelectInput aInput, TLspSelectMode aMode, ILspView aView, List<TLspDomainObjectContext> aSelectionCandidates) {
                                                             if (aView instanceof ILspAWTView) {
                                                               List<TLcdDomainObjectContext> candidates = new ArrayList<TLcdDomainObjectContext>(aSelectionCandidates);
                                                               DialogSelectionHandler.handleSelectionCandidates(((ILspAWTView) aView).getHostComponent(), candidates);
                                                             }
                                                           }
                                                         }
    );
  }

  @Override
  public void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    addActions(aView);
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    super.terminateInteractionImpl(aView);
    removeActions();
  }

  private void addActions(ILspView aView) {
    if (aView instanceof ILspAWTView) {
      ILcdAction[] actions = new ILcdAction[1];
      actions[0] = new FlyToSelectionAction(aView);
      setContextAction(new ShowPopupAction(actions, ((ILspAWTView) aView).getHostComponent()));
    }
  }

  private void removeActions() {
    setContextAction(null);
  }

  @Override
  protected TLspSelectMode selectMode(MouseEvent aMouseEvent,
                                      ALspSelectInput aInput,
                                      Set<TLspPaintRepresentation> aRepresentations,
                                      TLspSelectChoice aChoice) {
    TLspSelectMode mode;
    // Add for left mouse button, remove for right mouse button.
    if (SwingUtilities.isLeftMouseButton(aMouseEvent)) {
      mode = TLspSelectMode.ADD;
    } else if (SwingUtilities.isRightMouseButton(aMouseEvent)) {
      mode = TLspSelectMode.REMOVE;
    } else {
      mode = TLspSelectMode.NO_CHANGE;
    }
    return mode;
  }

  @Override
  protected boolean isSelectByDraggedRectangle(MouseEvent aPressedEvent) {
    return !SwingUtilities.isMiddleMouseButton(aPressedEvent);
  }

  @Override
  protected boolean isContextClick(MouseEvent aReleasedEvent) {
    return SwingUtilities.isMiddleMouseButton(aReleasedEvent);
  }

  private static class FlyToSelectionAction extends ALcdObjectSelectionAction {

    private static final ILcdGeoReference WGS_84 = new TLcdGeodeticReference();

    private TLcdGeoReference2GeoReference fGeo2Geo;
    private TLcdLonLatHeightBounds fTempBounds = new TLcdLonLatHeightBounds();

    private static ILcdFilter<TLcdDomainObjectContext> createBoundedObjectFilter() {
      return new ILcdFilter<TLcdDomainObjectContext>() {
        private final TLcdGeoReference2GeoReference fGeo2Geo = new TLcdGeoReference2GeoReference();
        private TLcdLonLatHeightBounds fTempBounds = new TLcdLonLatHeightBounds();

        {
          fGeo2Geo.setDestinationReference(WGS_84);
        }

        @Override
        public boolean accept(TLcdDomainObjectContext aObject) {
          Object domainObject = aObject.getDomainObject();
          ILcdModel model = aObject.getModel();
          ILcdBounds bounds = ALcdBounds.fromDomainObject(domainObject);
          if (ALcdBounds.isDefined(bounds)) {
            try {
              fGeo2Geo.setSourceReference(model.getModelReference());
              fGeo2Geo.sourceBounds2destinationSFCT(bounds, fTempBounds);
              return true;
            } catch (TLcdNoBoundsException ignore) {
              return false;
            }
          }
          return true;
        }
      };
    }

    private FlyToSelectionAction(ILspView aView) {
      super(aView, createBoundedObjectFilter(), 1, -1, false);
      fGeo2Geo = new TLcdGeoReference2GeoReference();
      fGeo2Geo.setDestinationReference(WGS_84);
      setName("Fly to selection");
    }

    private ILcdBounds calculateBounds(List<TLcdDomainObjectContext> aSelectionCandidates) {
      TLcdLonLatBounds bounds = null;
      for (TLcdDomainObjectContext candidate : aSelectionCandidates) {
        if (candidate.getDomainObject() instanceof ILcdBounded) {
          try {
            fGeo2Geo.setSourceReference(candidate.getLayer().getModel().getModelReference());
            fGeo2Geo.sourceBounds2destinationSFCT(((ILcdBounded) candidate.getDomainObject()).getBounds(), fTempBounds);
            if (bounds == null) {
              bounds = new TLcdLonLatBounds(fTempBounds);
            } else {
              bounds.setTo2DUnion(fTempBounds);
            }
          } catch (TLcdNoBoundsException ignore) {
          }
        }
      }
      return bounds;
    }

    @Override
    protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
      try {
        TLspViewNavigationUtil flyTo = new TLspViewNavigationUtil((ILspView) aSelection.get(0).getView());
        flyTo.animatedFit(calculateBounds(aSelection), WGS_84);
      } catch (TLcdOutOfBoundsException e) {
        //ignore
      }
    }
  }

}
