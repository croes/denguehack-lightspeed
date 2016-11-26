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
package samples.lightspeed.demo.application.data.milsym;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdView;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.services.asynchronous.TLspTaskExecutorRunnable;
import com.luciad.view.lightspeed.services.terrain.ILspTerrainSupport;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * Creates a route across the terrain between 2 points.
 *
 * @see CrossCountryPathPlanningSupport
 */
public class RouteCreateController extends ALspController {

  private final ILcdGeoReference fReference = new TLcdGeodeticReference();
  private final TLcdVectorModel fModel;
  private final ILspInteractivePaintableLayer fLayer;

  private boolean fCreatingPoint = false;
  private int fCreatedPointCount = 0;
  private final TLcdLonLatHeightPoint[] fPoints = new TLcdLonLatHeightPoint[]{
      new TLcdLonLatHeightPoint(),
      new TLcdLonLatHeightPoint(),
  };
  private Route fRoute;
  private Future fFuture;

  public RouteCreateController() {
    fModel = new TLcdVectorModel((ILcdModelReference) fReference);
    fLayer = TLspShapeLayerBuilder.newBuilder().model(fModel).bodyStyler(TLspPaintState.REGULAR, new RouteStyler()).bodyScaleRange(new TLcdInterval(0.005, Double.MAX_VALUE))
                                  .labelScaleRange(new TLcdInterval(0.005, Double.MAX_VALUE))
                                  .build();
  }

