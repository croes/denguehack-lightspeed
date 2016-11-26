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
package samples.realtime.lightspeed.clusterLabeling;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import samples.lightspeed.labels.util.LspLabelPainterUtil;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelConflictChecker;
import com.luciad.view.lightspeed.label.ILspLabelPlacer;
import com.luciad.view.lightspeed.label.TLspLabelPlacement;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.location.ALspLabelLocation;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;
import com.luciad.view.lightspeed.painter.label.location.TLspStampLabelLocation;

/**
 * <p>This labeling algorithm can be used to animate labels from one location to an other. It has two
 * modes : 'start animation' or 'return animation'. The start location is in both cases determined
 * by the current label location. When the the labeling algorithm is a 'start animation', the end
 * location is determined by a ConcentricCircleLabelLocationsCalculator. When the labeling algorithm is a
 * 'return animation', the end location is set to the start location of the 'start animation'.</p>
 *
 * <p>A duration is specified to control the speed of the animation.</p>
 */
class AnimationLabelingAlgorithm implements ILspLabelingAlgorithm {

  private ConcentricCircleLabelLocationsCalculator fCalculator;

  private Map<TLspLabelID, LocationInfo> fStartAnimationInfo = new HashMap<TLspLabelID, LocationInfo>();
  private Map<TLspLabelID, LocationInfo> fReturnAnimationInfo = new HashMap<TLspLabelID, LocationInfo>();

  private boolean fReturning = false;
  private boolean fInitialized = false;

  private long fStartTime;
  private long fDuration;

  public AnimationLabelingAlgorithm(ConcentricCircleLabelLocationsCalculator aCalculator) {
    fCalculator = aCalculator;
  }

  public void beginStartAnimation(long aStartAnimationDuration) {
    fReturning = false;
    fInitialized = false;
    fDuration = aStartAnimationDuration;
    fStartTime = System.currentTimeMillis();
  }

