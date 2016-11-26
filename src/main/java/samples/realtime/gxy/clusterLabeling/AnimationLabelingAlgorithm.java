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
package samples.realtime.gxy.clusterLabeling;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;
import com.luciad.view.gxy.labeling.util.TLcdGXYCollectedLabelInfoUtil;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.util.TLcdLabelingUtil;

/**
 * This labeling algorithm can be used to animate labels from one location to an other. It has two
 * modes : 'start animation' or 'return animation'. The start location is in both cases determined
 * by the current label location. When the the labeling algorithm is a 'start animation', the end
 * location is determined by a ConcentricCircleLabelLocationsCalculator. When the labeling algorithm is a
 * 'return animation', the end location is set to the start location of the 'start animation'.
 * <p>
 * A duration is specified to control the speed of the animation.
 */
class AnimationLabelingAlgorithm implements ILcdGXYLabelingAlgorithm {

  private ConcentricCircleLabelLocationsCalculator fCalculator;

  private Map<TLcdLabelIdentifier, LocationInfo> fStartAnimationInfo = new HashMap<TLcdLabelIdentifier, LocationInfo>();
  private Map<TLcdLabelIdentifier, LocationInfo> fReturnAnimationInfo = new HashMap<TLcdLabelIdentifier, LocationInfo>();

  private boolean fReturning = false;
  private boolean fInitialized = false;

  private long fStartTime;
  private long fDuration;

  public AnimationLabelingAlgorithm(ConcentricCircleLabelLocationsCalculator aCalculator) {
    fCalculator = aCalculator;
  }

  public void beginStartAnimation(long aDuration) {
    fReturning = false;
    fInitialized = false;
    fDuration = aDuration;
    fStartTime = System.currentTimeMillis();
  }

  public void beginReturnAnimation(long aDuration) {
    fReturning = true;
    fInitialized = false;
    fDuration = aDuration;
    fStartTime = System.currentTimeMillis();
  }

  public AnimationLabelingAlgorithm clone() {
    try {
      AnimationLabelingAlgorithm clone = (AnimationLabelingAlgorithm) super.clone();
      clone.fStartAnimationInfo = new HashMap<TLcdLabelIdentifier, LocationInfo>(this.fStartAnimationInfo);
      clone.fReturnAnimationInfo = new HashMap<TLcdLabelIdentifier, LocationInfo>(this.fReturnAnimationInfo);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Cloning is not supported for this object : " + this);
    }
  }

  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelsToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdCollectedLabelInfoList label_infos = TLcdGXYCollectedLabelInfoUtil.createCollectedLabelInfoList(aLabelsToCollect, aGXYView);

    // Info needed to calculate free placement label locations
    TLcdGXYCollectedLabelInfoUtil.addLabelLocationPrototypeDataSFCT(label_infos, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addDimensionAndLabelAnchorOffsetDataSFCT(label_infos, aGXYView, aGraphics);
    TLcdGXYCollectedLabelInfoUtil.addObjectAnchorPointDataSFCT(label_infos, aGXYView, aGraphics);

    // Needed to make sure the parent bounds are resolved correctly
    TLcdGXYCollectedLabelInfoUtil.addParentLabelIdentifierDataSFCT(label_infos, aGXYView);

    // Calculate the previous label locations for all labels. These are used to calculate the
    // start locations of the animation.
    initializePreviousInfo(label_infos, aGXYView);

    // Calculate the start and end positions.
    if (!fInitialized) {
      if (fReturning) {
        initializeReturnAnimation(label_infos, aGraphics, aGXYView);
      } else {
        initializeStartAnimation(label_infos, aGraphics, aGXYView);
      }
      fInitialized = true;
    }

    return label_infos;
  }