  @Override
  protected void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    clearRoute();
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    super.terminateInteractionImpl(aView);
    if (fCreatedPointCount < 2) {
      clearRoute();
    }
  }

  private void clearRoute() {
    fCreatedPointCount = 0;
    fCreatingPoint = false;
    fRoute = new Route();
    updateModel(RouteState.CREATING, null);
    if (fFuture != null) {
      fFuture.cancel(false);
      fFuture = null;
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    final ILspView view = getView();

    if (fCreatedPointCount < 2 && (isClick(aAWTEvent) || isMove(aAWTEvent))) {
      MouseEvent me = (MouseEvent) aAWTEvent;

      // Compute the point
      ILspTerrainSupport terrain = view.getServices().getTerrainSupport();
      TLspContext context = new TLspContext(fLayer, view);
      ILcdPoint wp = terrain.getPointOnTerrain(new TLcdXYPoint(me.getX(), me.getY()), context);
      try {
        TLcdLonLatHeightPoint p = fPoints[fCreatedPointCount];
        context.getModelXYZWorldTransformation().worldPoint2modelSFCT(wp, p);
        p.move3D(p.getLon(), p.getLat(), 0);
      } catch (TLcdOutOfBoundsException e) {
        return null; // out-of-bounds -> ignore
      }
      fCreatingPoint = true;
      if (isClick(aAWTEvent) && fCreatedPointCount < 2) {
        fCreatedPointCount++;
        fCreatingPoint = fCreatedPointCount < 2;
        if (fCreatingPoint) {
          fPoints[fCreatedPointCount].move3D(fPoints[fCreatedPointCount - 1]);
        }
      }

      if (fCreatedPointCount > 0) {
        // Restrict the distance between the points to avoid very long computations
        ILcdEllipsoid ellipsoid = fReference.getGeodeticDatum().getEllipsoid();
        double distance = ellipsoid.geodesicDistance(fPoints[0], fPoints[1]);
        if (distance > CrossCountryPathPlanningSupport.MAX_DISTANCE) {
          double azimuth = ellipsoid.forwardAzimuth2D(fPoints[0], fPoints[1]) * TLcdConstant.RAD2DEG;
          ellipsoid.geodesicPointSFCT(fPoints[0], CrossCountryPathPlanningSupport.MAX_DISTANCE, azimuth, fPoints[1]);
        }
      }

      // Update the model
      updateModel(RouteState.CREATING, null);
    }

    if (fCreatedPointCount == 2 && isClick(aAWTEvent)) {
      updateModel(RouteState.COMPUTING, null);

      // Start computing the route if we have just set the second point
      fFuture = scheduleRouteComputation(view);

      // Restore the default controller
      Map<ILspView, ILspController> view2controller = (Map<ILspView, ILspController>) Framework.getInstance().getSharedValue("view.default.controllers");
      view.setController(view2controller.get(view));

      return null;
    }

    return aAWTEvent;
  }

  private Future scheduleRouteComputation(final ILspView aView) {
    RouteComputationTask task = new RouteComputationTask(aView);
    Future future = aView.getServices().getTaskExecutor().execute(new TLspTaskExecutorRunnable(this, task, true));
    task.setFuture(future);
    return future;
  }

  private void updateModel(RouteState aState, ILcd2DEditablePointList aNewRoutePointList) {
    try (Lock autoUnlock = writeLock(fModel)) {
      int numPoints = fCreatedPointCount + (fCreatingPoint ? 1 : 0);
      if (numPoints == 0) {
        fModel.removeAllElements(ILcdModel.FIRE_LATER);
        return;
      }

      for (int i = 0; i < fPoints.length; i++) {
        boolean inModel = fModel.contains(fPoints[i]);
        updateModelElement(inModel, i < numPoints, fPoints[i]);
      }
      boolean routeInModel = fModel.contains(fRoute);
      boolean routeShouldBeInModel = numPoints >= 2;
      if (routeShouldBeInModel) {
        fRoute.setState(aState);
        if (aNewRoutePointList != null) {
          fRoute.set2DEditablePointList(aNewRoutePointList);
        } else {
          fRoute.set2DEditablePointList(createPointList(fPoints[0], fPoints[1]));
        }
      }
      updateModelElement(routeInModel, routeShouldBeInModel, fRoute);
    }
    fModel.fireCollectedModelChanges();
  }

  private void updateModelElement(boolean aInModel, boolean aShouldBeInModel, Object aElement) {
    if (aShouldBeInModel && !aInModel) {
      fModel.addElement(aElement, ILcdModel.FIRE_LATER);
    } else if (!aShouldBeInModel && aInModel) {
      fModel.removeElement(aElement, ILcdModel.FIRE_LATER);
    } else if (aShouldBeInModel && aInModel) {
      fModel.elementChanged(aElement, ILcdModel.FIRE_LATER);
    }
  }

  private boolean isClick(AWTEvent aAWTEvent) {
    return aAWTEvent.getID() == MouseEvent.MOUSE_CLICKED;
  }

  private boolean isMove(AWTEvent aAWTEvent) {
    return aAWTEvent.getID() == MouseEvent.MOUSE_MOVED;
  }

  public ILspInteractivePaintableLayer getLayer() {
    return fLayer;
  }

  private static ILcd2DEditablePointList convertRouteToPointList(ILcdRoute<ILcdPoint, ILcdPolyline> route) {
    List<ILcdPoint> points = new ArrayList<ILcdPoint>(route.getNodeCount());
    for (int i = 0; i < route.getNodeCount(); i++) {
      points.add(route.getNode(i));
    }
    // 3 steps of simple fairing to hide staircasing effect of path planning algorithm.
    List<ILcdPoint> smoothed = smooth(smooth(smooth(points)));
    return new TLcd2DEditablePointList(smoothed.toArray(new ILcd2DEditablePoint[smoothed.size()]), false);
  }

  private static ILcd2DEditablePointList createPointList(ILcdPoint aP0, ILcdPoint aP1) {
    final ILcd3DEditablePoint p0 = aP0.cloneAs3DEditablePoint();
    p0.move3D(p0.getX(), p0.getY(), 0);
    final ILcd3DEditablePoint p1 = aP1.cloneAs3DEditablePoint();
    p1.move3D(p1.getX(), p1.getY(), 0);
    return new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{p0, p1}, false);
  }

  private static List<ILcdPoint> smooth(List<ILcdPoint> aPoints) {
    ArrayList<ILcdPoint> result = new ArrayList<ILcdPoint>(aPoints.size());
    ILcd3DEditablePoint p0 = aPoints.get(0).cloneAs3DEditablePoint();
    p0.move3D(p0.getX(), p0.getY(), 0);

    result.add(p0);
    for (int i = 1; i < aPoints.size() - 1; i++) {
      ILcdPoint a = aPoints.get(i - 1);
      ILcdPoint b = aPoints.get(i);
      ILcdPoint c = aPoints.get(i + 1);

      double x = 0.5 * (a.getX() + c.getX());
      double y = 0.5 * (a.getY() + c.getY());

      ILcd3DEditablePoint newB = b.cloneAs3DEditablePoint();

      if (i % 20 != 0) {
        newB.move3D(0.5 * newB.getX() + 0.5 * x, 0.5 * newB.getY() + 0.5 * y, 0);
      } else {
        newB.move3D(newB.getX(), newB.getY(), 0);
      }
      result.add(newB);
    }
    p0 = aPoints.get(aPoints.size() - 1).cloneAs3DEditablePoint();
    p0.move3D(p0.getX(), p0.getY(), 0);

    result.add(p0);
    return result;
  }

  /**
   * The state of a route.
   */
  private enum RouteState {
    /**
     * The route is being created.
     */
    CREATING,
    /**
     * The route is being computed.
     */
    COMPUTING,
    /**
     * The shortest route solution.
     */
    SOLUTION,
    /**
     * No route was found.
     */
    NO_SOLUTION
  }

  /**
   * A route.
   */
  private static class Route extends TLcdXYPolyline {

    private RouteState fState = RouteState.CREATING;

    public Route() {
    }

    public RouteState getState() {
      return fState;
    }

    public void setState(RouteState aState) {
      fState = aState;
    }
  }

  /**
   * Styler for the routes.
   */
  private static class RouteStyler extends ALspStyler {
    private final Object fAnimationKey = new Object();
    private LoopingStyleAnimation fAnimation;

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        if (o instanceof ILcdPoint) {
          aStyleCollector.object(o).styles(
              TLspIconStyle.newBuilder().build(),
              TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, 5).build()
          ).submit();
        } else if (o instanceof Route) {
          TLspLineStyle.Builder<?> lineStyleBuilder = TLspLineStyle.newBuilder().width(2);
          switch (((Route) o).getState()) {
          case CREATING:
            stopAnimation();
            lineStyleBuilder.opacity(.5f).dashPattern(dashPattern());
            break;
          case COMPUTING:
            ensureAnimation(aContext);
            lineStyleBuilder.opacity(.5f).dashPattern(rotatedDashPattern(fAnimation.getFraction()));
            break;
          case NO_SOLUTION:
            stopAnimation();
            lineStyleBuilder.color(Color.RED).dashPattern(dashPattern());
            break;
          case SOLUTION:
            stopAnimation();
            break;
          }
          aStyleCollector.object(o).style(lineStyleBuilder.build()).submit();
        }
      }
    }

    private void ensureAnimation(TLspContext aContext) {
      if (fAnimation == null) {
        fAnimation = new LoopingStyleAnimation(5.0, aContext.getView(), this, fAnimationKey);
        ALcdAnimationManager.getInstance().putAnimation(fAnimationKey, fAnimation);
      }
    }

    private void stopAnimation() {
      if (fAnimation != null) {
        ALcdAnimationManager.getInstance().removeAnimation(fAnimationKey);
        fAnimation = null;
      }
    }

    private static final TLspLineStyle.DashPattern[] DASH_PATTERNS;

    static {
      DASH_PATTERNS = new TLspLineStyle.DashPattern[16];
      for (int i = 0; i < DASH_PATTERNS.length; i++) {
        int pattern = (TLspLineStyle.DashPattern.LONG_DASH & 0xFFFF) << i;
        pattern = pattern | (pattern >> 16);
        DASH_PATTERNS[i] = new TLspLineStyle.DashPattern((short) (pattern & 0xFFFF), 2);
      }
    }

    private static TLspLineStyle.DashPattern dashPattern() {
      return DASH_PATTERNS[0];
    }

    private static TLspLineStyle.DashPattern rotatedDashPattern(float aRotateFraction) {
      return DASH_PATTERNS[(Math.round(aRotateFraction * DASH_PATTERNS.length) % DASH_PATTERNS.length)];
    }
  }

  /**
   * Animation that loops infinitely.
   */
  private static class LoopingStyleAnimation extends ALcdAnimation {
    private final WeakReference<ALspStyler> fStylerRef;
    private final Object fAnimationKey;
    private double fTime = 0.0;

    public LoopingStyleAnimation(double aDuration, ILcdView aView, ALspStyler aStyler, Object aAnimationKey) {
      super(aDuration, aView);
      fAnimationKey = aAnimationKey;
      fStylerRef = new WeakReference<ALspStyler>(aStyler);
    }

    public float getFraction() {
      return (float) (fTime / getDuration());
    }

    @Override
    protected void setTimeImpl(double aTime) {
      fTime = aTime;
      ALspStyler styler = fStylerRef.get();
      if (styler != null) {
        // Fire a style change event
        styler.fireStyleChangeEvent();
      } else {
        // Cancel the animation
        ALcdAnimationManager.getInstance().removeAnimation(fAnimationKey);
      }
    }

    @Override
    public boolean isLoop() {
      return true;
    }
  }

  private class RouteComputationTask implements Runnable {
    private final ILspView fView;
    private Future fFuture;

    public RouteComputationTask(ILspView aView) {
      fView = aView;
    }

    public void setFuture(Future aFuture) {
      fFuture = aFuture;
    }

    @Override
    public void run() {
      ILcdRoute<ILcdPoint, ILcdPolyline> route = CrossCountryPathPlanningSupport.getShortestRoute(fPoints[0], fPoints[1], fReference, fView);
      ILcd2DEditablePointList pointList = null;
      if (route != null) {
        pointList = convertRouteToPointList(route);
      }
      if (fFuture == null || !fFuture.isCancelled()) {
        updateModel(pointList != null ? RouteState.SOLUTION : RouteState.NO_SOLUTION, pointList);
      }
    }
  }
}
