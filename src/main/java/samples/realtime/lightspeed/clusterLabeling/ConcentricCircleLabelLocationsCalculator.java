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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.label.TLspLabelObstacle;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * This class calculates label locations by spreading them on concentric circles.
 * This can be used to interactively declutter a group of objects.
 * The class could be modified to calculate other locations, for example, on a line or
 * on a square.
 */
class ConcentricCircleLabelLocationsCalculator {

  public static final double SPREAD = 35.0;

  private Map<Object, Double> fObjectAngles = new HashMap<Object, Double>();
  private List<TLspDomainObjectContext> fObjectsToDeclutter;
  private ILcdPoint fClusterCenter;

  private final InvalidateCachePropertyChangeListener fInvalidateListener = new InvalidateCachePropertyChangeListener();

  public ConcentricCircleLabelLocationsCalculator() {
  }

  /**
   * Adjusts the given aPointSFCT to point to the preferred location of the given label.
   *
   * @param aLabel         The given label.
   * @param aView          The view.
   * @param aPointSFCT     The point whose location should be adjusted to reflect the preferred
   *                       location.
   *
   * @return true if this calculator was able to calculate the preferred location of the given
   *         label, false if it did not know anything about the given label.
   */
  public boolean calculateLabelLocationSFCT(TLspLabelID aLabel, ILspView aView, ILcd2DEditablePoint aPointSFCT) {
    return calculateLabelLocationSFCT(aLabel, aView, SPREAD, aPointSFCT);
  }

  /**
   * Adjusts the given aPointSFCT to point to the preferred location of the given label, with a given spread. This
   * method can be used to assign an initial label location to a label that wasn't visible before.
   *
   * @param aLabel         The given label.
   * @param aView          The view.
   * @param aPointSFCT     The point whose location should be adjusted to reflect the preferred
   *                       location.
   *
   * @return true if this calculator was able to calculate the preferred location of the given
   *         label, false if it did not know anything about the given label.
   */
  public boolean calculateLabelLocationSFCT(TLspLabelID aLabel, ILspView aView, double aSpread, ILcd2DEditablePoint aPointSFCT) {
    determineClusterCenterAndPreferredAnglesIfNecessary(aView);
    Double theta = fObjectAngles.get(aLabel.getDomainObject());
    if (theta == null) {
      return false;
    }
    double x = aSpread * Math.cos(theta);
    double y = aSpread * Math.sin(theta);
    aPointSFCT.move2D(x, y);
    return true;
  }

  /**
   * Adjusts this calculator to determine the preferred locations for the new given List of
   * labeled objects.
   *
   * @param aObjectsToDeclutter The java.util.List containing the labeled objects whose labels
   *                            should be decluttered.
   */
  public void setObjectsToDeclutter(List<TLspDomainObjectContext> aObjectsToDeclutter, ILspView aView) {
    if (aObjectsToDeclutter == null) {
      aView.getViewXYZWorldTransformation().removePropertyChangeListener(fInvalidateListener);
    } else {
      aView.getViewXYZWorldTransformation().addPropertyChangeListener(fInvalidateListener);
    }
    fObjectsToDeclutter = aObjectsToDeclutter;
    invalidateCache();
  }

  /**
   * Determines the preferred angles for the labels. For each labeled object in
   * aObjectsToDeclutter the angle is calculated relative to the center of all labeled objects
   * in aObjectsToDeclutter. This information can then be used in retrieveDesiredLabelLocation
   * to position the labels in circles.
   *
   * @param aView  The view.
   */
  private void determineClusterCenterAndPreferredAnglesIfNecessary(ILspView aView) {
    if (!isCacheValid()) {
      List<ILcdPoint> anchorPoints = retrieveAnchorPointsOfDomainObjects(fObjectsToDeclutter, aView);
      fClusterCenter = determineClusterCenter(anchorPoints);
      fObjectAngles = determinePreferredAngles(fObjectsToDeclutter, anchorPoints, fClusterCenter);
    }
  }

  private boolean isCacheValid() {
    return fObjectAngles != null;
  }

  private void invalidateCache() {
    fObjectAngles = null;
  }

  /**
   * Updates the given label obstacle to cover an area around the center of the cluster of domain objects being covered.
   *
   * @param aView              The view.
   * @param aLabelObstacleSFCT The obstacle to updated.
   */
  public void updateLabelObstacleSFCT(ILspView aView, TLspLabelObstacle aLabelObstacleSFCT) {
    determineClusterCenterAndPreferredAnglesIfNecessary(aView);
    int halfSize = (int) (SPREAD);

    int x = (int) Math.rint(fClusterCenter.getX());
    int y = (int) Math.rint(fClusterCenter.getY());
    aLabelObstacleSFCT.setX(x - halfSize);
    aLabelObstacleSFCT.setY(y - halfSize);
    aLabelObstacleSFCT.setHeight(halfSize * 2);
    aLabelObstacleSFCT.setWidth(halfSize * 2);
  }

