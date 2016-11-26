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
package samples.lightspeed.demo.application.data.dynamictracks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler for the tracks which uses points when zoomed out, and 3D planes when
 * zoomed in.
 */
public abstract class TrackStylerBase extends ALspStyler {

  /**
   * Switching between icons and aircraft models happens at a fixed scale in 2D.
   */
  private static final double SHOW_AIRCRAFT_MODEL_IN_2D_SWITCH_SCALE = 5e-3;
  /**
   * Switching between icons and history trails happens at a fixed scale in 2D.
   */
  private static final double SHOW_HISTORY_TRAILS_IN_2D_SWITCH_SCALE = 1e-3;
  /**
   * Switching between icons and aircraft models in 3D uses the distance from each track to the camera.
   * The min distance makes sure that when the camera looks from the track (distance ~ 0), the aircraft model does
   * not interfere with the visualization. Beyond the max distance, no aircraft models are used, a simple point
   * is shown instead.
   */
  private static final ILcdInterval SHOW_AIRCRAFT_MODEL_IN_3D_DISTANCE_INTERVAL = new TLcdInterval(0.005, 3e5); //meters

  private static final ILcdInterval SHOW_HISTORY_TRAILS_IN_3D_DISTANCE_INTERVAL = new TLcdInterval(3e5, 6e5); //meters
  /**
   * Clamp the maximum number of aircraft models that are ever rendered, to guard performance.
   */
  private static final int MAXIMUM_NUMBER_OF_AIRCRAFT = 100;

  private final TLcdWeakIdentityHashMap<ILspView, StyleInvalidator> fViewsToStyleInvalidator = new TLcdWeakIdentityHashMap<>();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    ILspView view = aContext.getView();

    StyleInvalidator styleInvalidator = fViewsToStyleInvalidator.get(view);
    if (styleInvalidator == null) {
      styleInvalidator = new StyleInvalidator(this, view, SHOW_AIRCRAFT_MODEL_IN_2D_SWITCH_SCALE);
      fViewsToStyleInvalidator.put(view, styleInvalidator);
    }