  private void initializePreviousInfo(TLcdCollectedLabelInfoList aLabelInfosSFCT, ILcdGXYView aGXYView) {
    for (TLcdCollectedLabelInfo label : aLabelInfosSFCT.getLabels()) {
      TLcdCollectedLabeledObjectInfo labeled_object = label.getLabeledObject();

      if (!(labeled_object.getLayer() instanceof ILcdGXYEditableLabelsLayer)) {
        return;
      }
      ALcdLabelLocations label_locations = ((ILcdGXYEditableLabelsLayer) labeled_object.getLayer()).getLabelLocations();

      TLcdLabelLocation previous_location = label_locations.createLabelLocation();
      boolean was_present = label_locations.getLabelLocationSFCT(label.getLabelIdentifier().getDomainObject(), label.getLabelIdentifier().getLabelIndex(), label.getLabelIdentifier().getSubLabelIndex(), aGXYView, previous_location);
      if (!was_present) {
        previous_location = null;
      }

      boolean was_painted = label_locations.isPainted(label.getLabelIdentifier().getDomainObject(), label.getLabelIdentifier().getLabelIndex(), label.getLabelIdentifier().getSubLabelIndex(), aGXYView);

      TLcdLabelPlacement previous_placement = null;
      if (previous_location != null) {
        previous_placement = new TLcdLabelPlacement(label);
        previous_placement.setLabelLocation(previous_location);
      }
      label.setPreviousLabelPlacement(previous_placement);
      label.setPreviousPainted(was_painted);
    }
  }

  private void initializeStartAnimation(TLcdCollectedLabelInfoList aLabelInfoList, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdGXYContext context = new TLcdGXYContext();

    for (TLcdCollectedLabelInfo label_info : aLabelInfoList.getLabels()) {
      if (context.getGXYLayer() != label_info.getLabeledObject().getLayer()) {
        context.resetFor((ILcdGXYLayer) label_info.getLabeledObject().getLayer(), aGXYView);
      }

      // The start location is found using the current label location.
      Point start_label_anchor_point = new Point();
      calculateStartLabelAnchorPoint(start_label_anchor_point, label_info, context, aGraphics);

      // The end location is determined by the ConcentricCircleLabelLocationsCalculator
      Point end_label_anchor_point = new Point();
      if (!fCalculator.calculateLabelLocationSFCT(label_info, aGXYView, aGraphics, end_label_anchor_point)) {
        throw new RuntimeException("Could not calculate a end location for label : " + label_info.getLabelIdentifier());
      }

      LocationInfo location_info = new LocationInfo(start_label_anchor_point, end_label_anchor_point);
      fStartAnimationInfo.put(label_info.getLabelIdentifier(), location_info);
    }
  }

  private void initializeReturnAnimation(TLcdCollectedLabelInfoList aLabelInfoList, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdGXYContext context = new TLcdGXYContext();

    for (TLcdCollectedLabelInfo label_info : aLabelInfoList.getLabels()) {
      if (context.getGXYLayer() != label_info.getLabeledObject().getLayer()) {
        context.resetFor((ILcdGXYLayer) label_info.getLabeledObject().getLayer(), aGXYView);
      }

      // The start location is found using the current label location.
      Point start_label_anchor_point = new Point();
      calculateStartLabelAnchorPoint(start_label_anchor_point, label_info, context, aGraphics);

      // The end location is the start location of the start animation
      LocationInfo start_animation_location_info = fStartAnimationInfo.get(label_info.getLabelIdentifier());
      Point end_label_anchor_point = start_animation_location_info == null ? null : start_animation_location_info.getStartLabelAnchorPoint();
      if (end_label_anchor_point == null) {
        end_label_anchor_point = new Point(start_label_anchor_point);
      }

      LocationInfo location_info = new LocationInfo(start_label_anchor_point, end_label_anchor_point);
      fReturnAnimationInfo.put(label_info.getLabelIdentifier(), location_info);
    }
  }

  private void calculateStartLabelAnchorPoint(Point aPointSFCT, TLcdCollectedLabelInfo aLabelInfo, ILcdGXYContext aGXYContext, Graphics aGraphics) {
    try {
      ILcdGXYLabelPainter label_painter = aGXYContext.getGXYLayer().getGXYLabelPainter(aLabelInfo.getLabelIdentifier().getDomainObject());
      if (!(label_painter instanceof ILcdGXYLabelPainter2)) {
        throw new RuntimeException("Label painter should be a ILcdGXYLabelPainter2");
      }

      // Retrieve either the previous label location, or a new label location (resolved).
      TLcdLabelPlacement previous_placement = aLabelInfo.getPreviousLabelPlacement();
      TLcdLabelLocation location = previous_placement == null ? null : previous_placement.getLabelLocation();
      if (location == null || !aLabelInfo.isPreviousPainted()) {
        location = aLabelInfo.getLabelLocationPrototype();
        if (!TLcdGXYCollectedLabelInfoUtil.resolveParentBoundsSFCT(location, aGXYContext, aGraphics, retrieveLabelPainterMode(aLabelInfo))) {
          location = null;
        }
      }

      if (location == null) {
        throw new RuntimeException("No start location for label : " + aLabelInfo.getLabelIdentifier());
      }

      ILcdGXYLabelPainter2 label_painter2 = (ILcdGXYLabelPainter2) label_painter;
      label_painter2.setLabelIndex(aLabelInfo.getLabelIdentifier().getLabelIndex());
      label_painter2.setSubLabelIndex(aLabelInfo.getLabelIdentifier().getSubLabelIndex());
      label_painter2.setLabelLocation(location);
      label_painter2.labelAnchorPointSFCT(aGraphics, retrieveLabelPainterMode(aLabelInfo), aGXYContext, aPointSFCT);

      // The label anchor point is stored relative to the object anchor point.
      Point object_anchor_point = aLabelInfo.getLabeledObject().getObjectAnchorPoint();
      aPointSFCT.x -= object_anchor_point.x;
      aPointSFCT.y -= object_anchor_point.y;
    } catch (TLcdNoBoundsException e) {
      throw new RuntimeException("Could not calculate start location", e);
    }
  }

