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
package samples.gxy.common.labels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYMultiLabelPriorityProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.algorithm.discrete.ILcdLabelPlacementEvaluator;

/**
 * This TLcdGXYLocationListLabelingAlgorithm internally adds a label priority provider that
 * calculates priorities based on the order in which labels were placed in the previous labeling
 * iteration.
 */
public class LastPlacedPriorityLabelingAlgorithm extends TLcdGXYLocationListLabelingAlgorithm implements
                                                                                              ILcdGXYMultiLabelPriorityProvider {

  private Map<TLcdLabelIdentifier, Integer> fPriorityMap = new HashMap<TLcdLabelIdentifier, Integer>();
  private int fCurrentPriority = 0;

  public LastPlacedPriorityLabelingAlgorithm() {
    setLabelPriorityProvider(this);
  }

  @Override
  protected ILcdLabelPlacementEvaluator createLabelPlacementEvaluator(TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    fCurrentPriority = 0;
    fPriorityMap.clear();

    final ILcdLabelPlacementEvaluator delegate = super.createLabelPlacementEvaluator(aLabelInfoList, aBoundsConflictChecker, aView);
    return new ILcdLabelPlacementEvaluator() {
      public PlacementResult evaluatePlacement(TLcdLabelPlacement aLabelPlacement, ILcdLabelConflictChecker.Conflict aConflict, List<TLcdLabelPlacement> aPlacedLabels) {
        return delegate.evaluatePlacement(aLabelPlacement, aConflict, aPlacedLabels);
      }

      public void placementApplied(TLcdLabelPlacement aLabelPlacement, List<TLcdLabelPlacement> aPlacedLabels) {
        delegate.placementApplied(aLabelPlacement, aPlacedLabels);
        fPriorityMap.put(aLabelPlacement.getLabel().getLabelIdentifier(), fCurrentPriority++);
      }

      public void noPlacementApplied(TLcdCollectedLabelInfo aLabel, List<TLcdLabelPlacement> aPlacedLabels) {
        delegate.noPlacementApplied(aLabel, aPlacedLabels);
        fPriorityMap.remove(aLabel.getLabelIdentifier());
      }
    };
  }

  public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
    TLcdLabelIdentifier label = new TLcdLabelIdentifier(aGXYContext.getGXYLayer(), aObject, aLabelIndex, aSubLabelIndex);

    Integer priority = fPriorityMap.get(label);
    if (priority != null) {
      return priority;
    }

    return fCurrentPriority;
  }
}
