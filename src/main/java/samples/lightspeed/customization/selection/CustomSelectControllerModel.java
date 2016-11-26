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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.selection.ALspSelectInput;
import com.luciad.view.lightspeed.controller.selection.TLspSelectControllerModel;
import com.luciad.view.lightspeed.controller.selection.TLspSelectRectangleInput;
import com.luciad.view.lightspeed.layer.ALspBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspViewBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspWorldBoundsInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsBoundsQuery;

/**
 * This select controller model customizes the selection rectangle behavior:
 * it only retains selection candidates if their bounds are completely inside the selection rectangle.
 */
public class CustomSelectControllerModel extends TLspSelectControllerModel {

  @Override
  public List<TLspDomainObjectContext> selectionCandidates(ALspSelectInput aInput, Set<TLspPaintRepresentation> aRepresentations, boolean aMultiple, ILspView aView) {

    if (aInput instanceof TLspSelectRectangleInput) {
      List<TLspDomainObjectContext> result = new ArrayList<TLspDomainObjectContext>();
      TLcdXYBounds selectionBounds = new TLcdXYBounds(((TLspSelectRectangleInput) aInput).getRectangle());
      for (int i = 0; i < aView.layerCount(); i++) {
        if (aView.getLayer(i) instanceof ILspInteractivePaintableLayer) {
          ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aView.getLayer(i);
          for ( TLspPaintRepresentation paintRepresentation : aRepresentations) {
            addCandidatesForPaintRepresentationState(aView, selectionBounds, layer, TLspPaintRepresentationState.getInstance(paintRepresentation, TLspPaintState.REGULAR), result);
            addCandidatesForPaintRepresentationState(aView, selectionBounds, layer, TLspPaintRepresentationState.getInstance(paintRepresentation, TLspPaintState.EDITED), result);
            addCandidatesForPaintRepresentationState(aView, selectionBounds, layer, TLspPaintRepresentationState.getInstance(paintRepresentation, TLspPaintState.SELECTED), result);
          }
        }
      }
      return result;
    } else {
      return super.selectionCandidates(aInput, aRepresentations, aMultiple, aView);
    }
  }

  private void addCandidatesForPaintRepresentationState(ILspView aView,
                                                        TLcdXYBounds aSelectionBounds,
                                                        ILspInteractivePaintableLayer aLayer,
                                                        TLspPaintRepresentationState aPaintRepresentationState,
                                                        List<TLspDomainObjectContext> aResultSFCT) {
    Collection<ALspBoundsInfo> boundsInfos = aLayer.query(
        new TLspPaintedObjectsBoundsQuery(aPaintRepresentationState, aSelectionBounds, 0), new TLspContext(aLayer, aView));
    ILcd3DEditableBounds viewBounds = new TLcdXYZBounds();
    for (ALspBoundsInfo boundsInfo : boundsInfos) {
      if (boundsInfo instanceof ALspWorldBoundsInfo) {
        // this won't work that well in tilted 3D views; the world bounds will get bigger than necessary when transforming them to view bounds
        aView.getViewXYZWorldTransformation().worldBounds2viewSFCT(((ALspWorldBoundsInfo) boundsInfo).getWorldBounds(), viewBounds);
        if (aSelectionBounds.contains2D(viewBounds)) {
          aResultSFCT.add(new TLspDomainObjectContext(boundsInfo.getDomainObject(), aView, aLayer, aPaintRepresentationState));
        }
      } else if (boundsInfo instanceof ALspViewBoundsInfo) {
        if (aSelectionBounds.contains2D(((ALspViewBoundsInfo) boundsInfo).getViewBounds())) {
          aResultSFCT.add(new TLspDomainObjectContext(boundsInfo.getDomainObject(), aView, aLayer, aPaintRepresentationState));
        }
      }
    }
  }
}