  private static int retrieveLabelPainterMode(TLcdCollectedLabelInfo aLabel) {
    if (aLabel.getLabeledObject().isObjectSelected() == null) {
      return ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT;
    } else if (aLabel.getLabeledObject().isObjectSelected()) {
      return ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.SELECTED;
    } else {
      return ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT;
    }
  }

  public List<TLcdLabelPlacement> computeLabelPlacements(TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aLabelConflictChecker, ILcdGXYView aView) {
    List<TLcdLabelPlacement> placements = new ArrayList<TLcdLabelPlacement>();
    Map<TLcdLabelIdentifier, TLcdLabelPlacement> placement_map = new HashMap<TLcdLabelIdentifier, TLcdLabelPlacement>();

    long current_time = System.currentTimeMillis();
    double fraction = (double) (current_time - fStartTime) / (double) fDuration;

    Iterator<TLcdCollectedLabelInfo> label_info_iterator = new ParentAwareLabelIterator(aLabelInfoList);
    while (label_info_iterator.hasNext()) {
      TLcdCollectedLabelInfo label_info = label_info_iterator.next();

      LocationInfo location_info;
      if (fReturning) {
        location_info = fReturnAnimationInfo.get(label_info.getLabelIdentifier());
      } else {
        location_info = fStartAnimationInfo.get(label_info.getLabelIdentifier());
      }
      if (location_info == null) {
        continue;
      }

      Point object_anchor_point = label_info.getLabeledObject().getObjectAnchorPoint();
      Point label_anchor_offset = label_info.getLabelAnchorOffset();
      Dimension label_dimension = label_info.getLabelDimension();
      if (label_anchor_offset == null || label_dimension == null || object_anchor_point == null) {
        continue;
      }

      Point current_label_anchor_point = new Point();
      location_info.getCurrentPoint(fraction, current_label_anchor_point);

      Rectangle bounds = new Rectangle();
      double rotation = 0.0;
      bounds.x = current_label_anchor_point.x + object_anchor_point.x - label_anchor_offset.x;
      bounds.y = current_label_anchor_point.y + object_anchor_point.y - label_anchor_offset.y;
      bounds.width = label_dimension.width;
      bounds.height = label_dimension.height;

      TLcdLabelLocation location = label_info.getLabelLocationPrototype();
      TLcdLabelingUtil.adjustLabelLocationFromBoundsSFCT(object_anchor_point, label_anchor_offset, bounds.x, bounds.y, rotation, location);
      if (!resolveLabelLocation(location, label_info, placement_map)) {
        continue;
      }

      TLcdLabelPlacement label_placement = new TLcdLabelPlacement(label_info);
      label_placement.setLabelLocation(location);
      label_placement.setBounds(bounds, rotation);
      placement_map.put(label_info.getLabelIdentifier(), label_placement);
      placements.add(label_placement);
      aLabelConflictChecker.addLabelPlacement(label_placement);
    }

    return placements;
  }

