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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ILcdViewInvalidationListener;
import com.luciad.view.TLcdViewInvalidationEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYLabelObstacle;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;
import com.luciad.view.map.TLcdMapJPanel;

/**
 * This class calculates label locations by spreading them on concentric circles.
 * This can be used to interactively declutter a group of objects.
 * The class could be modified to calculate other locations, for example, on a line or
 * on a square.
 */
class ConcentricCircleLabelLocationsCalculator {

  public static final double SPREAD = 35.0d;

  private Map<Object, Double> fObjectAngles = new HashMap<Object, Double>();
  private List<TLcdCollectedLabeledObjectInfo> fObjectsToDeclutter;
  private Point2D fClusterCenter;

  public ConcentricCircleLabelLocationsCalculator(TLcdMapJPanel aView) {
    aView.addViewInvalidationListener(new ILcdViewInvalidationListener() {
      public void viewInvalidated(TLcdViewInvalidationEvent aEvent) {
        // When the view changes (e.g. due to panning the view), the cache is invalid.
        invalidateCache();
      }
    });
  }

  /**
   * Adjusts the given aPointSFCT to point to the preferred location of the given label.
   *
   * @param aLabel         The given label.
   * @param aGXYView       The gxy view.
   * @param aGraphics      The Graphics instance on which the label will be painted.
   * @param aPointSFCT     The point whose location should be adjusted to reflect the preferred
   *                       location.
   *
   * @return true if this calculator was able to calculate the preferred location of the given
   *         label, false if it did not know anything about the given label.
   */
  public boolean calculateLabelLocationSFCT(TLcdCollectedLabelInfo aLabel, ILcdGXYView aGXYView, Graphics aGraphics, Point2D aPointSFCT) {
    determineClusterCenterAndPreferredAnglesIfNecessary(aGXYView, aGraphics);
    Double theta = fObjectAngles.get(aLabel.getLabelIdentifier().getDomainObject());
    if (theta == null) {
      return false;
    }
    double x = SPREAD * Math.cos(theta) * (aLabel.getLabelIdentifier().getLabelIndex() + 1);
    double y = SPREAD * Math.sin(theta) * (aLabel.getLabelIdentifier().getLabelIndex() + 1);
    aPointSFCT.setLocation(x, y);
    return true;
  }

  /**
   * Adjusts this calculator to determine the preferred locations for the new given List of
   * labeled objects.
   *
   * @param aObjectsToDeclutter The java.util.List containing the labeled objects whose labels
   *                            should be decluttered.
   */
  public void setObjectsToDeclutter(List<TLcdCollectedLabeledObjectInfo> aObjectsToDeclutter) {
    fObjectsToDeclutter = aObjectsToDeclutter;
    invalidateCache();
  }

  /**
   * Determines the preferred angles for the labels. For each labeled object in
   * aObjectsToDeclutter the angle is calculated relative to the center of all labeled objects
   * in aObjectsToDeclutter. This information can then be used in retrieveDesiredLabelLocation
   * to position the labels in circles.
   *
   * @param aGXYView  The gxy view.
   * @param aGraphics The graphics to use for calculations.
   */
  private void determineClusterCenterAndPreferredAnglesIfNecessary(ILcdGXYView aGXYView, Graphics aGraphics) {
    if (!isCacheValid()) {
      List<Point> anchor_points = retrieveAnchorPointsOfDomainObjects(fObjectsToDeclutter, aGXYView, aGraphics);
      fClusterCenter = determineClusterCenter(anchor_points);
      fObjectAngles = determinePreferredAngles(fObjectsToDeclutter, anchor_points, fClusterCenter);
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
   * @param aGXYView           The gxy view.
   * @param aGraphics          The graphics to use for calculations.
   * @param aLabelObstacleSFCT The TLcdGXYLabelObstacle to updated.
   */
  public void updateLabelObstacleSFCT(ILcdGXYView aGXYView, Graphics aGraphics, TLcdGXYLabelObstacle aLabelObstacleSFCT) {
    determineClusterCenterAndPreferredAnglesIfNecessary(aGXYView, aGraphics);
    int halfSize = (int) (SPREAD);

    int x = (int) Math.rint(fClusterCenter.getX());
    int y = (int) Math.rint(fClusterCenter.getY());
    aLabelObstacleSFCT.setX(x - halfSize);
    aLabelObstacleSFCT.setY(y - halfSize);
    aLabelObstacleSFCT.setHeight(halfSize * 2);
    aLabelObstacleSFCT.setWidth(halfSize * 2);
  }

  private Map<Object, Double> determinePreferredAngles(List<TLcdCollectedLabeledObjectInfo> aObjectsToDeclutter, List<Point> aAnchorPoints, Point2D aCenterPoint) {
    double center_x = aCenterPoint.getX();
    double center_y = aCenterPoint.getY();

    //then store the view-angle of the domain object relative to this weighted center point
    Map<Object, Double> preferredLocations = new HashMap<Object, Double>();
    preferredLocations.clear();
    Iterator<TLcdCollectedLabeledObjectInfo> objects = aObjectsToDeclutter.iterator();
    Iterator<Point> anchors = aAnchorPoints.iterator();
    while (objects.hasNext()) {
      Object domain_object = objects.next().getDomainObject();
      Point anchor = anchors.next();
      double theta = Math.atan2((double) anchor.y - center_y, (double) anchor.x - center_x);
      preferredLocations.put(domain_object, theta);
    }
    return preferredLocations;
  }

  private List<Point> retrieveAnchorPointsOfDomainObjects(List<TLcdCollectedLabeledObjectInfo> aObjectsToDeclutter, ILcdGXYView aGXYView, Graphics aGraphics) {
    TLcdGXYContext context = new TLcdGXYContext();

    List<Point> anchor_points = new ArrayList<Point>();
    for (TLcdCollectedLabeledObjectInfo labeled_object : aObjectsToDeclutter) {
      ILcdGXYLayer layer = (ILcdGXYLayer) labeled_object.getLayer();
      if (context.getGXYLayer() != layer) {
        context.resetFor(layer, aGXYView);
      }

      try {
        Object domain_object = labeled_object.getDomainObject();
        Point anchor = new Point();
        layer.getGXYPainter(domain_object).anchorPointSFCT(aGraphics, ILcdGXYPainter.DEFAULT | ILcdGXYPainter.BODY, context, anchor);
        anchor_points.add(anchor);
      } catch (TLcdNoBoundsException e) {
        //ignore, the point will not be taken into account
      }
    }
    return anchor_points;
  }

  private Point2D determineClusterCenter(List<Point> aAnchor_points) {
    double center_x = 0;
    double center_y = 0;
    for (ListIterator<Point> iterator = aAnchor_points.listIterator(); iterator.hasNext(); ) {
      int i = iterator.nextIndex() + 1;
      Point anchor_point = iterator.next();
      center_x += (anchor_point.getX() - center_x) / i;
      center_y += (anchor_point.getY() - center_y) / i;
    }

    Point2D center_point = new Point2D.Double();
    center_point.setLocation(center_x, center_y);
    return center_point;
  }
}
