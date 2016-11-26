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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Timer;

import samples.lightspeed.labels.util.LspLabelPainterUtil;
import com.luciad.realtime.lightspeed.labeling.TLspContinuousLabelingAlgorithm;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelConflictChecker;
import com.luciad.view.lightspeed.label.ILspLabelObstacleProvider;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.label.TLspLabelObstacle;
import com.luciad.view.lightspeed.label.TLspLabelPlacement;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithmProvider;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.ALspDiscreteLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.discrete.ALspDiscreteLabelingAlgorithmWrapper;
import com.luciad.view.lightspeed.label.algorithm.discrete.ILspLabelPlacementEvaluator;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspCompositeDiscreteLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;

/**
 * This labeling algorithm provider allows animating labels from one location to another to
 * implement on-demand decluttering of a group of objects.
 * More particularly, the process is as follows:
 * - decluttering is started by animating the labels of the given objects towards the circle
 *   positions calculated by ConcentricCircleLabelLocationsCalculator.
 *   This is done by the AnimationLabelingAlgorithm.
 * - after this, continuous decluttering is activated to avoid overlap
 * - if decluttering is no longer needed, the labels are animated back to their starting positions by
 *   AnimationLabelingAlgorithm
 * <p>
 * The behavior is controlled by a number of states. Depending on the states, this provider returns
 * a different algorithm for the objects in 'fObjectsToDeclutter'. For all other objects it
 * always returns the discrete labeling algorithm.
 * <p>
 * - When the state is DeclutteringState.STATIC, the discrete labeling algorithm is always returned.
 * - When the state is DeclutteringState.DECLUTTERING, the continuous decluttering algorithm is
 *   returned for the objects in 'fObjectsToDeclutter'.
 * - When the state is DeclutteringState.START_ANIMATION, the animation algorithm is returned
 *   for the objects in 'fObjectsToDeclutter'. The animation algorithm is configured to animate
 *   from the current label location to a location specified by the ConcentricCircleLabelLocationsCalculator.
 * - When the state is DeclutteringState.RETURN_ANIMATION, the animation algorithm is returned
 *   for the objects in 'fObjectsToDeclutter'. The animation algorithm is configured to animate
 *   from the current label location to the previous start location of the animation algorithm
 *   (when the decluttering state was DeclutteringState.START_ANIMATION).
 * <p>
 * The switching between states is controlled by
 * - {@link #declutterObjects(List, ILspView)} : to start the decluttering process
 * - {@link #stopDecluttering()} : to start ending the decluttering process
 * - Timer instances
 *     - fStartAnimationTimer  : to switch from DeclutteringState.START_ANIMATION to DeclutteringState.DECLUTTERING
 *     - fReturnAnimationTimer : to switch from DeclutteringState.RETURN_ANIMATION to DeclutteringState.STATIC
 * <p>
 * This class also acts as a ILspLabelObstacleProvider. This obstacle priority provider can be
 * used to make sure that the decluttered icons and labels are not pushed back to inside the ring
 * of labels, resulting in less overlap and giving a nicer visual result.
 */
