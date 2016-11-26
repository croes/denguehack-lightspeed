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
package samples.gxy.labels.createalgorithm;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper;
import com.luciad.view.gxy.labeling.util.TLcdGXYCollectedLabelInfoUtil;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.util.TLcdLabelingUtil;

/**
 * This wrapper adds more possibilities when placing labels by offsetting the original placement
 * in four directions and trying them.
 */
public class MorePositionsAlgorithmWrapper extends ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper {

  public MorePositionsAlgorithmWrapper(ALcdGXYDiscretePlacementsLabelingAlgorithm aDelegate) {
    super(aDelegate);
  }

  @Override
  /**
   * Here we collect all painter and label painter information that we cannot directly access
   * in the compute step. This allows accessing the iterators in a separate label placement thread
   * (e.g. by TLcdGXYAsynchronousLabelPlacer).
   */
  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdCollectedLabelInfoList label_infos = super.collectLabelInfo(aLabelToCollect, aGraphics, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addObjectAnchorPointDataSFCT(label_infos, aGXYView, aGraphics);
    TLcdGXYCollectedLabelInfoUtil.addDimensionAndLabelAnchorOffsetDataSFCT(label_infos, aGXYView, aGraphics);

    // We remove the previous label location. Since this previous label location already contains
    // a placement shift added by this wrapper, it would apply the shift twice. By removing the
    // previous location, we make sure that the wrapped algorithm doesn't use it. Another solution
    // would be to undo the placement shift from the previous label placement.
    for (TLcdCollectedLabelInfo label : label_infos.getLabels()) {
      label.setPreviousLabelPlacement(null);
      label.setPreviousPainted(false);
    }

    return label_infos;
  }

  @Override
  public Iterator<TLcdLabelPlacement> createLabelPlacementIterator(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    Iterator<TLcdLabelPlacement> delegate = super.createLabelPlacementIterator(aLabel, aLabelInfoList, aBoundsConflictChecker, aView);
    return new MorePositionsIterator(delegate);
  }

  private static class MorePositionsIterator implements Iterator<TLcdLabelPlacement> {

    private Iterator<TLcdLabelPlacement> fDelegate;
    private TLcdLabelPlacement fNextPlacement = null;
    private int fNextPlacementIndex = 0;
    private TLcdLabelPlacement fOriginalPlacement;

    public MorePositionsIterator(Iterator<TLcdLabelPlacement> aDelegate) {
      fDelegate = aDelegate;
    }

    public boolean hasNext() {
      fNextPlacement = getNextPlacement();
      return fNextPlacement != null;
    }

    public TLcdLabelPlacement next() {
      TLcdLabelPlacement next_placement = fNextPlacement;
      fNextPlacement = null;
      return next_placement;
    }

    public void remove() {
      throw new UnsupportedOperationException("Removing label placements is not supported for this Iterator : " + this);
    }

    private TLcdLabelPlacement getNextPlacement() {
      if (fNextPlacement != null) {
        return fNextPlacement;
      }

      int placement_index = fNextPlacementIndex;
      fNextPlacementIndex++;

      if (placement_index == 0) {
        // Try the original placement.
        if (!fDelegate.hasNext()) {
          return null;
        }
        fOriginalPlacement = fDelegate.next();
        return fOriginalPlacement;
      } else if (placement_index >= 1 && placement_index <= 4) {
        TLcdLabelPlacement new_placement = fOriginalPlacement.clone();

        // Calculate how much the label will be offset in the x and y direction.
        double offset_x = 0;
        double offset_y = 0;
        double offset_size = 20;
        if (placement_index == 1) {
          offset_x = offset_size;
          offset_y = 0;
        } else if (placement_index == 2) {
          offset_x = -offset_size;
          offset_y = 0;
        } else if (placement_index == 3) {
          offset_x = 0;
          offset_y = -offset_size;
        } else if (placement_index == 4) {
          offset_x = 0;
          offset_y = offset_size;
        }

        double dx = fOriginalPlacement.getCosRotation() * offset_x - fOriginalPlacement.getSinRotation() * offset_y;
        double dy = fOriginalPlacement.getSinRotation() * offset_x + fOriginalPlacement.getCosRotation() * offset_y;

        // Adjust the label placement bounds by applying the calculated offset
        new_placement.setX(new_placement.getX() + (int) dx);
        new_placement.setY(new_placement.getY() + (int) dy);

        // Adjust the TLcdLabelLocation based on the adjusted placement bounds.
        Point object_anchor_point = fOriginalPlacement.getLabel().getLabeledObject().getObjectAnchorPoint();
        Point label_anchor_offset = retrieveLabelAnchorOffset(new_placement);
        TLcdLabelingUtil.adjustLabelLocationFromBoundsSFCT(object_anchor_point, label_anchor_offset, new_placement);

        return new_placement;
      } else {
        fNextPlacementIndex = 0;
        return getNextPlacement();
      }
    }

    private Point retrieveLabelAnchorOffset(TLcdLabelPlacement aPlacement) {
      // Some other wrapper may have modified the dimension => adjust for new dimension.
      Point label_anchor_offset = new Point(aPlacement.getLabel().getLabelAnchorOffset());
      Dimension original_dimension = aPlacement.getLabel().getLabelDimension();
      double ratio_w = (double) aPlacement.getWidth() / original_dimension.getWidth();
      double ratio_h = (double) aPlacement.getHeight() / original_dimension.getHeight();
      label_anchor_offset.setLocation(label_anchor_offset.getX() * ratio_w, label_anchor_offset.getY() * ratio_h);
      return label_anchor_offset;
    }
  }

}
