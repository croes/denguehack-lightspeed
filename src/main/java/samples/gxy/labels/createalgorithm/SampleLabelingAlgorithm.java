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
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYMultiLabelPriorityProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.util.TLcdGXYCollectedLabelInfoUtil;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.algorithm.discrete.ILcdLabelPlacementEvaluator;
import com.luciad.view.labeling.util.TLcdLabelingUtil;

/**
 * This labeling algorithm places each label on the associated domain object's anchor point,
 * unless it would overlap with another label. A consequence of this is that this algorithm
 * only supports a single label per object.
 */
public class SampleLabelingAlgorithm extends ALcdGXYDiscretePlacementsLabelingAlgorithm {

  private ILcdGXYMultiLabelPriorityProvider fLabelPriorityProvider = null;

  /**
   * Returns the label placing priority provider.
   * @return the label placing priority provider.
   */
  public ILcdGXYMultiLabelPriorityProvider getLabelPriorityProvider() {
    return fLabelPriorityProvider;
  }

  /**
   * Sets the label placing priority provider, specifying the priority for individual
   * labels. This priority provider specifies the order in which labels are placed.
   * In the event labels overlap, those with higher priority are painted on top of
   * labels with lower priority.  So lower priority labels are the first candidates
   * if labels need to be obscured.
   * <p>
   * When no label priority provider is set, no label priorities are set.
   * <p>
   * The priority provider is used to sort the list of labels before their placements are
   * computed.
   *
   * @param aLabelPriorityProvider The priority provider to set.
   */
  public void setLabelPriorityProvider(ILcdGXYMultiLabelPriorityProvider aLabelPriorityProvider) {
    fLabelPriorityProvider = aLabelPriorityProvider;
  }

  /**
   * Here we collect all painter and label painter information that we cannot directly access
   * in the compute step. This allows accessing the iterators in a separate label placement thread
   * (e.g. by TLcdGXYAsynchronousLabelPlacer).
   */
  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdCollectedLabelInfoList label_info = super.collectLabelInfo(aLabelToCollect, aGraphics, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addLabelLocationPrototypeDataSFCT(label_info, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addObjectAnchorPointDataSFCT(label_info, aGXYView, aGraphics);
    TLcdGXYCollectedLabelInfoUtil.addDimensionAndLabelAnchorOffsetDataSFCT(label_info, aGXYView, aGraphics);
    if (fLabelPriorityProvider != null) {
      // Priorities are added here, ALcdGXYDiscretePlacementsLabelingAlgorithm makes sure that the
      // labels are sorted based on these priorities before starting to place them.
      TLcdGXYCollectedLabelInfoUtil.addPrioritiesDataSFCT(label_info, fLabelPriorityProvider, aGXYView);
    }
    return label_info;
  }

  protected Iterator<TLcdCollectedLabelInfo> createLabelIterator(TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    return new MyLabelIterator(aLabelInfoList);
  }

  protected Iterator<TLcdLabelPlacement> createLabelPlacementIterator(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    return new MyLabelPlacementIterator(aLabel);
  }

  protected ILcdLabelPlacementEvaluator createLabelPlacementEvaluator(TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    return new MyLabelPlacementEvaluator();
  }

  private static class MyLabelIterator implements Iterator<TLcdCollectedLabelInfo> {

    private List<TLcdCollectedLabelInfo> fLabels;
    private int fNextLabelIndex = 0;

    public MyLabelIterator(TLcdCollectedLabelInfoList aLabelInfoList) {
      // ALcdGXYDiscretePlacementsLabelingAlgorithm has already sorted the labels, so we can assume
      // that TLcdCollectedLabelInfoList returns the labels in the correct order.
      fLabels = aLabelInfoList.getLabels();
    }

    public boolean hasNext() {
      return fNextLabelIndex < fLabels.size();
    }

    public TLcdCollectedLabelInfo next() {
      return fLabels.get(fNextLabelIndex++);
    }

    public void remove() {
      throw new UnsupportedOperationException("Removing labels is not supported for this Iterator : " + this);
    }
  }

  private static class MyLabelPlacementIterator implements Iterator<TLcdLabelPlacement> {

    private static final double LABEL_ROTATION = 0.0;
    private static final double SIN_ROTATION = Math.sin(LABEL_ROTATION);
    private static final double COS_ROTATION = Math.cos(LABEL_ROTATION);

    private TLcdCollectedLabelInfo fLabel;
    private int fPlacementCount = 0;

    public MyLabelPlacementIterator(TLcdCollectedLabelInfo aLabel) {
      fLabel = aLabel;
    }

    public boolean hasNext() {
      // There is only one placement per label. If this placement was already created, return false.
      return fPlacementCount == 0;
    }

    public TLcdLabelPlacement next() {
      fPlacementCount++;

      Dimension dimension = fLabel.getLabelDimension();
      Point label_anchor_offset = fLabel.getLabelAnchorOffset();
      Point object_anchor_point = fLabel.getLabeledObject().getObjectAnchorPoint();

      double half_w = dimension.getWidth() / 2.0;
      double half_h = dimension.getHeight() / 2.0;
      double middle_x = object_anchor_point.getX();
      double middle_y = object_anchor_point.getY();

      // Calculate the bounds of the label placement
      TLcdLabelPlacement placement = new TLcdLabelPlacement(fLabel);
      placement.setX((int) (middle_x - COS_ROTATION * half_w + SIN_ROTATION * half_h));
      placement.setY((int) (middle_y - COS_ROTATION * half_h - SIN_ROTATION * half_w));
      placement.setWidth(dimension.width);
      placement.setHeight(dimension.height);
      placement.setRotation(LABEL_ROTATION);

      // Create a TLcdLabelLocation based on these bounds
      TLcdLabelLocation label_location = fLabel.getLabelLocationPrototype();
      placement.setLabelLocation(label_location);
      TLcdLabelingUtil.adjustLabelLocationFromBoundsSFCT(object_anchor_point, label_anchor_offset, placement);

      return placement;
    }

    public void remove() {
      throw new UnsupportedOperationException("Removing label placements is not supported for this Iterator : " + this);
    }
  }

  private static class MyLabelPlacementEvaluator implements ILcdLabelPlacementEvaluator {

    public PlacementResult evaluatePlacement(TLcdLabelPlacement aLabelPlacement, ILcdLabelConflictChecker.Conflict aConflict, List<TLcdLabelPlacement> aPlacedLabels) {
      // No conflicts => add label
      if (aConflict == null) {
        return PlacementResult.SUCCESS;
      }

      // Partially overlap with view => add label afterwards if no better placement is found
      if (aConflict.isPartiallyOutsideView() && !aConflict.isOverlapWithLabelPlacements() && !aConflict.isOverlapWithLabelObstacles()) {
        return PlacementResult.FALLBACK;
      }

      return PlacementResult.TRY_NEW_PLACEMENT;
    }

    public void placementApplied(TLcdLabelPlacement aLabelPlacement, List<TLcdLabelPlacement> aPlacedLabels) {
      // No action necessary.
    }

    public void noPlacementApplied(TLcdCollectedLabelInfo aLabel, List<TLcdLabelPlacement> aLabels) {
      // No action necessary.
    }
  }
}
