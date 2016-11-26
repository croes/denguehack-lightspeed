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

import com.luciad.util.TLcdConstant;
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
 * This labeling algorithm wrapper adds a specified amount of rotation to the labels
 * created by its delegate labeling algorithm.
 */
public class RotationAlgorithmWrapper extends ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper {

  private double fRotation;

  public RotationAlgorithmWrapper(ALcdGXYDiscretePlacementsLabelingAlgorithm aDelegate, double aRotation) {
    super(aDelegate);
    fRotation = aRotation;
  }

  public double getRotation() {
    return fRotation;
  }

  /**
   * Here we collect all painter and label painter information that we cannot directly access
   * in the compute step. This allows accessing the iterators in a separate label placement thread
   * (e.g. by TLcdGXYAsynchronousLabelPlacer).
   */
  @Override
  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdCollectedLabelInfoList label_infos = super.collectLabelInfo(aLabelToCollect, aGraphics, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addObjectAnchorPointDataSFCT(label_infos, aGXYView, aGraphics);
    TLcdGXYCollectedLabelInfoUtil.addDimensionAndLabelAnchorOffsetDataSFCT(label_infos, aGXYView, aGraphics);

    // We remove the previous label location. Since this previous label location already contains
    // a rotation added by this wrapper, it would apply the rotation twice. By removing the previous
    // location, we make sure that the wrapped algorithm doesn't use it. Another solution would be to
    // undo the rotation from the previous label placement.
    for (TLcdCollectedLabelInfo label : label_infos.getLabels()) {
      label.setPreviousLabelPlacement(null);
      label.setPreviousPainted(false);
    }

    return label_infos;
  }

  @Override
  protected Iterator<TLcdLabelPlacement> createLabelPlacementIterator(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    Iterator<TLcdLabelPlacement> delegate = super.createLabelPlacementIterator(aLabel, aLabelInfoList, aBoundsConflictChecker, aView);
    return new RotatedLabelPlacementIterator(delegate, this);
  }

  private static class RotatedLabelPlacementIterator implements Iterator<TLcdLabelPlacement> {

    private Iterator<TLcdLabelPlacement> fDelegate;
    private RotationAlgorithmWrapper fRotationWrapper;

    public RotatedLabelPlacementIterator(Iterator<TLcdLabelPlacement> aDelegate, RotationAlgorithmWrapper aRotationWrapper) {
      fDelegate = aDelegate;
      fRotationWrapper = aRotationWrapper;
    }

    public boolean hasNext() {
      return fDelegate.hasNext();
    }

    public TLcdLabelPlacement next() {
      TLcdLabelPlacement placement = fDelegate.next();
      TLcdLabelPlacement new_placement = placement.clone();

      // Find the middle of the bounds of the returned placement.
      double middle_x = placement.getX();
      double middle_y = placement.getY();

      middle_x += placement.getCosRotation() * 0.5 * placement.getWidth();
      middle_y += placement.getSinRotation() * 0.5 * placement.getWidth();

      middle_x -= placement.getSinRotation() * 0.5 * placement.getHeight();
      middle_y += placement.getCosRotation() * 0.5 * placement.getHeight();

      // Adjust the rotation of the label bounds.
      double original_rotation = placement.getRotation();
      double new_rotation = original_rotation + fRotationWrapper.getRotation() * TLcdConstant.DEG2RAD;
      new_placement.setRotation(new_rotation);

      // Rotate the upper left point around the middle of the unrotated bounds
      double new_x = middle_x + new_placement.getCosRotation() * (placement.getX() - middle_x) - new_placement.getSinRotation() * (placement.getY() - middle_y);
      double new_y = middle_y + new_placement.getSinRotation() * (placement.getX() - middle_x) + new_placement.getCosRotation() * (placement.getY() - middle_y);
      new_placement.setX((int) new_x);
      new_placement.setY((int) new_y);

      // Adjust the TLcdLabelLocation based on the adjusted bounds.
      Point object_anchor_point = placement.getLabel().getLabeledObject().getObjectAnchorPoint();
      Point label_anchor_offset = retrieveLabelAnchorOffset(new_placement);
      TLcdLabelingUtil.adjustLabelLocationFromBoundsSFCT(object_anchor_point, label_anchor_offset, new_placement);

      return new_placement;
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

    public void remove() {
      throw new UnsupportedOperationException("Removing label placements is not supported for this Iterator : " + this);
    }

  }

}
