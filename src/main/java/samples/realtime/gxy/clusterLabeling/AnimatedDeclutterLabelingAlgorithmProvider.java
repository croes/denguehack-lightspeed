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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import com.luciad.realtime.gxy.labeling.TLcdGXYContinuousLabelingAlgorithm;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLabelObstacleProvider;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYMultiLabelPriorityProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYLabelObstacle;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLabelPainterLocationLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;
import com.luciad.view.map.TLcdMapJPanel;

/**
 * This algorithm provider allows animating labels from one location/label algorithm to another to
 * implement on-demand decluttering of a group of objects.
 * More particularly, the process is as follows:
 * - decluttering is started by animating the labels of the given objects towards the circle
 *   positions calculated by ConcentricCircleLabelLocationsCalculator.
 *   This is done by the AnimationLabelingAlgorithm.
 * - after this, continuous decluttering is activated to avoid overlap
 * - if decluttering is no longer needed, the labels are animated back to their starting positions by
 *   AnimationLabelingAlgorithm
 * <p>
 * The behavior is controlled by five states. Depending on the states, this provider returns
 * a different algorithm for the objects in 'fObjectsToDeclutter'. For all other objects it
 * always returns the discrete labeling algorithm.
 * <p>
 * - When the state is DeclutteringState.STATIC, the discrete labeling algorithm is always returned.
 * - When the state is DeclutteringState.DECLUTTERING, the continuous decluttering algorithm is
 *   returned for the objects in 'fObjectsToDeclutter'.
 * - When the state is DeclutteringState.DECLUTTERING_STABILIZED, the continuous decluttering
 *   algorithm is returned for the objects in 'fObjectsToDeclutter'. The difference with the
 *   previous state is that all labels for those objects are made sticky.
 * - When the state is DeclutteringState.START_ANIMATION, the animation algorithm is returned
 *   for the objects in 'fObjectsToDeclutter'. The animation algorithm is configured to animate
 *   from the current label location to a location specified by the ConcentricCircleLabelLocationsCalculator.
 * - When the state is DeclutteringState.RETURN_ANIMATION, the animation algorithm is returned
 *   for the objects in 'fObjectsToDeclutter'. The animation algorithm is configured to animate
 *   from the current label location to the previous start location of the animation algorithm
 *   (when the decluttering state was DeclutteringState.START_ANIMATION).
 * <p>
 * The switching between states is controlled by
 * - {@link #declutterObjects(List)} : to start the decluttering process
 * - {@link #stopDecluttering()} : to start ending the decluttering process
 * - Timer instances
 *     - fStartAnimationTimer  : to switch from DeclutteringState.START_ANIMATION to DeclutteringState.DECLUTTERING
 *     - fStabilizationTimer   : to switch from DeclutteringState.DECLUTTERING to DeclutteringState.DECLUTTERING_STABILIZED
 *     - fReturnAnimationTimer : to switch from DeclutteringState.RETURN_ANIMATION to DeclutteringState.STATIC
 * <p>
 * This class also acts as a ILcdGXYLabelObstacleProvider. This obstacle priority provider can be
 * used to make sure that the decluttered icons and labels are not pushed back to inside the ring
 * of labels, resulting in less overlap and giving a nicer visual result.
 */