  private Map<Object, Double> determinePreferredAngles(List<TLspDomainObjectContext> aObjectsToDeclutter, List<ILcdPoint> aAnchorPoints, ILcdPoint aCenterPoint) {
    double centerX = aCenterPoint.getX();
    double centerY = aCenterPoint.getY();

    //then store the view-angle of the domain object relative to this weighted center point
    Map<Object, Double> preferredLocations = new HashMap<Object, Double>();
    preferredLocations.clear();
    Iterator<TLspDomainObjectContext> objects = aObjectsToDeclutter.iterator();
    Iterator<ILcdPoint> anchors = aAnchorPoints.iterator();
    while (objects.hasNext()) {
      Object domainObject = objects.next().getDomainObject();
      ILcdPoint anchor = anchors.next();
      double theta = Math.atan2(anchor.getY() - centerY, anchor.getX() - centerX);
      preferredLocations.put(domainObject, theta);
    }
    return preferredLocations;
  }

  private List<ILcdPoint> retrieveAnchorPointsOfDomainObjects(List<TLspDomainObjectContext> aObjectsToDeclutter, ILspView aView) {
    final TLspContext context = new TLspContext();

    List<ILcdPoint> anchorPoints = new ArrayList<ILcdPoint>();
    for (final TLspDomainObjectContext labeledObject : aObjectsToDeclutter) {
      ILspInteractivePaintableLayer layer = labeledObject.getLayer();
      if (context.getLayer() != layer) {
        context.resetFor(layer, aView);
      }

      try {
        ILcdPoint focusPoint = retrieveObjectAnchor(labeledObject, context);

        ILcdModelXYZWorldTransformation m2w = context.getModelXYZWorldTransformation();
        ILcd3DEditablePoint worldPoint = new TLcdXYZPoint();
        m2w.modelPoint2worldSFCT(focusPoint, worldPoint);

        ALspViewXYZWorldTransformation v2w = context.getViewXYZWorldTransformation();
        ILcd3DEditablePoint viewPoint = new TLcdXYZPoint();
        v2w.worldPoint2ViewSFCT(worldPoint, viewPoint);

        anchorPoints.add(viewPoint);
      } catch (TLcdOutOfBoundsException e) {
        //ignore, the point will not be taken into account
      }
    }
    return anchorPoints;
  }

  private static ILcdPoint retrieveObjectAnchor(final TLspDomainObjectContext labeledObject, final TLspContext aContext) {
    ILspInteractivePaintableLayer layer = labeledObject.getLayer();
    List<Object> objects = Collections.singletonList(labeledObject.getObject());

    ILspStyler styler = layer.getStyler(labeledObject.getPaintRepresentationState());
    AnchorPointCollector anchorPointCollector = new AnchorPointCollector(objects, labeledObject, aContext);
    styler.style(objects, anchorPointCollector, aContext);

    return anchorPointCollector.getAnchorPoint();
  }

  private static ILcdPoint extractPoint(Object aObject) {
    if (aObject instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aObject;
      return extractPoint(shapeList.getShape(0));
    } else if (aObject instanceof ILcdShape) {
      ILcdShape shape = (ILcdShape) aObject;
      return shape.getFocusPoint();
    } else {
      return null;
    }
  }

  private ILcdPoint determineClusterCenter(List<ILcdPoint> aAnchorPoints) {
    double centerX = 0;
    double centerY = 0;
    for (ListIterator<ILcdPoint> iterator = aAnchorPoints.listIterator(); iterator.hasNext(); ) {
      int i = iterator.nextIndex() + 1;
      ILcdPoint anchorPoint = iterator.next();
      centerX += (anchorPoint.getX() - centerX) / i;
      centerY += (anchorPoint.getY() - centerY) / i;
    }

    return new TLcdXYPoint(centerX, centerY);
  }

  private static class AnchorPointCollector extends ALspStyleCollector {
    private final TLspDomainObjectContext fLabeledObject;
    private final TLspContext fContext;

    private ILcdPoint fAnchorPoint;

    public AnchorPointCollector(List<Object> aObjects, TLspDomainObjectContext aLabeledObject, TLspContext aContext) {
      super(aObjects);
      fLabeledObject = aLabeledObject;
      fContext = aContext;
    }

    @Override
    protected void submitImpl() {
      ALspStyleTargetProvider styleTargetProvider = getStyleTargetProvider();
      ArrayList<Object> geometry = new ArrayList<Object>();
      if (styleTargetProvider != null) {
        styleTargetProvider.getStyleTargetsSFCT(fLabeledObject.getObject(), fContext, geometry);
      } else {
        geometry.add(fLabeledObject.getObject());
      }

      for (Object geom : geometry) {
        ILcdPoint point = extractPoint(geom);
        if (point != null) {
          fAnchorPoint = point;
          break;
        }
      }
    }

    public ILcdPoint getAnchorPoint() {
      return fAnchorPoint;
    }
  }

  private class InvalidateCachePropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      // When the view changes (e.g. due to panning the view), the cache is invalid.
      invalidateCache();
    }
  }
}
