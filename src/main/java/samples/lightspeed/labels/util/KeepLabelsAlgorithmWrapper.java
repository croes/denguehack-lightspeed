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
package samples.lightspeed.labels.util;

import java.util.List;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.label.ILspLabelConflictChecker;
import com.luciad.view.lightspeed.label.TLspLabelPlacement;
import com.luciad.view.lightspeed.label.algorithm.discrete.ALspDiscreteLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.discrete.ALspDiscreteLabelingAlgorithmWrapper;
import com.luciad.view.lightspeed.label.algorithm.discrete.ILspLabelPlacementEvaluator;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;

/**
 * This labeling algorithm wrapper makes sure that labels are not removed when they are located outside the
 * view border. This is for example useful to make sure that the label pin remains visible when the label
 * moves out of the view, but the object it is attached to is still in it.
 */
public class KeepLabelsAlgorithmWrapper extends ALspDiscreteLabelingAlgorithmWrapper {

  public KeepLabelsAlgorithmWrapper(ALspDiscreteLabelingAlgorithm aDelegate) {
    super(aDelegate);
  }

  @Override
  protected ILspLabelPlacementEvaluator createLabelPlacementEvaluator(List<TLspLabelID> aLabels, LabelContext aLabelContext, ILspLabelConflictChecker aConflictChecker, ILspView aView) {
    final ILspLabelPlacementEvaluator delegate = super.createLabelPlacementEvaluator(aLabels, aLabelContext, aConflictChecker, aView);
    return new ILspLabelPlacementEvaluator() {
      @Override
      public PlacementResult evaluatePlacement(TLspLabelPlacement aPlacement, ILspLabelConflictChecker.Conflict aConflict, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
        if (aConflict != null && (aConflict.isOutsideView() || aConflict.isPartiallyOutsideView())) {
          if (aConflict.isOverlapWithObstacles() || aConflict.isOverlapWithPlacements()) {
            aConflict = new ILspLabelConflictChecker.Conflict(false, false, aConflict.isOverlapWithPlacements(), aConflict.isOverlapWithObstacles());
          } else {
            aConflict = null;
          }
        }
        return delegate.evaluatePlacement(aPlacement, aConflict, aPlacements, aCurrentLabelLocations);
      }

      @Override
      public void placementApplied(TLspLabelPlacement aPlacement, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
        delegate.placementApplied(aPlacement, aPlacements, aCurrentLabelLocations);
      }

      @Override
      public void noPlacementApplied(TLspLabelID aLabel, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
        delegate.noPlacementApplied(aLabel, aPlacements, aCurrentLabelLocations);
      }
    };
  }
}