  private boolean resolveLabelLocation(TLcdLabelLocation aLocation, TLcdCollectedLabelInfo aLabel, Map<TLcdLabelIdentifier, TLcdLabelPlacement> aPlacements) {
    if (aLabel.getParentLabelIdentifier() == null) {
      return true;
    }

    TLcdLabelPlacement parent_placement = aPlacements.get(aLabel.getParentLabelIdentifier());
    if (parent_placement == null) {
      return false;
    }

    Rectangle parent_bounds = new Rectangle(parent_placement.getX(), parent_placement.getY(), parent_placement.getWidth(), parent_placement.getHeight());
    double parent_rotation = parent_placement.getRotation();

    Point object_anchor_point = aLabel.getLabeledObject().getObjectAnchorPoint();
    if (object_anchor_point == null) {
      return false;
    }

    parent_bounds.x -= object_anchor_point.x;
    parent_bounds.y -= object_anchor_point.y;

    aLocation.setParentBounds(parent_bounds, parent_rotation);
    Point new_anchor_point = new Point();
    if (!aLocation.getAnchorPoint(new_anchor_point)) {
      return false;
    }

    aLocation.setLocationX(aLocation.getLocationX() - new_anchor_point.x);
    aLocation.setLocationY(aLocation.getLocationY() - new_anchor_point.y);

    return true;
  }

  private static class LocationInfo {

    // The start and the end anchor point, defined relative to the object anchor point.
    private Point fStartLabelAnchorPoint;
    private Point fEndLabelAnchorPoint;

    private LocationInfo(Point aStartLabelAnchorPoint, Point aEndLabelAnchorPoint) {
      fStartLabelAnchorPoint = aStartLabelAnchorPoint;
      fEndLabelAnchorPoint = aEndLabelAnchorPoint;
    }

    public void getCurrentPoint(double aFraction, Point aPointSFCT) {
      if (aFraction <= 0.0) {
        aPointSFCT.setLocation(fStartLabelAnchorPoint);
      } else if (aFraction >= 1.0) {
        aPointSFCT.setLocation(fEndLabelAnchorPoint);
      } else {
        aPointSFCT.x = fStartLabelAnchorPoint.x + (int) (aFraction * (fEndLabelAnchorPoint.x - fStartLabelAnchorPoint.x));
        aPointSFCT.y = fStartLabelAnchorPoint.y + (int) (aFraction * (fEndLabelAnchorPoint.y - fStartLabelAnchorPoint.y));
      }
    }

    public Point getStartLabelAnchorPoint() {
      return fStartLabelAnchorPoint;
    }
  }

  /*
  * This iterator makes sure that parent labels are iterated before their children. This is
  * needed because during the computing of label placements, the children depend on their
  * parents to retrieve a correct label location.
  */
  private static class ParentAwareLabelIterator implements Iterator<TLcdCollectedLabelInfo> {

    private List<TLcdCollectedLabelInfo> fLabels;
    private Map<TLcdLabelIdentifier, List<TLcdCollectedLabelInfo>> fQueue = new HashMap<TLcdLabelIdentifier, List<TLcdCollectedLabelInfo>>();
    private Set<TLcdLabelIdentifier> fReturned = new HashSet<TLcdLabelIdentifier>();
    private TLcdCollectedLabelInfo fNextLabel;

    private ParentAwareLabelIterator(TLcdCollectedLabelInfoList aLabelInfoList) {
      fLabels = aLabelInfoList.getLabels();
    }

    public boolean hasNext() {
      if (fLabels.size() == 0) {
        return false;
      }
      TLcdCollectedLabelInfo label = fLabels.remove(0);

      if (label.getParentLabelIdentifier() == null || fReturned.contains(label.getParentLabelIdentifier())) {
        fReturned.add(label.getLabelIdentifier());
        // Remove all queued labels that depend on this label from the queue
        List<TLcdCollectedLabelInfo> queued = fQueue.remove(label.getLabelIdentifier());
        if (queued != null) {
          // Add all queued labels to the front of the labels list
          while (queued.size() > 0) {
            TLcdCollectedLabelInfo label_info = queued.remove(queued.size() - 1);
            fLabels.add(0, label_info);
          }
        }
        fNextLabel = label;
        return true;
      }

      // Queue this label until the parent is placed
      List<TLcdCollectedLabelInfo> parent_queue = fQueue.get(label.getParentLabelIdentifier());
      if (parent_queue == null) {
        parent_queue = new ArrayList<TLcdCollectedLabelInfo>();
        fQueue.put(label.getParentLabelIdentifier(), parent_queue);
      }
      parent_queue.add(label);

      return hasNext();
    }

    public TLcdCollectedLabelInfo next() {
      return fNextLabel;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
