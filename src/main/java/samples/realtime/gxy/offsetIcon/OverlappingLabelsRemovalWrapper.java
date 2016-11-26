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
package samples.realtime.gxy.offsetIcon;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelObstacle;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;

/**
 * This labeling algorithm wrapper removes all overlapping labels or labels that overlap with an
 * obstacle. It also removes labels that fall completely outside the view.
 *
 * This wrapper also makes sure that when a label is dropped, its children are also dropped
 * (see TLcdLabelLocation#getParentLabel()). This wrapper assumes that a label can only have
 * children of the first degree, i.e. : no children of children.
 */
public class OverlappingLabelsRemovalWrapper implements ILcdGXYLabelingAlgorithm {

  private ILcdGXYLabelingAlgorithm fDelegate;
  private boolean fEnabled = true;

  public OverlappingLabelsRemovalWrapper(ILcdGXYLabelingAlgorithm aAlgorithm) {
    fDelegate = aAlgorithm;
  }

  public void setEnabled(boolean aEnabled) {
    fEnabled = aEnabled;
  }

  @Override
  public OverlappingLabelsRemovalWrapper clone() {
    try {
      OverlappingLabelsRemovalWrapper clone = (OverlappingLabelsRemovalWrapper) super.clone();
      clone.fDelegate = (ILcdGXYLabelingAlgorithm) fDelegate.clone();
      return clone;
    } catch (CloneNotSupportedException ignore) {
      // Cannot happen.
      throw new RuntimeException("Cloning is not supported for this object.", ignore);
    }
  }

  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelsToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    return fDelegate.collectLabelInfo(aLabelsToCollect, aGraphics, aGXYView);
  }

  public List<TLcdLabelPlacement> computeLabelPlacements(TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aLabelConflictChecker, ILcdGXYView aView) {
    List<TLcdLabelPlacement> labels = fDelegate.computeLabelPlacements(aLabelInfoList, aLabelConflictChecker, aView);
    if (!fEnabled) {
      return labels;
    }

    // Initialize painted label buffer.
    List<TLcdLabelObstacle> obstacles = aLabelConflictChecker.getLabelObstacles();
    aLabelConflictChecker.reset(new Rectangle(0, 0, aView.getWidth(), aView.getHeight()));
    for (TLcdLabelObstacle obstacle : obstacles) {
      aLabelConflictChecker.addLabelObstacle(obstacle);
    }

    // Only add the labels that do not have conflicts.
    Map<TLcdLabelIdentifier, TLcdLabelPlacement> all_labels = new HashMap<TLcdLabelIdentifier, TLcdLabelPlacement>();
    List<TLcdLabelPlacement> result = new ArrayList<TLcdLabelPlacement>();
    for (TLcdLabelPlacement label : labels) {
      all_labels.put(label.getLabel().getLabelIdentifier(), label);

      // Add this label if it doesn't have too many conflicts.
      ILcdLabelConflictChecker.Conflict conflict = aLabelConflictChecker.getConflict(label);
      if (canAddLabel(conflict)) {
        result.add(label);
        aLabelConflictChecker.addLabelPlacement(label);
      } else {
        // Mark the placement as invisible.
        label.setVisible(false);
        result.add(label);
      }
    }

    // Also remove the children if the parent was removed
    for (TLcdLabelPlacement label : labels) {
      if (!label.isVisible()) {
        continue;
      }

      TLcdLabelIdentifier parent_identifier = label.getLabel().getParentLabelIdentifier();
      if (parent_identifier == null) {
        continue;
      }

      TLcdLabelPlacement parent_placement = all_labels.get(parent_identifier);
      if (!parent_placement.isVisible()) {
        // Also set its child to invisible
        label.setVisible(false);
      }
    }

    return result;
  }

  private boolean canAddLabel(ILcdLabelConflictChecker.Conflict aConflict) {
    return aConflict == null ||
           !aConflict.isOverlapWithLabelPlacements() &&
           !aConflict.isOutsideView() &&
           !aConflict.isOverlapWithLabelObstacles();
  }
}