    ALspViewXYZWorldTransformation viewXYZWorldTransformation = view.getViewXYZWorldTransformation();
    if (viewXYZWorldTransformation instanceof TLspViewXYZWorldTransformation2D) {
      double scale = viewXYZWorldTransformation.getScale();
      if (scale < SHOW_HISTORY_TRAILS_IN_2D_SWITCH_SCALE) {
        //for the point style there is no need to fix the orientation
        styleAsPoint(aObjects, aStyleCollector, aContext);
      } else if (scale < SHOW_AIRCRAFT_MODEL_IN_2D_SWITCH_SCALE) {
        //for the point style there is no need to fix the orientation
        styleAsTrails(aObjects, aStyleCollector, aContext);
      } else {
        styleAsAircraft(aObjects, aStyleCollector, aContext);
      }
    } else if (viewXYZWorldTransformation instanceof TLspViewXYZWorldTransformation3D) {
      TLspViewXYZWorldTransformation3D transformation = (TLspViewXYZWorldTransformation3D) viewXYZWorldTransformation;
      ILcdModelXYZWorldTransformation modelXYZWorldTransformation = aContext.getModelXYZWorldTransformation();

      List<Object> pointStyledObjects = new ArrayList<>();
      List<Object> trailsStyledObjects = new ArrayList<>();
      final Map<Object, Double> objectToDistance = new HashMap<>();
      List<Object> aircraftStyledObjects = new ArrayList<>();
      for (Object aObject : aObjects) {
        ILcdPoint track = (ILcdPoint) aObject;
        try {
          double distance = distanceFromObjectToEyePoint(track, modelXYZWorldTransformation, transformation);
          if (distance > SHOW_AIRCRAFT_MODEL_IN_3D_DISTANCE_INTERVAL.getMin() &&
              distance < SHOW_AIRCRAFT_MODEL_IN_3D_DISTANCE_INTERVAL.getMax()) {
            objectToDistance.put(System.identityHashCode(track), distance);
            aircraftStyledObjects.add(track);
          } else if (distance > SHOW_HISTORY_TRAILS_IN_3D_DISTANCE_INTERVAL.getMin() &&
                     distance <= SHOW_HISTORY_TRAILS_IN_3D_DISTANCE_INTERVAL.getMax()) {
            trailsStyledObjects.add(track);
          } else {
            pointStyledObjects.add(track);
          }
        } catch (TLcdOutOfBoundsException e) {
          pointStyledObjects.add(track);
        }
      }
      if (!aircraftStyledObjects.isEmpty()) {
        if (aircraftStyledObjects.size() <= MAXIMUM_NUMBER_OF_AIRCRAFT) {
          styleAsAircraft(aircraftStyledObjects, aStyleCollector, aContext);
        } else {
          Collections.sort(aircraftStyledObjects, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
              Double firstDistance = objectToDistance.get(System.identityHashCode(o1));
              Double secondDistance = objectToDistance.get(System.identityHashCode(o2));
              if (firstDistance < secondDistance) {
                return -1;
              } else if (firstDistance > secondDistance) {
                return 1;
              }
              return 0;
            }
          });
          styleAsAircraft(aircraftStyledObjects.subList(0, MAXIMUM_NUMBER_OF_AIRCRAFT), aStyleCollector, aContext);
          trailsStyledObjects.addAll(aircraftStyledObjects.subList(MAXIMUM_NUMBER_OF_AIRCRAFT, aircraftStyledObjects.size()));
        }
      }
      if (!trailsStyledObjects.isEmpty()) {
        styleAsTrails(trailsStyledObjects, aStyleCollector, aContext);
      }
      if (!pointStyledObjects.isEmpty()) {
        styleAsPoint(pointStyledObjects, aStyleCollector, aContext);
      }

    }
  }

  protected abstract void styleAsPoint(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext);

  protected abstract void styleAsTrails(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext);

  protected abstract void styleAsAircraft(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext);

  private double distanceFromObjectToEyePoint(ILcdPoint aPoint,
                                              ILcdModelXYZWorldTransformation aModelToWorldTransformation,
                                              TLspViewXYZWorldTransformation3D aViewXYZWorldTransformation3D) throws TLcdOutOfBoundsException {
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    aModelToWorldTransformation.modelPoint2worldSFCT(aPoint, worldPoint);

    ILcdPoint eyePoint = aViewXYZWorldTransformation3D.getEyePoint();
    return TLcdCartesian.distance3D(worldPoint, eyePoint);
  }

  /**
   * Class that listens for changes in the scale of the view, and triggers a style change event when needed
   */
  private static class StyleInvalidator implements PropertyChangeListener {

    private final WeakReference<TrackStylerBase> fStyler;
    private final WeakReference<ILspView> fView;
    private final double fSwitchScale;
    private double fOldScale;

    private StyleInvalidator(TrackStylerBase aStyler, ILspView aView, double aSwitchScale) {
      fStyler = new WeakReference<>(aStyler);
      fView = new WeakReference<>(aView);
      fSwitchScale = aSwitchScale;
      aView.addPropertyChangeListener(this);
      aView.getViewXYZWorldTransformation().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      TrackStylerBase styler = fStyler.get();
      ILspView view = fView.get();
      if (styler == null || view == null) {
        dispose(evt);
        return;
      }
      if (evt.getSource() == view) {
        handleViewPropertyChange(evt, styler);
        return;
      }
      if (evt.getSource() == view.getViewXYZWorldTransformation() &&
          view.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation2D) {
        handle2DViewXYZWorldTransformationPropertyChange(view, styler);
        return;
      }
      if (evt.getSource() == view.getViewXYZWorldTransformation() &&
          view.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D) {
        handle3DViewXYZWorldTransformationPropertyChange(evt, styler);
        return;
      }
    }

    private void handle2DViewXYZWorldTransformationPropertyChange(ILspView aView, TrackStylerBase aStyler) {
      double oldScale = fOldScale;
      double newScale = aView.getViewXYZWorldTransformation().getScale();

      if (Double.isNaN(oldScale)) {
        fOldScale = newScale;
        aStyler.fireStyleChangeEvent();
      }

      if ((oldScale > fSwitchScale && newScale < fSwitchScale) ||
          (oldScale < fSwitchScale && newScale > fSwitchScale)) {
        fOldScale = newScale;
        aStyler.fireStyleChangeEvent();
      }
    }

    private void handle3DViewXYZWorldTransformationPropertyChange(PropertyChangeEvent aEvent, TrackStylerBase aStyler) {
      if ("eyePoint".equals(aEvent.getPropertyName())) {
        aStyler.fireStyleChangeEvent();
      }
    }

    private void handleViewPropertyChange(PropertyChangeEvent evt, TrackStylerBase aStyler) {
      if ("viewXYZWorldTransformation".equals(evt.getPropertyName())) {
        ALspViewXYZWorldTransformation old = (ALspViewXYZWorldTransformation) evt.getOldValue();
        old.removePropertyChangeListener(this);
        ALspViewXYZWorldTransformation newValue = (ALspViewXYZWorldTransformation) evt.getNewValue();
        newValue.addPropertyChangeListener(this);

        fOldScale = Double.NaN;

        aStyler.fireStyleChangeEvent();
      }
    }

    private void dispose(PropertyChangeEvent evt) {
      Object source = evt.getSource();
      if (source instanceof ILcdView) {
        ((ILcdView) source).removePropertyChangeListener(this);
      }
      if (source instanceof ALspViewXYZWorldTransformation) {
        ((ALspViewXYZWorldTransformation) source).removePropertyChangeListener(this);
      }
      ILspView view = fView.get();
      if (view != null) {
        view.removePropertyChangeListener(this);
        view.getViewXYZWorldTransformation().removePropertyChangeListener(this);
      }
    }
  }
}