  public void beginReturnAnimation(long aReturnAnimationDuration) {
    fReturning = true;
    fInitialized = false;
    fDuration = aReturnAnimationDuration;
    fStartTime = System.currentTimeMillis();
  }

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  @Override
  public AnimationLabelingAlgorithm clone() {
    try {
      AnimationLabelingAlgorithm clone = (AnimationLabelingAlgorithm) super.clone();
      clone.fStartAnimationInfo = new HashMap<TLspLabelID, LocationInfo>(this.fStartAnimationInfo);
      clone.fReturnAnimationInfo = new HashMap<TLspLabelID, LocationInfo>(this.fReturnAnimationInfo);
      clone.fInitialized = false;
      return clone;
    } catch (CloneNotSupportedException e) {
      // Shouldn't happen
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TLspLabelPlacement> placeLabels(List<TLspLabelID> aLabelIDs, LabelContext aLabelContext, ILspLabelConflictChecker aConflictChecker, ILspView aView) {
    // Calculate the start and end positions.
    if (!fInitialized) {
      if (fReturning) {
        initializeReturnAnimation(aLabelIDs, aLabelContext, aView);
      } else {
        initializeStartAnimation(aLabelIDs, aLabelContext, aView);
      }
      fInitialized = true;
    }

    List<TLspLabelPlacement> placements = new ArrayList<TLspLabelPlacement>();

    long currentTime = System.currentTimeMillis();
    double fraction = (double) (currentTime - fStartTime) / (double) fDuration;

    ALspLabelLocations labelLocations = getLabelLocations(aView);

    Iterator<TLspLabelID> labelIterator = new ParentAwareLabelIterator(aLabelIDs, aLabelContext, aView);
    while (labelIterator.hasNext()) {
      TLspLabelID label = labelIterator.next();

      LocationInfo locationInfo;
      if (fReturning) {
        locationInfo = fReturnAnimationInfo.get(label);
      } else {
        locationInfo = fStartAnimationInfo.get(label);
      }
      if (locationInfo == null) {
        continue;
      }

      ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(label);

      TLspPaintState paintState = aLabelContext.getPaintState(label, aView);
      TLspContext context = aLabelContext.getContext(label.getLayer(), aView);

      try {
        ILcd3DEditablePoint objectAnchorPoint = new TLcdXYZPoint();
        labelPainter.viewObjectAnchorPointSFCT(label, labelLocations, paintState, context, objectAnchorPoint);

        Dimension labelDimension = new Dimension();
        labelPainter.labelDimensionSFCT(label, paintState, context, labelDimension);

        TLcdXYPoint labelAnchorOffset = new TLcdXYPoint();
        labelPainter.labelAnchorPointOffsetSFCT(label, labelDimension, paintState, context, labelAnchorOffset);

        ILcd2DEditablePoint currentLabelAnchorPoint = new TLcdXYPoint();
        locationInfo.getCurrentPoint(fraction, currentLabelAnchorPoint);

        // Calculate the view bounds of the label
        double rotation = 0.0;
        double x = currentLabelAnchorPoint.getX() + objectAnchorPoint.getX() - labelAnchorOffset.getX();
        double y = currentLabelAnchorPoint.getY() + objectAnchorPoint.getY() - labelAnchorOffset.getY();
        double w = labelDimension.getWidth();
        double h = labelDimension.getHeight();

        // Adjust the label location
        TLspStampLabelLocation location = new TLspStampLabelLocation();
        location.setEditedByPlacer(true);
        location.setViewOffset(currentLabelAnchorPoint.getX(), currentLabelAnchorPoint.getY());
        location.setRotation(rotation);

        TLspLabelPlacement labelPlacement = new TLspLabelPlacement(
            label,
            location,
            true,
            x, y, w, h,
            rotation
        );
        placements.add(labelPlacement);

        // We want to always paint all labels at this point, so only add the label to the conflict checker, but
        // don't check if there are conflicts.
        aConflictChecker.addPlacement(labelPlacement);
      } catch (TLcdNoBoundsException e) {
        // Do nothing, no placement is added
      }
    }

    return placements;
  }

  private void initializeStartAnimation(List<TLspLabelID> aLabelIDs, LabelContext aLabelContext, ILspView aView) {
    for (TLspLabelID label : aLabelIDs) {
      // The start location is found using the current label location.
      ILcd3DEditablePoint startLabelAnchorPoint = new TLcdXYZPoint();
      calculateStartLabelAnchorPoint(label, aLabelContext, aView, startLabelAnchorPoint);

      // The end location is determined by the ConcentricCircleLabelLocationsCalculator
      ILcd2DEditablePoint endLabelAnchorPoint = new TLcdXYPoint();
      if (!fCalculator.calculateLabelLocationSFCT(label, aView, endLabelAnchorPoint)) {
        throw new RuntimeException("Could not calculate a end location for label : " + label);
      }

      LocationInfo locationInfo = new LocationInfo(startLabelAnchorPoint, endLabelAnchorPoint);
      fStartAnimationInfo.put(label, locationInfo);
    }
  }

  private void initializeReturnAnimation(List<TLspLabelID> aLabelIDs, LabelContext aLabelContext, ILspView aView) {
    for (TLspLabelID label : aLabelIDs) {
      // The start location is found using the current label location.
      ILcd3DEditablePoint startLabelAnchorPoint = new TLcdXYZPoint();
      calculateStartLabelAnchorPoint(label, aLabelContext, aView, startLabelAnchorPoint);

      // The end location is the start location of the start animation
      LocationInfo startAnimationLocationInfo = fStartAnimationInfo.get(label);
      ILcdPoint endLabelAnchorPoint = startAnimationLocationInfo == null ? null : startAnimationLocationInfo.getStartLabelAnchorPoint();
      if (endLabelAnchorPoint == null) {
        endLabelAnchorPoint = new TLcdXYPoint(startLabelAnchorPoint);
      }

      LocationInfo locationInfo = new LocationInfo(startLabelAnchorPoint, endLabelAnchorPoint);
      fReturnAnimationInfo.put(label, locationInfo);
    }
  }

  private void calculateStartLabelAnchorPoint(TLspLabelID aLabel, LabelContext aLabelContext, ILspView aView, ILcd3DEditablePoint aPointSFCT) {
    ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabel);
    ALspLabelLocations labelLocations = getLabelLocations(aView);
    if (labelLocations == null) {
      throw new RuntimeException("Could not calculate start location, ALspLabelLocations is null");
    }

    ALspLabelLocations.LocationInfo previousLocationInfo = labelLocations.getLabelLocation(aView, aLabel);
    ALspLabelLocation previousLocation = previousLocationInfo.isVisible() ? previousLocationInfo.getLocation() : null;
    if (previousLocation != null) {
      try {
        TLspPaintState paintState = aLabelContext.getPaintState(aLabel, aView);
        TLspContext context = aLabelContext.getContext(aLabel.getLayer(), aView);
        labelPainter.labelAnchorPointSFCT(aLabel, previousLocation, labelLocations, paintState, context, aPointSFCT);

        // Calculate the label anchor point, relative to the object anchor point
        TLcdXYZPoint objectAnchorPoint = new TLcdXYZPoint();
        labelPainter.viewObjectAnchorPointSFCT(aLabel, labelLocations, paintState, context, objectAnchorPoint);
        aPointSFCT.translate3D(
            -objectAnchorPoint.getX(),
            -objectAnchorPoint.getY(),
            -objectAnchorPoint.getZ()
        );
      } catch (TLcdNoBoundsException e) {
        throw new RuntimeException("Could not calculate start location", e);
      }
    } else {
      // If no previous location is present, we calculate a start location ourselves
      if (!fCalculator.calculateLabelLocationSFCT(aLabel, aView, 30.0, aPointSFCT)) {
        throw new RuntimeException("Could not calculate a start location for label : " + aLabel);
      }
    }
  }

  private ALspLabelLocations getLabelLocations(ILspView aView) {
    ILspLabelPlacer placer = aView.getLabelPlacer();
    if (placer != null) {
      return placer.getLabelLocations();
    }
    return null;
  }

  private static class LocationInfo {

    // The start and the end anchor point, defined relative to the object anchor point.
    private ILcdPoint fStartLabelAnchorPoint;
    private ILcdPoint fEndLabelAnchorPoint;

    private LocationInfo(ILcdPoint aStartLabelAnchorPoint, ILcdPoint aEndLabelAnchorPoint) {
      fStartLabelAnchorPoint = aStartLabelAnchorPoint;
      fEndLabelAnchorPoint = aEndLabelAnchorPoint;
    }

    public void getCurrentPoint(double aFraction, ILcd2DEditablePoint aPointSFCT) {
      if (aFraction <= 0.0) {
        aPointSFCT.move2D(fStartLabelAnchorPoint);
      } else if (aFraction >= 1.0) {
        aPointSFCT.move2D(fEndLabelAnchorPoint);
      } else {
        aPointSFCT.move2D(
            fStartLabelAnchorPoint.getX() + aFraction * (fEndLabelAnchorPoint.getX() - fStartLabelAnchorPoint.getX()),
            fStartLabelAnchorPoint.getY() + aFraction * (fEndLabelAnchorPoint.getY() - fStartLabelAnchorPoint.getY())
        );
      }
    }

    public ILcdPoint getStartLabelAnchorPoint() {
      return fStartLabelAnchorPoint;
    }
  }

  /**
   * This iterator makes sure that parent labels are iterated before their children. This is
   * needed because during the computing of label placements, the children depend on their
   * parents to retrieve a correct label location.
   */
  private static class ParentAwareLabelIterator implements Iterator<TLspLabelID> {

    private final LabelContext fLabelContext;
    private final ILspView fView;

    private final List<TLspLabelID> fLabels;
    private final Map<TLspLabelID, List<TLspLabelID>> fQueue = new HashMap<TLspLabelID, List<TLspLabelID>>();
    private final Set<TLspLabelID> fReturned = new HashSet<TLspLabelID>();

    private TLspLabelID fNextLabel;

    private ParentAwareLabelIterator(List<TLspLabelID> aLabels, LabelContext aLabelContext, ILspView aView) {
      fLabels = aLabels;
      fLabelContext = aLabelContext;
      fView = aView;
    }

    public boolean hasNext() {
      if (fLabels.size() == 0) {
        return false;
      }
      TLspLabelID label = fLabels.remove(0);

      TLspLabelID parentLabel = getParentLabel(label);
      if (parentLabel == null || fReturned.contains(parentLabel)) {
        fReturned.add(label);
        // Remove all queued labels that depend on this label from the queue
        List<TLspLabelID> queued = fQueue.remove(label);
        if (queued != null) {
          // Add all queued labels to the front of the labels list
          while (queued.size() > 0) {
            TLspLabelID labelInfo = queued.remove(queued.size() - 1);
            fLabels.add(0, labelInfo);
          }
        }
        fNextLabel = label;
        return true;
      }

      // Queue this label until the parent is placed
      List<TLspLabelID> parentQueue = fQueue.get(parentLabel);
      if (parentQueue == null) {
        parentQueue = new ArrayList<TLspLabelID>();
        fQueue.put(parentLabel, parentQueue);
      }
      parentQueue.add(label);

      return hasNext();
    }

    private TLspLabelID getParentLabel(TLspLabelID aLabel) {
      ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabel);
      TLspPaintState paintState = fLabelContext.getPaintState(aLabel, fView);
      TLspContext context = fLabelContext.getContext(aLabel.getLayer(), fView);
      try {
        Object anchorObject = labelPainter.getAnchorObject(aLabel, paintState, context);
        return anchorObject instanceof TLspLabelID ? (TLspLabelID) anchorObject : null;
      } catch (TLcdNoBoundsException e) {
        return null;
      }
    }

    public TLspLabelID next() {
      return fNextLabel;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