class AnimatedDeclutterLabelingAlgorithmProvider implements
                                                 ILcdGXYLabelLabelingAlgorithmProvider<ILcdGXYLabelingAlgorithm>,
                                                 ILcdGXYLabelObstacleProvider {

  /**
   * The animation is optional, it only provides some eye-candy. You can turn it of
   * by setting this flag to false.
   */
  private static boolean ANIMATION = true;

  private static final int START_ANIMATION_DURATION = 200;
  private static final int RETURN_ANIMATION_DURATION = 300;
  private static final int STABILIZATION_DURATION = 1000;

  // Decluttering states
  private enum DeclutteringState {
    STATIC,
    START_ANIMATION,
    DECLUTTERING,
    DECLUTTERING_STABILIZED,
    RETURN_ANIMATION
  }

  private DeclutteringState fDeclutteringState = DeclutteringState.STATIC;

  private Timer fRepaintTimer;
  private Timer fStartAnimationTimer;
  private Timer fReturnAnimationTimer;
  private Timer fStabilizationTimer;

  private final TLcdMapJPanel fMapJPanel;

  // Info about which objects/labels/layers are decluttered
  private final List<TLcdCollectedLabeledObjectInfo> fObjectsToDeclutter = new ArrayList<TLcdCollectedLabeledObjectInfo>();
  private final Map<TLcdLabelIdentifier, TLcdCollectedLabelInfo> fLabelsToDeclutter = new HashMap<TLcdLabelIdentifier, TLcdCollectedLabelInfo>();
  private final List<ILcdGXYLayer> fDeclutteredLayers = new ArrayList<ILcdGXYLayer>();

  // The possible returned algorithms
  private final ILcdGXYLabelingAlgorithm fContinuousAlgorithm;
  private final ILcdGXYLabelingAlgorithm fDiscreteAlgorithm;
  private final AnimationLabelingAlgorithm fAnimationAlgorithm;

  private final ConcentricCircleLabelLocationsCalculator fCalculator;

  private final List<TLcdGXYLabelObstacle> fObstacles = new ArrayList<TLcdGXYLabelObstacle>();

  public AnimatedDeclutterLabelingAlgorithmProvider(TLcdMapJPanel aMapJPanel) {
    fMapJPanel = aMapJPanel;

    fCalculator = new ConcentricCircleLabelLocationsCalculator(aMapJPanel);

    fDiscreteAlgorithm = createDiscreteLabelingAlgorithm();
    fContinuousAlgorithm = createContinuousLabelingAlgorithm();
    fAnimationAlgorithm = createAnimationLabelingAlgorithm();

    // This timer, when active, will continuously trigger repaints so that the
    // continuous decluttering algorithm can repeatedly declutter.
    createRepaintTimer();

    // This timer will trigger the end of the start animation, causing
    // continuous decluttering to start.
    createStartAnimationTimer();

    // This timer will trigger the end of the return animation, causing
    // static decluttering to start.
    createReturnAnimationTimer();

    // This timer will trigger the stabilization phase during continuous
    // decluttering, it will cause the label locations to be made sticky.
    createStabilizationTimer();
  }

  private void createRepaintTimer() {
    fRepaintTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invalidateLabeledLayers("Timed repaint");
      }
    });
    fRepaintTimer.setRepeats(true);
    fRepaintTimer.setCoalesce(true);
  }

  private void createStartAnimationTimer() {
    fStartAnimationTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fStartAnimationTimer.stop();
        startContinuousDecluttering();
      }
    });
    fStartAnimationTimer.setInitialDelay(START_ANIMATION_DURATION);
    fStartAnimationTimer.setRepeats(false);
  }

  private void createReturnAnimationTimer() {
    fReturnAnimationTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fReturnAnimationTimer.stop();
        startStaticDecluttering();
      }
    });
    fReturnAnimationTimer.setInitialDelay(RETURN_ANIMATION_DURATION);
    fReturnAnimationTimer.setRepeats(false);
  }

  private void createStabilizationTimer() {
    fStabilizationTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fStabilizationTimer.stop();
        fRepaintTimer.stop();
        fDeclutteringState = DeclutteringState.DECLUTTERING_STABILIZED;
        setDeclutteredLabelsSticky(true);
      }
    });
    fStabilizationTimer.setInitialDelay(STABILIZATION_DURATION);
    fStabilizationTimer.setRepeats(false);
  }

  private ILcdGXYLabelingAlgorithm createDiscreteLabelingAlgorithm() {
    TLcdGXYLabelPainterLocationLabelingAlgorithm discrete_algorithm = new TLcdGXYLabelPainterLocationLabelingAlgorithm();
    discrete_algorithm.setForcedPaintingThresholdPriority(10);
    // Icons have a higher priority than text labels
    discrete_algorithm.setLabelPriorityProvider(new MyPriorityProvider());
    return discrete_algorithm;
  }

  private ILcdGXYLabelingAlgorithm createContinuousLabelingAlgorithm() {
    TLcdGXYContinuousLabelingAlgorithm continuous_algorithm = new MyContinuousLabelingAlgorithm();
    continuous_algorithm.setDesiredRelativeLocation(new Point(0, 0));
    // Icons have a higher priority than text labels
    continuous_algorithm.setLabelPriorityProvider(new MyPriorityProvider());
    return continuous_algorithm;
  }

  private AnimationLabelingAlgorithm createAnimationLabelingAlgorithm() {
    return new AnimationLabelingAlgorithm(fCalculator);
  }

  // Implementation LabelingAlgorithmProvider

  public ILcdGXYLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel) {
    return getLabelingAlgorithmForObject(aLabel.getDomainObject());
  }

  private ILcdGXYLabelingAlgorithm getLabelingAlgorithmForObject(Object aDomainObject) {
    if (fDeclutteringState == DeclutteringState.STATIC) {
      return fDiscreteAlgorithm;
    } else if (fDeclutteringState == DeclutteringState.DECLUTTERING ||
               fDeclutteringState == DeclutteringState.DECLUTTERING_STABILIZED) {
      if (isObjectToDeclutter(aDomainObject)) {
        return fContinuousAlgorithm;
      }
      return fDiscreteAlgorithm;
    } else if (fDeclutteringState == DeclutteringState.START_ANIMATION ||
               fDeclutteringState == DeclutteringState.RETURN_ANIMATION) {
      if (isObjectToDeclutter(aDomainObject)) {
        return fAnimationAlgorithm;
      }
      return fDiscreteAlgorithm;
    } else {
      // Cannot happen
      return null;
    }
  }

  private boolean isObjectToDeclutter(Object aDomainObject) {
    for (TLcdCollectedLabeledObjectInfo labeled_object : fObjectsToDeclutter) {
      if (labeled_object.getDomainObject() == aDomainObject) {
        return true;
      }
    }
    return false;
  }

  // Implementation ILcdGXYLabelObstacleProvider

  public List getLabelObstacles(Graphics aGraphics, ILcdGXYView aGXYView) {
    fObstacles.clear();

    // Static -> no obstacles needed
    if (fDeclutteringState == DeclutteringState.STATIC) {
      return fObstacles;
    }

    // The central label obstacle covers the central area of the cluster of domain objects which is
    // being decluttered. By putting this label obstacle in place, we ensure that the icons and
    // labels are not pushed back to inside the ring of labels, resulting in less overlap and giving
    // a nicer visual result.    
    TLcdGXYLabelObstacle obstacle = new TLcdGXYLabelObstacle();
    fCalculator.updateLabelObstacleSFCT(fMapJPanel, fMapJPanel.getGraphics(), obstacle);
    fObstacles.add(obstacle);

    return fObstacles;
  }

  // Public methods

  public void declutterObjects(List<TLcdCollectedLabeledObjectInfo> aObjectsToDeclutter) {
    // Prevent re-entrant animations
    if (fDeclutteringState != DeclutteringState.STATIC) {
      return;
    }

    // Notify the decluttering algorithm of the objects that need to be decluttered,
    // so that it can update the preferred locations for the labels of these objects.
    fCalculator.setObjectsToDeclutter(aObjectsToDeclutter);

    // Store the objects that need to be decluttered in order to choose the correct labeling
    // algorithm, see getLabelingAlgorithm(TLcdLabeledObjectInfo). Also initialize the info about
    // the decluttered labels/layers
    fObjectsToDeclutter.addAll(aObjectsToDeclutter);
    calculateLabelsToDeclutter();
    calculateDeclutteredLayers();

    // Start the animation
    doStartAnimation();
  }

  public void stopDecluttering() {
    // No need to stop more than once.
    if (fDeclutteringState == DeclutteringState.STATIC ||
        fDeclutteringState == DeclutteringState.RETURN_ANIMATION) {
      return;
    }

    fStartAnimationTimer.stop();
    fStabilizationTimer.stop();
    fRepaintTimer.stop();

    // Start the return animation
    doReturnAnimation();
  }

  public List<TLcdCollectedLabeledObjectInfo> getObjectsToDeclutter() {
    return fObjectsToDeclutter;
  }

  public List<TLcdCollectedLabelInfo> getLabelsToDeclutter() {
    return new ArrayList<TLcdCollectedLabelInfo>(fLabelsToDeclutter.values());
  }

  // Other

  private void doStartAnimation() {
    if (!ANIMATION) {
      startContinuousDecluttering();
      return;
    }

    fDeclutteringState = DeclutteringState.START_ANIMATION;
    fRepaintTimer.restart();
    fAnimationAlgorithm.beginStartAnimation(START_ANIMATION_DURATION);
    fStartAnimationTimer.start();
  }

  private void startContinuousDecluttering() {
    fDeclutteringState = DeclutteringState.DECLUTTERING;
    fRepaintTimer.restart();
    fStabilizationTimer.start();
  }

  private void doReturnAnimation() {
    if (fDeclutteringState == DeclutteringState.DECLUTTERING_STABILIZED) {
      // The label locations are still sticky because of the stabilization phase
      setDeclutteredLabelsSticky(false);
    }

    if (!ANIMATION) {
      startStaticDecluttering();
      return;
    }

    fDeclutteringState = DeclutteringState.RETURN_ANIMATION;
    fRepaintTimer.restart();
    fAnimationAlgorithm.beginReturnAnimation(RETURN_ANIMATION_DURATION);
    fReturnAnimationTimer.start();
  }

  private void startStaticDecluttering() {
    // No more objects to declutter
    fObjectsToDeclutter.clear();
    fLabelsToDeclutter.clear();

    // Remove the obstacle as it stands in the way for discrete decluttering
    fObstacles.clear();

    fDeclutteringState = DeclutteringState.STATIC;

    // Invalidate once more to make sure all labels are correctly decluttered again.
    invalidateLabeledLayers("Started static decluttering");

    fDeclutteredLayers.clear();
  }

  private void invalidateLabeledLayers(String aMessage) {
    for (ILcdGXYLayer layer : fDeclutteredLayers) {
      fMapJPanel.invalidateGXYLayer(layer, true, this, aMessage);
    }
  }

  private void calculateLabelsToDeclutter() {
    TLcdGXYContext context = new TLcdGXYContext();

    for (TLcdCollectedLabeledObjectInfo labeled_object : getObjectsToDeclutter()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) labeled_object.getLayer();
      if (context.getGXYLayer() != layer) {
        context.resetFor(layer, fMapJPanel);
      }

      ILcdGXYLabelPainter2 label_painter = (ILcdGXYLabelPainter2) layer.getGXYLabelPainter(labeled_object.getDomainObject());
      int label_count = label_painter.getLabelCount(fMapJPanel.getGraphics(), context);
      for (int label = 0; label < label_count; label++) {
        int sublabel_count = label_painter.getSubLabelCount(label);
        for (int sublabel = 0; sublabel < sublabel_count; sublabel++) {
          TLcdCollectedLabelInfo label_info = new TLcdCollectedLabelInfo(labeled_object, label, sublabel);
          fLabelsToDeclutter.put(label_info.getLabelIdentifier(), label_info);
        }
      }
    }
  }

  private void calculateDeclutteredLayers() {
    for (TLcdCollectedLabeledObjectInfo labeled_object : fObjectsToDeclutter) {
      ILcdGXYLayer layer = (ILcdGXYLayer) labeled_object.getLayer();
      if (!fDeclutteredLayers.contains(layer)) {
        fDeclutteredLayers.add(layer);
      }
    }
  }

  private void setDeclutteredLabelsSticky(boolean aSticky) {
    for (TLcdCollectedLabelInfo label : fLabelsToDeclutter.values()) {
      if (!(label.getLabeledObject().getLayer() instanceof ILcdGXYEditableLabelsLayer)) {
        continue;
      }
      ILcdGXYEditableLabelsLayer labels_layer = (ILcdGXYEditableLabelsLayer) label.getLabeledObject().getLayer();

      ALcdLabelLocations locations = labels_layer.getLabelLocations();
      TLcdLabelLocation work_location = locations.createLabelLocation();
      locations.getLabelLocationSFCT(label.getLabelIdentifier().getDomainObject(),
                                     label.getLabelIdentifier().getLabelIndex(),
                                     label.getLabelIdentifier().getSubLabelIndex(),
                                     fMapJPanel, work_location);

      if (aSticky) {
        work_location.setLabelEditMode(work_location.getLabelEditMode() | TLcdLabelLocation.STICKY_LABEL_LOCATION);
      } else {
        work_location.setLabelEditMode(work_location.getLabelEditMode() & ~TLcdLabelLocation.STICKY_LABEL_LOCATION);
      }
      locations.putLabelLocation(label.getLabelIdentifier().getDomainObject(),
                                 label.getLabelIdentifier().getLabelIndex(),
                                 label.getLabelIdentifier().getSubLabelIndex(),
                                 fMapJPanel, work_location, ILcdFireEventMode.FIRE_LATER);
    }

    for (ILcdGXYLayer layer : fDeclutteredLayers) {
      if (layer instanceof ILcdGXYEditableLabelsLayer) {
        ILcdGXYEditableLabelsLayer labels_layer = (ILcdGXYEditableLabelsLayer) layer;
        labels_layer.getLabelLocations().fireCollectedChanges(fMapJPanel);
      }
    }
    invalidateLabeledLayers("Labels set sticky");
  }

  private class MyPriorityProvider implements ILcdGXYMultiLabelPriorityProvider {
    public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
      ILcdGXYLabelingAlgorithm algorithm = getLabelingAlgorithmForObject(aObject);
      // Icons always have the highest priority, discrete decluttering is always performed last
      if (aLabelIndex == 0 && aSubLabelIndex == 0) {
        if (algorithm == fContinuousAlgorithm) {
          return 1;
        } else if (algorithm == fAnimationAlgorithm) {
          return 1;
        }
        return 5;
      } else {
        if (algorithm == fContinuousAlgorithm) {
          return 15;
        } else if (algorithm == fAnimationAlgorithm) {
          return 15;
        }
        return 20;
      }
    }
  }

  /**
   * This force based labeling algorithm calculates its desired label locations
   * using a ConcentricCircleLabelLocationsCalculator.
   */
  private class MyContinuousLabelingAlgorithm extends TLcdGXYContinuousLabelingAlgorithm {
    protected void retrieveDesiredLabelLocation(Graphics aGraphics, ILcdGXYContext aGXYContext, Object aObject, int aLabelIndex, int aSubLabelIndex, Point aRelativeLocationSFCT) {
      TLcdLabelIdentifier label_identifier = new TLcdLabelIdentifier(aGXYContext.getGXYLayer(), aObject, aLabelIndex, aSubLabelIndex);
      TLcdCollectedLabelInfo label_info = fLabelsToDeclutter.get(label_identifier);
      if (label_info == null || !fCalculator.calculateLabelLocationSFCT(label_info, fMapJPanel, aGraphics, aRelativeLocationSFCT)) {
        super.retrieveDesiredLabelLocation(aGraphics, aGXYContext, aObject, aLabelIndex, aSubLabelIndex, aRelativeLocationSFCT);
        aRelativeLocationSFCT.setLocation(aRelativeLocationSFCT.getX() * (1 + aLabelIndex + aSubLabelIndex),
                                          aRelativeLocationSFCT.getY() * (1 + aLabelIndex + aSubLabelIndex));
      }
    }
  }
}