public class AnimatedDeclutterLabelingAlgorithmProvider implements
                                                        ILspLabelingAlgorithmProvider<ILspLabelingAlgorithm>,
                                                        ILspLabelObstacleProvider,
                                                        ILspLabelPriorityProvider {

  // Decluttering states
  private enum DeclutteringState {
    STATIC,
    START_ANIMATION,
    DECLUTTERING,
    RETURN_ANIMATION
  }

  /**
   * The animation is optional, it only provides some eye-candy. You can turn it of
   * by setting this flag to false.
   */
  private static boolean ANIMATION = true;

  // Constants that define the duration of the different decluttering states
  private static final int START_ANIMATION_DURATION = 200;
  private static final int RETURN_ANIMATION_DURATION = 300;

  private DeclutteringState fDeclutteringState = DeclutteringState.STATIC;

  // Timers which make sure that the decluttering state is switched at the right moments
  private final Timer fStartAnimationTimer;
  private final Timer fReturnAnimationTimer;

  // Info about which objects/labels/layers are decluttered
  private final Set<TLspDomainObjectContext> fObjectsToDeclutter = new HashSet<TLspDomainObjectContext>();
  private final List<TLspLabelID> fLabelsToDeclutter = new ArrayList<TLspLabelID>();
  private final List<ILspInteractivePaintableLayer> fDeclutteredLayers = new ArrayList<ILspInteractivePaintableLayer>();

  // The algorithms that are used in different decluttering states
  private final ILspLabelingAlgorithm fDiscreteAlgorithm;
  private final ILspLabelingAlgorithm fContinuousAlgorithm;
  private final AnimationLabelingAlgorithm fAnimationAlgorithm;

  // Used by fAnimationAlgorithm to calculate positions to which labels should be animated
  private final ConcentricCircleLabelLocationsCalculator fCalculator;

  private ILspView fView;

  public AnimatedDeclutterLabelingAlgorithmProvider() {
    fCalculator = new ConcentricCircleLabelLocationsCalculator();

    fDiscreteAlgorithm = createDiscreteLabelingAlgorithm();
    fContinuousAlgorithm = createContinuousLabelingAlgorithm();
    fAnimationAlgorithm = createAnimationLabelingAlgorithm();

    // This timer will trigger the end of the start animation, causing
    // continuous decluttering to start.
    fStartAnimationTimer = createStartAnimationTimer();

    // This timer will trigger the end of the return animation, causing
    // static decluttering to start.
    fReturnAnimationTimer = createReturnAnimationTimer();
  }

  private ILspLabelingAlgorithm createDiscreteLabelingAlgorithm() {
    final ALspDiscreteLabelingAlgorithm discreteAlgorithmChild = createDiscreteLabelingAlgorithmChild();
    final ALspDiscreteLabelingAlgorithm discreteAlgorithmParent = createDiscreteLabelingAlgorithmParent();
    return new TLspCompositeDiscreteLabelingAlgorithm(new ILspLabelingAlgorithmProvider<ALspDiscreteLabelingAlgorithm>() {
      @Override
      public ALspDiscreteLabelingAlgorithm getLabelingAlgorithm(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
        return isParentLabel(aLabel, aPaintState, aContext) ? discreteAlgorithmParent : discreteAlgorithmChild;
      }
    });
  }

  private ALspDiscreteLabelingAlgorithm createDiscreteLabelingAlgorithmParent() {
    TLspLabelingAlgorithm baseAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(0, TLspLabelLocationProvider.Location.CENTER));
    return new NoDeclutterAlgorithmWrapper(baseAlgorithm);
  }

  private ALspDiscreteLabelingAlgorithm createDiscreteLabelingAlgorithmChild() {
    return new TLspLabelingAlgorithm(new TLspLabelLocationProvider(
        15,
        TLspLabelLocationProvider.Location.SOUTH_EAST,
        TLspLabelLocationProvider.Location.SOUTH_WEST,
        TLspLabelLocationProvider.Location.NORTH_WEST,
        TLspLabelLocationProvider.Location.NORTH_EAST
    ));
  }

  private ILspLabelingAlgorithm createContinuousLabelingAlgorithm() {
    TLspContinuousLabelingAlgorithm algorithm = new TLspContinuousLabelingAlgorithm();
    algorithm.setDesiredRelativeLocation(new Point(0, 0));
    algorithm.setLabelMovementBehavior(TLspContinuousLabelingAlgorithm.LabelMovementBehavior.OPTIMAL_SPREAD);
    algorithm.setReuseLocationsScaleRatioInterval(new TLcdInterval(0, Double.MAX_VALUE));
    return algorithm;
  }

  private AnimationLabelingAlgorithm createAnimationLabelingAlgorithm() {
    return new AnimationLabelingAlgorithm(fCalculator);
  }

  private Timer createStartAnimationTimer() {
    Timer startAnimationTimer = new Timer(0, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fStartAnimationTimer.stop();
        startContinuousDecluttering();
      }
    });
    startAnimationTimer.setInitialDelay(START_ANIMATION_DURATION);
    startAnimationTimer.setRepeats(false);
    return startAnimationTimer;
  }

  private Timer createReturnAnimationTimer() {
    Timer returnAnimationTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fReturnAnimationTimer.stop();
        startStaticDecluttering();
      }
    });
    returnAnimationTimer.setInitialDelay(RETURN_ANIMATION_DURATION);
    returnAnimationTimer.setRepeats(false);
    return returnAnimationTimer;
  }

  @Override
  public void getLabelObstacles(ILspView aView, List<TLspLabelObstacle> aObstaclesSFCT) {
    // Static -> no obstacles needed
    if (fDeclutteringState == DeclutteringState.STATIC) {
      return;
    }

    // The central label obstacle covers the central area of the cluster of domain objects which is
    // being decluttered. By putting this label obstacle in place, we ensure that the icons and
    // labels are not pushed back to inside the ring of labels, resulting in less overlap and giving
    // a nicer visual result.
    TLspLabelObstacle obstacle = new TLspLabelObstacle();
    fCalculator.updateLabelObstacleSFCT(aView, obstacle);
    aObstaclesSFCT.add(obstacle);
  }

  @Override
  public ILspLabelingAlgorithm getLabelingAlgorithm(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
    switch (fDeclutteringState) {
    case STATIC:
      return fDiscreteAlgorithm;
    case DECLUTTERING:
      return isObjectToDeclutter(aLabel, aContext.getView()) ? fContinuousAlgorithm : fDiscreteAlgorithm;
    case START_ANIMATION:
    case RETURN_ANIMATION:
      return isObjectToDeclutter(aLabel, aContext.getView()) ? fAnimationAlgorithm : fDiscreteAlgorithm;
    default:
      throw new IllegalArgumentException("Unknown state : " + fDeclutteringState);
    }
  }

  @Override
  public int getPriority(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
    ILspLabelingAlgorithm algorithm = getLabelingAlgorithm(aLabel, aPaintState, aContext);
    // Icons always have the highest priority, discrete decluttering is always performed last
    if (isParentLabel(aLabel, aPaintState, aContext)) {
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

  private boolean isParentLabel(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
    ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabel);
    try {
      Object anchorObject = labelPainter.getAnchorObject(aLabel, aPaintState, aContext);
      return !(anchorObject instanceof TLspLabelID);
    } catch (TLcdNoBoundsException e) {
      return true;
    }
  }

  private boolean isObjectToDeclutter(TLspLabelID aLabel, ILspView aView) {
    if (!(aLabel.getLayer() instanceof ILspInteractivePaintableLayer)) {
      return false;
    }
    for (TLspDomainObjectContext domainObjectContext : fObjectsToDeclutter) {
      if (domainObjectContext.getLayer() == aLabel.getLayer() &&
          domainObjectContext.getDomainObject() == aLabel.getDomainObject() &&
          domainObjectContext.getView() == aView) {
        return true;
      }
    }
    return false;
  }

  public void declutterObjects(List<TLspDomainObjectContext> aObjectsToDeclutter, ILspView aView) {
    // Prevent re-entrant animations
    if (fDeclutteringState != DeclutteringState.STATIC) {
      return;
    }

    fView = aView;

    // Notify the decluttering algorithm of the objects that need to be decluttered,
    // so that it can update the preferred locations for the labels of these objects.
    fCalculator.setObjectsToDeclutter(aObjectsToDeclutter, aView);

    // Store the objects that need to be decluttered in order to choose the correct labeling
    // algorithm, see getLabelingAlgorithm(TLcdLabeledObjectInfo). Also initialize the info about
    // the decluttered labels/layers
    fObjectsToDeclutter.addAll(aObjectsToDeclutter);
    calculateLabelsToDeclutter(aView);
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

    // Start the return animation
    doReturnAnimation();
  }

  private void calculateLabelsToDeclutter(ILspView aView) {
    TLspContext context = new TLspContext();

    for (TLspDomainObjectContext domainObjectContext : fObjectsToDeclutter) {
      ILspInteractivePaintableLayer layer = domainObjectContext.getLayer();
      context.resetFor(layer, aView);
      Collection<TLspPaintRepresentation> paintRepresentations = layer.getPaintRepresentations();
      for (TLspPaintRepresentation paintRepresentation : paintRepresentations) {
        ILspPainter painter = layer.getPainter(paintRepresentation);
        if (painter instanceof ILspLabelPainter) {
          ILspLabelPainter labelPainter = (ILspLabelPainter) painter;
          TLspPaintRepresentationState prs = TLspPaintRepresentationState.getInstance(paintRepresentation, TLspPaintState.REGULAR);
          Iterable<TLspLabelID> labelIDs = labelPainter.getLabelIDs(domainObjectContext.getObject(), prs, context);
          for (TLspLabelID labelID : labelIDs) {
            fLabelsToDeclutter.add(labelID);
          }
        }
      }
    }
  }

  private void calculateDeclutteredLayers() {
    for (TLspDomainObjectContext labeledObject : fObjectsToDeclutter) {
      ILspInteractivePaintableLayer layer = labeledObject.getLayer();
      if (!fDeclutteredLayers.contains(layer)) {
        fDeclutteredLayers.add(layer);
      }
    }
  }

  private void doStartAnimation() {
    if (!ANIMATION) {
      startContinuousDecluttering();
      return;
    }

    fDeclutteringState = DeclutteringState.START_ANIMATION;
    fAnimationAlgorithm.beginStartAnimation(START_ANIMATION_DURATION);
    fStartAnimationTimer.start();
    fView.invalidate(true, this, "Begin label start animation");
  }

  private void startStaticDecluttering() {
    // No more objects to declutter
    fObjectsToDeclutter.clear();
    fLabelsToDeclutter.clear();

    fDeclutteringState = DeclutteringState.STATIC;

    fDeclutteredLayers.clear();

    fCalculator.setObjectsToDeclutter(null, fView);

    fView = null;
  }

  private void startContinuousDecluttering() {
    fDeclutteringState = DeclutteringState.DECLUTTERING;
    fView.invalidate(true, this, "Begin continuous decluttering");
  }

  private void doReturnAnimation() {
    if (!ANIMATION) {
      startStaticDecluttering();
      return;
    }

    fDeclutteringState = DeclutteringState.RETURN_ANIMATION;
    fAnimationAlgorithm.beginReturnAnimation(RETURN_ANIMATION_DURATION);
    fReturnAnimationTimer.start();
    fView.invalidate(true, this, "Begin label return animation");
  }

  public List<TLspLabelID> getLabelsToDeclutter() {
    return new ArrayList<TLspLabelID>(fLabelsToDeclutter);
  }

  /**
   * This labeling algorithm wrapper makes sure that labels that are placed by the delegate algorithm are never
   * dropped. This is used for the icon labels, which should always be visible.
   */
  private static class NoDeclutterAlgorithmWrapper extends ALspDiscreteLabelingAlgorithmWrapper {

    public NoDeclutterAlgorithmWrapper(ALspDiscreteLabelingAlgorithm aDelegate) {
      super(aDelegate);
    }

    @Override
    protected ILspLabelPlacementEvaluator createLabelPlacementEvaluator(List<TLspLabelID> aLabels, LabelContext aLabelContext, ILspLabelConflictChecker aConflictChecker, ILspView aView) {
      final ILspLabelPlacementEvaluator evaluator = super.createLabelPlacementEvaluator(aLabels, aLabelContext, aConflictChecker, aView);
      return new ILspLabelPlacementEvaluator() {
        @Override
        public PlacementResult evaluatePlacement(TLspLabelPlacement aPlacement, ILspLabelConflictChecker.Conflict aConflict, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
          if (aConflict != null) {
            aConflict = new ILspLabelConflictChecker.Conflict(aConflict.isOutsideView(), aConflict.isPartiallyOutsideView(), false, false);
            if (!aConflict.isOverlapWithPlacements() && !aConflict.isPartiallyOutsideView() && !aConflict.isOutsideView() && !aConflict.isOverlapWithObstacles()) {
              aConflict = null;
            }
          }
          return evaluator.evaluatePlacement(aPlacement, aConflict, aPlacements, aCurrentLabelLocations);
        }

        @Override
        public void placementApplied(TLspLabelPlacement aPlacement, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
          evaluator.placementApplied(aPlacement, aPlacements, aCurrentLabelLocations);
        }

        @Override
        public void noPlacementApplied(TLspLabelID aLabel, List<TLspLabelPlacement> aPlacements, ALspLabelLocations aCurrentLabelLocations) {
          evaluator.noPlacementApplied(aLabel, aPlacements, aCurrentLabelLocations);
        }
      };
    }
  }
}
